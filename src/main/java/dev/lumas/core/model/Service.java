package dev.lumas.core.model;

import dev.lumas.core.annotation.Register;
import dev.lumas.core.manager.Modules;
import dev.lumas.core.manager.Services;

/**
 * Classes implementing this interface may use
 * {@link Register} to
 * automatically register and unregister themselves.
 */
public interface Service {

    /**
     * Called when {@link Modules#register()} is called.
     * This method will register this service to {@link Services}.
     */
    void register();

    /**
     * Called when {@link Modules#unregister()} is called.
     * This method will unregister this service from {@link Services}.
     */
    void unregister();
}
