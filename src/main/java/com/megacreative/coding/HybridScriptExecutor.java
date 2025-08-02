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
 * Гибридный исполнитель скриптов, поддерживающий как старые, так и новые блоки.
 * Обеспечивает плавный переход на новую архитектуру.
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
     */
    private void registerOldActions() {
        // Базовые действия
        oldActionRegistry.put("sendMessage", new SendMessageAction());
        oldActionRegistry.put("teleport", new TeleportAction());
        oldActionRegistry.put("giveItem", new GiveItemAction());
        oldActionRegistry.put("setVar", new SetVarAction());
        oldActionRegistry.put("broadcast", new BroadcastAction());
        oldActionRegistry.put("spawnMob", new SpawnMobAction());
        
        // Математические операции
        oldActionRegistry.put("addVar", new AddVarAction());
        oldActionRegistry.put("subVar", new SubVarAction());
        oldActionRegistry.put("mulVar", new MulVarAction());
        oldActionRegistry.put("divVar", new DivVarAction());
        
        // Действия с миром
        oldActionRegistry.put("playSound", new PlaySoundAction());
        oldActionRegistry.put("effect", new EffectAction());
        oldActionRegistry.put("command", new CommandAction());
        oldActionRegistry.put("healPlayer", new HealPlayerAction());
        oldActionRegistry.put("setGameMode", new SetGameModeAction());
        
        // Действия с временем и погодой
        oldActionRegistry.put("setTime", new SetTimeAction());
        oldActionRegistry.put("setWeather", new SetWeatherAction());
        
        // Действия с блоками и взрывами
        oldActionRegistry.put("explosion", new ExplosionAction());
        oldActionRegistry.put("setBlock", new SetBlockAction());
        
        // Получение данных
        oldActionRegistry.put("getVar", new GetVarAction());
        oldActionRegistry.put("getPlayerName", new GetPlayerNameAction());
        
        // Глобальные переменные игрока
        oldActionRegistry.put("setGlobalVar", new SetGlobalVariableAction());
        oldActionRegistry.put("getGlobalVar", new GetGlobalVariableAction());
        
        // Серверные переменные
        oldActionRegistry.put("setServerVar", new SetServerVariableAction());
        oldActionRegistry.put("getServerVar", new GetServerVariableAction());
        
        // Действие ожидания
        oldActionRegistry.put("wait", new WaitAction());
        
        // Новые действия с виртуальными инвентарями
        oldActionRegistry.put("giveItems", new GiveItemsAction());
        oldActionRegistry.put("spawnEntity", new SpawnEntityAction());
        oldActionRegistry.put("removeItems", new RemoveItemsAction());
        oldActionRegistry.put("setArmor", new SetArmorAction());
        oldActionRegistry.put("playParticleEffect", new PlayParticleEffectAction());
        oldActionRegistry.put("randomNumber", new RandomNumberAction());
        
        // Повторения
        oldActionRegistry.put("repeat", new RepeatAction());
        oldActionRegistry.put("repeatTrigger", new RepeatTriggerAction());
        
        // Функции
        oldActionRegistry.put("callFunction", new CallFunctionAction());
        oldActionRegistry.put("saveFunction", new SaveFunctionAction());
    }
    
    /**
     * Регистрирует старые условия для обратной совместимости.
     */
    private void registerOldConditions() {
        // Базовые условия
        oldConditionRegistry.put("isOp", new IsOpCondition());
        oldConditionRegistry.put("hasItem", new HasItemCondition());
        oldConditionRegistry.put("hasPermission", new HasPermissionCondition());
        oldConditionRegistry.put("isInWorld", new IsInWorldCondition());
        oldConditionRegistry.put("playerHealth", new PlayerHealthCondition());
        
        // Условия переменных
        oldConditionRegistry.put("ifVarEquals", new IfVarEqualsCondition());
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
     * Обрабатывает условие с гибридной системой.
     */
    private void handleCondition(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        String conditionName = block.getAction();
        
        // 1. Пытаемся найти в НОВОЙ системе
        BlockCondition newCondition = BlockFactory.getCondition(conditionName);
        if (newCondition != null) {
            try {
                boolean result = newCondition.evaluate(context);
                if (result) {
                    for (CodeBlock child : block.getChildren()) {
                        Location childLocation = findBlockLocation(child);
                        ExecutionContext childContext = context.withCurrentBlock(child, childLocation);
                        processBlock(child, childContext);
                    }
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
                    for (CodeBlock child : block.getChildren()) {
                        Location childLocation = findBlockLocation(child);
                        ExecutionContext childContext = context.withCurrentBlock(child, childLocation);
                        processBlock(child, childContext);
                    }
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
     * Возвращает информацию о доступных блоках.
     */
    public String getSystemInfo() {
        int newActions = BlockFactory.getAvailableActions().size();
        int newConditions = BlockFactory.getAvailableConditions().size();
        int oldActions = oldActionRegistry.size();
        int oldConditions = oldConditionRegistry.size();
        
        return String.format(
            "Гибридная система:\n" +
            "Новые блоки: %d действий, %d условий\n" +
            "Старые блоки: %d действий, %d условий\n" +
            "Всего: %d действий, %d условий",
            newActions, newConditions, oldActions, oldConditions,
            newActions + oldActions, newConditions + oldConditions
        );
    }
} 