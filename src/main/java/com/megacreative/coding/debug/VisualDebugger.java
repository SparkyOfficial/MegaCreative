package com.megacreative.coding.debug;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableScope;
import lombok.extern.java.Log;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log
public class VisualDebugger {
    
    private final MegaCreative plugin;
    private final Map<UUID, DebugSession> activeSessions = new ConcurrentHashMap<>();
    
    // Advanced debugging features
    private final Map<UUID, BreakpointManager> breakpointManagers = new ConcurrentHashMap<>();
    private final Map<UUID, ExecutionTracer> executionTracers = new ConcurrentHashMap<>();
    private final Map<UUID, VariableWatcher> variableWatchers = new ConcurrentHashMap<>();
    
    // Advanced visual debugger
    private final AdvancedVisualDebugger advancedDebugger;
    
    public VisualDebugger(MegaCreative plugin) {
        this.plugin = plugin;
        this.advancedDebugger = new AdvancedVisualDebugger(plugin, this);
    }
    
    public void startDebugSession(Player player, String sessionName) {
        DebugSession session = new DebugSession(player, sessionName);
        activeSessions.put(player.getUniqueId(), session);
        player.sendMessage("§a✓ Visual debugger started: " + sessionName);
    }
    
    public void stopDebugSession(Player player) {
        DebugSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            player.sendMessage("§c✖ Visual debugger stopped");
        }
    }
    
    public void highlightBlockExecution(Player player, Location blockLocation, CodeBlock block) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        
        session.executionStep++;
        player.sendMessage("§e▶ Executing: " + block.getAction());
    }
    
    public void showVariableChange(Player player, Location blockLocation, String variableName, 
                                 DataValue oldValue, DataValue newValue, VariableScope scope) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        
        player.sendMessage("§b" + variableName + "§7: §c" + 
            (oldValue != null ? oldValue.asString() : "null") + " §7→ §a" + 
            (newValue != null ? newValue.asString() : "null"));
    }
    
    public void showError(Player player, Location blockLocation, String errorMessage) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        
        session.errorCount++;
        player.sendMessage("§c✖ ERROR: " + errorMessage);
    }
    
    /**
     * Sets a breakpoint at a specific block location
     */
    public void setBreakpoint(Player player, Location blockLocation, String condition) {
        BreakpointManager manager = breakpointManagers.computeIfAbsent(
            player.getUniqueId(), 
            k -> new BreakpointManager()
        );
        
        Breakpoint breakpoint = new Breakpoint(blockLocation, condition);
        manager.addBreakpoint(breakpoint);
        
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            player.sendMessage("§a✓ Breakpoint set at " + formatLocation(blockLocation));
            if (condition != null && !condition.isEmpty()) {
                player.sendMessage("§7Condition: " + condition);
            }
        }
    }
    
    /**
     * Removes a breakpoint at a specific block location
     */
    public void removeBreakpoint(Player player, Location blockLocation) {
        BreakpointManager manager = breakpointManagers.get(player.getUniqueId());
        if (manager != null) {
            if (manager.removeBreakpoint(blockLocation)) {
                player.sendMessage("§a✓ Breakpoint removed at " + formatLocation(blockLocation));
            } else {
                player.sendMessage("§cNo breakpoint found at " + formatLocation(blockLocation));
            }
        }
    }
    
    /**
     * Lists all breakpoints for a player
     */
    public void listBreakpoints(Player player) {
        BreakpointManager manager = breakpointManagers.get(player.getUniqueId());
        if (manager == null || manager.getBreakpoints().isEmpty()) {
            player.sendMessage("§eNo breakpoints set");
            return;
        }
        
        player.sendMessage("§a§lActive Breakpoints:");
        for (Breakpoint bp : manager.getBreakpoints()) {
            String conditionInfo = bp.getCondition() != null ? " §7[" + bp.getCondition() + "]" : "";
            player.sendMessage("§7- §f" + formatLocation(bp.getLocation()) + conditionInfo);
        }
    }
    
    /**
     * Starts execution tracing for a player
     */
    public void startTracing(Player player, int maxSteps) {
        ExecutionTracer tracer = new ExecutionTracer(maxSteps);
        executionTracers.put(player.getUniqueId(), tracer);
        
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            player.sendMessage("§a✓ Execution tracing started (max " + maxSteps + " steps)");
        }
    }
    
    /**
     * Stops execution tracing for a player
     */
    public void stopTracing(Player player) {
        ExecutionTracer tracer = executionTracers.remove(player.getUniqueId());
        if (tracer != null) {
            player.sendMessage("§c✖ Execution tracing stopped");
            player.sendMessage("§7Traced " + tracer.getTracedSteps() + " execution steps");
        }
    }
    
    /**
     * Shows execution trace for a player
     */
    public void showTrace(Player player) {
        ExecutionTracer tracer = executionTracers.get(player.getUniqueId());
        if (tracer == null) {
            player.sendMessage("§cNo execution trace available");
            return;
        }
        
        List<ExecutionStep> steps = tracer.getTrace();
        if (steps.isEmpty()) {
            player.sendMessage("§eExecution trace is empty");
            return;
        }
        
        player.sendMessage("§a§lExecution Trace (" + steps.size() + " steps):");
        int maxSteps = Math.min(steps.size(), 10); // Show only last 10 steps
        int start = Math.max(0, steps.size() - maxSteps);
        
        for (int i = start; i < steps.size(); i++) {
            ExecutionStep step = steps.get(i);
            player.sendMessage("§7" + (i + 1) + ". §e" + step.getAction() + 
                             " §7at " + formatLocation(step.getLocation()));
        }
        
        if (steps.size() > maxSteps) {
            player.sendMessage("§8... and " + (steps.size() - maxSteps) + " more steps");
        }
    }
    
    /**
     * Adds a variable watcher
     */
    public void watchVariable(Player player, String variableName, String expression) {
        VariableWatcher watcher = variableWatchers.computeIfAbsent(
            player.getUniqueId(),
            k -> new VariableWatcher()
        );
        
        watcher.addWatch(variableName, expression);
        player.sendMessage("§a✓ Watching variable: " + variableName);
    }
    
    /**
     * Removes a variable watcher
     */
    public void unwatchVariable(Player player, String variableName) {
        VariableWatcher watcher = variableWatchers.get(player.getUniqueId());
        if (watcher != null && watcher.removeWatch(variableName)) {
            player.sendMessage("§a✓ Stopped watching variable: " + variableName);
        }
    }
    
    /**
     * Shows watched variables and their current values
     */
    public void showWatchedVariables(Player player) {
        VariableWatcher watcher = variableWatchers.get(player.getUniqueId());
        if (watcher == null || watcher.getWatches().isEmpty()) {
            player.sendMessage("§eNo variables being watched");
            return;
        }
        
        player.sendMessage("§a§lWatched Variables:");
        for (Map.Entry<String, String> entry : watcher.getWatches().entrySet()) {
            String varName = entry.getKey();
            String expression = entry.getValue();
            player.sendMessage("§7- §f" + varName + " §7= §b" + expression);
        }
    }
    
    /**
     * Toggles debug mode for a player
     */
    public boolean toggleDebug(Player player) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            stopDebugSession(player);
            return false;
        } else {
            startDebugSession(player, "Debug Session");
            return true;
        }
    }
    
    /**
     * Checks if debug is enabled for a player
     */
    public boolean isDebugEnabled(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }
    
    /**
     * Called when a script starts execution
     */
    public void onScriptStart(Player player, com.megacreative.coding.CodeScript script) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        
        player.sendMessage("§a▶ Starting script: " + script.getName());
    }
    
    /**
     * Called when a script ends execution
     */
    public void onScriptEnd(Player player, com.megacreative.coding.CodeScript script) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        
        player.sendMessage("§c▣ Script ended: " + script.getName());
    }
    
    /**
     * Called when a block is executed (enhanced version with visualization)
     */
    public void onBlockExecute(Player player, CodeBlock block, Location blockLocation) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        
        session.executionStep++;
        
        // Check for breakpoints
        BreakpointManager bpManager = breakpointManagers.get(player.getUniqueId());
        if (bpManager != null && bpManager.hasBreakpointAt(blockLocation)) {
            Breakpoint bp = bpManager.getBreakpointAt(blockLocation);
            player.sendMessage("§6⚠ Execution paused at breakpoint: " + block.getAction());
            player.sendMessage("§7Location: " + formatLocation(blockLocation));
        }
        
        // Add to execution trace
        ExecutionTracer tracer = executionTracers.get(player.getUniqueId());
        if (tracer != null) {
            tracer.addStep(new ExecutionStep(block.getAction(), blockLocation, System.currentTimeMillis()));
        }
        
        // Visualize execution
        visualizeBlockExecution(player, block, blockLocation);
        
        // Show execution message
        player.sendMessage("§e▶ Executing: " + block.getAction() + 
                          " §7(" + formatLocation(blockLocation) + ")");
    }
    
    /**
     * Called when a condition is evaluated
     */
    public void onConditionResult(Player player, CodeBlock block, boolean result) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        
        player.sendMessage("§e? Condition " + block.getAction() + ": " + (result ? "§aTrue" : "§cFalse"));
    }
    
    /**
     * Shows enhanced debug statistics for a player
     */
    public void showDebugStats(Player player) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage("§cDebug not enabled!");
            return;
        }
        
        player.sendMessage("§6=== Debug Statistics ===");
        player.sendMessage("§fExecution Steps: " + session.executionStep);
        player.sendMessage("§fErrors: " + session.errorCount);
        player.sendMessage("§fSession: " + session.sessionName);
        
        // Show additional info if available
        BreakpointManager bpManager = breakpointManagers.get(player.getUniqueId());
        if (bpManager != null) {
            player.sendMessage("§fBreakpoints: " + bpManager.getBreakpoints().size());
        }
        
        ExecutionTracer tracer = executionTracers.get(player.getUniqueId());
        if (tracer != null) {
            player.sendMessage("§fTraced Steps: " + tracer.getTracedSteps());
        }
        
        VariableWatcher watcher = variableWatchers.get(player.getUniqueId());
        if (watcher != null) {
            player.sendMessage("§fWatched Variables: " + watcher.getWatches().size());
        }
    }
    
    /**
     * Formats a location for display
     */
    private String formatLocation(Location location) {
        if (location == null) return "unknown";
        return String.format("§7[%d, %d, %d]", 
                           location.getBlockX(), 
                           location.getBlockY(), 
                           location.getBlockZ());
    }
    
    /**
     * Starts a visualization session
     */
    public void startVisualization(Player player, AdvancedVisualDebugger.VisualizationMode mode) {
        advancedDebugger.startVisualizationSession(player, mode);
    }
    
    /**
     * Stops a visualization session
     */
    public void stopVisualization(Player player) {
        advancedDebugger.stopVisualizationSession(player);
    }
    
    /**
     * Checks if visualization is enabled
     */
    public boolean isVisualizationEnabled(Player player) {
        return advancedDebugger.isVisualizationEnabled(player);
    }
    
    /**
     * Gets the visualization mode
     */
    public AdvancedVisualDebugger.VisualizationMode getVisualizationMode(Player player) {
        return advancedDebugger.getVisualizationMode(player);
    }
    
    /**
     * Visualizes block execution
     */
    public void visualizeBlockExecution(Player player, CodeBlock block, Location blockLocation) {
        advancedDebugger.visualizeBlockExecution(player, block, blockLocation);
    }
    
    /**
     * Starts performance analysis
     */
    public void startPerformanceAnalysis(Player player, com.megacreative.coding.CodeScript script) {
        advancedDebugger.startPerformanceAnalysis(player, script);
    }
    
    /**
     * Records execution data for performance analysis
     */
    public void recordExecutionData(Player player, CodeBlock block, Location location, long executionTime) {
        advancedDebugger.recordExecutionData(player, block, location, executionTime);
    }
    
    /**
     * Shows performance report
     */
    public void showPerformanceReport(Player player) {
        advancedDebugger.showPerformanceReport(player);
    }
    
    /**
     * Cleans up resources when plugin disables
     * @deprecated Use shutdown() instead
     */
    @Deprecated
    public void cleanup() {
        shutdown();
    }
    
    /**
     * Shuts down the visual debugger and cleans up resources
     */
    public void shutdown() {
        // Stop all debug sessions
        for (UUID playerId : new HashSet<>(activeSessions.keySet())) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                stopDebugSession(player);
            }
        }
        
        // Stop all visualization sessions
        for (UUID playerId : new HashSet<>(breakpointManagers.keySet())) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                stopTracing(player);
            }
        }
        
        // Clear all collections
        activeSessions.clear();
        breakpointManagers.clear();
        executionTracers.clear();
        variableWatchers.clear();
        
        // Shutdown advanced debugger
        if (advancedDebugger != null) {
            advancedDebugger.shutdown();
        }
        
        plugin.getLogger().info("Visual debugger has been shut down");
    }
    
    private static class DebugSession {
        final String sessionName;
        final Player player;
        int executionStep = 0;
        int errorCount = 0;
        
        DebugSession(Player player, String sessionName) {
            this.player = player;
            this.sessionName = sessionName;
        }
    }
    
    /**
     * Represents a breakpoint in the code
     */
    public static class Breakpoint {
        private final Location location;
        private final String condition;
        private final long createdTime;
        
        public Breakpoint(Location location, String condition) {
            this.location = location.clone();
            this.condition = condition;
            this.createdTime = System.currentTimeMillis();
        }
        
        // Getters
        public Location getLocation() { return location; }
        public String getCondition() { return condition; }
        public long getCreatedTime() { return createdTime; }
    }
    
    /**
     * Manages breakpoints for a player
     */
    public static class BreakpointManager {
        private final Map<String, Breakpoint> breakpoints = new ConcurrentHashMap<>();
        
        public void addBreakpoint(Breakpoint breakpoint) {
            String key = formatLocationKey(breakpoint.getLocation());
            breakpoints.put(key, breakpoint);
        }
        
        public boolean removeBreakpoint(Location location) {
            String key = formatLocationKey(location);
            return breakpoints.remove(key) != null;
        }
        
        public boolean hasBreakpointAt(Location location) {
            String key = formatLocationKey(location);
            return breakpoints.containsKey(key);
        }
        
        public Breakpoint getBreakpointAt(Location location) {
            String key = formatLocationKey(location);
            return breakpoints.get(key);
        }
        
        public Collection<Breakpoint> getBreakpoints() {
            return breakpoints.values();
        }
        
        private String formatLocationKey(Location location) {
            return location.getWorld().getName() + ":" + 
                   location.getBlockX() + "," + 
                   location.getBlockY() + "," + 
                   location.getBlockZ();
        }
    }
    
    /**
     * Represents a step in execution trace
     */
    public static class ExecutionStep {
        private final String action;
        private final Location location;
        private final long timestamp;
        
        public ExecutionStep(String action, Location location, long timestamp) {
            this.action = action;
            this.location = location.clone();
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getAction() { return action; }
        public Location getLocation() { return location; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Traces execution steps
     */
    public static class ExecutionTracer {
        private final List<ExecutionStep> trace = new ArrayList<>();
        private final int maxSteps;
        private int tracedSteps = 0;
        
        public ExecutionTracer(int maxSteps) {
            this.maxSteps = maxSteps > 0 ? maxSteps : 1000; // Default to 1000 steps
        }
        
        public void addStep(ExecutionStep step) {
            tracedSteps++;
            if (trace.size() >= maxSteps) {
                trace.remove(0); // Remove oldest step
            }
            trace.add(step);
        }
        
        // Getters
        public List<ExecutionStep> getTrace() { return new ArrayList<>(trace); }
        public int getTracedSteps() { return tracedSteps; }
        public int getMaxSteps() { return maxSteps; }
    }
    
    /**
     * Watches variables for changes
     */
    public static class VariableWatcher {
        private final Map<String, String> watches = new ConcurrentHashMap<>();
        
        public void addWatch(String variableName, String expression) {
            watches.put(variableName, expression);
        }
        
        public boolean removeWatch(String variableName) {
            return watches.remove(variableName) != null;
        }
        
        public Map<String, String> getWatches() {
            return new HashMap<>(watches);
        }
        
        public boolean hasWatches() {
            return !watches.isEmpty();
        }
    }
}