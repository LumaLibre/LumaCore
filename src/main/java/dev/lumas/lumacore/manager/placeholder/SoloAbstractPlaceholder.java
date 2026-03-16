package dev.lumas.lumacore.manager.placeholder;

/**
 * @deprecated Use {@link dev.lumas.core.model.placeholder.SoloAbstractPlaceholder}
 */
@Deprecated
public abstract class SoloAbstractPlaceholder extends dev.lumas.core.model.placeholder.SoloAbstractPlaceholder {

    public SoloAbstractPlaceholder() {
        super();
    }

    public SoloAbstractPlaceholder(String identifier, String author, String version, boolean persist) {
        super(identifier, author, version, persist);
    }

}
