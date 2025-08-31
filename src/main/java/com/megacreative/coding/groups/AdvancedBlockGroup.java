package com.megacreative.coding.groups;

import com.megacreative.coding.CodeBlock;
import java.util.Objects;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced block group with enhanced functionality including nesting, templates, and execution control
 */
public class AdvancedBlockGroup extends BlockGroup {
    
    // Getters and Setters
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata.clear(); this.metadata.putAll(metadata); }
    
    public List<UUID> getNestedGroups() { return new ArrayList<>(nestedGroups); }
    public void setNestedGroups(List<UUID> nestedGroups) { this.nestedGroups.clear(); this.nestedGroups.addAll(nestedGroups); }
    
    public Set<String> getTags() { return new HashSet<>(tags); }
    public void setTags(Set<String> tags) { this.tags.clear(); this.tags.addAll(tags); }
    
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    
    public boolean isTemplate() { return isTemplate; }
    public void setTemplate(boolean template) { isTemplate = template; }
    
    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }
    
    public ExecutionMode getExecutionMode() { return executionMode; }
    public void setExecutionMode(ExecutionMode executionMode) { this.executionMode = executionMode; }
    
    public int getExecutionLimit() { return executionLimit; }
    public void setExecutionLimit(int executionLimit) { this.executionLimit = executionLimit; }
    
    public int getExecutionCount() { return executionCount; }
    public void setExecutionCount(int executionCount) { this.executionCount = executionCount; }
    
    public String getConditionExpression() { return conditionExpression; }
    public void setConditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; }
    
    public List<UUID> getDependencies() { return new ArrayList<>(dependencies); }
    public void setDependencies(List<UUID> dependencies) { this.dependencies.clear(); this.dependencies.addAll(dependencies); }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AdvancedBlockGroup that = (AdvancedBlockGroup) o;
        return isTemplate == that.isTemplate && 
               isLocked == that.isLocked && 
               executionLimit == that.executionLimit && 
               executionCount == that.executionCount &&
               Objects.equals(metadata, that.metadata) &&
               Objects.equals(nestedGroups, that.nestedGroups) &&
               Objects.equals(tags, that.tags) &&
               Objects.equals(templateId, that.templateId) &&
               executionMode == that.executionMode &&
               Objects.equals(conditionExpression, that.conditionExpression) &&
               Objects.equals(dependencies, that.dependencies) &&
               Objects.equals(version, that.version);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), metadata, nestedGroups, tags, templateId, isTemplate, 
                          isLocked, executionMode, executionLimit, executionCount, 
                          conditionExpression, dependencies, version);
    }
    
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
        // Create a deep copy of the blocks map
        Map<Location, CodeBlock> blocksCopy = new HashMap<>();
        for (Map.Entry<Location, CodeBlock> entry : this.getBlocks().entrySet()) {
            blocksCopy.put(entry.getKey().clone(), entry.getValue());
        }
        
        // Create a new group with the copied blocks
        AdvancedBlockGroup copy = new AdvancedBlockGroup(
            UUID.randomUUID(),
            newName != null ? newName : this.getName() + "_copy",
            newOwnerId != null ? newOwnerId : this.getOwner(),
            blocksCopy,
            this.getBounds()
        );
        
        // Copy advanced properties
        copy.setTemplateId(this.getTemplateId());
        copy.setTemplate(this.isTemplate());
        copy.setLocked(this.isLocked());
        copy.setExecutionMode(this.getExecutionMode());
        copy.setExecutionLimit(this.getExecutionLimit());
        copy.setConditionExpression(this.getConditionExpression());
        copy.setVersion(this.getVersion());
        copy.setExecutionCount(0); // Reset execution count for the copy
        
        // Copy collections (create new instances to avoid reference sharing)
        copy.setNestedGroups(new ArrayList<>(this.nestedGroups));
        copy.setTags(new HashSet<>(this.tags));
        copy.setDependencies(new ArrayList<>(this.dependencies));
        
        // Deep copy metadata
        Map<String, Object> metadataCopy = new ConcurrentHashMap<>();
        for (Map.Entry<String, Object> entry : this.metadata.entrySet()) {
            // For deep copying, we need to handle different types appropriately
            Object value = entry.getValue();
            if (value instanceof Collection) {
                value = new ArrayList<>((Collection<?>) value);
            } else if (value instanceof Map) {
                value = new HashMap<>((Map<?, ?>) value);
            }
            metadataCopy.put(entry.getKey(), value);
        }
        copy.setMetadata(metadataCopy);
        
        // Copy last modified time
        copy.lastModified = this.lastModified;
        
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