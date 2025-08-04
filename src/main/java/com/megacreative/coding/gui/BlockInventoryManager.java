package com.megacreative.coding.gui;

import com.megacreative.coding.blocks.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер инвентарей для работы с блоками кода.
 */
public class BlockInventoryManager implements Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, BlockInventory> openInventories = new HashMap<>();
    
    public BlockInventoryManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Открывает инвентарь с блоками для игрока.
     */
    public void openBlockInventory(Player player) {
        BlockInventory inventory = new BlockInventory("Выберите блок", 27);
        
        // Добавляем блоки в инвентарь
        inventory.addBlock(createBlockItem(BlockType.EVENT, "Событие"), 10);
        inventory.addBlock(createBlockItem(BlockType.ACTION, "Действие"), 11);
        inventory.addBlock(createBlockItem(BlockType.CONDITION, "Условие"), 12);
        inventory.addBlock(createBlockItem(BlockType.LOOP, "Цикл"), 13);
        inventory.addBlock(createBlockItem(BlockType.FUNCTION, "Функция"), 14);
        inventory.addBlock(createBlockItem(BlockType.VARIABLE, "Переменная"), 15);
        inventory.addBlock(createBlockItem(BlockType.VALUE, "Значение"), 16);
        
        // Открываем инвентарь
        inventory.open(player);
        openInventories.put(player.getUniqueId(), inventory);
    }
    
    /**
     * Создает предмет, представляющий блок кода.
     */
    private ItemStack createBlockItem(BlockType type, String displayName) {
        ItemStack item = new ItemStack(getMaterialForBlockType(type));
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Блок кода: " + displayName);
            // Можно добавить лор и другую информацию
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Возвращает материал для отображения блока в инвентаре.
     */
    private Material getMaterialForBlockType(BlockType type) {
        switch (type) {
            case EVENT: return Material.NOTE_BLOCK;
            case ACTION: return Material.COMMAND_BLOCK;
            case CONDITION: return Material.REPEATING_COMMAND_BLOCK;
            case LOOP: return Material.CHAIN_COMMAND_BLOCK;
            case FUNCTION: return Material.BOOK;
            case VARIABLE: return Material.NAME_TAG;
            case VALUE: return Material.PAPER;
            default: return Material.STONE;
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        
        // Проверяем, что кликнули в открытый нами инвентарь
        if (!openInventories.containsKey(playerId) || 
            !event.getInventory().equals(openInventories.get(playerId).getInventory())) {
            return;
        }
        
        event.setCancelled(true);
        
        // Проверяем, что кликнули по предмету
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }
        
        // Обрабатываем выбор блока
        // TODO: Добавить логику выбора блока
        
        // Закрываем инвентарь
        player.closeInventory();
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Удаляем инвентарь из списка открытых
        openInventories.remove(playerId);
    }
    
    /**
     * Класс, представляющий инвентарь с блоками кода.
     */
    private static class BlockInventory implements InventoryHolder {
        private final Inventory inventory;
        private final Map<Integer, BlockType> blockSlots = new HashMap<>();
        
        public BlockInventory(String title, int size) {
            this.inventory = Bukkit.createInventory(this, size, title);
        }
        
        /**
         * Добавляет блок в указанный слот инвентаря.
         */
        public void addBlock(ItemStack item, int slot) {
            if (item == null || slot < 0 || slot >= inventory.getSize()) {
                return;
            }
            
            // Сохраняем тип блока для слота
            BlockType type = getBlockTypeFromItem(item);
            if (type != null) {
                blockSlots.put(slot, type);
            }
            
            // Добавляем предмет в инвентарь
            inventory.setItem(slot, item);
        }
        
        /**
         * Открывает инвентарь для игрока.
         */
        public void open(Player player) {
            player.openInventory(inventory);
        }
        
        /**
         * Получает тип блока в указанном слоте.
         */
        public BlockType getBlockTypeAtSlot(int slot) {
            return blockSlots.getOrDefault(slot, null);
        }
        
        @Override
        public Inventory getInventory() {
            return inventory;
        }
        
        /**
         * Получает тип блока из предмета.
         */
        private BlockType getBlockTypeFromItem(ItemStack item) {
            if (item == null || !item.hasItemMeta()) {
                return null;
            }
            
            String displayName = item.getItemMeta().getDisplayName();
            
            if (displayName.contains("Событие")) return BlockType.EVENT;
            if (displayName.contains("Действие")) return BlockType.ACTION;
            if (displayName.contains("Условие")) return BlockType.CONDITION;
            if (displayName.contains("Цикл")) return BlockType.LOOP;
            if (displayName.contains("Функция")) return BlockType.FUNCTION;
            if (displayName.contains("Переменная")) return BlockType.VARIABLE;
            if (displayName.contains("Значение")) return BlockType.VALUE;
            
            return null;
        }
    }
}
