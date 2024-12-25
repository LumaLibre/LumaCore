package dev.jsinco.luma.manager.commands;

import dev.jsinco.luma.manager.modules.LumaModule;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommandManager extends BukkitCommand implements LumaModule {

    protected Map<String, AbstractSubCommand> subCommands = new HashMap<>();

    protected AbstractCommandManager(@NotNull String name) {
        super(name);
    }

    protected AbstractCommandManager(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }

    public void addSubCommand(AbstractSubCommand abstractSubCommand) {
        subCommands.put(abstractSubCommand.name(), abstractSubCommand);
        for (String alias : abstractSubCommand.info().aliases()) {
            subCommands.put(alias, abstractSubCommand);
        }
    }

    public void removeSubCommand(AbstractSubCommand abstractSubCommand) {
        subCommands.remove(abstractSubCommand.name());
        for (String alias : abstractSubCommand.info().aliases()) {
            subCommands.remove(alias);
        }
    }
}
