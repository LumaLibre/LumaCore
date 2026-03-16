package dev.lumas.lumacore.manager.modules;

import dev.lumas.core.manager.Modules;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

/**
 * @deprecated Use {@link dev.lumas.core.manager.Modules}
 */
@Deprecated
public class ModuleManager {

    private final Modules handle;

    public ModuleManager(JavaPlugin caller) {
        this.handle = new Modules(caller);
    }

    public void reflectivelyRegisterModules() {
        handle.register();
    }

    public void reflectivelyRegisterModules(Set<Class<?>> classes) {
        handle.register(classes);
    }

    public void unregisterModules() {
        handle.unregister();
    }
}

