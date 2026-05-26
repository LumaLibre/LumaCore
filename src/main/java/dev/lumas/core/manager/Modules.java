package dev.lumas.core.manager;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.Provided;
import dev.lumas.core.model.ModuleContext;
import dev.lumas.core.model.internal.RegisterHandler;
import dev.lumas.core.model.internal.handlers.BrigadierCommandHandler;
import dev.lumas.core.model.internal.handlers.CommandHandler;
import dev.lumas.core.model.internal.handlers.ListenerHandler;
import dev.lumas.core.model.internal.handlers.PlaceholderHandler;
import dev.lumas.core.model.internal.handlers.ServiceHandler;
import dev.lumas.core.model.internal.handlers.SubCommandHandler;
import dev.lumas.core.model.internal.register.RegisterAnnotation;
import dev.lumas.core.util.Annotations;
import dev.lumas.core.util.Logging;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A manager for reflectively registering modules based on the presence of the {@link dev.lumas.core.annotation.Register} annotation.
 */
@NullMarked
public class Modules {

    public static final String FALLBACK_PREFIX = "lumacore";

    @Getter
    private final ModuleContext context;
    private final Map<Autowire, RegisterHandler<?>> handlers = new EnumMap<>(Autowire.class);
    private final List<Object> modules = new ArrayList<>();


    public Modules(Plugin caller) {
        this(new ModuleContext(caller, caller.getName().toLowerCase()));
    }

    public Modules(Plugin caller, String fallbackPrefix) {
        this(new ModuleContext(caller, fallbackPrefix));
    }

    public Modules(ModuleContext context) {
        this.context = context;

        CommandHandler commandHandler = new CommandHandler();
        addHandler(Autowire.LISTENER, new ListenerHandler());
        addHandler(Autowire.COMMAND, commandHandler);
        addHandler(Autowire.SUBCOMMAND, new SubCommandHandler(commandHandler));
        addHandler(Autowire.PLACEHOLDER, new PlaceholderHandler());
        addHandler(Autowire.SERVICE, new ServiceHandler());
        addHandler(Autowire.BRIGADIER, new BrigadierCommandHandler());
    }

    /**
     * Registers a handler for a specific Autowire type.
     * This allows you to define custom registration logic for different types of modules.
     * @param type the Autowire type to register the handler for
     * @param handler the handler to register
     * @param <T> the type of module handled by the handler
     */
    public <T> void addHandler(Autowire type, RegisterHandler<T> handler) {
        handlers.put(type, handler);
    }

    /**
     * Scans the plugin's classes for those annotated with @Register and registers them using the appropriate handlers.
     * This should be called during plugin startup.
     */
    public void register() {
        Set<Class<?>> classes = Reflect.from(context.plugin().getClass()).scan();
        register(classes);
    }

    /**
     * Registers the given classes if they are annotated with @Register and meet the requirements specified in the annotation.
     * This should be called during plugin startup.
     * @param classes the classes to register
     */
    public void register(Set<Class<?>> classes) {
        for (Class<?> aClass : classes) {
            if (aClass.isInterface() || Modifier.isAbstract(aClass.getModifiers())) continue;

            RegisterAnnotation annotation = Annotations.getRegisterAnnotation(aClass);
            if (annotation == null) continue;
            if (!annotation.requires().isBlank() && !requiredExists(annotation.requires())) continue;

            try {
                Object instance = resolveInstance(aClass);
                if (instance == null) continue;

                modules.add(instance);

                for (Autowire type : annotation.value()) {
                    tryRegister(type, instance);
                }
            } catch (NoSuchMethodException ignored) {
                Logging.warningLog("No no-args constructor: " + aClass.getCanonicalName());
            } catch (Exception e) {
                Logging.errorLog("Failed to register: " + aClass.getCanonicalName(), e);
            }
        }

        handlers.values().forEach(handler -> handler.postProcess(context));

        Logging.log("Finished registering modules reflectively! (" + modules.size() + ")");
    }
    /**
     * Unregisters all registered modules using the appropriate handlers.
     * This should be called during plugin shutdown to ensure proper cleanup.
     */
    public void unregister() {
        for (Object module : modules) {
            RegisterAnnotation annotation = Annotations.getRegisterAnnotation(module.getClass());
            if (annotation == null) {
                Logging.warningLog("No @Register annotation found on " + module.getClass().getCanonicalName() + " during unregister.");
                continue;
            }
            for (Autowire type : annotation.value()) {
                tryUnregister(type, module);
            }
        }
    }


    @SuppressWarnings("unchecked")
    private <T> void tryRegister(Autowire type, Object instance) {
        RegisterHandler<T> handler = (RegisterHandler<T>) handlers.get(type);
        if (handler != null && handler.accepts(instance)) {
            handler.register((T) instance, context);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void tryUnregister(Autowire type, Object instance) {
        RegisterHandler<T> handler = (RegisterHandler<T>) handlers.get(type);
        if (handler != null && handler.accepts(instance)) {
            handler.unregister((T) instance, context);
        }
    }

    private boolean requiredExists(String string) {
        return Bukkit.getPluginManager().getPlugin(string) != null || Reflect.classExists(string);
    }

    /**
     * Resolves an instance for the given class.
     * <p>
     * Resolution order:
     * <ol>
     *   <li>If the class is annotated with {@link Provided}, read the static field named
     *       by {@link Provided#value()} (default {@code "INSTANCE"}).</li>
     *   <li>Otherwise, if the class is a Kotlin {@code object} (has a static {@code INSTANCE}
     *       field of its own type), use that field.</li>
     *   <li>Otherwise, invoke the no-args constructor.</li>
     * </ol>
     *
     * @param aClass the class to resolve an instance for
     * @return the resolved instance, or {@code null} if a declared singleton field was unusable
     * @throws ReflectiveOperationException if constructor invocation fails
     */
    @ApiStatus.Internal
    public static @Nullable Object resolveInstance(Class<?> aClass) throws ReflectiveOperationException {
        Provided provided = Annotations.getProvidedAnnotation(aClass);
        if (provided != null) {
            Object instance = readStaticField(aClass, provided.value());
            if (instance == null) {
                Logging.warningLog("@Provided class " + aClass.getCanonicalName() + " has no usable static field '" + provided.value() + "'");
            }
            return instance;
        }

        Object kotlinObject = readStaticField(aClass, "INSTANCE");
        if (kotlinObject != null) {
            return kotlinObject;
        }

        Constructor<?> constructor = aClass.getDeclaredConstructor();
        if (Modifier.isPrivate(constructor.getModifiers())) {
            constructor.setAccessible(true);
        }
        return constructor.newInstance();
    }

    /**
     * Attempts to read a static field of the given name whose type is assignable to the declaring class.
     * Returns null if the field doesn't exist, isn't static, or is of an incompatible type.
     */
    private static @Nullable Object readStaticField(Class<?> aClass, String fieldName) {
        try {
            Field field = aClass.getDeclaredField(fieldName);
            if (!Modifier.isStatic(field.getModifiers()) || !aClass.isAssignableFrom(field.getType()))  {
                return null;
            }
            field.setAccessible(true);
            return field.get(null);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            Logging.warningLog("Could not access field '" + fieldName + "' on " + aClass.getCanonicalName());
            return null;
        }
    }
}
