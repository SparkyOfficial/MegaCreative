package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Wrapper class for BlockConfigService to maintain backward compatibility
 * with the old BlockConfiguration API while using the new configuration system.
 */
public class BlockConfiguration {
    
    private final MegaCreative plugin;
    private BlockConfigService blockConfigService;
    
    public BlockConfiguration(MegaCreative plugin) {
        this.plugin = plugin;
        // Initialize with the new service
        this.blockConfigService = plugin.getServiceRegistry().getService(BlockConfigService.class);
    }
    
    /**
     * Reloads the configuration from the new service
     */
    public void loadConfiguration() {
        if (blockConfigService != null) {
            blockConfigService.reload();
            plugin.getLogger().info("Block configuration reloaded using new configuration system");
        }
    }
    
    /**
     * Gets the block configuration for a material
     * @param material The material to get configuration for
     * @return The block configuration or null if not found
     */
    public BlockConfig getBlockConfig(Material material) {
        if (blockConfigService == null) return null;
        
        // Get the first block config for this material for backward compatibility
        com.megacreative.services.BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(material);
        if (config == null) return null;
        
        return new BlockConfig(
            config.getDisplayName(),
            config.getDescription(),
            new ArrayList<>(config.getParameters().keySet()) // Use parameter names as actions for compatibility
        );
    }
    
    /**
     * Gets the list of actions for a material
     * @param material The material to get actions for
     * @return List of actions or null if not found
     */
    public List<String> getActionsForMaterial(Material material) {
        if (blockConfigService == null) return null;
        
        com.megacreative.services.BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(material);
        if (config == null) return null;
        
        return new ArrayList<>(config.getParameters().keySet());
    }
    
    /**
     * Gets the slot number for an action and slot name
     * @param actionName The action name
     * @param slotName The slot name
     * @return The slot number or null if not found
     */
    public Integer getSlotNumber(String actionName, String slotName) {
        if (blockConfigService == null) return null;
        
        com.megacreative.services.BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(actionName);
        if (config == null) return null;
        
        com.megacreative.services.BlockConfigService.ParameterConfig paramConfig = config.getParameter(slotName);
        if (paramConfig == null) return null;
        
        return paramConfig.getSlot();
    }
    
    /**
     * Gets the slots for a group
     * @param actionName The action name
     * @param groupName The group name
     * @return List of slot numbers
     */
    public List<Integer> getSlotsForGroup(String actionName, String groupName) {
        // In the new system, we don't have groups in the same way, so we'll return a default list
        // This is for backward compatibility only
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            slots.add(i);
        }
        return slots;
    }
    
    /**
     * Creates a placeholder item for a slot
     * @param actionName The action name
     * @param slotNumber The slot number
     * @return The placeholder item
     */
    public ItemStack createPlaceholderItem(String actionName, int slotNumber) {
        // Create a simple placeholder item
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7Slot " + slotNumber);
            List<String> lore = new ArrayList<>();
            lore.add("§8Action: " + actionName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Creates a group placeholder item
     * @param actionName The action name
     * @param groupName The group name
     * @return The group placeholder item
     */
    public ItemStack createGroupPlaceholderItem(String actionName, String groupName) {
        // Create a simple group placeholder item
        ItemStack item = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + groupName);
            List<String> lore = new ArrayList<>();
            lore.add("§8Action: " + actionName);
            lore.add("§8Group placeholder");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Gets the action slot configuration
     * @param actionName The action name
     * @return The action slot configuration
     */
    public ActionSlotConfig getActionSlotConfig(String actionName) {
        if (blockConfigService == null) return null;
        
        com.megacreative.services.BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(actionName);
        if (config == null) return null;
        
        Map<Integer, SlotConfig> slots = new HashMap<>();
        for (com.megacreative.services.BlockConfigService.ParameterConfig param : config.getParameters().values()) {
            slots.put(param.getSlot(), new SlotConfig(
                param.getName(),
                param.getDescription(),
                "STONE", // Default material
                param.getName()
            ));
        }
        
        return new ActionSlotConfig(slots);
    }
    
    /**
     * Gets the action group configuration
     * @param actionName The action name
     * @return The action group configuration
     */
    public ActionGroupConfig getActionGroupConfig(String actionName) {
        // Return a simple group config for backward compatibility
        Map<String, GroupConfig> groups = new HashMap<>();
        groups.put("default", new GroupConfig(
            Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8),
            "Default Group",
            "Default group for " + actionName,
            "CHEST"
        ));
        return new ActionGroupConfig(groups);
    }
    
    /**
     * Inner class for block configuration
     */
    public static class BlockConfig {
        private final String name;
        private final String description;
        private final List<String> actions;
        
        public BlockConfig(String name, String description, List<String> actions) {
            this.name = name;
            this.description = description;
            this.actions = actions;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public List<String> getActions() {
            return actions;
        }
    }
    
    /**
     * Inner class for slot configuration
     */
    public static class SlotConfig {
        private final String name;
        private final String description;
        private final String placeholderItem;
        private final String slotName;
        
        public SlotConfig(String name, String description, String placeholderItem, String slotName) {
            this.name = name;
            this.description = description;
            this.placeholderItem = placeholderItem;
            this.slotName = slotName;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getPlaceholderItem() {
            return placeholderItem;
        }
        
        public String getSlotName() {
            return slotName;
        }
    }
    
    /**
     * Inner class for group configuration
     */
    public static class GroupConfig {
        private final List<Integer> slots;
        private final String name;
        private final String description;
        private final String placeholderItem;
        
        public GroupConfig(List<Integer> slots, String name, String description, String placeholderItem) {
            this.slots = slots;
            this.name = name;
            this.description = description;
            this.placeholderItem = placeholderItem;
        }
        
        public List<Integer> getSlots() {
            return slots;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getPlaceholderItem() {
            return placeholderItem;
        }
    }
    
    /**
     * Inner class for action slot configuration
     */
    public static class ActionSlotConfig {
        private final Map<Integer, SlotConfig> slots;
        
        public ActionSlotConfig(Map<Integer, SlotConfig> slots) {
            this.slots = slots;
        }
        
        public Map<Integer, SlotConfig> getSlots() {
            return slots;
        }
    }
    
    /**
     * Inner class for action group configuration
     */
    public static class ActionGroupConfig {
        private final Map<String, GroupConfig> groups;
        
        public ActionGroupConfig(Map<String, GroupConfig> groups) {
            this.groups = groups;
        }
        
        public Map<String, GroupConfig> getGroups() {
            return groups;
        }
    }
}