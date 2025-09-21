package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command for manually executing scripts
 * Useful for testing and debugging script execution
 */
public class ExecuteCommand implements CommandExecutor, TabCompleter {
    private final MegaCreative plugin;

    /**
     * Initializes the execute command with required dependencies
     * @param plugin main plugin instance
     */
    public ExecuteCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles execute command execution
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
        
        try {
            // Get the ScriptEngine
            ScriptEngine scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
            if (scriptEngine == null) {
                player.sendMessage("§cScriptEngine is not available!");
                return true;
            }
            
            // Find the creative world
            CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld == null) {
                player.sendMessage("§cThis command can only be used in creative worlds!");
                return true;
            }
            
            // For testing purposes, let's create a simple test script
            player.sendMessage("§eExecuting test script...");
            
            // Create a simple test script that sends a message to the player
            // Event block (DIAMOND_BLOCK with onJoin action)
            CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
            
            // Action block (COBBLESTONE with sendMessage action)
            CodeBlock actionBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
            actionBlock.setParameter("message", "Hello from test script!");
            
            // Connect the event block to the action block
            eventBlock.setNextBlock(actionBlock);
            
            // Create the script with the event block as root
            CodeScript testScript = new CodeScript("Test Script", true, eventBlock);
            
            // Execute the script
            CompletableFuture<ExecutionResult> future = scriptEngine.executeScript(testScript, player, "manual_execute");
            future.thenAccept(result -> {
                if (result.isSuccess()) {
                    player.sendMessage("§a✓ Script executed successfully: " + result.getMessage());
                } else {
                    player.sendMessage("§cScript execution failed: " + result.getMessage());
                }
            }).exceptionally(throwable -> {
                player.sendMessage("§cScript execution error: " + throwable.getMessage());
                return null;
            });
            
        } catch (Exception e) {
            player.sendMessage("§cError executing script: " + e.getMessage());
            plugin.getLogger().severe("Error executing script for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Handles execute command tab completion
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