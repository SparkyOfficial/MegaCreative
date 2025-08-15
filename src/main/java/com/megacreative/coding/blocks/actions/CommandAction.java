package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.entity.Player;

/**
 * Действие для выполнения команды от имени игрока
 */
public class CommandAction implements BlockAction {
    
    private final ParameterArgument commandArgument;
    
    public CommandAction() {
        this.commandArgument = new ParameterArgument("command");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            context.getPlugin().getLogger().warning("CommandAction: Игрок не найден в контексте");
            return;
        }
        
        // Получаем команду из аргумента
        if (context.getCurrentBlock() == null) {
            context.getPlugin().getLogger().warning("CommandAction: Текущий блок не найден");
            return;
        }
        
        var commandValueOpt = commandArgument.parse(context.getCurrentBlock());
        if (commandValueOpt.isEmpty()) {
            context.getPlugin().getLogger().warning("CommandAction: Не удалось получить команду");
            return;
        }
        
        String command = commandValueOpt.get().get(context);
        if (command == null || command.trim().isEmpty()) {
            context.getPlugin().getLogger().warning("CommandAction: Команда пуста");
            return;
        }
        
        // Убираем слеш в начале, если есть
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        // Выполняем команду от имени игрока
        try {
            boolean success = player.performCommand(command);
            if (success) {
                context.getPlugin().getLogger().info("CommandAction: Команда выполнена успешно: " + command);
            } else {
                context.getPlugin().getLogger().warning("CommandAction: Команда не выполнена: " + command);
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("CommandAction: Ошибка выполнения команды '" + command + "': " + e.getMessage());
        }
    }
    
    /**
     * Возвращает имя действия
     */
    public String getActionName() {
        return "command";
    }
    
    /**
     * Возвращает описание действия
     */
    public String getDescription() {
        return "Выполняет команду от имени игрока";
    }
} 