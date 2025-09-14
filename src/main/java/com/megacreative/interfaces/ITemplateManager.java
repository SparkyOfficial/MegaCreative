package com.megacreative.interfaces;

import com.megacreative.coding.CodeScript;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс для управления шаблонами
 * Управление созданием, хранением и использованием шаблонов скриптов
 * Работа с публичными и приватными шаблонами
 *
 * Interface for template management
 * Management of script template creation, storage and usage
 * Working with public and private templates
 *
 * Schnittstelle zur Vorlagenverwaltung
 * Verwaltung der Skriptvorlagenerstellung, -speicherung und -nutzung
 * Arbeiten mit öffentlichen und privaten Vorlagen
 */
public interface ITemplateManager {
    
    /**
     * Инициализация менеджера шаблонов
     * Подготовка к работе с шаблонами
     * Загрузка начальных данных
     *
     * Initializes template manager
     * Preparation for working with templates
     * Loading initial data
     *
     * Initialisiert den Vorlagenmanager
     * Vorbereitung auf die Arbeit mit Vorlagen
     * Laden der Anfangsdaten
     */
    void initialize();
    
    /**
     * Создает новый шаблон
     * @param name Имя шаблона
     * @param description Описание шаблона
     * @param script Скрипт шаблона
     * @param creator Создатель шаблона
     *
     * Creates new template
     * @param name Template name
     * @param description Template description
     * @param script Template script
     * @param creator Template creator
     *
     * Erstellt eine neue Vorlage
     * @param name Vorlagenname
     * @param description Vorlagenbeschreibung
     * @param script Vorlagenskript
     * @param creator Vorlagenersteller
     */
    void createTemplate(String name, String description, CodeScript script, Player creator);
    
    /**
     * Получает шаблон по имени
     * @param name Имя шаблона
     * @return Шаблон или null, если не найден
     *
     * Gets template by name
     * @param name Template name
     * @return Template or null if not found
     *
     * Ruft Vorlage nach Name ab
     * @param name Vorlagenname
     * @return Vorlage oder null, wenn nicht gefunden
     */
    CodeScript getTemplate(String name);
    
    /**
     * Получает все шаблоны
     * @return Список всех шаблонов
     *
     * Gets all templates
     * @return List of all templates
     *
     * Ruft alle Vorlagen ab
     * @return Liste aller Vorlagen
     */
    List<CodeScript> getAllTemplates();
    
    /**
     * Получает шаблоны игрока
     * @param player Игрок
     * @return Список шаблонов игрока
     *
     * Gets player templates
     * @param player Player
     * @return List of player templates
     *
     * Ruft Spieler-Vorlagen ab
     * @param player Spieler
     * @return Liste der Spieler-Vorlagen
     */
    List<CodeScript> getPlayerTemplates(Player player);
    
    /**
     * Получает публичные шаблоны
     * @return Список публичных шаблонов
     *
     * Gets public templates
     * @return List of public templates
     *
     * Ruft öffentliche Vorlagen ab
     * @return Liste der öffentlichen Vorlagen
     */
    List<CodeScript> getPublicTemplates();
    
    /**
     * Удаляет шаблон
     * @param name Имя шаблона
     * @param requester Игрок, запрашивающий удаление
     *
     * Deletes template
     * @param name Template name
     * @param requester Player requesting deletion
     *
     * Löscht Vorlage
     * @param name Vorlagenname
     * @param requester Spieler, der die Löschung anfordert
     */
    void deleteTemplate(String name, Player requester);
    
    /**
     * Обновляет шаблон
     * @param name Имя шаблона
     * @param script Новый скрипт
     * @param updater Игрок, обновляющий шаблон
     *
     * Updates template
     * @param name Template name
     * @param script New script
     * @param updater Player updating template
     *
     * Aktualisiert Vorlage
     * @param name Vorlagenname
     * @param script Neues Skript
     * @param updater Spieler, der die Vorlage aktualisiert
     */
    void updateTemplate(String name, CodeScript script, Player updater);
    
    /**
     * Проверяет, существует ли шаблон
     * @param name Имя шаблона
     * @return true если шаблон существует
     *
     * Checks if template exists
     * @param name Template name
     * @return true if template exists
     *
     * Prüft, ob die Vorlage existiert
     * @param name Vorlagenname
     * @return true, wenn die Vorlage existiert
     */
    boolean templateExists(String name);
    
    /**
     * Получает метаданные шаблона
     * @param name Имя шаблона
     * @return Метаданные шаблона
     *
     * Gets template metadata
     * @param name Template name
     * @return Template metadata
     *
     * Ruft Vorlagen-Metadaten ab
     * @param name Vorlagenname
     * @return Vorlagen-Metadaten
     */
    Map<String, Object> getTemplateMetadata(String name);
    
    /**
     * Сохраняет все шаблоны
     * Запись данных в постоянное хранилище
     * Обеспечение сохранности информации
     *
     * Saves all templates
     * Writing data to persistent storage
     * Ensuring information safety
     *
     * Speichert alle Vorlagen
     * Schreiben von Daten in den persistenten Speicher
     * Gewährleistung der Informationssicherheit
     */
    void saveAllTemplates();
    
    /**
     * Загружает все шаблоны
     * Чтение данных из постоянного хранилища
     * Инициализация внутренних структур
     *
     * Loads all templates
     * Reading data from persistent storage
     * Initializing internal structures
     *
     * Lädt alle Vorlagen
     * Lesen von Daten aus dem persistenten Speicher
     * Initialisierung interner Strukturen
     */
    void loadAllTemplates();
}