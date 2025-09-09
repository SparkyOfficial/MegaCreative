package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DefaultScriptEngine implements ScriptEngine {
    
    private final MegaCreative plugin;
    private final VariableManager variableManager;
    private final VisualDebugger debugger;
    private final BlockConfigService blockConfigService;
    private final ActionFactory actionFactory;
    private final ConditionFactory conditionFactory;
    
    private final Map<String, ExecutionContext> activeExecutions = new ConcurrentHashMap<>();
    private static final int MAX_RECURSION_DEPTH = 100;

    public DefaultScriptEngine(MegaCreative plugin, VariableManager variableManager, VisualDebugger debugger,
                               BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.variableManager = variableManager;
        this.debugger = debugger;
        this.blockConfigService = blockConfigService;
        // Передаем DependencyContainer, если он у вас есть, или создаем новые
        this.actionFactory = new ActionFactory(plugin.getDependencyContainer());
        this.conditionFactory = new ConditionFactory();
    }
    
    public void initialize() {
        // Initialize the script engine with any required setup
        // This method can be used to register built-in actions and conditions
    }
    
    public int getActionCount() {
        // Return the number of registered actions
        return 0; // Placeholder
    }
    
    public int getConditionCount() {
        // Return the number of registered conditions
        return 0; // Placeholder
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, String trigger) {
        if (script == null || !script.isEnabled() || script.getRootBlock() == null) {
            return CompletableFuture.completedFuture(ExecutionResult.success("Script is invalid or disabled."));
        }

        String executionId = UUID.randomUUID().toString();
        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .creativeWorld(plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld()))
            .currentBlock(script.getRootBlock())
            .build();
        
        activeExecutions.put(executionId, context);

        return CompletableFuture.supplyAsync(() -> {
            try {
                if (debugger.isDebugging(player)) {
                    debugger.onScriptStart(player, script);
                }
                return processBlock(script.getRootBlock(), context, 0);
            } catch (Exception e) {
                return ExecutionResult.error("Script execution failed: " + e.getMessage(), e);
            } finally {
                if (debugger.isDebugging(player)) {
                    debugger.onScriptEnd(player, script);
                }
                activeExecutions.remove(executionId);
            }
        });
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeBlock(CodeBlock block, Player player, String trigger) {
        if (block == null) {
            return CompletableFuture.completedFuture(ExecutionResult.success("Block is null."));
        }

        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .creativeWorld(plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld()))
            .currentBlock(block)
            .build();

        return CompletableFuture.supplyAsync(() -> {
            try {
                return processBlock(block, context, 0);
            } catch (Exception e) {
                return ExecutionResult.error("Block execution failed: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public CompletableFuture<ExecutionResult> executeBlockChain(CodeBlock startBlock, Player player, String trigger) {
        if (startBlock == null) {
            return CompletableFuture.completedFuture(ExecutionResult.success("Start block is null."));
        }

        ExecutionContext context = new ExecutionContext.Builder()
            .plugin(plugin)
            .player(player)
            .creativeWorld(plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld()))
            .currentBlock(startBlock)
            .build();

        return CompletableFuture.supplyAsync(() -> {
            try {
                return processBlock(startBlock, context, 0);
            } catch (Exception e) {
                return ExecutionResult.error("Block chain execution failed: " + e.getMessage(), e);
            }
        });
    }

    private ExecutionResult processBlock(CodeBlock block, ExecutionContext context, int recursionDepth) {
        if (block == null || context.isCancelled()) {
            return ExecutionResult.success("End of chain or cancelled.");
        }
        if (recursionDepth > MAX_RECURSION_DEPTH) {
            return ExecutionResult.error("Max recursion depth exceeded.");
        }
        if (debugger.isDebugging(context.getPlayer())) {
            debugger.onBlockExecute(context.getPlayer(), block, context.getBlockLocation());
        }

        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(block.getAction());
        if (config == null) {
            return ExecutionResult.error("Unknown block action ID: " + block.getAction());
        }
        
        String blockType = config.getType();
        
        // --- ОСНОВНАЯ ЛОГИКА ---
        switch (blockType) {
            case "EVENT":
            case "ACTION":
                BlockAction action = actionFactory.createAction(block.getAction());
                if (action == null) {
                    return ExecutionResult.error("Action handler not found for: " + block.getAction());
                }
                ExecutionResult result = action.execute(block, context);
                // Если действие прошло успешно, переходим к следующему блоку
                if (result.isSuccess()) {
                    return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                }
                return result; // Если была ошибка, останавливаемся
                
            case "CONDITION":
                BlockCondition condition = conditionFactory.createCondition(block.getAction());
                if (condition == null) {
                    return ExecutionResult.error("Condition handler not found for: " + block.getAction());
                }
                boolean conditionResult = condition.evaluate(block, context);
                context.setLastConditionResult(conditionResult);
                if (debugger.isDebugging(context.getPlayer())) {
                    debugger.onConditionResult(context.getPlayer(), block, conditionResult);
                }
                // Сохраняем результат условия для последующего использования в блоках CONTROL
                return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                
            case "CONTROL":
                // Здесь будет логика для IF/ELSE, LOOP и т.д.
                if (block.getAction().equals("conditionalBranch")) {
                    if (context.getLastConditionResult()) {
                        // Результат последнего условия был TRUE, выполняем дочерние блоки
                        if (!block.getChildren().isEmpty()) {
                            // Выполняем первую дочернюю ветку
                            ExecutionResult childResult = processBlock(block.getChildren().get(0), context, recursionDepth + 1);
                            // Если дочерняя ветка завершилась ошибкой, прерываемся
                            if (!childResult.isSuccess()) return childResult;
                        }
                    }
                    // Независимо от результата, после IF мы идем к следующему блоку в ОСНОВНОЙ цепи
                    return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                } else if (block.getAction().equals("else")) {
                    // Обработка блока ELSE
                    if (!context.getLastConditionResult()) {
                        // Предыдущее условие было FALSE, выполняем блок ELSE
                        if (!block.getChildren().isEmpty()) {
                            // Выполняем первую дочернюю ветку (тело else)
                            ExecutionResult childResult = processBlock(block.getChildren().get(0), context, recursionDepth + 1);
                            // Если дочерняя ветка завершилась ошибкой, прерываемся
                            if (!childResult.isSuccess()) return childResult;
                        }
                    }
                    // После ELSE мы идем к следующему блоку в ОСНОВНОЙ цепи
                    return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                }
                // Для других CONTROL блоков просто переходим к следующему блоку
                return processBlock(block.getNextBlock(), context, recursionDepth + 1);
                
            case "FUNCTION":
                // Обработка функций - это следующий большой шаг.
                // Пока что просто переходим к следующему блоку.
                return processBlock(block.getNextBlock(), context, recursionDepth + 1);

            default:
                return ExecutionResult.error("Unsupported block type: " + blockType);
        }
    }
    
    @Override
    public void registerAction(BlockType type, BlockAction action) {
        // Implementation for registering actions
        // This would typically involve adding to an internal registry
    }

    @Override
    public void registerCondition(BlockType type, BlockCondition condition) {
        // Implementation for registering conditions
        // This would typically involve adding to an internal registry
    }
    
    @Override
    public BlockType getBlockType(Material material, String actionName) {
        // Implementation for getting block type
        // This would typically involve looking up in a configuration or registry
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(actionName);
        if (config != null) {
            String type = config.getType();
            if ("EVENT".equals(type)) return BlockType.EVENT;
            if ("ACTION".equals(type)) return BlockType.ACTION;
            if ("CONDITION".equals(type)) return BlockType.CONDITION;
            if ("CONTROL".equals(type)) return BlockType.CONTROL;
            if ("FUNCTION".equals(type)) return BlockType.FUNCTION;
        }
        return null;
    }
    
    // Остальные методы интерфейса (getVariableManager, getDebugger, stopExecution...)
    
    @Override
    public VariableManager getVariableManager() {
        return variableManager;
    }
    
    @Override
    public VisualDebugger getDebugger() {
        return debugger;
    }
    
    @Override
    public boolean pauseExecution(String executionId) {
        ExecutionContext context = activeExecutions.get(executionId);
        if (context != null) {
            context.setPaused(true);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean resumeExecution(String executionId) {
        ExecutionContext context = activeExecutions.get(executionId);
        if (context != null && context.isPaused()) {
            context.setPaused(false);
            synchronized (context) {
                context.notifyAll();
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean stepExecution(String executionId) {
        ExecutionContext context = activeExecutions.get(executionId);
        if (context != null && context.isPaused()) {
            context.setStepping(true);
            return resumeExecution(executionId);
        }
        return false;
    }
    
    @Override
    public boolean stopExecution(String executionId) {
        ExecutionContext context = activeExecutions.remove(executionId);
        if (context != null) {
            context.setCancelled(true);
            return true;
        }
        return false;
    }
}