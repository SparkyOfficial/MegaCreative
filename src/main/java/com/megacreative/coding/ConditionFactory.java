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
import java.util.logging.Level;

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
     * 
     * Сканирует аннотированные условия и регистрирует их
     * 
     * Scannt nach annotierten Bedingungen und registriert sie
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
                        
                        String conditionId = meta.id();
                        String displayName = meta.displayName();
                        String className = clazz.getName();
                        
                        Supplier<BlockCondition> supplier = createConditionSupplier(clazz, className);
                        register(conditionId, displayName, supplier);
                    } catch (Exception e) {
                        LOGGER.warning("Не удалось зарегистрировать условие из класса (нужен пустой конструктор): " + clazz.getName());
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

    /**
     * Creates a supplier for a block condition
     * @param clazz The class of the block condition
     * @param className The name of the class
     * @return A supplier that creates instances of the block condition
     * 
     * Создает поставщика для условия блока
     * @param clazz Класс условия блока
     * @param className Имя класса
     * @return Поставщик, который создает экземпляры условия блока
     * 
     * Erstellt einen Lieferanten für eine Blockbedingung
     * @param clazz Die Klasse der Blockbedingung
     * @param className Der Name der Klasse
     * @return Ein Lieferant, der Instanzen der Blockbedingung erstellt
     */
    private Supplier<BlockCondition> createConditionSupplier(Class<?> clazz, String className) {
        return () -> {
            try {
                java.lang.reflect.Constructor<? extends BlockCondition> constructor = 
                    clazz.asSubclass(BlockCondition.class).getConstructor();
                return constructor.newInstance();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Не удалось создать экземпляр условия: " + className, e);
                return null;
            }
        };
    }

    public BlockCondition createCondition(String conditionId) {
        Supplier<BlockCondition> supplier = conditionRegistry.get(conditionId);
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
     * 
     * Получает отображаемое имя для условия
     * 
     * @param conditionId ID условия
     * @return Отображаемое имя или ID условия, если отображаемое имя не установлено
     * 
     * Ruft den Anzeigenamen für eine Bedingung ab
     * 
     * @param conditionId Die Bedingungs-ID
     * @return Der Anzeigename oder die Bedingungs-ID, wenn kein Anzeigename festgelegt ist
     */
    public String getConditionDisplayName(String conditionId) {
        return conditionDisplayNames.getOrDefault(conditionId, conditionId);
    }
    
    /**
     * Gets all registered condition display names
     * 
     * @return A map of condition IDs to display names
     * 
     * Получает все зарегистрированные отображаемые имена условий
     * 
     * @return Карта ID условий и отображаемых имен
     * 
     * Ruft alle registrierten Bedingungs-Anzeigenamen ab
     * 
     * @return Eine Karte von Bedingungs-IDs zu Anzeigenamen
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
            
            LOGGER.info("Published event: " + event.getName());
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
     * @param eventName Имя события
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
            
            LOGGER.info("Published event: " + eventName + " with data: " + eventData.size() + " fields");
        }
    }
}