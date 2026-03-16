package dev.lumas.core.model.internal.command;

import dev.lumas.core.model.command.AbstractCommandManager;
import dev.lumas.core.model.internal.AnnotationHolder;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface CommandAnnotation extends AnnotationHolder {

    String name();

    String description();

    String permission();

    String[] aliases();

    Class<? extends AbstractCommandManager> parent();

    boolean playerOnly();

    String usage();

}
