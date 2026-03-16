package dev.lumas.core.model.placeholder;

import dev.lumas.core.model.DelegateHolder;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder manager that stores child placeholders and handles placeholder requests by delegating them to the appropriate child placeholder.
 * @param <P> the type of the plugin instance
 * @param <T> the type of the child placeholder
 */
public abstract class AbstractPlaceholderManager<P extends JavaPlugin, T extends AbstractPlaceholder<P>> extends SoloAbstractPlaceholder implements DelegateHolder<T> {

    protected final Map<String, T> placeholders = new HashMap<>();
    protected final P plugin;

    protected AbstractPlaceholderManager(P plugin) {
        super();
        this.plugin = plugin;
    }

    protected AbstractPlaceholderManager(P plugin, String identifier, String author, String version, boolean persist) {
        super(identifier, author, version, persist);
        this.plugin = plugin;
    }

    /**
     * Called when the requested child placeholder is not found.
     * @param player the player requesting the placeholder
     * @return the value to return when the requested child placeholder is not found, or null to return nothing
     */
    public @Nullable String onUnknown(@Nullable OfflinePlayer player) {
        return null;
    }

    /**
     * Called by PlaceholderAPI when a placeholder is requested.
     * @param player the player requesting the placeholder
     * @param params the parameters passed to the placeholder
     * @return the value to return for the placeholder, or null to return nothing
     */
    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer player, String params) {
        String[] paramsSplit = params.split("_");
        if (paramsSplit.length == 0 || !placeholders.containsKey(paramsSplit[0])) {
            return this.onUnknown(player);
        }

        T placeholder = placeholders.get(paramsSplit[0]);
        List<String> args = List.of(paramsSplit).subList(1, paramsSplit.length);
        return placeholder.onRequest(plugin, player, args);
    }

    /**
     * Adds a placeholder and all of its aliases to the registry.
     * {@inheritDoc}
     */
    @Override
    public void add(T instance) {
        placeholders.put(instance.identifier(), instance);
        for (String alias : instance.aliases()) {
            placeholders.put(alias, instance);
        }
    }

    /**
     * Removes a placeholder and all of its aliases from the registry.
     * @param instance the placeholder to remove
     */
    @Override
    public void remove(T instance) {
        placeholders.remove(instance.identifier());
        for (String alias : instance.aliases()) {
            placeholders.remove(alias);
        }
    }
}
