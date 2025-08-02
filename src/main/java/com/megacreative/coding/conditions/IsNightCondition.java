package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Условие для проверки времени суток.
 * Использует предметы из виртуального инвентаря конфигурации блока.
 * 
 * Пример использования:
 * onTick -> if (isNight) -> spawnEntity([зомби])
 */
public class IsNightCondition implements BlockCondition {

    @Override
    public boolean evaluate(ExecutionContext context) {
        // 1. Убедимся, что у нас есть игрок
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        // 2. Получаем наш блок кода и его конфигурацию
        CodeBlock conditionBlock = context.getCurrentBlock();
        if (conditionBlock == null) return false;

        // 3. Получаем тип времени из именованного слота "time_slot"
        var timeItem = conditionBlock.getItemFromSlot("time_slot");
        if (timeItem == null) {
            // Fallback на старый способ для совместимости
            timeItem = conditionBlock.getConfigItem(0);
        }

        // 4. Определяем требуемое время суток
        TimeType requiredTime = getTimeTypeFromItem(timeItem);
        if (requiredTime == null) {
            // По умолчанию проверяем ночь
            requiredTime = TimeType.NIGHT;
        }

        // 5. Проверяем текущее время в мире
        World world = player.getWorld();
        long time = world.getTime();
        
        return requiredTime.matches(time);
    }
    
    /**
     * Определяет тип времени по предмету
     * @param item Предмет для анализа
     * @return Тип времени или null, если не удалось определить
     */
    private TimeType getTimeTypeFromItem(org.bukkit.inventory.ItemStack item) {
        if (item == null) return null;
        
        String displayName = item.getItemMeta() != null ? item.getItemMeta().getDisplayName() : "";
        
        // Проверяем по названию предмета
        switch (item.getType()) {
            case SUNFLOWER:
            case YELLOW_WOOL:
            case YELLOW_STAINED_GLASS:
                return TimeType.DAY;
            case BLACK_WOOL:
            case BLACK_STAINED_GLASS:
            case COAL:
                return TimeType.NIGHT;
            case ORANGE_WOOL:
            case ORANGE_STAINED_GLASS:
            case ORANGE_TULIP:
                return TimeType.SUNSET;
            case LIGHT_BLUE_WOOL:
            case LIGHT_BLUE_STAINED_GLASS:
            case BLUE_ORCHID:
                return TimeType.SUNRISE;
            case PAPER:
                // Проверяем по названию бумаги
                if (displayName.contains("день") || displayName.contains("day")) {
                    return TimeType.DAY;
                } else if (displayName.contains("ночь") || displayName.contains("night")) {
                    return TimeType.NIGHT;
                } else if (displayName.contains("закат") || displayName.contains("sunset")) {
                    return TimeType.SUNSET;
                } else if (displayName.contains("рассвет") || displayName.contains("sunrise")) {
                    return TimeType.SUNRISE;
                }
                break;
        }
        
        return null;
    }
    
    /**
     * Перечисление типов времени суток
     */
    private enum TimeType {
        DAY("День", 0, 12000),
        NIGHT("Ночь", 13000, 23000),
        SUNSET("Закат", 12000, 13000),
        SUNRISE("Рассвет", 23000, 24000);
        
        private final String name;
        private final long startTime;
        private final long endTime;
        
        TimeType(String name, long startTime, long endTime) {
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        public boolean matches(long time) {
            if (this == DAY) {
                return time >= startTime && time < endTime;
            } else if (this == NIGHT) {
                return time >= startTime || time < 1000;
            } else {
                return time >= startTime && time < endTime;
            }
        }
        
        public String getName() {
            return name;
        }
    }
} 