package dev.lumas.core.model.placeholder;

import dev.lumas.core.model.internal.placeholder.PlaceholderAnnotation;
import dev.lumas.core.util.Annotations;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A child placeholder that belongs to a {@link AbstractPlaceholderManager}.
 * @param <P> the type of the plugin instance
 */
public interface AbstractPlaceholder<P extends JavaPlugin> {

    /**
     * Called when this placeholder is requested.
     * @param plugin the plugin instance
     * @param player the player associated with this request.
     * @param args the arguments passed to the placeholder.
     * @return the value to return for this placeholder, or null to return nothing
     */
    @Nullable
    String onRequest(P plugin, @Nullable OfflinePlayer player, List<String> args);

    /**
     * The identifier of this placeholder.
     * @return the identifier of this placeholder
     */
    @NonNull
    default String identifier() {
        return meta().identifier();
    }

    /**
     * The author of this placeholder.
     * @return the author of this placeholder
     */
    @NonNull
    default String[] aliases() {
        return meta().aliases();
    }

    /**
     * The parent of this child placeholder.
     * @return the parent of this child placeholder
     */
    @NonNull
    default Class<? extends AbstractPlaceholderManager<?, ?>> parent() {
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
