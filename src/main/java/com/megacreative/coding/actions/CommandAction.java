package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.utils.SafeCommandExecutor;
import org.bukkit.entity.Player;

public class CommandAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();

        if (player == null || block == null || variableManager == null) return;

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawCommand = block.getParameter("command");

        if (rawCommand == null) return;

        DataValue commandValue = resolver.resolve(context, rawCommand);
        String commandStr = commandValue.asString();

        if (commandStr == null || commandStr.isEmpty()) return;

        try {
            // Заменяем плейсхолдеры
            String finalCommand = commandStr
                .replace("%player%", player.getName())
                .replace("%world%", player.getWorld().getName());
            
            // Безопасное выполнение команды от имени игрока
            boolean success = SafeCommandExecutor.executeCommand(player, finalCommand);
            
            if (success) {
                player.sendMessage("§a✓ Команда выполнена: " + finalCommand);
            }
            // Сообщения об ошибках уже отправляются в SafeCommandExecutor
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка выполнения команды: " + e.getMessage());
        }
    }
}