package dev.jsinco.luma.lumacore.manager.guis;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class GuiItem {

    private int index;
    private ItemStack itemStack;
    private GuiItemAction action;

    public GuiItem(int index, ItemStack itemStack, GuiItemAction action) {
        this.index = index;
        this.itemStack = itemStack;
        this.action = action;
    }

    public void handleAction(InventoryClickEvent event) {
        action.run(event, this);
    }
}
