package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.actions.*;
import com.megacreative.coding.actions.HandleEventAction;
import com.megacreative.coding.actions.TriggerEventAction;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.conditions.IsOpCondition;
import com.megacreative.coding.conditions.CompareVariableCondition;
import com.megacreative.coding.conditions.WorldTimeCondition;
import com.megacreative.coding.conditions.IsNearBlockCondition;
import com.megacreative.coding.conditions.MobNearCondition;
import com.megacreative.coding.conditions.PlayerGameModeCondition;
// Removed duplicate condition imports - using unified CompareVariableCondition
import com.megacreative.coding.conditions.IsBlockTypeCondition;
import com.megacreative.coding.conditions.IsPlayerHoldingCondition;
import com.megacreative.coding.conditions.IsNearEntityCondition;
import com.megacreative.coding.conditions.HasArmorCondition;
import com.megacreative.coding.conditions.IsNightCondition;
import com.megacreative.coding.conditions.IsRidingCondition;
import com.megacreative.coding.actions.PlayerHealthCondition;
import com.megacreative.coding.actions.HasItemCondition;
import com.megacreative.coding.actions.HasPermissionCondition;
import com.megacreative.coding.actions.IsInWorldCondition;
import com.megacreative.coding.actions.WaitAction;
import com.megacreative.coding.actions.GiveItemsAction;
import com.megacreative.coding.actions.SpawnEntityAction;
import com.megacreative.coding.actions.RandomNumberAction;
import com.megacreative.coding.actions.PlayParticleEffectAction;
import com.megacreative.coding.actions.RemoveItemsAction;
import com.megacreative.coding.actions.SetArmorAction;
import com.megacreative.coding.monitoring.ScriptPerformanceMonitor;
import com.megacreative.coding.monitoring.model.ExecutionTracker;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * "Движок" для выполнения скриптов.
 * Использует паттерн "Стратегия" для выполнения действий.
 * Интегрирован с системой мониторинга производительности для контроля лимитов.
 */
public class ScriptExecutor {
    private final MegaCreative plugin;
    private final Map<String, BlockAction> actionRegistry = new HashMap<>();
    private final Map<String, BlockCondition> conditionRegistry = new HashMap<>();
    private final Map<String, Location> blockLocationCache = new HashMap<>();
    
    // Performance monitoring and limits
    private final Map<UUID, Integer> playerExecutionCounts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> scriptStartTimes = new ConcurrentHashMap<>();
    
    // Configuration-based limits (loaded from config.yml)
    private final int maxExecutionTime; // Maximum execution time in ms
    private final int maxScriptSize; // Maximum script size in blocks
    private final int maxRecursionDepth; // Maximum recursion depth for processBlock
    private final int maxIterations; // Maximum iterations for loops
    private final int maxConcurrentScripts; // Per player

    public ScriptExecutor(MegaCreative plugin) {
        this.plugin = plugin;
        
        // Load performance limits from config
        this.maxExecutionTime = plugin.getConfig().getInt("coding.max_execution_time", 5000);
        this.maxScriptSize = plugin.getConfig().getInt("coding.max_script_size", 100);
        this.maxRecursionDepth = plugin.getConfig().getInt("coding.max_recursion_depth", 50);
        this.maxConcurrentScripts = plugin.getConfig().getInt("coding.max_concurrent_scripts", 20);
        this.maxIterations = plugin.getConfig().getInt("coding.performance.max_iterations", 1000);
        
        registerActions();
        registerConditions();
    }

