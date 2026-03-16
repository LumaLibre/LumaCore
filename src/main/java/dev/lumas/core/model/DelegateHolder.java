package dev.lumas.core.model;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Represents a holder for delegates.
 * @param <T> The type of delegate.
 * @see dev.lumas.core.model.command.AbstractCommandManager
 * @see dev.lumas.core.model.placeholder.AbstractPlaceholderManager
 */
@NullMarked
public interface DelegateHolder<T> {

    /**
     * Adds an instance to the holder.
     * @param instance The instance to add.
     */
    void add(T instance);

    /**
     * Removes an instance from the holder.
     * @param instance The instance to remove.
     */
    void remove(T instance);

    /**
     * Adds an untyped instance to the holder.
     * @param instance The instance to add.
     */
    @ApiStatus.Internal
    default void addUntyped(Object instance) {
        @SuppressWarnings("unchecked")
        T casted = (T) instance;
        add(casted);
    }

    /**
     * Removes an untyped instance from the holder.
     * @param instance The instance to remove.
     */
    @ApiStatus.Internal
    default void removeUntyped(Object instance) {
        @SuppressWarnings("unchecked")
        T casted = (T) instance;
        remove(casted);
    }
}
