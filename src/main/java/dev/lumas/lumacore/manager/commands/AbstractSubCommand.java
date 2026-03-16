package dev.lumas.lumacore.manager.commands;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link dev.lumas.core.model.command.AbstractSubCommand}
 * @param <P>
 */
@Deprecated
public interface AbstractSubCommand<P extends JavaPlugin> extends dev.lumas.core.model.command.AbstractSubCommand<@NotNull P> {

}
