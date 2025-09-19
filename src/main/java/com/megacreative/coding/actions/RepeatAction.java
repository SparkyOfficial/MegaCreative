package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class RepeatAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        // Get times parameter from the container configuration
        int times = getTimesFromContainer(block, context);
        
        if (times <= 0) {
            return ExecutionResult.error("Repeat count must be greater than 0");
        }
        
        if (times > 1000) {
            return ExecutionResult.error("Maximum repeat count is 1000");
        }
        
        // Получаем следующий блок для выполнения
        CodeBlock nextBlock = block.getNextBlock();
        if (nextBlock == null) {
            return ExecutionResult.error("No block to repeat");
        }
        
        // Получаем ScriptEngine из ServiceRegistry
        ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
        if (scriptEngine == null) {
            return ExecutionResult.error("Failed to get ScriptEngine");
        }
        
        // Запускаем выполнение в асинхронном контексте
        executeRepeatLoop(times, nextBlock, context, scriptEngine, player);
        
        return ExecutionResult.success("Repeat action started");
    }
    
    /**
     * Executes the repeat loop asynchronously
     */
    private void executeRepeatLoop(int times, CodeBlock nextBlock, ExecutionContext context, 
                                  ScriptEngine scriptEngine, Player player) {
        CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i < times; i++) {
                    final int currentIndex = i; // Создаем effectively final переменную
                    
                    // Check if we should break or continue before executing iteration
                    if (shouldBreak(context, player, currentIndex + 1)) {
                        break;
                    }
                    
                    if (shouldContinue(context, player, currentIndex + 1)) {
                        continue;
                    }
                    
                    // Create a new context for each iteration
                    ExecutionContext loopContext = createLoopContext(context, nextBlock, currentIndex, times);
                    
                    // Execute the block chain for this iteration
                    if (!executeIteration(scriptEngine, nextBlock, player, loopContext, currentIndex)) {
                        break;
                    }
                    
                    // Check if we should break or continue after executing iteration
                    if (shouldBreak(context, player, currentIndex + 1)) {
                        break;
                    }
                    
                    if (shouldContinue(context, player, currentIndex + 1)) {
                        continue;
                    }
                }
                
                player.sendMessage("§a🔄 Цикл выполнен " + times + " раз");
            } catch (Exception e) {
                player.sendMessage("§cОшибка при выполнении цикла: " + e.getMessage());
            }
        });
    }
    
    /**
     * Checks if the loop should break
     */
    private boolean shouldBreak(ExecutionContext context, Player player, int iteration) {
        if (context.hasBreakFlag()) {
            context.clearBreakFlag();
            player.sendMessage("§aRepeat loop terminated by break statement at iteration " + iteration);
            return true;
        }
        return false;
    }
    
    /**
     * Checks if the loop should continue
     */
    private boolean shouldContinue(ExecutionContext context, Player player, int iteration) {
        if (context.hasContinueFlag()) {
            context.clearContinueFlag();
            player.sendMessage("§aSkipping iteration " + iteration + " due to continue statement");
            return true;
        }
        return false;
    }
    
    /**
     * Creates a new execution context for a loop iteration
     */
    private ExecutionContext createLoopContext(ExecutionContext context, CodeBlock nextBlock, 
                                             int currentIndex, int times) {
        ExecutionContext loopContext = context.withCurrentBlock(nextBlock, context.getBlockLocation());
        loopContext.setVariable("loopIndex", currentIndex + 1);
        loopContext.setVariable("loopCount", times);
        return loopContext;
    }
    
    /**
     * Executes a single iteration of the loop
     */
    private boolean executeIteration(ScriptEngine scriptEngine, CodeBlock nextBlock, 
                                   Player player, ExecutionContext loopContext, int currentIndex) {
        try {
            ExecutionResult result = scriptEngine.executeBlockChain(nextBlock, player, "repeat_loop")
                .exceptionally(throwable -> {
                    player.sendMessage("§cОшибка в итерации " + (currentIndex + 1) + ": " + throwable.getMessage());
                    return null;
                })
                .join(); // Ждем завершения итерации
            return true;
        } catch (Exception e) {
            player.sendMessage("§cОшибка в итерации " + (currentIndex + 1) + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets times parameter from the container configuration
     */
    private int getTimesFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get times from the times slot
                Integer timesSlot = slotResolver.apply("times_slot");
                if (timesSlot != null) {
                    ItemStack timesItem = block.getConfigItem(timesSlot);
                    if (timesItem != null && timesItem.hasItemMeta()) {
                        // Extract times from item
                        return getTimesFromItem(timesItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting times parameter from container in RepeatAction: " + e.getMessage());
        }
        
        return 1; // Default to 1 iteration
    }
    
    /**
     * Extracts times from an item
     */
    private int getTimesFromItem(ItemStack item) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse times from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Math.max(1, Integer.parseInt(cleanName));
                }
            }
            
            // Fallback to item amount
            return Math.max(1, item.getAmount());
        } catch (Exception e) {
            return 1; // Default to 1 iteration
        }
    }
}