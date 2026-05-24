package dev.lumas.core.listener;

import dev.lumas.core.LumaCore;
import dev.lumas.core.model.gui.AbstractGui;
import dev.lumas.core.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class GuiListeners implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder(false) instanceof AbstractGui gui) {
            gui.handleInventoryInitialClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder(false) instanceof AbstractGui gui) {
            gui.handleInventoryInitialClose(event);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(LumaCore.getInstance()) || LumaCore.isStopping()) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getScheduler().run(LumaCore.getInstance(), task -> {
                InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder(false);
                if (holder == null) return;

                boolean shouldClose;
                try {
                    shouldClose = JavaPlugin.getProvidingPlugin(holder.getClass()).equals(event.getPlugin());
                } catch (IllegalStateException e) {
                    shouldClose = true;
                } catch (IllegalArgumentException e) {
                    shouldClose = false;
                }

                if (shouldClose) {
                    Logging.log("Closing inventory for " + player.getName()
                            + " because plugin " + event.getPlugin().getName() + " is being disabled.");
                    player.closeInventory();
                }
            }, null);
        }
    }
}
