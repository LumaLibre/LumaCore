package dev.lumas.core.model.brigadier;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Argument;
import dev.lumas.core.annotation.BrigadierExecutor;
import dev.lumas.core.model.MetaHolder;
import dev.lumas.core.model.internal.command.CommandAnnotation;
import dev.lumas.core.util.Annotations;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

/**
 * Base class for top-level Brigadier commands. Subclasses must be annotated with
 * {@link CommandMeta} and provide their argument tree via one of two paths:
 *
 * <ol>
 *     <li><b>Annotation path:</b> declare a single method annotated
 *         {@link BrigadierExecutor}. The default {@link #buildTree(Commands)}
 *         synthesizes a linear chain from the method's {@link Argument}-annotated
 *         parameters. Use this for one-shot commands like {@code /spawn}.</li>
 *     <li><b>DSL path:</b> override {@link #buildTree(Commands)} and return a
 *         {@link LiteralArgumentBuilder} rooted at {@code Commands.literal(name())}.
 *         Use this when you need branching, custom suggestions, or multiple
 *         executors at different depths.</li>
 * </ol>
 * <p>
 * {@link BrigadierCommandManager} overrides the default to graft each registered
 * subcommand's branch onto the root literal, so manager subclasses generally
 * don't need to override anything themselves.
 * <p>
 * Permission and player-only flags from {@code @CommandMeta} are applied as a
 * {@code .requires(...)} predicate on the root literal automatically by the
 * registration framework; subclasses do not need to repeat them.
 */
@NullMarked
public abstract class BrigadierCommand implements MetaHolder {

    private final CommandAnnotation meta;

    protected BrigadierCommand() {
        CommandAnnotation info = Annotations.getCommandMeta(this);
        if (info == null) {
            throw new IllegalStateException("@CommandMeta annotation not found on " + getClass().getName());
        }
        this.meta = info;
    }

    /**
     * Build the argument tree for this command. Sets up the root literal with
     * permission/sender checks, then delegates to {@link #buildTree} for
     * the command's actual argument structure.
     */
    public LiteralArgumentBuilder<CommandSourceStack> handleBuildTree(Commands commands) throws CommandSyntaxException {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal(meta.name())
                .requires(src -> {
                    if (meta.playerOnly() && !(src.getSender() instanceof Player)) {
                        return false;
                    }
                    return meta.permission().isEmpty() || src.getSender().hasPermission(meta.permission());
                });

        return buildTree(cmd, commands);
    }

    /**
     * Extend the command tree. The default implementation synthesizes a tree
     * from a {@link BrigadierExecutor}-annotated method on this class; override
     * to declare the tree explicitly using the provided {@code builder}.
     *
     * @param builder the root literal, pre-configured with permission/sender requirements
     * @param commands the Commands context for building argument nodes
     */
    public LiteralArgumentBuilder<CommandSourceStack> buildTree(LiteralArgumentBuilder<CommandSourceStack> builder, Commands commands) {
        return BrigadierTrees.buildAnnotatedTree(this);
    }

    public final String name() {
        return meta.name();
    }

    public final String description() {
        return meta.description();
    }

    public final String usage() {
        return meta.usage();
    }

    public final String[] aliases() {
        return meta.aliases();
    }

    public final String permission() {
        return meta.permission();
    }

    public final boolean playerOnly() {
        return meta.playerOnly();
    }

    @Override
    public final CommandAnnotation meta() {
        return meta;
    }
}