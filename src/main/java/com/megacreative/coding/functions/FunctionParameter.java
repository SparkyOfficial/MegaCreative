package com.megacreative.coding.functions;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Represents a function parameter with name, type, and default value
 */
@Data
@NoArgsConstructor
public class FunctionParameter {
    private String name;
    private ValueType expectedType;
    private DataValue defaultValue;
    private boolean required;
    private String description;
    
    public FunctionParameter(String name, ValueType expectedType, boolean required) {
        this.name = name;
        this.expectedType = expectedType;
        this.required = required;
        this.description = \"\";
    }
    
    public FunctionParameter(String name, ValueType expectedType, DataValue defaultValue, String description) {
        this.name = name;
        this.expectedType = expectedType;
        this.defaultValue = defaultValue;
        this.required = defaultValue == null;
        this.description = description != null ? description : \"\";
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
            throw new IllegalArgumentException(\"Required parameter '\" + name + \"' not provided\");
        }
        
        return expectedType.getDefaultValue();
    }
}