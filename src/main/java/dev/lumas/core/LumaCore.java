package dev.lumas.core;

import dev.lumas.core.listener.GuiListeners;
import dev.lumas.core.manager.Modules;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class LumaCore extends JavaPlugin {

    @Getter
    private static LumaCore instance;
    @Getter
    private static boolean withPlaceholderAPI;
    @Getter
    private static boolean stopping;

    private static Modules modules;

    @Override
    public void onLoad() {
        instance = this;
        modules = new Modules(this);
        withPlaceholderAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public void onEnable() {
        modules.register();
        // Not worth using a module manager for this
        getServer().getPluginManager().registerEvents(new GuiListeners(), this);
    }


    @Override
    public void onDisable() {
        stopping = true;
        modules.unregister();
    }
}