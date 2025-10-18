package com.megacreative.coding.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents the configuration for a block type.
 */
public class BlockConfig implements ConfigurationSerializable {
    private String id;
    private String name;
    private Material material;
    private String actionName;
    private String category;
    private int customModelData;
    private List<String> lore;
    private Map<String, Object> parameters;
    private List<String> requiredPermissions;
    private boolean enabled;
    
    /**
     * Creates a new BlockConfig with default values.
     */
    public BlockConfig() {
        this.lore = new ArrayList<>();
        this.parameters = new HashMap<>();
        this.requiredPermissions = new ArrayList<>();
        this.enabled = true;
    }
    
    /**
     * Creates a BlockConfig from a configuration section.
     * 
     * @param section The configuration section to load from
     */
    public BlockConfig(ConfigurationSection section) {
        this();
        this.id = section.getName();
        this.name = section.getString("name", id);
        this.material = Material.matchMaterial(section.getString("material", "STONE"));
        this.actionName = section.getString("action");
        this.category = section.getString("category", "default");
        this.customModelData = section.getInt("custom-model-data", 0);
        this.lore = section.getStringList("lore");
        this.enabled = section.getBoolean("enabled", true);
        
        
        if (section.isConfigurationSection("parameters")) {
            ConfigurationSection paramsSection = section.getConfigurationSection("parameters");
            if (paramsSection != null) {
                for (String key : paramsSection.getKeys(false)) {
                    this.parameters.put(key, paramsSection.get(key));
                }
            }
        }
        
        
        if (section.isList("required-permissions")) {
            this.requiredPermissions = section.getStringList("required-permissions");
        }
    }
    
    /**
     * Saves this BlockConfig to a configuration section.
     * 
     * @param section The configuration section to save to
     */
    public void save(ConfigurationSection section) {
        section.set("name", name);
        
        if (material != null) {
            section.set("material", material.name());
        }
        
        section.set("action", actionName);
        section.set("category", category);
        
        if (customModelData > 0) {
            section.set("custom-model-data", customModelData);
        }
        
        if (lore != null && !lore.isEmpty()) {
            section.set("lore", lore);
        }
        
        section.set("enabled", enabled);
        
        
        if (parameters != null && !parameters.isEmpty()) {
            ConfigurationSection paramsSection = section.createSection("parameters");
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                paramsSection.set(entry.getKey(), entry.getValue());
            }
        }
        
        
        if (requiredPermissions != null && !requiredPermissions.isEmpty()) {
            section.set("required-permissions", requiredPermissions);
        }
    }
    
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        
        if (material != null) {
            result.put("material", material.name());
        }
        
        result.put("action", actionName);
        result.put("category", category);
        
        if (customModelData > 0) {
            result.put("custom-model-data", customModelData);
        }
        
        if (lore != null && !lore.isEmpty()) {
            result.put("lore", lore);
        }
        
        if (parameters != null && !parameters.isEmpty()) {
            result.put("parameters", parameters);
        }
        
        if (requiredPermissions != null && !requiredPermissions.isEmpty()) {
            result.put("required-permissions", requiredPermissions);
        }
        
        result.put("enabled", enabled);
        return result;
    }
    
    
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public void setMaterial(Material material) {
        this.material = material;
    }
    
    public String getActionName() {
        return actionName;
    }
    
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public int getCustomModelData() {
        return customModelData;
    }
    
    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }
    
    public List<String> getLore() {
        return lore;
    }
    
    public void setLore(List<String> lore) {
        this.lore = lore;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public List<String> getRequiredPermissions() {
        return requiredPermissions;
    }
    
    public void setRequiredPermissions(List<String> requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockConfig that = (BlockConfig) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "BlockConfig{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", material=" + material +
                ", actionName='" + actionName + '\'' +
                ", category='" + category + '\'' +
                ", customModelData=" + customModelData +
                ", enabled=" + enabled +
                '}';
    }
}
