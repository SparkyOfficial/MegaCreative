package com.megacreative.coding.executors;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.placeholders.ReferenceSystemPlaceholderResolver;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
    private final com.megacreative.services.BlockConfigService blockConfigService; // Add BlockConfigService reference
    
    // Execution tracking
    private final Map<String, ExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    private final Map<String, ExecutionStats> executionStats = new ConcurrentHashMap<>();
    
    // Visual feedback
    private final Map<Location, VisualIndicator> visualIndicators = new ConcurrentHashMap<>();
    
    public ExecutorEngine(MegaCreative plugin, VariableManager variableManager) {
        this.plugin = plugin;
        this.variableManager = variableManager;
        // Initialize BlockConfigService reference
        this.blockConfigService = plugin.getServiceRegistry().getService(com.megacreative.services.BlockConfigService.class);
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
     * Executes a single block with type-specific handling and enhanced parameter validation
     */
    private ExecutionResult executeBlock(com.megacreative.coding.CodeBlock block, ExecutionContext context) {
        try {
            String action = block.getAction();
            Map<String, DataValue> parameters = block.getParameters() != null ? block.getParameters() : new HashMap<>();
            
            // Log block execution
            plugin.getLogger().fine("Executing block: " + action + " with params: " + parameters);
            
            // 🎆 ENHANCED: Preprocess parameters with validation and placeholder resolution
            Map<String, DataValue> processedParameters = preprocessParameters(block, context);
            
            // Check for validation errors
            String validationError = validateBlockParameters(block, processedParameters);
            if (validationError != null) {
                // Create detailed error result with validation information
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("action", action);
                errorDetails.put("validationError", validationError);
                errorDetails.put("processedParameters", processedParameters);
                
                ExecutionResult errorResult = new ExecutionResult.Builder()
                    .success(false)
                    .message("Parameter validation failed: " + validationError)
                    .executedBlock(block)
                    .executor(context.getPlayer())
                    .executionTime(System.currentTimeMillis() - context.getStartTime())
                    .details(errorDetails)
                    .build();
                
                // Log the details
                plugin.getLogger().warning("Validation failed for action '" + action + "': " + validationError);
                plugin.getLogger().warning("Parameters: " + processedParameters);
                
                return errorResult;
            }
            
            // Execute the appropriate action
            ExecutionResult result;
            switch (action) {
                case "sendMessage":
                    result = executeSendMessage(processedParameters, context);
                    break;
                case "teleport":
                    result = executeTeleport(processedParameters, context);
                    break;
                case "giveItem":
                    result = executeGiveItem(processedParameters, context);
                    break;
                case "playSound":
                    result = executePlaySound(processedParameters, context);
                    break;
                case "effect":
                    result = executeEffect(processedParameters, context);
                    break;
                case "command":
                    result = executeCommand(processedParameters, context);
                    break;
                case "setVar":
                    result = executeSetVariable(processedParameters, context);
                    break;
                case "ifVarEquals":
                    result = executeIfVariable(block, processedParameters, context);
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
    
    private ExecutionResult executeEffect(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue effectValue = params.get("effect_type");
            if (effectValue == null) {
                return context.createErrorResult("No effect specified");
            }
            
            String effectName = effectValue.asString();
            if (effectName == null || effectName.trim().isEmpty()) {
                return context.createErrorResult("Effect name cannot be empty");
            }
            
            // Get duration and amplifier with defaults
            int duration = 30; // Default 30 seconds
            int amplifier = 1; // Default level 1
            
            try {
                if (params.containsKey("duration")) {
                    duration = Integer.parseInt(params.get("duration").asString());
                }
                if (params.containsKey("amplifier")) {
                    amplifier = Integer.parseInt(params.get("amplifier").asString());
                }
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid number format for duration or amplifier");
            }
            
            // Try to get the effect
            org.bukkit.potion.PotionEffectType effectType;
            try {
                effectType = org.bukkit.potion.PotionEffectType.getByName(effectName.toUpperCase());
                if (effectType == null) {
                    return context.createErrorResult("Invalid effect: " + effectName);
                }
            } catch (Exception e) {
                return context.createErrorResult("Invalid effect: " + effectName);
            }
            
            // Apply the effect
            Player player = context.getPlayer();
            org.bukkit.potion.PotionEffect effect = new org.bukkit.potion.PotionEffect(
                effectType, duration * 20, amplifier - 1, false, true);
            player.addPotionEffect(effect);
            
            // Visual effect
            player.spawnParticle(org.bukkit.Particle.SPELL_WITCH, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Applied effect: " + effectType.getName().toLowerCase().replace("_", " "));
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to apply effect: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeCommand(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue commandValue = params.get("command_slot");
            if (commandValue == null) {
                return context.createErrorResult("No command specified");
            }
            
            String command = commandValue.asString();
            if (command == null || command.trim().isEmpty()) {
                return context.createErrorResult("Command cannot be empty");
            }
            
            // Resolve placeholders in command
            command = resolvePlaceholders(command, context);
            
            // Execute the command
            boolean success = plugin.getServer().dispatchCommand(context.getPlayer(), command);
            
            // Visual effect
            context.getPlayer().spawnParticle(org.bukkit.Particle.COMMAND, context.getPlayer().getLocation().add(0, 2, 0), 5);
            
            return context.createResult(success, "Executed command: " + command);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to execute command: " + e.getMessage(), e);
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
    
    /**
     * 🎆 ENHANCED: Preprocess parameters with placeholder resolution and validation
     */
    private Map<String, DataValue> preprocessParameters(com.megacreative.coding.CodeBlock block, ExecutionContext context) {
        Map<String, DataValue> processedParams = new HashMap<>();
        
        // Process configuration items from GUI
        if (block.hasConfigItems()) {
            // Get action configuration
            var actionConfigurations = blockConfigService.getActionConfigurations();
            if (actionConfigurations != null) {
                var actionConfig = actionConfigurations.getConfigurationSection(block.getAction());
                if (actionConfig != null) {
                    var slotsConfig = actionConfig.getConfigurationSection("slots");
                    if (slotsConfig != null) {
                        // Process each configured slot
                        for (String slotKey : slotsConfig.getKeys(false)) {
                            try {
                                int slotIndex = Integer.parseInt(slotKey);
                                ItemStack configItem = block.getConfigItem(slotIndex);
                                
                                if (configItem != null && configItem.hasItemMeta()) {
                                    ItemMeta meta = configItem.getItemMeta();
                                    if (meta != null && meta.hasDisplayName()) {
                                        String slotName = slotsConfig.getConfigurationSection(slotKey).getString("slot_name", "slot_" + slotIndex);
                                        String rawValue = meta.getDisplayName();
                                        
                                        // Resolve placeholders in the value using reference system
                                        String resolvedValue = ReferenceSystemPlaceholderResolver.resolvePlaceholders(rawValue, createCodingContext(context));
                                        
                                        // Store processed parameter
                                        processedParams.put(slotName, DataValue.of(resolvedValue));
                                    }
                                }
                            } catch (NumberFormatException e) {
                                // Skip invalid slot indices
                            }
                        }
                    }
                    
                    // Process item groups
                    var itemGroupsConfig = actionConfig.getConfigurationSection("item_groups");
                    if (itemGroupsConfig != null) {
                        for (String groupKey : itemGroupsConfig.getKeys(false)) {
                            var groupConfig = itemGroupsConfig.getConfigurationSection(groupKey);
                            if (groupConfig != null) {
                                List<Integer> slots = groupConfig.getIntegerList("slots");
                                String groupName = groupConfig.getString("name", groupKey);
                                
                                // Process items in the group
                                List<String> groupItems = new ArrayList<>();
                                for (int slotIndex : slots) {
                                    ItemStack configItem = block.getConfigItem(slotIndex);
                                    if (configItem != null && configItem.hasItemMeta()) {
                                        ItemMeta meta = configItem.getItemMeta();
                                        if (meta != null && meta.hasDisplayName()) {
                                            String rawValue = meta.getDisplayName();
                                            String resolvedValue = ReferenceSystemPlaceholderResolver.resolvePlaceholders(rawValue, createCodingContext(context));
                                            groupItems.add(resolvedValue);
                                        }
                                    }
                                }
                                
                                // Store group items as a comma-separated string
                                if (!groupItems.isEmpty()) {
                                    processedParams.put(groupName, DataValue.of(String.join(",", groupItems)));
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Merge with existing parameters
        if (block.getParameters() != null) {
            processedParams.putAll(block.getParameters());
        }
        
        // Apply additional preprocessing transformations
        return applyParameterTransformations(processedParams, context);
    }
    
    /**
     * 🎆 ENHANCED: Create a coding.ExecutionContext from executors.ExecutionContext
     */
    private com.megacreative.coding.ExecutionContext createCodingContext(ExecutionContext context) {
        return new com.megacreative.coding.ExecutionContext.Builder()
            .plugin(plugin)
            .player(context.getPlayer())
            .build();
    }
    
    /**
     * 🎆 ENHANCED: Apply additional parameter transformations
     */
    private Map<String, DataValue> applyParameterTransformations(Map<String, DataValue> parameters, ExecutionContext context) {
        Map<String, DataValue> transformedParams = new HashMap<>(parameters);
        
        // Apply transformations based on parameter names and values
        for (Map.Entry<String, DataValue> entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            DataValue paramValue = entry.getValue();
            
            if (paramValue != null && paramValue.asString() != null) {
                String value = paramValue.asString();
                
                // Handle special parameter transformations
                if (paramName.startsWith("number:")) {
                    // Convert to number
                    try {
                        Double.parseDouble(value);
                        // Store as numeric value
                        transformedParams.put(paramName.substring(7), DataValue.of(Double.parseDouble(value)));
                    } catch (NumberFormatException e) {
                        // Keep as string if not a valid number
                    }
                } else if (paramName.startsWith("boolean:")) {
                    // Convert to boolean
                    boolean boolValue = "true".equalsIgnoreCase(value) || "1".equals(value);
                    transformedParams.put(paramName.substring(8), DataValue.of(boolValue));
                } else if (paramName.startsWith("list:")) {
                    // Convert to list
                    String[] items = value.split(",");
                    List<DataValue> listItems = new ArrayList<>();
                    for (String item : items) {
                        listItems.add(DataValue.of(item.trim()));
                    }
                    transformedParams.put(paramName.substring(5), DataValue.of(listItems));
                }
            }
        }
        
        return transformedParams;
    }
    
    /**
     * 🎆 ENHANCED: Validate block parameters based on configuration
     */
    private String validateBlockParameters(com.megacreative.coding.CodeBlock block, Map<String, DataValue> parameters) {
        var actionConfigurations = blockConfigService.getActionConfigurations();
        if (actionConfigurations == null) return null;
        
        var actionConfig = actionConfigurations.getConfigurationSection(block.getAction());
        if (actionConfig == null) return null;
        
        var slotsConfig = actionConfig.getConfigurationSection("slots");
        if (slotsConfig == null) return null;
        
        // First pass: Validate each parameter based on slot configuration
        for (String slotKey : slotsConfig.getKeys(false)) {
            try {
                var slotConfig = slotsConfig.getConfigurationSection(slotKey);
                if (slotConfig == null) continue;
                
                String slotName = slotConfig.getString("slot_name", "slot_" + slotKey);
                boolean required = slotConfig.getBoolean("required", false);
                String validationRule = slotConfig.getString("validation");
                
                // Check if required parameter is missing
                if (required && !parameters.containsKey(slotName)) {
                    return "Required parameter '" + slotName + "' is missing";
                }
                
                // Validate parameter value if present
                if (parameters.containsKey(slotName) && validationRule != null && !validationRule.isEmpty()) {
                    DataValue value = parameters.get(slotName);
                    if (value != null) {
                        String error = validateParameterValue(slotName, value.asString(), validationRule);
                        if (error != null) {
                            return error;
                        }
                    }
                }
            } catch (Exception e) {
                // Continue with other validations
            }
        }
        
        // Second pass: Validate dependent parameters
        for (String slotKey : slotsConfig.getKeys(false)) {
            try {
                var slotConfig = slotsConfig.getConfigurationSection(slotKey);
                if (slotConfig == null) continue;
                
                String slotName = slotConfig.getString("slot_name", "slot_" + slotKey);
                String dependsOn = slotConfig.getString("depends_on");
                
                // Check dependencies
                if (dependsOn != null && !dependsOn.isEmpty()) {
                    String error = validateDependentParameter(slotName, dependsOn, parameters);
                    if (error != null) {
                        return error;
                    }
                }
            } catch (Exception e) {
                // Continue with other validations
            }
        }
        
        return null; // No validation errors
    }
    
    /**
     * 🎆 ENHANCED: Validate dependent parameters
     */
    private String validateDependentParameter(String slotName, String dependsOn, Map<String, DataValue> parameters) {
        // Parse dependency: "slotName=value" or "slotName!=value"
        String[] parts = dependsOn.split("(!?=)");
        if (parts.length >= 2) {
            String dependencySlotName = parts[0].trim();
            String expectedValue = parts[1].trim();
            boolean isNotEqual = dependsOn.contains("!=");
            
            // Check if the dependency parameter exists
            if (!parameters.containsKey(dependencySlotName)) {
                return "Parameter '" + slotName + "' depends on '" + dependencySlotName + "' which is not set";
            }
            
            // Get the actual value of the dependency parameter
            DataValue dependencyValue = parameters.get(dependencySlotName);
            if (dependencyValue == null) {
                return "Parameter '" + slotName + "' depends on '" + dependencySlotName + "' which has no value";
            }
            
            String actualValue = dependencyValue.asString();
            if (actualValue == null) {
                return "Parameter '" + slotName + "' depends on '" + dependencySlotName + "' which has no value";
            }
            
            // Check if dependency condition is met
            boolean conditionMet = false;
            if (isNotEqual) {
                conditionMet = !actualValue.equals(expectedValue);
            } else {
                conditionMet = actualValue.equals(expectedValue);
            }
            
            // If condition is not met, this parameter should not be set
            if (!conditionMet && parameters.containsKey(slotName)) {
                return "Parameter '" + slotName + "' is only available when '" + dependencySlotName + 
                    (isNotEqual ? " ≠ " : " = ") + expectedValue + "'";
            }
        }
        
        return null; // No dependency validation error
    }
    
    /**
     * 🎆 ENHANCED: Validate a single parameter value based on validation rule
     */
    private String validateParameterValue(String paramName, String value, String validationRule) {
        if (value == null || value.trim().isEmpty()) return null;
        
        // Remove color codes for validation
        String cleanedValue = value.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Handle different validation rules
        switch (validationRule) {
            case "number":
                try {
                    Double.parseDouble(cleanedValue);
                } catch (NumberFormatException e) {
                    return "Parameter '" + paramName + "' must be a number";
                }
                break;
                
            case "sound_name":
                if (!isValidSoundName(cleanedValue)) {
                    return "Parameter '" + paramName + "' must be a valid sound name";
                }
                break;
                
            case "effect_name":
                if (!isValidEffectName(cleanedValue)) {
                    return "Parameter '" + paramName + "' must be a valid effect name";
                }
                break;
                
            case "player_name":
                if (!isValidPlayerName(cleanedValue)) {
                    return "Parameter '" + paramName + "' must be a valid player name";
                }
                break;
                
            case "world_name":
                if (!isValidWorldName(cleanedValue)) {
                    return "Parameter '" + paramName + "' must be a valid world name";
                }
                break;
                
            case "material_name":
                if (!isValidMaterialName(cleanedValue)) {
                    return "Parameter '" + paramName + "' must be a valid material name";
                }
                break;
                
            case "color_hex":
                if (!isValidHexColor(cleanedValue)) {
                    return "Parameter '" + paramName + "' must be a valid hex color (#RRGGBB)";
                }
                break;
                
            case "email":
                if (!isValidEmail(cleanedValue)) {
                    return "Parameter '" + paramName + "' must be a valid email address";
                }
                break;
                
            case "url":
                if (!isValidUrl(cleanedValue)) {
                    return "Parameter '" + paramName + "' must be a valid URL";
                }
                break;
                
            default:
                // Handle range validations like "number_range:0.0-1.0"
                if (validationRule.startsWith("number_range:")) {
                    String range = validationRule.substring("number_range:".length());
                    String[] parts = range.split("-");
                    if (parts.length == 2) {
                        try {
                            double min = Double.parseDouble(parts[0]);
                            double max = Double.parseDouble(parts[1]);
                            double val = Double.parseDouble(cleanedValue);
                            if (val < min || val > max) {
                                return "Parameter '" + paramName + "' must be between " + min + " and " + max;
                            }
                        } catch (NumberFormatException e) {
                            return "Parameter '" + paramName + "' has invalid range format";
                        }
                    }
                }
                // Handle regex validations like "regex:[a-zA-Z]+"
                else if (validationRule.startsWith("regex:")) {
                    String regex = validationRule.substring("regex:".length());
                    if (!cleanedValue.matches(regex)) {
                        return "Parameter '" + paramName + "' does not match required format";
                    }
                }
                // Handle length validations like "length:5-20"
                else if (validationRule.startsWith("length:")) {
                    String lengthSpec = validationRule.substring("length:".length());
                    String[] parts = lengthSpec.split("-");
                    if (parts.length == 1) {
                        try {
                            int exactLength = Integer.parseInt(parts[0]);
                            if (cleanedValue.length() != exactLength) {
                                return "Parameter '" + paramName + "' must be exactly " + exactLength + " characters";
                            }
                        } catch (NumberFormatException e) {
                            return "Parameter '" + paramName + "' has invalid length specification";
                        }
                    } else if (parts.length == 2) {
                        try {
                            int minLength = Integer.parseInt(parts[0]);
                            int maxLength = Integer.parseInt(parts[1]);
                            int length = cleanedValue.length();
                            if (length < minLength || length > maxLength) {
                                return "Parameter '" + paramName + "' must be between " + minLength + " and " + maxLength + " characters";
                            }
                        } catch (NumberFormatException e) {
                            return "Parameter '" + paramName + "' has invalid length specification";
                        }
                    }
                }
                // Handle enum validations like "enum:option1,option2,option3"
                else if (validationRule.startsWith("enum:")) {
                    String enumValues = validationRule.substring("enum:".length());
                    String[] values = enumValues.split(",");
                    boolean found = false;
                    for (String val : values) {
                        if (cleanedValue.equalsIgnoreCase(val.trim())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return "Parameter '" + paramName + "' must be one of: " + enumValues;
                    }
                }
                break;
        }
        
        return null; // No validation error
    }
    
    /**
     * 🎆 ENHANCED: Check if string represents a valid player name
     */
    private boolean isValidPlayerName(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) return false;
        String cleaned = playerName.replaceAll("§[0-9a-fk-or]", "").trim();
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        return cleaned.matches("[a-zA-Z0-9_]{3,16}");
    }
    
    /**
     * 🎆 ENHANCED: Check if string represents a valid world name
     */
    private boolean isValidWorldName(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) return false;
        String cleaned = worldName.replaceAll("§[0-9a-fk-or]", "").trim();
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        return cleaned.matches("[a-zA-Z0-9_.\\-]+");
    }
    
    /**
     * 🎆 ENHANCED: Check if string represents a valid material name
     */
    private boolean isValidMaterialName(String materialName) {
        if (materialName == null || materialName.trim().isEmpty()) return false;
        String cleaned = materialName.replaceAll("§[0-9a-fk-or]", "").trim();
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        return org.bukkit.Material.matchMaterial(cleaned) != null;
    }
    
    /**
     * 🎆 ENHANCED: Check if string represents a valid hex color
     */
    private boolean isValidHexColor(String color) {
        if (color == null || color.trim().isEmpty()) return false;
        String cleaned = color.replaceAll("§[0-9a-fk-or]", "").trim();
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        return cleaned.matches("#[0-9a-fA-F]{6}");
    }
    
    /**
     * 🎆 ENHANCED: Check if string represents a valid email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String cleaned = email.replaceAll("§[0-9a-fk-or]", "").trim();
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        return cleaned.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }
    
    /**
     * 🎆 ENHANCED: Check if string represents a valid URL
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        String cleaned = url.replaceAll("§[0-9a-fk-or]", "").trim();
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        // Basic URL validation - fixed regex with proper escaping
        return cleaned.matches("https?://[\\w.-]+(?:\\.[\\w.-]+)+[/\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]*");
    }
    
    /**
     * 🎆 ENHANCED: Check if string represents a valid sound name
     */
    private boolean isValidSoundName(String soundName) {
        if (soundName == null || soundName.trim().isEmpty()) return false;
        String cleaned = soundName.replaceAll("§[0-9a-fk-or]", "").trim();
        return cleaned.contains(":") || 
               cleaned.startsWith("minecraft:") || 
               cleaned.contains("block.") || 
               cleaned.contains("entity.") || 
               cleaned.contains("item.") || 
               cleaned.contains("music.") || 
               cleaned.contains("ambient.");
    }
    
    /**
     * 🎆 ENHANCED: Check if string represents a valid effect name
     */
    private boolean isValidEffectName(String effectName) {
        if (effectName == null || effectName.trim().isEmpty()) return false;
        String cleaned = effectName.replaceAll("§[0-9a-fk-or]", "").trim();
        String[] validEffects = {
            "SPEED", "SLOW", "FAST_DIGGING", "SLOW_DIGGING", "INCREASE_DAMAGE", 
            "HEAL", "HARM", "JUMP", "CONFUSION", "REGENERATION", "DAMAGE_RESISTANCE",
            "FIRE_RESISTANCE", "WATER_BREATHING", "INVISIBILITY", "BLINDNESS",
            "NIGHT_VISION", "HUNGER", "WEAKNESS", "POISON", "WITHER", "HEALTH_BOOST",
            "ABSORPTION", "SATURATION", "GLOWING", "LEVITATION", "LUCK", "UNLUCK",
            "SLOW_FALLING", "CONDUIT_POWER", "DOLPHINS_GRACE", "BAD_OMEN", "HERO_OF_THE_VILLAGE"
        };
        for (String effect : validEffects) {
            if (effect.equalsIgnoreCase(cleaned)) {
                return true;
            }
        }
        return false;
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