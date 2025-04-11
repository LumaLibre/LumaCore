package dev.jsinco.luma.lumacore.manager.guis;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface GuiItemAction {
    void run(InventoryClickEvent event, GuiItem guiItem);
}
