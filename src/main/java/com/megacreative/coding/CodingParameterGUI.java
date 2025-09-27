package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.gui.AnvilInputGUI;
import com.megacreative.managers.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.Arrays;

public class CodingParameterGUI implements GUIManager.ManagedGUIInterface {
    // Constants for GUI messages
    private static final String GUI_TITLE_PREFIX = "§bНастройка: ";
    private static final String PARAMETER_NAME_PREFIX = "§a";
    private static final String PARAMETER_DESCRIPTION_PREFIX = "§7";
    private static final String CURRENT_VALUE_PREFIX = "§eТекущее значение: §f";
    private static final String CLICK_TO_CHANGE = "§e▶ Нажмите для изменения";
    private static final String DONE_BUTTON_NAME = "§a§lГотово";
    private static final String PREV_PAGE_BUTTON_NAME = "§e§lПредыдущая страница";
    private static final String NEXT_PAGE_BUTTON_NAME = "§e§lСледующая страница";
    private static final String PARAMETER_INPUT_TITLE_PREFIX = "Ввод параметра: ";
    private static final String PARAMETER_UPDATED_MESSAGE = "§a✅ Параметр '%s' обновлен!";
    private static final String PARAMETER_UPDATE_ERROR = "§c❌ Ошибка при обновлении параметра: ";
    private static final String PARAMETER_SELECTION_ERROR = "§c❌ Ошибка при выборе параметра: ";
    private static final String PARAMETER_INPUT_CANCELLED = "§c❌ Ввод параметра отменен";
    
    // Constants for action names
    private static final String ACTION_SEND_MESSAGE = "sendMessage";
    private static final String ACTION_TELEPORT = "teleport";
    private static final String ACTION_GIVE_ITEM = "giveItem";
    private static final String ACTION_PLAY_SOUND = "playSound";
    private static final String ACTION_EFFECT = "effect";
    private static final String ACTION_COMMAND = "command";
    private static final String ACTION_BROADCAST = "broadcast";
    private static final String ACTION_HEAL_PLAYER = "healPlayer";
    private static final String ACTION_SET_GAMEMODE = "setGameMode";
    private static final String ACTION_EXPLOSION = "explosion";
    private static final String ACTION_SET_BLOCK = "setBlock";
    private static final String ACTION_SET_VAR = "setVar";
    private static final String ACTION_ADD_VAR = "addVar";
    private static final String ACTION_SUB_VAR = "subVar";
    private static final String ACTION_MUL_VAR = "mulVar";
    private static final String ACTION_DIV_VAR = "divVar";
    private static final String ACTION_SET_TIME = "setTime";
    private static final String ACTION_SET_WEATHER = "setWeather";
    private static final String ACTION_SPAWN_MOB = "spawnMob";
    private static final String ACTION_IS_IN_WORLD = "isInWorld";
    private static final String ACTION_HAS_ITEM = "hasItem";
    private static final String ACTION_IS_NEAR_BLOCK = "isNearBlock";
    private static final String ACTION_TIME_OF_DAY = "timeOfDay";
    private static final String ACTION_WORLD_TIME = "worldTime";
    private static final String ACTION_IF_VAR_EQUALS = "ifVarEquals";
    private static final String ACTION_IF_VAR_GREATER = "ifVarGreater";
    private static final String ACTION_IF_VAR_LESS = "ifVarLess";
    private static final String ACTION_COMPARE_VARIABLE = "compareVariable";
    private static final String ACTION_PLAYER_HEALTH = "playerHealth";
    private static final String ACTION_IF_GAMEMODE = "ifGameMode";
    private static final String ACTION_IF_WORLD_TYPE = "ifWorldType";
    private static final String ACTION_IF_MOB_TYPE = "ifMobType";
    private static final String ACTION_IF_MOB_NEAR = "ifMobNear";
    private static final String ACTION_GET_VAR = "getVar";
    private static final String ACTION_SET_GLOBAL_VAR = "setGlobalVar";
    private static final String ACTION_GET_GLOBAL_VAR = "getGlobalVar";
    private static final String ACTION_SET_SERVER_VAR = "setServerVar";
    private static final String ACTION_GET_SERVER_VAR = "getServerVar";
    private static final String ACTION_WAIT = "wait";
    private static final String ACTION_RANDOM_NUMBER = "randomNumber";
    private static final String ACTION_PLAY_PARTICLE = "playParticle";

    private final MegaCreative plugin;
    private final Player player;
    private final String action;
    private final Location blockLocation;
    private final Consumer<Map<String, Object>> onComplete;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final Map<String, Object> parameters = new HashMap<>();
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 7;

