package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveItemAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;
        
        String itemName = (String) block.getParameter("item");
        int amount = (int) block.getParameter("amount");
        
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