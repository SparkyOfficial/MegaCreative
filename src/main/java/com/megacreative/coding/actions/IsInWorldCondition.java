package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Condition that checks if a player is in a specific world.
 * Handles color codes in world names and provides detailed logging.
 */
public class IsInWorldCondition implements BlockCondition {
    
    /**
     * Removes color codes from a string.
     * @param input The string to strip color codes from
     * @return The string without color codes
     */
    private String stripColorCodes(String input) {
        if (input == null) {
            return null;
        }
        // Remove Minecraft color codes (both § and & formats)
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "");
    }
    @Override
    public boolean evaluate(ExecutionContext context) {
        if (context == null) {
            logEvaluationFailure("Context is null", null, null);
            return false;
        }
        
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null) {
            logEvaluationFailure("Player is null", null, context);
            return false;
        }
        
        if (block == null) {
            logEvaluationFailure("CodeBlock is null", player.getName(), context);
            return false;
        }
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        DataValue rawWorldName = block.getParameter("world");
        if (rawWorldName == null) {
            logEvaluationFailure("World parameter is not set", player.getName(), context);
            return false;
        }
        
        DataValue resolvedWorldValue = resolver.resolve(context, rawWorldName);
        if (resolvedWorldValue == null) {
            logEvaluationFailure("Could not resolve world name", player.getName(), context);
            return false;
        }
        
        String worldName = stripColorCodes(resolvedWorldValue.asString());
        if (worldName == null || worldName.trim().isEmpty()) {
            logEvaluationFailure("World name is empty or invalid", player.getName(), context);
            return false;
        }
        
        String playerWorldName = stripColorCodes(player.getWorld().getName());
        boolean result = worldName.equals(playerWorldName);
        
        if (!result) {
            logEvaluationFailure(String.format("Player '%s' is in world '%s', expected '%s'", 
                player.getName(), playerWorldName, worldName), player.getName(), context);
        } else {
            context.getPlugin().getLogger().fine(String.format("[IsInWorld] Player '%s' is in the correct world: %s", 
                player.getName(), worldName));
        }
        
        return result;
    }
    
    /**
     * Logs evaluation failures with detailed information.
     * @param message The error message
     * @param playerName The name of the player involved, or null if not available
     * @param context The execution context, or null if not available
     */
    private void logEvaluationFailure(String message, String playerName, ExecutionContext context) {
        StringBuilder logMessage = new StringBuilder("[IsInWorld] Condition failed: ").append(message);
        
        if (playerName != null) {
            logMessage.append(" (Player: ").append(playerName).append(")");
        }
        
        if (context != null && context.getPlugin() != null) {
            context.getPlugin().getLogger().warning(logMessage.toString());
        } else {
            System.err.println(logMessage);
        }
    }
}