package com.megacreative.coding.debug;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableScope;
import com.megacreative.coding.debug.AdvancedVisualDebugger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

// Required for variable watching
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class VisualDebugger {
    private static final Logger log = Logger.getLogger(VisualDebugger.class.getName());
    
    private final MegaCreative plugin;
    private final Map<UUID, DebugSession> activeSessions = new ConcurrentHashMap<>();
    
    // Advanced debugging features
    private final Map<UUID, BreakpointManager> breakpointManagers = new ConcurrentHashMap<>();
    private final Map<UUID, ExecutionTracer> executionTracers = new ConcurrentHashMap<>();
    private final Map<UUID, VariableWatcher> variableWatchers = new ConcurrentHashMap<>();
    private final Map<UUID, PerformanceProfiler> performanceProfilers = new ConcurrentHashMap<>();
    
    // Advanced visual debugger
    private final AdvancedVisualDebugger advancedDebugger;
    
    public VisualDebugger(MegaCreative plugin) {
        this.plugin = plugin;
        this.advancedDebugger = new AdvancedVisualDebugger(plugin, this);
        
        // Initialize breakpoint managers for online players
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                breakpointManagers.computeIfAbsent(player.getUniqueId(), k -> new BreakpointManager());
                executionTracers.computeIfAbsent(player.getUniqueId(), k -> new ExecutionTracer());
                variableWatchers.computeIfAbsent(player.getUniqueId(), k -> new VariableWatcher());
                performanceProfilers.computeIfAbsent(player.getUniqueId(), k -> new PerformanceProfiler());
            }
        }, 20L, 6000L); // Check every 5 minutes
    }
    
    /**
     * Checks if a player is currently in a debug session
     */
    public boolean isDebugging(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }
    
    /**
     * Starts a new debug session for the player
     */
    public void startDebugSession(Player player, String sessionName) {
        DebugSession session = new DebugSession(player, sessionName);
        activeSessions.put(player.getUniqueId(), session);
        player.sendMessage("§a✓ Visual debugger started: " + sessionName);
        
        // Initialize breakpoint manager for this player
        breakpointManagers.computeIfAbsent(player.getUniqueId(), k -> new BreakpointManager());
    }
    
    /**
     * Stops the current debug session for a player
     */
    public void stopDebugSession(Player player) {
        DebugSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            // Clean up resources
            breakpointManagers.remove(player.getUniqueId());
            executionTracers.remove(player.getUniqueId());
            variableWatchers.remove(player.getUniqueId());
            performanceProfilers.remove(player.getUniqueId());
            
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
     * Logs an error message for a player
     * @param player The player to log the error for
     * @param errorMessage The error message to log
     */
    public void logError(Player player, String errorMessage) {
        // Log the error to the console
        log.severe(errorMessage);
        
        // Show the error to the player if they're in a debug session
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            session.errorCount++;
            player.sendMessage("§c✖ ERROR: " + errorMessage);
        }
    }
    
    /**
     * Sets a breakpoint at a specific block location
     */
    /**
     * Adds a breakpoint at the specified location
     */
    public void addBreakpoint(Player player, Location location, String condition) {
        BreakpointManager manager = breakpointManagers.computeIfAbsent(
            player.getUniqueId(), 
            k -> new BreakpointManager()
        );
        
        Breakpoint breakpoint = new Breakpoint(location, condition);
        manager.addBreakpoint(breakpoint);
        
        player.sendMessage("§a✓ Breakpoint set at " + formatLocation(location));
        if (condition != null && !condition.isEmpty()) {
            player.sendMessage("§7Condition: " + condition);
        }
    }
    
    /**
     * Gets all breakpoints for a player
     */
    public List<Location> getBreakpoints(Player player) {
        BreakpointManager manager = breakpointManagers.get(player.getUniqueId());
        if (manager == null) {
            return new ArrayList<>();
        }
        return manager.getBreakpoints().stream()
            .map(Breakpoint::getLocation)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Removes a breakpoint at a specific block location
     */
    /**
     * Removes a breakpoint at the specified location
     */
    public void removeBreakpoint(Player player, Location location) {
        BreakpointManager manager = breakpointManagers.get(player.getUniqueId());
        if (manager != null) {
            if (manager.removeBreakpoint(location)) {
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
     * Lists all breakpoints for a player (enhanced version)
     */
    public void listBreakpointsEnhanced(Player player) {
        if (advancedDebugger != null) {
            advancedDebugger.listBreakpoints(player);
        } else {
            listBreakpoints(player);
        }
    }
    
    /**
     * Sets a breakpoint using the advanced debugger
     */
    public void setBreakpoint(Player player, Location location, String condition) {
        if (advancedDebugger != null) {
            advancedDebugger.setBreakpoint(player, location, condition);
        } else {
            addBreakpoint(player, location, condition);
        }
    }
    
    /**
     * Removes a breakpoint using the advanced debugger
     */
    public void removeBreakpointAdvanced(Player player, Location location) {
        if (advancedDebugger != null) {
            advancedDebugger.removeBreakpoint(player, location);
        } else {
            removeBreakpoint(player, location);
        }
    }
    
    /**
     * Toggles a breakpoint using the advanced debugger
     */
    public void toggleBreakpoint(Player player, Location location) {
        if (advancedDebugger != null) {
            advancedDebugger.toggleBreakpoint(player, location);
        }
    }
    
    /**
     * Sets a hit limit for a breakpoint using the advanced debugger
     */
    public void setBreakpointHitLimit(Player player, Location location, int limit) {
        if (advancedDebugger != null) {
            advancedDebugger.setBreakpointHitLimit(player, location, limit);
        }
    }
    
    /**
     * Steps to the next block in step-by-step execution mode
     */
    public void stepToNextBlock(Player player) {
        if (advancedDebugger != null) {
            advancedDebugger.stepToNextBlock(player);
        } else {
            player.sendMessage("§cAdvanced debugger not available");
        }
    }
    
    /**
     * Continues execution until the next breakpoint
     */
    public void continueExecution(Player player) {
        if (advancedDebugger != null) {
            advancedDebugger.continueExecution(player);
        } else {
            player.sendMessage("§cAdvanced debugger not available");
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
     * Starts performance profiling for a player
     */
    public void startProfiling(Player player) {
        PerformanceProfiler profiler = performanceProfilers.computeIfAbsent(
            player.getUniqueId(),
            k -> new PerformanceProfiler()
        );
        
        profiler.startProfiling();
        player.sendMessage("§a✓ Performance profiling started");
    }
    
    /**
     * Stops performance profiling for a player
     */
    public void stopProfiling(Player player) {
        PerformanceProfiler profiler = performanceProfilers.get(player.getUniqueId());
        if (profiler != null) {
            profiler.stopProfiling();
            player.sendMessage("§c✖ Performance profiling stopped");
        }
    }
    
    /**
     * Shows performance profile for a player
     */
    public void showProfile(Player player) {
        PerformanceProfiler profiler = performanceProfilers.get(player.getUniqueId());
        if (profiler == null) {
            player.sendMessage("§cNo performance profile available");
            return;
        }
        
        profiler.showProfile(player);
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
        
        // Start performance profiling
        PerformanceProfiler profiler = performanceProfilers.get(player.getUniqueId());
        if (profiler != null) {
            profiler.startScript(script.getName());
        }
    }
    
    /**
     * Called when a script ends execution
     */
    public void onScriptEnd(Player player, com.megacreative.coding.CodeScript script) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        
        player.sendMessage("§c▣ Script ended: " + script.getName());
        
        // End performance profiling
        PerformanceProfiler profiler = performanceProfilers.get(player.getUniqueId());
        if (profiler != null) {
            profiler.endScript();
        }
    }
    
    /**
     * Called when a block is executed (enhanced version with visualization)
     */
    public void onBlockExecute(Player player, CodeBlock block, Location blockLocation) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        
        session.executionStep++;
        
        // Check for breakpoints using advanced debugger if available
        if (advancedDebugger != null) {
            // The advanced debugger handles breakpoint checking internally
            advancedDebugger.visualizeBlockExecution(player, block, blockLocation);
        } else {
            // Fallback to basic breakpoint checking
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
        
        PerformanceProfiler profiler = performanceProfilers.get(player.getUniqueId());
        if (profiler != null && profiler.isProfiling()) {
            player.sendMessage("§fPerformance Profiling: §aActive");
        }
    }
    
    /**
     * Formats a location for display
     */
    private String formatLocation(Location location) {
        if (location == null) return "unknown";
        String worldName = location.getWorld() != null ? location.getWorld().getName() : "";
        return String.format("§7[%s, %d, %d, %d]", 
                           worldName,
                           location.getBlockX(), 
                           location.getBlockY(), 
                           location.getBlockZ());
    }
    
    /**
     * Starts a visualization session with the specified mode
     * @param player The player to start visualization for
     * @param mode The visualization mode to use
     */
    public void startVisualization(Player player, AdvancedVisualDebugger.VisualizationMode mode) {
        if (player == null || mode == null) {
            throw new IllegalArgumentException("Player and mode cannot be null");
        }
        advancedDebugger.startVisualizationSession(player, mode);
    }
    
    /**
     * Stops the visualization session for a player
     * @param player The player to stop visualization for
     */
    public void stopVisualization(Player player) {
        if (player != null) {
            advancedDebugger.stopVisualizationSession(player);
        }
    }
    
    /**
     * Checks if visualization is enabled for a player
     * @param player The player to check
     * @return true if visualization is enabled, false otherwise
     */
    public boolean isVisualizationEnabled(Player player) {
        return player != null && advancedDebugger.isVisualizationEnabled(player);
    }
    
    /**
     * Gets the current visualization mode for a player
     * @param player The player to check
     * @return The current visualization mode, or null if not in visualization mode
     */
    public AdvancedVisualDebugger.VisualizationMode getVisualizationMode(Player player) {
        return player != null ? advancedDebugger.getVisualizationMode(player) : null;
    }
    
    /**
     * Visualizes block execution
     */
    public void visualizeBlockExecution(Player player, CodeBlock block, Location blockLocation) {
        if (player == null || block == null || blockLocation == null) {
            return;
        }
        
        // Visualize the block execution using the advanced debugger
        if (advancedDebugger != null) {
            advancedDebugger.visualizeBlockExecution(player, block, blockLocation);
        } else {
            // Fallback visualization if advanced debugger is not available
            try {
                player.sendBlockChange(blockLocation, blockLocation.getBlock().getBlockData());
            } catch (Exception e) {
                log.warning("Failed to visualize block execution: " + e.getMessage());
            }
        }
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
        performanceProfilers.clear();
        
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
        
        public List<Breakpoint> getBreakpoints() {
            return new ArrayList<>(breakpoints.values());
        }
        
        private String formatLocationKey(Location location) {
            if (location == null) return "";
            return String.format("%d,%d,%d,%s", 
                location.getBlockX(), 
                location.getBlockY(), 
                location.getBlockZ(),
                location.getWorld() != null ? location.getWorld().getName() : "");
        }
    }
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
        
        public ExecutionTracer() {
            this(1000); // Default to 1000 steps
        }
        
        public ExecutionTracer(int maxSteps) {
            this.maxSteps = maxSteps > 0 ? maxSteps : 1000; // Ensure positive value
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
    
    /**
     * Profiles script performance
     */
    public static class PerformanceProfiler {
        private boolean profiling = false;
        private long startTime = 0;
        private long endTime = 0;
        private String currentScript = "";
        private final Map<String, Long> scriptExecutionTimes = new ConcurrentHashMap<>();
        private final Map<String, Integer> scriptExecutionCounts = new ConcurrentHashMap<>();
        
        public void startProfiling() {
            profiling = true;
            startTime = System.currentTimeMillis();
        }
        
        public void stopProfiling() {
            profiling = false;
            endTime = System.currentTimeMillis();
        }
        
        public void startScript(String scriptName) {
            if (profiling) {
                currentScript = scriptName;
                scriptExecutionTimes.putIfAbsent(scriptName, 0L);
                scriptExecutionCounts.putIfAbsent(scriptName, 0);
            }
        }
        
        public void endScript() {
            if (profiling && !currentScript.isEmpty()) {
                long executionTime = System.currentTimeMillis() - startTime;
                scriptExecutionTimes.merge(currentScript, executionTime, Long::sum);
                scriptExecutionCounts.merge(currentScript, 1, Integer::sum);
                currentScript = "";
            }
        }
        
        public boolean isProfiling() {
            return profiling;
        }
        
        public void showProfile(Player player) {
            if (!profiling) {
                player.sendMessage("§cPerformance profiling is not active");
                return;
            }
            
            player.sendMessage("§6=== Performance Profile ===");
            player.sendMessage("§7Total profiling time: §f" + (endTime - startTime) + "ms");
            
            if (scriptExecutionTimes.isEmpty()) {
                player.sendMessage("§eNo script execution data available");
                return;
            }
            
            player.sendMessage("§a§lScript Execution Times:");
            for (Map.Entry<String, Long> entry : scriptExecutionTimes.entrySet()) {
                String scriptName = entry.getKey();
                long totalTime = entry.getValue();
                int count = scriptExecutionCounts.getOrDefault(scriptName, 0);
                double avgTime = count > 0 ? (double) totalTime / count : 0;
                
                player.sendMessage("§7- §f" + scriptName + " §7: §f" + totalTime + "ms §8(total), §f" + 
                    String.format("%.2f", avgTime) + "ms §8(avg), §f" + count + "x §8(count)");
            }
        }
    }
}