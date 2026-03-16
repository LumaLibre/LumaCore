package dev.lumas.core.model.internal.placeholder;

import dev.lumas.core.model.placeholder.AbstractPlaceholderManager;
import dev.lumas.lumacore.manager.placeholder.PlaceholderInfo;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record PlaceholderInfoHolder(PlaceholderInfo handle) implements PlaceholderAnnotation {

    @Override
    public String identifier() {
        return handle.identifier();
    }

    @Override
    public String author() {
        return handle.author();
    }

    @Override
    public String version() {
        return handle.version();
    }

    @Override
    public boolean persist() {
        return handle.persist();
    }

    @Override
    public String[] aliases() {
        return handle.aliases();
    }

    @Override
    public Class<? extends AbstractPlaceholderManager> parent() {
        return handle.parent();
    }
}
