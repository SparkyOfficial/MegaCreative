package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.debug.AdvancedVisualDebugger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Command to control visualization features
 * Allows players to enable/disable different visualization modes
 */
public class VisualizeCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public VisualizeCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        if (args.length < 1) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                showHelp(player);
                break;
                
            case "standard":
                plugin.getScriptDebugger().startVisualization(player, AdvancedVisualDebugger.VisualizationMode.STANDARD);
                player.sendMessage("§aStandard visualization enabled!");
                player.sendMessage("§7Blocks will be highlighted with green particles during execution.");
                break;
                
            case "step":
                plugin.getScriptDebugger().startVisualization(player, AdvancedVisualDebugger.VisualizationMode.STEP_BY_STEP);
                player.sendMessage("§aStep-by-step visualization enabled!");
                player.sendMessage("§7Blocks will be highlighted with blue particles during execution.");
                break;
                
            case "performance":
                plugin.getScriptDebugger().startVisualization(player, AdvancedVisualDebugger.VisualizationMode.PERFORMANCE);
                player.sendMessage("§aPerformance visualization enabled!");
                player.sendMessage("§7Blocks will be highlighted with color-coded particles based on execution time.");
                break;
                
            case "memory":
                plugin.getScriptDebugger().startVisualization(player, AdvancedVisualDebugger.VisualizationMode.MEMORY);
                player.sendMessage("§aMemory visualization enabled!");
                player.sendMessage("§7Blocks will be highlighted with orange particles during execution.");
                break;
                
            case "variables":
                plugin.getScriptDebugger().startVisualization(player, AdvancedVisualDebugger.VisualizationMode.VARIABLES);
                player.sendMessage("§aVariables visualization enabled!");
                player.sendMessage("§7Blocks will be highlighted with purple particles during execution.");
                break;
                
            case "off":
            case "disable":
                plugin.getScriptDebugger().stopVisualization(player);
                player.sendMessage("§cVisualization disabled!");
                break;
                
            default:
                player.sendMessage("§cUnknown visualization mode: " + subCommand);
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§6=== Visualization Commands ===");
        player.sendMessage("§f/visualize standard §7- Enable standard block highlighting");
        player.sendMessage("§f/visualize step §7- Enable step-by-step visualization");
        player.sendMessage("§f/visualize performance §7- Enable performance visualization");
        player.sendMessage("§f/visualize memory §7- Enable memory usage visualization");
        player.sendMessage("§f/visualize variables §7- Enable variable tracking visualization");
        player.sendMessage("§f/visualize off §7- Disable visualization");
        player.sendMessage("");
        player.sendMessage("§7Visualization helps you see how your scripts execute in real-time!");
    }
}