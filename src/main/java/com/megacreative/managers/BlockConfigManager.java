package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockConfiguration;
import com.megacreative.coding.CodeBlock;
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
        if (parameters.isEmpty()) return;
        
        // Convert each parameter to ItemStack representation
        for (Map.Entry<String, DataValue> entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            DataValue paramValue = entry.getValue();
            
            // Try to find appropriate slot for this parameter
            Integer slot = findSlotForParameter(codeBlock.getAction(), paramName);
            if (slot != null && slot < inventory.getSize()) {
                ItemStack paramItem = convertDataValueToItemStack(paramName, paramValue);
                if (paramItem != null) {
                    inventory.setItem(slot, paramItem);
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
        
        // Try to extract value from item name or lore
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();
            
            // Try to parse number from display name
            if (item.getType() == Material.GOLD_NUGGET) {
                try {
                    String numberStr = displayName.replaceAll("[^0-9.-]", "");
                    if (!numberStr.isEmpty()) {
                        return new NumberValue(Double.parseDouble(numberStr));
                    }
                } catch (NumberFormatException ignored) {}
            }
            
            // Try to parse boolean
            if (item.getType() == Material.LIME_DYE) {
                return new BooleanValue(true);
            } else if (item.getType() == Material.RED_DYE) {
                return new BooleanValue(false);
            }
            
            // For text items (paper), extract the text content
            if (item.getType() == Material.PAPER) {
                String text = displayName.replaceAll("§[0-9a-fk-or]", ""); // Remove color codes
                return new TextValue(text);
            }
            
            // Default: use display name as text
            return new TextValue(displayName);
        }
        
        // Fallback: create value based on item type and amount
        if (item.getAmount() > 1) {
            return new NumberValue(item.getAmount());
        }
        
        // Default: use item type name as text
        return new TextValue(item.getType().name().toLowerCase());
    }
    
    /**
     * Finds the appropriate slot for a parameter name
     */
    private Integer findSlotForParameter(String action, String paramName) {
        // Check named slots configuration
        var slotConfig = plugin.getBlockConfiguration().getActionSlotConfig(action);
        if (slotConfig != null) {
            for (Map.Entry<Integer, BlockConfiguration.SlotConfig> entry : slotConfig.getSlots().entrySet()) {
                if (entry.getValue().getSlotName().equals(paramName)) {
                    return entry.getKey();
                }
            }
        }
        
        // Fallback: use standard parameter name mapping
        return switch (paramName) {
            case "message", "text" -> 0;
            case "amount", "value", "number" -> 1;
            case "target", "player" -> 2;
            case "item", "material" -> 3;
            case "location", "world" -> 4;
            default -> null;
        };
    }
    
    /**
     * Gets parameter name for a specific slot
     */
    private String getParameterNameForSlot(String action, int slot) {
        // Check named slots configuration  
        var slotConfig = plugin.getBlockConfiguration().getActionSlotConfig(action);
        if (slotConfig != null) {
            BlockConfiguration.SlotConfig config = slotConfig.getSlots().get(slot);
            if (config != null) {
                return config.getSlotName();
            }
        }
        
        // Fallback mapping
        return switch (slot) {
            case 0 -> "message";
            case 1 -> "amount";
            case 2 -> "target";
            case 3 -> "item";
            case 4 -> "location";
            default -> "param_" + slot;
        };
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