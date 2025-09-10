package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;

import java.util.Random;

/**
 * Action for generating a random number.
 * This action generates a random number and stores it in a variable.
 */
public class RandomNumberAction implements BlockAction {
    
    private static final Random random = new Random();

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get the target variable name parameter from the block
            DataValue targetValue = block.getParameter("target");
            if (targetValue == null) {
                return ExecutionResult.error("Target variable name parameter is missing");
            }

            // Get the min parameter from the block (default to 0)
            double min = 0;
            DataValue minValue = block.getParameter("min");
            if (minValue != null) {
                try {
                    min = minValue.asNumber().doubleValue();
                } catch (NumberFormatException e) {
                    // Use default min if parsing fails
                }
            }

            // Get the max parameter from the block (default to 100)
            double max = 100;
            DataValue maxValue = block.getParameter("max");
            if (maxValue != null) {
                try {
                    max = maxValue.asNumber().doubleValue();
                } catch (NumberFormatException e) {
                    // Use default max if parsing fails
                }
            }

            // Resolve any placeholders in the target variable name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTarget = resolver.resolve(context, targetValue);
            
            // Parse target parameter
            String targetName = resolvedTarget.asString();
            if (targetName == null || targetName.isEmpty()) {
                return ExecutionResult.error("Target variable name is empty or null");
            }

            // Generate random number
            double randomNumber = min + (max - min) * random.nextDouble();
            
            // Store the random number in the target variable
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                String scriptId = context.getScriptId() != null ? context.getScriptId() : "global";
                variableManager.setVariable(targetName, DataValue.of(randomNumber), VariableScope.LOCAL, scriptId);
                return ExecutionResult.success("Generated random number " + randomNumber + " and stored in '" + targetName + "'");
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to generate random number: " + e.getMessage());
        }
    }
}