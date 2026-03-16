package dev.lumas.core.model.internal;

import dev.lumas.core.model.ModuleContext;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface RegisterHandler<T> {

    void register(T instance, ModuleContext context);

    void unregister(T instance, ModuleContext context);

    /**
     * Called after all modules have been instantiated and registered.
     * Use for deferred operations like resolving parent-child relationships.
     * @param context the module context
     */
    default void postProcess(ModuleContext context) {

    }

    /**
     * Whether this handler should process the given instance.
     * @param instance the instance to check
     * @return true if this handler can process the instance, false otherwise
     */
    boolean accepts(Object instance);
}