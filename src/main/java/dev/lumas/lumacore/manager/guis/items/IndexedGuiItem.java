package dev.lumas.lumacore.manager.guis.items;

import dev.lumas.lumacore.manager.guis.GuiItemAction;
import org.bukkit.inventory.ItemStack;

/**
 * @deprecated Use {@link dev.lumas.core.model.gui.items.IndexedGuiItem}
 */
@Deprecated
public class IndexedGuiItem extends dev.lumas.core.model.gui.items.IndexedGuiItem {

    public IndexedGuiItem(int index, ItemStack itemStack, GuiItemAction action) {
        super(index, itemStack, action);
    }

    public static IndexedGuiItem of(int index, ItemStack itemStack, GuiItemAction action) {
        return new IndexedGuiItem(index, itemStack, action);
    }
}
