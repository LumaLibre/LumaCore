package dev.lumas.core.model.placeholder;

import dev.lumas.core.model.internal.placeholder.PlaceholderAnnotation;
import dev.lumas.core.util.Annotations;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jspecify.annotations.NullMarked;
import dev.lumas.core.annotation.PlaceholderMeta;

/**
 * Extension of PlaceholderAPI's provided {@link PlaceholderExpansion} class
 * with support for the {@link PlaceholderMeta} annotation.
 */
@NullMarked
public abstract class SoloAbstractPlaceholder extends PlaceholderExpansion {

    protected final String identifier;
    protected final String author;
    protected final String version;
    protected final boolean persist;

    protected SoloAbstractPlaceholder() {
        PlaceholderAnnotation info = Annotations.getPlaceholderMeta(this);
        if (info == null) {
            throw new IllegalStateException("@PlaceholderMeta annotation not found on " + getClass().getName());
        }
        this.identifier = info.identifier();
        this.author = info.author();
        this.version = info.version();
        this.persist = info.persist();
    }

    protected SoloAbstractPlaceholder(String identifier, String author, String version, boolean persist) {
        this.identifier = identifier;
        this.author = author;
        this.version = version;
        this.persist = persist;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthor() {
        return author;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean persist() {
        return persist;
    }
}
