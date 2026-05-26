package dev.lumas.core.model.brigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lumas.core.annotation.Argument;
import dev.lumas.core.annotation.BrigadierExecutor;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.model.MetaHolder;
import dev.lumas.core.model.internal.command.CommandAnnotation;
import dev.lumas.core.util.Annotations;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/**
 * A Brigadier subcommand. Implementations must be annotated with {@link CommandMeta}
 * (including {@code parent = ...}) and provide their argument tree via one of two paths:
 *
 * <ol>
 *     <li><b>Annotation path:</b> declare a single method annotated
 *         {@link BrigadierExecutor}. The default {@link #buildTree(LiteralArgumentBuilder, Commands)}
 *         synthesizes a linear chain from the method's {@link Argument}-annotated
 *         parameters.</li>
 *     <li><b>DSL path:</b> override {@link #buildTree(LiteralArgumentBuilder, Commands)}
 *         and add child nodes to the supplied {@code builder}. The builder is
 *         pre-gated with permission/playerOnly from {@code @CommandMeta}.</li>
 * </ol>
 *
 * <p>Read metadata via {@link #meta()} — e.g. {@code meta().name()},
 * {@code meta().permission()}. The framework reads these at registration time.
 */
@NullMarked
public interface BrigadierSubCommand extends MetaHolder {

    /**
     * Build this subcommand's argument tree. Creates a pre-gated root literal
     * from {@code @CommandMeta} (permission + playerOnly applied via
     * {@code .requires(...)}), then delegates to
     * {@link #buildTree(LiteralArgumentBuilder, Commands)} for the user's
     * argument structure.
     */
    default LiteralArgumentBuilder<CommandSourceStack> handleBuildTree(Commands commands) {
        CommandAnnotation m = meta();
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(m.name())
                .requires(src -> {
                    CommandSender sender = src.getSender();
                    String perm = m.permission();
                    if (m.playerOnly() && !(sender instanceof Player)) {
                        return false;
                    }
                    return perm.isEmpty() || sender.hasPermission(perm);
                });
        return buildTree(root, commands);
    }

    /**
     * Extend the subcommand tree. The default implementation synthesizes a tree
     * from a {@link BrigadierExecutor}-annotated method on this class; override
     * to declare the tree explicitly using the provided {@code builder}.
     *
     * @param builder the root literal, pre-configured with permission/sender requirements
     * @param commands the Commands context for building argument nodes
     */
    default LiteralArgumentBuilder<CommandSourceStack> buildTree(LiteralArgumentBuilder<CommandSourceStack> builder, Commands commands) {
        return BrigadierTrees.buildAnnotatedTree(this);
    }

    @Override
    default CommandAnnotation meta() {
        CommandAnnotation info = Annotations.getCommandMeta(this);
        if (info == null) {
            throw new IllegalStateException("@CommandMeta annotation not found on " + getClass().getName());
        }
        return info;
    }

    default String name() {
        return meta().name();
    }
}