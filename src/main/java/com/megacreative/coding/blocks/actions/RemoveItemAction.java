package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RemoveItemAction implements BlockAction {
    
    private final Argument<TextValue> itemNameArgument;
    private final Argument<TextValue> amountArgument;
    
    public RemoveItemAction() {
        this.itemNameArgument = new ParameterArgument("itemName");
        this.amountArgument = new ParameterArgument("amount");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // Получаем имя предмета
        TextValue itemNameValue = itemNameArgument.parse(context.getCurrentBlock()).orElse(null);
        if (itemNameValue == null) {
            player.sendMessage("§cОшибка: не указано имя предмета!");
            return;
        }
        
        // Получаем количество
        TextValue amountValue = amountArgument.parse(context.getCurrentBlock()).orElse(null);
        if (amountValue == null) {
            player.sendMessage("§cОшибка: не указано количество!");
            return;
        }
        
        try {
            String itemName = itemNameValue.get(context);
            String amountStr = amountValue.get(context);
            
            Material material = Material.valueOf(itemName.toUpperCase());
            int amount = Integer.parseInt(amountStr);
            
            ItemStack itemToRemove = new ItemStack(material, amount);
            player.getInventory().removeItem(itemToRemove);
            
            player.sendMessage("§a✓ Удалено " + amount + "x " + itemName);
        } catch (Exception e) {
            player.sendMessage("§c✗ Ошибка: " + e.getMessage());
        }
    }
} 