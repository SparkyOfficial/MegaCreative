package com.megacreative.coding;

import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.core.DependencyContainer;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.util.ClassScanner;
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
public class ActionFactory {
    private static final Logger LOGGER = java.util.logging.Logger.getLogger(ActionFactory.class.getName());

    private final Map<String, Supplier<BlockAction>> actionMap = new HashMap<>();
    private final DependencyContainer dependencyContainer;
    
    // Map to store action display names
    private final Map<String, String> actionDisplayNames = new HashMap<>();
    
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
                                        return null;
                                    }
                                });
                            } catch (NoSuchMethodException ex) {
                                LOGGER.warning("No suitable constructor found for action class: " + clazz.getName());
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.warning("Error registering action class " + clazz.getName() + ": " + e.getMessage());
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
}