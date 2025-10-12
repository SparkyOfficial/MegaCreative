package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.Constants;
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
 * 🎆 Enhanced Connection Debug GUI
 * 
 * Implements Reference System-style: universal blocks with GUI configuration
 * with categories, beautiful selection, and smart signs on blocks with information.
 *
 * 🎆 Улучшенный графический интерфейс отладки связей
 * 
 * Реализует стиль reference system: универсальные блоки с настройкой через GUI
 * с категориями, красивым выбором и умными табличками на блоках с информацией.
 *
 * 🎆 Erweiterte Verbindungs-Debug-GUI
 * 
 * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Konfiguration
 * mit Kategorien, schöner Auswahl und intelligenten Schildern an Blöcken mit Informationen.
 */
public class ConnectionDebugGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location rootBlockLocation;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockPlacementHandler blockPlacementHandler;
    
    private final Map<Integer, Location> slotToBlockLocation = new HashMap<>();
    
    /**
     * Initializes connection debug GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param rootBlockLocation Location of root block to debug
     */
    public ConnectionDebugGUI(MegaCreative plugin, Player player, Location rootBlockLocation) {
        this.plugin = plugin;
        this.player = player;
        this.rootBlockLocation = rootBlockLocation;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
        this.blockPlacementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
        
        
        this.inventory = Bukkit.createInventory(null, 54, "§8Отладка связей: " + getLocationString(rootBlockLocation));
        
        setupInventory();
    }
    
    /**
     * Gets location string for display
     */
    private String getLocationString(Location location) {
        if (location == null) return "Неизвестно";
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }
    
    /**
     * Sets up the GUI inventory with enhanced design
     */
    private void setupInventory() {
        inventory.clear();
        
        
        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(" ");
        borderItem.setItemMeta(borderMeta);
        
        
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderItem);
            }
        }
        
        
        CodeBlock rootBlock = blockPlacementHandler.getCodeBlock(rootBlockLocation);
        if (rootBlock == null) {
            showError(Constants.BLOCK_NOT_FOUND);
            return;
        }
        
        
        ItemStack rootItem = createBlockInfoItem(rootBlock, rootBlockLocation, true);
        inventory.setItem(22, rootItem); 
        slotToBlockLocation.put(22, rootBlockLocation);
        
        
        mapConnectedBlocks(rootBlock, rootBlockLocation);
        
        
        addControlItems();
    }
    
    /**
     * Maps connected blocks
     */
    private void mapConnectedBlocks(CodeBlock rootBlock, Location rootLocation) {
        Set<Location> visitedBlocks = new HashSet<>();
        Queue<BlockConnection> toProcess = new LinkedList<>();
        
        
        toProcess.offer(new BlockConnection(rootBlock, rootLocation, 22, 0));
        visitedBlocks.add(rootLocation);
        
        while (!toProcess.isEmpty()) {
            BlockConnection current = toProcess.poll();
            
            
            if (current.depth > 3) continue;
            
            
            if (current.block.getNextBlock() != null) {
                Location nextLocation = findBlockLocation(current.block.getNextBlock());
                if (nextLocation != null && !visitedBlocks.contains(nextLocation)) {
                    int nextSlot = getNextAvailableSlot(current.slot, Constants.NEXT_SLOT);
                    if (nextSlot != -1) {
                        ItemStack nextItem = createBlockInfoItem(current.block.getNextBlock(), nextLocation, false);
                        inventory.setItem(nextSlot, nextItem);
                        slotToBlockLocation.put(nextSlot, nextLocation);
                        
                        
                        addConnectionArrow(current.slot, nextSlot, Constants.NEXT_BLOCK_ARROW);
                        
                        toProcess.offer(new BlockConnection(current.block.getNextBlock(), nextLocation, nextSlot, current.depth + 1));
                        visitedBlocks.add(nextLocation);
                    }
                }
            }
            
            
            if (!current.block.getChildren().isEmpty()) {
                for (int i = 0; i < current.block.getChildren().size() && i < 2; i++) {
                    CodeBlock child = current.block.getChildren().get(i);
                    Location childLocation = findBlockLocation(child);
                    if (childLocation != null && !visitedBlocks.contains(childLocation)) {
                        int childSlot = getNextAvailableSlot(current.slot, Constants.CHILD_SLOT_PREFIX + i);
                        if (childSlot != -1) {
                            ItemStack childItem = createBlockInfoItem(child, childLocation, false);
                            inventory.setItem(childSlot, childItem);
                            slotToBlockLocation.put(childSlot, childLocation);
                            
                            
                            addConnectionArrow(current.slot, childSlot, Constants.CHILD_BLOCK_ARROW);
                            
                            toProcess.offer(new BlockConnection(child, childLocation, childSlot, current.depth + 1));
                            visitedBlocks.add(childLocation);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Checks if a block has a parent block
     */
    private boolean hasParentBlock(CodeBlock block) {
        if (block == null) {
            return false;
        }
        
        
        
        if (blockPlacementHandler != null) {
            Map<Location, CodeBlock> allBlocks = blockPlacementHandler.getBlockCodeBlocks();
            if (allBlocks != null) {
                for (CodeBlock otherBlock : allBlocks.values()) {
                    if (otherBlock != block) {
                        
                        if (otherBlock.getChildren().contains(block)) {
                            return true;
                        }
                        
                        if (otherBlock.getNextBlock() == block) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Creates block info item with enhanced design
     */
    private ItemStack createBlockInfoItem(CodeBlock block, Location location, boolean isRoot) {
        Material blockMaterial = location.getBlock().getType();
        ItemStack item = new ItemStack(blockMaterial);
        ItemMeta meta = item.getItemMeta();
        
        String prefix = isRoot ? "§e★ " : "§7• ";
        meta.setDisplayName(prefix + "§f" + (block.getAction() != null ? block.getAction() : Constants.BLOCK_UNASSIGNED));
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Координаты: §f" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        lore.add("§7Материал: §f" + blockMaterial.name());
        
        if (block.getAction() != null) {
            lore.add("§7Действие: §a" + block.getAction());
        } else {
            lore.add("§7Действие: §c<не установлено>");
        }
        
        
        if (!block.getParameters().isEmpty()) {
            lore.add("");
            lore.add("§eПараметры:");
            int count = 0;
            for (Map.Entry<String, DataValue> param : block.getParameters().entrySet()) {
                if (count >= 3) {
                    lore.add("§7  ... и ещё " + (block.getParameters().size() - 3));
                    break;
                }
                lore.add("§7  • " + param.getKey() + ": §f" + param.getValue());
                count++;
            }
        }
        
        
        lore.add("");
        if (block.getNextBlock() != null) {
            lore.add("§a→ Имеет следующий блок");
        }
        if (!block.getChildren().isEmpty()) {
            lore.add("§b↓ Дочерних блоков: " + block.getChildren().size());
        }
        
        
        if (hasParentBlock(block)) {
            lore.add("§c↑ Имеет родительский блок");
        }
        
        if (isRoot) {
            lore.add("");
            lore.add("§e⭐ Корневой блок");
        }
        
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        lore.add("§fс настройкой через GUI");
        lore.add("");
        lore.add("§eКлик для телепортации");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Adds connection arrow
     */
    private void addConnectionArrow(int fromSlot, int toSlot, String connectionType) {
        
        int arrowSlot = calculateArrowSlot(fromSlot, toSlot);
        if (arrowSlot != -1 && inventory.getItem(arrowSlot) != null) {
            ItemStack currentItem = inventory.getItem(arrowSlot);
            if (currentItem.getType() == Material.GRAY_STAINED_GLASS_PANE || 
                currentItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                
                ItemStack arrow = new ItemStack(Material.ARROW);
                ItemMeta arrowMeta = arrow.getItemMeta();
                arrowMeta.setDisplayName(connectionType);
                List<String> arrowLore = new ArrayList<>();
                arrowLore.add("§7Связь между блоками");
                arrowLore.add("");
                arrowLore.add("§f✨ Reference system-стиль");
                arrowMeta.setLore(arrowLore);
                arrow.setItemMeta(arrowMeta);
                inventory.setItem(arrowSlot, arrow);
            }
        }
    }
    
    /**
     * Calculates arrow slot
     */
    private int calculateArrowSlot(int fromSlot, int toSlot) {
        
        if (Math.abs(fromSlot - toSlot) == 1) {
            return -1; 
        }
        
        
        return (fromSlot + toSlot) / 2;
    }
    
    /**
     * Gets next available slot
     */
    private int getNextAvailableSlot(int centerSlot, String direction) {
        
        int[] candidates;
        
        switch (direction) {
            case "next":
                candidates = new int[]{centerSlot + 1, centerSlot + 9, centerSlot + 2};
                break;
            case "child0":
                candidates = new int[]{centerSlot - 9, centerSlot - 8, centerSlot - 10};
                break;
            case "child1":
                candidates = new int[]{centerSlot + 9, centerSlot + 8, centerSlot + 10};
                break;
            default:
                candidates = new int[]{centerSlot + 1, centerSlot - 1, centerSlot + 9, centerSlot - 9};
        }
        
        for (int candidate : candidates) {
            if (candidate >= 0 && candidate < 54) {
                ItemStack item = inventory.getItem(candidate);
                if (item == null || 
                    item.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                    item.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                    return candidate;
                }
            }
        }
        
        return -1; 
    }
    
    /**
     * Finds block location
     */
    private Location findBlockLocation(CodeBlock block) {
        
        
        if (block == null || blockPlacementHandler == null) {
            return null;
        }
        
        
        Map<Location, CodeBlock> allCodeBlocks = blockPlacementHandler.getBlockCodeBlocks();
        if (allCodeBlocks != null) {
            for (Map.Entry<Location, CodeBlock> entry : allCodeBlocks.entrySet()) {
                if (entry.getValue() == block || 
                    (entry.getValue().getId() != null && entry.getValue().getId().equals(block.getId()))) {
                    return entry.getKey();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Adds control items with enhanced design
     */
    private void addControlItems() {
        
        ItemStack refresh = new ItemStack(Material.LIME_STAINED_GLASS);
        ItemMeta refreshMeta = refresh.getItemMeta();
        refreshMeta.setDisplayName("§a§l🔄 Обновить");
        List<String> refreshLore = new ArrayList<>();
        refreshLore.add("§7Перестроить карту связей");
        refreshLore.add("");
        refreshLore.add("§f✨ Reference system-стиль: универсальные блоки");
        refreshLore.add("§fс настройкой через GUI");
        refreshMeta.setLore(refreshLore);
        refresh.setItemMeta(refreshMeta);
        inventory.setItem(45, refresh);
        
        
        ItemStack close = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§c§l❌ Закрыть");
        List<String> closeLore = new ArrayList<>();
        closeLore.add("§7Закрыть отладчик связей");
        closeLore.add("");
        closeLore.add("§f✨ Reference system-стиль: универсальные блоки");
        closeMeta.setLore(closeLore);
        close.setItemMeta(closeMeta);
        inventory.setItem(53, close);
        
        
        ItemStack help = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = help.getItemMeta();
        helpMeta.setDisplayName("§e§l❓ Помощь");
        List<String> helpLore = new ArrayList<>();
        helpLore.add("§7Как читать карту связей:");
        helpLore.add("§a→ §7Зелёная стрелка = следующий блок");
        helpLore.add("§b↓ §7Синяя стрелка = дочерний блок");
        helpLore.add("§e⭐ §7Жёлтая звезда = корневой блок");
        helpLore.add("");
        helpLore.add("§eКликните по блоку для телепортации");
        helpLore.add("");
        helpLore.add("§f✨ Reference system-стиль: универсальные блоки");
        helpLore.add("§fс настройкой через GUI");
        helpMeta.setLore(helpLore);
        help.setItemMeta(helpMeta);
        inventory.setItem(49, help);
        
        
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c⬅ Назад");
        List<String> backLore = new ArrayList<>();
        backLore.add("§7Вернуться к предыдущему меню");
        backLore.add("");
        backLore.add("§f✨ Reference system-стиль: универсальные блоки");
        backLore.add("§fс настройкой через GUI");
        backMeta.setLore(backLore);
        backButton.setItemMeta(backMeta);
        inventory.setItem(46, backButton);
    }
    
    /**
     * Shows error message
     */
    private void showError(String message) {
        ItemStack errorItem = new ItemStack(Material.BARRIER);
        ItemMeta errorMeta = errorItem.getItemMeta();
        errorMeta.setDisplayName("§c❌ Ошибка");
        List<String> errorLore = new ArrayList<>();
        errorLore.add("§7" + message);
        errorLore.add("");
        errorLore.add("§f✨ Reference system-стиль: универсальные блоки");
        errorLore.add("§fс настройкой через GUI");
        errorMeta.setLore(errorLore);
        errorItem.setItemMeta(errorMeta);
        inventory.setItem(22, errorItem);
    }
    
    /**
     * Handles inventory click events
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != inventory) return;
        
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        
        
        if (slotToBlockLocation.containsKey(slot)) {
            Location blockLocation = slotToBlockLocation.get(slot);
            if (blockLocation != null) {
                player.teleport(blockLocation.add(0.5, 1.0, 0.5));
                player.sendMessage("§aТелепортирован к блоку на координатах: " + 
                    blockLocation.getBlockX() + ", " + blockLocation.getBlockY() + ", " + blockLocation.getBlockZ());
            }
        } else if (slot == 45) { 
            setupInventory();
            player.sendMessage("§aКарта связей обновлена!");
        } else if (slot == 53) { 
            player.closeInventory();
        } else if (slot == 49) { 
            showHelp();
        } else if (slot == 46) { 
            
            player.closeInventory();
        }
    }
    
    /**
     * Shows help information
     */
    private void showHelp() {
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§e❓ Помощь по отладке связей");
        List<String> helpLore = new ArrayList<>();
        helpLore.add("§7Как читать карту связей:");
        helpLore.add("");
        helpLore.add("§a→ §7Зелёная стрелка = следующий блок");
        helpLore.add("§b↓ §7Синяя стрелка = дочерний блок");
        helpLore.add("§e⭐ §7Жёлтая звезда = корневой блок");
        helpLore.add("§7• §7Серая точка = обычный блок");
        helpLore.add("");
        helpLore.add("§7Символы:");
        helpLore.add("§e★ §7- Корневой блок");
        helpLore.add("§a→ §7- Следующий блок");
        helpLore.add("§b↓ §7- Дочерний блок");
        helpLore.add("§c↑ §7- Родительский блок");
        helpLore.add("");
        helpLore.add("§eКликните по блоку для телепортации");
        helpLore.add("§eКликните \"Обновить\" для перестройки карты");
        helpLore.add("");
        helpLore.add("§f✨ Reference system-стиль: универсальные блоки");
        helpLore.add("§fс настройкой через GUI");
        helpMeta.setLore(helpLore);
        helpItem.setItemMeta(helpMeta);
        
        Inventory helpInventory = Bukkit.createInventory(null, 27, "§8Помощь по отладке связей");
        helpInventory.setItem(13, helpItem);
        
        
        ItemStack close = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§c§l❌ Закрыть");
        List<String> closeLore = new ArrayList<>();
        closeLore.add("§7Закрыть помощь");
        closeLore.add("");
        closeLore.add("§f✨ Reference system-стиль: универсальные блоки");
        closeLore.add("§fс настройкой через GUI");
        closeMeta.setLore(closeLore);
        close.setItemMeta(closeMeta);
        helpInventory.setItem(26, close);
        
        player.openInventory(helpInventory);
    }
    
    /**
     * Handles inventory close events
     */
    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        
        slotToBlockLocation.clear();
    }
    
    /**
     * Gets the GUI title for debugging
     */
    @Override
    public String getGUITitle() {
        return "Connection Debug GUI";
    }
    
    /**
     * Gets the inventory
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Inner class for block connection tracking
     */
    private static class BlockConnection {
        final CodeBlock block;
        final Location location;
        final int slot;
        final int depth;
        
        BlockConnection(CodeBlock block, Location location, int slot, int depth) {
            this.block = block;
            this.location = location;
            this.slot = slot;
            this.depth = depth;
        }
    }
}