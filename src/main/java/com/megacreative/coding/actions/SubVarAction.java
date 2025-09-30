package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

/**
 * Action for subtracting a value from a variable.
 * This action retrieves variable parameters from the new parameter system and subtracts the value from the variable.
 */
@BlockMeta(id = "subVar", displayName = "§aSubtract from Variable", type = BlockType.ACTION)
public class SubVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get and validate parameters from the new parameter system
            DataValue nameValue = block.getParameter("name");
            DataValue valueValue = block.getParameter("value");
            
            if (nameValue == null || nameValue.isEmpty()) {
                return ExecutionResult.error("No variable name provided");
            }
            
            if (valueValue == null || valueValue.isEmpty()) {
                return ExecutionResult.error("No value provided");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();

            if (varName == null || varName.isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }

            // Parse the numeric value to subtract
            double valueToSubtract = parseValue(valueStr);
            if (Double.isNaN(valueToSubtract)) {
                return ExecutionResult.error("Invalid value: " + valueStr);
            }

            // Process the variable update
            return updateVariableValue(context, varName, valueToSubtract);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }

    /**
     * Parses a string value to double, returns Double.NaN if parsing fails
     */
    private double parseValue(String valueStr) {
        try {
            return Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /**
     * Updates the variable value in the appropriate scope
     */
    private ExecutionResult updateVariableValue(ExecutionContext context, String varName, double valueToSubtract) {
        VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
        Player player = context.getPlayer();

        // Find the variable in different scopes
        VariableScopeInfo scopeInfo = findVariableScope(variableManager, player, context.getScriptId(), varName);

        // Get current value or default to 0
        double currentValue = getCurrentVariableValue(scopeInfo.getCurrentVar());

        // Calculate and set new value (subtract instead of add)
        double newValue = currentValue - valueToSubtract;
        setVariableValue(variableManager, scopeInfo, varName, newValue, context.getScriptId(), player);

        // Log the operation
        context.getPlugin().getLogger().info(
                String.format("Subtracting %s from variable %s (new value: %s)",
                        valueToSubtract, varName, newValue)
        );

        return ExecutionResult.success("Variable updated successfully");
    }

    /**
     * Finds the scope of an existing variable
     */
    private VariableScopeInfo findVariableScope(VariableManager variableManager, Player player, String scriptId, String varName) {
        // Try player variables first if player is not null
        if (player != null) {
            DataValue playerVar = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            if (playerVar != null) {
                return new VariableScopeInfo(
                        playerVar,
                        VariableManager.VariableScope.PLAYER,
                        player.getUniqueId().toString()
                );
            }
        }

        // Try local variables
        DataValue localVar = variableManager.getLocalVariable(scriptId, varName);
        if (localVar != null) {
            return new VariableScopeInfo(
                    localVar,
                    VariableManager.VariableScope.LOCAL,
                    scriptId
            );
        }

        // Try global variables
        DataValue globalVar = variableManager.getGlobalVariable(varName);
        if (globalVar != null) {
            return new VariableScopeInfo(
                    globalVar,
                    VariableManager.VariableScope.GLOBAL,
                    "global"
            );
        }

        // Try server variables
        DataValue serverVar = variableManager.getServerVariable(varName);
        if (serverVar != null) {
            return new VariableScopeInfo(
                    serverVar,
                    VariableManager.VariableScope.SERVER,
                    "server"
            );
        }

        // Variable doesn't exist yet, will be created as local
        return new VariableScopeInfo(null, null, null);
    }

    /**
     * Gets the current numeric value of a variable, defaults to 0 if not a number
     */
    private double getCurrentVariableValue(DataValue variable) {
        if (variable == null) {
            return 0.0;
        }
        try {
            return variable.asNumber().doubleValue();
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Sets the variable value in the appropriate scope
     */
    private void setVariableValue(VariableManager variableManager, VariableScopeInfo scopeInfo,
                                   String varName, double newValue, String scriptId, Player player) {
        DataValue newValueData = DataValue.of(newValue);

        if (scopeInfo.getScope() == null) {
            // Variable doesn't exist, create as local
            variableManager.setLocalVariable(scriptId, varName, newValueData);
            return;
        }

        switch (scopeInfo.getScope()) {
            case PLAYER:
                variableManager.setPlayerVariable(player.getUniqueId(), varName, newValueData);
                break;
            case LOCAL:
                variableManager.setLocalVariable(scriptId, varName, newValueData);
                break;
            case GLOBAL:
                variableManager.setGlobalVariable(varName, newValueData);
                break;
            case SERVER:
                variableManager.setServerVariable(varName, newValueData);
                break;
            default:
                // Fallback to local variable
                variableManager.setLocalVariable(scriptId, varName, newValueData);
        }
    }

    /**
     * Helper class to hold variable parameters
     */
    private static class SubVarParams {
        String nameStr = "";
        String valueStr = "";
    }

    /**
     * Helper class to hold variable scope information
     */
    private static class VariableScopeInfo {
        private final DataValue currentVar;
        private final VariableManager.VariableScope scope;
        private final String scopeContext;

        public VariableScopeInfo(DataValue currentVar, VariableManager.VariableScope scope, String scopeContext) {
            this.currentVar = currentVar;
            this.scope = scope;
            this.scopeContext = scopeContext;
        }

        public DataValue getCurrentVar() {
            return currentVar;
        }

        public VariableManager.VariableScope getScope() {
            return scope;
        }

        public String getScopeContext() {
            return scopeContext;
        }
    }
}