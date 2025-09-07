package com.megacreative.coding;

import com.megacreative.coding.conditions.*; // Здесь все классы-условия
import com.megacreative.services.BlockConfigService;
import java.util.HashMap;
import java.util.Map;

public class ConditionFactory {

    private final Map<String, Class<? extends BlockCondition>> conditionMap = new HashMap<>();
    private final BlockConfigService blockConfigService;

    public ConditionFactory(BlockConfigService blockConfigService) {
        this.blockConfigService = blockConfigService;
        // Регистрируем все наши условия
        register("checkWorldWeather", CheckWorldWeatherCondition.class);
        // ... и так далее для ВСЕХ блоков с type: "CONDITION"
    }

    private void register(String conditionId, Class<? extends BlockCondition> conditionClass) {
        conditionMap.put(conditionId, conditionClass);
    }

    public BlockCondition createCondition(String conditionId) {
        Class<? extends BlockCondition> conditionClass = conditionMap.get(conditionId);
        if (conditionClass != null) {
            try {
                return conditionClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                // Логирование ошибки
                e.printStackTrace();
                return null;
            }
        }
        return null; // или возвращать какое-то условие по-умолчанию
    }
}