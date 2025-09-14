package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.values.DataValue;
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
 * 🔗 ВИЗУАЛЬНАЯ ОТЛАДКА СВЯЗЕЙ
 * Помогает пользователям визуализировать и понимать связи блоков в их скриптах
 * Особенности:
 * - Визуализация карты связей блоков
 * - Предварительный просмотр потока выполнения
 * - Валидация связей
 * - Быстрая навигация к связанным блокам
 *
 * 🔗 VISUAL CONNECTION DEBUGGING GUI
 * Helps users visualize and understand block connections in their scripts
 * Features:
 * - Block connection map visualization
 * - Execution flow preview
 * - Connection validation
 * - Quick navigation to connected blocks
 *
 * 🔗 VISUELLE VERBINDUNGS-DEBUGGING-GUI
 * Hilft Benutzern dabei, Blockverbindungen in ihren Skripten zu visualisieren und zu verstehen
 * Funktionen:
 * - Visualisierung der Blockverbindungs-Karte
 * - Vorschau des Ausführungsflusses
 * - Verbindungsvalidierung
 * - Schnelle Navigation zu verbundenen Blöcken
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
     * Инициализирует графический интерфейс отладки связей
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param rootBlockLocation Расположение корневого блока для отладки
     *
     * Initializes connection debug GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param rootBlockLocation Location of root block to debug
     *
     * Initialisiert die Verbindungs-Debug-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     * @param rootBlockLocation Position des zu debuggenden Wurzelblocks
     */
    public ConnectionDebugGUI(MegaCreative plugin, Player player, Location rootBlockLocation) {
        this.plugin = plugin;
        this.player = player;
        this.rootBlockLocation = rootBlockLocation;
        this.guiManager = plugin.getGuiManager();
        this.blockPlacementHandler = plugin.getBlockPlacementHandler();
        
        this.inventory = Bukkit.createInventory(null, 54, "§8🔗 Связи блоков");
        
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
        
        // Add background
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        
        // Fill background
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, glassPane);
        }
        
        // Get root block
        CodeBlock rootBlock = blockPlacementHandler.getCodeBlock(rootBlockLocation);
        if (rootBlock == null) {
            showError("Блок не найден");
            return;
        }
        
        // Add root block info
        ItemStack rootItem = createBlockInfoItem(rootBlock, rootBlockLocation, true);
        inventory.setItem(22, rootItem); // Center position
        slotToBlockLocation.put(22, rootBlockLocation);
        
        // Map connected blocks
        mapConnectedBlocks(rootBlock, rootBlockLocation);
        
        // Add control items
        addControlItems();
    }
    
    /**
     * Сопоставляет связанные блоки
     *
     * Maps connected blocks
     *
     * Ordnet verbundene Blöcke zu
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
                    int nextSlot = getNextAvailableSlot(current.slot, "next");
                    if (nextSlot != -1) {
                        ItemStack nextItem = createBlockInfoItem(current.block.getNextBlock(), nextLocation, false);
                        inventory.setItem(nextSlot, nextItem);
                        slotToBlockLocation.put(nextSlot, nextLocation);
                        
                        // Add connection arrow
                        addConnectionArrow(current.slot, nextSlot, "§a→ Следующий");
                        
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
                        int childSlot = getNextAvailableSlot(current.slot, "child" + i);
                        if (childSlot != -1) {
                            ItemStack childItem = createBlockInfoItem(child, childLocation, false);
                            inventory.setItem(childSlot, childItem);
                            slotToBlockLocation.put(childSlot, childLocation);
                            
                            // Add connection arrow
                            addConnectionArrow(current.slot, childSlot, "§b↓ Дочерний");
                            
                            toProcess.offer(new BlockConnection(child, childLocation, childSlot, current.depth + 1));
                            visitedBlocks.add(childLocation);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Создает элемент информации о блоке
     *
     * Creates block info item
     *
     * Erstellt Blockinformationsgegenstand
     */
    private ItemStack createBlockInfoItem(CodeBlock block, Location location, boolean isRoot) {
        Material blockMaterial = location.getBlock().getType();
        ItemStack item = new ItemStack(blockMaterial);
        ItemMeta meta = item.getItemMeta();
        
        String prefix = isRoot ? "§e★ " : "§7• ";
        meta.setDisplayName(prefix + "§f" + (block.getAction() != null ? block.getAction() : "Неназначено"));
        
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
        // Note: Parent relationship tracking would need to be implemented separately
        // if (block.getParent() != null) {
        //     lore.add("§c↑ Имеет родительский блок");
        // }
        
        if (isRoot) {
            lore.add("");
            lore.add("§e⭐ Корневой блок");
        }
        
        lore.add("");
        lore.add("§eКлик для телепортации");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Добавляет стрелку связи
     *
     * Adds connection arrow
     *
     * Fügt Verbindungspfeil hinzu
     */
    private void addConnectionArrow(int fromSlot, int toSlot, String connectionType) {
        // Calculate position between slots for arrow
        int arrowSlot = calculateArrowSlot(fromSlot, toSlot);
        if (arrowSlot != -1 && inventory.getItem(arrowSlot) != null) {
            ItemStack currentItem = inventory.getItem(arrowSlot);
            if (currentItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
                // Replace glass pane with connection arrow
                ItemStack arrow = new ItemStack(Material.ARROW);
                ItemMeta arrowMeta = arrow.getItemMeta();
                arrowMeta.setDisplayName(connectionType);
                arrowMeta.setLore(Arrays.asList("§7Связь между блоками"));
                arrow.setItemMeta(arrowMeta);
                inventory.setItem(arrowSlot, arrow);
            }
        }
    }
    
    /**
     * Вычисляет слот для стрелки
     *
     * Calculates arrow slot
     *
     * Berechnet den Pfeil-Slot
     */
    private int calculateArrowSlot(int fromSlot, int toSlot) {
        // Simple calculation for arrow position
        if (Math.abs(fromSlot - toSlot) == 1) {
            return -1; // Adjacent slots, no space for arrow
        }
        
        if (toSlot > fromSlot) {
            return (fromSlot + toSlot) / 2;
        } else {
            return (toSlot + fromSlot) / 2;
        }
    }
    
    /**
     * Получает следующий доступный слот
     *
     * Gets next available slot
     *
     * Ruft den nächsten verfügbaren Slot ab
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
                if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) {
                    return candidate;
                }
            }
        }
        
        return -1; // No available slot
    }
    
    /**
     * Находит расположение блока
     *
     * Finds block location
     *
     * Findet die Blockposition
     */
    private Location findBlockLocation(CodeBlock block) {
        // This would need to be implemented based on how blocks are tracked
        // For now, return null as placeholder
        return null;
    }
    
    /**
     * Добавляет элементы управления
     *
     * Adds control items
     *
     * Fügt Steuerelemente hinzu
     */
    private void addControlItems() {
        // Refresh button
        ItemStack refresh = new ItemStack(Material.LIME_STAINED_GLASS);
        ItemMeta refreshMeta = refresh.getItemMeta();
        refreshMeta.setDisplayName("§a§l🔄 Обновить");
        refreshMeta.setLore(Arrays.asList("§7Перестроить карту связей"));
        refresh.setItemMeta(refreshMeta);
        inventory.setItem(45, refresh);
        
        // Close button
        ItemStack close = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§c§l❌ Закрыть");
        closeMeta.setLore(Arrays.asList("§7Закрыть отладчик связей"));
        close.setItemMeta(closeMeta);
        inventory.setItem(53, close);
        
        // Help button
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
        help.setItemMeta(helpMeta);
        inventory.setItem(49, help);
    }
    
    /**
     * Показывает ошибку
     *
     * Shows error
     *
     * Zeigt Fehler an
     */
    private void showError(String message) {
        ItemStack error = new ItemStack(Material.BARRIER);
        ItemMeta errorMeta = error.getItemMeta();
        errorMeta.setDisplayName("§c❌ Ошибка");
        errorMeta.setLore(Arrays.asList("§7" + message));
        error.setItemMeta(errorMeta);
        inventory.setItem(22, error);
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
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ENDER_CHEST_OPEN, 0.7f, 1.2f);
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
        return "Connection Debug GUI";
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
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        int slot = event.getSlot();
        
        // Handle control buttons
        if (displayName.contains("Обновить")) {
            setupInventory();
            player.sendMessage("§a🔄 Карта связей обновлена!");
            return;
        }
        
        if (displayName.contains("Закрыть")) {
            player.closeInventory();
            return;
        }
        
        if (displayName.contains("Помощь")) {
            player.sendMessage("§e💡 Используйте карту для понимания связей между блоками кода.");
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
        // Cleanup
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
        slotToBlockLocation.clear();
    }
    
    /**
     * Вспомогательный класс для отслеживания связей блоков во время сопоставления
     *
     * Helper class for tracking block connections during mapping
     *
     * Hilfsklasse zum Verfolgen von Blockverbindungen während der Zuordnung
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