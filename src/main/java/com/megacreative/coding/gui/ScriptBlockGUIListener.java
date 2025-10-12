package com.megacreative.coding.gui;

import com.megacreative.coding.script.ScriptBlock;
import com.megacreative.coding.script.ScriptBlockType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles inventory events for the script block GUI.
 * This class manages player interactions with the script blocks in the inventory.
 */
public class ScriptBlockGUIListener implements Listener {
    private final Map<UUID, ScriptBlockGUI> openGUIs;
    
    public ScriptBlockGUIListener() {
        this.openGUIs = new HashMap<>();
    }
    
    /**
     * Registers a GUI session for a player
     */
    public void registerGUI(Player player, ScriptBlockGUI gui) {
        openGUIs.put(player.getUniqueId(), gui);
    }
    
    /**
     * Unregisters a GUI session for a player
     */
    public void unregisterGUI(Player player) {
        openGUIs.remove(player.getUniqueId());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ScriptBlockGUI gui = openGUIs.get(player.getUniqueId());
        
        if (gui == null) {
            return;
        }
        
        event.setCancelled(true); 
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }
        
        int slot = event.getRawSlot();
        
        
        if (slot >= 45) { 
            handleBlockTypeSelection(player, gui, clickedItem);
            return;
        }
        
        
        ScriptBlock block = gui.getBlockAt(slot);
        if (block != null) {
            handleBlockClick(player, gui, block, event.isRightClick());
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (openGUIs.containsKey(player.getUniqueId())) {
            event.setCancelled(true); 
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        unregisterGUI(player);
    }
    
    /**
     * Handles clicks on block type selector items
     */
    private void handleBlockTypeSelection(Player player, ScriptBlockGUI gui, ItemStack item) {
        String name = item.getItemMeta().getDisplayName();
        ScriptBlockType type = null;
        
        if (name.contains("триггер")) {
            type = ScriptBlockType.TRIGGER;
        } else if (name.contains("действие")) {
            type = ScriptBlockType.ACTION;
        } else if (name.contains("условие")) {
            type = ScriptBlockType.CONDITION;
        } else if (name.contains("цикл")) {
            type = ScriptBlockType.LOOP;
        }
        
        if (type != null) {
            openBlockConfigMenu(player, gui, type);
        }
    }
    
    /**
     * Handles clicks on existing script blocks
     */
    private void handleBlockClick(Player player, ScriptBlockGUI gui, ScriptBlock block, boolean isRightClick) {
        if (isRightClick) {
            
            gui.getScriptBuilder().removeBlock(gui.getScriptBuilder().getBlocks().indexOf(block));
            gui.updateDisplay();
            player.sendMessage(ChatColor.RED + "Блок удален");
        } else {
            
            openBlockConfigMenu(player, gui, block.getType());
        }
    }
    
    /**
     * Opens the configuration menu for a block type
     */
    private void openBlockConfigMenu(Player player, ScriptBlockGUI gui, ScriptBlockType type) {
        
        player.sendMessage(ChatColor.YELLOW + "Открытие меню настройки для " + type);
    }
}