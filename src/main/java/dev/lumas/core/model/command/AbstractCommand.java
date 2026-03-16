package dev.lumas.core.model.command;

import com.google.common.collect.ImmutableList;
import dev.lumas.core.model.internal.command.CommandAnnotation;
import dev.lumas.core.util.Annotations;
import dev.lumas.core.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import dev.lumas.core.annotation.CommandMeta;

/**
 * Extension of Bukkit's provided {@link BukkitCommand} class
 * with support for the {@link CommandMeta} annotation.
 */
@NullMarked
public abstract class AbstractCommand extends BukkitCommand {

    // JDK 21 requires args be passed in when calling super(). This string is transient.
    private static final String HOLDER = "holder";

    private final String permission;
    private final boolean playerOnly;

    protected AbstractCommand(String name) {
        super(name);
        this.permission = "";
        this.playerOnly = false;
    }

    protected AbstractCommand(String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
        this.permission = "";
        this.playerOnly = false;
    }

    protected AbstractCommand(String name, String description, String usageMessage, List<String> aliases, String permission, boolean playerOnly) {
        super(name, description, usageMessage, aliases);
        this.permission = permission;
        this.playerOnly = playerOnly;
    }

    protected AbstractCommand() {
        super(HOLDER, HOLDER, HOLDER, List.of());

        CommandAnnotation info = Annotations.getCommandMeta(this);
        if (info == null) {
            throw new IllegalStateException("@CommandMeta annotation not found on " + getClass().getName());
        }
        this.setName(info.name());
        this.setLabel(info.name());
        this.setDescription(info.description());
        this.setUsage(info.usage());
        this.setAliases(List.of(info.aliases()));
        this.permission = info.permission();
        this.playerOnly = info.playerOnly();
    }

    /**
     * Handles the command.
     * @param sender Source object which is executing this command
     * @param label The alias of the command used
     * @param args All arguments passed to the command, split via ' '
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (playerOnly && !(sender instanceof Player)) {
            Text.msg(sender, "This command can only be executed by players.");
            return true;
        } else if (!permission.isEmpty() && !sender.hasPermission(permission)) {
            Text.msg(sender, "You do not have permission to execute this command.");
            return true;
        }

        if (!handle(sender, label, args)) {
            Text.msg(sender, "Invalid usage. Usage: " + getUsage().replace("<command>", label));
        }
        return true;
    }

    /**
     * Handles tab completion.
     * @param sender Source object which is executing this command
     * @param alias the alias being used
     * @param args All arguments passed to the command, split via ' '
     * @return the list of tab completions, or null if no completions are available
     * @throws IllegalArgumentException if sender, alias, or args is null
     */
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> provider = handleTabComplete(sender, alias, args);

        if (provider != null) {
            return provider;
        }

        if (sender.getServer().suggestPlayerNamesWhenNullTabCompletions()) {
            String lastWord = args[args.length - 1];
            Player senderPlayer = sender instanceof Player ? (Player)sender : null;
            ArrayList<String> matchedPlayers = new ArrayList<>();

            for(Player player : sender.getServer().getOnlinePlayers()) {
                String name = player.getName();
                if ((senderPlayer == null || senderPlayer.canSee(player)) && StringUtil.startsWithIgnoreCase(name, lastWord)) {
                    matchedPlayers.add(name);
                }
            }

            matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
            return matchedPlayers;
        } else {
            return ImmutableList.of();
        }
    }

    /**
     * Delegate handler.
     * @param sender the sender of the command
     * @param label the label of the command
     * @param args the arguments of the command
     * @return whether the command was handled successfully
     */
    public abstract boolean handle(CommandSender sender, String label, String[] args);

    /**
     * Delegate handler.
     * @param sender the sender of the command
     * @param label the label of the command
     * @param args the arguments of the command
     * @return the list of tab completions, or null if no completions are available
     */
    public abstract @Nullable List<String> handleTabComplete(CommandSender sender, String label, String[] args);
}
