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
 * Расширенный графический интерфейс перетаскивания для настройки параметров действий
 * 🎆 РАСШИРЕННЫЕ ФУНКЦИИ:
 * - Динамическая настройка слотов на основе YAML
 * - Валидация параметров в реальном времени
 * - Визуальная обратная связь по статусу конфигурации
 * - Умная генерация заполнителей
 * - Предотвращение ошибок и руководство пользователя
 * 
 * Расширенный графический интерфейс перетаскивания для настройки параметров действий
 * 🎆 УЛУЧШЕННЫЕ ФУНКЦИИ:
 * - Динамическая конфигурация слотов на основе YAML
 * - Валидация параметров в реальном времени
 * - Визуальная обратная связь по статусу конфигурации
 * - Умная генерация заполнителей
 * - Предотвращение ошибок и руководство пользователя
 *
 * Advanced drag-and-drop GUI for configuring action parameters
 * 🎆 ENHANCED FEATURES:
 * - Dynamic YAML-driven slot configuration
 * - Real-time parameter validation
 * - Visual feedback for configuration status
 * - Smart placeholder generation
 * - Error prevention and user guidance
 *
 * Erweiterte Drag-and-Drop-GUI zur Konfiguration von Aktionsparametern
 * 🎆 ERWEITERT FUNKTIONEN:
 * - Dynamische YAML-gesteuerte Slot-Konfiguration
 * - Echtzeit-Parameter-Validierung
 * - Visuelle Rückmeldung zum Konfigurationsstatus
 * - Intelligente Platzhaltergenerierung
 * - Fehlervermeidung und Benutzerführung
 * 
 * Features intuitive interface for each action type with named slots and item groups
 * Based on the configuration from coding_blocks.yml
 *
 * Bietet eine intuitive Schnittstelle für jeden Aktionstyp mit benannten Slots und Artikelgruppen
 * Basierend auf der Konfiguration aus coding_blocks.yml
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
     * Инициализирует графический интерфейс параметров действий
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param blockLocation Расположение блока для настройки
     * @param actionId Идентификатор действия для настройки
     *
     * Initializes action parameters GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param blockLocation Location of block to configure
     * @param actionId Action ID to configure
     *
     * Initialisiert die Aktionsparameter-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     * @param blockLocation Position des zu konfigurierenden Blocks
     * @param actionId Aktions-ID zum Konfigurieren
     */
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
    
    /**
     * Настраивает инвентарь графического интерфейса
     *
     * Sets up the GUI inventory
     *
     * Richtet das GUI-Inventar ein
     */
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
     * Загружает конфигурацию действия из coding_blocks.yml и настраивает элементы-заполнители
     *
     * Loads the action configuration from coding_blocks.yml and sets up placeholder items
     *
     * Lädt die Aktionskonfiguration aus coding_blocks.yml und richtet Platzhalterelemente ein
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
     * Настраивает именованные слоты на основе конфигурации
     *
     * Sets up named slots based on configuration
     *
     * Richtet benannte Slots basierend auf der Konфигuration ein
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
     * Настраивает группы предметов на основе конфигурации
     *
     * Sets up item groups based on configuration
     *
     * Richtet Artikelgruppen basierend auf der Konфигuration ein
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
     * Настраивает базовые слоты, когда конкретная конфигурация не найдена
     *
     * Sets up generic slots when no specific configuration is found
     *
     * Richtet generische Slots ein, wenn keine spezifische Konфигuration gefunden wird
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
     * Загружает существующие параметры из блока кода в графический интерфейс
     *
     * Loads existing parameters from the code block into the GUI
     *
     * Lädt vorhandene Parameter aus dem Codeblock in die GUI
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
     * Сохраняет настроенные параметры обратно в блок кода
     * 🎆 УЛУЧШЕННОЕ: С обратной связью по валидации
     *
     * Saves the configured parameters back to the code block
     * 🎆 ENHANCED: With validation feedback
     *
     * Speichert die konfigurierten Parameter zurück in den Codeblock
     * 🎆 ERWEITERT: Mit Validierungs-Rückmeldung
     */
    private void saveParameters() {
        BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
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
        var creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getWorldManager().saveWorld(creativeWorld);
        }
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Валидация параметров в реальном времени
     * Проверяет конфигурацию определенного слота и предоставляет пользователю обратную связь
     *
     * 🎆 ENHANCED: Real-time parameter validation
     * Validates a specific slot configuration and provides user feedback
     *
     * 🎆 ERWEITERT: Echtzeit-Parameter-Validierung
     * Validiert eine bestimmte Slot-Konfiguration und gibt dem Benutzer Feedback
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
     * 🎆 УЛУЧШЕННОЕ: Проверяет зависимые слоты при изменении значения
     *
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
                if (slot == changedSlot) continue; // Skip the slot that just changed
                
                var slotConfig = slotsConfig.getConfigurationSection(slotKey);
                if (slotConfig == null) continue;
                
                // Check for dependencies
                String dependsOn = slotConfig.getString("depends_on");
                if (dependsOn != null && !dependsOn.isEmpty()) {
                    // Parse dependency: "slotName=value" or "slotName!=value"
                    String[] parts = dependsOn.split("(!?=)");
                    if (parts.length >= 2) {
                        String dependencySlotName = parts[0].trim();
                        String expectedValue = parts[1].trim();
                        boolean isNotEqual = dependsOn.contains("!=");
                        
                        // Find the dependency slot number
                        Integer dependencySlot = findSlotNumberByName(dependencySlotName);
                        if (dependencySlot != null) {
                            String currentValue = slotCurrentValues.get(dependencySlot);
                            
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
                                slotValidationErrors.put(slot, error);
                                slotValidationStatus.put(slot, false);
                                updateSlotVisualFeedback(slot, false, error);
                            } else {
                                // Re-validate the slot since dependency condition is now met
                                ItemStack item = inventory.getItem(slot);
                                if (item != null && !item.getType().isAir()) {
                                    String newError = validateItemForSlot(slot, item);
                                    boolean newValid = (newError == null);
                                    slotValidationErrors.put(slot, newError);
                                    slotValidationStatus.put(slot, newValid);
                                    updateSlotVisualFeedback(slot, newValid, newError);
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
     * 🎆 УЛУЧШЕННОЕ: Находит номер слота по имени
     *
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
     * 🎆 УЛУЧШЕННОЕ: Проверяет, является ли слот обязательным для этого действия
     *
     * 🎆 ENHANCED: Check if a slot is required for this action
     *
     * 🎆 ERWEITERT: Prüft, ob ein Slot für diese Aktion erforderlich ist
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
     * 🎆 УЛУЧШЕННОЕ: Проверяет содержимое предмета для определенного слота
     *
     * 🎆 ENHANCED: Validate item content for specific slot
     *
     * 🎆 ERWEITERT: Validiert den Artikelinhalt für einen bestimmten Slot
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
                if (slot == 0 && itemName.trim().isEmpty()) {
                    return "Сообщение не может быть пустым";
                }
                break;
            case "executeasynccommand":
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
            case "giveitem":
                if (slot == 0 && item.getType().isAir()) {
                    return "Предмет не может быть пустым";
                }
                if (slot == 1 && !isValidNumber(itemName)) {
                    return "Количество должно быть числом";
                }
                break;
            case "playsound":
                if (slot == 0 && !isValidSoundName(itemName)) {
                    return "Неверное имя звука";
                }
                if (slot == 1 && !isValidNumberInRange(itemName, 0.0, 1.0)) {
                    return "Громкость должна быть от 0.0 до 1.0";
                }
                if (slot == 2 && !isValidNumberInRange(itemName, 0.5, 2.0)) {
                    return "Тон должен быть от 0.5 до 2.0";
                }
                break;
            case "effect":
                if (slot == 0 && !isValidEffectName(itemName)) {
                    return "Неверное имя эффекта";
                }
                if (slot == 1 && !isValidNumber(itemName)) {
                    return "Длительность должна быть числом";
                }
                if (slot == 2 && !isValidNumberInRange(itemName, 1, 255)) {
                    return "Уровень должен быть от 1 до 255";
                }
                break;
            case "wait":
                if (slot == 0 && !isValidNumber(itemName)) {
                    return "Время ожидания должно быть числом";
                }
                break;
        }
        
        return null; // No error
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Получает имя слота из конфигурации
     *
     * 🎆 ENHANCED: Get slot name from configuration
     *
     * 🎆 ERWEITERT: Ruft den Slot-Namen aus der Konфигuration ab
     */
    private String getSlotName(int slot) {
        var actionConfigurations = blockConfigService.getActionConfigurations();
        if (actionConfigurations == null) return null;
        
        var actionConfig = actionConfigurations.getConfigurationSection(actionId);
        if (actionConfig == null) return null;
        
        var slotsConfig = actionConfig.getConfigurationSection("slots");
        if (slotsConfig == null) return null;
        
        var slotConfig = slotsConfig.getConfigurationSection(String.valueOf(slot));
        if (slotConfig == null) return null;
        
        return slotConfig.getString("slot_name");
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Проверяет предмет по имени слота и правилам валидации
     *
     * 🎆 ENHANCED: Validate item by slot name and validation rules
     *
     * 🎆 ERWEITERT: Validiert den Artikel nach Slot-Name und Validierungsregeln
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
     * 🎆 УЛУЧШЕННОЕ: Проверяет предмет по правилу валидации
     *
     * 🎆 ENHANCED: Validate item by validation rule
     *
     * 🎆 ERWEITERT: Validiert den Artikel nach Validierungsregel
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
     * 🎆 УЛУЧШЕННОЕ: Проверяет, представляет ли строка допустимое имя игрока
     *
     * 🎆 ENHANCED: Check if string represents a valid player name
     */
    private boolean isValidPlayerName(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) return false;
        
        // Remove color codes
        String cleaned = playerName.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Check for pattern like "player:Name" or just "Name"
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        
        // Player names should be 3-16 characters, alphanumeric and underscores
        return cleaned.matches("[a-zA-Z0-9_]{3,16}");
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Проверяет, представляет ли строка допустимое имя мира
     *
     * 🎆 ENHANCED: Check if string represents a valid world name
     */
    private boolean isValidWorldName(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) return false;
        
        // Remove color codes
        String cleaned = worldName.replaceAll("§[0-9a-fk-or]", "").trim();
        
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
     * 🎆 УЛУЧШЕННОЕ: Проверяет, представляет ли строка допустимое имя материала
     *
     * 🎆 ENHANCED: Check if string represents a valid material name
     */
    private boolean isValidMaterialName(String materialName) {
        if (materialName == null || materialName.trim().isEmpty()) return false;
        
        // Remove color codes
        String cleaned = materialName.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Check for pattern like "material:Name" or just "Name"
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        
        // Check if material exists
        return org.bukkit.Material.matchMaterial(cleaned) != null;
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Проверяет, представляет ли строка допустимый HEX цвет
     *
     * 🎆 ENHANCED: Check if string represents a valid hex color
     */
    private boolean isValidHexColor(String color) {
        if (color == null || color.trim().isEmpty()) return false;
        
        // Remove color codes
        String cleaned = color.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Check for pattern like "color:#RRGGBB" or just "#RRGGBB"
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        
        // HEX color should be in format #RRGGBB
        return cleaned.matches("#[0-9a-fA-F]{6}");
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Проверяет, представляет ли строка допустимый email
     *
     * 🎆 ENHANCED: Check if string represents a valid email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        
        // Remove color codes
        String cleaned = email.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Check for pattern like "email:address@domain.com" or just "address@domain.com"
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        
        // Basic email validation
        return cleaned.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Проверяет, представляет ли строка допустимый URL
     *
     * 🎆 ENHANCED: Check if string represents a valid URL
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        
        // Remove color codes
        String cleaned = url.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Check for pattern like "url:https://example.com" or just "https://example.com"
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        
        // Basic URL validation - fixed regex with proper escaping
        return cleaned.matches("https?://[\\w.-]+(?:\\.[\\w.-]+)+[/\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]*");
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Проверяет, соответствует ли строка регулярному выражению
     *
     * 🎆 ENHANCED: Check if string matches a regex pattern
     */
    private boolean isValidRegex(String str, String regex) {
        if (str == null || str.trim().isEmpty()) return false;
        
        // Remove color codes
        String cleaned = str.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Check for pattern like "value:text" or just "text"
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        
        try {
            return cleaned.matches(regex);
        } catch (Exception e) {
            return false; // Invalid regex pattern
        }
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Проверяет, соответствует ли длина строки спецификации
     *
     * 🎆 ENHANCED: Check if string length matches specification
     */
    private boolean isValidLength(String str, String lengthSpec) {
        if (str == null) return false;
        
        // Remove color codes
        String cleaned = str.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Check for pattern like "value:text" or just "text"
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        
        String[] parts = lengthSpec.split("-");
        if (parts.length == 1) {
            try {
                int exactLength = Integer.parseInt(parts[0]);
                return cleaned.length() == exactLength;
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (parts.length == 2) {
            try {
                int minLength = Integer.parseInt(parts[0]);
                int maxLength = Integer.parseInt(parts[1]);
                int length = cleaned.length();
                return length >= minLength && length <= maxLength;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Проверяет, является ли значение одним из перечисленных
     *
     * 🎆 ENHANCED: Check if value is one of the enumerated values
     */
    private boolean isValidEnum(String str, String enumValues) {
        if (str == null || str.trim().isEmpty()) return false;
        
        // Remove color codes
        String cleaned = str.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Check for pattern like "value:text" or just "text"
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        
        String[] values = enumValues.split(",");
        for (String value : values) {
            if (cleaned.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Проверяет, представляет ли строка допустимое имя звука
     *
     * 🎆 ENHANCED: Check if string represents a valid sound name
     *
     * 🎆 ERWEITERT: Prüft, ob die Zeichenfolge einen gültigen Klangnamen darstellt
     */
    private boolean isValidSoundName(String soundName) {
        if (soundName == null || soundName.trim().isEmpty()) return false;
        
        // Remove color codes and common prefixes
        String cleaned = soundName.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Check for common sound name patterns
        return cleaned.contains(":") || 
               cleaned.startsWith("minecraft:") || 
               cleaned.contains("block.") || 
               cleaned.contains("entity.") || 
               cleaned.contains("item.") || 
               cleaned.contains("music.") || 
               cleaned.contains("ambient.");
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Проверяет, представляет ли строка допустимое имя эффекта
     *
     * 🎆 ENHANCED: Check if string represents a valid effect name
     *
     * 🎆 ERWEITERT: Prüft, ob die Zeichenfolge einen gültigen Effektnamen darstellt
     */
    private boolean isValidEffectName(String effectName) {
        if (effectName == null || effectName.trim().isEmpty()) return false;
        
        // Remove color codes and common prefixes
        String cleaned = effectName.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Check for common effect names
        String[] validEffects = {
            "SPEED", "SLOW", "FAST_DIGGING", "SLOW_DIGGING", "INCREASE_DAMAGE", 
            "HEAL", "HARM", "JUMP", "CONFUSION", "REGENERATION", "DAMAGE_RESISTANCE",
            "FIRE_RESISTANCE", "WATER_BREATHING", "INVISIBILITY", "BLINDNESS",
            "NIGHT_VISION", "HUNGER", "WEAKNESS", "POISON", "WITHER", "HEALTH_BOOST",
            "ABSORPTION", "SATURATION", "GLOWING", "LEVITATION", "LUCK", "UNLUCK",
            "SLOW_FALLING", "CONDUIT_POWER", "DOLPHINS_GRACE", "BAD_OMEN", "HERO_OF_THE_VILLAGE"
        };
        
        for (String effect : validEffects) {
            if (effect.equalsIgnoreCase(cleaned)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Проверяет, представляет ли строка допустимое число в диапазоне
     *
     * 🎆 ENHANCED: Check if string represents a valid number in range
     *
     * 🎆 ERWEITERT: Prüft, ob die Zeichenfolge eine gültige Zahl im Bereich darstellt
     */
    private boolean isValidNumberInRange(String str, double min, double max) {
        if (str == null || str.trim().isEmpty()) return false;
        
        // Remove color codes and common prefixes
        String cleaned = str.replaceAll("§[0-9a-fk-or]", "").trim();
        
        // Check for pattern like "value:5" or "amount:20"
        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            if (parts.length == 2) {
                cleaned = parts[1].trim();
            }
        }
        
        try {
            double value = Double.parseDouble(cleaned);
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 🎆 УЛУЧШЕННОЕ: Проверяет, представляет ли строка допустимое число
     *
     * 🎆 ENHANCED: Check if string represents a valid number
     *
     * 🎆 ERWEITERT: Prüft, ob die Zeichenfolge eine gültige Zahl darstellt
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
     * 🎆 УЛУЧШЕННОЕ: Обновляет визуальную обратную связь для валидации слота
     *
     * 🎆 ENHANCED: Update visual feedback for slot validation
     *
     * 🎆 ERWEITERT: Aktualisiert die visuelle Rückmeldung zur Slot-Validierung
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
     * 🎆 УЛУЧШЕННОЕ: Получает подсказку по валидации для имени слота
     *
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
     * 🎆 УЛУЧШЕННОЕ: Обновляет визуальную обратную связь для всех слотов
     *
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
     * Проверяет, является ли предмет элементом-заполнителем
     *
     * Checks if an item is a placeholder item
     *
     * Prüft, ob ein Artikel ein Platzhalterartikel ist
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
     * Открывает графический интерфейс для игрока
     *
     * Opens the GUI for the player
     *
     * Öffnet die GUI für den Spieler
     */
    public void open() {
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        // Аудио обратная связь при открытии GUI
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 0.6f, 1.1f);
    }
    
    @Override
    /**
     * Получает заголовок графического интерфейса
     * @return Заголовок интерфейса
     *
     * Gets the GUI title
     * @return Interface title
     *
     * Ruft den GUI-Titel ab
     * @return Schnittstellentitel
     */
    public String getGUITitle() {
        return "Action Parameter GUI for " + actionId;
    }
    
    @Override
    /**
     * Обрабатывает события кликов в инвентаре
     * @param event Событие клика в инвентаре
     *
     * Handles inventory click events
     * @param event Inventory click event
     *
     * Verarbeitet Inventarklick-Ereignisse
     * @param event Inventarklick-Ereignis
     */
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
    /**
     * Обрабатывает события закрытия инвентаря
     * @param event Событие закрытия инвентаря
     *
     * Handles inventory close events
     * @param event Inventory close event
     *
     * Verarbeitet Inventarschließ-Ereignisse
     * @param event Inventarschließ-Ereignis
     */
    public void onInventoryClose(InventoryCloseEvent event) {
        // Save parameters when GUI is closed
        saveParameters();
        
        // Optional cleanup when GUI is closed
        // GUIManager handles automatic unregistration
    }
    
    @Override
    /**
     * Выполняет очистку ресурсов при закрытии интерфейса
     *
     * Performs resource cleanup when interface is closed
     *
     * Führt eine Ressourcenbereinigung durch, wenn die Schnittstelle geschlossen wird
     */
    public void onCleanup() {
        // Called when GUI is being cleaned up by GUIManager
        // No special cleanup needed for this GUI
    }
}