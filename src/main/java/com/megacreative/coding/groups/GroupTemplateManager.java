package com.megacreative.coding.groups;

import com.megacreative.coding.CodeBlock;
import org.bukkit.Location;
import java.util.logging.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages group templates for creating reusable block group configurations
 */
public class GroupTemplateManager {
    private static final Logger log = Logger.getLogger(GroupTemplateManager.class.getName());
    
    private final BlockGroupManager groupManager;
    private final Map<String, GroupTemplate> templates = new ConcurrentHashMap<>();
    
    public GroupTemplateManager(BlockGroupManager groupManager) {
        this.groupManager = groupManager;
    }
    
    /**
     * Creates a template from an advanced group
     */
    public GroupTemplate createTemplateFromGroup(AdvancedBlockGroup group, String templateName, String description) {
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }
        
        if (templates.containsKey(templateName)) {
            throw new IllegalArgumentException("Template with name " + templateName + " already exists");
        }
        
        GroupTemplate template = new GroupTemplate(templateName, description, group);
        templates.put(templateName, template);
        
        log.info("Created template: " + templateName + " from group " + group.getName());
        return template;
    }
    
    /**
     * Creates a template from a set of blocks
     */
    public GroupTemplate createTemplateFromBlocks(String templateName, String description, 
                                                Map<Location, CodeBlock> blocks) {
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }
        
        if (templates.containsKey(templateName)) {
            throw new IllegalArgumentException("Template with name " + templateName + " already exists");
        }
        
        GroupTemplate template = new GroupTemplate(templateName, description, blocks);
        templates.put(templateName, template);
        
        log.info("Created template: " + templateName + " from " + blocks.size() + " blocks");
        return template;
    }
    
    /**
     * Gets a template by name
     */
    public GroupTemplate getTemplate(String templateName) {
        return templates.get(templateName);
    }
    
    /**
     * Lists all templates
     */
    public List<GroupTemplate> listTemplates() {
        return new ArrayList<>(templates.values());
    }
    
    /**
     * Lists templates by tag
     */
    public List<GroupTemplate> listTemplatesByTag(String tag) {
        List<GroupTemplate> result = new ArrayList<>();
        for (GroupTemplate template : templates.values()) {
            if (template.hasTag(tag)) {
                result.add(template);
            }
        }
        return result;
    }
    
    /**
     * Deletes a template
     */
    public void deleteTemplate(String templateName) {
        GroupTemplate removed = templates.remove(templateName);
        if (removed != null) {
            log.info("Deleted template: " + templateName);
        }
    }
    
    /**
     * Updates a template's description
     */
    public void updateTemplateDescription(String templateName, String newDescription) {
        GroupTemplate template = templates.get(templateName);
        if (template != null) {
            template.setDescription(newDescription);
            log.info("Updated template description: " + templateName);
        }
    }
    
    /**
     * Adds a tag to a template
     */
    public void addTagToTemplate(String templateName, String tag) {
        GroupTemplate template = templates.get(templateName);
        if (template != null) {
            template.addTag(tag);
            log.info("Added tag '" + tag + "' to template: " + templateName);
        }
    }
    
    /**
     * Removes a tag from a template
     */
    public void removeTagFromTemplate(String templateName, String tag) {
        GroupTemplate template = templates.get(templateName);
        if (template != null) {
            template.removeTag(tag);
            log.info("Removed tag '" + tag + "' from template: " + templateName);
        }
    }
    
    /**
     * Represents a reusable group template
     */
    public static class GroupTemplate {
        private final String name;
        private String description;
        private final Map<Location, CodeBlock> templateBlocks;
        private final Set<String> tags = new HashSet<>();
        private final Map<String, Object> metadata = new ConcurrentHashMap<>();
        private final String version;
        private final long createdTime;
        private long lastModified;
        
        public GroupTemplate(String name, String description, AdvancedBlockGroup sourceGroup) {
            this.name = name;
            this.description = description != null ? description : "";
            this.templateBlocks = new HashMap<>(sourceGroup.getBlocks());
            this.version = sourceGroup.getVersion();
            this.createdTime = System.currentTimeMillis();
            this.lastModified = this.createdTime;
            
            // Copy tags from source group
            this.tags.addAll(sourceGroup.getTags());
            
            // Copy metadata from source group
            this.metadata.putAll(sourceGroup.getMetadata());
        }
        
        public GroupTemplate(String name, String description, Map<Location, CodeBlock> blocks) {
            this.name = name;
            this.description = description != null ? description : "";
            this.templateBlocks = new HashMap<>(blocks);
            this.version = "1.0";
            this.createdTime = System.currentTimeMillis();
            this.lastModified = this.createdTime;
        }
        
        /**
         * Instantiates this template into a new advanced group
         */
        public AdvancedBlockGroup instantiate(String groupName, java.util.UUID ownerId) {
            // Create new group from template blocks
            AdvancedBlockGroup group = new AdvancedBlockGroup(
                java.util.UUID.randomUUID(),
                groupName,
                ownerId,
                new HashMap<>(this.templateBlocks),
                calculateBounds(this.templateBlocks.keySet())
            );
            
            // Apply template properties
            group.setTemplateId(this.name);
            group.setVersion(this.version);
            
            // Copy all tags individually to ensure they're properly added
            for (String tag : this.tags) {
                group.addTag(tag);
            }
            
            // Copy all metadata individually to ensure proper handling
            for (Map.Entry<String, Object> entry : this.metadata.entrySet()) {
                group.setMetadata(entry.getKey(), entry.getValue());
            }
            
            return group;
        }
        
        /**
         * Adds a tag to this template
         */
        public void addTag(String tag) {
            tags.add(tag.toLowerCase());
            touch();
        }
        
        /**
         * Removes a tag from this template
         */
        public void removeTag(String tag) {
            if (tags.remove(tag.toLowerCase())) {
                touch();
            }
        }
        
        /**
         * Checks if this template has a specific tag
         */
        public boolean hasTag(String tag) {
            return tags.contains(tag.toLowerCase());
        }
        
        /**
         * Sets a metadata value
         */
        public void setMetadata(String key, Object value) {
            metadata.put(key, value);
            touch();
        }
        
        /**
         * Gets a metadata value
         */
        @SuppressWarnings("unchecked")
        public <T> T getMetadata(String key, Class<T> type) {
            Object value = metadata.get(key);
            if (value != null && type.isInstance(value)) {
                return (T) value;
            }
            return null;
        }
        
        /**
         * Updates the last modified timestamp
         */
        private void touch() {
            this.lastModified = System.currentTimeMillis();
        }
        
        /**
         * Gets template information
         */
        public String getInfo() {
            return String.format("Template '%s' (%d blocks, v%s) - %s", 
                               name, templateBlocks.size(), version, description);
        }
        
        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; touch(); }
        public Map<Location, CodeBlock> getTemplateBlocks() { return templateBlocks; }
        public Set<String> getTags() { return tags; }
        public Map<String, Object> getMetadata() { return metadata; }
        public String getVersion() { return version; }
        public long getCreatedTime() { return createdTime; }
        public long getLastModified() { return lastModified; }
        
        /**
         * Calculates bounds for a set of locations
         */
        private BlockGroupManager.GroupBounds calculateBounds(Set<Location> locations) {
            if (locations.isEmpty()) {
                return new BlockGroupManager.GroupBounds(0, 0, 0, 0, 0, 0);
            }
            
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            
            for (Location loc : locations) {
                minX = Math.min(minX, loc.getBlockX());
                minY = Math.min(minY, loc.getBlockY());
                minZ = Math.min(minZ, loc.getBlockZ());
                maxX = Math.max(maxX, loc.getBlockX());
                maxY = Math.max(maxY, loc.getBlockY());
                maxZ = Math.max(maxZ, loc.getBlockZ());
            }
            
            return new BlockGroupManager.GroupBounds(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}