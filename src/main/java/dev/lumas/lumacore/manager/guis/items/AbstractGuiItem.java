package dev.lumas.lumacore.manager.guis.items;

import dev.lumas.lumacore.manager.guis.GuiItemAction;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@Getter
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
