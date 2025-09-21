package com.megacreative.coding.events;

import com.megacreative.coding.values.DataValue;

import java.util.*;

/**
 * Represents a custom event that can be triggered and handled within scripts
 */
public class CustomEvent {
    // No-args constructor
    public CustomEvent() {
        this.id = UUID.randomUUID();
        this.createdTime = System.currentTimeMillis();
        this.dataFields = new HashMap<>();
        this.tags = new HashSet<>();
        this.metadata = new HashMap<>();
        this.aliases = new ArrayList<>();
    }
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public Map<String, EventDataField> getDataFields() { return dataFields; }
    public void setDataFields(Map<String, EventDataField> dataFields) { this.dataFields = dataFields; }
    
    public boolean isGlobal() { return isGlobal; }
    public void setGlobal(boolean global) { isGlobal = global; }
    
    public boolean isOneTime() { return isOneTime; }
    public void setOneTime(boolean oneTime) { isOneTime = oneTime; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    
    public String getParentEvent() { return parentEvent; }
    public void setParentEvent(String parentEvent) { this.parentEvent = parentEvent; }
    
    public boolean isAbstract() { return isAbstract; }
    public void setAbstract(boolean anAbstract) { isAbstract = anAbstract; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public List<String> getAliases() { return aliases; }
    public void setAliases(List<String> aliases) { this.aliases = aliases; }
    private UUID id;
    private String name;
    private String description;
    private String category;
    private long createdTime;
    private String author;
    
    // Event data schema - defines what data this event can carry
    private Map<String, EventDataField> dataFields = new HashMap<>();
    
    // Event metadata
    private boolean isGlobal = false; // Global events can be triggered/handled across worlds
    private boolean isOneTime = false; // One-time events are automatically removed after first trigger
    private int priority = 0; // Higher priority events are handled first
    private Set<String> tags = new HashSet<>();
    
    // Advanced features
    private String parentEvent; // Parent event for inheritance
    private boolean isAbstract = false; // Abstract events cannot be directly triggered
    private Map<String, Object> metadata = new HashMap<>(); // Custom metadata
    private List<String> aliases = new ArrayList<>(); // Event name aliases
    
    public CustomEvent(String name, String author) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.author = author;
        this.createdTime = System.currentTimeMillis();
        this.description = "";
        this.category = "general";
    }
    
    /**
     * Adds a data field to this event
     */
    public CustomEvent addDataField(String fieldName, Class<?> fieldType, boolean required, String description) {
        EventDataField field = new EventDataField(fieldName, fieldType, required, description);
        dataFields.put(fieldName, field);
        return this;
    }
    
    /**
     * Adds a data field with default value
     */
    public CustomEvent addDataField(String fieldName, Class<?> fieldType, DataValue defaultValue, String description) {
        EventDataField field = new EventDataField(fieldName, fieldType, defaultValue != null, description);
        field.setDefaultValue(defaultValue);
        dataFields.put(fieldName, field);
        return this;
    }
    
    /**
     * Validates event data against the schema
     */
    public void validateEventData(Map<String, DataValue> eventData) {
        // Check required fields
        for (EventDataField field : dataFields.values()) {
            if (field.isRequired() && !eventData.containsKey(field.getName())) {
                throw new IllegalArgumentException("Missing required field: " + field.getName());
            }
        }
        
        // Check field types
        for (Map.Entry<String, DataValue> entry : eventData.entrySet()) {
            String fieldName = entry.getKey();
            DataValue value = entry.getValue();
            
            EventDataField field = dataFields.get(fieldName);
            if (field == null) {
                throw new IllegalArgumentException("Unknown field: " + fieldName);
            }
            
            if (!field.isCompatible(value)) {
                throw new IllegalArgumentException(
                    "Field '" + fieldName + "' expects " + field.getExpectedType().getSimpleName() + 
                    " but got " + (value != null ? value.getClass().getSimpleName() : "null")
                );
            }
        }
    }
    
    /**
     * Prepares event data with defaults
     */
    public Map<String, DataValue> prepareEventData(Map<String, DataValue> providedData) {
        Map<String, DataValue> effectiveData = new HashMap<>(providedData);
        
        // Apply defaults for missing optional fields
        for (EventDataField field : dataFields.values()) {
            if (!effectiveData.containsKey(field.getName()) && field.getDefaultValue() != null) {
                effectiveData.put(field.getName(), field.getDefaultValue());
            }
        }
        
        return effectiveData;
    }
    
    /**
     * Adds a tag to this event
     */
    public CustomEvent addTag(String tag) {
        this.tags.add(tag.toLowerCase());
        return this;
    }
    
    /**
     * Checks if event has a specific tag
     */
    public boolean hasTag(String tag) {
        return this.tags.contains(tag.toLowerCase());
    }
    
    /**
     * Gets event signature as string
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append("event ").append(name).append("(");
        
        boolean first = true;
        for (EventDataField field : dataFields.values()) {
            if (!first) sb.append(", ");
            sb.append(field.getExpectedType().getSimpleName().toLowerCase());
            sb.append(" ").append(field.getName());
            if (!field.isRequired()) {
                sb.append("?");
            }
            first = false;
        }
        
        sb.append(")");
        return sb.toString();
    }
    
    /**
     * Creates a copy of this event definition
     */
    public CustomEvent copy() {
        CustomEvent copy = new CustomEvent(this.name + "_copy", this.author);
        copy.setDescription(this.description);
        copy.setCategory(this.category);
        copy.setGlobal(this.isGlobal);
        copy.setOneTime(this.isOneTime);
        copy.setPriority(this.priority);
        copy.setParentEvent(this.parentEvent);
        copy.setAbstract(this.isAbstract);
        copy.getMetadata().putAll(this.metadata);
        copy.getAliases().addAll(this.aliases);
        
        // Copy data fields
        for (EventDataField field : this.dataFields.values()) {
            copy.addDataField(field.getName(), field.getExpectedType(), field.isRequired(), field.getDescription());
            if (field.getDefaultValue() != null) {
                copy.dataFields.get(field.getName()).setDefaultValue(field.getDefaultValue());
            }
        }
        
        // Copy tags
        copy.tags.addAll(this.tags);
        
        return copy;
    }
    
    /**
     * Inherits from a parent event, copying its fields and properties
     */
    public CustomEvent inheritFrom(CustomEvent parent) {
        this.parentEvent = parent.getName();
        
        // Copy parent's data fields
        for (EventDataField field : parent.getDataFields().values()) {
            // Only add if not already defined (allow overriding)
            if (!this.dataFields.containsKey(field.getName())) {
                EventDataField copiedField = new EventDataField(
                    field.getName(), 
                    field.getExpectedType(), 
                    field.isRequired(), 
                    field.getDescription()
                );
                if (field.getDefaultValue() != null) {
                    copiedField.setDefaultValue(field.getDefaultValue());
                }
                this.dataFields.put(field.getName(), copiedField);
            }
        }
        
        // Copy parent's tags
        this.tags.addAll(parent.getTags());
        
        // Copy parent's metadata if not already present
        for (Map.Entry<String, Object> entry : parent.getMetadata().entrySet()) {
            if (!this.metadata.containsKey(entry.getKey())) {
                this.metadata.put(entry.getKey(), entry.getValue());
            }
        }
        
        return this;
    }
    
    /**
     * Adds an alias for this event
     */
    public CustomEvent addAlias(String alias) {
        this.aliases.add(alias);
        return this;
    }
    
    /**
     * Sets a metadata value
     */
    public CustomEvent setMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
    
    /**
     * Gets a metadata value
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = this.metadata.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Checks if this event is compatible with another event (inheritance check)
     */
    public boolean isCompatibleWith(String eventName) {
        if (this.name.equals(eventName)) return true;
        if (this.aliases.contains(eventName)) return true;
        if (this.parentEvent != null && this.parentEvent.equals(eventName)) return true;
        return false;
    }
    
    /**
     * Gets the full inheritance chain
     */
    public List<String> getInheritanceChain(CustomEventManager eventManager) {
        List<String> chain = new ArrayList<>();
        CustomEvent current = this;
        
        while (current != null) {
            chain.add(current.getName());
            if (current.getParentEvent() != null) {
                CustomEvent parent = eventManager.getEvents().get(current.getParentEvent());
                current = parent;
            } else {
                current = null;
            }
        }
        
        return chain;
    }
    
    /**
     * Event data field definition
     */
    
    public static class EventDataField {
        // No-args constructor
        public EventDataField() {
            this.name = "";
            this.expectedType = Object.class;
            this.required = false;
            this.description = "";
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Class<?> getExpectedType() { return expectedType; }
        public void setExpectedType(Class<?> expectedType) { this.expectedType = expectedType; }
        
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public DataValue getDefaultValue() { return defaultValue; }
        public void setDefaultValue(DataValue defaultValue) { this.defaultValue = defaultValue; }
        private String name;
        private Class<?> expectedType;
        private boolean required;
        private String description;
        private DataValue defaultValue;

        
        public EventDataField(String name, Class<?> expectedType, boolean required, String description) {
            this.name = name;
            this.expectedType = expectedType;
            this.required = required;
            this.description = description != null ? description : "";
        }
        
        /**
         * Checks if a value is compatible with this field
         */
        public boolean isCompatible(DataValue value) {
            if (value == null) return !required;
            
            // Handle direct type matching first
            if (expectedType.isAssignableFrom(value.getClass())) {
                return true;
            }
            
            // Handle DataValue wrapper to expected type matching
            if (expectedType == org.bukkit.entity.Player.class && value instanceof com.megacreative.coding.values.types.PlayerValue) {
                return true;
            }
            if (expectedType == String.class && value instanceof com.megacreative.coding.values.types.TextValue) {
                return true;
            }
            if ((expectedType == Number.class || expectedType == Integer.class || expectedType == Long.class || expectedType == Double.class) 
                && value instanceof com.megacreative.coding.values.types.NumberValue) {
                return true;
            }
            if (expectedType == Boolean.class && value instanceof com.megacreative.coding.values.types.BooleanValue) {
                return true;
            }
            if (expectedType == org.bukkit.Location.class && value instanceof com.megacreative.coding.values.types.LocationValue) {
                return true;
            }
            if (expectedType == org.bukkit.inventory.ItemStack.class && value instanceof com.megacreative.coding.values.types.ItemValue) {
                return true;
            }
            if (expectedType == java.util.List.class && value instanceof com.megacreative.coding.values.types.ListValue) {
                return true;
            }
            if (expectedType == java.util.Map.class && value instanceof com.megacreative.coding.values.types.MapValue) {
                return true;
            }
            
            // Handle generic object matching
            if (expectedType.equals(Object.class)) {
                return true;
            }
            
            // Check if the underlying value in DataValue is compatible
            if (value.getValue() != null && expectedType.isAssignableFrom(value.getValue().getClass())) {
                return true;
            }
            
            return false;
        }
    }
}