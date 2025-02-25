package dev.jsinco.luma.lumacore.manager.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {

    String name();

    String description() default "";
    String permission() default "";
    String[] aliases() default {};
    Class<? extends AbstractCommandManager> parent() default AbstractCommandManager.class;
    boolean playerOnly() default false;
    String usage() default "";
}
