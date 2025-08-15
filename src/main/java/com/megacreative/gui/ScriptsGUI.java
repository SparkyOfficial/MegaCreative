package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.listeners.GuiListener;
import com.megacreative.models.CreativeWorld;
import java.util.ArrayList;
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
import java.util.List;

public class ScriptsGUI {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Inventory inventory;
    private int page = 0;
    
    public ScriptsGUI(MegaCreative plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§8§lМои скрипты");
        
        // Регистрируем GUI в централизованной системе
        GuiListener.registerOpenGui(player, this);
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Заполнение стеклом
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, glass);
        }
        
        // Получение скриптов текущего мира
        CreativeWorld currentWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        List<CodeScript> worldScripts = currentWorld != null ? currentWorld.getScripts() : new ArrayList<>();
        int startIndex = page * 28;
        int endIndex = Math.min(startIndex + 28, worldScripts.size());
        
        // Отображение скриптов
        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            if (slot > 43) break;
            
            CodeScript script = worldScripts.get(i);
            ItemStack scriptItem = new ItemStack(Material.BOOK);
            ItemMeta scriptMeta = scriptItem.getItemMeta();
            scriptMeta.setDisplayName("§f§l" + script.getName());
            scriptMeta.setLore(Arrays.asList(
                "§7Статус: " + (script.isEnabled() ? "§aВключен" : "§cВыключен"),
                "§7Блоков: §f" + countBlocks(script),
                "",
                "§a▶ ЛКМ - Редактировать",
                "§e▶ ПКМ - Настройки"
            ));
            scriptItem.setItemMeta(scriptMeta);
            inventory.setItem(slot, scriptItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2;
        }
        
        // Кнопка создания нового скрипта
        ItemStack createButton = new ItemStack(Material.EMERALD);
        ItemMeta createMeta = createButton.getItemMeta();
        createMeta.setDisplayName("§a§lСоздать новый скрипт");
        createMeta.setLore(Arrays.asList(
            "§7Создайте новый скрипт",
            "§7для автоматизации",
            "§e▶ Нажмите для создания"
        ));
        createButton.setItemMeta(createMeta);
        inventory.setItem(49, createButton);
        
        // Навигация
        if (page > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName("§a§lПредыдущая страница");
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(45, prevButton);
        }
        
        if (endIndex < worldScripts.size()) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName("§a§lСледующая страница");
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(53, nextButton);
        }
    }
    
    private int countBlocks(CodeScript script) {
        if (script.getRootBlock() == null) return 0;
        return countBlocksRecursive(script.getRootBlock());
    }
    
    private int countBlocksRecursive(com.megacreative.coding.CodeBlock block) {
        if (block == null) return 0;
        int count = 1;
        count += countBlocksRecursive(block.getNextBlock());
        for (com.megacreative.coding.CodeBlock child : block.getChildren()) {
            count += countBlocksRecursive(child);
        }
        return count;
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Создание нового скрипта
        if (clicked.getType() == Material.EMERALD && displayName.contains("Создать")) {
            player.closeInventory();
            // Удаляем регистрацию GUI
            GuiListener.unregisterOpenGui(player);
            player.performCommand("createscript");
            return;
        }
        
        // Навигация
        if (displayName.contains("Предыдущая страница")) {
            page--;
            setupInventory();
            return;
        }
        
        if (displayName.contains("Следующая страница")) {
            page++;
            setupInventory();
            return;
        }
        
        // Клик по скрипту
        CreativeWorld currentWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        List<CodeScript> worldScripts = currentWorld != null ? currentWorld.getScripts() : new ArrayList<>();
        int slot = event.getSlot();
        int scriptIndex = getScriptIndexFromSlot(slot);
        
        if (scriptIndex >= 0 && scriptIndex < worldScripts.size()) {
            CodeScript script = worldScripts.get(scriptIndex);
            
            if (event.isLeftClick()) {
                // Редактирование скрипта - открываем AnvilInputGUI для переименования
                player.closeInventory();
                GuiListener.unregisterOpenGui(player);
                
                // Открываем GUI для переименования
                new AnvilInputGUI(plugin, player, "Переименовать скрипт", (newName) -> {
                    if (newName != null && !newName.trim().isEmpty()) {
                        script.setName(newName.trim());
                        plugin.getWorldManager().saveWorld(currentWorld);
                        player.sendMessage("§a✓ Скрипт переименован в: " + newName.trim());
                    }
                }, () -> {
                    // Отменено
                });
                
            } else if (event.isRightClick()) {
                // Включение/выключение скрипта
                script.setEnabled(!script.isEnabled());
                plugin.getWorldManager().saveWorld(currentWorld);
                
                String status = script.isEnabled() ? "§aвключен" : "§cвыключен";
                player.sendMessage("§a✓ Скрипт '" + script.getName() + "' " + status);
                
                // Обновляем GUI
                setupInventory();
            }
        }
    }
    
    private int getScriptIndexFromSlot(int slot) {
        if (slot < 10 || slot > 43) return -1;
        
        int row = slot / 9;
        int col = slot % 9;
        
        if (col == 0 || col == 8) return -1;
        
        return (row - 1) * 7 + (col - 1) + page * 28;
    }
} 