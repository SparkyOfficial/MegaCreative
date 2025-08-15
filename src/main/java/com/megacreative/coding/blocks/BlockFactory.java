package com.megacreative.coding.blocks;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.blocks.actions.*;
import com.megacreative.coding.blocks.conditions.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика для создания и управления блоками действий и условий.
 * Централизованная система регистрации всех доступных блоков.
 */
public class BlockFactory {
    
    private static final Map<String, BlockAction> actionRegistry = new HashMap<>();
    private static final Map<String, BlockCondition> conditionRegistry = new HashMap<>();
    
    static {
        registerActions();
        registerConditions();
    }
    
    /**
     * Регистрирует все доступные действия.
     */
    private static void registerActions() {
        // Новые улучшенные действия
        actionRegistry.put("sendMessage", new SendMessageAction());
        actionRegistry.put("giveItem", new GiveItemAction());
        actionRegistry.put("randomNumber", new RandomNumberAction());
        actionRegistry.put("setVar", new SetVarAction());
        actionRegistry.put("teleport", new TeleportAction());
        actionRegistry.put("wait", new WaitAction());
        actionRegistry.put("setBlock", new SetBlockAction());
        actionRegistry.put("broadcast", new BroadcastAction());
        actionRegistry.put("command", new CommandAction());
        actionRegistry.put("playSound", new PlaySoundAction());
        actionRegistry.put("effect", new EffectAction());
        
        // Математические операции
        actionRegistry.put("addVar", new AddVarAction());
        actionRegistry.put("subVar", new SubVarAction());
        actionRegistry.put("mulVar", new MulVarAction());
        actionRegistry.put("divVar", new DivVarAction());
        
        // Простые действия
        actionRegistry.put("spawnMob", new SpawnMobAction());
        actionRegistry.put("healPlayer", new HealPlayerAction());
        actionRegistry.put("setGameMode", new SetGameModeAction());
        actionRegistry.put("setTime", new SetTimeAction());
        actionRegistry.put("setWeather", new SetWeatherAction());
        
        // Получение данных
        actionRegistry.put("explosion", new ExplosionAction());
        actionRegistry.put("getVar", new GetVarAction());
        actionRegistry.put("getPlayerName", new GetPlayerNameAction());
        
        // Циклы
        actionRegistry.put("forLoop", new ForLoopAction());
        actionRegistry.put("whileLoop", new WhileLoopAction());
        
        // Функции
        actionRegistry.put("callFunction", new CallFunctionAction());
        
        // Действия с инвентарем
        actionRegistry.put("clearInventory", new ClearInventoryAction());
        actionRegistry.put("removeItem", new RemoveItemAction());
        
        // Действия с миром
        actionRegistry.put("spawnParticle", new SpawnParticleAction());
        actionRegistry.put("createExplosion", new CreateExplosionAction());
        
        // GUI-действия
        actionRegistry.put("openChestGUI", new OpenChestGUIAction());
        actionRegistry.put("showMessageInTitle", new ShowMessageInTitleAction());
        
        // TODO: Добавить остальные действия по мере их рефакторинга
        // и так далее...
    }
    
    /**
     * Регистрирует все доступные условия.
     */
    private static void registerConditions() {
        // Новые улучшенные условия
        conditionRegistry.put("isOp", new IsOpCondition());
        conditionRegistry.put("hasItem", new HasItemCondition());
        conditionRegistry.put("ifVarEquals", new IfVarEqualsCondition());
        conditionRegistry.put("playerHealth", new PlayerHealthCondition());
        conditionRegistry.put("else", new ElseCondition());
        
        // Новые условия
        conditionRegistry.put("hasItemInSlot", new HasItemInSlotCondition());
        conditionRegistry.put("isNearLocation", new IsNearLocationCondition());
        conditionRegistry.put("playerHasEffect", new PlayerHasEffectCondition());
        
        // TODO: Добавить остальные условия по мере их рефакторинга
        // conditionRegistry.put("isInWorld", new IsInWorldCondition());
        // conditionRegistry.put("hasPermission", new HasPermissionCondition());
        // и так далее...
    }
    
    /**
     * Получает действие по имени.
     * @param actionName Имя действия
     * @return Блок действия или null, если не найден
     */
    public static BlockAction getAction(String actionName) {
        return actionRegistry.get(actionName);
    }
    
    /**
     * Получает условие по имени.
     * @param conditionName Имя условия
     * @return Блок условия или null, если не найден
     */
    public static BlockCondition getCondition(String conditionName) {
        return conditionRegistry.get(conditionName);
    }
    
    /**
     * Проверяет, существует ли действие с указанным именем.
     * @param actionName Имя действия
     * @return true, если действие существует
     */
    public static boolean hasAction(String actionName) {
        return actionRegistry.containsKey(actionName);
    }
    
    /**
     * Проверяет, существует ли условие с указанным именем.
     * @param conditionName Имя условия
     * @return true, если условие существует
     */
    public static boolean hasCondition(String conditionName) {
        return conditionRegistry.containsKey(conditionName);
    }
    
    /**
     * Возвращает все зарегистрированные имена действий.
     * @return Множество имен действий
     */
    public static java.util.Set<String> getAvailableActions() {
        return new java.util.HashSet<>(actionRegistry.keySet());
    }
    
    /**
     * Возвращает все зарегистрированные имена условий.
     * @return Множество имен условий
     */
    public static java.util.Set<String> getAvailableConditions() {
        return new java.util.HashSet<>(conditionRegistry.keySet());
    }
    
    /**
     * Регистрирует новое действие.
     * @param name Имя действия
     * @param action Блок действия
     */
    public static void registerAction(String name, BlockAction action) {
        actionRegistry.put(name, action);
    }
    
    /**
     * Регистрирует новое условие.
     * @param name Имя условия
     * @param condition Блок условия
     */
    public static void registerCondition(String name, BlockCondition condition) {
        conditionRegistry.put(name, condition);
    }
} 