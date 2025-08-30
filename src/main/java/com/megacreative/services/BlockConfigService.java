package com.megacreative.services;

import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Service for loading and managing block configurations from coding_blocks.yml
 * Replaces hardcoded material mappings with config-driven approach
 */
public class BlockConfigService {
    
    private static final Logger log = Logger.getLogger(BlockConfigService.class.getName());
    
    private final Plugin plugin;
    private final Map<Material, BlockConfig> materialToConfig = new HashMap<>();
    private final Set<Material> codeBlocks = new HashSet<>();
    private YamlConfiguration config;
    
    public BlockConfigService(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Loads block configurations from coding_blocks.yml
     */
    public void loadConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "coding_blocks.yml");
            
            if (!configFile.exists()) {
                plugin.saveResource("coding_blocks.yml", false);
            }
            
            config = YamlConfiguration.loadConfiguration(configFile);
            parseBlockConfigurations();
            
            log.info("Loaded " + materialToConfig.size() + " block configurations from coding_blocks.yml");
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to load block configurations", e);
        }
    }
    
    /**
     * Parses block configurations from the YAML config
     */
    private void parseBlockConfigurations() {
        materialToConfig.clear();
        codeBlocks.clear();
        
        ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
        if (blocksSection == null) {
            log.warning("No 'blocks' section found in coding_blocks.yml");
            return;
        }
        
        for (String materialName : blocksSection.getKeys(false)) {
            try {
                Material material = Material.valueOf(materialName);
                ConfigurationSection blockSection = blocksSection.getConfigurationSection(materialName);
                
                if (blockSection != null) {
                    BlockConfig blockConfig = parseBlockConfig(material, blockSection);
                    materialToConfig.put(material, blockConfig);
                    codeBlocks.add(material);
                }
                
            } catch (IllegalArgumentException e) {
                log.warning("Invalid material in config: " + materialName);
            }
        }
    }
    
    /**
     * Parses individual block configuration
     */
    private BlockConfig parseBlockConfig(Material material, ConfigurationSection section) {
        String name = section.getString("name", material.name());
        String description = section.getString("description", "");
        String type = section.getString("type", "ACTION"); // Default to ACTION if not specified
        List<String> actions = section.getStringList("actions");
        
        // Get default action (first action in the list)
        String defaultAction = actions.isEmpty() ? null : actions.get(0);
        
        return new BlockConfig(material, name, description, type, actions, defaultAction);
    }
    
    /**
     * Gets the default action for a material
     */
    public String getDefaultAction(Material material) {
        BlockConfig config = materialToConfig.get(material);
        return config != null ? config.getDefaultAction() : null;
    }
    
    /**
     * Gets all available actions for a material
     */
    public List<String> getAvailableActions(Material material) {
        BlockConfig config = materialToConfig.get(material);
        return config != null ? config.getActions() : Collections.emptyList();
    }
    
    /**
     * Checks if a material is a valid code block
     */
    public boolean isCodeBlock(Material material) {
        return codeBlocks.contains(material);
    }
    
    /**
     * Gets all valid code block materials
     */
    public Set<Material> getCodeBlockMaterials() {
        return new HashSet<>(codeBlocks);
    }
    
    /**
     * Gets block configuration for a material
     */
    public BlockConfig getBlockConfig(Material material) {
        return materialToConfig.get(material);
    }
    
    /**
     * Gets the display name for a material
     */
    public String getBlockName(Material material) {
        BlockConfig config = materialToConfig.get(material);
        return config != null ? config.getName() : material.name();
    }
    
    /**
     * Gets the block category/type for a material
     */
    public String getBlockCategory(Material material) {
        BlockConfig config = materialToConfig.get(material);
        return config != null ? config.getType() : "ACTION";
    }
    
    /**
     * Checks if a block category is a control or event block
     */
    public boolean isControlOrEventBlock(String category) {
        return "EVENT".equals(category) || "CONTROL".equals(category) || "FUNCTION".equals(category);
    }
    
    /**
     * Checks if a material is a control or event block
     */
    public boolean isControlOrEventBlock(Material material) {
        String category = getBlockCategory(material);
        return isControlOrEventBlock(category);
    }
    
    /**
     * Reloads the configuration
     */
    public void reload() {
        loadConfig();
    }
    
    /**
     * Gets slot number for action and group
     */
    public int getSlotNumber(String action, String group) {
        // Default slot assignment logic
        int hash = (action + group).hashCode();
        return Math.abs(hash % 54); // GUI has 54 slots
    }
    
    /**
     * Gets available slots for a group
     */
    public List<Integer> getSlotsForGroup(String action, String group) {
        List<Integer> slots = new ArrayList<>();
        int baseSlot = getSlotNumber(action, group);
        // Return 9 consecutive slots for the group
        for (int i = 0; i < 9; i++) {
            slots.add((baseSlot + i) % 54);
        }
        return slots;
    }
    
    /**
     * Gets action slot configuration
     */
    public Map<String, Object> getActionSlotConfig(String action) {
        Map<String, Object> config = new HashMap<>();
        config.put("name", action);
        config.put("slot", getSlotNumber(action, "default"));
        config.put("material", "STONE");
        return config;
    }
    
    /**
     * Gets action group configuration
     */
    public Map<String, Object> getActionGroupConfig(String action) {
        Map<String, Object> config = new HashMap<>();
        config.put("name", action + " Group");
        config.put("actions", Arrays.asList(action));
        config.put("slots", getSlotsForGroup(action, "group"));
        return config;
    }
    
    /**
     * Creates placeholder item for slot configuration
     */
    public org.bukkit.inventory.ItemStack createPlaceholderItem(String name, int slot) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.GRAY_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7" + name);
            meta.setLore(Arrays.asList("§8Slot: " + slot));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Creates group placeholder item
     */
    public org.bukkit.inventory.ItemStack createGroupPlaceholderItem(String groupName, String action) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.YELLOW_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + groupName);
            meta.setLore(Arrays.asList("§8Action: " + action, "§8Group placeholder"));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Block configuration data class
     */
    public static class BlockConfig {
        private final Material material;
        private final String name;
        private final String description;
        private final String type;
        private final List<String> actions;
        private final String defaultAction;
        
        public BlockConfig(Material material, String name, String description, String type,
                          List<String> actions, String defaultAction) {
            this.material = material;
            this.name = name;
            this.description = description;
            this.type = type;
            this.actions = new ArrayList<>(actions);
            this.defaultAction = defaultAction;
        }
        
        public Material getMaterial() { return material; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getType() { return type; }
        public List<String> getActions() { return actions; }
        public String getDefaultAction() { return defaultAction; }
        
        /**
         * Checks if this block supports a specific action
         */
        public boolean supportsAction(String action) {
            return actions.contains(action);
        }
        
        @Override
        public String toString() {
            return "BlockConfig{" +
                    "material=" + material +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", defaultAction='" + defaultAction + '\'' +
                    ", actions=" + actions.size() +
                    '}';
        }
    }
}