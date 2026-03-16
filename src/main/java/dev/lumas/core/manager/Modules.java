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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Modules {

    @Getter
    private final ModuleContext context;
    private final Map<Autowire, RegisterHandler<?>> handlers = new EnumMap<>(Autowire.class);
    private final List<Object> modules = new ArrayList<>();

    public Modules(Plugin caller) {
        this.context = new ModuleContext(caller, "lumacore");

        CommandHandler commandHandler = new CommandHandler();
        addHandler(Autowire.LISTENER, new ListenerHandler());
        addHandler(Autowire.COMMAND, commandHandler);
        addHandler(Autowire.SUBCOMMAND, new SubCommandHandler(commandHandler));
        addHandler(Autowire.PLACEHOLDER, new PlaceholderHandler());
        addHandler(Autowire.SERVICE, new ServiceHandler());
    }

    public <T> void addHandler(Autowire type, RegisterHandler<T> handler) {
        handlers.put(type, handler);
    }

    public void register() {
        Set<Class<?>> classes = Reflect.from(context.plugin().getClass()).scan();
        register(classes);
    }

    public void register(Set<Class<?>> classes) {
        for (Class<?> aClass : classes) {
            if (aClass.isInterface() || Modifier.isAbstract(aClass.getModifiers())) continue;

            RegisterAnnotation annotation = Annotations.getRegisterAnnotation(aClass);
            if (annotation == null) continue;
            if (!annotation.requires().isBlank() && !requiredExists(annotation.requires())) continue;

            try {
                Constructor<?> constructor = aClass.getConstructor();
                if (Modifier.isPrivate(constructor.getModifiers())) continue;

                Object instance = constructor.newInstance();
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
