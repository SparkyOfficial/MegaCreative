package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HasItemCondition implements BlockCondition {
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawMaterial = block.getParameter("material");
        DataValue rawAmount = block.getParameter("amount");

        if (rawMaterial == null) {
            context.getPlugin().getLogger().warning("Material not specified in HasItemCondition");
            return false;
        }

        DataValue materialValue = resolver.resolve(context, rawMaterial);
        String materialName = materialValue.asString();

        int amount = 1; // Default amount
        if (rawAmount != null) {
            DataValue amountValue = resolver.resolve(context, rawAmount);
            try {
                amount = Integer.parseInt(amountValue.asString());
            } catch (NumberFormatException e) {
                // Use default amount
            }
        }

        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            return player.getInventory().contains(material, amount);
        } catch (IllegalArgumentException e) {
            context.getPlugin().getLogger().warning("Invalid material in HasItemCondition: " + materialName);
            return false;
        }
    }
}