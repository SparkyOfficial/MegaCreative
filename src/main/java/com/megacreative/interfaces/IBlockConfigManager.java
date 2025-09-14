package com.megacreative.interfaces;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс для управления конфигурацией блоков
 * Управление типами блоков, материалами и их конфигурациями
 * Загрузка и сохранение конфигураций блоков
 *
 * Interface for block configuration management
 * Management of block types, materials and their configurations
 * Loading and saving block configurations
 *
 * Schnittstelle zur Verwaltung der Blockkonfiguration
 * Verwaltung von Blocktypen, Materialien und deren Konfigurationen
 * Laden und Speichern von Blockkonfigurationen
 */
public interface IBlockConfigManager {
    
    /**
     * Инициализация менеджера конфигурации блоков
     * Подготовка к работе с конфигурациями блоков
     * Загрузка начальных данных
     *
     * Initializes the block configuration manager
     * Preparation for working with block configurations
     * Loading initial data
     *
     * Initialisiert den Blockkonfigurationsmanager
     * Vorbereitung auf die Arbeit mit Blockkonfigurationen
     * Laden der Anfangsdaten
     */
    void initialize();
    
    /**
     * Загружает конфигурацию блоков
     * Чтение данных из файлов конфигурации
     * Инициализация внутренних структур данных
     *
     * Loads block configuration
     * Reading data from configuration files
     * Initializing internal data structures
     *
     * Lädt die Blockkonfiguration
     * Lesen von Daten aus Konfigurationsdateien
     * Initialisierung interner Datenstrukturen
     */
    void loadBlockConfiguration();
    
    /**
     * Сохраняет конфигурацию блоков
     * Запись текущих данных в файлы конфигурации
     * Обеспечение сохранности данных
     *
     * Saves block configuration
     * Writing current data to configuration files
     * Ensuring data safety
     *
     * Speichert die Blockkonfiguration
     * Schreiben aktueller Daten in Konfigurationsdateien
     * Gewährleistung der Datensicherheit
     */
    void saveBlockConfiguration();
    
    /**
     * Получает действия для материала
     * @param material Материал блока
     * @return Список действий
     *
     * Gets actions for material
     * @param material Block material
     * @return List of actions
     *
     * Ruft Aktionen für Material ab
     * @param material Blockmaterial
     * @return Liste der Aktionen
     */
    List<String> getActionsForMaterial(Material material);
    
    /**
     * Получает условия для материала
     * @param material Материал блока
     * @return Список условий
     *
     * Gets conditions for material
     * @param material Block material
     * @return List of conditions
     *
     * Ruft Bedingungen für Material ab
     * @param material Blockmaterial
     * @return Liste der Bedingungen
     */
    List<String> getConditionsForMaterial(Material material);
    
    /**
     * Получает материал для типа блока
     * @param blockType Тип блока
     * @return Материал или null
     *
     * Gets material for block type
     * @param blockType Block type
     * @return Material or null
     *
     * Ruft Material für Blocktyp ab
     * @param blockType Blocktyp
     * @return Material oder null
     */
    Material getMaterialForBlockType(String blockType);
    
    /**
     * Получает тип блока для материала
     * @param material Материал
     * @return Тип блока или null
     *
     * Gets block type for material
     * @param material Material
     * @return Block type or null
     *
     * Ruft Blocktyp für Material ab
     * @param material Material
     * @return Blocktyp oder null
     */
    String getBlockTypeForMaterial(Material material);
    
    /**
     * Проверяет, является ли материал блоком кодинга
     * @param material Материал
     * @return true если это блок кодинга
     *
     * Checks if material is a coding block
     * @param material Material
     * @return true if it's a coding block
     *
     * Prüft, ob das Material ein Codierblock ist
     * @param material Material
     * @return true, wenn es ein Codierblock ist
     */
    boolean isCodingBlock(Material material);
    
    /**
     * Получает все доступные типы блоков
     * @return Список типов блоков
     *
     * Gets all available block types
     * @return List of block types
     *
     * Ruft alle verfügbaren Blocktypen ab
     * @return Liste der Blocktypen
     */
    List<String> getAvailableBlockTypes();
    
    /**
     * Получает все доступные материалы
     * @return Список материалов
     *
     * Gets all available materials
     * @return List of materials
     *
     * Ruft alle verfügbaren Materialien ab
     * @return Liste der Materialien
     */
    List<Material> getAvailableMaterials();
    
    /**
     * Добавляет новый тип блока
     * @param blockType Тип блока
     * @param material Материал
     * @param actions Действия
     * @param conditions Условия
     *
     * Adds a new block type
     * @param blockType Block type
     * @param material Material
     * @param actions Actions
     * @param conditions Conditions
     *
     * Fügt einen neuen Blocktyp hinzu
     * @param blockType Blocktyp
     * @param material Material
     * @param actions Aktionen
     * @param conditions Bedingungen
     */
    void addBlockType(String blockType, Material material, List<String> actions, List<String> conditions);
    
    /**
     * Удаляет тип блока
     * @param blockType Тип блока
     *
     * Removes block type
     * @param blockType Block type
     *
     * Entfernt Blocktyp
     * @param blockType Blocktyp
     */
    void removeBlockType(String blockType);
    
    /**
     * Обновляет конфигурацию блока
     * @param blockType Тип блока
     * @param material Материал
     * @param actions Действия
     * @param conditions Условия
     *
     * Updates block configuration
     * @param blockType Block type
     * @param material Material
     * @param actions Actions
     * @param conditions Conditions
     *
     * Aktualisiert die Blockkonfiguration
     * @param blockType Blocktyp
     * @param material Material
     * @param actions Aktionen
     * @param conditions Bedingungen
     */
    void updateBlockType(String blockType, Material material, List<String> actions, List<String> conditions);
    
    /**
     * Получает полную конфигурацию блоков
     * @return Карта конфигурации
     *
     * Gets full block configuration
     * @return Configuration map
     *
     * Ruft die vollständige Blockkonfiguration ab
     * @return Konfigurationskarte
     */
    Map<String, Object> getFullConfiguration();
    
    /**
     * Перезагружает конфигурацию
     * Полная перезагрузка всех данных конфигурации
     * Сброс кэша и обновление внутренних структур
     *
     * Reloads configuration
     * Full reload of all configuration data
     * Cache reset and internal structure update
     *
     * Lädt die Konfiguration neu
     * Vollständiges Neuladen aller Konfigurationsdaten
     * Cache-Reset und Aktualisierung interner Strukturen
     */
    void reloadConfiguration();
}