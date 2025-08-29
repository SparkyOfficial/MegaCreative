package com.megacreative.interfaces;

import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ScriptExecutor;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс для управления кодингом и скриптами
 */
public interface ICodingManager {
    
    /**
     * Загружает скрипты для мира
     * @param world Мир
     */
    void loadScriptsForWorld(CreativeWorld world);
    
    /**
     * Выгружает скрипты для мира
     * @param world Мир
     */
    void unloadScriptsForWorld(CreativeWorld world);
    
    /**
     * Выполняет скрипт
     * @param script Скрипт для выполнения
     * @param player Игрок
     * @param trigger Триггер выполнения
     */
    void executeScript(CodeScript script, Player player, String trigger);
    
    /**
     * Получает скрипт по имени
     * @param name Имя скрипта
     * @return Скрипт или null, если не найден
     */
    CodeScript getScript(String name);
    
    /**
     * Получает все скрипты мира
     * @param world Мир
     * @return Список скриптов
     */
    List<CodeScript> getWorldScripts(CreativeWorld world);
    
    /**
     * Сохраняет скрипт
     * @param script Скрипт для сохранения
     */
    void saveScript(CodeScript script);
    
    /**
     * Отменяет выполнение скрипта
     * @param scriptId ID скрипта
     */
    void cancelScriptExecution(String scriptId);
    
    /**
     * Завершает работу менеджера кодинга
     */
    void shutdown();
    
    /**
     * Удаляет скрипт
     * @param scriptName Имя скрипта
     */
    void deleteScript(String scriptName);
    
    /**
     * Получает глобальную переменную
     * @param name Имя переменной
     * @return Значение переменной
     */
    Object getGlobalVariable(String name);
    
    /**
     * Устанавливает глобальную переменную
     * @param name Имя переменной
     * @param value Значение переменной
     */
    void setGlobalVariable(String name, Object value);
    
    /**
     * Получает серверную переменную
     * @param name Имя переменной
     * @return Значение переменной
     */
    Object getServerVariable(String name);
    
    /**
     * Устанавливает серверную переменную
     * @param name Имя переменной
     * @param value Значение переменной
     */
    void setServerVariable(String name, Object value);
    
    /**
     * Получает все глобальные переменные
     * @return Карта переменных
     */
    Map<String, Object> getGlobalVariables();
    
    /**
     * Получает все серверные переменные
     * @return Карта переменных
     */
    Map<String, Object> getServerVariables();
    
    /**
     * Очищает все переменные
     */
    void clearVariables();
    
    /**
     * Получает исполнитель скриптов
     */
    ScriptExecutor getScriptExecutor();
}
