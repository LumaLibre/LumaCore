package dev.jsinco.luma.lumacore.manager.guis;

import dev.jsinco.luma.lumacore.manager.guis.items.AbstractGuiItem;
import org.bukkit.event.inventory.InventoryClickEvent;

public interface GuiItemAction {
     void run(InventoryClickEvent event, AbstractGuiItem guiItem);
}
