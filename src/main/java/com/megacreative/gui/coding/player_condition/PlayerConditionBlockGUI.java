package com.megacreative.gui.coding.player_condition;

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
 * 🎆 Enhanced Player Condition Block GUI
 * 
 * Provides a specialized interface for selecting player condition actions.
 * Implements Reference System-style: universal blocks with GUI configuration.
 *
 * 🎆 Улучшенный графический интерфейс условий игроков
 * 
 * Предоставляет специализированный интерфейс для выбора действий, связанных с условиями игроков.
 * Реализует стиль Reference System: универсальные блоки с настройкой через GUI.
 *
 * 🎆 Erweiterte Spielerbedingungs-Block-GUI
 * 
 * Bietet eine spezialisierte Schnittstelle zur Auswahl von Spielerbedingungs-Aktionen.
 * Implementiert Reference System-Stil: universelle Blöcke mit GUI-Konfiguration.
 */
public class PlayerConditionBlockGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    /**
     * Initializes player condition block GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param blockLocation Location of block to configure
     * @param blockMaterial Material of block to configure
     */
    public PlayerConditionBlockGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getGuiManager();
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        // Create inventory with appropriate size
        this.inventory = Bukkit.createInventory(null, 54, "§8Условие игрока: " + getBlockDisplayName());
        
        setupInventory();
    }
    
    /**
     * Gets display name for block
     */
    private String getBlockDisplayName() {
        // Get display name from block config service
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(blockMaterial);
        return config != null ? config.getDisplayName() : blockMaterial.name();
    }
    
    /**
     * Sets up the GUI inventory
     */
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
        infoLore.add("§7Выберите условие игрока");
        infoLore.add("");
        infoLore.add("§aКликните на условие чтобы");
        infoLore.add("§аназначить его блоку");
        infoLore.add("");
        infoLore.add("§f✨ Reference system-стиль: универсальные блоки");
        infoLore.add("§fс настройкой через GUI");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        // Load available player condition actions for this block type
        loadAvailablePlayerConditionActions();
    }
    
    /**
     * Loads available player condition actions for this block type
     */
    private void loadAvailablePlayerConditionActions() {
        // Debug logging
        player.sendMessage("§eDebug: Checking material " + blockMaterial.name());
        
        // Get available player condition actions for block material using BlockConfigService
        List<String> availableActions = blockConfigService.getActionsForMaterial(blockMaterial);
        
        player.sendMessage("§eDebug: Available player condition actions count: " + (availableActions != null ? availableActions.size() : "null"));
        
        // Enhanced player condition action loading logic
        if (availableActions == null || availableActions.isEmpty()) {
            player.sendMessage("§cОшибка: Нет доступных условий для блока игрока " + blockMaterial.name());
            
            // Try to get all block configs for debugging
            var allConfigs = blockConfigService.getAllBlockConfigs();
            player.sendMessage("§eDebug: Total block configs: " + allConfigs.size());
            
            // Check if this material is recognized as a code block
            boolean isCodeBlock = blockConfigService.isCodeBlock(blockMaterial);
            player.sendMessage("§eDebug: Is code block: " + isCodeBlock);
            
            // Try to get block config by material
            var blockConfig = blockConfigService.getBlockConfigByMaterial(blockMaterial);
            if (blockConfig != null) {
                player.sendMessage("§eDebug: Block config found: " + blockConfig.getId() + " - " + blockConfig.getDisplayName());
                player.sendMessage("§eDebug: Block type: " + blockConfig.getType());
                player.sendMessage("§eDebug: Default action: " + blockConfig.getDefaultAction());
                
                // Load actions from the block configuration's actions list
                List<String> actions = new ArrayList<>();
                
                // First, try to get actions directly from the block config
                if (blockConfig.getParameters().containsKey("actions")) {
                    // This is for backward compatibility with old config format
                    Object actionsObj = blockConfig.getParameters().get("actions");
                    if (actionsObj instanceof List) {
                        actions.addAll((List<String>) actionsObj);
                        player.sendMessage("§aDebug: Found actions from block config parameters: " + actions.size());
                    }
                }
                
                // Try to get actions from the YAML configuration file
                actions = blockConfigService.getActionsForMaterial(blockMaterial);
                
                // Fallback to getting actions from material mapping
                if (actions.isEmpty()) {
                    actions = blockConfigService.getActionsForMaterial(blockMaterial);
                }
                
                if (actions != null && !actions.isEmpty()) {
                    availableActions = actions;
                    player.sendMessage("§aDebug: Found actions after re-check: " + actions.size());
                }
            } else {
                player.sendMessage("§eDebug: No block config found for material");
                
                // List all available materials for debugging
                Set<Material> codeBlockMaterials = blockConfigService.getCodeBlockMaterials();
                player.sendMessage("§eDebug: Available code block materials (" + codeBlockMaterials.size() + "):");
                int count = 0;
                for (Material mat : codeBlockMaterials) {
                    player.sendMessage("§7- " + mat.name());
                    count++;
                    if (count >= 10) {
                        player.sendMessage("§7... and " + (codeBlockMaterials.size() - 10) + " more");
                        break;
                    }
                }
            }
            
            // If we still don't have actions, use default player condition actions as fallback
            if (availableActions == null || availableActions.isEmpty()) {
                // Add default player condition actions
                availableActions = new ArrayList<>();
                availableActions.add("isPlayerOp");
                availableActions.add("isPlayerFlying");
                availableActions.add("isPlayerSneaking");
                availableActions.add("isPlayerSprinting");
                availableActions.add("isPlayerInWater");
                availableActions.add("isPlayerInLava");
                availableActions.add("isPlayerOnGround");
                availableActions.add("hasPlayerPermission");
                player.sendMessage("§6Using player condition default actions as fallback");
            }
        }
        
        // 🎆 ENHANCED: Group player condition actions by category for better organization
        Map<String, List<String>> categorizedActions = categorizePlayerConditionActions(availableActions);
        
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
     * 🎆 ENHANCED: Categorizes player condition actions for better organization
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private Map<String, List<String>> categorizePlayerConditionActions(List<String> actions) {
        Map<String, List<String>> categories = new LinkedHashMap<>();
        
        for (String action : actions) {
            String category = getPlayerConditionActionCategory(action);
            categories.computeIfAbsent(category, k -> new ArrayList<>()).add(action);
        }
        
        return categories;
    }
    
    /**
     * 🎆 ENHANCED: Get category for a player condition action
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private String getPlayerConditionActionCategory(String actionId) {
        switch (actionId.toLowerCase()) {
            case "isplayerop":
            case "hasplayerpermission":
                return "🛡️ Права";
            
            case "isplayerflying":
            case "isplayeronground":
                return "🌍 Положение";
            
            case "isplayersneaking":
            case "isplayersprinting":
                return "🏃 Движение";
            
            case "isplayerinwater":
            case "isplayerinlava":
                return "🌊 Окружение";
            
            case "isplayerholding":
            case "isplayerwearing":
                return "🎒 Инвентарь";
            
            case "isplayerhealth":
            case "isplayerhunger":
                return "❤️ Состояние";
            
            default:
                return "🔧 Основные";
        }
    }
    
    /**
     * 🎆 ENHANCED: Create category header item
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private ItemStack createCategoryItem(String categoryName, int actionCount) {
        ItemStack item = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§e§l" + categoryName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Доступно условий: " + actionCount);
        lore.add("§8Категория");
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 🎆 ENHANCED: Create player condition action item
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private ItemStack createActionItem(String actionId, String category) {
        // Create appropriate material for action type
        Material material = getPlayerConditionActionMaterial(actionId);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        meta.setDisplayName("§a§l" + getPlayerConditionActionDisplayName(actionId));
        
        // Set lore with description and category
        List<String> lore = new ArrayList<>();
        lore.add("§7" + getPlayerConditionActionDescription(actionId));
        lore.add("");
        lore.add("§8⚙️ Категория: " + category);
        lore.add("");
        lore.add("§e⚡ Кликните чтобы выбрать");
        lore.add("§8ID: " + actionId);
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Gets material for player condition action
     */
    private Material getPlayerConditionActionMaterial(String actionId) {
        // Return appropriate materials based on action type
        switch (actionId.toLowerCase()) {
            case "isplayerop":
                return Material.COMMAND_BLOCK;
            case "isplayerflying":
                return Material.FEATHER;
            case "isplayersneaking":
                return Material.LEATHER_BOOTS;
            case "isplayerinwater":
                return Material.WATER_BUCKET;
            case "isplayerinlava":
                return Material.LAVA_BUCKET;
            case "hasplayerpermission":
                return Material.PAPER;
            default:
                return Material.PAPER;
        }
    }
    
    /**
     * Gets display name for player condition action
     */
    private String getPlayerConditionActionDisplayName(String actionId) {
        // Return user-friendly names for player condition actions
        switch (actionId.toLowerCase()) {
            case "isplayerop": return "Игрок оператор";
            case "isplayerflying": return "Игрок летает";
            case "isplayersneaking": return "Игрок приседает";
            case "isplayersprinting": return "Игрок бежит";
            case "isplayerinwater": return "Игрок в воде";
            case "isplayerinlava": return "Игрок в лаве";
            case "isplayeronground": return "Игрок на земле";
            case "hasplayerpermission": return "Игрок имеет право";
            case "isplayerholding": return "Игрок держит предмет";
            case "isplayerwearing": return "Игрок одет";
            case "isplayerhealth": return "Здоровье игрока";
            case "isplayerhunger": return "Голод игрока";
            default: return actionId;
        }
    }

    /**
     * Gets description for player condition action
     */
    private String getPlayerConditionActionDescription(String actionId) {
        // Return descriptions for player condition actions
        switch (actionId.toLowerCase()) {
            case "isplayerop": return "Проверяет, является ли игрок оператором сервера";
            case "isplayerflying": return "Проверяет, летает ли игрок";
            case "isplayersneaking": return "Проверяет, приседает ли игрок";
            case "isplayersprinting": return "Проверяет, бежит ли игрок";
            case "isplayerinwater": return "Проверяет, находится ли игрок в воде";
            case "isplayerinlava": return "Проверяет, находится ли игрок в лаве";
            case "isplayeronground": return "Проверяет, находится ли игрок на земле";
            case "hasplayerpermission": return "Проверяет, имеет ли игрок определенное право";
            case "isplayerholding": return "Проверяет, держит ли игрок определенный предмет";
            case "isplayerwearing": return "Проверяет, одет ли игрок в определенную броню";
            case "isplayerhealth": return "Проверяет здоровье игрока";
            case "isplayerhunger": return "Проверяет уровень голода игрока";
            default: return "Условие игрока " + actionId;
        }
    }
    
    /**
     * Opens the GUI for the player
     * Implements reference system-style: universal blocks with GUI configuration
     */
    public void open() {
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        // Audio feedback when opening GUI
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        
        // Add visual effects for reference system-style magic
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, 
            player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 1);
    }
    
    @Override
    /**
     * Gets the GUI title
     */
    public String getGUITitle() {
        return "Player Condition Block GUI for " + blockMaterial.name();
    }
    
    @Override
    /**
     * Handles inventory click events
     */
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
            player.sendMessage("§eℹ Это заголовок категории. Кликните по условию ниже.");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.8f);
            return;
        }
        
        if (actionId != null) {
            selectAction(actionId);
        }
    }
    
    /**
     * 🎆 ENHANCED: Select action for the block
     * Implements reference system-style: universal blocks with GUI configuration
     */
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
        player.sendMessage("§a✓ Условие '" + getPlayerConditionActionDisplayName(actionId) + "' установлено!");
        player.sendMessage("§eКликните снова по блоку для настройки параметров.");
        
        // Add visual feedback for reference system-style magic
        player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, 
            player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 1);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        
        // Close this GUI
        player.closeInventory();
    }
    
    @Override
    /**
     * Handles inventory close events
     */
    public void onInventoryClose(InventoryCloseEvent event) {
        // Optional cleanup when GUI is closed
        // GUIManager handles automatic unregistration
    }
    
    @Override
    /**
     * Performs resource cleanup when interface is closed
     */
    public void onCleanup() {
        // Called when GUI is being cleaned up by GUIManager
        // No special cleanup needed for this GUI
    }
}