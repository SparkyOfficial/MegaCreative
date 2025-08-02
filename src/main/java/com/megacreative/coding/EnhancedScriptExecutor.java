package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.blocks.BlockFactory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Улучшенный исполнитель скриптов с использованием новой архитектуры блоков.
 * Использует BlockFactory для получения блоков действий и условий.
 */
public class EnhancedScriptExecutor {
    
    private final MegaCreative plugin;
    
    public EnhancedScriptExecutor(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Выполняет скрипт с указанным контекстом.
     * @param script Скрипт для выполнения
     * @param context Контекст выполнения
     * @param triggerAction Действие-триггер
     */
    public void execute(CodeScript script, ExecutionContext context, String triggerAction) {
        Player player = context.getPlayer();
        if (player != null && plugin.getScriptDebugger().isDebugEnabled(player)) {
            plugin.getScriptDebugger().onScriptStart(player, script);
        }
        
        CodeBlock root = script.getRootBlock();
        if (root.getMaterial() == Material.DIAMOND_BLOCK && root.getAction().equals(triggerAction)) {
            CodeBlock nextBlock = root.getNextBlock();
            if (nextBlock != null) {
                // Находим локацию первого блока ОДИН РАЗ при запуске скрипта
                Location firstBlockLocation = findBlockLocation(nextBlock);
                // Создаем контекст с локацией первого блока
                ExecutionContext startContext = context.withCurrentBlock(nextBlock, firstBlockLocation);
                // Запускаем выполнение с уже известной локацией
                processBlock(nextBlock, startContext);
            }
        }

        if (player != null && plugin.getScriptDebugger().isDebugEnabled(player)) {
            plugin.getScriptDebugger().onScriptEnd(player, script);
        }
    }
    
    /**
     * Обрабатывает блок с использованием новой системы.
     * @param block Блок для обработки
     * @param context Контекст выполнения
     */
    public void processBlock(CodeBlock block, ExecutionContext context) {
        if (block == null) return;

        // 1. Используем локацию из контекста (уже передана из предыдущего вызова)
        Location blockLocation = context.getBlockLocation();
        
        // 2. Создаем контекст для текущего шага
        ExecutionContext currentContext = context.withCurrentBlock(block, blockLocation);

        // 3. Отладка
        if (currentContext.getPlayer() != null && plugin.getScriptDebugger().isDebugEnabled(currentContext.getPlayer())) {
            plugin.getScriptDebugger().onBlockExecute(currentContext.getPlayer(), block, blockLocation != null ? blockLocation : currentContext.getPlayer().getLocation());
        }

        // 4. Логика выполнения с использованием новой системы
        String actionName = block.getAction();
        
        // Пытаемся получить действие из новой системы
        BlockAction action = BlockFactory.getAction(actionName);
        if (action != null) {
            try {
                action.execute(currentContext);
            } catch (Exception e) {
                Player player = currentContext.getPlayer();
                if (player != null) {
                    player.sendMessage("§cОшибка при выполнении действия '" + actionName + "': " + e.getMessage());
                }
                plugin.getLogger().warning("Ошибка при выполнении действия '" + actionName + "': " + e.getMessage());
            }
        } 
        // Если действие не найдено в новой системе, проверяем условия
        else if (isConditionBlock(block.getMaterial())) {
            handleCondition(block, currentContext);
        }
        // Если ничего не найдено, логируем это
        else {
            Player player = currentContext.getPlayer();
            if (player != null) {
                player.sendMessage("§cДействие '" + actionName + "' не реализовано");
            }
        }

        // 5. Переход к следующему блоку с оптимизацией
        CodeBlock nextBlock = block.getNextBlock();
        if (nextBlock != null) {
            // Находим локацию СЛЕДУЮЩЕГО блока ОДИН РАЗ
            Location nextBlockLocation = findBlockLocation(nextBlock);
            // Создаем новый контекст для следующего шага с уже известной локацией
            ExecutionContext nextContext = currentContext.withCurrentBlock(nextBlock, nextBlockLocation);
            // Рекурсивно обрабатываем следующий блок с уже известной локацией
            processBlock(nextBlock, nextContext);
        }
    }
    
    /**
     * Обрабатывает условие с использованием новой системы.
     * @param block Блок условия
     * @param context Контекст выполнения
     */
    private void handleCondition(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        String conditionName = block.getAction();
        
        // Пытаемся получить условие из новой системы
        BlockCondition condition = BlockFactory.getCondition(conditionName);
        if (condition != null) {
            try {
                boolean result = condition.evaluate(context);
                
                // Если условие истинно, выполняем дочерние блоки
                if (result) {
                    for (CodeBlock child : block.getChildren()) {
                        Location childLocation = findBlockLocation(child);
                        ExecutionContext childContext = context.withCurrentBlock(child, childLocation);
                        processBlock(child, childContext);
                    }
                }
                
            } catch (Exception e) {
                player.sendMessage("§cОшибка при выполнении условия '" + conditionName + "': " + e.getMessage());
                plugin.getLogger().warning("Ошибка при выполнении условия '" + conditionName + "': " + e.getMessage());
            }
        } else {
            // Fallback для нереализованных условий
            player.sendMessage("§cУсловие '" + conditionName + "' не реализовано");
        }
    }
    
    /**
     * Проверяет, является ли блок условием.
     * @param material Материал блока
     * @return true, если блок является условием
     */
    private boolean isConditionBlock(Material material) {
        return material == Material.REDSTONE_BLOCK || 
               material == Material.EMERALD_BLOCK ||
               material == Material.LAPIS_BLOCK;
    }
    
    /**
     * Находит локацию блока в мире.
     * @param block Блок для поиска
     * @return Локация блока или null, если не найдена
     */
    private Location findBlockLocation(CodeBlock block) {
        // TODO: Реализовать поиск локации блока
        // Пока возвращаем null - это будет реализовано позже
        return null;
    }
} 