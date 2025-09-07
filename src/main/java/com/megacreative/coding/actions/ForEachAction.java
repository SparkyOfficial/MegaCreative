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
import java.util.UUID;

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
                // Get slot resolver from BlockConfigService
                com.megacreative.services.BlockConfigService configService = 
                    context.getPlugin().getServiceRegistry().getBlockConfigService();
                java.util.function.Function<String, Integer> slotResolver = 
                    configService != null ? configService.getSlotResolver("forEach") : null;
                    
                var listItem = slotResolver != null ? 
                    block.getItemFromSlot("list_slot", slotResolver) : null;
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
        UUID playerId = player != null ? player.getUniqueId() : null;
        
        // Store original variable values to restore later
        DataValue originalItemValue = null;
        DataValue originalIndexValue = null;
        
        try {
            // Save original values based on scope
            switch (scope) {
                case LOCAL:
                    originalItemValue = variableManager.getLocalVariable(scriptId, itemVariable);
                    if (indexVariable != null) {
                        originalIndexValue = variableManager.getLocalVariable(scriptId, indexVariable);
                    }
                    break;
                case GLOBAL:
                    originalItemValue = variableManager.getGlobalVariable(itemVariable);
                    if (indexVariable != null) {
                        originalIndexValue = variableManager.getGlobalVariable(indexVariable);
                    }
                    break;
                case PLAYER:
                    if (playerId != null) {
                        originalItemValue = variableManager.getPlayerVariable(playerId, itemVariable);
                        if (indexVariable != null) {
                            originalIndexValue = variableManager.getPlayerVariable(playerId, indexVariable);
                        }
                    }
                    break;
                case SERVER:
                    originalItemValue = variableManager.getServerVariable(itemVariable);
                    if (indexVariable != null) {
                        originalIndexValue = variableManager.getServerVariable(indexVariable);
                    }
                    break;
                case PERSISTENT:
                    originalItemValue = variableManager.getPersistentVariable(itemVariable);
                    if (indexVariable != null) {
                        originalIndexValue = variableManager.getPersistentVariable(indexVariable);
                    }
                    break;
            }
            
            // Iterate over the list
            for (int i = 0; i < list.size(); i++) {
                DataValue currentItem = list.get(i);
                
                // Set loop variables based on scope
                switch (scope) {
                    case LOCAL:
                        variableManager.setLocalVariable(scriptId, itemVariable, currentItem);
                        if (indexVariable != null) {
                            variableManager.setLocalVariable(scriptId, indexVariable, DataValue.of(i));
                        }
                        break;
                    case GLOBAL:
                        variableManager.setGlobalVariable(itemVariable, currentItem);
                        if (indexVariable != null) {
                            variableManager.setGlobalVariable(indexVariable, DataValue.of(i));
                        }
                        break;
                    case PLAYER:
                        if (playerId != null) {
                            variableManager.setPlayerVariable(playerId, itemVariable, currentItem);
                            if (indexVariable != null) {
                                variableManager.setPlayerVariable(playerId, indexVariable, DataValue.of(i));
                            }
                        }
                        break;
                    case SERVER:
                        variableManager.setServerVariable(itemVariable, currentItem);
                        if (indexVariable != null) {
                            variableManager.setServerVariable(indexVariable, DataValue.of(i));
                        }
                        break;
                    case PERSISTENT:
                        variableManager.setPersistentVariable(itemVariable, currentItem);
                        if (indexVariable != null) {
                            variableManager.setPersistentVariable(indexVariable, DataValue.of(i));
                        }
                        break;
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
        // Get ScriptEngine from ServiceRegistry
        com.megacreative.coding.ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(com.megacreative.coding.ScriptEngine.class);
        if (scriptEngine == null) {
            Player player = context.getPlayer();
            if (player != null) {
                player.sendMessage("§cОшибка: не удалось получить ScriptEngine");
            }
            return;
        }
        
        // Execute each child block
        for (CodeBlock childBlock : parentBlock.getChildren()) {
            try {
                // Execute the child block using ScriptEngine
                scriptEngine.executeBlockChain(childBlock, context.getPlayer(), "foreach_loop")
                    .exceptionally(throwable -> {
                        Player player = context.getPlayer();
                        if (player != null) {
                            player.sendMessage("§cОшибка в цикле ForEach: " + throwable.getMessage());
                        }
                        return null;
                    })
                    .join(); // Wait for completion before next iteration
            } catch (Exception e) {
                Player player = context.getPlayer();
                if (player != null) {
                    player.sendMessage("§cОшибка при выполнении блока в цикле ForEach: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Helper method to set variable value based on scope
     */
    private void setVariableValue(VariableManager manager, VariableScope scope, 
                                 String scriptId, String worldId, Player player, String name, DataValue value) {
        UUID playerId = player != null ? player.getUniqueId() : null;
        
        switch (scope) {
            case LOCAL:
                manager.setLocalVariable(scriptId, name, value);
                break;
            case GLOBAL:
                manager.setGlobalVariable(name, value);
                break;
            case PLAYER:
                if (playerId != null) {
                    manager.setPlayerVariable(playerId, name, value);
                }
                break;
            case SERVER:
                manager.setServerVariable(name, value);
                break;
            case PERSISTENT:
                manager.setPersistentVariable(name, value);
                break;
        }
    }
    
    /**
     * Helper method to remove variable based on scope (by setting to null)
     */
    private void removeVariable(VariableManager manager, VariableScope scope, 
                               String scriptId, String worldId, Player player, String name) {
        UUID playerId = player != null ? player.getUniqueId() : null;
        
        // Since VariableManager doesn't have remove methods, we set to null
        switch (scope) {
            case LOCAL:
                manager.setLocalVariable(scriptId, name, null);
                break;
            case GLOBAL:
                manager.setGlobalVariable(name, null);
                break;
            case PLAYER:
                if (playerId != null) {
                    manager.setPlayerVariable(playerId, name, null);
                }
                break;
            case SERVER:
                manager.setServerVariable(name, null);
                break;
            case PERSISTENT:
                manager.setPersistentVariable(name, null);
                break;
        }
    }
}