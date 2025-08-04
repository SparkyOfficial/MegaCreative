package com.megacreative.coding.commands;

import com.megacreative.coding.gui.BlockInventoryManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Обработчик команд для работы с блоками кода.
 */
public class BlockCommand implements CommandExecutor {
    private final BlockInventoryManager inventoryManager;
    
    public BlockCommand(BlockInventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, 
                            @NotNull Command command, 
                            @NotNull String label, 
                            @NotNull String[] args) {
        // Проверяем, что отправитель - игрок
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭту команду можно использовать только в игре!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Обрабатываем подкоманды
        if (args.length == 0) {
            // Если команда без аргументов, открываем меню блоков
            inventoryManager.openBlockInventory(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "give":
                handleGiveCommand(player, args);
                break;
                
            case "list":
                handleListCommand(player);
                break;
                
            case "help":
            default:
                sendHelpMessage(player);
                break;
        }
        
        return true;
    }
    
    /**
     * Обрабатывает подкоманду give.
     */
    private void handleGiveCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /block give <тип> [количество]");
            return;
        }
        
        String blockType = args[1].toUpperCase();
        int amount = 1;
        
        // Парсим количество, если указано
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                amount = Math.min(Math.max(1, amount), 64); // Ограничиваем от 1 до 64
            } catch (NumberFormatException e) {
                player.sendMessage("§cНеверное количество: " + args[2]);
                return;
            }
        }
        
        // Создаем предмет блока
        // TODO: Реализовать создание предмета блока
        player.sendMessage(String.format("§aВыдано %d блоков типа %s", amount, blockType));
    }
    
    /**
     * Обрабатывает подкоманду list.
     */
    private void handleListCommand(Player player) {
        player.sendMessage("§6=== Доступные типы блоков ===");
        player.sendMessage("§e- EVENT: Блок-событие");
        player.sendMessage("§e- ACTION: Блок-действие");
        player.sendMessage("§e- CONDITION: Условный блок");
        player.sendMessage("§e- LOOP: Циклический блок");
        player.sendMessage("§e- FUNCTION: Функциональный блок");
        player.sendMessage("§e- VARIABLE: Блок переменной");
        player.sendMessage("§e- VALUE: Блок значения");
    }
    
    /**
     * Отправляет справку по командам.
     */
    private void sendHelpMessage(Player player) {
        player.sendMessage("§6=== Помощь по командам блоков ===");
        player.sendMessage("§e/block §7- Открыть меню блоков");
        player.sendMessage("§e/block give <тип> [количество] §7- Получить блок");
        player.sendMessage("§e/block list §7- Список доступных блоков");
        player.sendMessage("§e/block help §7- Показать эту справку");
    }
}
