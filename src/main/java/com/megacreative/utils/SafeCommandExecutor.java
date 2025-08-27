package com.megacreative.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Безопасный исполнитель команд для скриптов
 * Предотвращает выполнение опасных команд
 */
public class SafeCommandExecutor {
    
    // Белый список разрешенных команд
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
        "gamemode", "time", "weather", "difficulty", "say", "me", "tell",
        "tp", "teleport", "give", "effect", "clear", "enchant", "title",
        "subtitle", "actionbar", "playsound", "particle", "spreadplayers",
        "setblock", "fill", "clone", "summon", "kill", "damage"
    );
    
    /**
     * Безопасно выполняет команду от имени игрока
     * @param player Игрок, от имени которого выполняется команда
     * @param command Команда для выполнения
     * @return true если команда выполнена успешно, false в противном случае
     */
    public static boolean executeCommand(Player player, String command) {
        if (command == null || command.trim().isEmpty()) {
<<<<<<< HEAD
            player.sendMessage("§cКоманда не может быть пустой.");
=======
>>>>>>> ba7215a (Я вернулся)
            return false;
        }
        
        String[] parts = command.trim().split("\\s+");
        if (parts.length == 0) {
<<<<<<< HEAD
            player.sendMessage("§cНекорректная команда.");
=======
>>>>>>> ba7215a (Я вернулся)
            return false;
        }
        
        String baseCommand = parts[0].toLowerCase();
        
        // Проверяем, разрешена ли команда
        if (!ALLOWED_COMMANDS.contains(baseCommand)) {
<<<<<<< HEAD
            player.sendMessage("§cКоманда '" + baseCommand + "' не разрешена для выполнения через блоки кода.");
=======
>>>>>>> ba7215a (Я вернулся)
            return false;
        }
        
        // Дополнительная проверка прав
        if (!player.hasPermission("megacreative.script.command." + baseCommand)) {
<<<<<<< HEAD
            player.sendMessage("§cУ вас нет прав на выполнение команды '" + baseCommand + "' через скрипты.");
=======
>>>>>>> ba7215a (Я вернулся)
            return false;
        }
        
        // Дополнительные проверки безопасности для критических команд
        if (!isCommandSafe(baseCommand, parts)) {
<<<<<<< HEAD
            player.sendMessage("§cКоманда '" + baseCommand + "' заблокирована по соображениям безопасности.");
=======
>>>>>>> ba7215a (Я вернулся)
            return false;
        }
        
        try {
            // Безопасное выполнение команды
            boolean success = Bukkit.dispatchCommand(player, command);
<<<<<<< HEAD
            if (!success) {
                player.sendMessage("§cКоманда '" + baseCommand + "' не выполнена.");
            }
            return success;
        } catch (Exception e) {
            player.sendMessage("§cОшибка выполнения команды: " + e.getMessage());
=======
            return success;
        } catch (Exception e) {
>>>>>>> ba7215a (Я вернулся)
            return false;
        }
    }
    
    /**
     * Проверяет безопасность команды
     * @param baseCommand Базовая команда
     * @param parts Части команды
     * @return true если команда безопасна
     */
    private static boolean isCommandSafe(String baseCommand, String[] parts) {
        // Блокируем команды с потенциально опасными параметрами
        switch (baseCommand) {
            case "gamemode":
                // Разрешаем только стандартные режимы игры
                if (parts.length > 1) {
                    String mode = parts[1].toLowerCase();
                    return mode.equals("survival") || mode.equals("creative") || 
                           mode.equals("adventure") || mode.equals("spectator") ||
                           mode.equals("0") || mode.equals("1") || 
                           mode.equals("2") || mode.equals("3");
                }
                return true;
                
            case "give":
                // Ограничиваем количество предметов
                if (parts.length > 2) {
                    try {
                        int amount = Integer.parseInt(parts[2]);
                        return amount <= 64; // Максимум 64 предмета
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                return true;
                
            case "effect":
                // Ограничиваем длительность эффектов
                if (parts.length > 2) {
                    try {
                        int duration = Integer.parseInt(parts[2]);
                        return duration <= 600; // Максимум 30 секунд (600 тиков)
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                return true;
                
            case "kill":
                // Разрешаем убивать только себя (проверка будет в executeCommand)
                return true;
                
            default:
                return true;
        }
    }
    
    /**
     * Получает список разрешенных команд
     * @return Множество разрешенных команд
     */
    public static Set<String> getAllowedCommands() {
        return Set.copyOf(ALLOWED_COMMANDS);
    }
    
    /**
     * Проверяет, разрешена ли команда
     * @param command Команда для проверки
     * @return true если команда разрешена
     */
    public static boolean isCommandAllowed(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        String baseCommand = command.trim().split("\\s+")[0].toLowerCase();
        return ALLOWED_COMMANDS.contains(baseCommand);
    }
}
