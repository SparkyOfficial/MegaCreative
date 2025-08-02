package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.listeners.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.function.Consumer;

public class AnvilInputGUI implements Listener {
    
    private final MegaCreative plugin;
    private final Player player;
    private final String title;
    private final Consumer<String> onComplete;
    private final Runnable onCancel;
    
    public AnvilInputGUI(MegaCreative plugin, Player player, String title, Consumer<String> onComplete, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.onComplete = onComplete;
        this.onCancel = onCancel;
        
        // Регистрируем GUI в централизованной системе
        GuiListener.registerOpenGui(player, this);
        openAnvil();
    }
    
    private void openAnvil() {
        // Создаем наковальню
        Inventory anvil = Bukkit.createInventory(player, 3, title);
        
        // Устанавливаем начальный предмет
        ItemStack inputItem = new ItemStack(Material.PAPER);
        ItemMeta inputMeta = inputItem.getItemMeta();
        inputMeta.setDisplayName("§7Введите текст здесь");
        inputItem.setItemMeta(inputMeta);
        anvil.setItem(0, inputItem);
        
        player.openInventory(anvil);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(player.getOpenInventory().getTopInventory())) return;
        
        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }
        
        // Проверяем, что это наковальня
        if (!(event.getInventory() instanceof AnvilInventory)) return;
        
        AnvilInventory anvil = (AnvilInventory) event.getInventory();
        
        // Если игрок нажал на результат (слот 2)
        if (event.getSlot() == 2) {
            ItemStack result = event.getCurrentItem();
            if (result != null && result.hasItemMeta()) {
                String inputText = result.getItemMeta().getDisplayName();
                if (inputText != null && !inputText.isEmpty() && !inputText.equals("§7Введите текст здесь")) {
                    // Убираем цветовые коды
                    String cleanText = inputText.replaceAll("§[0-9a-fk-or]", "");
                    
                    player.closeInventory();
                    // Удаляем регистрацию GUI
                    GuiListener.unregisterOpenGui(player);
                    
                    // Вызываем callback
                    onComplete.accept(cleanText);
                }
            }
        }
        
        // Если игрок закрыл инвентарь
        if (event.getAction().name().contains("DROP") || event.getAction().name().contains("PICKUP")) {
            // Проверяем, закрыл ли игрок инвентарь
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.getOpenInventory().getTitle().equals(title)) {
                    // Удаляем регистрацию GUI
                    GuiListener.unregisterOpenGui(player);
                    onCancel.run();
                }
            }, 1L);
        }
    }
    
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!event.getInventory().getViewers().contains(player)) return;
        
        ItemStack firstItem = event.getInventory().getItem(0);
        if (firstItem != null && firstItem.hasItemMeta()) {
            String inputText = firstItem.getItemMeta().getDisplayName();
            if (inputText != null && !inputText.isEmpty() && !inputText.equals("§7Введите текст здесь")) {
                // Создаем результат
                ItemStack result = new ItemStack(Material.PAPER);
                ItemMeta resultMeta = result.getItemMeta();
                resultMeta.setDisplayName(inputText);
                resultMeta.setLore(Arrays.asList(
                    "§7Нажмите, чтобы подтвердить",
                    "§7или закройте для отмены"
                ));
                result.setItemMeta(resultMeta);
                event.setResult(result);
            }
        }
    }
} 