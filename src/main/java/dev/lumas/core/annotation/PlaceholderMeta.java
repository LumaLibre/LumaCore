package dev.lumas.core.annotation;

import dev.lumas.core.model.placeholder.SoloAbstractPlaceholder;
import dev.lumas.core.model.placeholder.AbstractPlaceholderManager;
import dev.lumas.core.model.placeholder.AbstractPlaceholder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for defining a {@link SoloAbstractPlaceholder},
 * {@link AbstractPlaceholderManager}, or {@link AbstractPlaceholder} class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlaceholderMeta {

    /**
     * The identifier of this placeholder <b>(%parent%)</b>.
     * If this is a child, the identifier will be <b>%parent_identifier%</b>
     * @return the identifier of this placeholder
     */
    String identifier();

    // Parent

    /**
     * A parent-only attribute which specifies the author of this placeholder.
     * This data is just passed along to PAPI.
     * @return the author of this placeholder
     */
    String author() default "";

    /**
     * A parent-only attribute which specifies the version of this placeholder.
     * This data is just passed along to PAPI.
     * @return the version of this placeholder
     */
    String version() default "";

    /**
     * A parent-only attribute which specifies whether this placeholder should be persisted across reloads.
     * @return whether this placeholder should be persisted across reloads
     */
    boolean persist() default true;

    // Child

    /**
     * A child-only attribute which specifies the aliases of this placeholder.
     * @return the aliases of this placeholder
     */
    String[] aliases() default {};

    /**
     * The parent class of this placeholder, if it's a child.
     * @return the parent class of this placeholder
     */
    Class<? extends AbstractPlaceholderManager> parent() default AbstractPlaceholderManager.class;
}
