package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.NumberValue;
import org.bukkit.entity.Player;

/**
 * Multiplies a variable by a numeric value.
 * Supports LOCAL, GLOBAL, PLAYER, and SERVER scopes.
 */
public class MulVarAction implements BlockAction {
    
    // Конструктор по умолчанию (без параметров)
    public MulVarAction() {}
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }
        
        try {
            // Создаем ParameterResolver прямо здесь, передавая ему текущий контекст
            ParameterResolver resolver = new ParameterResolver(context);

            // Get variable name (required)
            DataValue rawVarName = block.getParameter("var");
            if (rawVarName == null || rawVarName.isEmpty()) {
                player.sendMessage("§cОшибка: не указано имя переменной!");
                return ExecutionResult.error("Variable name not specified");
            }

            // Get value to multiply by (required)
            DataValue rawValue = block.getParameter("value");
            if (rawValue == null || rawValue.isEmpty()) {
                player.sendMessage("§cОшибка: не указано значение для умножения!");
                return ExecutionResult.error("Value to multiply not specified");
            }
            
            // Resolve variable name and value
            String varName = resolver.resolve(context, rawVarName).asString();
            if (varName == null || varName.trim().isEmpty()) {
                player.sendMessage("§cОшибка: имя переменной не может быть пустым!");
                return ExecutionResult.error("Variable name is empty");
            }
            
            // Get scope (optional, defaults to LOCAL)
            DataValue scopeValue = block.getParameter("scope");
            String scope = scopeValue != null ? scopeValue.asString().toUpperCase() : "LOCAL";
            
            // Get current value from the appropriate scope
            Object currentValue = null;
            switch (scope) {
                case "GLOBAL":
                    currentValue = context.getGlobalVariable(varName);
                    break;
                case "PLAYER":
                    currentValue = context.getPlayerVariable(varName);
                    break;
                case "SERVER":
                    currentValue = context.getServerVariable(varName);
                    break;
                case "LOCAL":
                default:
                    currentValue = context.getVariable(varName);
                    break;
            }
            
            // Parse current value as double
            double currentNum = 0.0;
            if (currentValue != null) {
                if (currentValue instanceof Number) {
                    currentNum = ((Number) currentValue).doubleValue();
                } else if (currentValue instanceof DataValue) {
                    try {
                        currentNum = ((DataValue) currentValue).asNumber().doubleValue();
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cОшибка: текущее значение переменной не является числом!");
                        return ExecutionResult.error("Current variable value is not a number");
                    }
                } else {
                    try {
                        currentNum = Double.parseDouble(currentValue.toString());
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cОшибка: текущее значение переменной не является числом!");
                        return ExecutionResult.error("Current variable value is not a number");
                    }
                }
            }
            
            // Parse and multiply the value
            DataValue resolvedValue = resolver.resolve(context, rawValue);
            double mulValue;
            try {
                mulValue = resolvedValue.asNumber().doubleValue();
            } catch (NumberFormatException e) {
                player.sendMessage("§cОшибка: значение для умножения должно быть числом!");
                return ExecutionResult.error("Value to multiply must be a number");
            }
            
            double result = currentNum * mulValue;
            
            // Save the result back to the appropriate scope
            DataValue resultValue = new NumberValue(result);
            switch (scope) {
                case "GLOBAL":
                    context.setGlobalVariable(varName, resultValue);
                    break;
                case "PLAYER":
                    context.setPlayerVariable(varName, resultValue);
                    break;
                case "SERVER":
                    context.setServerVariable(varName, resultValue);
                    break;
                case "LOCAL":
                default:
                    context.setVariable(varName, resultValue);
                    break;
            }
            
            player.sendMessage("§a✓ Переменная '§f" + varName + "§a' (§b" + scope + 
                "§a) умножена на §e" + mulValue + "§a = §e" + result);
            
            return ExecutionResult.success("Variable '" + varName + "' multiplied by " + mulValue);
        } catch (Exception e) {
            player.sendMessage("§cОшибка при изменении переменной: " + e.getMessage());
            context.getPlugin().getLogger().warning("Error in MulVarAction: " + e.getMessage());
            e.printStackTrace();
            return ExecutionResult.error("Error in MulVarAction: " + e.getMessage());
        }
    }
}