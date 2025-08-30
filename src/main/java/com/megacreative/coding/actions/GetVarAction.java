package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Retrieves and displays the value of a variable from the specified scope.
 * Supports LOCAL, GLOBAL, PLAYER, and SERVER scopes.
 */
public class GetVarAction implements BlockAction {
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

            String varName = resolver.resolve(context, rawVarName).asString();
            if (varName == null || varName.trim().isEmpty()) {
                player.sendMessage("§cОшибка: имя переменной не может быть пустым!");
                return;
            }

            // Get scope (optional, defaults to LOCAL)
            DataValue scopeValue = block.getParameter("scope");
            String scope = scopeValue != null ? scopeValue.asString().toUpperCase() : "LOCAL";
            
            // Get variable from the appropriate scope
            Object value = null;
            switch (scope) {
                case "GLOBAL":
                    value = context.getGlobalVariable(varName);
                    break;
                case "PLAYER":
                    value = context.getPlayerVariable(varName);
                    break;
                case "SERVER":
                    value = context.getServerVariable(varName);
                    break;
                case "LOCAL":
                default:
                    value = context.getVariable(varName);
                    break;
            }
            
            // Format the value for display
            String displayValue = "не установлена";
            String typeInfo = "unknown";
            
            if (value != null) {
                if (value instanceof DataValue) {
                    DataValue dataValue = (DataValue) value;
                    typeInfo = dataValue.getType().toString();
                    displayValue = dataValue.asString();
                } else {
                    typeInfo = value.getClass().getSimpleName();
                    displayValue = value.toString();
                }
                
                if (displayValue.length() > 50) {
                    displayValue = displayValue.substring(0, 47) + "...";
                }
            }
            
            player.sendMessage("§a📖 Переменная '§f" + varName + "§a' (§b" + scope + "§a, §e" + typeInfo + "§a) = §f" + displayValue);
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка при получении переменной: " + e.getMessage());
            context.getPlugin().getLogger().warning("Error in GetVarAction: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 