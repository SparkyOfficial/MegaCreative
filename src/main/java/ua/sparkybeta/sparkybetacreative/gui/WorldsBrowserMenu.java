package ua.sparkybeta.sparkybetacreative.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.util.ItemBuilder;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;
import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;
import ua.sparkybeta.sparkybetacreative.listeners.MenuItemsListener;

import java.util.List;

public class WorldsBrowserMenu {

    public static final String MENU_TITLE = "Your Worlds";
    private final Inventory inventory;
    private final Player player;

    public WorldsBrowserMenu(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, net.kyori.adventure.text.Component.text(MENU_TITLE));
        initializeItems();
    }

    private void initializeItems() {
        List<SparkyWorld> worlds = SparkyBetaCreative.getInstance().getWorldManager().getPlayerWorlds(player.getUniqueId());

        if (worlds.isEmpty()) {
            inventory.setItem(22, new ItemBuilder(Material.BARRIER)
                    .setName("§cNo worlds found")
                    .setLore("§7Create one with /world create <type>")
                    .build());
            return;
        }

        int slot = 0;
        for (SparkyWorld world : worlds) {
            if (slot >= 54) break;
            inventory.setItem(slot++, new ItemBuilder(Material.GRASS_BLOCK)
                    .setName("§a" + world.getDisplayName())
                    .setLore(
                            "§7ID: " + world.getCustomId(),
                            "§7Type: " + world.getType().name(),
                            "§eLeft-click to teleport.",
                            "§cRight-click to delete."
                    ).build());
        }
    }

    public void onInventoryClick(Player player, int slot, ClickType clickType) {
        List<SparkyWorld> worlds = SparkyBetaCreative.getInstance().getWorldManager().getPlayerWorlds(player.getUniqueId());
        if (slot >= worlds.size()) return;

        SparkyWorld clickedWorld = worlds.get(slot);

        if (clickType.isLeftClick()) {
            player.closeInventory();
            SparkyBetaCreative.getInstance().getWorldManager().teleportToWorld(player, clickedWorld);
        } else if (clickType.isRightClick()) {
            // Open confirmation menu
            new ConfirmationMenu(player, "Delete " + clickedWorld.getDisplayName() + "?", (confirm) -> {
                if (confirm) {
                    player.closeInventory();
                    SparkyBetaCreative.getInstance().getWorldManager().deleteWorld(clickedWorld).thenAccept(success -> {
                        if (success) {
                            MessageUtils.sendSuccess(player, "World deleted successfully.");
                        } else {
                            MessageUtils.sendError(player, "Failed to delete world.");
                        }
                    });
                } else {
                    // Re-open browser if they cancel
                    open();
                }
            }).open();
        }
    }

    public void open() {
        player.openInventory(inventory);
        MenuItemsListener.setOpenMenu(player, this);
    }
} 