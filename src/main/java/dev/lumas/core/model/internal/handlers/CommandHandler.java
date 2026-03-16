package dev.lumas.core.model.internal.handlers;

import dev.lumas.core.model.ModuleContext;
import dev.lumas.core.model.command.AbstractCommand;
import dev.lumas.core.model.command.AbstractCommandManager;
import dev.lumas.core.model.command.AbstractSubCommand;
import dev.lumas.core.model.internal.RegisterHandler;
import dev.lumas.core.util.Logging;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NullMarked
public class CommandHandler implements RegisterHandler<AbstractCommand> {

    private final Map<AbstractCommandManager<?, ?>, List<AbstractSubCommand<?>>> managers = new LinkedHashMap<>();
    private final List<AbstractSubCommand<?>> queuedSubCommands = new ArrayList<>();

    @Override
    public void register(AbstractCommand instance, ModuleContext ctx) {
        Bukkit.getCommandMap().register(instance.getLabel(), ctx.fallbackPrefix(), instance);

        if (instance instanceof AbstractCommandManager<?, ?> manager) {
            managers.put(manager, new ArrayList<>());
        }
    }

    @Override
    public void unregister(AbstractCommand instance, ModuleContext ctx) {
        var commands = Bukkit.getCommandMap().getKnownCommands();
        commands.remove(ctx.fallbackPrefix() + ":" + instance.getLabel());
        commands.remove(instance.getLabel());
        instance.getAliases().forEach(alias -> {
            commands.remove(ctx.fallbackPrefix() + ":" + alias);
            commands.remove(alias);
        });
    }

    @Override
    public void postProcess(ModuleContext context) {
        for (AbstractSubCommand<?> sub : queuedSubCommands) {
            AbstractCommandManager<?, ?> parent = managers.keySet().stream()
                    .filter(m -> sub.parent().isInstance(m))
                    .findFirst()
                    .orElse(null);

            if (parent == null) {
                Logging.warningLog("No parent CommandManager for: " + sub.getClass().getSimpleName());
                continue;
            }
            parent.addUntyped(sub);
        }
        queuedSubCommands.clear();
    }

    /**
     * Queue a subcommand for deferred parent resolution.
     */
    public void queue(AbstractSubCommand<?> subCommand) {
        queuedSubCommands.add(subCommand);
    }

    @Override
    public boolean accepts(Object instance) {
        return instance instanceof AbstractCommand;
    }
}
