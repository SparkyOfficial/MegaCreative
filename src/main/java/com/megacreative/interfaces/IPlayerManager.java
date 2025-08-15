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
}
