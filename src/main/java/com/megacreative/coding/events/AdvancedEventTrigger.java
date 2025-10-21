package com.megacreative.coding.events;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class AdvancedEventTrigger {
    private static final Logger log = Logger.getLogger(AdvancedEventTrigger.class.getName());
    
    private final String triggerId;
    // Fields are final as they are used in the equals/hashCode methods
    private final String eventName;
    private final Map<String, DataValue> eventData;
    private final TriggerCondition condition;
    private final List<EventChain> eventChains;
    private final long delayMs;
    private final int repeatCount;
    private final long repeatIntervalMs;
    private final UUID ownerId;
    private final String worldName;
    private final boolean isGlobal;
    private final MegaCreative plugin; 

    public AdvancedEventTrigger(String triggerId, String eventName, Map<String, DataValue> eventData,
                               TriggerCondition condition, List<EventChain> eventChains,
                               long delayMs, int repeatCount, long repeatIntervalMs,
                               UUID ownerId, String worldName, boolean isGlobal, MegaCreative plugin) {
        this.triggerId = triggerId;
        this.eventName = eventName;
        this.eventData = eventData != null ? new HashMap<>(eventData) : new HashMap<>();
        this.condition = condition;
        this.eventChains = eventChains != null ? new ArrayList<>(eventChains) : new ArrayList<>();
        this.delayMs = delayMs;
        this.repeatCount = repeatCount;
        this.repeatIntervalMs = repeatIntervalMs;
        this.ownerId = ownerId;
        this.worldName = worldName;
        this.isGlobal = isGlobal;
        this.plugin = plugin; 
    }

    
    public String getTriggerId() { return triggerId; }
    public String getEventName() { return eventName; }
    public Map<String, DataValue> getEventData() { return new HashMap<>(eventData); }
    public TriggerCondition getCondition() { return condition; }
    public List<EventChain> getEventChains() { return new ArrayList<>(eventChains); }
    public long getDelayMs() { return delayMs; }
    public int getRepeatCount() { return repeatCount; }
    public long getRepeatIntervalMs() { return repeatIntervalMs; }
    public UUID getOwnerId() { return ownerId; }
    public String getWorldName() { return worldName; }
    public boolean isGlobal() { return isGlobal; }
    public MegaCreative getPlugin() { return plugin; } 

    /**
     * Executes the trigger
     */
    public void execute(CustomEventManager eventManager, Player player, String world) {
        
        if (condition != null && !condition.test(player, world, eventData)) {
            log.info("Trigger condition not met for: " + triggerId);
            return;
        }
        
        
        Map<String, DataValue> data = new HashMap<>(eventData);
        data.put("trigger_id", DataValue.fromObject(triggerId));
        data.put("trigger_time", DataValue.fromObject(System.currentTimeMillis()));
        
        
        if (delayMs <= 0) {
            
            eventManager.triggerEvent(eventName, data, player, world);
            
            
            for (EventChain chain : eventChains) {
                if (chain.getDelayMs() > 0) {
                    scheduleDelayedExecution(eventManager, player, world, data, chain.getDelayMs());
                } else {
                    eventManager.triggerEvent(chain.getChainedEventName(), data, player, world);
                }
            }
        } else {
            
            scheduleDelayedExecution(eventManager, player, world, data, delayMs);
        }
    }
    
    private void scheduleDelayedExecution(CustomEventManager eventManager, Player player, 
                                       String world, Map<String, DataValue> data, long delay) {
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            eventManager.triggerEvent(eventName, data, player, world);
        }, delay / 50); 
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdvancedEventTrigger that = (AdvancedEventTrigger) o;
        return delayMs == that.delayMs &&
               repeatCount == that.repeatCount &&
               repeatIntervalMs == that.repeatIntervalMs &&
               isGlobal == that.isGlobal &&
               Objects.equals(triggerId, that.triggerId) &&
               Objects.equals(eventName, that.eventName) &&
               Objects.equals(eventData, that.eventData) &&
               Objects.equals(condition, that.condition) &&
               Objects.equals(eventChains, that.eventChains) &&
               Objects.equals(ownerId, that.ownerId) &&
               Objects.equals(worldName, that.worldName) &&
               Objects.equals(plugin, that.plugin);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(triggerId, eventName, eventData, condition, eventChains,
                           delayMs, repeatCount, repeatIntervalMs, ownerId, worldName, isGlobal, plugin);
    }
    
    @Override
    public String toString() {
        return "AdvancedEventTrigger{" +
               "triggerId='" + triggerId + '\'' +
               ", eventName='" + eventName + '\'' +
               ", delayMs=" + delayMs +
               ", repeatCount=" + repeatCount +
               ", isGlobal=" + isGlobal +
               '}';
    }
    
    /**
     * Builder for AdvancedEventTrigger
     */
    public static class Builder {
        private String triggerId = UUID.randomUUID().toString();
        private String eventName;
        private Map<String, DataValue> eventData = new HashMap<>();
        private TriggerCondition condition;
        private List<EventChain> eventChains = new ArrayList<>();
        private long delayMs = 0;
        private int repeatCount = 1;
        private long repeatIntervalMs = 0;
        private UUID ownerId;
        private String worldName;
        private boolean isGlobal = false;
        private MegaCreative plugin; 

        public Builder(String eventName) {
            this.eventName = eventName;
        }
        
        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }
        
        public Builder eventData(Map<String, DataValue> eventData) {
            this.eventData = new HashMap<>(eventData);
            return this;
        }
        
        public Builder addEventData(String key, DataValue value) {
            this.eventData.put(key, value);
            return this;
        }
        
        public Builder condition(TriggerCondition condition) {
            this.condition = condition;
            return this;
        }
        
        public Builder addEventChain(EventChain chain) {
            this.eventChains.add(chain);
            return this;
        }
        
        public Builder delay(long delayMs) {
            this.delayMs = delayMs;
            return this;
        }
        
        public Builder repeat(int count, long intervalMs) {
            this.repeatCount = count;
            this.repeatIntervalMs = intervalMs;
            return this;
        }
        
        public Builder owner(UUID ownerId) {
            this.ownerId = ownerId;
            return this;
        }
        
        public Builder world(String worldName) {
            this.worldName = worldName;
            return this;
        }
        
        public Builder global(boolean isGlobal) {
            this.isGlobal = isGlobal;
            return this;
        }
        
        public Builder plugin(MegaCreative plugin) { 
            this.plugin = plugin;
            return this;
        }
        
        public AdvancedEventTrigger build() {
            return new AdvancedEventTrigger(
                triggerId, eventName, eventData, condition, eventChains,
                delayMs, repeatCount, repeatIntervalMs,
                ownerId, worldName, isGlobal, plugin
            );
        }
    }
    
    
    @FunctionalInterface
    public interface TriggerCondition {
        boolean test(Player player, String world, Map<String, DataValue> eventData);
    }
    
    public static class EventChain {
        private final String chainedEventName;
        private final long delayMs;
        
        public EventChain(String chainedEventName, long delayMs) {
            this.chainedEventName = chainedEventName;
            this.delayMs = delayMs;
        }
        
        public String getChainedEventName() { return chainedEventName; }
        public long getDelayMs() { return delayMs; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EventChain that = (EventChain) o;
            return delayMs == that.delayMs &&
                   Objects.equals(chainedEventName, that.chainedEventName);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(chainedEventName, delayMs);
        }
    }
}