package com.megacreative.coding.containers;

import com.megacreative.coding.values.ValueType;

/**
 * Action parameter definition
 */
public class ActionParameter {
    private final String name;
    private final ValueType type;
    private final String description;
    
    public ActionParameter(String name, ValueType type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }
    
    public String getName() { return name; }
    public ValueType getType() { return type; }
    public String getDescription() { return description; }
}