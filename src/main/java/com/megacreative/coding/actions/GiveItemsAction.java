package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Действие для выдачи предметов игроку.
 * Использует предметы из виртуального инвентаря конфигурации блока.
 * 
 * Пример использования:
 * onJoin -> giveItems([алмазный меч, алмазная броня]) -> sendMessage("Добро пожаловать!")
 */
public class GiveItemsAction implements BlockAction {

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

        // 3. Получаем все предметы из именованной группы "items_to_give"
        List<ItemStack> itemsToGive = actionBlock.getItemsFromNamedGroup("items_to_give");
        
        // Если группа не найдена, используем старый способ для совместимости
        if (itemsToGive.isEmpty()) {
            itemsToGive = actionBlock.getItemsFromGroup("items");
            if (itemsToGive.isEmpty()) {
                // Fallback на слоты 0-8
                for (int i = 0; i < 9; i++) {
                    ItemStack item = actionBlock.getConfigItem(i);
                    if (item != null) {
                        itemsToGive.add(item);
                    }
                }
            }
        }

        // 4. Выдаем предметы игроку
        int givenItems = 0;
        for (ItemStack item : itemsToGive) {
            if (item != null) {
                // Создаем копию предмета для выдачи
                ItemStack itemToGive = item.clone();
                
                // Получаем количество из параметра или используем 1
                int amount = 1;
                Object amountParam = actionBlock.getParameter("amount");
                if (amountParam != null) {
                    try {
                        amount = Integer.parseInt(amountParam.toString());
                    } catch (NumberFormatException e) {
                        // Используем значение по умолчанию
                    }
                }
                
                itemToGive.setAmount(amount);
                
                // Выдаем предмет
                player.getInventory().addItem(itemToGive);
                givenItems++;
            }
        }

        // 5. Уведомляем игрока
        if (givenItems > 0) {
            player.sendMessage("§a✓ Вам выдано " + givenItems + " предметов!");
        }
    }
} 