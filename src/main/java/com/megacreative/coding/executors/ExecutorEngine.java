package com.megacreative.coding.executors;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * Advanced executor engine with visual feedback and performance optimization
 * Features:
 * - Async execution with visual indicators
 * - Real-time debugging with particle effects
 * - Performance monitoring and optimization
 * - Error handling with detailed feedback
 * - Multi-threaded script execution
 */
public class ExecutorEngine {
    
    private final MegaCreative plugin;
    private final VariableManager variableManager;
    
    // Execution tracking
    private final Map<String, ExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    private final Map<String, ExecutionStats> executionStats = new ConcurrentHashMap<>();
    
    // Visual feedback
    private final Map<Location, VisualIndicator> visualIndicators = new ConcurrentHashMap<>();
    
    public ExecutorEngine(MegaCreative plugin) {
        this.plugin = plugin;
        this.variableManager = plugin.getVariableManager();
        startVisualUpdater();
    }
    
    /**
     * Executes a script with full visual feedback and monitoring
     */
    public CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, String trigger) {
        String executionId = UUID.randomUUID().toString();
        
        ExecutionContext context = new ExecutionContext(executionId, script, player, trigger);
        activeExecutions.put(executionId, context);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Show start visual effect
                showExecutionStart(context);
                
                ExecutionResult result = executeWithMonitoring(context);
                
                // Show completion visual effect
                showExecutionComplete(context, result);
                
                // Update statistics
                updateExecutionStats(context, result);
                
                return result;
                
            } catch (Exception e) {
                ExecutionResult errorResult = ExecutionResult.error("Execution failed: " + e.getMessage());
                showExecutionError(context, e);
                return errorResult;
                
            } finally {
                activeExecutions.remove(executionId);
            }
        });
    }
    
    /**
     * Executes script with detailed monitoring and visual feedback
     */
    private ExecutionResult executeWithMonitoring(ExecutionContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate script before execution
            if (!validateScript(context.getScript())) {
                return ExecutionResult.error("Script validation failed");
            }
            
            // Execute blocks sequentially with visual feedback
            var rootBlock = context.getScript().getRootBlock();
            return executeBlockChain(rootBlock, context);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            plugin.getLogger().warning("Script execution failed after " + duration + "ms: " + e.getMessage());
            return ExecutionResult.error("Execution error: " + e.getMessage());
        }
    }
    
    /**
     * Executes a chain of connected blocks
     */
    private ExecutionResult executeBlockChain(com.megacreative.coding.CodeBlock block, ExecutionContext context) {
        var currentBlock = block;
        ExecutionResult lastResult = ExecutionResult.success();
        
        while (currentBlock != null && !context.isCancelled()) {
            // Show visual indicator for current block
            showBlockExecution(currentBlock, context);
            
            // Execute current block
            ExecutionResult blockResult = executeBlock(currentBlock, context);
            
            // Handle execution result
            if (!blockResult.isSuccess()) {
                showBlockError(currentBlock, context, blockResult.getError());
                return blockResult;
            }
            
            // Show block completion
            showBlockComplete(currentBlock, context);
            
            // Move to next block
            currentBlock = currentBlock.getNextBlock();
            lastResult = blockResult;
        }
        
        return lastResult;
    }
    
    /**
     * Executes a single block with type-specific handling
     */
    private ExecutionResult executeBlock(com.megacreative.coding.CodeBlock block, ExecutionContext context) {
        try {
            String action = block.getAction();
            Map<String, Object> parameters = block.getParameters();
            
            switch (action) {
                case "sendMessage":
                    return executeSendMessage(parameters, context);
                case "teleport":
                    return executeTeleport(parameters, context);
                case "giveItem":
                    return executeGiveItem(parameters, context);
                case "playSound":
                    return executePlaySound(parameters, context);
                case "setVar":
                    return executeSetVariable(parameters, context);
                case "ifVarEquals":
                    return executeIfVariable(block, parameters, context);
                default:
                    return ExecutionResult.error("Unknown action: " + action);
            }
            
        } catch (Exception e) {
            return ExecutionResult.error("Block execution failed: " + e.getMessage());
        }
    }
    
    // === SPECIFIC ACTION EXECUTORS ===
    
    private ExecutionResult executeSendMessage(Map<String, Object> params, ExecutionContext context) {
        String message = (String) params.getOrDefault("message", "Hello World!");
        
        // Resolve placeholders
        message = resolvePlaceholders(message, context);
        
        context.getPlayer().sendMessage(message);
        return ExecutionResult.success("Message sent: " + message);
    }
    
    private ExecutionResult executeTeleport(Map<String, Object> params, ExecutionContext context) {
        Location location = (Location) params.get("location");
        if (location == null) {
            return ExecutionResult.error("No location specified for teleport");
        }
        
        Player player = context.getPlayer();
        player.teleport(location);
        
        // Visual effect
        player.spawnParticle(Particle.PORTAL, player.getLocation(), 20);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        
        return ExecutionResult.success("Player teleported to " + location);
    }
    
    private ExecutionResult executeGiveItem(Map<String, Object> params, ExecutionContext context) {
        org.bukkit.inventory.ItemStack item = (org.bukkit.inventory.ItemStack) params.get("item");
        if (item == null) {
            return ExecutionResult.error("No item specified");
        }
        
        Player player = context.getPlayer();
        player.getInventory().addItem(item);
        
        // Visual effect
        player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
        
        return ExecutionResult.success("Item given: " + item.getType().name());
    }
    
    private ExecutionResult executePlaySound(Map<String, Object> params, ExecutionContext context) {
        String soundName = (String) params.get("sound");
        float volume = ((Number) params.getOrDefault("volume", 1.0)).floatValue();
        float pitch = ((Number) params.getOrDefault("pitch", 1.0)).floatValue();
        
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            context.getPlayer().playSound(context.getPlayer().getLocation(), sound, volume, pitch);
            return ExecutionResult.success("Sound played: " + soundName);
        } catch (IllegalArgumentException e) {
            return ExecutionResult.error("Invalid sound: " + soundName);
        }
    }
    
    private ExecutionResult executeSetVariable(Map<String, Object> params, ExecutionContext context) {
        String varName = (String) params.get("variable");
        Object value = params.get("value");
        
        if (varName == null) {
            return ExecutionResult.error("Variable name not specified");
        }
        
        DataValue dataValue = DataValue.fromObject(value);
        variableManager.setVariable(varName, dataValue, context.getScript().getId().toString(), 
                                   context.getPlayer().getWorld().getName());
        
        return ExecutionResult.success("Variable set: " + varName + " = " + value);
    }
    
    private ExecutionResult executeIfVariable(com.megacreative.coding.CodeBlock block, 
                                            Map<String, Object> params, ExecutionContext context) {
        String varName = (String) params.get("variable");
        Object expectedValue = params.get("value");
        
        if (varName == null) {
            return ExecutionResult.error("Variable name not specified");
        }
        
        DataValue currentValue = variableManager.getVariable(varName, 
                context.getScript().getId().toString(), context.getPlayer().getWorld().getName());
        
        boolean condition = false;
        if (currentValue != null && expectedValue != null) {
            condition = Objects.equals(currentValue.getValue(), expectedValue);
        }
        
        // Execute child blocks if condition is true
        if (condition && !block.getChildren().isEmpty()) {
            for (var childBlock : block.getChildren()) {
                ExecutionResult childResult = executeBlockChain(childBlock, context);
                if (!childResult.isSuccess()) {
                    return childResult;
                }
            }
        }
        
        return ExecutionResult.success("Condition evaluated: " + condition);
    }
    
    // === VISUAL FEEDBACK SYSTEM ===
    
    private void showExecutionStart(ExecutionContext context) {
        Player player = context.getPlayer();
        
        // Particle effect
        player.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation().add(0, 2, 0), 10);
        
        // Sound effect
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.5f);
        
        // Action bar message
        player.sendActionBar("§a§lScript Executing...");
        
        plugin.getLogger().fine("Started script execution: " + context.getExecutionId());
    }
    
    private void showExecutionComplete(ExecutionContext context, ExecutionResult result) {
        Player player = context.getPlayer();
        
        if (result.isSuccess()) {
            // Success effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 15);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
            player.sendActionBar("§a§lScript Completed Successfully!");
        } else {
            // Error effects
            player.spawnParticle(Particle.SMOKE_LARGE, player.getLocation().add(0, 2, 0), 10);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 0.8f);
            player.sendActionBar("§c§lScript Failed: " + result.getError());
        }
    }
    
    private void showBlockExecution(com.megacreative.coding.CodeBlock block, ExecutionContext context) {
        // Find block location in world
        Location blockLocation = findBlockLocation(block, context);
        if (blockLocation != null) {
            // Glowing effect
            blockLocation.getWorld().spawnParticle(Particle.END_ROD, 
                    blockLocation.add(0.5, 1, 0.5), 3, 0.2, 0.2, 0.2, 0.02);
            
            // Create visual indicator
            VisualIndicator indicator = new VisualIndicator(blockLocation, IndicatorType.EXECUTING, 
                    System.currentTimeMillis() + 2000);
            visualIndicators.put(blockLocation, indicator);
        }
    }
    
    private void showBlockComplete(com.megacreative.coding.CodeBlock block, ExecutionContext context) {
        Location blockLocation = findBlockLocation(block, context);
        if (blockLocation != null) {
            // Success particle
            blockLocation.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, 
                    blockLocation.add(0.5, 1, 0.5), 5);
            
            // Update visual indicator
            VisualIndicator indicator = new VisualIndicator(blockLocation, IndicatorType.COMPLETED, 
                    System.currentTimeMillis() + 1000);
            visualIndicators.put(blockLocation, indicator);
        }
    }
    
    private void showBlockError(com.megacreative.coding.CodeBlock block, ExecutionContext context, String error) {
        Location blockLocation = findBlockLocation(block, context);
        if (blockLocation != null) {
            // Error particle
            blockLocation.getWorld().spawnParticle(Particle.SMOKE_LARGE, 
                    blockLocation.add(0.5, 1, 0.5), 8);
            
            // Error indicator
            VisualIndicator indicator = new VisualIndicator(blockLocation, IndicatorType.ERROR, 
                    System.currentTimeMillis() + 5000);
            visualIndicators.put(blockLocation, indicator);
        }
        
        context.getPlayer().sendMessage("§c[Block Error] " + error);
    }
    
    private void showExecutionError(ExecutionContext context, Exception e) {
        Player player = context.getPlayer();
        
        // Error effects
        player.spawnParticle(Particle.LAVA, player.getLocation().add(0, 2, 0), 20);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.5f);
        player.sendActionBar("§4§lExecution Error!");
        
        plugin.getLogger().warning("Script execution error: " + e.getMessage());
    }
    
    // === VISUAL UPDATER ===
    
    private void startVisualUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateVisualIndicators();
            }
        }.runTaskTimer(plugin, 0, 5); // Update every 5 ticks
    }
    
    private void updateVisualIndicators() {
        long currentTime = System.currentTimeMillis();
        
        visualIndicators.entrySet().removeIf(entry -> {
            VisualIndicator indicator = entry.getValue();
            
            if (currentTime > indicator.getExpireTime()) {
                return true; // Remove expired indicators
            }
            
            // Update visual effect
            Location location = entry.getKey();
            switch (indicator.getType()) {
                case EXECUTING:
                    location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, 
                            location.add(0.5, 1, 0.5), 2);
                    break;
                case COMPLETED:
                    location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, 
                            location.add(0.5, 1, 0.5), 1);
                    break;
                case ERROR:
                    location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, 
                            location.add(0.5, 1, 0.5), 1);
                    break;
            }
            
            return false;
        });
    }
    
    // === HELPER METHODS ===
    
    private boolean validateScript(CodeScript script) {
        if (script.getRootBlock() == null) {
            return false;
        }
        
        // Additional validation logic
        return script.isValid();
    }
    
    private String resolvePlaceholders(String text, ExecutionContext context) {
        return text.replace("%player%", context.getPlayer().getName())
                  .replace("%world%", context.getPlayer().getWorld().getName())
                  .replace("%x%", String.valueOf(context.getPlayer().getLocation().getBlockX()))
                  .replace("%y%", String.valueOf(context.getPlayer().getLocation().getBlockY()))
                  .replace("%z%", String.valueOf(context.getPlayer().getLocation().getBlockZ()));
    }
    
    private Location findBlockLocation(com.megacreative.coding.CodeBlock block, ExecutionContext context) {
        // TODO: Integrate with AutoConnectionManager to find actual block location
        return context.getPlayer().getLocation();
    }
    
    private void updateExecutionStats(ExecutionContext context, ExecutionResult result) {
        String scriptId = context.getScript().getId().toString();
        ExecutionStats stats = executionStats.computeIfAbsent(scriptId, k -> new ExecutionStats());
        
        stats.incrementExecutions();
        if (result.isSuccess()) {
            stats.incrementSuccesses();
        } else {
            stats.incrementFailures();
        }
        
        long duration = System.currentTimeMillis() - context.getStartTime();
        stats.addExecutionTime(duration);
    }
    
    public void shutdown() {
        // Cancel all active executions
        for (ExecutionContext context : activeExecutions.values()) {
            context.cancel();
        }
        
        activeExecutions.clear();
        visualIndicators.clear();
        
        plugin.getLogger().info("Executor engine shut down");
    }
}

