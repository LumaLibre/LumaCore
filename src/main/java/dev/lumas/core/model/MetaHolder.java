package dev.lumas.core.model;

import dev.lumas.core.model.brigadier.BrigadierCommand;
import dev.lumas.core.model.brigadier.BrigadierSubCommand;
import dev.lumas.core.model.internal.command.CommandAnnotation;
import org.jspecify.annotations.NullMarked;

/**
 * Internal marker for anything that exposes a {@link CommandAnnotation} -
 * implemented by both {@link BrigadierCommand} and {@link BrigadierSubCommand}
 * so BrigadierTrees can synthesize a tree for either.
 * <p>
 * Not part of the public API. Users should implement {@link BrigadierSubCommand}
 * or extend {@link BrigadierCommand} as before.
 */
@NullMarked
public interface MetaHolder {

    CommandAnnotation meta();
}