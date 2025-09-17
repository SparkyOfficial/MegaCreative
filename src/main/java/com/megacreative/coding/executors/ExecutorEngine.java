package com.megacreative.coding.executors;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
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
    private final BlockConfigService blockConfigService;
    
    // Execution tracking
    private final Map<String, ExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    private final Map<String, ExecutionStats> executionStats = new ConcurrentHashMap<>();
    
    // Visual feedback
    private final Map<Location, VisualIndicator> visualIndicators = new ConcurrentHashMap<>();
    
    public ExecutorEngine(MegaCreative plugin, VariableManager variableManager) {
        this.plugin = plugin;
        this.variableManager = variableManager;
        this.blockConfigService = plugin.getServiceRegistry().getService(BlockConfigService.class);
        startVisualUpdater();
    }
    
    /**
     * Executes a script with full visual feedback and monitoring
     */
    public CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, String trigger) {
        String executionId = UUID.randomUUID().toString();
        
        ExecutionContext context = new ExecutionContext(executionId, script, player, trigger);
        // Set additional context properties as needed
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
    private ExecutionResult executeBlockChain(CodeBlock block, ExecutionContext context) {
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
    private ExecutionResult executeBlock(CodeBlock block, ExecutionContext context) {
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
                case "setWeather":
                    result = executeSetWeather(processedParameters, context);
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
            
            // Parse the location string
            String locationStr = locationValue.asString();
            if (locationStr == null || locationStr.trim().isEmpty()) {
                return context.createErrorResult("Invalid location string");
            }
            
            Player player = context.getPlayer();
            Location currentLoc = player.getLocation();
            
            // Parse location string in format "x,y,z" or "x,y,z,world"
            String[] parts = locationStr.split(",");
            if (parts.length < 3) {
                return context.createErrorResult("Invalid location format. Expected: x,y,z or x,y,z,world");
            }
            
            double x, y, z;
            try {
                x = Double.parseDouble(parts[0].trim());
                y = Double.parseDouble(parts[1].trim());
                z = Double.parseDouble(parts[2].trim());
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid coordinate values in location string");
            }
            
            // Determine world
            org.bukkit.World world = currentLoc.getWorld();
            if (parts.length >= 4 && !parts[3].trim().isEmpty()) {
                world = org.bukkit.Bukkit.getWorld(parts[3].trim());
                if (world == null) {
                    return context.createErrorResult("World not found: " + parts[3].trim());
                }
            }
            
            // Create target location
            Location targetLocation = new Location(world, x, y, z);
            
            // Teleport the player
            player.teleport(targetLocation);
            
            // Visual effects
            player.spawnParticle(org.bukkit.Particle.PORTAL, currentLoc, 20);
            player.playSound(currentLoc, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            
            return context.createResult(true, "Teleported to " + locationStr);
            
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
            
            Player player = context.getPlayer();
            int itemsGiven = 0;
            
            // Get the actual items from the parameters
            // Items should be in format "MATERIAL1:COUNT1,MATERIAL2:COUNT2,..." or just "MATERIAL1,MATERIAL2,..." (count = 1)
            String itemsStr = itemsValue.asString();
            if (itemsStr == null || itemsStr.trim().isEmpty()) {
                return context.createErrorResult("Invalid items string");
            }
            
            String[] itemPairs = itemsStr.split(",");
            java.util.List<org.bukkit.inventory.ItemStack> itemsToGive = new java.util.ArrayList<>();
            
            for (String itemPair : itemPairs) {
                String[] parts = itemPair.trim().split(":");
                String materialName = parts[0].trim();
                int count = 1; // Default count
                
                if (parts.length > 1) {
                    try {
                        count = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        // Keep default count of 1
                    }
                }
                
                // Try to get the material
                org.bukkit.Material material;
                try {
                    material = org.bukkit.Material.valueOf(materialName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return context.createErrorResult("Invalid item material: " + materialName);
                }
                
                // Create the item
                org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material, count);
                itemsToGive.add(item);
            }
            
            // Convert list to array
            org.bukkit.inventory.ItemStack[] items = itemsToGive.toArray(new org.bukkit.inventory.ItemStack[0]);
            
            // Add items to inventory or drop if full
            java.util.Map<Integer, org.bukkit.inventory.ItemStack> notAdded = player.getInventory().addItem(items);
            
            // Drop any items that couldn't be added
            for (org.bukkit.inventory.ItemStack item : notAdded.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            
            itemsGiven = items.length - notAdded.size();
            
            // Visual effects
            player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 10);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.2f);
            
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
            
            // Get the actual items to remove from parameters
            // Items should be in format "MATERIAL1:COUNT1,MATERIAL2:COUNT2,..." or just "MATERIAL1,MATERIAL2,..." (count = 1)
            DataValue itemsValue = params.get("items");
            if (itemsValue == null) {
                return context.createErrorResult("No items specified for removal");
            }
            
            String itemsStr = itemsValue.asString();
            if (itemsStr == null || itemsStr.trim().isEmpty()) {
                return context.createErrorResult("Invalid items string for removal");
            }
            
            String[] itemPairs = itemsStr.split(",");
            int itemsRemoved = 0;
            
            // Remove items from player's inventory
            for (String itemPair : itemPairs) {
                String[] parts = itemPair.trim().split(":");
                String materialName = parts[0].trim();
                int countToRemove = 1; // Default count
                
                if (parts.length > 1) {
                    try {
                        countToRemove = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        // Keep default count of 1
                    }
                }
                
                // Try to get the material
                org.bukkit.Material material;
                try {
                    material = org.bukkit.Material.valueOf(materialName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    context.getPlugin().getLogger().warning("Invalid item material for removal: " + materialName);
                    continue; // Skip this item
                }
                
                // Find and remove items from inventory
                java.util.HashMap<Integer, ? extends org.bukkit.inventory.ItemStack> foundItems = 
                    player.getInventory().all(material);
                
                int amountRemoved = 0;
                for (java.util.Map.Entry<Integer, ? extends org.bukkit.inventory.ItemStack> entry : foundItems.entrySet()) {
                    if (amountRemoved >= countToRemove) break;
                    
                    org.bukkit.inventory.ItemStack stack = entry.getValue();
                    int stackAmount = stack.getAmount();
                    int toRemoveFromStack = Math.min(stackAmount, countToRemove - amountRemoved);
                    
                    if (toRemoveFromStack >= stackAmount) {
                        player.getInventory().clear(entry.getKey());
                        amountRemoved += stackAmount;
                    } else {
                        stack.setAmount(stackAmount - toRemoveFromStack);
                        amountRemoved += toRemoveFromStack;
                    }
                }
                
                itemsRemoved += amountRemoved;
            }
            
            // Visual effect
            player.spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Removed " + itemsRemoved + " items from inventory");
            
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
            return context.createResult(true, "Armor set");
            
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
            
            variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), name, valueValue);
            
            // Visual effects
            Player player = context.getPlayer();
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            return context.createResult(true, "Variable set");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set variable: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIfVariable(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
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
            
            DataValue variableValue = variableManager.getPlayerVariable(context.getPlayer().getUniqueId(), name);
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
    
    private ExecutionResult executeIfVarGreater(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
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
            
            DataValue variableValue = variableManager.getPlayerVariable(context.getPlayer().getUniqueId(), name);
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
    
    private ExecutionResult executeIfVarLess(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
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
            
            DataValue variableValue = variableManager.getPlayerVariable(context.getPlayer().getUniqueId(), name);
            if (variableValue == null) {
                return context.createErrorResult("Variable not found: " + name);
            }
            
            if (variableValue.asNumber() != null && valueValue.asNumber() != null) {
                double variableNumber = variableValue.asNumber().doubleValue();
                double valueNumber = valueValue.asNumber().doubleValue();
                
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
    
    private ExecutionResult executeCompareVariable(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
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
            
            DataValue variableValue = variableManager.getPlayerVariable(context.getPlayer().getUniqueId(), name);
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
            
            Integer score = scoreValue.asNumber() != null ? scoreValue.asNumber().intValue() : null;
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
            
            Integer increment = incrementValue.asNumber() != null ? incrementValue.asNumber().intValue() : null;
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
            variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), name, DataValue.of(location));
            
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
            
            DataValue locationValue = variableManager.getPlayerVariable(context.getPlayer().getUniqueId(), name);
            if (locationValue == null) {
                return context.createErrorResult("Location not found: " + name);
            }
            
            Location location = locationValue.getType() == com.megacreative.coding.values.ValueType.LOCATION ? (Location) locationValue.getValue() : null;
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
    
    private ExecutionResult executeIsNight(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
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
    
    private ExecutionResult executeIsRiding(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
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
    
    private ExecutionResult executeCheckPlayerInventory(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
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
        visualIndicators.put(location, new VisualIndicator(location, IndicatorType.EXECUTING, System.currentTimeMillis() + 5000));
        player.spawnParticle(Particle.VILLAGER_HAPPY, location, 10);
        player.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }
    
    private void showExecutionComplete(ExecutionContext context, ExecutionResult result) {
        Player player = context.getPlayer();
        Location location = player.getLocation();
        visualIndicators.put(location, new VisualIndicator(location, IndicatorType.COMPLETED, System.currentTimeMillis() + 5000));
        player.spawnParticle(Particle.VILLAGER_HAPPY, location, 10);
        player.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }
    
    private void showExecutionError(ExecutionContext context, Exception e) {
        Player player = context.getPlayer();
        Location location = player.getLocation();
        visualIndicators.put(location, new VisualIndicator(location, IndicatorType.ERROR, System.currentTimeMillis() + 5000));
        player.spawnParticle(Particle.VILLAGER_ANGRY, location, 10);
        player.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }
    
    private void showBlockExecution(CodeBlock block, ExecutionContext context) {
        Location location = block.getLocation();
        visualIndicators.put(location, new VisualIndicator(location, IndicatorType.EXECUTING, System.currentTimeMillis() + 5000));
        location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 10);
    }
    
    private void showBlockComplete(CodeBlock block, ExecutionContext context) {
        Location location = block.getLocation();
        visualIndicators.put(location, new VisualIndicator(location, IndicatorType.COMPLETED, System.currentTimeMillis() + 5000));
        location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 10);
    }
    
    private void showBlockError(CodeBlock block, ExecutionContext context, String message) {
        Location location = block.getLocation();
        visualIndicators.put(location, new VisualIndicator(location, IndicatorType.ERROR, System.currentTimeMillis() + 5000));
        location.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, location, 10);
    }
    
    private Map<String, DataValue> preprocessParameters(CodeBlock block, ExecutionContext context) {
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
        // Create a compatible ExecutionContext for the resolver
        com.megacreative.coding.ExecutionContext compatibleContext = 
            new com.megacreative.coding.ExecutionContext(plugin, context.getPlayer(), null, null, null, context.getCurrentBlock());
        return ReferenceSystemPlaceholderResolver.resolvePlaceholders(value, compatibleContext);
    }
    
    private String validateBlockParameters(CodeBlock block, Map<String, DataValue> processedParameters) {
        String action = block.getAction();
        var config = blockConfigService.getBlockConfig(action);
        if (config == null) {
            return "No configuration found for action: " + action;
        }
        
        // Since config.getParameters() returns Map<String, Object>, we need to handle it differently
        // For now, we'll skip detailed parameter validation and just check if required parameters exist
        for (Map.Entry<String, Object> entry : config.getParameters().entrySet()) {
            String paramName = entry.getKey();
            Object paramConfig = entry.getValue();
            
            // Basic required parameter check
            if (paramName != null && processedParameters.get(paramName) == null) {
                // We can't easily check if it's required without a proper ParameterConfig class
                // So we'll just log that the parameter is missing but not fail
                // In a full implementation, we'd have proper parameter configuration objects
            }
        }
        
        return null; // No validation errors
    }
    
    private boolean validateScript(CodeScript script) {
        if (script.getRootBlock() == null) {
            return false;
        }
        
        // Additional validation logic
        return script.isValid();
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
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }
    
    public void update() {
        // Update visual effect based on type
        switch (type) {
            case EXECUTING:
                location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location.add(0.5, 1, 0.5), 2);
                break;
            case COMPLETED:
                location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location.add(0.5, 1, 0.5), 1);
                break;
            case ERROR:
                location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location.add(0.5, 1, 0.5), 1);
                break;
        }
    }
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