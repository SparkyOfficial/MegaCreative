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
    private final com.megacreative.coding.AutoConnectionManager autoConnectionManager;
    
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
        this.guiManager = plugin.getGuiManager();
        this.blockPlacementHandler = plugin.getBlockPlacementHandler();
        this.autoConnectionManager = plugin.getServiceRegistry().getAutoConnectionManager();
        
        // Create inventory with appropriate size (54 slots for double chest GUI)
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
        
        // Get root block
        CodeBlock rootBlock = blockPlacementHandler.getCodeBlock(rootBlockLocation);
        if (rootBlock == null) {
            showError(Constants.BLOCK_NOT_FOUND);
            return;
        }
        
        // Add root block info with enhanced visual design
        ItemStack rootItem = createBlockInfoItem(rootBlock, rootBlockLocation, true);
        inventory.setItem(22, rootItem); // Center position
        slotToBlockLocation.put(22, rootBlockLocation);
        
        // Map connected blocks
        mapConnectedBlocks(rootBlock, rootBlockLocation);
        
        // Add control items with enhanced design
        addControlItems();
    }
    
    /**
     * Maps connected blocks
     */
    private void mapConnectedBlocks(CodeBlock rootBlock, Location rootLocation) {
        Set<Location> visitedBlocks = new HashSet<>();
        Queue<BlockConnection> toProcess = new LinkedList<>();
        
        // Start with root block
        toProcess.offer(new BlockConnection(rootBlock, rootLocation, 22, 0));
        visitedBlocks.add(rootLocation);
        
        while (!toProcess.isEmpty()) {
            BlockConnection current = toProcess.poll();
            
            // Skip if too deep to avoid infinite loops
            if (current.depth > 3) continue;
            
            // Process next block
            if (current.block.getNextBlock() != null) {
                Location nextLocation = findBlockLocation(current.block.getNextBlock());
                if (nextLocation != null && !visitedBlocks.contains(nextLocation)) {
                    int nextSlot = getNextAvailableSlot(current.slot, Constants.NEXT_SLOT);
                    if (nextSlot != -1) {
                        ItemStack nextItem = createBlockInfoItem(current.block.getNextBlock(), nextLocation, false);
                        inventory.setItem(nextSlot, nextItem);
                        slotToBlockLocation.put(nextSlot, nextLocation);
                        
                        // Add connection arrow
                        addConnectionArrow(current.slot, nextSlot, Constants.NEXT_BLOCK_ARROW);
                        
                        toProcess.offer(new BlockConnection(current.block.getNextBlock(), nextLocation, nextSlot, current.depth + 1));
                        visitedBlocks.add(nextLocation);
                    }
                }
            }
            
            // Process child blocks
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
                            
                            // Add connection arrow
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
        if (block == null || autoConnectionManager == null) {
            return false;
        }
        
        // Search through all blocks to see if any block has this block as a child
        try {
            // Use reflection to access the private locationToBlock field
            java.lang.reflect.Field locationToBlockField = autoConnectionManager.getClass().getDeclaredField("locationToBlock");
            locationToBlockField.setAccessible(true);
            Map<Location, CodeBlock> locationToBlock = (Map<Location, CodeBlock>) locationToBlockField.get(autoConnectionManager);
            
            // Search for any block that has this block as a child
            for (CodeBlock parentBlock : locationToBlock.values()) {
                if (parentBlock != null && parentBlock.getChildren().contains(block)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // If reflection fails, return false
            return false;
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
        
        // Add parameter info
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
        
        // Add connection info
        lore.add("");
        if (block.getNextBlock() != null) {
            lore.add("§a→ Имеет следующий блок");
        }
        if (!block.getChildren().isEmpty()) {
            lore.add("§b↓ Дочерних блоков: " + block.getChildren().size());
        }
        
        // Check for parent relationship
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
        // Calculate position between slots for arrow
        int arrowSlot = calculateArrowSlot(fromSlot, toSlot);
        if (arrowSlot != -1 && inventory.getItem(arrowSlot) != null) {
            ItemStack currentItem = inventory.getItem(arrowSlot);
            if (currentItem.getType() == Material.GRAY_STAINED_GLASS_PANE || 
                currentItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
                // Replace glass pane with connection arrow
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
        // Simple calculation for arrow position
        if (Math.abs(fromSlot - toSlot) == 1) {
            return -1; // Adjacent slots, no space for arrow
        }
        
        // Collapse the if statement - both branches return the same value
        return (fromSlot + toSlot) / 2;
    }
    
    /**
     * Gets next available slot
     */
    private int getNextAvailableSlot(int centerSlot, String direction) {
        // Get available slot around center based on direction
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
        
        return -1; // No available slot
    }
    
    /**
     * Finds block location
     */
    private Location findBlockLocation(CodeBlock block) {
        // Implementation to find block location by searching through the locationToBlock map
        if (block == null || autoConnectionManager == null) {
            return null;
        }
        
        // Get the location to block mapping from AutoConnectionManager
        try {
            // Use reflection to access the private locationToBlock field
            java.lang.reflect.Field locationToBlockField = autoConnectionManager.getClass().getDeclaredField("locationToBlock");
            locationToBlockField.setAccessible(true);
            Map<Location, CodeBlock> locationToBlock = (Map<Location, CodeBlock>) locationToBlockField.get(autoConnectionManager);
            
            // Search for the block location
            for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
                if (entry.getValue() == block) {
                    return entry.getKey();
                }
            }
        } catch (Exception e) {
            // Fallback approach - try to get location from block directly
            if (block.getLocation() != null) {
                return block.getLocation();
            }
        }
        
        return null;
    }
    
    /**
     * Adds control items with enhanced design
     */
    private void addControlItems() {
        // Refresh button with enhanced visual design
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
        
        // Close button with enhanced visual design
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
        
        // Help button with enhanced visual design
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
        
        // Add back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c⬅ Назад");
        List<String> backLore = new ArrayList<>();
        backLore.add("§7Вернуться к предыдущему меню");
        backLore.add("");
        backLore.add("§f✨ Reference system-стиль: универсальные блоки");
        backMeta.setLore(backLore);
        backButton.setItemMeta(backMeta);
        inventory.setItem(46, backButton);
    }
    
    /**
     * Shows error with enhanced design
     */
    private void showError(String message) {
        ItemStack error = new ItemStack(Material.BARRIER);
        ItemMeta errorMeta = error.getItemMeta();
        errorMeta.setDisplayName("§c❌ Ошибка");
        List<String> errorLore = new ArrayList<>();
        errorLore.add("§7" + message);
        errorLore.add("");
        errorLore.add("§f✨ Reference system-стиль: универсальные блоки");
        errorLore.add("§fс настройкой через GUI");
        errorMeta.setLore(errorLore);
        error.setItemMeta(errorMeta);
        inventory.setItem(22, error);
    }
    
    /**
     * Opens the GUI for the player
     */
    public void open() {
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        // Audio feedback when opening GUI
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ENDER_CHEST_OPEN, 0.7f, 1.2f);
        
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
        return "Connection Debug GUI";
    }
    
    @Override
    /**
     * Handles inventory click events
     * @param event Inventory click event
     */
    public void onInventoryClick(InventoryClickEvent event) {
        if (!player.equals(event.getWhoClicked())) return;
        if (!inventory.equals(event.getInventory())) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        int slot = event.getSlot();
        
        // Handle back button
        if (slot == 46) {
            player.closeInventory();
            return;
        }
        
        // Handle control buttons
        if (displayName.contains("Обновить")) {
            setupInventory();
            player.sendMessage("§a🔄 Карта связей обновлена!");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
            return;
        }
        
        if (displayName.contains("Закрыть")) {
            player.closeInventory();
            return;
        }
        
        if (displayName.contains("Помощь")) {
            player.sendMessage("§e💡 Используйте карту для понимания связей между блоками кода.");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.2f);
            return;
        }
        
        // Handle block teleportation
        Location blockLocation = slotToBlockLocation.get(slot);
        if (blockLocation != null) {
            Location teleportLocation = blockLocation.clone().add(0.5, 1, 0.5);
            player.teleport(teleportLocation);
            player.sendMessage("§a✈ Телепортированы к блоку!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.0f);
            player.closeInventory();
        }
    }
    
    @Override
    /**
     * Handles inventory close events
     * @param event Inventory close event
     */
    public void onInventoryClose(InventoryCloseEvent event) {
        // Cleanup
    }
    
    @Override
    /**
     * Performs resource cleanup when interface is closed
     */
    public void onCleanup() {
        slotToBlockLocation.clear();
    }
    
    /**
     * Helper class for tracking block connections during mapping
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