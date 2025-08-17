package com.megacreative.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class VirtualInventoryManager {

    public static Inventory createInventory(InventoryHolder owner, int size, String title) {
        return Bukkit.createInventory(owner, size, title);
    }
}
