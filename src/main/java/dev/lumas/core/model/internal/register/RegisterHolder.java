package dev.lumas.core.model.internal.register;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.Register;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record RegisterHolder(Register handle) implements RegisterAnnotation {

    @Override
    public Autowire[] value() {
        return handle.value();
    }

    @Override
    public String requires() {
        return handle.requires();
    }
}
