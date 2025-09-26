package com.megacreative.commands;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.CodeCompiler;

/**
 * Команда для переключения мира в режим игры
 *
 * Command to switch world to play mode
 *
 * Befehl zum Wechseln der Welt in den Spielmodus
 */
public class PlayCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Конструктор команды PlayCommand
     * @param plugin основной плагин
     *
     * Constructor for PlayCommand
     * @param plugin main plugin
     *
     * Konstruktor für PlayCommand
     * @param plugin Haupt-Plugin
     */
    public PlayCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды /play
     * @param sender отправитель команды
     * @param command команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles execution of the /play command
     * @param sender command sender
     * @param command command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des /play-Befehls
     * @param sender Befehlsabsender
     * @param command Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command is only available to players!");
            return true;
        }
        
        // Get the PlayerModeManager
        PlayerModeManager modeManager = plugin.getServiceRegistry().getPlayerModeManager();
        
        // Check if player is already in PLAY mode
        if (modeManager.isInPlayMode(player)) {
            player.sendMessage("§cYou are already in PLAY mode!");
            return true;
        }
        
        // Switch player to PLAY mode
        modeManager.setMode(player, PlayerModeManager.PlayerMode.PLAY);
        
        // Change game mode to ADVENTURE
        player.setGameMode(GameMode.ADVENTURE);
        
        // Clear inventory
        player.getInventory().clear();
        
        // Check if world manager is available
        if (plugin.getServiceRegistry().getWorldManager() == null) {
            player.sendMessage("§cWorld manager not available!");
            plugin.getLogger().severe("World manager is null in PlayCommand!");
            return true;
        }
        
        // Find the creative world
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        
        // 🔧 FIX: Enhanced world finding logic with better pattern matching
        if (creativeWorld == null) {
            player.sendMessage("§cYou are not in a MegaCreative world!");
            player.sendMessage("§7Current world: " + player.getWorld().getName());
            player.sendMessage("§7Available worlds: " + plugin.getServiceRegistry().getWorldManager().getCreativeWorlds().size());
            
            // Try multiple pattern matching approaches
            String worldName = player.getWorld().getName();
            
            if (worldName.startsWith("megacreative_")) {
                // Extract ID using more precise method for complex naming
                String potentialId = null;
                
                // Handle new reference system-style naming (megacreative_ID-code, megacreative_ID-world)
                if (worldName.contains("-code") || worldName.contains("-world")) {
                    // Extract everything between "megacreative_" and the first suffix
                    int startIndex = "megacreative_".length();
                    int endIndex = worldName.length();
                    
                    // Find the first suffix
                    int codeIndex = worldName.indexOf("-code");
                    int worldIndex = worldName.indexOf("-world");
                    int devIndex = worldName.indexOf("_dev");
                    
                    if (codeIndex != -1 && codeIndex < endIndex) endIndex = codeIndex;
                    if (worldIndex != -1 && worldIndex < endIndex) endIndex = worldIndex;
                    if (devIndex != -1 && devIndex < endIndex) endIndex = devIndex;
                    
                    if (startIndex < endIndex) {
                        potentialId = worldName.substring(startIndex, endIndex);
                    }
                } 
                // Handle legacy naming (megacreative_ID_dev)
                else if (worldName.contains("_dev")) {
                    potentialId = worldName.replace("megacreative_", "").replace("_dev", "");
                }
                // Handle basic naming (megacreative_ID)
                else {
                    potentialId = worldName.replace("megacreative_", "");
                }
                
                if (potentialId != null) {
                    CreativeWorld foundWorld = plugin.getServiceRegistry().getWorldManager().getWorld(potentialId);
                    if (foundWorld != null) {
                        creativeWorld = foundWorld;
                        // player.sendMessage("§aFound world by extracted ID: " + potentialId);
                    }
                }
            }
            
            // If still not found, try all available worlds
            if (creativeWorld == null) {
                for (CreativeWorld world : plugin.getServiceRegistry().getWorldManager().getCreativeWorlds()) {
                    if (worldName.contains(world.getId()) || worldName.contains(world.getName().toLowerCase().replace(" ", ""))) {
                        creativeWorld = world;
                        // player.sendMessage("§aFound world by partial name matching: " + world.getName());
                        break;
                    }
                }
            }
            
            // If still not found, return
            if (creativeWorld == null) {
                player.sendMessage("§cUnable to find associated MegaCreative world. Please contact an administrator.");
                return true;
            }
        }
        
        // Save current world's code blocks before switching
        if (plugin.getServiceRegistry().getBlockPlacementHandler() != null) {
            plugin.getServiceRegistry().getBlockPlacementHandler().saveAllCodeBlocksInWorld(player.getWorld());
        }
        
        World currentWorld = player.getWorld();
        if (currentWorld.getName().contains("-code")) {
            // Compile code before switching to play mode
            CodeCompiler codeCompiler = plugin.getServiceRegistry().getCodeCompiler();
            if (codeCompiler != null) {
                try {
                    List<String> codeStrings = codeCompiler.compileWorldToCodeStrings(currentWorld);
                    // Use the world ID from the creativeWorld object we already found
                    codeCompiler.saveCompiledCode(creativeWorld.getId(), codeStrings);
                    player.sendMessage("§aCode compiled successfully!");
                } catch (Exception e) {
                    player.sendMessage("§cCode compilation error: " + e.getMessage());
                    plugin.getLogger().severe("Failed to compile world code: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        // Save player inventory before switching
        if (plugin.getServiceRegistry().getBlockPlacementHandler().isInDevWorld(player)) {
            plugin.getServiceRegistry().getDevInventoryManager().savePlayerInventory(player);
        }
        
        // Restore player inventory for play mode
        plugin.getServiceRegistry().getDevInventoryManager().restorePlayerInventory(player);
        
        // Find and execute the onJoin script
        executeOnJoinScript(player, creativeWorld);
        
        player.sendMessage("§aSwitched to PLAY mode! Your game is now running.");
        return true;
    }
    
    /**
     * Finds and executes the onJoin script for the player
     * @param player The player to execute the script for
     * @param creativeWorld The creative world to search for scripts
     */
    private void executeOnJoinScript(Player player, CreativeWorld creativeWorld) {
        // Find the onJoin script using ScriptCompiler instead of AutoConnectionManager
        if (creativeWorld.getScripts() != null) {
            for (CodeScript script : creativeWorld.getScripts()) {
                // Check if the script's root block is an onJoin event
                if (script.getRootBlock() != null && 
                    "onJoin".equals(script.getRootBlock().getAction())) {
                    
                    // Execute script using ScriptEngine
                    if (plugin.getServiceRegistry().getScriptEngine() != null) {
                        plugin.getServiceRegistry().getScriptEngine().executeScript(script, player, "play_mode_start")
                            .whenComplete((result, throwable) -> {
                                if (throwable != null) {
                                    player.sendMessage("§cError executing onJoin script: " + throwable.getMessage());
                                    plugin.getLogger().warning("onJoin script execution failed with exception: " + throwable.getMessage());
                                } else if (result != null && !result.isSuccess()) {
                                    player.sendMessage("§conJoin script execution failed: " + result.getMessage());
                                    plugin.getLogger().warning("onJoin script execution failed: " + result.getMessage());
                                } else {
                                    plugin.getLogger().info("Successfully executed onJoin script for player: " + player.getName());
                                }
                            });
                    }
                    break;
                }
            }
        }
    }
}