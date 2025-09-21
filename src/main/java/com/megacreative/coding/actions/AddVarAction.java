package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;
import java.util.function.Function;

/**
 * Action for adding a value to a variable.
 * This action retrieves variable parameters from the container configuration and adds the value to the variable.
 */
public class AddVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get and validate parameters
            AddVarParams params = getAndValidateParams(block, context);
            if (params == null) {
                return ExecutionResult.error("Invalid variable configuration");
            }

            // Parse the numeric value to add
            double valueToAdd = parseValue(params.valueStr);
            if (Double.isNaN(valueToAdd)) {
                return ExecutionResult.error("Invalid value: " + params.valueStr);
            }

            // Process the variable update
            return updateVariableValue(context, params.nameStr, valueToAdd);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }

    /**
     * Gets and validates parameters from the block and context
     */
    private AddVarParams getAndValidateParams(CodeBlock block, ExecutionContext context) {
        AddVarParams params = getVarParamsFromContainer(block, context);
        if (params.nameStr == null || params.nameStr.isEmpty()) {
            return null;
        }

        // Resolve any placeholders in the parameters
        ParameterResolver resolver = new ParameterResolver(context);
        String varName = resolver.resolve(context, DataValue.of(params.nameStr)).asString();
        String valueStr = resolver.resolve(context, DataValue.of(params.valueStr)).asString();

        if (varName == null || varName.isEmpty()) {
            return null;
        }

        params.nameStr = varName;
        params.valueStr = valueStr;
        return params;
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
    private ExecutionResult updateVariableValue(ExecutionContext context, String varName, double valueToAdd) {
        VariableManager variableManager = context.getPlugin().getVariableManager();
        Player player = context.getPlayer();

        // Find the variable in different scopes
        VariableScopeInfo scopeInfo = findVariableScope(variableManager, player, context.getScriptId(), varName);

        // Get current value or default to 0
        double currentValue = getCurrentVariableValue(scopeInfo.getCurrentVar());

        // Calculate and set new value
        double newValue = currentValue + valueToAdd;
        setVariableValue(variableManager, scopeInfo, varName, newValue, context.getScriptId(), player);

        // Log the operation
        context.getPlugin().getLogger().info(
                String.format("Adding %s to variable %s (new value: %s)",
                        valueToAdd, varName, newValue)
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
     * Gets variable parameters from the container configuration
     */
    private AddVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        AddVarParams params = new AddVarParams();

        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();

            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());

            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
                    ItemStack nameItem = block.getConfigItem(nameSlot);
                    if (nameItem != null && nameItem.hasItemMeta()) {
                        // Extract variable name from item
                        params.nameStr = getVariableNameFromItem(nameItem);
                    }
                }

                // Get value from the value slot
                Integer valueSlot = slotResolver.apply("value");
                if (valueSlot != null) {
                    ItemStack valueItem = block.getConfigItem(valueSlot);
                    if (valueItem != null) {
                        // Extract value from item
                        params.valueStr = getValueFromItem(valueItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting variable parameters from container in AddVarAction: " + e.getMessage());
        }

        return params;
    }

    /**
     * Extracts variable name from an item
     */
    private String getVariableNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the variable name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }

    /**
     * Extracts value from an item
     */
    private String getValueFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the value
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }

        // If no display name, use the item amount as a number
        return String.valueOf(item.getAmount());
    }

    /**
     * Helper class to hold variable parameters
     */
    private static class AddVarParams {
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