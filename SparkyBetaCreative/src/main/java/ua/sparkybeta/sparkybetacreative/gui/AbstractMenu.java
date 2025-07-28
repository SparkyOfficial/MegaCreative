package ua.sparkybeta.sparkybetacreative.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractMenu implements InventoryHolder, Listener {

    protected final Player player;
    protected Inventory inventory;
    private final Map<Integer, Consumer<InventoryClickEvent>> buttonActions = new HashMap<>();

    public AbstractMenu(Player player, int size, String title) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, size, net.kyori.adventure.text.Component.text(title));
    }

    protected void setButton(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        inventory.setItem(slot, item);
        buttonActions.put(slot, action);
    }

    public void open() {
        player.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(this, SparkyBetaCreative.getInstance());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().getHolder().equals(this)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        event.setCancelled(true);

        Consumer<InventoryClickEvent> action = buttonActions.get(event.getSlot());
        if (action != null) {
            action.accept(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder().equals(this)) {
            HandlerList.unregisterAll(this);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
} 