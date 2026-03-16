package dev.lumas.lumacore.manager.commands;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @deprecated Use {@link dev.lumas.core.model.command.AbstractCommand}
 */
@Deprecated
public abstract class AbstractCommand extends dev.lumas.core.model.command.AbstractCommand {

    protected AbstractCommand(@NotNull String name) {
        super(name);
    }

    protected AbstractCommand(@NotNull String name,
                              @NotNull String description,
                              @NotNull String usageMessage,
                              @NotNull List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }

    protected AbstractCommand(@NotNull String name,
                              @NotNull String description,
                              @NotNull String usageMessage,
                              @NotNull List<String> aliases,
                              @NotNull String permission,
                              boolean playerOnly) {
        super(name, description, usageMessage, aliases);
    }

    protected AbstractCommand() {
        super();
    }
}
