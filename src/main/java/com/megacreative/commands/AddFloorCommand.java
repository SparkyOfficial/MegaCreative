package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для добавления новых этажей в миры разработки
 * Позволяет игрокам расширять пространство для программирования вертикально
 *
 * Command to add new floors to development worlds
 * Allows players to expand their coding space vertically
 *
 * Befehl zum Hinzufügen neuer Ebenen zu Entwicklungswelten
 * Ermöglicht es Spielern, ihren Programmierbereich vertikal zu erweitern
 */
public class AddFloorCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Инициализирует команду добавления этажей
     * @param plugin основной экземпляр плагина
     *
     * Initializes the add floor command
     * @param plugin main plugin instance
     *
     * Initialisiert den Befehl zum Hinzufügen von Ebenen
     * @param plugin Haupt-Plugin-Instanz
     */
    public AddFloorCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды добавления этажей
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles add floor command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Befehls zum Hinzufügen von Ebenen
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        
        String worldName = player.getWorld().getName();
        if (!isDevWorld(worldName)) {
            player.sendMessage("§cЭта команда работает только в мирах разработки!");
            return true;
        }
        
        
        int floorNumber;
        if (args.length == 0) {
            
            floorNumber = 1;
        } else {
            try {
                floorNumber = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cОшибка: Укажите корректный номер этажа (число)");
                player.sendMessage("§7Использование: /addfloor <номер_этажа>");
                return true;
            }
        }
        
        
        if (floorNumber < 1) {
            player.sendMessage("§cНомер этажа должен быть больше 0!");
            return true;
        }
        
        if (floorNumber > DevWorldGenerator.getMaxFloors()) {
            player.sendMessage("§cМаксимальное количество этажей: " + DevWorldGenerator.getMaxFloors());
            return true;
        }
        
        
        if (!player.hasPermission("megacreative.addfloor") && !player.isOp()) {
            player.sendMessage("§cУ вас нет прав для использования этой команды!");
            return true;
        }
        
        
        try {
            DevWorldGenerator.addFloorForPlayer(player.getWorld(), player, floorNumber);
        } catch (Exception e) {
            player.sendMessage("§cОшибка при создании этажа: " + e.getMessage());
            plugin.getLogger().severe("Failed to create floor " + floorNumber + " for player " + player.getName() + ": " + e.getMessage());
            plugin.getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
        
        return true;
    }
    
    /**
     * Проверяет, является ли мир миром разработки
     * @param worldName имя мира для проверки
     * @return true если мир является миром разработки
     *
     * Checks if the world is a development world
     * @param worldName world name to check
     * @return true if the world is a development world
     *
     * Prüft, ob die Welt eine Entwicklungswelt ist
     * @param worldName zu prüfender Weltname
     * @return true, wenn die Welt eine Entwicklungswelt ist
     */
    private boolean isDevWorld(String worldName) {
        return worldName.contains("dev") || worldName.contains("Dev") || 
               worldName.contains("разработка") || worldName.contains("Разработка") ||
               worldName.contains("creative") || worldName.contains("Creative");
    }
}