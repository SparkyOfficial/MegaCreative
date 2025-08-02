package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptExecutor;
import com.megacreative.MegaCreative;
import org.bukkit.entity.Player;

import java.util.Map;

public class CallFunctionAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawFunctionName = block.getParameter("function");
        String functionName = ParameterResolver.resolve(context, rawFunctionName);

        if (functionName == null) {
            player.sendMessage("§cОшибка: параметр 'function' не указан");
            return;
        }

        // Получаем плагин для доступа к функциям
        MegaCreative plugin = context.getPlugin();
        if (plugin == null) {
            player.sendMessage("§cОшибка: плагин недоступен");
            return;
        }

        // Ищем функцию в текущем мире
        CodeScript function = findFunction(plugin, player, functionName);
        if (function == null) {
            player.sendMessage("§cОшибка: функция '" + functionName + "' не найдена");
            return;
        }

        // Создаем новый контекст для выполнения функции
        ExecutionContext functionContext = context.withCurrentBlock(function.getRootBlock(), context.getBlockLocation());
        
        // Копируем переменные из текущего контекста в контекст функции
        for (Map.Entry<String, Object> entry : context.getVariables().entrySet()) {
            functionContext.setVariable(entry.getKey(), entry.getValue());
        }

        try {
            // Выполняем функцию
            ScriptExecutor executor = new ScriptExecutor(plugin);
            executor.processBlock(function.getRootBlock(), functionContext);
            
            player.sendMessage("§a📞 Функция '" + functionName + "' выполнена");
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка при выполнении функции '" + functionName + "': " + e.getMessage());
        }
    }
    
    /**
     * Ищет функцию в скриптах текущего мира
     */
    private CodeScript findFunction(MegaCreative plugin, Player player, String functionName) {
        // Получаем текущий мир игрока
        var creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null) return null;
        
        // Ищем функцию среди скриптов мира
        for (CodeScript script : creativeWorld.getScripts()) {
            if (script.getName().equals(functionName) && script.getType() == CodeScript.ScriptType.FUNCTION) {
                return script;
            }
        }
        
        return null;
    }
} 