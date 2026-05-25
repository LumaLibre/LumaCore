package dev.lumas.core.model.brigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lumas.core.model.DelegateHolder;
import dev.lumas.core.model.command.BaseCommandManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Base for Brigadier commands that act as a parent for subcommands. The default
 * {@link #buildTree(Commands)} implementation creates a root literal for this
 * command's name, applies its {@code permission}/{@code playerOnly} as a
 * {@code .requires(...)} predicate, then grafts each registered subcommand's
 * branch onto it.
 * <p>
 * Subclasses generally don't need to override anything  declaring the class with
 * {@code @CommandMeta} and {@code @Register(Autowire.BRIGADIER)} is enough.
 */
@NullMarked
@SuppressWarnings("UnstableApiUsage")
public abstract class BrigadierCommandManager<T extends BrigadierSubCommand> extends BrigadierCommand implements DelegateHolder<T>, BaseCommandManager {

    protected final Map<String, T> subCommands = new LinkedHashMap<>();

    protected BrigadierCommandManager() {
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> buildTree(Commands commands) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(meta().name());
        applyRequires(root);

        for (T sub : subCommands.values()) {
            LiteralArgumentBuilder<CommandSourceStack> subTree = sub.buildTree(commands);
            applySubRequires(subTree, sub);
            root.then(subTree.build());
        }

        return root;
    }

    /**
     * Apply permission/playerOnly from this command's {@code @CommandMeta} as a
     * predicate on the root literal. Composes with any existing {@code requires(...)}
     * the builder may already carry. Override to add custom gating.
     */
    protected void applyRequires(LiteralArgumentBuilder<CommandSourceStack> root) {
        composeRequires(root, meta().permission(), meta().playerOnly());
    }

    /**
     * Apply permission/playerOnly from a subcommand's {@code @CommandMeta} as a
     * predicate on its literal. Composes with any existing {@code requires(...)}
     * the subcommand declared via the DSL path. Override to add custom per-subcommand gating.
     */
    protected void applySubRequires(LiteralArgumentBuilder<CommandSourceStack> subRoot, T sub) {
        composeRequires(subRoot, sub.meta().permission(), sub.meta().playerOnly());
    }

    private static void composeRequires(
            LiteralArgumentBuilder<CommandSourceStack> builder,
            String perm,
            boolean playerOnly
    ) {
        if (perm.isEmpty() && !playerOnly) {
            return;
        }
        Predicate<CommandSourceStack> existing = builder.getRequirement();
        Predicate<CommandSourceStack> gate = src -> {
            if (playerOnly && !(src.getSender() instanceof Player)) {
                return false;
            }
            return perm.isEmpty() || src.getSender().hasPermission(perm);
        };
        builder.requires(existing == null ? gate : gate.and(existing));
    }

    /**
     * Registers a subcommand by its primary name.
     * Brigadier aliases (if any) are added at the tree level by the framework  not duplicated here.
     * {@inheritDoc}
     */
    @Override
    public void add(@NonNull T instance) {
        subCommands.put(instance.meta().name(), instance);
    }

    @Override
    public void remove(@NonNull T instance) {
        subCommands.remove(instance.meta().name());
    }
}