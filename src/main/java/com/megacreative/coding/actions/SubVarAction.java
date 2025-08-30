package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.NumberValue;
import org.bukkit.entity.Player;

/**
 * Subtracts a numeric value from a variable.
 * Supports LOCAL, GLOBAL, PLAYER, and SERVER scopes.
 */
public class SubVarAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;
        
        try {
            ParameterResolver resolver = new ParameterResolver(context);

            // Get variable name (required)
            DataValue rawVarName = block.getParameter("var");
            if (rawVarName == null || rawVarName.isEmpty()) {
                player.sendMessage("§cОшибка: не указано имя переменной!");
                return;
            }

            // Get value to subtract (required)
            DataValue rawValue = block.getParameter("value");
            if (rawValue == null || rawValue.isEmpty()) {
                player.sendMessage("§cОшибка: не указано значение для вычитания!");
                return;
            }
            
            // Resolve variable name and value
            String varName = resolver.resolve(context, rawVarName).asString();
            if (varName == null || varName.trim().isEmpty()) {
                player.sendMessage("§cОшибка: имя переменной не может быть пустым!");
                return;
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
                        return;
                    }
                } else {
                    try {
                        currentNum = Double.parseDouble(currentValue.toString());
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cОшибка: текущее значение переменной не является числом!");
                        return;
                    }
                }
            }
            
            // Parse and subtract the value
            DataValue resolvedValue = resolver.resolve(context, rawValue);
            double subValue;
            try {
                subValue = resolvedValue.asNumber().doubleValue();
            } catch (NumberFormatException e) {
                player.sendMessage("§cОшибка: значение для вычитания должно быть числом!");
                return;
            }
            
            double result = currentNum - subValue;
            
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
                "§a) уменьшена на §e" + subValue + "§a = §e" + result);
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка при изменении переменной: " + e.getMessage());
            context.getPlugin().getLogger().warning("Error in SubVarAction: " + e.getMessage());
            e.printStackTrace();
        }
    }
}