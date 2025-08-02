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
        // Этот список нужно будет поддерживать в актуальном состоянии
        // Лучше всего вынести названия предметов в константы в классе CodingItems
        String displayName = item.getItemMeta().getDisplayName();
        return displayName.contains("§b§lСобытие игрока") ||
               displayName.contains("§6§lУсловие игрока") ||
               displayName.contains("§7§lДействие игрока") ||
               displayName.contains("§f§lПрисвоить переменную") ||
               displayName.contains("§e§lИначе") ||
               displayName.contains("§8§lИгровое действие") ||
               displayName.contains("§5§lЕсли переменная") ||
               displayName.contains("§c§lЕсли игра") ||
               displayName.contains("§d§lЕсли существо") ||
               displayName.contains("§a§lПолучить данные") ||
               displayName.contains("§e§lСвязующий жезл") ||
               displayName.contains("§b🔍 Инспектор блоков") ||
               displayName.contains("§6📋 Копировщик блоков") ||
               displayName.contains("§b§lСоздать данные");
    }

    // Запрещаем выкидывать предметы кодинга
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (isInDevWorld(player) && isCodingItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            player.sendMessage("§cВы не можете выкидывать инструменты для кодинга!");
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
                player.sendMessage("§cВы не можете перемещать предметы для кодинга!");
            }
        }
    }
} 