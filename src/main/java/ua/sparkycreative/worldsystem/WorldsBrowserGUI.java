package ua.sparkycreative.worldsystem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class WorldsBrowserGUI implements Listener {
    private static final String GUI_TITLE = "§bВаши миры";
    private final WorldManager worldManager;

    public WorldsBrowserGUI(WorldManager worldManager) {
        this.worldManager = worldManager;
        Bukkit.getPluginManager().registerEvents(this, WorldSystemPlugin.getInstance());
    }

    public void open(Player player) {
        List<String> worlds = worldManager.getWorlds(player.getUniqueId());
        int size = 9 * 3;
        Inventory inv = Bukkit.createInventory(null, size, GUI_TITLE);
        int i = 0;
        for (String world : worlds) {
            ItemStack item = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§aМир: " + world);
            meta.setLore(List.of("§7[ЛКМ] Войти", "§c[ПКМ] Удалить", "§e[Q] dev-мир"));
            item.setItemMeta(meta);
            inv.setItem(i++, item);
        }
        // Кнопка создания мира
        if (worlds.size() < 3) {
            ItemStack create = new ItemStack(Material.EMERALD_BLOCK);
            ItemMeta meta = create.getItemMeta();
            meta.setDisplayName("§aСоздать новый мир");
            create.setItemMeta(meta);
            inv.setItem(size - 1, create);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (!e.getView().getTitle().equals(GUI_TITLE)) return;
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        String name = item.getItemMeta().getDisplayName();
        if (name == null) return;
        if (name.startsWith("§aМир: ")) {
            String worldName = name.substring(8);
            if (e.isLeftClick()) {
                // Телепорт в мир
                WorldTeleportUtil.teleportToWorld(player, worldName);
                player.closeInventory();
            } else if (e.isRightClick()) {
                // Удалить мир
                boolean deleted = worldManager.deleteWorld(player.getUniqueId(), worldName);
                if (deleted) {
                    player.sendMessage("§cМир удалён: " + worldName);
                    open(player);
                } else {
                    player.sendMessage("§cОшибка удаления мира.");
                }
            } else if (e.getClick().isKeyboardClick() && e.getHotbarButton() == 0) {
                // dev-мир (Q)
                WorldTeleportUtil.teleportToWorld(player, worldName + "_dev");
                player.closeInventory();
            }
        } else if (name.equals("§aСоздать новый мир")) {
            if (worldManager.canCreateWorld(player.getUniqueId())) {
                String newWorld = worldManager.createWorld(player.getUniqueId(), "world");
                if (newWorld != null) {
                    player.sendMessage("§aМир создан: " + newWorld);
                    open(player);
                } else {
                    player.sendMessage("§cОшибка создания мира.");
                }
            } else {
                player.sendMessage("§cДостигнут лимит миров.");
            }
        }
    }
} 