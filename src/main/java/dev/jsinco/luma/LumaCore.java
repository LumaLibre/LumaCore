package dev.jsinco.luma;

import dev.jsinco.luma.manager.modules.ModuleManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;

public class LumaCore extends JavaPlugin {

    @Getter
    private static LumaCore instance;
    private static ModuleManager moduleManager;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        moduleManager = new ModuleManager(this);
        moduleManager.reflectivelyRegisterModules();
    }

    @Override
    public void onDisable() {
        moduleManager.unregisterModules();
    }
}