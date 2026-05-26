package dev.lumas.core;

import dev.lumas.core.listener.GuiListeners;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class LumaCore extends JavaPlugin {

    @Getter
    private static LumaCore instance;
    @Getter
    private static boolean placeholderAPI;
    @Getter
    private static boolean started;
    @Getter
    private static boolean stopping;

    @Override
    public void onLoad() {
        instance = this;
        placeholderAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new GuiListeners(), this);
        getServer().getGlobalRegionScheduler().run(this, _ -> started = true);
    }


    @Override
    public void onDisable() {
        stopping = true;
    }
}