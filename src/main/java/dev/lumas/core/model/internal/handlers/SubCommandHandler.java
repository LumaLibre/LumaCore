package dev.lumas.core.model.internal.handlers;

import dev.lumas.core.model.ModuleContext;
import dev.lumas.core.model.command.AbstractSubCommand;
import dev.lumas.core.model.internal.RegisterHandler;

public class SubCommandHandler implements RegisterHandler<AbstractSubCommand<?>> {

    private final CommandHandler commandHandler;

    public SubCommandHandler(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void register(AbstractSubCommand<?> instance, ModuleContext context) {
        commandHandler.queue(instance);
    }

    @Override
    public void unregister(AbstractSubCommand<?> instance, ModuleContext context) {
        // nothing to do
    }

    @Override
    public boolean accepts(Object instance) {
        return instance instanceof AbstractSubCommand<?>;
    }
}
