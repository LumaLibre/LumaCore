package dev.lumas.core.model.internal.handlers;

import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.lumas.core.model.ModuleContext;
import dev.lumas.core.model.brigadier.BrigadierCommand;
import dev.lumas.core.model.brigadier.BrigadierCommandManager;
import dev.lumas.core.model.brigadier.BrigadierSubCommand;
import dev.lumas.core.model.internal.RegisterHandler;
import dev.lumas.core.util.Logging;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registration handler for Brigadier commands (including command managers).
 * Owns the {@code Autowire.BRIGADIER} slot and forwards {@link BrigadierSubCommand}
 * instances to a {@link BrigadierSubCommandHandler}, so a single autowire value
 * covers both top-level commands and subcommands.
 */
@NullMarked
@SuppressWarnings("UnstableApiUsage")
public class BrigadierCommandHandler implements RegisterHandler<Object> {

    /** Insertion-ordered so the COMMANDS lifecycle callback registers in declaration order. */
    private final Map<Class<? extends BrigadierCommand>, BrigadierCommand> commands = new LinkedHashMap<>();
    private final List<BrigadierCommandManager<?>> managers = new ArrayList<>();
    private final BrigadierSubCommandHandler subHandler = new BrigadierSubCommandHandler(this);
    private boolean lifecycleRegistered = false;

    @Override
    public boolean accepts(Object instance) {
        return instance instanceof BrigadierCommand || subHandler.accepts(instance);
    }

    @Override
    public void register(Object instance, ModuleContext ctx) {
        if (instance instanceof BrigadierSubCommand sub) {
            subHandler.register(sub, ctx);
            return;
        }
        if (!(instance instanceof BrigadierCommand command)) {
            // accepts() should prevent this; defensive guard.
            throw new IllegalArgumentException(
                    "BrigadierCommandHandler received unsupported type: " + instance.getClass().getName()
            );
        }
        commands.put(command.getClass(), command);
        if (command instanceof BrigadierCommandManager<?> manager) {
            managers.add(manager);
        }
        ensureLifecycleRegistered(ctx);
    }

    @Override
    public void unregister(Object instance, ModuleContext ctx) {
        if (instance instanceof BrigadierSubCommand sub) {
            subHandler.unregister(sub, ctx);
            return;
        }
        if (!(instance instanceof BrigadierCommand command)) {
            return;
        }
        commands.remove(command.getClass());
        if (command instanceof BrigadierCommandManager<?> manager) {
            managers.remove(manager);
        }
        // Paper doesn't expose runtime command removal off the COMMANDS event.
        // The plugin should be reloaded for unregistrations to take effect.
    }

    @Override
    public void postProcess(ModuleContext context) {
        // Top-level commands are registered inside the COMMANDS lifecycle callback,
        // not here. The sub handler still needs its postProcess to resolve parents.
        subHandler.postProcess(context);
    }

    /**
     * Expose registered managers so {@link BrigadierSubCommandHandler} can resolve
     * subcommand parents without duplicating bookkeeping.
     */
    public List<BrigadierCommandManager<?>> managers() {
        return managers;
    }

    private void ensureLifecycleRegistered(ModuleContext ctx) {
        if (lifecycleRegistered) {
            return;
        }
        Plugin plugin = ctx.plugin();
        if (!(plugin instanceof JavaPlugin javaPlugin)) {
            Logging.warningLog("BrigadierCommandHandler requires a JavaPlugin to access the LifecycleEventManager; got " + plugin.getClass().getName());
            return;
        }
        LifecycleEventManager<?> lifecycle = javaPlugin.getLifecycleManager();
        lifecycle.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands registrar = event.registrar();
            for (BrigadierCommand command : commands.values()) {
                try {
                    LiteralCommandNode<CommandSourceStack> node = command.buildTree(registrar).build();
                    List<String> aliases = Arrays.asList(command.aliases());
                    String description = command.description();

                    registrar.register(javaPlugin.getPluginMeta(), node, description, aliases);
                } catch (Exception e) {
                    Logging.errorLog("Failed to register Brigadier command " + command.getClass().getName(), e);
                }
            }
        });
        lifecycleRegistered = true;
    }
}