package com.megacreative.coding;

import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.core.DependencyContainer;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.util.ClassScanner;
import com.megacreative.coding.events.EventPublisher;
import com.megacreative.coding.events.CustomEvent;
import com.megacreative.coding.values.DataValue;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Factory for creating block actions using annotation-based discovery
 *
 * Фабрика для создания действий блоков с использованием обнаружения аннотаций
 *
 * Fabrik zum Erstellen von Blockaktionen mit anmerkungsbasierter Erkennung
 */
public class ActionFactory implements EventPublisher {
    private static final Logger LOGGER = java.util.logging.Logger.getLogger(ActionFactory.class.getName());

    private final Map<String, Supplier<BlockAction>> actionMap = new HashMap<>();
    private final DependencyContainer dependencyContainer;
    
    // Map to store action display names
    private final Map<String, String> actionDisplayNames = new HashMap<>();
    
    // Constants for action registration events
    private static final String EVENT_ACTION_REGISTERED = "action_registered";
    private static final String EVENT_ACTION_REGISTRATION_FAILED = "action_registration_failed";
    
    /**
     * Creates an ActionFactory
     * @param dependencyContainer Dependency container for resolving dependencies
     *
     * Создает ActionFactory
     * @param dependencyContainer Контейнер зависимостей для разрешения зависимостей
     *
     * Erstellt eine ActionFactory
     * @param dependencyContainer Abhängigkeitscontainer zum Auflösen von Abhängigkeiten
     */
    public ActionFactory(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
    }

    /**
     * Scans for annotated actions and registers them
     */
    public void registerAllActions() {
        // Scan packages for annotated actions
        String basePackage = "com.megacreative.coding.actions";
        
        for (Class<?> clazz : ClassScanner.findClasses(dependencyContainer.resolve(com.megacreative.MegaCreative.class), basePackage)) {
            if (BlockAction.class.isAssignableFrom(clazz) && !clazz.isInterface() && clazz.isAnnotationPresent(BlockMeta.class)) {
                BlockMeta meta = clazz.getAnnotation(BlockMeta.class);
                if (meta.type() == BlockType.ACTION) {
                    try {
                        // Try constructor with MegaCreative parameter
                        try {
                            java.lang.reflect.Constructor<? extends BlockAction> constructor = 
                                clazz.asSubclass(BlockAction.class).getConstructor(com.megacreative.MegaCreative.class);
                            register(meta.id(), meta.displayName(), () -> {
                                try {
                                    com.megacreative.MegaCreative plugin = dependencyContainer.resolve(com.megacreative.MegaCreative.class);
                                    return constructor.newInstance(plugin);
                                } catch (Exception e) {
                                    LOGGER.warning("Failed to create action instance with plugin parameter: " + e.getMessage());
                                    // Publish registration failure event
                                    Map<String, DataValue> eventData = new HashMap<>();
                                    eventData.put("action_id", DataValue.fromObject(meta.id()));
                                    eventData.put("class_name", DataValue.fromObject(clazz.getName()));
                                    eventData.put("error", DataValue.fromObject(e.getMessage()));
                                    publishEvent(EVENT_ACTION_REGISTRATION_FAILED, eventData);
                                    return null;
                                }
                            });
                        } catch (NoSuchMethodException e) {
                            // Try no-argument constructor
                            try {
                                java.lang.reflect.Constructor<? extends BlockAction> constructor = clazz.asSubclass(BlockAction.class).getConstructor();
                                register(meta.id(), meta.displayName(), () -> {
                                    try {
                                        return constructor.newInstance();
                                    } catch (Exception ex) {
                                        LOGGER.warning("Failed to create action instance: " + ex.getMessage());
                                        // Publish registration failure event
                                        Map<String, DataValue> eventData = new HashMap<>();
                                        eventData.put("action_id", DataValue.fromObject(meta.id()));
                                        eventData.put("class_name", DataValue.fromObject(clazz.getName()));
                                        eventData.put("error", DataValue.fromObject(ex.getMessage()));
                                        publishEvent(EVENT_ACTION_REGISTRATION_FAILED, eventData);
                                        return null;
                                    }
                                });
                            } catch (NoSuchMethodException ex) {
                                LOGGER.warning("No suitable constructor found for action class: " + clazz.getName());
                                // Publish registration failure event
                                Map<String, DataValue> eventData = new HashMap<>();
                                eventData.put("action_id", DataValue.fromObject(meta.id()));
                                eventData.put("class_name", DataValue.fromObject(clazz.getName()));
                                eventData.put("error", DataValue.fromObject("No suitable constructor found"));
                                publishEvent(EVENT_ACTION_REGISTRATION_FAILED, eventData);
                            }
                        }
                        
                        // Publish successful registration event
                        Map<String, DataValue> eventData = new HashMap<>();
                        eventData.put("action_id", DataValue.fromObject(meta.id()));
                        eventData.put("display_name", DataValue.fromObject(meta.displayName()));
                        eventData.put("class_name", DataValue.fromObject(clazz.getName()));
                        publishEvent(EVENT_ACTION_REGISTERED, eventData);
                    } catch (Exception e) {
                        LOGGER.warning("Error registering action class " + clazz.getName() + ": " + e.getMessage());
                        // Publish registration failure event
                        Map<String, DataValue> eventData = new HashMap<>();
                        eventData.put("action_id", DataValue.fromObject(clazz.getName()));
                        eventData.put("error", DataValue.fromObject(e.getMessage()));
                        publishEvent(EVENT_ACTION_REGISTRATION_FAILED, eventData);
                    }
                }
            }
        }
        
        LOGGER.info("Загружено " + actionMap.size() + " действий блоков.");
    }

