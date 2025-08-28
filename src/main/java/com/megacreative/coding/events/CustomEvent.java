package com.megacreative.coding.events;

import com.megacreative.coding.values.DataValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Represents a custom event that can be triggered and handled within scripts
 */
@Data
@NoArgsConstructor
public class CustomEvent {
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
     * Event data field definition
     */
    @Data
    @NoArgsConstructor
    public static class EventDataField {
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
            return expectedType.isAssignableFrom(value.getClass()) || 
                   expectedType == Object.class || 
                   value.getClass() == Object.class;
        }
    }
}