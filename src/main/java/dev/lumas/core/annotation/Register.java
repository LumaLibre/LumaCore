package dev.lumas.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for automatically reflecting over and
 * registering a class with the specified type(s) of registration.
 *
 * @see Autowire
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Register {

    /**
     * Specifies the type(s) of registration for the annotated class
     * @return the type(s) of registration
     */
    Autowire[] value();

    /**
     * Specifies the required plugin or canonical class name
     * if required for registration
     *
     * @return the required plugin or canonical class name
     */
    String requires() default "";
}
