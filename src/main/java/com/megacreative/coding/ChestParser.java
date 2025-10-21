package com.megacreative.coding;

import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Parser that extracts parameters from chests placed next to signs.
 * This system allows players to configure script parameters visually by placing items in chests,
 * making the system much more user-friendly than traditional configuration files.
 * 
 * The parser recognizes different item types and converts them to appropriate data values:
 * - Books with names become text values
 * - Slime balls with names become numeric values
 * - Paper with names become location values
 * - Potions with names become effect values
 * - Apples with special names become dynamic values
 * - Magma cream becomes dynamic variables
 * 
 * Парсер, который извлекает параметры из сундуков, размещенных рядом со знаками.
 * Эта система позволяет игрокам визуально настраивать параметры скрипта, размещая предметы в сундуках,
 * делая систему намного более удобной для пользователя, чем традиционные файлы конфигурации.
 * 
 * Парсер распознает различные типы предметов и преобразует их в соответствующие значения данных:
 * - Книги с именами становятся текстовыми значениями
 * - Слизневые шары с именами становятся числовыми значениями
 * - Бумага с именами становится значениями местоположения
 * - Зелья с именами становятся значениями эффектов
 * - Яблоки со специальными именами становятся динамическими значениями
 * - Магмовый крем становится динамическими переменными
 * 
 * @author Андрій Budильников
 */
public class ChestParser {
    
    private static final Logger LOGGER = Logger.getLogger(ChestParser.class.getName());
    
    private final Inventory chestInventory;
    private final Location chestLocation;
    
    public ChestParser(Inventory chestInventory, Location chestLocation) {
        this.chestInventory = chestInventory;
        this.chestLocation = chestLocation;
    }
    
    /**
     * Creates a ChestParser for a chest block adjacent to a sign
     * @param signLocation The location of the sign
     * @return The ChestParser, or null if no chest is found
     * 
     * Создает ChestParser для блока сундука, примыкающего к знаку
     * @param signLocation Расположение знака
     * @return ChestParser или null, если сундук не найден
     */
    public static ChestParser forAdjacentChest(Location signLocation) {
        
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
        
        for (BlockFace face : faces) {
            Block adjacentBlock = signLocation.getBlock().getRelative(face);
            if (adjacentBlock.getType() == Material.CHEST) {
                Chest chest = (Chest) adjacentBlock.getState();
                return new ChestParser(chest.getInventory(), chest.getLocation());
            }
        }
        
        return null;
    }
    
    /**
     * Gets text from an item in the chest
     * @param slot The slot index (0-26 for single chest)
     * @return The text value, or null if not found
     * 
     * Получает текст из предмета в сундуке
     * @param slot Индекс слота (0-26 для одиночного сундука)
     * @return Текстовое значение или null, если не найдено
     */
    public String getText(int slot) {
        if (slot < 0 || slot >= chestInventory.getSize()) {
            return null;
        }
        
        ItemStack item = chestInventory.getItem(slot);
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        
        
        if (item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.WRITABLE_BOOK) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        }
        
        return null;
    }
    
    /**
     * Gets a number from an item in the chest
     * @param slot The slot index (0-26 for single chest)
     * @return The numeric value, or 0 if not found
     * 
     * Получает число из предмета в сундуке
     * @param slot Индекс слота (0-26 для одиночного сундука)
     * @return Числовое значение или 0, если не найдено
     */
    public double getNumber(int slot) {
        if (slot < 0 || slot >= chestInventory.getSize()) {
            return 0;
        }
        
        ItemStack item = chestInventory.getItem(slot);
        if (item == null || item.getType() == Material.AIR) {
            return 0;
        }
        
        
        if (item.getType() == Material.SLIME_BALL) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                try {
                    return Double.parseDouble(meta.getDisplayName());
                } catch (NumberFormatException e) {
                    LOGGER.warning("Invalid number format in chest slot " + slot + ": " + meta.getDisplayName());
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Gets a location from an item in the chest
     * @param slot The slot index (0-26 for single chest)
     * @return The location value, or null if not found
     * 
     * Получает местоположение из предмета в сундуке
     * @param slot Индекс слота (0-26 для одиночного сундука)
     * @return Значение местоположения или null, если не найдено
     */
    public Location getLocation(int slot) {
        if (slot < 0 || slot >= chestInventory.getSize()) {
            return null;
        }
        
        ItemStack item = chestInventory.getItem(slot);
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        
        
        if (item.getType() == Material.PAPER) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String[] parts = meta.getDisplayName().split(",");
                if (parts.length >= 3) {
                    try {
                        double x = Double.parseDouble(parts[0].trim());
                        double y = Double.parseDouble(parts[1].trim());
                        double z = Double.parseDouble(parts[2].trim());
                        
                        
                        return new Location(chestLocation.getWorld(), x, y, z);
                    } catch (NumberFormatException e) {
                        LOGGER.warning("Invalid location format in chest slot " + slot + ": " + meta.getDisplayName());
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Gets an item stack from a slot in the chest
     * @param slot The slot index (0-26 for single chest)
     * @return The item stack, or null if not found
     * 
     * Получает стек предметов из слота в сундуке
     * @param slot Индекс слота (0-26 для одиночного сундука)
     * @return Стек предметов или null, если не найден
     */
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= chestInventory.getSize()) {
            return null;
        }
        
        return chestInventory.getItem(slot);
    }
    
    /**
     * Gets all items from the chest as a list
     * @return List of all items in the chest
     * 
     * Получает все предметы из сундука в виде списка
     * @return Список всех предметов в сундуке
     */
    public List<ItemStack> getAllItems() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < chestInventory.getSize(); i++) {
            ItemStack item = chestInventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                items.add(item);
            }
        }
        return items;
    }
    
    /**
     * Gets the chest inventory
     * @return The chest inventory
     * 
     * Получает инвентарь сундука
     * @return Инвентарь сундука
     */
    public Inventory getChestInventory() {
        return chestInventory;
    }
    
    /**
     * Gets the chest location
     * @return The chest location
     * 
     * Получает местоположение сундука
     * @return Местоположение сундука
     */
    public Location getChestLocation() {
        return chestLocation;
    }
}