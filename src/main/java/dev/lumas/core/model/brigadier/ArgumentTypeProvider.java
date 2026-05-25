package dev.lumas.core.model.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;

/**
 * Provides an {@link ArgumentType} instance for an {@code @Argument}-annotated parameter.
 * Use this when the argument type needs construction parameters that can't be expressed
 * with a no-args constructor or {@code INSTANCE} field.
 * <p>
 * Implementations must have a public no-args constructor.
 */
@FunctionalInterface
public interface ArgumentTypeProvider {

    ArgumentType<?> provide();

    /**
     * Sentinel "no provider" used as the default of {@code @Argument#provider()}.
     */
    final class None implements ArgumentTypeProvider {
        @Override
        public ArgumentType<?> provide() {
            throw new UnsupportedOperationException("ArgumentTypeProvider.None is a sentinel");
        }
    }
}