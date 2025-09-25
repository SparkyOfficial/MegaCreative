package com.megacreative.coding.groups;

import com.megacreative.coding.CodeBlock;
import org.bukkit.Location;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.logging.Level;

/**
 * Manages group templates for creating reusable block group configurations
 */
public class GroupTemplateManager {
    private static final Logger log = Logger.getLogger(GroupTemplateManager.class.getName());
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final BlockGroupManager groupManager;
    private final Map<String, GroupTemplate> templates = new ConcurrentHashMap<>();
    private final String templateDirectory;
    
    public GroupTemplateManager(final BlockGroupManager groupManager) {
        this.groupManager = groupManager;
        this.templateDirectory = "templates"; // Default template directory
        loadTemplatesFromDisk();
    }
    
    public GroupTemplateManager(final BlockGroupManager groupManager, final String templateDirectory) {
        this.groupManager = groupManager;
        this.templateDirectory = templateDirectory != null ? templateDirectory : "templates";
        loadTemplatesFromDisk();
    }
    
    /**
     * Creates a template from an advanced group
     */
    public GroupTemplate createTemplateFromGroup(final AdvancedBlockGroup group, final String templateName, final String description) {
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }
        
        if (templates.containsKey(templateName)) {
            throw new IllegalArgumentException("Template with name " + templateName + " already exists");
        }
        
        GroupTemplate template = new GroupTemplate(templateName, description, group);
        templates.put(templateName, template);
        
        // Save template to disk
        saveTemplateToDisk(template);
        
