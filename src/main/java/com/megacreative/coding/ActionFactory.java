package com.megacreative.coding;

import com.megacreative.coding.actions.*;
import com.megacreative.core.DependencyContainer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ActionFactory {

    // Используем Supplier для "ленивого" создания. Это эффективнее.
    private final Map<String, Supplier<BlockAction>> actionMap = new HashMap<>();
    private final DependencyContainer dependencyContainer;

    // Принимаем DependencyContainer для создания действий с зависимостями
    public ActionFactory(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
        
        // РЕГИСТРИРУЕМ АБСОЛЮТНО ВСЕ ДЕЙСТВИЯ ИЗ coding_blocks.yml с type: "ACTION"
        // For actions that don't require dependencies, we can use method references
        register("teleport", TeleportAction::new);
        register("giveItem", GiveItemAction::new);
        register("playSound", PlaySoundAction::new);
        register("effect", EffectAction::new);
        register("command", CommandAction::new);
        register("broadcast", BroadcastAction::new);
        register("giveItems", GiveItemsAction::new);
        register("spawnEntity", SpawnEntityAction::new);
        register("removeItems", RemoveItemsAction::new);
        register("setArmor", SetArmorAction::new);
        register("setVar", SetVariableAction::new);
        register("spawnMob", SpawnMobAction::new);
        register("healPlayer", HealPlayerAction::new);
        register("setGameMode", SetGameModeAction::new);
        register("setTime", SetTimeAction::new);
        register("setWeather", SetWeatherAction::new);
        register("explosion", ExplosionAction::new);
        register("setBlock", SetBlockAction::new);
        register("getVar", GetVariableAction::new);
        register("getPlayerName", GetPlayerNameAction::new);
        register("setGlobalVar", SetGlobalVariableAction::new);
        register("getGlobalVar", GetGlobalVariableAction::new);
        register("setServerVar", SetServerVariableAction::new);
        register("getServerVar", GetServerVariableAction::new);
        register("wait", WaitAction::new);
        register("randomNumber", RandomNumberAction::new);
        register("playParticle", PlayParticleAction::new);
        register("sendActionBar", SendActionBarAction::new);
        register("callFunction", CallFunctionAction::new);
        register("saveFunction", SaveFunctionAction::new);
        register("repeat", RepeatAction::new);
        register("repeatTrigger", RepeatTriggerAction::new);
        register("else", ElseAction::new);
        register("sendTitle", SendTitleAction::new);
        register("executeAsyncCommand", ExecuteAsyncCommandAction::new);
        register("asyncLoop", AsyncLoopAction::new);
        // Game-specific actions
        register("createScoreboard", CreateScoreboardAction::new);
        register("setScore", SetScoreAction::new);
        register("incrementScore", IncrementScoreAction::new);
        register("createTeam", CreateTeamAction::new);
        register("addPlayerToTeam", AddPlayerToTeamAction::new);
        register("saveLocation", SaveLocationAction::new);
        register("getLocation", GetLocationAction::new);
        // Добавляем недостающие действия
        // Для действий с зависимостями используем dependencyContainer
        register("sendMessage", () -> dependencyContainer.resolve(SendMessageAction.class));
        register("addVar", () -> dependencyContainer.resolve(AddVarAction.class));
        register("subVar", () -> dependencyContainer.resolve(SubVarAction.class));
        register("mulVar", () -> dependencyContainer.resolve(MulVarAction.class));
        register("divVar", () -> dependencyContainer.resolve(DivVarAction.class));
        // New target selector action
        register("sendMessageToTarget", SendMessageToTargetAction::new);
        // ... и т.д. для всех ACTION блоков...
    }

    private void register(String actionId, Supplier<BlockAction> supplier) {
        actionMap.put(actionId, supplier);
    }

    public BlockAction createAction(String actionId) {
        Supplier<BlockAction> supplier = actionMap.get(actionId);
        if (supplier != null) {
            try {
                return supplier.get(); // Создаем новый экземпляр
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        // Возвращаем null, если действие не найдено
        return null;
    }
}