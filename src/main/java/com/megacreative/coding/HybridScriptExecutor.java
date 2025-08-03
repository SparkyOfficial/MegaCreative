package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.blocks.BlockFactory;
import com.megacreative.coding.actions.*;
import com.megacreative.coding.conditions.IsOpCondition;
import com.megacreative.coding.conditions.CompareVariableCondition;
import com.megacreative.coding.conditions.WorldTimeCondition;
import com.megacreative.coding.conditions.IsNearBlockCondition;
import com.megacreative.coding.conditions.MobNearCondition;
import com.megacreative.coding.conditions.PlayerGameModeCondition;
import com.megacreative.coding.conditions.IfVarEqualsCondition;
import com.megacreative.coding.conditions.IfVarGreaterCondition;
import com.megacreative.coding.conditions.IfVarLessCondition;
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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * ГЛАВНЫЙ исполнитель скриптов - единственная точка входа для выполнения скриптов.
 * Поддерживает гибридную архитектуру с приоритетом новой системы над старой.
 * 
 * ПРИОРИТЕТ ВЫПОЛНЕНИЯ:
 * 1. НОВАЯ СИСТЕМА (BlockFactory) - блоки с аргументами и значениями
 * 2. СТАРАЯ СИСТЕМА (oldActionRegistry) - блоки обратной совместимости
 * 3. ОШИБКА - если блок не найден
 * 
 * ПЛАН МИГРАЦИИ:
 * - Постепенно переводить блоки из oldActionRegistry в BlockFactory
 * - Удалять блоки из старого реестра после успешной миграции
 * - Когда oldActionRegistry опустеет, переименовать в ScriptExecutor
 */
public class HybridScriptExecutor {
    
    private final MegaCreative plugin;
    private final Map<String, BlockAction> oldActionRegistry = new HashMap<>();
    private final Map<String, BlockCondition> oldConditionRegistry = new HashMap<>();
    
    public HybridScriptExecutor(MegaCreative plugin) {
        this.plugin = plugin;
        registerOldActions();
        registerOldConditions();
    }
    
    /**
     * Регистрирует старые действия для обратной совместимости.
     * 
     * МИГРИРОВАННЫЕ БЛОКИ УДАЛЕНЫ ИЗ СТАРОГО РЕЕСТРА:
     * ✅ sendMessage → SendMessageAction (blocks/actions/)
     * ✅ teleport → TeleportAction (blocks/actions/)
     * ✅ giveItem → GiveItemAction (blocks/actions/)
     * ✅ setVar → SetVarAction (blocks/actions/)
     * ✅ broadcast → BroadcastAction (blocks/actions/)
     * ✅ randomNumber → RandomNumberAction (blocks/actions/)
     * ✅ wait → WaitAction (blocks/actions/)
     * ✅ setBlock → SetBlockAction (blocks/actions/)
     * ✅ command → CommandAction (blocks/actions/)
     */
    private void registerOldActions() {
        // Базовые действия (мигрированы в новую систему)
        // oldActionRegistry.put("sendMessage", new SendMessageAction()); // МИГРИРОВАН
        // oldActionRegistry.put("teleport", new TeleportAction()); // МИГРИРОВАН
        // oldActionRegistry.put("giveItem", new GiveItemAction()); // МИГРИРОВАН
        // oldActionRegistry.put("setVar", new SetVarAction()); // МИГРИРОВАН
        // oldActionRegistry.put("broadcast", new BroadcastAction()); // МИГРИРОВАН
        // oldActionRegistry.put("randomNumber", new RandomNumberAction()); // МИГРИРОВАН
        // oldActionRegistry.put("wait", new WaitAction()); // МИГРИРОВАН
        // oldActionRegistry.put("setBlock", new SetBlockAction()); // МИГРИРОВАН
        
        // Оставшиеся блоки для миграции
        // oldActionRegistry.put("spawnMob", new SpawnMobAction()); // МИГРИРОВАН
        
        // Математические операции (мигрированы в новую систему)
        // oldActionRegistry.put("addVar", new AddVarAction()); // МИГРИРОВАН
        // oldActionRegistry.put("subVar", new SubVarAction()); // МИГРИРОВАН
        // oldActionRegistry.put("mulVar", new MulVarAction()); // МИГРИРОВАН
        // oldActionRegistry.put("divVar", new DivVarAction()); // МИГРИРОВАН
        
        // Действия с миром
        // oldActionRegistry.put("playSound", new PlaySoundAction()); // МИГРИРОВАН
        // oldActionRegistry.put("effect", new EffectAction()); // МИГРИРОВАН
        // oldActionRegistry.put("command", new CommandAction()); // МИГРИРОВАН
        // oldActionRegistry.put("healPlayer", new HealPlayerAction()); // МИГРИРОВАН
        // oldActionRegistry.put("setGameMode", new SetGameModeAction()); // МИГРИРОВАН
        
        // Действия с временем и погодой
        // oldActionRegistry.put("setTime", new SetTimeAction()); // МИГРИРОВАН
        // oldActionRegistry.put("setWeather", new SetWeatherAction()); // МИГРИРОВАН
        
        // Действия с блоками и взрывами
        // oldActionRegistry.put("explosion", new ExplosionAction()); // МИГРИРОВАН
        oldActionRegistry.put("setBlock", new SetBlockAction());
        
        // Получение данных
        // oldActionRegistry.put("getVar", new GetVarAction()); // МИГРИРОВАН
        // oldActionRegistry.put("getPlayerName", new GetPlayerNameAction()); // МИГРИРОВАН
        
        // Глобальные переменные игрока
        oldActionRegistry.put("setGlobalVar", new SetGlobalVariableAction());
        oldActionRegistry.put("getGlobalVar", new GetGlobalVariableAction());
        
        // Серверные переменные
        oldActionRegistry.put("setServerVar", new SetServerVariableAction());
        oldActionRegistry.put("getServerVar", new GetServerVariableAction());
        
        // Действие ожидания (мигрировано в новую систему)
        // oldActionRegistry.put("wait", new WaitAction()); // МИГРИРОВАН
        
        // Новые действия с виртуальными инвентарями
        oldActionRegistry.put("giveItems", new GiveItemsAction());
        oldActionRegistry.put("spawnEntity", new SpawnEntityAction());
        oldActionRegistry.put("removeItems", new RemoveItemsAction());
        oldActionRegistry.put("setArmor", new SetArmorAction());
        oldActionRegistry.put("playParticleEffect", new PlayParticleEffectAction());
        // oldActionRegistry.put("randomNumber", new RandomNumberAction()); // МИГРИРОВАН
        
        // Повторения
        oldActionRegistry.put("repeat", new RepeatAction());
        oldActionRegistry.put("repeatTrigger", new RepeatTriggerAction());
        
        // Функции
        oldActionRegistry.put("callFunction", new CallFunctionAction());
        oldActionRegistry.put("saveFunction", new SaveFunctionAction());
    }
    
