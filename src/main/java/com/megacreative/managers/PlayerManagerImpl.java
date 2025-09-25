package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IPlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManagerImpl implements IPlayerManager {
    
    private final MegaCreative plugin;
    private final Map<UUID, Set<String>> playerFavorites;
    
    // Player mode manager for DEV/PLAY mode system
    private final PlayerModeManager playerModeManager = new PlayerModeManager();
    
    // 🎆 ENHANCED: World tracking for dual world architecture
    private final Map<UUID, PlayerWorldSession> playerSessions; // Current sessions
    private final Map<String, Map<UUID, String>> worldPlayerModes; // World -> Player -> Mode
    private final Map<String, WorldStatistics> worldStats; // World analytics
    
    // Session tracking class
    private static class PlayerWorldSession {
        private final String worldId;
        private final String mode;
        private final long entryTime;
        
        public PlayerWorldSession(String worldId, String mode) {
            this.worldId = worldId;
            this.mode = mode;
            this.entryTime = System.currentTimeMillis();
        }
        
        public String getWorldId() { return worldId; }
        public String getMode() { return mode; }
        public long getEntryTime() { return entryTime; }
        public long getSessionTime() { return System.currentTimeMillis() - entryTime; }
    }
    
    // World statistics class
    private static class WorldStatistics {
        private final Set<UUID> uniqueVisitors = new HashSet<>();
        private long totalTimeSpent = 0;
        private int totalSessions = 0;
        private final Map<String, Integer> modeSessions = new HashMap<>();
        
        public void recordSession(UUID playerId, String mode, long duration) {
            uniqueVisitors.add(playerId);
            totalTimeSpent += duration;
            totalSessions++;
            modeSessions.merge(mode, 1, Integer::sum);
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("uniqueVisitors", uniqueVisitors.size());
            stats.put("totalTimeSpent", totalTimeSpent);
            stats.put("totalSessions", totalSessions);
            stats.put("modeSessions", new HashMap<>(modeSessions));
            stats.put("averageSessionTime", totalSessions > 0 ? totalTimeSpent / totalSessions : 0);
            return stats;
        }
    }
    
    public PlayerManagerImpl(MegaCreative plugin) {
        this.plugin = plugin;
        this.playerFavorites = new HashMap<>();
        
        // 🎆 ENHANCED: Initialize world tracking collections
        this.playerSessions = new ConcurrentHashMap<>();
        this.worldPlayerModes = new ConcurrentHashMap<>();
        this.worldStats = new ConcurrentHashMap<>();
    }
    
    @Override
    public void initialize() {
        // Инициализация менеджера игроков
    }
    
    @Override
    public void registerPlayer(Player player) {
        // Register player in system
    }
    
    @Override
    public void unregisterPlayer(Player player) {
        // Remove player from system
        // Clear player mode when they leave
        playerModeManager.clearMode(player);
    }
    
    @Override
    public Map<String, Object> getPlayerData(UUID playerId) {
        // Получение данных игрока
        return new HashMap<>();
    }
    
    @Override
    public void setPlayerData(UUID playerId, Map<String, Object> data) {
        // Установка данных игрока
    }
    
    @Override
    public boolean isPlayerRegistered(UUID playerId) {
        // Проверка регистрации игрока
        return true;
    }
    
    @Override
    public int getPlayerCount() {
        // Количество зарегистрированных игроков
        return playerFavorites.size();
    }
    
    @Override
    public void saveAllPlayerData() {
        // Сохранение данных всех игроков
    }
    
    @Override
    public void loadAllPlayerData() {
        // Загрузка данных всех игроков
    }
    
    @Override
    public void clearPlayerData(UUID playerId) {
        // Очистка данных игрока
        playerFavorites.remove(playerId);
    }
    
    @Override
    public void giveStarterItems(Player player) {
        player.getInventory().clear();
        
        // Алмаз (Мои миры)
        ItemStack myWorldsItem = new ItemStack(Material.DIAMOND);
        ItemMeta myWorldsMeta = myWorldsItem.getItemMeta();
        myWorldsMeta.setDisplayName("§b§lМои миры");
        myWorldsMeta.setLore(Arrays.asList(
            "§7Управляйте своими мирами",
            "§7Создавайте новые миры",
            "§e▶ Нажмите, чтобы открыть"
        ));
        myWorldsItem.setItemMeta(myWorldsMeta);
        
        // Компас (Браузер миров)
        ItemStack browserItem = new ItemStack(Material.COMPASS);
        ItemMeta browserMeta = browserItem.getItemMeta();
        browserMeta.setDisplayName("§a§lБраузер миров");
        browserMeta.setLore(Arrays.asList(
            "§7Исследуйте миры других игроков",
            "§7Ставьте лайки и комментарии",
            "§e▶ Нажмите, чтобы открыть"
        ));
        browserItem.setItemMeta(browserMeta);
        
        player.getInventory().setItem(0, myWorldsItem);
        player.getInventory().setItem(1, browserItem);
    }
    
    public void addToFavorites(UUID playerId, String worldId) {
        playerFavorites.computeIfAbsent(playerId, k -> new HashSet<>()).add(worldId);
    }
    
    public void removeFromFavorites(UUID playerId, String worldId) {
        Set<String> favorites = playerFavorites.get(playerId);
        if (favorites != null) {
            favorites.remove(worldId);
        }
    }
    
    @Override
    public boolean isFavorite(UUID playerId, String worldId) {
        Set<String> favorites = playerFavorites.get(playerId);
        return favorites != null && favorites.contains(worldId);
    }
    
    public Set<String> getFavorites(UUID playerId) {
        return playerFavorites.getOrDefault(playerId, new HashSet<>());
    }
    
    @Override
    public void shutdown() {
        // 🎆 ENHANCED: Process final sessions before shutdown
        for (Map.Entry<UUID, PlayerWorldSession> entry : playerSessions.entrySet()) {
            PlayerWorldSession session = entry.getValue();
            recordSessionEnd(entry.getKey(), session);
        }
        
        // Clear player favorites to free memory
        playerFavorites.clear();
        
        // 🎆 ENHANCED: Clear tracking data
        playerSessions.clear();
        worldPlayerModes.clear();
        worldStats.clear();
        
        // Any other cleanup needed for player data
    }
    
    // 🎆 ENHANCED: World tracking implementation
    
    @Override
    public void trackPlayerWorldEntry(Player player, String worldId, String mode) {
        UUID playerId = player.getUniqueId();
        
        // End previous session if exists
        PlayerWorldSession previousSession = playerSessions.get(playerId);
        if (previousSession != null) {
            recordSessionEnd(playerId, previousSession);
        }
        
        // Start new session
        PlayerWorldSession newSession = new PlayerWorldSession(worldId, mode);
        playerSessions.put(playerId, newSession);
        
        // Track in world-player mapping
        worldPlayerModes.computeIfAbsent(worldId, k -> new ConcurrentHashMap<>())
                       .put(playerId, mode);
        
        plugin.getLogger().info("🏠 Player " + player.getName() + " entered world " + worldId + " in " + mode + " mode");
    }
    
    @Override
    public void trackPlayerWorldExit(Player player, String worldId) {
        UUID playerId = player.getUniqueId();
        
        // End current session
        PlayerWorldSession session = playerSessions.remove(playerId);
        if (session != null && session.getWorldId().equals(worldId)) {
            recordSessionEnd(playerId, session);
        }
        
        // Remove from world-player mapping
        Map<UUID, String> worldPlayers = worldPlayerModes.get(worldId);
        if (worldPlayers != null) {
            worldPlayers.remove(playerId);
            if (worldPlayers.isEmpty()) {
                worldPlayerModes.remove(worldId);
            }
        }
        
        plugin.getLogger().info("🏠 Player " + player.getName() + " exited world " + worldId);
    }
    
    @Override
    public Map<String, String> getCurrentPlayerLocation(UUID playerId) {
        PlayerWorldSession session = playerSessions.get(playerId);
        if (session == null) {
            return null;
        }
        
        Map<String, String> location = new HashMap<>();
        location.put("worldId", session.getWorldId());
        location.put("mode", session.getMode());
        location.put("sessionTime", String.valueOf(session.getSessionTime()));
        return location;
    }
    
    @Override
    public Map<UUID, String> getPlayersInWorld(String worldId) {
        Map<UUID, String> worldPlayers = worldPlayerModes.get(worldId);
        return worldPlayers != null ? new HashMap<>(worldPlayers) : new HashMap<>();
    }
    
    @Override
    public Map<String, Object> getWorldStatistics(String worldId) {
        WorldStatistics stats = worldStats.get(worldId);
        return stats != null ? stats.toMap() : new HashMap<>();
    }
    
    @Override
    public long getPlayerSessionTime(UUID playerId) {
        PlayerWorldSession session = playerSessions.get(playerId);
        return session != null ? session.getSessionTime() : 0;
    }
    
    /**
     * Records the end of a player session for statistics
     */
    private void recordSessionEnd(UUID playerId, PlayerWorldSession session) {
        String worldId = session.getWorldId();
        String mode = session.getMode();
        long duration = session.getSessionTime();
        
        // Update world statistics
        worldStats.computeIfAbsent(worldId, k -> new WorldStatistics())
                 .recordSession(playerId, mode, duration);
        
        plugin.getLogger().info("📈 Session recorded: Player " + playerId + " spent " + 
                              (duration / 1000) + "s in world " + worldId + " (" + mode + " mode)");
    }
    
    /**
     * Gets the player mode manager
     * @return Player mode manager instance
     */
    public PlayerModeManager getPlayerModeManager() {
        return playerModeManager;
    }
}
