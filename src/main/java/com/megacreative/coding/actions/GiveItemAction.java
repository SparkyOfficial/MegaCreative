package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveItemAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        ParameterResolver resolver = new ParameterResolver(context);

        // 1. Получаем "сырые" значения из параметров блока
        DataValue rawItemName = block.getParameter("item");
        DataValue rawAmount = block.getParameter("amount");
        
        if (rawItemName == null || rawAmount == null) return;

        // 2. "Разрешаем" их с помощью ParameterResolver
        String itemName = resolver.resolve(context, rawItemName).asString();
        String amountStr = resolver.resolve(context, rawAmount).asString();

        try {
            Material material = Material.valueOf(itemName.toUpperCase());
            int amount = Integer.parseInt(amountStr);

            player.getInventory().addItem(new ItemStack(material, amount));
            player.sendMessage("§a✓ Вы получили " + amount + "x " + material.name());
        } catch (Exception e) {
            player.sendMessage("§cОшибка в блоке 'Выдать предмет': неверный материал или количество.");
        }
    }
} 