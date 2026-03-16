package dev.lumas.core.listener;

import dev.lumas.core.model.gui.AbstractGui;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
}
