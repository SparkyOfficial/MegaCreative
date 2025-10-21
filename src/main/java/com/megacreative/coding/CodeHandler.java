package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.activators.Activator;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized script execution manager that handles the flow from activators to actions.
 * This class serves as the "glue" that connects the event system to the script execution system.
 * 
 * CodeHandler is responsible for:
 * 1. Managing all activators in a creative world
 * 2. Receiving events from activators
 * 3. Finding and executing the appropriate scripts
 * 4. Coordinating the execution flow
 * 
 * Централизованный менеджер выполнения скриптов, который обрабатывает поток от активаторов к действиям.
 * Этот класс служит "клеем", который соединяет систему событий с системой выполнения скриптов.
 * 
 * CodeHandler отвечает за:
 * 1. Управление всеми активаторами в творческом мире
 * 2. Получение событий от активаторов
 * 3. Поиск и выполнение соответствующих скриптов
 * 4. Координацию потока выполнения
 * 
 * @author Андрій Budильников
 */
public class CodeHandler {
    
    private static final Logger LOGGER = Logger.getLogger(CodeHandler.class.getName());
    
    private final MegaCreative plugin;
    private final CreativeWorld creativeWorld;
    private final Map<UUID, Activator> activators;
    private final ScriptEngine scriptEngine;
    
    public CodeHandler(MegaCreative plugin, CreativeWorld creativeWorld) {
        this.plugin = plugin;
        this.creativeWorld = creativeWorld;
        this.activators = new ConcurrentHashMap<>();
        this.scriptEngine = plugin.getServiceRegistry().getScriptEngine();
    }
    
    /**
     * Registers an activator with this code handler
     * @param activator The activator to register
     * 
     * Регистрирует активатор с этим обработчиком кода
     * @param activator Активатор для регистрации
     */
    public void registerActivator(Activator activator) {
        activators.put(activator.getId(), activator);
        LOGGER.info("Registered activator: " + activator.getId() + " of type: " + activator.getType() + " with name: " + activator.getCustomName());
    }
    
    /**
     * Unregisters an activator from this code handler
     * @param activatorId The ID of the activator to unregister
     * 
     * Отменяет регистрацию активатора из этого обработчика кода
     * @param activatorId ID активатора для отмены регистрации
     */
    public void unregisterActivator(UUID activatorId) {
        Activator removed = activators.remove(activatorId);
        if (removed != null) {
            LOGGER.info("Unregistered activator: " + activatorId);
        }
    }
    
    /**
     * Gets an activator by its ID
     * @param activatorId The ID of the activator to get
     * @return The activator, or null if not found
     * 
     * Получает активатор по его ID
     * @param activatorId ID активатора для получения
     * @return Активатор или null, если не найден
     */
    public Activator getActivator(UUID activatorId) {
        return activators.get(activatorId);
    }
    
    /**
     * Gets all activators of a specific type
     * @param type The activator type to filter by
     * @return List of activators of the specified type
     * 
     * Получает все активаторы определенного типа
     * @param type Тип активатора для фильтрации
     * @return Список активаторов указанного типа
     */
    public List<Activator> getActivatorsByType(com.megacreative.coding.activators.ActivatorType type) {
        List<Activator> result = new ArrayList<>();
        for (Activator activator : activators.values()) {
            if (activator.getType() == type) {
                result.add(activator);
            }
        }
        return result;
    }
    
    /**
     * Handles an event by executing all activators of the matching type
     * @param activatorType The type of activator to trigger
     * @param gameEvent The game event containing context data
     * @param player The player associated with the event (can be null)
     * 
     * Обрабатывает событие, выполняя все активаторы соответствующего типа
     * @param activatorType Тип активатора для запуска
     * @param gameEvent Игровое событие, содержащее контекстные данные
     * @param player Игрок, связанный с событием (может быть null)
     */
    public void handleEvent(com.megacreative.coding.activators.ActivatorType activatorType, GameEvent gameEvent, Player player) {
        
        List<Activator> typeActivators = getActivatorsByType(activatorType);
        
        if (typeActivators.isEmpty()) {
            LOGGER.fine("No activators found for type: " + activatorType);
            return;
        }
        
        LOGGER.info("Handling activator type: " + activatorType + " with " + typeActivators.size() + " activators");
        
        
        for (Activator activator : typeActivators) {
            try {
                LOGGER.info("Executing activator: " + activator.getId() + " of type: " + activatorType + " with name: " + activator.getCustomName());
                
                activator.execute(gameEvent, 0, new AtomicInteger());
                LOGGER.info("Successfully executed activator: " + activator.getId());
            } catch (Exception e) {
                LOGGER.severe("Error executing activator " + activator.getId() + ": " + e.getMessage());
                LOGGER.severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            }
        }
    }
    
    /**
     * Gets all registered activators
     * @return Collection of all activators
     * 
     * Получает все зарегистрированные активаторы
     * @return Коллекция всех активаторов
     */
    public Collection<Activator> getAllActivators() {
        return new ArrayList<>(activators.values());
    }
    
    /**
     * Gets the creative world this code handler manages
     * @return The creative world
     * 
     * Получает творческий мир, которым управляет этот обработчик кода
     * @return Творческий мир
     */
    public CreativeWorld getCreativeWorld() {
        return creativeWorld;
    }
    
    /**
     * Gets the script engine used by this code handler
     * @return The script engine
     * 
     * Получает движок скриптов, используемый этим обработчиком кода
     * @return Движок скриптов
     */
    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }
    
    /**
     * Clears all activators from this code handler
     * 
     * Очищает все активаторы из этого обработчика кода
     */
    public void clearActivators() {
        activators.clear();
        LOGGER.info("Cleared all activators");
    }
    
    /**
     * Gets the number of registered activators
     * @return The count of activators
     * 
     * Получает количество зарегистрированных активаторов
     * @return Количество активаторов
     */
    public int getActivatorCount() {
        return activators.size();
    }
    
    /**
     * Starts a game loop with the specified name
     * @param name The name of the game loop
     * @param gameEvent The game event to pass to the loop
     * 
     * Запускает игровой цикл с указанным именем
     * @param name Имя игрового цикла
     * @param gameEvent Игровое событие для передачи в цикл
     */
    public void startGameLoop(String name, GameEvent gameEvent) {
        
        List<Activator> gameLoops = getActivatorsByType(com.megacreative.coding.activators.ActivatorType.GAME_LOOP);
        for (Activator activator : gameLoops) {
            if (name.equals(activator.getCustomName())) {
                
                activator.execute(gameEvent, 0, new AtomicInteger());
                break;
            }
        }
    }
    
    /**
     * Calls a function with the specified name
     * @param name The name of the function to call
     * @param gameEvent The game event to pass to the function
     * 
     * Вызывает функцию с указанным именем
     * @param name Имя функции для вызова
     * @param gameEvent Игровое событие для передачи в функцию
     */
    public void callFunction(String name, GameEvent gameEvent) {
        
        List<Activator> functions = getActivatorsByType(com.megacreative.coding.activators.ActivatorType.FUNCTION);
        for (Activator activator : functions) {
            if (name.equals(activator.getCustomName())) {
                
                activator.execute(gameEvent, 0, new AtomicInteger());
                break;
            }
        }
    }
}