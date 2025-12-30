package dev.lumas.lumacore.manager.guis;

import dev.lumas.lumacore.manager.guis.items.AbstractGuiItem;
import org.bukkit.event.inventory.InventoryClickEvent;

public interface GuiItemAction {
     void run(InventoryClickEvent event, AbstractGuiItem guiItem);
}
