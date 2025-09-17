package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.functions.AdvancedFunctionManager;
import com.megacreative.coding.functions.FunctionDefinition;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Команда для управления функциями в стиле Reference System
 * Предоставляет интерфейс командной строки для управления функциями:
 * - Список доступных функций
 * - Получение информации о функциях
 * - Тестирование выполнения функций
 * - Управление правами доступа к функциям
 *
 * Command for managing Reference System-style functions
 * Provides command-line interface for function management:
 * - List available functions
 * - Get function information
 * - Test function execution
 * - Manage function permissions
 *
 * Befehl zur Verwaltung von Reference System-Stil-Funktionen
 * Bietet eine Befehlszeilenschnittstelle für die Funktionsverwaltung:
 * - Liste verfügbarer Funktionen
 * - Informationen zu Funktionen abrufen
 * - Testen der Funktionsausführung
 * - Verwaltung von Funktionsberechtigungen
 */
public class FunctionCommand implements CommandExecutor, TabCompleter {
    
    private final MegaCreative plugin;
    private final AdvancedFunctionManager functionManager;
    
    /**
     * Инициализирует команду управления функциями
     * @param plugin основной экземпляр плагина
     *
     * Initializes the function management command
     * @param plugin main plugin instance
     *
     * Initialisiert den Funktionsverwaltungsbefehl
     * @param plugin Haupt-Plugin-Instanz
     */
    public FunctionCommand(MegaCreative plugin) {
        this.plugin = plugin;
        // Add null check for service registry
        if (plugin.getServiceRegistry() != null) {
            this.functionManager = plugin.getServiceRegistry().getService(AdvancedFunctionManager.class);
        } else {
            this.functionManager = null;
        }
    }

