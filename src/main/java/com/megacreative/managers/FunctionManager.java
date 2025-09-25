package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.CodeBlock;
import com.megacreative.models.CreativeWorld;
import com.megacreative.coding.functions.FunctionDefinition;
import com.megacreative.coding.functions.AdvancedFunctionManager;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Менеджер для работы с пользовательскими функциями.
 * Позволяет сохранять, загружать и выполнять функции.
 * Enhanced with call stack management and integration with AdvancedFunctionManager.
 */
public class FunctionManager {
    private final MegaCreative plugin;
    private final AdvancedFunctionManager advancedFunctionManager;
    private final Map<String, CodeScript> globalFunctions = new HashMap<>();
    
    public FunctionManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.advancedFunctionManager = new AdvancedFunctionManager(plugin);
    }
    
    /**
     * Gets the advanced function manager
     */
    public AdvancedFunctionManager getAdvancedFunctionManager() {
        return advancedFunctionManager;
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
        
        // Register with AdvancedFunctionManager
        registerWithAdvancedManager(function, world);
        
        return true;
    }
    
    /**
     * Registers a function with the AdvancedFunctionManager
     */
    private void registerWithAdvancedManager(CodeScript functionScript, CreativeWorld world) {
        try {
            // Create function parameters based on the script
            List<FunctionDefinition.FunctionParameter> parameters = new ArrayList<>();
            
            // Extract parameters from the function root block
            CodeBlock rootBlock = functionScript.getRootBlock();
            if (rootBlock != null) {
                // Add parameters from block configuration
                Map<String, DataValue> params = rootBlock.getParameters();
                int paramIndex = 0;
                for (Map.Entry<String, DataValue> entry : params.entrySet()) {
                    String paramName = entry.getKey();
                    DataValue paramValue = entry.getValue();
                    
                    // Skip non-parameter entries
                    if (paramName.startsWith("param_")) {
                        String cleanName = paramName.substring(6); // Remove "param_" prefix
                        ValueType paramType = paramValue != null ? paramValue.getType() : ValueType.TEXT;
                        parameters.add(new FunctionDefinition.FunctionParameter(
                            cleanName, 
                            paramType, 
                            true, 
                            paramValue, 
                            "Parameter " + cleanName
                        ));
                        paramIndex++;
                    }
                }
            }
            
            // Create function definition
            FunctionDefinition functionDef = new FunctionDefinition(
                functionScript.getName(),
                functionScript.getDescription(),
                null, // owner will be set by scope
                parameters,
                functionScript.getBlocks(), // function blocks
                ValueType.ANY, // return type - can be anything
                FunctionDefinition.FunctionScope.WORLD // scope within the world
            );
            
            // Register the function
            advancedFunctionManager.registerFunction(functionDef);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register function with AdvancedFunctionManager: " + e.getMessage());
        }
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
            boolean removed = world.getScripts().remove(functionToRemove);
            if (removed) {
                // Also remove from AdvancedFunctionManager
                // Note: AdvancedFunctionManager doesn't have a direct remove method in the current implementation
                plugin.getLogger().info("Removed function: " + functionName);
            }
            return removed;
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
    
    /**
     * Executes a function with the given arguments
     * @param functionName The name of the function to execute
     * @param caller The player calling the function
     * @param arguments The arguments to pass to the function
     * @return A CompletableFuture with the execution result
     */
    public CompletableFuture<ExecutionResult> executeFunction(String functionName, Player caller, DataValue[] arguments) {
        if (functionName == null || caller == null) {
            return CompletableFuture.completedFuture(
                ExecutionResult.error("Invalid function call parameters"));
        }
        
        // Delegate to AdvancedFunctionManager
        return advancedFunctionManager.executeFunction(functionName, caller, arguments);
    }
    
    /**
     * Gets function statistics and performance data
     * @return A map containing function system statistics
     */
    public Map<String, Object> getFunctionStatistics() {
        return advancedFunctionManager.getFunctionStatistics();
    }
    
    /**
     * Gets all functions available to a specific player
     * @param player The player to get functions for
     * @return A list of available functions
     */
    public List<FunctionDefinition> getAvailableFunctions(Player player) {
        if (player == null) {
            return new ArrayList<>();
        }
        return advancedFunctionManager.getAvailableFunctions(player);
    }
    
    /**
     * Shutdown the function manager and clean up resources
     */
    public void shutdown() {
        if (advancedFunctionManager != null) {
            advancedFunctionManager.shutdown();
        }
    }
}