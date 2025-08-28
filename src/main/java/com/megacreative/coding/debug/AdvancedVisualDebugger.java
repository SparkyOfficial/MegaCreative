package com.megacreative.coding.debug;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.groups.AdvancedBlockGroup;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableScope;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced visual debugger with enhanced visualization and analysis capabilities
 */
public class AdvancedVisualDebugger {
    
    private final MegaCreative plugin;
    private final VisualDebugger basicDebugger;
    private final Map<UUID, VisualizationSession> visualizationSessions = new ConcurrentHashMap<>();
    private final Map<UUID, PerformanceAnalyzer> performanceAnalyzers = new ConcurrentHashMap<>();
    
    public AdvancedVisualDebugger(MegaCreative plugin, VisualDebugger basicDebugger) {
        this.plugin = plugin;
        this.basicDebugger = basicDebugger;
    }
    
    /**
     * Starts a visualization session for a player
     */
    public void startVisualizationSession(Player player, VisualizationMode mode) {
        VisualizationSession session = new VisualizationSession(player, mode);
        visualizationSessions.put(player.getUniqueId(), session);
        player.sendMessage("§a✓ Visualization session started in " + mode.getDisplayName() + " mode");
    }
    
    /**
     * Stops a visualization session for a player
     */
    public void stopVisualizationSession(Player player) {
        VisualizationSession session = visualizationSessions.remove(player.getUniqueId());
        if (session != null) {
            player.sendMessage("§c✖ Visualization session stopped");
        }
    }
    
    /**
     * Checks if visualization is enabled for a player
     */
    public boolean isVisualizationEnabled(Player player) {
        return visualizationSessions.containsKey(player.getUniqueId());
    }
    
    /**
     * Gets the visualization mode for a player
     */
    public VisualizationMode getVisualizationMode(Player player) {
        VisualizationSession session = visualizationSessions.get(player.getUniqueId());
        return session != null ? session.getMode() : null;
    }
    
    /**
     * Visualizes block execution with enhanced effects
     */
    public void visualizeBlockExecution(Player player, CodeBlock block, Location blockLocation) {
        VisualizationSession session = visualizationSessions.get(player.getUniqueId());
        if (session == null) return;
        
        // Apply visualization based on mode
        switch (session.getMode()) {
            case BLOCK_HIGHLIGHTING:
                highlightBlock(player, blockLocation, "#00FF00"); // Green
                break;
            case FLOW_TRACING:
                traceExecutionFlow(player, blockLocation);
                break;
            case PERFORMANCE_MAPPING:
                mapPerformance(player, block, blockLocation);
                break;
        }
    }
    
    /**
     * Visualizes group execution
     */
    public void visualizeGroupExecution(Player player, AdvancedBlockGroup group) {
        VisualizationSession session = visualizationSessions.get(player.getUniqueId());
        if (session == null || session.getMode() != VisualizationMode.GROUP_MAPPING) return;
        
        // Visualize the entire group bounds
        highlightRegion(player, group.getBounds(), "#00FFFF"); // Cyan
        player.sendMessage("§b▶ Executing group: " + group.getName() + 
                          " §7(" + group.getBlocks().size() + " blocks)");
    }
    
    /**
     * Starts performance analysis for a script
     */
    public void startPerformanceAnalysis(Player player, CodeScript script) {
        PerformanceAnalyzer analyzer = new PerformanceAnalyzer(script);
        performanceAnalyzers.put(player.getUniqueId(), analyzer);
        player.sendMessage("§a✓ Performance analysis started for script: " + script.getName());
    }
    
    /**
     * Records execution data for performance analysis
     */
    public void recordExecutionData(Player player, CodeBlock block, Location location, long executionTime) {
        PerformanceAnalyzer analyzer = performanceAnalyzers.get(player.getUniqueId());
        if (analyzer != null) {
            analyzer.recordExecution(block, location, executionTime);
        }
    }
    
    /**
     * Shows performance analysis report
     */
    public void showPerformanceReport(Player player) {
        PerformanceAnalyzer analyzer = performanceAnalyzers.get(player.getUniqueId());
        if (analyzer == null) {
            player.sendMessage("§cNo performance analysis data available");
            return;
        }
        
        player.sendMessage("§6=== Performance Analysis Report ===");
        player.sendMessage("§fScript: " + analyzer.getScript().getName());
        player.sendMessage("§fTotal Executions: " + analyzer.getTotalExecutions());
        player.sendMessage("§fAverage Time: " + analyzer.getAverageExecutionTime() + "ms");
        player.sendMessage("§fSlowest Block: " + analyzer.getSlowestBlockTime() + "ms");
        player.sendMessage("§fFastest Block: " + analyzer.getFastestBlockTime() + "ms");
        
        // Show slowest blocks
        List<PerformanceAnalyzer.ExecutionRecord> slowest = analyzer.getSlowestBlocks(5);
        if (!slowest.isEmpty()) {
            player.sendMessage("§e§lSlowest Blocks:");
            for (int i = 0; i < slowest.size(); i++) {
                PerformanceAnalyzer.ExecutionRecord record = slowest.get(i);
                player.sendMessage("§7" + (i + 1) + ". §c" + record.getExecutionTime() + "ms §7- " + 
                                 record.getBlock().getAction() + " at " + formatLocation(record.getLocation()));
            }
        }
    }
    
