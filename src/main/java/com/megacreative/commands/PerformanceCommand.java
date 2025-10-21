package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.monitoring.AdvancedScriptOptimizer;
import com.megacreative.coding.monitoring.OptimizationPriority;
import com.megacreative.coding.monitoring.model.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Performance monitoring command for MegaCreative
 * Allows players to view performance reports and optimization suggestions
 */
public class PerformanceCommand implements CommandExecutor, TabCompleter {
    
    private final MegaCreative plugin;
    
    public PerformanceCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, 
                            @NotNull Command command, 
                            @NotNull String label, 
                            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        if (args.length == 0) {
            sendGeneralHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "report" -> showSystemReport(player);
            case "script" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /performance script <scriptName>");
                    return true;
                }
                showScriptReport(player, args[1]);
            }
            case "optimize" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /performance optimize <scriptName>");
                    return true;
                }
                showOptimizationSuggestions(player, args[1]);
            }
            case "bottlenecks" -> showBottlenecks(player);
            case "clear" -> {
                if (!player.hasPermission("megacreative.admin")) {
                    player.sendMessage("§cYou don't have permission to clear performance data!");
                    return true;
                }
                clearPerformanceData(player);
            }
            default -> sendGeneralHelp(player);
        }
        
        return true;
    }
    
    /**
     * Sends general help information to the player
     */
    private void sendGeneralHelp(Player player) {
        player.sendMessage("§8§m                    §r §b§lPerformance §8§m                    ");
        player.sendMessage("§7/performance report §8- §fShow system performance report");
        player.sendMessage("§7/performance script <name> §8- §fShow script performance details");
        player.sendMessage("§7/performance optimize <name> §8- §fShow optimization suggestions");
        player.sendMessage("§7/performance bottlenecks §8- §fShow detected performance bottlenecks");
        player.sendMessage("§7/performance clear §8- §fClear performance data (admin only)");
        player.sendMessage("§8§m                                                        ");
    }
    
    /**
     * Shows system-wide performance report
     */
    private void showSystemReport(Player player) {
        SystemPerformanceReport report = plugin.getServiceRegistry().getScriptPerformanceMonitor().getSystemPerformanceReport();
        
        player.sendMessage("§8§m                    §r §b§lSystem Performance Report §8§m                    ");
        player.sendMessage("§7Total Executions: §f" + report.getTotalExecutions());
        player.sendMessage("§7Total Execution Time: §f" + report.getTotalExecutionTime() + "ms");
        player.sendMessage("§7Average Execution Time: §f" + String.format("%.2f", report.getAverageExecutionTime()) + "ms");
        player.sendMessage("§7Active Players: §f" + report.getActivePlayerCount());
        player.sendMessage("§7Script Profiles: §f" + report.getScriptProfilesCount());
        player.sendMessage("§7Uptime: §f" + formatUptime(report.getUptimeMs()));
        
        
        MemoryUsage memory = report.getMemoryUsage();
        if (memory != null) {
            player.sendMessage("§7Memory Usage: §f" + String.format("%.1f", memory.getUsedMemoryMB()) + "MB / " + 
                             String.format("%.1f", memory.getMaxMemoryMB()) + "MB " +
                             "(" + String.format("%.1f", memory.getMemoryUsagePercentage()) + "%)");
        }
        
        
        GarbageCollectionMonitor.GcStatistics gcStats = report.getGcStatistics();
        if (gcStats != null) {
            player.sendMessage("§7GC Collections: §f" + gcStats.getTotalGcCount() + 
                             " (+" + gcStats.getGcCountDelta() + " recent)");
            player.sendMessage("§7GC Time: §f" + gcStats.getTotalGcTime() + "ms " +
                             "(" + String.format("%.2f", gcStats.getGcTimePercentage(report.getUptimeMs())) + "% of uptime)");
            if (gcStats.getTotalGcCount() > 0) {
                player.sendMessage("§7Avg GC Time: §f" + String.format("%.2f", gcStats.getAverageGcTime()) + "ms");
            }
        }
        
        
        Collection<Bottleneck> bottlenecks = report.getBottlenecks();
        if (!bottlenecks.isEmpty()) {
            player.sendMessage("§cDetected Bottlenecks: §f" + bottlenecks.size());
            for (Bottleneck bottleneck : bottlenecks) {
                player.sendMessage("§c» §f" + bottleneck.getScriptName() + " - " + bottleneck.getDescription());
            }
        } else {
            player.sendMessage("§aNo bottlenecks detected! System performance is optimal.");
        }
        
        player.sendMessage("§8§m                                                        ");
    }
    
    /**
     * Shows performance details for a specific script
     */
    private void showScriptReport(Player player, String scriptName) {
        ScriptPerformanceProfile profile = plugin.getServiceRegistry().getScriptPerformanceMonitor().getScriptProfile(scriptName);
        
        if (profile == null) {
            player.sendMessage("§cScript '" + scriptName + "' not found!");
            return;
        }
        
        player.sendMessage("§8§m                    §r §b§lScript Performance: " + scriptName + " §8§m                    ");
        player.sendMessage("§7Total Executions: §f" + profile.getTotalExecutions());
        player.sendMessage("§7Total Execution Time: §f" + profile.getTotalExecutionTime() + "ms");
        player.sendMessage("§7Average Execution Time: §f" + String.format("%.2f", profile.getAverageExecutionTime()) + "ms");
        player.sendMessage("§7Peak Execution Time: §f" + profile.getPeakExecutionTime() + "ms");
        player.sendMessage("§7Min Execution Time: §f" + profile.getMinExecutionTime() + "ms");
        player.sendMessage("§7Success Rate: §f" + String.format("%.1f", profile.getSuccessRate() * 100) + "%");
        player.sendMessage("§7Error Rate: §f" + String.format("%.1f", profile.getErrorRate() * 100) + "%");
        
        
        Collection<ActionPerformanceData> actionData = profile.getAllActionData();
        if (!actionData.isEmpty()) {
            player.sendMessage("§7Action Performance:");
            for (ActionPerformanceData data : actionData) {
                player.sendMessage("§8» §f" + data.getActionType() + ": " + 
                                 String.format("%.2f", data.getAverageExecutionTime()) + "ms avg " +
                                 "(success: " + String.format("%.1f", data.getSuccessRate()) + "%)");
            }
        }
        
        player.sendMessage("§8§m                                                        ");
    }
    
    /**
     * Shows optimization suggestions for a specific script
     */
    private void showOptimizationSuggestions(Player player, String scriptName) {
        ScriptPerformanceProfile profile = plugin.getServiceRegistry().getScriptPerformanceMonitor().getScriptProfile(scriptName);
        
        if (profile == null) {
            player.sendMessage("§cScript '" + scriptName + "' not found!");
            return;
        }
        
        
        com.megacreative.coding.CodeScript script = plugin.getServiceRegistry().getCodingManager().getScript(scriptName);
        if (script == null) {
            player.sendMessage("§cScript '" + scriptName + "' not found!");
            return;
        }
        
        com.megacreative.coding.monitoring.AdvancedScriptOptimizer optimizer = 
            new com.megacreative.coding.monitoring.AdvancedScriptOptimizer(plugin, plugin.getServiceRegistry().getScriptPerformanceMonitor());
        
        ScriptOptimizationReport report = 
            optimizer.analyzeScript(script);
        
        player.sendMessage("§8§m                    §r §b§lOptimization Suggestions: " + scriptName + " §8§m                    ");
        player.sendMessage("§7" + report.getSummary());
        
        List<OptimizationSuggestion> suggestions = 
            report.getSuggestions();
        
        if (suggestions.isEmpty()) {
            player.sendMessage("§aNo optimization suggestions found! Script is well optimized.");
        } else {
            for (OptimizationSuggestion suggestion : suggestions) {
                String priorityColor = getPriorityColor(suggestion.getPriority());
                player.sendMessage("§e» §f" + suggestion.getDescription() + " - " + priorityColor + suggestion.getPriority().name());
            }
        }
        
        player.sendMessage("§8§m                                                        ");
    }
    
    /**
     * Shows detected performance bottlenecks
     */
    private void showBottlenecks(Player player) {
        Collection<Bottleneck> bottlenecks = plugin.getServiceRegistry().getScriptPerformanceMonitor().getSystemPerformanceReport().getBottlenecks();
        
        player.sendMessage("§8§m                    §r §b§lPerformance Bottlenecks §8§m                    ");
        if (bottlenecks.isEmpty()) {
            player.sendMessage("§aNo performance bottlenecks detected!");
        } else {
            player.sendMessage("§cDetected " + bottlenecks.size() + " bottlenecks:");
            for (Bottleneck bottleneck : bottlenecks) {
                player.sendMessage("§c» §f" + bottleneck.getScriptName() + " - " + bottleneck.getDescription());
            }
        }
        player.sendMessage("§8§m                                                        ");
    }
    
    /**
     * Clears performance data
     */
    private void clearPerformanceData(Player player) {
        plugin.getServiceRegistry().getScriptPerformanceMonitor().clearData();
        player.sendMessage("§aPerformance data cleared successfully!");
    }
    
    /**
     * Formats uptime in a human-readable format
     */
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        seconds %= 60;
        minutes %= 60;
        hours %= 24;
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        
        return sb.toString().trim();
    }
    
    /**
     * Gets color code for optimization priority
     */
    private String getPriorityColor(OptimizationPriority priority) {
        switch (priority) {
            case CRITICAL:
                return "§c"; // Red
            case HIGH:
                return "§e"; // Yellow
            case MEDIUM:
                return "§a"; // Green
            case LOW:
                return "§7"; // Gray
            default:
                return "§f"; // White
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, 
                                     @NotNull Command command, 
                                     @NotNull String alias, 
                                     @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("report", "script", "optimize", "bottlenecks"));
            if (sender.hasPermission("megacreative.admin")) {
                completions.add("clear");
            }
            return completions;
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if ("script".equals(subCommand) || "optimize".equals(subCommand)) {
                // Return script names
                Collection<ScriptMetrics> scripts = plugin.getServiceRegistry().getScriptPerformanceMonitor().getAllScriptProfiles();
                for (ScriptMetrics script : scripts) {
                    completions.add(script.getScriptName());
                }
                return completions;
            }
        }
        
        return new ArrayList<>();
    }
}