    private void registerActions() {
        // Базовые действия
        actionRegistry.put("sendMessage", new SendMessageAction());
        actionRegistry.put("teleport", new TeleportAction());
        actionRegistry.put("giveItem", new GiveItemAction());
        actionRegistry.put("setVar", new SetVarAction());
        actionRegistry.put("broadcast", new BroadcastAction());
        actionRegistry.put("spawnMob", new SpawnMobAction());
        
        // Новое действие для обработки входа игрока
        actionRegistry.put("playerEntry", new PlayerEntryAction());
        
        // Математические операции
        actionRegistry.put("addVar", new AddVarAction());
        actionRegistry.put("subVar", new SubVarAction());
        actionRegistry.put("mulVar", new MulVarAction());
        actionRegistry.put("divVar", new DivVarAction());
        
        // Действия с миром
        actionRegistry.put("playSound", new PlaySoundAction());
        actionRegistry.put("effect", new EffectAction());
        actionRegistry.put("command", new CommandAction());
        actionRegistry.put("healPlayer", new HealPlayerAction());
        actionRegistry.put("setGameMode", new SetGameModeAction());
        
        // Действия с временем и погодой
        actionRegistry.put("setTime", new SetTimeAction());
        actionRegistry.put("setWeather", new SetWeatherAction());
        
        // Действия с блоками и взрывами
        actionRegistry.put("explosion", new ExplosionAction());
        actionRegistry.put("setBlock", new SetBlockAction());
        
        // Получение данных
        actionRegistry.put("getVar", new GetVarAction());
        actionRegistry.put("getPlayerName", new GetPlayerNameAction());
        
        // Глобальные переменные игрока
        actionRegistry.put("setGlobalVar", new SetGlobalVariableAction());
        actionRegistry.put("getGlobalVar", new GetGlobalVariableAction());
        
        // Серверные переменные
        actionRegistry.put("setServerVar", new SetServerVariableAction());
        actionRegistry.put("getServerVar", new GetServerVariableAction());
        
        // Действие ожидания
        actionRegistry.put("wait", new WaitAction());
        
        // Новые действия с виртуальными инвентарями
        actionRegistry.put("giveItems", new GiveItemsAction());
        actionRegistry.put("spawnEntity", new SpawnEntityAction());
        actionRegistry.put("removeItems", new RemoveItemsAction());
        actionRegistry.put("setArmor", new SetArmorAction());
        
        // Действия, которые были в конфигурации, но не зарегистрированы
        actionRegistry.put("randomNumber", new RandomNumberAction());
        actionRegistry.put("playParticle", new PlayParticleEffectAction());
        
        // Повторяющийся триггер
        actionRegistry.put("repeatTrigger", new RepeatTriggerAction());
        
        // Циклы и функции
        actionRegistry.put("repeat", new RepeatAction());
        actionRegistry.put("callFunction", new CallFunctionAction());
        actionRegistry.put("saveFunction", new SaveFunctionAction());
        
        // Advanced loop control
        actionRegistry.put("asyncLoop", new com.megacreative.coding.actions.advanced.AsyncLoopControl());
        
        // Custom event system actions
        CustomEventManager eventManager = plugin.getServiceRegistry().getCustomEventManager();
        if (eventManager != null) {
            actionRegistry.put("handleEvent", new HandleEventAction(eventManager));
            actionRegistry.put("triggerEvent", new TriggerEventAction(eventManager));
        }
    }

    private void registerConditions() {
        // Базовые условия
        conditionRegistry.put("isOp", new IsOpCondition());
        conditionRegistry.put("compareVariable", new CompareVariableCondition());
        conditionRegistry.put("worldTime", new WorldTimeCondition());
        
        // Пространственные условия
        conditionRegistry.put("isNearBlock", new IsNearBlockCondition());
        conditionRegistry.put("mobNear", new MobNearCondition());
        
        // Условия игрока
        conditionRegistry.put("playerGameMode", new PlayerGameModeCondition());
        conditionRegistry.put("playerHealth", new PlayerHealthCondition());
        
        // Условия проверки
        conditionRegistry.put("hasItem", new HasItemCondition());
        conditionRegistry.put("hasPermission", new HasPermissionCondition());
        conditionRegistry.put("isInWorld", new IsInWorldCondition());
        
        // Variable comparison conditions (unified under compareVariable)
        // Legacy support: map old condition names to CompareVariableCondition
        conditionRegistry.put("ifVarEquals", new CompareVariableCondition());
        conditionRegistry.put("ifVarGreater", new CompareVariableCondition());
        conditionRegistry.put("ifVarLess", new CompareVariableCondition());
        
        // Условие для проверки типа блока
        conditionRegistry.put("isBlockType", new IsBlockTypeCondition());
        
        // Новые условия с виртуальными инвентарями
        conditionRegistry.put("isPlayerHolding", new IsPlayerHoldingCondition());
        conditionRegistry.put("isNearEntity", new IsNearEntityCondition());
        conditionRegistry.put("hasArmor", new HasArmorCondition());
        conditionRegistry.put("isNight", new IsNightCondition());
        conditionRegistry.put("isRiding", new IsRidingCondition());
    }

