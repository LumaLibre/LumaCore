package dev.lumas.core.model.gui.items;

import dev.lumas.core.model.gui.GuiItemAction;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@Getter
@NullMarked
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
