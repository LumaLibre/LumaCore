package dev.lumas.core.model.command;

import dev.lumas.core.model.DelegateHolder;
import dev.lumas.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommandManager<P extends JavaPlugin, T extends AbstractSubCommand<P>> extends AbstractCommand implements DelegateHolder<T> {

    protected final Map<String, T> subCommands = new HashMap<>();
    protected final P plugin;

    protected AbstractCommandManager(@NonNull P plugin) {
        super();
        this.plugin = plugin;
    }

    /**
     * Handles the command.
     * @param sender the sender of the command
     * @param label the label of the command
     * @param args the arguments of the command
     * @return whether the command was handled successfully
     */
    @Override
    public boolean handle(@NonNull CommandSender sender, @NonNull String label, @NonNull String @NonNull[] args) {
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


    /**
     * Handles tab completion.
     * @param sender the sender of the command
     * @param label the label of the command
     * @param args the arguments of the command
     * @return the list of tab completions, or null if no completions are available
     */
    @Override
    public @Nullable List<String> handleTabComplete(@NonNull CommandSender sender, @NonNull String label, @NonNull String @NonNull[] args) {
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

    /**
     * Adds a subcommand and all of its aliases to the registry.
     * {@inheritDoc}
     */
    @Override
    public void add(T instance) {
        subCommands.put(instance.name(), instance);
        for (String alias : instance.aliases()) {
            subCommands.put(alias, instance);
        }
    }

    /**
     * Removes a subcommand and all of its aliases from the registry.
     * {@inheritDoc}
     */
    @Override
    public void remove(T instance) {
        subCommands.remove(instance.name());
        for (String alias : instance.aliases()) {
            subCommands.remove(alias);
        }
    }
}
