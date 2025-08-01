package com.megacreative.gui;

import com.megacreative.MegaCreative;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public class AnvilInputGUI implements Listener {

    private final Player player;
    private final Consumer<String> onComplete;
    private InventoryView view;

    public AnvilInputGUI(Player player, String initialText, Consumer<String> onComplete) {
        this.player = player;
        this.onComplete = onComplete;
        
        // Открываем инвентарь наковальни
        this.view = player.openAnvil(null, true);
        
        // Устанавливаем начальный предмет
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(initialText);
        paper.setItemMeta(meta);
        view.getTopInventory().setItem(0, paper);
        
        Bukkit.getPluginManager().registerEvents(this, MegaCreative.getInstance());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().equals(view)) return;
        event.setCancelled(true);
        
        if (event.getRawSlot() == 2) { // Слот результата
            ItemStack item = event.getCurrentItem();
            if (item != null && item.hasItemMeta()) {
                String text = item.getItemMeta().getDisplayName();
                onComplete.accept(text);
                player.closeInventory();
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().equals(view)) {
            HandlerList.unregisterAll(this);
            // Важно! Нужно очистить левый слот асинхронно, иначе предмет может выпасть
            Bukkit.getScheduler().runTaskLater(MegaCreative.getInstance(), () -> {
                event.getInventory().clear();
            }, 1L);
        }
    }
} 