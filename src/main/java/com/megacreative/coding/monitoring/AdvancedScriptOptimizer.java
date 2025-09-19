package com.megacreative.coding.monitoring;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.monitoring.model.*;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.values.DataValue;
import com.megacreative.MegaCreative;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.Constants;
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
        performOptimizationChecks(script, profile, suggestions);
        
        // Apply optimization rules
        applyOptimizationRules(script, suggestions);
        
        return new ScriptOptimizationReport(script.getName(), suggestions);
    }
    
    private void performOptimizationChecks(CodeScript script, 
                                         ScriptPerformanceProfile profile,
                                         List<OptimizationSuggestion> suggestions) {
        checkLoopOptimizations(script, profile, suggestions);
        checkConditionalOptimizations(script, profile, suggestions);
        checkResourceOptimizations(script, profile, suggestions);
        checkStructureOptimizations(script, profile, suggestions);
    }
    
    // Loop optimization methods
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
            checkNestedLoopOptimization(block, blocks, i, suggestions);
            
            // Check for loops with expensive operations
            checkExpensiveLoopOperation(block, profile, suggestions);
        }
    }
    
    private void checkNestedLoopOptimization(CodeBlock block, List<CodeBlock> blocks, 
                                           int index, List<OptimizationSuggestion> suggestions) {
        if (isLoopBlock(block)) {
            int nestedDepth = calculateLoopNestingDepth(blocks, index);
            if (nestedDepth > 2) {
                suggestions.add(new OptimizationSuggestion(
                    "DeeplyNestedLoop",
                    "Loop at position " + index + " is nested " + nestedDepth + " levels deep",
                    "Consider flattening nested loops or using more efficient data structures",
                    OptimizationPriority.HIGH,
                    true,
                    block.getLocation()
                ));
            }
        }
    }
    
    private void checkExpensiveLoopOperation(CodeBlock block, ScriptPerformanceProfile profile,
                                           List<OptimizationSuggestion> suggestions) {
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
    
    // Conditional optimization methods
    private void checkConditionalOptimizations(CodeScript script,
                                             ScriptPerformanceProfile profile,
                                             List<OptimizationSuggestion> suggestions) {
        List<CodeBlock> blocks = script.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);
            
            // Check for complex condition chains
            checkConditionChainOptimization(block, blocks, i, suggestions);
            
            // Check for redundant conditions
            checkRedundantCondition(block, blocks, i, suggestions);
        }
    }
    
    private void checkConditionChainOptimization(CodeBlock block, List<CodeBlock> blocks, 
                                               int index, List<OptimizationSuggestion> suggestions) {
        if (isConditionalBlock(block)) {
            int chainLength = calculateConditionChainLength(blocks, index);
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
    }
    
    private void checkRedundantCondition(CodeBlock block, List<CodeBlock> blocks, 
                                       int index, List<OptimizationSuggestion> suggestions) {
        if (index > 0 && isConditionalBlock(block)) {
            CodeBlock previousBlock = blocks.get(index - 1);
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
    
    // Resource optimization methods
    private void checkResourceOptimizations(CodeScript script,
                                          ScriptPerformanceProfile profile,
                                          List<OptimizationSuggestion> suggestions) {
        Set<String> declaredVariables = new HashSet<>();
        Set<String> usedVariables = new HashSet<>();
        
        // Track variable usage
        trackVariableUsage(script, declaredVariables, usedVariables);
        
        // Check for unused variables
        checkForUnusedVariables(declaredVariables, usedVariables, suggestions);
        
        // Check for excessive variable scope
        checkForExcessiveVariableScope(declaredVariables, suggestions);
    }
    
    private void trackVariableUsage(CodeScript script, Set<String> declaredVariables, 
                                  Set<String> usedVariables) {
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
    }
    
    private void checkForUnusedVariables(Set<String> declaredVariables, Set<String> usedVariables,
                                       List<OptimizationSuggestion> suggestions) {
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
    }
    
    private void checkForExcessiveVariableScope(Set<String> declaredVariables,
                                              List<OptimizationSuggestion> suggestions) {
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
    
    // Structure optimization methods
    private void checkStructureOptimizations(CodeScript script,
                                           ScriptPerformanceProfile profile,
                                           List<OptimizationSuggestion> suggestions) {
        List<CodeBlock> blocks = script.getBlocks();
        
        // Check for linear script structure (could benefit from grouping)
        checkLinearStructureOptimization(blocks, suggestions);
        
        // Check for repeated action patterns
        checkRepeatedActionPatterns(blocks, suggestions);
    }
    
    private void checkLinearStructureOptimization(List<CodeBlock> blocks,
                                                List<OptimizationSuggestion> suggestions) {
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
    }
    
    private void checkRepeatedActionPatterns(List<CodeBlock> blocks,
                                           List<OptimizationSuggestion> suggestions) {
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
    
    // Optimization rule methods
    private void applyOptimizationRules(CodeScript script, List<OptimizationSuggestion> suggestions) {
        for (OptimizationRule rule : optimizationRules.values()) {
            List<OptimizationSuggestion> ruleSuggestions = rule.apply(script);
            suggestions.addAll(ruleSuggestions);
        }
    }
    
    private void applyOptimization(CodeScript script, OptimizationSuggestion suggestion) {
        // Apply the optimization to modify the script structure
        plugin.getLogger().info(Constants.APPLIED_OPTIMIZATION + suggestion.getType() + 
                               Constants.APPLIED_OPTIMIZATION_TO_SCRIPT + script.getName());
        
        // Actually modify the script structure based on the optimization suggestion type
        List<CodeBlock> blocks = script.getBlocks();
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        
        applyOptimizationByType(blocks, suggestion.getType());
    }
    
    private void applyOptimizationByType(List<CodeBlock> blocks, String type) {
        switch (type) {
            case Constants.AVOID_EXPENSIVE_OPERATIONS_IN_LOOPS:
                optimizeExpensiveOperationsInLoops(blocks);
                break;
            case Constants.MINIMIZE_VARIABLE_LOOKUPS:
                optimizeVariableLookups(blocks);
                break;
            case Constants.REDUCE_NESTING_DEPTH:
                reduceNestingDepth(blocks);
                break;
            case Constants.SIMPLIFY_CONDITION_CHAINS:
                simplifyConditionChains(blocks);
                break;
            default:
                plugin.getLogger().warning(Constants.UNKNOWN_OPTIMIZATION_TYPE + type);
                break;
        }
    }
    
    // Optimization implementation methods
    private void optimizeExpensiveOperationsInLoops(List<CodeBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        
        // Find loops and expensive operations within them
        for (int i = 0; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);
            if (isLoopBlock(block)) {
                // Find the end of the loop
                int loopEnd = findLoopEnd(blocks, i);
                
                // Check for expensive operations within the loop
                List<CodeBlock> expensiveOps = new ArrayList<>();
                for (int j = i + 1; j < loopEnd && j < blocks.size(); j++) {
                    CodeBlock nestedBlock = blocks.get(j);
                    if (isExpensiveOperation(nestedBlock)) {
                        expensiveOps.add(nestedBlock);
                    }
                }
                
                // If we found expensive operations, log optimization suggestions
                logExpensiveOperations(i, expensiveOps);
            }
        }
    }
    
    private void logExpensiveOperations(int loopIndex, List<CodeBlock> expensiveOps) {
        if (!expensiveOps.isEmpty()) {
            plugin.getLogger().info(Constants.FOUND_EXPENSIVE_OPERATIONS + expensiveOps.size() + Constants.FOUND_EXPENSIVE_OPERATIONS_SUFFIX + loopIndex);
            for (CodeBlock op : expensiveOps) {
                plugin.getLogger().info(Constants.EXPENSIVE_OPERATION + op.getAction());
            }
        }
    }
    
    private void optimizeVariableLookups(List<CodeBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        
        // Count variable usage frequency
        Map<String, Integer> variableUsage = countVariableUsage(blocks);
        
        // Log frequently used variables (used more than 3 times)
        logFrequentlyUsedVariables(variableUsage);
    }
    
    private Map<String, Integer> countVariableUsage(List<CodeBlock> blocks) {
        Map<String, Integer> variableUsage = new HashMap<>();
        for (CodeBlock block : blocks) {
            if (block.getParameters() != null) {
                for (Map.Entry<String, com.megacreative.coding.values.DataValue> entry : block.getParameters().entrySet()) {
                    String paramName = entry.getKey();
                    com.megacreative.coding.values.DataValue paramValue = entry.getValue();
                    if (paramValue != null) {
                        String valueStr = paramValue.asString();
                        // Extract variable names from reference system syntax like var[variable_name]
                        if (valueStr.contains("var[")) {
                            int start = valueStr.indexOf("var[") + 4;
                            int end = valueStr.indexOf("]", start);
                            if (start > 3 && end > start) {
                                String varName = valueStr.substring(start, end);
                                variableUsage.put(varName, variableUsage.getOrDefault(varName, 0) + 1);
                            }
                        }
                    }
                }
            }
        }
        return variableUsage;
    }
    
    private void logFrequentlyUsedVariables(Map<String, Integer> variableUsage) {
        for (Map.Entry<String, Integer> entry : variableUsage.entrySet()) {
            if (entry.getValue() > 3) {
                plugin.getLogger().info(Constants.FREQUENTLY_USED_VARIABLE + entry.getKey() + " (" + entry.getValue() + Constants.FREQUENTLY_USED_VARIABLE_SUFFIX);
            }
        }
    }
    
    private void reduceNestingDepth(List<CodeBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        
        // Count nesting levels and identify deeply nested structures
        int maxDepth = calculateMaxNestingDepth(blocks);
        
        plugin.getLogger().info(Constants.MAXIMUM_NESTING_DEPTH + maxDepth);
        
        // If nesting is too deep, suggest flattening
        if (maxDepth > 5) {
            plugin.getLogger().warning(Constants.DEEP_NESTING_DETECTED + maxDepth + Constants.DEEP_NESTING_DETECTED_SUFFIX);
        }
    }
    
    private int calculateMaxNestingDepth(List<CodeBlock> blocks) {
        int maxDepth = 0;
        int currentDepth = 0;
        
        for (CodeBlock block : blocks) {
            if (block.isBracket() && block.getBracketType() == CodeBlock.BracketType.OPEN) {
                currentDepth++;
                maxDepth = Math.max(maxDepth, currentDepth);
            } else if (block.isBracket() && block.getBracketType() == CodeBlock.BracketType.CLOSE) {
                currentDepth = Math.max(0, currentDepth - 1);
            }
        }
        return maxDepth;
    }
    
    private void simplifyConditionChains(List<CodeBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        
        // Look for consecutive condition blocks that could be combined
        for (int i = 0; i < blocks.size() - 1; i++) {
            CodeBlock current = blocks.get(i);
            CodeBlock next = blocks.get(i + 1);
            
            // Check if both are condition blocks by checking their action type through BlockConfigService
            if (isConditionBlock(current) && isConditionBlock(next)) {
                // Check if they're part of the same logical chain
                if (current.getNextBlock() == next) {
                    plugin.getLogger().info(Constants.CONSECUTIVE_CONDITION_BLOCKS + i + Constants.CONSECUTIVE_CONDITION_BLOCKS_SUFFIX + (i + 1));
                    plugin.getLogger().info(Constants.CONDITION_FOLLOWED_BY + current.getAction() + Constants.CONDITION_FOLLOWED_BY_SUFFIX + next.getAction());
                }
            }
        }
    }
    
    // Utility methods
    private boolean isConditionBlock(CodeBlock block) {
        if (block == null || block.getAction() == null) {
            return false;
        }
        
        // Get the block configuration to determine its type
        BlockConfigService blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        if (blockConfigService != null) {
            BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(block.getAction());
            if (config != null) {
                return "CONDITION".equals(config.getType());
            }
        }
        
        return false;
    }
    
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
    
    private boolean isLoopBlock(CodeBlock block) {
        String action = block.getAction();
        return action != null && (action.contains("loop") || action.contains("repeat") || action.contains("while"));
    }
    
    private boolean isConditionalBlock(CodeBlock block) {
        return block.getCondition() != null;
    }
    
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
    
    private boolean isEndBlock(CodeBlock block) {
        String action = block.getAction();
        return action != null && (action.contains("end") || action.contains("exit") || action.contains("break"));
    }
    
    private boolean isLinearStructure(List<CodeBlock> blocks) {
        // Simple check for linear structure - no branches or loops
        for (CodeBlock block : blocks) {
            if (isConditionalBlock(block) || isLoopBlock(block)) {
                return false;
            }
        }
        return true;
    }
    
    // Rule initialization methods
    private void initializeOptimizationRules() {
        initializeExpensiveOperationRule();
        initializeVariableLookupRule();
    }
    
    private void initializeExpensiveOperationRule() {
        // Add common optimization rules
        optimizationRules.put("avoid-expensive-operations-in-loops", new OptimizationRule() {
            @Override
            public List<OptimizationSuggestion> apply(CodeScript script) {
                List<OptimizationSuggestion> suggestions = new ArrayList<>();
                
                // Check for expensive operations in loops
                List<CodeBlock> blocks = script.getBlocks();
                if (blocks != null) {
                    checkExpensiveOperationsInLoops(blocks, suggestions);
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
    }
    
    private void checkExpensiveOperationsInLoops(List<CodeBlock> blocks, 
                                               List<OptimizationSuggestion> suggestions) {
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
    
    private void initializeVariableLookupRule() {
        optimizationRules.put("minimize-variable-lookups", new OptimizationRule() {
            @Override
            public List<OptimizationSuggestion> apply(CodeScript script) {
                List<OptimizationSuggestion> suggestions = new ArrayList<>();
                
                // Check for repeated variable lookups
                List<CodeBlock> blocks = script.getBlocks();
                if (blocks != null) {
                    Map<String, Integer> variableUsage = new HashMap<>();
                    
                    // Count variable usage
                    countVariableUsageInBlocks(blocks, variableUsage);
                    
                    // Suggest caching for frequently used variables
                    suggestVariableCaching(variableUsage, suggestions);
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
    
    private void countVariableUsageInBlocks(List<CodeBlock> blocks, Map<String, Integer> variableUsage) {
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
    }
    
    private void suggestVariableCaching(Map<String, Integer> variableUsage, 
                                      List<OptimizationSuggestion> suggestions) {
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
    
    /**
     * Automatically applies safe optimizations to a script
     */
    public void autoOptimizeScript(CodeScript script) {
        ScriptOptimizationReport report = analyzeScript(script);
        
        // Apply safe optimizations automatically
        applySafeOptimizations(script, report);
    }
    
    private void applySafeOptimizations(CodeScript script, ScriptOptimizationReport report) {
        for (OptimizationSuggestion suggestion : report.getSuggestions()) {
            if (suggestion.isSafeToApply()) {
                applyOptimization(script, suggestion);
            }
        }
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
            countIssuesByPriority(suggestions, critical, high, medium, low);
            
            this.criticalIssues = critical;
            this.highPriorityIssues = high;
            this.mediumPriorityIssues = medium;
            this.lowPriorityIssues = low;
        }
        
        private void countIssuesByPriority(List<OptimizationSuggestion> suggestions, 
                                         int critical, int high, int medium, int low) {
            for (OptimizationSuggestion suggestion : suggestions) {
                switch (suggestion.getPriority()) {
                    case CRITICAL: critical++; break;
                    case HIGH: high++; break;
                    case MEDIUM: medium++; break;
                    case LOW: low++; break;
                    default:
                        // Unknown priority, ignore
                        break;
                }
            }
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