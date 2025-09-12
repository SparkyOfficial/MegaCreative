package com.megacreative.coding;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.coding.placeholders.FrameLandPlaceholderResolver;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 🎆 ENHANCED: Legacy placeholder resolver - now delegates to FrameLand system
 * Supports player-related placeholders like %player%, %world%, etc.
 * Also supports variable placeholders like ${variable_name}.
 * 
 * @deprecated Use FrameLandPlaceholderResolver for better functionality
 */
@Deprecated
public class PlaceholderResolver {
    
    private static final Pattern PLAYER_PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
    private static final Pattern VARIABLE_PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    /**
     * 🎆 ENHANCED: Resolves all placeholders using FrameLand system
     * @param input The input string with placeholders
     * @param context The execution context containing player and variables
     * @return The string with all placeholders resolved
     */
    public static String resolvePlaceholders(String input, ExecutionContext context) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // Delegate to FrameLand placeholder resolver for comprehensive support
        return FrameLandPlaceholderResolver.resolvePlaceholders(input, context);
    }
    
    /**
     * Resolves player-related placeholders like %player%, %world%, %x%, %y%, %z%
     * @param input The input string with placeholders
     * @param context The execution context containing player information
     * @return The string with player placeholders resolved
     */
    private static String resolvePlayerPlaceholders(String input, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return input;
        }
        
        String result = input;
        Location location = player.getLocation();
        
        // Replace common player placeholders
        result = result.replace("%player%", player.getName())
                      .replace("%world%", player.getWorld().getName())
                      .replace("%x%", String.valueOf(location.getBlockX()))
                      .replace("%y%", String.valueOf(location.getBlockY()))
                      .replace("%z%", String.valueOf(location.getBlockZ()));
        
        return result;
    }
    
    /**
     * Resolves variable placeholders like ${variable_name}
     * @param input The input string with placeholders
     * @param context The execution context containing variables
     * @return The string with variable placeholders resolved
     */
    private static String resolveVariablePlaceholders(String input, ExecutionContext context) {
        // Find all variable placeholders
        Matcher matcher = VARIABLE_PLACEHOLDER_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            
            // Get the variable value from the context
            DataValue value = context.getPlugin().getVariableManager().getVariable(variableName, 
                com.megacreative.coding.variables.IVariableManager.VariableScope.PLAYER, 
                context.getPlayer() != null ? context.getPlayer().getUniqueId().toString() : null);
            
            // Replace with the variable value or empty string if not found
            String replacement = value != null ? value.asString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Resolves a single placeholder
     * @param placeholder The placeholder to resolve (without % or ${})
     * @param context The execution context
     * @return The resolved value or the original placeholder if not found
     */
    public static String resolvePlaceholder(String placeholder, ExecutionContext context) {
        // Try player placeholders first
        Player player = context.getPlayer();
        if (player != null) {
            Location location = player.getLocation();
            
            switch (placeholder.toLowerCase()) {
                case "player":
                    return player.getName();
                case "world":
                    return player.getWorld().getName();
                case "x":
                    return String.valueOf(location.getBlockX());
                case "y":
                    return String.valueOf(location.getBlockY());
                case "z":
                    return String.valueOf(location.getBlockZ());
            }
        }
        
        // Try variable placeholders
        DataValue value = context.getPlugin().getVariableManager().getVariable(placeholder, 
            com.megacreative.coding.variables.IVariableManager.VariableScope.PLAYER, 
            context.getPlayer() != null ? context.getPlayer().getUniqueId().toString() : null);
        if (value != null) {
            return value.asString();
        }
        
        // Return the original placeholder if not found
        return "%" + placeholder + "%";
    }
}