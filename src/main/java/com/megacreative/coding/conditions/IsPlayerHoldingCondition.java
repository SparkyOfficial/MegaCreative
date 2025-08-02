package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Условие для проверки предмета в руке игрока.
 * Использует предметы из виртуального инвентаря конфигурации блока.
 * 
 * Пример использования:
 * onInteract -> if (holding == [меч]) -> teleport("спавн")
 */
public class IsPlayerHoldingCondition implements BlockCondition {

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

        // 3. Получаем предмет-образец из именованного слота "item_slot"
        ItemStack sampleItem = conditionBlock.getItemFromSlot("item_slot");
        if (sampleItem == null) {
            // Fallback на старый способ для совместимости
            sampleItem = conditionBlock.getConfigItem(0);
            if (sampleItem == null) {
                // Если в GUI ничего не положили, условие не выполняется
                return false; 
            }
        }

        // 4. Получаем предмет в руке игрока
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        
        // 5. Сравниваем типы материалов
        Material requiredType = sampleItem.getType();
        Material heldType = itemInHand.getType();

        return requiredType == heldType;
    }
} 