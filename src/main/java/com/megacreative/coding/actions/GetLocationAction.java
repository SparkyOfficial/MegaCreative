package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Action for getting a saved location.
 * This action retrieves a saved location and stores it in a variable.
 */
public class GetLocationAction implements BlockAction {

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

            // Get the target variable name parameter from the block
            DataValue targetValue = block.getParameter("targetVariable");
            if (targetValue == null) {
                return ExecutionResult.error("Target variable name parameter is missing");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedLocationName = resolver.resolve(context, locationNameValue);
            DataValue resolvedTarget = resolver.resolve(context, targetValue);
            
            // Parse parameters
            String locationName = resolvedLocationName.asString();
            if (locationName == null || locationName.isEmpty()) {
                return ExecutionResult.error("Location name is empty or null");
            }
            
            String targetName = resolvedTarget.asString();
            if (targetName == null || targetName.isEmpty()) {
                return ExecutionResult.error("Target variable name is empty or null");
            }

            // Get the location from the variable manager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                // Try to get the location from different scopes
                DataValue locationValue = null;
                
                // Try player scope first if we have a player
                if (context.getPlayer() != null) {
                    locationValue = variableManager.getVariable(locationName, VariableScope.PLAYER, context.getPlayer().getUniqueId().toString());
                }
                
                // Try local scope if we have a script context
                if (locationValue == null && context.getScriptId() != null) {
                    locationValue = variableManager.getVariable(locationName, VariableScope.LOCAL, context.getScriptId());
                }
                
                // Try global scope
                if (locationValue == null) {
                    locationValue = variableManager.getVariable(locationName, VariableScope.GLOBAL, "global");
                }
                
                // Try server scope
                if (locationValue == null) {
                    locationValue = variableManager.getVariable(locationName, VariableScope.SERVER, "server");
                }
                
                if (locationValue != null) {
                    String locationString = locationValue.asString();
                    if (locationString != null && !locationString.isEmpty()) {
                        // Store the location string in the target variable
                        String scriptId = context.getScriptId() != null ? context.getScriptId() : "global";
                        variableManager.setVariable(targetName, DataValue.of(locationString), VariableScope.LOCAL, scriptId);
                        return ExecutionResult.success("Retrieved location '" + locationName + "' and stored in '" + targetName + "'");
                    }
                }
                
                return ExecutionResult.error("Location '" + locationName + "' not found");
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get location: " + e.getMessage());
        }
    }
}