package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Условие для проверки, держит ли игрок определенный предмет в руке.
 */
public class IsPlayerHoldingCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawMaterial = block.getParameter("material");

        if (rawMaterial == null) {
            context.getPlugin().getLogger().warning("Material not specified in IsPlayerHoldingCondition");
            return false;
        }

        DataValue materialValue = resolver.resolve(context, rawMaterial);
        String materialName = materialValue.asString();

        try {
            Material requiredMaterial = Material.valueOf(materialName.toUpperCase());
            
            // Check main hand
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand != null && mainHand.getType() == requiredMaterial) {
                return true;
            }
            
            // Check off hand
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (offHand != null && offHand.getType() == requiredMaterial) {
                return true;
            }
            
            return false;
        } catch (IllegalArgumentException e) {
            context.getPlugin().getLogger().warning("Invalid material in IsPlayerHoldingCondition: " + materialName);
            return false;
        }
    }
}