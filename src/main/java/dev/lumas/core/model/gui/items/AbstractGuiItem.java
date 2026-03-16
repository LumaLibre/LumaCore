package dev.lumas.core.model.gui.items;

import dev.lumas.core.model.gui.GuiItemAction;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@Getter
@NullMarked
public abstract sealed class AbstractGuiItem permits IndexedGuiItem, KeyedGuiItem {

    protected ItemStack itemStack;
    protected GuiItemAction action;

    public AbstractGuiItem(ItemStack itemStack, GuiItemAction action) {
        this.itemStack = itemStack;
        this.action = action;
    }

    public void handleAction(InventoryClickEvent event) {
        action.run(event, this);
    }
}
