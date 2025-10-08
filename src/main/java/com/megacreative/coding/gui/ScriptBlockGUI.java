package com.megacreative.coding.gui;

import com.megacreative.coding.script.ScriptBlock;
import com.megacreative.coding.script.ScriptBlockType;
import com.megacreative.coding.script.ScriptBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the visual interface for script blocks in an inventory GUI.
 * This class handles the display and interaction with script blocks.
 */
public class ScriptBlockGUI {
    private static final int INVENTORY_SIZE = 54; // 6 rows of inventory
    private static final String INVENTORY_TITLE = ChatColor.DARK_PURPLE + "Визуальный редактор скриптов";
    
    private final Player player;
    private final ScriptBuilder scriptBuilder;
    private final Inventory inventory;
    private final Map<Integer, ScriptBlock> blockSlots;
    private final Map<Material, ConfigurationSection> blockConfigs;
    
    public ScriptBlockGUI(Player player, ScriptBuilder scriptBuilder) {
        this.player = player;
        this.scriptBuilder = scriptBuilder;
        this.inventory = Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE);
        this.blockSlots = new HashMap<>();
        this.blockConfigs = new HashMap<>();
        loadBlockConfigs();
        updateDisplay();
    }
    
    /**
     * Loads block configurations from the config file
     */
    private void loadBlockConfigs() {
        // TODO: Load block configurations from coding_blocks.yml
    }
    
    /**
     * Updates the inventory display with current script blocks
     */
    public void updateDisplay() {
        inventory.clear();
        blockSlots.clear();
        
        List<ScriptBlock> blocks = scriptBuilder.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            ScriptBlock block = blocks.get(i);
            ItemStack item = createBlockItem(block);
            int slot = i;
            inventory.setItem(slot, item);
            blockSlots.put(slot, block);
        }
        
        // Add block type selector items
        addBlockTypeSelectors();
    }
    
    /**
     * Creates an ItemStack representing a script block
     */
    private ItemStack createBlockItem(ScriptBlock block) {
        Material material;
        String name;
        List<String> lore = new ArrayList<>();
        
        switch (block.getType()) {
            case TRIGGER:
                material = Material.DIAMOND_BLOCK;
                name = ChatColor.AQUA + "Триггер";
                break;
            case ACTION:
                material = Material.COBBLESTONE;
                name = ChatColor.GRAY + "Действие";
                break;
            case CONDITION:
                material = Material.GOLD_BLOCK;
                name = ChatColor.YELLOW + "Условие";
                break;
            case LOOP:
                material = Material.EMERALD_BLOCK;
                name = ChatColor.GREEN + "Цикл";
                break;
            default:
                material = Material.STONE;
                name = ChatColor.WHITE + "Блок";
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        // Add block description and parameters to lore
        lore.add(ChatColor.GRAY + "Тип: " + block.getType());
        lore.add(ChatColor.GRAY + "Содержимое: " + block.getContent());
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Adds block type selector items to the inventory
     */
    private void addBlockTypeSelectors() {
        int startSlot = INVENTORY_SIZE - 9; // Bottom row
        
        // Add trigger block selector
        ItemStack triggerSelector = createTypeSelector(
            Material.DIAMOND_BLOCK,
            ChatColor.AQUA + "Добавить триггер",
            ScriptBlockType.TRIGGER
        );
        inventory.setItem(startSlot, triggerSelector);
        
        // Add action block selector
        ItemStack actionSelector = createTypeSelector(
            Material.COBBLESTONE,
            ChatColor.GRAY + "Добавить действие",
            ScriptBlockType.ACTION
        );
        inventory.setItem(startSlot + 1, actionSelector);
        
        // Add condition block selector
        ItemStack conditionSelector = createTypeSelector(
            Material.GOLD_BLOCK,
            ChatColor.YELLOW + "Добавить условие",
            ScriptBlockType.CONDITION
        );
        inventory.setItem(startSlot + 2, conditionSelector);
        
        // Add loop block selector
        ItemStack loopSelector = createTypeSelector(
            Material.EMERALD_BLOCK,
            ChatColor.GREEN + "Добавить цикл",
            ScriptBlockType.LOOP
        );
        inventory.setItem(startSlot + 3, loopSelector);
    }
    
    /**
     * Creates a block type selector item
     */
    private ItemStack createTypeSelector(Material material, String name, ScriptBlockType type) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Нажмите, чтобы добавить");
        lore.add(ChatColor.GRAY + "новый блок этого типа");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Opens the GUI for the player
     */
    public void open() {
        player.openInventory(inventory);
    }
    
    /**
     * Gets the script builder associated with this GUI
     */
    public ScriptBuilder getScriptBuilder() {
        return scriptBuilder;
    }
    
    /**
     * Gets the block at the given inventory slot
     */
    public ScriptBlock getBlockAt(int slot) {
        return blockSlots.get(slot);
    }
}