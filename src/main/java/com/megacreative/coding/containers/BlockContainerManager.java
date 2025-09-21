package com.megacreative.coding.containers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.megacreative.coding.containers.ContainerType;
import com.megacreative.coding.containers.ActionConfiguration;
import com.megacreative.coding.containers.ActionParameter;
import org.bukkit.Location;

/**
 * Advanced container system for code blocks
 * Creates visual containers above code blocks for parameter configuration
 * Supports signs, chests, and other container types
 */
public class BlockContainerManager {
    
    private final MegaCreative plugin;
    
    // Container mappings
    private final Map<Location, BlockContainer> containers = new ConcurrentHashMap<>();
    private final Map<Location, Location> blockToContainer = new ConcurrentHashMap<>();
    
    public BlockContainerManager(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Creates a container above a code block
     */
    public BlockContainer createContainer(Location blockLocation, ContainerType type, String action) {
        Location containerLocation = blockLocation.clone().add(0, 1, 0);
        
        // Remove existing container if present
        removeContainer(blockLocation);
        
        // Create the container block
        Block containerBlock = containerLocation.getBlock();
        
        switch (type) {
            case SIGN:
                containerBlock.setType(Material.OAK_SIGN);
                break;
            case CHEST:
                containerBlock.setType(Material.CHEST);
                break;
            case BARREL:
                containerBlock.setType(Material.BARREL);
                break;
            case SHULKER_BOX:
                containerBlock.setType(Material.SHULKER_BOX);
                break;
            default:
                containerBlock.setType(Material.CHEST);
        }
        
        // Create container object
        BlockContainer container = new BlockContainer(blockLocation, containerLocation, type, action);
        
        // Initialize container content
        initializeContainer(container);
        
        // Store mappings
        containers.put(containerLocation, container);
        blockToContainer.put(blockLocation, containerLocation);
        
        plugin.getLogger().fine("Created " + type + " container at " + containerLocation + " for block at " + blockLocation);
        
        return container;
    }
    
    /**
     * Initializes container with default content based on action
     */
    private void initializeContainer(BlockContainer container) {
        switch (container.getType()) {
            case SIGN:
                initializeSign(container);
                break;
            case CHEST:
            case BARREL:
            case SHULKER_BOX:
                initializeInventoryContainer(container);
                break;
        }
    }
    
    /**
     * Sets up a sign with parameter information
     */
    private void initializeSign(BlockContainer container) {
        Block signBlock = container.getContainerLocation().getBlock();
        if (signBlock.getState() instanceof Sign sign) {
            String action = container.getAction();
            
            // Get action configuration
            var actionConfig = getActionConfiguration(action);
            
            sign.setLine(0, "§1§l" + action);
            sign.setLine(1, "§2Parameters:");
            sign.setLine(2, "§3" + actionConfig.getParameterCount() + " params");
            sign.setLine(3, "§4Click to edit");
            
            sign.update();
        }
    }
    
    /**
     * Sets up an inventory container with parameter slots
     */
    private void initializeInventoryContainer(BlockContainer container) {
        Block containerBlock = container.getContainerLocation().getBlock();
        if (containerBlock.getState() instanceof Container containerState) {
            Inventory inventory = containerState.getInventory();
            
            String action = container.getAction();
            var actionConfig = getActionConfiguration(action);
            
            // Clear inventory
            inventory.clear();
            
            // Add parameter slots
            int slot = 0;
            for (ActionParameter param : actionConfig.getParameters()) {
                if (slot >= inventory.getSize()) break;
                
                ItemStack paramItem = createParameterItem(param);
                inventory.setItem(slot, paramItem);
                slot++;
            }
            
            // Add help item
            if (slot < inventory.getSize()) {
                ItemStack helpItem = createHelpItem(action);
                inventory.setItem(inventory.getSize() - 1, helpItem);
            }
        }
    }
    
    /**
     * Creates a parameter item for the inventory
     */
    private ItemStack createParameterItem(ActionParameter param) {
        Material material = getParameterMaterial(param.getType());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§e" + param.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Type: §f" + param.getType().getDisplayName());
            lore.add("§7Description: §f" + param.getDescription());
            lore.add("");
            lore.add("§aClick to configure");
            lore.add("§7Right-click for advanced options");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Gets appropriate material for parameter type
     */
    private Material getParameterMaterial(ValueType type) {
        switch (type) {
            case TEXT: return Material.PAPER;
            case NUMBER: return Material.GOLD_NUGGET;
            case BOOLEAN: return Material.LEVER;
            case LOCATION: return Material.COMPASS;
            case ITEM: return Material.CHEST;
            case PLAYER: return Material.PLAYER_HEAD;
            case ENTITY: return Material.EGG;
            case SOUND: return Material.MUSIC_DISC_CAT;
            case PARTICLE: return Material.FIREWORK_ROCKET;
            case POTION: return Material.POTION;
            case COLOR: return Material.WHITE_DYE;
            default: return Material.BARRIER;
        }
    }
    
    /**
     * Creates a help item
     */
    private ItemStack createHelpItem(String action) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§lHelp: " + action);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7This action performs:");
            lore.add("§f" + getActionDescription(action));
            lore.add("");
            lore.add("§eUsage Instructions:");
            lore.add("§71. Place items in parameter slots");
            lore.add("§72. Configure each parameter");
            lore.add("§73. Test your configuration");
            lore.add("");
            lore.add("§aClick for detailed help");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Removes a container
     */
    public void removeContainer(Location blockLocation) {
        Location containerLocation = blockToContainer.get(blockLocation);
        if (containerLocation != null) {
            // Remove the container block
            containerLocation.getBlock().setType(Material.AIR);
            
            // Remove from mappings
            containers.remove(containerLocation);
            blockToContainer.remove(blockLocation);
            
            plugin.getLogger().fine("Removed container at " + containerLocation);
        }
    }
    
