package dev.jsinco.luma;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class LumaCore extends JavaPlugin {

    @Getter
    private static LumaCore instance;

    @Override
    public void onLoad() {
        instance = this;
    }

}