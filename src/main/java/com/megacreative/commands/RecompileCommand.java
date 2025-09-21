package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.AutoConnectionManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for recompiling scripts in the current world
 * Useful for debugging and testing script changes
 */
public class RecompileCommand implements CommandExecutor, TabCompleter {
    private final MegaCreative plugin;

    /**
     * Initializes the recompile command with required dependencies
     * @param plugin main plugin instance
     */
    public RecompileCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles recompile command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command is only for players!");
            return true;
        }

        Player player = (Player) sender;
        World world = player.getWorld();
        
        // Check if this is a dev world
        if (!isDevWorld(world)) {
            player.sendMessage("§cThis command can only be used in development worlds!");
            return true;
        }
        
        try {
            // Get the AutoConnectionManager
            AutoConnectionManager connectionManager = plugin.getServiceRegistry().getAutoConnectionManager();
            if (connectionManager == null) {
                player.sendMessage("§cAutoConnectionManager is not available!");
                return true;
            }
            
            // Recompile all scripts in the world
            connectionManager.recompileWorldScripts(world);
            
            player.sendMessage("§a✓ Scripts recompiled successfully in world: " + world.getName());
            plugin.getLogger().info("Scripts recompiled by " + player.getName() + " in world: " + world.getName());
            
        } catch (Exception e) {
            player.sendMessage("§cError recompiling scripts: " + e.getMessage());
            plugin.getLogger().severe("Error recompiling scripts for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Checks if a world is a development world
     * @param world the world to check
     * @return true if it's a dev world
     */
    private boolean isDevWorld(World world) {
        String worldName = world.getName();
        return worldName.contains("dev") || worldName.contains("Dev") || 
               worldName.contains("разработка") || worldName.contains("Разработка") ||
               worldName.contains("creative") || worldName.contains("Creative") ||
               worldName.contains("-code") || worldName.endsWith("-code") || 
               worldName.contains("_code") || worldName.endsWith("_dev") ||
               worldName.contains("megacreative_");
    }

    /**
     * Handles recompile command tab completion
     * @param sender command sender
     * @param command executed command
     * @param alias command alias
     * @param args command arguments
     * @return list of possible completions
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // No tab completions needed for this simple command
        return completions;
    }
}