package dev.jsinco.luma.lumacore.manager.guis;

import dev.jsinco.luma.lumacore.LumaCore;
import dev.jsinco.luma.lumacore.manager.guis.items.AbstractGuiItem;
import dev.jsinco.luma.lumacore.manager.guis.items.IndexedGuiItem;
import dev.jsinco.luma.lumacore.manager.guis.items.KeyedGuiItem;
import dev.jsinco.luma.lumacore.utility.Logging;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractGui<I extends AbstractGuiItem> implements InventoryHolder {

    protected final Set<I> guiItemsMap = new HashSet<>();

    public void addItem(I guiItem) {
        guiItemsMap.add(guiItem);
        if (guiItem instanceof IndexedGuiItem indexedGuiItem) {
            this.getInventory().setItem(indexedGuiItem.getIndex(), indexedGuiItem.getItemStack());
        }
    }

    public void removeItem(I guiItem) {
        guiItemsMap.remove(guiItem);
        if (guiItem instanceof IndexedGuiItem indexedGuiItem) {
            this.getInventory().setItem(indexedGuiItem.getIndex(), null);
        }
    }

    protected void handleInventoryInitialClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null) {
            for (AbstractGuiItem guiItem : this.guiItemsMap) {
                if (guiItem instanceof IndexedGuiItem indexedGuiItem && event.getSlot() == indexedGuiItem.getIndex()) {
                    indexedGuiItem.handleAction(event);
                    break;
                } else if (guiItem instanceof KeyedGuiItem keyedGuiItem && currentItem.hasItemMeta()) {
                    if (currentItem.getItemMeta().getPersistentDataContainer().has(keyedGuiItem.getKey())) {
                        keyedGuiItem.handleAction(event);
                        break;
                    }
                }
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
            if (!AbstractGuiItem.class.isAssignableFrom(field.getType())) continue;
            try {
                I guiItem = (I) field.get(this);
                if (guiItem != null) {
                    this.addItem(guiItem);
                } else {
                    Logging.errorLog("GuiItem field '" + field.getName() + "' is null in " + this.getClass().getSimpleName());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void open(HumanEntity humanEntity) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(LumaCore.getInstance(), () -> humanEntity.openInventory(this.getInventory()));
        } else {
            humanEntity.openInventory(this.getInventory());
        }
    }


    public abstract void onInventoryClick(InventoryClickEvent event);
    public abstract void onInventoryClose(InventoryCloseEvent event);
}
