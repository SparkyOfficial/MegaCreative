package com.megacreative.coding;

import com.megacreative.coding.actions.*;
import com.megacreative.services.BlockConfigService;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ActionFactory {

    // Используем Supplier для "ленивого" создания. Это эффективнее.
    private final Map<String, Supplier<BlockAction>> actionMap = new HashMap<>();

    // Убираем зависимость от BlockConfigService, она здесь не нужна
    public ActionFactory(BlockConfigService blockConfigService) {
        // РЕГИСТРИРУЕМ АБСОЛЮТНО ВСЕ ДЕЙСТВИЯ ИЗ coding_blocks.yml с type: "ACTION"
        register("sendMessage", SendMessageAction::new);
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