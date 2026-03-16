package dev.lumas.core.model.placeholder;

import dev.lumas.core.model.internal.placeholder.PlaceholderAnnotation;
import dev.lumas.core.util.Annotations;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface AbstractPlaceholder<P extends JavaPlugin> {

    @Nullable
    String onRequest(P plugin, @Nullable OfflinePlayer player, List<String> args);

    @NonNull
    default String identifier() {
        return meta().identifier();
    }

    @NonNull
    default String[] aliases() {
        return meta().aliases();
    }

    @NonNull
    default Class<? extends AbstractPlaceholderManager> parent() {
        return meta().parent();
    }

    @NonNull
    default PlaceholderAnnotation meta() {
        PlaceholderAnnotation info = Annotations.getPlaceholderMeta(this);
        if (info == null) {
            throw new IllegalStateException("@PlaceholderMeta annotation not found on " + getClass().getName());
        }
        return info;
    }
}
