package com.megacreative.commands;

import com.megacreative.MegaCreative;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Основная команда плагина MegaCreative
 * Предоставляет базовую информацию и функции управления
 * Доступ к справке, перезагрузке конфигурации и информации о плагине
 *
 * Main command of the MegaCreative plugin
 * Provides basic information and management functions
 * Access to help, configuration reload and plugin information
 *
 * Hauptbefehl des MegaCreative-Plugins
 * Bietet grundlegende Informationen und Verwaltungsfunktionen
 * Zugriff auf Hilfe, Konfigurationsneuladen und Plugin-Informationen
 */
public class MainCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Инициализирует основную команду плагина
     * @param plugin основной экземпляр плагина
     *
     * Initializes the main plugin command
     * @param plugin main plugin instance
     *
     * Initialisiert den Hauptbefehl des Plugins
     * @param plugin Haupt-Plugin-Instanz
     */
    public MainCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение основной команды
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles main command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Hauptbefehls
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
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help" -> sendHelp(player);
            case "reload" -> {
                if (!player.hasPermission("megacreative.admin")) {
                    player.sendMessage("§cУ вас нет прав на это действие!");
                    return true;
                }
                plugin.getConfigManager().loadConfig();
                player.sendMessage("§aКонфигурация перезагружена!");
            }
            case "info" -> sendInfo(player);
            default -> sendHelp(player);
        }
        
        return true;
    }
    
    /**
     * Отправляет справочную информацию игроку
     * @param player игрок, которому отправляется справка
     *
     * Sends help information to the player
     * @param player player to send help to
     *
     * Sendet Hilfsinformationen an den Spieler
     * @param player Spieler, dem die Hilfe gesendet wird
     */
    private void sendHelp(Player player) {
        player.sendMessage("§8§m                    §r §b§lMegaCreative §8§m                    ");
        player.sendMessage("§7/megacreative help §8- §fПоказать эту справку");
        player.sendMessage("§7/myworlds §8- §fОткрыть меню ваших миров");
        player.sendMessage("§7/worldbrowser §8- §fОткрыть браузер миров");
        player.sendMessage("§7/join <ID> §8- §fПрисоединиться к миру по ID");
        player.sendMessage("§7/play §8- §fПереключиться в режим игры");
        player.sendMessage("§7/build §8- §fПереключиться в режим строительства");
        player.sendMessage("§7/dev §8- §fПереключиться в режим разработки");
        player.sendMessage("§8§m                                                        ");
    }
    
    /**
     * Отправляет информацию о плагине игроку
     * @param player игрок, которому отправляется информация
     *
     * Sends plugin information to the player
     * @param player player to send information to
     *
     * Sendet Plugin-Informationen an den Spieler
     * @param player Spieler, dem die Informationen gesendet werden
     */
    private void sendInfo(Player player) {
        player.sendMessage("§8§m                    §r §b§lMegaCreative §8§m                    ");
        player.sendMessage("§7Версия: §f1.0.0");
        player.sendMessage("§7Автор: §fMegaCreative Team");
        player.sendMessage("§7Ваших миров: §f" + plugin.getWorldManager().getPlayerWorldCount(player) + "/5");
        player.sendMessage("§8§m                                                        ");
    }
}