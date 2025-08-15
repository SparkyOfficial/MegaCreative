package com.megacreative.interfaces;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс для управления конфигурацией блоков
 */
public interface IBlockConfigManager {
    
    /**
     * Инициализация менеджера конфигурации блоков
     */
    void initialize();
    
    /**
     * Загружает конфигурацию блоков
     */
    void loadBlockConfiguration();
    
    /**
     * Сохраняет конфигурацию блоков
     */
    void saveBlockConfiguration();
    
    /**
     * Получает действия для материала
     * @param material Материал блока
     * @return Список действий
     */
    List<String> getActionsForMaterial(Material material);
    
    /**
     * Получает условия для материала
     * @param material Материал блока
     * @return Список условий
     */
    List<String> getConditionsForMaterial(Material material);
    
    /**
     * Получает материал для типа блока
     * @param blockType Тип блока
     * @return Материал или null
     */
    Material getMaterialForBlockType(String blockType);
    
    /**
     * Получает тип блока для материала
     * @param material Материал
     * @return Тип блока или null
     */
    String getBlockTypeForMaterial(Material material);
    
    /**
     * Проверяет, является ли материал блоком кодинга
     * @param material Материал
     * @return true если это блок кодинга
     */
    boolean isCodingBlock(Material material);
    
    /**
     * Получает все доступные типы блоков
     * @return Список типов блоков
     */
    List<String> getAvailableBlockTypes();
    
    /**
     * Получает все доступные материалы
     * @return Список материалов
     */
    List<Material> getAvailableMaterials();
    
    /**
     * Добавляет новый тип блока
     * @param blockType Тип блока
     * @param material Материал
     * @param actions Действия
     * @param conditions Условия
     */
    void addBlockType(String blockType, Material material, List<String> actions, List<String> conditions);
    
    /**
     * Удаляет тип блока
     * @param blockType Тип блока
     */
    void removeBlockType(String blockType);
    
    /**
     * Обновляет конфигурацию блока
     * @param blockType Тип блока
     * @param material Материал
     * @param actions Действия
     * @param conditions Условия
     */
    void updateBlockType(String blockType, Material material, List<String> actions, List<String> conditions);
    
    /**
     * Получает полную конфигурацию блоков
     * @return Карта конфигурации
     */
    Map<String, Object> getFullConfiguration();
    
    /**
     * Перезагружает конфигурацию
     */
    void reloadConfiguration();
}
