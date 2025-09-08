package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.CodeBlock;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Менеджер для работы с пользовательскими функциями.
 * Позволяет сохранять, загружать и выполнять функции.
 */
public class FunctionManager {
    private final MegaCreative plugin;
    private final Map<String, CodeScript> globalFunctions = new HashMap<>();
    
    public FunctionManager(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Сохраняет функцию в указанном мире
     * @param world Мир, в котором сохраняется функция
     * @param functionName Имя функции
     * @param functionRoot Корневой блок функции
     * @return true если функция успешно сохранена, false если функция с таким именем уже существует
     */
    public boolean saveFunction(CreativeWorld world, String functionName, CodeBlock functionRoot) {
        if (world == null || functionName == null || functionRoot == null) {
            return false;
        }
        
        // Проверяем, не существует ли уже функция с таким именем
        for (CodeScript script : world.getScripts()) {
            if (script.getName().equals(functionName) && script.getType() == CodeScript.ScriptType.FUNCTION) {
                return false; // Функция с таким именем уже существует
            }
        }
        
        // Создаем новую функцию
        CodeScript function = new CodeScript(
            functionName,
            true,
            functionRoot,
            CodeScript.ScriptType.FUNCTION
        );
        
        // Добавляем функцию в мир
        world.getScripts().add(function);
        
        return true;
    }
    
    /**
     * Получает функцию по имени из указанного мира
     * @param world Мир, в котором ищется функция
     * @param functionName Имя функции
     * @return Функция или null если не найдена
     */
    public CodeScript getFunction(CreativeWorld world, String functionName) {
        if (world == null || functionName == null) {
            return null;
        }
        
        for (CodeScript script : world.getScripts()) {
            if (script.getName().equals(functionName) && script.getType() == CodeScript.ScriptType.FUNCTION) {
                return script;
            }
        }
        
        return null;
    }
    
    /**
     * Получает список всех функций в мире
     * @param world Мир, из которого получается список функций
     * @return Список функций
     */
    public List<CodeScript> getFunctions(CreativeWorld world) {
        List<CodeScript> functions = new ArrayList<>();
        
        if (world != null) {
            for (CodeScript script : world.getScripts()) {
                if (script.getType() == CodeScript.ScriptType.FUNCTION) {
                    functions.add(script);
                }
            }
        }
        
        return functions;
    }
    
    /**
     * Удаляет функцию из мира
     * @param world Мир, из которого удаляется функция
     * @param functionName Имя функции для удаления
     * @return true если функция была удалена, false если функция не найдена
     */
    public boolean removeFunction(CreativeWorld world, String functionName) {
        if (world == null || functionName == null) {
            return false;
        }
        
        CodeScript functionToRemove = null;
        for (CodeScript script : world.getScripts()) {
            if (script.getName().equals(functionName) && script.getType() == CodeScript.ScriptType.FUNCTION) {
                functionToRemove = script;
                break;
            }
        }
        
        if (functionToRemove != null) {
            return world.getScripts().remove(functionToRemove);
        }
        
        return false;
    }
    
    /**
     * Переименовывает функцию
     * @param world Мир, в котором находится функция
     * @param oldName Старое имя функции
     * @param newName Новое имя функции
     * @return true если функция была переименована, false если функция не найдена или новое имя уже занято
     */
    public boolean renameFunction(CreativeWorld world, String oldName, String newName) {
        if (world == null || oldName == null || newName == null || oldName.equals(newName)) {
            return false;
        }
        
        // Проверяем, не занято ли новое имя
        for (CodeScript script : world.getScripts()) {
            if (script.getName().equals(newName) && script.getType() == CodeScript.ScriptType.FUNCTION) {
                return false; // Новое имя уже занято
            }
        }
        
        // Ищем функцию с старым именем
        for (CodeScript script : world.getScripts()) {
            if (script.getName().equals(oldName) && script.getType() == CodeScript.ScriptType.FUNCTION) {
                script.setName(newName);
                return true;
            }
        }
        
        return false;
    }
}