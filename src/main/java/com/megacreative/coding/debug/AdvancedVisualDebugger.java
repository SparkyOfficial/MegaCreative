package com.megacreative.coding.debug;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.groups.AdvancedBlockGroup;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableScope;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

/**
 * Advanced visual debugger with enhanced visualization and analysis capabilities
 */

/**
 * Represents the state of a block during visualization
 */
// Make VisualizationState package-private instead of public to fix visibility issue
enum VisualizationState {
    IDLE,          // Default state
    EXECUTING,     // Block is currently being executed
    SUCCESS,       // Block executed successfully
    ERROR,         // Block execution resulted in an error
    BREAKPOINT,    // Execution is paused at this breakpoint
    WATCHED,       // Block is being watched
    HIGHLIGHTED,   // Block is highlighted for emphasis
    MODIFIED       // Block's state was modified during execution
}

public class AdvancedVisualDebugger {
    
    private final MegaCreative plugin;
    private final VisualDebugger basicDebugger;
    private final Map<UUID, VisualizationSession> visualizationSessions = new ConcurrentHashMap<>();
    private final Map<UUID, PerformanceAnalyzer> performanceAnalyzers = new ConcurrentHashMap<>();
    
    /**
     * Starts performance analysis for a player's script execution
     * @param player The player to start analysis for
     * @param script The script being analyzed
     */
    public void startPerformanceAnalysis(Player player, CodeScript script) {
        if (player == null || script == null) return;
        
        PerformanceAnalyzer analyzer = new PerformanceAnalyzer(player, script);
        analyzer.setStartTime(System.currentTimeMillis());
        performanceAnalyzers.put(player.getUniqueId(), analyzer);
        
        player.sendMessage("§aStarted performance analysis for script: §e" + script.getName());
    }
    
    /**
     * Records execution data for performance analysis
     * @param player The player whose execution is being recorded
     * @param block The code block being executed
     * @param location The location where the block is being executed
     * @param executionTime The time taken to execute the block in milliseconds
     */
    public void recordExecutionData(Player player, CodeBlock block, Location location, long executionTime) {
        if (player == null || block == null) return;
        
        PerformanceAnalyzer analyzer = performanceAnalyzers.get(player.getUniqueId());
        if (analyzer != null) {
            analyzer.recordExecution(block, location, executionTime);
        }
    }
    
    public AdvancedVisualDebugger(MegaCreative plugin, VisualDebugger basicDebugger) {
        this.plugin = plugin;
        this.basicDebugger = basicDebugger;
    }
    
    // ... (rest of the code remains the same)

    /**
     * Represents a visualization session
     */
    public static class VisualizationSession {
        private final UUID sessionId;
        private final Player player;
        private final CodeScript script;
        private final VisualizationMode mode;
        private final Map<Location, VisualizationState> blockStates = new HashMap<>();
        private boolean active = true;
        private long startTime;
        private long lastUpdate;
        private int currentStep = 0;
        private final List<Object> executionHistory = new ArrayList<>();
        private final Set<UUID> activeBreakpoints = new HashSet<>();
        private boolean paused = false;
        private int executionSpeed = 1; // 1x speed by default
        private final Map<String, DataValue> watchExpressions = new HashMap<>();
        private final Map<String, Object> metadata = new HashMap<>();

        public VisualizationSession(Player player, CodeScript script) {
            this.sessionId = UUID.randomUUID();
            this.player = player;
            this.script = script;
            this.mode = VisualizationMode.STANDARD;
            this.startTime = System.currentTimeMillis();
            this.lastUpdate = startTime;
        }
        
        public VisualizationSession(Player player, VisualizationMode mode) {
            this.sessionId = UUID.randomUUID();
            this.player = player;
            this.script = null;
            this.mode = mode;
            this.startTime = System.currentTimeMillis();
            this.lastUpdate = startTime;
        }

        // Getters and setters
        public UUID getSessionId() { return sessionId; }
        public Player getPlayer() { return player; }
        public CodeScript getScript() { return script; }
        public Map<Location, VisualizationState> getBlockStates() { 
            return new HashMap<>(blockStates); 
        }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(long lastUpdate) { this.lastUpdate = lastUpdate; }
        public int getCurrentStep() { return currentStep; }
        public void setCurrentStep(int currentStep) { this.currentStep = currentStep; }
        @SuppressWarnings("unchecked")
        public List<ExecutionStep> getExecutionHistory() {
            List<ExecutionStep> result = new ArrayList<>();
            for (Object item : executionHistory) {
                if (item instanceof ExecutionStep) {
                    result.add((ExecutionStep) item);
                }
            }
            return result;
        }
        public Set<UUID> getActiveBreakpoints() { return new HashSet<>(activeBreakpoints); }
        public boolean isPaused() { return paused; }
        public void setPaused(boolean paused) { this.paused = paused; }
        public int getExecutionSpeed() { return executionSpeed; }
        public void setExecutionSpeed(int executionSpeed) { this.executionSpeed = executionSpeed; }
        public Map<String, DataValue> getWatchExpressions() { return new HashMap<>(watchExpressions); }
        public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VisualizationSession that = (VisualizationSession) o;
            return Objects.equals(sessionId, that.sessionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sessionId);
        }

