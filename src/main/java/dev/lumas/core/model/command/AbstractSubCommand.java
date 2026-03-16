package dev.lumas.core.model.command;

import dev.lumas.core.model.internal.command.CommandAnnotation;
import dev.lumas.core.util.Annotations;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface AbstractSubCommand<P extends JavaPlugin> {

    boolean execute(@NonNull P plugin, @NonNull CommandSender sender, @NonNull String label, @NonNull String @NonNull[] args);

    @Nullable List<String> tabComplete(@NonNull P plugin, @NonNull CommandSender sender, @NonNull String @NonNull[] args);

    @NonNull
    default Class<? extends AbstractCommandManager> parent() {
        return meta().parent();
    }

    @NonNull
    default String name() {
        return meta().name();
    }

    @NonNull
    default String permission() {
        return meta().permission();
    }

    default boolean playerOnly() {
        return meta().playerOnly();
    }

    @NonNull
    default String usage(String label) {
        return meta().usage().replace("<command>", label);
    }

    @NonNull
    default String[] aliases() {
        return meta().aliases();
    }

    @NonNull
    default String description() {
        return meta().description();
    }

    @NonNull
    default CommandAnnotation meta() {
        CommandAnnotation meta = Annotations.getCommandMeta(this);
        if (meta == null) {
            throw new IllegalStateException("@CommandMeta annotation not found on " + getClass().getName());
        }
        return meta;
    }
}
