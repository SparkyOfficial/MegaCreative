package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Условие для проверки брони игрока.
 * Использует предметы из виртуального инвентаря конфигурации блока.
 * 
 * Пример использования:
 * onJoin -> if (hasArmor == [алмазная броня]) -> sendMessage("У вас хорошая броня!")
 */
public class HasArmorCondition implements BlockCondition {

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

        // 3. Получаем предмет-образец из именованного слота "armor_slot"
        ItemStack sampleItem = conditionBlock.getItemFromSlot("armor_slot");
        if (sampleItem == null) {
            // Fallback на старый способ для совместимости
            sampleItem = conditionBlock.getConfigItem(0);
            if (sampleItem == null) {
                // Если в GUI ничего не положили, условие не выполняется
                return false; 
            }
        }

        // 4. Определяем тип брони по предмету
        ArmorType requiredArmorType = getArmorTypeFromItem(sampleItem);
        if (requiredArmorType == null) {
            return false;
        }

        // 5. Проверяем броню игрока
        PlayerInventory inventory = player.getInventory();
        ItemStack[] armorContents = inventory.getArmorContents();
        
        return armorContents[requiredArmorType.getSlot()] != null && 
               armorContents[requiredArmorType.getSlot()].getType() == requiredArmorType.getMaterial();
    }
    
    /**
     * Определяет тип брони по предмету
     * @param item Предмет для анализа
     * @return Тип брони или null, если не удалось определить
     */
    private ArmorType getArmorTypeFromItem(ItemStack item) {
        Material material = item.getType();
        
        // Маппинг предметов на типы брони
        switch (material) {
            case DIAMOND_HELMET:
                return new ArmorType(Material.DIAMOND_HELMET, 3);
            case DIAMOND_CHESTPLATE:
                return new ArmorType(Material.DIAMOND_CHESTPLATE, 2);
            case DIAMOND_LEGGINGS:
                return new ArmorType(Material.DIAMOND_LEGGINGS, 1);
            case DIAMOND_BOOTS:
                return new ArmorType(Material.DIAMOND_BOOTS, 0);
            case IRON_HELMET:
                return new ArmorType(Material.IRON_HELMET, 3);
            case IRON_CHESTPLATE:
                return new ArmorType(Material.IRON_CHESTPLATE, 2);
            case IRON_LEGGINGS:
                return new ArmorType(Material.IRON_LEGGINGS, 1);
            case IRON_BOOTS:
                return new ArmorType(Material.IRON_BOOTS, 0);
            case GOLDEN_HELMET:
                return new ArmorType(Material.GOLDEN_HELMET, 3);
            case GOLDEN_CHESTPLATE:
                return new ArmorType(Material.GOLDEN_CHESTPLATE, 2);
            case GOLDEN_LEGGINGS:
                return new ArmorType(Material.GOLDEN_LEGGINGS, 1);
            case GOLDEN_BOOTS:
                return new ArmorType(Material.GOLDEN_BOOTS, 0);
            case CHAINMAIL_HELMET:
                return new ArmorType(Material.CHAINMAIL_HELMET, 3);
            case CHAINMAIL_CHESTPLATE:
                return new ArmorType(Material.CHAINMAIL_CHESTPLATE, 2);
            case CHAINMAIL_LEGGINGS:
                return new ArmorType(Material.CHAINMAIL_LEGGINGS, 1);
            case CHAINMAIL_BOOTS:
                return new ArmorType(Material.CHAINMAIL_BOOTS, 0);
            case NETHERITE_HELMET:
                return new ArmorType(Material.NETHERITE_HELMET, 3);
            case NETHERITE_CHESTPLATE:
                return new ArmorType(Material.NETHERITE_CHESTPLATE, 2);
            case NETHERITE_LEGGINGS:
                return new ArmorType(Material.NETHERITE_LEGGINGS, 1);
            case NETHERITE_BOOTS:
                return new ArmorType(Material.NETHERITE_BOOTS, 0);
            case TURTLE_HELMET:
                return new ArmorType(Material.TURTLE_HELMET, 3);
            case ELYTRA:
                return new ArmorType(Material.ELYTRA, 2);
            default:
                return null;
        }
    }
    
    /**
     * Внутренний класс для хранения информации о типе брони
     */
    private static class ArmorType {
        private final Material material;
        private final int slot; // 0 - ботинки, 1 - штаны, 2 - нагрудник, 3 - шлем
        
        public ArmorType(Material material, int slot) {
            this.material = material;
            this.slot = slot;
        }
        
        public Material getMaterial() {
            return material;
        }
        
        public int getSlot() {
            return slot;
        }
    }
} 