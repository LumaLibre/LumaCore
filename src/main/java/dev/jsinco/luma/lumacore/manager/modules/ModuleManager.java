package dev.jsinco.luma.lumacore.manager.modules;

import dev.jsinco.luma.lumacore.manager.commands.AbstractCommandManager;
import dev.jsinco.luma.lumacore.manager.commands.AbstractSubCommand;
import dev.jsinco.luma.lumacore.reflect.ReflectionUtil;
import dev.jsinco.luma.lumacore.utility.Logging;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModuleManager {

    private final String fallbackPrefix = "lumacore";
    private final List<Object> modules = new ArrayList<>();
    private final Map<AbstractCommandManager<?, ?>, List<AbstractSubCommand<?>>> mappedCommands = new HashMap<>();
    private final JavaPlugin caller;

    public ModuleManager(JavaPlugin caller) {
        this.caller = caller;
        //this.fallbackPrefix = caller.getName().toLowerCase();
    }

    public void reflectivelyRegisterModules() {
        Set<Class<?>> classes = ReflectionUtil.of(caller.getClass()).getAllClassesFor();
        List<AbstractSubCommand<?>> queuedSubCommands = new LinkedList<>();

        for (Class<?> aClass : classes) {
            if (aClass.isInterface() || Modifier.isAbstract(aClass.getModifiers()) || !aClass.isAnnotationPresent(AutoRegister.class)) {
                continue;
            }

            try {
                Constructor<?> constructor = aClass.getConstructor();
                if (Modifier.isPrivate(constructor.getModifiers())) {
                    continue;
                }

                List<RegisterType> types = List.of(aClass.getAnnotation(AutoRegister.class).value());
                Object instance = constructor.newInstance();
                modules.add(instance);

                if (types.contains(RegisterType.LISTENER) && instance instanceof Listener listener) {
                    registerForBukkitListener(listener);
                }

                if (types.contains(RegisterType.COMMAND) && instance instanceof BukkitCommand bukkitCommand) {
                    registerForBukkitCommand(bukkitCommand);
                }

                if (types.contains(RegisterType.SUBCOMMAND) && instance instanceof AbstractSubCommand<?> abstractSubCommand) {
                    queuedSubCommands.add(abstractSubCommand);
                }

            } catch (NoSuchMethodException ignored) {
                // If the class doesn't have a no-args constructor, has to be registered manually
                Logging.warningLog("Class " + aClass.getCanonicalName() + " does not have a no-args constructor, skipping.");
            } catch (Exception e) {
                Logging.errorLog("An error occurred while registering a class reflectively: " + aClass.getCanonicalName(), e);
            }
        }


        for (AbstractSubCommand<?> abstractSubCommand : queuedSubCommands) {
            AbstractCommandManager<?, ?> parent = mappedCommands.keySet().stream()
                    .filter(it -> {
                        Class<?> parentClass = abstractSubCommand.parent();
                        return parentClass.isInstance(it);
                    }).findFirst().orElse(null);
            if (parent == null) {
                Logging.warningLog("Could not find parent CommandManager class for: " + abstractSubCommand.getClass().getSimpleName());
                continue;
            }

            parent.addSubCommand(abstractSubCommand);
        }

        Logging.log("Finished registering classes/modules reflectively! (" + modules.size() + ")");
    }

    public void unregisterModules() {
        for (Object module : modules) {
            List<RegisterType> types = List.of(module.getClass().getAnnotation(AutoRegister.class).value());
            if (types.contains(RegisterType.LISTENER) && module instanceof Listener listener) {
                unregisterForBukkitListener(listener);
            }
            if (types.contains(RegisterType.COMMAND) && module instanceof BukkitCommand bukkitCommand) {
                unregisterForBukkitCommand(bukkitCommand);
            }
//            if (types.contains(RegisterType.SUBCOMMAND) && module instanceof AbstractSubCommand abstractSubCommand) {
//                unregisterForSubcommand(abstractSubCommand);
//            }
        }
    }


    private void registerForBukkitListener(Listener listener) {
        caller.getServer().getPluginManager().registerEvents(listener, caller);
    }

    private void unregisterForBukkitListener(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    private void registerForBukkitCommand(BukkitCommand bukkitCommand) {
        CommandMap commandMap = Bukkit.getCommandMap();
        commandMap.register(bukkitCommand.getLabel(), fallbackPrefix, bukkitCommand);

        if (bukkitCommand instanceof AbstractCommandManager<?, ?> commandManager) {
            mappedCommands.put(commandManager, new ArrayList<>());
        }
    }


    private void unregisterForBukkitCommand(BukkitCommand bukkitCommand) {
        CommandMap commandMap = Bukkit.getCommandMap();
        commandMap.getKnownCommands().remove(fallbackPrefix + ":" + bukkitCommand.getLabel());
        for (String alias : bukkitCommand.getAliases()) {
            commandMap.getKnownCommands().remove(fallbackPrefix + ":" + alias);
        }
    }
}

