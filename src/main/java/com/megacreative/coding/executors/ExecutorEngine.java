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
                case "giveItems":
                    result = executeGiveItems(processedParameters, context);
                    break;
                case "spawnEntity":
                    result = executeSpawnEntity(processedParameters, context);
                    break;
                case "removeItems":
                    result = executeRemoveItems(processedParameters, context);
                    break;
                case "setArmor":
                    result = executeSetArmor(processedParameters, context);
                    break;
                case "spawnMob":
                    result = executeSpawnMob(processedParameters, context);
                    break;
                case "healPlayer":
                    result = executeHealPlayer(processedParameters, context);
                    break;
                case "setGameMode":
                    result = executeSetGameMode(processedParameters, context);
                    break;
                case "setTime":
                    result = executeSetTime(processedParameters, context);
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
                case "broadcast":
                    result = executeBroadcast(processedParameters, context);
                    break;
                case "setVar":
                    result = executeSetVariable(processedParameters, context);
                    break;
                case "ifVarEquals":
                    result = executeIfVariable(block, processedParameters, context);
                    break;
                case "ifVarGreater":
                    result = executeIfVarGreater(block, processedParameters, context);
                    break;
                case "ifVarLess":
                    result = executeIfVarLess(block, processedParameters, context);
                    break;
                case "compareVariable":
                    result = executeCompareVariable(block, processedParameters, context);
                    break;
                case "sendActionBar":
                    result = executeSendActionBar(processedParameters, context);
                    break;
                case "executeAsyncCommand":
                    result = executeExecuteAsyncCommand(processedParameters, context);
                    break;
                case "createScoreboard":
                    result = executeCreateScoreboard(processedParameters, context);
                    break;
                case "setScore":
                    result = executeSetScore(processedParameters, context);
                    break;
                case "incrementScore":
                    result = executeIncrementScore(processedParameters, context);
                    break;
                case "createTeam":
                    result = executeCreateTeam(processedParameters, context);
                    break;
                case "addPlayerToTeam":
                    result = executeAddPlayerToTeam(processedParameters, context);
                    break;
                case "saveLocation":
                    result = executeSaveLocation(processedParameters, context);
                    break;
                case "getLocation":
                    result = executeGetLocation(processedParameters, context);
                    break;
                case "isNight":
                    result = executeIsNight(block, processedParameters, context);
                    break;
                case "isRiding":
                    result = executeIsRiding(block, processedParameters, context);
                    break;
                case "checkPlayerInventory":
                    result = executeCheckPlayerInventory(block, processedParameters, context);
                    break;
                case "setWeather":
                    result = executeSetWeather(processedParameters, context);
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
            return context.createResult(true, "Item given");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to give item: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeGiveItems(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue itemsValue = params.get("items");
            if (itemsValue == null) {
                return context.createErrorResult("No items specified");
            }
            
            List<DataValue> items = itemsValue.asList();
            if (items == null || items.isEmpty()) {
                return context.createErrorResult("Items list cannot be empty");
            }
            
            Player player = context.getPlayer();
            for (DataValue itemValue : items) {
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
                
                // Add to inventory or drop if full
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                    player.sendMessage("§eYour inventory is full, the item was dropped at your feet!");
                } else {
                    player.getInventory().addItem(item);
                }
            }
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Items given");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to give items: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSpawnEntity(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue entityValue = params.get("entity");
            if (entityValue == null) {
                return context.createErrorResult("No entity specified");
            }
            
            String entityName = entityValue.asString();
            if (entityName == null || entityName.trim().isEmpty()) {
                return context.createErrorResult("Entity name cannot be empty");
            }
            
            // Try to get the entity type
            org.bukkit.entity.EntityType entityType;
            try {
                entityType = org.bukkit.entity.EntityType.valueOf(entityName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid entity type: " + entityName);
            }
            
            // Spawn the entity at the player's location
            Player player = context.getPlayer();
            Location location = player.getLocation();
            player.getWorld().spawnEntity(location, entityType);
            
            // Visual effects
            player.spawnParticle(Particle.EXPLOSION_NORMAL, location, 10);
            player.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            
            return context.createResult(true, "Entity spawned");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to spawn entity: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeRemoveItems(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue itemsValue = params.get("items");
            if (itemsValue == null) {
                return context.createErrorResult("No items specified");
            }
            
            List<DataValue> items = itemsValue.asList();
            if (items == null || items.isEmpty()) {
                return context.createErrorResult("Items list cannot be empty");
            }
            
            Player player = context.getPlayer();
            for (DataValue itemValue : items) {
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
                
                // Remove the item from the player's inventory
                player.getInventory().remove(material);
            }
            
            // Visual effects
            player.spawnParticle(Particle.ITEM_CRACK, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Items removed");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to remove items: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSetArmor(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue helmetValue = params.get("helmet");
            DataValue chestplateValue = params.get("chestplate");
            DataValue leggingsValue = params.get("leggings");
            DataValue bootsValue = params.get("boots");
            
            Player player = context.getPlayer();
            org.bukkit.inventory.PlayerInventory inventory = player.getInventory();
            
            if (helmetValue != null) {
                String materialName = helmetValue.asString();
                if (materialName != null && !materialName.trim().isEmpty()) {
                    org.bukkit.Material material = org.bukkit.Material.valueOf(materialName.toUpperCase());
                    inventory.setHelmet(new org.bukkit.inventory.ItemStack(material));
                }
            }
            
            if (chestplateValue != null) {
                String materialName = chestplateValue.asString();
                if (materialName != null && !materialName.trim().isEmpty()) {
                    org.bukkit.Material material = org.bukkit.Material.valueOf(materialName.toUpperCase());
                    inventory.setChestplate(new org.bukkit.inventory.ItemStack(material));
                }
            }
            
            if (leggingsValue != null) {
                String materialName = leggingsValue.asString();
                if (materialName != null && !materialName.trim().isEmpty()) {
                    org.bukkit.Material material = org.bukkit.Material.valueOf(materialName.toUpperCase());
                    inventory.setLeggings(new org.bukkit.inventory.ItemStack(material));
                }
            }
            
            if (bootsValue != null) {
                String materialName = bootsValue.asString();
                if (materialName != null && !materialName.trim().isEmpty()) {
                    org.bukkit.Material material = org.bukkit.Material.valueOf(materialName.toUpperCase());
                    inventory.setBoots(new org.bukkit.inventory.ItemStack(material));
                }
            }
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Armor set");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set armor: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSpawnMob(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue mobValue = params.get("mob");
            if (mobValue == null) {
                return context.createErrorResult("No mob specified");
            }
            
            String mobName = mobValue.asString();
            if (mobName == null || mobName.trim().isEmpty()) {
                return context.createErrorResult("Mob name cannot be empty");
            }
            
            // Try to get the entity type
            org.bukkit.entity.EntityType entityType;
            try {
                entityType = org.bukkit.entity.EntityType.valueOf(mobName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid mob type: " + mobName);
            }
            
            // Spawn the mob at the player's location
            Player player = context.getPlayer();
            Location location = player.getLocation();
            player.getWorld().spawnEntity(location, entityType);
            
            // Visual effects
            player.spawnParticle(Particle.EXPLOSION_NORMAL, location, 10);
            player.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            
            return context.createResult(true, "Mob spawned");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to spawn mob: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeHealPlayer(Map<String, DataValue> params, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            player.setHealth(player.getMaxHealth());
            
            // Visual effects
            player.spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Player healed");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to heal player: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSetGameMode(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue gameModeValue = params.get("gameMode");
            if (gameModeValue == null) {
                return context.createErrorResult("No game mode specified");
            }
            
            String gameModeName = gameModeValue.asString();
            if (gameModeName == null || gameModeName.trim().isEmpty()) {
                return context.createErrorResult("Game mode name cannot be empty");
            }
            
            // Try to get the game mode
            org.bukkit.GameMode gameMode;
            try {
                gameMode = org.bukkit.GameMode.valueOf(gameModeName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid game mode: " + gameModeName);
            }
            
            Player player = context.getPlayer();
            player.setGameMode(gameMode);
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Game mode set");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set game mode: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSetTime(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue timeValue = params.get("time");
            if (timeValue == null) {
                return context.createErrorResult("No time specified");
            }
            
            long time = Long.parseLong(timeValue.asString());
                return context.createErrorResult("Time must be a number");
            }
            
            Player player = context.getPlayer();
            player.getWorld().setTime(time);
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Time set");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set time: " + e.getMessage(), e);
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
            
            // Try to get the sound
            org.bukkit.Sound sound;
            try {
                sound = org.bukkit.Sound.valueOf(soundName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid sound: " + soundName);
            }
            
            Player player = context.getPlayer();
            Location location = player.getLocation();
            player.playSound(location, sound, 1.0f, 1.0f);
            
            // Visual effects
            player.spawnParticle(Particle.NOTE, location, 10);
            return context.createResult(true, "Sound played");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to play sound: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeEffect(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue effectValue = params.get("effect");
            if (effectValue == null) {
                return context.createErrorResult("No effect specified");
            }
            
            String effectName = effectValue.asString();
            if (effectName == null || effectName.trim().isEmpty()) {
                return context.createErrorResult("Effect name cannot be empty");
            }
            
            // Try to get the particle effect
            org.bukkit.Particle effect;
            try {
                effect = org.bukkit.Particle.valueOf(effectName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid effect: " + effectName);
            }
            
            Player player = context.getPlayer();
            Location location = player.getLocation();
            player.spawnParticle(effect, location, 10);
            
            return context.createResult(true, "Effect played");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to play effect: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeCommand(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue commandValue = params.get("command");
            if (commandValue == null) {
                return context.createErrorResult("No command specified");
            }
            
            String command = commandValue.asString();
            if (command == null || command.trim().isEmpty()) {
                return context.createErrorResult("Command cannot be empty");
            }
            
            Player player = context.getPlayer();
            player.performCommand(command);
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Command executed");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to execute command: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeBroadcast(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue messageValue = params.get("message");
            if (messageValue == null) {
                return context.createErrorResult("No message specified");
            }
            
            String message = messageValue.asString();
            if (message == null || message.trim().isEmpty()) {
                return context.createErrorResult("Message cannot be empty");
            }
            
            // Resolve placeholders and broadcast message
            message = resolvePlaceholders(message, context);
            plugin.getServer().broadcastMessage(message);
            
            return context.createResult(true, "Message broadcasted");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to broadcast message: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSetVariable(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue nameValue = params.get("name");
            DataValue valueValue = params.get("value");
            if (nameValue == null || valueValue == null) {
                return context.createErrorResult("Name and value must be specified");
            }
            
            String name = nameValue.asString();
            if (name == null || name.trim().isEmpty()) {
                return context.createErrorResult("Variable name cannot be empty");
            }
            
            variableManager.setVariable(name, valueValue);
            
            // Visual effects
            Player player = context.getPlayer();
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Variable set");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set variable: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIfVariable(com.megacreative.coding.CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue nameValue = params.get("name");
            DataValue valueValue = params.get("value");
            if (nameValue == null || valueValue == null) {
                return context.createErrorResult("Name and value must be specified");
            }
            
            String name = nameValue.asString();
            if (name == null || name.trim().isEmpty()) {
                return context.createErrorResult("Variable name cannot be empty");
            }
            
            DataValue variableValue = variableManager.getVariable(name);
            if (variableValue == null) {
                return context.createErrorResult("Variable not found: " + name);
            }
            
            if (variableValue.equals(valueValue)) {
                var nextBlock = block.getNextBlock();
                if (nextBlock != null) {
                    return executeBlock(nextBlock, context);
                }
            }
            
            return context.createResult(true, "Condition not met");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to check variable: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIfVarGreater(com.megacreative.coding.CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue nameValue = params.get("name");
            DataValue valueValue = params.get("value");
            if (nameValue == null || valueValue == null) {
                return context.createErrorResult("Name and value must be specified");
            }
            
            String name = nameValue.asString();
            if (name == null || name.trim().isEmpty()) {
                return context.createErrorResult("Variable name cannot be empty");
            }
            
            DataValue variableValue = variableManager.getVariable(name);
            if (variableValue == null) {
                return context.createErrorResult("Variable not found: " + name);
            }
            
            if (variableValue.asNumber() != null && valueValue.asNumber() != null) {
                double variableNumber = variableValue.asNumber().doubleValue();
                double valueNumber = valueValue.asNumber().doubleValue();
                
                if (variableNumber > valueNumber) {
                    var nextBlock = block.getNextBlock();
                    if (nextBlock != null) {
                        return executeBlock(nextBlock, context);
                    }
                }
            } else {
                return context.createErrorResult("Variable and value must be numbers");
            }
            
            return context.createResult(true, "Condition not met");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to check variable: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIfVarLess(com.megacreative.coding.CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue nameValue = params.get("name");
            DataValue valueValue = params.get("value");
            if (nameValue == null || valueValue == null) {
                return context.createErrorResult("Name and value must be specified");
            }
            
            String name = nameValue.asString();
            if (name == null || name.trim().isEmpty()) {
                return context.createErrorResult("Variable name cannot be empty");
            }
            
            DataValue variableValue = variableManager.getVariable(name);
            if (variableValue == null) {
                return context.createErrorResult("Variable not found: " + name);
            }
            
            if (variableValue.isNumber() && valueValue.isNumber()) {
                double variableNumber = variableValue.asDouble();
                double valueNumber = valueValue.asDouble();
                
                if (variableNumber < valueNumber) {
                    var nextBlock = block.getNextBlock();
                    if (nextBlock != null) {
                        return executeBlock(nextBlock, context);
                    }
                }
            } else {
                return context.createErrorResult("Variable and value must be numbers");
            }
            
            return context.createResult(true, "Condition not met");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to check variable: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeCompareVariable(com.megacreative.coding.CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue nameValue = params.get("name");
            DataValue valueValue = params.get("value");
            if (nameValue == null || valueValue == null) {
                return context.createErrorResult("Name and value must be specified");
            }
            
            String name = nameValue.asString();
            if (name == null || name.trim().isEmpty()) {
                return context.createErrorResult("Variable name cannot be empty");
            }
            
            DataValue variableValue = variableManager.getVariable(name);
            if (variableValue == null) {
                return context.createErrorResult("Variable not found: " + name);
            }
            
            if (variableValue.equals(valueValue)) {
                var nextBlock = block.getNextBlock();
                if (nextBlock != null) {
                    return executeBlock(nextBlock, context);
                }
            }
            
            return context.createResult(true, "Condition not met");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to compare variable: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSendActionBar(Map<String, DataValue> params, ExecutionContext context) {
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
            Player player = context.getPlayer();
            player.sendActionBar(message);
            
            return context.createResult(true, "Action bar sent");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to send action bar: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeExecuteAsyncCommand(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue commandValue = params.get("command");
            if (commandValue == null) {
                return context.createErrorResult("No command specified");
            }
            
            String command = commandValue.asString();
            if (command == null || command.trim().isEmpty()) {
                return context.createErrorResult("Command cannot be empty");
            }
            
            Player player = context.getPlayer();
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> player.performCommand(command));
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Command executed asynchronously");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to execute command asynchronously: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeCreateScoreboard(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue nameValue = params.get("name");
            if (nameValue == null) {
                return context.createErrorResult("No name specified");
            }
            
            String name = nameValue.asString();
            if (name == null || name.trim().isEmpty()) {
                return context.createErrorResult("Name cannot be empty");
            }
            
            Player player = context.getPlayer();
            org.bukkit.scoreboard.Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
            org.bukkit.scoreboard.Objective objective = scoreboard.registerNewObjective("main", "dummy", name);
            objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
            player.setScoreboard(scoreboard);
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Scoreboard created");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to create scoreboard: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSetScore(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue nameValue = params.get("name");
            DataValue scoreValue = params.get("score");
            if (nameValue == null || scoreValue == null) {
                return context.createErrorResult("Name and score must be specified");
            }
            
            String name = nameValue.asString();
            if (name == null || name.trim().isEmpty()) {
                return context.createErrorResult("Name cannot be empty");
            }
            
            Integer score = scoreValue.asInt();
            if (score == null) {
                return context.createErrorResult("Score must be a number");
            }
            
            Player player = context.getPlayer();
            org.bukkit.scoreboard.Scoreboard scoreboard = player.getScoreboard();
            org.bukkit.scoreboard.Objective objective = scoreboard.getObjective("main");
            if (objective == null) {
                return context.createErrorResult("Scoreboard not found");
            }
            
            objective.getScore(name).setScore(score);
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Score set");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set score: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIncrementScore(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue nameValue = params.get("name");
            DataValue incrementValue = params.get("increment");
            if (nameValue == null || incrementValue == null) {
                return context.createErrorResult("Name and increment must be specified");
            }
            
            String name = nameValue.asString();
            if (name == null || name.trim().isEmpty()) {
                return context.createErrorResult("Name cannot be empty");
            }
            
            Integer increment = incrementValue.asInt();
            if (increment == null) {
                return context.createErrorResult("Increment must be a number");
            }
            
            Player player = context.getPlayer();
            org.bukkit.scoreboard.Scoreboard scoreboard = player.getScoreboard();
            org.bukkit.scoreboard.Objective objective = scoreboard.getObjective("main");
            if (objective == null) {
                return context.createErrorResult("Scoreboard not found");
            }
            
            org.bukkit.scoreboard.Score score = objective.getScore(name);
            score.setScore(score.getScore() + increment);
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Score incremented");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to increment score: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeCreateTeam(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue nameValue = params.get("name");
            if (nameValue == null) {
                return context.createErrorResult("No name specified");
            }
            
            String name = nameValue.asString();
            if (name == null || name.trim().isEmpty()) {
                return context.createErrorResult("Name cannot be empty");
            }
            
            Player player = context.getPlayer();
            org.bukkit.scoreboard.Scoreboard scoreboard = player.getScoreboard();
            org.bukkit.scoreboard.Team team = scoreboard.registerNewTeam(name);
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Team created");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to create team: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeAddPlayerToTeam(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue teamNameValue = params.get("teamName");
            DataValue playerNameValue = params.get("playerName");
            if (teamNameValue == null || playerNameValue == null) {
                return context.createErrorResult("Team name and player name must be specified");
            }
            
            String teamName = teamNameValue.asString();
            if (teamName == null || teamName.trim().isEmpty()) {
                return context.createErrorResult("Team name cannot be empty");
            }
            
            String playerName = playerNameValue.asString();
            if (playerName == null || playerName.trim().isEmpty()) {
                return context.createErrorResult("Player name cannot be empty");
            }
            
            Player player = context.getPlayer();
            org.bukkit.scoreboard.Scoreboard scoreboard = player.getScoreboard();
            org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                return context.createErrorResult("Team not found: " + teamName);
            }
            
            Player targetPlayer = plugin.getServer().getPlayer(playerName);
            if (targetPlayer == null) {
                return context.createErrorResult("Player not found: " + playerName);
            }
            
            team.addEntry(targetPlayer.getName());
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Player added to team");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to add player to team: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSaveLocation(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue nameValue = params.get("name");
            if (nameValue == null) {
                return context.createErrorResult("No name specified");
            }
            
            String name = nameValue.asString();
            if (name == null || name.trim().isEmpty()) {
                return context.createErrorResult("Name cannot be empty");
            }
            
            Player player = context.getPlayer();
            Location location = player.getLocation();
            variableManager.setVariable(name, DataValue.of(location));
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Location saved");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to save location: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeGetLocation(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue nameValue = params.get("name");
            if (nameValue == null) {
                return context.createErrorResult("No name specified");
            }
            
            String name = nameValue.asString();
            if (name == null || name.trim().isEmpty()) {
                return context.createErrorResult("Name cannot be empty");
            }
            
            DataValue locationValue = variableManager.getVariable(name);
            if (locationValue == null) {
                return context.createErrorResult("Location not found: " + name);
            }
            
            Location location = locationValue.asLocation();
            if (location == null) {
                return context.createErrorResult("Invalid location value");
            }
            
            Player player = context.getPlayer();
            player.teleport(location);
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Location retrieved");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to get location: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIsNight(com.megacreative.coding.CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            if (player.getWorld().getTime() >= 12000) {
                var nextBlock = block.getNextBlock();
                if (nextBlock != null) {
                    return executeBlock(nextBlock, context);
                }
            }
            
            return context.createResult(true, "Condition not met");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to check if it's night: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIsRiding(com.megacreative.coding.CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            if (player.isInsideVehicle()) {
                var nextBlock = block.getNextBlock();
                if (nextBlock != null) {
                    return executeBlock(nextBlock, context);
                }
            }
            
            return context.createResult(true, "Condition not met");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to check if player is riding: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeCheckPlayerInventory(com.megacreative.coding.CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
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
            
            Player player = context.getPlayer();
            org.bukkit.inventory.PlayerInventory inventory = player.getInventory();
            if (inventory.contains(material)) {
                var nextBlock = block.getNextBlock();
                if (nextBlock != null) {
                    return executeBlock(nextBlock, context);
                }
            }
            
            return context.createResult(true, "Condition not met");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to check player inventory: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the setWeather action to set the weather in the world
     */
    private ExecutionResult executeSetWeather(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the weather parameter (required)
            DataValue weatherValue = params.get("weather");
            if (weatherValue == null) {
                return context.createErrorResult("No weather specified for setWeather action");
            }
            
            String weatherStr = weatherValue.asString();
            if (weatherStr == null || weatherStr.trim().isEmpty()) {
                return context.createErrorResult("Weather value cannot be empty");
            }
            
            // Validate and parse the weather value
            org.bukkit.WeatherType weatherType;
            switch (weatherStr.toLowerCase()) {
                case "clear":
                    weatherType = org.bukkit.WeatherType.CLEAR;
                    break;
                case "rain":
                    weatherType = org.bukkit.WeatherType.DOWNFALL;
                    break;
                case "thunder":
                    weatherType = org.bukkit.WeatherType.DOWNFALL;
                    break;
                default:
                    return context.createErrorResult("Invalid weather type: " + weatherStr + ". Must be one of: clear, rain, thunder.");
            }
            
            // Get the player's world
            Player player = context.getPlayer();
            org.bukkit.World world = player.getWorld();
            
            // Set the weather based on type
            switch (weatherStr.toLowerCase()) {
                case "clear":
                    world.setStorm(false);
                    world.setThundering(false);
                    break;
                case "rain":
                    world.setStorm(true);
                    world.setThundering(false);
                    break;
                case "thunder":
                    world.setStorm(true);
                    world.setThundering(true);
                    break;
            }
            
            // Visual effect
            player.spawnParticle(Particle.WATER_SPLASH, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.WEATHER_RAIN, 1.0f, 1.0f);
            
            // Create success message
            return context.createResult(true, "Set world weather to " + weatherStr);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set weather: " + e.getMessage(), e);
        }
    }
    
    // === HELPER METHODS ===
    
    private void startVisualUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : visualIndicators.entrySet()) {
                    Location location = entry.getKey();
                    VisualIndicator indicator = entry.getValue();
                    
                    if (indicator.isExpired()) {
                        visualIndicators.remove(location);
                    } else {
                        indicator.update();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }
    
    private void showExecutionStart(ExecutionContext context) {
        Player player = context.getPlayer();
        Location location = player.getLocation();
        visualIndicators.put(location, new VisualIndicator(location, VisualIndicator.Type.START));
        player.spawnParticle(Particle.VILLAGER_HAPPY, location, 10);
        player.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }
    
    private void showExecutionComplete(ExecutionContext context, ExecutionResult result) {
        Player player = context.getPlayer();
        Location location = player.getLocation();
        visualIndicators.put(location, new VisualIndicator(location, VisualIndicator.Type.COMPLETE));
        player.spawnParticle(Particle.VILLAGER_HAPPY, location, 10);
        player.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }
    
    private void showExecutionError(ExecutionContext context, Exception e) {
        Player player = context.getPlayer();
        Location location = player.getLocation();
        visualIndicators.put(location, new VisualIndicator(location, VisualIndicator.Type.ERROR));
        player.spawnParticle(Particle.VILLAGER_ANGRY, location, 10);
        player.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }
    
    private void showBlockExecution(com.megacreative.coding.CodeBlock block, ExecutionContext context) {
        Location location = block.getLocation();
        visualIndicators.put(location, new VisualIndicator(location, VisualIndicator.Type.EXECUTING));
        location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 10);
    }
    
    private void showBlockComplete(com.megacreative.coding.CodeBlock block, ExecutionContext context) {
        Location location = block.getLocation();
        visualIndicators.put(location, new VisualIndicator(location, VisualIndicator.Type.COMPLETE));
        location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 10);
    }
    
    private void showBlockError(com.megacreative.coding.CodeBlock block, ExecutionContext context, String message) {
        Location location = block.getLocation();
        visualIndicators.put(location, new VisualIndicator(location, VisualIndicator.Type.ERROR));
        location.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, location, 10);
    }
    
    private Map<String, DataValue> preprocessParameters(com.megacreative.coding.CodeBlock block, ExecutionContext context) {
        Map<String, DataValue> parameters = block.getParameters() != null ? block.getParameters() : new HashMap<>();
        Map<String, DataValue> processedParameters = new HashMap<>();
        
        for (var entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            DataValue paramValue = entry.getValue();
            
            // Resolve placeholders in parameter value
            String resolvedValue = resolvePlaceholders(paramValue.asString(), context);
            processedParameters.put(paramName, DataValue.of(resolvedValue));
        }
        
        return processedParameters;
    }
    
    private String resolvePlaceholders(String value, ExecutionContext context) {
        ReferenceSystemPlaceholderResolver resolver = new ReferenceSystemPlaceholderResolver();
        resolver.setVariableManager(variableManager);
        resolver.setPlayer(context.getPlayer());
        return resolver.resolve(value);
    }
    
    private String validateBlockParameters(com.megacreative.coding.CodeBlock block, Map<String, DataValue> processedParameters) {
        String action = block.getAction();
        var config = blockConfigService.getBlockConfig(action);
        if (config == null) {
            return "No configuration found for action: " + action;
        }
        
        for (var paramConfig : config.getParameters()) {
            String paramName = paramConfig.getName();
            DataValue paramValue = processedParameters.get(paramName);
            
            if (paramConfig.isRequired() && paramValue == null) {
                return "Missing required parameter: " + paramName;
            }
            
            if (paramValue != null && !paramConfig.getType().equals(paramValue.getType())) {
                return "Invalid type for parameter '" + paramName + "'. Expected: " + paramConfig.getType() + ", but got: " + paramValue.getType();
            }
        }
        
        return null; // No validation errors
    }
    
    private ExecutionResult executeGiveItems(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // For giveItems, we need to get items from the item groups
            // This would typically come from the configuration items in the block
            // For now, we'll implement a basic version that gives multiple items
            
            Player player = context.getPlayer();
            int itemsGiven = 0;
            
            // In a real implementation, we would get the items from the block's config items
            // For now, we'll just give some example items
            org.bukkit.inventory.ItemStack[] items = {
                new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND, 1),
                new org.bukkit.inventory.ItemStack(org.bukkit.Material.GOLD_INGOT, 3),
                new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_INGOT, 5)
            };
            
            // Add items to inventory or drop if full
            java.util.Map<Integer, org.bukkit.inventory.ItemStack> notAdded = player.getInventory().addItem(items);
            
            // Drop any items that couldn't be added
            for (org.bukkit.inventory.ItemStack item : notAdded.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            
            itemsGiven = items.length - notAdded.size();
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 10);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
            
            return context.createResult(true, "Given " + itemsGiven + " items");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to give items: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSpawnEntity(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue entityValue = params.get("entity_slot");
            if (entityValue == null) {
                return context.createErrorResult("No entity specified");
            }
            
            String entityName = entityValue.asString();
            if (entityName == null || entityName.trim().isEmpty()) {
                return context.createErrorResult("Entity name cannot be empty");
            }
            
            // Get count and radius with defaults
            int count = 1; // Default 1 entity
            int radius = 3; // Default 3 blocks radius
            
            try {
                if (params.containsKey("count_slot")) {
                    String countStr = params.get("count_slot").asString();
                    if (countStr != null && countStr.startsWith("количество:")) {
                        count = Integer.parseInt(countStr.substring("количество:".length()));
                    }
                }
                if (params.containsKey("radius_slot")) {
                    String radiusStr = params.get("radius_slot").asString();
                    if (radiusStr != null && radiusStr.startsWith("радиус:")) {
                        radius = Integer.parseInt(radiusStr.substring("радиус:".length()));
                    }
                }
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid number format for count or radius");
            }
            
            // Try to get the entity type
            org.bukkit.entity.EntityType entityType;
            try {
                entityType = org.bukkit.entity.EntityType.valueOf(entityName.toUpperCase());
                if (entityType == null || !entityType.isSpawnable()) {
                    return context.createErrorResult("Invalid or unspawnable entity: " + entityName);
                }
            } catch (Exception e) {
                return context.createErrorResult("Invalid entity: " + entityName);
            }
            
            // Spawn the entities
            Player player = context.getPlayer();
            Location spawnLocation = player.getLocation().add(
                (Math.random() - 0.5) * radius * 2,
                1,
                (Math.random() - 0.5) * radius * 2
            );
            
            int spawned = 0;
            for (int i = 0; i < count; i++) {
                Location loc = spawnLocation.clone().add(
                    (Math.random() - 0.5) * 2,
                    0,
                    (Math.random() - 0.5) * 2
                );
                if (player.getWorld().spawnEntity(loc, entityType) != null) {
                    spawned++;
                }
            }
            
            // Visual effect
            player.spawnParticle(Particle.EXPLOSION_NORMAL, spawnLocation, 5);
            
            return context.createResult(true, "Spawned " + spawned + " " + entityType.name().toLowerCase().replace("_", " "));
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to spawn entity: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeRemoveItems(Map<String, DataValue> params, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            
            // In a real implementation, we would get the items from the block's config items
            // For now, we'll implement a basic version that removes some example items
            
            // Example items to remove
            org.bukkit.inventory.ItemStack[] exampleItems = {
                new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIRT, 1),
                new org.bukkit.inventory.ItemStack(org.bukkit.Material.COBBLESTONE, 5),
                new org.bukkit.inventory.ItemStack(org.bukkit.Material.STICK, 3)
            };
            
            // Remove items from player's inventory
            for (org.bukkit.inventory.ItemStack item : exampleItems) {
                if (item != null) {
                    java.util.HashMap<Integer, ? extends org.bukkit.inventory.ItemStack> foundItems = 
                        player.getInventory().all(item.getType());
                    
                    int amountToRemove = item.getAmount();
                    for (java.util.Map.Entry<Integer, ? extends org.bukkit.inventory.ItemStack> entry : foundItems.entrySet()) {
                        if (amountToRemove <= 0) break;
                        
                        org.bukkit.inventory.ItemStack stack = entry.getValue();
                        int stackAmount = stack.getAmount();
                        
                        if (stackAmount <= amountToRemove) {
                            player.getInventory().clear(entry.getKey());
                            amountToRemove -= stackAmount;
                        } else {
                            stack.setAmount(stackAmount - amountToRemove);
                            amountToRemove = 0;
                        }
                    }
                }
            }
            
            // Visual effect
            player.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Removed items from inventory");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to remove items: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSetArmor(Map<String, DataValue> params, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            org.bukkit.inventory.PlayerInventory inventory = player.getInventory();
            
            // Set armor pieces based on parameters
            if (params.containsKey("helmet_slot")) {
                String helmetName = params.get("helmet_slot").asString();
                if (helmetName != null && !helmetName.trim().isEmpty()) {
                    org.bukkit.Material material = org.bukkit.Material.getMaterial(helmetName.toUpperCase());
                    if (material != null && material.name().endsWith("_HELMET")) {
                        inventory.setHelmet(new org.bukkit.inventory.ItemStack(material));
                    }
                }
            }
            
            if (params.containsKey("chestplate_slot")) {
                String chestplateName = params.get("chestplate_slot").asString();
                if (chestplateName != null && !chestplateName.trim().isEmpty()) {
                    org.bukkit.Material material = org.bukkit.Material.getMaterial(chestplateName.toUpperCase());
                    if (material != null && material.name().endsWith("_CHESTPLATE")) {
                        inventory.setChestplate(new org.bukkit.inventory.ItemStack(material));
                    }
                }
            }
            
            if (params.containsKey("leggings_slot")) {
                String leggingsName = params.get("leggings_slot").asString();
                if (leggingsName != null && !leggingsName.trim().isEmpty()) {
                    org.bukkit.Material material = org.bukkit.Material.getMaterial(leggingsName.toUpperCase());
                    if (material != null && material.name().endsWith("_LEGGINGS")) {
                        inventory.setLeggings(new org.bukkit.inventory.ItemStack(material));
                    }
                }
            }
            
            if (params.containsKey("boots_slot")) {
                String bootsName = params.get("boots_slot").asString();
                if (bootsName != null && !bootsName.trim().isEmpty()) {
                    org.bukkit.Material material = org.bukkit.Material.getMaterial(bootsName.toUpperCase());
                    if (material != null && material.name().endsWith("_BOOTS")) {
                        inventory.setBoots(new org.bukkit.inventory.ItemStack(material));
                    }
                }
            }
            
            // Visual effect
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Set armor pieces");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set armor: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSpawnMob(Map<String, DataValue> params, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            
            // For spawnMob, we'll spawn a zombie by default
            org.bukkit.entity.EntityType entityType = org.bukkit.entity.EntityType.ZOMBIE;
            
            // Try to get entity type from parameters
            if (params.containsKey("mob_type")) {
                String mobTypeName = params.get("mob_type").asString();
                if (mobTypeName != null && !mobTypeName.trim().isEmpty()) {
                    try {
                        entityType = org.bukkit.entity.EntityType.valueOf(mobTypeName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // Keep default zombie
                    }
                }
            }
            
            // Spawn the mob near the player
            Location spawnLocation = player.getLocation().add(
                (Math.random() - 0.5) * 5,
                1,
                (Math.random() - 0.5) * 5
            );
            
            org.bukkit.entity.Entity spawnedEntity = player.getWorld().spawnEntity(spawnLocation, entityType);
            
            // Visual effect
            player.spawnParticle(Particle.EXPLOSION_NORMAL, spawnLocation, 5);
            
            return context.createResult(true, "Spawned " + entityType.name().toLowerCase().replace("_", " "));
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to spawn mob: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeHealPlayer(Map<String, DataValue> params, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            
            // Get heal amount from parameters, default to full heal
            double healAmount = player.getMaxHealth() - player.getHealth();
            
            if (params.containsKey("heal_amount")) {
                String healAmountStr = params.get("heal_amount").asString();
                if (healAmountStr != null && !healAmountStr.trim().isEmpty()) {
                    try {
                        healAmount = Double.parseDouble(healAmountStr);
                    } catch (NumberFormatException e) {
                        // Keep default heal amount
                    }
                }
            }
            
            // Heal the player
            double newHealth = Math.min(player.getHealth() + healAmount, player.getMaxHealth());
            player.setHealth(newHealth);
            
            // Visual effect
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Healed player by " + healAmount + " health points");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to heal player: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSetGameMode(Map<String, DataValue> params, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            
            // Get game mode from parameters, default to SURVIVAL
            org.bukkit.GameMode gameMode = org.bukkit.GameMode.SURVIVAL;
            
            if (params.containsKey("game_mode")) {
                String gameModeStr = params.get("game_mode").asString();
                if (gameModeStr != null && !gameModeStr.trim().isEmpty()) {
                    try {
                        gameMode = org.bukkit.GameMode.valueOf(gameModeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        // Keep default game mode
                    }
                }
            }
            
            // Set the game mode
            player.setGameMode(gameMode);
            
            // Visual effect
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Set game mode to " + gameMode.name().toLowerCase());
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set game mode: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the setTime action to set the time of day in the world
     */
    private ExecutionResult executeSetTime(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the time parameter (required)
            DataValue timeValue = params.get("time");
            if (timeValue == null) {
                return context.createErrorResult("No time specified for setTime action");
            }
            
            String timeStr = timeValue.asString();
            if (timeStr == null || timeStr.trim().isEmpty()) {
                return context.createErrorResult("Time value cannot be empty");
            }
            
            // Parse the time value
            long time;
            try {
                time = Long.parseLong(timeStr);
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid time format: " + timeStr + ". Must be a number between 0 and 24000.");
            }
            
            // Validate time range
            if (time < 0 || time > 24000) {
                return context.createErrorResult("Time must be between 0 and 24000. Provided: " + time);
            }
            
            // Get the mode parameter (optional, defaults to "absolute")
            String mode = "absolute";
            if (params.containsKey("mode")) {
                DataValue modeValue = params.get("mode");
                if (modeValue != null) {
                    String modeStr = modeValue.asString();
                    if (modeStr != null && !modeStr.trim().isEmpty()) {
                        mode = modeStr.toLowerCase();
                    }
                }
            }
            
            // Get the player's world
            Player player = context.getPlayer();
            org.bukkit.World world = player.getWorld();
            
            // Set the time based on mode
            if ("relative".equals(mode)) {
                // Add the time to the current time
                long currentTime = world.getTime();
                world.setTime(currentTime + time);
            } else {
                // Set absolute time (default behavior)
                world.setTime(time);
            }
            
            // Visual effect
            player.spawnParticle(Particle.CLOUD, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
            
            // Create success message
            String modeText = "relative".equals(mode) ? "relative to current time" : "absolute";
            return context.createResult(true, "Set world time to " + time + " (" + modeText + ")");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set time: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the setWeather action to set the weather in the world
     */
    private ExecutionResult executeSetWeather(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the weather parameter (required)
            DataValue weatherValue = params.get("weather");
            if (weatherValue == null) {
                return context.createErrorResult("No weather specified for setWeather action");
            }
            
            String weatherStr = weatherValue.asString();
            if (weatherStr == null || weatherStr.trim().isEmpty()) {
                return context.createErrorResult("Weather value cannot be empty");
            }
            
            // Validate and parse the weather value
            org.bukkit.WeatherType weatherType;
            switch (weatherStr.toLowerCase()) {
                case "clear":
                    weatherType = org.bukkit.WeatherType.CLEAR;
                    break;
                case "rain":
                    weatherType = org.bukkit.WeatherType.DOWNFALL;
                    break;
                case "thunder":
                    weatherType = org.bukkit.WeatherType.DOWNFALL;
                    break;
                default:
                    return context.createErrorResult("Invalid weather type: " + weatherStr + ". Must be one of: clear, rain, thunder.");
            }
            
            // Get the player's world
            Player player = context.getPlayer();
            org.bukkit.World world = player.getWorld();
            
            // Set the weather based on type
            switch (weatherStr.toLowerCase()) {
                case "clear":
                    world.setStorm(false);
                    world.setThundering(false);
                    break;
                case "rain":
                    world.setStorm(true);
                    world.setThundering(false);
                    break;
                case "thunder":
                    world.setStorm(true);
                    world.setThundering(true);
                    break;
            }
            
            // Visual effect
            player.spawnParticle(Particle.WATER_SPLASH, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.WEATHER_RAIN, 1.0f, 1.0f);
            
            // Create success message
            return context.createResult(true, "Set world weather to " + weatherStr);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set weather: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the setWeather action to set the weather in the world
     */
    private ExecutionResult executeSetWeather(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the weather parameter (required)
            DataValue weatherValue = params.get("weather");
            if (weatherValue == null) {
                return context.createErrorResult("No weather specified for setWeather action");
            }
            
            String weatherStr = weatherValue.asString();
            if (weatherStr == null || weatherStr.trim().isEmpty()) {
                return context.createErrorResult("Weather value cannot be empty");
            }
            
            // Validate and parse the weather value
            org.bukkit.WeatherType weatherType;
            switch (weatherStr.toLowerCase()) {
                case "clear":
                    weatherType = org.bukkit.WeatherType.CLEAR;
                    break;
                case "rain":
                    weatherType = org.bukkit.WeatherType.DOWNFALL;
                    break;
                case "thunder":
                    weatherType = org.bukkit.WeatherType.DOWNFALL;
                    break;
                default:
                    return context.createErrorResult("Invalid weather type: " + weatherStr + ". Must be one of: clear, rain, thunder.");
            }
            
            // Get the player's world
            Player player = context.getPlayer();
            org.bukkit.World world = player.getWorld();
            
            // Set the weather based on type
            switch (weatherStr.toLowerCase()) {
                case "clear":
                    world.setStorm(false);
                    world.setThundering(false);
                    break;
                case "rain":
                    world.setStorm(true);
                    world.setThundering(false);
                    break;
                case "thunder":
                    world.setStorm(true);
                    world.setThundering(true);
                    break;
            }
            
            // Visual effect
            player.spawnParticle(Particle.WATER_SPLASH, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.WEATHER_RAIN, 1.0f, 1.0f);
            
            // Create success message
            return context.createResult(true, "Set world weather to " + weatherStr);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set weather: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the explosion action to create an explosion at the player's location
     */
    private ExecutionResult executeExplosion(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the power parameter (required)
            DataValue powerValue = params.get("power");
            if (powerValue == null) {
                return context.createErrorResult("No power specified for explosion action");
            }
            
            String powerStr = powerValue.asString();
            if (powerStr == null || powerStr.trim().isEmpty()) {
                return context.createErrorResult("Power value cannot be empty");
            }
            
            // Parse the power value
            float power;
            try {
                power = Float.parseFloat(powerStr);
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid power format: " + powerStr + ". Must be a number between 0.0 and 10.0.");
            }
            
            // Validate power range
            if (power < 0.0f || power > 10.0f) {
                return context.createErrorResult("Power must be between 0.0 and 10.0. Provided: " + power);
            }
            
            // Get the break_blocks parameter (optional, defaults to true)
            boolean breakBlocks = true;
            if (params.containsKey("break_blocks")) {
                DataValue breakBlocksValue = params.get("break_blocks");
                if (breakBlocksValue != null) {
                    String breakBlocksStr = breakBlocksValue.asString();
                    if (breakBlocksStr != null && !breakBlocksStr.trim().isEmpty()) {
                        breakBlocks = "true".equalsIgnoreCase(breakBlocksStr);
                    }
                }
            }
            
            // Get the create_fire parameter (optional, defaults to false)
            boolean createFire = false;
            if (params.containsKey("create_fire")) {
                DataValue createFireValue = params.get("create_fire");
                if (createFireValue != null) {
                    String createFireStr = createFireValue.asString();
                    if (createFireStr != null && !createFireStr.trim().isEmpty()) {
                        createFire = "true".equalsIgnoreCase(createFireStr);
                    }
                }
            }
            
            // Get the player's location
            Player player = context.getPlayer();
            Location location = player.getLocation();
            
            // Create the explosion
            boolean success = location.getWorld().createExplosion(location, power, breakBlocks, createFire, player);
            
            // Visual effect
            player.spawnParticle(Particle.EXPLOSION_LARGE, location, 3);
            player.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            
            // Create success message
            return context.createResult(true, "Created explosion with power " + power + 
                (breakBlocks ? " (breaking blocks)" : " (no block damage)") + 
                (createFire ? " (creating fire)" : ""));
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to create explosion: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the setBlock action to set a block at a relative position from the player
     */
    private ExecutionResult executeSetBlock(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the block type parameter (required)
            DataValue blockTypeValue = params.get("block_type");
            if (blockTypeValue == null) {
                return context.createErrorResult("No block type specified for setBlock action");
            }
            
            String blockTypeName = blockTypeValue.asString();
            if (blockTypeName == null || blockTypeName.trim().isEmpty()) {
                return context.createErrorResult("Block type cannot be empty");
            }
            
            // Try to get the material
            org.bukkit.Material material;
            try {
                material = org.bukkit.Material.valueOf(blockTypeName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid block type: " + blockTypeName);
            }
            
            // Get relative coordinates with defaults
            int relativeX = 0;
            int relativeY = 0;
            int relativeZ = 0;
            
            try {
                if (params.containsKey("relative_x")) {
                    relativeX = Integer.parseInt(params.get("relative_x").asString());
                }
                if (params.containsKey("relative_y")) {
                    relativeY = Integer.parseInt(params.get("relative_y").asString());
                }
                if (params.containsKey("relative_z")) {
                    relativeZ = Integer.parseInt(params.get("relative_z").asString());
                }
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid number format for relative coordinates");
            }
            
            // Get the player's location and calculate target position
            Player player = context.getPlayer();
            Location playerLocation = player.getLocation();
            Location targetLocation = playerLocation.clone().add(relativeX, relativeY, relativeZ);
            
            // Set the block
            targetLocation.getBlock().setType(material);
            
            // Visual effect
            player.spawnParticle(Particle.BLOCK_CRACK, targetLocation, 10, material.createBlockData());
            player.playSound(targetLocation, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            
            // Create success message
            return context.createResult(true, "Set block at " + relativeX + ", " + relativeY + ", " + relativeZ + " to " + material.name().toLowerCase().replace("_", " "));
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set block: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the getPlayerName action to get the player's name
     */
    private ExecutionResult executeGetPlayerName(Map<String, DataValue> params, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            String playerName = player.getName();
            return context.createResult(true, "Player name is " + playerName);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to get player name: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the wait action to pause execution for a specified number of ticks
     */
    private ExecutionResult executeWait(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the delay parameter (required)
            DataValue delayValue = params.get("delay");
            if (delayValue == null) {
                return context.createErrorResult("No delay specified for wait action");
            }
            
            String delayStr = delayValue.asString();
            if (delayStr == null || delayStr.trim().isEmpty()) {
                return context.createErrorResult("Delay value cannot be empty");
            }
            
            // Parse the delay value
            int delay;
            try {
                delay = Integer.parseInt(delayStr);
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid delay format: " + delayStr + ". Must be a number.");
            }
            
            // Validate delay range (must be positive)
            if (delay <= 0) {
                return context.createErrorResult("Delay must be a positive number. Provided: " + delay);
            }
            
            // Get the player
            Player player = context.getPlayer();
            
            // Visual effect before waiting
            player.spawnParticle(Particle.SUSPENDED_DEPTH, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
            
            // Wait for the specified number of ticks
            try {
                Thread.sleep(delay * 50); // Convert ticks to milliseconds (1 tick = 50ms)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return context.createErrorResult("Wait interrupted: " + e.getMessage(), e);
            }
            
            // Visual effect after waiting
            player.spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
            
            // Create success message
            double seconds = delay / 20.0; // Convert ticks to seconds
            return context.createResult(true, "Waited for " + delay + " ticks (" + String.format("%.2f", seconds) + " seconds)");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to execute wait: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the randomNumber action to generate a random number and store it in a variable
     */
    private ExecutionResult executeRandomNumber(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the min value parameter (required)
            DataValue minValueValue = params.get("min_value");
            if (minValueValue == null) {
                return context.createErrorResult("No minimum value specified for randomNumber action");
            }
            
            String minValueStr = minValueValue.asString();
            if (minValueStr == null || minValueStr.trim().isEmpty()) {
                return context.createErrorResult("Minimum value cannot be empty");
            }
            
            // Parse the min value
            int minValue;
            try {
                minValue = Integer.parseInt(minValueStr);
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid minimum value format: " + minValueStr + ". Must be a number.");
            }
            
            // Get the max value parameter (required)
            DataValue maxValueValue = params.get("max_value");
            if (maxValueValue == null) {
                return context.createErrorResult("No maximum value specified for randomNumber action");
            }
            
            String maxValueStr = maxValueValue.asString();
            if (maxValueStr == null || maxValueStr.trim().isEmpty()) {
                return context.createErrorResult("Maximum value cannot be empty");
            }
            
            // Parse the max value
            int maxValue;
            try {
                maxValue = Integer.parseInt(maxValueStr);
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid maximum value format: " + maxValueStr + ". Must be a number.");
            }
            
            // Validate min and max values
            if (minValue > maxValue) {
                return context.createErrorResult("Minimum value must be less than or equal to maximum value. Provided: " + minValue + " > " + maxValue);
            }
            
            // Generate the random number
            int randomNumber = minValue + (int) (Math.random() * (maxValue - minValue + 1));
            
            // Get the target variable parameter (required)
            DataValue targetVariableValue = params.get("target_variable");
            if (targetVariableValue == null) {
                return context.createErrorResult("No target variable specified for randomNumber action");
            }
            
            String targetVariable = targetVariableValue.asString();
            if (targetVariable == null || targetVariable.trim().isEmpty()) {
                return context.createErrorResult("Target variable cannot be empty");
            }
            
            // Store the random number in the variable
            variableManager.setLocalVariable("global", targetVariable, DataValue.of(randomNumber));
            
            // Create success message
            return context.createResult(true, "Generated random number " + randomNumber + " and stored in variable '" + targetVariable + "'");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to generate random number: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the spawnParticles action to spawn particles at a relative position
     */
    private ExecutionResult executeSpawnParticles(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the particle type parameter (required)
            DataValue particleTypeValue = params.get("particle_type");
            if (particleTypeValue == null) {
                return context.createErrorResult("No particle type specified for spawnParticles action");
            }
            
            String particleTypeName = particleTypeValue.asString();
            if (particleTypeName == null || particleTypeName.trim().isEmpty()) {
                return context.createErrorResult("Particle type cannot be empty");
            }
            
            // Try to get the particle type
            Particle particleType;
            try {
                particleType = Particle.valueOf(particleTypeName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid particle type: " + particleTypeName);
            }
            
            // Get count with default 1
            int count = 1;
            if (params.containsKey("count")) {
                String countStr = params.get("count").asString();
                if (countStr != null) {
                    try {
                        count = Integer.parseInt(countStr);
                    } catch (NumberFormatException e) {
                        return context.createErrorResult("Invalid number format for count");
                    }
                }
            }
            
            // Get relative position with defaults (0, 0, 0)
            double relativeX = 0.0;
            double relativeY = 0.0;
            double relativeZ = 0.0;
            if (params.containsKey("relative_x")) {
                String relativeXStr = params.get("relative_x").asString();
                if (relativeXStr != null) {
                    try {
                        relativeX = Double.parseDouble(relativeXStr);
                    } catch (NumberFormatException e) {
                        return context.createErrorResult("Invalid number format for relative_x");
                    }
                }
            }
            if (params.containsKey("relative_y")) {
                String relativeYStr = params.get("relative_y").asString();
                if (relativeYStr != null) {
                    try {
                        relativeY = Double.parseDouble(relativeYStr);
                    } catch (NumberFormatException e) {
                        return context.createErrorResult("Invalid number format for relative_y");
                    }
                }
            }
            if (params.containsKey("relative_z")) {
                String relativeZStr = params.get("relative_z").asString();
                if (relativeZStr != null) {
                    try {
                        relativeZ = Double.parseDouble(relativeZStr);
                    } catch (NumberFormatException e) {
                        return context.createErrorResult("Invalid number format for relative_z");
                    }
                }
            }
            
            // Get the player
            Player player = context.getPlayer();
            
            // Spawn the particles at the relative position
            player.spawnParticle(particleType, player.getLocation().add(relativeX, relativeY, relativeZ), count);
            
            // Visual effect
            player.spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
            
            // Create success message
            return context.createResult(true, "Spawned " + count + " " + particleType.name().toLowerCase() + 
                " particles at relative position (" + relativeX + ", " + relativeY + ", " + relativeZ + ")");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to spawn particles: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the sendTitle action to send a title and subtitle to the player
     */
    private ExecutionResult executeSendTitle(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the title parameter (required)
            DataValue titleValue = params.get("title");
            if (titleValue == null) {
                return context.createErrorResult("No title specified for sendTitle action");
            }
            
            String title = titleValue.asString();
            if (title == null || title.trim().isEmpty()) {
                return context.createErrorResult("Title cannot be empty");
            }
            
            // Get subtitle with default empty string
            String subtitle = "";
            if (params.containsKey("subtitle")) {
                DataValue subtitleValue = params.get("subtitle");
                if (subtitleValue != null) {
                    subtitle = subtitleValue.asString();
                    if (subtitle == null) {
                        subtitle = "";
                    }
                }
            }
            
            // Get timing parameters with defaults
            int fadeIn = 10;   // Default 10 ticks
            int stay = 70;     // Default 70 ticks
            int fadeOut = 20;  // Default 20 ticks
            
            try {
                if (params.containsKey("fadein")) {
                    String fadeInStr = params.get("fadein").asString();
                    if (fadeInStr != null && fadeInStr.startsWith("fadein:")) {
                        fadeIn = Integer.parseInt(fadeInStr.substring("fadein:".length()));
                    } else if (fadeInStr != null) {
                        fadeIn = Integer.parseInt(fadeInStr);
                    }
                }
                if (params.containsKey("stay")) {
                    String stayStr = params.get("stay").asString();
                    if (stayStr != null && stayStr.startsWith("stay:")) {
                        stay = Integer.parseInt(stayStr.substring("stay:".length()));
                    } else if (stayStr != null) {
                        stay = Integer.parseInt(stayStr);
                    }
                }
                if (params.containsKey("fadeout")) {
                    String fadeOutStr = params.get("fadeout").asString();
                    if (fadeOutStr != null && fadeOutStr.startsWith("fadeout:")) {
                        fadeOut = Integer.parseInt(fadeOutStr.substring("fadeout:".length()));
                    } else if (fadeOutStr != null) {
                        fadeOut = Integer.parseInt(fadeOutStr);
                    }
                }
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid number format for timing parameters");
            }
            
            // Validate timing ranges
            if (fadeIn < 0 || fadeIn > 100) {
                fadeIn = Math.max(0, Math.min(100, fadeIn)); // Clamp to valid range
            }
            if (stay < 0 || stay > 1000) {
                stay = Math.max(0, Math.min(1000, stay)); // Clamp to valid range
            }
            if (fadeOut < 0 || fadeOut > 100) {
                fadeOut = Math.max(0, Math.min(100, fadeOut)); // Clamp to valid range
            }
            
            // Get the player
            Player player = context.getPlayer();
            
            // Resolve placeholders in title and subtitle
            title = resolvePlaceholders(title, context);
            subtitle = resolvePlaceholders(subtitle, context);
            
            // Send the title to the player
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            
            // Visual effect
            player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            
            // Create success message
            return context.createResult(true, "Sent title '" + title + "' to player");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to send title: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the playParticle action to spawn particles at a relative position from the player
     */
    private ExecutionResult executePlayParticle(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the particle type parameter (required)
            DataValue particleTypeValue = params.get("particle_type");
            if (particleTypeValue == null) {
                return context.createErrorResult("No particle type specified for playParticle action");
            }
            
            String particleTypeName = particleTypeValue.asString();
            if (particleTypeName == null || particleTypeName.trim().isEmpty()) {
                return context.createErrorResult("Particle type cannot be empty");
            }
            
            // Try to get the particle type
            org.bukkit.Particle particleType;
            try {
                particleType = org.bukkit.Particle.valueOf(particleTypeName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid particle type: " + particleTypeName);
            }
            
            // Get count with default
            int count = 10;
            try {
                if (params.containsKey("count")) {
                    count = Integer.parseInt(params.get("count").asString());
                }
            } catch (NumberFormatException e) {
                // Keep default count
            }
            
            // Validate count range
            if (count < 1 || count > 100) {
                count = Math.max(1, Math.min(100, count)); // Clamp to valid range
            }
            
            // Get relative coordinates with defaults
            double relativeX = 0.0;
            double relativeY = 0.0;
            double relativeZ = 0.0;
            
            try {
                if (params.containsKey("relative_x")) {
                    relativeX = Double.parseDouble(params.get("relative_x").asString());
                }
                if (params.containsKey("relative_y")) {
                    relativeY = Double.parseDouble(params.get("relative_y").asString());
                }
                if (params.containsKey("relative_z")) {
                    relativeZ = Double.parseDouble(params.get("relative_z").asString());
                }
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid number format for relative coordinates");
            }
            
            // Get the player's location and calculate target position
            Player player = context.getPlayer();
            Location playerLocation = player.getLocation();
            Location targetLocation = playerLocation.clone().add(relativeX, relativeY, relativeZ);
            
            // Spawn the particles
            player.spawnParticle(particleType, targetLocation, count);
            
            // Visual effect
            player.playSound(targetLocation, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
            
            // Create success message
            return context.createResult(true, "Spawned " + count + " " + particleType.name().toLowerCase() + 
                " particles at relative position (" + relativeX + ", " + relativeY + ", " + relativeZ + ")");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to spawn particles: " + e.getMessage(), e);
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
            context.getPlayer().spawnParticle(Particle.ENCHANTMENT_TABLE, context.getPlayer().getLocation().add(0, 2, 0), 5);
            
            return context.createResult(success, "Executed command: " + command);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to execute command: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeBroadcast(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue messageValue = params.get("message_slot");
            if (messageValue == null) {
                return context.createErrorResult("No message specified");
            }
            
            String message = messageValue.asString();
            if (message == null || message.trim().isEmpty()) {
                return context.createErrorResult("Message cannot be empty");
            }
            
            // Resolve placeholders in message
            message = resolvePlaceholders(message, context);
            
            // Broadcast the message to all players
            plugin.getServer().broadcastMessage(message);
            
            // Visual effect
            context.getPlayer().spawnParticle(Particle.VILLAGER_HAPPY, context.getPlayer().getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Broadcasted message: " + message);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to broadcast message: " + e.getMessage(), e);
        }
    }

    /**
     * Executes the sendActionBar action to send a message to the player's action bar
     */
    private ExecutionResult executeSendActionBar(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the message parameter (required)
            DataValue messageValue = params.get("message");
            if (messageValue == null) {
                return context.createErrorResult("No message specified for sendActionBar action");
            }
            
            String message = messageValue.asString();
            if (message == null || message.trim().isEmpty()) {
                return context.createErrorResult("Message cannot be empty");
            }
            
            // Validate message length (1-64 characters as per config)
            if (message.length() < 1 || message.length() > 64) {
                return context.createErrorResult("Message must be between 1 and 64 characters. Provided: " + message.length());
            }
            
            // Resolve placeholders in the message
            message = resolvePlaceholders(message, context);
            
            // Send the action bar message to the player
            Player player = context.getPlayer();
            player.sendActionBar(message);
            
            // Visual effect
            player.spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 2, 0), 3);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
            
            // Create success message
            return context.createResult(true, "Sent action bar message: " + message);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to send action bar message: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the executeAsyncCommand action to run a command asynchronously
     */
    private ExecutionResult executeExecuteAsyncCommand(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the command parameter (required)
            DataValue commandValue = params.get("command");
            if (commandValue == null) {
                return context.createErrorResult("No command specified for executeAsyncCommand action");
            }
            
            String command = commandValue.asString();
            if (command == null || command.trim().isEmpty()) {
                return context.createErrorResult("Command cannot be empty");
            }
            
            // Validate command format using regex from config
            if (!command.matches("^[\\w/][\\w\\s\\-._~:/?#\\[\\]@!$&'()*+,;=]*$")) {
                return context.createErrorResult("Command format is invalid");
            }
            
            // Resolve placeholders in command
            command = resolvePlaceholders(command, context);
            
            // Get the player (must be final for lambda)
            final Player player = context.getPlayer();
            final String finalCommand = command;
            
            // Execute command asynchronously
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean success = plugin.getServer().dispatchCommand(player, finalCommand);
                        if (success) {
                            player.sendMessage("§aCommand executed successfully: " + finalCommand);
                        } else {
                            player.sendMessage("§cCommand execution failed: " + finalCommand);
                        }
                    } catch (Exception e) {
                        player.sendMessage("§cError executing command: " + e.getMessage());
                        plugin.getLogger().warning("Async command execution failed: " + e.getMessage());
                    }
                }
            });
            
            // Visual effect
            player.spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
            
            // Create success message
            return context.createResult(true, "Executing command asynchronously: " + command);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to execute async command: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the createScoreboard action to create a scoreboard for the player
     */
    private ExecutionResult executeCreateScoreboard(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the title parameter (required)
            DataValue titleValue = params.get("title");
            if (titleValue == null) {
                return context.createErrorResult("No title specified for createScoreboard action");
            }
            
            String title = titleValue.asString();
            if (title == null || title.trim().isEmpty()) {
                return context.createErrorResult("Title cannot be empty");
            }
            
            // Validate title length (1-32 characters as per config)
            if (title.length() < 1 || title.length() > 32) {
                return context.createErrorResult("Title must be between 1 and 32 characters. Provided: " + title.length());
            }
            
            // Resolve placeholders in the title
            title = resolvePlaceholders(title, context);
            
            // Get the player
            Player player = context.getPlayer();
            
            // Create scoreboard
            org.bukkit.scoreboard.ScoreboardManager scoreboardManager = plugin.getServer().getScoreboardManager();
            if (scoreboardManager == null) {
                return context.createErrorResult("Scoreboard manager is not available");
            }
            
            org.bukkit.scoreboard.Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
            org.bukkit.scoreboard.Objective objective = scoreboard.registerNewObjective("megacreative", "dummy", title);
            objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
            
            // Set the scoreboard for the player
            player.setScoreboard(scoreboard);
            
            // Visual effect
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.5f);
            
            // Create success message
            return context.createResult(true, "Created scoreboard with title: " + title);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to create scoreboard: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the setScore action to set a score on the player's scoreboard
     */
    private ExecutionResult executeSetScore(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the key parameter (required)
            DataValue keyValue = params.get("key");
            if (keyValue == null) {
                return context.createErrorResult("No key specified for setScore action");
            }
            
            String key = keyValue.asString();
            if (key == null || key.trim().isEmpty()) {
                return context.createErrorResult("Key cannot be empty");
            }
            
            // Validate key length (1-16 characters as per config)
            if (key.length() < 1 || key.length() > 16) {
                return context.createErrorResult("Key must be between 1 and 16 characters. Provided: " + key.length());
            }
            
            // Get the value parameter (required)
            DataValue valueValue = params.get("value");
            if (valueValue == null) {
                return context.createErrorResult("No value specified for setScore action");
            }
            
            String valueStr = valueValue.asString();
            if (valueStr == null || valueStr.trim().isEmpty()) {
                return context.createErrorResult("Value cannot be empty");
            }
            
            // Parse the value as an integer
            int value;
            try {
                value = Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                return context.createErrorResult("Value must be a number. Provided: " + valueStr);
            }
            
            // Get the player
            Player player = context.getPlayer();
            
            // Get the player's scoreboard
            org.bukkit.scoreboard.Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) {
                return context.createErrorResult("Player has no scoreboard");
            }
            
            // Get the objective (assuming the default objective from createScoreboard)
            org.bukkit.scoreboard.Objective objective = scoreboard.getObjective("megacreative");
            if (objective == null) {
                return context.createErrorResult("No scoreboard objective found");
            }
            
            // Set the score
            org.bukkit.scoreboard.Score score = objective.getScore(key);
            score.setScore(value);
            
            // Visual effect
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 3);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
            
            // Create success message
            return context.createResult(true, "Set score '" + key + "' to " + value);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set score: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the incrementScore action to increment a score on the player's scoreboard
     */
    private ExecutionResult executeIncrementScore(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the key parameter (required)
            DataValue keyValue = params.get("key");
            if (keyValue == null) {
                return context.createErrorResult("No key specified for incrementScore action");
            }
            
            String key = keyValue.asString();
            if (key == null || key.trim().isEmpty()) {
                return context.createErrorResult("Key cannot be empty");
            }
            
            // Validate key length (1-16 characters as per config)
            if (key.length() < 1 || key.length() > 16) {
                return context.createErrorResult("Key must be between 1 and 16 characters. Provided: " + key.length());
            }
            
            // Get the increment parameter (required)
            DataValue incrementValue = params.get("increment");
            if (incrementValue == null) {
                return context.createErrorResult("No increment value specified for incrementScore action");
            }
            
            String incrementStr = incrementValue.asString();
            if (incrementStr == null || incrementStr.trim().isEmpty()) {
                return context.createErrorResult("Increment value cannot be empty");
            }
            
            // Parse the increment as an integer
            int increment;
            try {
                increment = Integer.parseInt(incrementStr);
            } catch (NumberFormatException e) {
                return context.createErrorResult("Increment value must be a number. Provided: " + incrementStr);
            }
            
            // Get the player
            Player player = context.getPlayer();
            
            // Get the player's scoreboard
            org.bukkit.scoreboard.Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) {
                return context.createErrorResult("Player has no scoreboard");
            }
            
            // Get the objective (assuming the default objective from createScoreboard)
            org.bukkit.scoreboard.Objective objective = scoreboard.getObjective("megacreative");
            if (objective == null) {
                return context.createErrorResult("No scoreboard objective found");
            }
            
            // Get the current score and increment it
            org.bukkit.scoreboard.Score score = objective.getScore(key);
            int currentValue = score.getScore();
            score.setScore(currentValue + increment);
            
            // Visual effect
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 3);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
            
            // Create success message
            return context.createResult(true, "Incremented score '" + key + "' by " + increment + " (new value: " + (currentValue + increment) + ")");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to increment score: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the createTeam action to create a team on the player's scoreboard
     */
    private ExecutionResult executeCreateTeam(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the team name parameter (required)
            DataValue teamNameValue = params.get("teamName");
            if (teamNameValue == null) {
                return context.createErrorResult("No team name specified for createTeam action");
            }
            
            String teamName = teamNameValue.asString();
            if (teamName == null || teamName.trim().isEmpty()) {
                return context.createErrorResult("Team name cannot be empty");
            }
            
            // Validate team name length (1-16 characters as per config)
            if (teamName.length() < 1 || teamName.length() > 16) {
                return context.createErrorResult("Team name must be between 1 and 16 characters. Provided: " + teamName.length());
            }
            
            // Get optional parameters
            String displayName = "";
            String prefix = "";
            String suffix = "";
            
            if (params.containsKey("displayName")) {
                DataValue displayNameValue = params.get("displayName");
                if (displayNameValue != null) {
                    displayName = displayNameValue.asString();
                    if (displayName == null) displayName = "";
                }
            }
            
            if (params.containsKey("prefix")) {
                DataValue prefixValue = params.get("prefix");
                if (prefixValue != null) {
                    prefix = prefixValue.asString();
                    if (prefix == null) prefix = "";
                }
                // Validate prefix length (0-16 characters as per config)
                if (prefix.length() > 16) {
                    prefix = prefix.substring(0, 16);
                }
            }
            
            if (params.containsKey("suffix")) {
                DataValue suffixValue = params.get("suffix");
                if (suffixValue != null) {
                    suffix = suffixValue.asString();
                    if (suffix == null) suffix = "";
                }
                // Validate suffix length (0-16 characters as per config)
                if (suffix.length() > 16) {
                    suffix = suffix.substring(0, 16);
                }
            }
            
            // Get the player
            Player player = context.getPlayer();
            
            // Get the player's scoreboard
            org.bukkit.scoreboard.Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) {
                return context.createErrorResult("Player has no scoreboard");
            }
            
            // Create or get the team
            org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
            }
            
            // Set team properties
            team.setDisplayName(displayName.isEmpty() ? teamName : displayName);
            team.setPrefix(prefix);
            team.setSuffix(suffix);
            
            // Visual effect
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.5f);
            
            // Create success message
            return context.createResult(true, "Created team '" + teamName + "' with display name '" + team.getDisplayName() + "'");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to create team: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the addPlayerToTeam action to add a player to a team
     */
    private ExecutionResult executeAddPlayerToTeam(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the team name parameter (required)
            DataValue teamNameValue = params.get("teamName");
            if (teamNameValue == null) {
                return context.createErrorResult("No team name specified for addPlayerToTeam action");
            }
            
            String teamName = teamNameValue.asString();
            if (teamName == null || teamName.trim().isEmpty()) {
                return context.createErrorResult("Team name cannot be empty");
            }
            
            // Validate team name length (1-16 characters as per config)
            if (teamName.length() < 1 || teamName.length() > 16) {
                return context.createErrorResult("Team name must be between 1 and 16 characters. Provided: " + teamName.length());
            }
            
            // Get the target player parameter (optional, defaults to current player)
            Player targetPlayer = context.getPlayer();
            if (params.containsKey("targetPlayer")) {
                DataValue targetPlayerValue = params.get("targetPlayer");
                if (targetPlayerValue != null) {
                    String targetPlayerName = targetPlayerValue.asString();
                    if (targetPlayerName != null && !targetPlayerName.trim().isEmpty()) {
                        targetPlayer = plugin.getServer().getPlayer(targetPlayerName);
                        if (targetPlayer == null) {
                            return context.createErrorResult("Player not found: " + targetPlayerName);
                        }
                    }
                }
            }
            
            // Get the player
            Player player = context.getPlayer();
            
            // Get the player's scoreboard
            org.bukkit.scoreboard.Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) {
                return context.createErrorResult("Player has no scoreboard");
            }
            
            // Get the team
            org.bukkit.scoreboard.Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                return context.createErrorResult("Team not found: " + teamName);
            }
            
            // Add player to team
            team.addEntry(targetPlayer.getName());
            
            // Visual effect
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.5f);
            
            // Create success message
            String message = targetPlayer.equals(player) ? 
                "Added you to team '" + teamName + "'" : 
                "Added player '" + targetPlayer.getName() + "' to team '" + teamName + "'";
            return context.createResult(true, message);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to add player to team: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the saveLocation action to save a player's current location
     */
    private ExecutionResult executeSaveLocation(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the location name parameter (required)
            DataValue locationNameValue = params.get("locationName");
            if (locationNameValue == null) {
                return context.createErrorResult("No location name specified for saveLocation action");
            }
            
            String locationName = locationNameValue.asString();
            if (locationName == null || locationName.trim().isEmpty()) {
                return context.createErrorResult("Location name cannot be empty");
            }
            
            // Validate location name length (1-32 characters as per config)
            if (locationName.length() < 1 || locationName.length() > 32) {
                return context.createErrorResult("Location name must be between 1 and 32 characters. Provided: " + locationName.length());
            }
            
            // Get the player
            Player player = context.getPlayer();
            Location location = player.getLocation();
            
            // Create location string representation
            String locationString = String.format("%s:%.2f,%.2f,%.2f:%.2f,%.2f", 
                player.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
            
            // Save location to variable manager
            String scriptId = context.getScript() != null ? 
                context.getScript().getId().toString() : "global";
            variableManager.setLocalVariable(scriptId, "location_" + locationName, DataValue.of(locationString));
            
            // Visual effect
            player.spawnParticle(Particle.PORTAL, location.add(0, 2, 0), 10);
            player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            
            // Create success message
            return context.createResult(true, "Saved location '" + locationName + "' at " + 
                String.format("%.2f, %.2f, %.2f", location.getX(), location.getY(), location.getZ()));
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to save location: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the getLocation action to retrieve a saved location and store it in a variable
     */
    private ExecutionResult executeGetLocation(Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the location name parameter (required)
            DataValue locationNameValue = params.get("locationName");
            if (locationNameValue == null) {
                return context.createErrorResult("No location name specified for getLocation action");
            }
            
            String locationName = locationNameValue.asString();
            if (locationName == null || locationName.trim().isEmpty()) {
                return context.createErrorResult("Location name cannot be empty");
            }
            
            // Validate location name length (1-32 characters as per config)
            if (locationName.length() < 1 || locationName.length() > 32) {
                return context.createErrorResult("Location name must be between 1 and 32 characters. Provided: " + locationName.length());
            }
            
            // Get the target variable parameter (required)
            DataValue targetVariableValue = params.get("targetVariable");
            if (targetVariableValue == null) {
                return context.createErrorResult("No target variable specified for getLocation action");
            }
            
            String targetVariable = targetVariableValue.asString();
            if (targetVariable == null || targetVariable.trim().isEmpty()) {
                return context.createErrorResult("Target variable cannot be empty");
            }
            
            // Validate target variable length (1-16 characters as per config)
            if (targetVariable.length() < 1 || targetVariable.length() > 16) {
                return context.createErrorResult("Target variable must be between 1 and 16 characters. Provided: " + targetVariable.length());
            }
            
            // Get the player
            Player player = context.getPlayer();
            
            // Get location from variable manager
            String scriptId = context.getScript() != null ? 
                context.getScript().getId().toString() : "global";
            DataValue locationValue = variableManager.getLocalVariable(scriptId, "location_" + locationName);
            
            if (locationValue == null) {
                return context.createErrorResult("Location not found: " + locationName);
            }
            
            String locationString = locationValue.asString();
            if (locationString == null || locationString.trim().isEmpty()) {
                return context.createErrorResult("Location data is invalid: " + locationName);
            }
            
            // Store location in target variable
            variableManager.setLocalVariable(scriptId, targetVariable, DataValue.of(locationString));
            
            // Visual effect
            player.spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
            
            // Create success message
            return context.createResult(true, "Retrieved location '" + locationName + "' and stored in variable '" + targetVariable + "'");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to get location: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the isNight condition to check if it's night time in the player's world
     */
    private ExecutionResult executeIsNight(com.megacreative.coding.CodeBlock block, 
                                         Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the time parameter (required)
            DataValue timeValue = params.get("time_slot");
            if (timeValue == null) {
                return context.createErrorResult("No time specified for isNight condition");
            }
            
            String timeStr = timeValue.asString();
            if (timeStr == null || timeStr.trim().isEmpty()) {
                return context.createErrorResult("Time value cannot be empty");
            }
            
            // Validate time value (night or day as per config)
            if (!"night".equalsIgnoreCase(timeStr) && !"day".equalsIgnoreCase(timeStr)) {
                return context.createErrorResult("Time must be either 'night' or 'day'. Provided: " + timeStr);
            }
            
            // Get the player
            Player player = context.getPlayer();
            
            // Check if it's night (time between 12542 and 23459 is considered night in Minecraft)
            long time = player.getWorld().getTime();
            boolean isNightTime = (time >= 12542 && time <= 23459);
            boolean checkForNight = "night".equalsIgnoreCase(timeStr);
            
            // Check condition
            boolean condition = (checkForNight && isNightTime) || (!checkForNight && !isNightTime);
            
            // Log the condition check
            plugin.getLogger().fine(String.format("Condition check: isNight=%s, checkForNight=%s -> %s",
                isNightTime, checkForNight, condition));
            
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
            
            // Visual feedback
            if (condition) {
                player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 3);
            } else {
                player.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 2, 0), 3);
            }
            
            return context.createResult(true, "Condition evaluated: " + condition);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to evaluate isNight condition: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the isRiding condition to check if the player is riding a specific entity
     */
    private ExecutionResult executeIsRiding(com.megacreative.coding.CodeBlock block, 
                                         Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the entity parameter (required)
            DataValue entityValue = params.get("entity_slot");
            if (entityValue == null) {
                return context.createErrorResult("No entity specified for isRiding condition");
            }
            
            String entityStr = entityValue.asString();
            if (entityStr == null || entityStr.trim().isEmpty()) {
                return context.createErrorResult("Entity value cannot be empty");
            }
            
            // Validate entity value (horse, boat, pig, strider as per config)
            if (!"horse".equalsIgnoreCase(entityStr) && 
                !"boat".equalsIgnoreCase(entityStr) && 
                !"pig".equalsIgnoreCase(entityStr) && 
                !"strider".equalsIgnoreCase(entityStr)) {
                return context.createErrorResult("Entity must be one of: horse, boat, pig, strider. Provided: " + entityStr);
            }
            
            // Get the player
            Player player = context.getPlayer();
            
            // Check if player is riding something
            boolean isRiding = player.isInsideVehicle();
            boolean condition = false;
            
            if (isRiding) {
                org.bukkit.entity.Entity vehicle = player.getVehicle();
                if (vehicle != null) {
                    // Check specific entity type
                    switch (entityStr.toLowerCase()) {
                        case "horse":
                            condition = vehicle instanceof org.bukkit.entity.Horse;
                            break;
                        case "boat":
                            condition = vehicle instanceof org.bukkit.entity.Boat;
                            break;
                        case "pig":
                            condition = vehicle instanceof org.bukkit.entity.Pig;
                            break;
                        case "strider":
                            condition = vehicle instanceof org.bukkit.entity.Strider;
                            break;
                    }
                }
            }
            
            // Log the condition check
            plugin.getLogger().fine(String.format("Condition check: isRiding=%s, entity=%s -> %s",
                isRiding, entityStr, condition));
            
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
            
            // Visual feedback
            if (condition) {
                player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 3);
            } else {
                player.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 2, 0), 3);
            }
            
            return context.createResult(true, "Condition evaluated: " + condition);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to evaluate isRiding condition: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the checkPlayerInventory condition to check if the player has specific items in their inventory
     */
    private ExecutionResult executeCheckPlayerInventory(com.megacreative.coding.CodeBlock block, 
                                                     Map<String, DataValue> params, ExecutionContext context) {
        try {
            // Get the item parameter (required)
            DataValue itemValue = params.get("item_slot");
            if (itemValue == null) {
                return context.createErrorResult("No item specified for checkPlayerInventory condition");
            }
            
            String itemStr = itemValue.asString();
            if (itemStr == null || itemStr.trim().isEmpty()) {
                return context.createErrorResult("Item value cannot be empty");
            }
            
            // Try to get the material
            org.bukkit.Material material;
            try {
                material = org.bukkit.Material.valueOf(itemStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid item material: " + itemStr);
            }
            
            // Get optional amount parameter (defaults to 1)
            int amount = 1;
            if (params.containsKey("amount_slot")) {
                DataValue amountValue = params.get("amount_slot");
                if (amountValue != null) {
                    String amountStr = amountValue.asString();
                    if (amountStr != null && !amountStr.trim().isEmpty()) {
                        try {
                            amount = Integer.parseInt(amountStr);
                        } catch (NumberFormatException e) {
                            // Keep default amount of 1
                        }
                    }
                }
            }
            
            // Get optional check type parameter (defaults to "has")
            String checkType = "has";
            if (params.containsKey("check_type_slot")) {
                DataValue checkTypeValue = params.get("check_type_slot");
                if (checkTypeValue != null) {
                    String checkTypeStr = checkTypeValue.asString();
                    if (checkTypeStr != null && !checkTypeStr.trim().isEmpty()) {
                        checkType = checkTypeStr.toLowerCase();
                    }
                }
            }
            
            // Validate check type
            if (!"has".equals(checkType) && !"missing".equals(checkType) && !"exact".equals(checkType)) {
                return context.createErrorResult("Check type must be one of: has, missing, exact. Provided: " + checkType);
            }
            
            // Get the player
            Player player = context.getPlayer();
            
            // Check player's inventory for the item
            int itemCount = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == material) {
                    itemCount += item.getAmount();
                }
            }
            
            // Evaluate condition based on check type
            boolean condition = false;
            switch (checkType) {
                case "has":
                    condition = itemCount >= amount;
                    break;
                case "missing":
                    condition = itemCount < amount;
                    break;
                case "exact":
                    condition = itemCount == amount;
                    break;
            }
            
            // Log the condition check
            plugin.getLogger().fine(String.format("Condition check: itemCount=%d, required=%d, checkType=%s -> %s",
                itemCount, amount, checkType, condition));
            
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
            
            // Visual feedback
            if (condition) {
                player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 3);
            } else {
                player.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 2, 0), 3);
            }
            
            return context.createResult(true, "Condition evaluated: " + condition);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to evaluate checkPlayerInventory condition: " + e.getMessage(), e);
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
    
    /**
     * Executes the ifVarGreater condition to check if a variable is greater than a value
     */
    private ExecutionResult executeIfVarGreater(com.megacreative.coding.CodeBlock block,
                                              Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue varNameValue = params.get("name");
            DataValue compareValue = params.get("value");
            
            if (varNameValue == null) {
                return context.createErrorResult("Variable name not specified");
            }
            
            String varName = varNameValue.asString();
            if (varName == null || varName.trim().isEmpty()) {
                return context.createErrorResult("Variable name cannot be empty");
            }
            
            if (compareValue == null) {
                return context.createErrorResult("Comparison value not specified");
            }
            
            // Get world and script IDs for variable scoping
            String scriptId = context.getScript() != null ? 
                context.getScript().getId().toString() : "global";
            
            // Get current variable value
            DataValue currentValue = variableManager.getLocalVariable(scriptId, varName);
            
            // Check condition - variable > compareValue
            boolean condition = false;
            if (currentValue != null && currentValue.getValue() != null && compareValue.getValue() != null) {
                try {
                    // Try to compare as numbers first
                    double currentNum = Double.parseDouble(currentValue.asString());
                    double compareNum = Double.parseDouble(compareValue.asString());
                    condition = currentNum > compareNum;
                } catch (NumberFormatException e) {
                    // If not numbers, compare as strings
                    condition = currentValue.asString().compareTo(compareValue.asString()) > 0;
                }
            }
            
            // Log the condition check
            plugin.getLogger().fine(String.format("Condition check: %s > %s -> %s",
                currentValue != null ? currentValue.asString() : "null",
                compareValue.asString(),
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
            
            // Visual feedback
            Player player = context.getPlayer();
            if (condition) {
                player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 3);
            } else {
                player.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 2, 0), 3);
            }
            
            return context.createResult(true, "Condition evaluated: " + condition);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to evaluate condition: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the ifVarLess condition to check if a variable is less than a value
     */
    private ExecutionResult executeIfVarLess(com.megacreative.coding.CodeBlock block,
                                           Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue varNameValue = params.get("name");
            DataValue compareValue = params.get("value");
            
            if (varNameValue == null) {
                return context.createErrorResult("Variable name not specified");
            }
            
            String varName = varNameValue.asString();
            if (varName == null || varName.trim().isEmpty()) {
                return context.createErrorResult("Variable name cannot be empty");
            }
            
            if (compareValue == null) {
                return context.createErrorResult("Comparison value not specified");
            }
            
            // Get world and script IDs for variable scoping
            String scriptId = context.getScript() != null ? 
                context.getScript().getId().toString() : "global";
            
            // Get current variable value
            DataValue currentValue = variableManager.getLocalVariable(scriptId, varName);
            
            // Check condition - variable < compareValue
            boolean condition = false;
            if (currentValue != null && currentValue.getValue() != null && compareValue.getValue() != null) {
                try {
                    // Try to compare as numbers first
                    double currentNum = Double.parseDouble(currentValue.asString());
                    double compareNum = Double.parseDouble(compareValue.asString());
                    condition = currentNum < compareNum;
                } catch (NumberFormatException e) {
                    // If not numbers, compare as strings
                    condition = currentValue.asString().compareTo(compareValue.asString()) < 0;
                }
            }
            
            // Log the condition check
            plugin.getLogger().fine(String.format("Condition check: %s < %s -> %s",
                currentValue != null ? currentValue.asString() : "null",
                compareValue.asString(),
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
            
            // Visual feedback
            Player player = context.getPlayer();
            if (condition) {
                player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 3);
            } else {
                player.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 2, 0), 3);
            }
            
            return context.createResult(true, "Condition evaluated: " + condition);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to evaluate condition: " + e.getMessage(), e);
        }
    }
    
    /**
     * Executes the compareVariable condition to compare two variables with various operators
     */
    private ExecutionResult executeCompareVariable(com.megacreative.coding.CodeBlock block,
                                                 Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue var1Value = params.get("var1");
            DataValue operatorValue = params.get("operator");
            DataValue var2Value = params.get("var2");
            
            if (var1Value == null) {
                return context.createErrorResult("First variable name not specified");
            }
            
            if (operatorValue == null) {
                return context.createErrorResult("Operator not specified");
            }
            
            if (var2Value == null) {
                return context.createErrorResult("Second variable name not specified");
            }
            
            String var1Name = var1Value.asString();
            String operator = operatorValue.asString();
            String var2Name = var2Value.asString();
            
            if (var1Name == null || var1Name.trim().isEmpty()) {
                return context.createErrorResult("First variable name cannot be empty");
            }
            
            if (operator == null || operator.trim().isEmpty()) {
                return context.createErrorResult("Operator cannot be empty");
            }
            
            if (var2Name == null || var2Name.trim().isEmpty()) {
                return context.createErrorResult("Second variable name cannot be empty");
            }
            
            // Get world and script IDs for variable scoping
            String scriptId = context.getScript() != null ? 
                context.getScript().getId().toString() : "global";
            
            // Get both variable values
            DataValue value1 = variableManager.getLocalVariable(scriptId, var1Name);
            DataValue value2 = variableManager.getLocalVariable(scriptId, var2Name);
            
            // Check condition based on operator
            boolean condition = false;
            if (value1 != null && value1.getValue() != null && value2 != null && value2.getValue() != null) {
                try {
                    // Try to compare as numbers first
                    double num1 = Double.parseDouble(value1.asString());
                    double num2 = Double.parseDouble(value2.asString());
                    
                    switch (operator) {
                        case "==":
                        case "===":
                            condition = num1 == num2;
                            break;
                        case "!=":
                        case "!==":
                            condition = num1 != num2;
                            break;
                        case "<":
                            condition = num1 < num2;
                            break;
                        case ">":
                            condition = num1 > num2;
                            break;
                        case "<=":
                            condition = num1 <= num2;
                            break;
                        case ">=":
                            condition = num1 >= num2;
                            break;
                        default:
                            return context.createErrorResult("Invalid operator: " + operator);
                    }
                } catch (NumberFormatException e) {
                    // If not numbers, compare as strings
                    int comparison = value1.asString().compareTo(value2.asString());
                    
                    switch (operator) {
                        case "==":
                        case "===":
                            condition = comparison == 0;
                            break;
                        case "!=":
                        case "!==":
                            condition = comparison != 0;
                            break;
                        case "<":
                            condition = comparison < 0;
                            break;
                        case ">":
                            condition = comparison > 0;
                            break;
                        case "<=":
                            condition = comparison <= 0;
                            break;
                        case ">=":
                            condition = comparison >= 0;
                            break;
                        default:
                            return context.createErrorResult("Invalid operator: " + operator);
                    }
                }
            } else if (value1 == null && value2 == null) {
                // Both null - only == or != make sense
                switch (operator) {
                    case "==":
                    case "===":
                        condition = true;
                        break;
                    case "!=":
                    case "!==":
                        condition = false;
                        break;
                    default:
                        condition = false;
                        break;
                }
            } else {
                // One is null, the other isn't - only != makes sense
                switch (operator) {
                    case "!=":
                    case "!==":
                        condition = true;
                        break;
                    default:
                        condition = false;
                        break;
                }
            }
            
            // Log the condition check
            plugin.getLogger().fine(String.format("Condition check: %s %s %s -> %s",
                value1 != null ? value1.asString() : "null",
                operator,
                value2 != null ? value2.asString() : "null",
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
            
            // Visual feedback
            Player player = context.getPlayer();
            if (condition) {
                player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 3);
            } else {
                player.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 2, 0), 3);
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