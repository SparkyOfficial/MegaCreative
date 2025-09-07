package com.megacreative.coding;

import com.megacreative.coding.actions.*;
import com.megacreative.services.BlockConfigService;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ActionFactory {

    // Используем Supplier для "ленивого" создания. Это эффективнее.
    private final Map<String, Supplier<BlockAction>> actionMap = new HashMap<>();

    private final BlockConfigService blockConfigService;

    public ActionFactory(BlockConfigService blockConfigService) {
        this.blockConfigService = blockConfigService;
        // РЕГИСТРИРУЕМ АБСОЛЮТНО ВСЕ ДЕЙСТВИЯ ИЗ coding_blocks.yml с type: "ACTION"
        register("sendMessage", () -> new SendMessageAction(null));
        register("teleport", () -> new TeleportAction());
        register("giveItem", () -> new GiveItemAction());
        register("playSound", () -> new PlaySoundAction());
        register("effect", () -> new EffectAction());
        register("command", () -> new CommandAction());
        register("broadcast", () -> new BroadcastAction());
        register("giveItems", () -> new GiveItemsAction());
        register("spawnEntity", () -> new SpawnEntityAction());
        register("removeItems", () -> new RemoveItemsAction());
        register("setArmor", () -> new SetArmorAction());
        register("setVar", () -> new SetVariableAction());
        register("spawnMob", () -> new SpawnMobAction());
        register("healPlayer", () -> new HealPlayerAction());
        register("setGameMode", () -> new SetGameModeAction());
        register("setTime", () -> new SetTimeAction());
        register("setWeather", () -> new SetWeatherAction());
        register("explosion", () -> new ExplosionAction());
        register("setBlock", () -> new SetBlockAction());
        register("getVar", () -> new GetVariableAction());
        register("getPlayerName", () -> new GetPlayerNameAction());
        register("setGlobalVar", () -> new SetGlobalVariableAction());
        register("getGlobalVar", () -> new GetGlobalVariableAction());
        register("setServerVar", () -> new SetServerVariableAction());
        register("getServerVar", () -> new GetServerVariableAction());
        register("wait", () -> new WaitAction());
        register("randomNumber", () -> new RandomNumberAction());
        register("playParticle", () -> new PlayParticleAction());
        register("sendActionBar", () -> new SendActionBarAction());
        register("callFunction", () -> new CallFunctionAction());
        register("saveFunction", () -> new SaveFunctionAction());
        register("repeat", () -> new RepeatAction());
        register("repeatTrigger", () -> new RepeatTriggerAction());
        register("else", () -> new ElseAction());
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