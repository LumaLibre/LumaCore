package dev.jsinco.luma.lumacore.manager.guis.items;

import dev.jsinco.luma.lumacore.manager.guis.GuiItemAction;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public non-sealed class IndexedGuiItem extends AbstractGuiItem {

    protected int index;

    public IndexedGuiItem(int index, ItemStack itemStack, GuiItemAction action) {
        super(itemStack, action);
        this.index = index;
    }

    public static IndexedGuiItem of(int index, ItemStack itemStack, GuiItemAction action) {
        return new IndexedGuiItem(index, itemStack, action);
    }
}
