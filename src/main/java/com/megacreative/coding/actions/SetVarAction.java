package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Advanced variable setting action with full DataValue and ExecutionContext support
 * Supports multiple scopes: Local, Global, Player, Server
 * Uses type-safe DataValue system for proper variable handling
 */
public class SetVarAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return;
        
        try {
            ParameterResolver resolver = new ParameterResolver(context);
            
            // Get variable name (required)
            DataValue varNameValue = block.getParameter("var");
            if (varNameValue == null || varNameValue.isEmpty()) {
                player.sendMessage("§cОшибка: не указано имя переменной!");
                return;
            }
            
            String varName = resolver.resolve(context, varNameValue).asString();
            if (varName.trim().isEmpty()) {
                player.sendMessage("§cОшибка: имя переменной не может быть пустым!");
                return;
            }
            
            // Get variable value (required)
            DataValue valueParam = block.getParameter("value");
            if (valueParam == null) {
                player.sendMessage("§cОшибка: не указано значение переменной!");
                return;
            }
            
            DataValue resolvedValue = resolver.resolve(context, valueParam);
            
            // Get scope (optional, defaults to LOCAL)
            DataValue scopeValue = block.getParameter("scope");
            String scope = scopeValue != null ? scopeValue.asString().toUpperCase() : "LOCAL";
            
            // Set variable in the appropriate scope
            switch (scope) {
                case "GLOBAL":
                    context.setGlobalVariable(varName, resolvedValue);
                    break;
                case "PLAYER":
                    context.setPlayerVariable(varName, resolvedValue);
                    break;
                case "SERVER":
                    context.setServerVariable(varName, resolvedValue);
                    break;
                case "LOCAL":
                default:
                    context.setVariable(varName, resolvedValue);
                    break;
            }
            
            // Success message with type information
            String typeInfo = resolvedValue.getType().toString();
            String valueDisplay = resolvedValue.asString();
            if (valueDisplay.length() > 50) {
                valueDisplay = valueDisplay.substring(0, 47) + "...";
            }
            
            player.sendMessage("§a✓ Переменная '§f" + varName + "§a' (§b" + scope + "§a, §e" + typeInfo + "§a) установлена в: §f" + valueDisplay);
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка при установке переменной: " + e.getMessage());
            context.getPlugin().getLogger().warning("Error in SetVarAction: " + e.getMessage());
            e.printStackTrace();
        }
    }
}