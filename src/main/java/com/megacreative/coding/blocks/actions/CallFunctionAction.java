package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.entity.Player;

import java.util.List;

public class CallFunctionAction implements BlockAction {
    
    private final Argument<TextValue> functionNameArgument;
    
    public CallFunctionAction() {
        this.functionNameArgument = new ParameterArgument("functionName");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        try {
            TextValue functionNameValue = functionNameArgument.parse(context.getCurrentBlock()).orElse(null);
            if (functionNameValue == null) {
                player.sendMessage("§cОшибка: не указано имя функции!");
                return;
            }
            
            String functionName = functionNameValue.get(context);
            
            // Ищем функцию в текущем мире
            var creativeWorld = context.getCreativeWorld();
            if (creativeWorld == null) {
                player.sendMessage("§cОшибка: не удалось определить мир!");
                return;
            }
            
            List<CodeScript> scripts = creativeWorld.getScripts();
            CodeScript targetFunction = null;
            
            // Ищем функцию по имени
            for (CodeScript script : scripts) {
                if (script.getType() == CodeScript.ScriptType.FUNCTION && 
                    script.getName().equals(functionName) && 
                    script.isEnabled()) {
                    targetFunction = script;
                    break;
                }
            }
            
            if (targetFunction == null) {
                player.sendMessage("§cОшибка: функция '" + functionName + "' не найдена!");
                return;
            }
            
            // Выполняем функцию
            player.sendMessage("§aВызываю функцию: " + functionName);
            context.getPlugin().getCodingManager().getScriptExecutor().execute(targetFunction, context, "function");
            player.sendMessage("§a✓ Функция '" + functionName + "' выполнена");
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка в блоке 'Вызвать функцию': " + e.getMessage());
        }
    }
} 