    /**
     * Регистрирует старые условия для обратной совместимости.
     * 
     * МИГРИРОВАННЫЕ УСЛОВИЯ УДАЛЕНЫ ИЗ СТАРОГО РЕЕСТРА:
     * ✅ isOp → IsOpCondition (blocks/conditions/)
     * ✅ hasItem → HasItemCondition (blocks/conditions/)
     * ✅ ifVarEquals → IfVarEqualsCondition (blocks/conditions/)
     * ✅ playerHealth → PlayerHealthCondition (blocks/conditions/)
     */
    private void registerOldConditions() {
        // Базовые условия (мигрированы в новую систему)
        // oldConditionRegistry.put("isOp", new IsOpCondition()); // МИГРИРОВАН
        // oldConditionRegistry.put("hasItem", new HasItemCondition()); // МИГРИРОВАН
        oldConditionRegistry.put("hasPermission", new HasPermissionCondition());
        oldConditionRegistry.put("isInWorld", new IsInWorldCondition());
        // oldConditionRegistry.put("playerHealth", new PlayerHealthCondition()); // МИГРИРОВАН
        
        // Условия переменных (мигрированы в новую систему)
        // oldConditionRegistry.put("ifVarEquals", new IfVarEqualsCondition()); // МИГРИРОВАН
        oldConditionRegistry.put("ifVarGreater", new IfVarGreaterCondition());
        oldConditionRegistry.put("ifVarLess", new IfVarLessCondition());
        oldConditionRegistry.put("compareVariable", new CompareVariableCondition());
        
        // Условия мира
        oldConditionRegistry.put("worldTime", new WorldTimeCondition());
        oldConditionRegistry.put("isNearBlock", new IsNearBlockCondition());
        oldConditionRegistry.put("isBlockType", new IsBlockTypeCondition());
        oldConditionRegistry.put("isNight", new IsNightCondition());
        
        // Условия сущностей
        oldConditionRegistry.put("mobNear", new MobNearCondition());
        oldConditionRegistry.put("isNearEntity", new IsNearEntityCondition());
        oldConditionRegistry.put("isRiding", new IsRidingCondition());
        
        // Условия игрока
        oldConditionRegistry.put("playerGameMode", new PlayerGameModeCondition());
        oldConditionRegistry.put("isPlayerHolding", new IsPlayerHoldingCondition());
        oldConditionRegistry.put("hasArmor", new HasArmorCondition());
    }
    
