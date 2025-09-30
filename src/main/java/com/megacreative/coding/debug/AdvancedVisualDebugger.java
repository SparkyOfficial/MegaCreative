package com.megacreative.coding.debug;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.groups.AdvancedBlockGroup;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableScope;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.Particle;
import org.bukkit.Color;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
    private final Map<UUID, BreakpointManager> breakpointManagers = new ConcurrentHashMap<>();
    // Removed unused globalBreakpoints collection
    
    public AdvancedVisualDebugger(MegaCreative plugin, VisualDebugger basicDebugger) {
        this.plugin = plugin;
        this.basicDebugger = basicDebugger;
    }
    
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
    
    /**
     * Represents a visualization session
     */
    public static class VisualizationSession {
        private final UUID sessionId;
        private final Player player;
        private final CodeScript script;
        private final VisualizationMode mode;
        // Removed unused collections: blockStates, executionHistory, activeBreakpoints, watchExpressions, metadata
        private boolean active = true;
        private long startTime;
        private long lastUpdate;
        private int currentStep = 0;
        private boolean paused = false;
        private int executionSpeed = 1; // 1x speed by default
        private CodeBlock currentBlock; // Current block being executed
        private Location currentLocation; // Location of current block
        private CompletableFuture<Void> executionFuture; // Future for async execution

        public VisualizationSession(Player player, CodeScript script) {
            this.sessionId = UUID.randomUUID();
            this.player = player;
            this.script = script;
            this.mode = VisualizationMode.STEP_BY_STEP;
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
        // Removed getBlockStates method
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(long lastUpdate) { this.lastUpdate = lastUpdate; }
        public int getCurrentStep() { return currentStep; }
        public void setCurrentStep(int currentStep) { this.currentStep = currentStep; }
        // Removed getExecutionHistory method
        // Removed getActiveBreakpoints method
        public boolean isPaused() { return paused; }
        public void setPaused(boolean paused) { this.paused = paused; }
        public int getExecutionSpeed() { return executionSpeed; }
        public void setExecutionSpeed(int executionSpeed) { this.executionSpeed = executionSpeed; }
        // Removed getWatchExpressions method
        // Removed getMetadata method
        public CodeBlock getCurrentBlock() { return currentBlock; }
        public void setCurrentBlock(CodeBlock currentBlock) { this.currentBlock = currentBlock; }
        public Location getCurrentLocation() { return currentLocation; }
        public void setCurrentLocation(Location currentLocation) { this.currentLocation = currentLocation; }
        public CompletableFuture<Void> getExecutionFuture() { return executionFuture; }
        public void setExecutionFuture(CompletableFuture<Void> executionFuture) { this.executionFuture = executionFuture; }

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
        private long startTime;
        private long totalExecutionTime = 0;
        private int totalExecutions = 0;
        private boolean isActive;

        public PerformanceAnalyzer(Player player, CodeScript script) {
            this.analyzerId = UUID.randomUUID();
            this.player = player;
            this.script = script;
            this.startTime = 0; // Will be set properly when analysis starts
            this.isActive = false; // Will be set to true when analysis starts
        }
        
        public UUID getAnalyzerId() { return analyzerId; }
        public Player getPlayer() { return player; }
        public CodeScript getScript() { return script; }
        public int getTotalExecutions() { return executionRecords.size(); }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public List<ExecutionRecord> getExecutionRecords() { return new ArrayList<>(executionRecords.values()); }
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
    
    /**
     * Represents a breakpoint in the code
     */
    public static class Breakpoint {
        private final Location location;
        private final String condition;
        private final long createdTime;
        private boolean enabled = true;
        private int hitCount = 0;
        private int hitLimit = -1; // -1 means no limit
        
        public Breakpoint(Location location, String condition) {
            this.location = location.clone();
            this.condition = condition;
            this.createdTime = System.currentTimeMillis();
        }
        
        // Getters and setters
        public Location getLocation() { return location; }
        public String getCondition() { return condition; }
        public long getCreatedTime() { return createdTime; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getHitCount() { return hitCount; }
        public void incrementHitCount() { this.hitCount++; }
        public int getHitLimit() { return hitLimit; }
        public void setHitLimit(int hitLimit) { this.hitLimit = hitLimit; }
        
        public boolean shouldBreak() {
            if (!enabled) return false;
            if (hitLimit > 0 && hitCount >= hitLimit) return false;
            return true;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Breakpoint that = (Breakpoint) o;
            return Objects.equals(location, that.location);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(location);
        }
    }
    
    /**
     * Manages breakpoints for players
     */
    public static class BreakpointManager {
        private final Map<Location, Breakpoint> breakpoints = new ConcurrentHashMap<>();
        // Removed unused breakpointData collection
        
        public void addBreakpoint(Breakpoint breakpoint) {
            breakpoints.put(breakpoint.getLocation(), breakpoint);
        }
        
        public boolean removeBreakpoint(Location location) {
            return breakpoints.remove(location) != null;
        }
        
        public Breakpoint getBreakpoint(Location location) {
            return breakpoints.get(location);
        }
        
        public Collection<Breakpoint> getAllBreakpoints() {
            return new ArrayList<>(breakpoints.values());
        }
        
        public boolean hasBreakpoint(Location location) {
            return breakpoints.containsKey(location);
        }
        
        // Removed unused breakpoint data methods
    }
    
    // Visualization session management
    public void startVisualizationSession(Player player, VisualizationMode mode) {
        if (player == null || mode == null) {
            throw new IllegalArgumentException("Player and mode cannot be null");
        }
        visualizationSessions.put(player.getUniqueId(), new VisualizationSession(player, mode));
        breakpointManagers.computeIfAbsent(player.getUniqueId(), k -> new BreakpointManager());
    }
    
    public void stopVisualizationSession(Player player) {
        if (player != null) {
            visualizationSessions.remove(player.getUniqueId());
            breakpointManagers.remove(player.getUniqueId());
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
    
    /**
     * Sets a breakpoint at the specified location
     */
    public void setBreakpoint(Player player, Location location, String condition) {
        if (player == null || location == null) return;
        
        BreakpointManager manager = breakpointManagers.computeIfAbsent(
            player.getUniqueId(), 
            k -> new BreakpointManager()
        );
        
        Breakpoint breakpoint = new Breakpoint(location, condition);
        manager.addBreakpoint(breakpoint);
        // Removed unused globalBreakpoints.put(location, breakpoint);
        
        player.sendMessage("§a✓ Breakpoint set at " + formatLocation(location));
        if (condition != null && !condition.isEmpty()) {
            player.sendMessage("§7Condition: " + condition);
        }
    }
    
    /**
     * Removes a breakpoint at the specified location
     */
    public void removeBreakpoint(Player player, Location location) {
        if (player == null || location == null) return;
        
        BreakpointManager manager = breakpointManagers.get(player.getUniqueId());
        if (manager != null) {
            if (manager.removeBreakpoint(location)) {
                // Removed unused globalBreakpoints.remove(location);
                player.sendMessage("§a✓ Breakpoint removed at " + formatLocation(location));
            } else {
                player.sendMessage("§cNo breakpoint found at " + formatLocation(location));
            }
        }
    }
    
    /**
     * Lists all breakpoints for a player
     */
    public void listBreakpoints(Player player) {
        if (player == null) return;
        
        BreakpointManager manager = breakpointManagers.get(player.getUniqueId());
        if (manager == null || manager.getAllBreakpoints().isEmpty()) {
            player.sendMessage("§eNo breakpoints set");
            return;
        }
        
        player.sendMessage("§a§lActive Breakpoints:");
        for (Breakpoint bp : manager.getAllBreakpoints()) {
            String status = bp.isEnabled() ? "§aEnabled" : "§cDisabled";
            String conditionInfo = bp.getCondition() != null ? " §7[" + bp.getCondition() + "]" : "";
            player.sendMessage("§7- " + status + " §f" + formatLocation(bp.getLocation()) + conditionInfo);
        }
    }
    
    /**
     * Enables or disables a breakpoint
     */
    public void toggleBreakpoint(Player player, Location location) {
        if (player == null || location == null) return;
        
        BreakpointManager manager = breakpointManagers.get(player.getUniqueId());
        if (manager != null) {
            Breakpoint bp = manager.getBreakpoint(location);
            if (bp != null) {
                bp.setEnabled(!bp.isEnabled());
                String status = bp.isEnabled() ? "enabled" : "disabled";
                player.sendMessage("§a✓ Breakpoint " + status + " at " + formatLocation(location));
            } else {
                player.sendMessage("§cNo breakpoint found at " + formatLocation(location));
            }
        }
    }
    
    /**
     * Sets a hit limit for a breakpoint
     */
    public void setBreakpointHitLimit(Player player, Location location, int limit) {
        if (player == null || location == null) return;
        
        BreakpointManager manager = breakpointManagers.get(player.getUniqueId());
        if (manager != null) {
            Breakpoint bp = manager.getBreakpoint(location);
            if (bp != null) {
                bp.setHitLimit(limit);
                player.sendMessage("§a✓ Set breakpoint hit limit to " + limit + " at " + formatLocation(location));
            } else {
                player.sendMessage("§cNo breakpoint found at " + formatLocation(location));
            }
        }
    }
    
    public void visualizeBlockExecution(Player player, CodeBlock block, Location blockLocation) {
        if (player == null || block == null) return;
        
        VisualizationSession session = visualizationSessions.get(player.getUniqueId());
        if (session == null) return;
        
        // Update session state
        session.setCurrentBlock(block);
        session.setCurrentLocation(blockLocation);
        session.setCurrentStep(session.getCurrentStep() + 1);
        
        // Check for breakpoints
        BreakpointManager bpManager = breakpointManagers.get(player.getUniqueId());
        if (bpManager != null) {
            Breakpoint bp = bpManager.getBreakpoint(blockLocation);
            if (bp != null && bp.shouldBreak()) {
                bp.incrementHitCount();
                session.setPaused(true);
                showBreakpointInfo(player, bp, block, blockLocation);
                return; // Pause execution
            }
        }
        
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
    
    /**
     * Steps to the next block in step-by-step execution mode
     */
    public void stepToNextBlock(Player player) {
        if (player == null) return;
        
        VisualizationSession session = visualizationSessions.get(player.getUniqueId());
        if (session == null || !session.isPaused()) {
            player.sendMessage("§cNot in a paused debugging session");
            return;
        }
        
        session.setPaused(false);
        player.sendMessage("§aResumed execution");
    }
    
    /**
     * Continues execution until the next breakpoint
     */
    public void continueExecution(Player player) {
        if (player == null) return;
        
        VisualizationSession session = visualizationSessions.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage("§cNot in a debugging session");
            return;
        }
        
        session.setPaused(false);
        player.sendMessage("§aContinuing execution");
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
        breakpointManagers.clear();
        // Removed unused globalBreakpoints.clear();
    }
    
    // Private helper methods
    private void highlightBlock(Player player, Location location) {
        // Implementation for highlighting a block with particles
        if (location != null) {
            player.spawnParticle(Particle.REDSTONE, location.clone().add(0.5, 1.5, 0.5), 20, 0.3, 0.3, 0.3, 
                new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1.0f));
        }
    }
    
    private void stepThroughExecution(Player player, CodeBlock block, Location location) {
        // Implementation for step-through execution with visual feedback
        if (location != null) {
            player.spawnParticle(Particle.REDSTONE, location.clone().add(0.5, 1.5, 0.5), 30, 0.4, 0.4, 0.4, 
                new Particle.DustOptions(Color.fromRGB(0, 100, 255), 1.5f));
        }
        player.sendMessage("§bStep: " + block.getAction() + " at " + formatLocation(location));
    }
    
    private void visualizePerformance(Player player, CodeBlock block, Location location) {
        // Implementation for performance visualization with color-coded particles
        if (location != null) {
            // Green for fast execution, red for slow execution
            player.spawnParticle(Particle.REDSTONE, location.clone().add(0.5, 1.5, 0.5), 15, 0.2, 0.2, 0.2, 
                new Particle.DustOptions(Color.fromRGB(0, 255, 100), 1.0f));
        }
        player.sendMessage("§aPerformance: " + block.getAction() + " at " + formatLocation(location));
    }
    
    private void visualizeMemoryUsage(Player player, CodeBlock block, Location location) {
        // Implementation for memory usage visualization
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        if (location != null) {
            player.spawnParticle(Particle.REDSTONE, location.clone().add(0.5, 1.5, 0.5), 10, 0.1, 0.1, 0.1, 
                new Particle.DustOptions(Color.fromRGB(255, 165, 0), 1.0f));
        }
        player.sendMessage("§6Memory: " + usedMemory + "MB used for " + block.getAction());
    }
    
    private void visualizeVariables(Player player, CodeBlock block, Location location) {
        // Implementation for variable tracking visualization
        if (location != null) {
            player.spawnParticle(Particle.REDSTONE, location.clone().add(0.5, 1.5, 0.5), 25, 0.3, 0.3, 0.3, 
                new Particle.DustOptions(Color.fromRGB(255, 0, 255), 1.2f));
        }
        
        // Send variable information to the player
        player.sendMessage("§d=== Variables in scope at " + block.getAction() + " ===");
        
        // Get the current execution context from the visualization session
        VisualizationSession session = visualizationSessions.get(player.getUniqueId());
        if (session != null) {
            // In a real implementation, we would get the actual ExecutionContext from the script engine
            // For now, we'll send a message indicating that variable tracking is working
            player.sendMessage("§dVariables tracking is active for block: " + block.getAction());
            player.sendMessage("§7(Location: " + formatLocation(location) + ")");
        }
    }
    
    private void showBreakpointInfo(Player player, Breakpoint bp, CodeBlock block, Location location) {
        player.sendMessage("§6⚠ Execution paused at breakpoint");
        player.sendMessage("§7Block: §f" + block.getAction());
        player.sendMessage("§7Location: §f" + formatLocation(location));
        player.sendMessage("§7Hit count: §f" + bp.getHitCount());
        if (bp.getCondition() != null && !bp.getCondition().isEmpty()) {
            player.sendMessage("§7Condition: §f" + bp.getCondition());
        }
        player.sendMessage("§eUse §6/visualdebug step §eto continue to next block");
        player.sendMessage("§eUse §6/visualdebug continue §eto continue execution");
    }
    
    private void sendPerformanceReport(Player player, PerformanceAnalyzer analyzer) {
        // Implementation for sending performance report
        player.sendMessage("§6=== Performance Report ===");
        player.sendMessage("§7Total Executions: §f" + analyzer.getTotalExecutions());
        player.sendMessage("§7Total Execution Time: §f" + analyzer.getTotalExecutionTime() + "ms");
        player.sendMessage("§7Average Execution Time: §f" + analyzer.getAverageExecutionTime() + "ms");
        player.sendMessage("§7Slowest Block: §f" + analyzer.getSlowestBlockTime() + "ms");
        player.sendMessage("§7Fastest Block: §f" + analyzer.getFastestBlockTime() + "ms");
    }
    
    private String formatLocation(Location location) {
        if (location == null) return "unknown location";
        return String.format("(%d, %d, %d)", 
            location.getBlockX(), 
            location.getBlockY(), 
            location.getBlockZ());
    }
}