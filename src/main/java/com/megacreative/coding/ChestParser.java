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
     */
    public Inventory getChestInventory() {
        return chestInventory;
    }
    
    /**
     * Gets the chest location
     * @return The chest location
     */
    public Location getChestLocation() {
        return chestLocation;
    }
}