    /**
     * Выполняет скрипт с поддержкой как старых, так и новых блоков.
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
                Location firstBlockLocation = findBlockLocation(nextBlock);
                ExecutionContext startContext = context.withCurrentBlock(nextBlock, firstBlockLocation);
                processBlock(nextBlock, startContext);
            }
        }

        if (player != null && plugin.getScriptDebugger().isDebugEnabled(player)) {
            plugin.getScriptDebugger().onScriptEnd(player, script);
        }
    }
    
    /**
     * Обрабатывает блок, используя гибридную систему.
     * Сначала пытается найти в новой системе, затем в старой.
     */
    public void processBlock(CodeBlock block, ExecutionContext context) {
        if (block == null) return;

        Location blockLocation = context.getBlockLocation();
        ExecutionContext currentContext = context.withCurrentBlock(block, blockLocation);

        // Отладка
        if (currentContext.getPlayer() != null && plugin.getScriptDebugger().isDebugEnabled(currentContext.getPlayer())) {
            plugin.getScriptDebugger().onBlockExecute(currentContext.getPlayer(), block, 
                blockLocation != null ? blockLocation : currentContext.getPlayer().getLocation());
        }

        String actionName = block.getAction();
        
        // 1. Пытаемся найти в НОВОЙ системе
        BlockAction newAction = BlockFactory.getAction(actionName);
        if (newAction != null) {
            try {
                newAction.execute(currentContext);
                if (currentContext.getPlayer() != null) {
                    currentContext.getPlayer().sendMessage("§a[НОВАЯ СИСТЕМА] Выполнено действие: " + actionName);
                }
            } catch (Exception e) {
                Player player = currentContext.getPlayer();
                if (player != null) {
                    player.sendMessage("§cОшибка в новом действии '" + actionName + "': " + e.getMessage());
                }
                plugin.getLogger().warning("Ошибка в новом действии '" + actionName + "': " + e.getMessage());
            }
        } 
        // 2. Если не найдено в новой системе, ищем в СТАРОЙ
        else if (oldActionRegistry.containsKey(actionName)) {
            try {
                BlockAction oldAction = oldActionRegistry.get(actionName);
                oldAction.execute(currentContext);
                if (currentContext.getPlayer() != null) {
                    currentContext.getPlayer().sendMessage("§e[СТАРАЯ СИСТЕМА] Выполнено действие: " + actionName);
                }
            } catch (Exception e) {
                Player player = currentContext.getPlayer();
                if (player != null) {
                    player.sendMessage("§cОшибка в старом действии '" + actionName + "': " + e.getMessage());
                }
                plugin.getLogger().warning("Ошибка в старом действии '" + actionName + "': " + e.getMessage());
            }
        }
        // 3. Проверяем условия
        else if (isConditionBlock(block.getMaterial())) {
            handleCondition(block, currentContext);
        }
        // 4. Если ничего не найдено
        else {
            Player player = currentContext.getPlayer();
            if (player != null) {
                player.sendMessage("§cДействие '" + actionName + "' не найдено ни в новой, ни в старой системе");
            }
        }

        // Переход к следующему блоку
        CodeBlock nextBlock = block.getNextBlock();
        if (nextBlock != null) {
            Location nextBlockLocation = findBlockLocation(nextBlock);
            ExecutionContext nextContext = currentContext.withCurrentBlock(nextBlock, nextBlockLocation);
            processBlock(nextBlock, nextContext);
        }
    }
    
