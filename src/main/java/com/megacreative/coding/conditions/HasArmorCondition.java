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
 * Условие для проверки, надета ли на игрока определенная броня.
 */
public class HasArmorCondition implements BlockCondition {

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
            context.getPlugin().getLogger().warning("Material not specified in HasArmorCondition");
            return false;
        }

        DataValue materialValue = resolver.resolve(context, rawMaterial);
        String materialName = materialValue.asString();

        try {
            Material requiredMaterial = Material.valueOf(materialName.toUpperCase());
            
            // Check armor slots
            ItemStack helmet = player.getInventory().getHelmet();
            ItemStack chestplate = player.getInventory().getChestplate();
            ItemStack leggings = player.getInventory().getLeggings();
            ItemStack boots = player.getInventory().getBoots();
            
            // Check if any armor piece matches the required material
            if (helmet != null && helmet.getType() == requiredMaterial) {
                return true;
            }
            
            if (chestplate != null && chestplate.getType() == requiredMaterial) {
                return true;
            }
            
            if (leggings != null && leggings.getType() == requiredMaterial) {
                return true;
            }
            
            if (boots != null && boots.getType() == requiredMaterial) {
                return true;
            }
            
            return false;
        } catch (IllegalArgumentException e) {
            context.getPlugin().getLogger().warning("Invalid material in HasArmorCondition: " + materialName);
            return false;
        }
    }
}