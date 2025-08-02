package com.megacreative.commands;

import com.megacreative.MegaCreative;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Команда для очистки поврежденных миров
 */
public class CleanWorldsCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public CleanWorldsCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.isOp()) {
            player.sendMessage("§cУ вас нет прав для использования этой команды!");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "check":
                checkDamagedWorlds(player);
                break;
            case "clean":
                cleanDamagedWorlds(player);
                break;
            case "backup":
                backupWorlds(player);
                break;
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    /**
     * Показывает справку по команде
     */
    private void showHelp(Player player) {
        player.sendMessage("§6=== Очистка поврежденных миров ===");
        player.sendMessage("§e/cleanworlds check §7- Проверить поврежденные миры");
        player.sendMessage("§e/cleanworlds clean §7- Очистить поврежденные миры");
        player.sendMessage("§e/cleanworlds backup §7- Создать резервную копию");
    }
    
    /**
     * Проверяет поврежденные миры
     */
    private void checkDamagedWorlds(Player player) {
        player.sendMessage("§6=== Проверка поврежденных миров ===");
        
        File dataFolder = new File(plugin.getDataFolder(), "worlds");
        if (!dataFolder.exists()) {
            player.sendMessage("§aПапка с мирами не найдена. Все чисто!");
            return;
        }
        
        File[] worldFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (worldFiles == null || worldFiles.length == 0) {
            player.sendMessage("§aФайлы миров не найдены. Все чисто!");
            return;
        }
        
        int damagedCount = 0;
        int totalCount = worldFiles.length;
        
        for (File worldFile : worldFiles) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(worldFile);
            String worldData = config.getString("worldData");
            
            if (worldData == null || worldData.trim().isEmpty()) {
                player.sendMessage("§c❌ " + worldFile.getName() + " - поврежден");
                damagedCount++;
            } else {
                player.sendMessage("§a✅ " + worldFile.getName() + " - в порядке");
            }
        }
        
        player.sendMessage("§7");
        player.sendMessage("§6Результат проверки:");
        player.sendMessage("§7Всего файлов: " + totalCount);
        player.sendMessage("§aИсправных: " + (totalCount - damagedCount));
        player.sendMessage("§cПоврежденных: " + damagedCount);
        
        if (damagedCount > 0) {
            player.sendMessage("§eИспользуйте /cleanworlds clean для очистки");
        }
    }
    
    /**
     * Очищает поврежденные миры
     */
    private void cleanDamagedWorlds(Player player) {
        player.sendMessage("§6=== Очистка поврежденных миров ===");
        
        File dataFolder = new File(plugin.getDataFolder(), "worlds");
        if (!dataFolder.exists()) {
            player.sendMessage("§aПапка с мирами не найдена.");
            return;
        }
        
        File[] worldFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (worldFiles == null || worldFiles.length == 0) {
            player.sendMessage("§aФайлы миров не найдены.");
            return;
        }
        
        int cleanedCount = 0;
        
        for (File worldFile : worldFiles) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(worldFile);
            String worldData = config.getString("worldData");
            
            if (worldData == null || worldData.trim().isEmpty()) {
                try {
                    // Создаем резервную копию
                    File backupFile = new File(worldFile.getParentFile(), worldFile.getName() + ".backup");
                    worldFile.renameTo(backupFile);
                    
                    player.sendMessage("§e🗑️ Удален поврежденный мир: " + worldFile.getName());
                    cleanedCount++;
                } catch (Exception e) {
                    player.sendMessage("§c❌ Ошибка при удалении " + worldFile.getName() + ": " + e.getMessage());
                }
            }
        }
        
        player.sendMessage("§7");
        player.sendMessage("§aОчистка завершена! Удалено файлов: " + cleanedCount);
        
        if (cleanedCount > 0) {
            player.sendMessage("§eПерезапустите сервер для применения изменений");
        }
    }
    
    /**
     * Создает резервную копию миров
     */
    private void backupWorlds(Player player) {
        player.sendMessage("§6=== Создание резервной копии ===");
        
        File dataFolder = new File(plugin.getDataFolder(), "worlds");
        if (!dataFolder.exists()) {
            player.sendMessage("§aПапка с мирами не найдена.");
            return;
        }
        
        File backupFolder = new File(plugin.getDataFolder(), "worlds_backup");
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }
        
        File[] worldFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (worldFiles == null || worldFiles.length == 0) {
            player.sendMessage("§aФайлы миров не найдены.");
            return;
        }
        
        int backupCount = 0;
        
        for (File worldFile : worldFiles) {
            try {
                File backupFile = new File(backupFolder, worldFile.getName());
                java.nio.file.Files.copy(worldFile.toPath(), backupFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                backupCount++;
            } catch (Exception e) {
                player.sendMessage("§c❌ Ошибка при резервном копировании " + worldFile.getName());
            }
        }
        
        player.sendMessage("§aРезервная копия создана! Скопировано файлов: " + backupCount);
        player.sendMessage("§7Папка: " + backupFolder.getAbsolutePath());
    }
} 