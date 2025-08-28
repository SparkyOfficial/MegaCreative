package com.megacreative.coding.groups;

import com.megacreative.coding.CodeBlock;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced block group with enhanced functionality including nesting, templates, and execution control
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdvancedBlockGroup extends BlockGroup {
    
    // Advanced features
    private final Map<String, Object> metadata = new ConcurrentHashMap<>();
    private final List<UUID> nestedGroups = new ArrayList<>();
    private final Set<String> tags = new HashSet<>();
    private String templateId; // Reference to a template this group was created from
    private boolean isTemplate = false; // Whether this group is a template
    private boolean isLocked = false; // Whether this group is locked from editing
    private ExecutionMode executionMode = ExecutionMode.SEQUENTIAL; // How blocks in this group are executed
    private int executionLimit = -1; // Maximum number of executions (-1 for unlimited)
    private int executionCount = 0; // Current execution count
    
    // Conditional execution
    private String conditionExpression; // Expression that must be true for group to execute
    private List<UUID> dependencies = new ArrayList<>(); // Groups that must execute before this one
    
    // Versioning
    private String version = "1.0";
    private long lastModified = System.currentTimeMillis();
    
    public AdvancedBlockGroup(UUID id, String name, UUID owner, 
                             Map<Location, CodeBlock> blocks, 
                             BlockGroupManager.GroupBounds bounds) {
        super(id, name, owner, blocks, bounds);
    }
    
    /**
     * Adds a nested group
     */
    public void addNestedGroup(UUID groupId) {
        if (!nestedGroups.contains(groupId)) {
            nestedGroups.add(groupId);
            touch();
        }
    }
    
    /**
     * Removes a nested group
     */
    public void removeNestedGroup(UUID groupId) {
        if (nestedGroups.remove(groupId)) {
            touch();
        }
    }
    
    /**
     * Adds a tag to this group
     */
    public void addTag(String tag) {
        tags.add(tag.toLowerCase());
        touch();
    }
    
    /**
     * Removes a tag from this group
     */
    public void removeTag(String tag) {
        if (tags.remove(tag.toLowerCase())) {
            touch();
        }
    }
    
    /**
     * Checks if this group has a specific tag
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
     * Removes a metadata value
     */
    public void removeMetadata(String key) {
        if (metadata.remove(key) != null) {
            touch();
        }
    }
    
    /**
     * Adds a dependency
     */
    public void addDependency(UUID groupId) {
        if (!dependencies.contains(groupId)) {
            dependencies.add(groupId);
            touch();
        }
    }
    
    /**
     * Removes a dependency
     */
    public void removeDependency(UUID groupId) {
        if (dependencies.remove(groupId)) {
            touch();
        }
    }
    
    /**
     * Checks if this group can be executed based on its conditions
     */
    public boolean canExecute() {
        // Check execution limit
        if (executionLimit > 0 && executionCount >= executionLimit) {
            return false;
        }
        
        // Check if locked
        if (isLocked) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Increments execution count
     */
    public void incrementExecutionCount() {
        executionCount++;
        touch();
    }
    
    /**
     * Resets execution count
     */
    public void resetExecutionCount() {
        executionCount = 0;
        touch();
    }
    
    /**
     * Updates the last modified timestamp
     */
    @Override
    public void touch() {
        super.touch();
        this.lastModified = System.currentTimeMillis();
    }
    
    /**
     * Gets the display info for this advanced group
     */
    @Override
    public String getDisplayInfo() {
        String baseInfo = super.getDisplayInfo();
        String advancedInfo = String.format(" [Tags: %d, Nested: %d]", tags.size(), nestedGroups.size());
        String statusInfo = isLocked ? " §c[Locked]" : "";
        return baseInfo + advancedInfo + statusInfo;
    }
    
    /**
     * Creates a copy of this group
     */
    public AdvancedBlockGroup copy(String newName, UUID newOwnerId) {
        AdvancedBlockGroup copy = new AdvancedBlockGroup(
            UUID.randomUUID(),
            newName != null ? newName : this.getName() + "_copy",
            newOwnerId != null ? newOwnerId : this.getOwner(),
            new HashMap<>(this.getBlocks()),
            this.getBounds()
        );
        
        // Copy advanced properties
        copy.setTemplateId(this.templateId);
        copy.setTemplate(this.isTemplate);
        copy.setLocked(this.isLocked);
        copy.setExecutionMode(this.executionMode);
        copy.setExecutionLimit(this.executionLimit);
        copy.setConditionExpression(this.conditionExpression);
        copy.setVersion(this.version);
        
        // Copy collections
        copy.getNestedGroups().addAll(this.nestedGroups);
        copy.getTags().addAll(this.tags);
        copy.getDependencies().addAll(this.dependencies);
        copy.getMetadata().putAll(this.metadata);
        
        return copy;
    }
    
    /**
     * Execution modes for block groups
     */
    public enum ExecutionMode {
        SEQUENTIAL,    // Execute blocks in order
        PARALLEL,      // Execute all blocks simultaneously
        RANDOM,        // Execute blocks in random order
        CONDITIONAL    // Execute blocks based on individual conditions
    }
}