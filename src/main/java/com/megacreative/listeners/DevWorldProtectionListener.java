package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DevWorldProtectionListener implements Listener {

    private final MegaCreative plugin;

    public DevWorldProtectionListener(MegaCreative plugin) {
        this.plugin = plugin;
    }

    private boolean isInDevWorld(Player player) {
        return player.getWorld().getName().endsWith("_dev");
    }

    private boolean isCodingItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        String displayName = item.getItemMeta().getDisplayName();
        return displayName.contains(CodingItems.EVENT_BLOCK_NAME) ||
               displayName.contains(CodingItems.CONDITION_BLOCK_NAME) ||
               displayName.contains(CodingItems.ACTION_BLOCK_NAME) ||
               displayName.contains(CodingItems.VARIABLE_BLOCK_NAME) ||
               displayName.contains(CodingItems.ELSE_BLOCK_NAME) ||
               displayName.contains(CodingItems.GAME_ACTION_BLOCK_NAME) ||
               displayName.contains(CodingItems.IF_VAR_BLOCK_NAME) ||
               displayName.contains(CodingItems.IF_GAME_BLOCK_NAME) ||
               displayName.contains(CodingItems.IF_MOB_BLOCK_NAME) ||
               displayName.contains(CodingItems.GET_DATA_BLOCK_NAME) ||
<<<<<<< HEAD
               displayName.contains(CodingItems.LINKER_TOOL_NAME) ||
               displayName.contains(CodingItems.INSPECTOR_TOOL_NAME) ||
=======

>>>>>>> ba7215a (Я вернулся)
               displayName.contains(CodingItems.COPIER_TOOL_NAME) ||
               displayName.contains(CodingItems.DATA_CREATOR_NAME);
    }

    // Запрещаем выкидывать предметы кодинга
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (isInDevWorld(player) && isCodingItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
<<<<<<< HEAD
            player.sendMessage("§cВы не можете выкидывать инструменты для кодинга!");
=======
>>>>>>> ba7215a (Я вернулся)
        }
    }

    // Запрещаем перемещать предметы кодинга в инвентаре (например, в сундук)
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!isInDevWorld(player)) return;

        ItemStack clickedItem = event.getCurrentItem();

        if (isCodingItem(clickedItem)) {
            // Разрешаем клики в своем инвентаре (hotbar/main), но отменяем любые другие
            if (event.getClickedInventory() != player.getInventory()) {
                event.setCancelled(true);
<<<<<<< HEAD
                player.sendMessage("§cВы не можете перемещать предметы для кодинга!");
=======
>>>>>>> ba7215a (Я вернулся)
            }
        }
    }
} 