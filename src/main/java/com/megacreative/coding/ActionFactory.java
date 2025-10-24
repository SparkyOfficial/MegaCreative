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
import java.util.logging.Level;
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
     * 
     * Сканирует аннотированные действия и регистрирует их
     * 
     * Scannt nach annotierten Aktionen und registriert sie
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
                                
                                try {
                                    java.lang.reflect.Constructor<? extends BlockAction> constructor = 
                                        clazz.asSubclass(BlockAction.class).getConstructor(MegaCreative.class);
                                    return constructor.newInstance(plugin);
                                } catch (NoSuchMethodException e) {
                                    
                                    java.lang.reflect.Constructor<? extends BlockAction> constructor = 
                                        clazz.asSubclass(BlockAction.class).getConstructor();
                                    return constructor.newInstance();
                                }
                            } catch (Exception e) {
                                LOGGER.log(Level.WARNING, "Не удалось создать экземпляр действия: " + clazz.getName(), e);
                                return null;
                            }
                        };
                        register(meta.id(), meta.displayName(), supplier);
                    } catch (Exception e) {
                        LOGGER.warning("Не удалось зарегистрировать действие из класса (нужен пустой конструктор): " + clazz.getName());
                    }
                }
            }
        }
        LOGGER.fine("Загружено " + actionRegistry.size() + " действий блоков.");
    }

    /**
     * Register an action with display name
     * 
     * Регистрирует действие с отображаемым именем
     * 
     * Registriert eine Aktion mit Anzeigenamen
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
        return null; 
    }

    /**
     * Gets the display name for an action
     * 
     * @param actionId The action ID
     * @return The display name, or the action ID if no display name is set
     * 
     * Получает отображаемое имя для действия
     * 
     * @param actionId ID действия
     * @return Отображаемое имя или ID действия, если отображаемое имя не установлено
     * 
     * Ruft den Anzeigenamen für eine Aktion ab
     * 
     * @param actionId Die Aktions-ID
     * @return Der Anzeigename oder die Aktions-ID, wenn kein Anzeigename festgelegt ist
     */
    public String getActionDisplayName(String actionId) {
        return actionDisplayNames.getOrDefault(actionId, actionId);
    }
    
    /**
     * Gets all registered action display names
     * 
     * @return A map of action IDs to display names
     * 
     * Получает все зарегистрированные отображаемые имена действий
     * 
     * @return Карта ID действий и отображаемых имен
     * 
     * Ruft alle registrierten Aktions-Anzeigenamen ab
     * 
     * @return Eine Karte von Aktions-IDs zu Anzeigenamen
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
     * 
     * Публикует событие в системе событий.
     * 
     * @param event Событие для публикации
     * 
     * Veröffentlicht ein Ereignis im Ereignissystem.
     * 
     * @param event Das zu veröffentlichende Ereignis
     */
    @Override
    public void publishEvent(CustomEvent event) {
        
        if (eventManager == null) {
            eventManager = plugin.getServiceRegistry().getService(CustomEventManager.class);
        }
        
        
        if (eventManager != null) {
            try {
                
                Map<String, DataValue> eventData = new HashMap<>();
                
                
                eventData.put("event_id", DataValue.fromObject(event.getId().toString()));
                eventData.put("event_name", DataValue.fromObject(event.getName()));
                eventData.put("event_category", DataValue.fromObject(event.getCategory()));
                eventData.put("event_description", DataValue.fromObject(event.getDescription()));
                eventData.put("event_author", DataValue.fromObject(event.getAuthor()));
                eventData.put("event_created_time", DataValue.fromObject(event.getCreatedTime()));
                
                
                for (Map.Entry<String, DataValue> entry : eventData.entrySet()) {
                    eventData.put("data_" + entry.getKey(), entry.getValue());
                }
                
                
                eventManager.triggerEvent(event.getName(), eventData, null, "global");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to publish event through CustomEventManager: " + e.getMessage(), e);
            }
        } else {
            
            LOGGER.fine("Published event: " + event.getName());
        }
    }
    
    /**
     * Publishes an event with associated data to the event system.
     * 
     * @param eventName The name of the event
     * @param eventData The data associated with the event
     * 
     * Публикует событие с ассоциированными данными в системе событий.
     * 
     * @param eventName Название события
     * @param eventData Данные, ассоциированные с событием
     * 
     * Veröffentlicht ein Ereignis mit zugehörigen Daten im Ereignissystem.
     * 
     * @param eventName Der Name des Ereignisses
     * @param eventData Die mit dem Ereignis verknüpften Daten
     */
    @Override
    public void publishEvent(String eventName, Map<String, DataValue> eventData) {
        
        if (eventManager == null) {
            eventManager = plugin.getServiceRegistry().getService(CustomEventManager.class);
        }
        
        
        if (eventManager != null) {
            try {
                
                eventManager.triggerEvent(eventName, eventData, null, "global");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to publish event through CustomEventManager: " + e.getMessage(), e);
            }
        } else {
            
            LOGGER.fine("Published event: " + eventName + " with data: " + eventData.size() + " fields");
        }
    }
}