package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.CodeCompiler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;

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
        
        // Check if world manager is available
        if (plugin.getWorldManager() == null) {
            player.sendMessage("§cWorld manager not available!");
            plugin.getLogger().severe("World manager is null in PlayCommand!");
            return true;
        }
        
        // Log debug information
        plugin.getLogger().info("PlayCommand executed by player: " + player.getName());
        plugin.getLogger().info("Current world: " + player.getWorld().getName());
        plugin.getLogger().info("ServiceRegistry available: " + (plugin.getServiceRegistry() != null));
        if (plugin.getServiceRegistry() != null) {
            plugin.getLogger().info("WorldManager in ServiceRegistry: " + (plugin.getServiceRegistry().getWorldManager() != null));
        }
        
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "switch", "world" -> {
                    CreativeWorld currentWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
                    if (currentWorld != null && currentWorld.getPairedWorldId() != null) {
                        if (plugin.getBlockPlacementHandler().isInDevWorld(player)) {
                            plugin.getServiceRegistry().getDevInventoryManager().savePlayerInventory(player);
                        }
                        plugin.getServiceRegistry().getDevInventoryManager().restorePlayerInventory(player);
                        plugin.getWorldManager().switchToPlayWorld(player, currentWorld.getId());
                        player.getInventory().clear();
                        return true;
                    }
                }
            }
        }
        
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        
        // 🔧 FIX: Enhanced world finding logic with better pattern matching
        if (creativeWorld == null) {
            player.sendMessage("§cYou are not in a MegaCreative world!");
            player.sendMessage("§7Current world: " + player.getWorld().getName());
            player.sendMessage("§7Available worlds: " + plugin.getWorldManager().getCreativeWorlds().size());
            
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
                    CreativeWorld foundWorld = plugin.getWorldManager().getWorld(potentialId);
                    if (foundWorld != null) {
                        creativeWorld = foundWorld;
                        player.sendMessage("§aFound world by extracted ID: " + potentialId);
                    }
                }
            }
            
            // If still not found, try all available worlds
            if (creativeWorld == null) {
                for (CreativeWorld world : plugin.getWorldManager().getCreativeWorlds()) {
                    if (worldName.contains(world.getId()) || worldName.contains(world.getName().toLowerCase().replace(" ", ""))) {
                        creativeWorld = world;
                        player.sendMessage("§aFound world by partial name matching: " + world.getName());
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
        if (plugin.getBlockPlacementHandler().isInDevWorld(player)) {
            plugin.getServiceRegistry().getDevInventoryManager().savePlayerInventory(player);
        }
        
        // Restore player inventory for play mode
        plugin.getServiceRegistry().getDevInventoryManager().restorePlayerInventory(player);
        
        // Switch to play world
        plugin.getWorldManager().switchToPlayWorld(player, creativeWorld.getId());
        
        // Clear inventory to prevent item duplication
        player.getInventory().clear();
        
        return true;
    }
}