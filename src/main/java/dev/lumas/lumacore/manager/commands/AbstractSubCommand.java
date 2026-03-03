package dev.lumas.lumacore.manager.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AbstractSubCommand<P extends JavaPlugin> {


    boolean execute(@NotNull P plugin, @NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull[] args);

    @Nullable List<@NotNull String> tabComplete(@NotNull P plugin, @NotNull CommandSender sender, @NotNull String @NotNull[] args);

    default Class<? extends AbstractCommandManager> parent() {
        return info().parent();
    }

    default String name() {
        return info().name();
    }

    default String permission() {
        return info().permission();
    }

    default boolean playerOnly() {
        return info().playerOnly();
    }

    default String usage(String label) {
        return info().usage().replace("<command>", label);
    }

    default CommandInfo info() {
        CommandInfo info = getClass().getAnnotation(CommandInfo.class);
        if (info == null) {
            throw new IllegalStateException("CommandInfo annotation not found on " + getClass().getName());
        }
        return info;
    }
}
