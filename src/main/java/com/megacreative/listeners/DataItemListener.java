package com.megacreative.listeners;

import com.megacreative.coding.data.DataItemFactory;
import com.megacreative.coding.data.DataType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class DataItemListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Проверяем, держит ли игрок наш предмет-данные
        if (DataItemFactory.isDataItem(itemInHand)) {
            event.setCancelled(true);
            
            DataItemFactory.DataItem data = DataItemFactory.fromItemStack(itemInHand).get();
            String newValue = event.getMessage();
            
            // Валидация
            if (data.type() == DataType.NUMBER) {
                try {
                    Double.parseDouble(newValue);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cОшибка! Значение для типа 'Число' должно быть числом.");
                    return;
                }
            }

            // Обновляем NBT и Lore предмета
            ItemMeta meta = itemInHand.getItemMeta();
            meta.getPersistentDataContainer().set(DataItemFactory.DATA_VALUE_KEY, PersistentDataType.STRING, newValue);
            meta.setLore(Arrays.asList(
                "§7Тип: §f" + data.type().name(),
                "§7Значение: §f" + newValue
            ));
            itemInHand.setItemMeta(meta);

            player.sendMessage("§a✓ Значение для предмета '" + data.type().getDisplayName() + "' установлено на: §e" + newValue);
        }
    }
} 