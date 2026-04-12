package dev.lumas.core.manager;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.model.ModuleContext;
import dev.lumas.core.model.internal.RegisterHandler;
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
import org.jspecify.annotations.NullMarked;

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
                Object instance = null;

                // Check for Kotlin object singleton (static INSTANCE field)
                try {
                    Field instanceField = aClass.getDeclaredField("INSTANCE");
                    if (Modifier.isStatic(instanceField.getModifiers()) && aClass.isAssignableFrom(instanceField.getType())) {
                        instanceField.setAccessible(true);
                        instance = instanceField.get(null);
                    }
                } catch (NoSuchFieldException ignored) {
                    // Not a Kotlin object, fall through to constructor
                }

                // Fall back to no-args constructor
                if (instance == null) {
                    Constructor<?> constructor = aClass.getConstructor();
                    //if (Modifier.isPrivate(constructor.getModifiers())) continue;
                    instance = constructor.newInstance();
                }

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
}
