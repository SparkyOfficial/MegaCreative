package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.gui.TemplateBrowserGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для открытия браузера шаблонов
 * Позволяет игрокам просматривать и использовать доступные шаблоны скриптов
 * Управление доступом к публичным и приватным шаблонам
 *
 * Command to open the template browser
 * Allows players to browse and use available script templates
 * Management of access to public and private templates
 *
 * Befehl zum Öffnen des Vorlagenbrowsers
 * Ermöglicht es Spielern, verfügbare Skriptvorlagen zu durchsuchen und zu verwenden
 * Verwaltung des Zugriffs auf öffentliche und private Vorlagen
 */
public class TemplatesCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Инициализирует команду браузера шаблонов
     * @param plugin основной экземпляр плагина
     *
     * Initializes the template browser command
     * @param plugin main plugin instance
     *
     * Initialisiert den Vorlagenbrowser-Befehl
     * @param plugin Haupt-Plugin-Instanz
     */
    public TemplatesCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды браузера шаблонов
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles template browser command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Vorlagenbrowser-Befehls
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
        
        // Открываем браузер шаблонов
        new TemplateBrowserGUI(plugin, player).open();
        
        return true;
    }
}