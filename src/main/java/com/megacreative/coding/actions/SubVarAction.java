package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for subtracting a value from a variable.
 * This action retrieves variable parameters from the container configuration and subtracts the value from the variable.
 */
@BlockMeta(id = "subVar", displayName = "§aSubtract from Variable", type = BlockType.ACTION)
public class SubVarAction implements BlockAction {
    
    // Define the missing constants
    private static final double MIN_VARIABLE_VALUE = -1000000.0;
    private static final double MAX_VARIABLE_VALUE = 1000000.0;

    private VariableValue getVariableValue(VariableManager variableManager, String varName, String scriptId, Player player) {
        // Try to get the variable from different scopes
        
        // First try player variables
        if (player != null) {
            DataValue var = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            if (var != null) {
                return new VariableValue(var, VariableManager.VariableScope.PLAYER, player.getUniqueId().toString());
            }
        }
        
        // If not found, try local variables
        DataValue localVar = variableManager.getLocalVariable(scriptId, varName);
        if (localVar != null) {
            return new VariableValue(localVar, VariableManager.VariableScope.LOCAL, scriptId);
        }
        
        // If not found, try global variables
        DataValue globalVar = variableManager.getGlobalVariable(varName);
        if (globalVar != null) {
            return new VariableValue(globalVar, VariableManager.VariableScope.GLOBAL, "global");
        }
        
        // If not found, try server variables
        DataValue serverVar = variableManager.getServerVariable(varName);
        if (serverVar != null) {
            return new VariableValue(serverVar, VariableManager.VariableScope.SERVER, "server");
        }
        
        return null;
    }
    
