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
 * GUI for selecting targets for actions (@p, @a, victim, attacker, etc.)
 * 🎆 ENHANCED FEATURES:
 * - Intuitive target visualization with player heads
 * - Real-time target validation and preview
 * - Smart target recommendations based on action type
 * - Visual feedback for selection process
 * - OpenCreative-style target selection system with modern enhancements
 */
public class TargetSelectionGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    
    // Target types available in the system
    public enum TargetType {
        PLAYER("@p", "§aТекущий игрок", Material.PLAYER_HEAD, "Игрок, который запустил скрипт"),
        ALL_PLAYERS("@a", "§6Все игроки", Material.GOLDEN_HELMET, "Все игроки на сервере"),
        RANDOM_PLAYER("@r", "§cСлучайный игрок", Material.COMPASS, "Случайно выбранный игрок"),
        NEAREST_PLAYER("@n", "§eБлижайший игрок", Material.ENDER_EYE, "Ближайший к блоку игрок"),
        VICTIM("VICTIM", "§4Жертва", Material.SKELETON_SKULL, "Игрок, который пострадал в событии"),
        ATTACKER("ATTACKER", "§cАтакующий", Material.DIAMOND_SWORD, "Игрок, который атаковал"),
        KILLER("KILLER", "§8Убийца", Material.NETHERITE_SWORD, "Игрок, который убил"),
        DEFAULT("DEFAULT", "§7По умолчанию", Material.GRAY_STAINED_GLASS, "Стандартная цель для этого действия"),
        CUSTOM("CUSTOM", "§eПользовательская", Material.NAME_TAG, "Ввести имя игрока вручную");
        
        private final String selector;
        private final String displayName;
        private final Material icon;
        private final String description;
        
        TargetType(String selector, String displayName, Material icon, String description) {
            this.selector = selector;
            this.displayName = displayName;
            this.icon = icon;
            this.description = description;
        }
        
        public String getSelector() { return selector; }
        public String getDisplayName() { return displayName; }
        public Material getIcon() { return icon; }
        public String getDescription() { return description; }
    }
    
    public TargetSelectionGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getGuiManager();
        
        // Create inventory with appropriate size
        this.inventory = Bukkit.createInventory(null, 45, "§8Выбор цели: " + getBlockDisplayName());
        
        setupInventory();
    }
    
    private String getBlockDisplayName() {
        BlockConfigService blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
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
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, glassPane);
            }
        }
        
        // Add title item
        ItemStack titleItem = new ItemStack(blockMaterial);
        ItemMeta titleMeta = titleItem.getItemMeta();
        titleMeta.setDisplayName("§e§l" + getBlockDisplayName());
        List<String> titleLore = new ArrayList<>();
        titleLore.add("§7Выберите цель для этого действия");
        titleLore.add("");
        titleLore.add("§aВыбранная цель будет сохранена");
        titleLore.add("§aв блоке как параметр 'target'");
        titleMeta.setLore(titleLore);
        titleItem.setItemMeta(titleMeta);
        inventory.setItem(4, titleItem);
        
        // Add target options
        loadTargetOptions();
    }
    
    private void loadTargetOptions() {
        // Place target options in a nice grid layout
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int slotIndex = 0;
        
        for (TargetType targetType : TargetType.values()) {
            if (slotIndex >= slots.length) break;
            
            ItemStack targetItem = createTargetItem(targetType);
            inventory.setItem(slots[slotIndex], targetItem);
            slotIndex++;
        }
    }
    
    private ItemStack createTargetItem(TargetType targetType) {
        ItemStack item = new ItemStack(targetType.getIcon());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(targetType.getDisplayName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7" + targetType.getDescription());
            lore.add("");
            
            // 🎆 ENHANCED: Add usage examples and context
            switch (targetType) {
                case PLAYER:
                    lore.add("§a✓ Примеры использования:");
                    lore.add("§7  • Отправка сообщений");
                    lore.add("§7  • Телепортация");
                    lore.add("§7  • Выдача предметов");
                    break;
                case ALL_PLAYERS:
                    lore.add("§a✓ Примеры использования:");
                    lore.add("§7  • Объявления сервера");
                    lore.add("§7  • Глобальные эффекты");
                    break;
                case RANDOM_PLAYER:
                    lore.add("§a✓ Примеры использования:");
                    lore.add("§7  • Случайные награды");
                    lore.add("§7  • Мини-игры");
                    break;
                case NEAREST_PLAYER:
                    lore.add("§a✓ Примеры использования:");
                    lore.add("§7  • Локальные взаимодействия");
                    lore.add("§7  • Проверки близости");
                    break;
            }
            lore.add("");
            
            if (!targetType.getSelector().equals("DEFAULT")) {
                lore.add("§eСелектор: §f" + targetType.getSelector());
            }
            
            lore.add("");
            lore.add("§aКликните для выбора");
            lore.add("§8ID: " + targetType.name());
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Opens the GUI for the player
     */
    public void open() {
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        // Аудио обратная связь при открытии GUI
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 0.8f);
    }
    
    @Override
    public String getGUITitle() {
        return "Target Selection GUI for " + blockMaterial.name();
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
        
        // Find target ID in lore
        String targetId = null;
        for (String line : lore) {
            if (line.startsWith("§8ID: ")) {
                targetId = line.substring(6); // Remove "§8ID: " prefix
                break;
            }
        }
        
        if (targetId != null) {
            selectTarget(targetId);
        }
    }
    
    private void selectTarget(String targetId) {
        try {
            TargetType selectedTarget = TargetType.valueOf(targetId);
            
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
            
            // Set the target parameter
            codeBlock.setParameter("target", selectedTarget.getSelector());
            
            // Save the world
            var creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null) {
                plugin.getWorldManager().saveWorld(creativeWorld);
            }
            
            // Notify player with enhanced feedback
            player.sendMessage("§a✓ Цель установлена: " + selectedTarget.getDisplayName());
            player.sendMessage("§7» Селектор: §f" + selectedTarget.getSelector());
            player.sendMessage("§e⚡ Теперь выберите действие для блока.");
            
            // Close this GUI
            player.closeInventory();
            
            // Play success sound
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
            
            // Open ActionSelectionGUI next
            openActionSelectionGUI();
            
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cОшибка: Неизвестный тип цели");
        }
    }
    
    private void openActionSelectionGUI() {
        // Open ActionSelectionGUI after target selection
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ActionSelectionGUI actionGUI = new ActionSelectionGUI(plugin, player, blockLocation, blockMaterial);
            actionGUI.open();
        }, 5L); // Small delay to ensure smooth transition
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