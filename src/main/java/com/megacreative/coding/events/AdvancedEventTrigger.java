package com.megacreative.coding.events;

import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Advanced event trigger system supporting event chaining, conditional triggering, and scheduled events
 */
public class AdvancedEventTrigger {
    private static final Logger log = Logger.getLogger(AdvancedEventTrigger.class.getName());
    
    private final String triggerId;
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
    
    private long createdTime;
    private final AtomicInteger executionCount = new AtomicInteger(0);
    private boolean isActive = true;
    
    // Getters and Setters
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
    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }
    public int getExecutionCount() { return executionCount.get(); }
    public void incrementExecutionCount() { executionCount.incrementAndGet(); }
    
    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdvancedEventTrigger that = (AdvancedEventTrigger) o;
        return Objects.equals(triggerId, that.triggerId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(triggerId);
    }
    
    @Override
    public String toString() {
        return "AdvancedEventTrigger{" +
               "triggerId='" + triggerId + '\'' +
               ", eventName='" + eventName + '\'' +
               ", ownerId=" + ownerId +
               ", isGlobal=" + isGlobal +
               '}';
    }
    public AdvancedEventTrigger(String triggerId, String eventName, Map<String, DataValue> eventData,
                               TriggerCondition condition, List<EventChain> eventChains,
                               long delayMs, int repeatCount, long repeatIntervalMs,
                               UUID ownerId, String worldName, boolean isGlobal) {
        this.triggerId = triggerId;
        this.eventName = eventName;
        this.eventData = new ConcurrentHashMap<>(eventData);
        this.condition = condition;
        this.eventChains = new ArrayList<>(eventChains);
        this.delayMs = delayMs;
        this.repeatCount = repeatCount;
        this.repeatIntervalMs = repeatIntervalMs;
        this.ownerId = ownerId;
        this.worldName = worldName;
        this.isGlobal = isGlobal;
        this.createdTime = System.currentTimeMillis();
    }
    
    /**
     * Checks if this trigger can be executed for the given player and world
     */
    public boolean canExecute(Player player, String world) {
        if (!isActive) return false;
        
        // Check owner restriction
        if (ownerId != null && player != null && !ownerId.equals(player.getUniqueId())) {
            return false;
        }
        
        // Check world restriction
        if (worldName != null && !isGlobal && world != null && !worldName.equals(world)) {
            return false;
        }
        
        // Check condition
        if (condition != null && player != null && !condition.test(player)) {
            return false;
        }
        
        // Check repeat limit
        if (repeatCount > 0 && executionCount.get() >= repeatCount) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Executes this trigger
     */
    public void execute(CustomEventManager eventManager, Player player, String world) {
        if (!canExecute(player, world)) {
            return;
        }
        
        // Trigger the main event
        eventManager.triggerEvent(eventName, eventData, player, world);
        
        // Execute event chains
        for (EventChain chain : eventChains) {
            chain.execute(eventManager, player, world, eventData);
        }
        
        // Handle repeat logic
        if (repeatCount > 0 && executionCount.incrementAndGet() >= repeatCount) {
            isActive = false;
        }
    }
    
    /**
     * Trigger condition interface
     */
    @FunctionalInterface
    public interface TriggerCondition extends Predicate<Player> {
        boolean test(Player player);
    }
    
    /**
     * Event chain that can be triggered after the main event
     */
    public static class EventChain {
        private final String chainedEventName;
        private final Map<String, Object> chainedEventData;
        private final long delayMs;
        private final TriggerCondition condition;
        
        public String getChainedEventName() { return chainedEventName; }
        public Map<String, Object> getChainedEventData() { return new HashMap<>(chainedEventData); }
        public long getDelayMs() { return delayMs; }
        public TriggerCondition getCondition() { return condition; }
        
        public EventChain(String chainedEventName, Map<String, Object> chainedEventData, 
                         long delayMs, TriggerCondition condition) {
            this.chainedEventName = chainedEventName;
            this.chainedEventData = new HashMap<>(chainedEventData);
            this.delayMs = delayMs;
            this.condition = condition;
        }
        
        public void execute(CustomEventManager eventManager, Player player, String world, 
                          Map<String, DataValue> parentEventData) {
            // Check condition
            if (condition != null && player != null && !condition.test(player)) {
                return;
            }
            
            // Prepare chained event data
            Map<String, DataValue> finalData = new HashMap<>();
            for (Map.Entry<String, Object> entry : chainedEventData.entrySet()) {
                // Handle data transformation from parent event
                if (entry.getValue() instanceof String && ((String) entry.getValue()).startsWith("$")) {
                    String parentKey = ((String) entry.getValue()).substring(1);
                    if (parentEventData.containsKey(parentKey)) {
                        finalData.put(entry.getKey(), parentEventData.get(parentKey));
                    }
                } else {
                    finalData.put(entry.getKey(), DataValue.fromObject(entry.getValue()));
                }
            }
            
            // Schedule or immediate execution
            if (delayMs > 0) {
                // Schedule delayed execution
                scheduleDelayedExecution(eventManager, player, world, finalData, delayMs);
            } else {
                // Immediate execution
                eventManager.triggerEvent(chainedEventName, finalData, player, world);
            }
        }
        
        private void scheduleDelayedExecution(CustomEventManager eventManager, Player player, 
                                           String world, Map<String, DataValue> data, long delay) {
            // In a real implementation, this would use Bukkit's scheduler
            // For now, we'll simulate with a simple delayed execution
            new Thread(() -> {
                try {
                    Thread.sleep(delay);
                    eventManager.triggerEvent(chainedEventName, data, player, world);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
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
        
        public AdvancedEventTrigger build() {
            return new AdvancedEventTrigger(
                triggerId, eventName, eventData, condition, eventChains,
                delayMs, repeatCount, repeatIntervalMs,
                ownerId, worldName, isGlobal
            );
        }
    }
}