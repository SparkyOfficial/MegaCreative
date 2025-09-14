package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для удаления мира
 *
 * Command to delete a world
 *
 * Befehl zum Löschen einer Welt
 */
public class DeleteCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Конструктор команды DeleteCommand
     * @param plugin основной плагин
     *
     * Constructor for DeleteCommand
     * @param plugin main plugin
     *
     * Konstruktor für DeleteCommand
     * @param plugin Haupt-Plugin
     */
    public DeleteCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды /delete
     * @param sender отправитель команды
     * @param command команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles execution of the /delete command
     * @param sender command sender
     * @param command command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des /delete-Befehls
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
            player.sendMessage("§cИспользование: /delete <ID мира>");
            player.sendMessage("§7Чтобы узнать ID мира, используйте /myworlds");
            return true;
        }
        
        String worldId = args[0];
        CreativeWorld world = plugin.getWorldManager().getWorld(worldId);
        
        if (world == null) {
            player.sendMessage("§cМир с ID " + worldId + " не найден.");
            return true;
        }
        
        // Проверка, что игрок - владелец
        if (!world.isOwner(player)) {
            player.sendMessage("§cТолько владелец может удалить этот мир.");
            return true;
        }
        
        // Подтверждение удаления
        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            player.sendMessage("§eВы уверены, что хотите удалить мир '" + world.getName() + "'?");
            player.sendMessage("§eВсе данные будут потеряны безвозвратно!");
            player.sendMessage("§eДля подтверждения введите: §f/delete " + worldId + " confirm");
            return true;
        }
        
        plugin.getWorldManager().deleteWorld(worldId, player);
        // WorldManager сам отправит сообщение об успехе.
        return true;
    }
}