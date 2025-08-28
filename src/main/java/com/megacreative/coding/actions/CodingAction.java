package com.megacreative.coding.actions;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Base class for all coding actions that can be executed by code blocks
 */
@Data
public abstract class CodingAction {
    
    protected String name;
    protected String displayName;
    protected String description;
    protected String category;
    protected List<ActionParameter> parameters = new ArrayList<>();
    protected ValueType returnType = ValueType.ANY;
    protected boolean async = false;
    protected int executionTime = 0; // Estimated execution time in milliseconds
    
    public CodingAction(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.category = "general";
    }
    
    /**
     * Executes the action with the given parameters
     */
    public abstract void execute(Player player, Map<String, DataValue> parameters);
    
    /**
     * Validates if the provided parameters are valid for this action
     */
    public abstract boolean validate(Map<String, DataValue> parameters);
    
    /**
     * Adds a parameter to this action
     */
    public CodingAction addParameter(String name, ValueType type, boolean required, String description) {
        parameters.add(new ActionParameter(name, type, required, description));
        return this;
    }
    
    /**
     * Adds a parameter with default value
     */
    public CodingAction addParameter(String name, ValueType type, DataValue defaultValue, String description) {
        ActionParameter param = new ActionParameter(name, type, defaultValue == null, description);
        param.setDefaultValue(defaultValue);
        parameters.add(param);
        return this;
    }
    
    /**
     * Gets a parameter by name
     */
    public Optional<ActionParameter> getParameter(String name) {
        return parameters.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst();
    }
    
    /**
     * Sets the return type for this action
     */
    public CodingAction setReturnType(ValueType returnType) {
        this.returnType = returnType;
        return this;
    }
    
    /**
     * Sets the category for this action
     */
    public CodingAction setCategory(String category) {
        this.category = category;
        return this;
    }
    
    /**
     * Sets whether this action should run asynchronously
     */
    public CodingAction setAsync(boolean async) {
        this.async = async;
        return this;
    }
    
    /**
     * Gets the action signature as a string
     */
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(returnType.name().toLowerCase()).append(" ");
        sb.append(name).append("(");
        
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            ActionParameter param = parameters.get(i);
            sb.append(param.getType().name().toLowerCase());
            sb.append(" ").append(param.getName());
            if (!param.isRequired()) {
                sb.append("?");
            }
        }
        
        sb.append(")");
        return sb.toString();
    }
    
    /**
     * Action parameter definition
     */
    @Data
    public static class ActionParameter {
        private final String name;
        private final ValueType type;
        private final boolean required;
        private final String description;
        private DataValue defaultValue;
        
        public ActionParameter(String name, ValueType type, boolean required, String description) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.description = description != null ? description : "";
        }
        
        /**
         * Validates if a value is compatible with this parameter
         */
        public boolean isCompatible(DataValue value) {
            if (value == null) return !required;
            return type == ValueType.ANY || value.getType() == type || type.isCompatible(value.getType());
        }
        
        /**
         * Gets the effective value for this parameter
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
            
            return DataValue.fromObject(type.getDefaultValue());
        }
    }
}