    public CodingParameterGUI(MegaCreative plugin, Player player, String action, Location blockLocation, Consumer<Map<String, Object>> onComplete, GUIManager guiManager) {
        this.plugin = plugin;
        this.player = player;
        this.action = action;
        this.blockLocation = blockLocation;
        this.onComplete = onComplete;
        this.guiManager = guiManager;
        this.inventory = Bukkit.createInventory(null, 9, GUI_TITLE_PREFIX + action);
        setupInventory();
    }

    private void setupInventory() {
        inventory.clear();
        List<ParameterField> fields = getParameterFields(action);
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, fields.size());
        
        // Добавляем поля параметров для текущей страницы
        for (int i = 0; i < endIndex - startIndex; i++) {
            ParameterField field = fields.get(startIndex + i);
            ItemStack item = new ItemStack(field.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(PARAMETER_NAME_PREFIX + field.getName());
                String currentValue = parameters.getOrDefault(field.getKey(), field.getDefaultValue()).toString();
                meta.setLore(Arrays.asList(
                    PARAMETER_DESCRIPTION_PREFIX + field.getDescription(),
                    CURRENT_VALUE_PREFIX + currentValue,
                    CLICK_TO_CHANGE
                ));
                item.setItemMeta(meta);
            }
            inventory.setItem(i, item);
        }
        
        // Кнопка "Готово"
        if (currentPage == 0) {
            ItemStack doneButton = new ItemStack(Material.EMERALD);
            ItemMeta doneMeta = doneButton.getItemMeta();
            doneMeta.setDisplayName(DONE_BUTTON_NAME);
            doneButton.setItemMeta(doneMeta);
            inventory.setItem(8, doneButton);
        }
        
