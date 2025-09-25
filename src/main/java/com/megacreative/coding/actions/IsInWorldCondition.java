package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        if (context == null) {
            logEvaluationFailure("Context is null", null, null);
            return false;
        }
        
        Player player = context.getPlayer();
        
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
            LOGGER.log(Level.FINE, "[IsInWorld] Player '{0}' is in the correct world: {1}", 
                new Object[]{player.getName(), worldName});
        }
        
        return result;
    }
    
    /**
     * Logs evaluation failures with detailed information.
     * @param message The error message
     * @param playerName The name of the player involved, or null if not available
     * @param context The execution context, or null if not available
     */
    private static final Logger LOGGER = Logger.getLogger(IsInWorldCondition.class.getName());
    
    private void logEvaluationFailure(String message, String playerName, ExecutionContext context) {
        String logMessage = String.format("[IsInWorld] Condition failed: %s%s", 
            message,
            playerName != null ? " (Player: " + playerName + ")" : "");
        
        if (context != null && context.getPlugin() != null) {
            LOGGER.log(Level.WARNING, logMessage);
        } else {
            LOGGER.log(Level.WARNING, logMessage);
        }
    }
}