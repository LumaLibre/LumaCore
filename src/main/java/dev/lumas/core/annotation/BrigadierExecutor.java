package dev.lumas.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the executor method on a Brigadier subcommand when using the annotation-based API.
 * <p>
 * The annotated method must have {@code io.papermc.paper.command.brigadier.CommandSourceStack}
 * as its first parameter. All later parameters must be annotated with {@link Argument}
 * and will be resolved from the Brigadier {@code CommandContext} in declaration order.
 * <p>
 * Return type is ignored - execution always returns {@link com.mojang.brigadier.Command#SINGLE_SUCCESS}.
 * If you need richer control flow (Brigadier's int return), override
 * {@link dev.lumas.core.model.brigadier.BrigadierSubCommand#buildTree} directly instead
 * of using this annotation.
 * <p>
 * Example:
 * <pre>{@code
 * @BrigadierExecutor
 * public void run(CommandSourceStack source,
 *                 @Argument("target") Player target,
 *                 @Argument("amount") int amount) { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BrigadierExecutor {
}