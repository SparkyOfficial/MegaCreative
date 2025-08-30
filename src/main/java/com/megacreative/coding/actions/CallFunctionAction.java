package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.MegaCreative;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Действие для вызова функции с параметрами.
 * Поддерживает параметры из виртуального инвентаря.
 * 
 * Пример использования:
 * onInteract -> callFunction("teleportToSpawn", [компас]) -> sendMessage("Телепортация...")
 */
public class CallFunctionAction implements BlockAction {

    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;

        CodeBlock actionBlock = context.getCurrentBlock();
        if (actionBlock == null) return;

        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем имя функции из параметра
        DataValue functionNameValue = actionBlock.getParameter("functionName");
        if (functionNameValue == null) {
            player.sendMessage("§cОшибка: не указано имя функции!");
            return;
        }
        
        String functionName = resolver.resolve(context, functionNameValue).asString();
        if (functionName == null || functionName.isEmpty()) {
            player.sendMessage("§cОшибка: не указано имя функции!");
            return;
        }

        // Получаем параметры из виртуального инвентаря
        Map<String, DataValue> functionParams = new HashMap<>();
        
        // Добавляем стандартные параметры
        for (Map.Entry<String, DataValue> entry : actionBlock.getParameters().entrySet()) {
            if (!entry.getKey().equals("functionName")) {
                functionParams.put(entry.getKey(), entry.getValue());
            }
        }
        
        // Получаем параметры из виртуального инвентаря
        for (int i = 0; i < 27; i++) {
            ItemStack item = actionBlock.getConfigItem(i);
            if (item != null) {
                functionParams.put("param_" + i, DataValue.fromObject(item.getType().name()));
                functionParams.put("param_" + i + "_amount", DataValue.fromObject(item.getAmount()));
            }
        }

        // Создаем новый контекст с параметрами функции
        ExecutionContext functionContext = context.withCurrentBlock(context.getCurrentBlock(), context.getBlockLocation());
        
        // Добавляем параметры в контекст функции
        for (Map.Entry<String, DataValue> entry : functionParams.entrySet()) {
            functionContext.setVariable(entry.getKey(), entry.getValue());
        }
        
        // Вызываем функцию
        MegaCreative plugin = context.getPlugin();
        if (plugin != null) {
            // Ищем функцию в текущем мире
            var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            CodeScript function = null;
            if (creativeWorld != null) {
                for (CodeScript script : creativeWorld.getScripts()) {
                    if (script.getName().equals(functionName) && script.getType() == CodeScript.ScriptType.FUNCTION) {
                        function = script;
                        break;
                    }
                }
            }
            
            if (function != null) {
                new com.megacreative.coding.ScriptExecutor(plugin).execute(function, functionContext, "function");
                player.sendMessage("§a✓ Функция '" + functionName + "' выполнена!");
            } else {
                player.sendMessage("§cОшибка: функция '" + functionName + "' не найдена!");
            }
        }
    }
} 