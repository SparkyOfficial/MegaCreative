package com.megacreative.interfaces;

import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс для управления доверенными игроками
 * Управление доступом доверенных игроков к мирам
 * Работа с разрешениями строителей и программистов
 *
 * Interface for trusted player management
 * Management of trusted player access to worlds
 * Working with builder and coder permissions
 *
 * Schnittstelle zur Verwaltung vertrauenswürdiger Spieler
 * Verwaltung des Zugriffs vertrauenswürdiger Spieler auf Welten
 * Arbeiten mit Builder- und Coder-Berechtigungen
 */
public interface ITrustedPlayerManager {
    
    /**
     * Инициализация менеджера доверенных игроков
     * Подготовка к работе с доверенными игроками
     * Загрузка начальных данных
     *
     * Initializes trusted player manager
     * Preparation for working with trusted players
     * Loading initial data
     *
     * Initialisiert den Manager für vertrauenswürdige Spieler
     * Vorbereitung auf die Arbeit mit vertrauenswürdigen Spielern
     * Laden der Anfangsdaten
     */
    void initialize();
    
    /**
     * Добавляет доверенного игрока к миру
     * @param world Мир
     * @param trustedPlayer Доверенный игрок
     * @param owner Владелец мира
     * @throws IllegalArgumentException если world, trustedPlayer или owner равны null
     *
     * Adds trusted player to world
     * @param world World
     * @param trustedPlayer Trusted player
     * @param owner World owner
     * @throws IllegalArgumentException if world, trustedPlayer or owner are null
     *
     * Fügt vertrauenswürdigen Spieler zur Welt hinzu
     * @param world Welt
     * @param trustedPlayer Vertrauenswürdiger Spieler
     * @param owner Weltenbesitzer
     * @throws IllegalArgumentException wenn world, trustedPlayer oder owner null sind
     */
    void addTrustedPlayer(CreativeWorld world, Player trustedPlayer, Player owner);
    
    /**
     * Удаляет доверенного игрока из мира
     * @param world Мир
     * @param trustedPlayer Доверенный игрок
     * @param owner Владелец мира
     * @throws IllegalArgumentException если world, trustedPlayer или owner равны null
     *
     * Removes trusted player from world
     * @param world World
     * @param trustedPlayer Trusted player
     * @param owner World owner
     * @throws IllegalArgumentException if world, trustedPlayer or owner are null
     *
     * Entfernt vertrauenswürdigen Spieler aus der Welt
     * @param world Welt
     * @param trustedPlayer Vertrauenswürdiger Spieler
     * @param owner Weltenbesitzer
     * @throws IllegalArgumentException wenn world, trustedPlayer oder owner null sind
     */
    void removeTrustedPlayer(CreativeWorld world, Player trustedPlayer, Player owner);
    
    /**
     * Проверяет, является ли игрок доверенным для мира
     * @param world Мир
     * @param player Игрок
     * @return true если игрок доверенный
     * @throws IllegalArgumentException если world или player равны null
     *
     * Checks if player is trusted for world
     * @param world World
     * @param player Player
     * @return true if player is trusted
     * @throws IllegalArgumentException if world or player are null
     *
     * Prüft, ob der Spieler für die Welt vertrauenswürdig ist
     * @param world Welt
     * @param player Spieler
     * @return true, wenn der Spieler vertrauenswürdig ist
     * @throws IllegalArgumentException wenn world oder player null sind
     */
    boolean isTrustedPlayer(CreativeWorld world, Player player);
    
    /**
     * Получает список доверенных игроков мира
     * @param world Мир
     * @return Список доверенных игроков (не null, может быть пустым)
     * @throws IllegalArgumentException если world равен null
     *
     * Gets list of trusted players for world
     * @param world World
     * @return List of trusted players (not null, may be empty)
     * @throws IllegalArgumentException if world is null
     *
     * Ruft die Liste vertrauenswürdiger Spieler für die Welt ab
     * @param world Welt
     * @return Liste vertrauenswürdiger Spieler (nicht null, kann leer sein)
     * @throws IllegalArgumentException wenn world null ist
     */
    List<com.megacreative.models.TrustedPlayer> getTrustedPlayers(CreativeWorld world);
    
    /**
     * Получает список всех доверенных игроков
     * @return Список всех доверенных игроков (не null, может быть пустым)
     *
     * Gets list of all trusted players
     * @return List of all trusted players (not null, may be empty)
     *
     * Ruft die Liste aller vertrauenswürdigen Spieler ab
     * @return Liste aller vertrauenswürdigen Spieler (nicht null, kann leer sein)
     */
    List<com.megacreative.models.TrustedPlayer> getAllTrustedPlayers();
    
    /**
     * Получает список доверенных строителей
     * @return Список доверенных строителей (не null, может быть пустым)
     *
     * Gets list of trusted builders
     * @return List of trusted builders (not null, may be empty)
     *
     * Ruft die Liste vertrauenswürdiger Builder ab
     * @return Liste vertrauenswürdiger Builder (nicht null, kann leer sein)
     */
    List<com.megacreative.models.TrustedPlayer> getTrustedBuilders();
    
