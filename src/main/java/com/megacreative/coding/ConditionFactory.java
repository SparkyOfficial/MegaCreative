package com.megacreative.coding;

import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.util.ClassScanner;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ConditionFactory {

    private static final Logger LOGGER = Logger.getLogger(ConditionFactory.class.getName());
    
    private final Map<String, Supplier<BlockCondition>> conditionMap = new HashMap<>();
    private final Map<String, String> conditionDisplayNames = new HashMap<>();

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
                                return null;
                            }
                        });
                    } catch (NoSuchMethodException e) {
                        LOGGER.warning("No suitable constructor found for condition class: " + clazz.getName());
                    } catch (Exception e) {
                        LOGGER.warning("Error registering condition class " + clazz.getName() + ": " + e.getMessage());
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
}