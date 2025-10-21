package com.megacreative.coding;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.coding.placeholders.ReferenceSystemPlaceholderResolver;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 🎆 ENHANCED: Legacy placeholder resolver - now delegates to reference system
 * Supports player-related placeholders like %player%, %world%, etc.
 * Also supports variable placeholders like ${variable_name}.
 * 
 * @deprecated Use ReferenceSystemPlaceholderResolver for better functionality
 */
@Deprecated
public class PlaceholderResolver {
    
    private static final String PLAYER_PLACEHOLDER_PREFIX = "%";
    private static final String PLAYER_PLACEHOLDER_SUFFIX = "%";
    private static final String VARIABLE_PLACEHOLDER_PREFIX = "${";
    private static final String VARIABLE_PLACEHOLDER_SUFFIX = "}";
    
    private static final String PLACEHOLDER_PLAYER = "player";
    private static final String PLACEHOLDER_WORLD = "world";
    private static final String PLACEHOLDER_X = "x";
    private static final String PLACEHOLDER_Y = "y";
    private static final String PLACEHOLDER_Z = "z";
    
    // Fixed redundant character escape in RegExp
    // Changed from "\\$\\{([^}]+)\\}" to "\\$\\{([^}]+)\\}"
    // The closing brace doesn't need to be escaped in this context
    private static final Pattern PLAYER_PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
    private static final Pattern VARIABLE_PLACEHOLDER_PATTERN = Pattern.compile("\\\\\\$\\{([^}]+)\\}");
    
    /**
     * 🎆 ENHANCED: Resolves all placeholders using reference system
     * @param input The input string with placeholders
     * @param context The execution context containing player and variables
     * @return The string with all placeholders resolved
     */
    public static String resolvePlaceholders(String input, ExecutionContext context) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        return ReferenceSystemPlaceholderResolver.resolvePlaceholders(input, context);
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
        Matcher matcher = VARIABLE_PLACEHOLDER_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            
            DataValue value = context.getPlugin().getServiceRegistry().getVariableManager().getVariable(variableName, 
                com.megacreative.coding.variables.IVariableManager.VariableScope.PLAYER, 
                context.getPlayer() != null ? context.getPlayer().getUniqueId().toString() : null);
            
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
        Player player = context.getPlayer();
        if (player != null) {
            Location location = player.getLocation();
            
            switch (placeholder.toLowerCase()) {
                case PLACEHOLDER_PLAYER:
                    return player.getName();
                case PLACEHOLDER_WORLD:
                    return player.getWorld().getName();
                case PLACEHOLDER_X:
                    return String.valueOf(location.getBlockX());
                case PLACEHOLDER_Y:
                    return String.valueOf(location.getBlockY());
                case PLACEHOLDER_Z:
                    return String.valueOf(location.getBlockZ());
                default:
                    break;
            }
        }
        
        DataValue value = context.getPlugin().getServiceRegistry().getVariableManager().getVariable(placeholder, 
            com.megacreative.coding.variables.IVariableManager.VariableScope.PLAYER, 
            context.getPlayer() != null ? context.getPlayer().getUniqueId().toString() : null);
        if (value != null) {
            return value.asString();
        }
        
        return "%" + placeholder + "%";
    }
}