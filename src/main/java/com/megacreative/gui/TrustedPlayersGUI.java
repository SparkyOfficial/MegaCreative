package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.models.TrustedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * GUI для управления доверенными игроками
 */
public class TrustedPlayersGUI {

    private final MegaCreative plugin;
    private final Player player;
    private final Inventory inventory;

    public TrustedPlayersGUI(MegaCreative plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§8Управление доверенными игроками");
        setupGUI();
    }

    private void setupGUI() {
        // Заполняем фон
        ItemStack background = createItem(Material.BLACK_STAINED_GLASS_PANE, "§7", "");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, background);
        }

        // Заголовок
        inventory.setItem(4, createItem(Material.SHIELD, "§e§lДоверенные игроки", 
            "§7Управление правами доступа"));

        // Кнопки действий
        inventory.setItem(19, createItem(Material.EMERALD, "§a§lДобавить строителя", 
            "§7Добавить доверенного строителя",
            "§7ПКМ для выбора игрока"));
        inventory.setItem(21, createItem(Material.LAPIS_LAZULI, "§b§lДобавить программиста", 
            "§7Добавить доверенного программиста",
            "§7ПКМ для выбора игрока"));
        inventory.setItem(23, createItem(Material.REDSTONE, "§c§lУдалить игрока", 
            "§7Удалить из доверенных",
            "§7ПКМ для выбора игрока"));
        inventory.setItem(25, createItem(Material.BOOK, "§6§lИнформация", 
            "§7Просмотр информации о правах",
            "§7ПКМ для выбора игрока"));

        // Список доверенных игроков
        displayTrustedPlayers();
    }

    private void displayTrustedPlayers() {
        List<TrustedPlayer> allTrusted = plugin.getTrustedPlayerManager().getAllTrustedPlayers();
        
        if (allTrusted.isEmpty()) {
            inventory.setItem(31, createItem(Material.BARRIER, "§cНет доверенных игроков", 
                "§7Список пуст"));
            return;
        }

        int slot = 28;
        for (TrustedPlayer trusted : allTrusted) {
            if (slot >= 53) break; // Не выходим за границы инвентаря
            
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setDisplayName("§f" + trusted.getPlayerName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Тип: §f" + trusted.getType().getDisplayName());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String addedDate = sdf.format(new Date(trusted.getAddedAt()));
            lore.add("§7Добавлен: §f" + addedDate);
            lore.add("§7Добавил: §f" + trusted.getAddedBy());
            lore.add("");
            lore.add("§7ПКМ для удаления");
            
            meta.setLore(lore);
            head.setItemMeta(meta);
            
            inventory.setItem(slot, head);
            slot++;
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public Inventory getInventory() {
        return inventory;
    }
} 