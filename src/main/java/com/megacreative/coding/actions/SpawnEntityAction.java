package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Действие для спавна существ.
 * Использует предметы из виртуального инвентаря конфигурации блока.
 * 
 * Пример использования:
 * onInteract -> spawnEntity([голова зомби, бумага с "количество:5", бумага с "радиус:3"])
 */
public class SpawnEntityAction implements BlockAction {

    @Override
    public void execute(ExecutionContext context) {
        // 1. Убедимся, что у нас есть игрок
        Player player = context.getPlayer();
        if (player == null) {
            return;
        }

        // 2. Получаем наш блок кода и его конфигурацию
        CodeBlock actionBlock = context.getCurrentBlock();
        if (actionBlock == null) return;

        // 3. Получаем slot resolver из BlockConfigService
        BlockConfigService configService = context.getPlugin().getServiceRegistry().getBlockConfigService();
        java.util.function.Function<String, Integer> slotResolver = 
            configService != null ? configService.getSlotResolver("spawnEntity") : null;

        // 4. Получаем предмет-образец из именованного слота "entity_slot"
        ItemStack sampleItem = slotResolver != null ? 
            actionBlock.getItemFromSlot("entity_slot", slotResolver) : null;
        if (sampleItem == null) {
            // Fallback на старый способ для совместимости
            sampleItem = actionBlock.getConfigItem(0);
            if (sampleItem == null) {
                return;
            }
        }

        // 5. Определяем тип существа по предмету
        EntityType entityType = getEntityTypeFromItem(sampleItem);
        if (entityType == null) {
            player.sendMessage("§cНе удалось определить тип существа по предмету!");
            return;
        }

        // 6. Получаем количество существ для спавна из именованного слота "count_slot"
        int count = 1;
        ItemStack countItem = slotResolver != null ? 
            actionBlock.getItemFromSlot("count_slot", slotResolver) : null;
        if (countItem == null) {
            // Fallback на старый способ для совместимости
            countItem = actionBlock.getConfigItem(1);
        }
        
        if (countItem != null && countItem.getType() == Material.PAPER) {
            try {
                String countStr = countItem.getItemMeta().getDisplayName();
                if (countStr != null && countStr.contains("количество:")) {
                    count = Integer.parseInt(countStr.split(":")[1].trim());
                }
            } catch (Exception e) {
                // Используем значение по умолчанию
            }
        }

        // 7. Получаем радиус спавна из именованного слота "radius_slot"
        double radius = 3.0;
        ItemStack radiusItem = slotResolver != null ? 
            actionBlock.getItemFromSlot("radius_slot", slotResolver) : null;
        if (radiusItem == null) {
            // Fallback на старый способ для совместимости
            radiusItem = actionBlock.getConfigItem(2);
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

        // 8. Спавним существ
        int spawnedCount = 0;
        Location playerLocation = player.getLocation();
        
        for (int i = 0; i < count; i++) {
            // Генерируем случайную позицию в радиусе
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * radius;
            double x = playerLocation.getX() + distance * Math.cos(angle);
            double z = playerLocation.getZ() + distance * Math.sin(angle);
            
            Location spawnLocation = new Location(player.getWorld(), x, playerLocation.getY(), z);
            
            // Спавним существо
            Entity entity = player.getWorld().spawnEntity(spawnLocation, entityType);
            if (entity != null) {
                spawnedCount++;
            }
        }

        // 9. Уведомляем игрока
        if (spawnedCount > 0) {
            player.sendMessage("§a✓ Создано " + spawnedCount + " существ типа " + entityType.name());
        }
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