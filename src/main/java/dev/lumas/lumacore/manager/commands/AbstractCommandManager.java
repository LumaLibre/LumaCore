package dev.lumas.lumacore.manager.commands;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @deprecated Use {@link dev.lumas.core.model.command.AbstractCommandManager}
 * @param <P>
 * @param <T>
 */
@Deprecated
public abstract class AbstractCommandManager<P extends JavaPlugin, T extends AbstractSubCommand<P>> extends dev.lumas.core.model.command.AbstractCommandManager<P, T> {

    protected AbstractCommandManager(P plugin) {
        super(plugin);
    }

}
