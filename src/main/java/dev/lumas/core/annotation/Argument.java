package dev.lumas.core.annotation;

import com.mojang.brigadier.arguments.ArgumentType;
import dev.lumas.core.model.brigadier.ArgumentTypeProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a Brigadier argument node bound to a method parameter.
 * <p>
 * Resolution order for the {@link ArgumentType}:
 * <ol>
 *     <li>If {@link #provider()} is set (non-default), instantiate it and call
 *         {@link ArgumentTypeProvider#provide()}.</li>
 *     <li>Otherwise if {@link #type()} is set (non-default), instantiate the
 *         {@link ArgumentType} via its public static {@code INSTANCE} field
 *         (if any) or its no-args constructor.</li>
 *     <li>Otherwise infer from the parameter's Java type. Supported inferred types:
 *         {@code int}/{@code Integer}, {@code long}/{@code Long},
 *         {@code float}/{@code Float}, {@code double}/{@code Double},
 *         {@code boolean}/{@code Boolean}, {@code String} (single word),
 *         {@code Player}, {@code World}, {@code net.kyori.adventure.key.Key}.</li>
 * </ol>
 * <p>
 * A custom {@link ArgumentTypeProvider} is the right choice whenever the argument type
 * needs constructor parameters (bounds, options, plugin handles).
 *
 * <h2>Optional arguments</h2>
 * Setting {@link #optional()} {@code = true} attaches an additional executor to the
 * preceding node, allowing the user to stop the command there. The framework will
 * invoke the executor method with {@code null} for any optional parameter that
 * was not supplied.
 * <p>
 * Constraints (enforced at registration time):
 * <ul>
 *     <li>Once any argument is optional, every following argument must also be
 *         optional - Brigadier has no notion of "middle-optional" arguments.</li>
 *     <li>An optional parameter must accept {@code null} - i.e. it must not be a
 *         primitive type. Use boxed types ({@code Double}, {@code Integer}, ...)
 *         or reference types ({@code Player}, {@code String}, ...).</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {

    /**
     * The literal name of the Brigadier argument node (the key used to look it up
     * in the {@code CommandContext}).
     */
    String value();

    /**
     * Whether this argument can be omitted. When true, the preceding node also
     * carries an executor so the user can stop the command before this argument.
     * The executor method receives {@code null} for omitted optional arguments.
     * <p>
     * All optional arguments must appear after every non-optional argument.
     */
    boolean optional() default false;

    /**
     * Optional explicit {@link ArgumentType} class. Used when {@link #provider()}
     * is the default. The class must expose either a public static {@code INSTANCE}
     * field of an assignable type, or a public no-args constructor.
     */
    Class<? extends ArgumentType<?>> type() default DefaultArgumentType.class;

    /**
     * Optional provider class. Takes precedence over {@link #type()} when set.
     * Use this when the argument type needs configuration.
     */
    Class<? extends ArgumentTypeProvider> provider() default ArgumentTypeProvider.None.class;

    /**
     * Sentinel used to detect that {@link #type()} was not explicitly set.
     */
    interface DefaultArgumentType extends ArgumentType<Object> {
    }
}