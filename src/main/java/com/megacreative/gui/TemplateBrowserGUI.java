package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.CodeBlock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TemplateBrowserGUI implements Listener {
    private final Player player;
    private final Inventory inventory;
    private final List<CodeScript> templates;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 7;

    public TemplateBrowserGUI(Player player) {
        this.player = player;
        this.templates = MegaCreative.getInstance().getTemplateManager().getTemplates();
        this.inventory = Bukkit.createInventory(null, 9, "§bБраузер шаблонов");
        Bukkit.getPluginManager().registerEvents(this, MegaCreative.getInstance());
        setupInventory();
    }

    private void setupInventory() {
        inventory.clear();
        
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, templates.size());
        
        // Добавляем шаблоны для текущей страницы
        for (int i = 0; i < endIndex - startIndex; i++) {
            CodeScript template = templates.get(startIndex + i);
            ItemStack item = new ItemStack(Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            
            meta.setDisplayName("§a" + template.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Автор: §f" + template.getAuthor());
            if (!template.getDescription().isEmpty()) {
                lore.add("§7Описание: §f" + template.getDescription());
            }
            lore.add("§7Блоков: §f" + countBlocks(template.getRootBlock()));
            lore.add("§e▶ Нажмите для импорта");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            inventory.setItem(i, item);
        }
        
        // Кнопка "Закрыть"
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName("§c§lЗакрыть");
        closeButton.setItemMeta(closeMeta);
        inventory.setItem(8, closeButton);
        
        // Кнопки навигации
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName("§e§lПредыдущая страница");
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(7, prevButton);
        }
        
        if (endIndex < templates.size()) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName("§e§lСледующая страница");
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(8, nextButton);
        }
    }
    
    private int countBlocks(CodeBlock block) {
        if (block == null) return 0;
        
        int count = 1; // Текущий блок
        
        // Считаем следующий блок
        if (block.getNextBlock() != null) {
            count += countBlocks(block.getNextBlock());
        }
        
        // Считаем дочерние блоки
        for (CodeBlock child : block.getChildren()) {
            count += countBlocks(child);
        }
        
        return count;
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)) return;
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Обработка "Закрыть"
        if (displayName.contains("Закрыть")) {
            player.closeInventory();
            InventoryClickEvent.getHandlerList().unregister(this);
            return;
        }
        
        // Обработка навигации
        if (displayName.contains("Предыдущая страница")) {
            currentPage--;
            setupInventory();
            return;
        }
        if (displayName.contains("Следующая страница")) {
            currentPage++;
            setupInventory();
            return;
        }
        
        // Обработка выбора шаблона
        int clickedSlot = event.getSlot();
        int startIndex = currentPage * ITEMS_PER_PAGE;
        
        if (clickedSlot < templates.size() - startIndex) {
            CodeScript selectedTemplate = templates.get(startIndex + clickedSlot);
            importTemplate(selectedTemplate);
        }
    }
    
    private void importTemplate(CodeScript template) {
        // Проверяем, что игрок в мире разработки
        if (!player.getWorld().getName().equals("dev")) {
            player.sendMessage("§cВы должны находиться в мире разработки для импорта шаблонов!");
            return;
        }
        
        // Клонируем корневой блок
        try {
            CodeBlock clonedRoot = cloneBlock(template.getRootBlock());
            
            // Размещаем блоки в мире
            placeBlocksInWorld(clonedRoot, player.getLocation());
            
            player.sendMessage("§a✓ Шаблон '" + template.getName() + "' успешно импортирован!");
            player.sendMessage("§7Блоки размещены вокруг вас. Используйте связующий жезл для соединения.");
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка импорта шаблона: " + e.getMessage());
        }
    }
    
    private CodeBlock cloneBlock(CodeBlock original) {
        if (original == null) return null;
        
        CodeBlock cloned = new CodeBlock(original.getMaterial(), original.getAction());
        cloned.setId(UUID.randomUUID()); // Новый уникальный ID
        
        // Копируем параметры
        cloned.getParameters().putAll(original.getParameters());
        
        // Рекурсивно клонируем следующий блок
        if (original.getNextBlock() != null) {
            cloned.setNext(cloneBlock(original.getNextBlock()));
        }
        
        // Рекурсивно клонируем дочерние блоки
        for (CodeBlock child : original.getChildren()) {
            cloned.addChild(cloneBlock(child));
        }
        
        return cloned;
    }
    
    private void placeBlocksInWorld(CodeBlock block, Location centerLocation) {
        if (block == null) return;
        
        // Размещаем текущий блок
        Location blockLocation = centerLocation.clone().add(0, 0, 0);
        blockLocation.getBlock().setType(block.getMaterial());
        
        // Добавляем в систему блоков
        MegaCreative.getInstance().getBlockPlacementHandler().getBlockCodeBlocks().put(blockLocation, block);
        
        // Устанавливаем табличку
        setSignOnBlock(blockLocation, block.getAction());
        
        // Рекурсивно размещаем следующий блок
        if (block.getNextBlock() != null) {
            Location nextLocation = centerLocation.clone().add(2, 0, 0);
            placeBlocksInWorld(block.getNextBlock(), nextLocation);
        }
        
        // Рекурсивно размещаем дочерние блоки
        int childIndex = 0;
        for (CodeBlock child : block.getChildren()) {
            Location childLocation = centerLocation.clone().add(0, 2, childIndex * 2);
            placeBlocksInWorld(child, childLocation);
            childIndex++;
        }
    }
    
    private void setSignOnBlock(Location location, String action) {
        // Простая реализация установки таблички
        // В реальности нужно использовать BlockPlacementHandler.setSignOnBlock
        if (location.getBlock().getType().name().contains("SIGN")) {
            // Устанавливаем табличку с действием
            // Это упрощенная версия, в реальности нужно использовать Bukkit API для табличек
        }
    }
} 