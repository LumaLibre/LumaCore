package dev.lumas.core.model.internal.handlers;

import dev.lumas.core.model.ModuleContext;
import dev.lumas.core.model.brigadier.BrigadierCommandManager;
import dev.lumas.core.model.brigadier.BrigadierSubCommand;
import dev.lumas.core.model.command.AbstractCommandManager;
import dev.lumas.core.model.internal.RegisterHandler;
import dev.lumas.core.util.Logging;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

/**
 * Queues Brigadier subcommands and, in {@link #postProcess(ModuleContext)},
 * resolves each one to its parent {@link BrigadierCommandManager}
 * (declared via {@code @CommandMeta(parent = ...)}).
 * <p>
 * Throws if a subcommand's parent class resolves to a Bukkit
 * {@link AbstractCommandManager} — Brigadier subcommands cannot attach to Bukkit
 * managers because their tree-building contract is incompatible.
 * <p>
 * Not registered against an {@code Autowire} value directly. Owned by
 * {@link BrigadierCommandHandler}, which delegates to it when an instance of
 * {@link BrigadierSubCommand} comes through {@code Autowire.BRIGADIER}.
 */
@NullMarked
public class BrigadierSubCommandHandler implements RegisterHandler<BrigadierSubCommand> {

    private final BrigadierCommandHandler commandHandler;
    private final List<BrigadierSubCommand> queued = new ArrayList<>();

    public BrigadierSubCommandHandler(BrigadierCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void register(BrigadierSubCommand instance, ModuleContext context) {
        queued.add(instance);
    }

    @Override
    public void unregister(BrigadierSubCommand instance, ModuleContext context) {
        // nothing to do — handled at the parent command level
    }

    @Override
    public boolean accepts(Object instance) {
        return instance instanceof BrigadierSubCommand;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void postProcess(ModuleContext context) {
        for (BrigadierSubCommand sub : queued) {
            Class<?> declaredParent = sub.meta().parent();

            // Mismatch check: Bukkit parent declared on a Brigadier subcommand.
            if (AbstractCommandManager.class.isAssignableFrom(declaredParent)
                    && !BrigadierCommandManager.class.isAssignableFrom(declaredParent)) {
                throw new IllegalStateException(
                        "Brigadier subcommand " + sub.getClass().getName() +
                                " declares parent " + declaredParent.getName() +
                                " which is a Bukkit AbstractCommandManager. " +
                                "Brigadier subcommands must declare a parent extending BrigadierCommandManager."
                );
            }

            BrigadierCommandManager parent = commandHandler.managers().stream()
                    .filter(declaredParent::isInstance)
                    .findFirst()
                    .orElse(null);

            if (parent == null) {
                Logging.warningLog(
                        "No registered Brigadier parent " + declaredParent.getName() +
                                " for subcommand " + sub.getClass().getName()
                );
                continue;
            }
            parent.add(sub);
        }
        queued.clear();
    }
}