    /**
     * Получает список доверенных кодеров
     * @return Список доверенных кодеров (не null, может быть пустым)
     *
     * Gets list of trusted coders
     * @return List of trusted coders (not null, may be empty)
     *
     * Ruft die Liste vertrauenswürdiger Coder ab
     * @return Liste vertrauenswürdiger Coder (nicht null, kann leer sein)
     */
    List<com.megacreative.models.TrustedPlayer> getTrustedCoders();
    
    /**
     * Получает доверенного игрока по UUID
     * @param playerId UUID игрока
     * @return Объект TrustedPlayer или null если не найден или playerId равен null
     *
     * Gets trusted player by UUID
     * @param playerId Player UUID
     * @return TrustedPlayer object or null if not found or playerId is null
     *
     * Ruft vertrauenswürdigen Spieler nach UUID ab
     * @param playerId Spieler-UUID
     * @return TrustedPlayer-Objekt oder null, wenn nicht gefunden oder playerId null ist
     */
    com.megacreative.models.TrustedPlayer getTrustedPlayer(UUID playerId);
    
    /**
     * Завершает работу менеджера доверенных игроков
     * Освобождение ресурсов и сохранение данных
     * Закрытие соединений и файлов
     *
     * Shuts down trusted player manager
     * Resource release and data saving
     * Closing connections and files
     *
     * Schaltet den Manager für vertrauenswürdige Spieler herunter
     * Ressourcenfreigabe und Datenspeicherung
     * Schließen von Verbindungen und Dateien
     */
    void shutdown();
    
    /**
     * Получает список миров, где игрок является доверенным
     * @param player Игрок
     * @return Список миров (не null, может быть пустым)
     * @throws IllegalArgumentException если player равен null
     *
     * Gets list of worlds where player is trusted
     * @param player Player
     * @return List of worlds (not null, may be empty)
     * @throws IllegalArgumentException if player is null
     *
     * Ruft die Liste der Welten ab, in denen der Spieler vertrauenswürdig ist
     * @param player Spieler
     * @return Liste der Welten (nicht null, kann leer sein)
     * @throws IllegalArgumentException wenn player null ist
     */
    List<CreativeWorld> getTrustedWorlds(Player player);
    
    /**
     * Очищает всех доверенных игроков из мира
     * @param world Мир
     * @param owner Владелец мира
     * @throws IllegalArgumentException если world или owner равны null
     *
     * Clears all trusted players from world
     * @param world World
     * @param owner World owner
     * @throws IllegalArgumentException if world or owner are null
     *
     * Löscht alle vertrauenswürdigen Spieler aus der Welt
     * @param world Welt
     * @param owner Weltenbesitzer
     * @throws IllegalArgumentException wenn world oder owner null sind
     */
    void clearTrustedPlayers(CreativeWorld world, Player owner);
    
    /**
     * Получает количество доверенных игроков в мире
     * @param world Мир
     * @return Количество доверенных игроков (0 если world равен null или нет доверенных игроков)
     *
     * Gets trusted player count in world
     * @param world World
     * @return Number of trusted players (0 if world is null or no trusted players)
     *
     * Ruft die Anzahl vertrauenswürdiger Spieler in der Welt ab
     * @param world Welt
     * @return Anzahl vertrauenswürdiger Spieler (0 wenn world null ist oder keine vertrauenswürdigen Spieler)
     */
    int getTrustedPlayerCount(CreativeWorld world);
    
    /**
     * Сохраняет данные доверенных игроков
     * Запись данных в постоянное хранилище
     * Обеспечение сохранности информации
     *
     * Saves trusted player data
     * Writing data to persistent storage
     * Ensuring information safety
     *
     * Speichert Daten vertrauenswürdiger Spieler
     * Schreiben von Daten in den persistenten Speicher
     * Gewährleistung der Informationssicherheit
     */
    void saveTrustedPlayers();
    
    /**
     * Загружает данные доверенных игроков
     * Чтение данных из постоянного хранилища
     * Инициализация внутренних структур
     *
     * Loads trusted player data
     * Reading data from persistent storage
     * Initializing internal structures
     *
     * Lädt Daten vertrauenswürdiger Spieler
     * Lesen von Daten aus dem persistenten Speicher
     * Initialisierung interner Strukturen
     */
    void loadTrustedPlayers();
    
    /**
     * Удаляет игрока из всех доверенных списков
     * @param playerId UUID игрока
     *
     * Removes player from all trusted lists
     * @param playerId Player UUID
     *
     * Entfernt Spieler aus allen Vertrauenslisten
     * @param playerId Spieler-UUID
     */
    void removePlayerFromAllTrustedLists(UUID playerId);
    
    /**
     * Проверяет, может ли игрок программировать в мире разработки
     * @param player Игрок
     * @return true если игрок может программировать
     *
     * Checks if player can code in development world
     * @param player Player
     * @return true if player can code
     *
     * Prüft, ob der Spieler in der Entwicklungswelt programmieren kann
     * @param player Spieler
     * @return true, wenn der Spieler programmieren kann
     */
    boolean canCodeInDevWorld(Player player);
}