package com.megacreative.listeners;

import com.megacreative.coding.data.DataItemFactory;
import com.megacreative.coding.data.DataType;
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

            // Schedule the inventory update synchronously
            String finalNewValue = newValue;
            DataItemFactory.DataItem finalData = data;
            event.getPlayer().getServer().getScheduler().runTask(
                com.megacreative.MegaCreative.getInstance(), 
                () -> {
                    // Обновляем NBT и Lore предмета
                    ItemStack currentItem = player.getInventory().getItemInMainHand();
                    if (DataItemFactory.isDataItem(currentItem)) {
                        ItemMeta meta = currentItem.getItemMeta();
                        meta.getPersistentDataContainer().set(DataItemFactory.getDataValueKey(), PersistentDataType.STRING, finalNewValue);
                        meta.setLore(Arrays.asList(
                            "§7Тип: §f" + finalData.type().name(),
                            "§7Значение: §f" + finalNewValue
                        ));
                        currentItem.setItemMeta(meta);
                        player.getInventory().setItemInMainHand(currentItem);
                    }

                    player.sendMessage("§a✓ Значение для предмета '" + finalData.type().getDisplayName() + "' установлено на: §e" + finalNewValue);
                }
            );
        }
    }
}