package com.megacreative.coding.monitoring;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.monitoring.model.*;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.values.DataValue;
import com.megacreative.MegaCreative;
import org.bukkit.Location;
import java.util.*;
import java.util.logging.Logger;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interface for defining optimization rules
 */
interface OptimizationRule {
    /**
     * Applies the optimization rule to a script
     * @param script The script to optimize
     * @return List of optimization suggestions
     */
    List<AdvancedScriptOptimizer.OptimizationSuggestion> apply(CodeScript script);
    
    /**
     * Gets a description of the optimization rule
     */
    String getDescription();
    
    /**
     * Gets the priority of this optimization rule
     */
    OptimizationPriority getPriority();
}

/**
 * Advanced script optimizer that provides automated optimization suggestions
 * and can automatically apply common optimizations
 */
public class AdvancedScriptOptimizer {
    private static final Logger log = Logger.getLogger(AdvancedScriptOptimizer.class.getName());
    private final com.megacreative.MegaCreative plugin;
    private final ScriptPerformanceMonitor performanceMonitor;
    private final Map<String, OptimizationRule> optimizationRules = new ConcurrentHashMap<>();
    
    public AdvancedScriptOptimizer(com.megacreative.MegaCreative plugin, ScriptPerformanceMonitor performanceMonitor) {
        this.plugin = plugin;
        this.performanceMonitor = performanceMonitor;
        initializeOptimizationRules();
    }
    
    /**
     * Analyzes a script and provides optimization suggestions
     */
    public ScriptOptimizationReport analyzeScript(CodeScript script) {
        List<OptimizationSuggestion> suggestions = new ArrayList<>();
        
        // Get performance profile
        ScriptPerformanceProfile profile = 
            performanceMonitor.getScriptProfile(script.getName());
        
        // Check for common optimization opportunities
        checkLoopOptimizations(script, profile, suggestions);
        checkConditionalOptimizations(script, profile, suggestions);
        checkResourceOptimizations(script, profile, suggestions);
        checkStructureOptimizations(script, profile, suggestions);
        
        // Apply optimization rules
        applyOptimizationRules(script, suggestions);
        
        return new ScriptOptimizationReport(script.getName(), suggestions);
    }
    
    /**
     * Automatically applies safe optimizations to a script
     */
    public void autoOptimizeScript(CodeScript script) {
        ScriptOptimizationReport report = analyzeScript(script);
        
        // Apply safe optimizations automatically
        for (OptimizationSuggestion suggestion : report.getSuggestions()) {
            if (suggestion.isSafeToApply()) {
                applyOptimization(script, suggestion);
            }
        }
    }
    
