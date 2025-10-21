package com.megacreative.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Utility class for creating and manipulating ItemStacks
 * 
 * Утилитный класс для создания и манипулирования ItemStacks
 * 
 * @author Андрій Будильников
 */
public class ItemUtils {
    
    /**
     * Creates an ItemStack with the specified material, name, and lore
     * @param material The material of the item
     * @param name The display name of the item
     * @param lore The lore lines of the item
     * @return The created ItemStack
     * 
     * Создает ItemStack с указанным материалом, именем и описанием
     * @param material Материал предмета
     * @param name Отображаемое имя предмета
     * @param lore Строки описания предмета
     * @return Созданный ItemStack
     */
    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Creates an ItemStack with the specified material, amount, name, and lore
     * @param material The material of the item
     * @param amount The amount of the item
     * @param name The display name of the item
     * @param lore The lore lines of the item
     * @return The created ItemStack
     * 
     * Создает ItemStack с указанным материалом, количеством, именем и описанием
     * @param material Материал предмета
     * @param amount Количество предмета
     * @param name Отображаемое имя предмета
     * @param lore Строки описания предмета
     * @return Созданный ItemStack
     */
    public static ItemStack createItem(Material material, int amount, String name, String... lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}