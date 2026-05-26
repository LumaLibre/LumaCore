package dev.lumas.core.model;

import dev.lumas.core.LumaCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Context provider for Module Managers
 * @param plugin The plugin instance of the module manager
 * @param fallbackPrefix The fallback prefix for the module manager
 */
@NullMarked
public record ModuleContext(Plugin plugin, String fallbackPrefix) {

    /**
     * Internal canonical constructor for framework wiring.
     * Use {@link #of(Plugin, String)}.
     */
    @ApiStatus.Internal
    public ModuleContext {
    }

    public static ModuleContext of(Plugin plugin, String fallbackPrefix) {
        return new ModuleContext(plugin, fallbackPrefix);
    }

    public enum LoadType {
        STARTUP,
        RELOAD;

        public boolean isStartup() {
            return this == STARTUP;
        }

        public boolean isReload() {
            return this == RELOAD;
        }

        public static LoadType determine() {
            if (LumaCore.isStarted() || !Bukkit.getOnlinePlayers().isEmpty()) {
                return RELOAD;
            }
            return STARTUP;
        }
    }
}