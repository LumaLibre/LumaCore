package dev.lumas.core.model.brigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Argument;
import dev.lumas.core.annotation.BrigadierExecutor;
import dev.lumas.core.model.MetaHolder;
import dev.lumas.core.model.internal.command.CommandAnnotation;
import dev.lumas.core.util.Annotations;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NullMarked;

/**
 * A Brigadier subcommand. Implementations must be annotated with {@link CommandMeta}
 * (including {@code parent = ...}) and provide their argument tree via one of two paths:
 *
 * <ol>
 *     <li><b>DSL path:</b> override {@link #buildTree(Commands)} and return a
 *         {@link LiteralArgumentBuilder} rooted at {@code commands.literal(meta().name())}.
 *         Use this when you need full control (multiple executors at different
 *         depths, sibling branches, fork/redirect).</li>
 *     <li><b>Annotation path:</b> declare a single method annotated
 *         {@link BrigadierExecutor}. The default {@link #buildTree(Commands)}
 *         synthesizes a linear chain from the method's {@link Argument}-annotated
 *         parameters and binds the executor to the leaf.</li>
 * </ol>
 *
 * <p>Read metadata via {@link #meta()}  e.g. {@code meta().name()},
 * {@code meta().permission()}. The framework reads these at registration time.
 */
@NullMarked
public interface BrigadierSubCommand extends MetaHolder {

    /**
     * Build this subcommand's argument tree, rooted at {@code commands.literal(meta().name())}.
     * The default implementation synthesizes a tree from a {@link BrigadierExecutor}
     * annotated method on this class; override to declare the tree explicitly.
     */
    default LiteralArgumentBuilder<CommandSourceStack> buildTree(Commands commands) {
        return BrigadierTrees.buildAnnotatedTree(this);
    }

    /**
     * The {@link CommandMeta} attached to this subcommand, exposed as a
     * {@link CommandAnnotation}. All metadata (name, permission, parent, aliases,
     * etc.) is read through this object.
     *
     * @throws IllegalStateException if no {@code @CommandMeta} is present
     */
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