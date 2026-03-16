package dev.lumas.lumacore.manager.placeholder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated Use {@link dev.lumas.core.annotation.PlaceholderMeta}
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlaceholderInfo {
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
