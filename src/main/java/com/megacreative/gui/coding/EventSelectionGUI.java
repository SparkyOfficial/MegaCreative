package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.events.CustomEvent;
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
 * 🎆 УЛУЧШЕННЫЙ Графический интерфейс для выбора событий для блоков кода.
 * 
 * Реализует стиль reference system: универсальные блоки с настройкой через GUI
 * с категориями, красивым выбором и умными табличками на блоках с информацией.
 *
 * 🎆 ENHANCED GUI for selecting events for code blocks.
 * 
 * Implements reference system-style: universal blocks with GUI configuration
 * with categories, beautiful selection, and smart signs on blocks with information.
 *
 * 🎆 ERWEITERT GUI zur Auswahl von Ereignissen für Codeblöcke.
 * 
 * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Konfiguration
 * mit Kategorien, schöner Auswahl und intelligenten Schildern an Blöcken mit Informationen.
 */
public class EventSelectionGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    private final CustomEventManager eventManager;
    
    
    private static final Map<String, String> CATEGORY_NAMES = new LinkedHashMap<>();
    private static final Map<String, Material> CATEGORY_MATERIALS = new HashMap<>();
    
    static {
        
        CATEGORY_NAMES.put("PLAYER", "👤 Игрок");
        CATEGORY_NAMES.put("WORLD", "🌍 Мир");
        CATEGORY_NAMES.put("BLOCK", "🧱 Блоки");
        CATEGORY_NAMES.put("ENTITY", "🧟 Существа");
        CATEGORY_NAMES.put("SYSTEM", "⚙️ Система");
        CATEGORY_NAMES.put("CHAT", "💬 Чат");
        
        
        CATEGORY_MATERIALS.put("PLAYER", Material.PLAYER_HEAD);
        CATEGORY_MATERIALS.put("WORLD", Material.GRASS_BLOCK);
        CATEGORY_MATERIALS.put("BLOCK", Material.COBBLESTONE);
        CATEGORY_MATERIALS.put("ENTITY", Material.ZOMBIE_SPAWN_EGG);
        CATEGORY_MATERIALS.put("SYSTEM", Material.COMMAND_BLOCK);
        CATEGORY_MATERIALS.put("CHAT", Material.PAPER);
    }
    
    /**
     * Инициализирует графический интерфейс выбора событий
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param blockLocation Расположение блока для настройки
     * @param blockMaterial Материал блока для настройки
     */
    public EventSelectionGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
        
        // Initialize services directly since plugin is never null
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        this.eventManager = plugin.getServiceRegistry().getCustomEventManager();
        
        this.inventory = Bukkit.createInventory(null, 54, "§8Выбор события: " + getBlockDisplayName());
        
        setupInventory();
    }
    
    /**
     * Получает отображаемое имя блока
     */
    private String getBlockDisplayName() {
        
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(blockMaterial);
        return config != null ? config.getDisplayName() : blockMaterial.name();
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
        infoLore.add("§7Выберите категорию событий");
        infoLore.add("");
        infoLore.add("§aКликните по категории чтобы");
        infoLore.add("§апросмотреть доступные события");
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
        return "Event Selection GUI for " + blockMaterial.name();
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
                
                openCategorySelectionGUI(category.getKey());
                return;
            }
        }
        
        
        List<String> lore = meta.getLore();
        if (lore != null) {
            
            String eventId = null;
            for (String line : lore) {
                if (line.startsWith("§8ID: ")) {
                    eventId = line.substring(5).trim(); 
                    break;
                }
            }
            
            if (eventId != null && !eventId.isEmpty()) {
                selectEvent(eventId);
            } else {
                player.sendMessage("§eℹ Это заголовок категории. Кликните по событию ниже.");
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.8f);
            }
        }
    }
    
    /**
     * Открывает графический интерфейс выбора событий в категории
     * @param category Категория для отображения
     */
    private void openCategorySelectionGUI(String category) {
        
        Inventory categoryInventory = Bukkit.createInventory(null, 54, "§8" + CATEGORY_NAMES.getOrDefault(category, category));
        
        
        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(" ");
        borderItem.setItemMeta(borderMeta);
        
        
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                categoryInventory.setItem(i, borderItem);
            }
        }
        
        
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c⬅ Назад");
        List<String> backLore = new ArrayList<>();
        backLore.add("§7Вернуться к выбору категорий");
        backMeta.setLore(backLore);
        backButton.setItemMeta(backMeta);
        categoryInventory.setItem(49, backButton);
        
        
        loadEventsForCategory(categoryInventory, category);
        
        
        player.openInventory(categoryInventory);
    }
    
    /**
     * Загружает события для категории
     * @param inventory Инвентарь для заполнения
     * @param category Категория для загрузки
     */
    private void loadEventsForCategory(Inventory inventory, String category) {
        
        if (eventManager == null) {
            player.sendMessage("§cОшибка: Менеджер событий недоступен!");
            return;
        }
        
        
        Map<String, CustomEvent> events = eventManager.getEvents();
        
        
        List<String> categoryEvents = new ArrayList<>();
        for (Map.Entry<String, CustomEvent> entry : events.entrySet()) {
            String eventId = entry.getKey();
            CustomEvent event = entry.getValue();
            
            String eventCategory = getEventCategory(event);
            if (category.equals(eventCategory)) {
                categoryEvents.add(eventId);
            }
        }
        
        
        int slot = 10;
        for (String eventId : categoryEvents) {
            if (slot >= 44) break; 
            
            CustomEvent event = events.get(eventId);
            if (event != null) {
                ItemStack eventItem = createEventItem(event);
                inventory.setItem(slot, eventItem);
            }
            
            slot++;
            if (slot % 9 == 8) slot += 2; 
        }
    }
    
    /**
     * Создает элемент события
     * @param event Событие
     * @return ItemStack элемент события
     */
    private ItemStack createEventItem(CustomEvent event) {
        
        Material material = getEventMaterial(event);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        
        meta.setDisplayName("§a§l" + getEventDisplayName(event));
        
        
        List<String> lore = new ArrayList<>();
        lore.add("§7" + getEventDescription(event));
        lore.add("");
        lore.add("§8⚙️ Категория: " + getEventCategoryName(event));
        lore.add("");
        lore.add("§e⚡ Кликните чтобы выбрать");
        lore.add("§8ID: " + event.getId());
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Получает отображаемое имя категории для события
     * @param event Событие
     * @return Отображаемое имя категории
     */
    private String getEventCategoryName(CustomEvent event) {
        String category = getEventCategory(event);
        return CATEGORY_NAMES.getOrDefault(category, category);
    }
    
    /**
     * Получает категорию для события
     * @param event Событие
     * @return Категория события
     */
    private String getEventCategory(CustomEvent event) {
        String category = event.getCategory();
        if (category == null || category.isEmpty()) {
            category = "SYSTEM";
        }
        
        switch (category.toLowerCase()) {
            case "player": return "PLAYER";
            case "world": return "WORLD";
            case "block": return "BLOCK";
            case "entity": return "ENTITY";
            case "system": return "SYSTEM";
            case "chat": return "CHAT";
            default: return "SYSTEM";
        }
    }
    
    /**
     * Получает материал для события
     * @param event Событие
     * @return Материал для события
     */
    private Material getEventMaterial(CustomEvent event) {
        
        switch (getEventCategory(event)) {
            case "PLAYER": return Material.PLAYER_HEAD;
            case "WORLD": return Material.GRASS_BLOCK;
            case "BLOCK": return Material.COBBLESTONE;
            case "ENTITY": return Material.ZOMBIE_SPAWN_EGG;
            case "SYSTEM": return Material.COMMAND_BLOCK;
            case "CHAT": return Material.PAPER;
            default: return Material.NETHER_STAR;
        }
    }
    
    /**
     * Получает отображаемое имя события
     * @param event Событие
     * @return Отображаемое имя события
     */
    private String getEventDisplayName(CustomEvent event) {
        
        String eventId = event.getId().toString();
        switch (eventId.toLowerCase()) {
            case "onjoin": return "При входе";
            case "onleave": return "При выходе";
            case "onchat": return "При чате";
            case "onblockbreak": return "При разрушении блока";
            case "onblockplace": return "При установке блока";
            case "onplayermove": return "При движении игрока";
            case "onplayerdeath": return "При смерти игрока";
            case "oncommand": return "При команде";
            case "ontick": return "Каждый тик";
            default: return event.getName();
        }
    }

    /**
     * Получает описание события
     * @param event Событие
     * @return Описание события
     */
    private String getEventDescription(CustomEvent event) {
        
        String eventId = event.getId().toString();
        switch (eventId.toLowerCase()) {
            case "onjoin": return "Срабатывает когда игрок заходит на сервер";
            case "onleave": return "Срабатывает когда игрок выходит с сервера";
            case "onchat": return "Срабатывает когда игрок пишет в чат";
            case "onblockbreak": return "Срабатывает когда игрок ломает блок";
            case "onblockplace": return "Срабатывает когда игрок ставит блок";
            case "onplayermove": return "Срабатывает когда игрок двигается";
            case "onplayerdeath": return "Срабатывает когда игрок умирает";
            case "oncommand": return "Срабатывает когда игрок использует команду";
            case "ontick": return "Срабатывает каждый игровой тик";
            default: return event.getDescription();
        }
    }
    
    /**
     * Выбирает событие для блока
     * @param eventId ID события
     */
    private void selectEvent(String eventId) {
        
        BlockPlacementHandler placementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
        if (placementHandler == null) {
            player.sendMessage("§cОшибка: Не удалось получить обработчик блоков");
            return;
        }
        
        CodeBlock codeBlock = placementHandler.getCodeBlock(blockLocation);
        if (codeBlock == null) {
            player.sendMessage("§cОшибка: Блок кода не найден");
            return;
        }
        
        
        codeBlock.setEvent(eventId);
        codeBlock.setAction("NOT_SET");
        
        
        var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
        }
        
        
        player.sendMessage("§a✓ Событие '" + eventId + "' установлено!");
        player.sendMessage("§eКликните снова по блоку для настройки параметров.");
        
        
        player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, 
            player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 1);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        
        
        player.closeInventory();
    }
    
}