        log.info("Created template: " + templateName + " from group " + group.getName());
        return template;
    }
    
    /**
     * Creates a template from a set of blocks
     */
    public GroupTemplate createTemplateFromBlocks(final String templateName, final String description, 
                                                final Map<Location, CodeBlock> blocks) {
        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }
        
        if (templates.containsKey(templateName)) {
            throw new IllegalArgumentException("Template with name " + templateName + " already exists");
        }
        
        GroupTemplate template = new GroupTemplate(templateName, description, blocks);
        templates.put(templateName, template);
        
        // Save template to disk
        saveTemplateToDisk(template);
        
        log.info("Created template: " + templateName + " from " + blocks.size() + " blocks");
        return template;
    }
    
    /**
     * Gets a template by name
     */
    public GroupTemplate getTemplate(final String templateName) {
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
    public List<GroupTemplate> listTemplatesByTag(final String tag) {
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
    public void deleteTemplate(final String templateName) {
        GroupTemplate removed = templates.remove(templateName);
        if (removed != null) {
            // Delete template file from disk
            deleteTemplateFile(templateName);
            log.info("Deleted template: " + templateName);
        }
    }
    
    /**
     * Updates a template's description
     */
    public void updateTemplateDescription(final String templateName, final String newDescription) {
        GroupTemplate template = templates.get(templateName);
        if (template != null) {
            template.setDescription(newDescription);
            // Save updated template to disk
            saveTemplateToDisk(template);
            log.info("Updated template description: " + templateName);
        }
    }
    
    /**
     * Adds a tag to a template
     */
    public void addTagToTemplate(final String templateName, final String tag) {
        GroupTemplate template = templates.get(templateName);
        if (template != null) {
            template.addTag(tag);
            // Save updated template to disk
            saveTemplateToDisk(template);
            log.info("Added tag '" + tag + "' to template: " + templateName);
        }
    }
    
    /**
     * Removes a tag from a template
     */
    public void removeTagFromTemplate(final String templateName, final String tag) {
        GroupTemplate template = templates.get(templateName);
        if (template != null) {
            template.removeTag(tag);
            // Save updated template to disk
            saveTemplateToDisk(template);
            log.info("Removed tag '" + tag + "' from template: " + templateName);
        }
    }
    
    /**
     * Saves all templates to disk
     */
    public void saveAllTemplates() {
        for (GroupTemplate template : templates.values()) {
            saveTemplateToDisk(template);
        }
        log.info("Saved " + templates.size() + " templates to disk");
    }
    
    /**
     * Loads all templates from disk
     */
    public void loadTemplatesFromDisk() {
        // Create template directory if it doesn't exist
        Path templatePath = Paths.get(templateDirectory);
        if (!Files.exists(templatePath)) {
            try {
                Files.createDirectories(templatePath);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed to create template directory: " + templateDirectory, e);
                return;
            }
        }
        
        // Load all template files
        try {
            Files.list(templatePath)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(this::loadTemplateFromFile);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to list template files in directory: " + templateDirectory, e);
        }
        
        log.info("Loaded " + templates.size() + " templates from disk");
    }
    
    /**
     * Saves a template to disk as JSON
     */
    private void saveTemplateToDisk(GroupTemplate template) {
        try {
            // Create template directory if it doesn't exist
            Path templatePath = Paths.get(templateDirectory);
            if (!Files.exists(templatePath)) {
                Files.createDirectories(templatePath);
            }
            
            // Convert template to JSON
            String json = GSON.toJson(template);
            
            // Write to file
            Path filePath = templatePath.resolve(template.getName() + ".json");
            Files.write(filePath, json.getBytes());
            
            log.fine("Saved template to disk: " + template.getName());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to save template to disk: " + template.getName(), e);
        }
    }
    
    /**
     * Loads a template from a JSON file
     */
    private void loadTemplateFromFile(Path filePath) {
        try {
            // Read JSON from file
            String json = new String(Files.readAllBytes(filePath));
            
            // Convert JSON to template
            GroupTemplate template = GSON.fromJson(json, GroupTemplate.class);
            
            // Add to templates map
            if (template != null && template.getName() != null) {
                templates.put(template.getName(), template);
                log.fine("Loaded template from disk: " + template.getName());
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to load template from file: " + filePath, e);
        }
    }
    
    /**
     * Deletes a template file from disk
     */
    private void deleteTemplateFile(String templateName) {
        try {
            Path filePath = Paths.get(templateDirectory, templateName + ".json");
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.fine("Deleted template file: " + templateName);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to delete template file: " + templateName, e);
        }
    }
    
    /**
     * Exports a template to a file
     */
    public void exportTemplate(String templateName, String exportPath) {
        GroupTemplate template = templates.get(templateName);
        if (template == null) {
            log.warning("Template not found: " + templateName);
            return;
        }
        
        try {
            // Convert template to JSON
            String json = GSON.toJson(template);
            
            // Write to export file
            Path filePath = Paths.get(exportPath);
            Files.write(filePath, json.getBytes());
            
            log.info("Exported template: " + templateName + " to " + exportPath);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to export template: " + templateName, e);
        }
    }
    
    /**
     * Imports a template from a file
     */
    public GroupTemplate importTemplate(String importPath) {
        try {
            // Read JSON from file
            Path filePath = Paths.get(importPath);
            String json = new String(Files.readAllBytes(filePath));
            
            // Convert JSON to template
            GroupTemplate template = GSON.fromJson(json, GroupTemplate.class);
            
            // Add to templates map
            if (template != null && template.getName() != null) {
                templates.put(template.getName(), template);
                saveTemplateToDisk(template); // Save to standard location
                log.info("Imported template: " + template.getName());
                return template;
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to import template from: " + importPath, e);
        }
        
        return null;
    }
    
    /**
     * Calculates bounds for a set of locations
     */
    private static BlockGroupManager.GroupBounds calculateBounds(Set<Location> locations) {
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
        private String author; // Author of the template
        private int usageCount; // How many times this template has been used
        
        public GroupTemplate(final String name, final String description, final AdvancedBlockGroup sourceGroup) {
            this.name = name;
            this.description = description != null ? description : "";
            this.templateBlocks = new HashMap<>(sourceGroup.getBlocks());
            this.version = sourceGroup.getVersion();
            this.createdTime = System.currentTimeMillis();
            this.lastModified = this.createdTime;
            this.author = "Unknown";
            this.usageCount = 0;
            
            // Copy tags from source group
            this.tags.addAll(sourceGroup.getTags());
            
            // Copy metadata from source group
            this.metadata.putAll(sourceGroup.getMetadata());
        }
        
        public GroupTemplate(final String name, final String description, final Map<Location, CodeBlock> blocks) {
            this.name = name;
            this.description = description != null ? description : "";
            this.templateBlocks = new HashMap<>(blocks);
            this.version = "1.0";
            this.createdTime = System.currentTimeMillis();
            this.lastModified = this.createdTime;
            this.author = "Unknown";
            this.usageCount = 0;
        }
        
        /**
         * Instantiates this template into a new advanced group
         */
        public AdvancedBlockGroup instantiate(final String groupName, final UUID ownerId) {
            // Create new group from template blocks
            AdvancedBlockGroup group = new AdvancedBlockGroup(
                UUID.randomUUID(),
                groupName,
                ownerId,
                new HashMap<>(this.templateBlocks),
                calculateBounds(this.templateBlocks.keySet()) // Fixed method call
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
            
            // Increment usage count
            this.usageCount++;
            
            return group;
        }
        
        /**
         * Adds a tag to this template
         */
        public void addTag(final String tag) {
            if (tag != null && !tag.trim().isEmpty()) {
                tags.add(tag.trim());
                lastModified = System.currentTimeMillis();
            }
        }
        
        /**
         * Removes a tag from this template
         */
        public void removeTag(final String tag) {
            if (tag != null) {
                tags.remove(tag);
                lastModified = System.currentTimeMillis();
            }
        }
        
        /**
         * Checks if this template has a specific tag
         */
        public boolean hasTag(final String tag) {
            return tag != null && tags.contains(tag);
        }
        
        /**
         * Gets all tags for this template
         */
        public Set<String> getTags() {
            return new HashSet<>(tags);
        }
        
        /**
         * Sets metadata for this template
         */
        public void setMetadata(final String key, final Object value) {
            if (key != null) {
                if (value != null) {
                    metadata.put(key, value);
                } else {
                    metadata.remove(key);
                }
                lastModified = System.currentTimeMillis();
            }
        }
        
        /**
         * Gets metadata for this template
         */
        public Object getMetadata(final String key) {
            return key != null ? metadata.get(key) : null;
        }
        
        /**
         * Gets all metadata for this template
         */
        public Map<String, Object> getAllMetadata() {
            return new HashMap<>(metadata);
        }
        
        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public void setDescription(final String description) { 
            this.description = description != null ? description : "";
            this.lastModified = System.currentTimeMillis();
        }
        public Map<Location, CodeBlock> getTemplateBlocks() { return new HashMap<>(templateBlocks); }
        public String getVersion() { return version; }
        public long getCreatedTime() { return createdTime; }
        public long getLastModified() { return lastModified; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public int getUsageCount() { return usageCount; }
        public void setUsageCount(int usageCount) { this.usageCount = usageCount; }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupTemplate that = (GroupTemplate) o;
            return Objects.equals(name, that.name);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
        

        
        @Override
        public String toString() {
            return "GroupTemplate{" +
                   "name='" + name + '\'' +
                   ", description='" + description + '\'' +
                   ", version='" + version + '\'' +
                   ", tags=" + tags +
                   ", author='" + author + '\'' +
                   ", usageCount=" + usageCount +
                   '}';
        }
    }
}