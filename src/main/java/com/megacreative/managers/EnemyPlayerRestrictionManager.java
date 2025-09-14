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

public class EnemyPlayerRestrictionManager implements Listener {
    
    private final MegaCreative plugin;
    private final Logger logger;
    private final Set<String> enemyPlayers;
    private final Set<String> restrictedPlayers;
    
    public EnemyPlayerRestrictionManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.enemyPlayers = new HashSet<>();
        this.restrictedPlayers = new HashSet<>();
        loadConfig();
    }
    
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
    
    public boolean isEnemyPlayer(String playerName) {
        return enemyPlayers.contains(playerName);
    }
    
    public boolean isRestrictedPlayer(String playerName) {
        return restrictedPlayers.contains(playerName);
    }
    
    public void addEnemyPlayer(String playerName) {
        enemyPlayers.add(playerName);
        addToAllWorldBlacklists(playerName);
    }
    
    public void removeEnemyPlayer(String playerName) {
        enemyPlayers.remove(playerName);
        removeFromAllWorldBlacklists(playerName);
    }
    
    private void addToAllWorldBlacklists(String playerName) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getName().equals(playerName)) {
                UUID playerId = player.getUniqueId();
                for (CreativeWorld world : plugin.getWorldManager().getCreativeWorlds()) {
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
    
    private void removeFromAllWorldBlacklists(String playerName) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getName().equals(playerName)) {
                UUID playerId = player.getUniqueId();
                for (CreativeWorld world : plugin.getWorldManager().getCreativeWorlds()) {
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
    
    private void applyRestrictions(Player player) {
        if (isRestrictedPlayer(player.getName())) {
            for (CreativeWorld world : plugin.getWorldManager().getCreativeWorlds()) {
                WorldPermissions permissions = world.getPermissions();
                if (permissions != null) {
                    permissions.setPlayerPermission(player.getUniqueId(), WorldPermissions.PermissionLevel.VISITOR);
                    logger.info("Applied restrictions for player " + player.getName() + " in world " + world.getName());
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (isEnemyPlayer(player.getName())) {
            UUID playerId = player.getUniqueId();
            for (CreativeWorld world : plugin.getWorldManager().getCreativeWorlds()) {
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
    
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        applyRestrictions(player);
    }
    
    public Set<String> getEnemyPlayers() {
        return new HashSet<>(enemyPlayers);
    }
    
    public Set<String> getRestrictedPlayers() {
        return new HashSet<>(restrictedPlayers);
    }
}