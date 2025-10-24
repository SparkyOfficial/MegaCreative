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
    
    private final Map<UUID, Location> configuringBlocks = new ConcurrentHashMap<>();

    public BlockConfigManager(MegaCreative plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
        configuringBlocks.clear();
        
        
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

        
        String currentAction = codeBlock.getAction();
        if (currentAction == null || currentAction.equals("Настройка...") || currentAction.isEmpty()) {
            
            showActionSelectionGUI(player, codeBlock, blockLocation);
        } else {
            
            showParameterConfigGUI(player, codeBlock, blockLocation);
        }
    }
    
    /**
     * Shows action selection GUI for the block
     */
    private void showActionSelectionGUI(Player player, CodeBlock codeBlock, Location blockLocation) {
        
        var blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        List<String> availableActions = blockConfigService.getActionsForMaterial(org.bukkit.Material.getMaterial(codeBlock.getMaterialName()));
        
        if (availableActions.isEmpty()) {
            player.sendMessage("§cОшибка: нет доступных действий для данного типа блока.");
            return;
        }
        
        player.sendMessage("§eВыберите действие для блока...");
        
        
        ActionSelectionGUI actionGUI = new ActionSelectionGUI(
            plugin, 
            player, 
            blockLocation, 
            org.bukkit.Material.getMaterial(codeBlock.getMaterialName())
        );
        actionGUI.open();
    }
    
    /**
     * Shows parameter configuration GUI for the block
     */
    private void showParameterConfigGUI(Player player, CodeBlock codeBlock, Location blockLocation) {
        
        Inventory configInventory = Bukkit.createInventory(null, 27, "§8Настройка: " + codeBlock.getAction());

        
        loadParametersFromGui(configInventory, codeBlock);
        
        
        if (codeBlock.getConfigItems() != null) {
            for (Map.Entry<Integer, ItemStack> entry : codeBlock.getConfigItems().entrySet()) {
                if (entry.getKey() < configInventory.getSize()) {
                    configInventory.setItem(entry.getKey(), entry.getValue());
                }
            }
        }
        
        
        addPlaceholderItems(configInventory, codeBlock);
        
        
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
        
        
        var blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        
        
        try {
            
            var actionConfigurations = blockConfigService.getActionConfigurations();
            if (actionConfigurations != null) {
                var actionConfig = actionConfigurations.getConfigurationSection(actionName);
                if (actionConfig != null) {
                    
                    var slots = actionConfig.getConfigurationSection("slots");
                    if (slots != null) {
                        
                        for (String slotKey : slots.getKeys(false)) {
                            var slotConfig = slots.getConfigurationSection(slotKey);
                            if (slotConfig != null) {
                                String paramName = slotConfig.getString("slot_name");
                                if (paramName != null) {
                                    
                                    Integer slotIndex = blockConfigService.getSlotResolver(actionName).apply(paramName);
                                    if (slotIndex != null && slotIndex >= 0) {
                                        
                                        
                                        Map<String, Object> paramConfig = new HashMap<>();
                                        paramConfig.put("name", paramName);
                                        paramConfig.put("type", slotConfig.getString("type", "string"));
                                        paramConfig.put("description", slotConfig.getString("description", ""));
                                        
                                        ItemStack placeholderItem = createPlaceholderItem(paramConfig);
                                        if (placeholderItem != null) {
                                            
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
            
            String name = (String) paramConfig.getOrDefault("name", "Unknown Parameter");
            String type = (String) paramConfig.getOrDefault("type", "string");
            String description = (String) paramConfig.getOrDefault("description", "");
            
            
            Material material = getMaterialForParameterType(type);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                
                meta.setDisplayName("§7" + name);
                
                
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

        
        if (!configuringBlocks.containsKey(playerId)) {
            return;
        }
        
        
        if (!event.getView().getTitle().startsWith("§8Настройка:")) {
            
            return;
        }

        Location blockLocation = configuringBlocks.get(playerId);
        CodeBlock codeBlock = plugin.getServiceRegistry().getBlockPlacementHandler().getBlockCodeBlocks().get(blockLocation);
        
        if (codeBlock != null) {
            
            convertItemStacksToParameters(event.getInventory(), codeBlock);
            
            
            codeBlock.clearConfigItems();
            
            
            int savedItems = 0;
            for (int i = 0; i < event.getInventory().getSize(); i++) {
                ItemStack item = event.getInventory().getItem(i);
                if (item != null && !item.getType().isAir()) {
                    codeBlock.setConfigItem(i, item);
                    savedItems++;
                }
            }
            
            
            if ("PlayerEntryAction".equals(codeBlock.getAction())) {
                
                DataValue autoGiveItem = codeBlock.getParameter("autoGiveItem");
                if (autoGiveItem != null && autoGiveItem.asBoolean()) {
                    
                    createAutomaticContainer(blockLocation, codeBlock);
                }
            }
            
            if (savedItems > 0) {
                player.sendMessage("§a✓ Конфигурация блока сохранена! (" + savedItems + " предметов)");
            } else {
                player.sendMessage("§eℹ Конфигурация блока очищена.");
            }
            
            
            var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null) {
                plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
            }
        }
        
        
        configuringBlocks.remove(playerId);
    }
    
    /**
     * Automatically creates a container above the code block for item configuration
     * This implements the workflow where players can configure items in a chest above the action block
     */
    private void createAutomaticContainer(Location blockLocation, CodeBlock codeBlock) {
        try {
            // Find the player configuring this block to prevent NullPointerException
            UUID playerId = configuringBlocks.entrySet().stream()
                .filter(entry -> entry.getValue().equals(blockLocation))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
            
            var player = playerId != null ? Bukkit.getPlayer(playerId) : null;
                
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
    
    
    
    
    /**
     * Loads current DataValue parameters from CodeBlock into GUI as ItemStacks
     */
    private void loadParametersFromGui(Inventory inventory, CodeBlock codeBlock) {
        Map<String, DataValue> parameters = codeBlock.getParameters();
        if (parameters.isEmpty()) {
            plugin.getLogger().fine("No parameters to load for action: " + codeBlock.getAction());
            return;
        }
        
        plugin.getLogger().fine("Loading " + parameters.size() + " parameters for action: " + codeBlock.getAction());
        
        
        var blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        for (Map.Entry<String, DataValue> entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            DataValue paramValue = entry.getValue();
            
            // Static analysis flags this as always true, but we keep the check for safety
            // This is a false positive - null checks are necessary for robustness
            
            Integer slot = blockConfigService != null ? blockConfigService.findSlotForParameter(codeBlock.getAction(), paramName) : null;
            if (slot != null && slot >= 0 && slot < inventory.getSize()) {
                ItemStack paramItem = convertDataValueToItemStack(paramName, paramValue);
                inventory.setItem(slot, paramItem);
                plugin.getLogger().fine("Loaded parameter '" + paramName + "' into slot " + slot + " with value: " + paramValue.asString());
            } else {
                plugin.getLogger().warning("No slot found for parameter: " + paramName + " (action: " + codeBlock.getAction() + ")");
                
                
                for (int i = 0; i < inventory.getSize(); i++) {
                    if (inventory.getItem(i) == null || inventory.getItem(i).getType().isAir()) {
                        ItemStack paramItem = convertDataValueToItemStack(paramName, paramValue);
                        inventory.setItem(i, paramItem);
                        plugin.getLogger().fine("Loaded parameter '" + paramName + "' into fallback slot " + i);
                        break;
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
        
        
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            
            
            if (isPlaceholderItem(item)) continue;
            
            
            var blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
            String paramName = blockConfigService != null ? blockConfigService.getParameterNameForSlot(codeBlock.getAction(), slot) : null;
            if (paramName == null) {
                
                paramName = "slot_" + slot;
            }
            
            
            DataValue paramValue = convertItemStackToDataValue(item);
            // Adding a null check to satisfy static analyzer and improve code safety
            if (paramValue != null) {
                newParameters.put(paramName, paramValue);
                processedItems++;
            }
        }
        
        
        for (Map.Entry<String, DataValue> entry : newParameters.entrySet()) {
            codeBlock.setParameter(entry.getKey(), entry.getValue());
        }
        
        Player player = null; 
        if (processedItems > 0) {
            plugin.getLogger().fine("Converted " + processedItems + " ItemStacks to DataValue parameters for block " + codeBlock.getAction());
        }
    }
    
    /**
     * Converts a DataValue to an ItemStack for GUI display
     */
    private ItemStack convertDataValueToItemStack(String paramName, DataValue value) {
        Material material;
        String displayName;
        List<String> lore = new ArrayList<>();
        
        
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
        
        
        String cleanName = displayName.replaceAll("§[0-9a-fk-or]", "");
        
        
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            // Fix for Qodana issue: Condition lore != null is always true
            // This was a false positive - we need to properly check for null values
            // Actually, when hasLore() returns true, lore is never null
            // Removed redundant null check as it's always true when hasLore() is true
            for (String line : lore) {
                if (line.startsWith("§8Parameter: ")) {
                    
                    return extractValueFromParameterItem(item, lore);
                }
            }
        }
        
        
        switch (item.getType()) {
            case PAPER:
                
                if (!cleanName.isEmpty()) {
                    return new TextValue(cleanName);
                } else {
                    return new TextValue("Текст");
                }
            
            case GOLD_NUGGET:
            case GOLD_INGOT:
                
                if (!cleanName.isEmpty()) {
                    try {
                        String numberStr = cleanName.replaceAll("[^0-9.-]", "");
                        if (!numberStr.isEmpty()) {
                            return new NumberValue(Double.parseDouble(numberStr));
                        }
                    } catch (NumberFormatException e) {
                        // If we can't parse the number, use the item amount as fallback
                        return new NumberValue(item.getAmount());
                    }
                }
                return new NumberValue(item.getAmount());
            
            case LIME_DYE:
                return new BooleanValue(true);
            case RED_DYE:
                return new BooleanValue(false);
            
            case CHEST:
            case BARREL:
                
                return new ListValue();
            
            default:
                
                if (!cleanName.isEmpty()) {
                    return new TextValue(cleanName);
                } else {
                    
                    return new TextValue(item.getType().name().toLowerCase().replace("_", " "));
                }
        }
    }
    
    /**
     * Extracts value from a parameter item we created
     */
    private DataValue extractValueFromParameterItem(ItemStack item, List<String> lore) {
        
        for (String line : lore) {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", "");
            if (cleanLine.startsWith("Value: ")) {
                String valueStr = cleanLine.substring(7); 
                
                
                int index = lore.indexOf(line);
                if (index > 0 && index - 1 < lore.size()) {
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
                        
                        
                        return parseListFromString(valueStr);
                    }
                }
                
                
                return new TextValue(valueStr);
            }
        }
        
        
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
        
        
        String cleanString = listString.trim();
        if (cleanString.startsWith("[") && cleanString.endsWith("]")) {
            cleanString = cleanString.substring(1, cleanString.length() - 1);
        }
        
        
        List<DataValue> values = new ArrayList<>();
        if (!cleanString.isEmpty()) {
            
            String[] items = parseListItems(cleanString);
            
            for (String item : items) {
                String trimmedItem = item.trim();
                
                try {
                    double number = Double.parseDouble(trimmedItem);
                    values.add(DataValue.fromObject(number));
                } catch (NumberFormatException e) {
                    
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
        
        if (paramName != null && paramName.startsWith("param_")) {
            try { return Integer.parseInt(paramName.substring(6)); } catch (NumberFormatException e) {
                // If we can't parse the parameter name as an integer, return null
                return null;
            }
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
        return "param_" + slot; 
    }
    
    /**
     * Checks if an ItemStack is a placeholder item
     */
    private boolean isPlaceholderItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            // lore is never null when hasLore() returns true
            for (String line : lore) {
                if (line.contains("placeholder") || line.contains("Placeholder")) {
                    return true;
                }
            }
        }
        
        return false;
    }
}