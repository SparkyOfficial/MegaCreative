package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Действие для спавна существ.
 * Использует предметы из виртуального инвентаря конфигурации блока.
 * 
 * Пример использования:
 * onInteract -> spawnEntity([зомби]) -> sendMessage("Зомби появился!")
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

        // 3. Получаем предмет-образец из слота 0 нашего виртуального GUI
        ItemStack sampleItem = actionBlock.getConfigItem(0);
        if (sampleItem == null) {
            return;
        }

        // 4. Определяем тип существа по предмету
        EntityType entityType = getEntityTypeFromItem(sampleItem);
        if (entityType == null) {
            player.sendMessage("§cНе удалось определить тип существа по предмету!");
            return;
        }

        // 5. Получаем количество существ для спавна
        int count = 1;
        ItemStack countItem = actionBlock.getConfigItem(1);
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

        // 6. Получаем радиус спавна
        double radius = 3.0;
        ItemStack radiusItem = actionBlock.getConfigItem(2);
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

        // 7. Спавним существ
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

        // 8. Уведомляем игрока
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