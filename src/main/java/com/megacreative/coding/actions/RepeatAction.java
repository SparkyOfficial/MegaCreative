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
        CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i < times; i++) {
                    final int currentIndex = i; // Создаем effectively final переменную
                    // Создаем новый контекст для каждой итерации
                    ExecutionContext loopContext = context.withCurrentBlock(nextBlock, context.getBlockLocation());
                    
                    // Check for break flag before executing iteration
                    if (context.hasBreakFlag()) {
                        context.clearBreakFlag();
                        player.sendMessage("§aRepeat loop terminated by break statement at iteration " + (currentIndex + 1));
                        break;
                    }
                    
                    // Check for continue flag
                    if (context.hasContinueFlag()) {
                        context.clearContinueFlag();
                        player.sendMessage("§aSkipping iteration " + (currentIndex + 1) + " due to continue statement");
                        continue;
                    }
                    
                    // Добавляем переменную с номером итерации
                    loopContext.setVariable("loopIndex", currentIndex + 1);
                    loopContext.setVariable("loopCount", times);
                    
                    // Выполняем блок синхронно в основном потоке
                    try {
                        ExecutionResult result = scriptEngine.executeBlockChain(nextBlock, player, "repeat_loop")
                            .exceptionally(throwable -> {
                                player.sendMessage("§cОшибка в итерации " + (currentIndex + 1) + ": " + throwable.getMessage());
                                return null;
                            })
                            .join(); // Ждем завершения итерации
                            
                        // Check for break flag after executing iteration
                        if (context.hasBreakFlag()) {
                            context.clearBreakFlag();
                            player.sendMessage("§aRepeat loop terminated by break statement at iteration " + (currentIndex + 1));
                            break;
                        }
                        
                        // Check for continue flag after executing iteration
                        if (context.hasContinueFlag()) {
                            context.clearContinueFlag();
                            player.sendMessage("§aContinuing to next iteration after iteration " + (currentIndex + 1));
                            continue;
                        }
                    } catch (Exception e) {
                        player.sendMessage("§cОшибка в итерации " + (currentIndex + 1) + ": " + e.getMessage());
                        break;
                    }
                }
                
                player.sendMessage("§a🔄 Цикл выполнен " + times + " раз");
            } catch (Exception e) {
                player.sendMessage("§cОшибка при выполнении цикла: " + e.getMessage());
            }
        });
        
        return ExecutionResult.success("Repeat action started");
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