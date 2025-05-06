package dev.jsinco.luma.lumacore.manager.guis.items;

import dev.jsinco.luma.lumacore.manager.guis.GuiItemAction;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

@Getter
public non-sealed class KeyedGuiItem extends AbstractGuiItem {

    protected NamespacedKey key;

    public KeyedGuiItem(NamespacedKey key, ItemStack itemStack, GuiItemAction action) {
        super(itemStack, action);
        this.key = key;

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;
        if (!meta.getPersistentDataContainer().has(key)) {
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            itemStack.setItemMeta(meta);
        }
    }

    public static KeyedGuiItem of(NamespacedKey key, ItemStack itemStack, GuiItemAction action) {
        return new KeyedGuiItem(key, itemStack, action);
    }
}
