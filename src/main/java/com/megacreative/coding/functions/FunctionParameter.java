package com.megacreative.coding.functions;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import java.util.Objects;

/**
 * Represents a function parameter with name, type, and default value
 */
public class FunctionParameter {
    private String name;
    private ValueType expectedType;
    private DataValue defaultValue;
    private boolean required;
    private String description;
    
    
    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    
    public ValueType getExpectedType() { return expectedType; }
    public void setExpectedType(final ValueType expectedType) { this.expectedType = expectedType; }
    
    public DataValue getDefaultValue() { return defaultValue; }
    public void setDefaultValue(final DataValue defaultValue) { this.defaultValue = defaultValue; }
    
    public boolean isRequired() { return required; }
    public void setRequired(final boolean required) { this.required = required; }
    
    public String getDescription() { return description; }
    public void setDescription(final String description) { this.description = description; }
    
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FunctionParameter that = (FunctionParameter) o;
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
    
    
    public FunctionParameter() {
    }
    
    public FunctionParameter(final String name, final ValueType expectedType, final boolean required) {
        this.name = name;
        this.expectedType = expectedType;
        this.required = required;
        this.description = "";
    }
    
    public FunctionParameter(final String name, final ValueType expectedType, final DataValue defaultValue, final String description) {
        this.name = name;
        this.expectedType = expectedType;
        this.defaultValue = defaultValue;
        this.required = defaultValue == null;
        this.description = description != null ? description : "";
    }
    
    /**
     * Validates if a value is compatible with this parameter
     *
     * @param value the value to check compatibility for
     * @return true if the value is compatible with this parameter, false otherwise
     */
    public boolean isCompatible(final DataValue value) {
        if (value == null) {
            return !required;
        }
        return expectedType == ValueType.ANY || value.getType() == expectedType || expectedType.isCompatible(value.getType());
    }
    
    /**
     * Gets the value to use for this parameter, applying defaults if needed
     *
     * @param providedValue the value provided for this parameter
     * @return the effective value for this parameter
     * @throws IllegalArgumentException if a required parameter is not provided
     */
    public DataValue getEffectiveValue(final DataValue providedValue) {
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