package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Действие для установки брони игроку.
 * Использует предметы из виртуального инвентаря конфигурации блока.
 * 
 * Пример использования:
 * onJoin -> setArmor([алмазный шлем, алмазный нагрудник]) -> sendMessage("Броня установлена!")
 */
public class SetArmorAction implements BlockAction {

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
            configService != null ? configService.getSlotResolver("setArmor") : null;

        // 4. Получаем предметы брони из именованных слотов
        ItemStack helmet = slotResolver != null ? 
            actionBlock.getItemFromSlot("helmet_slot", slotResolver) : null;
        ItemStack chestplate = slotResolver != null ? 
            actionBlock.getItemFromSlot("chestplate_slot", slotResolver) : null;
        ItemStack leggings = slotResolver != null ? 
            actionBlock.getItemFromSlot("leggings_slot", slotResolver) : null;
        ItemStack boots = slotResolver != null ? 
            actionBlock.getItemFromSlot("boots_slot", slotResolver) : null;
        
        // Fallback на старый способ для совместимости
        if (helmet == null) helmet = actionBlock.getConfigItem(0);
        if (chestplate == null) chestplate = actionBlock.getConfigItem(1);
        if (leggings == null) leggings = actionBlock.getConfigItem(2);
        if (boots == null) boots = actionBlock.getConfigItem(3);

        // 5. Устанавливаем броню
        int armorPieces = 0;
        
        if (helmet != null && isArmorPiece(helmet.getType(), ArmorType.HELMET)) {
            player.getInventory().setHelmet(helmet.clone());
            armorPieces++;
        }
        
        if (chestplate != null && isArmorPiece(chestplate.getType(), ArmorType.CHESTPLATE)) {
            player.getInventory().setChestplate(chestplate.clone());
            armorPieces++;
        }
        
        if (leggings != null && isArmorPiece(leggings.getType(), ArmorType.LEGGINGS)) {
            player.getInventory().setLeggings(leggings.clone());
            armorPieces++;
        }
        
        if (boots != null && isArmorPiece(boots.getType(), ArmorType.BOOTS)) {
            player.getInventory().setBoots(boots.clone());
            armorPieces++;
        }

        // 6. Уведомляем игрока
        if (armorPieces > 0) {
            player.sendMessage("§a✓ Установлено " + armorPieces + " предметов брони!");
        } else {
            player.sendMessage("§eℹ Нечего устанавливать.");
        }
    }
    
    /**
     * Проверяет, является ли предмет броней указанного типа
     */
    private boolean isArmorPiece(Material material, ArmorType armorType) {
        switch (armorType) {
            case HELMET:
                return material.name().endsWith("_HELMET") || material == Material.TURTLE_HELMET;
            case CHESTPLATE:
                return material.name().endsWith("_CHESTPLATE") || material == Material.ELYTRA;
            case LEGGINGS:
                return material.name().endsWith("_LEGGINGS");
            case BOOTS:
                return material.name().endsWith("_BOOTS");
            default:
                return false;
        }
    }
    
    /**
     * Типы брони
     */
    private enum ArmorType {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS
    }
}