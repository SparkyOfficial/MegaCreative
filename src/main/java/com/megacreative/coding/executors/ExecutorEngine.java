package com.megacreative.coding.executors;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.executors.ExecutionResult;
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
    
    public ExecutorEngine(MegaCreative plugin, VariableManager variableManager) {
        this.plugin = plugin;
        this.variableManager = variableManager;
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
                
                // Execute with monitoring and get result
                ExecutionResult result = executeWithMonitoring(context);
                
                // Show completion visual effect
                showExecutionComplete(context, result);
                
                // Update statistics
                updateExecutionStats(context, result);
                
                return result;
                
            } catch (Exception e) {
                // Create error result with context
                ExecutionResult errorResult = context.createErrorResult("Execution failed: " + e.getMessage(), e);
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
        try {
            // Set start time in context
            context.setStartTime(System.currentTimeMillis());
            
            // Validate script before execution
            if (!validateScript(context.getScript())) {
                return context.createErrorResult("Script validation failed", null);
            }
            
            // Execute blocks sequentially with visual feedback
            var rootBlock = context.getScript().getRootBlock();
            if (rootBlock == null) {
                return context.createErrorResult("No root block found in script", null);
            }
            
            return executeBlockChain(rootBlock, context);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - context.getStartTime();
            plugin.getLogger().warning("Script execution failed after " + duration + "ms: " + e.getMessage());
            return context.createErrorResult("Execution error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes a chain of connected blocks
     */
    private ExecutionResult executeBlockChain(com.megacreative.coding.CodeBlock block, ExecutionContext context) {
        var currentBlock = block;
        ExecutionResult lastResult = context.createResult(true, "Block chain execution completed");
        
        try {
            while (currentBlock != null && !context.isCancelled()) {
                // Set current block in context for error reporting
                context.setCurrentBlock(currentBlock);
                
                // Show visual indicator for current block
                showBlockExecution(currentBlock, context);
                
                try {
                    // Execute current block
                    ExecutionResult blockResult = executeBlock(currentBlock, context);
                    
                    // Handle execution result
                    if (!blockResult.isSuccess()) {
                        showBlockError(currentBlock, context, blockResult.getMessage());
                        return blockResult;
                    }
                    
                    // Show block completion
                    showBlockComplete(currentBlock, context);
                    
                    // Store last successful result
                    lastResult = blockResult;
                    
                    // Move to next block
                    currentBlock = currentBlock.getNextBlock();
                    
                } catch (Exception e) {
                    // Create error result with current block context
                    ExecutionResult errorResult = context.createErrorResult("Block execution failed: " + e.getMessage(), e);
                    showBlockError(currentBlock, context, errorResult.getMessage());
                    return errorResult;
                }
            }
            
            return lastResult;
            
        } finally {
            // Clear current block from context
            context.setCurrentBlock(null);
        }
    }
    
    /**
     * Executes a single block with type-specific handling
     */
    private ExecutionResult executeBlock(com.megacreative.coding.CodeBlock block, ExecutionContext context) {
        try {
            String action = block.getAction();
            Map<String, DataValue> parameters = block.getParameters() != null ? block.getParameters() : new HashMap<>();
            
            // Log block execution
            plugin.getLogger().fine("Executing block: " + action + " with params: " + parameters);
            
            // Execute the appropriate action
            ExecutionResult result;
            switch (action) {
                case "sendMessage":
                    result = executeSendMessage(parameters, context);
                    break;
                case "teleport":
                    result = executeTeleport(parameters, context);
                    break;
                case "giveItem":
                    result = executeGiveItem(parameters, context);
                    break;
                case "playSound":
                    result = executePlaySound(parameters, context);
                    break;
                case "setVar":
                    result = executeSetVariable(parameters, context);
                    break;
                case "ifVarEquals":
                    result = executeIfVariable(block, parameters, context);
                    break;
                default:
                    return context.createErrorResult("Unknown action: " + action);
            }
            
            // Log the result
            if (result.isSuccess()) {
                plugin.getLogger().fine("Block executed successfully: " + action);
            } else {
                plugin.getLogger().warning("Block execution failed: " + action + ". Error: " + result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Unexpected error executing block: " + e.getMessage() + ". Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            return context.createErrorResult(e);
        }
    }
    
    // === SPECIFIC ACTION EXECUTORS ===
    
    private ExecutionResult executeSendMessage(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue messageValue = params.get("message");
            if (messageValue == null) {
                return context.createErrorResult("No message specified");
            }
            
            String message = messageValue.asString();
            if (message == null || message.trim().isEmpty()) {
                return context.createErrorResult("Message cannot be empty");
            }
            
            // Resolve placeholders and send message
            message = resolvePlaceholders(message, context);
            context.getPlayer().sendMessage(message);
            
            return context.createResult(true, "Message sent");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to send message: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeTeleport(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue locationValue = params.get("location");
            if (locationValue == null) {
                return context.createErrorResult("No location specified for teleport");
            }
            
            // In a real implementation, you'd parse the location string
            // For now, we'll just show an effect at the current location
            Player player = context.getPlayer();
            Location currentLoc = player.getLocation();
            
            // Visual effects
            player.spawnParticle(Particle.PORTAL, currentLoc, 20);
            player.playSound(currentLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            
            // In a real implementation, you'd do the actual teleport here
            // player.teleport(targetLocation);
            
            return context.createResult(true, "Teleport effect played");
            
        } catch (Exception e) {
            return context.createErrorResult("Teleport failed: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeGiveItem(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue itemValue = params.get("item");
            if (itemValue == null) {
                return context.createErrorResult("No item specified");
            }
        
            String materialName = itemValue.asString();
            if (materialName == null || materialName.trim().isEmpty()) {
                return context.createErrorResult("Item name cannot be empty");
            }
            
            // Try to get the material
            org.bukkit.Material material;
            try {
                material = org.bukkit.Material.valueOf(materialName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid item material: " + materialName);
            }
            
            // Create and give the item
            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material, 1);
            Player player = context.getPlayer();
            
            // Add to inventory or drop if full
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                player.sendMessage("§eYour inventory is full, the item was dropped at your feet!");
            } else {
                player.getInventory().addItem(item);
            }
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
            
            return context.createResult(true, "Given " + material.name().toLowerCase().replace("_", " "));
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to give item: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executePlaySound(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue soundValue = params.get("sound");
            if (soundValue == null) {
                return context.createErrorResult("No sound specified");
            }
            
            String soundName = soundValue.asString();
            if (soundName == null || soundName.trim().isEmpty()) {
                return context.createErrorResult("Sound name cannot be empty");
            }
            
            // Get volume and pitch with defaults
            float volume = 1.0f;
            float pitch = 1.0f;
            
            try {
                if (params.containsKey("volume")) {
                    volume = Float.parseFloat(params.get("volume").asString());
                }
                if (params.containsKey("pitch")) {
                    pitch = Float.parseFloat(params.get("pitch").asString());
                }
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid number format for volume or pitch");
            }
            
            // Try to get the sound
            Sound sound;
            try {
                sound = Sound.valueOf(soundName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid sound: " + soundName);
            }
            
            // Play the sound
            Player player = context.getPlayer();
            Location location = player.getLocation();
            player.playSound(location, sound, volume, pitch);
            
            // Visual effect
            player.spawnParticle(Particle.NOTE, location.add(0, 2, 0), 5);
            
            return context.createResult(true, "Played sound: " + sound.name().toLowerCase().replace("_", " "));
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to play sound: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSetVariable(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue varNameValue = params.get("variable");
            DataValue value = params.get("value");
            
            if (varNameValue == null) {
                return context.createErrorResult("Variable name not specified");
            }
            
            String varName = varNameValue.asString();
            if (varName == null || varName.trim().isEmpty()) {
                return context.createErrorResult("Variable name cannot be empty");
            }
            
            // Get world and script IDs for variable scoping
            String scriptId = context.getScript() != null ? 
                context.getScript().getId().toString() : "global";
            String worldName = context.getPlayer() != null ? 
                context.getPlayer().getWorld().getName() : "global";
            
            // Set the variable
            variableManager.setLocalVariable(scriptId, varName, value);
            
            return context.createResult(true, "Set variable " + varName + " to " + 
                (value != null ? value.asString() : "null"));
                
        } catch (Exception e) {
            return context.createErrorResult("Failed to set variable: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIfVariable(com.megacreative.coding.CodeBlock block, 
                                            Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue varNameValue = params.get("variable");
            DataValue expectedValue = params.get("value");
            
            if (varNameValue == null) {
                return context.createErrorResult("Variable name not specified");
            }
            
            String varName = varNameValue.asString();
            if (varName == null || varName.trim().isEmpty()) {
                return context.createErrorResult("Variable name cannot be empty");
            }
            
            // Get world and script IDs for variable scoping
            String scriptId = context.getScript() != null ? 
                context.getScript().getId().toString() : "global";
            String worldName = context.getPlayer() != null ? 
                context.getPlayer().getWorld().getName() : "global";
            
            // Get current variable value
            DataValue currentValue = variableManager.getLocalVariable(scriptId, varName);
            
            // Check condition
            boolean condition = false;
            if (currentValue != null && expectedValue != null) {
                condition = Objects.equals(currentValue.getValue(), expectedValue.getValue());
            } else if (currentValue == null && expectedValue == null) {
                condition = true; // Both null is considered equal
            }
            
            // Log the condition check
            plugin.getLogger().fine(String.format("Condition check: %s == %s -> %s",
                currentValue != null ? currentValue.asString() : "null",
                expectedValue != null ? expectedValue.asString() : "null",
                condition));
            
            // Execute child blocks if condition is true and there are children
            if (condition && block.getChildren() != null && !block.getChildren().isEmpty()) {
                for (var childBlock : block.getChildren()) {
                    if (childBlock == null) continue;
                    
                    ExecutionResult childResult = executeBlockChain(childBlock, context);
                    if (!childResult.isSuccess()) {
                        return childResult; // Return first failure
                    }
                    
                    // Stop if execution was cancelled
                    if (context.isCancelled()) {
                        return context.createResult(false, "Execution cancelled");
                    }
                }
            }
            
            return context.createResult(true, "Condition evaluated: " + condition);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to evaluate condition: " + e.getMessage(), e);
        }
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

class ExecutionContext {
    private final String executionId;
    private final CodeScript script;
    private final Player player;
    private final String trigger;
    private long startTime;
    private boolean cancelled = false;
    private CodeBlock currentBlock;
    
    public ExecutionContext(String executionId, CodeScript script, Player player, String trigger) {
        this.executionId = executionId;
        this.script = script;
        this.player = player;
        this.trigger = trigger;
        this.startTime = System.currentTimeMillis();
    }
    
    // Getters
    public String getExecutionId() { return executionId; }
    public CodeScript getScript() { return script; }
    public Player getPlayer() { return player; }
    public String getTrigger() { return trigger; }
    public long getStartTime() { return startTime; }
    public boolean isCancelled() { return cancelled; }
    public CodeBlock getCurrentBlock() { return currentBlock; }
    
    // Setters
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public void setCurrentBlock(CodeBlock block) { this.currentBlock = block; }
    
    // Control methods
    public void cancel() { this.cancelled = true; }
    
    /**
     * Creates a success result with the given message
     */
    public ExecutionResult createResult(boolean success, String message) {
        return new ExecutionResult.Builder()
            .success(success)
            .message(message)
            .executedBlock(currentBlock)
            .executor(player)
            .executionTime(System.currentTimeMillis() - startTime)
            .build();
    }
    
    /**
     * Creates a success result with the default message
     */
    public ExecutionResult createResult(boolean success) {
        return createResult(success, success ? "Operation completed successfully" : "Operation failed");
    }
    
    /**
     * Creates an error result with the given message and throwable
     */
    public ExecutionResult createErrorResult(String errorMessage, Throwable error) {
        return new ExecutionResult.Builder()
            .success(false)
            .message(errorMessage)
            .executedBlock(currentBlock)
            .executor(player)
            .error(error)
            .executionTime(System.currentTimeMillis() - startTime)
            .build();
    }
    
    /**
     * Creates an error result with the given message
     */
    public ExecutionResult createErrorResult(String errorMessage) {
        return createErrorResult(errorMessage, null);
    }
    
    /**
     * Creates an error result from an exception
     */
    public ExecutionResult createErrorResult(Throwable error) {
        return createErrorResult(error.getMessage(), error);
    }
}

// End of file