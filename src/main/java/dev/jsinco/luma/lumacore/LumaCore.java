package dev.jsinco.luma.lumacore;

import dev.jsinco.luma.lumacore.manager.modules.ModuleManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class LumaCore extends JavaPlugin {

    // TODO: I really need to fixup this project. Concept is cool, api frontend is okay,
    //  backend is HORRIBLE/incomplete

    @Getter
    private static LumaCore instance;
    private static ModuleManager coreModuleManager;
    @Getter
    private static boolean withPlaceholderAPI;

    @Override
    public void onLoad() {
        instance = this;
        coreModuleManager = new ModuleManager(this);
        withPlaceholderAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public void onEnable() {
        coreModuleManager.reflectivelyRegisterModules();
    }

    @Override
    public void onDisable() {
        coreModuleManager.unregisterModules();
    }
}