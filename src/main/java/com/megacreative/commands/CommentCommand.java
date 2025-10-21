package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldComment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Команда для добавления комментариев к миру
 * Позволяет игрокам оставлять отзывы о мирах
 *
 * Command to comment on a world
 * Allows players to leave feedback on worlds
 *
 * Befehl zum Kommentieren einer Welt
 * Ermöglicht es Spielern, Feedback zu Welten zu hinterlassen
 * 
 * @author Андрій Будильников
 */
public class CommentCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    
    /**
     * Инициализирует команду CommentCommand
     * @param plugin основной плагин
     * @param worldManager менеджер миров
     *
     * Initializes the CommentCommand
     * @param plugin main plugin
     * @param worldManager world manager
     *
     * Initialisiert den CommentCommand
     * @param plugin Haupt-Plugin
     * @param worldManager Welt-Manager
     */
    public CommentCommand(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }
    
    /**
     * Обрабатывает выполнение команды /comment
     * @param sender отправитель команды
     * @param command команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles execution of the /comment command
     * @param sender command sender
     * @param command command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des /comment-Befehls
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
        
        if (args.length == 0) {
            player.sendMessage("§cУкажите текст комментария!");
            player.sendMessage("§7Использование: §f/comment <текст>");
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
            player.sendMessage("§cВы не можете комментировать свой собственный мир!");
            return true;
        }
        
        // Создаем комментарий
        String commentText = String.join(" ", args);
        
        // Ограничиваем длину комментария
        if (commentText.length() > 100) {
            commentText = commentText.substring(0, 100) + "...";
        }
        
        WorldComment comment = new WorldComment(
            player.getUniqueId(),
            player.getName(),
            commentText
        );
        
        // Добавляем комментарий к миру
        currentWorld.addComment(comment);
        
        player.sendMessage("§a✓ Вы оставили комментарий к миру §f" + currentWorld.getName());
        
        // Уведомляем владельца мира, если он онлайн
        notifyOwner(currentWorld, player, commentText);
        
        return true;
    }
    
    /**
     * Уведомляет владельца мира о новом комментарии
     * @param world мир
     * @param player игрок, оставивший комментарий
     * @param comment текст комментария
     *
     * Notifies the world owner about a new comment
     * @param world world
     * @param player player who commented
     * @param comment comment text
     *
     * Benachrichtigt den Weltbesitzer über einen neuen Kommentar
     * @param world Welt
     * @param player Spieler, der kommentiert hat
     * @param comment Kommentartext
     */
    private void notifyOwner(CreativeWorld world, Player player, String comment) {
        Player owner = plugin.getServer().getPlayer(world.getOwnerId());
        if (owner != null && owner.isOnline()) {
            owner.sendMessage("§eИгрок §f" + player.getName() + " §eоставил комментарий к вашему миру §f" + world.getName() + "§e:");
            owner.sendMessage("§7\"" + comment + "\"");
        }
    }
}