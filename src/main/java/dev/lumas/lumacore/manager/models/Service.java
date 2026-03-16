package dev.lumas.lumacore.manager.models;

/**
 * @deprecated Use {@link dev.lumas.core.model.Service}
 */
@Deprecated
public interface Service extends dev.lumas.core.model.Service {
    void register();

    void unregister();
}
