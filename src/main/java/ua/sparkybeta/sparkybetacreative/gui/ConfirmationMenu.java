package ua.sparkybeta.sparkybetacreative.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ua.sparkybeta.sparkybetacreative.util.ItemBuilder;
import ua.sparkybeta.sparkybetacreative.listeners.MenuItemsListener;

import java.util.function.Consumer;

public class ConfirmationMenu {

    public static final String MENU_TITLE = "Are you sure?";
    private final Inventory inventory;
    private final Player player;
    private final Consumer<Boolean> callback;

    public ConfirmationMenu(Player player, String title, Consumer<Boolean> callback) {
        this.player = player;
        this.callback = callback;
        this.inventory = Bukkit.createInventory(null, 27, net.kyori.adventure.text.Component.text(title));
        initializeItems();
    }

    private void initializeItems() {
        inventory.setItem(11, new ItemBuilder(Material.GREEN_WOOL)
                .setName("§aConfirm")
                .build());

        inventory.setItem(15, new ItemBuilder(Material.RED_WOOL)
                .setName("§cCancel")
                .build());
    }

    public void onInventoryClick(int slot) {
        if (slot == 11) {
            callback.accept(true);
        } else if (slot == 15) {
            callback.accept(false);
        }
    }

    public void open() {
        player.openInventory(inventory);
        MenuItemsListener.setOpenMenu(player, this);
    }
} 