    public void execute(CodeScript script, ExecutionContext context, String triggerAction) {
        Player player = context.getPlayer();
        UUID playerId = player != null ? player.getUniqueId() : null;
        
        // Check if player has reached script execution limits
        if (playerId != null && !canExecuteScript(playerId, script)) {
            return;
        }
        
        // Start performance tracking for the entire script
        ExecutionTracker scriptTracker = null;
        if (playerId != null) {
            scriptStartTimes.put(playerId, System.currentTimeMillis());
            playerExecutionCounts.put(playerId, playerExecutionCounts.getOrDefault(playerId, 0) + 1);
            
            ScriptPerformanceMonitor monitor = plugin.getScriptPerformanceMonitor();
            if (monitor != null) {
                scriptTracker = monitor.startTracking(player, script.getId().toString(), "script_execution");
            }
        }
        
        try {
            if (player != null && plugin.getScriptDebugger().isDebugEnabled(player)) {
                plugin.getScriptDebugger().onScriptStart(player, script);
            }
            
            // Execute the script starting from the root block
            if (script.getRootBlock() != null) {
                processBlock(script.getRootBlock(), context, 0);
            }
            
            if (player != null && plugin.getScriptDebugger().isDebugEnabled(player)) {
                plugin.getScriptDebugger().onScriptEnd(player, script);
            }
        } finally {
            // Clean up performance tracking
            if (scriptTracker != null) {
                scriptTracker.close();
            }
            
            if (playerId != null) {
                // Reset execution count after script completion
                playerExecutionCounts.remove(playerId);
                scriptStartTimes.remove(playerId);
            }
        }
    }
    
    // ЭТОТ МЕТОД - НАШ НОВЫЙ ДВИЖОК С КОНТРОЛЕМ ПРОИЗВОДИТЕЛЬНОСТИ
    public void processBlock(CodeBlock block, ExecutionContext context) {
        processBlock(block, context, 0);
    }
    
    /**
     * Enhanced processBlock with performance monitoring and limits
     * @param block CodeBlock to execute
     * @param context Execution context
     * @param recursionDepth Current recursion depth for stack overflow prevention
     */
    public void processBlock(CodeBlock block, ExecutionContext context, int recursionDepth) {
        if (block == null) return;
        
        Player player = context.getPlayer();
        UUID playerId = player != null ? player.getUniqueId() : null;
        
        // 1. Performance and safety checks
        if (!performSafetyChecks(playerId, recursionDepth)) {
            return;
        }

        // 2. Используем локацию из контекста (уже передана из предыдущего вызова)
        Location blockLocation = context.getBlockLocation();
        
        // 3. Создаем контекст для текущего шага
        ExecutionContext currentContext = context.withCurrentBlock(block, blockLocation);
        
        // 4. Start performance tracking for this block
        ExecutionTracker blockTracker = null;
        if (playerId != null) {
            ScriptPerformanceMonitor monitor = plugin.getScriptPerformanceMonitor();
            if (monitor != null) {
                blockTracker = monitor.startTracking(player, block.getId().toString(), block.getAction());
            }
        }
        
        try {
            // 5. Отладка
            if (currentContext.getPlayer() != null && plugin.getScriptDebugger().isDebugEnabled(currentContext.getPlayer())) {
                plugin.getScriptDebugger().onBlockExecute(currentContext.getPlayer(), block, blockLocation != null ? blockLocation : currentContext.getPlayer().getLocation());
            }

            // 6. Логика выполнения (вся магия здесь)
            BlockAction action = actionRegistry.get(block.getAction());
            if (action != null) {
                action.execute(currentContext);
            } 
            // Если у нас нет зарегистрированного действия (например, для условий), обрабатываем отдельно
            else if (isConditionBlock(block.getMaterial())) {
                handleCondition(block, currentContext, recursionDepth);
            }
            // Если действие не найдено, логируем это
            else {
                if (player != null) {
                    player.sendMessage("§cДействие '" + block.getAction() + "' не реализовано");
                }
                if (blockTracker != null) {
                    blockTracker.markError("Action not implemented: " + block.getAction());
                }
            }

            // 7. Переход к следующему блоку с контролем глубины рекурсии
            CodeBlock nextBlock = block.getNextBlock();
            if (nextBlock != null) {
                // Находим локацию СЛЕДУЮЩЕГО блока ОДИН РАЗ
                Location nextBlockLocation = findBlockLocation(nextBlock);
                // Создаем новый контекст для следующего шага с уже известной локацией
                ExecutionContext nextContext = currentContext.withCurrentBlock(nextBlock, nextBlockLocation);
                // Рекурсивно обрабатываем следующий блок с увеличенной глубиной рекурсии
                processBlock(nextBlock, nextContext, recursionDepth + 1);
            }
            
        } catch (Exception e) {
            // Handle execution errors
            if (player != null) {
                player.sendMessage("§c[MegaCreative] Error executing block: " + e.getMessage());
            }
            
            if (blockTracker != null) {
                blockTracker.markError(e.getMessage());
            }
            
            // Log for debugging
            plugin.getLogger().warning("Error executing block " + block.getAction() + ": " + e.getMessage());
            
        } finally {
            // Close performance tracker
            if (blockTracker != null) {
                blockTracker.close();
            }
        }
    }
    
