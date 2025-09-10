package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CheckPlayerInventoryCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;

        try {
            // Получаем параметры из блока, используя getParameter()
            // Важно: нужно предусмотреть, что параметр может отсутствовать
            DataValue itemValue = block.getParameter("item");
            DataValue amountValue = block.getParameter("amount", DataValue.of(1)); // Значение по умолчанию 1
            DataValue checkTypeValue = block.getParameter("check_type", DataValue.of("has")); // "has" по умолчанию

            if (itemValue == null || itemValue.isEmpty()) {
                context.getPlugin().getLogger().warning("InventoryCheck: 'item' parameter is missing.");
                return false;
            }

            Material material = Material.matchMaterial(itemValue.asString());
            if (material == null) {
                context.getPlugin().getLogger().warning("InventoryCheck: Invalid material '" + itemValue.asString() + "'.");
                return false;
            }
            
            int amount = amountValue.asNumber().intValue();
            String checkType = checkTypeValue.asString().toLowerCase();

            switch (checkType) {
                case "has":
                    return player.getInventory().containsAtLeast(new ItemStack(material), amount);
                case "missing":
                    return !player.getInventory().containsAtLeast(new ItemStack(material), amount);
                case "exact":
                    int count = 0;
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType() == material) {
                            count += item.getAmount();
                        }
                    }
                    return count == amount;
                default:
                    return false;
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error evaluating CheckPlayerInventoryCondition: " + e.getMessage());
            return false;
        }
    }
}