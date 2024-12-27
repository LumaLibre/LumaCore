package dev.jsinco.luma.lumacore.manager.guis;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public interface AbstractGui extends InventoryHolder {

    void onInventoryClick(InventoryClickEvent event);
    void onInventoryClose(InventoryCloseEvent event);

    void open(HumanEntity player);
}
