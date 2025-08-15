package com.megacreative.interfaces;

import com.megacreative.coding.CodeScript;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс для управления шаблонами
 */
public interface ITemplateManager {
    
    /**
     * Инициализация менеджера шаблонов
     */
    void initialize();
    
    /**
     * Создает новый шаблон
     * @param name Имя шаблона
     * @param description Описание шаблона
     * @param script Скрипт шаблона
     * @param creator Создатель шаблона
     */
    void createTemplate(String name, String description, CodeScript script, Player creator);
    
    /**
     * Получает шаблон по имени
     * @param name Имя шаблона
     * @return Шаблон или null, если не найден
     */
    CodeScript getTemplate(String name);
    
    /**
     * Получает все шаблоны
     * @return Список всех шаблонов
     */
    List<CodeScript> getAllTemplates();
    
    /**
     * Получает шаблоны игрока
     * @param player Игрок
     * @return Список шаблонов игрока
     */
    List<CodeScript> getPlayerTemplates(Player player);
    
    /**
     * Получает публичные шаблоны
     * @return Список публичных шаблонов
     */
    List<CodeScript> getPublicTemplates();
    
    /**
     * Удаляет шаблон
     * @param name Имя шаблона
     * @param requester Игрок, запрашивающий удаление
     */
    void deleteTemplate(String name, Player requester);
    
    /**
     * Обновляет шаблон
     * @param name Имя шаблона
     * @param script Новый скрипт
     * @param updater Игрок, обновляющий шаблон
     */
    void updateTemplate(String name, CodeScript script, Player updater);
    
    /**
     * Проверяет, существует ли шаблон
     * @param name Имя шаблона
     * @return true если шаблон существует
     */
    boolean templateExists(String name);
    
    /**
     * Получает метаданные шаблона
     * @param name Имя шаблона
     * @return Метаданные шаблона
     */
    Map<String, Object> getTemplateMetadata(String name);
    
    /**
     * Сохраняет все шаблоны
     */
    void saveAllTemplates();
    
    /**
     * Загружает все шаблоны
     */
    void loadAllTemplates();
}