        // Кнопки навигации
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName(PREV_PAGE_BUTTON_NAME);
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(7, prevButton);
        }
        
        if (endIndex < fields.size()) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName(NEXT_PAGE_BUTTON_NAME);
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(8, nextButton);
        }
    }

    private List<ParameterField> getParameterFields(String action) {
        switch (action) {
            case ACTION_SEND_MESSAGE:
                return Arrays.asList(
                    new ParameterField("message", "Сообщение", "Привет, %player%!", Material.PAPER)
                );
            case ACTION_TELEPORT:
                return Arrays.asList(
                    new ParameterField("coords", "Координаты", "100 70 200", Material.COMPASS)
                );
            case ACTION_GIVE_ITEM:
                return Arrays.asList(
                    new ParameterField("item", "Предмет", "DIAMOND", Material.CHEST),
                    new ParameterField("amount", "Количество", "1", Material.HOPPER)
                );
            case ACTION_PLAY_SOUND:
                return Arrays.asList(
                    new ParameterField("sound", "Звук", "ENTITY_PLAYER_LEVELUP", Material.NOTE_BLOCK),
                    new ParameterField("volume", "Громкость", "1.0", Material.SLIME_BLOCK),
                    new ParameterField("pitch", "Тон", "1.0", Material.NOTE_BLOCK)
                );
            case ACTION_EFFECT:
                return Arrays.asList(
                    new ParameterField("effect", "Эффект", "SPEED", Material.POTION),
                    new ParameterField("duration", "Длительность", "200", Material.CLOCK),
                    new ParameterField("amplifier", "Усиление", "0", Material.REDSTONE)
                );
            case ACTION_COMMAND:
                return Arrays.asList(
                    new ParameterField("command", "Команда", "say Привет!", Material.COMMAND_BLOCK)
                );
            case ACTION_BROADCAST:
                return Arrays.asList(
                    new ParameterField("message", "Сообщение", "Всем привет!", Material.BELL)
                );
            case ACTION_HEAL_PLAYER:
                return Arrays.asList(
                    new ParameterField("amount", "Количество здоровья", "10.0", Material.GOLDEN_APPLE)
                );
            case ACTION_SET_GAMEMODE:
                return Arrays.asList(
                    new ParameterField("mode", "Режим игры", "CREATIVE", Material.DIAMOND_SWORD)
                );
            case ACTION_EXPLOSION:
                return Arrays.asList(
                    new ParameterField("power", "Мощность", "4.0", Material.TNT),
                    new ParameterField("breakBlocks", "Ломать блоки", "true", Material.BARRIER)
                );
            case ACTION_SET_BLOCK:
                return Arrays.asList(
                    new ParameterField("material", "Материал", "STONE", Material.STONE),
                    new ParameterField("coords", "Координаты", "100 70 200", Material.COMPASS)
                );
            case ACTION_SET_VAR:
            case ACTION_ADD_VAR:
            case ACTION_SUB_VAR:
            case ACTION_MUL_VAR:
            case ACTION_DIV_VAR:
                return Arrays.asList(
                    new ParameterField("var", "Переменная", "myVar", Material.IRON_INGOT),
                    new ParameterField("value", "Значение", "0", Material.GOLD_INGOT)
                );
            case ACTION_SET_TIME:
                return Arrays.asList(
                    new ParameterField("time", "Время", "0", Material.CLOCK)
                );
            case ACTION_SET_WEATHER:
                return Arrays.asList(
                    new ParameterField("weather", "Погода", "clear", Material.WATER_BUCKET)
                );
            case ACTION_SPAWN_MOB:
                return Arrays.asList(
                    new ParameterField("mob", "Моб", "ZOMBIE", Material.ZOMBIE_HEAD),
                    new ParameterField("amount", "Количество", "1", Material.HOPPER)
                );
            case ACTION_IS_IN_WORLD:
                return Arrays.asList(
                    new ParameterField("world", "Мир", "world", Material.GLOBE_BANNER_PATTERN)
                );
            case ACTION_HAS_ITEM:
                return Arrays.asList(
                    new ParameterField("item", "Предмет", "DIAMOND", Material.STICK)
                );
            case ACTION_IS_NEAR_BLOCK:
                return Arrays.asList(
                    new ParameterField("block", "Блок", "STONE", Material.GLASS),
                    new ParameterField("radius", "Радиус", "5", Material.COMPASS)
                );
            case ACTION_TIME_OF_DAY:
            case ACTION_WORLD_TIME:
                return Arrays.asList(
                    new ParameterField("timeRange", "Время дня", "DAY", Material.SUNFLOWER)
                );
            case ACTION_IF_VAR_EQUALS:
            case ACTION_IF_VAR_GREATER:
            case ACTION_IF_VAR_LESS:
                return Arrays.asList(
                    new ParameterField("variable", "Переменная", "myVar", Material.OBSIDIAN),
                    new ParameterField("value", "Значение", "0", Material.GOLD_INGOT)
                );
            case ACTION_COMPARE_VARIABLE:
                return Arrays.asList(
                    new ParameterField("var1", "Переменная 1", "var1", Material.IRON_INGOT),
                    new ParameterField("operator", "Оператор", ">", Material.COMPARATOR),
                    new ParameterField("var2", "Переменная 2", "var2", Material.GOLD_INGOT)
                );
            case ACTION_PLAYER_HEALTH:
                return Arrays.asList(
                    new ParameterField("health", "Здоровье", "10.0", Material.GOLDEN_APPLE),
                    new ParameterField("operator", "Оператор", ">", Material.COMPARATOR)
                );
            case ACTION_IF_GAMEMODE:
                return Arrays.asList(
                    new ParameterField("mode", "Режим", "SURVIVAL", Material.DIAMOND_SWORD)
                );
            case ACTION_IF_WORLD_TYPE:
                return Arrays.asList(
                    new ParameterField("type", "Тип", "NORMAL", Material.GRASS_BLOCK)
                );
            case ACTION_IF_MOB_TYPE:
                return Arrays.asList(
                    new ParameterField("mob", "Моб", "ZOMBIE", Material.SPAWNER)
                );
            case ACTION_IF_MOB_NEAR:
                return Arrays.asList(
                    new ParameterField("radius", "Радиус", "5", Material.ENDER_EYE)
                );
            case ACTION_GET_VAR:
                return Arrays.asList(
                    new ParameterField("var", "Переменная", "myVar", Material.BOOK)
                );
            case ACTION_SET_GLOBAL_VAR:
                return Arrays.asList(
                    new ParameterField("var", "Глобальная переменная", "money", Material.EMERALD),
                    new ParameterField("value", "Значение", "100", Material.GOLD_INGOT)
                );
            case ACTION_GET_GLOBAL_VAR:
                return Arrays.asList(
                    new ParameterField("var", "Глобальная переменная", "money", Material.EMERALD),
                    new ParameterField("localVar", "Локальная переменная", "temp", Material.BOOK)
                );
            case ACTION_SET_SERVER_VAR:
                return Arrays.asList(
                    new ParameterField("var", "Серверная переменная", "event_active", Material.BEACON),
                    new ParameterField("value", "Значение", "true", Material.GOLD_INGOT)
                );
            case ACTION_GET_SERVER_VAR:
                return Arrays.asList(
                    new ParameterField("var", "Серверная переменная", "event_active", Material.BEACON),
                    new ParameterField("localVar", "Локальная переменная", "temp", Material.BOOK)
                );
            case ACTION_WAIT:
                return List.of(new ParameterField("ticks", "Задержка (в тиках)", "20", Material.CLOCK));
            
            case ACTION_RANDOM_NUMBER:
                return List.of(
                    new ParameterField("min", "Мин. число", "1", Material.IRON_NUGGET),
                    new ParameterField("max", "Макс. число", "100", Material.GOLD_NUGGET),
                    new ParameterField("var", "Сохранить в переменную", "random_num", Material.NAME_TAG)
                );
            
            case ACTION_PLAY_PARTICLE:
                return List.of(
                    new ParameterField("particle", "Частица", "FLAME", Material.BLAZE_POWDER),
                    new ParameterField("count", "Количество", "20", Material.GLOWSTONE_DUST),
                    new ParameterField("offset", "Разброс", "0.5", Material.COMPASS)
                );
            
            default:
                return Arrays.asList();
        }
    }

    public void open() {
        // Register with GUIManager and open inventory
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
    }
    
    @Override
    public String getGUITitle() {
        return "Parameter Config GUI for " + action;
    }
    
    /**
     * Обновляет инвентарь с новыми данными
     */
    public void refresh() {
        setupInventory(); // Просто перестраиваем иконки с обновленными параметрами
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)) return;
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Обработка навигации
        if (displayName.contains("Предыдущая страница")) {
            currentPage--;
            setupInventory();
            return;
        }
        if (displayName.contains("Следующая страница")) {
            currentPage++;
            setupInventory();
            return;
        }
        
        // Обработка "Готово"
        if (displayName.contains("Готово")) {
            onComplete.accept(parameters);
            player.closeInventory();
            // GUIManager automatically unregisters on close
            return;
        }
        
        // Обработка выбора параметра - открываем AnvilInputGUI
        int clickedSlot = event.getSlot();
        List<ParameterField> fields = getParameterFields(action);
        int startIndex = currentPage * ITEMS_PER_PAGE;
        
        if (clickedSlot < fields.size() - startIndex && startIndex + clickedSlot < fields.size()) {
            try {
                ParameterField field = fields.get(startIndex + clickedSlot);
                String currentValue = parameters.getOrDefault(field.getKey(), field.getDefaultValue()).toString();
                
                // Открываем наковальню для ввода
                new AnvilInputGUI(plugin, player, PARAMETER_INPUT_TITLE_PREFIX + field.getName(), (newValue) -> {
                    try {
                        // Этот код выполнится, когда игрок подтвердит ввод в наковальне
                        parameters.put(field.getKey(), newValue);
                        player.sendMessage(String.format(PARAMETER_UPDATED_MESSAGE, field.getName()));

                        // --- ИСПРАВЛЕНИЕ ---
                        // Обновляем и открываем ТЕКУЩЕЕ GUI, а не создаем новое
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            this.refresh(); // Обновляем иконки в инвентаре
                            this.open();    // Показываем его игроку снова
                        });
                    } catch (Exception e) {
                        player.sendMessage(PARAMETER_UPDATE_ERROR + e.getMessage());
                        plugin.getLogger().warning("Ошибка в CodingParameterGUI: " + e.getMessage());
                    }
                }, () -> {
                    // Callback для отмены
                    player.sendMessage(PARAMETER_INPUT_CANCELLED);
                });
            } catch (Exception e) {
                player.sendMessage(PARAMETER_SELECTION_ERROR + e.getMessage());
                plugin.getLogger().warning("Ошибка в CodingParameterGUI при выборе параметра: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Optional cleanup when GUI is closed
        // GUIManager handles automatic unregistration
    }
    
    @Override
    public void onCleanup() {
        // Called when GUI is being cleaned up by GUIManager
        // No special cleanup needed for this GUI
    }

    // Вспомогательный класс для описания параметра
    private static class ParameterField {
        private final String key;
        private final String name;
        private final String defaultValue;
        private final Material icon;

        public ParameterField(String key, String name, String defaultValue, Material icon) {
            this.key = key;
            this.name = name;
            this.defaultValue = defaultValue;
            this.icon = icon;
        }

        public String getKey() { return key; }
        public String getName() { return name; }
        public String getDefaultValue() { return defaultValue; }
        public Material getIcon() { return icon; }
        public String getDescription() { return "Параметр: " + name; }
    }
}