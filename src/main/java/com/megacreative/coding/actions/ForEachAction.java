package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.coding.values.types.TextValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.VariableScope;
import org.bukkit.entity.Player;

/**
 * Advanced For Each loop action with DataValue and VariableManager integration
 * Iterates over lists and executes child blocks for each item
 * 
 * Parameters:
 * - "list": The list to iterate over (can be variable reference or direct ListValue)
 * - "item_variable": The name of the variable to store current item (default: "item")
 * - "index_variable": The name of the variable to store current index (optional)
 * 
 * Examples:
 * - For Each item in ${player_list}: Sets "item" variable for each iteration
 * - For Each element in [1,2,3] as index: Sets "element" and "index" variables
 */
public class ForEachAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();
        
        if (player == null || block == null || variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        try {
            // Get the list to iterate over
            DataValue listValue = block.getParameter("list");
            if (listValue == null || listValue.isEmpty()) {
                // Fallback to GUI slot
                var listItem = block.getItemFromSlot("list_slot");
                if (listItem != null && listItem.hasItemMeta()) {
                    listValue = new TextValue(listItem.getItemMeta().getDisplayName());
                } else {
                    player.sendMessage("§c[ForEach] No list specified for iteration!");
                    return;
                }
            }
            
            // Resolve the list value (might be a variable reference)
            DataValue resolvedList = resolver.resolve(context, listValue);
            
            if (!(resolvedList instanceof ListValue)) {
                player.sendMessage("§c[ForEach] Value is not a list: " + resolvedList.getType());
                return;
            }
            
            ListValue list = (ListValue) resolvedList;
            
            // Get variable names
            DataValue itemVarValue = block.getParameter("item_variable");
            String itemVariable = itemVarValue != null ? itemVarValue.asString() : "item";
            
            DataValue indexVarValue = block.getParameter("index_variable");
            String indexVariable = indexVarValue != null ? indexVarValue.asString() : null;
            
            // Get iteration scope (default to LOCAL)
            DataValue scopeValue = block.getParameter("scope");
            VariableScope scope = VariableScope.LOCAL;
            if (scopeValue != null && !scopeValue.isEmpty()) {
                try {
                    scope = VariableScope.valueOf(scopeValue.asString().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Use default scope
                }
            }
            
            // Execute loop
            executeForEachLoop(context, block, list, itemVariable, indexVariable, scope, variableManager);
            
        } catch (Exception e) {
            player.sendMessage("§c[ForEach] Error during execution: " + e.getMessage());
            context.getPlugin().getLogger().warning("ForEach execution error: " + e.getMessage());
        }
    }
    
    /**
     * Executes the for-each loop with proper variable management
     */
    private void executeForEachLoop(ExecutionContext context, CodeBlock block, ListValue list, 
                                   String itemVariable, String indexVariable, VariableScope scope, 
                                   VariableManager variableManager) {
        
        Player player = context.getPlayer();
        String scriptId = context.getScriptId();
        String worldId = context.getWorldId();
        
        // Store original variable values to restore later
        DataValue originalItemValue = null;
        DataValue originalIndexValue = null;
        
        try {
            // Save original values
            originalItemValue = getVariableValue(variableManager, scope, scriptId, worldId, player, itemVariable);
            if (indexVariable != null) {
                originalIndexValue = getVariableValue(variableManager, scope, scriptId, worldId, player, indexVariable);
            }
            
            // Iterate over the list
            for (int i = 0; i < list.size(); i++) {
                DataValue currentItem = list.get(i);
                
                // Set loop variables
                setVariableValue(variableManager, scope, scriptId, worldId, player, itemVariable, currentItem);
                
                if (indexVariable != null) {
                    setVariableValue(variableManager, scope, scriptId, worldId, player, indexVariable, 
                        DataValue.fromObject(i));
                }
                
                // Execute child blocks
                executeChildBlocks(context, block);
                
                // Check for break conditions or script interruption
                // Note: Basic loop without cancellation support for now
            }
            
            player.sendMessage("§a[ForEach] Completed iteration over " + list.size() + " items");
            
        } finally {
            // Restore original variable values
            if (originalItemValue != null) {
                setVariableValue(variableManager, scope, scriptId, worldId, player, itemVariable, originalItemValue);
            } else {
                removeVariable(variableManager, scope, scriptId, worldId, player, itemVariable);
            }
            
            if (indexVariable != null) {
                if (originalIndexValue != null) {
                    setVariableValue(variableManager, scope, scriptId, worldId, player, indexVariable, originalIndexValue);
                } else {
                    removeVariable(variableManager, scope, scriptId, worldId, player, indexVariable);
                }
            }
        }
    }
    
    /**
     * Executes all child blocks in sequence
     */
    private void executeChildBlocks(ExecutionContext context, CodeBlock parentBlock) {
        for (CodeBlock childBlock : parentBlock.getChildren()) {
            // Use ScriptExecutor from ServiceRegistry to process child blocks properly
            com.megacreative.coding.ScriptExecutor executor = new com.megacreative.coding.ScriptExecutor(context.getPlugin());
            executor.processBlock(childBlock, context);
        }
    }
    
    /**
     * Helper method to get variable value based on scope
     */
    private DataValue getVariableValue(VariableManager manager, VariableScope scope, 
                                      String scriptId, String worldId, Player player, String name) {
        return switch (scope) {
            case LOCAL -> manager.getLocalVariable(scriptId, name);
            case WORLD -> manager.getGlobalVariable(worldId, name);
            case PLAYER -> {
                // Use the scoped variable approach for player variables
                String playerScopedName = "player." + name;
                yield manager.getVariable(playerScopedName, scriptId, worldId);
            }
            case SERVER -> manager.getPersistentVariable(name);
        };
    }
    
    /**
     * Helper method to set variable value based on scope
     */
    private void setVariableValue(VariableManager manager, VariableScope scope, 
                                 String scriptId, String worldId, Player player, String name, DataValue value) {
        switch (scope) {
            case LOCAL -> manager.setLocalVariable(scriptId, name, value);
            case WORLD -> manager.setGlobalVariable(worldId, name, value);
            case PLAYER -> {
                // Use the scoped variable approach for player variables
                String playerScopedName = "player." + name;
                manager.setVariable(playerScopedName, value, scriptId, worldId);
            }
            case SERVER -> manager.setPersistentVariable(name, value);
        }
    }
    
    /**
     * Helper method to remove variable based on scope
     */
    private void removeVariable(VariableManager manager, VariableScope scope, 
                               String scriptId, String worldId, Player player, String name) {
        // Since VariableManager doesn't have remove methods, we set to null
        switch (scope) {
            case LOCAL -> manager.setLocalVariable(scriptId, name, null);
            case WORLD -> manager.setGlobalVariable(worldId, name, null);
            case PLAYER -> {
                String playerScopedName = "player." + name;
                manager.setVariable(playerScopedName, null, scriptId, worldId);
            }
            case SERVER -> manager.setPersistentVariable(name, null);
        }
    }
}