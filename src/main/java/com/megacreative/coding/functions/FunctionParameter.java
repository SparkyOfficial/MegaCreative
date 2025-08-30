package com.megacreative.coding.functions;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import java.util.*;

/**
 * Represents a function parameter with name, type, and default value
 */
public class FunctionParameter {
    private String name;
    private ValueType expectedType;
    private DataValue defaultValue;
    private boolean required;
    private String description;
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public ValueType getExpectedType() { return expectedType; }
    public void setExpectedType(ValueType expectedType) { this.expectedType = expectedType; }
    
    public DataValue getDefaultValue() { return defaultValue; }
    public void setDefaultValue(DataValue defaultValue) { this.defaultValue = defaultValue; }
    
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionParameter that = (FunctionParameter) o;
        return required == that.required &&
               Objects.equals(name, that.name) &&
               expectedType == that.expectedType &&
               Objects.equals(defaultValue, that.defaultValue) &&
               Objects.equals(description, that.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, expectedType, defaultValue, required, description);
    }
    
    // No-args constructor
    public FunctionParameter() {
    }
    
    public FunctionParameter(String name, ValueType expectedType, boolean required) {
        this.name = name;
        this.expectedType = expectedType;
        this.required = required;
        this.description = "";
    }
    
    public FunctionParameter(String name, ValueType expectedType, DataValue defaultValue, String description) {
        this.name = name;
        this.expectedType = expectedType;
        this.defaultValue = defaultValue;
        this.required = defaultValue == null;
        this.description = description != null ? description : "";
    }
    
    /**
     * Validates if a value is compatible with this parameter
     */
    public boolean isCompatible(DataValue value) {
        if (value == null) return !required;
        return expectedType == ValueType.ANY || value.getType() == expectedType || expectedType.isCompatible(value.getType());
    }
    
    /**
     * Gets the value to use for this parameter, applying defaults if needed
     */
    public DataValue getEffectiveValue(DataValue providedValue) {
        if (providedValue != null && isCompatible(providedValue)) {
            return providedValue;
        }
        
        if (defaultValue != null) {
            return defaultValue;
        }
        
        if (required) {
            throw new IllegalArgumentException("Required parameter '" + name + "' not provided");
        }
        
        return DataValue.fromObject(expectedType.getDefaultValue());
    }
}