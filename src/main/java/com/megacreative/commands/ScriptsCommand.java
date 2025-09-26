package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.gui.ScriptsGUI;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для открытия GUI управления скриптами
 * Позволяет игроку просматривать, редактировать и управлять скриптами в мире
 * Доступ к функциям создания, удаления и настройки скриптов
 *
 * Command to open the script management GUI
 * Allows players to view, edit and manage scripts in the world
 * Access to script creation, deletion and configuration functions
 *
 * Befehl zum Öffnen der Skriptverwaltungs-GUI
 * Ermöglicht es Spielern, Skripte in der Welt anzuzeigen, zu bearbeiten und zu verwalten
 * Zugriff auf Funktionen zur Skripterstellung, -löschung und -konfiguration
 */
public class ScriptsCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Инициализирует команду управления скриптами
     * @param plugin основной экземпляр плагина
     *
     * Initializes the script management command
     * @param plugin main plugin instance
     *
     * Initialisiert den Skriptverwaltungs-Befehl
     * @param plugin Haupt-Plugin-Instanz
     */
    public ScriptsCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды управления скриптами
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles script management command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Skriptverwaltungs-Befehls
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
        
        // Проверяем, что игрок находится в мире MegaCreative
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null) {
            player.sendMessage("§cВы должны находиться в мире MegaCreative!");
            return true;
        }
        
        // Проверяем права на кодирование
        if (!creativeWorld.canCode(player)) {
            player.sendMessage("§cУ вас нет прав на управление скриптами в этом мире!");
            return true;
        }
        
        // Открываем GUI скриптов
        new ScriptsGUI(plugin, player).open();
        
        return true;
    }
}