        @Override
        public String toString() {
            return "VisualizationSession{" +
                   "sessionId=" + sessionId +
                   ", player=" + player.getName() +
                   ", script=" + (script != null ? script.getName() : "null") +
                   ", active=" + active +
                   ", currentStep=" + currentStep +
                   ", mode=" + mode +
                   '}';
        }

        public static class ExecutionStep {
            private final CodeBlock block;
            private final Location location;
            private final long timestamp;

            public ExecutionStep(CodeBlock block, Location location) {
                this.block = block;
                this.location = location != null ? location.clone() : null;
                this.timestamp = System.currentTimeMillis();
            }

            public CodeBlock getBlock() { return block; }
            public Location getLocation() { return location; }
            public long getTimestamp() { return timestamp; }
        }
    }

    /**
     * Analyzes performance of script execution
     */
    public static class PerformanceAnalyzer {
        private final UUID analyzerId;
        private final Player player;
        private final CodeScript script;
        private final Map<String, ExecutionRecord> executionRecords = new HashMap<>();
        private final Map<String, Long> executionTimes = new HashMap<>();
        private final Map<String, Integer> executionCounts = new HashMap<>();
        private final Map<String, Map<CodeBlock, Integer>> blockExecutionCounts = new HashMap<>();
        private long startTime = System.currentTimeMillis();
        private long totalExecutionTime = 0;
        private int totalExecutions = 0;
        private boolean isActive = true;

        public PerformanceAnalyzer(Player player, CodeScript script) {
            this.analyzerId = UUID.randomUUID();
            this.player = player;
            this.script = script;
            this.startTime = System.currentTimeMillis();
            this.isActive = true;
        }
        
        public UUID getAnalyzerId() { return analyzerId; }
        public Player getPlayer() { return player; }
        public CodeScript getScript() { return script; }
        public int getTotalExecutions() { return executionRecords.size(); }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public List<ExecutionRecord> getExecutionRecords() { return new ArrayList<>(executionRecords.values()); }
        public Map<String, Long> getExecutionTimes() { return new HashMap<>(executionTimes); }
        public Map<String, Integer> getExecutionCounts() { return new HashMap<>(executionCounts); }
        public Map<String, Map<CodeBlock, Integer>> getBlockExecutionCounts() { 
            Map<String, Map<CodeBlock, Integer>> copy = new HashMap<>();
            blockExecutionCounts.forEach((k, v) -> copy.put(k, new HashMap<>(v)));
            return copy;
        }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }

        public void recordExecution(CodeBlock block, Location location, long executionTime) {
            Map<String, Object> context = new HashMap<>();
            context.put("location", location);
            UUID recordId = UUID.randomUUID();
            executionRecords.put(recordId.toString(), new ExecutionRecord(block, executionTime, context, true, null));
            totalExecutionTime += executionTime;
            totalExecutions++;
        }
        
        public List<ExecutionRecord> getSlowestBlocks(int count) {
            return executionRecords.values().stream()
                .sorted((a, b) -> Long.compare(b.executionTime, a.executionTime))
                .limit(count)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        
        public long getAverageExecutionTime() {
            return executionRecords.isEmpty() ? 0 : totalExecutionTime / executionRecords.size();
        }
        
        public long getSlowestBlockTime() {
            return executionRecords.values().stream()
                .mapToLong(ExecutionRecord::getExecutionTime)
                .max()
                .orElse(0);
        }
        
        public long getFastestBlockTime() {
            return executionRecords.values().stream()
                .mapToLong(ExecutionRecord::getExecutionTime)
                .min()
                .orElse(0);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PerformanceAnalyzer that = (PerformanceAnalyzer) o;
            return Objects.equals(analyzerId, that.analyzerId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(analyzerId);
        }

        @Override
        public String toString() {
            return "PerformanceAnalyzer{" +
                   "analyzerId=" + analyzerId +
                   ", player=" + (player != null ? player.getName() : "null") +
                   ", totalExecutions=" + totalExecutions +
                   ", totalExecutionTime=" + totalExecutionTime + "ms" +
                   '}';
        }
        
        /**
         * Represents a single execution record
         */
        public static class ExecutionRecord {
            private final String recordId;
            private final CodeBlock block;
            private final long executionTime;
            private final long timestamp;
            private final Map<String, Object> context;
            private final boolean successful;
            private final String errorMessage;

            public ExecutionRecord(CodeBlock block, long executionTime, Map<String, Object> context, boolean successful, String errorMessage) {
            this(UUID.randomUUID(), block, executionTime, context, successful, errorMessage);
        }
        
        public ExecutionRecord(UUID id, CodeBlock block, long executionTime, Map<String, Object> context, boolean successful, String errorMessage) {
                this.recordId = id.toString();
                this.block = block;
                this.executionTime = executionTime;
                this.timestamp = System.currentTimeMillis();
                this.context = context != null ? new HashMap<>(context) : new HashMap<>();
                this.successful = successful;
                this.errorMessage = errorMessage;
            }

            // Getters
            public String getRecordId() { return recordId; }
            public CodeBlock getBlock() { return block; }
            public long getExecutionTime() { return executionTime; }
            public long getTimestamp() { return timestamp; }
            public Map<String, Object> getContext() { return new HashMap<>(context); }
            public boolean isSuccessful() { return successful; }
            public String getErrorMessage() { return errorMessage; }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ExecutionRecord that = (ExecutionRecord) o;
                return Objects.equals(recordId, that.recordId);
            }

            @Override
            public int hashCode() {
                return Objects.hash(recordId);
            }

            @Override
            public String toString() {
                return "ExecutionRecord{" +
                       "recordId='" + recordId + '\'' +
                       ", block=" + (block != null ? block.getAction() : "null") +
                       ", executionTime=" + executionTime + "ms" +
                       ", successful=" + successful +
                       ", errorMessage='" + errorMessage + '\'' +
                       '}';
            }
        }
    }
    
    /**
     * Represents the visualization mode for debugging
     */
    public enum VisualizationMode {
        STANDARD,       // Standard visualization with basic highlighting
        STEP_BY_STEP,   // Step-through debugging mode
        PERFORMANCE,    // Performance visualization mode
        MEMORY,         // Memory usage visualization
        VARIABLES       // Variable tracking visualization
    }
    
    // Visualization session management
    public void startVisualizationSession(Player player, VisualizationMode mode) {
        if (player == null || mode == null) {
            throw new IllegalArgumentException("Player and mode cannot be null");
        }
        visualizationSessions.put(player.getUniqueId(), new VisualizationSession(player, mode));
    }
    
    public void stopVisualizationSession(Player player) {
        if (player != null) {
            visualizationSessions.remove(player.getUniqueId());
        }
    }
    
    public boolean isVisualizationEnabled(Player player) {
        return player != null && visualizationSessions.containsKey(player.getUniqueId());
    }
    
    public VisualizationMode getVisualizationMode(Player player) {
        if (player == null) return null;
        VisualizationSession session = visualizationSessions.get(player.getUniqueId());
        return session != null ? session.mode : null;
    }
    
    public void visualizeBlockExecution(Player player, CodeBlock block, Location blockLocation) {
        if (player == null || block == null) return;
        
        VisualizationSession session = visualizationSessions.get(player.getUniqueId());
        if (session == null) return;
        
        // Update the visualization based on the mode
        switch (session.mode) {
            case STANDARD:
                // Basic block highlighting
                highlightBlock(player, blockLocation);
                break;
            case STEP_BY_STEP:
                // Step through execution
                stepThroughExecution(player, block, blockLocation);
                break;
            case PERFORMANCE:
                // Performance visualization
                visualizePerformance(player, block, blockLocation);
                break;
            case MEMORY:
                // Memory usage visualization
                visualizeMemoryUsage(player, block, blockLocation);
                break;
            case VARIABLES:
                // Variable tracking visualization
                visualizeVariables(player, block, blockLocation);
                break;
        }
    }
    
    public void showPerformanceReport(Player player) {
        if (player == null) return;
        PerformanceAnalyzer analyzer = performanceAnalyzers.get(player.getUniqueId());
        if (analyzer != null) {
            // Generate and send performance report to player
            sendPerformanceReport(player, analyzer);
        }
    }
    
    public void shutdown() {
        visualizationSessions.clear();
        performanceAnalyzers.clear();
    }
    
    // Private helper methods
    private void highlightBlock(Player player, Location location) {
        // Implementation for highlighting a block
        player.sendMessage("§eHighlighting block at " + formatLocation(location));
    }
    
    private void stepThroughExecution(Player player, CodeBlock block, Location location) {
        // Implementation for step-through execution
        player.sendMessage("§bStep: " + block.getAction() + " at " + formatLocation(location));
    }
    
    private void visualizePerformance(Player player, CodeBlock block, Location location) {
        // Implementation for performance visualization
        player.sendMessage("§aPerformance: " + block.getAction() + " at " + formatLocation(location));
    }
    
    private void visualizeMemoryUsage(Player player, CodeBlock block, Location location) {
        // Implementation for memory usage visualization
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        player.sendMessage("§6Memory: " + usedMemory + "MB used for " + block.getAction());
    }
    
    private void visualizeVariables(Player player, CodeBlock block, Location location) {
        // Implementation for variable tracking visualization
        player.sendMessage("§dVariables in scope at " + block.getAction());
    }
    
    private void sendPerformanceReport(Player player, PerformanceAnalyzer analyzer) {
        // Implementation for sending performance report
        player.sendMessage("§6=== Performance Report ===");
        player.sendMessage("§7Total Executions: §f" + analyzer.getTotalExecutions());
        player.sendMessage("§7Total Execution Time: §f" + analyzer.getTotalExecutionTime() + "ms");
        player.sendMessage("§7Average Execution Time: §f" + analyzer.getAverageExecutionTime() + "ms");
    }
    
    private String formatLocation(Location location) {
        if (location == null) return "unknown location";
        return String.format("(%d, %d, %d)", 
            location.getBlockX(), 
            location.getBlockY(), 
            location.getBlockZ());
    }
}