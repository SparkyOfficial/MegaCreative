package com.megacreative.coding.monitoring.model;

import com.megacreative.coding.monitoring.OptimizationPriority;
import java.util.*;

/**
 * Report containing optimization suggestions for a script
 */
public class ScriptOptimizationReport {
    private final String scriptName;
    private final List<OptimizationSuggestion> suggestions;
    private final Date generatedAt;
    
    public ScriptOptimizationReport(String scriptName, List<OptimizationSuggestion> suggestions) {
        this.scriptName = scriptName;
        this.suggestions = suggestions != null ? new ArrayList<>(suggestions) : new ArrayList<>();
        this.generatedAt = new Date();
    }
    
    
    public String getScriptName() {
        return scriptName;
    }
    
    public List<OptimizationSuggestion> getSuggestions() {
        return new ArrayList<>(suggestions);
    }
    
    public Date getGeneratedAt() {
        return new Date(generatedAt.getTime());
    }
    
    public int getTotalSuggestions() {
        return suggestions.size();
    }
    
    public Map<OptimizationPriority, List<OptimizationSuggestion>> getSuggestionsByPriority() {
        Map<OptimizationPriority, List<OptimizationSuggestion>> byPriority = new HashMap<>();
        
        for (OptimizationSuggestion suggestion : suggestions) {
            OptimizationPriority priority = suggestion.getPriority();
            byPriority.computeIfAbsent(priority, k -> new ArrayList<>()).add(suggestion);
        }
        
        return byPriority;
    }
    
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Total suggestions: ").append(suggestions.size());
        
        Map<OptimizationPriority, List<OptimizationSuggestion>> byPriority = getSuggestionsByPriority();
        for (Map.Entry<OptimizationPriority, List<OptimizationSuggestion>> entry : byPriority.entrySet()) {
            summary.append(" | ").append(entry.getKey()).append(": ").append(entry.getValue().size());
        }
        
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return "ScriptOptimizationReport{" +
                "scriptName='" + scriptName + '\'' +
                ", totalSuggestions=" + suggestions.size() +
                ", generatedAt=" + generatedAt +
                '}';
    }
}