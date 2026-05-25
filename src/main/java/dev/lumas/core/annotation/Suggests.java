package dev.lumas.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as the tab-completion supplier for an {@link Argument} on the
 * same class. Used with the annotation path of
 * {@link dev.lumas.core.model.brigadier.BrigadierSubCommand} /
 * {@link dev.lumas.core.model.brigadier.BrigadierCommand}.
 * <p>
 * The annotated method must have the signature:
 * <pre>{@code
 * CompletableFuture<Suggestions> name(CommandContext<CommandSourceStack> ctx,
 *                                     SuggestionsBuilder builder)
 * }</pre>
 * — i.e. Brigadier's {@link com.mojang.brigadier.suggestion.SuggestionProvider}
 * shape, but as a method on your command class.
 * <p>
 * The {@link #value()} must match an {@code @Argument(value=...)} on the same
 * {@code @BrigadierExecutor}-annotated method.
 * <p>
 * Example:
 * <pre>{@code
 * @BrigadierExecutor
 * public void run(CommandSourceStack src, @Argument("key") Key key) { ... }
 *
 * @Suggests("key")
 * public CompletableFuture<Suggestions> suggestKey(
 *         CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
 *     String remaining = builder.getRemaining().toLowerCase();
 *     MarketManager.INSTANCE.keys().stream()
 *         .map(Key::asString)
 *         .filter(s -> s.toLowerCase().startsWith(remaining))
 *         .forEach(builder::suggest);
 *     return builder.buildFuture();
 * }
 * }</pre>
 *
 * <p>For reusable, cross-command suggestion sources, prefer wrapping the logic
 * in a Brigadier {@link com.mojang.brigadier.suggestion.SuggestionProvider} and
 * dropping to the DSL path via {@code buildTree(...)}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Suggests {

    /**
     * The argument name this suggestion supplier is bound to — must match an
     * {@link Argument#value()} on the executor method.
     */
    String value();
}