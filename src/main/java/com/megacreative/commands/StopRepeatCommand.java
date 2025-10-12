package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.actions.RepeatTriggerAction;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.services.RepeatingTaskManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для остановки повторяющихся задач
 * Позволяет игрокам останавливать свои повторяющиеся задачи
 * Администраторы могут останавливать все задачи сразу
 *
 * Command to stop repeating tasks
 * Allows players to stop their repeating tasks
 * Administrators can stop all tasks at once
 *
 * Befehl zum Stoppen von wiederkehrenden Aufgaben
 * Ermöglicht es Spielern, ihre wiederkehrenden Aufgaben zu stoppen
 * Administratoren können alle Aufgaben gleichzeitig stoppen
 */
public class StopRepeatCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Инициализирует команду остановки повторяющихся задач
     * @param plugin основной экземпляр плагина
     *
     * Initializes the stop repeating tasks command
     * @param plugin main plugin instance
     *
     * Initialisiert den Befehl zum Stoppen von wiederkehrenden Aufgaben
     * @param plugin Haupt-Plugin-Instanz
     */
    public StopRepeatCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды остановки повторяющихся задач
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles stop repeating tasks command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Befehls zum Stoppen von wiederkehrenden Aufgaben
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        RepeatingTaskManager taskManager = serviceRegistry.getRepeatingTaskManager();
        
        if (args.length == 0) {
            
            if (taskManager.hasActiveTask(player.getUniqueId())) {
                taskManager.stopRepeatingTask(player.getUniqueId());
                player.sendMessage("§a✅ Ваши повторяющиеся задачи остановлены!");
            } else {
                player.sendMessage("§eℹ У вас нет активных повторяющихся задач.");
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
            
            if (player.isOp()) {
                int stoppedCount = taskManager.stopAllRepeatingTasks();
                player.sendMessage("§a✅ Остановлено " + stoppedCount + " повторяющихся задач!");
            } else {
                player.sendMessage("§c❌ У вас нет прав для остановки всех задач!");
            }
        } else {
            player.sendMessage("§eИспользование:");
            player.sendMessage("§7/stoprepeat §8- остановить ваши повторяющиеся задачи");
            player.sendMessage("§7/stoprepeat all §8- остановить все задачи (только для операторов)");
        }
        
        return true;
    }
}