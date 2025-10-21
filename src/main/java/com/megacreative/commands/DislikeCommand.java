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
 * Команда для добавления дизлайка миру
 * Позволяет игрокам оценивать миры отрицательно
 *
 * Command to dislike a world
 * Allows players to rate worlds negatively
 *
 * Befehl zum Disliken einer Welt
 * Ermöglicht es Spielern, Welten negativ zu bewerten
 * 
 * @author Андрій Будильников
 */
public class DislikeCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    
    /**
     * Инициализирует команду DislikeCommand
     * @param plugin основной плагин
     * @param worldManager менеджер миров
     *
     * Initializes the DislikeCommand
     * @param plugin main plugin
     * @param worldManager world manager
     *
     * Initialisiert den DislikeCommand
     * @param plugin Haupt-Plugin
     * @param worldManager Welt-Manager
     */
    public DislikeCommand(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }
    
    /**
     * Обрабатывает выполнение команды /dislike
     * @param sender отправитель команды
     * @param command команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles execution of the /dislike command
     * @param sender command sender
     * @param command command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des /dislike-Befehls
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
        
        // Добавляем дизлайк
        boolean added = currentWorld.addDislike(player.getUniqueId());
        
        if (added) {
            player.sendMessage("§c✗ Вы поставили дизлайк миру §f" + currentWorld.getName());
            // Уведомляем владельца мира, если он онлайн
            notifyOwner(currentWorld, player, "дизлайк");
        } else {
            player.sendMessage("§cВы уже поставили дизлайк этому миру!");
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