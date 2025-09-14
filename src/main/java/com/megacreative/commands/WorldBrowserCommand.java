package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.gui.WorldBrowserGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для открытия браузера миров
 * Позволяет игрокам просматривать доступные миры и присоединяться к ним
 * Управление навигацией по мирам и выбором миров для игры
 *
 * Command to open the world browser
 * Allows players to browse available worlds and join them
 * Management of world navigation and world selection for gameplay
 *
 * Befehl zum Öffnen des Weltenbrowsers
 * Ermöglicht es Spielern, verfügbare Welten zu durchsuchen und ihnen beizutreten
 * Verwaltung der Welt-Navigation und der Weltenauswahl für das Gameplay
 */
public class WorldBrowserCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Инициализирует команду браузера миров
     * @param plugin основной экземпляр плагина
     *
     * Initializes the world browser command
     * @param plugin main plugin instance
     *
     * Initialisiert den Weltenbrowser-Befehl
     * @param plugin Haupt-Plugin-Instanz
     */
    public WorldBrowserCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды браузера миров
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles world browser command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Weltenbrowser-Befehls
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
        
        new WorldBrowserGUI(plugin, player).open();
        return true;
    }
}