package dev.lumas.core.model.command;

import dev.lumas.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NullMarked
public abstract class AbstractCommandManager<P extends JavaPlugin, T extends AbstractSubCommand<P>> extends AbstractCommand {

    protected final Map<String, T> subCommands = new HashMap<>();
    protected final P plugin;

    protected AbstractCommandManager(P plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean handle(CommandSender sender, String label, String[] args) {
        if (args.length == 0 || !subCommands.containsKey(args[0])) {
            return false;
        }

        T subCommand = subCommands.get(args[0]);
        if (subCommand.playerOnly() && !(sender instanceof Player)) {
            return false;
        } else if (!subCommand.permission().isEmpty() && !sender.hasPermission(subCommand.permission())) {
            return false;
        }

        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        if (!subCommand.execute(plugin, sender, label, newArgs)) {
            Text.msg(sender, "Invalid usage. Usage: " + subCommand.usage(label));
        }
        return true;
    }


    @Override
    public @Nullable List<String> handleTabComplete(CommandSender sender, String label, String[] args) {
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
        for (String alias : abstractSubCommand.aliases()) {
            subCommands.put(alias, (T) abstractSubCommand);
        }
    }

    public void removeSubCommand(AbstractSubCommand<?> abstractSubCommand) {
        subCommands.remove(abstractSubCommand.name());
        for (String alias : abstractSubCommand.aliases()) {
            subCommands.remove(alias);
        }
    }
}
