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
 * 🎆 УЛУЧШЕННЫЙ Графический интерфейс для выбора действий, событий и условий для блоков кода.
 * 
 * Реализует стиль reference system: универсальные блоки с настройкой через GUI
 * с категориями, красивым выбором и умными табличками на блоках с информацией.
 *
 * 🎆 ENHANCED GUI for selecting actions, events, and conditions for code blocks.
 * 
 * Implements reference system-style: universal blocks with GUI configuration
 * with categories, beautiful selection, and smart signs on blocks with information.
 *
 * 🎆 ERWEITERT GUI zur Auswahl von Aktionen, Ereignissen und Bedingungen für Codeblöcke.
 * 
 * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Konfiguration
 * mit Kategorien, schöner Auswahl und intelligenten Schildern an Blöcken mit Informationen.
 */
public class ActionSelectionGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    
    private static final Map<String, String> CATEGORY_NAMES = new LinkedHashMap<>();
    private static final Map<String, Material> CATEGORY_MATERIALS = new HashMap<>();
    
    static {
        
        CATEGORY_NAMES.put("EVENT", "🌟 События");
        CATEGORY_NAMES.put("ACTION", "⚡ Действия");
        CATEGORY_NAMES.put("CONDITION", "❓ Условия");
        CATEGORY_NAMES.put("CONTROL", "⚙️ Управление");
        CATEGORY_NAMES.put("FUNCTION", "📚 Функции");
        CATEGORY_NAMES.put("VARIABLE", "📊 Переменные");
        
        
        CATEGORY_MATERIALS.put("EVENT", Material.NETHER_STAR);
        CATEGORY_MATERIALS.put("ACTION", Material.REDSTONE);
        CATEGORY_MATERIALS.put("CONDITION", Material.COMPARATOR);
        CATEGORY_MATERIALS.put("CONTROL", Material.REPEATER);
        CATEGORY_MATERIALS.put("FUNCTION", Material.WRITABLE_BOOK);
        CATEGORY_MATERIALS.put("VARIABLE", Material.NAME_TAG);
    }
    
    /**
     * Инициализирует графический интерфейс выбора действий
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param blockLocation Расположение блока для настройки
     * @param blockMaterial Материал блока для настройки
     */
    public ActionSelectionGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
        
        
        // Condition plugin != null is always true
        // Removed redundant null check
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        
        this.inventory = Bukkit.createInventory(null, 54, "§8Выбор действия: " + getBlockDisplayName());
        
        setupInventory();
    }
    
    /**
     * Получает отображаемое имя блока
     */
    private String getBlockDisplayName() {
        
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(blockMaterial);
        if (config != null) {
            return config.getDisplayName();
        }
        
        
        for (BlockConfigService.BlockConfig blockConfig : blockConfigService.getAllBlockConfigs()) {
            if (blockConfig.getMaterial() == blockMaterial) {
                return blockConfig.getDisplayName();
            }
        }
        
        return blockMaterial.name();
    }
    
    /**
     * Настраивает инвентарь графического интерфейса
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
        
        
        ItemStack infoItem = new ItemStack(blockMaterial);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e§l" + getBlockDisplayName());
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Выберите категорию действий");
        infoLore.add("");
        infoLore.add("§aКликните по категории чтобы");
        infoLore.add("§aпросмотреть доступные действия");
        infoLore.add("");
        infoLore.add("§f✨ Reference system-стиль: универсальные блоки");
        infoLore.add("§fс настройкой через GUI");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        
        int slot = 10;
        for (Map.Entry<String, String> category : CATEGORY_NAMES.entrySet()) {
            String categoryKey = category.getKey();
            String categoryName = category.getValue();
            
            ItemStack categoryItem = new ItemStack(CATEGORY_MATERIALS.getOrDefault(categoryKey, Material.PAPER));
            ItemMeta categoryMeta = categoryItem.getItemMeta();
            categoryMeta.setDisplayName("§6" + categoryName);
            
            List<String> categoryLore = new ArrayList<>();
            categoryLore.add("§7Категория: " + categoryKey);
            categoryLore.add("");
            categoryLore.add("§e⚡ Кликните чтобы выбрать");
            categoryMeta.setLore(categoryLore);
            
            categoryItem.setItemMeta(categoryMeta);
            inventory.setItem(slot, categoryItem);
            
            slot += 2; 
            if (slot >= 44) break; 
        }
    }
    
    /**
     * Открывает графический интерфейс для игрока
     */
    public void open() {
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        
        
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, 
            player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 1);
    }
    
    /**
     * Получает заголовок графического интерфейса
     * @return Заголовок интерфейса
     */
    @Override
    public String getGUITitle() {
        return "Action Selection GUI for " + blockMaterial.name();
    }
    
    /**
     * Обрабатывает события кликов в инвентаре
     * @param event Событие клика в инвентаре
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (!player.equals(event.getWhoClicked())) return;
        if (!inventory.equals(event.getInventory())) return;
        
        event.setCancelled(true); 
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        ItemMeta meta = clicked.getItemMeta();
        String displayName = meta.getDisplayName();
        
        
        for (Map.Entry<String, String> category : CATEGORY_NAMES.entrySet()) {
            String categoryName = category.getValue();
            if (displayName.contains(categoryName)) {
                
                //openCategorySelectionGUI(category.getKey());
                return;
            }
        }
        
        
        if (displayName.contains("Назад")) {
            
            setupInventory();
            player.openInventory(inventory);
            return;
        }
        
        
        List<String> lore = meta.getLore();
        if (lore != null) {
            
            String actionId = null;
            for (String line : lore) {
                if (line.startsWith("§8ID: ")) {
                    actionId = line.substring(5).trim(); 
                    break;
                }
            }
            
            if (actionId != null && !actionId.isEmpty()) {
                //selectAction(actionId);
            } else {
                player.sendMessage("§eℹ Это заголовок категории. Кликните по действию ниже.");
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.8f);
            }
        }
    }
    
    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Clean up if needed
    }
    
}