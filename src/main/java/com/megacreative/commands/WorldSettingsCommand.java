package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.gui.WorldSettingsGUI;
import com.megacreative.models.CreativeWorld;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для открытия настроек мира
 * Позволяет владельцу мира изменять параметры и настройки мира
 * Управление доступом, режимами и другими параметрами мира
 *
 * Command to open world settings
 * Allows world owner to change world parameters and settings
 * Management of access, modes and other world parameters
 *
 * Befehl zum Öffnen der Welteinstellungen
 * Ermöglicht es dem Weltenbesitzer, Weltparameter und -einstellungen zu ändern
 * Verwaltung von Zugriff, Modi und anderen Weltparametern
 */
public class WorldSettingsCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Инициализирует команду настроек мира
     * @param plugin основной экземпляр плагина
     *
     * Initializes the world settings command
     * @param plugin main plugin instance
     *
     * Initialisiert den Welteinstellungs-Befehl
     * @param plugin Haupt-Plugin-Instanz
     */
    public WorldSettingsCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды настроек мира
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles world settings command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Welteinstellungs-Befehls
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        World currentWorld = player.getWorld();
        CreativeWorld creativeWorld = findCreativeWorld(currentWorld);
        
        if (creativeWorld == null) {
            player.sendMessage("§cВы не находитесь в мире MegaCreative!");
            return true;
        }
        
        if (!creativeWorld.isOwner(player)) {
            player.sendMessage("§cТолько владелец мира может изменять настройки!");
            return true;
        }
        
        new WorldSettingsGUI(plugin, player, creativeWorld).open();
        return true;
    }
    
    /**
     * Находит творческий мир по Bukkit миру
     * @param bukkitWorld Bukkit мир для поиска
     * @return творческий мир или null если не найден
     *
     * Finds creative world by Bukkit world
     * @param bukkitWorld Bukkit world to search
     * @return creative world or null if not found
     *
     * Findet die Creative-Welt anhand der Bukkit-Welt
     * @param bukkitWorld Zu suchende Bukkit-Welt
     * @return Creative-Welt oder null, wenn nicht gefunden
     */
    private CreativeWorld findCreativeWorld(World bukkitWorld) {
        String worldName = bukkitWorld.getName();
        
        // 🔧 FIX: Remove prefix and ALL possible suffixes for dual world architecture
        if (worldName.startsWith("megacreative_")) {
            String id = worldName.replace("megacreative_", "")
                                  .replace("-code", "")    // New dev world suffix
                                  .replace("-world", "")   // New play world suffix  
                                  .replace("_dev", "");    // Legacy compatibility
            return plugin.getWorldManager().getWorld(id);
        }
        return null;
    }
}