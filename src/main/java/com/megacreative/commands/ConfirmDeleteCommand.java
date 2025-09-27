package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для подтверждения удаления мира
 *
 * Command to confirm world deletion
 *
 * Befehl zur Bestätigung der Weltenlöschung
 */
public class ConfirmDeleteCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Конструктор команды ConfirmDeleteCommand
     * @param plugin основной плагин
     *
     * Constructor for ConfirmDeleteCommand
     * @param plugin main plugin
     *
     * Konstruktor für ConfirmDeleteCommand
     * @param plugin Haupt-Plugin
     */
    public ConfirmDeleteCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды /confirmdelete
     * @param sender отправитель команды
     * @param command команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles execution of the /confirmdelete command
     * @param sender command sender
     * @param command command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des /confirmdelete-Befehls
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
        
        if (args.length == 0) {
            player.sendMessage("§cИспользование: /confirmdelete <ID мира>");
            return true;
        }
        
        String worldId = args[0];
        CreativeWorld world = plugin.getServiceRegistry().getWorldManager().getWorld(worldId);
        
        if (world == null) {
            player.sendMessage("§cМир с ID " + worldId + " не найден.");
            return true;
        }
        
        // Проверка, что игрок - владелец
        if (!world.isOwner(player)) {
            player.sendMessage("§cТолько владелец может удалить этот мир.");
            return true;
        }
        
        plugin.getServiceRegistry().getWorldManager().deleteWorld(worldId, player);
        player.sendMessage("§aМир '" + world.getName() + "' помечен к удалению!");
        return true;
    }
}