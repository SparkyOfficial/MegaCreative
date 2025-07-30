package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class WorldSettingsGUI implements Listener {
    
    private final MegaCreative plugin;
    private final Player player;
    private final CreativeWorld world;
    private final Inventory inventory;
    
    public WorldSettingsGUI(MegaCreative plugin, Player player, CreativeWorld world) {
        this.plugin = plugin;
        this.player = player;
        this.world = world;
        this.inventory = Bukkit.createInventory(null, 54, "§8§lНастройки: " + world.getName());
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }
    
    private void setupInventory() {
        // Заполнение стеклом
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, glass);
        }
        
        // Информация о мире
        ItemStack infoItem = new ItemStack(world.getWorldType().getIcon());
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§f§l" + world.getName());
        infoMeta.setLore(Arrays.asList(
            "§7ID: §f" + world.getId(),
            "§7Тип: §f" + world.getWorldType().getDisplayName(),
            "§7Режим: §f" + world.getMode().getDisplayName(),
            "§7Приватность: " + (world.isPrivate() ? "§cПриватный" : "§aПубличный"),
            "§7Онлайн: §f" + world.getOnlineCount(),
            "§7Рейтинг: " + (world.getRating() >= 0 ? "§a+" : "§c") + world.getRating()
        ));
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(13, infoItem);
        
        // Приватность
        ItemStack privacyItem = new ItemStack(world.isPrivate() ? Material.RED_CONCRETE : Material.GREEN_CONCRETE);
        ItemMeta privacyMeta = privacyItem.getItemMeta();
        privacyMeta.setDisplayName(world.isPrivate() ? "§c§lПриватный мир" : "§a§lПубличный мир");
        privacyMeta.setLore(Arrays.asList(
            world.isPrivate() ? "§7Мир доступен только вам и доверенным игрокам" : "§7Мир доступен всем игрокам",
            "§e▶ Нажмите для изменения"
        ));
        privacyItem.setItemMeta(privacyMeta);
        inventory.setItem(20, privacyItem);
        
        // Флаги мира
        setupFlags();
        
        // Доверенные игроки
        ItemStack trustItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta trustMeta = trustItem.getItemMeta();
        trustMeta.setDisplayName("§b§lДоверенные игроки");
        trustMeta.setLore(Arrays.asList(
            "§7Строители: §f" + world.getTrustedBuilders().size(),
            "§7Кодеры: §f" + world.getTrustedCoders().size(),
            "§e▶ Нажмите для управления"
        ));
        trustItem.setItemMeta(trustMeta);
        inventory.setItem(24, trustItem);
        
        // Удаление мира
        ItemStack deleteItem = new ItemStack(Material.TNT);
        ItemMeta deleteMeta = deleteItem.getItemMeta();
        deleteMeta.setDisplayName("§c§lУдалить мир");
        deleteMeta.setLore(Arrays.asList(
            "§7Полностью удалить этот мир",
            "§c⚠ Это действие необратимо!"
        ));
        deleteItem.setItemMeta(deleteMeta);
        inventory.setItem(49, deleteItem);
        
        // Назад
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c§lНазад");
        backButton.setItemMeta(backMeta);
        inventory.setItem(45, backButton);
    }
    
    private void setupFlags() {
        // Мобы
        ItemStack mobItem = new ItemStack(world.getFlags().isMobSpawning() ? Material.ZOMBIE_HEAD : Material.BARRIER);
        ItemMeta mobMeta = mobItem.getItemMeta();
        mobMeta.setDisplayName("§6§lСпавн мобов: " + (world.getFlags().isMobSpawning() ? "§aВКЛ" : "§cВЫКЛ"));
        mobItem.setItemMeta(mobMeta);
        inventory.setItem(29, mobItem);
        
        // PvP
        ItemStack pvpItem = new ItemStack(world.getFlags().isPvp() ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD);
        ItemMeta pvpMeta = pvpItem.getItemMeta();
        pvpMeta.setDisplayName("§6§lPvP: " + (world.getFlags().isPvp() ? "§aВКЛ" : "§cВЫКЛ"));
        pvpItem.setItemMeta(pvpMeta);
        inventory.setItem(31, pvpItem);
        
        // Взрывы
        ItemStack explosionItem = new ItemStack(world.getFlags().isExplosions() ? Material.TNT : Material.COBBLESTONE);
        ItemMeta explosionMeta = explosionItem.getItemMeta();
        explosionMeta.setDisplayName("§6§lВзрывы: " + (world.getFlags().isExplosions() ? "§aВКЛ" : "§cВЫКЛ"));
        explosionItem.setItemMeta(explosionMeta);
        inventory.setItem(33, explosionItem);
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        int slot = event.getSlot();
        
        // Назад
        if (clicked.getType() == Material.ARROW && displayName.contains("Назад")) {
            player.closeInventory();
            new MyWorldsGUI(plugin, player).open();
            return;
        }
        
        // Приватность
        if (slot == 20) {
            world.setPrivate(!world.isPrivate());
            player.sendMessage("§7Мир теперь " + (world.isPrivate() ? "§cприватный" : "§aпубличный"));
            plugin.getWorldManager().saveWorld(world);
            setupInventory();
        }
        
        // Флаги
        else if (slot == 29) { // Мобы
            world.getFlags().setMobSpawning(!world.getFlags().isMobSpawning());
            player.sendMessage("§7Спавн мобов: " + (world.getFlags().isMobSpawning() ? "§aвключен" : "§cотключен"));
            plugin.getWorldManager().saveWorld(world);
            setupInventory();
        }
        else if (slot == 31) { // PvP
            world.getFlags().setPvp(!world.getFlags().isPvp());
            player.sendMessage("§7PvP: " + (world.getFlags().isPvp() ? "§aвключен" : "§cотключен"));
            plugin.getWorldManager().saveWorld(world);
            setupInventory();
        }
        else if (slot == 33) { // Взрывы
            world.getFlags().setExplosions(!world.getFlags().isExplosions());
            player.sendMessage("§7Взрывы: " + (world.getFlags().isExplosions() ? "§aвключены" : "§cотключены"));
            plugin.getWorldManager().saveWorld(world);
            setupInventory();
        }
        
        // Доверенные игроки
        else if (slot == 24) {
            player.sendMessage("§eФункция управления доверенными игроками пока в разработке!");
        }
        
        // Удаление мира
        else if (slot == 49) {
            player.closeInventory();
            player.sendMessage("§cВведите в чат 'УДАЛИТЬ' для подтверждения удаления мира");
            // Здесь можно добавить систему подтверждения
        }
    }
}