    // Обработка условий с контролем производительности
    private void handleCondition(CodeBlock block, ExecutionContext context) {
        handleCondition(block, context, 0);
    }
    
    private void handleCondition(CodeBlock block, ExecutionContext context, int recursionDepth) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // Используем систему условий
        BlockCondition condition = conditionRegistry.get(block.getAction());
        boolean result = false;
        
        if (condition != null) {
            result = condition.evaluate(context);
        } else {
            // Fallback для нереализованных условий
            String action = block.getAction();
            switch (action) {
                case "isOp":
                    result = player.isOp();
                    break;
                case "isInWorld":
                    DataValue worldNameValue = block.getParameter("world");
                    if (worldNameValue != null) {
                        result = player.getWorld().getName().equals(worldNameValue.asString());
                    }
                    break;
                case "hasItem":
                    DataValue itemNameValue = block.getParameter("item");
                    if (itemNameValue != null) {
                        result = player.getInventory().contains(org.bukkit.Material.valueOf(itemNameValue.asString().toUpperCase()));
                    }
                    break;
                case "hasPermission":
                    DataValue permissionValue = block.getParameter("permission");
                    if (permissionValue != null) {
                        result = player.hasPermission(permissionValue.asString());
                    }
                    break;
                // Добавьте другие условия по необходимости
            }
        }
        
        // Отладка результата условия
        if (plugin.getScriptDebugger().isDebugEnabled(player)) {
            plugin.getScriptDebugger().onConditionResult(player, block, result);
        }
        
