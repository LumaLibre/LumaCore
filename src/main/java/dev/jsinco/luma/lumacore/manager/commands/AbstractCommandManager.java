package dev.jsinco.luma.lumacore.manager.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommandManager<P extends JavaPlugin, T extends AbstractSubCommand<P>> extends AbstractCommand {

    protected Map<String, T> subCommands = new HashMap<>();
    protected final P plugin;

    protected AbstractCommandManager(P plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean handle(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        if (args.length == 0 || !subCommands.containsKey(args[0])) {
            return false;
        }

        T subCommand = subCommands.get(args[0]);
        if (subCommand.playerOnly() && !(sender instanceof Player)) {
            return false;
        } else if (subCommand.permission() != null && !sender.hasPermission(subCommand.permission())) {
            return false;
        }

        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        if (!subCommand.execute(plugin, sender, label, newArgs)) {
            sender.sendMessage("Invalid usage. Usage: " + subCommand.info().usage());
        }
        return true;
    }


    @Override
    public @Nullable List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        if (args.length == 0) {
            return null;
        } else if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(s -> sender.hasPermission(subCommands.get(s).permission()))
                    .toList();
        }

        T subCommand = subCommands.get(args[0]);
        if (subCommand != null) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            return subCommand.tabComplete(plugin, sender, newArgs);
        }
        return null;
    }

    public void addSubCommand(AbstractSubCommand<?> abstractSubCommand) {
        subCommands.put(abstractSubCommand.name(), (T) abstractSubCommand);
        for (String alias : abstractSubCommand.info().aliases()) {
            subCommands.put(alias, (T) abstractSubCommand);
        }
    }

    public void removeSubCommand(AbstractSubCommand<?> abstractSubCommand) {
        subCommands.remove(abstractSubCommand.name());
        for (String alias : abstractSubCommand.info().aliases()) {
            subCommands.remove(alias);
        }
    }
}
