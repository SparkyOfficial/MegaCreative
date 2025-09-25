package com.megacreative.coding.monitoring.model;

import com.megacreative.coding.monitoring.OptimizationPriority;
import org.bukkit.Location;

/**
 * Represents a suggestion for optimizing a script
 */
public class OptimizationSuggestion {
    private final String type;
    private final String description;
    private final String recommendation;
    private final OptimizationPriority priority;
    private final boolean autoApplicable;
    private final Location location;
    
    public OptimizationSuggestion(String type, String description, String recommendation, 
                                OptimizationPriority priority, boolean autoApplicable, Location location) {
        this.type = type;
        this.description = description;
        this.recommendation = recommendation;
        this.priority = priority;
        this.autoApplicable = autoApplicable;
        this.location = location;
    }
    
    // Getters
    public String getType() {
        return type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public OptimizationPriority getPriority() {
        return priority;
    }
    
    public boolean isAutoApplicable() {
        return autoApplicable;
    }
    
    public Location getLocation() {
        return location;
    }
    
    @Override
    public String toString() {
        return "OptimizationSuggestion{" +
                "type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                '}';
    }
}