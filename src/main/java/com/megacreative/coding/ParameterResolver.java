package com.megacreative.coding;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.coding.values.types.*;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

/**
 * Advanced parameter resolver that handles DataValue resolution with variable support
 * Supports placeholder resolution, variable references, and type-safe operations
 */
public class ParameterResolver {
    
    private final VariableManager variableManager;
    
    public ParameterResolver(VariableManager variableManager) {
        this.variableManager = variableManager;
    }
    
    /**
     * Resolves a parameter value with full DataValue support
     * @param context Execution context
     * @param parameterValue Raw parameter value (can be DataValue, String, or Object)
     * @return Resolved DataValue
     */
    public DataValue resolve(ExecutionContext context, Object parameterValue) {
        if (parameterValue == null) {
            return new AnyValue(null);
        }
        
        // If already DataValue, process for variables and placeholders
        if (parameterValue instanceof DataValue dataValue) {
            return resolveDataValue(context, dataValue);
        }
        
        // Convert to DataValue and resolve
        DataValue dataValue = DataValue.fromObject(parameterValue);
        return resolveDataValue(context, dataValue);
    }
    
    /**
     * Resolves DataValue with variable substitution and placeholder processing
     */
    private DataValue resolveDataValue(ExecutionContext context, DataValue value) {
        if (value == null) return new AnyValue(null);
        
        // Handle text values with placeholders and variables
        if (value instanceof TextValue textValue) {
            String resolved = resolveString(context, textValue.asString());
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
    private String resolveString(ExecutionContext context, String input) {
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
        result = resolveVariableReferences(context, result);
        
        return result;
    }
    
    /**
     * Resolves variable references in format ${variableName}
     */
    private String resolveVariableReferences(ExecutionContext context, String input) {
        // Pattern: ${variableName} or ${scope:variableName}
        String result = input;
        
        // Simple regex replacement for ${...}
        while (result.contains("${") && result.contains("}")) {
            int start = result.indexOf("${");
            int end = result.indexOf("}", start);
            
            if (end > start) {
                String varRef = result.substring(start + 2, end);
                DataValue varValue = getVariable(context, varRef);
                String replacement = varValue != null ? varValue.asString() : "";
                
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
        return getVariable(context, varName);
    }
    
    /**
     * Gets a variable value from VariableManager
     */
    private DataValue getVariable(ExecutionContext context, String variableName) {
        if (variableManager == null) {
            return new AnyValue(null);
        }
        
        // Parse scope:name format
        String[] parts = variableName.split(":", 2);
        if (parts.length == 2) {
            // Scoped variable: scope:name
            String scope = parts[0].toLowerCase();
            String name = parts[1];
            
            switch (scope) {
                case "local":
                    return variableManager.getVariable(name, context.getScriptId(), null);
                case "world":
                    return variableManager.getVariable(name, null, context.getWorldId());
                case "player":
                    return variableManager.getVariable(name, context.getPlayer().getUniqueId().toString(), null);
                case "server":
                    return variableManager.getPersistentVariable(name);
                default:
                    return variableManager.getVariable(name, context.getScriptId(), context.getWorldId());
            }
        } else {
            // Unscoped variable - use priority order
            return variableManager.getVariable(variableName, context.getScriptId(), context.getWorldId());
        }
    }
    
    /**
     * Static method for backward compatibility
     */
    public static String resolveString(ExecutionContext context, Object parameterValue) {
        // Create temporary resolver without variable manager for legacy support
        ParameterResolver resolver = new ParameterResolver(null);
        DataValue result = resolver.resolve(context, parameterValue);
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