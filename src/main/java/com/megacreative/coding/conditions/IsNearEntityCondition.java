package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Условие для проверки существ рядом с игроком.
 * Использует предметы из виртуального инвентаря конфигурации блока.
 * 
 * Пример использования:
 * onPlayerMove -> if (near == [зомби]) -> sendMessage("Осторожно, рядом зомби!")
 */
public class IsNearEntityCondition implements BlockCondition {

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
                // Если в GUI ничего не положили, условие не выполняется
                return false; 
            }
        }

        // 4. Определяем тип существа по предмету
        EntityType requiredType = getEntityTypeFromItem(sampleItem);
        if (requiredType == null) {
            return false;
        }

        // 5. Получаем радиус поиска из именованного слота "radius_slot"
        double radius = 5.0; // Default
        ItemStack radiusItem = conditionBlock.getItemFromSlot("radius_slot");
        if (radiusItem == null) {
            // Fallback на старый способ для совместимости
            radiusItem = conditionBlock.getConfigItem(1);
        }
        
        if (radiusItem != null && radiusItem.getType() == Material.PAPER) {
            try {
                String radiusStr = radiusItem.getItemMeta().getDisplayName();
                if (radiusStr != null && radiusStr.contains("радиус:")) {
                    radius = Double.parseDouble(radiusStr.split(":")[1].trim());
                }
            } catch (Exception e) {
                // Используем значение по умолчанию
            }
        }

        // 6. Ищем существа указанного типа в радиусе
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (entity.getType() == requiredType) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Определяет тип существа по предмету
     * @param item Предмет для анализа
     * @return Тип существа или null, если не удалось определить
     */
    private EntityType getEntityTypeFromItem(ItemStack item) {
        Material material = item.getType();
        
        // Маппинг предметов на типы существ
        switch (material) {
            case ZOMBIE_HEAD:
            case ROTTEN_FLESH:
                return EntityType.ZOMBIE;
            case SKELETON_SKULL:
            case BONE:
                return EntityType.SKELETON;
            case CREEPER_HEAD:
            case GUNPOWDER:
                return EntityType.CREEPER;
            case SPIDER_EYE:
                return EntityType.SPIDER;
            case BLAZE_ROD:
                return EntityType.BLAZE;
            case GHAST_TEAR:
                return EntityType.GHAST;
            case SLIME_BALL:
                return EntityType.SLIME;
            case MAGMA_CREAM:
                return EntityType.MAGMA_CUBE;
            case ENDER_PEARL:
                return EntityType.ENDERMAN;
            case PHANTOM_MEMBRANE:
                return EntityType.PHANTOM;
            case PRISMARINE_SHARD:
                return EntityType.GUARDIAN;
            case SHULKER_SHELL:
                return EntityType.SHULKER;
            case RABBIT_HIDE:
                return EntityType.RABBIT;
            case FEATHER:
                return EntityType.CHICKEN;
            case PORKCHOP:
                return EntityType.PIG;
            case BEEF:
                return EntityType.COW;
            case MUTTON:
                return EntityType.SHEEP;
            case COD:
                return EntityType.COD;
            case SALMON:
                return EntityType.SALMON;
            case TROPICAL_FISH:
                return EntityType.TROPICAL_FISH;
            case PUFFERFISH:
                return EntityType.PUFFERFISH;
            case INK_SAC:
                return EntityType.SQUID;
            case GLOW_INK_SAC:
                return EntityType.GLOW_SQUID;
            case AXOLOTL_BUCKET:
                return EntityType.AXOLOTL;
            case GOAT_HORN:
                return EntityType.GOAT;
            case FROGSPAWN:
                return EntityType.FROG;
            case TADPOLE_BUCKET:
                return EntityType.TADPOLE;
            case ALLAY_SPAWN_EGG:
                return EntityType.ALLAY;
            case WARDEN_SPAWN_EGG:
                return EntityType.WARDEN;
            default:
                return null;
        }
    }
} 