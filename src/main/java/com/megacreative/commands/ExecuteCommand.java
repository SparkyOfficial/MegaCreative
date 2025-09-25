package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.managers.PlayerModeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;

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
        
        // Check if player is in DEV mode
        if (plugin.getServiceRegistry() != null) {
            PlayerModeManager modeManager = plugin.getServiceRegistry().getPlayerModeManager();
            if (!modeManager.isInDevMode(player)) {
                player.sendMessage("§cYou can only execute scripts in DEV mode!");
                return true;
            }
        }
        
        try {
            // Log command execution start
            plugin.getLogger().info("Player " + player.getName() + " is executing /execute command");
            
            // Get the ScriptEngine
            ScriptEngine scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
            if (scriptEngine == null) {
                player.sendMessage("§cScriptEngine is not available!");
                plugin.getLogger().severe("ScriptEngine is not available when executing /execute command for player " + player.getName());
                return true;
            }
            
            // Get the BlockPlacementHandler
            BlockPlacementHandler blockPlacementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
            if (blockPlacementHandler == null) {
                player.sendMessage("§cBlockPlacementHandler is not available!");
                plugin.getLogger().severe("BlockPlacementHandler is not available when executing /execute command for player " + player.getName());
                return true;
            }
            
            // Find the block the player is looking at
            Block targetBlock = getTargetBlock(player);
            if (targetBlock == null) {
                player.sendMessage("§cYou must be looking at a code block to execute it!");
                plugin.getLogger().warning("Player " + player.getName() + " is not looking at a block when executing /execute command");
                return true;
            }
            
            plugin.getLogger().info("Player " + player.getName() + " is looking at block at " + targetBlock.getLocation());
            
            // Get the CodeBlock from the BlockPlacementHandler
            CodeBlock codeBlock = blockPlacementHandler.getCodeBlock(targetBlock.getLocation());
            if (codeBlock == null) {
                player.sendMessage("§cThe block you're looking at is not a code block!");
                plugin.getLogger().warning("Block at " + targetBlock.getLocation() + " is not a code block when executing /execute command for player " + player.getName());
                return true;
            }
            
            plugin.getLogger().info("Player " + player.getName() + " is executing code block with action: " + codeBlock.getAction());
            player.sendMessage("§eExecuting code block: " + codeBlock.getAction());
            
            // Execute the code block chain
            CompletableFuture<ExecutionResult> future = scriptEngine.executeBlockChain(codeBlock, player, "manual_execute");
            future.thenAccept(result -> {
                if (result.isSuccess()) {
                    player.sendMessage("§a✓ Script executed successfully: " + result.getMessage());
                    plugin.getLogger().info("Script executed successfully for player " + player.getName() + ": " + result.getMessage());
                } else {
                    player.sendMessage("§cScript execution failed: " + result.getMessage());
                    plugin.getLogger().severe("Script execution failed for player " + player.getName() + ": " + result.getMessage());
                }
            }).exceptionally(throwable -> {
                player.sendMessage("§cScript execution error: " + throwable.getMessage());
                plugin.getLogger().severe("Script execution error for player " + player.getName() + ": " + throwable.getMessage());
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
     * Gets the block the player is looking at
     * @param player the player
     * @return the block the player is looking at, or null if not found
     */
    private Block getTargetBlock(Player player) {
        try {
            // Use BlockIterator to find the block the player is looking at
            BlockIterator iterator = new BlockIterator(player, 100); // 100 block range
            while (iterator.hasNext()) {
                Block block = iterator.next();
                if (block.getType().isSolid()) {
                    return block;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting target block for player " + player.getName() + ": " + e.getMessage());
        }
        return null;
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