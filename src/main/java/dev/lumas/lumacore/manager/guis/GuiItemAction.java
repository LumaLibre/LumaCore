package dev.lumas.lumacore.manager.guis;

import dev.lumas.core.model.gui.items.AbstractGuiItem;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @deprecated Use {@link dev.lumas.core.model.gui.GuiItemAction}
 */
@Deprecated
public interface GuiItemAction extends dev.lumas.core.model.gui.GuiItemAction {
    void run(InventoryClickEvent event, AbstractGuiItem guiItem);
}
