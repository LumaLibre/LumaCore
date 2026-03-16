package dev.lumas.lumacore.manager.guis;

import dev.lumas.core.model.gui.items.IndexedGuiItem;
import org.bukkit.inventory.ItemStack;

// Only here for backwards compatibility

/**
 * @deprecated Use {@link IndexedGuiItem}
 */
@Deprecated
public class GuiItem extends IndexedGuiItem {

    public GuiItem(int index, ItemStack itemStack, GuiItemAction action) {
        super(index, itemStack, action);
    }
}
