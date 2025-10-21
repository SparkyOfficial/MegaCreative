package com.megacreative.coding.events;

import com.megacreative.coding.values.DataValue;

import java.util.*;

/**
 * Builder for creating complex custom events with fluent API
 */
public class CustomEventBuilder {
    private final String name;
    private String author = "system";
    private String description = "";
    private String category = "general";
    private boolean isGlobal = false;
    private boolean isOneTime = false;
    private int priority = 0;
    private final Set<String> tags = new HashSet<>();
    private final Map<String, CustomEvent.EventDataField> dataFields = new LinkedHashMap<>();
    
    public CustomEventBuilder(String name) {
        this.name = name;
    }
    
    /**
     * Sets the author of the event
     */
    public CustomEventBuilder author(String author) {
        this.author = author;
        return this;
    }
    
    /**
     * Sets the description of the event
     */
    public CustomEventBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * Sets the category of the event
     */
    public CustomEventBuilder category(String category) {
        this.category = category;
        return this;
    }
    
    /**
     * Makes the event global (can be triggered/handled across worlds)
     */
    public CustomEventBuilder global() {
        this.isGlobal = true;
        return this;
    }
    
    /**
     * Makes the event one-time (automatically removed after first trigger)
     */
    public CustomEventBuilder oneTime() {
        this.isOneTime = true;
        return this;
    }
    
    /**
     * Sets the priority of the event
     */
    public CustomEventBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }
    
    /**
     * Adds a tag to the event
     */
    public CustomEventBuilder tag(String tag) {
        this.tags.add(tag.toLowerCase());
        return this;
    }
    
    /**
     * Adds multiple tags to the event
     */
    public CustomEventBuilder tags(String... tags) {
        for (String tag : tags) {
            this.tags.add(tag.toLowerCase());
        }
        return this;
    }
    
    /**
     * Adds a required data field to the event
     */
    public CustomEventBuilder requiredField(String fieldName, Class<?> fieldType, String description) {
        CustomEvent.EventDataField field = new CustomEvent.EventDataField(fieldName, fieldType, true, description);
        this.dataFields.put(fieldName, field);
        return this;
    }
    
    /**
     * Adds an optional data field to the event
     */
    public CustomEventBuilder optionalField(String fieldName, Class<?> fieldType, String description) {
        CustomEvent.EventDataField field = new CustomEvent.EventDataField(fieldName, fieldType, false, description);
        this.dataFields.put(fieldName, field);
        return this;
    }
    
    /**
     * Adds an optional data field with default value to the event
     */
    public CustomEventBuilder optionalField(String fieldName, Class<?> fieldType, DataValue defaultValue, String description) {
        CustomEvent.EventDataField field = new CustomEvent.EventDataField(fieldName, fieldType, false, description);
        field.setDefaultValue(defaultValue);
        this.dataFields.put(fieldName, field);
        return this;
    }
    
    /**
     * Adds a string field to the event
     */
    public CustomEventBuilder stringField(String fieldName, boolean required, String description) {
        return required ? 
            requiredField(fieldName, String.class, description) : 
            optionalField(fieldName, String.class, description);
    }
    
    /**
     * Adds an integer field to the event
     */
    public CustomEventBuilder intField(String fieldName, boolean required, String description) {
        return required ? 
            requiredField(fieldName, Integer.class, description) : 
            optionalField(fieldName, Integer.class, description);
    }
    
    /**
     * Adds a boolean field to the event
     */
    public CustomEventBuilder boolField(String fieldName, boolean required, String description) {
        return required ? 
            requiredField(fieldName, Boolean.class, description) : 
            optionalField(fieldName, Boolean.class, description);
    }
    
    /**
     * Adds a player field to the event
     */
    public CustomEventBuilder playerField(String fieldName, boolean required, String description) {
        return required ? 
            requiredField(fieldName, org.bukkit.entity.Player.class, description) : 
            optionalField(fieldName, org.bukkit.entity.Player.class, description);
    }
    
    /**
     * Adds a location field to the event
     */
    public CustomEventBuilder locationField(String fieldName, boolean required, String description) {
        return required ? 
            requiredField(fieldName, org.bukkit.Location.class, description) : 
            optionalField(fieldName, org.bukkit.Location.class, description);
    }
    
    /**
     * Builds the custom event
     */
    public CustomEvent build() {
        CustomEvent event = new CustomEvent(name, author);
        event.setDescription(description);
        event.setCategory(category);
        event.setGlobal(isGlobal);
        event.setOneTime(isOneTime);
        event.setPriority(priority);
        
        
        for (CustomEvent.EventDataField field : dataFields.values()) {
            if (field.getDefaultValue() != null) {
                event.addDataField(field.getName(), field.getExpectedType(), field.getDefaultValue(), field.getDescription());
            } else {
                event.addDataField(field.getName(), field.getExpectedType(), field.isRequired(), field.getDescription());
            }
        }
        
        
        for (String tag : tags) {
            event.addTag(tag);
        }
        
        return event;
    }
    
    /**
     * Builds and registers the event with the event manager
     */
    public CustomEvent buildAndRegister(CustomEventManager eventManager) {
        CustomEvent event = build();
        eventManager.registerEvent(event);
        return event;
    }
    
    /**
     * Creates a preset for a player-related event
     */
    public static CustomEventBuilder playerEvent(String eventName) {
        return new CustomEventBuilder(eventName)
            .category("player")
            .tag("player");
    }
    
    /**
     * Creates a preset for a world-related event
     */
    public static CustomEventBuilder worldEvent(String eventName) {
        return new CustomEventBuilder(eventName)
            .category("world")
            .tag("world");
    }
    
    /**
     * Creates a preset for a script-related event
     */
    public static CustomEventBuilder scriptEvent(String eventName) {
        return new CustomEventBuilder(eventName)
            .category("script")
            .tag("script");
    }
    
    /**
     * Creates a preset for a timed event
     */
    public static CustomEventBuilder timedEvent(String eventName) {
        return new CustomEventBuilder(eventName)
            .category("timed")
            .tag("timed")
            .global();
    }
}