package com.megacreative.coding.gui;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.script.ScriptBuilder;
import com.megacreative.coding.DefaultScriptEngine;
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
 * Manages the visual interface for code blocks in an inventory GUI.
 * This class handles the display and interaction with code blocks.
 */
public class CodeBlockGUI {
    private static final int INVENTORY_SIZE = 54; 
    private static final String INVENTORY_TITLE = ChatColor.DARK_PURPLE + "Визуальный редактор кода";
    
    private final Player player;
    private final ScriptBuilder scriptBuilder;
    private final Inventory inventory;
    private final Map<Integer, CodeBlock> blockSlots;
    private final Map<Material, ConfigurationSection> blockConfigs;
    
    public CodeBlockGUI(Player player, ScriptBuilder scriptBuilder) {
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
        
    }
    
    /**
     * Updates the inventory display with current code blocks
     */
    public void updateDisplay() {
        inventory.clear();
        blockSlots.clear();
        
        List<CodeBlock> blocks = scriptBuilder.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);
            ItemStack item = createBlockItem(block);
            int slot = i;
            inventory.setItem(slot, item);
            blockSlots.put(slot, block);
        }
        
        // Add block type selectors at the bottom
        addBlockTypeSelectors();
    }
    
    /**
     * Creates an ItemStack representing a code block
     */
    private ItemStack createBlockItem(CodeBlock block) {
        // Determine block type using the same approach as DefaultScriptEngine
        BlockType blockType = BlockType.ACTION; // Default fallback
        // We don't have access to ScriptEngine here, so we'll use a simpler approach
        // In a real implementation, we would use the ScriptEngine's getBlockType method
        
        Material material;
        String name;
        List<String> lore = new ArrayList<>();
        
        // Determine block type based on material and action
        if (block.getMaterialName() != null) {
            Material mat = Material.getMaterial(block.getMaterialName());
            if (mat == Material.DIAMOND_BLOCK) {
                blockType = BlockType.EVENT;
            } else if (mat == Material.COBBLESTONE) {
                blockType = BlockType.ACTION;
            } else if (mat == Material.GOLD_BLOCK) {
                blockType = BlockType.CONDITION;
            } else if (mat == Material.EMERALD_BLOCK) {
                blockType = BlockType.CONTROL;
            }
        }
        
        switch (blockType) {
            case EVENT:
                material = Material.DIAMOND_BLOCK;
                name = ChatColor.AQUA + "Событие";
                break;
            case ACTION:
                material = Material.COBBLESTONE;
                name = ChatColor.GRAY + "Действие";
                break;
            case CONDITION:
                material = Material.GOLD_BLOCK;
                name = ChatColor.YELLOW + "Условие";
                break;
            case CONTROL:
                material = Material.EMERALD_BLOCK;
                name = ChatColor.GREEN + "Управление";
                break;
            default:
                material = Material.STONE;
                name = ChatColor.WHITE + "Блок";
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        // Add lore with block information
        lore.add(ChatColor.GRAY + "Тип: " + blockType);
        lore.add(ChatColor.GRAY + "Действие: " + block.getAction());
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Adds block type selector items to the inventory
     */
    private void addBlockTypeSelectors() {
        int startSlot = INVENTORY_SIZE - 9; 
        
        // Add event block selector
        ItemStack eventSelector = createTypeSelector(
            Material.DIAMOND_BLOCK,
            ChatColor.AQUA + "Добавить событие",
            BlockType.EVENT
        );
        inventory.setItem(startSlot, eventSelector);
        
        // Add action block selector
        ItemStack actionSelector = createTypeSelector(
            Material.COBBLESTONE,
            ChatColor.GRAY + "Добавить действие",
            BlockType.ACTION
        );
        inventory.setItem(startSlot + 1, actionSelector);
        
        // Add condition block selector
        ItemStack conditionSelector = createTypeSelector(
            Material.GOLD_BLOCK,
            ChatColor.YELLOW + "Добавить условие",
            BlockType.CONDITION
        );
        inventory.setItem(startSlot + 2, conditionSelector);
        
        // Add control block selector
        ItemStack controlSelector = createTypeSelector(
            Material.EMERALD_BLOCK,
            ChatColor.GREEN + "Добавить управление",
            BlockType.CONTROL
        );
        inventory.setItem(startSlot + 3, controlSelector);
    }
    
    /**
     * Creates a block type selector item
     */
    private ItemStack createTypeSelector(Material material, String name, BlockType type) {
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
    public CodeBlock getBlockAt(int slot) {
        return blockSlots.get(slot);
    }
}