package dev.jsinco.luma.lumacore.manager.guis;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGui implements InventoryHolder {

    protected final Map<Integer, GuiItem> guiItemsMap = new HashMap<>();

    public void addItem(GuiItem guiItem) {
        guiItemsMap.put(guiItem.getIndex(), guiItem);
        this.getInventory().setItem(guiItem.getIndex(), guiItem.getItemStack());
    }

    public void removeItem(int index) {
        guiItemsMap.remove(index);
        this.getInventory().setItem(index, null);
    }

    protected void handleInventoryInitialClick(InventoryClickEvent event) {
        for (var mapEntry : guiItemsMap.entrySet()) {
            if (event.getSlot() == mapEntry.getKey()) {
                GuiItem guiItem = mapEntry.getValue();
                guiItem.handleAction(event);
                break;
            }
        }
        this.onInventoryClick(event);
    }

    protected void handleInventoryInitialClose(InventoryCloseEvent event) {
        this.onInventoryClose(event);
    }

    protected void autoRegister() {
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getType() != GuiItem.class) {
                continue;
            }
            try {
                GuiItem guiItem = (GuiItem) field.get(this);
                if (guiItem != null) {
                    this.addItem(guiItem);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public abstract void onInventoryClick(InventoryClickEvent event);
    public abstract void onInventoryClose(InventoryCloseEvent event);
}