    private void setVariableValue(VariableManager variableManager, String varName, DataValue value, 
                                 VariableManager.VariableScope scope, String context) {
        switch (scope) {
            case PLAYER:
                variableManager.setPlayerVariable(java.util.UUID.fromString(context), varName, value);
                break;
            case LOCAL:
                variableManager.setLocalVariable(context, varName, value);
                break;
            case GLOBAL:
                variableManager.setGlobalVariable(varName, value);
                break;
            case SERVER:
                variableManager.setServerVariable(varName, value);
                break;
            default:
                // Default to local scope if scope is not specified
                variableManager.setLocalVariable(context, varName, value);
                break;
        }
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        try {
            // Get variable parameters from the container configuration
            SubVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            if (varName == null || varName.trim().isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }
            
            varName = varName.trim();

            // Parse the value as a number with validation
            double value;
            try {
                value = Double.parseDouble(valueStr);
                // Validate value range
                if (value < MIN_VARIABLE_VALUE || value > MAX_VARIABLE_VALUE) {
                    return ExecutionResult.error(String.format("Value must be between %f and %f", 
                        MIN_VARIABLE_VALUE, MAX_VARIABLE_VALUE));
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid numeric value: " + valueStr);
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager is not available");
            }
            
            // Get the variable value using the refactored method
            VariableValue variable = getVariableValue(variableManager, varName, context.getScriptId(), context.getPlayer());
            
            // Get current value or default to 0
            double currentValue = 0.0;
            VariableManager.VariableScope scope = VariableManager.VariableScope.LOCAL;
            String scopeContext = context.getScriptId();
            
            if (variable != null && variable.value != null) {
                try {
                    currentValue = variable.value.asNumber().doubleValue();
                    scope = variable.scope;
                    scopeContext = variable.context;
                } catch (NumberFormatException e) {
                    // If current value is not a number, treat as 0 and log a warning
                    context.getPlugin().getLogger().warning("Variable " + varName + " is not a number, treating as 0");
                }
            }
            
            // Calculate new value with bounds checking
            double newValue;
            try {
                newValue = Math.subtractExact((long)currentValue, (long)value);
                
                // Additional check for double overflow
                if (newValue > MAX_VARIABLE_VALUE || newValue < MIN_VARIABLE_VALUE) {
                    throw new ArithmeticException("Variable value out of range");
                }
            } catch (ArithmeticException e) {
                return ExecutionResult.error("Arithmetic error: " + e.getMessage());
            }
            
            // Set the updated value using the refactored method
            DataValue newValueData = DataValue.of(newValue);
            setVariableValue(variableManager, varName, newValueData, scope, scopeContext);
            
            context.getPlugin().getLogger().info("Subtracting " + value + " from variable " + varName + " (new value: " + newValue + ")");
            
            return ExecutionResult.success("Variable updated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private SubVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            throw new IllegalArgumentException("CodeBlock and ExecutionContext cannot be null");
        }
        
        SubVarParams params = new SubVarParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            if (blockConfigService == null) {
                context.getPlugin().getLogger().warning("BlockConfigService is not available");
                return params;
            }
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
</original_code>```

```
package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for subtracting a value from a variable.
 * This action retrieves variable parameters from the container configuration and subtracts the value from the variable.
 */
@BlockMeta(id = "subVar", displayName = "§aSubtract from Variable", type = BlockType.ACTION)
public class SubVarAction implements BlockAction {
    
    // Define the missing constants
    private static final double MIN_VARIABLE_VALUE = -1000000.0;
    private static final double MAX_VARIABLE_VALUE = 1000000.0;

    private VariableValue getVariableValue(VariableManager variableManager, String varName, String scriptId, Player player) {
        // Try to get the variable from different scopes
        
        // First try player variables
        if (player != null) {
            DataValue var = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            if (var != null) {
                return new VariableValue(var, VariableManager.VariableScope.PLAYER, player.getUniqueId().toString());
            }
        }
        
        // If not found, try local variables
        DataValue localVar = variableManager.getLocalVariable(scriptId, varName);
        if (localVar != null) {
            return new VariableValue(localVar, VariableManager.VariableScope.LOCAL, scriptId);
        }
        
        // If not found, try global variables
        DataValue globalVar = variableManager.getGlobalVariable(varName);
        if (globalVar != null) {
            return new VariableValue(globalVar, VariableManager.VariableScope.GLOBAL, "global");
        }
        
        // If not found, try server variables
        DataValue serverVar = variableManager.getServerVariable(varName);
        if (serverVar != null) {
            return new VariableValue(serverVar, VariableManager.VariableScope.SERVER, "server");
        }
        
        return null;
    }
    
    private void setVariableValue(VariableManager variableManager, String varName, DataValue value, 
                                 VariableManager.VariableScope scope, String context) {
        switch (scope) {
            case PLAYER:
                variableManager.setPlayerVariable(java.util.UUID.fromString(context), varName, value);
                break;
            case LOCAL:
                variableManager.setLocalVariable(context, varName, value);
                break;
            case GLOBAL:
                variableManager.setGlobalVariable(varName, value);
                break;
            case SERVER:
                variableManager.setServerVariable(varName, value);
                break;
            default:
                // Default to local scope if scope is not specified
                variableManager.setLocalVariable(context, varName, value);
                break;
        }
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        try {
            // Get variable parameters from the container configuration
            SubVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            if (varName == null || varName.trim().isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }
            
            varName = varName.trim();

            // Parse the value as a number with validation
            double value;
            try {
                value = Double.parseDouble(valueStr);
                // Validate value range
                if (value < MIN_VARIABLE_VALUE || value > MAX_VARIABLE_VALUE) {
                    return ExecutionResult.error(String.format("Value must be between %f and %f", 
                        MIN_VARIABLE_VALUE, MAX_VARIABLE_VALUE));
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid numeric value: " + valueStr);
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager is not available");
            }
            
            // Get the variable value using the refactored method
            VariableValue variable = getVariableValue(variableManager, varName, context.getScriptId(), context.getPlayer());
            
            // Get current value or default to 0
            double currentValue = 0.0;
            VariableManager.VariableScope scope = VariableManager.VariableScope.LOCAL;
            String scopeContext = context.getScriptId();
            
            if (variable != null && variable.value != null) {
                try {
                    currentValue = variable.value.asNumber().doubleValue();
                    scope = variable.scope;
                    scopeContext = variable.context;
                } catch (NumberFormatException e) {
                    // If current value is not a number, treat as 0 and log a warning
                    context.getPlugin().getLogger().warning("Variable " + varName + " is not a number, treating as 0");
                }
            }
            
            // Calculate new value with bounds checking
            double newValue;
            try {
                newValue = Math.subtractExact((long)currentValue, (long)value);
                
                // Additional check for double overflow
                if (newValue > MAX_VARIABLE_VALUE || newValue < MIN_VARIABLE_VALUE) {
                    throw new ArithmeticException("Variable value out of range");
                }
            } catch (ArithmeticException e) {
                return ExecutionResult.error("Arithmetic error: " + e.getMessage());
            }
            
            // Set the updated value using the refactored method
            DataValue newValueData = DataValue.of(newValue);
            setVariableValue(variableManager, varName, newValueData, scope, scopeContext);
            
            context.getPlugin().getLogger().info("Subtracting " + value + " from variable " + varName + " (new value: " + newValue + ")");
            
            return ExecutionResult.success("Variable updated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private SubVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            throw new IllegalArgumentException("CodeBlock and ExecutionContext cannot be null");
        }
        
        SubVarParams params = new SubVarParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            if (blockConfigService == null) {
                context.getPlugin().getLogger().warning("BlockConfigService is not available");
                return params;
            }
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
```

```
package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for subtracting a value from a variable.
 * This action retrieves variable parameters from the container configuration and subtracts the value from the variable.
 */
@BlockMeta(id = "subVar", displayName = "§aSubtract from Variable", type = BlockType.ACTION)
public class SubVarAction implements BlockAction {
    
    // Define the missing constants
    private static final double MIN_VARIABLE_VALUE = -1000000.0;
    private static final double MAX_VARIABLE_VALUE = 1000000.0;

    private VariableValue getVariableValue(VariableManager variableManager, String varName, String scriptId, Player player) {
        // Try to get the variable from different scopes
        
        // First try player variables
        if (player != null) {
            DataValue var = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            if (var != null) {
                return new VariableValue(var, VariableManager.VariableScope.PLAYER, player.getUniqueId().toString());
            }
        }
        
        // If not found, try local variables
        DataValue localVar = variableManager.getLocalVariable(scriptId, varName);
        if (localVar != null) {
            return new VariableValue(localVar, VariableManager.VariableScope.LOCAL, scriptId);
        }
        
        // If not found, try global variables
        DataValue globalVar = variableManager.getGlobalVariable(varName);
        if (globalVar != null) {
            return new VariableValue(globalVar, VariableManager.VariableScope.GLOBAL, "global");
        }
        
        // If not found, try server variables
        DataValue serverVar = variableManager.getServerVariable(varName);
        if (serverVar != null) {
            return new VariableValue(serverVar, VariableManager.VariableScope.SERVER, "server");
        }
        
        return null;
    }
    
    private void setVariableValue(VariableManager variableManager, String varName, DataValue value, 
                                 VariableManager.VariableScope scope, String context) {
        switch (scope) {
            case PLAYER:
                variableManager.setPlayerVariable(java.util.UUID.fromString(context), varName, value);
                break;
            case LOCAL:
                variableManager.setLocalVariable(context, varName, value);
                break;
            case GLOBAL:
                variableManager.setGlobalVariable(varName, value);
                break;
            case SERVER:
                variableManager.setServerVariable(varName, value);
                break;
            default:
                // Default to local scope if scope is not specified
                variableManager.setLocalVariable(context, varName, value);
                break;
        }
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        try {
            // Get variable parameters from the container configuration
            SubVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            if (varName == null || varName.trim().isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }
            
            varName = varName.trim();

            // Parse the value as a number with validation
            double value;
            try {
                value = Double.parseDouble(valueStr);
                // Validate value range
                if (value < MIN_VARIABLE_VALUE || value > MAX_VARIABLE_VALUE) {
                    return ExecutionResult.error(String.format("Value must be between %f and %f", 
                        MIN_VARIABLE_VALUE, MAX_VARIABLE_VALUE));
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid numeric value: " + valueStr);
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager is not available");
            }
            
            // Get the variable value using the refactored method
            VariableValue variable = getVariableValue(variableManager, varName, context.getScriptId(), context.getPlayer());
            
            // Get current value or default to 0
            double currentValue = 0.0;
            VariableManager.VariableScope scope = VariableManager.VariableScope.LOCAL;
            String scopeContext = context.getScriptId();
            
            if (variable != null && variable.value != null) {
                try {
                    currentValue = variable.value.asNumber().doubleValue();
                    scope = variable.scope;
                    scopeContext = variable.context;
                } catch (NumberFormatException e) {
                    // If current value is not a number, treat as 0 and log a warning
                    context.getPlugin().getLogger().warning("Variable " + varName + " is not a number, treating as 0");
                }
            }
            
            // Calculate new value with bounds checking
            double newValue;
            try {
                newValue = Math.subtractExact((long)currentValue, (long)value);
                
                // Additional check for double overflow
                if (newValue > MAX_VARIABLE_VALUE || newValue < MIN_VARIABLE_VALUE) {
                    throw new ArithmeticException("Variable value out of range");
                }
            } catch (ArithmeticException e) {
                return ExecutionResult.error("Arithmetic error: " + e.getMessage());
            }
            
            // Set the updated value using the refactored method
            DataValue newValueData = DataValue.of(newValue);
            setVariableValue(variableManager, varName, newValueData, scope, scopeContext);
            
            context.getPlugin().getLogger().info("Subtracting " + value + " from variable " + varName + " (new value: " + newValue + ")");
            
            return ExecutionResult.success("Variable updated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private SubVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            throw new IllegalArgumentException("CodeBlock and ExecutionContext cannot be null");
        }
        
        SubVarParams params = new SubVarParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            if (blockConfigService == null) {
                context.getPlugin().getLogger().warning("BlockConfigService is not available");
                return params;
            }
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
```

```
package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for subtracting a value from a variable.
 * This action retrieves variable parameters from the container configuration and subtracts the value from the variable.
 */
@BlockMeta(id = "subVar", displayName = "§aSubtract from Variable", type = BlockType.ACTION)
public class SubVarAction implements BlockAction {
    
    // Define the missing constants
    private static final double MIN_VARIABLE_VALUE = -1000000.0;
    private static final double MAX_VARIABLE_VALUE = 1000000.0;

    private VariableValue getVariableValue(VariableManager variableManager, String varName, String scriptId, Player player) {
        // Try to get the variable from different scopes
        
        // First try player variables
        if (player != null) {
            DataValue var = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            if (var != null) {
                return new VariableValue(var, VariableManager.VariableScope.PLAYER, player.getUniqueId().toString());
            }
        }
        
        // If not found, try local variables
        DataValue localVar = variableManager.getLocalVariable(scriptId, varName);
        if (localVar != null) {
            return new VariableValue(localVar, VariableManager.VariableScope.LOCAL, scriptId);
        }
        
        // If not found, try global variables
        DataValue globalVar = variableManager.getGlobalVariable(varName);
        if (globalVar != null) {
            return new VariableValue(globalVar, VariableManager.VariableScope.GLOBAL, "global");
        }
        
        // If not found, try server variables
        DataValue serverVar = variableManager.getServerVariable(varName);
        if (serverVar != null) {
            return new VariableValue(serverVar, VariableManager.VariableScope.SERVER, "server");
        }
        
        return null;
    }
    
    private void setVariableValue(VariableManager variableManager, String varName, DataValue value, 
                                 VariableManager.VariableScope scope, String context) {
        switch (scope) {
            case PLAYER:
                variableManager.setPlayerVariable(java.util.UUID.fromString(context), varName, value);
                break;
            case LOCAL:
                variableManager.setLocalVariable(context, varName, value);
                break;
            case GLOBAL:
                variableManager.setGlobalVariable(varName, value);
                break;
            case SERVER:
                variableManager.setServerVariable(varName, value);
                break;
            default:
                // Default to local scope if scope is not specified
                variableManager.setLocalVariable(context, varName, value);
                break;
        }
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        try {
            // Get variable parameters from the container configuration
            SubVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            if (varName == null || varName.trim().isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }
            
            varName = varName.trim();

            // Parse the value as a number with validation
            double value;
            try {
                value = Double.parseDouble(valueStr);
                // Validate value range
                if (value < MIN_VARIABLE_VALUE || value > MAX_VARIABLE_VALUE) {
                    return ExecutionResult.error(String.format("Value must be between %f and %f", 
                        MIN_VARIABLE_VALUE, MAX_VARIABLE_VALUE));
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid numeric value: " + valueStr);
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager is not available");
            }
            
            // Get the variable value using the refactored method
            VariableValue variable = getVariableValue(variableManager, varName, context.getScriptId(), context.getPlayer());
            
            // Get current value or default to 0
            double currentValue = 0.0;
            VariableManager.VariableScope scope = VariableManager.VariableScope.LOCAL;
            String scopeContext = context.getScriptId();
            
            if (variable != null && variable.value != null) {
                try {
                    currentValue = variable.value.asNumber().doubleValue();
                    scope = variable.scope;
                    scopeContext = variable.context;
                } catch (NumberFormatException e) {
                    // If current value is not a number, treat as 0 and log a warning
                    context.getPlugin().getLogger().warning("Variable " + varName + " is not a number, treating as 0");
                }
            }
            
            // Calculate new value with bounds checking
            double newValue;
            try {
                newValue = Math.subtractExact((long)currentValue, (long)value);
                
                // Additional check for double overflow
                if (newValue > MAX_VARIABLE_VALUE || newValue < MIN_VARIABLE_VALUE) {
                    throw new ArithmeticException("Variable value out of range");
                }
            } catch (ArithmeticException e) {
                return ExecutionResult.error("Arithmetic error: " + e.getMessage());
            }
            
            // Set the updated value using the refactored method
            DataValue newValueData = DataValue.of(newValue);
            setVariableValue(variableManager, varName, newValueData, scope, scopeContext);
            
            context.getPlugin().getLogger().info("Subtracting " + value + " from variable " + varName + " (new value: " + newValue + ")");
            
            return ExecutionResult.success("Variable updated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private SubVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            throw new IllegalArgumentException("CodeBlock and ExecutionContext cannot be null");
        }
        
        SubVarParams params = new SubVarParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            if (blockConfigService == null) {
                context.getPlugin().getLogger().warning("BlockConfigService is not available");
                return params;
            }
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
```

```
package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for subtracting a value from a variable.
 * This action retrieves variable parameters from the container configuration and subtracts the value from the variable.
 */
@BlockMeta(id = "subVar", displayName = "§aSubtract from Variable", type = BlockType.ACTION)
public class SubVarAction implements BlockAction {
    
    // Define the missing constants
    private static final double MIN_VARIABLE_VALUE = -1000000.0;
    private static final double MAX_VARIABLE_VALUE = 1000000.0;

    private VariableValue getVariableValue(VariableManager variableManager, String varName, String scriptId, Player player) {
        // Try to get the variable from different scopes
        
        // First try player variables
        if (player != null) {
            DataValue var = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            if (var != null) {
                return new VariableValue(var, VariableManager.VariableScope.PLAYER, player.getUniqueId().toString());
            }
        }
        
        // If not found, try local variables
        DataValue localVar = variableManager.getLocalVariable(scriptId, varName);
        if (localVar != null) {
            return new VariableValue(localVar, VariableManager.VariableScope.LOCAL, scriptId);
        }
        
        // If not found, try global variables
        DataValue globalVar = variableManager.getGlobalVariable(varName);
        if (globalVar != null) {
            return new VariableValue(globalVar, VariableManager.VariableScope.GLOBAL, "global");
        }
        
        // If not found, try server variables
        DataValue serverVar = variableManager.getServerVariable(varName);
        if (serverVar != null) {
            return new VariableValue(serverVar, VariableManager.VariableScope.SERVER, "server");
        }
        
        return null;
    }
    
    private void setVariableValue(VariableManager variableManager, String varName, DataValue value, 
                                 VariableManager.VariableScope scope, String context) {
        switch (scope) {
            case PLAYER:
                variableManager.setPlayerVariable(java.util.UUID.fromString(context), varName, value);
                break;
            case LOCAL:
                variableManager.setLocalVariable(context, varName, value);
                break;
            case GLOBAL:
                variableManager.setGlobalVariable(varName, value);
                break;
            case SERVER:
                variableManager.setServerVariable(varName, value);
                break;
            default:
                // Default to local scope if scope is not specified
                variableManager.setLocalVariable(context, varName, value);
                break;
        }
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        try {
            // Get variable parameters from the container configuration
            SubVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            if (varName == null || varName.trim().isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }
            
            varName = varName.trim();

            // Parse the value as a number with validation
            double value;
            try {
                value = Double.parseDouble(valueStr);
                // Validate value range
                if (value < MIN_VARIABLE_VALUE || value > MAX_VARIABLE_VALUE) {
                    return ExecutionResult.error(String.format("Value must be between %f and %f", 
                        MIN_VARIABLE_VALUE, MAX_VARIABLE_VALUE));
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid numeric value: " + valueStr);
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager is not available");
            }
            
            // Get the variable value using the refactored method
            VariableValue variable = getVariableValue(variableManager, varName, context.getScriptId(), context.getPlayer());
            
            // Get current value or default to 0
            double currentValue = 0.0;
            VariableManager.VariableScope scope = VariableManager.VariableScope.LOCAL;
            String scopeContext = context.getScriptId();
            
            if (variable != null && variable.value != null) {
                try {
                    currentValue = variable.value.asNumber().doubleValue();
                    scope = variable.scope;
                    scopeContext = variable.context;
                } catch (NumberFormatException e) {
                    // If current value is not a number, treat as 0 and log a warning
                    context.getPlugin().getLogger().warning("Variable " + varName + " is not a number, treating as 0");
                }
            }
            
            // Calculate new value with bounds checking
            double newValue;
            try {
                newValue = Math.subtractExact((long)currentValue, (long)value);
                
                // Additional check for double overflow
                if (newValue > MAX_VARIABLE_VALUE || newValue < MIN_VARIABLE_VALUE) {
                    throw new ArithmeticException("Variable value out of range");
                }
            } catch (ArithmeticException e) {
                return ExecutionResult.error("Arithmetic error: " + e.getMessage());
            }
            
            // Set the updated value using the refactored method
            DataValue newValueData = DataValue.of(newValue);
            setVariableValue(variableManager, varName, newValueData, scope, scopeContext);
            
            context.getPlugin().getLogger().info("Subtracting " + value + " from variable " + varName + " (new value: " + newValue + ")");
            
            return ExecutionResult.success("Variable updated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private SubVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            throw new IllegalArgumentException("CodeBlock and ExecutionContext cannot be null");
        }
        
        SubVarParams params = new SubVarParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            if (blockConfigService == null) {
                context.getPlugin().getLogger().warning("BlockConfigService is not available");
                return params;
            }
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
```

```
package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for subtracting a value from a variable.
 * This action retrieves variable parameters from the container configuration and subtracts the value from the variable.
 */
@BlockMeta(id = "subVar", displayName = "§aSubtract from Variable", type = BlockType.ACTION)
public class SubVarAction implements BlockAction {
    
    // Define the missing constants
    private static final double MIN_VARIABLE_VALUE = -1000000.0;
    private static final double MAX_VARIABLE_VALUE = 1000000.0;

    private VariableValue getVariableValue(VariableManager variableManager, String varName, String scriptId, Player player) {
        // Try to get the variable from different scopes
        
        // First try player variables
        if (player != null) {
            DataValue var = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            if (var != null) {
                return new VariableValue(var, VariableManager.VariableScope.PLAYER, player.getUniqueId().toString());
            }
        }
        
        // If not found, try local variables
        DataValue localVar = variableManager.getLocalVariable(scriptId, varName);
        if (localVar != null) {
            return new VariableValue(localVar, VariableManager.VariableScope.LOCAL, scriptId);
        }
        
        // If not found, try global variables
        DataValue globalVar = variableManager.getGlobalVariable(varName);
        if (globalVar != null) {
            return new VariableValue(globalVar, VariableManager.VariableScope.GLOBAL, "global");
        }
        
        // If not found, try server variables
        DataValue serverVar = variableManager.getServerVariable(varName);
        if (serverVar != null) {
            return new VariableValue(serverVar, VariableManager.VariableScope.SERVER, "server");
        }
        
        return null;
    }
    
    private void setVariableValue(VariableManager variableManager, String varName, DataValue value, 
                                 VariableManager.VariableScope scope, String context) {
        switch (scope) {
            case PLAYER:
                variableManager.setPlayerVariable(java.util.UUID.fromString(context), varName, value);
                break;
            case LOCAL:
                variableManager.setLocalVariable(context, varName, value);
                break;
            case GLOBAL:
                variableManager.setGlobalVariable(varName, value);
                break;
            case SERVER:
                variableManager.setServerVariable(varName, value);
                break;
            default:
                // Default to local scope if scope is not specified
                variableManager.setLocalVariable(context, varName, value);
                break;
        }
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        try {
            // Get variable parameters from the container configuration
            SubVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            if (varName == null || varName.trim().isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }
            
            varName = varName.trim();

            // Parse the value as a number with validation
            double value;
            try {
                value = Double.parseDouble(valueStr);
                // Validate value range
                if (value < MIN_VARIABLE_VALUE || value > MAX_VARIABLE_VALUE) {
                    return ExecutionResult.error(String.format("Value must be between %f and %f", 
                        MIN_VARIABLE_VALUE, MAX_VARIABLE_VALUE));
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid numeric value: " + valueStr);
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager is not available");
            }
            
            // Get the variable value using the refactored method
            VariableValue variable = getVariableValue(variableManager, varName, context.getScriptId(), context.getPlayer());
            
            // Get current value or default to 0
            double currentValue = 0.0;
            VariableManager.VariableScope scope = VariableManager.VariableScope.LOCAL;
            String scopeContext = context.getScriptId();
            
            if (variable != null && variable.value != null) {
                try {
                    currentValue = variable.value.asNumber().doubleValue();
                    scope = variable.scope;
                    scopeContext = variable.context;
                } catch (NumberFormatException e) {
                    // If current value is not a number, treat as 0 and log a warning
                    context.getPlugin().getLogger().warning("Variable " + varName + " is not a number, treating as 0");
                }
            }
            
            // Calculate new value with bounds checking
            double newValue;
            try {
                newValue = Math.subtractExact((long)currentValue, (long)value);
                
                // Additional check for double overflow
                if (newValue > MAX_VARIABLE_VALUE || newValue < MIN_VARIABLE_VALUE) {
                    throw new ArithmeticException("Variable value out of range");
                }
            } catch (ArithmeticException e) {
                return ExecutionResult.error("Arithmetic error: " + e.getMessage());
            }
            
            // Set the updated value using the refactored method
            DataValue newValueData = DataValue.of(newValue);
            setVariableValue(variableManager, varName, newValueData, scope, scopeContext);
            
            context.getPlugin().getLogger().info("Subtracting " + value + " from variable " + varName + " (new value: " + newValue + ")");
            
            return ExecutionResult.success("Variable updated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private SubVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            throw new IllegalArgumentException("CodeBlock and ExecutionContext cannot be null");
        }
        
        SubVarParams params = new SubVarParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            if (blockConfigService == null) {
                context.getPlugin().getLogger().warning("BlockConfigService is not available");
                return params;
            }
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
```

```
package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for subtracting a value from a variable.
 * This action retrieves variable parameters from the container configuration and subtracts the value from the variable.
 */
@BlockMeta(id = "subVar", displayName = "§aSubtract from Variable", type = BlockType.ACTION)
public class SubVarAction implements BlockAction {
    
    // Define the missing constants
    private static final double MIN_VARIABLE_VALUE = -1000000.0;
    private static final double MAX_VARIABLE_VALUE = 1000000.0;

    private VariableValue getVariableValue(VariableManager variableManager, String varName, String scriptId, Player player) {
        // Try to get the variable from different scopes
        
        // First try player variables
        if (player != null) {
            DataValue var = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            if (var != null) {
                return new VariableValue(var, VariableManager.VariableScope.PLAYER, player.getUniqueId().toString());
            }
        }
        
        // If not found, try local variables
        DataValue localVar = variableManager.getLocalVariable(scriptId, varName);
        if (localVar != null) {
            return new VariableValue(localVar, VariableManager.VariableScope.LOCAL, scriptId);
        }
        
        // If not found, try global variables
        DataValue globalVar = variableManager.getGlobalVariable(varName);
        if (globalVar != null) {
            return new VariableValue(globalVar, VariableManager.VariableScope.GLOBAL, "global");
        }
        
        // If not found, try server variables
        DataValue serverVar = variableManager.getServerVariable(varName);
        if (serverVar != null) {
            return new VariableValue(serverVar, VariableManager.VariableScope.SERVER, "server");
        }
        
        return null;
    }
    
    private void setVariableValue(VariableManager variableManager, String varName, DataValue value, 
                                 VariableManager.VariableScope scope, String context) {
        switch (scope) {
            case PLAYER:
                variableManager.setPlayerVariable(java.util.UUID.fromString(context), varName, value);
                break;
            case LOCAL:
                variableManager.setLocalVariable(context, varName, value);
                break;
            case GLOBAL:
                variableManager.setGlobalVariable(varName, value);
                break;
            case SERVER:
                variableManager.setServerVariable(varName, value);
                break;
            default:
                // Default to local scope if scope is not specified
                variableManager.setLocalVariable(context, varName, value);
                break;
        }
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        try {
            // Get variable parameters from the container configuration
            SubVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            if (varName == null || varName.trim().isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }
            
            varName = varName.trim();

            // Parse the value as a number with validation
            double value;
            try {
                value = Double.parseDouble(valueStr);
                // Validate value range
                if (value < MIN_VARIABLE_VALUE || value > MAX_VARIABLE_VALUE) {
                    return ExecutionResult.error(String.format("Value must be between %f and %f", 
                        MIN_VARIABLE_VALUE, MAX_VARIABLE_VALUE));
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid numeric value: " + valueStr);
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager is not available");
            }
            
            // Get the variable value using the refactored method
            VariableValue variable = getVariableValue(variableManager, varName, context.getScriptId(), context.getPlayer());
            
            // Get current value or default to 0
            double currentValue = 0.0;
            VariableManager.VariableScope scope = VariableManager.VariableScope.LOCAL;
            String scopeContext = context.getScriptId();
            
            if (variable != null && variable.value != null) {
                try {
                    currentValue = variable.value.asNumber().doubleValue();
                    scope = variable.scope;
                    scopeContext = variable.context;
                } catch (NumberFormatException e) {
                    // If current value is not a number, treat as 0 and log a warning
                    context.getPlugin().getLogger().warning("Variable " + varName + " is not a number, treating as 0");
                }
            }
            
            // Calculate new value with bounds checking
            double newValue;
            try {
                newValue = Math.subtractExact((long)currentValue, (long)value);
                
                // Additional check for double overflow
                if (newValue > MAX_VARIABLE_VALUE || newValue < MIN_VARIABLE_VALUE) {
                    throw new ArithmeticException("Variable value out of range");
                }
            } catch (ArithmeticException e) {
                return ExecutionResult.error("Arithmetic error: " + e.getMessage());
            }
            
            // Set the updated value using the refactored method
            DataValue newValueData = DataValue.of(newValue);
            setVariableValue(variableManager, varName, newValueData, scope, scopeContext);
            
            context.getPlugin().getLogger().info("Subtracting " + value + " from variable " + varName + " (new value: " + newValue + ")");
            
            return ExecutionResult.success("Variable updated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private SubVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            throw new IllegalArgumentException("CodeBlock and ExecutionContext cannot be null");
        }
        
        SubVarParams params = new SubVarParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            if (blockConfigService == null) {
                context.getPlugin().getLogger().warning("BlockConfigService is not available");
                return params;
            }
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
```

```
package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for subtracting a value from a variable.
 * This action retrieves variable parameters from the container configuration and subtracts the value from the variable.
 */
@BlockMeta(id = "subVar", displayName = "§aSubtract from Variable", type = BlockType.ACTION)
public class SubVarAction implements BlockAction {
    
    // Define the missing constants
    private static final double MIN_VARIABLE_VALUE = -1000000.0;
    private static final double MAX_VARIABLE_VALUE = 1000000.0;

    private VariableValue getVariableValue(VariableManager variableManager, String varName, String scriptId, Player player) {
        // Try to get the variable from different scopes
        
        // First try player variables
        if (player != null) {
            DataValue var = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            if (var != null) {
                return new VariableValue(var, VariableManager.VariableScope.PLAYER, player.getUniqueId().toString());
            }
        }
        
        // If not found, try local variables
        DataValue localVar = variableManager.getLocalVariable(scriptId, varName);
        if (localVar != null) {
            return new VariableValue(localVar, VariableManager.VariableScope.LOCAL, scriptId);
        }
        
        // If not found, try global variables
        DataValue globalVar = variableManager.getGlobalVariable(varName);
        if (globalVar != null) {
            return new VariableValue(globalVar, VariableManager.VariableScope.GLOBAL, "global");
        }
        
        // If not found, try server variables
        DataValue serverVar = variableManager.getServerVariable(varName);
        if (serverVar != null) {
            return new VariableValue(serverVar, VariableManager.VariableScope.SERVER, "server");
        }
        
        return null;
    }
    
    private void setVariableValue(VariableManager variableManager, String varName, DataValue value, 
                                 VariableManager.VariableScope scope, String context) {
        switch (scope) {
            case PLAYER:
                variableManager.setPlayerVariable(java.util.UUID.fromString(context), varName, value);
                break;
            case LOCAL:
                variableManager.setLocalVariable(context, varName, value);
                break;
            case GLOBAL:
                variableManager.setGlobalVariable(varName, value);
                break;
            case SERVER:
                variableManager.setServerVariable(varName, value);
                break;
            default:
                // Default to local scope if scope is not specified
                variableManager.setLocalVariable(context, varName, value);
                break;
        }
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        try {
            // Get variable parameters from the container configuration
            SubVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            if (varName == null || varName.trim().isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }
            
            varName = varName.trim();

            // Parse the value as a number with validation
            double value;
            try {
                value = Double.parseDouble(valueStr);
                // Validate value range
                if (value < MIN_VARIABLE_VALUE || value > MAX_VARIABLE_VALUE) {
                    return ExecutionResult.error(String.format("Value must be between %f and %f", 
                        MIN_VARIABLE_VALUE, MAX_VARIABLE_VALUE));
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid numeric value: " + valueStr);
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager is not available");
            }
            
            // Get the variable value using the refactored method
            VariableValue variable = getVariableValue(variableManager, varName, context.getScriptId(), context.getPlayer());
            
            // Get current value or default to 0
            double currentValue = 0.0;
            VariableManager.VariableScope scope = VariableManager.VariableScope.LOCAL;
            String scopeContext = context.getScriptId();
            
            if (variable != null && variable.value != null) {
                try {
                    currentValue = variable.value.asNumber().doubleValue();
                    scope = variable.scope;
                    scopeContext = variable.context;
                } catch (NumberFormatException e) {
                    // If current value is not a number, treat as 0 and log a warning
                    context.getPlugin().getLogger().warning("Variable " + varName + " is not a number, treating as 0");
                }
            }
            
            // Calculate new value with bounds checking
            double newValue;
            try {
                newValue = Math.subtractExact((long)currentValue, (long)value);
                
                // Additional check for double overflow
                if (newValue > MAX_VARIABLE_VALUE || newValue < MIN_VARIABLE_VALUE) {
                    throw new ArithmeticException("Variable value out of range");
                }
            } catch (ArithmeticException e) {
                return ExecutionResult.error("Arithmetic error: " + e.getMessage());
            }
            
            // Set the updated value using the refactored method
            DataValue newValueData = DataValue.of(newValue);
            setVariableValue(variableManager, varName, newValueData, scope, scopeContext);
            
            context.getPlugin().getLogger().info("Subtracting " + value + " from variable " + varName + " (new value: " + newValue + ")");
            
            return ExecutionResult.success("Variable updated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private SubVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            throw new IllegalArgumentException("CodeBlock and ExecutionContext cannot be null");
        }
        
        SubVarParams params = new SubVarParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            if (blockConfigService == null) {
                context.getPlugin().getLogger().warning("BlockConfigService is not available");
                return params;
            }
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
```

```
package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for subtracting a value from a variable.
 * This action retrieves variable parameters from the container configuration and subtracts the value from the variable.
 */
@BlockMeta(id = "subVar", displayName = "§aSubtract from Variable", type = BlockType.ACTION)
public class SubVarAction implements BlockAction {
    
    // Define the missing constants
    private static final double MIN_VARIABLE_VALUE = -1000000.0;
    private static final double MAX_VARIABLE_VALUE = 1000000.0;

    private VariableValue getVariableValue(VariableManager variableManager, String varName, String scriptId, Player player) {
        // Try to get the variable from different scopes
        
        // First try player variables
        if (player != null) {
            DataValue var = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            if (var != null) {
                return new VariableValue(var, VariableManager.VariableScope.PLAYER, player.getUniqueId().toString());
            }
        }
        
        // If not found, try local variables
        DataValue localVar = variableManager.getLocalVariable(scriptId, varName);
        if (localVar != null) {
            return new VariableValue(localVar, VariableManager.VariableScope.LOCAL, scriptId);
        }
        
        // If not found, try global variables
        DataValue globalVar = variableManager.getGlobalVariable(varName);
        if (globalVar != null) {
            return new VariableValue(globalVar, VariableManager.VariableScope.GLOBAL, "global");
        }
        
        // If not found, try server variables
        DataValue serverVar = variableManager.getServerVariable(varName);
        if (serverVar != null) {
            return new VariableValue(serverVar, VariableManager.VariableScope.SERVER, "server");
        }
        
        return null;
    }
    
    private void setVariableValue(VariableManager variableManager, String varName, DataValue value, 
                                 VariableManager.VariableScope scope, String context) {
        switch (scope) {
            case PLAYER:
                variableManager.setPlayerVariable(java.util.UUID.fromString(context), varName, value);
                break;
            case LOCAL:
                variableManager.setLocalVariable(context, varName, value);
                break;
            case GLOBAL:
                variableManager.setGlobalVariable(varName, value);
                break;
            case SERVER:
                variableManager.setServerVariable(varName, value);
                break;
            default:
                // Default to local scope if scope is not specified
                variableManager.setLocalVariable(context, varName, value);
                break;
        }
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        try {
            // Get variable parameters from the container configuration
            SubVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            if (varName == null || varName.trim().isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }
            
            varName = varName.trim();

            // Parse the value as a number with validation
            double value;
            try {
                value = Double.parseDouble(valueStr);
                // Validate value range
                if (value < MIN_VARIABLE_VALUE || value > MAX_VARIABLE_VALUE) {
                    return ExecutionResult.error(String.format("Value must be between %f and %f", 
                        MIN_VARIABLE_VALUE, MAX_VARIABLE_VALUE));
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid numeric value: " + valueStr);
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager is not available");
            }
            
            // Get the variable value using the refactored method
            VariableValue variable = getVariableValue(variableManager, varName, context.getScriptId(), context.getPlayer());
            
            // Get current value or default to 0
            double currentValue = 0.0;
            VariableManager.VariableScope scope = VariableManager.VariableScope.LOCAL;
            String scopeContext = context.getScriptId();
            
            if (variable != null && variable.value != null) {
                try {
                    currentValue = variable.value.asNumber().doubleValue();
                    scope = variable.scope;
                    scopeContext = variable.context;
                } catch (NumberFormatException e) {
                    // If current value is not a number, treat as 0 and log a warning
                    context.getPlugin().getLogger().warning("Variable " + varName + " is not a number, treating as 0");
                }
            }
            
            // Calculate new value with bounds checking
            double newValue;
            try {
                newValue = Math.subtractExact((long)currentValue, (long)value);
                
                // Additional check for double overflow
                if (newValue > MAX_VARIABLE_VALUE || newValue < MIN_VARIABLE_VALUE) {
                    throw new ArithmeticException("Variable value out of range");
                }
            } catch (ArithmeticException e) {
                return ExecutionResult.error("Arithmetic error: " + e.getMessage());
            }
            
            // Set the updated value using the refactored method
            DataValue newValueData = DataValue.of(newValue);
            setVariableValue(variableManager, varName, newValueData, scope, scopeContext);
            
            context.getPlugin().getLogger().info("Subtracting " + value + " from variable " + varName + " (new value: " + newValue + ")");
            
            return ExecutionResult.success("Variable updated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private SubVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            throw new IllegalArgumentException("CodeBlock and ExecutionContext cannot be null");
        }
        
        SubVarParams params = new SubVarParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            if (blockConfigService == null) {
                context.getPlugin().getLogger().warning("BlockConfigService is not available");
                return params;
            }
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
```

```
package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for subtracting a value from a variable.
 * This action retrieves variable parameters from the container configuration and subtracts the value from the variable.
 */
@BlockMeta(id = "subVar", displayName = "§aSubtract from Variable", type = BlockType.ACTION)
public class SubVarAction implements BlockAction {
    
    // Define the missing constants
    private static final double MIN_VARIABLE_VALUE = -1000000.0;
    private static final double MAX_VARIABLE_VALUE = 1000000.0;

    private VariableValue getVariableValue(VariableManager variableManager, String varName, String scriptId, Player player) {
        // Try to get the variable from different scopes
        
        // First try player variables
        if (player != null) {
            DataValue var = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            if (var != null) {
                return new VariableValue(var, VariableManager.VariableScope.PLAYER, player.getUniqueId().toString());
            }
        }
        
        // If not found, try local variables
        DataValue localVar = variableManager.getLocalVariable(scriptId, varName);
        if (localVar != null) {
            return new VariableValue(localVar, VariableManager.VariableScope.LOCAL, scriptId);
        }
        
        // If not found, try global variables
        DataValue globalVar = variableManager.getGlobalVariable(varName);
        if (globalVar != null) {
            return new VariableValue(globalVar, VariableManager.VariableScope.GLOBAL, "global");
        }
        
        // If not found, try server variables
        DataValue serverVar = variableManager.getServerVariable(varName);
        if (serverVar != null) {
            return new VariableValue(serverVar, VariableManager.VariableScope.SERVER, "server");
        }
        
        return null;
    }
    
    private void setVariableValue(VariableManager variableManager, String varName, DataValue value, 
                                 VariableManager.VariableScope scope, String context) {
        switch (scope) {
            case PLAYER:
                variableManager.setPlayerVariable(java.util.UUID.fromString(context), varName, value);
                break;
            case LOCAL:
                variableManager.setLocalVariable(context, varName, value);
                break;
            case GLOBAL:
                variableManager.setGlobalVariable(varName, value);
                break;
            case SERVER:
                variableManager.setServerVariable(varName, value);
                break;
            default:
                // Default to local scope if scope is not specified
                variableManager.setLocalVariable(context, varName, value);
                break;
        }
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        try {
            // Get variable parameters from the container configuration
            SubVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            if (varName == null || varName.trim().isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }
            
            varName = varName.trim();

            // Parse the value as a number with validation
            double value;
            try {
                value = Double.parseDouble(valueStr);
                // Validate value range
                if (value < MIN_VARIABLE_VALUE || value > MAX_VARIABLE_VALUE) {
                    return ExecutionResult.error(String.format("Value must be between %f and %f", 
                        MIN_VARIABLE_VALUE, MAX_VARIABLE_VALUE));
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid numeric value: " + valueStr);
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager is not available");
            }
            
            // Get the variable value using the refactored method
            VariableValue variable = getVariableValue(variableManager, varName, context.getScriptId(), context.getPlayer());
            
            // Get current value or default to 0
            double currentValue = 0.0;
            VariableManager.VariableScope scope = VariableManager.VariableScope.LOCAL;
            String scopeContext = context.getScriptId();
            
            if (variable != null && variable.value != null) {
                try {
                    currentValue = variable.value.asNumber().doubleValue();
                    scope = variable.scope;
                    scopeContext = variable.context;
                } catch (NumberFormatException e) {
                    // If current value is not a number, treat as 0 and log a warning
                    context.getPlugin().getLogger().warning("Variable " + varName + " is not a number, treating as 0");
                }
            }
            
            // Calculate new value with bounds checking
            double newValue;
            try {
                newValue = Math.subtractExact((long)currentValue, (long)value);
                
                // Additional check for double overflow
                if (newValue > MAX_VARIABLE_VALUE || newValue < MIN_VARIABLE_VALUE) {
                    throw new ArithmeticException("Variable value out of range");
                }
            } catch (ArithmeticException e) {
                return ExecutionResult.error("Arithmetic error: " + e.getMessage());
            }
            
            // Set the updated value using the refactored method
            DataValue newValueData = DataValue.of(newValue);
            setVariableValue(variableManager, varName, newValueData, scope, scopeContext);
            
            context.getPlugin().getLogger().info("Subtracting " + value + " from variable " + varName + " (new value: " + newValue + ")");
            
            return ExecutionResult.success("Variable updated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to update variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private SubVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            throw new IllegalArgumentException("CodeBlock and ExecutionContext cannot be null");
        }
        
        SubVarParams params = new SubVarParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            if (blockConfigService == null) {
                context.getPlugin().getLogger().warning("BlockConfigService is not available");
                return params;
            }
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
```

```
package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for subtracting a value from a variable.
 * This action retrieves variable parameters from the container configuration and subtracts the value from the variable.
 */
@BlockMeta(id = "subVar", displayName = "§aSubtract from Variable", type = BlockType.ACTION)
public class SubVarAction implements BlockAction {
    
    // Define the missing constants
    private static final double MIN_VARIABLE_VALUE = -1000000.0;
    private static final double MAX_VARIABLE_VALUE = 1000000.0;

    private VariableValue getVariableValue(VariableManager variableManager, String varName, String scriptId, Player player) {
        // Try to get the variable from different scopes
        
        // First try player variables
        if (player != null) {
            DataValue var = variableManager.getPlayerVariable(player.getUniqueId(), varName);
            if (var != null) {
                return new VariableValue(var, VariableManager.VariableScope.PLAYER, player.getUniqueId().toString());
            }
        }
        
        // If not found, try local variables
        DataValue localVar = variableManager.getLocalVariable(scriptId, varName);
        if (localVar != null) {
            return new VariableValue(localVar, VariableManager.VariableScope.LOCAL, scriptId);
        }
        
        // If not found, try global variables
        DataValue globalVar = variableManager.getGlobalVariable(varName);
        if (globalVar != null) {
            return new VariableValue(globalVar, VariableManager.VariableScope.GLOBAL, "global");
        }
        
        // If not found, try server variables
        DataValue serverVar = variableManager.getServerVariable(varName);
        if (serverVar != null) {
            return new VariableValue(serverVar, VariableManager.VariableScope.SERVER, "server");
        }
        
        return null;
    }
    
    private void setVariableValue(VariableManager variableManager, String varName, DataValue value, 
                                 VariableManager.VariableScope scope, String context) {
        switch (scope) {
            case PLAYER:
                variableManager.setPlayerVariable(java.util.UUID.fromString(context), varName, value);
                break;
            case LOCAL:
                variableManager.setLocalVariable(context, varName, value);
                break;
            case GLOBAL:
                variableManager.setGlobalVariable(varName, value);
                break;
            case SERVER:
                variableManager.setServerVariable(varName, value);
                break;
            default:
                // Default to local scope if scope is not specified
                variableManager.setLocalVariable(context, varName, value);
                break;
        }
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        try {
            // Get variable parameters from the container configuration
            SubVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            if (varName == null || varName.trim().isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }
            
            varName = varName.trim();

            // Parse the value as a number with validation
            double value;
            try {
                value = Double.parseDouble(valueStr);
                // Validate value range
                if (value < MIN_VARIABLE_VALUE || value > MAX_VARIABLE_VALUE) {
                    return ExecutionResult.error(String.format("Value must be between %f and %f", 
                        MIN_VARIABLE_VALUE, MAX_VARIABLE_VALUE));
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid numeric value: " + valueStr);
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager is not available");
            }
            
            // Get the variable value using the refactored method
            VariableValue variable = getVariableValue(variableManager, varName, context.getScriptId(), context.getPlayer());
            
            // Get current value or default to 0
            double currentValue = 0.0;
            VariableManager.VariableScope scope = VariableManager.VariableScope.LOCAL;
            String scopeContext = context.getScriptId();
            
            if (variable != null && variable.value != null) {
                try {
                    currentValue = variable.value.asNumber().doubleValue();
                    scope = variable.scope;
                    scopeContext = variable.context;
                } catch (NumberFormatException e) {
                    // If current value is not a number, treat as 0 and log a warning
                    context.getPlugin().getLogger().warning("Variable " + varName + " is not a number, treating as 0");
                }
            }
            
            // Calculate new value with bounds checking
            double newValue;
            try {
                newValue = Math.subtractExact((long)currentValue, (long)value);
                
                // Additional check for double overflow
                if (newValue > MAX_VARIABLE_VALUE || newValue < MIN_VARIABLE_VALUE) {
                    throw new ArithmeticException("Variable value out