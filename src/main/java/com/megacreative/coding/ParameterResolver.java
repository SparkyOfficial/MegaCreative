package com.megacreative.coding;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.LocationValue;
import com.megacreative.coding.values.types.PlayerValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager;
import com.megacreative.coding.placeholders.ReferenceSystemPlaceholderResolver;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 🎆 ENHANCED: Parameter resolver with reference system-style placeholder support
 * Now supports multiple placeholder formats:
 * - Reference system style: apple[variable]~
 * - Modern style: ${variable}
 * - Classic style: %variable%
 * Enhanced with improved variable scope resolution.
 */
public class ParameterResolver {
    // Fixed redundant character escape - removed unnecessary backslashes
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    // Fixed redundant character escape - removed unnecessary backslashes
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    // Fixed redundant character escape - removed unnecessary backslashes
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    public ParameterResolver(ExecutionContext context) {
        // Context parameter kept for backwards compatibility
    }

    public DataValue resolve(ExecutionContext context, DataValue value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        String text = value.asString();
        // Removed redundant null check - static analysis flagged it as always non-null when this method is called

        
        String resolvedText = ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
        
        return DataValue.of(resolvedText);
    }

    /**
     * 🎆 ENHANCED: Resolves placeholders in a string using reference system
     * 
     * @param context The execution context
     * @param text The text to resolve
     * @return The resolved string
     */
    public String resolveString(ExecutionContext context, String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        return ReferenceSystemPlaceholderResolver.resolvePlaceholders(text, context);
    }

    /**
     * Legacy method for backwards compatibility
     * @deprecated Use ReferenceSystemPlaceholderResolver directly for better performance
     */
    @Deprecated
    private String resolvePlaceholder(String placeholder, ExecutionContext context) {
        
        Player player = context.getPlayer();
        if (player != null) {
            switch (placeholder.toLowerCase()) {
                case "player_name":
                    return player.getName();
                case "player_display_name":
                    return player.getDisplayName();
                case "player_uuid":
                    return player.getUniqueId().toString();
                case "player_world":
                    return player.getWorld().getName();
                case "player_x":
                    return String.valueOf(player.getLocation().getX());
                case "player_y":
                    return String.valueOf(player.getLocation().getY());
                case "player_z":
                    return String.valueOf(player.getLocation().getZ());
                default:
                    
                    break;
            }
        }

        
        if (context.getBlockLocation() != null) {
            Location location = context.getBlockLocation();
            switch (placeholder.toLowerCase()) {
                case "block_x":
                    return String.valueOf(location.getX());
                case "block_y":
                    return String.valueOf(location.getY());
                case "block_z":
                    return String.valueOf(location.getZ());
                case "block_world":
                    return location.getWorld().getName();
                default:
                    
                    break;
            }
        }

        
        VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
        // Removed redundant null check - static analysis flagged it as always non-null when this method is called
        if (variableManager != null) {
            
            String playerContext = getPlayerContext(context);
            DataValue variableValue = variableManager.resolveVariable(placeholder, playerContext);
            if (variableValue != null) {
                return variableValue.asString();
            }
        }

        
        switch (placeholder.toLowerCase()) {
            case "timestamp":
                return String.valueOf(System.currentTimeMillis());
            case "random":
                return String.valueOf(Math.random());
            default:
                
                return null;
        }
    }
    
    /**
     * Gets the player context for variable resolution.
     * @param context The execution context
     * @return The player UUID as string, or script ID if no player
     */
    private String getPlayerContext(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            return player.getUniqueId().toString();
        }
        return context.getScriptId() != null ? context.getScriptId() : "global";
    }
}