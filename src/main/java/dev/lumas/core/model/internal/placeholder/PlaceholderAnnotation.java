package dev.lumas.core.model.internal.placeholder;

import dev.lumas.core.model.internal.AnnotationHolder;
import dev.lumas.core.model.placeholder.AbstractPlaceholderManager;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PlaceholderAnnotation extends AnnotationHolder {

    String identifier();

    String author();

    String version();

    boolean persist();

    String[] aliases();

    Class<? extends AbstractPlaceholderManager<?, ?>> parent();
}
