package dev.lumas.lumacore.manager.guis;

import dev.lumas.lumacore.manager.guis.items.IndexedGuiItem;
import org.bukkit.inventory.ItemStack;

// Only here for backwards compatibility
public class GuiItem extends IndexedGuiItem {

    public GuiItem(int index, ItemStack itemStack, GuiItemAction action) {
        super(index, itemStack, action);
    }
}
