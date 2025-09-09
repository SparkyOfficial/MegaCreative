package com.megacreative.coding;

import com.megacreative.coding.variables.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for resolving parameters with placeholders.
 * This class handles the resolution of DataValue objects that may contain placeholders.
 */
public class ParameterResolver {
    
    private final ExecutionContext context;
    
    public ParameterResolver(ExecutionContext context) {
        this.context = context;
    }
    
    /**
     * Resolves a DataValue that may contain placeholders
     * @param context The execution context
     * @param value The DataValue to resolve
     * @return The resolved DataValue
     */
    public DataValue resolve(ExecutionContext context, DataValue value) {
        if (value == null) {
            return value;
        }
        
        // If it's a string value, resolve placeholders in it
        if (value.isText()) {
            String text = value.asString();
            String resolvedText = PlaceholderResolver.resolvePlaceholders(text, context);
            return new DataValue(resolvedText);
        }
        
        // For other types, return as is
        return value;
    }
    
    /**
     * Resolves a string that may contain placeholders
     * @param input The input string with placeholders
     * @return The string with all placeholders resolved
     */
    public String resolveString(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        return PlaceholderResolver.resolvePlaceholders(input, context);
    }
}