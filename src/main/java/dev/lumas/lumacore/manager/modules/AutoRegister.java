package dev.lumas.lumacore.manager.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated Use {@link dev.lumas.core.annotation.Autowire}
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegister {

    /**
     * Specifies the type(s) of registration for the annotated class
     * @return the type(s) of registration
     */
    RegisterType[] value();

    /**
     * Specifies the required plugin or canonical class name
     * if required for registration
     *
     * @return the required plugin or canonical class name
     */
    String requires() default "";
}
