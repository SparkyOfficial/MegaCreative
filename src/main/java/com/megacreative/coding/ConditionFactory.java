package com.megacreative.coding;

import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.util.ClassScanner;
import com.megacreative.coding.events.EventPublisher;
import com.megacreative.coding.events.CustomEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ConditionFactory implements EventPublisher {

    private static final Logger LOGGER = Logger.getLogger(ConditionFactory.class.getName());
    
    private final Map<String, Supplier<BlockCondition>> conditionMap = new HashMap<>();
    private final Map<String, String> conditionDisplayNames = new HashMap<>();
    
    // Constants for condition registration events
    private static final String EVENT_CONDITION_REGISTERED = "condition_registered";
    private static final String EVENT_CONDITION_REGISTRATION_FAILED = "condition_registration_failed";

    public ConditionFactory() {
        // Constructor is now empty as registration happens later
    }
    
    /**
     * Scans for annotated conditions and registers them
     */
    public void registerAllConditions() {
        // Scan packages for annotated conditions
        String basePackage = "com.megacreative.coding.conditions";
        
        com.megacreative.MegaCreative plugin = com.megacreative.MegaCreative.getInstance();
        if (plugin == null) {
            LOGGER.severe("Plugin instance not available for condition scanning");
            return;
        }
        
        for (Class<?> clazz : ClassScanner.findClasses(plugin, basePackage)) {
            if (BlockCondition.class.isAssignableFrom(clazz) && !clazz.isInterface() && clazz.isAnnotationPresent(BlockMeta.class)) {
                BlockMeta meta = clazz.getAnnotation(BlockMeta.class);
                if (meta.type() == BlockType.CONDITION) {
                    try {
                        // Try no-argument constructor
                        java.lang.reflect.Constructor<? extends BlockCondition> constructor = clazz.asSubclass(BlockCondition.class).getConstructor();
                        register(meta.id(), meta.displayName(), () -> {
                            try {
                                return constructor.newInstance();
                            } catch (Exception e) {
                                LOGGER.warning("Failed to create condition instance: " + e.getMessage());
                                // Publish registration failure event
                                Map<String, DataValue> eventData = new HashMap<>();
                                eventData.put("condition_id", DataValue.fromObject(meta.id()));
                                eventData.put("class_name", DataValue.fromObject(clazz.getName()));
                                eventData.put("error", DataValue.fromObject(e.getMessage()));
                                publishEvent(EVENT_CONDITION_REGISTRATION_FAILED, eventData);
                                return null;
                            }
                        });
                        
                        // Publish successful registration event
                        Map<String, DataValue> eventData = new HashMap<>();
                        eventData.put("condition_id", DataValue.fromObject(meta.id()));
                        eventData.put("display_name", DataValue.fromObject(meta.displayName()));
                        eventData.put("class_name", DataValue.fromObject(clazz.getName()));
                        publishEvent(EVENT_CONDITION_REGISTERED, eventData);
                    } catch (NoSuchMethodException e) {
                        LOGGER.warning("No suitable constructor found for condition class: " + clazz.getName());
                        // Publish registration failure event
                        Map<String, DataValue> eventData = new HashMap<>();
                        eventData.put("condition_id", DataValue.fromObject(meta.id()));
                        eventData.put("class_name", DataValue.fromObject(clazz.getName()));
                        eventData.put("error", DataValue.fromObject("No suitable constructor found"));
                        publishEvent(EVENT_CONDITION_REGISTRATION_FAILED, eventData);
                    } catch (Exception e) {
                        LOGGER.warning("Error registering condition class " + clazz.getName() + ": " + e.getMessage());
                        // Publish registration failure event
                        Map<String, DataValue> eventData = new HashMap<>();
                        eventData.put("condition_id", DataValue.fromObject(clazz.getName()));
                        eventData.put("error", DataValue.fromObject(e.getMessage()));
                        publishEvent(EVENT_CONDITION_REGISTRATION_FAILED, eventData);
                    }
                }
            }
        }
        
        LOGGER.info("Загружено " + conditionMap.size() + " условий блоков.");
    }
    
    private void register(String conditionId, String displayName, Supplier<BlockCondition> supplier) {
        conditionMap.put(conditionId, supplier);
        conditionDisplayNames.put(conditionId, displayName);
    }

    public BlockCondition createCondition(String conditionId) {
        Supplier<BlockCondition> supplier = conditionMap.get(conditionId);
        if (supplier != null) {
            return supplier.get();
        }
        return null;
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
        return conditionMap.size();
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