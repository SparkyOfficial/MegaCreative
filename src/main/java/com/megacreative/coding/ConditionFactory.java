package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.utils.ClassScanner;
import com.megacreative.coding.events.EventPublisher;
import com.megacreative.coding.events.CustomEvent;
import com.megacreative.interfaces.IConditionFactory;
import com.megacreative.coding.events.CustomEventManager;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ConditionFactory implements IConditionFactory {

    private static final Logger LOGGER = Logger.getLogger(ConditionFactory.class.getName());
    
    private final MegaCreative plugin;
    private final Map<String, Supplier<BlockCondition>> conditionRegistry = new HashMap<>();
    private final Map<String, String> conditionDisplayNames = new HashMap<>();
    private CustomEventManager eventManager;

    public ConditionFactory(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Scans for annotated conditions and registers them
     */
    public void registerAllConditions() {
        conditionRegistry.clear();
        conditionDisplayNames.clear();
        String basePackage = "com.megacreative.coding.conditions";
        
        if (plugin == null) {
            LOGGER.severe("Plugin instance not available for condition scanning");
            return;
        }

        for (Class<?> clazz : ClassScanner.findClasses(plugin, basePackage)) {
            if (BlockCondition.class.isAssignableFrom(clazz) && !clazz.isInterface() && clazz.isAnnotationPresent(BlockMeta.class)) {
                BlockMeta meta = clazz.getAnnotation(BlockMeta.class);
                if (meta.type() == BlockType.CONDITION) {
                    try {
                        // Capture values in effectively final variables
                        String conditionId = meta.id();
                        String displayName = meta.displayName();
                        String className = clazz.getName();
                        
                        Supplier<BlockCondition> supplier = () -> {
                            try {
                                // Create new instance with no-argument constructor
                                java.lang.reflect.Constructor<? extends BlockCondition> constructor = 
                                    clazz.asSubclass(BlockCondition.class).getConstructor();
                                return constructor.newInstance();
                            } catch (Exception e) {
                                LOGGER.severe("Не удалось создать экземпляр условия: " + className);
                                e.printStackTrace();
                                return null;
                            }
                        };
                        register(conditionId, displayName, supplier);
                    } catch (Exception e) {
                        LOGGER.severe("Не удалось зарегистрировать условие из класса (нужен пустой конструктор): " + clazz.getName());
                    }
                }
            }
        }
        
        LOGGER.info("Загружено " + conditionRegistry.size() + " условий блоков.");
    }
    
    private void register(String conditionId, String displayName, Supplier<BlockCondition> supplier) {
        conditionRegistry.put(conditionId, supplier);
        conditionDisplayNames.put(conditionId, displayName);
    }

    public BlockCondition createCondition(String conditionId) {
        Supplier<BlockCondition> supplier = conditionRegistry.get(conditionId);
        if (supplier != null) {
            return supplier.get();
        }
        return null; // Не логируем ошибку здесь, чтобы не спамить
    }
    
    /**
     * Gets the display name for a condition
     * 
     * @param conditionId The condition ID
     * @return The display name, or the condition ID if no display name is set
     */
    public String getConditionDisplayName(String conditionId) {
        return conditionDisplayNames.getOrDefault(conditionId, conditionId);
    }
    
    /**
     * Gets all registered condition display names
     * 
     * @return A map of condition IDs to display names
     */
    public Map<String, String> getConditionDisplayNames() {
        return new HashMap<>(conditionDisplayNames);
    }
    
    public int getConditionCount() {
        return conditionRegistry.size();
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