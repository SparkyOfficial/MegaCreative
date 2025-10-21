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
        
        
        if (!isCommandAllowed(player, baseCommand)) {
            return false;
        }
        
        
        if (!isCommandSafe(baseCommand, parts)) {
            return false;
        }
        
        try {
            boolean success = Bukkit.dispatchCommand(player, command);
            return success;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Checks if a command is allowed for a player
     * @param player The player to check
     * @param baseCommand The base command to check
     * @return true if the command is allowed
     */
    private static boolean isCommandAllowed(Player player, String baseCommand) {
        if (!ALLOWED_COMMANDS.contains(baseCommand)) {
            return false;
        }
        
        if (!player.hasPermission("megacreative.script.command." + baseCommand)) {
            return false;
        }
        
        return true;
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
        
        
        
        switch (baseCommand) {
            case "gamemode":
                
                
                
                if (parts.length > 1) {
                    String mode = parts[1].toLowerCase();
                    return mode.equals("survival") || mode.equals("creative") || 
                           mode.equals("adventure") || mode.equals("spectator") ||
                           mode.equals("0") || mode.equals("1") || 
                           mode.equals("2") || mode.equals("3");
                }
                return true;
                
            case "give":
                
                
                
                if (parts.length > 2) {
                    try {
                        int amount = Integer.parseInt(parts[2]);
                        return amount <= 64; 
                        
                        
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
                return true;
                
            case "effect":
                
                
                
                if (parts.length > 2) {
                    try {
                        int duration = Integer.parseInt(parts[2]);
                        return duration <= 600; 
                        
                        
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
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