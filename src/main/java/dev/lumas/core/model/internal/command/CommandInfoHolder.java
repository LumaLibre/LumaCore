package dev.lumas.core.model.internal.command;

import dev.lumas.core.model.command.AbstractCommandManager;
import dev.lumas.lumacore.manager.commands.CommandInfo;

public record CommandInfoHolder(CommandInfo handle) implements CommandAnnotation {

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
    public Class<? extends AbstractCommandManager> parent() {
        return handle.parent();
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
