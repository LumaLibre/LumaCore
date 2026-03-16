package dev.lumas.lumacore.manager.placeholder;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * @deprecated Use {@link dev.lumas.core.model.placeholder.AbstractPlaceholderManager}
 */
@Deprecated
public abstract class AbstractPlaceholderManager<P extends JavaPlugin, T extends AbstractPlaceholder<P>> extends dev.lumas.core.model.placeholder.AbstractPlaceholderManager<P, T> {

    protected AbstractPlaceholderManager(P plugin) {
        super(plugin);
    }

    protected AbstractPlaceholderManager(P plugin, String identifier, String author, String version, boolean persist) {
        super(plugin, identifier, author, version, persist);
    }

}
