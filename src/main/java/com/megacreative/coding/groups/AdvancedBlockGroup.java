package com.megacreative.coding.groups;

import com.megacreative.coding.CodeBlock;
import java.util.Objects;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced block group with enhanced functionality including nesting, templates, and execution control
 * 
 * Розширена група блоків з розширеними функціями, включаючи вкладеність та контроль виконання
 * 
 * Erweiterte Blockgruppe mit erweiterten Funktionen, einschließlich Verschachtelung und Ausführungssteuerung
 * 
 * @author Андрій Будильников
 */
public class AdvancedBlockGroup extends BlockGroup {
    
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata.clear(); this.metadata.putAll(metadata); }
    
    public List<UUID> getNestedGroups() { return new ArrayList<>(nestedGroups); }
    public void setNestedGroups(List<UUID> nestedGroups) { this.nestedGroups.clear(); this.nestedGroups.addAll(nestedGroups); }
    
    public Set<String> getTags() { return new HashSet<>(tags); }
    public void setTags(Set<String> tags) { this.tags.clear(); this.tags.addAll(tags); }
    
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    
    
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
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AdvancedBlockGroup that = (AdvancedBlockGroup) o;
        return isLocked == that.isLocked &&
               executionMode == that.executionMode &&
               executionLimit == that.executionLimit &&
               executionCount == that.executionCount &&
               Objects.equals(metadata, that.metadata) &&
               Objects.equals(nestedGroups, that.nestedGroups) &&
               Objects.equals(tags, that.tags) &&
               Objects.equals(templateId, that.templateId) &&
               Objects.equals(conditionExpression, that.conditionExpression) &&
               Objects.equals(dependencies, that.dependencies) &&
               Objects.equals(version, that.version);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), metadata, nestedGroups, tags, templateId, 
                isLocked, executionMode, executionLimit, executionCount, 
                conditionExpression, dependencies, version);
    }
    
    
    private final Map<String, Object> metadata = new ConcurrentHashMap<>();
    private final List<UUID> nestedGroups = new ArrayList<>();
    private final Set<String> tags = new HashSet<>();
    private String templateId; 
    private boolean isLocked = false; 
    private ExecutionMode executionMode = ExecutionMode.SEQUENTIAL; 
    private int executionLimit = -1; 
    private int executionCount = 0; 
    
    
    private String conditionExpression; 
    private List<UUID> dependencies = new ArrayList<>(); 
    
    
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
        if (type.isInstance(value)) {
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
        
        if (executionLimit > 0 && executionCount >= executionLimit) {
            return false;
        }
        
        
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
        
        Map<Location, CodeBlock> blocksCopy = new HashMap<>();
        for (Map.Entry<Location, CodeBlock> entry : this.getBlocks().entrySet()) {
            blocksCopy.put(entry.getKey().clone(), entry.getValue());
        }
        
        
        AdvancedBlockGroup copy = new AdvancedBlockGroup(
            UUID.randomUUID(),
            newName != null ? newName : this.getName() + "_copy",
            newOwnerId != null ? newOwnerId : this.getOwner(),
            blocksCopy,
            this.getBounds()
        );
        
        
        copy.setTemplateId(this.getTemplateId());
        return copy;
    }
    
    /**
     * Execution modes for block groups
     */
    public enum ExecutionMode {
        SEQUENTIAL,    
        PARALLEL,      
        RANDOM,        
        CONDITIONAL    
    }
}