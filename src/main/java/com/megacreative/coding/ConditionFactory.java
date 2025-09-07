package com.megacreative.coding;

import com.megacreative.coding.conditions.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConditionFactory {

    private final Map<String, Supplier<BlockCondition>> conditionMap = new HashMap<>();

    // Убираем зависимость от BlockConfigService, она здесь не нужна
    public ConditionFactory() {
        // Регистрируем все наши условия
        register("isOp", IsOpCondition::new);
        register("compareVariable", CompareVariableCondition::new);
        register("worldTime", WorldTimeCondition::new);
        register("isNearBlock", IsNearBlockCondition::new);
        register("mobNear", MobNearCondition::new);
        register("playerGameMode", PlayerGameModeCondition::new);
        register("playerHealth", PlayerHealthCondition::new);
        register("hasItem", HasItemCondition::new);
        register("hasPermission", HasPermissionCondition::new);
        register("isInWorld", IsInWorldCondition::new);
        register("ifVarEquals", IfVarEqualsCondition::new);
        register("ifVarGreater", IfVarGreaterCondition::new);
        register("ifVarLess", IfVarLessCondition::new);
        register("isBlockType", IsBlockTypeCondition::new);
        register("isPlayerHolding", IsPlayerHoldingCondition::new);
        register("isNearEntity", IsNearEntityCondition::new);
        register("hasArmor", HasArmorCondition::new);
        register("isNight", IsNightCondition::new);
        register("isRiding", IsRidingCondition::new);
        register("checkWorldWeather", CheckWorldWeatherCondition::new);
        // ... и так далее для ВСЕХ блоков с type: "CONDITION"
    }

    private void register(String conditionId, Supplier<BlockCondition> conditionSupplier) {
        conditionMap.put(conditionId, conditionSupplier);
    }

    public BlockCondition createCondition(String conditionId) {
        Supplier<BlockCondition> supplier = conditionMap.get(conditionId);
        if (supplier != null) {
            try {
                return supplier.get();
            } catch (Exception e) {
                // Логирование ошибки
                e.printStackTrace();
                return null;
            }
        }
        return null; // или возвращать какое-то условие по-умолчанию
    }
}