package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для присоединения к миру
 *
 * Command to join a world
 *
 * Befehl zum Betreten einer Welt
 */
public class JoinCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    
    /**
     * Конструктор команды JoinCommand
     * @param plugin основной плагин
     * @param worldManager менеджер миров
     *
     * Constructor for JoinCommand
     * @param plugin main plugin
     * @param worldManager world manager
     *
     * Konstruktor für JoinCommand
     * @param plugin Haupt-Plugin
     * @param worldManager Welt-Manager
     */
    public JoinCommand(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }
    
    /**
     * Обрабатывает выполнение команды /join
     * @param sender отправитель команды
     * @param command команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles execution of the /join command
     * @param sender command sender
     * @param command command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des /join-Befehls
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
            player.sendMessage("§cИспользование: /join <ID>");
            return true;
        }
        
        String worldId = args[0];
        CreativeWorld creativeWorld = worldManager.getWorld(worldId);
        
        if (creativeWorld == null) {
            player.sendMessage("§cМир с ID §f" + worldId + " §cне найден!");
            return true;
        }
        
        // Проверка доступа к приватному миру
        if (creativeWorld.isPrivate() && !creativeWorld.isOwner(player) && 
            !creativeWorld.isTrustedBuilder(player) && !creativeWorld.isTrustedCoder(player)) {
            player.sendMessage("§cЭтот мир приватный!");
            return true;
        }
        
        // Загрузка мира если не загружен
        World bukkitWorld = Bukkit.getWorld(creativeWorld.getWorldName());
        if (bukkitWorld == null) {
            player.sendMessage("§eЗагрузка мира...");
            // Загружаем или создаем мир если он не загружен
            org.bukkit.WorldCreator creator = new org.bukkit.WorldCreator(creativeWorld.getWorldName());
            bukkitWorld = org.bukkit.Bukkit.createWorld(creator);
            if (bukkitWorld == null) {
                player.sendMessage("§cОшибка загрузки мира!");
                return true;
            }
        }
        
        // Телепортация
        player.teleport(bukkitWorld.getSpawnLocation());
        creativeWorld.addOnlinePlayer(player.getUniqueId());
        
        player.sendMessage("§aВы телепортированы в мир §f" + creativeWorld.getName() + " §7(ID: " + worldId + ")");
        
        return true;
    }
}
