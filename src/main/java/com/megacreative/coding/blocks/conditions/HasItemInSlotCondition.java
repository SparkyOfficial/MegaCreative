package com.megacreative.coding.blocks.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HasItemInSlotCondition implements BlockCondition {
    
    private final Argument<TextValue> slotArgument;
    private final Argument<TextValue> itemNameArgument;
    
    public HasItemInSlotCondition() {
        this.slotArgument = new ParameterArgument("slot");
        this.itemNameArgument = new ParameterArgument("itemName");
    }
    
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        // Получаем номер слота
        TextValue slotValue = slotArgument.parse(context.getCurrentBlock()).orElse(null);
        if (slotValue == null) return false;
        
        // Получаем имя предмета
        TextValue itemNameValue = itemNameArgument.parse(context.getCurrentBlock()).orElse(null);
        if (itemNameValue == null) return false;
        
        try {
            int slot = Integer.parseInt(slotValue.get(context));
            String itemName = itemNameValue.get(context);
            
            if (slot < 0 || slot >= 36) {
                player.sendMessage("§c✗ Неверный номер слота: " + slot);
                return false;
            }
            
            ItemStack itemInSlot = player.getInventory().getItem(slot);
            if (itemInSlot == null) return false;
            
            Material requiredMaterial = Material.valueOf(itemName.toUpperCase());
            return itemInSlot.getType() == requiredMaterial;
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Ошибка в условии: " + e.getMessage());
            return false;
        }
    }
} 