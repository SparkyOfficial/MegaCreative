package com.megacreative.interfaces;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Интерфейс для управления игроками
 */
public interface IPlayerManager {
    
    /**
     * Инициализация менеджера игроков
     */
    void initialize();
    
    /**
     * Регистрирует игрока в системе
     * @param player Игрок для регистрации
     */
    void registerPlayer(Player player);
    
    /**
     * Удаляет игрока из системы
     * @param player Игрок для удаления
     */
    void unregisterPlayer(Player player);
    
    /**
     * Получает данные игрока
     * @param playerId UUID игрока
     * @return Данные игрока или null, если не найден
     */
    Map<String, Object> getPlayerData(UUID playerId);
    
    /**
     * Завершает работу менеджера игроков
     */
    void shutdown();
    
    /**
     * Устанавливает данные игрока
     * @param playerId UUID игрока
     * @param data Данные игрока
     */
    void setPlayerData(UUID playerId, Map<String, Object> data);
    
    /**
     * Проверяет, зарегистрирован ли игрок
     * @param playerId UUID игрока
     * @return true если игрок зарегистрирован
     */
    boolean isPlayerRegistered(UUID playerId);
    
    /**
     * Получает количество зарегистрированных игроков
     * @return Количество игроков
     */
    int getPlayerCount();
    
    /**
     * Сохраняет данные всех игроков
     */
    void saveAllPlayerData();
    
    /**
     * Загружает данные всех игроков
     */
    void loadAllPlayerData();
    
    /**
     * Очищает данные игрока
     * @param playerId UUID игрока
     */
    void clearPlayerData(UUID playerId);
    
    /**
     * Выдает стартовые предметы игроку
     * @param player Игрок
     */
    void giveStarterItems(Player player);
    
    /**
     * Проверяет, является ли мир избранным для игрока
     * @param playerId ID игрока
     * @param worldId ID мира
     * @return true если мир в избранном
     */
    boolean isFavorite(UUID playerId, String worldId);
    
    // 🎆 ENHANCED: World tracking methods for dual world architecture
    
    /**
     * Tracks player entry into a world and mode
     * @param player Player entering world
     * @param worldId World ID
     * @param mode World mode (DEV, PLAY, etc.)
     */
    void trackPlayerWorldEntry(Player player, String worldId, String mode);
    
    /**
     * Tracks player exit from a world
     * @param player Player leaving world
     * @param worldId World ID
     */
    void trackPlayerWorldExit(Player player, String worldId);
    
    /**
     * Gets the current world and mode for a player
     * @param playerId Player UUID
     * @return Map with worldId and mode, or null if not tracked
     */
    Map<String, String> getCurrentPlayerLocation(UUID playerId);
    
    /**
     * Gets all players currently in a specific world
     * @param worldId World ID
     * @return Map of player UUIDs to their modes
     */
    Map<UUID, String> getPlayersInWorld(String worldId);
    
    /**
     * Gets world statistics for analytics
     * @param worldId World ID
     * @return Statistics including unique players, time spent, etc.
     */
    Map<String, Object> getWorldStatistics(String worldId);
    
    /**
     * Gets player session time in current world
     * @param playerId Player UUID
     * @return Session time in milliseconds, or 0 if not in a world
     */
    long getPlayerSessionTime(UUID playerId);
}
