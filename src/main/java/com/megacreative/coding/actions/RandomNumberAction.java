package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

import java.util.Random;

/**
 * Action for generating a random number.
 * This action retrieves parameters from the container configuration and generates a random number.
 */
public class RandomNumberAction implements BlockAction {
    private static final Random RANDOM = new Random();

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters directly from block parameters (new system)
            String minValue = block.getParameterValue("minValue", String.class);
            String maxValue = block.getParameterValue("maxValue", String.class);
            String targetVariable = block.getParameterValue("targetVariable", String.class);

            // Parse parameters with defaults
            int min = 1;
            int max = 100;

            try {
                if (minValue != null && !minValue.isEmpty()) {
                    min = Integer.parseInt(minValue);
                }
                if (maxValue != null && !maxValue.isEmpty()) {
                    max = Integer.parseInt(maxValue);
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid number format for min/max values: " + e.getMessage());
            }

            if (targetVariable == null || targetVariable.isEmpty()) {
                return ExecutionResult.error("Target variable not specified");
            }

            // Generate random number
            int randomNumber = RANDOM.nextInt(max - min + 1) + min;

            // 🎆 ENHANCED: Actually set the variable using VariableManager
            Player player = context.getPlayer();
            if (player == null) {
                return ExecutionResult.error("No player found in execution context");
            }

            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager not available");
            }

            // Set the variable for the player
            DataValue dataValue = DataValue.of(String.valueOf(randomNumber));
            variableManager.setPlayerVariable(player.getUniqueId(), targetVariable, dataValue);

            context.getPlugin().getLogger().info("Random number generated: " + randomNumber + " -> " + targetVariable + " for player " + player.getName());

            return ExecutionResult.success("Random number " + randomNumber + " generated and stored in '" + targetVariable + "'");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to generate random number: " + e.getMessage());
        }
    }
}