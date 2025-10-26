package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for selecting categories of actions/events/conditions for a code block
 * 
 * @author Андрій Будильников
 */
public class CategorySelectionGUI {
    private static final int INVENTORY_SIZE = 27;
    private static final String INVENTORY_TITLE = ChatColor.DARK_PURPLE + "Выбор категории";
    
    private final MegaCreative plugin;
    private final Player player;
    private final CodeBlock codeBlock;
    private final Inventory inventory;
    
    public CategorySelectionGUI(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        this.plugin = plugin;
        this.player = player;
        this.codeBlock = codeBlock;
        this.inventory = Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE);
        setupGUI();
    }
    
    /**
     * Sets up the GUI with category items
     */
    private void setupGUI() {
        inventory.clear();
        
        // Add category items based on the block type
        String blockMaterial = codeBlock.getMaterialName();
        BlockConfigService configService = plugin.getServiceRegistry().getBlockConfigService();
        
        if (configService != null) {
            BlockConfigService.BlockConfig blockConfig = configService.getBlockConfigByMaterial(
                Material.getMaterial(blockMaterial));
            
            if (blockConfig != null) {
                String blockType = blockConfig.getType();
                
                // Add categories based on block type
                switch (blockType) {
                    case "EVENT":
                        addEventCategories();
                        break;
                    case "ACTION":
                        addActionCategories();
                        break;
                    case "CONDITION":
                        addConditionCategories();
                        break;
                    case "CONTROL":
                        addControlCategories();
                        break;
                    case "FUNCTION":
                        addFunctionCategories();
                        break;
                    default:
                        addDefaultCategories();
                        break;
                }
            } else {
                addDefaultCategories();
            }
        } else {
            addDefaultCategories();
        }
        
        // Add close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Закрыть");
        closeItem.setItemMeta(closeMeta);
        inventory.setItem(26, closeItem);
    }
    
    /**
     * Adds event categories to the GUI
     */
    private void addEventCategories() {
        // Player events
        addItem(0, Material.PLAYER_HEAD, ChatColor.AQUA + "Игрок", 
                "События, связанные с игроками");
        
        // World events
        addItem(1, Material.GRASS_BLOCK, ChatColor.GREEN + "Мир", 
                "События, связанные с миром");
        
        // Server events
        addItem(2, Material.COMMAND_BLOCK, ChatColor.GOLD + "Сервер", 
                "События, связанные с сервером");
        
        // Block events
        addItem(3, Material.COBBLESTONE, ChatColor.GRAY + "Блоки", 
                "События, связанные с блоками");
        
        // Entity events
        addItem(4, Material.ZOMBIE_HEAD, ChatColor.DARK_RED + "Существа", 
                "События, связанные с существами");
    }
    
    /**
     * Adds action categories to the GUI
     */
    private void addActionCategories() {
        // Player actions
        addItem(0, Material.PLAYER_HEAD, ChatColor.AQUA + "Игрок", 
                "Действия с игроками");
        
        // World actions
        addItem(1, Material.GRASS_BLOCK, ChatColor.GREEN + "Мир", 
                "Действия с миром");
        
        // Item actions
        addItem(2, Material.CHEST, ChatColor.YELLOW + "Предметы", 
                "Действия с предметами");
        
        // Chat actions
        addItem(3, Material.WRITABLE_BOOK, ChatColor.LIGHT_PURPLE + "Чат", 
                "Действия с чатом");
        
        // Sound actions
        addItem(4, Material.NOTE_BLOCK, ChatColor.BLUE + "Звуки", 
                "Действия со звуками");
        
        // Teleportation actions
        addItem(5, Material.ENDER_PEARL, ChatColor.DARK_PURPLE + "Телепортация", 
                "Действия с телепортацией");
        
        // Time actions
        addItem(6, Material.CLOCK, ChatColor.GOLD + "Время", 
                "Действия со временем");
        
        // Variable actions
        addItem(7, Material.REDSTONE, ChatColor.RED + "Переменные", 
                "Действия с переменными");
        
        // Scoreboard actions
        addItem(8, Material.OAK_SIGN, ChatColor.DARK_GREEN + "Скорборд", 
                "Действия со скорбордами");
        
        // Team actions
        addItem(9, Material.WHITE_BANNER, ChatColor.AQUA + "Команды", 
                "Действия с командами");
        
        // Location actions
        addItem(10, Material.COMPASS, ChatColor.DARK_PURPLE + "Локации", 
                "Действия с локациями");
        
        // Economy actions
        addItem(11, Material.GOLD_INGOT, ChatColor.YELLOW + "Экономика", 
                "Действия с экономикой");
        
        // Integration actions
        addItem(12, Material.PAPER, ChatColor.BLUE + "Интеграция", 
                "Действия интеграции");
    }
    
    /**
     * Adds condition categories to the GUI
     */
    private void addConditionCategories() {
        // Player conditions
        addItem(0, Material.PLAYER_HEAD, ChatColor.AQUA + "Игрок", 
                "Условия, связанные с игроками");
        
        // World conditions
        addItem(1, Material.GRASS_BLOCK, ChatColor.GREEN + "Мир", 
                "Условия, связанные с миром");
        
        // Item conditions
        addItem(2, Material.CHEST, ChatColor.YELLOW + "Предметы", 
                "Условия с предметами");
        
        // Time conditions
        addItem(3, Material.CLOCK, ChatColor.GOLD + "Время", 
                "Условия, связанные со временем");
        
        // Variable conditions
        addItem(4, Material.REDSTONE, ChatColor.RED + "Переменные", 
                "Условия с переменными");
        
        // Entity conditions
        addItem(5, Material.ZOMBIE_HEAD, ChatColor.DARK_RED + "Существа", 
                "Условия с существами");
        
        // Permission conditions
        addItem(6, Material.COMMAND_BLOCK, ChatColor.LIGHT_PURPLE + "Права", 
                "Условия прав доступа");
        
        // Inventory conditions
        addItem(7, Material.CHEST, ChatColor.YELLOW + "Инвентарь", 
                "Условия инвентаря");
        
        // Stats conditions
        addItem(8, Material.PLAYER_HEAD, ChatColor.AQUA + "Статистика", 
                "Условия статистики");
        
        // Online conditions
        addItem(9, Material.COMMAND_BLOCK, ChatColor.GOLD + "Онлайн", 
                "Условия онлайн статуса");
        
        // Weather conditions
        addItem(10, Material.WATER_BUCKET, ChatColor.BLUE + "Погода", 
                "Условия погоды");
        
        // Region conditions
        addItem(11, Material.BARRIER, ChatColor.DARK_RED + "Регионы", 
                "Условия регионов");
    }
    
    /**
     * Adds control categories to the GUI
     */
    private void addControlCategories() {
        // Loop controls
        addItem(0, Material.REPEATER, ChatColor.GREEN + "Циклы", 
                "Блоки управления циклами");
        
        // Conditional controls
        addItem(1, Material.COMPARATOR, ChatColor.YELLOW + "Условия", 
                "Блоки управления условиями");
        
        // Function controls
        addItem(2, Material.ENDER_CHEST, ChatColor.DARK_PURPLE + "Функции", 
                "Блоки управления функциями");
        
        // Timing controls
        addItem(3, Material.CLOCK, ChatColor.GOLD + "Тайминг", 
                "Блоки управления временем");
    }
    
    /**
     * Adds function categories to the GUI
     */
    private void addFunctionCategories() {
        // Custom functions
        addItem(0, Material.BOOK, ChatColor.GREEN + "Пользовательские", 
                "Пользовательские функции");
        
        // System functions
        addItem(1, Material.ENCHANTED_BOOK, ChatColor.AQUA + "Системные", 
                "Системные функции");
        
        // Data structure functions
        addItem(2, Material.MAP, ChatColor.YELLOW + "Структуры данных", 
                "Функции структур данных");
    }
    
    /**
     * Adds default categories to the GUI
     */
    private void addDefaultCategories() {
        // General category
        addItem(0, Material.NETHER_STAR, ChatColor.WHITE + "Общие", 
                "Общие действия/события/условия");
    }
    
    /**
     * Adds an item to the GUI
     */
    private void addItem(int slot, Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + description);
        lore.add("");
        lore.add(ChatColor.YELLOW + "Нажмите, чтобы выбрать");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }
    
    /**
     * Opens the GUI for the player
     */
    public void open() {
        player.openInventory(inventory);
    }
    
    /**
     * Gets the inventory
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Gets the player
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Gets the code block
     */
    public CodeBlock getCodeBlock() {
        return codeBlock;
    }
}