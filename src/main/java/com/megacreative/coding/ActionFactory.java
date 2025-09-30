package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.utils.ClassScanner;
import com.megacreative.coding.events.EventPublisher;
import com.megacreative.coding.events.CustomEvent;
import com.megacreative.coding.values.DataValue;
import com.megacreative.interfaces.IActionFactory;
import com.megacreative.coding.events.CustomEventManager;
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
public class ActionFactory implements IActionFactory {
    private static final Logger LOGGER = java.util.logging.Logger.getLogger(ActionFactory.class.getName());

    private final Map<String, Supplier<BlockAction>> actionRegistry = new HashMap<>();
    private final Map<String, String> actionDisplayNames = new HashMap<>();
    private final MegaCreative plugin;
    private CustomEventManager eventManager;

    public ActionFactory(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Scans for annotated actions and registers them
     */
    public void registerAllActions() {
        actionRegistry.clear();
        actionDisplayNames.clear();
        String basePackage = "com.megacreative.coding.actions";

        for (Class<?> clazz : ClassScanner.findClasses(plugin, basePackage)) {
            if (BlockAction.class.isAssignableFrom(clazz) && !clazz.isInterface() && clazz.isAnnotationPresent(BlockMeta.class)) {
                BlockMeta meta = clazz.getAnnotation(BlockMeta.class);
                if (meta.type() == BlockType.ACTION) {
                    try {
                        Supplier<BlockAction> supplier = () -> {
                            try {
                                // Try constructor with MegaCreative parameter first
                                try {
                                    java.lang.reflect.Constructor<? extends BlockAction> constructor = 
                                        clazz.asSubclass(BlockAction.class).getConstructor(MegaCreative.class);
                                    return constructor.newInstance(plugin);
                                } catch (NoSuchMethodException e) {
                                    // Fallback to no-argument constructor
                                    java.lang.reflect.Constructor<? extends BlockAction> constructor = 
                                        clazz.asSubclass(BlockAction.class).getConstructor();
                                    return constructor.newInstance();
                                }
                            } catch (Exception e) {
                                LOGGER.severe("Не удалось создать экземпляр действия: " + clazz.getName());
                                e.printStackTrace();
                                return null;
                            }
                        };
                        register(meta.id(), meta.displayName(), supplier);
                    } catch (Exception e) {
                        LOGGER.severe("Не удалось зарегистрировать действие из класса (нужен пустой конструктор): " + clazz.getName());
                    }
                }
            }
        }
        LOGGER.info("Загружено " + actionRegistry.size() + " действий блоков.");
    }

    /**
     * Register an action with display name
     */
    private void register(String actionId, String displayName, Supplier<BlockAction> supplier) {
        actionRegistry.put(actionId, supplier);
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
        Supplier<BlockAction> supplier = actionRegistry.get(actionId);
        if (supplier != null) {
            return supplier.get();
        }
        return null; // Не логируем ошибку здесь, чтобы не спамить
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
        return actionRegistry.size();
    }
    
    /**
     * Publishes an event to the event system.
     * 
     * @param event The event to publish
     */
    @Override
    public void publishEvent(CustomEvent event) {
        // Get the event manager from the service registry
        if (eventManager == null) {
            eventManager = plugin.getServiceRegistry().getService(CustomEventManager.class);
        }
        
        // If we have an event manager, use it to trigger the event
        if (eventManager != null) {
            try {
                // Create event data map
                Map<String, DataValue> eventData = new HashMap<>();
                
                // Add basic event information
                eventData.put("event_id", DataValue.fromObject(event.getId().toString()));
                eventData.put("event_name", DataValue.fromObject(event.getName()));
                eventData.put("event_category", DataValue.fromObject(event.getCategory()));
                eventData.put("event_description", DataValue.fromObject(event.getDescription()));
                eventData.put("event_author", DataValue.fromObject(event.getAuthor()));
                eventData.put("event_created_time", DataValue.fromObject(event.getCreatedTime()));
                
                // Add event data fields
                for (Map.Entry<String, DataValue> entry : eventData.entrySet()) {
                    eventData.put("data_" + entry.getKey(), entry.getValue());
                }
                
                // Trigger the event through the event manager
                eventManager.triggerEvent(event.getName(), eventData, null, "global");
            } catch (Exception e) {
                LOGGER.severe("Failed to publish event through CustomEventManager: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Fallback to logging if no event manager is available
            LOGGER.info("Published event: " + event.getName());
        }
    }
    
    /**
     * Publishes an event with associated data to the event system.
     * 
     * @param eventName The name of the event
     * @param eventData The data associated with the event
     */
    @Override
    public void publishEvent(String eventName, Map<String, DataValue> eventData) {
        // Get the event manager from the service registry
        if (eventManager == null) {
            eventManager = plugin.getServiceRegistry().getService(CustomEventManager.class);
        }
        
        // If we have an event manager, use it to trigger the event
        if (eventManager != null) {
            try {
                // Trigger the event through the event manager
                eventManager.triggerEvent(eventName, eventData, null, "global");
            } catch (Exception e) {
                LOGGER.severe("Failed to publish event through CustomEventManager: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Fallback to logging if no event manager is available
            LOGGER.info("Published event: " + eventName + " with data: " + eventData.size() + " fields");
        }
    }
}