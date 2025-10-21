package com.megacreative.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Менеджер списка TAB для отображения информации об игроке
 *
 * TAB list manager for displaying player information
 *
 * TAB-Listen-Manager zur Anzeige von Spielerinformationen
 */
public class TabListManager {

    private static final String HEADER =
            ChatColor.AQUA.toString() + ChatColor.BOLD + " __  __      _        ______                _   \n" +
            ChatColor.AQUA.toString() + ChatColor.BOLD + "|  \\/  |    | |      |  ____|              | |  \n" +
            ChatColor.AQUA.toString() + ChatColor.BOLD + "| \\  / | ___| |_ __ _| |__ _ __ _   _ _ __ | |_ \n" +
            ChatColor.AQUA.toString() + ChatColor.BOLD + "| |\\/| |/ _ \\ __/ _` |  __| '__| | | | '_ \\| __|\n" +
            ChatColor.AQUA.toString() + ChatColor.BOLD + "| |  | |  __/ || (_| | |  | |  | |_| | |_) | |_ \n" +
            ChatColor.AQUA.toString() + ChatColor.BOLD + "|_|  |_|\\___|\\__\\__,_|_|  |_|   \\__, | .__/ \\__|\n" +
            "                               " + ChatColor.GRAY + "__/ | |      \n" +
            "                               " + ChatColor.GRAY + "|___/|_|      ";

    /**
     * Устанавливает TAB для игрока с нашим лого и динамическим футером
     * @param player Игрок, для которого устанавливается TAB
     *
     * Sets TAB for player with our logo and dynamic footer
     * @param player Player for whom TAB is set
     *
     * Stellt TAB für Spieler mit unserem Logo und dynamischem Footer ein
     * @param player Spieler, für den TAB eingestellt wird
     */
    public static void setTabList(Player player) {
        StringBuilder footerBuilder = new StringBuilder();
        footerBuilder.append("\n");
        footerBuilder.append(ChatColor.GRAY).append("Игроков онлайн: ").append(ChatColor.GREEN).append(Bukkit.getOnlinePlayers().size());
        footerBuilder.append(ChatColor.DARK_GRAY).append(" | ");
        footerBuilder.append(ChatColor.GRAY).append("Ваш пинг: ").append(ChatColor.GREEN).append(player.getPing()).append("ms");
        footerBuilder.append("\n").append(ChatColor.AQUA).append("Приятной игры!");
        // Static analysis flagged this as redundant, but toString() is needed to get the string content
        // This is a false positive - we need the actual string value
        // Removed unused variable assignment to fix Qodana warning
        // String footerText = footerBuilder.toString();

        
        
        
        Component headerComponent = Component.text(HEADER);
        Component footerComponent = Component.text(footerBuilder.toString());

        player.sendPlayerListHeaderAndFooter(headerComponent, footerComponent);
    }

    /**
     * Очищает TAB для игрока
     * @param player Игрок, для которого очищается TAB
     *
     * Clears TAB for player
     * @param player Player for whom TAB is cleared
     *
     * Löscht TAB für Spieler
     * @param player Spieler, für den TAB gelöscht wird
     */
    public static void clearTabList(Player player) {
        player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
    }
}