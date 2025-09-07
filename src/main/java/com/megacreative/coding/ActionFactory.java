package com.megacreative.coding;

import com.megacreative.coding.actions.*; // Здесь будут все твои классы-действия
import com.megacreative.services.BlockConfigService;
import java.util.HashMap;
import java.util.Map;

public class ActionFactory {

    private final Map<String, Class<? extends BlockAction>> actionMap = new HashMap<>();
    private final BlockConfigService blockConfigService;

    public ActionFactory(BlockConfigService blockConfigService) {
        this.blockConfigService = blockConfigService;
        // Регистрируем все наши действия
        register("sendActionBar", SendActionBarAction.class);
        // ... и так далее для ВСЕХ блоков с type: "ACTION"
    }

    private void register(String actionId, Class<? extends BlockAction> actionClass) {
        actionMap.put(actionId, actionClass);
    }

    public BlockAction createAction(String actionId) {
        Class<? extends BlockAction> actionClass = actionMap.get(actionId);
        if (actionClass != null) {
            try {
                return actionClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                // Логирование ошибки
                e.printStackTrace();
                return null;
            }
        }
        return null; // или возвращать какое-то действие по-умолчанию
    }
}