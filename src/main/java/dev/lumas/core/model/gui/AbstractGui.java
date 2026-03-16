package dev.lumas.core.model.gui;

import dev.lumas.core.LumaCore;
import dev.lumas.core.model.DelegateHolder;
import dev.lumas.core.model.gui.items.AbstractGuiItem;
import dev.lumas.core.model.gui.items.IndexedGuiItem;
import dev.lumas.core.model.gui.items.KeyedGuiItem;
import dev.lumas.core.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for GUIs.
 * Provides automatic registration of {@link AbstractGuiItem}s and handling of inventory events.
 * @see KeyedGuiItem
 * @see IndexedGuiItem
 */
@NullMarked
public abstract class AbstractGui implements InventoryHolder, DelegateHolder<AbstractGuiItem> {

    protected final Set<AbstractGuiItem> guiItemsMap = new HashSet<>();

    @Override
    public void add(AbstractGuiItem instance) {
        guiItemsMap.add(instance);
        if (instance instanceof IndexedGuiItem indexedGuiItem) {
            this.getInventory().setItem(indexedGuiItem.getIndex(), indexedGuiItem.getItemStack());
        }
    }

    @Override
    public void remove(AbstractGuiItem instance) {
        guiItemsMap.remove(instance);
        if (instance instanceof IndexedGuiItem indexedGuiItem) {
            this.getInventory().setItem(indexedGuiItem.getIndex(), null);
        }
    }

    @ApiStatus.Obsolete
    public void addItem(AbstractGuiItem guiItem) {
        this.add(guiItem);
    }

    @ApiStatus.Obsolete
    public void removeItem(AbstractGuiItem guiItem) {
        this.remove(guiItem);
    }

    @ApiStatus.Internal
    public void handleInventoryInitialClick(InventoryClickEvent event) {
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

    @ApiStatus.Internal
    public void handleInventoryInitialClose(InventoryCloseEvent event) {
        this.onInventoryClose(event);
    }

    protected void autoRegister(Class<?> forClass) {
        for (Field field : forClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (!AbstractGuiItem.class.isAssignableFrom(field.getType())) continue;

            try {
                AbstractGuiItem guiItem = (AbstractGuiItem) field.get(this);
                if (guiItem != null) {
                    this.addItem(guiItem);
                } else {
                    Logging.errorLog("GuiItem field '" + field.getName() + "' is null in " + forClass.getSimpleName());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void autoRegister(boolean walk) {
        Class<?> currentClass = this.getClass();
        if (walk) {
            while (currentClass != null && currentClass != Object.class) {
                this.autoRegister(currentClass);
                currentClass = currentClass.getSuperclass();
            }
        } else {
            this.autoRegister(currentClass);
        }
    }

    protected void autoRegister() {
        this.autoRegister(false);
    }


    public void open(HumanEntity humanEntity) {
        if (!Bukkit.isOwnedByCurrentRegion(humanEntity)) {
            humanEntity.getScheduler().execute(LumaCore.getInstance(), () -> humanEntity.openInventory(this.getInventory()), null, 1);
        } else {
            humanEntity.openInventory(this.getInventory());
        }
    }


    public abstract void onInventoryClick(InventoryClickEvent event);
    public abstract void onInventoryClose(InventoryCloseEvent event);
}
