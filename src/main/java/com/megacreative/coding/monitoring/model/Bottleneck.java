package com.megacreative.coding.monitoring.model;

/**
 * Represents a performance bottleneck
 */
public class Bottleneck {
    private final String scriptName;
    private final String actionType;
    private final String description;
    private final Severity severity;
    private final String recommendation;
    private final long timestamp;
    
    public Bottleneck(String scriptName, String actionType, String description, 
                     Severity severity, String recommendation) {
        this.scriptName = scriptName;
        this.actionType = actionType;
        this.description = description;
        this.severity = severity;
        this.recommendation = recommendation;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getScriptName() {
        return scriptName;
    }
    
    public String getActionType() {
        return actionType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
