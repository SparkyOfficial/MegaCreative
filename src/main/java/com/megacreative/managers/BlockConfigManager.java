package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockConfiguration;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodingActionGUI;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private final Map<UUID, Location> configuringBlocks = new HashMap<>();

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
        CodeBlock codeBlock = plugin.getBlockPlacementHandler().getBlockCodeBlocks().get(blockLocation);
        if (codeBlock == null) {
            player.sendMessage("§cОшибка: блок кода не найден.");
            return;
        }

        // Устанавливаем ссылку на плагин для доступа к конфигурации
        codeBlock.setPlugin(plugin);

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
        List<String> availableActions = blockConfigService.getAvailableActions(codeBlock.getMaterial());
        
        if (availableActions.isEmpty()) {
            player.sendMessage("§cОшибка: нет доступных действий для данного типа блока.");
            return;
        }
        
        player.sendMessage("§eВыберите действие для блока...");
        
        // Create and open CodingActionGUI
        CodingActionGUI actionGUI = new CodingActionGUI(
            player, 
            codeBlock.getMaterial(), 
            blockLocation, 
            availableActions,
            (selectedAction) -> {
                // Callback when action is selected
                codeBlock.setAction(selectedAction);
                player.sendMessage("§aВыбрано действие: §e" + selectedAction);
                
                // Now open parameter configuration
                showParameterConfigGUI(player, codeBlock, blockLocation);
            },
            plugin.getServiceRegistry().getGuiManager()
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
        
        // Добавляем placeholder предметы для именованных слотов
        var slotConfig = plugin.getBlockConfiguration().getActionSlotConfig(actionName);
        if (slotConfig != null) {
            for (Map.Entry<Integer, BlockConfiguration.SlotConfig> entry : slotConfig.getSlots().entrySet()) {
                int slotNumber = entry.getKey();
                BlockConfiguration.SlotConfig slotConfigData = entry.getValue();
                
                // Проверяем, есть ли уже предмет в этом слоте
                if (inventory.getItem(slotNumber) == null || inventory.getItem(slotNumber).getType().isAir()) {
                    ItemStack placeholder = plugin.getBlockConfiguration().createPlaceholderItem(actionName, slotNumber);
                    inventory.setItem(slotNumber, placeholder);
                }
            }
        }
        
        // Добавляем placeholder предметы для групп
        var groupConfig = plugin.getBlockConfiguration().getActionGroupConfig(actionName);
        if (groupConfig != null) {
            for (Map.Entry<String, BlockConfiguration.GroupConfig> entry : groupConfig.getGroups().entrySet()) {
                String groupName = entry.getKey();
                BlockConfiguration.GroupConfig groupConfigData = entry.getValue();
                
                // Добавляем placeholder в первый слот группы, если он пустой
                List<Integer> groupSlots = groupConfigData.getSlots();
                if (!groupSlots.isEmpty()) {
                    int firstSlot = groupSlots.get(0);
                    if (inventory.getItem(firstSlot) == null || inventory.getItem(firstSlot).getType().isAir()) {
                        ItemStack placeholder = plugin.getBlockConfiguration().createGroupPlaceholderItem(actionName, groupName);
                        inventory.setItem(firstSlot, placeholder);
                    }
                }
            }
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
        CodeBlock codeBlock = plugin.getBlockPlacementHandler().getBlockCodeBlocks().get(blockLocation);
        
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
            var creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null) {
                plugin.getWorldManager().saveWorld(creativeWorld);
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
            var containerManager = plugin.getServiceRegistry().getContainerManager();
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
        
        // Convert each parameter to ItemStack representation
        for (Map.Entry<String, DataValue> entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            DataValue paramValue = entry.getValue();
            
            if (paramValue == null) continue;
            
            // Find the appropriate slot for this parameter
            Integer slot = findSlotForParameter(codeBlock.getAction(), paramName);
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
            
            // Try to determine parameter name for this slot
            String paramName = getParameterNameForSlot(codeBlock.getAction(), slot);
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
                        return new ListValue(); // TODO: Implement list parsing
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
     * Finds the appropriate slot for a parameter name (reverse mapping)
     */
    private Integer findSlotForParameter(String action, String paramName) {
        // Use reverse mapping based on our getParameterNameForSlot logic
        
        // Action-specific parameter mapping
        switch (action) {
            case "sendMessage":
                if ("message".equals(paramName)) return 0;
                break;
            case "teleport":
                if ("coords".equals(paramName)) return 0;
                break;
            case "giveItem":
                if ("item".equals(paramName)) return 0;
                if ("amount".equals(paramName)) return 1;
                break;
            case "playSound":
                if ("sound".equals(paramName)) return 0;
                if ("volume".equals(paramName)) return 1;
                if ("pitch".equals(paramName)) return 2;
                break;
            case "effect":
                if ("effect".equals(paramName)) return 0;
                if ("duration".equals(paramName)) return 1;
                if ("amplifier".equals(paramName)) return 2;
                break;
            case "setVar":
            case "addVar":
            case "subVar":
            case "mulVar":
            case "divVar":
                if ("var".equals(paramName)) return 0;
                if ("value".equals(paramName)) return 1;
                break;
            case "spawnMob":
                if ("mob".equals(paramName)) return 0;
                if ("amount".equals(paramName)) return 1;
                break;
            case "wait":
                if ("ticks".equals(paramName)) return 0;
                break;
            case "randomNumber":
                if ("min".equals(paramName)) return 0;
                if ("max".equals(paramName)) return 1;
                if ("var".equals(paramName)) return 2;
                break;
            case "setTime":
                if ("time".equals(paramName)) return 0;
                break;
            case "setWeather":
                if ("weather".equals(paramName)) return 0;
                break;
            case "command":
                if ("command".equals(paramName)) return 0;
                break;
            case "broadcast":
                if ("message".equals(paramName)) return 0;
                break;
            case "healPlayer":
                if ("amount".equals(paramName)) return 0;
                break;
            case "explosion":
                if ("power".equals(paramName)) return 0;
                if ("breakBlocks".equals(paramName)) return 1;
                break;
            case "setBlock":
                if ("material".equals(paramName)) return 0;
                if ("coords".equals(paramName)) return 1;
                break;
            // Variable conditions (unified handling)
            case "compareVariable":
                if ("var1".equals(paramName)) return 0;
                if ("operator".equals(paramName)) return 1;
                if ("var2".equals(paramName)) return 2;
                break;
            case "ifVarEquals":
            case "ifVarGreater":
            case "ifVarLess":
                if ("variable".equals(paramName)) return 0; // Legacy parameter name
                if ("value".equals(paramName)) return 1;
                break;
            case "hasItem":
                if ("item".equals(paramName)) return 0;
                break;
            case "isNearBlock":
                if ("block".equals(paramName)) return 0;
                if ("radius".equals(paramName)) return 1;
                break;
            case "mobNear":
                if ("mob".equals(paramName)) return 0;
                if ("radius".equals(paramName)) return 1;
                break;
        }
        
        // Generic fallback mapping
        switch (paramName) {
            case "message", "text" -> { return 0; }
            case "amount", "value", "number" -> { return 1; }
            case "target", "player" -> { return 2; }
            case "item", "material" -> { return 3; }
            case "location", "world", "coords" -> { return 4; }
        }
        
        // Try to extract slot number from param_X format
        if (paramName.startsWith("param_")) {
            try {
                return Integer.parseInt(paramName.substring(6));
            } catch (NumberFormatException ignored) {}
        }
        
        // No mapping found
        return null;
    }
    
    /**
     * Gets parameter name for a specific slot based on action type
     */
    private String getParameterNameForSlot(String action, int slot) {
        // Check named slots configuration from coding_blocks.yml
        var blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        // Action-specific parameter mapping based on coding_blocks.yml
        switch (action) {
            case "sendMessage":
                return slot == 0 ? "message" : "param_" + slot;
            case "teleport":
                return slot == 0 ? "coords" : "param_" + slot;
            case "giveItem":
                return switch (slot) {
                    case 0 -> "item";
                    case 1 -> "amount";
                    default -> "param_" + slot;
                };
            case "playSound":
                return switch (slot) {
                    case 0 -> "sound";
                    case 1 -> "volume";
                    case 2 -> "pitch";
                    default -> "param_" + slot;
                };
            case "effect":
                return switch (slot) {
                    case 0 -> "effect";
                    case 1 -> "duration";
                    case 2 -> "amplifier";
                    default -> "param_" + slot;
                };
            case "setVar":
            case "addVar":
            case "subVar":
            case "mulVar":
            case "divVar":
                return switch (slot) {
                    case 0 -> "var";
                    case 1 -> "value";
                    default -> "param_" + slot;
                };
            case "spawnMob":
                return switch (slot) {
                    case 0 -> "mob";
                    case 1 -> "amount";
                    default -> "param_" + slot;
                };
            case "wait":
                return slot == 0 ? "ticks" : "param_" + slot;
            case "randomNumber":
                return switch (slot) {
                    case 0 -> "min";
                    case 1 -> "max";
                    case 2 -> "var";
                    default -> "param_" + slot;
                };
            case "setTime":
                return slot == 0 ? "time" : "param_" + slot;
            case "setWeather":
                return slot == 0 ? "weather" : "param_" + slot;
            case "command":
                return slot == 0 ? "command" : "param_" + slot;
            case "broadcast":
                return slot == 0 ? "message" : "param_" + slot;
            case "healPlayer":
                return slot == 0 ? "amount" : "param_" + slot;
            case "explosion":
                return switch (slot) {
                    case 0 -> "power";
                    case 1 -> "breakBlocks";
                    default -> "param_" + slot;
                };
            case "setBlock":
                return switch (slot) {
                    case 0 -> "material";
                    case 1 -> "coords";
                    default -> "param_" + slot;
                };
            // Variable conditions (unified handling)
            case "compareVariable":
                return switch (slot) {
                    case 0 -> "var1";
                    case 1 -> "operator";
                    case 2 -> "var2";
                    default -> "param_" + slot;
                };
            case "ifVarEquals":
            case "ifVarGreater":
            case "ifVarLess":
                return switch (slot) {
                    case 0 -> "variable"; // Legacy parameter name for backward compatibility
                    case 1 -> "value";
                    default -> "param_" + slot;
                };
            case "hasItem":
                return slot == 0 ? "item" : "param_" + slot;
            case "isNearBlock":
                return switch (slot) {
                    case 0 -> "block";
                    case 1 -> "radius";
                    default -> "param_" + slot;
                };
            case "mobNear":
                return switch (slot) {
                    case 0 -> "mob";
                    case 1 -> "radius";
                    default -> "param_" + slot;
                };
            
            // Generic fallback
            default:
                return switch (slot) {
                    case 0 -> "message";
                    case 1 -> "amount";
                    case 2 -> "target";
                    case 3 -> "item";
                    case 4 -> "location";
                    default -> "param_" + slot;
                };
        }
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