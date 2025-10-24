package com.megacreative.coding.events;

import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import java.util.logging.Logger;
import java.util.Objects;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * Event correlation engine for detecting complex event patterns and sequences
 */
public class EventCorrelationEngine {
    private static final Logger log = Logger.getLogger(EventCorrelationEngine.class.getName());
    
    private final CustomEventManager eventManager;
    
    
    // According to static analysis, this field can be converted to a local variable
    // However, it needs to be a class field to maintain state
    private final Map<String, EventPattern> patterns = new ConcurrentHashMap<>();
    
    
    private final Map<String, PatternInstance> activeInstances = new ConcurrentHashMap<>();
    
    
    private final List<PatternCompletionListener> completionListeners = new CopyOnWriteArrayList<>();
    
    public EventCorrelationEngine(CustomEventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    /**
     * Registers an event pattern
     */
    public void registerPattern(EventPattern pattern) {
        patterns.put(pattern.getPatternId(), pattern);
        log.fine("Registered event pattern: " + pattern.getPatternId());
    }
    
    /**
     * Unregisters an event pattern
     */
    public void unregisterPattern(String patternId) {
        patterns.remove(patternId);
        
        activeInstances.entrySet().removeIf(entry -> 
            entry.getValue().getPattern().getPatternId().equals(patternId));
        log.fine("Unregistered event pattern: " + patternId);
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
        
        for (EventPattern pattern : patterns.values()) {
            
            if (pattern.getSteps().isEmpty()) continue;
            
            EventPattern.Step firstStep = pattern.getSteps().get(0);
            if (firstStep.getEventName().equals(eventName) && 
                (firstStep.getCondition() == null || firstStep.getCondition().test(eventData))) {
                
                
                String instanceId = UUID.randomUUID().toString();
                PatternInstance instance = new PatternInstance(instanceId, pattern, source, worldName);
                instance.recordStep(0, eventName, eventData);
                activeInstances.put(instanceId, instance);
                
                log.fine("Started pattern instance: " + instanceId + " for pattern: " + pattern.getPatternId());
            }
        }
        
        
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
            
            
            if (instance.isCompleted() || instance.isExpired()) {
                iterator.remove();
                continue;
            }
            
            
            EventPattern pattern = instance.getPattern();
            int nextStepIndex = instance.getCompletedSteps().size();
            
            if (nextStepIndex < pattern.getSteps().size()) {
                EventPattern.Step nextStep = pattern.getSteps().get(nextStepIndex);
                
                if (nextStep.getEventName().equals(eventName) && 
                    (nextStep.getCondition() == null || nextStep.getCondition().test(eventData))) {
                    
                    
                    instance.recordStep(nextStepIndex, eventName, eventData);
                    
                    
                    if (instance.getCompletedSteps().size() == pattern.getSteps().size()) {
                        instance.markCompleted();
                        iterator.remove();
                        
                        
                        notifyPatternCompletion(instance);
                        
                        log.fine("Pattern completed: " + pattern.getPatternId() + " (instance: " + instanceId + ")");
                    }
                }
                
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
    public static class EventPattern {
        private final String patternId;
        private final String name;
        private final String description;
        private final List<Step> steps;
        private final long timeoutMs; 
        
        public String getPatternId() { return patternId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<Step> getSteps() { return new ArrayList<>(steps); }
        public long getTimeoutMs() { return timeoutMs; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EventPattern that = (EventPattern) o;
            return timeoutMs == that.timeoutMs &&
                   Objects.equals(patternId, that.patternId) &&
                   Objects.equals(name, that.name) &&
                   Objects.equals(description, that.description) &&
                   Objects.equals(steps, that.steps);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(patternId, name, description, steps, timeoutMs);
        }
        
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
        public static class Step {
            private final String eventName;
            private final Predicate<Map<String, DataValue>> condition;
            
            public String getEventName() { return eventName; }
            public Predicate<Map<String, DataValue>> getCondition() { return condition; }
            
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Step step = (Step) o;
                return Objects.equals(eventName, step.eventName) &&
                       Objects.equals(condition, step.condition);
            }
            
            @Override
            public int hashCode() {
                return Objects.hash(eventName, condition);
            }
            
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
            private final List<Step> steps = new ArrayList<>();
            private long timeoutMs = 30000; 
            
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
    public static class PatternInstance {
        private final String instanceId;
        private final EventPattern pattern;
        private final UUID playerId;
        private final String worldName;
        private final long startTime;
        
        private final List<CompletedStep> completedSteps = new ArrayList<>();
        private boolean completed = false;
        private long completionTime = 0;
        
        public String getInstanceId() { return instanceId; }
        public EventPattern getPattern() { return pattern; }
        public UUID getPlayerId() { return playerId; }
        public String getWorldName() { return worldName; }
        public long getStartTime() { return startTime; }
        public List<CompletedStep> getCompletedSteps() { return new ArrayList<>(completedSteps); }
        public boolean isCompleted() { return completed; }
        public long getCompletionTime() { return completionTime; }
        
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
        public static class CompletedStep {
            private final int stepIndex;
            private final String eventName;
            private final Map<String, DataValue> eventData;
            private final long timestamp;
            
            public int getStepIndex() { return stepIndex; }
            public String getEventName() { return eventName; }
            public Map<String, DataValue> getEventData() { return new HashMap<>(eventData); }
            public long getTimestamp() { return timestamp; }
            
            public CompletedStep(int stepIndex, String eventName, Map<String, DataValue> eventData, long timestamp) {
                this.stepIndex = stepIndex;
                this.eventName = eventName;
                this.eventData = new HashMap<>(eventData);
                this.timestamp = timestamp;
            }
            
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                CompletedStep that = (CompletedStep) o;
                return stepIndex == that.stepIndex &&
                       timestamp == that.timestamp &&
                       Objects.equals(eventName, that.eventName) &&
                       Objects.equals(eventData, that.eventData);
            }
            
            @Override
            public int hashCode() {
                return Objects.hash(stepIndex, eventName, eventData, timestamp);
            }
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
    public static class PatternStatistics {
        private final int registeredPatterns;
        private final int activeInstances;
        private final int completedPatterns;
        
        public int getRegisteredPatterns() { return registeredPatterns; }
        public int getActiveInstances() { return activeInstances; }
        public int getCompletedPatterns() { return completedPatterns; }
        
        public PatternStatistics(int registeredPatterns, int activeInstances, int completedPatterns) {
            this.registeredPatterns = registeredPatterns;
            this.activeInstances = activeInstances;
            this.completedPatterns = completedPatterns;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PatternStatistics that = (PatternStatistics) o;
            return registeredPatterns == that.registeredPatterns &&
                   activeInstances == that.activeInstances &&
                   completedPatterns == that.completedPatterns;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(registeredPatterns, activeInstances, completedPatterns);
        }
    }
}