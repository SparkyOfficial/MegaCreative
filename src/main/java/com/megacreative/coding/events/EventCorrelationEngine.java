package com.megacreative.coding.events;

import com.megacreative.coding.values.DataValue;
import lombok.Data;
import lombok.extern.java.Log;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * Event correlation engine for detecting complex event patterns and sequences
 */
@Log
public class EventCorrelationEngine {
    
    private final CustomEventManager eventManager;
    
    // Pattern definitions
    private final Map<String, EventPattern> patterns = new ConcurrentHashMap<>();
    
    // Active pattern instances being tracked
    private final Map<String, PatternInstance> activeInstances = new ConcurrentHashMap<>();
    
    // Pattern completion listeners
    private final List<PatternCompletionListener> completionListeners = new CopyOnWriteArrayList<>();
    
    public EventCorrelationEngine(CustomEventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    /**
     * Registers an event pattern
     */
    public void registerPattern(EventPattern pattern) {
        patterns.put(pattern.getPatternId(), pattern);
        log.info("Registered event pattern: " + pattern.getPatternId());
    }
    
    /**
     * Unregisters an event pattern
     */
    public void unregisterPattern(String patternId) {
        patterns.remove(patternId);
        // Clean up any active instances of this pattern
        activeInstances.entrySet().removeIf(entry -> 
            entry.getValue().getPattern().getPatternId().equals(patternId));
        log.info("Unregistered event pattern: " + patternId);
    }
    
    /**
     * Gets a registered pattern by ID
     */
    public EventPattern getPattern(String patternId) {
        return patterns.get(patternId);
    }
    
    /**
     * Processes an event for pattern matching
     */
    public void processEvent(String eventName, Map<String, DataValue> eventData, Player source, String worldName) {
        // Check all registered patterns
        for (EventPattern pattern : patterns.values()) {
            // Check if this event matches the first step of any pattern
            if (pattern.getSteps().isEmpty()) continue;
            
            EventPattern.Step firstStep = pattern.getSteps().get(0);
            if (firstStep.getEventName().equals(eventName) && 
                (firstStep.getCondition() == null || firstStep.getCondition().test(eventData))) {
                
                // Create new pattern instance
                String instanceId = UUID.randomUUID().toString();
                PatternInstance instance = new PatternInstance(instanceId, pattern, source, worldName);
                instance.recordStep(0, eventName, eventData);
                activeInstances.put(instanceId, instance);
                
                log.fine("Started pattern instance: " + instanceId + " for pattern: " + pattern.getPatternId());
            }
        }
        
        // Check existing pattern instances for continuation
        processActiveInstances(eventName, eventData, source, worldName);
    }
    
    /**
     * Processes active pattern instances
     */
    private void processActiveInstances(String eventName, Map<String, DataValue> eventData, 
                                      Player source, String worldName) {
        Iterator<Map.Entry<String, PatternInstance>> iterator = activeInstances.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, PatternInstance> entry = iterator.next();
            String instanceId = entry.getKey();
            PatternInstance instance = entry.getValue();
            
            // Skip if this instance is already completed or expired
            if (instance.isCompleted() || instance.isExpired()) {
                iterator.remove();
                continue;
            }
            
            // Check if this event continues the pattern
            EventPattern pattern = instance.getPattern();
            int nextStepIndex = instance.getCompletedSteps().size();
            
            if (nextStepIndex < pattern.getSteps().size()) {
                EventPattern.Step nextStep = pattern.getSteps().get(nextStepIndex);
                
                if (nextStep.getEventName().equals(eventName) && 
                    (nextStep.getCondition() == null || nextStep.getCondition().test(eventData))) {
                    
                    // Record this step
                    instance.recordStep(nextStepIndex, eventName, eventData);
                    
                    // Check if pattern is now complete
                    if (instance.getCompletedSteps().size() == pattern.getSteps().size()) {
                        instance.markCompleted();
                        iterator.remove();
                        
                        // Notify completion listeners
                        notifyPatternCompletion(instance);
                        
                        log.info("Pattern completed: " + pattern.getPatternId() + " (instance: " + instanceId + ")");
                    }
                }
                // If event doesn't match next step, instance continues waiting
            }
        }
    }
    
    /**
     * Adds a pattern completion listener
     */
    public void addCompletionListener(PatternCompletionListener listener) {
        completionListeners.add(listener);
    }
    
    /**
     * Removes a pattern completion listener
     */
    public void removeCompletionListener(PatternCompletionListener listener) {
        completionListeners.remove(listener);
    }
    
    /**
     * Notifies listeners of pattern completion
     */
    private void notifyPatternCompletion(PatternInstance instance) {
        for (PatternCompletionListener listener : completionListeners) {
            try {
                listener.onPatternCompleted(instance);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error notifying pattern completion listener", e);
            }
        }
    }
    