        if (result) {
            for (CodeBlock child : block.getChildren()) {
                processBlock(child, context, recursionDepth + 1);
            }
        } else {
            // Ищем блок "Иначе" (END_STONE)
            CodeBlock next = block.getNextBlock();
            while (next != null) {
                if (next.getMaterial() == Material.END_STONE) {
                    for (CodeBlock child : next.getChildren()) {
                        processBlock(child, context, recursionDepth + 1);
                    }
                    break; 
                }
                next = next.getNextBlock();
            }
        }
    }
    


    private boolean isConditionBlock(Material material) {
        return material == Material.OAK_PLANKS || material == Material.OBSIDIAN || 
               material == Material.REDSTONE_BLOCK || material == Material.BRICKS;
    }

    /**
     * Находит локацию блока в мире по его объекту CodeBlock.
     * Использует кэширование для оптимизации производительности.
     * @return Location блока или null, если не найден.
     */
    private Location findBlockLocation(CodeBlock block) {
        if (block == null) return null;
        
        // Проверяем кэш сначала
        String blockId = block.getId().toString();
        if (blockLocationCache.containsKey(blockId)) {
            return blockLocationCache.get(blockId);
        }
        
        // Если нет в кэше, ищем в мире
        Map<Location, CodeBlock> allBlocks = plugin.getBlockPlacementHandler().getBlockCodeBlocks();
        for (Map.Entry<Location, CodeBlock> entry : allBlocks.entrySet()) {
            if (entry.getValue().getId().equals(blockId)) {
                Location location = entry.getKey();
                // Сохраняем в кэш
                blockLocationCache.put(blockId, location);
                return location;
            }
        }
        return null;
    }
    
    /**
     * Очищает кэш локаций блоков
     */
    public void clearLocationCache() {
        blockLocationCache.clear();
    }
    
    /**
     * Удаляет локацию блока из кэша
     */
    public void removeFromLocationCache(String blockId) {
        blockLocationCache.remove(blockId);
    }
    
    /**
     * Performs safety checks before executing a block
     * @param playerId Player UUID
     * @param recursionDepth Current recursion depth
     * @return true if execution can continue, false if limits exceeded
     */
    private boolean performSafetyChecks(UUID playerId, int recursionDepth) {
        // Check recursion depth limit
        if (recursionDepth >= maxRecursionDepth) {
            if (playerId != null) {
                Player player = plugin.getServer().getPlayer(playerId);
                if (player != null) {
                    player.sendMessage("§c[MegaCreative] Script stopped: Maximum recursion depth exceeded (" + maxRecursionDepth + ")");
                }
            }
            throw new ScriptExecutionException("Maximum recursion depth exceeded");
        }
        
        // Check execution time limit
        if (playerId != null) {
            Long startTime = scriptStartTimes.get(playerId);
            if (startTime != null) {
                long executionTime = System.currentTimeMillis() - startTime;
                if (executionTime > maxExecutionTime) {
                    Player player = plugin.getServer().getPlayer(playerId);
                    if (player != null) {
                        player.sendMessage("§c[MegaCreative] Script stopped: Maximum execution time exceeded (" + maxExecutionTime + "ms)");
                    }
                    throw new ScriptExecutionException("Maximum execution time exceeded");
                }
            }
        }
        
        return true;
    }
    
    /**
     * Checks if a player can execute a script based on performance limits
     * @param playerId Player UUID
     * @param script Script to execute
     * @return true if player can execute the script
     */
    private boolean canExecuteScript(UUID playerId, CodeScript script) {
        Player player = plugin.getServer().getPlayer(playerId);
        
        // Check if player is already running too many scripts
        Integer currentExecutions = playerExecutionCounts.get(playerId);
        if (currentExecutions != null && currentExecutions >= maxConcurrentScripts) {
            if (player != null) {
                player.sendMessage("§c[MegaCreative] Too many scripts running simultaneously. Please wait.");
            }
            return false;
        }
        
        // Check script size limit
        int scriptSize = countBlocksInScript(script.getRootBlock());
        if (scriptSize > maxScriptSize) {
            if (player != null) {
                player.sendMessage("§c[MegaCreative] Script too large (" + scriptSize + " blocks, max: " + maxScriptSize + ")");
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * Recursively counts all blocks in a script
     * @param block Root block to start counting from
     * @return Total number of blocks in the script
     */
    private int countBlocksInScript(CodeBlock block) {
        if (block == null) return 0;
        
        int count = 1; // Count current block
        
        // Count children
        for (CodeBlock child : block.getChildren()) {
            count += countBlocksInScript(child);
        }
        
        // Count next block
        if (block.getNextBlock() != null) {
            count += countBlocksInScript(block.getNextBlock());
        }
        
        return count;
    }
    
    /**
     * Gets the maximum execution time from configuration
     */
    public int getMaxExecutionTime() {
        return maxExecutionTime;
    }
    
    /**
     * Gets the maximum script size from configuration
     */
    public int getMaxScriptSize() {
        return maxScriptSize;
    }
    
    /**
     * Cancels a running script by its ID
     * @param scriptId The ID of the script to cancel
     */
    public void cancelScript(String scriptId) {
        // TODO: Implement script cancellation logic
        plugin.getLogger().info("Requested cancellation of script: " + scriptId);
    }
    
    /**
     * Shuts down the script executor and cleans up resources
     */
    public void shutdown() {
        // Stop all repeating tasks
        plugin.getLogger().info("Shutting down ScriptExecutor...");
        // Additional cleanup can be added here
    }
    
    /**
     * Exception thrown when script execution limits are exceeded
     */
    public static class ScriptExecutionException extends RuntimeException {
        public ScriptExecutionException(String message) {
            super(message);
        }
    }
}
