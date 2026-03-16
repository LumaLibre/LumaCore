package dev.lumas.core.model.gui;

import dev.lumas.core.model.gui.items.AbstractGuiItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface GuiItemAction {
    void run(InventoryClickEvent event, AbstractGuiItem guiItem);
}
