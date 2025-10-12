package com.megacreative.interfaces;

import com.megacreative.models.CreativeWorld;
import com.megacreative.models.CreativeWorldType;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Интерфейс для управления мирами
 * Управление созданием, удалением и переключением миров
 * Работа с творческими мирами и их конфигурациями
 *
 * Interface for world management
 * Management of world creation, deletion and switching
 * Working with creative worlds and their configurations
 *
 * Schnittstelle zur Weltverwaltung
 * Verwaltung der Weltenerschaffung, -löschung und -umschaltung
 * Arbeiten mit Creative-Welten und deren Konfigurationen
 */
public interface IWorldManager {
    
    /**
     * Инициализация менеджера миров
     * Подготовка к работе с мирами
     * Загрузка начальных данных
     *
     * Initializes world manager
     * Preparation for working with worlds
     * Loading initial data
     *
     * Initialisiert den Weltmanager
     * Vorbereitung auf die Arbeit mit Welten
     * Laden der Anfangsdaten
     */
    void initialize();
    
    /**
     * Создает новый мир для игрока
     * @param player Игрок, создающий мир
     * @param name Имя мира
     * @param worldType Тип мира
     *
     * Creates new world for player
     * @param player Player creating world
     * @param name World name
     * @param worldType World type
     *
     * Erstellt eine neue Welt für den Spieler
     * @param player Spieler, der die Welt erstellt
     * @param name Weltname
     * @param worldType Welttyp
     */
    void createWorld(Player player, String name, CreativeWorldType worldType);
    
    /**
     * 🎆 ENHANCED: Creates a dual world pair for reference system-style development
     * @param player Игрок, создающий мир
     * @param name Имя мира
     * @param worldType Тип мира
     *
     * 🎆 ENHANCED: Erstellt ein Doppelweltpaar für die Entwicklung im Reference System-Stil
     * @param player Spieler, der die Welt erstellt
     * @param name Weltname
     * @param worldType Welttyp
     *
     * 🎆 ENHANCED: Creates a dual world pair for reference system-style development
     * @param player Player creating world
     * @param name World name
     * @param worldType World type
     */
    void createDualWorld(Player player, String name, CreativeWorldType worldType);
    
    /**
     * Удаляет мир
     * @param worldId ID мира
     * @param requester Игрок, запрашивающий удаление
     *
     * Deletes world
     * @param worldId World ID
     * @param requester Player requesting deletion
     *
     * Löscht Welt
     * @param worldId Welt-ID
     * @param requester Spieler, der die Löschung anfordert
     */
    void deleteWorld(String worldId, Player requester);
    
    /**
     * Получает мир по ID
     * @param id ID мира
     * @return Мир или null, если не найден
     *
     * Gets world by ID
     * @param id World ID
     * @return World or null if not found
     *
     * Ruft Welt nach ID ab
     * @param id Welt-ID
     * @return Welt oder null, wenn nicht gefunden
     */
    CreativeWorld getWorld(String id);
    
    /**
     * Находит мир по имени
     * @param name Имя мира
     * @return Мир или null, если не найден
     *
     * Finds world by name
     * @param name World name
     * @return World or null if not found
     *
     * Findet Welt nach Name
     * @param name Weltname
     * @return Welt oder null, wenn nicht gefunden
     */
    CreativeWorld getWorldByName(String name);
    
    /**
     * Находит CreativeWorld по Bukkit-миру
     * @param bukkitWorld Bukkit-мир
     * @return CreativeWorld или null, если не найден
     *
     * Finds CreativeWorld by Bukkit world
     * @param bukkitWorld Bukkit world
     * @return CreativeWorld or null if not found
     *
     * Findet CreativeWorld nach Bukkit-Welt
     * @param bukkitWorld Bukkit-Welt
     * @return CreativeWorld oder null, wenn nicht gefunden
     */
    CreativeWorld findCreativeWorldByBukkit(World bukkitWorld);
    
    /**
     * Получает все миры игрока
     * @param player Игрок
     * @return Список миров игрока
     *
     * Gets all player worlds
     * @param player Player
     * @return List of player worlds
     *
     * Ruft alle Spielerwelten ab
     * @param player Spieler
     * @return Liste der Spielerwelten
     */
    List<CreativeWorld> getPlayerWorlds(Player player);
    
