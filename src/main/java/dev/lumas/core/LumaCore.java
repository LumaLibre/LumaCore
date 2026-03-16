package dev.lumas.core;

import dev.lumas.core.listener.GuiListeners;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class LumaCore extends JavaPlugin {

    @Getter
    private static LumaCore instance;
    @Getter
    private static boolean withPlaceholderAPI;

    @Override
    public void onLoad() {
        instance = this;
        withPlaceholderAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public void onEnable() {
        // Not worth using a module manager for this
        getServer().getPluginManager().registerEvents(new GuiListeners(), this);
    }


    @Override
    public void onDisable() {

    }
}