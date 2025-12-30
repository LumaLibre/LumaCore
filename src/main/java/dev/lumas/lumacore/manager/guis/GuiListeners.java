package dev.lumas.lumacore.manager.guis;

import dev.lumas.lumacore.manager.modules.AutoRegister;
import dev.lumas.lumacore.manager.modules.RegisterType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

@AutoRegister(RegisterType.LISTENER)
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
}
