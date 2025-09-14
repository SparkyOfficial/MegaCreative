package com.megacreative.interfaces;

import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Интерфейс для управления кодингом и скриптами
 * Управление загрузкой, выполнением и хранением скриптов
 * Работа с переменными и движком выполнения
 *
 * Interface for coding and script management
 * Management of script loading, execution and storage
 * Working with variables and execution engine
 *
 * Schnittstelle zur Verwaltung von Codierung und Skripten
 * Verwaltung des Skriptladens, der Ausführung und der Speicherung
 * Arbeiten mit Variablen und Ausführungsengine
 */
public interface ICodingManager {
    
    /**
     * Загружает скрипты для мира
     * @param world Мир
     *
     * Loads scripts for world
     * @param world World
     *
     * Lädt Skripte für die Welt
     * @param world Welt
     */
    void loadScriptsForWorld(CreativeWorld world);
    
    /**
     * Выгружает скрипты для мира
     * @param world Мир
     *
     * Unloads scripts for world
     * @param world World
     *
     * Entlädt Skripte für die Welt
     * @param world Welt
     */
    void unloadScriptsForWorld(CreativeWorld world);
    
    /**
     * Выполняет скрипт
     * @param script Скрипт для выполнения
     * @param player Игрок
     * @param trigger Триггер выполнения
     *
     * Executes script
     * @param script Script to execute
     * @param player Player
     * @param trigger Execution trigger
     *
     * Führt Skript aus
     * @param script Auszuführendes Skript
     * @param player Spieler
     * @param trigger Auslöser der Ausführung
     */
    void executeScript(CodeScript script, Player player, String trigger);
    
    /**
     * Получает скрипт по имени
     * @param name Имя скрипта
     * @return Скрипт или null, если не найден
     *
     * Gets script by name
     * @param name Script name
     * @return Script or null if not found
     *
     * Ruft Skript nach Name ab
     * @param name Skriptname
     * @return Skript oder null, wenn nicht gefunden
     */
    CodeScript getScript(String name);
    
    /**
     * Получает все скрипты мира
     * @param world Мир
     * @return Список скриптов
     *
     * Gets all world scripts
     * @param world World
     * @return List of scripts
     *
     * Ruft alle Weltskripte ab
     * @param world Welt
     * @return Liste der Skripte
     */
    List<CodeScript> getWorldScripts(CreativeWorld world);
    
    /**
     * Сохраняет скрипт
     * @param script Скрипт для сохранения
     *
     * Saves script
     * @param script Script to save
     *
     * Speichert Skript
     * @param script Zu speicherndes Skript
     */
    void saveScript(CodeScript script);
    
    /**
     * Отменяет выполнение скрипта
     * @param scriptId ID скрипта
     *
     * Cancels script execution
     * @param scriptId Script ID
     *
     * Bricht die Skriptausführung ab
     * @param scriptId Skript-ID
     */
    void cancelScriptExecution(String scriptId);
    
    /**
     * Завершает работу менеджера кодинга
     *
     * Shuts down coding manager
     *
     * Schaltet den Codierungsmanager herunter
     */
    void shutdown();
    
    /**
     * Удаляет скрипт
     * @param scriptName Имя скрипта
     *
     * Deletes script
     * @param scriptName Script name
     *
     * Löscht Skript
     * @param scriptName Skriptname
     */
    void deleteScript(String scriptName);
    
    /**
     * Получает глобальную переменную
     * @param name Имя переменной
     * @return Значение переменной
     *
     * Gets global variable
     * @param name Variable name
     * @return Variable value
     *
     * Ruft globale Variable ab
     * @param name Variablenname
     * @return Variablenwert
     */
    Object getGlobalVariable(String name);
    
    /**
     * Устанавливает глобальную переменную
     * @param name Имя переменной
     * @param value Значение переменной
     *
     * Sets global variable
     * @param name Variable name
     * @param value Variable value
     *
     * Setzt globale Variable
     * @param name Variablenname
     * @param value Variablenwert
     */
    void setGlobalVariable(String name, Object value);
    
    /**
     * Получает серверную переменную
     * @param name Имя переменной
     * @return Значение переменной
     *
     * Gets server variable
     * @param name Variable name
     * @return Variable value
     *
     * Ruft Servervariable ab
     * @param name Variablenname
     * @return Variablenwert
     */
    Object getServerVariable(String name);
    
    /**
     * Устанавливает серверную переменную
     * @param name Имя переменной
     * @param value Значение переменной
     *
     * Sets server variable
     * @param name Variable name
     * @param value Variable value
     *
     * Setzt Servervariable
     * @param name Variablenname
     * @param value Variablenwert
     */
    void setServerVariable(String name, Object value);
    
    /**
     * Получает все глобальные переменные
     * @return Карта переменных
     *
     * Gets all global variables
     * @return Variables map
     *
     * Ruft alle globalen Variablen ab
     * @return Variablenkarte
     */
    Map<String, Object> getGlobalVariables();
    
    /**
     * Получает все серверные переменные
     * @return Карта переменных
     *
     * Gets all server variables
     * @return Variables map
     *
     * Ruft alle Servervariablen ab
     * @return Variablenkarte
     */
    Map<String, Object> getServerVariables();
    
    /**
     * Очищает все переменные
     *
     * Clears all variables
     *
     * Löscht alle Variablen
     */
    void clearVariables();
    
    /**
     * Получает движок выполнения скриптов
     * @return ScriptEngine для выполнения скриптов
     *
     * Gets script execution engine
     * @return ScriptEngine for script execution
     *
     * Ruft die Skriptausführungsengine ab
     * @return ScriptEngine für die Skriptausführung
     */
    ScriptEngine getScriptEngine();
    
}