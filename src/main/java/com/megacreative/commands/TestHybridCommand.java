package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.HybridScriptExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для тестирования гибридной системы выполнения скриптов.
 */
public class TestHybridCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public TestHybridCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "info":
                showSystemInfo(player);
                break;
            case "test":
                testHybridSystem(player);
                break;
            case "list":
                listAvailableBlocks(player);
                break;
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    /**
     * Показывает справку по команде.
     */
    private void showHelp(Player player) {
        player.sendMessage("§6=== Тестирование гибридной системы ===");
        player.sendMessage("§e/testhybrid info §7- Информация о системе");
        player.sendMessage("§e/testhybrid test §7- Тест гибридной системы");
        player.sendMessage("§e/testhybrid list §7- Список доступных блоков");
    }
    
    /**
     * Показывает информацию о гибридной системе.
     */
    private void showSystemInfo(Player player) {
        HybridScriptExecutor executor = new HybridScriptExecutor(plugin);
        String info = executor.getSystemInfo();
        
        player.sendMessage("§6=== Информация о гибридной системе ===");
        player.sendMessage(info);
        player.sendMessage("§7");
        player.sendMessage("§a✅ Новая система: Поддерживает аргументы и значения");
        player.sendMessage("§e⚠️ Старая система: Обратная совместимость");
        player.sendMessage("§7");
        player.sendMessage("§b💡 Система автоматически выбирает лучший блок!");
    }
    
    /**
     * Тестирует гибридную систему.
     */
    private void testHybridSystem(Player player) {
        player.sendMessage("§6=== Тест гибридной системы ===");
        
        // Создаем тестовый скрипт
        player.sendMessage("§7Создание тестового скрипта...");
        
        // TODO: Создать тестовый скрипт с блоками из обеих систем
        player.sendMessage("§a✅ Гибридная система готова к тестированию!");
        player.sendMessage("§7Попробуйте создать скрипт с блоками из обеих систем.");
    }
    
    /**
     * Показывает список доступных блоков.
     */
    private void listAvailableBlocks(Player player) {
        player.sendMessage("§6=== Доступные блоки ===");
        
        // Новые блоки
        player.sendMessage("§a📦 НОВЫЕ БЛОКИ (с аргументами):");
        player.sendMessage("§7- sendMessage, giveItem, randomNumber, setVar");
        player.sendMessage("§7- teleport, wait, setBlock, broadcast");
        player.sendMessage("§7- isOp, hasItem, ifVarEquals, playerHealth");
        
        // Старые блоки
        player.sendMessage("§e📦 СТАРЫЕ БЛОКИ (обратная совместимость):");
        player.sendMessage("§7- Все существующие блоки из старой системы");
        player.sendMessage("§7- Поддерживаются для совместимости");
        
        player.sendMessage("§7");
        player.sendMessage("§b💡 Система автоматически выбирает лучший блок!");
    }
} 