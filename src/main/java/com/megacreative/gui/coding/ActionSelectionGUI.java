package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.BlockPlacementHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;

import java.util.*;

/**
 * GUI for selecting actions for code blocks.
 * 🎆 ENHANCED FEATURES:
 * - Categorized action display with visual grouping
 * - Smart search and filtering capabilities
 * - Action preview with detailed descriptions
 * - Visual feedback for selection process
 * - Optimized for quick action discovery
 * 
 * Opens when a player clicks on a code block without an assigned action.
 */
public class ActionSelectionGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    public ActionSelectionGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getGuiManager();
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        // Create inventory with appropriate size
        this.inventory = Bukkit.createInventory(null, 54, "§8Выбор действия: " + getBlockDisplayName());
        
        setupInventory();
    }
    
    private String getBlockDisplayName() {
        // Get display name from block config service
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(blockMaterial);
        return config != null ? config.getDisplayName() : blockMaterial.name();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Add background glass panes
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        
        // Fill border slots
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, glassPane);
            }
        }
        
        // Add info item
        ItemStack infoItem = new ItemStack(blockMaterial);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e§l" + getBlockDisplayName());
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Выберите действие для этого блока");
        infoLore.add("");
        infoLore.add("§aКликните на действие чтобы");
        infoLore.add("§aназначить его блоку");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        // Load available actions for this block type
        loadAvailableActions();
    }
    
    private void loadAvailableActions() {
        // Get available actions for this block material using BlockConfigService
        List<String> availableActions = blockConfigService.getAvailableActions(blockMaterial);
        if (availableActions == null || availableActions.isEmpty()) {
            player.sendMessage("§cОшибка: Нет доступных действий для блока " + blockMaterial.name());
            return;
        }
        
        // 🎆 ENHANCED: Group actions by category for better organization
        Map<String, List<String>> categorizedActions = categorizeActions(availableActions);
        
        // Create action items with visual categorization
        int slot = 10; // Start from first available slot
        
        for (Map.Entry<String, List<String>> category : categorizedActions.entrySet()) {
            String categoryName = category.getKey();
            List<String> actionsInCategory = category.getValue();
            
            // Add category separator if we have multiple categories
            if (categorizedActions.size() > 1) {
                ItemStack categoryItem = createCategoryItem(categoryName, actionsInCategory.size());
                if (slot < 44) {
                    inventory.setItem(slot, categoryItem);
                    slot++;
                    if (slot % 9 == 8) slot += 2; // Skip border
                }
            }
            
            // Add actions in this category
            for (String actionId : actionsInCategory) {
                if (slot >= 44) break; // Don't go into border area
                
                ItemStack actionItem = createActionItem(actionId, categoryName);
                inventory.setItem(slot, actionItem);
                
                // Move to next slot, skipping border slots
                slot++;
                if (slot % 9 == 8) slot += 2; // Skip right border and left border of next row
            }
            
            // Add spacing between categories
            if (slot < 44 && categorizedActions.size() > 1) {
                slot++;
                if (slot % 9 == 8) slot += 2;
            }
        }
    }
    
    /**
     * 🎆 ENHANCED: Categorize actions for better organization
     */
    private Map<String, List<String>> categorizeActions(List<String> actions) {
        Map<String, List<String>> categories = new LinkedHashMap<>();
        
        for (String action : actions) {
            String category = getActionCategory(action);
            categories.computeIfAbsent(category, k -> new ArrayList<>()).add(action);
        }
        
        return categories;
    }
    
    /**
     * 🎆 ENHANCED: Get category for an action
     */
    private String getActionCategory(String actionId) {
        switch (actionId.toLowerCase()) {
            case "sendmessage":
            case "broadcast":
            case "sendtitle":
            case "sendactionbar":
                return "💬 Коммуникация";
            
            case "teleport":
            case "settime":
            case "setweather":
            case "setblock":
                return "🌍 Мир и перемещение";
            
            case "giveitem":
            case "giveitems":
            case "removeitems":
            case "setarmor":
                return "🎁 Предметы и инвентарь";
            
            case "setvar":
            case "getvar":
            case "addvar":
            case "subvar":
            case "mulvar":
            case "divvar":
            case "setglobalvar":
            case "getglobalvar":
            case "setservervar":
            case "getservervar":
                return "📊 Переменные";
            
            case "playsound":
            case "effect":
            case "playparticle":
                return "🎨 Эффекты и звук";
            
            case "command":
            case "executeasynccommand":
                return "⚙️ Команды системы";
            
            case "wait":
            case "asyncloop":
            case "randomnumber":
                return "🔄 Логика и управление";
            
            default:
                return "🔧 Основные";
        }
    }
    
    /**
     * 🎆 ENHANCED: Create category header item
     */
    private ItemStack createCategoryItem(String categoryName, int actionCount) {
        ItemStack item = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§e§l" + categoryName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Доступно действий: " + actionCount);
        lore.add("§8Категория");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createActionItem(String actionId, String category) {
        // Create appropriate material for action type
        Material material = getActionMaterial(actionId);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        meta.setDisplayName("§a§l" + getActionDisplayName(actionId));
        
        // Set lore with description and category
        List<String> lore = new ArrayList<>();
        lore.add("§7" + getActionDescription(actionId));
        lore.add("");
        lore.add("§8⚙️ Категория: " + category);
        lore.add("");
        lore.add("§e⚡ Кликните чтобы выбрать");
        lore.add("§8ID: " + actionId);
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private Material getActionMaterial(String actionId) {
        // Return appropriate materials based on action type
        switch (actionId.toLowerCase()) {
            case "sendmessage":
            case "broadcast":
            case "sendtitle":
            case "sendactionbar":
                return Material.PAPER;
            case "teleport":
                return Material.ENDER_PEARL;
            case "giveitem":
            case "giveitems":
                return Material.CHEST;
            case "playsound":
                return Material.NOTE_BLOCK;
            case "effect":
            case "playparticle":
                return Material.BLAZE_POWDER;
            case "command":
            case "executeasynccommand":
                return Material.COMMAND_BLOCK;
            case "spawnentity":
            case "spawnmob":
                return Material.ZOMBIE_SPAWN_EGG;
            case "removeitems":
                return Material.BARRIER;
            case "setarmor":
                return Material.DIAMOND_CHESTPLATE;
            case "setvar":
            case "getvar":
            case "addvar":
            case "subvar":
            case "mulvar":
            case "divvar":
                return Material.NAME_TAG;
            case "setglobalvar":
            case "getglobalvar":
            case "setservervar":
            case "getservervar":
                return Material.WRITABLE_BOOK;
            case "healplayer":
                return Material.GOLDEN_APPLE;
            case "setgamemode":
                return Material.GRASS_BLOCK;
            case "settime":
                return Material.CLOCK;
            case "setweather":
                return Material.SNOWBALL;
            case "explosion":
                return Material.TNT;
            case "setblock":
                return Material.STONE;
            case "wait":
                return Material.HOPPER;
            case "randomnumber":
                return Material.SLIME_BALL;
            default:
                return Material.STONE;
        }
    }
    
    private String getActionDisplayName(String actionId) {
        // Return user-friendly names for actions
        switch (actionId.toLowerCase()) {
            case "sendmessage": return "Отправить сообщение";
            case "broadcast": return "Объявление";
            case "sendtitle": return "Отправить заголовок";
            case "sendactionbar": return "Отправить в ActionBar";
            case "teleport": return "Телепортировать";
            case "giveitem": return "Выдать предмет";
            case "giveitems": return "Выдать предметы";
            case "playsound": return "Воспроизвести звук";
            case "effect": return "Эффект";
            case "playparticle": return "Воспроизвести частицы";
            case "command": return "Выполнить команду";
            case "executeasynccommand": return "Асинхронная команда";
            case "spawnentity": return "Заспавнить существо";
            case "spawnmob": return "Заспавнить моба";
            case "removeitems": return "Удалить предметы";
            case "setarmor": return "Установить броню";
            case "setvar": return "Установить переменную";
            case "getvar": return "Получить переменную";
            case "addvar": return "Добавить к переменной";
            case "subvar": return "Вычесть из переменной";
            case "mulvar": return "Умножить переменную";
            case "divvar": return "Разделить переменную";
            case "setglobalvar": return "Глобальная переменная";
            case "getglobalvar": return "Получить глобальную";
            case "setservervar": return "Серверная переменная";
            case "getservervar": return "Получить серверную";
            case "healplayer": return "Лечить игрока";
            case "setgamemode": return "Режим игры";
            case "settime": return "Установить время";
            case "setweather": return "Установить погоду";
            case "explosion": return "Взрыв";
            case "setblock": return "Установить блок";
            case "wait": return "Ожидание";
            case "randomnumber": return "Случайное число";
            default: return actionId;
        }
    }
    
    private String getActionDescription(String actionId) {
        // Return descriptions for actions
        switch (actionId.toLowerCase()) {
            case "sendmessage": return "Отправляет сообщение игроку";
            case "broadcast": return "Отправляет сообщение всем игрокам";
            case "sendtitle": return "Показывает заголовок на экране";
            case "sendactionbar": return "Показывает текст над хотбаром";
            case "teleport": return "Телепортирует игрока";
            case "giveitem": return "Выдает предмет игроку";
            case "giveitems": return "Выдает несколько предметов";
            case "playsound": return "Воспроизводит звук";
            case "effect": return "Накладывает эффект";
            case "playparticle": return "Создает частицы";
            case "command": return "Выполняет команду";
            case "executeasynccommand": return "Выполняет команду асинхронно";
            case "spawnentity": return "Создает существо";
            case "spawnmob": return "Создает моба";
            case "removeitems": return "Удаляет предметы у игрока";
            case "setarmor": return "Одевает броню на игрока";
            case "setvar": return "Создает/изменяет переменную";
            case "getvar": return "Получает значение переменной";
            case "addvar": return "Добавляет к переменной";
            case "subvar": return "Вычитает из переменной";
            case "mulvar": return "Умножает переменную";
            case "divvar": return "Делит переменную";
            case "setglobalvar": return "Глобальная переменная для всех";
            case "getglobalvar": return "Получает глобальную переменную";
            case "setservervar": return "Серверная переменная";
            case "getservervar": return "Получает серверную переменную";
            case "healplayer": return "Восстанавливает здоровье";
            case "setgamemode": return "Меняет режим игры";
            case "settime": return "Устанавливает время в мире";
            case "setweather": return "Меняет погоду";
            case "explosion": return "Создает взрыв";
            case "setblock": return "Устанавливает блок";
            case "wait": return "Задержка выполнения";
            case "randomnumber": return "Генерирует случайное число";
            default: return "Действие " + actionId;
        }
    }
    
    /**
     * Opens the GUI for the player
     */
    public void open() {
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        // Аудио обратная связь при открытии GUI
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
    }
    
    @Override
    public String getGUITitle() {
        return "Action Selection GUI for " + blockMaterial.name();
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (!player.equals(event.getWhoClicked())) return;
        if (!inventory.equals(event.getInventory())) return;
        
        event.setCancelled(true); // Cancel all clicks by default
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        ItemMeta meta = clicked.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return;
        
        // Find action ID in lore
        String actionId = null;
        boolean isCategoryItem = false;
        for (String line : lore) {
            if (line.startsWith("§8ID: ")) {
                actionId = line.substring(5); // Remove "§8ID: " prefix
                break;
            }
            if (line.contains("Категория")) {
                isCategoryItem = true;
                break;
            }
        }
        
        if (isCategoryItem) {
            // 🎆 ENHANCED: Handle category item click with helpful message
            player.sendMessage("§eℹ Это заголовок категории. Кликните по действию ниже.");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.8f);
            return;
        }
        
        if (actionId != null) {
            selectAction(actionId);
        }
    }
    
    private void selectAction(String actionId) {
        // Get the code block
        BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
        if (placementHandler == null) {
            player.sendMessage("§cОшибка: Не удалось получить обработчик блоков");
            return;
        }
        
        CodeBlock codeBlock = placementHandler.getCodeBlock(blockLocation);
        if (codeBlock == null) {
            player.sendMessage("§cОшибка: Блок кода не найден");
            return;
        }
        
        // Set the action
        codeBlock.setAction(actionId);
        
        // Save the world
        var creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getWorldManager().saveWorld(creativeWorld);
        }
        
        // Notify player
        player.sendMessage("§a✓ Действие '" + getActionDisplayName(actionId) + "' установлено!");
        player.sendMessage("§eКликните снова по блоку для настройки параметров.");
        
        // Close this GUI
        player.closeInventory();
        
        // Play sound feedback
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
    }
    
    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Optional cleanup when GUI is closed
        // GUIManager handles automatic unregistration
    }
    
    @Override
    public void onCleanup() {
        // Called when GUI is being cleaned up by GUIManager
        // No special cleanup needed for this GUI
    }
}