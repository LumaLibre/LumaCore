package dev.lumas.lumacore.manager.commands;

import com.google.common.collect.ImmutableList;
import dev.lumas.lumacore.utility.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractCommand extends BukkitCommand {

    private static final String HOLDER = "holder";

    private final String permission;
    private final boolean playerOnly;

    protected AbstractCommand(@NotNull String name) {
        super(name);
        this.permission = "";
        this.playerOnly = false;
    }

    protected AbstractCommand(@NotNull String name,
                              @NotNull String description,
                              @NotNull String usageMessage,
                              @NotNull List<String> aliases) {
        super(name, description, usageMessage, aliases);
        this.permission = "";
        this.playerOnly = false;
    }

    protected AbstractCommand(@NotNull String name,
                              @NotNull String description,
                              @NotNull String usageMessage,
                              @NotNull List<String> aliases,
                              @NotNull String permission,
                              boolean playerOnly) {
        super(name, description, usageMessage, aliases);
        this.permission = permission;
        this.playerOnly = playerOnly;
    }

    protected AbstractCommand() {
        super(HOLDER, HOLDER, HOLDER, List.of());

        CommandInfo info = getClass().getAnnotation(CommandInfo.class);
        if (info == null) {
            throw new IllegalStateException("CommandInfo annotation not found on " + getClass().getName());
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
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        if (playerOnly && !(commandSender instanceof Player)) {
            Text.msg(commandSender, "This command can only be executed by players.");
            return true;
        } else if (!permission.isEmpty() && !commandSender.hasPermission(permission)) {
            Text.msg(commandSender, "You do not have permission to execute this command.");
            return true;
        }

        if (!handle(commandSender, s, strings)) {
            Text.msg(commandSender, "Invalid usage. Usage: " + getUsage().replace("<command>", s));
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
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

    public abstract boolean handle(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args);

    public abstract @Nullable List<String> handleTabComplete(@NotNull CommandSender sender, @NotNull  String label, @NotNull String[] args);
}
