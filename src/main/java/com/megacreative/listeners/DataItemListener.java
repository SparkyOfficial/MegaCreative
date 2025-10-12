package com.megacreative.listeners;

import com.megacreative.MegaCreative;
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
import java.util.Optional;

public class DataItemListener implements Listener {
    
    private final MegaCreative plugin;
    
    public DataItemListener(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        
        if (DataItemFactory.isDataItem(itemInHand)) {
            event.setCancelled(true);
            
            Optional<DataItemFactory.DataItem> dataItemOpt = DataItemFactory.fromItemStack(itemInHand);
            if (!dataItemOpt.isPresent()) {
                player.sendMessage("§cОшибка! Не удалось получить данные из предмета.");
                return;
            }
            
            DataItemFactory.DataItem data = dataItemOpt.get();
            String newValue = event.getMessage();
            
            
            if (data.type() == DataType.NUMBER) {
                try {
                    Double.parseDouble(newValue);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cОшибка! Значение для типа 'Число' должно быть числом.");
                    return;
                }
            }

            
            String finalNewValue = newValue;
            DataItemFactory.DataItem finalData = data;
            event.getPlayer().getServer().getScheduler().runTask(
                plugin, 
                () -> {
                    
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