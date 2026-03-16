package dev.lumas.core.model;

import dev.lumas.core.annotation.Register;

/**
 * Classes implementing this interface may use
 * {@link Register} to
 * automatically register and unregister themselves.
 */
public interface Service {
    void register();

    void unregister();
}
