package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.models.TrustedPlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Менеджер для управления доверенными игроками
 */
public class TrustedPlayerManager {
    
    private final MegaCreative plugin;
    private final Map<UUID, TrustedPlayer> trustedPlayers = new ConcurrentHashMap<>();
    private final File trustedPlayersFile;
    private final FileConfiguration config;

    public TrustedPlayerManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.trustedPlayersFile = new File(plugin.getDataFolder(), "trusted_players.yml");
        this.config = YamlConfiguration.loadConfiguration(trustedPlayersFile);
        loadTrustedPlayers();
    }

    /**
     * Добавляет игрока в список доверенных
     */
    public boolean addTrustedPlayer(Player player, TrustedPlayer.TrustedPlayerType type, String addedBy) {
        TrustedPlayer trustedPlayer = new TrustedPlayer(player.getUniqueId(), player.getName(), type, addedBy);
        trustedPlayers.put(player.getUniqueId(), trustedPlayer);
        
        plugin.getLogger().info("Добавлен доверенный игрок: " + player.getName() + " (" + type.getDisplayName() + ")");
        return true;
    }

    /**
     * Добавляет игрока в список доверенных по UUID
     */
    public boolean addTrustedPlayer(UUID playerId, String playerName, TrustedPlayer.TrustedPlayerType type, String addedBy) {
        TrustedPlayer trustedPlayer = new TrustedPlayer(playerId, playerName, type, addedBy);
        trustedPlayers.put(playerId, trustedPlayer);
        
        plugin.getLogger().info("Добавлен доверенный игрок: " + playerName + " (" + type.getDisplayName() + ")");
        return true;
    }

    /**
     * Удаляет игрока из списка доверенных
     */
    public boolean removeTrustedPlayer(UUID playerId) {
        TrustedPlayer removed = trustedPlayers.remove(playerId);
        if (removed != null) {
            plugin.getLogger().info("Удален доверенный игрок: " + removed.getPlayerName());
            return true;
        }
        return false;
    }

    /**
     * Проверяет, является ли игрок доверенным строителем
     */
    public boolean isTrustedBuilder(Player player) {
        TrustedPlayer trustedPlayer = trustedPlayers.get(player.getUniqueId());
        return trustedPlayer != null && trustedPlayer.getType() == TrustedPlayer.TrustedPlayerType.TRUSTED_BUILDER;
    }

    /**
     * Проверяет, является ли игрок доверенным программистом
     */
    public boolean isTrustedCoder(Player player) {
        TrustedPlayer trustedPlayer = trustedPlayers.get(player.getUniqueId());
        return trustedPlayer != null && trustedPlayer.getType() == TrustedPlayer.TrustedPlayerType.TRUSTED_CODER;
    }

    /**
     * Проверяет, является ли игрок доверенным (любого типа)
     */
    public boolean isTrustedPlayer(Player player) {
        return trustedPlayers.containsKey(player.getUniqueId());
    }

    /**
     * Получает тип доверенного игрока
     */
    public TrustedPlayer.TrustedPlayerType getTrustedPlayerType(Player player) {
        TrustedPlayer trustedPlayer = trustedPlayers.get(player.getUniqueId());
        return trustedPlayer != null ? trustedPlayer.getType() : null;
    }

    /**
     * Получает список всех доверенных игроков
     */
    public List<TrustedPlayer> getAllTrustedPlayers() {
        return new ArrayList<>(trustedPlayers.values());
    }

    /**
     * Получает список доверенных строителей
     */
    public List<TrustedPlayer> getTrustedBuilders() {
        return trustedPlayers.values().stream()
                .filter(tp -> tp.getType() == TrustedPlayer.TrustedPlayerType.TRUSTED_BUILDER)
                .collect(Collectors.toList());
    }

    /**
     * Получает список доверенных программистов
     */
    public List<TrustedPlayer> getTrustedCoders() {
        return trustedPlayers.values().stream()
                .filter(tp -> tp.getType() == TrustedPlayer.TrustedPlayerType.TRUSTED_CODER)
                .collect(Collectors.toList());
    }

    /**
     * Загружает доверенных игроков из конфигурации
     */
    private void loadTrustedPlayers() {
        ConfigurationSection section = config.getConfigurationSection("trusted_players");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(key);
                ConfigurationSection playerSection = section.getConfigurationSection(key);
                
                if (playerSection != null) {
                    String playerName = playerSection.getString("name", "Unknown");
                    String typeStr = playerSection.getString("type", "TRUSTED_BUILDER");
                    long addedAt = playerSection.getLong("added_at", System.currentTimeMillis());
                    String addedBy = playerSection.getString("added_by", "Unknown");

                    TrustedPlayer.TrustedPlayerType type = TrustedPlayer.TrustedPlayerType.valueOf(typeStr);
                    TrustedPlayer trustedPlayer = new TrustedPlayer(playerId, playerName, type, addedAt, addedBy);
                    trustedPlayers.put(playerId, trustedPlayer);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Неверный UUID в конфигурации доверенных игроков: " + key);
            }
        }
        
        plugin.getLogger().info("Загружено доверенных игроков: " + trustedPlayers.size());
    }

    /**
     * Сохраняет доверенных игроков в конфигурацию
     */
    public void save() {
        config.set("trusted_players", null); // Очищаем секцию
        
        ConfigurationSection section = config.createSection("trusted_players");
        
        for (TrustedPlayer trustedPlayer : trustedPlayers.values()) {
            String key = trustedPlayer.getPlayerId().toString();
            ConfigurationSection playerSection = section.createSection(key);
            
            playerSection.set("name", trustedPlayer.getPlayerName());
            playerSection.set("type", trustedPlayer.getType().name());
            playerSection.set("added_at", trustedPlayer.getAddedAt());
            playerSection.set("added_by", trustedPlayer.getAddedBy());
        }
        
        try {
            config.save(trustedPlayersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить доверенных игроков: " + e.getMessage());
        }
    }

    /**
     * Получает информацию о доверенном игроке
     */
    public TrustedPlayer getTrustedPlayer(UUID playerId) {
        return trustedPlayers.get(playerId);
    }

    /**
     * Проверяет, может ли игрок строить в мире разработки
     */
    public boolean canBuildInDevWorld(Player player) {
        // Операторы всегда могут строить
        if (player.isOp()) return true;
        
        // Проверяем доверенных строителей
        return isTrustedBuilder(player);
    }

    /**
     * Проверяет, может ли игрок программировать в мире разработки
     */
    public boolean canCodeInDevWorld(Player player) {
        // Операторы всегда могут программировать
        if (player.isOp()) return true;
        
        // Проверяем доверенных программистов
        return isTrustedCoder(player);
    }
} 