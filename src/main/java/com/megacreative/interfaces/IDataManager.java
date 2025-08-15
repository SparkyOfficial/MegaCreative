package com.megacreative.interfaces;

import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Интерфейс для управления данными
 */
public interface IDataManager {
    
    /**
     * Сохраняет данные игрока
     * @param player Игрок
     */
    void savePlayerData(Player player);
    
    /**
     * Загружает данные игрока
     * @param player Игрок
     */
    void loadPlayerData(Player player);
    
    /**
     * Сохраняет все данные
     */
    void saveAllData();
    
    /**
     * Загружает все данные
     */
    void loadAllData();
    
    /**
     * Получает данные игрока
     * @param playerId UUID игрока
     * @return Данные игрока
     */
    Map<String, Object> getPlayerData(String playerId);
    
    /**
     * Устанавливает данные игрока
     * @param playerId UUID игрока
     * @param data Данные игрока
     */
    void setPlayerData(String playerId, Map<String, Object> data);
    
    /**
     * Удаляет данные игрока
     * @param playerId UUID игрока
     */
    void removePlayerData(String playerId);
    
    /**
     * Проверяет, существуют ли данные игрока
     * @param playerId UUID игрока
     * @return true если данные существуют
     */
    boolean hasPlayerData(String playerId);
}
