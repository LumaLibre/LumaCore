package dev.jsinco.luma.lumacore.manager.placeholder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlaceholderInfo {
    // All
    String identifier();

    // Parent
    String author() default "";
    String version() default "";


    // Child
    String[] aliases() default {};
    Class<? extends AbstractPlaceholderManager> parent() default AbstractPlaceholderManager.class;
}
