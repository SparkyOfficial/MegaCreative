package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.TextValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.VariableScope;
import org.bukkit.entity.Player;

/**
 * Advanced variable setting action with full DataValue and VariableManager support
 * Supports multiple scopes: Local, World, Player, Server
 * Uses type-safe DataValue system for proper variable handling
 */
public class SetVarAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();
        
        if (player == null || block == null || variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);
        
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
        DataValue scopeValue = block.getParameter("scope", new TextValue("LOCAL"));
        String scopeStr = resolver.resolve(context, scopeValue).asString().toUpperCase();
        VariableScope scope;
        
        try {
            scope = VariableScope.valueOf(scopeStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cОшибка: неизвестная область видимости '" + scopeStr + "'. Доступные: LOCAL, WORLD, PLAYER, SERVER");
            return;
        }
        
        // Set variable in appropriate scope
        try {
            switch (scope) {
                case LOCAL:
                    variableManager.setLocalVariable(context.getScriptId(), varName, resolvedValue);
                    break;
                case WORLD:
                    variableManager.setGlobalVariable(context.getWorldId(), varName, resolvedValue);
                    break;
                case PLAYER:
                    if (player != null) {
                        variableManager.setPlayerVariable(player.getUniqueId(), varName, resolvedValue);
                    }
                    break;
                case SERVER:
                    variableManager.setPersistentVariable(varName, resolvedValue);
                    break;
            }
            
            // Success message with type information
            String typeInfo = resolvedValue.getType().getDisplayName();
            String valueDisplay = resolvedValue.asString();
            if (valueDisplay.length() > 50) {
                valueDisplay = valueDisplay.substring(0, 47) + "...";
            }
            
            player.sendMessage("§a✓ Переменная '§f" + varName + "§a' (§b" + scope + "§a, §e" + typeInfo + "§a) установлена в: §f" + valueDisplay);
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка при установке переменной: " + e.getMessage());
            context.getPlugin().getLogger().warning("Error setting variable '" + varName + "': " + e.getMessage());
        }
    }
} 