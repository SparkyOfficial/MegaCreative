package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.*;
import com.megacreative.services.BlockConfigService;
import com.megacreative.services.BlockConfigService.ParameterConfig;
import com.megacreative.gui.coding.ActionSelectionGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced Block Configuration Manager with DataValue integration
 * Manages virtual inventories for block configuration and converts between:
 * - Visual ItemStack configuration (GUI)
 * - Type-safe DataValue parameters (CodeBlock)
 * 
 * Key Features:
 * - Automatic conversion between ItemStacks and DataValue types
 * - Support for named slots and item groups from coding_blocks.yml
 * - Real-time parameter extraction and validation
 * - Seamless integration with the new DataValue parameter system
 */
public class BlockConfigManager implements Listener {

    private final MegaCreative plugin;
    // Отслеживаем, какой игрок какой блок сейчас настраивает
    private final Map<UUID, Location> configuringBlocks = new ConcurrentHashMap<>();

    public BlockConfigManager(MegaCreative plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Shuts down the BlockConfigManager and cleans up resources
     */
    public void shutdown() {
        // Clear all configuring blocks to prevent memory leaks
        configuringBlocks.clear();
        
        // Any other cleanup if needed
    }

    /**
     * Открывает GUI для настройки блока
     * @param player Игрок, который будет настраивать блок
     * @param blockLocation Локация блока для настройки
     */
    public void openConfigGUI(Player player, Location blockLocation) {
        CodeBlock codeBlock = plugin.getServiceRegistry().getBlockPlacementHandler().getBlockCodeBlocks().get(blockLocation);
        if (codeBlock == null) {
            player.sendMessage("§cОшибка: блок кода не найден.");
            return;
        }

        // Check if block needs action selection first
        String currentAction = codeBlock.getAction();
        if (currentAction == null || currentAction.equals("Настройка...") || currentAction.isEmpty()) {
            // Show action selection GUI first
            showActionSelectionGUI(player, codeBlock, blockLocation);
        } else {
            // Show parameter configuration GUI
            showParameterConfigGUI(player, codeBlock, blockLocation);
        }
    }
    
    /**
     * Shows action selection GUI for the block
     */
    private void showActionSelectionGUI(Player player, CodeBlock codeBlock, Location blockLocation) {
        // Get available actions from BlockConfigService
        var blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        List<String> availableActions = blockConfigService.getActionsForMaterial(codeBlock.getMaterial());
        
        if (availableActions.isEmpty()) {
            player.sendMessage("§cОшибка: нет доступных действий для данного типа блока.");
            return;
        }
        
        player.sendMessage("§eВыберите действие для блока...");
        
        // Create and open ActionSelectionGUI
        ActionSelectionGUI actionGUI = new ActionSelectionGUI(
            plugin, 
            player, 
            blockLocation, 
            codeBlock.getMaterial()
        );
        actionGUI.open();
    }
    
    /**
     * Shows parameter configuration GUI for the block
     */
    private void showParameterConfigGUI(Player player, CodeBlock codeBlock, Location blockLocation) {
        // Создаем инвентарь (сундук на 27 слотов - 3 ряда)
        Inventory configInventory = Bukkit.createInventory(null, 27, "§8Настройка: " + codeBlock.getAction());

        // Load current DataValue parameters into GUI as ItemStacks
        loadParametersFromGui(configInventory, codeBlock);
        
        // Загружаем сохраненные предметы в GUI
        if (codeBlock.getConfigItems() != null) {
            for (Map.Entry<Integer, ItemStack> entry : codeBlock.getConfigItems().entrySet()) {
                if (entry.getKey() < configInventory.getSize()) {
                    configInventory.setItem(entry.getKey(), entry.getValue());
                }
            }
        }
        
        // --- НОВАЯ ЛОГИКА: ДОБАВЛЯЕМ PLACEHOLDER ПРЕДМЕТЫ ---
        addPlaceholderItems(configInventory, codeBlock);
        
        // Запоминаем, что игрок настраивает этот блок
        configuringBlocks.put(player.getUniqueId(), blockLocation);
        player.openInventory(configInventory);
        
        player.sendMessage("§e§l!§r §eНастройте блок, поместив предметы в инвентарь.");
        player.sendMessage("§7Предметы будут использоваться как параметры для действия '" + codeBlock.getAction() + "'");
        player.sendMessage("§7§oПодсказка: Используйте 27 слотов для сложных конфигураций");
    }
    
    /**
     * Добавляет placeholder предметы в инвентарь на основе конфигурации
     */
    private void addPlaceholderItems(Inventory inventory, CodeBlock codeBlock) {
        String actionName = codeBlock.getAction();
        
        // Get BlockConfigService instead of BlockConfiguration
        var blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        // Implement proper placeholder system integration
        // This involves adding placeholder items to the GUI based on the action configuration
        try {
            // Get the action configuration from the action configurations section
            var actionConfigurations = blockConfigService.getActionConfigurations();
            if (actionConfigurations != null) {
                var actionConfig = actionConfigurations.getConfigurationSection(actionName);
                if (actionConfig != null) {
                    // Get slots configuration
                    var slots = actionConfig.getConfigurationSection("slots");
                    if (slots != null) {
                        // Add placeholder items for each parameter slot
                        for (String slotKey : slots.getKeys(false)) {
                            var slotConfig = slots.getConfigurationSection(slotKey);
                            if (slotConfig != null) {
                                String paramName = slotConfig.getString("slot_name");
                                if (paramName != null) {
                                    // Get the slot index for this parameter
                                    Integer slotIndex = blockConfigService.getSlotResolver(actionName).apply(paramName);
                                    if (slotIndex != null && slotIndex >= 0) {
                                        // Create a placeholder item for this parameter
                                        // Create a parameter config map for the placeholder
                                        Map<String, Object> paramConfig = new HashMap<>();
                                        paramConfig.put("name", paramName);
                                        paramConfig.put("type", slotConfig.getString("type", "string"));
                                        paramConfig.put("description", slotConfig.getString("description", ""));
                                        
                                        ItemStack placeholderItem = createPlaceholderItem(paramConfig);
                                        if (placeholderItem != null) {
                                            // Add the placeholder item to the inventory
                                            inventory.setItem(slotIndex, placeholderItem);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error adding placeholder items for action " + actionName + ": " + e.getMessage());
        }
    }
    
    /**
     * Creates a placeholder item for a parameter
     */
    private ItemStack createPlaceholderItem(Map<String, Object> paramConfig) {
        try {
            // Extract parameter information from the config map
            String name = (String) paramConfig.getOrDefault("name", "Unknown Parameter");
            String type = (String) paramConfig.getOrDefault("type", "string");
            String description = (String) paramConfig.getOrDefault("description", "");
            
            // Create an item based on the parameter type
            Material material = getMaterialForParameterType(type);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                // Set display name
                meta.setDisplayName("§7" + name);
                
                // Set lore with parameter information
                List<String> lore = new ArrayList<>();
                lore.add("§8Parameter: " + name);
                lore.add("§8Type: " + type);
                
                if (description != null && !description.isEmpty()) {
                    lore.add("§7" + description);
                }
                
                lore.add("");
                lore.add("§ePlace an item here to set this parameter");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            return item;
        } catch (Exception e) {
            // Return null if we can't create the placeholder item
            return null;
        }
    }
    
    /**
     * Gets the appropriate material for a parameter type
     */
    private Material getMaterialForParameterType(String type) {
        if (type == null) return Material.PAPER;
        
        switch (type.toLowerCase()) {
            case "string":
                return Material.WRITABLE_BOOK;
            case "number":
                return Material.SUNFLOWER;
            case "boolean":
                return Material.LEVER;
            case "player":
                return Material.PLAYER_HEAD;
            case "location":
                return Material.COMPASS;
            case "item":
                return Material.CHEST;
            default:
                return Material.PAPER;
        }
    }
    
    /**
     * Сохраняет содержимое GUI, когда игрок его закрывает
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        
        UUID playerId = player.getUniqueId();

        // Проверяем, настраивал ли этот игрок блок
        if (!configuringBlocks.containsKey(playerId)) {
            return;
        }
        
        // Проверяем, что это наше GUI
        if (!event.getView().getTitle().startsWith("§8Настройка:")) {
            // Если игрок открыл что-то другое, не закрыв наше GUI, то просто ждем закрытия нашего.
            return;
        }

        Location blockLocation = configuringBlocks.get(playerId);
        CodeBlock codeBlock = plugin.getServiceRegistry().getBlockPlacementHandler().getBlockCodeBlocks().get(blockLocation);
        
        if (codeBlock != null) {
            // Convert ItemStacks to DataValue parameters
            convertItemStacksToParameters(event.getInventory(), codeBlock);
            
            // Очищаем старую конфигурацию
            codeBlock.clearConfigItems();
            
            // Считываем все предметы из инвентаря и сохраняем в CodeBlock
            int savedItems = 0;
            for (int i = 0; i < event.getInventory().getSize(); i++) {
                ItemStack item = event.getInventory().getItem(i);
                if (item != null && !item.getType().isAir()) {
                    codeBlock.setConfigItem(i, item);
                    savedItems++;
                }
            }
            
            // NEW: Automatically create container for PlayerEntryAction with autoGiveItem=true
            if ("PlayerEntryAction".equals(codeBlock.getAction())) {
                // Check if autoGiveItem parameter is set to true
                DataValue autoGiveItem = codeBlock.getParameter("autoGiveItem");
                if (autoGiveItem != null && autoGiveItem.asBoolean()) {
                    // Create container automatically
                    createAutomaticContainer(blockLocation, codeBlock);
                }
            }
            
            if (savedItems > 0) {
                player.sendMessage("§a✓ Конфигурация блока сохранена! (" + savedItems + " предметов)");
            } else {
                player.sendMessage("§eℹ Конфигурация блока очищена.");
            }
            
            // Сохраняем весь мир, чтобы изменения не потерялись после перезагрузки
            var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null) {
                plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
            }
        }
        
        // Убираем игрока из списка настраивающих
        configuringBlocks.remove(playerId);
    }
    
    /**
     * Automatically creates a container above the code block for item configuration
     * This implements the workflow where players can configure items in a chest above the action block
     */
    private void createAutomaticContainer(Location blockLocation, CodeBlock codeBlock) {
        try {
            // Get the container manager from the service registry
            var containerManager = plugin.getServiceRegistry().getBlockContainerManager();
            if (containerManager == null) {
                plugin.getLogger().warning("Container manager is not available for automatic container creation");
                return;
            }
            
            // Create a chest container above the block
            containerManager.createContainer(blockLocation, com.megacreative.coding.containers.ContainerType.CHEST, "PlayerEntryAction");
            
            // Notify the player
            var player = Bukkit.getPlayer(configuringBlocks.entrySet().stream()
                .filter(entry -> entry.getValue().equals(blockLocation))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null));
                
            if (player != null) {
                player.sendMessage("§a✓ Автоматически создан контейнер над блоком для настройки предметов!");
                player.sendMessage("§eКликните по сундуку над блоком, чтобы настроить предметы для выдачи.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error creating automatic container: " + e.getMessage());
        }
    }
    
    /**
     * Проверяет, настраивает ли игрок блок в данный момент
     * @param player Игрок для проверки
     * @return true, если игрок настраивает блок
     */
    public boolean isConfiguringBlock(Player player) {
        return configuringBlocks.containsKey(player.getUniqueId());
    }
    
    /**
     * Отменяет настройку блока для игрока
     * @param player Игрок, для которого нужно отменить настройку
     */
    public void cancelConfiguration(Player player) {
        configuringBlocks.remove(player.getUniqueId());
    }
    
    // ================================
    // DATAVALUE INTEGRATION METHODS
    // ================================
    
    /**
     * Loads current DataValue parameters from CodeBlock into GUI as ItemStacks
     */
    private void loadParametersFromGui(Inventory inventory, CodeBlock codeBlock) {
        Map<String, DataValue> parameters = codeBlock.getParameters();
        if (parameters.isEmpty()) {
            plugin.getLogger().info("No parameters to load for action: " + codeBlock.getAction());
            return;
        }
        
        plugin.getLogger().info("Loading " + parameters.size() + " parameters for action: " + codeBlock.getAction());
        
        // Convert each parameter to ItemStack representation using BlockConfigService mapping
        var blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        for (Map.Entry<String, DataValue> entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            DataValue paramValue = entry.getValue();
            
            if (paramValue == null) continue;
            
            // Find the appropriate slot for this parameter from config
            Integer slot = blockConfigService != null ? blockConfigService.findSlotForParameter(codeBlock.getAction(), paramName) : null;
            if (slot != null && slot >= 0 && slot < inventory.getSize()) {
                ItemStack paramItem = convertDataValueToItemStack(paramName, paramValue);
                if (paramItem != null) {
                    inventory.setItem(slot, paramItem);
                    plugin.getLogger().info("Loaded parameter '" + paramName + "' into slot " + slot + " with value: " + paramValue.asString());
                } else {
                    plugin.getLogger().warning("Failed to convert DataValue to ItemStack for parameter: " + paramName);
                }
            } else {
                plugin.getLogger().warning("No slot found for parameter: " + paramName + " (action: " + codeBlock.getAction() + ")");
                
                // Try to put in first available slot as fallback
                for (int i = 0; i < inventory.getSize(); i++) {
                    if (inventory.getItem(i) == null || inventory.getItem(i).getType().isAir()) {
                        ItemStack paramItem = convertDataValueToItemStack(paramName, paramValue);
                        if (paramItem != null) {
                            inventory.setItem(i, paramItem);
                            plugin.getLogger().info("Loaded parameter '" + paramName + "' into fallback slot " + i);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Converts ItemStacks from GUI inventory to DataValue parameters in CodeBlock
     */
    private void convertItemStacksToParameters(Inventory inventory, CodeBlock codeBlock) {
        Map<String, DataValue> newParameters = new HashMap<>();
        int processedItems = 0;
        
        // Process each slot in the inventory
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            
            // Skip placeholder items
            if (isPlaceholderItem(item)) continue;
            
            // Try to determine parameter name for this slot via BlockConfigService
            var blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
            String paramName = blockConfigService != null ? blockConfigService.getParameterNameForSlot(codeBlock.getAction(), slot) : null;
            if (paramName == null) {
                // Fallback: use generic slot-based parameter name
                paramName = "slot_" + slot;
            }
            
            // Convert ItemStack to DataValue
            DataValue paramValue = convertItemStackToDataValue(item);
            if (paramValue != null) {
                newParameters.put(paramName, paramValue);
                processedItems++;
            }
        }
        
        // Update CodeBlock parameters
        for (Map.Entry<String, DataValue> entry : newParameters.entrySet()) {
            codeBlock.setParameter(entry.getKey(), entry.getValue());
        }
        
        Player player = null; // We'll get from context later
        if (processedItems > 0) {
            plugin.getLogger().info("Converted " + processedItems + " ItemStacks to DataValue parameters for block " + codeBlock.getAction());
        }
    }
    
    /**
     * Converts a DataValue to an ItemStack for GUI display
     */
    private ItemStack convertDataValueToItemStack(String paramName, DataValue value) {
        Material material;
        String displayName;
        List<String> lore = new ArrayList<>();
        
        // Determine material based on DataValue type
        switch (value.getType()) {
            case TEXT -> {
                material = Material.PAPER;
                displayName = "§f" + paramName + ": §a" + value.asString();
                lore.add("§7Type: §fText");
                lore.add("§7Value: §f" + value.asString());
            }
            case NUMBER -> {
                material = Material.GOLD_NUGGET;
                displayName = "§f" + paramName + ": §6" + value.asNumber();
                lore.add("§7Type: §6Number");
                lore.add("§7Value: §6" + value.asNumber());
            }
            case BOOLEAN -> {
                material = value.asBoolean() ? Material.LIME_DYE : Material.RED_DYE;
                displayName = "§f" + paramName + ": " + (value.asBoolean() ? "§aTrue" : "§cFalse");
                lore.add("§7Type: §eBoolean");
                lore.add("§7Value: " + (value.asBoolean() ? "§aTrue" : "§cFalse"));
            }
            case LIST -> {
                material = Material.CHEST;
                ListValue listValue = (ListValue) value;
                displayName = "§f" + paramName + ": §dList[" + listValue.size() + "]"; 
                lore.add("§7Type: §dList");
                lore.add("§7Size: §d" + listValue.size());
                // Show first few items
                List<DataValue> items = listValue.getValues();
                for (int i = 0; i < Math.min(3, items.size()); i++) {
                    lore.add("§7[" + i + "] §f" + items.get(i).asString());
                }
                if (items.size() > 3) {
                    lore.add("§7... and " + (items.size() - 3) + " more");
                }
            }
            default -> {
                material = Material.BARRIER;
                displayName = "§f" + paramName + ": §7" + value.getType().getDisplayName();
                lore.add("§7Type: §7" + value.getType().getDisplayName());
                lore.add("§7Value: §7" + value.asString());
            }
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            lore.add("");
            lore.add("§8Parameter: " + paramName);
            lore.add("§8Click to modify value");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Converts an ItemStack to a DataValue
     */
    private DataValue convertItemStackToDataValue(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return new AnyValue(null);
        }
        
        ItemMeta meta = item.getItemMeta();
        String displayName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "";
        
        // Clean display name from color codes for processing
        String cleanName = displayName.replaceAll("§[0-9a-fk-or]", "");
        
        // 1. Try to extract value from existing parameter items (our converted items)
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                if (line.startsWith("§8Parameter: ")) {
                    // This is a parameter item we created - extract the value
                    return extractValueFromParameterItem(item, lore);
                }
            }
        }
        
        // 2. Try to detect type from material
        switch (item.getType()) {
            case PAPER:
                // Extract text from display name or use item name
                if (!cleanName.isEmpty()) {
                    return new TextValue(cleanName);
                } else {
                    return new TextValue("Текст");
                }
            
            case GOLD_NUGGET:
            case GOLD_INGOT:
                // Try to parse number from name or use amount
                if (!cleanName.isEmpty()) {
                    try {
                        String numberStr = cleanName.replaceAll("[^0-9.-]", "");
                        if (!numberStr.isEmpty()) {
                            return new NumberValue(Double.parseDouble(numberStr));
                        }
                    } catch (NumberFormatException ignored) {}
                }
                return new NumberValue(item.getAmount());
            
            case LIME_DYE:
                return new BooleanValue(true);
            case RED_DYE:
                return new BooleanValue(false);
            
            case CHEST:
            case BARREL:
                // Consider these as lists or containers
                return new ListValue();
            
            default:
                // For other items, create text value from name or material
                if (!cleanName.isEmpty()) {
                    return new TextValue(cleanName);
                } else {
                    // Use material name as text value
                    return new TextValue(item.getType().name().toLowerCase().replace("_", " "));
                }
        }
    }
    
    /**
     * Extracts value from a parameter item we created
     */
    private DataValue extractValueFromParameterItem(ItemStack item, List<String> lore) {
        // Look for "Value: " line in lore
        for (String line : lore) {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", "");
            if (cleanLine.startsWith("Value: ")) {
                String valueStr = cleanLine.substring(7); // Remove "Value: "
                
                // Check type from the previous line
                int index = lore.indexOf(line);
                if (index > 0) {
                    String typeLine = lore.get(index - 1).replaceAll("§[0-9a-fk-or]", "");
                    
                    if (typeLine.contains("Number")) {
                        try {
                            return new NumberValue(Double.parseDouble(valueStr));
                        } catch (NumberFormatException e) {
                            return new TextValue(valueStr);
                        }
                    } else if (typeLine.contains("Boolean")) {
                        return new BooleanValue("True".equalsIgnoreCase(valueStr));
                    } else if (typeLine.contains("List")) {
                        // Implement list parsing
                        // Try to parse the list from the value string
                        return parseListFromString(valueStr);
                    }
                }
                
                // Default to text
                return new TextValue(valueStr);
            }
        }
        
        // Fallback
        return new TextValue(item.getType().name().toLowerCase());
    }
    
    /**
     * Parses a string representation of a list into a ListValue
     * Supports formats like "[item1,item2,item3]" or "item1,item2,item3"
     * 
     * @param listString The string to parse
     * @return A ListValue containing the parsed items
     */
    private ListValue parseListFromString(String listString) {
        if (listString == null || listString.trim().isEmpty()) {
            return new ListValue(new ArrayList<>());
        }
        
        // Remove brackets if present
        String cleanString = listString.trim();
        if (cleanString.startsWith("[") && cleanString.endsWith("]")) {
            cleanString = cleanString.substring(1, cleanString.length() - 1);
        }
        
        // Split by comma and create DataValues
        List<DataValue> values = new ArrayList<>();
        if (!cleanString.isEmpty()) {
            // Handle quoted strings that might contain commas
            String[] items = parseListItems(cleanString);
            
            for (String item : items) {
                String trimmedItem = item.trim();
                // Try to parse as number first
                try {
                    double number = Double.parseDouble(trimmedItem);
                    values.add(DataValue.fromObject(number));
                } catch (NumberFormatException e) {
                    // Treat as string
                    values.add(DataValue.fromObject(trimmedItem));
                }
            }
        }
        
        return new ListValue(values);
    }
    
    /**
     * Parses list items, handling quoted strings that might contain commas
     * 
     * @param input The input string to parse
     * @return Array of parsed items
     */
    private String[] parseListItems(String input) {
        List<String> items = new ArrayList<>();
        StringBuilder currentItem = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = '"';
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (c == '"' || c == '\'') {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == quoteChar) {
                    inQuotes = false;
                } else {
                    currentItem.append(c);
                }
            } else if (c == ',' && !inQuotes) {
                items.add(currentItem.toString());
                currentItem = new StringBuilder();
            } else {
                currentItem.append(c);
            }
        }
        
        // Add the last item
        items.add(currentItem.toString());
        
        return items.toArray(new String[0]);
    }
    
    /**
     * Finds the appropriate slot for a parameter name (reverse mapping)
     */
    private Integer findSlotForParameter(String action, String paramName) {
        var blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        if (blockConfigService == null) return null;
        Integer slot = blockConfigService.findSlotForParameter(action, paramName);
        if (slot != null) return slot;
        // Fallback: param_# naming
        if (paramName != null && paramName.startsWith("param_")) {
            try { return Integer.parseInt(paramName.substring(6)); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
    
    /**
     * Gets parameter name for a specific slot based on action type
     */
    private String getParameterNameForSlot(String action, int slot) {
        var blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        if (blockConfigService == null) return null;
        String name = blockConfigService.getParameterNameForSlot(action, slot);
        if (name != null) return name;
        return "param_" + slot; // fallback
    }
    
    /**
     * Checks if an ItemStack is a placeholder item
     */
    private boolean isPlaceholderItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                if (line.contains("placeholder") || line.contains("Placeholder")) {
                    return true;
                }
            }
        }
        
        return false;
    }
} 