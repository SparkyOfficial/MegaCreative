package com.megacreative.interfaces;

import com.megacreative.managers.PlayerModeManager;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Интерфейс для управления игроками
 * Управление регистрацией, данными и статистикой игроков
 * Отслеживание активности и предпочтений игроков
 *
 * Interface for player management
 * Management of player registration, data and statistics
 * Tracking player activity and preferences
 *
 * Schnittstelle zur Spielerverwaltung
 * Verwaltung der Spielerregistrierung, Daten und Statistiken
 * Verfolgung der Spieleraktivität und -präferenzen
 */
public interface IPlayerManager {
    
    /**
     * Инициализация менеджера игроков
     * Подготовка к работе с данными игроков
     * Загрузка начальных данных
     *
     * Initializes player manager
     * Preparation for working with player data
     * Loading initial data
     *
     * Initialisiert den Spielermanager
     * Vorbereitung auf die Arbeit mit Spielerdaten
     * Laden der Anfangsdaten
     */
    void initialize();
    
    /**
     * Регистрирует игрока в системе
     * @param player Игрок для регистрации
     *
     * Registers player in system
     * @param player Player to register
     *
     * Registriert Spieler im System
     * @param player Zu registrierender Spieler
     */
    void registerPlayer(Player player);
    
    /**
     * Удаляет игрока из системы
     * @param player Игрок для удаления
     *
     * Removes player from system
     * @param player Player to remove
     *
     * Entfernt Spieler aus dem System
     * @param player Zu entfernender Spieler
     */
    void unregisterPlayer(Player player);
    
    /**
     * Получает данные игрока
     * @param playerId UUID игрока
     * @return Данные игрока или null, если не найден
     *
     * Gets player data
     * @param playerId Player UUID
     * @return Player data or null if not found
     *
     * Ruft Spielerdaten ab
     * @param playerId Spieler-UUID
     * @return Spielerdaten oder null, wenn nicht gefunden
     */
    Map<String, Object> getPlayerData(UUID playerId);
    
    /**
     * Завершает работу менеджера игроков
     * Освобождение ресурсов и сохранение данных
     * Закрытие соединений и файлов
     *
     * Shuts down player manager
     * Resource release and data saving
     * Closing connections and files
     *
     * Schaltet den Spielermanager herunter
     * Ressourcenfreigabe und Datenspeicherung
     * Schließen von Verbindungen und Dateien
     */
    void shutdown();
    
    /**
     * Устанавливает данные игрока
     * @param playerId UUID игрока
     * @param data Данные игрока
     *
     * Sets player data
     * @param playerId Player UUID
     * @param data Player data
     *
     * Setzt Spielerdaten
     * @param playerId Spieler-UUID
     * @param data Spielerdaten
     */
    void setPlayerData(UUID playerId, Map<String, Object> data);
    
    /**
     * Проверяет, зарегистрирован ли игрок
     * @param playerId UUID игрока
     * @return true если игрок зарегистрирован
     *
     * Checks if player is registered
     * @param playerId Player UUID
     * @return true if player is registered
     *
     * Prüft, ob der Spieler registriert ist
     * @param playerId Spieler-UUID
     * @return true, wenn der Spieler registriert ist
     */
    boolean isPlayerRegistered(UUID playerId);
    
    /**
     * Получает количество зарегистрированных игроков
     * @return Количество игроков
     *
     * Gets registered player count
     * @return Number of players
     *
     * Ruft die Anzahl registrierter Spieler ab
     * @return Anzahl der Spieler
     */
    int getPlayerCount();
    
    /**
     * Сохраняет данные всех игроков
     * Запись данных в постоянное хранилище
     * Обеспечение сохранности информации
     *
     * Saves all player data
     * Writing data to persistent storage
     * Ensuring information safety
     *
     * Speichert alle Spielerdaten
     * Schreiben von Daten in den persistenten Speicher
     * Gewährleistung der Informationssicherheit
     */
    void saveAllPlayerData();
    
    /**
     * Загружает данные всех игроков
     * Чтение данных из постоянного хранилища
     * Инициализация внутренних структур
     *
     * Loads all player data
     * Reading data from persistent storage
     * Initializing internal structures
     *
     * Lädt alle Spielerdaten
     * Lesen von Daten aus dem persistenten Speicher
     * Initialisierung interner Strukturen
     */
    void loadAllPlayerData();
    
    /**
     * Очищает данные игрока
     * @param playerId UUID игрока
     *
     * Clears player data
     * @param playerId Player UUID
     *
     * Löscht Spielerdaten
     * @param playerId Spieler-UUID
     */
    void clearPlayerData(UUID playerId);
    
