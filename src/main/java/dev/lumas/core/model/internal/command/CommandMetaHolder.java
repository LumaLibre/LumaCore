package dev.lumas.core.model.internal.command;

import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.model.command.AbstractCommandManager;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record CommandMetaHolder(CommandMeta handle) implements CommandAnnotation {

    @Override
    public String name() {
        return handle.name();
    }

    @Override
    public String description() {
        return handle.description();
    }

    @Override
    public String permission() {
        return handle.permission();
    }

    @Override
    public String[] aliases() {
        return handle.aliases();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends AbstractCommandManager> parent() {
        return (Class<? extends AbstractCommandManager>) handle.parent();
    }

    @Override
    public boolean playerOnly() {
        return handle.playerOnly();
    }

    @Override
    public String usage() {
        return handle.usage();
    }
}
