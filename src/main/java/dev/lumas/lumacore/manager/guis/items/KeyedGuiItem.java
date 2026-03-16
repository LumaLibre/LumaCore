package dev.lumas.lumacore.manager.guis.items;

import dev.lumas.lumacore.manager.guis.GuiItemAction;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

/**
 * @deprecated Use {@link dev.lumas.core.model.gui.items.KeyedGuiItem}
 */
@Deprecated
public class KeyedGuiItem extends dev.lumas.core.model.gui.items.KeyedGuiItem {

    public KeyedGuiItem(NamespacedKey key, ItemStack itemStack, GuiItemAction action) {
        super(key, itemStack, action);
    }

    public static KeyedGuiItem of(NamespacedKey key, ItemStack itemStack, GuiItemAction action) {
        return new KeyedGuiItem(key, itemStack, action);
    }
}
