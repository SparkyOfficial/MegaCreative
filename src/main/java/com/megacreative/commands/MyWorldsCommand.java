package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.gui.MyWorldsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Команда для открытия GUI меню миров игрока
 * Позволяет игроку просматривать и управлять своими мирами
 * Доступ к функциям создания, удаления и настройки миров
 *
 * Command to open the player's world GUI menu
 * Allows players to view and manage their worlds
 * Access to world creation, deletion and configuration functions
 *
 * Befehl zum Öffnen des GUI-Menüs der Spielerwelten
 * Ermöglicht es Spielern, ihre Welten anzuzeigen und zu verwalten
 * Zugriff auf Funktionen zur Weltenerschaffung, -löschung und -konfiguration
 */
public class MyWorldsCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Инициализирует команду меню миров игрока
     * @param plugin основной экземпляр плагина
     *
     * Initializes the player world menu command
     * @param plugin main plugin instance
     *
     * Initialisiert den Befehl für das Spielerweltmenü
     * @param plugin Haupt-Plugin-Instanz
     */
    public MyWorldsCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды меню миров игрока
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles player world menu command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Spielerweltmenü-Befehls
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        new MyWorldsGUI(plugin, player).open();
        return true;
    }
}