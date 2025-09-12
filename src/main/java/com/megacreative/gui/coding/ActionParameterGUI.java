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
 * Advanced drag-and-drop GUI for configuring action parameters
 * 🎆 ENHANCED FEATURES:
 * - Dynamic YAML-driven slot configuration
 * - Real-time parameter validation
 * - Visual feedback for configuration status
 * - Smart placeholder generation
 * - Error prevention and user guidance
 * 
 * Features intuitive interface for each action type with named slots and item groups
 * Based on the configuration from coding_blocks.yml
 */
public class ActionParameterGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final String actionId;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    // 🎆 Enhanced features
    private boolean hasUnsavedChanges = false;
    private final Map<Integer, String> slotValidationErrors = new HashMap<>();
    private final Map<Integer, Boolean> slotValidationStatus = new HashMap<>();
    
    public ActionParameterGUI(MegaCreative plugin, Player player, Location blockLocation, String actionId) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.actionId = actionId;
        this.guiManager = plugin.getGuiManager();
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        // Create inventory with appropriate size (27 slots for standard chest GUI)
        this.inventory = Bukkit.createInventory(null, 27, "§8Настройка: " + actionId);
        
        setupInventory();
    }
    
    private void setupInventory() {
        inventory.clear();
        
        // Add background glass panes for visual separation
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        
        // Fill border slots with glass panes
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, glassPane);
            }
        }
        
        // Add action information
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e§l" + actionId);
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Настройка параметров действия");
        infoLore.add("");
        infoLore.add("§aПеретащите предметы в слоты");
        infoLore.add("§aдля настройки параметров");
        infoLore.add("");
        infoLore.add("§f⚡ Оптимизировано для быстрой настройки");
        infoLore.add("§7• Валидация в реальном времени");
        infoLore.add("§7• Автоматическая подсказка");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        // Load action-specific configuration
        loadActionConfiguration();
        
        // Load existing parameters from the code block
        loadExistingParameters();
    }
    
    /**
     * Loads the action configuration from coding_blocks.yml and sets up placeholder items
     */
    private void loadActionConfiguration() {
        // Get the action configurations directly from BlockConfigService
        var actionConfigurations = blockConfigService.getActionConfigurations();
        if (actionConfigurations == null) {
            player.sendMessage("§eИнформация: Используются базовые настройки для " + actionId);
            setupGenericSlots();
            return;
        }
        
        // Get configuration for this specific action
        var actionConfig = actionConfigurations.getConfigurationSection(actionId);
        if (actionConfig == null) {
            // No specific configuration, use generic slots
            player.sendMessage("§eИнформация: Конфигурация для " + actionId + " не найдена, используются базовые слоты");
            setupGenericSlots();
            return;
        }
        
        player.sendMessage("§a✓ Загружена конфигурация для " + actionId);
        
        // Check for named slots configuration
        var slotsConfig = actionConfig.getConfigurationSection("slots");
        if (slotsConfig != null) {
            setupNamedSlots(slotsConfig);
        }
        
        // Check for item groups configuration  
        var itemGroupsConfig = actionConfig.getConfigurationSection("item_groups");
        if (itemGroupsConfig != null) {
            setupItemGroups(itemGroupsConfig);
        }
        
        // If neither slots nor item_groups were configured, use generic setup
        if (slotsConfig == null && itemGroupsConfig == null) {
            player.sendMessage("§eИнформация: Слоты не настроены для " + actionId + ", используются базовые");
            setupGenericSlots();
        }
    }
    
    /**
     * Sets up named slots based on configuration
     */
    private void setupNamedSlots(org.bukkit.configuration.ConfigurationSection slotsConfig) {
        int configuredSlots = 0;
        
        for (String slotKey : slotsConfig.getKeys(false)) {
            try {
                int slotIndex = Integer.parseInt(slotKey);
                if (slotIndex < 0 || slotIndex >= inventory.getSize()) {
                    plugin.getLogger().warning("Неверный индекс слота в конфигурации: " + slotKey + " для " + actionId);
                    continue;
                }
                
                var slotConfig = slotsConfig.getConfigurationSection(slotKey);
                if (slotConfig == null) continue;
                
                String name = slotConfig.getString("name", "Параметр");
                String description = slotConfig.getString("description", "Описание параметра");
                String placeholderItem = slotConfig.getString("placeholder_item", "PAPER");
                String slotName = slotConfig.getString("slot_name", "slot_" + slotIndex);
                
                // Create placeholder item
                Material material = Material.matchMaterial(placeholderItem);
                if (material == null) {
                    plugin.getLogger().warning("Неверный материал: " + placeholderItem + " для " + actionId + ", используется PAPER");
                    material = Material.PAPER;
                }
                
                ItemStack placeholder = new ItemStack(material);
                ItemMeta meta = placeholder.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(name);
                    List<String> lore = new ArrayList<>();
                    
                    // Split long descriptions into multiple lines
                    String[] descLines = description.split("\\. ");
                    for (String line : descLines) {
                        if (line.length() > 40) {
                            // Split long lines
                            String[] words = line.split(" ");
                            StringBuilder currentLine = new StringBuilder();
                            for (String word : words) {
                                if (currentLine.length() + word.length() + 1 > 40) {
                                    if (currentLine.length() > 0) {
                                        lore.add("§7" + currentLine.toString().trim());
                                        currentLine = new StringBuilder();
                                    }
                                }
                                currentLine.append(word).append(" ");
                            }
                            if (currentLine.length() > 0) {
                                lore.add("§7" + currentLine.toString().trim());
                            }
                        } else {
                            lore.add("§7" + line);
                        }
                    }
                    
                    lore.add("");
                    lore.add("§eПоместите предмет сюда");
                    lore.add("§7для настройки параметра");
                    lore.add("");
                    lore.add("§8ID: " + slotName);
                    meta.setLore(lore);
                    placeholder.setItemMeta(meta);
                }
                
                inventory.setItem(slotIndex, placeholder);
                configuredSlots++;
            } catch (NumberFormatException e) {
                // Invalid slot index, skip
                plugin.getLogger().warning("Неверный формат индекса слота: " + slotKey + " для " + actionId);
            }
        }
        
        if (configuredSlots > 0) {
            player.sendMessage("§a✓ Настроено " + configuredSlots + " слотов для " + actionId);
        }
    }
    
    /**
     * Sets up item groups based on configuration
     */
    private void setupItemGroups(org.bukkit.configuration.ConfigurationSection itemGroupsConfig) {
        for (String groupKey : itemGroupsConfig.getKeys(false)) {
            var groupConfig = itemGroupsConfig.getConfigurationSection(groupKey);
            if (groupConfig == null) continue;
            
            List<Integer> slots = groupConfig.getIntegerList("slots");
            String name = groupConfig.getString("name", "Группа предметов");
            String description = groupConfig.getString("description", "Описание группы");
            String placeholderItem = groupConfig.getString("placeholder_item", "CHEST");
            
            // Create placeholder items for each slot in the group
            Material material = Material.matchMaterial(placeholderItem);
            if (material == null) material = Material.CHEST;
            
            ItemStack placeholder = new ItemStack(material);
            ItemMeta meta = placeholder.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                List<String> lore = new ArrayList<>();
                lore.add("§7" + description);
                lore.add("");
                lore.add("§eПоместите предметы сюда");
                lore.add("§7для настройки группы");
                lore.add("");
                lore.add("§8Группа: " + groupKey);
                meta.setLore(lore);
                placeholder.setItemMeta(meta);
            }
            
            // Place placeholder items in all slots of the group
            for (int slot : slots) {
                if (slot >= 0 && slot < inventory.getSize()) {
                    inventory.setItem(slot, placeholder);
                }
            }
        }
    }
    
    /**
     * Sets up generic slots when no specific configuration is found
     */
    private void setupGenericSlots() {
        // Create generic placeholder items for slots 9-17 (center row)
        ItemStack placeholder = new ItemStack(Material.PAPER);
        ItemMeta meta = placeholder.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§fПараметр");
            List<String> lore = new ArrayList<>();
            lore.add("§7Поместите предмет сюда");
            lore.add("§7для настройки параметра");
            meta.setLore(lore);
            placeholder.setItemMeta(meta);
        }
        
        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, placeholder);
        }
    }
    
    /**
     * Loads existing parameters from the code block into the GUI
     */
    private void loadExistingParameters() {
        BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
        if (placementHandler == null) return;
        
        CodeBlock codeBlock = placementHandler.getCodeBlock(blockLocation);
        if (codeBlock == null) return;
        
        // Load existing configuration items
        Map<Integer, ItemStack> configItems = codeBlock.getConfigItems();
        if (configItems != null) {
            for (Map.Entry<Integer, ItemStack> entry : configItems.entrySet()) {
                int slot = entry.getKey();
                ItemStack item = entry.getValue();
                
                if (slot >= 0 && slot < inventory.getSize() && item != null && !item.getType().isAir()) {
                    inventory.setItem(slot, item);
                }
            }
        }
    }
    
    /**
     * Saves the configured parameters back to the code block
     * 🎆 ENHANCED: With validation feedback
     */
    private void saveParameters() {
        BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
        if (placementHandler == null) return;
        
        CodeBlock codeBlock = placementHandler.getCodeBlock(blockLocation);
        if (codeBlock == null) return;
        
        // 🎆 ENHANCED: Check validation status before saving
        boolean hasErrors = false;
        List<String> errorMessages = new ArrayList<>();
        
        for (Map.Entry<Integer, String> entry : slotValidationErrors.entrySet()) {
            if (entry.getValue() != null) {
                hasErrors = true;
                errorMessages.add("Слот " + entry.getKey() + ": " + entry.getValue());
            }
        }
        
        if (hasErrors && !errorMessages.isEmpty()) {
            player.sendMessage("§c⚠ Обнаружены ошибки в конфигурации:");
            for (String error : errorMessages) {
                player.sendMessage("§c  • " + error);
            }
            player.sendMessage("§eКонфигурация сохранена, но может работать некорректно.");
        }
        
        // Clear existing configuration
        codeBlock.clearConfigItems();
        
        // Save items from inventory to code block
        int savedItems = 0;
        int validItems = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir() && !isPlaceholderItem(item)) {
                codeBlock.setConfigItem(i, item);
                savedItems++;
                
                // Count valid items
                if (slotValidationStatus.getOrDefault(i, true)) {
                    validItems++;
                }
            }
        }
        
        if (savedItems > 0) {
            if (validItems == savedItems) {
                player.sendMessage("§a✓ Сохранено " + savedItems + " параметров для действия " + actionId);
            } else {
                player.sendMessage("§e⚠ Сохранено " + savedItems + " параметров (" + validItems + " корректных) для " + actionId);
            }
        } else {
            player.sendMessage("§eℹ Конфигурация очищена для действия " + actionId);
        }
        
        // Reset unsaved changes flag
        hasUnsavedChanges = false;
        
        // Save the world to persist changes
        var creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getWorldManager().saveWorld(creativeWorld);
        }
    }
    
    /**
     * 🎆 ENHANCED: Real-time parameter validation
     * Validates a specific slot configuration and provides user feedback
     */
    private void validateSlot(int slot, ItemStack item) {
        String error = null;
        boolean isValid = true;
        
        if (item == null || item.getType().isAir()) {
            // Empty slot - check if required
            if (isSlotRequired(slot)) {
                error = "Обязательный параметр";
                isValid = false;
            }
        } else {
            // Validate item type and content
            error = validateItemForSlot(slot, item);
            isValid = (error == null);
        }
        
        slotValidationErrors.put(slot, error);
        slotValidationStatus.put(slot, isValid);
        
        // Update visual feedback
        updateSlotVisualFeedback(slot, isValid, error);
        
        // Track unsaved changes
        hasUnsavedChanges = true;
    }
    
    /**
     * 🎆 ENHANCED: Check if a slot is required for this action
     */
    private boolean isSlotRequired(int slot) {
        var actionConfigurations = blockConfigService.getActionConfigurations();
        if (actionConfigurations == null) return false;
        
        var actionConfig = actionConfigurations.getConfigurationSection(actionId);
        if (actionConfig == null) return false;
        
        var slotsConfig = actionConfig.getConfigurationSection("slots");
        if (slotsConfig == null) return false;
        
        var slotConfig = slotsConfig.getConfigurationSection(String.valueOf(slot));
        if (slotConfig == null) return false;
        
        return slotConfig.getBoolean("required", slot == 0); // First slot usually required
    }
    
    /**
     * 🎆 ENHANCED: Validate item content for specific slot
     */
    private String validateItemForSlot(int slot, ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return "Нужно переименовать предмет";
        }
        
        String itemName = item.getItemMeta().getDisplayName();
        
        // Action-specific validation
        switch (actionId.toLowerCase()) {
            case "sendmessage":
                if (slot == 0 && itemName.trim().isEmpty()) {
                    return "Сообщение не может быть пустым";
                }
                break;
            case "executeAsyncCommand":
                if (slot == 0 && !itemName.startsWith("/") && !itemName.contains(":")) {
                    return "Команда должна начинаться с '/' или содержать ':'";
                }
                break;
            case "asyncloop":
                if (slot == 0 && !isValidNumber(itemName)) {
                    return "Количество итераций должно быть числом";
                }
                if (slot == 1 && !isValidNumber(itemName)) {
                    return "Задержка должна быть числом";
                }
                break;
        }
        
        return null; // No error
    }
    
    /**
     * 🎆 ENHANCED: Check if string represents a valid number
     */
    private boolean isValidNumber(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        
        // Remove color codes and common prefixes
        String cleaned = str.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Check for pattern like "iterations:5" or "delay:20"
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        
        try {
            Integer.parseInt(cleaned);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 🎆 ENHANCED: Update visual feedback for slot validation
     */
    private void updateSlotVisualFeedback(int slot, boolean isValid, String error) {
        ItemStack currentItem = inventory.getItem(slot);
        if (currentItem == null || currentItem.getType().isAir()) return;
        
        ItemMeta meta = currentItem.getItemMeta();
        if (meta == null) return;
        
        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();
        
        // Remove old validation messages
        lore.removeIf(line -> line.contains("✓") || line.contains("✗") || line.contains("Ошибка:"));
        
        // Add new validation status
        if (isValid) {
            lore.add("§a✓ Параметр корректен");
        } else if (error != null) {
            lore.add("§c✗ Ошибка: " + error);
        }
        
        meta.setLore(lore);
        currentItem.setItemMeta(meta);
        
        // Update item in inventory
        inventory.setItem(slot, currentItem);
    }
    
    /**
     * Checks if an item is a placeholder item
     */
    private boolean isPlaceholderItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        List<String> lore = meta.getLore();
        if (lore == null) return false;
        
        // Check if any line contains the placeholder indicator
        for (String line : lore) {
            if (line.contains("Поместите предмет сюда") || line.contains("для настройки")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Opens the GUI for the player
     */
    public void open() {
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        // Аудио обратная связь при открытии GUI
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 0.6f, 1.1f);
    }
    
    @Override
    public String getGUITitle() {
        return "Action Parameter GUI for " + actionId;
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (!player.equals(event.getWhoClicked())) return;
        if (!inventory.equals(event.getInventory())) return;
        
        int slot = event.getSlot();
        
        // Allow interaction with center slots (9-17) for parameter configuration
        if (slot >= 9 && slot <= 17) {
            // 🎆 ENHANCED: Trigger real-time validation after item placement
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ItemStack newItem = inventory.getItem(slot);
                validateSlot(slot, newItem);
            }, 1L);
            return;
        }
        
        // Check for named slots based on configuration
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && clickedItem.hasItemMeta()) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore != null) {
                    for (String line : lore) {
                        if (line.contains("ID:") || line.contains("Группа:")) {
                            // This is a configured slot, allow interaction
                            return;
                        }
                    }
                }
            }
        }
        
        // Cancel interaction with all other slots (placeholders, borders, etc.)
        event.setCancelled(true);
        
        // Handle clicks on special items
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Handle info item click
        if (displayName.contains(actionId)) {
            player.sendMessage("§eПодсказка: Перетащите предметы в центральные слоты для настройки параметров.");
        }
    }
    
    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Save parameters when GUI is closed
        saveParameters();
        
        // Optional cleanup when GUI is closed
        // GUIManager handles automatic unregistration
    }
    
    @Override
    public void onCleanup() {
        // Called when GUI is being cleaned up by GUIManager
        // No special cleanup needed for this GUI
    }
}