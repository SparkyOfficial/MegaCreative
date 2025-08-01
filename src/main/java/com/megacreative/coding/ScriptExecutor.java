package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.actions.*;
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
        // Регистрируем все наши классы-действия
        actionRegistry.put("sendMessage", new SendMessageAction());
        actionRegistry.put("teleport", new TeleportAction());
        actionRegistry.put("giveItem", new GiveItemAction());
        actionRegistry.put("setVar", new SetVarAction());
        actionRegistry.put("broadcast", new BroadcastAction());
        actionRegistry.put("spawnMob", new SpawnMobAction());
        
        // Переменные
        actionRegistry.put("addVar", new AddVarAction());
        actionRegistry.put("subVar", new SubVarAction());
        actionRegistry.put("mulVar", new MulVarAction());
        actionRegistry.put("divVar", new DivVarAction());
        
        // Действия игрока
        actionRegistry.put("playSound", new PlaySoundAction());
        actionRegistry.put("effect", new EffectAction());
        actionRegistry.put("command", new CommandAction());
        actionRegistry.put("healPlayer", new HealPlayerAction());
        actionRegistry.put("setGameMode", new SetGameModeAction());
        
        // Игровые действия
        actionRegistry.put("setTime", new SetTimeAction());
        actionRegistry.put("setWeather", new SetWeatherAction());
        actionRegistry.put("explosion", new ExplosionAction());
        actionRegistry.put("setBlock", new SetBlockAction());
        
        // Получение данных
        actionRegistry.put("getVar", new GetVarAction());
        actionRegistry.put("getPlayerName", new GetPlayerNameAction());
    }

    private void registerConditions() {
        // Регистрируем все наши классы-условия
        conditionRegistry.put("isOp", new IsOpCondition());
        conditionRegistry.put("isInWorld", new IsInWorldCondition());
        conditionRegistry.put("hasItem", new HasItemCondition());
        conditionRegistry.put("hasPermission", new HasPermissionCondition());
        
        // Новые условия
        conditionRegistry.put("compareVariable", new CompareVariableCondition());
        conditionRegistry.put("playerHealth", new PlayerHealthCondition());
        conditionRegistry.put("worldTime", new WorldTimeCondition());
    }

    public void execute(CodeScript script, ExecutionContext context, String triggerAction) {
        Player player = context.getPlayer();
        if (player != null && plugin.getScriptDebugger().isDebugEnabled(player)) {
            plugin.getScriptDebugger().onScriptStart(player, script);
        }
        
        CodeBlock root = script.getRootBlock();
        if (root.getMaterial() == Material.DIAMOND_BLOCK && root.getAction().equals(triggerAction)) {
            processBlock(root.getNextBlock(), context);
        }

        if (player != null && plugin.getScriptDebugger().isDebugEnabled(player)) {
            plugin.getScriptDebugger().onScriptEnd(player, script);
        }
    }
    
    // ЭТОТ МЕТОД - НАШ НОВЫЙ ДВИЖОК
    private void processBlock(CodeBlock block, ExecutionContext context) {
        if (block == null) return;

        // 1. Находим локацию для отладки
        Location blockLocation = findBlockLocation(block);
        
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

        // 5. Переход к следующему блоку
        if (block.getNextBlock() != null) {
            processBlock(block.getNextBlock(), currentContext);
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
            result = condition.check(context);
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
