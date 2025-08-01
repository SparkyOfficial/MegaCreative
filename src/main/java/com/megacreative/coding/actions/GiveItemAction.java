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
        
        Object rawItemName = block.getParameter("item");
        Object rawAmount = block.getParameter("amount");
        
        String itemName = ParameterResolver.resolve(context, rawItemName).toString();
        int amount = Integer.parseInt(ParameterResolver.resolve(context, rawAmount).toString());
        
        if (itemName != null) {
            try {
                Material material = Material.valueOf(itemName.toUpperCase());
                ItemStack item = new ItemStack(material, amount);
                player.getInventory().addItem(item);
                player.sendMessage("§aПолучен предмет: " + itemName + " x" + amount);
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cНеизвестный предмет: " + itemName);
            }
        }
    }
} 