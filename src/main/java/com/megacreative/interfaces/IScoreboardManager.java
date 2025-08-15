package com.megacreative.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Интерфейс для управления скорбордами
 */
public interface IScoreboardManager {
    
    /**
     * Инициализация менеджера скорбордов
     */
    void initialize();
    
    /**
     * Устанавливает скорборд игроку
     * @param player Игрок
     */
    void setScoreboard(Player player);
    
    /**
     * Обновляет скорборд игрока
     * @param player Игрок
     */
    void updateScoreboard(Player player);
    
    /**
     * Удаляет скорборд игрока
     * @param player Игрок
     */
    void removeScoreboard(Player player);
    
    /**
     * Обновляет скорборды всех игроков
     */
    void updateAllScoreboards();
    
    /**
     * Получает скорборд игрока
     * @param player Игрок
     * @return Скорборд игрока или null
     */
    Scoreboard getPlayerScoreboard(Player player);
    
    /**
     * Проверяет, имеет ли игрок скорборд
     * @param player Игрок
     * @return true если у игрока есть скорборд
     */
    boolean hasScoreboard(Player player);
    
    /**
     * Останавливает обновление скорбордов
     */
    void stopUpdating();
    
    /**
     * Возобновляет обновление скорбордов
     */
    void resumeUpdating();
    
    /**
     * Устанавливает интервал обновления
     * @param interval Интервал в тиках
     */
    void setUpdateInterval(int interval);
    
    /**
     * Получает интервал обновления
     * @return Интервал в тиках
     */
    int getUpdateInterval();
}
