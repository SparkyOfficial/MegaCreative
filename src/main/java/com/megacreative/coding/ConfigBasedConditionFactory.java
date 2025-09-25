package com.megacreative.coding;

import com.megacreative.MegaCreative;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Condition factory that loads conditions from configuration instead of manual registration.
 * This eliminates the need for a large switch-case in ConditionFactory.
 */
public class ConfigBasedConditionFactory {
    private static final Logger log = Logger.getLogger(ConfigBasedConditionFactory.class.getName());
    
    private final MegaCreative plugin;
    private final Map<String, Class<? extends BlockCondition>> conditionClasses = new HashMap<>();
    
    public ConfigBasedConditionFactory(MegaCreative plugin) {
        this.plugin = plugin;
        loadConditionClasses();
    }
    
    /**
     * Loads condition classes from configuration
     */
    private void loadConditionClasses() {
        // In a real implementation, you would load this from a config file
        // For now, we'll register some common conditions
        try {
            // Load condition classes dynamically
            registerConditionClass("hasItem", "com.megacreative.coding.conditions.HasItemCondition");
            registerConditionClass("compareVariable", "com.megacreative.coding.conditions.CompareVariableCondition");
            registerConditionClass("isInWorld", "com.megacreative.coding.conditions.IsInWorldCondition");
            registerConditionClass("ifVarEquals", "com.megacreative.coding.conditions.IfVarEqualsCondition");
            registerConditionClass("ifVarGreater", "com.megacreative.coding.conditions.IfVarGreaterCondition");
            registerConditionClass("ifVarLess", "com.megacreative.coding.conditions.IfVarLessCondition");
            // Add more as needed
        } catch (Exception e) {
            log.severe("Error loading condition classes: " + e.getMessage());
        }
    }
    
    /**
     * Registers a condition class by its fully qualified name
     */
    @SuppressWarnings("unchecked")
    private void registerConditionClass(String conditionId, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (BlockCondition.class.isAssignableFrom(clazz)) {
                conditionClasses.put(conditionId, (Class<? extends BlockCondition>) clazz);
                log.info("Registered condition class: " + conditionId + " -> " + className);
            } else {
                log.warning("Class " + className + " does not implement BlockCondition interface");
            }
        } catch (ClassNotFoundException e) {
            log.warning("Condition class not found: " + className);
        }
    }
    
    /**
     * Creates a condition instance by condition ID
     */
    public BlockCondition createCondition(String conditionId) {
        Class<? extends BlockCondition> conditionClass = conditionClasses.get(conditionId);
        if (conditionClass == null) {
            log.warning("No condition class registered for condition ID: " + conditionId);
            return null;
        }
        
        try {
            // Try to find a constructor that takes MegaCreative plugin
            try {
                Constructor<? extends BlockCondition> constructor = conditionClass.getConstructor(MegaCreative.class);
                return constructor.newInstance(plugin);
            } catch (NoSuchMethodException e) {
                // Fallback to no-argument constructor
                Constructor<? extends BlockCondition> constructor = conditionClass.getConstructor();
                return constructor.newInstance();
            }
        } catch (Exception e) {
            log.severe("Error creating condition instance for " + conditionId + ": " + e.getMessage());
            return null;
        }
    }
}