    /**
     * Highlights a block with a specific color
     */
    private void highlightBlock(Player player, Location location, String color) {
        // In a real implementation, this would create visual effects using particles or block changes
        // For now, we'll just send a message
        player.sendMessage("§d♦ Highlighting block at " + formatLocation(location) + " with color " + color);
    }
    
    /**
     * Traces execution flow between blocks
     */
    private void traceExecutionFlow(Player player, Location location) {
        // In a real implementation, this would show particle trails or lines between blocks
        // For now, we'll just send a message
        player.sendMessage("§d→ Tracing execution flow to " + formatLocation(location));
    }
    
    /**
     * Maps performance data to blocks
     */
    private void mapPerformance(Player player, CodeBlock block, Location location) {
        // In a real implementation, this would color blocks based on their performance
        // For now, we'll just send a message
        player.sendMessage("§d⚡ Performance mapping for " + block.getAction() + " at " + formatLocation(location));
    }
    
    /**
     * Highlights a region with a specific color
     */
    private void highlightRegion(Player player, com.megacreative.coding.groups.BlockGroupManager.GroupBounds bounds, String color) {
        // In a real implementation, this would highlight the entire region
        // For now, we'll just send a message
        player.sendMessage("§d■ Highlighting region with color " + color);
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
     * Cleans up resources when plugin disables
     */
    public void cleanup() {
        visualizationSessions.clear();
        performanceAnalyzers.clear();
    }
    
    /**
     * Visualization modes
     */
    public enum VisualizationMode {
        BLOCK_HIGHLIGHTING("Block Highlighting"),
        FLOW_TRACING("Flow Tracing"),
        PERFORMANCE_MAPPING("Performance Mapping"),
        GROUP_MAPPING("Group Mapping");
        
        private final String displayName;
        
        VisualizationMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Represents a visualization session
     */
    @Data
    public static class VisualizationSession {
        private final Player player;
        private final VisualizationMode mode;
        private final long startTime;
        private long lastUpdate;
        
        public VisualizationSession(Player player, VisualizationMode mode) {
            this.player = player;
            this.mode = mode;
            this.startTime = System.currentTimeMillis();
            this.lastUpdate = this.startTime;
        }
        
        public void update() {
            this.lastUpdate = System.currentTimeMillis();
        }
    }
    
    /**
     * Analyzes performance of script execution
     */
    public static class PerformanceAnalyzer {
        private final CodeScript script;
        private final List<ExecutionRecord> executionRecords = new ArrayList<>();
        private long totalExecutionTime = 0;
        
        public PerformanceAnalyzer(CodeScript script) {
            this.script = script;
        }
        
        public void recordExecution(CodeBlock block, Location location, long executionTime) {
            executionRecords.add(new ExecutionRecord(block, location, executionTime));
            totalExecutionTime += executionTime;
        }
        
        public List<ExecutionRecord> getSlowestBlocks(int count) {
            return executionRecords.stream()
                .sorted((a, b) -> Long.compare(b.executionTime, a.executionTime))
                .limit(count)
                .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
        }
        
        public long getAverageExecutionTime() {
            return executionRecords.isEmpty() ? 0 : totalExecutionTime / executionRecords.size();
        }
        
        public long getSlowestBlockTime() {
            return executionRecords.stream()
                .mapToLong(ExecutionRecord::getExecutionTime)
                .max()
                .orElse(0);
        }
        
        public long getFastestBlockTime() {
            return executionRecords.stream()
                .mapToLong(ExecutionRecord::getExecutionTime)
                .min()
                .orElse(0);
        }
        
        // Getters
        public CodeScript getScript() { return script; }
        public int getTotalExecutions() { return executionRecords.size(); }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public List<ExecutionRecord> getExecutionRecords() { return new ArrayList<>(executionRecords); }
        
        /**
         * Represents a single execution record
         */
        @Data
        public static class ExecutionRecord {
            private final CodeBlock block;
            private final Location location;
            private final long executionTime;
            private final long timestamp;
            
            public ExecutionRecord(CodeBlock block, Location location, long executionTime) {
                this.block = block;
                this.location = location.clone();
                this.executionTime = executionTime;
                this.timestamp = System.currentTimeMillis();
            }
        }
    }
}