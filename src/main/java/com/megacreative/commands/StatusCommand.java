package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для отображения статуса текущего мира
 * Показывает информацию о режиме мира, состоянии кодирования и количестве скриптов
 * Управление отображением статуса мира и его параметров
 *
 * Command to display the status of the current world
 * Shows information about world mode, coding state and number of scripts
 * Management of world status display and its parameters
 *
 * Befehl zur Anzeige des Status der aktuellen Welt
 * Zeigt Informationen über den Weltmodus, den Codierungsstatus und die Anzahl der Skripte an
 * Verwaltung der Weltstatusanzeige und ihrer Parameter
 */
public class StatusCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Инициализирует команду статуса мира
     * @param plugin основной экземпляр плагина
     *
     * Initializes the world status command
     * @param plugin main plugin instance
     *
     * Initialisiert den Weltstatus-Befehl
     * @param plugin Haupt-Plugin-Instanz
     */
    public StatusCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды статуса мира
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles world status command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Weltstatus-Befehls
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
        
        WorldMode mode = creativeWorld.getMode();
        boolean codeEnabled = mode.isCodeEnabled();
        
        player.sendMessage("§6=== Статус мира ===");
        player.sendMessage("§7Мир: §f" + creativeWorld.getName());
        player.sendMessage("§7Режим: §f" + mode.getDisplayName());
        player.sendMessage("§7Код: " + (codeEnabled ? "§a✅ ВКЛЮЧЕН" : "§c❌ ВЫКЛЮЧЕН"));
        player.sendMessage("§7Скриптов: §f" + creativeWorld.getScripts().size());
        
        if (codeEnabled) {
            player.sendMessage("§aСкрипты будут выполняться при событиях");
        } else {
            player.sendMessage("§cСкрипты не будут выполняться");
        }
        
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