package dev.lumas.core.annotation;

import dev.lumas.core.model.command.AbstractCommandManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining a command class with metadata such as name, description, permission, aliases, parent command, player-only status, and usage.
 * This annotation is used to mark classes that represent commands in the command management system.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandMeta {

    /**
     * The name of the command
     * @return the name of the command
     */
    String name();

    /**
     * The description of the command
     * @return the description of the command
     */
    String description() default "";

    /**
     * The permission required to execute the command
     * @return the permission required to execute the command
     */
    String permission() default "";

    /**
     * The aliases of the command
     * @return the aliases of the command
     */
    String[] aliases() default {};

    /**
     * The parent class of this command, if it's a subcommand
     * @return the parent class of this command
     */
    Class<? extends AbstractCommandManager> parent() default AbstractCommandManager.class;

    /**
     * Whether the command can only be executed by players
     * @return whether the command can only be executed by players
     */
    boolean playerOnly() default false;

    /**
     * The usage of the command
     * @return the usage of the command
     */
    String usage() default "/<command>";
}