    /**
     * Завершает работу менеджера миров
     * Освобождение ресурсов и сохранение данных
     * Закрытие соединений и файлов
     *
     * Shuts down world manager
     * Resource release and data saving
     * Closing connections and files
     *
     * Schaltet den Weltmanager herunter
     * Ressourcenfreigabe und Datenspeicherung
     * Schließen von Verbindungen und Dateien
     */
    void shutdown();
    
    /**
     * Получает все публичные миры
     * @return Список публичных миров
     *
     * Gets all public worlds
     * @return List of public worlds
     *
     * Ruft alle öffentlichen Welten ab
     * @return Liste der öffentlichen Welten
     */
    List<CreativeWorld> getAllPublicWorlds();
    
    /**
     * Получает количество миров игрока
     * @param player Игрок
     * @return Количество миров
     *
     * Gets player world count
     * @param player Player
     * @return Number of worlds
     *
     * Ruft die Anzahl der Spielerwelten ab
     * @param player Spieler
     * @return Anzahl der Welten
     */
    int getPlayerWorldCount(Player player);
    
    /**
     * Сохраняет мир асинхронно
     * @param world Мир для сохранения
     * @param player Игрок (для уведомлений)
     *
     * Saves world asynchronously
     * @param world World to save
     * @param player Player (for notifications)
     *
     * Speichert Welt asynchron
     * @param world Zu speichernde Welt
     * @param player Spieler (für Benachrichtigungen)
     */
    void saveWorldAsync(CreativeWorld world, Player player);
    
    /**
     * Сохраняет мир синхронно
     * @param world Мир для сохранения
     *
     * Saves world synchronously
     * @param world World to save
     *
     * Speichert Welt synchron
     * @param world Zu speichernde Welt
     */
    void saveWorld(CreativeWorld world);
    
    /**
     * Сохраняет все миры
     * Запись данных всех миров в постоянное хранилище
     * Обеспечение сохранности информации
     *
     * Saves all worlds
     * Writing all world data to persistent storage
     * Ensuring information safety
     *
     * Speichert alle Welten
     * Schreiben aller Weltdaten in den persistenten Speicher
     * Gewährleistung der Informationssicherheit
     */
    void saveAllWorlds();
    
    /**
     * Получает все творческие миры
     * @return Список всех творческих миров
     *
     * Gets all creative worlds
     * @return List of all creative worlds
     *
     * Ruft alle Creative-Welten ab
     * @return Liste aller Creative-Welten
     */
    List<CreativeWorld> getCreativeWorlds();
    
    
    
    /**
     * Gets the paired world for dual world architecture
     * @param world The world to find the pair for
     * @return The paired world or null if not found
     *
     * Получает парный мир для архитектуры двойных миров
     * @param world Мир, для которого нужно найти пару
     * @return Парный мир или null, если не найден
     *
     * Ruft die gepaarte Welt für die Doppelwelt-Architektur ab
     * @param world Die Welt, für die das Paar gefunden werden soll
     * @return Die gepaarte Welt oder null, wenn nicht gefunden
     */
    CreativeWorld getPairedWorld(CreativeWorld world);
    
    /**
     * Switches player to the development world
     * @param player Player to switch
     * @param worldId ID of the world
     *
     * Переключает игрока в мир разработки
     * @param player Игрок для переключения
     * @param worldId ID мира
     *
     * Schaltet den Spieler in die Entwicklungswelt
     * @param player Zu schaltender Spieler
     * @param worldId ID der Welt
     */
    void switchToDevWorld(Player player, String worldId);
    
    /**
     * Switches player to the play world
     * @param player Player to switch
     * @param worldId ID of the world
     *
     * Переключает игрока в игровой мир
     * @param player Игрок для переключения
     * @param worldId ID мира
     *
     * Schaltet den Spieler in die Spielwelt
     * @param player Zu schaltender Spieler
     * @param worldId ID der Welt
     */
    void switchToPlayWorld(Player player, String worldId);
    
    /**
     * 🎆 ENHANCED: Switches player to the build world (development mode with BUILD permissions)
     * @param player Player to switch
     * @param worldId ID of the world
     *
     * 🎆 ENHANCED: Переключает игрока в мир строительства (режим разработки с правами BUILD)
     * @param player Игрок для переключения
     * @param worldId ID мира
     *
     * 🎆 ENHANCED: Schaltet den Spieler in die Baurechte-Welt (Entwicklungsmodus mit BUILD-Rechten)
     * @param player Zu schaltender Spieler
     * @param worldId ID der Welt
     */
    void switchToBuildWorld(Player player, String worldId);
}