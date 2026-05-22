package dev.lumas.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an autowired class is of a singleton type and reflective operations should
 * not create new instances.
 * <p>
 * Usage:
 * <pre>
 * {@code
 * @Provided
 * @Register(Autowire.Listener)
 * public class Example implements Listener {
 *
 *      public static final Example INSTANCE = new Example();
 *
 *      //...
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Provided {

    /**
     * Specifies the default instance name to be used for the singleton instance.
     * @return the name of the default instance, defaulting to "INSTANCE"
     */
    String value() default "INSTANCE";
}
