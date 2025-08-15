package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Условие для проверки езды на существе.
 * Использует предметы из виртуального инвентаря конфигурации блока.
 * 
 * Пример использования:
 * onPlayerMove -> if (isRiding == [лошадь]) -> sendMessage("Вы едете на лошади!")
 */
public class IsRidingCondition implements BlockCondition {

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

        // 3. Получаем предмет-образец из именованного слота "entity_slot"
        ItemStack sampleItem = conditionBlock.getItemFromSlot("entity_slot");
        if (sampleItem == null) {
            // Fallback на старый способ для совместимости
            sampleItem = conditionBlock.getConfigItem(0);
            if (sampleItem == null) {
                // Если в GUI ничего не положили, проверяем любое существо
                return player.isInsideVehicle();
            }
        }

        // 4. Определяем тип существа по предмету
        EntityType requiredType = getEntityTypeFromItem(sampleItem);
        if (requiredType == null) {
            // Если не удалось определить тип, проверяем любое существо
            return player.isInsideVehicle();
        }

        // 5. Проверяем, едет ли игрок на существе указанного типа
        Entity vehicle = player.getVehicle();
        return vehicle != null && vehicle.getType() == requiredType;
    }
    
    /**
     * Определяет тип существа по предмету
     * @param item Предмет для анализа
     * @return Тип существа или null, если не удалось определить
     */
    private EntityType getEntityTypeFromItem(ItemStack item) {
        if (item == null) return null;
        
        // Маппинг предметов на типы существ для езды
        switch (item.getType()) {
            case HORSE_SPAWN_EGG:
            case SADDLE:
                return EntityType.HORSE;
            case DONKEY_SPAWN_EGG:
                return EntityType.DONKEY;
            case MULE_SPAWN_EGG:
                return EntityType.MULE;
            case PIG_SPAWN_EGG:
                return EntityType.PIG;
            case OAK_BOAT:
            case BIRCH_BOAT:
            case SPRUCE_BOAT:
            case JUNGLE_BOAT:
            case ACACIA_BOAT:
            case DARK_OAK_BOAT:
                return EntityType.BOAT;
            case MINECART:
                return EntityType.MINECART;
            case CHEST_MINECART:
                return EntityType.MINECART;
            case FURNACE_MINECART:
                return EntityType.MINECART;
            case TNT_MINECART:
                return EntityType.MINECART;
            case HOPPER_MINECART:
                return EntityType.MINECART;
            case COMMAND_BLOCK_MINECART:
                return EntityType.MINECART;
            case STRIDER_SPAWN_EGG:
                return EntityType.STRIDER;
            case CAMEL_SPAWN_EGG:
                return EntityType.CAMEL;
            case PAPER:
                // Проверяем по названию бумаги
                String displayName = item.getItemMeta() != null ? item.getItemMeta().getDisplayName() : "";
                if (displayName.contains("лошадь") || displayName.contains("horse")) {
                    return EntityType.HORSE;
                } else if (displayName.contains("осел") || displayName.contains("donkey")) {
                    return EntityType.DONKEY;
                } else if (displayName.contains("мул") || displayName.contains("mule")) {
                    return EntityType.MULE;
                } else if (displayName.contains("свинья") || displayName.contains("pig")) {
                    return EntityType.PIG;
                } else if (displayName.contains("лодка") || displayName.contains("boat")) {
                    return EntityType.BOAT;
                } else if (displayName.contains("вагонетка") || displayName.contains("minecart")) {
                    return EntityType.MINECART;
                } else if (displayName.contains("странник") || displayName.contains("strider")) {
                    return EntityType.STRIDER;
                } else if (displayName.contains("верблюд") || displayName.contains("camel")) {
                    return EntityType.CAMEL;
                }
                break;
        }
        
        return null;
    }
} 