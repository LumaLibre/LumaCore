package dev.lumas.core.annotation;

import dev.lumas.lumacore.manager.placeholder.AbstractPlaceholderManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlaceholderMeta {
    // All
    String identifier();

    // Parent
    String author() default "";
    String version() default "";
    boolean persist() default true;


    // Child
    String[] aliases() default {};
    Class<? extends AbstractPlaceholderManager> parent() default AbstractPlaceholderManager.class;
}
