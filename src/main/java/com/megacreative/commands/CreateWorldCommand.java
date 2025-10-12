package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для создания нового мира
 *
 * Command to create a new world
 *
 * Befehl zum Erstellen einer neuen Welt
 */
public class CreateWorldCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    
    /**
     * Конструктор команды CreateWorldCommand
     * @param plugin основной плагин
     * @param worldManager менеджер миров
     *
     * Constructor for CreateWorldCommand
     * @param plugin main plugin
     * @param worldManager world manager
     *
     * Konstruktor für CreateWorldCommand
     * @param plugin Haupt-Plugin
     * @param worldManager Welt-Manager
     */
    public CreateWorldCommand(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }
    
    /**
     * Обрабатывает выполнение команды /create
     * @param sender отправитель команды
     * @param command команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles execution of the /create command
     * @param sender command sender
     * @param command command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des /create-Befehls
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
        
        
        if (worldManager.getPlayerWorldCount(player) >= 5) {
            player.sendMessage("§c❌ Вы уже создали максимальное количество миров (5)!");
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage("§c❌ Укажите тип мира! Доступные типы:");
            player.sendMessage("§7- survival (обычный мир)");
            player.sendMessage("§7- flat (плоский мир)");
            player.sendMessage("§7- void (пустой мир)");
            player.sendMessage("§7- ocean (океанский мир)");
            player.sendMessage("§7- nether (адский мир)");
            player.sendMessage("§7- end (краевой мир)");
            player.sendMessage("");
            player.sendMessage("§e🎆 Reference system-style dual world mode:");
            player.sendMessage("§7  Add §f--dual §7to create paired dev/play worlds");
            player.sendMessage("§7  Example: §f/create flat --dual My World");
            return true;
        }
        
        String typeStr = args[0].toLowerCase();
        CreativeWorldType worldType;
        
        
        boolean isDualMode = false;
        
        try {
            worldType = CreativeWorldType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c❌ Неизвестный тип мира: " + typeStr);
            return true;
        }
        
        
        String worldName;
        int nameStartIndex = 1;
        
        
        if (args.length > 1 && args[1].equals("--dual")) {
            isDualMode = true;
            nameStartIndex = 2;
        }
        
        
        if (args.length > nameStartIndex) {
            
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = nameStartIndex; i < args.length; i++) {
                if (i > nameStartIndex) nameBuilder.append(" ");
                nameBuilder.append(args[i]);
            }
            worldName = nameBuilder.toString();
        } else {
            
            worldName = player.getName() + "'s World " + (worldManager.getPlayerWorldCount(player) + 1);
        }
        
        
        if (worldName.length() < 3 || worldName.length() > 20) {
            player.sendMessage("§cНазвание мира должно содержать от 3 до 20 символов!");
            return true;
        }
        
        if (!worldName.matches("^[a-zA-Z0-9_\\sА-Яа-яЁё]+$")) {
            player.sendMessage("§cНазвание мира может содержать только буквы, цифры, пробелы и подчеркивания!");
            return true;
        }
        
        
        if (isDualMode) {
            player.sendMessage("§a⏳ Создание парных миров \"" + worldName + "\"...");
            player.sendMessage("§7🔧 Мир разработки: " + worldName + "-code");
            player.sendMessage("§7🎮 Игровой мир: " + worldName + "-world");
            
            worldManager.createDualWorld(player, worldName, worldType);
        } else {
            player.sendMessage("§a⏳ Создание мира \"" + worldName + "\"...");
            
            worldManager.createWorld(player, worldName, worldType);
        }
        
        
        
        
        return true;
    }
}