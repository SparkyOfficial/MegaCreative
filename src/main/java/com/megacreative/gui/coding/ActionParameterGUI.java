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
 * 🎆 Enhanced Action Parameter GUI
 * 
 * Implements Reference System-style: universal blocks with GUI configuration
 * with categories, beautiful selection, and smart signs on blocks with information.
 *
 * 🎆 Улучшенный графический интерфейс параметров действий
 * 
 * Реализует стиль reference system: универсальные блоки с настройкой через GUI
 * с категориями, красивым выбором и умными табличками на блоках с информацией.
 *
 * 🎆 Erweiterte Aktionsparameter-GUI
 * 
 * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Konfiguration
 * mit Kategorien, schöner Auswahl und intelligenten Schildern an Blöcken mit Informationen.
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
    // 🎆 NEW: Store current values for dependent validation
    private final Map<Integer, String> slotCurrentValues = new HashMap<>();
    
    /**
     * Initializes action parameters GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param blockLocation Location of block to configure
     * @param actionId Action ID to configure
     */
    public ActionParameterGUI(MegaCreative plugin, Player player, Location blockLocation, String actionId) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.actionId = actionId;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        // Create inventory with appropriate size (54 slots for double chest GUI)
        this.inventory = Bukkit.createInventory(null, 54, "§8Настройка: " + actionId);
        
        setupInventory();
    }
    
    /**
     * Sets up the GUI inventory
     */
    private void setupInventory() {
        inventory.clear();
        
        // Add decorative border with category-specific materials
        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(" ");
        borderItem.setItemMeta(borderMeta);
        
        // Fill border slots
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderItem);
            }
        }
        
        // Add action information with enhanced visual design
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
        infoLore.add("");
        infoLore.add("§f✨ Reference system-стиль: универсальные блоки");
        infoLore.add("§fс настройкой через GUI");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        // Load action-specific configuration
        loadActionConfiguration();
        
        // Load existing parameters from the code block
        loadExistingParameters();
        
        // Add back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c⬅ Назад");
        List<String> backLore = new ArrayList<>();
        backLore.add("§7Вернуться к выбору действий");
        backMeta.setLore(backLore);
        backButton.setItemMeta(backMeta);
        inventory.setItem(49, backButton);
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
                // Adjust slot index for the larger inventory (54 slots)
                int adjustedSlot = slotIndex + 9; // Start from row 2
                if (adjustedSlot < 9 || adjustedSlot >= 45) {
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
                
                inventory.setItem(adjustedSlot, placeholder);
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
            
            // Place placeholder items in all slots of the group (adjusted for larger inventory)
            for (int slot : slots) {
                int adjustedSlot = slot + 9; // Start from row 2
                if (adjustedSlot >= 9 && adjustedSlot < 45) {
                    inventory.setItem(adjustedSlot, placeholder);
                }
            }
        }
    }
    
    /**
     * Sets up generic slots when no specific configuration is found
     */
    private void setupGenericSlots() {
        // Create generic placeholder items for slots 10-44 (main area)
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
        
        for (int i = 10; i < 44; i++) {
            // Skip border slots
            if (i % 9 != 0 && i % 9 != 8) {
                inventory.setItem(i, placeholder);
            }
        }
    }
    
    /**
     * Loads existing parameters from the code block into the GUI
     */
    private void loadExistingParameters() {
        BlockPlacementHandler placementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
        if (placementHandler == null) return;
        
        CodeBlock codeBlock = placementHandler.getCodeBlock(blockLocation);
        if (codeBlock == null) return;
        
        // Load existing configuration items
        Map<Integer, ItemStack> configItems = codeBlock.getConfigItems();
        if (configItems != null) {
            for (Map.Entry<Integer, ItemStack> entry : configItems.entrySet()) {
                int slot = entry.getKey();
                ItemStack item = entry.getValue();
                
                // Adjust slot index for the larger inventory
                int adjustedSlot = slot + 9; // Start from row 2
                if (adjustedSlot >= 9 && adjustedSlot < 45 && item != null && !item.getType().isAir()) {
                    inventory.setItem(adjustedSlot, item);
                }
            }
        }
    }
    
    /**
     * Saves the configured parameters back to the code block
     * 🎆 ENHANCED: With validation feedback
     */
    private void saveParameters() {
        BlockPlacementHandler placementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
        if (placementHandler == null) return;
        
        CodeBlock codeBlock = placementHandler.getCodeBlock(blockLocation);
        if (codeBlock == null) return;
        
        // 🎆 ENHANCED: Check validation status before saving
        boolean hasErrors = false;
        boolean hasWarnings = false;
        List<String> errorMessages = new ArrayList<>();
        List<String> warningMessages = new ArrayList<>();
        
        // Check for validation errors
        for (Map.Entry<Integer, String> entry : slotValidationErrors.entrySet()) {
            if (entry.getValue() != null) {
                hasErrors = true;
                errorMessages.add("Слот " + entry.getKey() + ": " + entry.getValue());
            }
        }
        
        // Check for required slots that are empty
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (isSlotRequired(slot)) {
                ItemStack item = inventory.getItem(slot);
                if (item == null || item.getType().isAir() || isPlaceholderItem(item)) {
                    hasWarnings = true;
                    warningMessages.add("Слот " + slot + ": Обязательный параметр не заполнен");
                }
            }
        }
        
        // 🎆 NEW: Check for dependent parameter errors
        for (Map.Entry<Integer, String> entry : slotValidationErrors.entrySet()) {
            String error = entry.getValue();
            if (error != null && error.startsWith("Доступно только если")) {
                hasErrors = true;
                errorMessages.add("Слот " + entry.getKey() + ": " + error);
            }
        }
        
        // Provide feedback to player
        if (hasErrors && !errorMessages.isEmpty()) {
            player.sendMessage("§c⚠ Обнаружены ошибки в конфигурации:");
            for (String error : errorMessages) {
                player.sendMessage("§c  • " + error);
            }
        }
        
        if (hasWarnings && !warningMessages.isEmpty()) {
            player.sendMessage("§e⚠ Обнаружены предупреждения:");
            for (String warning : warningMessages) {
                player.sendMessage("§e  • " + warning);
            }
        }
        
        // Clear existing configuration
        codeBlock.clearConfigItems();
        
        // Save items from inventory to code block
        int savedItems = 0;
        int validItems = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            // Adjust slot index back to original when saving
            int originalSlot = i - 9; // Adjust back from row 2
            if (originalSlot >= 0 && item != null && !item.getType().isAir() && !isPlaceholderItem(item)) {
                codeBlock.setConfigItem(originalSlot, item);
                savedItems++;
                
                // Count valid items
                if (slotValidationStatus.getOrDefault(i, true)) {
                    validItems++;
                }
            }
        }
        
        if (savedItems > 0) {
            if (validItems == savedItems && !hasErrors) {
                player.sendMessage("§a✓ Сохранено " + savedItems + " параметров для действия " + actionId);
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 1);
            } else if (hasErrors) {
                player.sendMessage("§e⚠ Сохранено " + savedItems + " параметров (" + validItems + " корректных) для " + actionId);
                player.sendMessage("§cНекоторые параметры содержат ошибки и могут работать некорректно!");
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            } else {
                player.sendMessage("§e⚠ Сохранено " + savedItems + " параметров (" + validItems + " корректных) для " + actionId);
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            }
        } else {
            player.sendMessage("§eℹ Конфигурация очищена для действия " + actionId);
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
        }
        
        // Reset unsaved changes flag
        hasUnsavedChanges = false;
        
        // Save the world to persist changes
        var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
        }
    }
    
    /**
     * 🎆 ENHANCED: Real-time parameter validation
     * Validates a specific slot configuration and provides user feedback
     */
    private void validateSlot(int slot, ItemStack item) {
        String error = null;
        boolean isValid = true;
        
        // Store current value for dependent validation
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            slotCurrentValues.put(slot, item.getItemMeta().getDisplayName());
        } else {
            slotCurrentValues.remove(slot);
        }
        
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
        
        // 🎆 NEW: Validate dependent slots
        validateDependentSlots(slot);
        
        // Track unsaved changes
        hasUnsavedChanges = true;
    }
    
    /**
     * 🎆 ENHANCED: Validate dependent slots when a value changes
     */
    private void validateDependentSlots(int changedSlot) {
        var actionConfigurations = blockConfigService.getActionConfigurations();
        if (actionConfigurations == null) return;
        
        var actionConfig = actionConfigurations.getConfigurationSection(actionId);
        if (actionConfig == null) return;
        
        var slotsConfig = actionConfig.getConfigurationSection("slots");
        if (slotsConfig == null) return;
        
        // Check all slots for dependencies on the changed slot
        for (String slotKey : slotsConfig.getKeys(false)) {
            try {
                int slot = Integer.parseInt(slotKey);
                // Adjust slot index for the larger inventory
                int adjustedSlot = slot + 9; // Start from row 2
                if (adjustedSlot == changedSlot) continue; // Skip the slot that just changed
                
                var slotConfig = slotsConfig.getConfigurationSection(slotKey);
                
                // Check for dependency conditions
                String dependencyCondition = slotConfig.getString("dependency");
                if (dependencyCondition != null) {
                    String[] parts = dependencyCondition.split(" ");
                    if (parts.length == 3) {
                        String dependencySlotName = parts[0];
                        String operator = parts[1];
                        String expectedValue = parts[2];
                        boolean isNotEqual = operator.equals("!=");
                        
                        // Find the dependency slot number
                        Integer dependencySlot = findSlotNumberByName(dependencySlotName);
                        if (dependencySlot != null) {
                            // Adjust dependency slot for larger inventory
                            int adjustedDependencySlot = dependencySlot + 9; // Start from row 2
                            String currentValue = slotCurrentValues.get(adjustedDependencySlot);
                            
                            // Check if dependency condition is met
                            boolean conditionMet = false;
                            if (currentValue != null) {
                                if (isNotEqual) {
                                    conditionMet = !currentValue.equals(expectedValue);
                                } else {
                                    conditionMet = currentValue.equals(expectedValue);
                                }
                            }
                            
                            // If condition is not met, mark dependent slot as invalid
                            if (!conditionMet) {
                                String error = "Доступно только если " + dependencySlotName + 
                                    (isNotEqual ? " ≠ " : " = ") + expectedValue;
                                slotValidationErrors.put(adjustedSlot, error);
                                slotValidationStatus.put(adjustedSlot, false);
                                updateSlotVisualFeedback(adjustedSlot, false, error);
                            } else {
                                // Re-validate the slot since dependency condition is now met
                                ItemStack item = inventory.getItem(adjustedSlot);
                                if (item != null && !item.getType().isAir()) {
                                    String newError = validateItemForSlot(adjustedSlot, item);
                                    boolean newValid = (newError == null);
                                    slotValidationErrors.put(adjustedSlot, newError);
                                    slotValidationStatus.put(adjustedSlot, newValid);
                                    updateSlotVisualFeedback(adjustedSlot, newValid, newError);
                                }
                            }
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // Invalid slot index, skip
            }
        }
    }
    
    /**
     * 🎆 ENHANCED: Find slot number by name
     */
    private Integer findSlotNumberByName(String slotName) {
        var actionConfigurations = blockConfigService.getActionConfigurations();
        if (actionConfigurations == null) return null;
        
        var actionConfig = actionConfigurations.getConfigurationSection(actionId);
        if (actionConfig == null) return null;
        
        var slotsConfig = actionConfig.getConfigurationSection("slots");
        if (slotsConfig == null) return null;
        
        // Find the slot configuration by slot_name
        for (String slotKey : slotsConfig.getKeys(false)) {
            try {
                var slotConfig = slotsConfig.getConfigurationSection(slotKey);
                if (slotConfig != null) {
                    String configSlotName = slotConfig.getString("slot_name");
                    if (slotName.equals(configSlotName)) {
                        return Integer.parseInt(slotKey);
                    }
                }
            } catch (NumberFormatException e) {
                // Invalid slot index, skip
            }
        }
        
        return null;
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
        
        // Adjust slot index back to original
        int originalSlot = slot - 9; // Adjust back from row 2
        if (originalSlot < 0) return false;
        
        var slotConfig = slotsConfig.getConfigurationSection(String.valueOf(originalSlot));
        if (slotConfig == null) return false;
        
        return slotConfig.getBoolean("required", originalSlot == 0); // First slot usually required
    }
    
    /**
     * 🎆 ENHANCED: Validate item content for specific slot
     */
    private String validateItemForSlot(int slot, ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return "Нужно переименовать предмет";
        }
        
        String itemName = item.getItemMeta().getDisplayName();
        
        // Get slot name from configuration
        String slotName = getSlotName(slot);
        
        // Action-specific validation based on slot name
        if (slotName != null) {
            // Validate based on slot name and validation rules from config
            String validationError = validateItemBySlotName(slotName, item);
            if (validationError != null) {
                return validationError;
            }
        }
        
        // Fallback to action-specific validation
        switch (actionId.toLowerCase()) {
            case "sendmessage":
                if (slot == 10 && itemName.trim().isEmpty()) { // Adjusted slot index
                    return "Сообщение не может быть пустым";
                }
                break;
            case "executeasynccommand":
                if (slot == 10 && !itemName.startsWith("/") && !itemName.contains(":")) { // Adjusted slot index
                    return "Команда должна начинаться с '/' или содержать ':'";
                }
                break;

            case "giveitem":
                if (slot == 10 && item.getType().isAir()) { // Adjusted slot index
                    return "Предмет не может быть пустым";
                }
                if (slot == 11 && !isValidNumber(itemName)) { // Adjusted slot index
                    return "Количество должно быть числом";
                }
                break;
            case "playsound":
                if (slot == 10 && !isValidSoundName(itemName)) { // Adjusted slot index
                    return "Неверное имя звука";
                }
                if (slot == 11 && !isValidNumberInRange(itemName, 0.0, 1.0)) { // Adjusted slot index
                    return "Громкость должна быть от 0.0 до 1.0";
                }
                if (slot == 12 && !isValidNumberInRange(itemName, 0.5, 2.0)) { // Adjusted slot index
                    return "Тон должен быть от 0.5 до 2.0";
                }
                break;
            case "effect":
                if (slot == 10 && !isValidEffectName(itemName)) { // Adjusted slot index
                    return "Неверное имя эффекта";
                }
                if (slot == 11 && !isValidNumber(itemName)) { // Adjusted slot index
                    return "Длительность должна быть числом";
                }
                if (slot == 12 && !isValidNumberInRange(itemName, 1, 255)) { // Adjusted slot index
                    return "Уровень должен быть от 1 до 255";
                }
                break;
            case "wait":
                if (slot == 10 && !isValidNumber(itemName)) { // Adjusted slot index
                    return "Время ожидания должно быть числом";
                }
                break;
            default:
                // No specific validation for this action
                break;
        }
        
        return null; // No error
    }
    
    /**
     * 🎆 ENHANCED: Get slot name from configuration
     */
    private String getSlotName(int slot) {
        var actionConfigurations = blockConfigService.getActionConfigurations();
        if (actionConfigurations == null) return null;
        
        var actionConfig = actionConfigurations.getConfigurationSection(actionId);
        if (actionConfig == null) return null;
        
        var slotsConfig = actionConfig.getConfigurationSection("slots");
        if (slotsConfig == null) return null;
        
        // Adjust slot index back to original
        int originalSlot = slot - 9; // Adjust back from row 2
        if (originalSlot < 0) return null;
        
        var slotConfig = slotsConfig.getConfigurationSection(String.valueOf(originalSlot));
        if (slotConfig == null) return null;
        
        return slotConfig.getString("slot_name");
    }
    
    /**
     * 🎆 ENHANCED: Validate item by slot name and validation rules
     */
    private String validateItemBySlotName(String slotName, ItemStack item) {
        var actionConfigurations = blockConfigService.getActionConfigurations();
        if (actionConfigurations == null) return null;
        
        var actionConfig = actionConfigurations.getConfigurationSection(actionId);
        if (actionConfig == null) return null;
        
        var slotsConfig = actionConfig.getConfigurationSection("slots");
        if (slotsConfig == null) return null;
        
        // Find the slot configuration by slot_name
        for (String slotKey : slotsConfig.getKeys(false)) {
            var slotConfig = slotsConfig.getConfigurationSection(slotKey);
            if (slotConfig != null) {
                String configSlotName = slotConfig.getString("slot_name");
                if (slotName.equals(configSlotName)) {
                    // Found the slot, check validation rules
                    String validation = slotConfig.getString("validation");
                    if (validation != null) {
                        return validateItemByRule(item, validation);
                    }
                    break;
                }
            }
        }
        
        return null; // No validation rule found
    }
    
    /**
     * 🎆 ENHANCED: Validate item by validation rule
     */
    private String validateItemByRule(ItemStack item, String validationRule) {
        String itemName = item.hasItemMeta() ? item.getItemMeta().getDisplayName() : "";
        
        switch (validationRule) {
            case "number":
                if (!isValidNumber(itemName)) {
                    return "Значение должно быть числом";
                }
                break;
            case "sound_name":
                if (!isValidSoundName(itemName)) {
                    return "Неверное имя звука";
                }
                break;
            case "effect_name":
                if (!isValidEffectName(itemName)) {
                    return "Неверное имя эффекта";
                }
                break;
            case "player_name":
                if (!isValidPlayerName(itemName)) {
                    return "Неверное имя игрока";
                }
                break;
            case "world_name":
                if (!isValidWorldName(itemName)) {
                    return "Неверное имя мира";
                }
                break;
            case "material_name":
                if (!isValidMaterialName(itemName)) {
                    return "Неверное имя материала";
                }
                break;
            case "color_hex":
                if (!isValidHexColor(itemName)) {
                    return "Неверный формат цвета (должен быть #RRGGBB)";
                }
                break;
            case "email":
                if (!isValidEmail(itemName)) {
                    return "Неверный формат email";
                }
                break;
            case "url":
                if (!isValidUrl(itemName)) {
                    return "Неверный формат URL";
                }
                break;
            default:
                // Handle range validations like "number_range:0.0-1.0"
                if (validationRule.startsWith("number_range:")) {
                    String range = validationRule.substring("number_range:".length());
                    String[] parts = range.split("-");
                    if (parts.length == 2) {
                        try {
                            double min = Double.parseDouble(parts[0]);
                            double max = Double.parseDouble(parts[1]);
                            if (!isValidNumberInRange(itemName, min, max)) {
                                return "Значение должно быть от " + min + " до " + max;
                            }
                        } catch (NumberFormatException e) {
                            return "Неверный формат диапазона";
                        }
                    }
                }
                // Handle regex validations like "regex:[a-zA-Z]+"
                else if (validationRule.startsWith("regex:")) {
                    String regex = validationRule.substring("regex:".length());
                    if (!isValidRegex(itemName, regex)) {
                        return "Значение не соответствует формату: " + regex;
                    }
                }
                // Handle length validations like "length:5-20"
                else if (validationRule.startsWith("length:")) {
                    String lengthSpec = validationRule.substring("length:".length());
                    if (!isValidLength(itemName, lengthSpec)) {
                        return "Длина значения должна быть от " + lengthSpec.replace("-", " до ");
                    }
                }
                // Handle enum validations like "enum:option1,option2,option3"
                else if (validationRule.startsWith("enum:")) {
                    String enumValues = validationRule.substring("enum:".length());
                    if (!isValidEnum(itemName, enumValues)) {
                        return "Значение должно быть одним из: " + enumValues;
                    }
                }
                break;
        }
        
        return null; // No error
    }
    
    /**
     * 🎆 ENHANCED: Check if string represents a valid world name
     */
    private boolean isValidWorldName(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) return false;
        
        // Remove color codes
        String cleaned = worldName.replaceAll("§[0-9a-fk-r]", "").trim();
        
        // Check for pattern like "world:Name" or just "Name"
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        
        // World names can contain letters, numbers, underscores, hyphens, and dots
        return cleaned.matches("[a-zA-Z0-9_.\\-]+");
    }

    /**
     * 🎆 ENHANCED: Get slot index by slot name
     */
    private Integer getSlotIndexByName(String slotName) {
        org.bukkit.configuration.ConfigurationSection actionConfigurations = blockConfigService.getActionConfigurations();
        if (actionConfigurations == null) return null;
        
        org.bukkit.configuration.ConfigurationSection actionConfig = actionConfigurations.getConfigurationSection(actionId);
        if (actionConfig == null) return null;
        
        org.bukkit.configuration.ConfigurationSection slotsConfig = actionConfig.getConfigurationSection("slots");
        if (slotsConfig == null) return null;
        
        // Find the slot configuration by slot_name
        for (String slotKey : slotsConfig.getKeys(false)) {
            try {
                org.bukkit.configuration.ConfigurationSection slotConfig = slotsConfig.getConfigurationSection(slotKey);
                if (slotConfig != null) {
                    String configSlotName = slotConfig.getString("slot_name");
                    if (slotName.equals(configSlotName)) {
                        return Integer.parseInt(slotKey);
                    }
                }
            } catch (NumberFormatException e) {
                // Invalid slot index, skip
            }
        }
        
        return null;
    }
    
    /**
     * 🎆 ENHANCED: Check if string represents a valid player name
     */
    private boolean isValidPlayerName(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) return false;
        
        // Remove color codes (0-9, a-f, k-r) and common prefixes
        String cleaned = playerName.replaceAll("§[0-9a-fk-r]", "").trim();
        
        // Check for pattern like "player:Name" or just "Name"
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        
        // Player names can contain letters, numbers, underscores, hyphens, and dots
        return cleaned.matches("[a-zA-Z0-9_.\\-]+");
    }

    /**
     * 🎆 ENHANCED: Check if string represents a valid material name
     */
    private boolean isValidMaterialName(String materialName) {
        if (materialName == null || materialName.trim().isEmpty()) return false;
        
        // Remove color codes
        String cleaned = materialName.replaceAll("§[0-9a-fk-r]", "").trim();
        
        // Check for pattern like "material:Name" or just "Name"
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
            // Try parsing as double for decimal numbers
            try {
                Double.parseDouble(cleaned);
                return true;
            } catch (NumberFormatException e2) {
                return false;
            }
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
        lore.removeIf(line -> line.contains("✓") || line.contains("✗") || line.contains("Ошибка:") || line.contains("Статус:") || line.contains("Подсказка:"));
        
        // Add new validation status with enhanced visual feedback
        if (isValid) {
            lore.add("§a✓ Параметр корректен");
            lore.add("§7Статус: §aГотов к использованию");
            
            // Add glow effect for valid items
            if (meta.hasEnchants()) {
                meta.removeEnchant(org.bukkit.enchantments.Enchantment.DURABILITY);
            }
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
        } else if (error != null) {
            lore.add("§c✗ Ошибка: " + error);
            lore.add("§7Статус: §cТребуется исправление");
            
            // Add red glow effect for invalid items
            if (meta.hasEnchants()) {
                meta.removeEnchant(org.bukkit.enchantments.Enchantment.DURABILITY);
            }
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 1, true);
        } else {
            lore.add("§7Статус: §eОжидает проверки");
        }
        
        // Add helpful hints based on slot configuration
        String slotName = getSlotName(slot);
        if (slotName != null) {
            String hint = getValidationHint(slotName);
            if (hint != null && !hint.isEmpty()) {
                lore.add("§bПодсказка: §7" + hint);
            }
        }
        
        meta.setLore(lore);
        currentItem.setItemMeta(meta);
        
        // Update item in inventory
        inventory.setItem(slot, currentItem);
        
        // Add particle effect for validation feedback
        Location effectLoc = player.getLocation().add(0, 1, 0);
        if (isValid) {
            player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, effectLoc, 5, 0.3, 0.3, 0.3, 0.1);
        } else if (error != null) {
            player.spawnParticle(org.bukkit.Particle.FLAME, effectLoc, 5, 0.3, 0.3, 0.3, 0.05);
        }
    }
    
    /**
     * 🎆 ENHANCED: Get validation hint for slot name
     */
    private String getValidationHint(String slotName) {
        var actionConfigurations = blockConfigService.getActionConfigurations();
        if (actionConfigurations == null) return null;
        
        var actionConfig = actionConfigurations.getConfigurationSection(actionId);
        if (actionConfig == null) return null;
        
        var slotsConfig = actionConfig.getConfigurationSection("slots");
        if (slotsConfig == null) return null;
        
        // Find the slot configuration by slot_name
        for (String slotKey : slotsConfig.getKeys(false)) {
            var slotConfig = slotsConfig.getConfigurationSection(slotKey);
            if (slotConfig != null) {
                String configSlotName = slotConfig.getString("slot_name");
                if (slotName.equals(configSlotName)) {
                    // Found the slot, get hint
                    return slotConfig.getString("hint", "");
                }
            }
        }
        
        return null;
    }
    
    /**
     * 🎆 ENHANCED: Update visual feedback for all slots
     */
    private void updateAllSlotsVisualFeedback() {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.getType().isAir() && !isPlaceholderItem(item)) {
                String error = slotValidationErrors.get(slot);
                Boolean isValid = slotValidationStatus.get(slot);
                updateSlotVisualFeedback(slot, isValid != null ? isValid : true, error);
            }
        }
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
        
        // Audio feedback when opening GUI
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 0.6f, 1.1f);
        
        // Add visual effects for reference system-style magic
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, 
            player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 1);
    }
    
    @Override
    /**
     * Gets the GUI title
     * @return Interface title
     */
    public String getGUITitle() {
        return "Action Parameter GUI for " + actionId;
    }
    
    @Override
    /**
     * Handles inventory click events
     * @param event Inventory click event
     */
    public void onInventoryClick(InventoryClickEvent event) {
        if (!player.equals(event.getWhoClicked())) return;
        if (!inventory.equals(event.getInventory())) return;
        
        event.setCancelled(true); // Cancel all clicks by default
        
        int slot = event.getSlot();
        
        // Handle back button click
        if (slot == 49) {
            // Go back to action selection
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                ActionSelectionGUI actionGUI = new ActionSelectionGUI(plugin, player, blockLocation, Material.STONE); // Default material
                actionGUI.open();
            }, 1L);
            return;
        }
        
        // Allow interaction with center slots for parameter configuration
        if (slot >= 9 && slot < 45 && slot % 9 != 0 && slot % 9 != 8) {
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
    /**
     * Handles inventory close events
     * @param event Inventory close event
     */
    public void onInventoryClose(InventoryCloseEvent event) {
        // Save parameters when GUI is closed
        saveParameters();
        
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
    
    /**
     * 🎆 ENHANCED: Check if string is a valid number
     */
    private boolean isValidNumber(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 🎆 ENHANCED: Check if string is a valid number in range
     */
    private boolean isValidNumberInRange(String str, double min, double max) {
        if (!isValidNumber(str)) return false;
        try {
            double value = Double.parseDouble(str);
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 🎆 ENHANCED: Check if string is a valid number in range
     */
    private boolean isValidNumberInRange(String str, int min, int max) {
        if (!isValidNumber(str)) return false;
        try {
            int value = Integer.parseInt(str);
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 🎆 ENHANCED: Check if string is a valid sound name
     */
    private boolean isValidSoundName(String soundName) {
        if (soundName == null || soundName.isEmpty()) return false;
        try {
            org.bukkit.Sound.valueOf(soundName.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 🎆 ENHANCED: Check if string is a valid effect name
     */
    private boolean isValidEffectName(String effectName) {
        if (effectName == null || effectName.isEmpty()) return false;
        try {
            org.bukkit.potion.PotionEffectType.getByName(effectName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 🎆 ENHANCED: Check if string is a valid regex
     */
    private boolean isValidRegex(String str, String regex) {
        if (str == null || str.isEmpty() || regex == null || regex.isEmpty()) return false;
        try {
            java.util.regex.Pattern.compile(regex);
            return str.matches(regex);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 🎆 ENHANCED: Check if string matches length specification
     */
    private boolean isValidLength(String str, String lengthSpec) {
        if (str == null || lengthSpec == null) return false;
        
        try {
            if (lengthSpec.contains("-")) {
                String[] parts = lengthSpec.split("-");
                int min = Integer.parseInt(parts[0].trim());
                int max = Integer.parseInt(parts[1].trim());
                return str.length() >= min && str.length() <= max;
            } else {
                int exact = Integer.parseInt(lengthSpec);
                return str.length() == exact;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 🎆 ENHANCED: Check if string is a valid hex color
     */
    private boolean isValidHexColor(String color) {
        if (color == null || color.isEmpty()) return false;
        return color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    }
    
    /**
     * 🎆 ENHANCED: Check if string is a valid email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }
    
    /**
     * 🎆 ENHANCED: Check if string is a valid URL
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 🎆 ENHANCED: Check if string is a valid enum value
     */
    private boolean isValidEnum(String value, String enumValues) {
        if (value == null || value.isEmpty() || enumValues == null || enumValues.isEmpty()) return false;
        
        String[] values = enumValues.split(",");
        for (String enumValue : values) {
            if (value.equalsIgnoreCase(enumValue.trim())) {
                return true;
            }
        }
        return false;
    }
}