// Supporting classes would be in separate files for clarity
enum IndicatorType { EXECUTING, COMPLETED, ERROR }

class VisualIndicator {
    private final Location location;
    private final IndicatorType type;
    private final long expireTime;
    
    public VisualIndicator(Location location, IndicatorType type, long expireTime) {
        this.location = location;
        this.type = type;
        this.expireTime = expireTime;
    }
    
    public Location getLocation() { return location; }
    public IndicatorType getType() { return type; }
    public long getExpireTime() { return expireTime; }
}

class ExecutionStats {
    private int totalExecutions = 0;
    private int successes = 0;
    private int failures = 0;
    private long totalExecutionTime = 0;
    
    public void incrementExecutions() { totalExecutions++; }
    public void incrementSuccesses() { successes++; }
    public void incrementFailures() { failures++; }
    public void addExecutionTime(long time) { totalExecutionTime += time; }
    
    public int getTotalExecutions() { return totalExecutions; }
    public int getSuccesses() { return successes; }
    public int getFailures() { return failures; }
    public double getAverageExecutionTime() { 
        return totalExecutions > 0 ? (double) totalExecutionTime / totalExecutions : 0; 
    }
}

class ExecutionResult {
    private final boolean success;
    private final String message;
    private final String error;
    
    private ExecutionResult(boolean success, String message, String error) {
        this.success = success;
        this.message = message;
        this.error = error;
    }
    
    public static ExecutionResult success() { return new ExecutionResult(true, "Success", null); }
    public static ExecutionResult success(String message) { return new ExecutionResult(true, message, null); }
    public static ExecutionResult error(String error) { return new ExecutionResult(false, null, error); }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getError() { return error; }
}

class ExecutionContext {
    private final String executionId;
    private final CodeScript script;
    private final Player player;
    private final String trigger;
    private final long startTime;
    private boolean cancelled = false;
    
    public ExecutionContext(String executionId, CodeScript script, Player player, String trigger) {
        this.executionId = executionId;
        this.script = script;
        this.player = player;
        this.trigger = trigger;
        this.startTime = System.currentTimeMillis();
    }
    
    public String getExecutionId() { return executionId; }
    public CodeScript getScript() { return script; }
    public Player getPlayer() { return player; }
    public String getTrigger() { return trigger; }
    public long getStartTime() { return startTime; }
    public boolean isCancelled() { return cancelled; }
    public void cancel() { this.cancelled = true; }
}