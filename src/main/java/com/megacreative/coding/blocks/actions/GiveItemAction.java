package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.NumberParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import com.megacreative.coding.values.NumberValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Действие для выдачи предмета игроку.
 * Поддерживает получение названия предмета и количества из параметров.
 */
public class GiveItemAction implements BlockAction {
    
    // Аргументы для получения данных
    private final Argument<TextValue> itemNameArgument = new ParameterArgument("item");
    private final Argument<NumberValue> amountArgument = new NumberParameterArgument("amount");
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // 1. Получаем название предмета
        TextValue itemNameValue = itemNameArgument.parse(context.getCurrentBlock()).orElse(null);
        if (itemNameValue == null) {
            player.sendMessage("§cОшибка: не указан предмет!");
            return;
        }
        
        // 2. Получаем количество
        NumberValue amountValue = amountArgument.parse(context.getCurrentBlock()).orElse(new NumberValue(1.0));
        
        try {
            // 3. Вычисляем значения
            String itemName = itemNameValue.get(context);
            int amount = amountValue.get(context).intValue();
            
            // 4. Создаем предмет
            Material material = Material.valueOf(itemName.toUpperCase());
            ItemStack itemStack = new ItemStack(material, amount);
            
            // 5. Выдаем предмет
            player.getInventory().addItem(itemStack);
            player.sendMessage("§a✓ Вы получили " + amount + "x " + material.name());
            
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cОшибка: неверный материал '" + itemNameValue.getRawText() + "'");
        } catch (Exception e) {
            player.sendMessage("§cОшибка в блоке 'Выдать предмет': " + e.getMessage());
        }
    }
} 