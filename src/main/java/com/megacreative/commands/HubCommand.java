package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для возврата игрока в хаб
 *
 * Command to return player to hub
 *
 * Befehl, um den Spieler zum Hub zurückzukehren
 */
public class HubCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    private final IPlayerManager playerManager;
    
    /**
     * Конструктор команды HubCommand
     * @param plugin основной плагин
     * @param playerManager менеджер игроков
     *
     * Constructor for HubCommand
     * @param plugin main plugin
     * @param playerManager player manager
     *
     * Konstruktor für HubCommand
     * @param plugin Haupt-Plugin
     * @param playerManager Spieler-Manager
     */
    public HubCommand(MegaCreative plugin, IPlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }
    
    /**
     * Обрабатывает выполнение команды /hub
     * @param sender отправитель команды
     * @param command команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles execution of the /hub command
     * @param sender command sender
     * @param command command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des /hub-Befehls
     * @param sender Befehlsabsender
     * @param command Befehl
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
        
        // Первый мир в списке сервера обычно является лобби
        World hubWorld = Bukkit.getWorlds().get(0); 
        
        player.teleport(hubWorld.getSpawnLocation());
        player.sendMessage("§aВы вернулись в хаб!");
        
        // Очищаем инвентарь и выдаем стартовые предметы
        player.getInventory().clear();
        playerManager.giveStarterItems(player);
        
        return true;
    }
} 