    /**
     * Register an action with display name
     */
    private void register(String actionId, String displayName, Supplier<BlockAction> supplier) {
        actionMap.put(actionId, supplier);
        actionDisplayNames.put(actionId, displayName);
    }

    /**
     * Creates an action by ID
     * @param actionId Action ID
     * @return BlockAction or null if not found
     *
     * Создает действие по ID
     * @param actionId ID действия
     * @return BlockAction или null, если не найдено
     *
     * Erstellt eine Aktion nach ID
     * @param actionId Aktions-ID
     * @return BlockAction oder null, wenn nicht gefunden
     */
    public BlockAction createAction(String actionId) {
        Supplier<BlockAction> supplier = actionMap.get(actionId);
        if (supplier != null) {
            return supplier.get();
        }
        return null;
    }

    /**
     * Gets the display name for an action
     * 
     * @param actionId The action ID
     * @return The display name, or the action ID if no display name is set
     */
    public String getActionDisplayName(String actionId) {
        return actionDisplayNames.getOrDefault(actionId, actionId);
    }
    
    /**
     * Gets all registered action display names
     * 
     * @return A map of action IDs to display names
     */
    public Map<String, String> getActionDisplayNames() {
        return new HashMap<>(actionDisplayNames);
    }
    
    /**
     * Gets the action count
     * @return Number of registered actions
     *
     * Получает количество действий
     * @return Количество зарегистрированных действий
     *
     * Ruft die Aktionsanzahl ab
     * @return Anzahl der registrierten Aktionen
     */
    public int getActionCount() {
        return actionMap.size();
    }
    
    /**
     * Publishes an event to the event system.
     * 
     * @param event The event to publish
     */
    @Override
    public void publishEvent(CustomEvent event) {
        // In a real implementation, this would send the event to the EventDispatcher
        // For now, we'll just log it
        LOGGER.info("Published event: " + event.getName());
    }
    
    /**
     * Publishes an event with associated data to the event system.
     * 
     * @param eventName The name of the event
     * @param eventData The data associated with the event
     */
    @Override
    public void publishEvent(String eventName, Map<String, DataValue> eventData) {
        // In a real implementation, this would send the event to the EventDispatcher
        // For now, we'll just log it
        LOGGER.info("Published event: " + eventName + " with data: " + eventData.size() + " fields");
    }
}