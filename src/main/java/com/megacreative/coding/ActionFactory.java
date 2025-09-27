package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.util.ClassScanner;
import com.megacreative.coding.events.EventPublisher;
import com.megacreative.coding.events.CustomEvent;
import com.megacreative.coding.values.DataValue;
import com.megacreative.interfaces.IActionFactory;
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