    /**
     * Обрабатывает выполнение команды управления функциями
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles function management command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Funktionsverwaltungsbefehls
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if functionManager is available
        if (functionManager == null) {
            player.sendMessage(ChatColor.RED + "Function manager not available.");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "list":
                listFunctions(player, args);
                break;
            case "info":
                showFunctionInfo(player, args);
                break;
            case "call":
                callFunction(player, args);
                break;
            case "remove":
                removeFunction(player, args);
                break;
            case "stats":
                showStatistics(player);
                break;
            case "help":
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    /**
     * Отображает справочную информацию по команде
     * @param player игрок, которому отправляется справка
     *
     * Shows command help
     * @param player player to send help to
     *
     * Zeigt Befehlshilfe an
     * @param player Spieler, dem die Hilfe gesendet wird
     */
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "🎆 Reference System Function Management");
        player.sendMessage(ChatColor.YELLOW + "/function list [scope] - List available functions");
        player.sendMessage(ChatColor.YELLOW + "/function info <name> - Show function details");
        player.sendMessage(ChatColor.YELLOW + "/function call <name> [args...] - Test function execution");
        player.sendMessage(ChatColor.YELLOW + "/function remove <name> - Remove your function");
        player.sendMessage(ChatColor.YELLOW + "/function stats - Show function statistics");
        player.sendMessage(ChatColor.YELLOW + "/function help - Show this help");
    }
    
    /**
     * Отображает список доступных функций
     * @param player игрок, которому отправляется список
     * @param args аргументы команды
     *
     * Lists available functions
     * @param player player to send list to
     * @param args command arguments
     *
     * Listet verfügbare Funktionen auf
     * @param player Spieler, dem die Liste gesendet wird
     * @param args Befehlsargumente
     */
    private void listFunctions(Player player, String[] args) {
        if (functionManager == null) {
            player.sendMessage(ChatColor.RED + "Function manager not available.");
            return;
        }
        
        List<FunctionDefinition> functions = functionManager.getAvailableFunctions(player);
        
        if (functions.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "🎆 No functions available.");
            return;
        }
        
        // Filter by scope if specified
        String scopeFilter = args.length > 1 ? args[1].toUpperCase() : null;
        if (scopeFilter != null) {
            try {
                FunctionDefinition.FunctionScope scope = FunctionDefinition.FunctionScope.valueOf(scopeFilter);
                functions = functions.stream()
                    .filter(f -> f.getScope() == scope)
                    .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Invalid scope: " + scopeFilter);
                return;
            }
        }
        
        player.sendMessage(ChatColor.GOLD + "🎆 Available Functions (" + functions.size() + "):");
        
        for (FunctionDefinition function : functions) {
            ChatColor scopeColor = getScopeColor(function.getScope());
            String status = function.isEnabled() ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
            
            player.sendMessage(ChatColor.GRAY + "- " + status + " " + 
                scopeColor + function.getName() + 
                ChatColor.GRAY + " (" + function.getScope() + ") - " + 
                ChatColor.WHITE + function.getDescription());
        }
    }
    
    /**
     * Отображает подробную информацию о функции
     * @param player игрок, которому отправляется информация
     * @param args аргументы команды
     *
     * Shows detailed function information
     * @param player player to send information to
     * @param args command arguments
     *
     * Zeigt detaillierte Funktionsinformationen an
     * @param player Spieler, dem die Informationen gesendet werden
     * @param args Befehlsargumente
     */
    private void showFunctionInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /function info <name>");
            return;
        }
        
        if (functionManager == null) {
            player.sendMessage(ChatColor.RED + "Function manager not available.");
            return;
        }
        
        String functionName = args[1];
        FunctionDefinition function = functionManager.findFunction(functionName, player);
        
        if (function == null) {
            player.sendMessage(ChatColor.RED + "Function not found: " + functionName);
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "🎆 Function Information");
        player.sendMessage(ChatColor.YELLOW + "Name: " + ChatColor.WHITE + function.getName());
        player.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + function.getDescription());
        player.sendMessage(ChatColor.YELLOW + "Owner: " + ChatColor.WHITE + function.getOwner().getName());
        player.sendMessage(ChatColor.YELLOW + "Scope: " + getScopeColor(function.getScope()) + function.getScope());
        player.sendMessage(ChatColor.YELLOW + "Enabled: " + (function.isEnabled() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
        player.sendMessage(ChatColor.YELLOW + "Parameters: " + ChatColor.WHITE + function.getParameters().size());
        
        if (!function.getParameters().isEmpty()) {
            player.sendMessage(ChatColor.GOLD + "Parameters:");
            for (FunctionDefinition.FunctionParameter param : function.getParameters()) {
                String required = param.isRequired() ? ChatColor.RED + "*" : ChatColor.GRAY + "";
                player.sendMessage(ChatColor.GRAY + "  - " + required + ChatColor.WHITE + param.getName() + 
                    ChatColor.GRAY + " (" + param.getType() + ") - " + param.getDescription());
            }
        }
        
        // Show execution statistics
        FunctionDefinition.FunctionStatistics stats = function.getStatistics();
        player.sendMessage(ChatColor.YELLOW + "Executions: " + ChatColor.WHITE + stats.getCallCount());
        player.sendMessage(ChatColor.YELLOW + "Total Time: " + ChatColor.WHITE + stats.getTotalExecutionTime() + "ms");
        player.sendMessage(ChatColor.YELLOW + "Avg Time: " + ChatColor.WHITE + 
            String.format("%.2fms", stats.getAverageExecutionTime()));
    }
    
    /**
     * Вызывает функцию для тестирования
     * @param player игрок, выполняющий вызов
     * @param args аргументы команды
     *
     * Calls a function for testing
     * @param player player executing the call
     * @param args command arguments
     *
     * Ruft eine Funktion zum Testen auf
     * @param player Spieler, der den Aufruf ausführt
     * @param args Befehlsargumente
     */
    private void callFunction(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /function call <name> [args...]");
            return;
        }
        
        if (functionManager == null) {
            player.sendMessage(ChatColor.RED + "Function manager not available.");
            return;
        }
        
        String functionName = args[1];
        FunctionDefinition function = functionManager.findFunction(functionName, player);
        
        if (function == null) {
            player.sendMessage(ChatColor.RED + "Function not found: " + functionName);
            return;
        }
        
        // Parse arguments
        String[] argStrings = Arrays.copyOfRange(args, 2, args.length);
        DataValue[] arguments = new DataValue[argStrings.length];
        
        for (int i = 0; i < argStrings.length; i++) {
            arguments[i] = parseArgument(argStrings[i]);
        }
        
        player.sendMessage(ChatColor.YELLOW + "🎆 Calling function: " + functionName);
        
        // Execute function
        functionManager.executeFunction(functionName, player, arguments)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    player.sendMessage(ChatColor.RED + "Function execution failed: " + throwable.getMessage());
                } else if (result.isSuccess()) {
                    player.sendMessage(ChatColor.GREEN + "Function executed successfully!");
                    if (result.getReturnValue() != null) {
                        player.sendMessage(ChatColor.YELLOW + "Return value: " + ChatColor.WHITE + result.getReturnValue());
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Function execution failed: " + result.getMessage());
                }
            });
    }
    
    /**
     * Удаляет функцию
     * @param player игрок, выполняющий удаление
     * @param args аргументы команды
     *
     * Removes a function
     * @param player player executing the removal
     * @param args command arguments
     *
     * Entfernt eine Funktion
     * @param player Spieler, der die Entfernung ausführt
     * @param args Befehlsargumente
     */
    private void removeFunction(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /function remove <name>");
            return;
        }
        
        if (functionManager == null) {
            player.sendMessage(ChatColor.RED + "Function manager not available.");
            return;
        }
        
        String functionName = args[1];
        boolean removed = functionManager.removeFunction(functionName, player);
        
        if (removed) {
            player.sendMessage(ChatColor.GREEN + "🎆 Function removed: " + functionName);
        } else {
            player.sendMessage(ChatColor.RED + "Failed to remove function: " + functionName + " (not found or not owned by you)");
        }
    }
    
    /**
     * Отображает статистику функций
     * @param player игрок, которому отправляется статистика
     *
     * Shows function statistics
     * @param player player to send statistics to
     *
     * Zeigt Funktionsstatistiken an
     * @param player Spieler, dem die Statistiken gesendet werden
     */
    private void showStatistics(Player player) {
        if (functionManager == null) {
            player.sendMessage(ChatColor.RED + "Function manager not available.");
            return;
        }
        
        Map<String, Object> stats = functionManager.getFunctionStatistics();
        
        player.sendMessage(ChatColor.GOLD + "🎆 Function System Statistics");
        player.sendMessage(ChatColor.YELLOW + "Total Functions: " + ChatColor.WHITE + stats.get("total_functions"));
        player.sendMessage(ChatColor.YELLOW + "Global Functions: " + ChatColor.WHITE + stats.get("global_functions"));
        player.sendMessage(ChatColor.YELLOW + "Player Functions: " + ChatColor.WHITE + stats.get("player_functions"));
        player.sendMessage(ChatColor.YELLOW + "World Functions: " + ChatColor.WHITE + stats.get("world_functions"));
        player.sendMessage(ChatColor.YELLOW + "Active Executions: " + ChatColor.WHITE + stats.get("active_executions"));
        player.sendMessage(ChatColor.YELLOW + "Function Libraries: " + ChatColor.WHITE + stats.get("libraries"));
    }
    
    /**
     * Вспомогательные методы
     *
     * Helper methods
     *
     * Hilfsmethoden
     */
    
    private ChatColor getScopeColor(FunctionDefinition.FunctionScope scope) {
        switch (scope) {
            case GLOBAL:
                return ChatColor.LIGHT_PURPLE;
            case WORLD:
                return ChatColor.AQUA;
            case SHARED:
                return ChatColor.GREEN;
            case PLAYER:
            default:
                return ChatColor.YELLOW;
        }
    }
    
    private DataValue parseArgument(String arg) {
        // Try to parse as number
        try {
            double number = Double.parseDouble(arg);
            return DataValue.of(number);
        } catch (NumberFormatException ignored) {}
        
        // Try to parse as boolean
        if ("true".equalsIgnoreCase(arg) || "false".equalsIgnoreCase(arg)) {
            return DataValue.of(Boolean.parseBoolean(arg));
        }
        
        // Default to string
        return DataValue.of(arg);
    }

    /**
     * Обрабатывает автозавершение команды
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param alias псевдоним команды
     * @param args аргументы команды
     * @return список возможных завершений
     *
     * Handles command tab completion
     * @param sender command sender
     * @param command executed command
     * @param alias command alias
     * @param args command arguments
     * @return list of possible completions
     *
     * Verarbeitet die Befehls-Tab-Vervollständigung
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param alias Befehlsalias
     * @param args Befehlsargumente
     * @return Liste möglicher Vervollständigungen
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Main command completions
            List<String> commands = Arrays.asList("list", "info", "call", "remove", "stats", "help");
            return commands.stream()
                .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "list":
                    // Scope completions
                    return Arrays.stream(FunctionDefinition.FunctionScope.values())
                        .map(Enum::name)
                        .filter(scope -> scope.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                        
                case "info":
                case "call":
                case "remove":
                    // Function name completions
                    if (functionManager != null) {
                        return functionManager.getAvailableFunctions(player).stream()
                            .map(FunctionDefinition::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                    }
                    break;
            }
        }
        
        return completions;
    }
}