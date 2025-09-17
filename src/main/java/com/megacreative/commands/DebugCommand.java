package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.debug.VisualDebugger;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Команда для управления системой отладки скриптов
 * Поддерживает различные режимы отладки, точки останова, наблюдение за переменными
 * Трассировку выполнения и визуализацию процесса работы скриптов
 *
 * Command for managing the script debugging system
 * Supports various debugging modes, breakpoints, variable watching
 * Execution tracing and visualization of script operation processes
 *
 * Befehl zur Verwaltung des Skript-Debugging-Systems
 * Unterstützt verschiedene Debugging-Modi, Haltepunkte, Variablenbeobachtung
 * Ausführungsverfolgung und Visualisierung von Skriptbetriebsprozessen
 */
public class DebugCommand implements CommandExecutor, TabCompleter {
    private final MegaCreative plugin;

    /**
     * Инициализирует команду отладки с необходимыми зависимостями
     * @param plugin основной экземпляр плагина
     *
     * Initializes the debug command with required dependencies
     * @param plugin main plugin instance
     *
     * Initialisiert den Debug-Befehl mit den erforderlichen Abhängigkeiten
     * @param plugin Haupt-Plugin-Instanz
     */
    public DebugCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Обрабатывает выполнение команды отладки
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles debug command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Debug-Befehls
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Toggle debug session
            VisualDebugger debugger = plugin.getScriptDebugger();
            if (debugger.isDebugging(player)) {
                debugger.stopDebugSession(player);
                player.sendMessage("§c✖ Отладка остановлена");
            } else {
                debugger.startDebugSession(player, "debug-session-" + System.currentTimeMillis());
                player.sendMessage("§a✓ Отладка запущена");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();
        VisualDebugger debugger = plugin.getScriptDebugger();

        switch (subCommand) {
            case "on":
            case "enable":
                if (!debugger.isDebugging(player)) {
                    debugger.startDebugSession(player, "debug-session-" + System.currentTimeMillis());
                    player.sendMessage("§a✓ Отладка включена");
                } else {
                    player.sendMessage("§eОтладка уже включена!");
                }
                break;
            case "off":
            case "disable":
                if (debugger.isDebugging(player)) {
                    debugger.stopDebugSession(player);
                    player.sendMessage("§c✖ Отладка выключена");
                } else {
                    player.sendMessage("§eОтладка уже выключена!");
                }
                break;
            case "status":
                boolean enabled = debugger.isDebugging(player);
                player.sendMessage("§7Статус отладки: " + (enabled ? "§aВключена" : "§cВыключена"));
                break;
            case "breakpoint":
                handleBreakpointCommand(player, args);
                break;
            case "watch":
                handleWatchCommand(player, args);
                break;
            case "trace":
                handleTraceCommand(player, args);
                break;
            case "profile":
                handleProfileCommand(player, args);
                break;
            case "performance":
                handlePerformanceCommand(player, args);
                break;
            case "visualize":
                handleVisualizationCommand(player, args);
                break;
            case "stats":
                debugger.showDebugStats(player);
                break;
            case "help":
                showHelp(player);
                break;
            default:
                player.sendMessage("§cНеизвестная подкоманда: " + subCommand);
                showHelp(player);
                break;
        }

        return true;
    }

