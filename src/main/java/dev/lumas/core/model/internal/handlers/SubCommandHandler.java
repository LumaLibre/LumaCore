package dev.lumas.core.model.internal.handlers;

import dev.lumas.core.model.ModuleContext;
import dev.lumas.core.model.brigadier.BrigadierCommandManager;
import dev.lumas.core.model.command.AbstractSubCommand;
import dev.lumas.core.model.internal.RegisterHandler;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SubCommandHandler implements RegisterHandler<AbstractSubCommand<?>> {

    private final CommandHandler commandHandler;

    public SubCommandHandler(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void register(AbstractSubCommand<?> instance, ModuleContext context) {
        // Fail fast on mismatched parent: Bukkit subcommand pointing at a Brigadier manager.
        Class<?> declaredParent = instance.parent();
        if (BrigadierCommandManager.class.isAssignableFrom(declaredParent)) {
            throw new IllegalStateException(
                    "Bukkit subcommand " + instance.getClass().getName() +
                            " declares parent " + declaredParent.getName() +
                            " which is an AbstractBrigadierCommandManager. " +
                            "Bukkit subcommands must declare a parent extending AbstractCommandManager."
            );
        }
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