    /**
     * Обрабатывает условие с гибридной системой и поддержкой IF/ELSE.
     */
    private void handleCondition(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        String conditionName = block.getAction();
        
        // Специальная обработка для ELSE блока
        if ("else".equals(conditionName)) {
            // ELSE блок выполняется только если он был вызван из IF/ELSE логики
            // Здесь мы просто выполняем дочерние блоки
            for (CodeBlock child : block.getChildren()) {
                Location childLocation = findBlockLocation(child);
                ExecutionContext childContext = context.withCurrentBlock(child, childLocation);
                processBlock(child, childContext);
            }
            if (player != null) {
                player.sendMessage("§b[IF/ELSE] Выполняется ELSE блок");
            }
            return;
        }
        
        // 1. Пытаемся найти в НОВОЙ системе
        BlockCondition newCondition = BlockFactory.getCondition(conditionName);
        if (newCondition != null) {
            try {
                boolean result = newCondition.evaluate(context);
                if (result) {
                    // Выполняем дочерние блоки IF
                    for (CodeBlock child : block.getChildren()) {
                        Location childLocation = findBlockLocation(child);
                        ExecutionContext childContext = context.withCurrentBlock(child, childLocation);
                        processBlock(child, childContext);
                    }
                } else {
                    // IF условие ложно - ищем и выполняем ELSE блок
                    handleElseBlock(block, context);
                }
                if (player != null) {
                    player.sendMessage("§a[НОВАЯ СИСТЕМА] Условие '" + conditionName + "' = " + result);
                }
            } catch (Exception e) {
                player.sendMessage("§cОшибка в новом условии '" + conditionName + "': " + e.getMessage());
            }
        } 
        // 2. Если не найдено в новой системе, ищем в СТАРОЙ
        else if (oldConditionRegistry.containsKey(conditionName)) {
            try {
                BlockCondition oldCondition = oldConditionRegistry.get(conditionName);
                boolean result = oldCondition.evaluate(context);
                if (result) {
                    // Выполняем дочерние блоки IF
                    for (CodeBlock child : block.getChildren()) {
                        Location childLocation = findBlockLocation(child);
                        ExecutionContext childContext = context.withCurrentBlock(child, childLocation);
                        processBlock(child, childContext);
                    }
                } else {
                    // IF условие ложно - ищем и выполняем ELSE блок
                    handleElseBlock(block, context);
                }
                if (player != null) {
                    player.sendMessage("§e[СТАРАЯ СИСТЕМА] Условие '" + conditionName + "' = " + result);
                }
            } catch (Exception e) {
                player.sendMessage("§cОшибка в старом условии '" + conditionName + "': " + e.getMessage());
            }
        } else {
            player.sendMessage("§cУсловие '" + conditionName + "' не найдено ни в новой, ни в старой системе");
        }
    }
    
    /**
     * Обрабатывает ELSE блок после ложного IF условия.
     */
    private void handleElseBlock(CodeBlock ifBlock, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // Ищем следующий блок после IF
        CodeBlock nextBlock = ifBlock.getNextBlock();
        if (nextBlock != null && "else".equals(nextBlock.getAction())) {
            // Нашли ELSE блок - выполняем его дочерние блоки
            for (CodeBlock child : nextBlock.getChildren()) {
                Location childLocation = findBlockLocation(child);
                ExecutionContext childContext = context.withCurrentBlock(child, childLocation);
                processBlock(child, childContext);
            }
            if (player != null) {
                player.sendMessage("§b[IF/ELSE] Выполняется ELSE блок");
            }
        } else {
            if (player != null) {
                player.sendMessage("§7[IF/ELSE] ELSE блок не найден");
            }
        }
    }
    
    /**
     * Проверяет, является ли блок условием.
     */
    private boolean isConditionBlock(Material material) {
        return material == Material.REDSTONE_BLOCK || 
               material == Material.EMERALD_BLOCK ||
               material == Material.LAPIS_BLOCK;
    }
    
    /**
     * Находит локацию блока в мире.
     */
    private Location findBlockLocation(CodeBlock block) {
        // TODO: Реализовать поиск локации блока
        return null;
    }
    
    /**
     * Возвращает информацию о доступных блоках и прогрессе миграции.
     */
    public String getSystemInfo() {
        int newActions = BlockFactory.getAvailableActions().size();
        int newConditions = BlockFactory.getAvailableConditions().size();
        int oldActions = oldActionRegistry.size();
        int oldConditions = oldConditionRegistry.size();
        
        int totalActions = newActions + oldActions;
        int totalConditions = newConditions + oldConditions;
        
        double migrationProgress = totalActions + totalConditions > 0 ? 
            (double)(newActions + newConditions) / (totalActions + totalConditions) * 100 : 0;
        
        return String.format(
            "🚀 ГИБРИДНАЯ СИСТЕМА - ПРОГРЕСС МИГРАЦИИ\n" +
            "📊 Статистика:\n" +
            "  ✅ Новые блоки: %d действий, %d условий\n" +
            "  ⚠️ Старые блоки: %d действий, %d условий\n" +
            "  📈 Всего: %d действий, %d условий\n" +
            "🎯 Прогресс миграции: %.1f%%\n" +
                           "📋 Мигрированные блоки:\n" +
               "  ✅ sendMessage, giveItem, randomNumber, setVar\n" +
               "  ✅ teleport, wait, setBlock, broadcast, command, playSound, effect\n" +
               "  ✅ addVar, subVar, mulVar, divVar (математические операции)\n" +
               "  ✅ spawnMob, healPlayer, setGameMode, setTime, setWeather (простые действия)\n" +
               "  ✅ explosion, getVar, getPlayerName (получение данных)\n" +
               "  ✅ forLoop, whileLoop, callFunction (циклы и функции)\n" +
               "  ✅ isOp, hasItem, ifVarEquals, playerHealth, else (условия)",
            newActions, newConditions, oldActions, oldConditions,
            totalActions, totalConditions, migrationProgress
        );
    }
} 