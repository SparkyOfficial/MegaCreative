package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldPermissions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.List;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Менеджер ограничений для враждебных игроков
 *
 * Enemy player restriction manager
 *
 * Feindlicher Spieler-Einschränkungs-Manager
 */
public class EnemyPlayerRestrictionManager implements Listener {
    
    private final MegaCreative plugin;
    private final Logger logger;
    private final Set<String> enemyPlayers;
    private final Set<String> restrictedPlayers;
    
    /**
     * Конструктор менеджера ограничений для враждебных игроков
     * @param plugin основной плагин
     *
     * Constructor for enemy player restriction manager
     * @param plugin main plugin
     *
     * Konstruktor für den Feind-Spieler-Einschränkungs-Manager
     * @param plugin Haupt-Plugin
     */
    public EnemyPlayerRestrictionManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.enemyPlayers = new HashSet<>();
        this.restrictedPlayers = new HashSet<>();
        loadConfig();
    }
    
    /**
     * Загружает конфигурацию враждебных игроков
     *
     * Loads enemy player configuration
     *
     * Lädt die Feind-Spieler-Konfiguration
     */
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        
        List<String> enemyList = config.getStringList("security.enemy_players");
        enemyPlayers.clear();
        enemyPlayers.addAll(enemyList);
        logger.info("Loaded " + enemyList.size() + " enemy players");
        
        List<String> restrictedList = config.getStringList("security.restricted_players");
        restrictedPlayers.clear();
        restrictedPlayers.addAll(restrictedList);
        logger.info("Loaded " + restrictedList.size() + " restricted players");
    }
    
    /**
     * Проверяет, является ли игрок враждебным
     * @param playerName имя игрока
     * @return true если игрок враждебный
     *
     * Checks if player is enemy
     * @param playerName player name
     * @return true if player is enemy
     *
     * Prüft, ob der Spieler ein Feind ist
     * @param playerName Spielername
     * @return true, wenn der Spieler ein Feind ist
     */
    public boolean isEnemyPlayer(String playerName) {
        return enemyPlayers.contains(playerName);
    }
    
    /**
     * Проверяет, является ли игрок ограниченным
     * @param playerName имя игрока
     * @return true если игрок ограничен
     *
     * Checks if player is restricted
     * @param playerName player name
     * @return true if player is restricted
     *
     * Prüft, ob der Spieler eingeschränkt ist
     * @param playerName Spielername
     * @return true, wenn der Spieler eingeschränkt ist
     */
    public boolean isRestrictedPlayer(String playerName) {
        return restrictedPlayers.contains(playerName);
    }
    
    /**
     * Добавляет игрока в список враждебных
     * @param playerName имя игрока
     *
     * Adds player to enemy list
     * @param playerName player name
     *
     * Fügt Spieler zur Feind-Liste hinzu
     * @param playerName Spielername
     */
    public void addEnemyPlayer(String playerName) {
        enemyPlayers.add(playerName);
        addToAllWorldBlacklists(playerName);
    }
    
    /**
     * Удаляет игрока из списка враждебных
     * @param playerName имя игрока
     *
     * Removes player from enemy list
     * @param playerName player name
     *
     * Entfernt Spieler von der Feind-Liste
     * @param playerName Spielername
     */
    public void removeEnemyPlayer(String playerName) {
        enemyPlayers.remove(playerName);
        removeFromAllWorldBlacklists(playerName);
    }
    
    /**
     * Добавляет игрока в черный список всех миров
     * @param playerName имя игрока
     *
     * Adds player to blacklist of all worlds
     * @param playerName player name
     *
     * Fügt Spieler zur Schwarzen Liste aller Welten hinzu
     * @param playerName Spielername
     */
    private void addToAllWorldBlacklists(String playerName) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getName().equals(playerName)) {
                UUID playerId = player.getUniqueId();
                for (CreativeWorld world : plugin.getServiceRegistry().getWorldManager().getCreativeWorlds()) {
                    WorldPermissions permissions = world.getPermissions();
                    if (permissions != null) {
                        permissions.addToBlacklist(playerId);
                        logger.info("Added " + playerName + " to blacklist of world " + world.getName());
                    }
                }
                break;
            }
        }
    }
    
    /**
     * Удаляет игрока из черного списка всех миров
     * @param playerName имя игрока
     *
     * Removes player from blacklist of all worlds
     * @param playerName player name
     *
     * Entfernt Spieler von der Schwarzen Liste aller Welten
     * @param playerName Spielername
     */
    private void removeFromAllWorldBlacklists(String playerName) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getName().equals(playerName)) {
                UUID playerId = player.getUniqueId();
                for (CreativeWorld world : plugin.getServiceRegistry().getWorldManager().getCreativeWorlds()) {
                    WorldPermissions permissions = world.getPermissions();
                    if (permissions != null) {
                        permissions.removeFromBlacklist(playerId);
                        logger.info("Removed " + playerName + " from blacklist of world " + world.getName());
                    }
                }
                break;
            }
        }
    }
    
    /**
     * Применяет ограничения к игроку
     * @param player игрок
     *
     * Applies restrictions to player
     * @param player player
     *
     * Wendet Einschränkungen auf den Spieler an
     * @param player Spieler
     */
    private void applyRestrictions(Player player) {
        if (isRestrictedPlayer(player.getName())) {
            for (CreativeWorld world : plugin.getServiceRegistry().getWorldManager().getCreativeWorlds()) {
                WorldPermissions permissions = world.getPermissions();
                if (permissions != null) {
                    permissions.setPlayerPermission(player.getUniqueId(), WorldPermissions.PermissionLevel.VISITOR);
                    logger.info("Applied restrictions for player " + player.getName() + " in world " + world.getName());
                }
            }
        }
    }
    
    /**
     * Обрабатывает событие входа игрока на сервер
     * @param event событие входа игрока
     *
     * Handles player join event
     * @param event player join event
     *
     * Verarbeitet das Spieler-Beitritts-Ereignis
     * @param event Spieler-Beitritts-Ereignis
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (isEnemyPlayer(player.getName())) {
            UUID playerId = player.getUniqueId();
            for (CreativeWorld world : plugin.getServiceRegistry().getWorldManager().getCreativeWorlds()) {
                WorldPermissions permissions = world.getPermissions();
                if (permissions != null) {
                    permissions.addToBlacklist(playerId);
                }
            }
            logger.info("Enemy player " + player.getName() + " added to all world blacklists");
            player.kickPlayer("§cYou have been blacklisted from this server!");
        }
        
        applyRestrictions(player);
    }
    
    /**
     * Обрабатывает событие смены мира игроком
     * @param event событие смены мира
     *
     * Handles player world change event
     * @param event player world change event
     *
     * Verarbeitet das Spieler-Weltwechsel-Ereignis
     * @param event Spieler-Weltwechsel-Ereignis
     */
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        applyRestrictions(player);
    }
    
    /**
     * Получает список враждebных игроков
     * @return список враждebных игроков
     *
     * Gets enemy players list
     * @return enemy players list
     *
     * Gibt die Liste der Feind-Spieler zurück
     * @return Liste der Feind-Spieler
     */
    public Set<String> getEnemyPlayers() {
        return new HashSet<>(enemyPlayers);
    }
    
    /**
     * Получает список ограниченных игроков
     * @return список ограниченных игроков
     *
     * Gets restricted players list
     * @return restricted players list
     *
     * Gibt die Liste der eingeschränkten Spieler zurück
     * @return Liste der eingeschränkten Spieler
     */
    public Set<String> getRestrictedPlayers() {
        return new HashSet<>(restrictedPlayers);
    }
}