    /**
     * Cleans up expired pattern instances
     */
    public void cleanupExpiredInstances() {
        long now = System.currentTimeMillis();
        activeInstances.entrySet().removeIf(entry -> {
            PatternInstance instance = entry.getValue();
            if (instance.isExpired()) {
                log.fine("Cleaned up expired pattern instance: " + entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Gets statistics about active patterns
     */
    public PatternStatistics getStatistics() {
        return new PatternStatistics(
            patterns.size(),
            activeInstances.size(),
            (int) activeInstances.values().stream().filter(PatternInstance::isCompleted).count()
        );
    }
    
    /**
     * Represents an event pattern definition
     */
    @Data
    public static class EventPattern {
        private final String patternId;
        private final String name;
        private final String description;
        private final List<Step> steps;
        private final long timeoutMs; // Timeout for pattern completion
        
        public EventPattern(String patternId, String name, String description, List<Step> steps, long timeoutMs) {
            this.patternId = patternId;
            this.name = name;
            this.description = description;
            this.steps = new ArrayList<>(steps);
            this.timeoutMs = timeoutMs;
        }
        
        /**
         * A step in the event pattern
         */
        @Data
        public static class Step {
            private final String eventName;
            private final Predicate<Map<String, DataValue>> condition;
            
            public Step(String eventName) {
                this(eventName, null);
            }
            
            public Step(String eventName, Predicate<Map<String, DataValue>> condition) {
                this.eventName = eventName;
                this.condition = condition;
            }
        }
        
        /**
         * Builder for EventPattern
         */
        public static class Builder {
            private String patternId = UUID.randomUUID().toString();
            private String name;
            private String description = "";
            private List<Step> steps = new ArrayList<>();
            private long timeoutMs = 30000; // 30 seconds default
            
            public Builder(String name) {
                this.name = name;
            }
            
            public Builder patternId(String patternId) {
                this.patternId = patternId;
                return this;
            }
            
            public Builder description(String description) {
                this.description = description;
                return this;
            }
            
            public Builder addStep(String eventName) {
                steps.add(new Step(eventName));
                return this;
            }
            
            public Builder addStep(String eventName, Predicate<Map<String, DataValue>> condition) {
                steps.add(new Step(eventName, condition));
                return this;
            }
            
            public Builder timeout(long timeoutMs) {
                this.timeoutMs = timeoutMs;
                return this;
            }
            
            public EventPattern build() {
                return new EventPattern(patternId, name, description, steps, timeoutMs);
            }
        }
    }
    
    /**
     * Represents an active instance of a pattern being tracked
     */
    @Data
    public static class PatternInstance {
        private final String instanceId;
        private final EventPattern pattern;
        private final UUID playerId;
        private final String worldName;
        private final long startTime;
        
        private final List<CompletedStep> completedSteps = new ArrayList<>();
        private boolean completed = false;
        private long completionTime = 0;
        
        public PatternInstance(String instanceId, EventPattern pattern, Player player, String worldName) {
            this.instanceId = instanceId;
            this.pattern = pattern;
            this.playerId = player != null ? player.getUniqueId() : null;
            this.worldName = worldName;
            this.startTime = System.currentTimeMillis();
        }
        
        /**
         * Records a completed step
         */
        public void recordStep(int stepIndex, String eventName, Map<String, DataValue> eventData) {
            completedSteps.add(new CompletedStep(stepIndex, eventName, new HashMap<>(eventData), System.currentTimeMillis()));
        }
        
        /**
         * Marks this pattern instance as completed
         */
        public void markCompleted() {
            this.completed = true;
            this.completionTime = System.currentTimeMillis();
        }
        
        /**
         * Checks if this instance has expired
         */
        public boolean isExpired() {
            return System.currentTimeMillis() - startTime > pattern.getTimeoutMs();
        }
        
        /**
         * Gets merged event data from all completed steps
         */
        public Map<String, DataValue> getMergedEventData() {
            Map<String, DataValue> merged = new HashMap<>();
            for (CompletedStep step : completedSteps) {
                merged.putAll(step.getEventData());
            }
            return merged;
        }
        
        /**
         * Represents a completed step in the pattern
         */
        @Data
        public static class CompletedStep {
            private final int stepIndex;
            private final String eventName;
            private final Map<String, DataValue> eventData;
            private final long timestamp;
        }
    }
    
    /**
     * Listener for pattern completion events
     */
    @FunctionalInterface
    public interface PatternCompletionListener {
        void onPatternCompleted(PatternInstance instance);
    }
    
    /**
     * Statistics about pattern processing
     */
    @Data
    public static class PatternStatistics {
        private final int registeredPatterns;
        private final int activeInstances;
        private final int completedPatterns;
    }
}