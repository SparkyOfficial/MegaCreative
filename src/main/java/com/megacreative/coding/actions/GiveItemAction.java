package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveItemAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // 1. Получаем "сырые" значения из параметров блока
        Object rawItemName = block.getParameter("item");
        Object rawAmount = block.getParameter("amount");

        // 2. "Разрешаем" их с помощью ParameterResolver
        String itemName = ParameterResolver.resolve(context, rawItemName);
        String amountStr = ParameterResolver.resolve(context, rawAmount);

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