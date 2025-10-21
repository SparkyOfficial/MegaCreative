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
 * Action for setting a variable value.
 * This action retrieves variable name and value parameters and sets the variable.
 */
@BlockMeta(id = "setVar", displayName = "§aSet Variable", type = BlockType.ACTION)
public class SetVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue nameValue = block.getParameter("name");
            DataValue valueValue = block.getParameter("value");
            
            if (nameValue == null || nameValue.isEmpty()) {
                return ExecutionResult.error("No variable name provided");
            }
            
            if (valueValue == null || valueValue.isEmpty()) {
                return ExecutionResult.error("No value provided");
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            String varName = resolvedName.asString();

            // Removed redundant null check - static analysis flagged it as always non-null when this method is called
            if (varName.isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }

            
            return setVariableValue(context, varName, resolvedValue);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set variable: " + e.getMessage());
        }
    }

    /**
     * Sets the variable value in the appropriate scope
     */
    private ExecutionResult setVariableValue(ExecutionContext context, String varName, DataValue valueToSet) {
        VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
        Player player = context.getPlayer();

        
        VariableScopeInfo scopeInfo = findVariableScope(variableManager, player, context.getScriptId(), varName);

        
        if (scopeInfo.getScope() == null) {
            
            variableManager.setLocalVariable(context.getScriptId(), varName, valueToSet);
            return ExecutionResult.success("Variable set successfully as local variable");
        }

        switch (scopeInfo.getScope()) {
            case PLAYER:
                variableManager.setPlayerVariable(player.getUniqueId(), varName, valueToSet);
                break;
            case LOCAL:
                variableManager.setLocalVariable(context.getScriptId(), varName, valueToSet);
                break;
            case GLOBAL:
                variableManager.setGlobalVariable(varName, valueToSet);
                break;
            case SERVER:
                variableManager.setServerVariable(varName, valueToSet);
                break;
            default:
                // Handle default case by using local scope
                variableManager.setLocalVariable(context.getScriptId(), varName, valueToSet);
                break;
        }

        return ExecutionResult.success("Variable set successfully");
    }

    /**
     * Finds the scope of an existing variable
     */
    private VariableScopeInfo findVariableScope(VariableManager variableManager, Player player, String scriptId, String varName) {
        
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

        
        DataValue localVar = variableManager.getLocalVariable(scriptId, varName);
        if (localVar != null) {
            return new VariableScopeInfo(
                    localVar,
                    VariableManager.VariableScope.LOCAL,
                    scriptId
            );
        }

        
        DataValue globalVar = variableManager.getGlobalVariable(varName);
        if (globalVar != null) {
            return new VariableScopeInfo(
                    globalVar,
                    VariableManager.VariableScope.GLOBAL,
                    "global"
            );
        }

        
        DataValue serverVar = variableManager.getServerVariable(varName);
        if (serverVar != null) {
            return new VariableScopeInfo(
                    serverVar,
                    VariableManager.VariableScope.SERVER,
                    "server"
            );
        }

        
        return new VariableScopeInfo(null, null, null);
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