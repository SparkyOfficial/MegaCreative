package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ScriptEditorGUI implements Listener {
    
    private final MegaCreative plugin;
    private final Player player;
    private final CreativeWorld world;
    private final Inventory inventory;
    private final List<CodeScript> scripts;
    
    public ScriptEditorGUI(MegaCreative plugin, Player player, CreativeWorld world) {
        this.plugin = plugin;
        this.player = player;
        this.world = world;
        this.scripts = world.getScripts();
        
        // Создаем инвентарь
        int size = Math.max(27, ((scripts.size() + 2) / 9 + 1) * 9); // Минимум 27 слотов
        this.inventory = Bukkit.createInventory(null, size, "§8Редактор скриптов");
        
        // Заполняем скриптами
        fillScripts();
        
        // Регистрируем слушатель
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void fillScripts() {
        inventory.clear();
        
        // Добавляем заголовок
        ItemStack header = new ItemStack(Material.BOOK);
        ItemMeta headerMeta = header.getItemMeta();
        headerMeta.setDisplayName("§eРедактор скриптов");
        headerMeta.setLore(Arrays.asList(
            "§7Кликните на скрипт для управления",
            "§7Зеленый = активен, Красный = неактивен"
        ));
        header.setItemMeta(headerMeta);
        inventory.setItem(4, header);
        
        // Добавляем кнопку создания нового скрипта
        ItemStack createButton = new ItemStack(Material.EMERALD);
        ItemMeta createMeta = createButton.getItemMeta();
        createMeta.setDisplayName("§a+ Создать новый скрипт");
        createMeta.setLore(Arrays.asList(
            "§7Создать новый скрипт",
            "§7Используйте /savescript для сохранения"
        ));
        createButton.setItemMeta(createMeta);
        inventory.setItem(0, createButton);
        
        // Добавляем скрипты
        for (int i = 0; i < scripts.size(); i++) {
            CodeScript script = scripts.get(i);
            ItemStack scriptItem = createScriptItem(script, i);
            inventory.setItem(i + 9, scriptItem);
        }
    }
    
    private ItemStack createScriptItem(CodeScript script, int index) {
        Material material = script.isEnabled() ? Material.LIME_DYE : Material.RED_DYE;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String status = script.isEnabled() ? "§aАктивен" : "§cНеактивен";
        String type = script.getType() == CodeScript.ScriptType.FUNCTION ? "Функция" : "Событие";
        
        meta.setDisplayName("§f" + script.getName());
        meta.setLore(Arrays.asList(
            "§7Тип: §e" + type,
            "§7Статус: " + status,
            "§7Автор: §e" + (script.getAuthor() != null ? script.getAuthor() : "Неизвестно"),
            "",
            "§7ЛКМ - Переименовать",
            "§7ПКМ - Включить/выключить",
            "§7Shift+ЛКМ - Удалить"
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() != inventory) return;
        if (event.getWhoClicked() != player) return;
        
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        
        // Кнопка создания нового скрипта
        if (slot == 0) {
            player.sendMessage("§a✓ Используйте /savescript для создания нового скрипта");
            player.closeInventory();
            return;
        }
        
        // Скрипты начинаются с 9-го слота
        if (slot >= 9) {
            int scriptIndex = slot - 9;
            if (scriptIndex < scripts.size()) {
                CodeScript script = scripts.get(scriptIndex);
                handleScriptClick(script, event.isRightClick(), event.isShiftClick());
            }
        }
    }
    
    private void handleScriptClick(CodeScript script, boolean isRightClick, boolean isShiftClick) {
        if (isShiftClick) {
            // Удаление скрипта
            handleScriptDelete(script);
        } else if (isRightClick) {
            // Включение/выключение скрипта
            handleScriptToggle(script);
        } else {
            // Переименование скрипта
            handleScriptRename(script);
        }
    }
    
    private void handleScriptDelete(CodeScript script) {
        player.sendMessage("§c⚠ Удаление скрипта '" + script.getName() + "'");
        player.sendMessage("§7Для подтверждения введите: §f/confirm delete " + script.getName());
        
        // Сохраняем информацию о подтверждении
        plugin.getDeleteConfirmations().put(player.getUniqueId(), "delete " + script.getName());
        
        player.closeInventory();
    }
    
    private void handleScriptToggle(CodeScript script) {
        script.setEnabled(!script.isEnabled());
        plugin.getWorldManager().saveWorld(world);
        
        String status = script.isEnabled() ? "§aвключен" : "§cвыключен";
        player.sendMessage("§a✓ Скрипт '" + script.getName() + "' " + status);
        
        // Обновляем GUI
        fillScripts();
    }
    
    private void handleScriptRename(CodeScript script) {
        player.sendMessage("§e✏ Введите новое имя для скрипта '" + script.getName() + "'");
        player.sendMessage("§7Используйте: §f/rename " + script.getName() + " <новое_имя>");
        
        player.closeInventory();
    }
    
    // Статические методы для удобного создания GUI
    
    public static void openForPlayer(Player player) {
        CreativeWorld world = MegaCreative.getInstance().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (world == null) {
            player.sendMessage("§cВы должны находиться в творческом мире!");
            return;
        }
        
        ScriptEditorGUI gui = new ScriptEditorGUI(MegaCreative.getInstance(), player, world);
        gui.open();
    }
} 