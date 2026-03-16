package dev.lumas.core.model.placeholder;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NullMarked
public abstract class AbstractPlaceholderManager<P extends JavaPlugin, T extends AbstractPlaceholder<P>> extends SoloAbstractPlaceholder {

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

    @Nullable
    public String unknownPlaceholderReturnValue(OfflinePlayer player) {
        return null;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, String params) {
        String[] paramsSplit = params.split("_");
        if (paramsSplit.length == 0 || !placeholders.containsKey(paramsSplit[0])) {
            return this.unknownPlaceholderReturnValue(player);
        }

        T placeholder = placeholders.get(paramsSplit[0]);
        List<String> args = List.of(paramsSplit).subList(1, paramsSplit.length);
        return placeholder.onRequest(plugin, player, args);
    }

    public void addPlaceholder(AbstractPlaceholder<?> abstractPlaceholder) {
        placeholders.put(abstractPlaceholder.identifier(), (T) abstractPlaceholder);
        for (String alias : abstractPlaceholder.meta().aliases()) {
            placeholders.put(alias, (T) abstractPlaceholder);
        }
    }

    public void removePlaceholder(AbstractPlaceholder<?> abstractPlaceholder) {
        placeholders.remove(abstractPlaceholder.identifier());
        for (String alias : abstractPlaceholder.meta().aliases()) {
            placeholders.remove(alias);
        }
    }
}
