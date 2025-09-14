package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldMode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для переключения мира в режим строительства
 *
 * Command to switch world to build mode
 *
 * Befehl zum Wechseln der Welt in den Bauplan-Modus
 */
public class BuildCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    
    /**
     * Конструктор команды BuildCommand
     * @param plugin основной плагин
     * @param worldManager менеджер миров
     *
     * Constructor for BuildCommand
     * @param plugin main plugin
     * @param worldManager world manager
     *
     * Konstruktor für BuildCommand
     * @param plugin Haupt-Plugin
     * @param worldManager Welt-Manager
     */
    public BuildCommand(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command is only available to players!");
            return true;
        }
        
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
        
        // If we can't find the world directly, try to find it by ID from the world name
        if (creativeWorld == null) {
            String worldName = player.getWorld().getName();
            if (worldName.startsWith("megacreative_")) {
                // Extract world ID from the world name - improved logic
                String worldId = extractWorldId(worldName);
                if (worldId != null) {
                    creativeWorld = worldManager.getWorld(worldId);
                }
            }
        }
        
        if (creativeWorld == null) {
            player.sendMessage("§cYou are not in a MegaCreative world!");
            return true;
        }
        
        if (!creativeWorld.canEdit(player)) {
            player.sendMessage("§cYou don't have permission to edit this world!");
            return true;
        }
        
        if (plugin.getBlockPlacementHandler().isInDevWorld(player)) {
            plugin.getServiceRegistry().getDevInventoryManager().savePlayerInventory(player);
        }
        
        plugin.getServiceRegistry().getDevInventoryManager().restorePlayerInventory(player);
        
        creativeWorld.setMode(com.megacreative.models.WorldMode.BUILD);
        worldManager.switchToBuildWorld(player, creativeWorld.getId());
        
        player.sendMessage("§aWorld mode changed to §f§lBUILD§a!");
        player.sendMessage("§7❌ Code disabled, scripts will not execute");
        player.sendMessage("§7Creative mode for builders");
        
        player.getInventory().clear();
        
        worldManager.saveWorld(creativeWorld);
        return true;
    }
    
    /**
     * Extracts world ID from world name with improved logic for dual world architecture
     * @param worldName the world name
     * @return the extracted world ID or null if not found
     */
    private String extractWorldId(String worldName) {
        if (!worldName.startsWith("megacreative_")) {
            return null;
        }
        
        // Remove prefix
        String withoutPrefix = worldName.substring("megacreative_".length());
        
        // Remove all possible suffixes for dual world architecture
        withoutPrefix = withoutPrefix.replace("-code", "")
                                    .replace("-world", "")
                                    .replace("_dev", "");
        
        // If we still have something, that's our ID
        return withoutPrefix.isEmpty() ? null : withoutPrefix;
    }
}