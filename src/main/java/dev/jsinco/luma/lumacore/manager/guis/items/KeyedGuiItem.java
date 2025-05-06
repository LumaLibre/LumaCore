package dev.jsinco.luma.lumacore.manager.guis.items;

import dev.jsinco.luma.lumacore.manager.guis.GuiItemAction;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

@Getter
public non-sealed class KeyedGuiItem extends AbstractGuiItem {

    protected NamespacedKey key;

    public KeyedGuiItem(NamespacedKey key, ItemStack itemStack, GuiItemAction action) {
        super(itemStack, action);
        this.key = key;
    }
}
