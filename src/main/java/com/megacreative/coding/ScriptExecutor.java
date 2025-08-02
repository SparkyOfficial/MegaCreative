package com.megacreative.coding;

import com.megacreative.MegaCreative;
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
import com.megacreative.coding.actions.PlayerHealthCondition;
import com.megacreative.coding.actions.HasItemCondition;
import com.megacreative.coding.actions.HasPermissionCondition;
import com.megacreative.coding.actions.IsInWorldCondition;
import com.megacreative.coding.actions.WaitAction;
import com.megacreative.coding.actions.GiveItemsAction;
import com.megacreative.coding.actions.SpawnEntityAction;
import com.megacreative.coding.actions.RandomNumberAction;
import com.megacreative.coding.actions.PlayParticleEffectAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * "Движок" для выполнения скриптов.
 * Использует паттерн "Стратегия" для выполнения действий.
 */
public class ScriptExecutor {
    private final MegaCreative plugin;
    private final Map<String, BlockAction> actionRegistry = new HashMap<>();
    private final Map<String, BlockCondition> conditionRegistry = new HashMap<>();

    public ScriptExecutor(MegaCreative plugin) {
        this.plugin = plugin;
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
        
        // Действия, которые были в конфигурации, но не зарегистрированы
        actionRegistry.put("randomNumber", new RandomNumberAction());
        actionRegistry.put("playParticle", new PlayParticleEffectAction());
        
        // Повторяющийся триггер
        actionRegistry.put("repeatTrigger", new RepeatTriggerAction());
        
        // Циклы и функции
        actionRegistry.put("repeat", new RepeatAction());
        actionRegistry.put("callFunction", new CallFunctionAction());
        actionRegistry.put("saveFunction", new SaveFunctionAction());
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
        
        // Новые условия для переменных
        conditionRegistry.put("ifVarEquals", new IfVarEqualsCondition());
        conditionRegistry.put("ifVarGreater", new IfVarGreaterCondition());
        conditionRegistry.put("ifVarLess", new IfVarLessCondition());
        
        // Условие для проверки типа блока
        conditionRegistry.put("isBlockType", new IsBlockTypeCondition());
        
        // Новые условия с виртуальными инвентарями
        conditionRegistry.put("isPlayerHolding", new IsPlayerHoldingCondition());
        conditionRegistry.put("isNearEntity", new IsNearEntityCondition());
        conditionRegistry.put("hasArmor", new HasArmorCondition());
    }

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
    
    // ЭТОТ МЕТОД - НАШ НОВЫЙ ДВИЖОК
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

        // 4. Логика выполнения (вся магия здесь)
        BlockAction action = actionRegistry.get(block.getAction());
        if (action != null) {
            action.execute(currentContext);
        } 
        // Если у нас нет зарегистрированного действия (например, для условий), обрабатываем отдельно
        else if (isConditionBlock(block.getMaterial())) {
            handleCondition(block, currentContext);
        }
        // Если действие не найдено, логируем это
        else {
            Player player = currentContext.getPlayer();
            if (player != null) {
                player.sendMessage("§cДействие '" + block.getAction() + "' не реализовано");
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
    
    // Обработка условий (можно тоже вынести в отдельные классы)
    private void handleCondition(CodeBlock block, ExecutionContext context) {
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
                    String worldName = (String) block.getParameter("world");
                    result = player.getWorld().getName().equals(worldName);
                    break;
                case "hasItem":
                    String itemName = (String) block.getParameter("item");
                    if (itemName != null) {
                        result = player.getInventory().contains(org.bukkit.Material.valueOf(itemName.toUpperCase()));
                    }
                    break;
                case "hasPermission":
                    String permission = (String) block.getParameter("permission");
                    result = player.hasPermission(permission);
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
                processBlock(child, context);
            }
        } else {
            // Ищем блок "Иначе" (END_STONE)
            CodeBlock next = block.getNextBlock();
            while (next != null) {
                if (next.getMaterial() == Material.END_STONE) {
                    for (CodeBlock child : next.getChildren()) {
                        processBlock(child, context);
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
     * Это медленная операция, используйте ее в основном для отладки.
     * @return Location блока или null, если не найден.
     */
    private Location findBlockLocation(CodeBlock block) {
        if (block == null) return null;
        // Мы берем карту всех блоков из обработчика
        Map<Location, CodeBlock> allBlocks = plugin.getBlockPlacementHandler().getBlockCodeBlocks();
        for (Map.Entry<Location, CodeBlock> entry : allBlocks.entrySet()) {
            // Сравниваем по ID, т.к. объекты могут быть разными экземплярами после сериализации
            if (entry.getValue().getId().equals(block.getId())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
