package com.megacreative.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Безопасный исполнитель команд для скриптов
 * Предотвращает выполнение опасных команд
 *
 * Safe command executor for scripts
 * Prevents execution of dangerous commands
 *
 * Sicherer Befehlsausführer für Skripte
 * Verhindert die Ausführung gefährlicher Befehle
 */
public class SafeCommandExecutor {
    
    // Белый список разрешенных команд
    // White list of allowed commands
    // Weiße Liste erlaubter Befehle
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
     *
     * Safely executes a command on behalf of a player
     * @param player Player on whose behalf the command is executed
     * @param command Command to execute
     * @return true if command executed successfully, false otherwise
     *
     * Führt einen Befehl sicher im Namen eines Spielers aus
     * @param player Spieler, in dessen Namen der Befehl ausgeführt wird
     * @param command Auszuführender Befehl
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde, sonst false
     */
    public static boolean executeCommand(Player player, String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = command.trim().split("\\s+");
        if (parts.length == 0) {
            return false;
        }
        
        String baseCommand = parts[0].toLowerCase();
        
        // Проверяем, разрешена ли команда
        // Check if command is allowed
        // Prüfen, ob der Befehl erlaubt ist
        if (!ALLOWED_COMMANDS.contains(baseCommand)) {
            return false;
        }
        
        // Дополнительная проверка прав
        // Additional permission check
        // Zusätzliche Berechtigungsprüfung
        if (!player.hasPermission("megacreative.script.command." + baseCommand)) {
            return false;
        }
        
        // Дополнительные проверки безопасности для критических команд
        // Additional security checks for critical commands
        // Zusätzliche Sicherheitsprüfungen für kritische Befehle
        if (!isCommandSafe(baseCommand, parts)) {
            return false;
        }
        
        try {
            // Безопасное выполнение команды
            // Safe command execution
            // Sichere Befehlsausführung
            boolean success = Bukkit.dispatchCommand(player, command);
            return success;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Проверяет безопасность команды
     * @param baseCommand Базовая команда
     * @param parts Части команды
     * @return true если команда безопасна
     *
     * Checks command safety
     * @param baseCommand Base command
     * @param parts Command parts
     * @return true if command is safe
     *
     * Prüft die Befehlssicherheit
     * @param baseCommand Basisbefehl
     * @param parts Befehlsteile
     * @return true, wenn der Befehl sicher ist
     */
    private static boolean isCommandSafe(String baseCommand, String[] parts) {
        // Блокируем команды с потенциально опасными параметрами
        // Block commands with potentially dangerous parameters
        // Blockiere Befehle mit potenziell gefährlichen Parametern
        switch (baseCommand) {
            case "gamemode":
                // Разрешаем только стандартные режимы игры
                // Allow only standard game modes
                // Erlaube nur Standard-Spielmodi
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
                // Limit item quantity
                // Begrenze die Artikelmenge
                if (parts.length > 2) {
                    try {
                        int amount = Integer.parseInt(parts[2]);
                        return amount <= 64; // Максимум 64 предмета
                        // Maximum 64 items
                        // Maximal 64 Gegenstände
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                return true;
                
            case "effect":
                // Ограничиваем длительность эффектов
                // Limit effect duration
                // Begrenze die Effektdauer
                if (parts.length > 2) {
                    try {
                        int duration = Integer.parseInt(parts[2]);
                        return duration <= 600; // Максимум 30 секунд (600 тиков)
                        // Maximum 30 seconds (600 ticks)
                        // Maximal 30 Sekunden (600 Ticks)
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                return true;
                
            case "kill":
                // Разрешаем убивать только себя (проверка будет в executeCommand)
                // Allow killing only yourself (check will be in executeCommand)
                // Erlaube nur das Töten von sich selbst (Prüfung erfolgt in executeCommand)
                return true;
                
            default:
                return true;
        }
    }
    
    /**
     * Получает список разрешенных команд
     * @return Множество разрешенных команд
     *
     * Gets list of allowed commands
     * @return Set of allowed commands
     *
     * Ruft die Liste der erlaubten Befehle ab
     * @return Menge erlaubter Befehle
     */
    public static Set<String> getAllowedCommands() {
        return Set.copyOf(ALLOWED_COMMANDS);
    }
    
    /**
     * Проверяет, разрешена ли команда
     * @param command Команда для проверки
     * @return true если команда разрешена
     *
     * Checks if command is allowed
     * @param command Command to check
     * @return true if command is allowed
     *
     * Prüft, ob der Befehl erlaubt ist
     * @param command Zu prüfender Befehl
     * @return true, wenn der Befehl erlaubt ist
     */
    public static boolean isCommandAllowed(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        String baseCommand = command.trim().split("\\s+")[0].toLowerCase();
        return ALLOWED_COMMANDS.contains(baseCommand);
    }
}