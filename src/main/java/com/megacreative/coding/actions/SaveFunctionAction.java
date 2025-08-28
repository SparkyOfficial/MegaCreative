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

public class SaveFunctionAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        VariableManager variableManager = context.getPlugin().getVariableManager();
        ParameterResolver resolver = new ParameterResolver(variableManager);

        // Получаем и разрешаем параметры
        DataValue rawFunctionName = block.getParameter("name");
        String functionName = resolver.resolve(context, rawFunctionName).asString();

        if (functionName == null) {
            player.sendMessage("§cОшибка: параметр 'name' не указан");
            return;
        }

        // Получаем плагин для доступа к менеджерам
        MegaCreative plugin = context.getPlugin();
        if (plugin == null) {
            player.sendMessage("§cОшибка: плагин недоступен");
            return;
        }

        // Получаем текущий мир игрока
        var creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null) {
            player.sendMessage("§cОшибка: вы не находитесь в творческом мире");
            return;
        }

        // Проверяем, не существует ли уже функция с таким именем
        for (CodeScript script : creativeWorld.getScripts()) {
            if (script.getName().equals(functionName) && script.getType() == CodeScript.ScriptType.FUNCTION) {
                player.sendMessage("§cОшибка: функция '" + functionName + "' уже существует");
                return;
            }
        }

        // Получаем следующий блок как корневой блок функции
        CodeBlock functionRoot = block.getNextBlock();
        if (functionRoot == null) {
            player.sendMessage("§cОшибка: нет блока для сохранения как функции");
            return;
        }

        // Создаем новую функцию
        CodeScript function = new CodeScript(
            functionName,
            true,
            functionRoot,
            CodeScript.ScriptType.FUNCTION
        );

        // Добавляем функцию в мир
        creativeWorld.getScripts().add(function);

        // Сохраняем мир
        plugin.getWorldManager().saveWorld(creativeWorld);

        player.sendMessage("§a💾 Функция '" + functionName + "' сохранена");
    }
} 