    /**
     * Обрабатывает подкоманды точек останова
     * @param player игрок, выполняющий команду
     * @param args аргументы команды
     *
     * Handles breakpoint subcommands
     * @param player player executing the command
     * @param args command arguments
     *
     * Verarbeitet Haltepunkt-Unterbefehle
     * @param player Spieler, der den Befehl ausführt
     * @param args Befehlsargumente
     */
    private void handleBreakpointCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /debug breakpoint <set|remove|list> [x y z] [condition]");
            return;
        }

        String action = args[1].toLowerCase();
        VisualDebugger debugger = plugin.getScriptDebugger();
        
        switch (action) {
            case "set":
                Location location = player.getLocation();
                String condition = null;
                
                if (args.length >= 3) {
                    // Try to parse condition
                    StringBuilder conditionBuilder = new StringBuilder();
                    for (int i = 2; i < args.length; i++) {
                        if (conditionBuilder.length() > 0) {
                            conditionBuilder.append(" ");
                        }
                        conditionBuilder.append(args[i]);
                    }
                    condition = conditionBuilder.toString();
                }
                
                debugger.addBreakpoint(player, location, condition);
                break;
                
            case "remove":
                debugger.removeBreakpoint(player, player.getLocation());
                break;
                
            case "list":
                debugger.listBreakpoints(player);
                break;
                
            default:
                player.sendMessage("§cНеизвестное действие для точек останова: " + action);
                player.sendMessage("§7Доступные действия: set, remove, list");
                break;
        }
    }

    /**
     * Обрабатывает подкоманды наблюдения за переменными
     * @param player игрок, выполняющий команду
     * @param args аргументы команды
     *
     * Handles variable watching subcommands
     * @param player player executing the command
     * @param args command arguments
     *
     * Verarbeitet Variablenbeobachtungs-Unterbefehle
     * @param player Spieler, der den Befehl ausführt
     * @param args Befehlsargumente
     */
    private void handleWatchCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /debug watch <add|remove|list> [variable] [expression]");
            return;
        }

        String action = args[1].toLowerCase();
        
        switch (action) {
            case "add":
                if (args.length >= 3) {
                    String variableName = args[2];
                    String expression = null;
                    
                    if (args.length > 3) {
                        // Join remaining arguments as expression
                        StringBuilder expressionBuilder = new StringBuilder();
                        for (int i = 3; i < args.length; i++) {
                            if (expressionBuilder.length() > 0) {
                                expressionBuilder.append(" ");
                            }
                            expressionBuilder.append(args[i]);
                        }
                        expression = expressionBuilder.toString();
                    }
                    
                    plugin.getScriptDebugger().watchVariable(player, variableName, expression);
                } else {
                    player.sendMessage("§cИспользование: /debug watch add <variable> [expression]");
                }
                break;
                
            case "remove":
                if (args.length >= 3) {
                    String variableName = args[2];
                    plugin.getScriptDebugger().unwatchVariable(player, variableName);
                } else {
                    player.sendMessage("§cИспользование: /debug watch remove <variable>");
                }
                break;
                
            case "list":
                plugin.getScriptDebugger().showWatchedVariables(player);
                break;
                
            default:
                player.sendMessage("§cНеизвестное действие для наблюдения: " + action);
                player.sendMessage("§7Доступные действия: add, remove, list");
                break;
        }
    }

    /**
     * Обрабатывает подкоманды трассировки
     * @param player игрок, выполняющий команду
     * @param args аргументы команды
     *
     * Handles trace subcommands
     * @param player player executing the command
     * @param args command arguments
     *
     * Verarbeitet Trace-Unterbefehle
     * @param player Spieler, der den Befehl ausführt
     * @param args Befehlsargumente
     */
    private void handleTraceCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /debug trace <start|stop|show> [maxSteps]");
            return;
        }

        String action = args[1].toLowerCase();
        
        switch (action) {
            case "start":
                int maxSteps = 100; // Default value
                if (args.length >= 3) {
                    try {
                        maxSteps = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cНеверный формат maxSteps. Используйте целое число.");
                        return;
                    }
                }
                plugin.getScriptDebugger().startTracing(player, maxSteps);
                break;
                
            case "stop":
                plugin.getScriptDebugger().stopTracing(player);
                break;
                
            case "show":
                plugin.getScriptDebugger().showTrace(player);
                break;
                
            default:
                player.sendMessage("§cНеизвестное действие для трассировки: " + action);
                player.sendMessage("§7Доступные действия: start, stop, show");
                break;
        }
    }

    /**
     * Обрабатывает подкоманды профилирования
     * @param player игрок, выполняющий команду
     * @param args аргументы команды
     *
     * Handles profile subcommands
     * @param player player executing the command
     * @param args command arguments
     */
    private void handleProfileCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /debug profile <start|stop|show>");
            return;
        }

        String action = args[1].toLowerCase();
        VisualDebugger debugger = plugin.getScriptDebugger();
        
        switch (action) {
            case "start":
                debugger.startProfiling(player);
                break;
                
            case "stop":
                debugger.stopProfiling(player);
                break;
                
            case "show":
                debugger.showProfile(player);
                break;
                
            default:
                player.sendMessage("§cНеизвестное действие для профилирования: " + action);
                player.sendMessage("§7Доступные действия: start, stop, show");
                break;
        }
    }

    /**
     * Обрабатывает подкоманды производительности
     * @param player игрок, выполняющий команду
     * @param args аргументы команды
     *
     * Handles performance subcommands
     * @param player player executing the command
     * @param args command arguments
     *
     * Verarbeitet Leistungs-Unterbefehle
     * @param player Spieler, der den Befehl ausführt
     * @param args Befehlsargumente
     */
    private void handlePerformanceCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /debug performance <start|stop|report>");
            return;
        }

        String action = args[1].toLowerCase();
        
        switch (action) {
            case "start":
                // This would require a script reference, which we don't have in this context
                player.sendMessage("§eЗапуск анализа производительности должен выполняться из скрипта");
                break;
                
            case "stop":
                plugin.getScriptDebugger().stopTracing(player);
                break;
                
            case "report":
                plugin.getScriptDebugger().showPerformanceReport(player);
                break;
                
            default:
                player.sendMessage("§cНеизвестное действие для производительности: " + action);
                player.sendMessage("§7Доступные действия: start, stop, report");
                break;
        }
    }

    /**
     * Обрабатывает подкоманды визуализации
     * @param player игрок, выполняющий команду
     * @param args аргументы команды
     *
     * Handles visualization subcommands
     * @param player player executing the command
     * @param args command arguments
     *
     * Verarbeitet Visualisierungs-Unterbefehle
     * @param player Spieler, der den Befehl ausführt
     * @param args Befehlsargumente
     */
    private void handleVisualizationCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /debug visualize <block|flow|performance|group> [on|off]");
            return;
        }

        String mode = args[1].toLowerCase();
        String action = args.length >= 3 ? args[2].toLowerCase() : "toggle";
        
        com.megacreative.coding.debug.AdvancedVisualDebugger.VisualizationMode vizMode = null;
        
        switch (mode) {
            case "standard":
                vizMode = com.megacreative.coding.debug.AdvancedVisualDebugger.VisualizationMode.STANDARD;
                break;
            case "step":
                vizMode = com.megacreative.coding.debug.AdvancedVisualDebugger.VisualizationMode.STEP_BY_STEP;
                break;
            case "performance":
                vizMode = com.megacreative.coding.debug.AdvancedVisualDebugger.VisualizationMode.PERFORMANCE;
                break;
            case "memory":
                vizMode = com.megacreative.coding.debug.AdvancedVisualDebugger.VisualizationMode.MEMORY;
                break;
            case "variables":
                vizMode = com.megacreative.coding.debug.AdvancedVisualDebugger.VisualizationMode.VARIABLES;
                break;
            case "block":
                vizMode = com.megacreative.coding.debug.AdvancedVisualDebugger.VisualizationMode.STANDARD;
                break;
            case "flow":
                vizMode = com.megacreative.coding.debug.AdvancedVisualDebugger.VisualizationMode.STEP_BY_STEP;
                break;
            case "perfmap":
                vizMode = com.megacreative.coding.debug.AdvancedVisualDebugger.VisualizationMode.PERFORMANCE;
                break;
            case "group":
                vizMode = com.megacreative.coding.debug.AdvancedVisualDebugger.VisualizationMode.STANDARD;
                break;
            default:
                player.sendMessage("§cНеизвестный режим визуализации: " + mode);
                player.sendMessage("§aДоступные режимы: standard, step, performance, memory, variables, block, flow, perfmap, group");
                player.sendMessage("§7Доступные режимы: block, flow, performance, group");
                return;
        }
        
        boolean enable = true;
        if ("off".equals(action) || "disable".equals(action)) {
            enable = false;
        }
        
        if (enable) {
            plugin.getScriptDebugger().startVisualization(player, vizMode);
            player.sendMessage("§aВизуализация " + mode + " включена");
        } else {
            plugin.getScriptDebugger().stopVisualization(player);
            player.sendMessage("§cВизуализация отключена");
        }
    }

    /**
     * Отображает справочную информацию по команде отладки
     * @param player игрок, которому отправляется справка
     *
     * Displays help information for the debug command
     * @param player player to send help to
     *
     * Zeigt Hilfsinformationen für den Debug-Befehl an
     * @param player Spieler, dem die Hilfe gesendet wird
     */
    private void showHelp(Player player) {
        player.sendMessage("§e=== Отладка скриптов ===");
        player.sendMessage("§7/debug §8- переключить отладку");
        player.sendMessage("§7/debug on §8- включить отладку");
        player.sendMessage("§7/debug off §8- выключить отладку");
        player.sendMessage("§7/debug stats §8- показать статистику");
        player.sendMessage("§7/debug status §8- показать статус");
        player.sendMessage("§7/debug breakpoint <set|remove|list> [x y z] [condition] §8- управление точками останова");
        player.sendMessage("§7/debug watch <add|remove|list> [variable] [expression] §8- наблюдение за переменными");
        player.sendMessage("§7/debug trace <start|stop|show> [maxSteps] §8- трассировка выполнения");
        player.sendMessage("§7/debug profile <start|stop|show> §8- профилирование производительности");
        player.sendMessage("§7/debug performance <report> §8- анализ производительности");
        player.sendMessage("§7/debug visualize <block|flow|performance|group> [on|off] §8- визуализация выполнения");
        player.sendMessage("§7/debug help §8- показать эту справку");
        player.sendMessage("§7");
        player.sendMessage("§7При включенной отладке вы увидите:");
        player.sendMessage("§7- Эффекты частиц вокруг выполняющихся блоков");
        player.sendMessage("§7- Сообщения о выполнении каждого блока");
        player.sendMessage("§7- Параметры блоков при выполнении");
        player.sendMessage("§7- Результаты условий (истина/ложь)");
        player.sendMessage("§7- Статистику выполнения скриптов");
    }

    /**
     * Обрабатывает автозавершение команды отладки
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param alias псевдоним команды
     * @param args аргументы команды
     * @return список возможных завершений
     *
     * Handles debug command tab completion
     * @param sender command sender
     * @param command executed command
     * @param alias command alias
     * @param args command arguments
     * @return list of possible completions
     *
     * Verarbeitet die Debug-Befehls-Tab-Vervollständigung
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param alias Befehlsalias
     * @param args Befehlsargumente
     * @return Liste möglicher Vervollständigungen
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("on");
            completions.add("off");
            completions.add("enable");
            completions.add("disable");
            completions.add("stats");
            completions.add("status");
            completions.add("breakpoint");
            completions.add("watch");
            completions.add("trace");
            completions.add("profile");
            completions.add("performance");
            completions.add("visualize");
            completions.add("help");
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "breakpoint":
                    completions.add("set");
                    completions.add("remove");
                    completions.add("list");
                    break;
                case "watch":
                    completions.add("add");
                    completions.add("remove");
                    completions.add("list");
                    break;
                case "trace":
                    completions.add("start");
                    completions.add("stop");
                    completions.add("show");
                    break;
                case "profile":
                    completions.add("start");
                    completions.add("stop");
                    completions.add("show");
                    break;
                case "performance":
                    completions.add("start");
                    completions.add("stop");
                    completions.add("report");
                    break;
                case "visualize":
                    completions.add("block");
                    completions.add("flow");
                    completions.add("performance");
                    completions.add("group");
                    break;
            }
        } else if (args.length == 3) {
            if ("breakpoint".equals(args[0].toLowerCase()) && "set".equals(args[1].toLowerCase())) {
                // Provide player's current coordinates as suggestions
                completions.add(String.valueOf(((Player) sender).getLocation().getBlockX()));
            } else if ("visualize".equals(args[0].toLowerCase())) {
                completions.add("on");
                completions.add("off");
            }
        }
        
        return completions;
    }
}