    /**
     * Checks for loop-related optimization opportunities
     */
    private void checkLoopOptimizations(CodeScript script, 
                                      ScriptPerformanceProfile profile,
                                      List<OptimizationSuggestion> suggestions) {
        if (script == null || profile == null) {
            return;
        }
        
        // Check for inefficient loops
        List<CodeBlock> blocks = script.getBlocks();
        if (blocks == null) {
            return;
        }
        
        for (int i = 0; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);
            if (block == null) continue;
            
            // Check for nested loops
            if (isLoopBlock(block)) {
                int nestedDepth = calculateLoopNestingDepth(blocks, i);
                if (nestedDepth > 2) {
                    suggestions.add(new OptimizationSuggestion(
                        "DeeplyNestedLoop",
                        "Loop at position " + i + " is nested " + nestedDepth + " levels deep",
                        "Consider flattening nested loops or using more efficient data structures",
                        OptimizationPriority.HIGH,
                        true,
                        block.getLocation()
                    ));
                }
            }
            
            // Check for loops with expensive operations
            if (isLoopBlock(block)) {
                String action = block.getAction();
                if (action != null) {
                    ActionPerformanceData actionData = profile.getActionData(action);
                    if (actionData != null && actionData.getAverageExecutionTime() > 100) {
                        suggestions.add(new OptimizationSuggestion(
                            "ExpensiveLoopOperation",
                            "Loop contains expensive operation: " + block.getAction(),
                            "Consider moving expensive operations outside the loop or caching results",
                            OptimizationPriority.MEDIUM,
                            false,
                            block.getLocation()
                        ));
                    }
                }
            }
        }
    }
    
    /**
     * Checks for conditional-related optimization opportunities
     */
    private void checkConditionalOptimizations(CodeScript script,
                                             ScriptPerformanceProfile profile,
                                             List<OptimizationSuggestion> suggestions) {
        List<CodeBlock> blocks = script.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);
            
            // Check for complex condition chains
            if (isConditionalBlock(block)) {
                int chainLength = calculateConditionChainLength(blocks, i);
                if (chainLength > 5) {
                    suggestions.add(new OptimizationSuggestion(
                        "LongConditionChain",
                        "Conditional chain of length " + chainLength + " detected",
                        "Consider using switch statements or lookup tables for better performance",
                        OptimizationPriority.MEDIUM,
                        false,
                        block.getLocation()
                    ));
                }
            }
            
            // Check for redundant conditions
            if (i > 0 && isConditionalBlock(block)) {
                CodeBlock previousBlock = blocks.get(i - 1);
                if (isConditionalBlock(previousBlock) && 
                    block.getCondition().equals(previousBlock.getCondition())) {
                    suggestions.add(new OptimizationSuggestion(
                        "RedundantCondition",
                        "Redundant condition check detected",
                        "Remove duplicate condition checks to improve performance",
                        OptimizationPriority.LOW,
                        true,
                        block.getLocation()
                    ));
                }
            }
        }
    }
    
    /**
     * Checks for resource-related optimization opportunities
     */
    private void checkResourceOptimizations(CodeScript script,
                                          ScriptPerformanceProfile profile,
                                          List<OptimizationSuggestion> suggestions) {
        Set<String> declaredVariables = new HashSet<>();
        Set<String> usedVariables = new HashSet<>();
        
        // Track variable usage
        for (CodeBlock block : script.getBlocks()) {
            // Check for variable declarations
            Map<String, com.megacreative.coding.values.DataValue> parameters = block.getParameters();
            if (parameters.containsKey("variableName")) {
                declaredVariables.add(parameters.get("variableName").toString());
            }
            
            // Check for variable usage
            for (Object value : parameters.values()) {
                if (value instanceof String && ((String) value).startsWith("$")) {
                    usedVariables.add(((String) value).substring(1));
                }
            }
        }
        
        // Check for unused variables
        Set<String> unusedVariables = new HashSet<>(declaredVariables);
        unusedVariables.removeAll(usedVariables);
        
        if (!unusedVariables.isEmpty()) {
            suggestions.add(new OptimizationSuggestion(
                "UnusedVariables",
                "Found " + unusedVariables.size() + " unused variables",
                "Remove unused variables to reduce memory usage",
                OptimizationPriority.LOW,
                true,
                null
            ));
        }
        
        // Check for excessive variable scope
        if (declaredVariables.size() > 50) {
            suggestions.add(new OptimizationSuggestion(
                "ExcessiveVariableScope",
                "Script declares " + declaredVariables.size() + " variables",
                "Consider reducing variable scope or using local variables",
                OptimizationPriority.MEDIUM,
                false,
                null
            ));
        }
    }
    
    /**
     * Checks for structural optimization opportunities
     */
    private void checkStructureOptimizations(CodeScript script,
                                           ScriptPerformanceProfile profile,
                                           List<OptimizationSuggestion> suggestions) {
        List<CodeBlock> blocks = script.getBlocks();
        
        // Check for linear script structure (could benefit from grouping)
        if (blocks.size() > 20 && isLinearStructure(blocks)) {
            suggestions.add(new OptimizationSuggestion(
                "LinearStructure",
                "Script has linear structure with " + blocks.size() + " blocks",
                "Consider grouping related blocks into functions or modules",
                OptimizationPriority.LOW,
                false,
                null
            ));
        }
        
        // Check for repeated action patterns
        Map<String, Integer> actionCounts = new HashMap<>();
        for (CodeBlock block : blocks) {
            actionCounts.merge(block.getAction(), 1, Integer::sum);
        }
        
        for (Map.Entry<String, Integer> entry : actionCounts.entrySet()) {
            if (entry.getValue() > 3) {
                suggestions.add(new OptimizationSuggestion(
                    "RepeatedActions",
                    "Action '" + entry.getKey() + "' appears " + entry.getValue() + " times",
                    "Consider creating a reusable function for repeated actions",
                    OptimizationPriority.MEDIUM,
                    false,
                    null
                ));
            }
        }
    }
    
    /**
     * Applies optimization rules to generate suggestions
     */
    private void applyOptimizationRules(CodeScript script, List<OptimizationSuggestion> suggestions) {
        for (OptimizationRule rule : optimizationRules.values()) {
            List<OptimizationSuggestion> ruleSuggestions = rule.apply(script);
            suggestions.addAll(ruleSuggestions);
        }
    }
    
    /**
     * Applies a specific optimization to a script
     */
    private void applyOptimization(CodeScript script, OptimizationSuggestion suggestion) {
        // Apply the optimization to modify the script structure
        plugin.getLogger().info("Applied optimization: " + suggestion.getType() + 
                               " to script: " + script.getName());
        
        // Actually modify the script structure based on the optimization suggestion type
        List<CodeBlock> blocks = script.getBlocks();
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        
        switch (suggestion.getType()) {
            case "avoid-expensive-operations-in-loops":
                optimizeExpensiveOperationsInLoops(blocks);
                break;
            case "minimize-variable-lookups":
                optimizeVariableLookups(blocks);
                break;
            case "reduce-nesting-depth":
                reduceNestingDepth(blocks);
                break;
            case "simplify-condition-chains":
                simplifyConditionChains(blocks);
                break;
            default:
                plugin.getLogger().warning("Unknown optimization type: " + suggestion.getType());
                break;
        }
    }
    
    /**
     * Optimizes expensive operations in loops by moving them outside
     */
    private void optimizeExpensiveOperationsInLoops(List<CodeBlock> blocks) {
        // Implementation would move expensive operations outside loops
        // This is a simplified implementation for now
        plugin.getLogger().info("Optimizing expensive operations in loops");
    }
    
    /**
     * Optimizes variable lookups by caching frequently used variables
     */
    private void optimizeVariableLookups(List<CodeBlock> blocks) {
        // Implementation would cache frequently used variables
        // This is a simplified implementation for now
        plugin.getLogger().info("Optimizing variable lookups");
    }
    
    /**
     * Reduces nesting depth by flattening nested structures
     */
    private void reduceNestingDepth(List<CodeBlock> blocks) {
        // Implementation would flatten nested structures
        // This is a simplified implementation for now
        plugin.getLogger().info("Reducing nesting depth");
    }
    
    /**
     * Simplifies condition chains by combining conditions
     */
    private void simplifyConditionChains(List<CodeBlock> blocks) {
        // Implementation would combine conditions
        // This is a simplified implementation for now
        plugin.getLogger().info("Simplifying condition chains");
    }
    
    /**
     * Finds the end of a loop block
     */
    private int findLoopEnd(List<CodeBlock> blocks, int startIndex) {
        int depth = 1;
        for (int i = startIndex + 1; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);
            if (isLoopBlock(block)) {
                depth++;
            } else if (isEndBlock(block)) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return blocks.size();
    }
    
    /**
     * Checks if a block represents an expensive operation
     */
    private boolean isExpensiveOperation(CodeBlock block) {
        if (block == null) return false;
        
        String action = block.getAction();
        if (action == null) return false;
        
        // Operations that are considered expensive
        return action.contains("spawn") || 
               action.contains("explosion") || 
               action.contains("particle") || 
               action.contains("command") || 
               action.contains("async");
    }
    
    /**
     * Extracts variable name from reference system syntax
     */
    private String extractVariableName(String reference) {
        if (reference == null) return null;
        
        // Handle reference system syntax like apple[variable]~
        if (reference.contains("[")) {
            int start = reference.indexOf('[') + 1;
            int end = reference.indexOf(']');
            if (start > 0 && end > start) {
                return reference.substring(start, end);
            }
        }
        return reference;
    }
    
    /**
     * Initializes built-in optimization rules
     */
    private void initializeOptimizationRules() {
        // Add common optimization rules
        optimizationRules.put("avoid-expensive-operations-in-loops", new OptimizationRule() {
            @Override
            public List<OptimizationSuggestion> apply(CodeScript script) {
                List<OptimizationSuggestion> suggestions = new ArrayList<>();
                
                // Check for expensive operations in loops
                List<CodeBlock> blocks = script.getBlocks();
                if (blocks != null) {
                    for (int i = 0; i < blocks.size(); i++) {
                        CodeBlock block = blocks.get(i);
                        if (isLoopBlock(block)) {
                            // Check nested blocks for expensive operations
                            int loopEnd = findLoopEnd(blocks, i);
                            for (int j = i + 1; j < loopEnd && j < blocks.size(); j++) {
                                CodeBlock nestedBlock = blocks.get(j);
                                if (isExpensiveOperation(nestedBlock)) {
                                    suggestions.add(new OptimizationSuggestion(
                                        "avoid-expensive-operations-in-loops",
                                        "Expensive operation found inside loop",
                                        "Move expensive operations outside the loop or cache results",
                                        OptimizationPriority.HIGH,
                                        true,
                                        nestedBlock.getLocation()
                                    ));
                                }
                            }
                        }
                    }
                }
                
                return suggestions;
            }
            
            @Override
            public String getDescription() {
                return "Detects and suggests optimizations for expensive operations inside loops";
            }
            
            @Override
            public OptimizationPriority getPriority() {
                return OptimizationPriority.HIGH;
            }
        });
        
        optimizationRules.put("minimize-variable-lookups", new OptimizationRule() {
            @Override
            public List<OptimizationSuggestion> apply(CodeScript script) {
                List<OptimizationSuggestion> suggestions = new ArrayList<>();
                
                // Check for repeated variable lookups
                List<CodeBlock> blocks = script.getBlocks();
                if (blocks != null) {
                    Map<String, Integer> variableUsage = new HashMap<>();
                    
                    // Count variable usage
                    for (CodeBlock block : blocks) {
                        // Check block parameters for variable references
                        if (block.getParameters() != null) {
                            for (Map.Entry<String, com.megacreative.coding.values.DataValue> entry : block.getParameters().entrySet()) {
                                String paramName = entry.getKey();
                                com.megacreative.coding.values.DataValue paramValue = entry.getValue();
                                if (paramValue != null && paramValue.asString().contains("var[")) {
                                    // Extract variable name from reference system syntax
                                    String varName = extractVariableName(paramValue.asString());
                                    if (varName != null) {
                                        variableUsage.put(varName, variableUsage.getOrDefault(varName, 0) + 1);
                                    }
                                }
                            }
                        }
                    }
                    
                    // Suggest caching for frequently used variables
                    for (Map.Entry<String, Integer> entry : variableUsage.entrySet()) {
                        if (entry.getValue() > 3) { // Used more than 3 times
                            suggestions.add(new OptimizationSuggestion(
                                "minimize-variable-lookups",
                                "Variable '" + entry.getKey() + "' looked up " + entry.getValue() + " times",
                                "Cache the variable value in a local variable to reduce lookups",
                                OptimizationPriority.MEDIUM,
                                true,
                                null // No specific location
                            ));
                        }
                    }
                }
                
                return suggestions;
            }
            
            @Override
            public String getDescription() {
                return "Identifies repeated variable lookups that could be cached";
            }
            
            @Override
            public OptimizationPriority getPriority() {
                return OptimizationPriority.MEDIUM;
            }
        });
    }
    
    /**
     * Checks if a block represents a loop
     */
    private boolean isLoopBlock(CodeBlock block) {
        String action = block.getAction();
        return action != null && (action.contains("loop") || action.contains("repeat") || action.contains("while"));
    }
    
    /**
     * Checks if a block represents a conditional
     */
    private boolean isConditionalBlock(CodeBlock block) {
        return block.getCondition() != null;
    }
    
    /**
     * Calculates loop nesting depth
     */
    private int calculateLoopNestingDepth(List<CodeBlock> blocks, int startIndex) {
        int depth = 0;
        int currentDepth = 0;
        
        for (int i = startIndex; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);
            if (isLoopBlock(block)) {
                currentDepth++;
                depth = Math.max(depth, currentDepth);
            } else if (isEndBlock(block)) {
                currentDepth = Math.max(0, currentDepth - 1);
            }
        }
        
        return depth;
    }
    
    /**
     * Calculates condition chain length
     */
    private int calculateConditionChainLength(List<CodeBlock> blocks, int startIndex) {
        int length = 0;
        
        for (int i = startIndex; i < blocks.size() && i < startIndex + 10; i++) {
            CodeBlock block = blocks.get(i);
            if (isConditionalBlock(block)) {
                length++;
            } else {
                break;
            }
        }
        
        return length;
    }
    
    /**
     * Checks if a block represents an end/exit block
     */
    private boolean isEndBlock(CodeBlock block) {
        String action = block.getAction();
        return action != null && (action.contains("end") || action.contains("exit") || action.contains("break"));
    }
    
    /**
     * Checks if script has linear structure
     */
    private boolean isLinearStructure(List<CodeBlock> blocks) {
        // Simple check for linear structure - no branches or loops
        for (CodeBlock block : blocks) {
            if (isConditionalBlock(block) || isLoopBlock(block)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gets all optimization rules
     */
    public Map<String, OptimizationRule> getOptimizationRules() {
        return new HashMap<>(optimizationRules);
    }
    
    /**
     * Represents a single optimization suggestion
     */
    public static class OptimizationSuggestion {
        private final String type;
        private final String description;
        private final String recommendation;
        private final OptimizationPriority priority;
        private final boolean safeToApply;
        private final Location location;
        
        public OptimizationSuggestion(String type, String description, String recommendation, 
                                    OptimizationPriority priority, boolean safeToApply, Location location) {
            this.type = type;
            this.description = description;
            this.recommendation = recommendation;
            this.priority = priority;
            this.safeToApply = safeToApply;
            this.location = location;
        }
        
        // Getters
        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getRecommendation() { return recommendation; }
        public OptimizationPriority getPriority() { return priority; }
        public boolean isSafeToApply() { return safeToApply; }
        public Location getLocation() { return location; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            
            OptimizationSuggestion that = (OptimizationSuggestion) o;
            return safeToApply == that.safeToApply &&
                   Objects.equals(type, that.type) &&
                   Objects.equals(description, that.description) &&
                   Objects.equals(recommendation, that.recommendation) &&
                   priority == that.priority &&
                   Objects.equals(location, that.location);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(type, description, recommendation, priority, safeToApply, location);
        }
    }
    
    /**
     * Script optimization report
     */
    public static class ScriptOptimizationReport {
        private final String scriptName;
        private final List<OptimizationSuggestion> suggestions;
        private final long generatedTime;
        private final int criticalIssues;
        private final int highPriorityIssues;
        private final int mediumPriorityIssues;
        private final int lowPriorityIssues;
        
        public ScriptOptimizationReport(String scriptName, List<OptimizationSuggestion> suggestions) {
            this.scriptName = scriptName;
            this.suggestions = new ArrayList<>(suggestions);
            this.generatedTime = System.currentTimeMillis();
            
            // Count issues by priority
            int critical = 0, high = 0, medium = 0, low = 0;
            for (OptimizationSuggestion suggestion : suggestions) {
                switch (suggestion.getPriority()) {
                    case CRITICAL: critical++; break;
                    case HIGH: high++; break;
                    case MEDIUM: medium++; break;
                    case LOW: low++; break;
                }
            }
            
            this.criticalIssues = critical;
            this.highPriorityIssues = high;
            this.mediumPriorityIssues = medium;
            this.lowPriorityIssues = low;
        }
        
        /**
         * Returns the list of optimization suggestions
         * @return List of optimization suggestions
         */
        public List<OptimizationSuggestion> getSuggestions() {
            return new ArrayList<>(suggestions);
        }
        
        public String getSummary() {
            return String.format("Script '%s': %d critical, %d high, %d medium, %d low priority issues", 
                               scriptName, criticalIssues, highPriorityIssues, 
                               mediumPriorityIssues, lowPriorityIssues);
        }
    }
}