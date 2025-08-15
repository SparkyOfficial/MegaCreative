package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class OpenChestGUIAction implements BlockAction {
    
    private final Argument<TextValue> titleArgument;
    private final Argument<TextValue> sizeArgument;
    
    public OpenChestGUIAction() {
        this.titleArgument = new ParameterArgument("title");
        this.sizeArgument = new ParameterArgument("size");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // Получаем параметры
        TextValue titleValue = titleArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue sizeValue = sizeArgument.parse(context.getCurrentBlock()).orElse(null);
        
        try {
            String title = titleValue != null ? titleValue.get(context) : "Сундук";
            int size = sizeValue != null ? Integer.parseInt(sizeValue.get(context)) : 27;
            
            // Ограничиваем размер
            if (size < 9) size = 9;
            if (size > 54) size = 54;
            if (size % 9 != 0) size = (size / 9) * 9;
            
            // Создаем инвентарь
            Inventory inventory = Bukkit.createInventory(null, size, "§8" + title);
            
            // Добавляем декоративные элементы
            fillInventoryWithDecoration(inventory);
            
            // Открываем GUI
            player.openInventory(inventory);
            player.sendMessage("§a✓ Открыт GUI: " + title);
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Ошибка: " + e.getMessage());
        }
    }
    
    private void fillInventoryWithDecoration(Inventory inventory) {
        // Заполняем границы стеклянными панелями
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);
        
        // Верхняя и нижняя границы
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(inventory.getSize() - 9 + i, border);
        }
        
        // Боковые границы
        for (int i = 0; i < inventory.getSize(); i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }
} 