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

@NullMarked
public abstract class AbstractCommand extends BukkitCommand {

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

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
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

            Collections.sort(matchedPlayers, String.CASE_INSENSITIVE_ORDER);
            return matchedPlayers;
        } else {
            return ImmutableList.of();
        }
    }

    public abstract boolean handle(CommandSender sender, String label, String[] args);

    public abstract @Nullable List<String> handleTabComplete(CommandSender sender, String label, String[] args);
}
