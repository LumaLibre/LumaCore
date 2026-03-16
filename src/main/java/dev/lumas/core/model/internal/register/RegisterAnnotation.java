package dev.lumas.core.model.internal.register;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.model.internal.AnnotationHolder;

public interface RegisterAnnotation extends AnnotationHolder {

    Autowire[] value();

    String requires();
}
