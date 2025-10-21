package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Команда для добавления лайка миру
 * Позволяет игрокам оценивать миры положительно
 *
 * Command to like a world
 * Allows players to rate worlds positively
 *
 * Befehl zum Liken einer Welt
 * Ermöglicht es Spielern, Welten positiv zu bewerten
 * 
 * @author Андрій Будильников
 */
public class LikeCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    
    /**
     * Инициализирует команду LikeCommand
     * @param plugin основной плагин
     * @param worldManager менеджер миров
     *
     * Initializes the LikeCommand
     * @param plugin main plugin
     * @param worldManager world manager
     *
     * Initialisiert den LikeCommand
     * @param plugin Haupt-Plugin
     * @param worldManager Welt-Manager
     */
    public LikeCommand(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }
    
    /**
     * Обрабатывает выполнение команды /like
     * @param sender отправитель команды
     * @param command команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles execution of the /like command
     * @param sender command sender
     * @param command command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des /like-Befehls
     * @param sender Befehlsabsender
     * @param command Befehl
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
        
        // Получаем текущий мир игрока
        CreativeWorld currentWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
        
        if (currentWorld == null) {
            player.sendMessage("§cВы не находитесь в творческом мире!");
            return true;
        }
        
        // Проверяем, что игрок не владелец мира
        if (currentWorld.isOwner(player)) {
            player.sendMessage("§cВы не можете оценивать свой собственный мир!");
            return true;
        }
        
        // Добавляем лайк
        boolean added = currentWorld.addLike(player.getUniqueId());
        
        if (added) {
            player.sendMessage("§a✓ Вы поставили лайк миру §f" + currentWorld.getName());
            // Уведомляем владельца мира, если он онлайн
            notifyOwner(currentWorld, player, "лайк");
        } else {
            player.sendMessage("§cВы уже поставили лайк этому миру!");
        }
        
        return true;
    }
    
    /**
     * Уведомляет владельца мира о новой оценке
     * @param world мир
     * @param player игрок, поставивший оценку
     * @param rating тип оценки
     *
     * Notifies the world owner about a new rating
     * @param world world
     * @param player player who rated
     * @param rating rating type
     *
     * Benachrichtigt den Weltbesitzer über eine neue Bewertung
     * @param world Welt
     * @param player Spieler, der bewertet hat
     * @param rating Bewertungstyp
     */
    private void notifyOwner(CreativeWorld world, Player player, String rating) {
        Player owner = plugin.getServer().getPlayer(world.getOwnerId());
        if (owner != null && owner.isOnline()) {
            owner.sendMessage("§eИгрок §f" + player.getName() + " §eпоставил " + rating + " вашему миру §f" + world.getName());
        }
    }
}