package dev.jsinco.luma.lumacore.manager.placeholder;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AbstractPlaceholder<P extends JavaPlugin> {

    @Nullable
    String onRequest(P plugin, @Nullable OfflinePlayer player, List<String> args);

    default String identifier() {
        return info().identifier();
    }

    default String[] aliases() {
        return info().aliases();
    }

    default Class<? extends AbstractPlaceholderManager> parent() {
        return info().parent();
    }

    default PlaceholderInfo info() {
        PlaceholderInfo info = getClass().getAnnotation(PlaceholderInfo.class);
        if (info == null) {
            throw new IllegalStateException("PlaceholderInfo annotation not found on " + getClass().getName());
        }
        return info;
    }
}
