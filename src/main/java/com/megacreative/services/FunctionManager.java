package com.megacreative.services;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для управления пользовательскими функциями в движке скриптов
 * Позволяет определять и вызывать повторно используемые блоки кода
 *
 * Service for managing custom functions in the scripting engine
 * Allows defining and calling reusable blocks of code
 *
 * Dienst zur Verwaltung benutzerdefinierter Funktionen in der Skript-Engine
 * Ermöglicht das Definieren und Aufrufen wiederverwendbarer Codeblöcke
 */
public class FunctionManager {
    
    private final MegaCreative plugin;
    
    // Map of worlds to their function definitions
    // Карта миров и их определений функций
    // Karte der Welten zu ihren Funktionsdefinitionen
    // Key: world name, Value: map of function name to first block of function
    // Ключ: имя мира, Значение: карта имени функции к первому блоку функции
    // Schlüssel: Weltname, Wert: Karte des Funktionsnamens zum ersten Block der Funktion
    private final Map<String, Map<String, CodeBlock>> worldFunctions = new ConcurrentHashMap<>();
    
    /**
     * Инициализирует менеджер функций
     * @param plugin Экземпляр основного плагина
     *
     * Initializes function manager
     * @param plugin Main plugin instance
     *
     * Initialisiert den Funktionsmanager
     * @param plugin Hauptplugin-Instanz
     */
    public FunctionManager(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Регистрирует определение функции для определенного мира
     * @param worldName Имя мира
     * @param functionName Имя функции
     * @param firstBlock Первый блок в функции (точка входа)
     *
     * Registers a function definition for a specific world
     * @param worldName The name of the world
     * @param functionName The name of the function
     * @param firstBlock The first block in the function (entry point)
     *
     * Registriert eine Funktionsdefinition für eine bestimmte Welt
     * @param worldName Der Name der Welt
     * @param functionName Der Name der Funktion
     * @param firstBlock Der erste Block in der Funktion (Einstiegspunkt)
     */
    public void registerFunction(String worldName, String functionName, CodeBlock firstBlock) {
        worldFunctions.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>())
                     .put(functionName, firstBlock);
        plugin.getLogger().info("Registered function '" + functionName + "' in world '" + worldName + "'");
        // Зарегистрированная функция в мире
        // Registrierte Funktion in Welt
        // Registrierte Funktion in der Welt
    }
    
    /**
     * Получает определение функции для определенного мира
     * @param worldName Имя мира
     * @param functionName Имя функции
     * @return Первый блок функции или null, если не найден
     *
     * Gets a function definition for a specific world
     * @param worldName The name of the world
     * @param functionName The name of the function
     * @return The first block of the function, or null if not found
     *
     * Ruft eine Funktionsdefinition für eine bestimmte Welt ab
     * @param worldName Der Name der Welt
     * @param functionName Der Name der Funktion
     * @return Der erste Block der Funktion oder null, wenn nicht gefunden
     */
    public CodeBlock getFunction(String worldName, String functionName) {
        Map<String, CodeBlock> functions = worldFunctions.get(worldName);
        if (functions != null) {
            return functions.get(functionName);
        }
        return null;
    }
    
    /**
     * Проверяет, существует ли функция в определенном мире
     * @param worldName Имя мира
     * @param functionName Имя функции
     * @return true, если функция существует, иначе false
     *
     * Checks if a function exists in a specific world
     * @param worldName The name of the world
     * @param functionName The name of the function
     * @return true if the function exists, false otherwise
     *
     * Prüft, ob eine Funktion in einer bestimmten Welt existiert
     * @param worldName Der Name der Welt
     * @param functionName Der Name der Funktion
     * @return true, wenn die Funktion existiert, sonst false
     */
    public boolean functionExists(String worldName, String functionName) {
        Map<String, CodeBlock> functions = worldFunctions.get(worldName);
        return functions != null && functions.containsKey(functionName);
    }
    
    /**
     * Удаляет все функции для определенного мира
     * @param worldName Имя мира
     *
     * Removes all functions for a specific world
     * @param worldName The name of the world
     *
     * Entfernt alle Funktionen für eine bestimmte Welt
     * @param worldName Der Name der Welt
     */
    public void clearWorldFunctions(String worldName) {
        worldFunctions.remove(worldName);
        plugin.getLogger().info("Cleared all functions for world '" + worldName + "'");
        // Очищены все функции для мира
        // Alle Funktionen für Welt gelöscht
        // Alle Funktionen für die Welt gelöscht
    }
    
    /**
     * Получает все имена функций для определенного мира
     * @param worldName Имя мира
     * @return Массив имен функций
     *
     * Gets all function names for a specific world
     * @param worldName The name of the world
     * @return Array of function names
     *
     * Ruft alle Funktionsnamen für eine bestimmte Welt ab
     * @param worldName Der Name der Welt
     * @return Array von Funktionsnamen
     */
    public String[] getFunctionNames(String worldName) {
        Map<String, CodeBlock> functions = worldFunctions.get(worldName);
        if (functions != null) {
            return functions.keySet().toArray(new String[0]);
        }
        return new String[0];
    }
    
    /**
     * Очищает все функции (используется при отключении плагина)
     *
     * Clears all functions (used when plugin is disabled)
     *
     * Löscht alle Funktionen (wird verwendet, wenn das Plugin deaktiviert wird)
     */
    public void clearAllFunctions() {
        worldFunctions.clear();
        plugin.getLogger().info("Cleared all functions");
        // Очищены все функции
        // Alle Funktionen gelöscht
        // Alle Funktionen gelöscht
    }
}