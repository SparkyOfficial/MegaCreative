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

public class DebugCommand implements CommandExecutor, TabCompleter {
    private final MegaCreative plugin;

    public DebugCommand(MegaCreative plugin) {
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
            case "performance":
                handlePerformanceCommand(player, args);
                break;
            case "visualize":
                handleVisualizationCommand(player, args);
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
     * Handles breakpoint subcommands
     */
    private void handleBreakpointCommand(Player player, String[] args) {
    }

    /**
     * Handles variable watching subcommands
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
     * Handles trace subcommands
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
     * Handles performance subcommands
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
     * Handles visualization subcommands
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