    /**
     * Выдает стартовые предметы игроку
     * @param player Игрок
     *
     * Gives starter items to player
     * @param player Player
     *
     * Gibt Startgegenstände an Spieler
     * @param player Spieler
     */
    void giveStarterItems(Player player);
    
    /**
     * Проверяет, является ли мир избранным для игрока
     * @param playerId ID игрока
     * @param worldId ID мира
     * @return true если мир в избранном
     *
     * Checks if world is favorite for player
     * @param playerId Player ID
     * @param worldId World ID
     * @return true if world is favorite
     *
     * Prüft, ob die Welt für den Spieler favorisiert ist
     * @param playerId Spieler-ID
     * @param worldId Welt-ID
     * @return true, wenn die Welt favorisiert ist
     */
    boolean isFavorite(UUID playerId, String worldId);
    
    // 🎆 ENHANCED: World tracking methods for dual world architecture
    
    /**
     * Tracks player entry into a world and mode
     * @param player Player entering world
     * @param worldId World ID
     * @param mode World mode (DEV, PLAY, etc.)
     *
     * Отслеживает вход игрока в мир и режим
     * @param player Игрок, входящий в мир
     * @param worldId ID мира
     * @param mode Режим мира (DEV, PLAY и т.д.)
     *
     * Verfolgt den Spieler-Eintritt in eine Welt und den Modus
     * @param player Spieler, der die Welt betritt
     * @param worldId Welt-ID
     * @param mode Weltmodus (DEV, PLAY usw.)
     */
    void trackPlayerWorldEntry(Player player, String worldId, String mode);
    
    /**
     * Tracks player exit from a world
     * @param player Player leaving world
     * @param worldId World ID
     *
     * Отслеживает выход игрока из мира
     * @param player Игрок, покидающий мир
     * @param worldId ID мира
     *
     * Verfolgt den Spieler-Austritt aus einer Welt
     * @param player Spieler, der die Welt verlässt
     * @param worldId Welt-ID
     */
    void trackPlayerWorldExit(Player player, String worldId);
    
    /**
     * Gets the current world and mode for a player
     * @param playerId Player UUID
     * @return Map with worldId and mode, or null if not tracked
     *
     * Получает текущий мир и режим для игрока
     * @param playerId UUID игрока
     * @return Карта с worldId и mode, или null если не отслеживается
     *
     * Ruft die aktuelle Welt und den Modus für einen Spieler ab
     * @param playerId Spieler-UUID
     * @return Karte mit worldId und mode, oder null wenn nicht verfolgt
     */
    Map<String, String> getCurrentPlayerLocation(UUID playerId);
    
    /**
     * Gets all players currently in a specific world
     * @param worldId World ID
     * @return Map of player UUIDs to their modes
     *
     * Получает всех игроков, находящихся в определенном мире
     * @param worldId ID мира
     * @return Карта UUID игроков и их режимов
     *
     * Ruft alle Spieler ab, die sich derzeit in einer bestimmten Welt befinden
     * @param worldId Welt-ID
     * @return Karte der Spieler-UUIDs zu ihren Modi
     */
    Map<UUID, String> getPlayersInWorld(String worldId);
    
    /**
     * Gets world statistics for analytics
     * @param worldId World ID
     * @return Statistics including unique players, time spent, etc.
     *
     * Получает статистику мира для аналитики
     * @param worldId ID мира
     * @return Статистика, включая уникальных игроков, время и т.д.
     *
     * Ruft Weltenstatistiken für Analysen ab
     * @param worldId Welt-ID
     * @return Statistiken einschließlich eindeutiger Spieler, verbrachter Zeit usw.
     */
    Map<String, Object> getWorldStatistics(String worldId);
    
    /**
     * Gets player session time in current world
     * @param playerId Player UUID
     * @return Session time in milliseconds, or 0 if not in a world
     *
     * Получает время сессии игрока в текущем мире
     * @param playerId UUID игрока
     * @return Время сессии в миллисекундах, или 0 если не в мире
     *
     * Ruft die Spieler-Sitzungszeit in der aktuellen Welt ab
     * @param playerId Spieler-UUID
     * @return Sitzungszeit in Millisekunden, oder 0 wenn nicht in einer Welt
     */
    long getPlayerSessionTime(UUID playerId);

    /**
     * Gets the player mode manager
     * @return Player mode manager instance
     */
    PlayerModeManager getPlayerModeManager();
}