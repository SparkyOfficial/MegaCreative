package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Действие для удаления предметов у игрока.
 * Использует предметы из виртуального инвентаря конфигурации блока.
 * 
 * Пример использования:
 * onInteract -> removeItems([алмазный меч]) -> sendMessage("Меч удален!")
 */
public class RemoveItemsAction implements BlockAction {

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

        // 3. Получаем group slots resolver из BlockConfigService
        BlockConfigService configService = context.getPlugin().getServiceRegistry().getBlockConfigService();
        java.util.function.Function<String, List<Integer>> groupSlotsResolver = 
            configService != null ? configService.getGroupSlotsResolver("removeItems") : null;

        // 4. Получаем все предметы из именованной группы "items_to_remove"
        List<ItemStack> itemsToRemove = groupSlotsResolver != null ? 
            actionBlock.getItemsFromNamedGroup("items_to_remove", groupSlotsResolver) : 
            actionBlock.getItemsFromGroup("items");
        
        // Если группа не найдена, используем старый способ для совместимости
        if (itemsToRemove.isEmpty()) {
            // Fallback на слоты 0-8
            for (int i = 0; i < 9; i++) {
                ItemStack item = actionBlock.getConfigItem(i);
                if (item != null) {
                    itemsToRemove.add(item);
                }
            }
        }

        // 5. Удаляем предметы у игрока
        int removedItems = 0;
        for (ItemStack item : itemsToRemove) {
            if (item != null) {
                Material material = item.getType();
                int amount = item.getAmount();
                
                // Удаляем предметы указанного типа и количества
                ItemStack[] contents = player.getInventory().getContents();
                for (int i = 0; i < contents.length && amount > 0; i++) {
                    ItemStack inventoryItem = contents[i];
                    if (inventoryItem != null && inventoryItem.getType() == material) {
                        int toRemove = Math.min(amount, inventoryItem.getAmount());
                        if (inventoryItem.getAmount() <= toRemove) {
                            player.getInventory().setItem(i, null);
                        } else {
                            inventoryItem.setAmount(inventoryItem.getAmount() - toRemove);
                        }
                        amount -= toRemove;
                        removedItems += toRemove;
                    }
                }
            }
        }

        // 6. Уведомляем игрока
        if (removedItems > 0) {
            player.sendMessage("§c✓ Удалено " + removedItems + " предметов!");
        } else {
            player.sendMessage("§eℹ Нечего удалять.");
        }
    }
}