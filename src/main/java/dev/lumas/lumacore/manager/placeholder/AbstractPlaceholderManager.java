package dev.lumas.lumacore.manager.placeholder;

import dev.lumas.lumacore.manager.commands.AbstractSubCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractPlaceholderManager<P extends JavaPlugin, T extends AbstractPlaceholder<P>> extends SoloAbstractPlaceholder {

    protected Map<String, T> placeholders = new HashMap<>();
    protected final P plugin;

    protected AbstractPlaceholderManager(P plugin) {
        super();
        this.plugin = plugin;
    }

    protected AbstractPlaceholderManager(P plugin, String identifier, String author, String version) {
        super(identifier, author, version);
        this.plugin = plugin;
    }

    @Nullable
    public String unknownPlaceholderReturnValue(OfflinePlayer player) {
        return null;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
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
        for (String alias : abstractPlaceholder.info().aliases()) {
            placeholders.put(alias, (T) abstractPlaceholder);
        }
    }

    public void removeSubCommand(AbstractSubCommand<?> abstractPlaceholder) {
        placeholders.remove(abstractPlaceholder.name());
        for (String alias : abstractPlaceholder.info().aliases()) {
            placeholders.remove(alias);
        }
    }
}
