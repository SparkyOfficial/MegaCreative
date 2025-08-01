package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ScriptsGUI implements Listener {
    private final Player player;
    private final CreativeWorld creativeWorld;
    private final Inventory inventory;
    private final MegaCreative plugin;

    public ScriptsGUI(Player player, CreativeWorld creativeWorld, MegaCreative plugin) {
        this.player = player;
        this.creativeWorld = creativeWorld;
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(null, 54, "§8Библиотека скриптов");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupItems();
    }

    private void setupItems() {
        List<CodeScript> scripts = creativeWorld.getScripts();
        
        for (int i = 0; i < scripts.size() && i < 45; i++) {
            CodeScript script = scripts.get(i);
            inventory.setItem(i, createScriptItem(script));
        }
        
        // Кнопка "Закрыть"
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName("§cЗакрыть");
        closeButton.setItemMeta(closeMeta);
        inventory.setItem(49, closeButton);
    }

    private ItemStack createScriptItem(CodeScript script) {
        Material material = script.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String status = script.isEnabled() ? "§aВключен" : "§cВыключен";
        meta.setDisplayName("§e" + script.getName());
        meta.setLore(Arrays.asList(
            "§7Статус: " + status,
            "§7ID: " + script.getId().toString().substring(0, 8) + "...",
            "",
            "§eЛКМ: §fПереключить статус",
            "§cПКМ: §fУдалить скрипт"
        ));
        
        item.setItemMeta(meta);
        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player) || !event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        if (displayName.contains("Закрыть")) {
            player.closeInventory();
            return;
        }
        
        // Находим скрипт по имени
        String scriptName = displayName.replace("§e", "");
        CodeScript targetScript = null;
        for (CodeScript script : creativeWorld.getScripts()) {
            if (script.getName().equals(scriptName)) {
                targetScript = script;
                break;
            }
        }
        
        if (targetScript == null) return;
        
        if (event.isLeftClick()) {
            // Переключаем статус
            targetScript.setEnabled(!targetScript.isEnabled());
            player.sendMessage("§a✓ Скрипт '" + scriptName + "' " + 
                (targetScript.isEnabled() ? "включен" : "выключен"));
            
            // Обновляем GUI
            setupItems();
            
        } else if (event.isRightClick()) {
            // Удаляем скрипт
            creativeWorld.getScripts().remove(targetScript);
            player.sendMessage("§c✓ Скрипт '" + scriptName + "' удален");
            
            // Сохраняем мир
            try {
                plugin.getWorldManager().saveWorld(creativeWorld);
            } catch (Exception e) {
                player.sendMessage("§cОшибка при сохранении: " + e.getMessage());
            }
            
            // Обновляем GUI
            setupItems();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            HandlerList.unregisterAll(this);
        }
    }
} 