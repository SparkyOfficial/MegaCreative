package com.megacreative.commands;

import com.megacreative.interfaces.IWorldManager;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldMode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Команда для переключения мира в режим строительства
 *
 * Command to switch world to build mode
 *
 * Befehl zum Wechseln der Welt in den Bauplan-Modus
 */
public class BuildCommand implements CommandExecutor {
    
    private final IWorldManager worldManager;
    private final PlayerModeManager playerModeManager;
    private final com.megacreative.managers.DevInventoryManager devInventoryManager;
    private final com.megacreative.coding.BlockPlacementHandler blockPlacementHandler;
    
    /**
     * Конструктор команды BuildCommand
     * @param worldManager менеджер миров
     * @param playerModeManager менеджер режимов игроков
     * @param devInventoryManager менеджер инвентаря разработки
     * @param blockPlacementHandler обработчик размещения блоков
     */
    public BuildCommand(
            IWorldManager worldManager,
            PlayerModeManager playerModeManager,
            com.megacreative.managers.DevInventoryManager devInventoryManager,
            com.megacreative.coding.BlockPlacementHandler blockPlacementHandler) {
        this.worldManager = worldManager;
        this.playerModeManager = playerModeManager;
        this.devInventoryManager = devInventoryManager;
        this.blockPlacementHandler = blockPlacementHandler;
    }
    
    /**
     * Обрабатывает выполнение команды /build
     * @param sender отправитель команды
     * @param command команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles execution of the /build command
     * @param sender command sender
     * @param command command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des /build-Befehls
     * @param sender Befehlsabsender
     * @param command Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command is only available to players!");
            return true;
        }
        
        
        playerModeManager.setMode(player, PlayerModeManager.PlayerMode.DEV);
        
        
        if (worldManager == null) {
            player.sendMessage("§cWorld manager not available!");
            return true;
        }
        
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
        
        
        if (creativeWorld == null) {
            player.sendMessage("§cYou are not in a MegaCreative world!");
            player.sendMessage("§7Current world: " + player.getWorld().getName());
            player.sendMessage("§7Available worlds: " + worldManager.getCreativeWorlds().size());
            
            
            String worldName = player.getWorld().getName();
            
            if (worldName.startsWith("megacreative_")) {
                    
                    String potentialId;
                    
                    
                    if (worldName.contains("-code") || worldName.contains("-world")) {
                        
                        int startIndex = "megacreative_".length();
                        
                        // Calculate indices
                        int codeIndex = worldName.indexOf("-code");
                        int worldIndex = worldName.indexOf("-world");
                        int devIndex = worldName.indexOf("_dev");
                        
                        // Use the extracted method to calculate endIndex
                        int endIndex = calculateEndIndex(worldName, codeIndex, worldIndex, devIndex, startIndex);
                        
                        // endIndex is always greater than startIndex when this point is reached
                        potentialId = worldName.substring(startIndex, endIndex);
                    } 
                    
                    else if (worldName.contains("_dev")) {
                        potentialId = worldName.replace("megacreative_", "").replace("_dev", "");
                    }
                    
                    else {
                        potentialId = worldName.replace("megacreative_", "");
                    }
                    
                    // potentialId is always assigned a value in all code paths
                    CreativeWorld foundWorld = worldManager.getWorld(potentialId);
                    if (foundWorld != null) {
                        creativeWorld = foundWorld;
                    }
                }
            
            
            if (creativeWorld == null) {
                for (CreativeWorld world : worldManager.getCreativeWorlds()) {
                    if (worldName.contains(world.getId()) || worldName.contains(world.getName().toLowerCase().replace(" ", ""))) {
                        creativeWorld = world;
                        break;
                    }
                }
            }
            
            
            if (creativeWorld == null) {
                player.sendMessage("§cUnable to find associated MegaCreative world. Please contact an administrator.");
                return true;
            }
        }
        
        
        if (!creativeWorld.canEdit(player)) {
            player.sendMessage("§cYou don't have permission to edit this world!");
            return true;
        }
        
        
        if (blockPlacementHandler != null) {
            blockPlacementHandler.saveAllCodeBlocksInWorld(player.getWorld());
        }
        
        
        if (devInventoryManager != null) {
            devInventoryManager.savePlayerInventory(player);
        }
        
        
        if (devInventoryManager != null) {
            devInventoryManager.restorePlayerInventory(player);
        }
        
        
        worldManager.switchToBuildWorld(player, creativeWorld.getId());
        
        player.sendMessage("§aWorld mode changed to §f§lBUILD§a!");
        player.sendMessage("§7❌ Code disabled, scripts will not execute");
        player.sendMessage("§7Creative mode for builders");
        
        
        player.getInventory().clear();
        
        
        worldManager.saveWorld(creativeWorld);
        
        return true;
    }
    
    /**
     * Calculates the end index for substring extraction
     * @param worldName the world name to process
     * @return the calculated end index
     */
    private int calculateEndIndex(String worldName) {
        return worldName.length();
    }
    
    /**
     * Calculates the end index for substring extraction
     * @param worldName the world name to process
     * @param codeIndex index of "-code" substring
     * @param worldIndex index of "-world" substring
     * @param devIndex index of "_dev" substring
     * @param startIndex starting index for substring
     * @return the calculated end index
     */
    private int calculateEndIndex(String worldName, int codeIndex, int worldIndex, int devIndex, int startIndex) {
        int endIndex = worldName.length();
        
        if (codeIndex != -1) endIndex = codeIndex;
        if (worldIndex != -1 && worldIndex < endIndex) endIndex = worldIndex;
        if (devIndex != -1 && devIndex < endIndex) endIndex = devIndex;
        
        return Math.max(endIndex, startIndex); // Ensure endIndex is never less than startIndex
    }
}
