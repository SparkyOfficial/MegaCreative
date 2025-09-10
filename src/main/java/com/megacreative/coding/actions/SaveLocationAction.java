package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Action for saving a player's current location.
 * This action saves the player's current location to a variable.
 */
public class SaveLocationAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the location name parameter from the block
            DataValue locationNameValue = block.getParameter("locationName");
            if (locationNameValue == null) {
                return ExecutionResult.error("Location name parameter is missing");
            }

            // Resolve any placeholders in the location name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedLocationName = resolver.resolve(context, locationNameValue);
            
            // Parse location name parameter
            String locationName = resolvedLocationName.asString();
            if (locationName == null || locationName.isEmpty()) {
                return ExecutionResult.error("Location name is empty or null");
            }

            // Get the player's current location
            Location location = player.getLocation();
            
            // Create a string representation of the location
            String locationString = String.format("%s:%.2f:%.2f:%.2f:%.2f:%.2f", 
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());
            
            // Save the location to a variable
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                String scriptId = context.getScriptId() != null ? context.getScriptId() : "global";
                variableManager.setVariable(locationName, DataValue.of(locationString), VariableScope.LOCAL, scriptId);
                return ExecutionResult.success("Saved location '" + locationName + "'");
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to save location: " + e.getMessage());
        }
    }
}