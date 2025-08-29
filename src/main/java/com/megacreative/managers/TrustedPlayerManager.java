package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.ITrustedPlayerManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.TrustedPlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Менеджер для управления доверенными игроками
 * Отвечает за хранение и управление доверенными игроками в различных мирах
 */
public class TrustedPlayerManager implements ITrustedPlayerManager {
    
    private final MegaCreative plugin;
    private final Map<UUID, Map<UUID, TrustedPlayer>> worldTrustedPlayers = new HashMap<>();
    private final File trustedPlayersFile;
    private final FileConfiguration config;

    public TrustedPlayerManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.trustedPlayersFile = new File(plugin.getDataFolder(), "trusted_players.yml");
        this.config = YamlConfiguration.loadConfiguration(trustedPlayersFile);
        loadTrustedPlayers();
    }
    
    /**
     * Shuts down the TrustedPlayerManager and cleans up resources
     */
    @Override
    public void initialize() {
        loadTrustedPlayers();
        plugin.getLogger().info("TrustedPlayerManager initialized");
    }
    
    
    @Override
    public void saveTrustedPlayers() {
        try {
            // Clear existing data
            for (String key : config.getKeys(false)) {
                config.set(key, null);
            }
            
            // Save trusted players by world
            for (Map.Entry<UUID, Map<UUID, TrustedPlayer>> worldEntry : worldTrustedPlayers.entrySet()) {
                String worldKey = worldEntry.getKey().toString();
                ConfigurationSection worldSection = config.createSection(worldKey);
                
                for (Map.Entry<UUID, TrustedPlayer> playerEntry : worldEntry.getValue().entrySet()) {
                    String playerKey = playerEntry.getKey().toString();
                    TrustedPlayer trustedPlayer = playerEntry.getValue();
                    
                    ConfigurationSection playerSection = worldSection.createSection(playerKey);
                    playerSection.set("name", trustedPlayer.getPlayerName());
                    playerSection.set("type", trustedPlayer.getType().name());
                    playerSection.set("timestamp", trustedPlayer.getTimestamp());
                    playerSection.set("addedBy", trustedPlayer.getAddedBy() != null ? trustedPlayer.getAddedBy().toString() : null);
                }
            }
            
            // Save to file
            config.save(trustedPlayersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка при сохранении доверенных игроков: " + e.getMessage());
        }
    }
    
    @Override
    public void loadTrustedPlayers() {
        worldTrustedPlayers.clear();
        
        if (!trustedPlayersFile.exists()) {
            return;
        }
        
        try {
            // Load the configuration
            config.load(trustedPlayersFile);
            
            // Load trusted players by world
            for (String worldKey : config.getKeys(false)) {
                try {
                    UUID worldId = UUID.fromString(worldKey);
                    ConfigurationSection worldSection = config.getConfigurationSection(worldKey);
                    
                    if (worldSection != null) {
                        Map<UUID, TrustedPlayer> worldPlayers = new HashMap<>();
                        
                        for (String playerKey : worldSection.getKeys(false)) {
                            try {
                                UUID playerId = UUID.fromString(playerKey);
                                ConfigurationSection playerSection = worldSection.getConfigurationSection(playerKey);
                                
                                if (playerSection != null) {
                                    String name = playerSection.getString("name", "Unknown");
                                    TrustedPlayer.TrustedPlayerType type = TrustedPlayer.TrustedPlayerType.valueOf(playerSection.getString("type"));
                                    long timestamp = playerSection.getLong("timestamp", System.currentTimeMillis());
                                    String addedByStr = playerSection.getString("addedBy");
                                    UUID addedBy = addedByStr != null ? UUID.fromString(addedByStr) : null;
                                    
                                    worldPlayers.put(playerId, new TrustedPlayer(playerId, name, type, timestamp, addedBy));
                                }
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Неверный формат UUID игрока: " + playerKey);
                            }
                        }
                        
                        worldTrustedPlayers.put(worldId, worldPlayers);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неверный формат UUID мира: " + worldKey);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при загрузке доверенных игроков: " + e.getMessage());
        }
    }
    
    @Override
    public void removePlayerFromAllTrustedLists(UUID playerId) {
        if (playerId == null) {
            return;
        }
        
        boolean modified = false;
        
        for (Map<UUID, TrustedPlayer> worldPlayers : worldTrustedPlayers.values()) {
            if (worldPlayers != null && worldPlayers.remove(playerId) != null) {
                modified = true;
            }
        }
        
        if (modified) {
            saveTrustedPlayers();
        }
    }
    
    @Override
    public boolean canCodeInDevWorld(Player player) {
        // Ops can always code
        if (player.isOp()) {
            return true;
        }
        
        // Check if player is a trusted coder in any world
        for (Map<UUID, TrustedPlayer> worldPlayers : worldTrustedPlayers.values()) {
            TrustedPlayer trustedPlayer = worldPlayers.get(player.getUniqueId());
            if (trustedPlayer != null && trustedPlayer.getType() == TrustedPlayer.TrustedPlayerType.TRUSTED_CODER) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void addTrustedPlayer(CreativeWorld world, Player trustedPlayer, Player owner) {
        if (world == null || trustedPlayer == null || owner == null) {
            throw new IllegalArgumentException("World, trusted player and owner cannot be null");
        }
        
        UUID worldId = world.getWorldId();
        UUID playerId = trustedPlayer.getUniqueId();
        
        // Initialize world entry if it doesn't exist
        worldTrustedPlayers.computeIfAbsent(worldId, k -> new HashMap<>());
        
        // Check if player is already trusted
        if (worldTrustedPlayers.get(worldId).containsKey(playerId)) {
            return; // Already trusted
        }
        
        // Add the player as trusted for this world
        TrustedPlayer trusted = new TrustedPlayer(
            playerId,
            trustedPlayer.getName(),
            TrustedPlayer.TrustedPlayerType.TRUSTED_BUILDER, // Default to builder
            System.currentTimeMillis(),
            owner.getUniqueId()
        );
        
        worldTrustedPlayers.get(worldId).put(playerId, trusted);
        saveTrustedPlayers();
    }
    
    @Override
    public void removeTrustedPlayer(CreativeWorld world, Player trustedPlayer, Player owner) {
        if (world == null || trustedPlayer == null) {
            return;
        }
        
        UUID worldId = world.getWorldId();
        UUID playerId = trustedPlayer.getUniqueId();
        
        // Remove the player from the world's trusted players if they exist
        if (worldTrustedPlayers.containsKey(worldId)) {
            Map<UUID, TrustedPlayer> worldPlayers = worldTrustedPlayers.get(worldId);
            if (worldPlayers != null && worldPlayers.remove(playerId) != null) {
                saveTrustedPlayers();
            }
        }
    }
    
    @Override
    public boolean isTrustedPlayer(CreativeWorld world, Player player) {
        if (world == null || player == null) {
            return false;
        }
        
        UUID worldId = world.getWorldId();
        UUID playerId = player.getUniqueId();
        
        // Check if the world has any trusted players
        if (!worldTrustedPlayers.containsKey(worldId)) {
            return false;
        }
        
        // Get the world's trusted players map
        Map<UUID, TrustedPlayer> worldPlayers = worldTrustedPlayers.get(worldId);
        
        // Check if the player is in the trusted players map
        return worldPlayers != null && worldPlayers.containsKey(playerId);
    }
    
    @Override
    public List<TrustedPlayer> getTrustedPlayers(CreativeWorld world) {
        if (world == null) {
            return new ArrayList<>();
        }
        
        UUID worldId = world.getWorldId();
        
        // Return an empty list if the world has no trusted players
        if (!worldTrustedPlayers.containsKey(worldId)) {
            return new ArrayList<>();
        }
        
        Map<UUID, TrustedPlayer> worldPlayers = worldTrustedPlayers.get(worldId);
        return worldPlayers != null ? new ArrayList<>(worldPlayers.values()) : new ArrayList<>();
    }
    
    @Override
    public List<TrustedPlayer> getAllTrustedPlayers() {
        List<TrustedPlayer> allPlayers = new ArrayList<>();
        
        for (Map<UUID, TrustedPlayer> worldPlayers : worldTrustedPlayers.values()) {
            if (worldPlayers != null) {
                allPlayers.addAll(worldPlayers.values());
            }
        }
        
        return allPlayers;
    }
    
    @Override
    public List<TrustedPlayer> getTrustedBuilders() {
        List<TrustedPlayer> builders = new ArrayList<>();
        
        for (Map<UUID, TrustedPlayer> worldPlayers : worldTrustedPlayers.values()) {
            if (worldPlayers != null) {
                for (TrustedPlayer player : worldPlayers.values()) {
                    if (player != null && player.getType() == TrustedPlayer.TrustedPlayerType.TRUSTED_BUILDER) {
                        builders.add(player);
                    }
                }
            }
        }
        
        return builders;
    }
    
    @Override
    public List<TrustedPlayer> getTrustedCoders() {
        List<TrustedPlayer> coders = new ArrayList<>();
        
        for (Map<UUID, TrustedPlayer> worldPlayers : worldTrustedPlayers.values()) {
            if (worldPlayers != null) {
                for (TrustedPlayer player : worldPlayers.values()) {
                    if (player != null && player.getType() == TrustedPlayer.TrustedPlayerType.TRUSTED_CODER) {
                        coders.add(player);
                    }
                }
            }
        }
        
        return coders;
    }
    
    @Override
    public List<CreativeWorld> getTrustedWorlds(Player player) {
        List<CreativeWorld> trustedWorlds = new ArrayList<>();
        
        if (player == null) {
            return trustedWorlds;
        }
        
        UUID playerId = player.getUniqueId();
        
        for (Map.Entry<UUID, Map<UUID, TrustedPlayer>> entry : worldTrustedPlayers.entrySet()) {
            if (entry.getValue() != null && entry.getValue().containsKey(playerId)) {
                // Convert UUID to string for getWorld method
                CreativeWorld world = plugin.getWorldManager().getWorld(entry.getKey().toString());
                if (world != null) {
                    trustedWorlds.add(world);
                }
            }
        }
        
        return trustedWorlds;
    }
    
    @Override
    public void clearTrustedPlayers(CreativeWorld world, Player owner) {
        if (world == null || owner == null) {
            return;
        }
        
        UUID worldId = world.getWorldId();
        if (!worldTrustedPlayers.containsKey(worldId)) {
            return;
        }
        
        Map<UUID, TrustedPlayer> players = worldTrustedPlayers.get(worldId);
        if (players != null && !players.isEmpty()) {
            players.clear();
            saveTrustedPlayers();
        }
    }
    
    @Override
    public TrustedPlayer getTrustedPlayer(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        
        for (Map<UUID, TrustedPlayer> worldPlayers : worldTrustedPlayers.values()) {
            if (worldPlayers != null) {
                TrustedPlayer player = worldPlayers.get(playerId);
                if (player != null) {
                    return player;
                }
            }
        }
        return null;
    }
    
    @Override
    public int getTrustedPlayerCount(CreativeWorld world) {
        if (world == null) {
            return 0;
        }
        
        UUID worldId = world.getWorldId();
        if (!worldTrustedPlayers.containsKey(worldId)) {
            return 0;
        }
        
        Map<UUID, TrustedPlayer> worldPlayers = worldTrustedPlayers.get(worldId);
        return worldPlayers != null ? worldPlayers.size() : 0;
    }
    
    @Override
    public void shutdown() {
        try {
            saveTrustedPlayers();
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при сохранении доверенных игроков при выключении: " + e.getMessage());
        } finally {
            worldTrustedPlayers.clear();
        }
    }

    /**
     * Gets trusted builders with their data for a specific world
     * @param world The world to get trusted builders for
     * @return List of trusted builders in the specified world
     */
    public List<TrustedPlayer> getTrustedBuildersData(CreativeWorld world) {
        if (world == null) {
            return new ArrayList<>();
        }
        
        List<TrustedPlayer> builders = new ArrayList<>();
        UUID worldId = world.getWorldId();
        
        if (worldTrustedPlayers.containsKey(worldId)) {
            Map<UUID, TrustedPlayer> worldPlayers = worldTrustedPlayers.get(worldId);
            if (worldPlayers != null) {
                for (TrustedPlayer player : worldPlayers.values()) {
                    if (player != null && player.getType() == TrustedPlayer.TrustedPlayerType.TRUSTED_BUILDER) {
                        builders.add(player);
                    }
                }
            }
        }
        
        return builders;
    }
    
    /**
     * Gets trusted coders with their data for a specific world
     * @param world The world to get trusted coders for
     * @return List of trusted coders in the specified world
     */
    public List<TrustedPlayer> getTrustedCodersData(CreativeWorld world) {
        if (world == null) {
            return new ArrayList<>();
        }
        
        List<TrustedPlayer> coders = new ArrayList<>();
        UUID worldId = world.getWorldId();
        
        if (worldTrustedPlayers.containsKey(worldId)) {
            Map<UUID, TrustedPlayer> worldPlayers = worldTrustedPlayers.get(worldId);
            if (worldPlayers != null) {
                for (TrustedPlayer player : worldPlayers.values()) {
                    if (player != null && player.getType() == TrustedPlayer.TrustedPlayerType.TRUSTED_CODER) {
                        coders.add(player);
                    }
                }
            }
        }
        
        return coders;
    }
}