    /**
     * Gets container for a block
     */
    public BlockContainer getContainer(Location blockLocation) {
        Location containerLocation = blockToContainer.get(blockLocation);
        return containerLocation != null ? containers.get(containerLocation) : null;
    }
    
    /**
     * Handles container interaction
     */
    public boolean handleContainerInteraction(Player player, Location containerLocation) {
        BlockContainer container = containers.get(containerLocation);
        if (container == null) return false;
        
        switch (container.getType()) {
            case SIGN:
                openSignEditor(player, container);
                return true;
            case CHEST:
            case BARREL:
            case SHULKER_BOX:
                openInventoryEditor(player, container);
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Opens sign editor GUI
     */
    private void openSignEditor(Player player, BlockContainer container) {
        // Use the existing ContainerConfigGUI for sign editing as well
        com.megacreative.gui.ContainerConfigGUI gui = new com.megacreative.gui.ContainerConfigGUI(
            plugin, player, container.getBlockLocation());
        gui.open();
    }
    
    /**
     * Opens inventory editor
     */
    private void openInventoryEditor(Player player, BlockContainer container) {
        // Use custom drag and drop GUI for container configuration
        com.megacreative.gui.ContainerConfigGUI gui = new com.megacreative.gui.ContainerConfigGUI(
            plugin, player, container.getBlockLocation());
        gui.open();
        
        /*
        Block containerBlock = container.getContainerLocation().getBlock();
        if (containerBlock.getState() instanceof Container containerState) {
            player.openInventory(containerState.getInventory());
        }
        */
    }
    
    /**
     * Updates container content based on parameters
     */
    public void updateContainer(BlockContainer container, Map<String, DataValue> parameters) {
        switch (container.getType()) {
            case SIGN:
                updateSign(container, parameters);
                break;
            case CHEST:
            case BARREL:
            case SHULKER_BOX:
                updateInventoryContainer(container, parameters);
                break;
        }
    }
    
    /**
     * Updates sign with parameter values
     */
    private void updateSign(BlockContainer container, Map<String, DataValue> parameters) {
        Block signBlock = container.getContainerLocation().getBlock();
        if (signBlock.getState() instanceof Sign sign) {
            sign.setLine(0, "§1§l" + container.getAction());
            
            int line = 1;
            for (Map.Entry<String, DataValue> entry : parameters.entrySet()) {
                if (line >= 4) break;
                
                String paramName = entry.getKey();
                DataValue value = entry.getValue();
                
                String displayText = paramName + ": " + value.asString();
                if (displayText.length() > 15) {
                    displayText = displayText.substring(0, 12) + "...";
                }
                
                sign.setLine(line, "§2" + displayText);
                line++;
            }
            
            sign.update();
        }
    }
    
    /**
     * Updates inventory container with parameter values
     */
    private void updateInventoryContainer(BlockContainer container, Map<String, DataValue> parameters) {
        Block containerBlock = container.getContainerLocation().getBlock();
        if (containerBlock.getState() instanceof Container containerState) {
            Inventory inventory = containerState.getInventory();
            
            // Update parameter items with actual values
            String action = container.getAction();
            var actionConfig = getActionConfiguration(action);
            
            int slot = 0;
            for (ActionParameter param : actionConfig.getParameters()) {
                if (slot >= inventory.getSize()) break;
                
                DataValue value = parameters.get(param.getName());
                ItemStack paramItem = createConfiguredParameterItem(param, value);
                inventory.setItem(slot, paramItem);
                slot++;
            }
        }
    }
    
    /**
     * Creates a configured parameter item with value
     */
    private ItemStack createConfiguredParameterItem(ActionParameter param, DataValue value) {
        Material material = getParameterMaterial(param.getType());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§e" + param.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Type: §f" + param.getType().getDisplayName());
            lore.add("§7Description: §f" + param.getDescription());
            lore.add("");
            
            if (value != null) {
                lore.add("§aCurrent Value:");
                lore.add("§f" + value.asString());
                lore.add("");
                lore.add("§eClick to modify");
            } else {
                lore.add("§cNo value set");
                lore.add("§eClick to configure");
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Gets all containers
     */
    public Collection<BlockContainer> getAllContainers() {
        return containers.values();
    }
    
    /**
     * Clears all containers for a world
     */
    public void clearWorldContainers(String worldName) {
        containers.entrySet().removeIf(entry -> {
            Location loc = entry.getKey();
            if (loc.getWorld() != null && loc.getWorld().getName().equals(worldName)) {
                loc.getBlock().setType(Material.AIR);
                return true;
            }
            return false;
        });
        
        blockToContainer.entrySet().removeIf(entry -> {
            Location loc = entry.getKey();
            return loc.getWorld() != null && loc.getWorld().getName().equals(worldName);
        });
    }
    
    // === HELPER METHODS ===
    
    private ActionConfiguration getActionConfiguration(String action) {
        // This would integrate with your action registry
        // For now, return a simple configuration
        return new ActionConfiguration(action);
    }
    
    private String getActionDescription(String action) {
        // Get description from action registry
        return "Performs the " + action + " operation with the specified parameters.";
    }
}



