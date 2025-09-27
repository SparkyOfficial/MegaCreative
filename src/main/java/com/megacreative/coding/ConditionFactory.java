package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.utils.ClassScanner;
import com.megacreative.coding.events.EventPublisher;
import com.megacreative.coding.events.CustomEvent;
import com.megacreative.interfaces.IConditionFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ConditionFactory implements IConditionFactory {

    private static final Logger LOGGER = Logger.getLogger(ConditionFactory.class.getName());
    
    private final MegaCreative plugin;
    private final Map<String, Supplier<BlockCondition>> conditionRegistry = new HashMap<>();
    private final Map<String, String> conditionDisplayNames = new HashMap<>();

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