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
     * Executes a single block with type-specific handling
     * @param block The block to execute
     * @param context The execution context
     * @return The execution result
     */
    private ExecutionResult executeBlock(CodeBlock block, ExecutionContext context) {
        try {
            String action = block.getAction();
            Map<String, DataValue> parameters = block.getParameters();
            
            // Preprocess parameters
            Map<String, DataValue> processedParameters = preprocessParameters(block, context);
            
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
                case "isSneaking":
                    result = executeIsSneaking(block, processedParameters, context);
                    break;
                default:
                    result = context.createErrorResult("Unknown action: " + action);
                    break;
            }
            
            return result;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Unexpected error executing block: " + e.getMessage());
            return context.createErrorResult("Unexpected error executing block: " + e.getMessage(), e);
        }
    }
    
    /**
     * Shows visual indicator for block execution start
     */
    private void showBlockExecution(CodeBlock block, ExecutionContext context) {
        Location location = block.getLocation();
        if (location != null) {
            location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 1);
        }
    }
    
    /**
     * Shows visual indicator for block execution completion
     */
    private void showBlockComplete(CodeBlock block, ExecutionContext context) {
        Location location = block.getLocation();
        if (location != null) {
            location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1.5f);
        }
    }
    
    /**
     * Shows visual indicator for block execution error
     */
    private void showBlockError(CodeBlock block, ExecutionContext context, String errorMessage) {
        Location location = block.getLocation();
        if (location != null) {
            location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 1);
        }
    }
    
    /**
     * Shows visual indicator for script execution start
     */
    private void showExecutionStart(ExecutionContext context) {
        Location playerLocation = context.getPlayer().getLocation();
        playerLocation.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, playerLocation, 1);
    }
    
    /**
     * Shows visual indicator for script execution completion
     */
    private void showExecutionComplete(ExecutionContext context, ExecutionResult result) {
        Location playerLocation = context.getPlayer().getLocation();
        playerLocation.getWorld().playSound(playerLocation, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1.5f);
    }
    
    /**
     * Shows visual indicator for script execution error
     */
    private void showExecutionError(ExecutionContext context, Exception e) {
        Location playerLocation = context.getPlayer().getLocation();
        playerLocation.getWorld().spawnParticle(Particle.SMOKE_NORMAL, playerLocation, 1);
    }
    
    /**
     * Starts a visual updater task to manage visual indicators
     */
    private void startVisualUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                visualIndicators.values().forEach(indicator -> {
                    if (indicator.isExpired()) {
                        indicator.getLocation().getWorld().spawnParticle(Particle.SMOKE_NORMAL, indicator.getLocation(), 1);
                        visualIndicators.remove(indicator.getLocation());
                    } else {
                        indicator.getLocation().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, indicator.getLocation(), 1);
                    }
                });
            }
        }.runTaskTimer(plugin, 0, 20);
    }
    
    /**
     * Validates the script before execution
     * @param script The script to validate
     * @return true if the script is valid, false otherwise
     */
    private boolean validateScript(CodeScript script) {
        // Add validation logic here
        return true;
    }
    
    /**
     * Validates the parameters of a block before execution
     * @param block The block to validate
     * @param parameters The processed parameters of the block
     * @return A validation error message if validation fails, null otherwise
     */
    private String validateBlockParameters(CodeBlock block, Map<String, DataValue> parameters) {
        // Add validation logic here
        return null;
    }
    
    /**
     * Preprocesses the parameters of a block
     * @param block The block to preprocess
     * @param context The execution context
     * @return The processed parameters
     */
    private Map<String, DataValue> preprocessParameters(CodeBlock block, ExecutionContext context) {
        Map<String, DataValue> processedParams = new HashMap<>();
        
        if (block.getParameters() != null) {
            var parameterResolver = new ParameterResolver(context);
            
            for (Map.Entry<String, DataValue> entry : block.getParameters().entrySet()) {
                String key = entry.getKey();
                DataValue value = entry.getValue();
                
                // Resolve placeholders in string values
                DataValue resolvedValue = parameterResolver.resolve(context, value);
                processedParams.put(key, resolvedValue);
            }
        }
        
        return processedParams;
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
            
            // Resolve placeholders
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
            DataValue entityTypeValue = params.get("entityType");
            if (entityTypeValue == null) {
                return context.createErrorResult("No entity type specified");
            }
            
            String entityTypeName = entityTypeValue.asString();
            if (entityTypeName == null || entityTypeName.trim().isEmpty()) {
                return context.createErrorResult("Entity type cannot be empty");
            }
            
            // Try to get the entity type
            org.bukkit.entity.EntityType entityType;
            try {
                entityType = org.bukkit.entity.EntityType.valueOf(entityTypeName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid entity type: " + entityTypeName);
            }
            
            // Spawn the entity at player location
            Player player = context.getPlayer();
            Location location = player.getLocation();
            org.bukkit.entity.Entity entity = location.getWorld().spawnEntity(location, entityType);
            
            // Visual effects
            player.spawnParticle(Particle.EXPLOSION_NORMAL, location, 5);
            player.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            
            return context.createResult(true, "Spawned " + entityType.name().toLowerCase().replace("_", " "));
            
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
            
            Player player = context.getPlayer();
            int itemsRemoved = 0;
            
            // Get the actual items from the parameters
            // Items should be in format "MATERIAL1:COUNT1,MATERIAL2:COUNT2,..." or just "MATERIAL1,MATERIAL2,..." (count = 1)
            String itemsStr = itemsValue.asString();
            if (itemsStr == null || itemsStr.trim().isEmpty()) {
                return context.createErrorResult("Invalid items string");
            }
            
            String[] itemPairs = itemsStr.split(",");
            
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
                
                // Remove the items
                org.bukkit.inventory.ItemStack itemToRemove = new org.bukkit.inventory.ItemStack(material, count);
                java.util.Map<Integer, org.bukkit.inventory.ItemStack> notRemoved = player.getInventory().removeItem(itemToRemove);
                
                // Count how many were actually removed
                itemsRemoved += count - notRemoved.size();
            }
            
            // Visual effects
            player.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
            
            return context.createResult(true, "Removed " + itemsRemoved + " items");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to remove items: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSetArmor(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue armorValue = params.get("armor");
            if (armorValue == null) {
                return context.createErrorResult("No armor specified");
            }
            
            String armorStr = armorValue.asString();
            if (armorStr == null || armorStr.trim().isEmpty()) {
                return context.createErrorResult("Armor cannot be empty");
            }
            
            Player player = context.getPlayer();
            org.bukkit.inventory.PlayerInventory inventory = player.getInventory();
            
            // Parse armor string in format "helmet,chestplate,leggings,boots"
            String[] armorParts = armorStr.split(",");
            if (armorParts.length != 4) {
                return context.createErrorResult("Invalid armor format. Expected: helmet,chestplate,leggings,boots");
            }
            
            // Set armor pieces
            for (int i = 0; i < 4; i++) {
                String materialName = armorParts[i].trim();
                if (!materialName.isEmpty()) {
                    try {
                        org.bukkit.Material material = org.bukkit.Material.valueOf(materialName.toUpperCase());
                        org.bukkit.inventory.ItemStack armorItem = new org.bukkit.inventory.ItemStack(material, 1);
                        switch (i) {
                            case 0: // Helmet
                                inventory.setHelmet(armorItem);
                                break;
                            case 1: // Chestplate
                                inventory.setChestplate(armorItem);
                                break;
                            case 2: // Leggings
                                inventory.setLeggings(armorItem);
                                break;
                            case 3: // Boots
                                inventory.setBoots(armorItem);
                                break;
                        }
                    } catch (IllegalArgumentException e) {
                        return context.createErrorResult("Invalid armor material: " + materialName);
                    }
                }
            }
            
            // Visual effects
            player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 2, 0), 10);
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
            
            return context.createResult(true, "Armor set");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set armor: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSpawnMob(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue mobTypeValue = params.get("mobType");
            if (mobTypeValue == null) {
                return context.createErrorResult("No mob type specified");
            }
            
            String mobTypeName = mobTypeValue.asString();
            if (mobTypeName == null || mobTypeName.trim().isEmpty()) {
                return context.createErrorResult("Mob type cannot be empty");
            }
            
            // Try to get the mob type
            org.bukkit.entity.EntityType mobType;
            try {
                mobType = org.bukkit.entity.EntityType.valueOf(mobTypeName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid mob type: " + mobTypeName);
            }
            
            // Check if it's a living entity
            if (!org.bukkit.entity.LivingEntity.class.isAssignableFrom(mobType.getEntityClass())) {
                return context.createErrorResult("Entity type is not a living entity: " + mobTypeName);
            }
            
            // Spawn the mob at player location
            Player player = context.getPlayer();
            Location location = player.getLocation();
            org.bukkit.entity.Entity entity = location.getWorld().spawnEntity(location, mobType);
            
            // Visual effects
            player.spawnParticle(Particle.EXPLOSION_LARGE, location, 3);
            player.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
            
            return context.createResult(true, "Spawned " + mobType.name().toLowerCase().replace("_", " "));
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to spawn mob: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeHealPlayer(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue healAmountValue = params.get("healAmount");
            double healAmount = 20.0; // Default to full health
            
            if (healAmountValue != null) {
                try {
                    healAmount = healAmountValue.asNumber().doubleValue();
                } catch (NumberFormatException e) {
                    return context.createErrorResult("Invalid heal amount: " + healAmountValue.asString());
                }
            }
            
            Player player = context.getPlayer();
            
            // Heal the player
            org.bukkit.entity.Damageable damageable = (org.bukkit.entity.Damageable) player;
            double currentHealth = damageable.getHealth();
            double maxHealth = damageable.getMaxHealth();
            double newHealth = Math.min(maxHealth, currentHealth + healAmount);
            damageable.setHealth(newHealth);
            
            // Visual effects
            player.spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
            
            return context.createResult(true, "Healed player by " + healAmount + " health points");
            
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
                return context.createErrorResult("Game mode cannot be empty");
            }
            
            // Try to get the game mode
            org.bukkit.GameMode gameMode;
            try {
                gameMode = org.bukkit.GameMode.valueOf(gameModeName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid game mode: " + gameModeName);
            }
            
            // Set the game mode
            Player player = context.getPlayer();
            player.setGameMode(gameMode);
            
            // Visual effects
            player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 2, 0), 10);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
            
            return context.createResult(true, "Set game mode to " + gameMode.name().toLowerCase());
            
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
            
            long time;
            try {
                time = timeValue.asNumber().longValue();
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid time value: " + timeValue.asString());
            }
            
            // Set the time
            Player player = context.getPlayer();
            player.getWorld().setTime(time);
            
            // Visual effects
            player.spawnParticle(Particle.CLOUD, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            
            return context.createResult(true, "Set world time to " + time);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set time: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSetWeather(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue weatherValue = params.get("weather");
            if (weatherValue == null) {
                return context.createErrorResult("No weather specified");
            }
            
            String weatherStr = weatherValue.asString();
            if (weatherStr == null || weatherStr.trim().isEmpty()) {
                return context.createErrorResult("Weather cannot be empty");
            }
            
            Player player = context.getPlayer();
            org.bukkit.World world = player.getWorld();
            
            // Set weather based on input
            switch (weatherStr.toLowerCase()) {
                case "clear":
                case "sunny":
                    world.setStorm(false);
                    world.setThundering(false);
                    break;
                case "rain":
                case "storm":
                    world.setStorm(true);
                    world.setThundering(false);
                    break;
                case "thunder":
                case "thunderstorm":
                    world.setStorm(true);
                    world.setThundering(true);
                    break;
                default:
                    return context.createErrorResult("Invalid weather type: " + weatherStr);
            }
            
            // Visual effects
            player.spawnParticle(Particle.WATER_DROP, player.getLocation().add(0, 2, 0), 10);
            player.playSound(player.getLocation(), Sound.WEATHER_RAIN, 1.0f, 1.0f);
            
            return context.createResult(true, "Set weather to " + weatherStr);
            
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
                return context.createErrorResult("Sound cannot be empty");
            }
            
            // Try to get the sound
            Sound sound;
            try {
                sound = Sound.valueOf(soundName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return context.createErrorResult("Invalid sound: " + soundName);
            }
            
            Player player = context.getPlayer();
            Location location = player.getLocation();
            
            // Play the sound
            player.playSound(location, sound, 1.0f, 1.0f);
            
            // Visual effects
            player.spawnParticle(Particle.NOTE, location.add(0, 2, 0), 3);
            
            return context.createResult(true, "Played sound " + sound.name().toLowerCase());
            
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
                return context.createErrorResult("Effect cannot be empty");
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
            
            // Get duration and amplifier (optional)
            int duration = 600; // 30 seconds default
            int amplifier = 0; // Level 1 default
            
            DataValue durationValue = params.get("duration");
            if (durationValue != null) {
                try {
                    duration = durationValue.asNumber().intValue();
                } catch (NumberFormatException e) {
                    // Keep default duration
                }
            }
            
            DataValue amplifierValue = params.get("amplifier");
            if (amplifierValue != null) {
                try {
                    amplifier = amplifierValue.asNumber().intValue();
                } catch (NumberFormatException e) {
                    // Keep default amplifier
                }
            }
            
            Player player = context.getPlayer();
            
            // Apply the effect
            org.bukkit.potion.PotionEffect effect = new org.bukkit.potion.PotionEffect(effectType, duration, amplifier);
            player.addPotionEffect(effect);
            
            // Visual effects
            player.spawnParticle(Particle.SPELL_WITCH, player.getLocation().add(0, 2, 0), 10);
            player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.0f);
            
            return context.createResult(true, "Applied effect " + effectType.getName());
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to apply effect: " + e.getMessage(), e);
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
            
            // Execute the command
            boolean success = player.performCommand(command);
            
            // Visual effects
            player.spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Executed command: " + command);
            
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
            
            // Resolve placeholders
            message = resolvePlaceholders(message, context);
            
            // Broadcast the message
            org.bukkit.Bukkit.broadcastMessage(message);
            
            // Visual effects
            for (Player onlinePlayer : org.bukkit.Bukkit.getOnlinePlayers()) {
                onlinePlayer.spawnParticle(Particle.TOTEM, onlinePlayer.getLocation().add(0, 2, 0), 3);
            }
            
            return context.createResult(true, "Broadcast message");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to broadcast message: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSetVariable(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue varNameValue = params.get("varName");
            if (varNameValue == null) {
                return context.createErrorResult("No variable name specified");
            }
            
            String varName = varNameValue.asString();
            if (varName == null || varName.trim().isEmpty()) {
                return context.createErrorResult("Variable name cannot be empty");
            }
            
            DataValue varValue = params.get("varValue");
            if (varValue == null) {
                return context.createErrorResult("No variable value specified");
            }
            
            // Set the variable in the context
            context.setVariable(varName, varValue.getValue());
            
            // Visual effects
            Player player = context.getPlayer();
            player.spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Set variable " + varName);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set variable: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIfVariable(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue varNameValue = params.get("varName");
            if (varNameValue == null) {
                return context.createErrorResult("No variable name specified");
            }
            
            String varName = varNameValue.asString();
            if (varName == null || varName.trim().isEmpty()) {
                return context.createErrorResult("Variable name cannot be empty");
            }
            
            DataValue expectedValue = params.get("expectedValue");
            if (expectedValue == null) {
                return context.createErrorResult("No expected value specified");
            }
            
            // Get the actual variable value
            Object actualValue = context.getVariable(varName);
            if (actualValue == null) {
                actualValue = ""; // Default to empty string for comparison
            }
            
            // Compare values
            boolean conditionMet = actualValue.toString().equals(expectedValue.asString());
            
            // Set the result for else block handling
            context.setLastConditionResult(conditionMet);
            
            // Visual effects
            Player player = context.getPlayer();
            Particle particle = conditionMet ? Particle.VILLAGER_HAPPY : Particle.SMOKE_NORMAL;
            player.spawnParticle(particle, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Condition evaluated: " + conditionMet);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to evaluate condition: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIfVarGreater(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue varNameValue = params.get("varName");
            if (varNameValue == null) {
                return context.createErrorResult("No variable name specified");
            }
            
            String varName = varNameValue.asString();
            if (varName == null || varName.trim().isEmpty()) {
                return context.createErrorResult("Variable name cannot be empty");
            }
            
            DataValue thresholdValue = params.get("threshold");
            if (thresholdValue == null) {
                return context.createErrorResult("No threshold specified");
            }
            
            // Get the actual variable value
            Object actualValue = context.getVariable(varName);
            if (actualValue == null) {
                actualValue = 0; // Default to 0 for comparison
            }
            
            // Convert to numbers for comparison
            double actualNum, thresholdNum;
            try {
                actualNum = Double.parseDouble(actualValue.toString());
                thresholdNum = thresholdValue.asNumber().doubleValue();
            } catch (NumberFormatException e) {
                return context.createErrorResult("Cannot compare non-numeric values");
            }
            
            // Compare values
            boolean conditionMet = actualNum > thresholdNum;
            
            // Set the result for else block handling
            context.setLastConditionResult(conditionMet);
            
            // Visual effects
            Player player = context.getPlayer();
            Particle particle = conditionMet ? Particle.VILLAGER_HAPPY : Particle.SMOKE_NORMAL;
            player.spawnParticle(particle, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Condition evaluated: " + conditionMet);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to evaluate condition: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIfVarLess(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue varNameValue = params.get("varName");
            if (varNameValue == null) {
                return context.createErrorResult("No variable name specified");
            }
            
            String varName = varNameValue.asString();
            if (varName == null || varName.trim().isEmpty()) {
                return context.createErrorResult("Variable name cannot be empty");
            }
            
            DataValue thresholdValue = params.get("threshold");
            if (thresholdValue == null) {
                return context.createErrorResult("No threshold specified");
            }
            
            // Get the actual variable value
            Object actualValue = context.getVariable(varName);
            if (actualValue == null) {
                actualValue = 0; // Default to 0 for comparison
            }
            
            // Convert to numbers for comparison
            double actualNum, thresholdNum;
            try {
                actualNum = Double.parseDouble(actualValue.toString());
                thresholdNum = thresholdValue.asNumber().doubleValue();
            } catch (NumberFormatException e) {
                return context.createErrorResult("Cannot compare non-numeric values");
            }
            
            // Compare values
            boolean conditionMet = actualNum < thresholdNum;
            
            // Set the result for else block handling
            context.setLastConditionResult(conditionMet);
            
            // Visual effects
            Player player = context.getPlayer();
            Particle particle = conditionMet ? Particle.VILLAGER_HAPPY : Particle.SMOKE_NORMAL;
            player.spawnParticle(particle, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Condition evaluated: " + conditionMet);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to evaluate condition: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeCompareVariable(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue var1NameValue = params.get("var1Name");
            if (var1NameValue == null) {
                return context.createErrorResult("No first variable name specified");
            }
            
            String var1Name = var1NameValue.asString();
            if (var1Name == null || var1Name.trim().isEmpty()) {
                return context.createErrorResult("First variable name cannot be empty");
            }
            
            DataValue var2NameValue = params.get("var2Name");
            if (var2NameValue == null) {
                return context.createErrorResult("No second variable name specified");
            }
            
            String var2Name = var2NameValue.asString();
            if (var2Name == null || var2Name.trim().isEmpty()) {
                return context.createErrorResult("Second variable name cannot be empty");
            }
            
            DataValue operatorValue = params.get("operator");
            if (operatorValue == null) {
                return context.createErrorResult("No operator specified");
            }
            
            String operator = operatorValue.asString();
            if (operator == null || operator.trim().isEmpty()) {
                return context.createErrorResult("Operator cannot be empty");
            }
            
            // Get the actual variable values
            Object var1Value = context.getVariable(var1Name);
            Object var2Value = context.getVariable(var2Name);
            
            if (var1Value == null) var1Value = "";
            if (var2Value == null) var2Value = "";
            
            // Convert to numbers if possible
            Double num1 = null, num2 = null;
            try {
                num1 = Double.parseDouble(var1Value.toString());
                num2 = Double.parseDouble(var2Value.toString());
            } catch (NumberFormatException e) {
                // Not numeric, will do string comparison
            }
            
            // Perform comparison based on operator
            boolean conditionMet;
            switch (operator) {
                case "==":
                case "equals":
                    if (num1 != null && num2 != null) {
                        conditionMet = Math.abs(num1 - num2) < 0.0001; // Floating point comparison
                    } else {
                        conditionMet = var1Value.toString().equals(var2Value.toString());
                    }
                    break;
                case "!=":
                case "notEquals":
                    if (num1 != null && num2 != null) {
                        conditionMet = Math.abs(num1 - num2) >= 0.0001; // Floating point comparison
                    } else {
                        conditionMet = !var1Value.toString().equals(var2Value.toString());
                    }
                    break;
                case ">":
                case "greater":
                    if (num1 != null && num2 != null) {
                        conditionMet = num1 > num2;
                    } else {
                        conditionMet = var1Value.toString().compareTo(var2Value.toString()) > 0;
                    }
                    break;
                case "<":
                case "less":
                    if (num1 != null && num2 != null) {
                        conditionMet = num1 < num2;
                    } else {
                        conditionMet = var1Value.toString().compareTo(var2Value.toString()) < 0;
                    }
                    break;
                case ">=":
                case "greaterEquals":
                    if (num1 != null && num2 != null) {
                        conditionMet = num1 >= num2;
                    } else {
                        conditionMet = var1Value.toString().compareTo(var2Value.toString()) >= 0;
                    }
                    break;
                case "<=":
                case "lessEquals":
                    if (num1 != null && num2 != null) {
                        conditionMet = num1 <= num2;
                    } else {
                        conditionMet = var1Value.toString().compareTo(var2Value.toString()) <= 0;
                    }
                    break;
                default:
                    return context.createErrorResult("Invalid operator: " + operator);
            }
            
            // Set the result for else block handling
            context.setLastConditionResult(conditionMet);
            
            // Visual effects
            Player player = context.getPlayer();
            Particle particle = conditionMet ? Particle.VILLAGER_HAPPY : Particle.SMOKE_NORMAL;
            player.spawnParticle(particle, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Condition evaluated: " + conditionMet);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to evaluate condition: " + e.getMessage(), e);
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
            
            // Resolve placeholders
            message = resolvePlaceholders(message, context);
            
            // Send action bar message
            Player player = context.getPlayer();
            player.sendActionBar(message);
            
            // Visual effects
            player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 2, 0), 3);
            
            return context.createResult(true, "Sent action bar message");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to send action bar message: " + e.getMessage(), e);
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
            
            // Execute the command asynchronously
            org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    boolean success = org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), command);
                    plugin.getLogger().info("Async command executed: " + command + " (success: " + success + ")");
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to execute async command: " + command + " - " + e.getMessage());
                }
            });
            
            // Visual effects
            player.spawnParticle(Particle.SPELL_INSTANT, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Executing async command: " + command);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to execute async command: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeCreateScoreboard(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue scoreboardNameValue = params.get("scoreboardName");
            if (scoreboardNameValue == null) {
                return context.createErrorResult("No scoreboard name specified");
            }
            
            String scoreboardName = scoreboardNameValue.asString();
            if (scoreboardName == null || scoreboardName.trim().isEmpty()) {
                return context.createErrorResult("Scoreboard name cannot be empty");
            }
            
            Player player = context.getPlayer();
            
            // Create scoreboard
            org.bukkit.scoreboard.ScoreboardManager manager = org.bukkit.Bukkit.getScoreboardManager();
            org.bukkit.scoreboard.Scoreboard board = manager.getNewScoreboard();
            
            // Create objective
            org.bukkit.scoreboard.Objective objective = board.registerNewObjective(scoreboardName, "dummy", scoreboardName);
            objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
            
            // Set player's scoreboard
            player.setScoreboard(board);
            
            // Visual effects
            player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 2, 0), 10);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.5f);
            
            return context.createResult(true, "Created scoreboard: " + scoreboardName);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to create scoreboard: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSetScore(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue objectiveNameValue = params.get("objectiveName");
            if (objectiveNameValue == null) {
                return context.createErrorResult("No objective name specified");
            }
            
            String objectiveName = objectiveNameValue.asString();
            if (objectiveName == null || objectiveName.trim().isEmpty()) {
                return context.createErrorResult("Objective name cannot be empty");
            }
            
            DataValue scoreValue = params.get("score");
            if (scoreValue == null) {
                return context.createErrorResult("No score specified");
            }
            
            int score;
            try {
                score = scoreValue.asNumber().intValue();
            } catch (NumberFormatException e) {
                return context.createErrorResult("Invalid score value: " + scoreValue.asString());
            }
            
            Player player = context.getPlayer();
            org.bukkit.scoreboard.Scoreboard board = player.getScoreboard();
            
            // Get or create objective
            org.bukkit.scoreboard.Objective objective = board.getObjective(objectiveName);
            if (objective == null) {
                objective = board.registerNewObjective(objectiveName, "dummy", objectiveName);
                objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
            }
            
            // Set score
            org.bukkit.scoreboard.Score playerScore = objective.getScore(player.getName());
            playerScore.setScore(score);
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            
            return context.createResult(true, "Set score to " + score);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to set score: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIncrementScore(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue objectiveNameValue = params.get("objectiveName");
            if (objectiveNameValue == null) {
                return context.createErrorResult("No objective name specified");
            }
            
            String objectiveName = objectiveNameValue.asString();
            if (objectiveName == null || objectiveName.trim().isEmpty()) {
                return context.createErrorResult("Objective name cannot be empty");
            }
            
            DataValue incrementValue = params.get("increment");
            int increment = 1; // Default increment
            
            if (incrementValue != null) {
                try {
                    increment = incrementValue.asNumber().intValue();
                } catch (NumberFormatException e) {
                    // Keep default increment
                }
            }
            
            Player player = context.getPlayer();
            org.bukkit.scoreboard.Scoreboard board = player.getScoreboard();
            
            // Get or create objective
            org.bukkit.scoreboard.Objective objective = board.getObjective(objectiveName);
            if (objective == null) {
                objective = board.registerNewObjective(objectiveName, "dummy", objectiveName);
                objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
            }
            
            // Get current score and increment
            org.bukkit.scoreboard.Score playerScore = objective.getScore(player.getName());
            int currentScore = playerScore.getScore();
            int newScore = currentScore + increment;
            playerScore.setScore(newScore);
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            
            return context.createResult(true, "Incremented score by " + increment);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to increment score: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeCreateTeam(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue teamNameValue = params.get("teamName");
            if (teamNameValue == null) {
                return context.createErrorResult("No team name specified");
            }
            
            String teamName = teamNameValue.asString();
            if (teamName == null || teamName.trim().isEmpty()) {
                return context.createErrorResult("Team name cannot be empty");
            }
            
            Player player = context.getPlayer();
            org.bukkit.scoreboard.Scoreboard board = player.getScoreboard();
            
            // Create or get team
            org.bukkit.scoreboard.Team team = board.getTeam(teamName);
            if (team == null) {
                team = board.registerNewTeam(teamName);
            }
            
            // Set team properties if provided
            DataValue displayNameValue = params.get("displayName");
            if (displayNameValue != null) {
                team.setDisplayName(displayNameValue.asString());
            }
            
            DataValue prefixValue = params.get("prefix");
            if (prefixValue != null) {
                team.setPrefix(prefixValue.asString());
            }
            
            DataValue suffixValue = params.get("suffix");
            if (suffixValue != null) {
                team.setSuffix(suffixValue.asString());
            }
            
            // Add player to team
            team.addEntry(player.getName());
            
            // Visual effects
            player.spawnParticle(Particle.TOTEM, player.getLocation().add(0, 2, 0), 10);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.5f);
            
            return context.createResult(true, "Created team: " + teamName);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to create team: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeAddPlayerToTeam(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue teamNameValue = params.get("teamName");
            if (teamNameValue == null) {
                return context.createErrorResult("No team name specified");
            }
            
            String teamName = teamNameValue.asString();
            if (teamName == null || teamName.trim().isEmpty()) {
                return context.createErrorResult("Team name cannot be empty");
            }
            
            DataValue playerNameValue = params.get("playerName");
            if (playerNameValue == null) {
                return context.createErrorResult("No player name specified");
            }
            
            String playerName = playerNameValue.asString();
            if (playerName == null || playerName.trim().isEmpty()) {
                return context.createErrorResult("Player name cannot be empty");
            }
            
            Player player = context.getPlayer();
            org.bukkit.scoreboard.Scoreboard board = player.getScoreboard();
            
            // Get team
            org.bukkit.scoreboard.Team team = board.getTeam(teamName);
            if (team == null) {
                return context.createErrorResult("Team not found: " + teamName);
            }
            
            // Add player to team
            team.addEntry(playerName);
            
            // Visual effects
            player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 2, 0), 5);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            
            return context.createResult(true, "Added player to team: " + playerName);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to add player to team: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeSaveLocation(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue locationNameValue = params.get("locationName");
            if (locationNameValue == null) {
                return context.createErrorResult("No location name specified");
            }
            
            String locationName = locationNameValue.asString();
            if (locationName == null || locationName.trim().isEmpty()) {
                return context.createErrorResult("Location name cannot be empty");
            }
            
            Player player = context.getPlayer();
            Location location = player.getLocation();
            
            // Save location as a variable
            context.setVariable(locationName, location);
            
            // Visual effects
            player.spawnParticle(Particle.PORTAL, location, 10);
            player.playSound(location, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 1.0f);
            
            return context.createResult(true, "Location saved");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to save location: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeGetLocation(Map<String, DataValue> params, ExecutionContext context) {
        try {
            DataValue locationNameValue = params.get("locationName");
            if (locationNameValue == null) {
                return context.createErrorResult("No location name specified");
            }
            
            String locationName = locationNameValue.asString();
            if (locationName == null || locationName.trim().isEmpty()) {
                return context.createErrorResult("Location name cannot be empty");
            }
            
            // Get location from variable
            Object locationObj = context.getVariable(locationName);
            if (!(locationObj instanceof Location)) {
                return context.createErrorResult("Variable " + locationName + " is not a location");
            }
            
            Location location = (Location) locationObj;
            Player player = context.getPlayer();
            
            // Teleport player to location
            player.teleport(location);
            
            // Visual effects
            player.spawnParticle(Particle.PORTAL, location, 20);
            player.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            
            return context.createResult(true, "Location retrieved");
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to get location: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIsNight(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            long time = player.getWorld().getTime();
            
            // Night is from 13000 to 23000 ticks
            boolean isNight = time >= 13000 && time <= 23000;
            
            // Set the result for else block handling
            context.setLastConditionResult(isNight);
            
            // Visual effects
            Particle particle = isNight ? Particle.VILLAGER_HAPPY : Particle.SMOKE_NORMAL;
            player.spawnParticle(particle, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Condition evaluated: " + isNight);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to evaluate condition: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIsRiding(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            boolean isRiding = player.isInsideVehicle();
            
            // Set the result for else block handling
            context.setLastConditionResult(isRiding);
            
            // Visual effects
            Particle particle = isRiding ? Particle.VILLAGER_HAPPY : Particle.SMOKE_NORMAL;
            player.spawnParticle(particle, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Condition evaluated: " + isRiding);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to evaluate condition: " + e.getMessage(), e);
        }
    }
    
    private ExecutionResult executeIsSneaking(CodeBlock block, Map<String, DataValue> params, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            boolean isSneaking = player.isSneaking();
            
            // Set the result for else block handling
            context.setLastConditionResult(isSneaking);
            
            // Visual effects
            Particle particle = isSneaking ? Particle.VILLAGER_HAPPY : Particle.SMOKE_NORMAL;
            player.spawnParticle(particle, player.getLocation().add(0, 2, 0), 5);
            
            return context.createResult(true, "Condition evaluated: " + isSneaking);
            
        } catch (Exception e) {
            return context.createErrorResult("Failed to evaluate condition: " + e.getMessage(), e);
        }
    }
    
    // === UTILITY METHODS ===
    
    private String resolvePlaceholders(String text, ExecutionContext context) {
        if (text == null) return null;
        
        // Use the placeholder resolver if available
        if (context.getPlugin() != null) {
            try {
                ReferenceSystemPlaceholderResolver resolver = context.getPlugin().getServiceRegistry()
                    .getService(ReferenceSystemPlaceholderResolver.class);
                if (resolver != null) {
                    return resolver.resolvePlaceholders(text, context);
                }
            } catch (Exception e) {
                // Fall back to simple placeholder resolution
                context.getPlugin().getLogger().warning("Failed to resolve placeholders: " + e.getMessage());
            }
        }
        
        // Simple placeholder resolution as fallback
        return text.replace("{player}", context.getPlayer().getName())
                  .replace("{world}", context.getPlayer().getWorld().getName())
                  .replace("{x}", String.valueOf(context.getPlayer().getLocation().getX()))
                  .replace("{y}", String.valueOf(context.getPlayer().getLocation().getY()))
                  .replace("{z}", String.valueOf(context.getPlayer().getLocation().getZ()));
    }
    
    // === STATISTICS AND MONITORING ===
    
    private void updateExecutionStats(ExecutionContext context, ExecutionResult result) {
        String executionId = context.getExecutionId();
        ExecutionStats stats = executionStats.computeIfAbsent(executionId, k -> new ExecutionStats());
        
        stats.incrementExecutions();
        if (result.isSuccess()) {
            stats.incrementSuccesses();
        } else {
            stats.incrementFailures();
        }
        stats.addExecutionTime(result.getExecutionTime());
    }
    
    // === SHUTDOWN AND CLEANUP ===
    
    public void shutdown() {
        // Cancel all active executions
        for (ExecutionContext context : activeExecutions.values()) {
            context.setCancelled(true);
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