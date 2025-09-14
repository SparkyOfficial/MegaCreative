package com.megacreative.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Интерфейс для управления скорбордами
 * Управление отображением информации для игроков
 * Обновление и синхронизация скорбордов
 *
 * Interface for scoreboard management
 * Management of information display for players
 * Updating and synchronizing scoreboards
 *
 * Schnittstelle zur Verwaltung von Scoreboards
 * Verwaltung der Informationsanzeige für Spieler
 * Aktualisierung und Synchronisierung von Scoreboards
 */
public interface IScoreboardManager {
    
    /**
     * Инициализация менеджера скорбордов
     * Подготовка к работе со скорбордами
     * Создание необходимых структур данных
     *
     * Initializes scoreboard manager
     * Preparation for working with scoreboards
     * Creating necessary data structures
     *
     * Initialisiert den Scoreboard-Manager
     * Vorbereitung auf die Arbeit mit Scoreboards
     * Erstellen notwendiger Datenstrukturen
     */
    void initialize();
    
    /**
     * Устанавливает скорборд игроку
     * @param player Игрок
     *
     * Sets scoreboard for player
     * @param player Player
     *
     * Setzt Scoreboard für Spieler
     * @param player Spieler
     */
    void setScoreboard(Player player);
    
    /**
     * Обновляет скорборд игрока
     * @param player Игрок
     *
     * Updates player scoreboard
     * @param player Player
     *
     * Aktualisiert das Spieler-Scoreboard
     * @param player Spieler
     */
    void updateScoreboard(Player player);
    
    /**
     * Удаляет скорборд игрока
     * @param player Игрок
     *
     * Removes player scoreboard
     * @param player Player
     *
     * Entfernt das Spieler-Scoreboard
     * @param player Spieler
     */
    void removeScoreboard(Player player);
    
    /**
     * Обновляет скорборды всех игроков
     * Массовое обновление для всех активных игроков
     * Оптимизация производительности
     *
     * Updates all player scoreboards
     * Mass update for all active players
     * Performance optimization
     *
     * Aktualisiert alle Spieler-Scoreboards
     * Massenaktualisierung für alle aktiven Spieler
     * Leistungsoptimierung
     */
    void updateAllScoreboards();
    
    /**
     * Получает скорборд игрока
     * @param player Игрок
     * @return Скорборд игрока или null
     *
     * Gets player scoreboard
     * @param player Player
     * @return Player scoreboard or null
     *
     * Ruft das Spieler-Scoreboard ab
     * @param player Spieler
     * @return Spieler-Scoreboard oder null
     */
    Scoreboard getPlayerScoreboard(Player player);
    
    /**
     * Проверяет, имеет ли игрок скорборд
     * @param player Игрок
     * @return true если у игрока есть скорборд
     *
     * Checks if player has scoreboard
     * @param player Player
     * @return true if player has scoreboard
     *
     * Prüft, ob der Spieler ein Scoreboard hat
     * @param player Spieler
     * @return true, wenn der Spieler ein Scoreboard hat
     */
    boolean hasScoreboard(Player player);
    
    /**
     * Останавливает обновление скорбордов
     * Приостановка всех задач обновления
     * Освобождение ресурсов
     *
     * Stops scoreboard updating
     * Suspending all update tasks
     * Releasing resources
     *
     * Stoppt die Scoreboard-Aktualisierung
     * Aussetzen aller Aktualisierungsaufgaben
     * Freigeben von Ressourcen
     */
    void stopUpdating();
    
    /**
     * Возобновляет обновление скорбордов
     * Возобновление задач обновления
     * Перезапуск планировщика
     *
     * Resumes scoreboard updating
     * Resuming update tasks
     * Restarting scheduler
     *
     * Setzt die Scoreboard-Aktualisierung fort
     * Fortsetzen der Aktualisierungsaufgaben
     * Neustart des Planers
     */
    void resumeUpdating();
    
    /**
     * Устанавливает интервал обновления
     * @param interval Интервал в тиках
     *
     * Sets update interval
     * @param interval Interval in ticks
     *
     * Setzt das Aktualisierungsintervall
     * @param interval Intervall in Ticks
     */
    void setUpdateInterval(int interval);
    
    /**
     * Получает интервал обновления
     * @return Интервал в тиках
     *
     * Gets update interval
     * @return Interval in ticks
     *
     * Ruft das Aktualisierungsintervall ab
     * @return Intervall in Ticks
     */
    int getUpdateInterval();
}