package com.megacreative.coding;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.coding.values.types.*;
import org.bukkit.entity.Player;

/**
 * Advanced parameter resolver that handles DataValue resolution with variable support
 * Supports placeholder resolution, variable references, and type-safe operations
 */
public class ParameterResolver {
    
    private final ExecutionContext context;
    
    public ParameterResolver(ExecutionContext context) {
        this.context = context;
    }
    
    /**
     * Resolves a parameter value with full DataValue support
     * @param parameterValue Raw parameter value (can be DataValue, String, or Object)
     * @return Resolved DataValue
     */
    public DataValue resolve(Object parameterValue) {
        if (parameterValue == null) {
            return new AnyValue(null);
        }
        
        // If already DataValue, process for variables and placeholders
        if (parameterValue instanceof DataValue dataValue) {
            return resolveDataValue(dataValue);
        }
        
        // Convert to DataValue and resolve
        DataValue dataValue = DataValue.fromObject(parameterValue);
        return resolveDataValue(dataValue);
    }
    
    /**
     * Resolves a parameter value with full DataValue support (legacy method)
     * @deprecated Use resolve(Object) instead
     */
    @Deprecated
    public DataValue resolve(ExecutionContext context, Object parameterValue) {
        return resolve(parameterValue);
    }
    
    /**
     * Resolves DataValue with variable substitution and placeholder processing
     */
    private DataValue resolveDataValue(DataValue value) {
        if (value == null) return new AnyValue(null);
        
        // Handle text values with placeholders and variables
        if (value instanceof TextValue textValue) {
            String resolved = resolveString(textValue.asString());
            return new TextValue(resolved);
        }
        
        // Handle variable references (format: ${variableName})
        if (value instanceof TextValue && isVariableReference(value.asString())) {
            return resolveVariableReference(context, value.asString());
        }
        
        return value;
    }
    
    /**
     * Resolves string with placeholders and variable substitution
     */
    private String resolveString(String input) {
        if (input == null) return "";
        
        String result = input;
        
        // Replace player placeholders
        Player player = context.getPlayer();
        if (player != null) {
            result = result.replace("%player%", player.getName())
                          .replace("%player_display%", player.getDisplayName())
                          .replace("%player_uuid%", player.getUniqueId().toString())
                          .replace("%world%", player.getWorld().getName());
        }
        
        // Replace variable references ${varName}
        result = resolveVariableReferences(result);
        
        return result;
    }
    
    /**
     * Resolves variable references in format ${variableName}
     */
    private String resolveVariableReferences(String input) {
        // Pattern: ${variableName} or ${scope:variableName}
        String result = input;
        
        // Simple regex replacement for ${...}
        while (result.contains("${") && result.contains("}")) {
            int start = result.indexOf("${");
            int end = result.indexOf("}", start);
            
            if (end > start) {
                String varRef = result.substring(start + 2, end);
                Object varValue = getVariable(varRef);
                String replacement = varValue != null ? varValue.toString() : "";
                
                result = result.substring(0, start) + replacement + result.substring(end + 1);
            } else {
                break; // Malformed reference
            }
        }
        
        return result;
    }
    
    /**
     * Checks if a string is a direct variable reference
     */
    private boolean isVariableReference(String value) {
        return value != null && value.startsWith("${") && value.endsWith("}");
    }
    
    /**
     * Resolves a direct variable reference
     */
    private DataValue resolveVariableReference(ExecutionContext context, String reference) {
        String varName = reference.substring(2, reference.length() - 1);
        Object value = getVariable(varName);
        return DataValue.fromObject(value);
    }
    
    /**
     * Gets a variable value from ExecutionContext
     */
    private Object getVariable(String variableName) {
        if (context == null) {
            return null;
        }
        
        // Parse scope:name format
        String[] parts = variableName.split(":", 2);
        if (parts.length == 2) {
            // Scoped variable: scope:name
            String scope = parts[0].toLowerCase();
            String name = parts[1];
            
            switch (scope) {
                case "local":
                    return context.getVariable(name);
                case "world":
                    return context.getGlobalVariable(name);
                case "player":
                    return context.getPlayerVariable(name);
                case "server":
                    return context.getPersistentVariable(name);
                default:
                    return context.getVariable(name);
            }
        } else {
            // Unscoped variable - use default scope
            return context.getVariable(variableName);
        }
    }
    
    /**
     * Static method for backward compatibility
     */
    public static String resolveString(ExecutionContext context, Object parameterValue) {
        if (context == null) {
            return parameterValue != null ? parameterValue.toString() : "";
        }
        ParameterResolver resolver = new ParameterResolver(context);
        DataValue result = resolver.resolve(parameterValue);
        return result.asString();
    }
    
    /**
     * Checks if value contains variables or placeholders
     */
    public static boolean requiresResolution(Object value) {
        if (!(value instanceof String str)) return false;
        return str.contains("%") || str.contains("${");
    }
}