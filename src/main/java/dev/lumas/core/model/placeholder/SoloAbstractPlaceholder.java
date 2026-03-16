package dev.lumas.core.model.placeholder;

import dev.lumas.lumacore.manager.placeholder.PlaceholderInfo;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class SoloAbstractPlaceholder extends PlaceholderExpansion {

    protected final String identifier;
    protected final String author;
    protected final String version;
    protected final boolean persist;

    public SoloAbstractPlaceholder() {
        PlaceholderInfo info = getClass().getAnnotation(PlaceholderInfo.class);
        if (info == null) {
            throw new IllegalStateException("PlaceholderInfo annotation not found on " + getClass().getName());
        }
        this.identifier = info.identifier();
        this.author = info.author();
        this.version = info.version();
        this.persist = info.persist();
    }

    public SoloAbstractPlaceholder(String identifier, String author, String version, boolean persist) {
        this.identifier = identifier;
        this.author = author;
        this.version = version;
        this.persist = persist;
    }


    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean persist() {
        return persist;
    }
}
