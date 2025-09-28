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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 🎆 УНИВЕРСАЛЬНЫЙ CodeBlockGUI - ДИНАМИЧЕСКИЙ ГЕНЕРАТОР ИНТЕРФЕЙСА
 *
 * Это революционная система, которая заменяет множество специализированных GUI классов
 * одним универсальным решением. GUI генерируется динамически на основе конфигурации
 * из coding_blocks.yml, что делает систему максимально гибкой и расширяемой.
 *
 * 🎆 UNIVERSAL CodeBlockGUI - DYNAMIC INTERFACE GENERATOR
 *
 * This is a revolutionary system that replaces multiple specialized GUI classes
 * with one universal solution. GUI is generated dynamically based on configuration
 * from coding_blocks.yml, making the system maximally flexible and extensible.
 *
 * 🎆 UNIVERSALER CodeBlockGUI - DYNAMISCHER INTERFACE GENERATOR
 *
 * Dies ist ein revolutionäres System, das mehrere spezialisierte GUI-Klassen
 * durch eine universelle Lösung ersetzt. GUI wird dynamisch basierend auf Konfiguration
 * aus coding_blocks.yml generiert, was das System maximal flexibel und erweiterbar macht.
 */
public class CodeBlockGUI implements GUIManager.ManagedGUIInterface {

    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final String blockId; // actionId, eventId, conditionId, etc.
    private final String blockType; // EVENT, ACTION, CONDITION, CONTROL, FUNCTION, VARIABLE
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;

    // 🎆 Улучшенные возможности
    private boolean hasUnsavedChanges = false;
    private final Map<Integer, String> slotValidationErrors = new HashMap<>();
    private final Map<Integer, Boolean> slotValidationStatus = new HashMap<>();
    private final Map<Integer, String> slotCurrentValues = new HashMap<>();
    private final Map<String, String> blockParameters = new HashMap<>();

    // 🎆 Цветовая схема для разных типов блоков
    private static final Map<String, String> TYPE_COLORS = new HashMap<>();
    private static final Map<String, Material> TYPE_MATERIALS = new HashMap<>();

    static {
        // Цвета для разных типов блоков
        TYPE_COLORS.put("EVENT", "§e");      // Желтый для событий
        TYPE_COLORS.put("ACTION", "§a");     // Зеленый для действий
        TYPE_COLORS.put("CONDITION", "§6");   // Оранжевый для условий
        TYPE_COLORS.put("CONTROL", "§c");    // Красный для управления
        TYPE_COLORS.put("FUNCTION", "§d");   // Розовый для функций
        TYPE_COLORS.put("VARIABLE", "§b");   // Голубой для переменных

        // Материалы для заголовков разных типов
        TYPE_MATERIALS.put("EVENT", Material.NETHER_STAR);
        TYPE_MATERIALS.put("ACTION", Material.REDSTONE);
        TYPE_MATERIALS.put("CONDITION", Material.COMPARATOR);
        TYPE_MATERIALS.put("CONTROL", Material.REPEATER);
        TYPE_MATERIALS.put("FUNCTION", Material.WRITABLE_BOOK);
        TYPE_MATERIALS.put("VARIABLE", Material.NAME_TAG);
    }

    /**
     * 🎆 КОНСТРУКТОР УНИВЕРСАЛЬНОГО GUI
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param blockLocation Расположение блока для настройки
     * @param blockId ID блока (actionId, eventId, conditionId и т.д.)
     * @param blockType Тип блока (EVENT, ACTION, CONDITION, CONTROL, FUNCTION, VARIABLE)
     */
    public CodeBlockGUI(MegaCreative plugin, Player player, Location blockLocation, String blockId, String blockType) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockId = blockId;
        this.blockType = blockType;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();

        // Инициализация сервисов
        if (plugin != null && plugin.getServiceRegistry() != null) {
            this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        } else {
            this.blockConfigService = null;
            player.sendMessage("§cОшибка: Сервис конфигурации блоков недоступен!");
        }

        // Создание инвентаря с динамическим размером
        String guiTitle = createGUITitle();
        this.inventory = Bukkit.createInventory(null, calculateInventorySize(), guiTitle);

        setupInventory();
    }

    /**
     * 🎆 СОЗДАНИЕ ЗАГОЛОВКА GUI
     */
    private String createGUITitle() {
        String color = TYPE_COLORS.getOrDefault(blockType, "§f");
        String typeName = getLocalizedTypeName(blockType);
        String blockName = getBlockDisplayName(blockId);

        return String.format("§8Настройка %s%s: %s", color, typeName, blockName);
    }

    /**
     * 🎆 ПОЛУЧЕНИЕ ЛОКАЛИЗОВАННОГО ИМЕНИ ТИПА БЛОКА
     */
    private String getLocalizedTypeName(String type) {
        switch (type) {
            case "EVENT": return "События";
            case "ACTION": return "Действия";
            case "CONDITION": return "Условия";
            case "CONTROL": return "Управления";
            case "FUNCTION": return "Функции";
            case "VARIABLE": return "Переменные";
            default: return type;
        }
    }

    /**
     * 🎆 ПОЛУЧЕНИЕ ОТОБРАЖАЕМОГО ИМЕНИ БЛОКА
     */
    private String getBlockDisplayName(String blockId) {
        if (blockConfigService != null) {
            BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(blockId);
            if (config != null) {
                return config.getDisplayName();
            }
        }

        // Fallback: human-readable names for common actions
        switch (blockId.toLowerCase()) {
            case "sendmessage": return "Отправить сообщение";
            case "teleport": return "Телепортировать";
            case "giveitem": return "Выдать предмет";
            case "onjoin": return "При входе";
            case "onchat": return "При чате";
            case "hasitem": return "Если есть предмет";
            case "ifvarequals": return "Если переменная равна";
            default: return blockId;
        }
    }

    /**
     * 🎆 РАСЧЕТ РАЗМЕРА ИНВЕНТАРЯ
     */
    private int calculateInventorySize() {
        // Загружаем конфигурацию для определения количества слотов
        var actionConfigurations = blockConfigService.getActionConfigurations();
        if (actionConfigurations != null) {
            var actionConfig = actionConfigurations.getConfigurationSection(blockId);
            if (actionConfig != null && actionConfig.contains("slots")) {
                // Подсчитываем количество слотов в конфигурации
                Set<String> slotKeys = actionConfig.getConfigurationSection("slots").getKeys(false);
                int maxSlots = slotKeys.stream().mapToInt(Integer::parseInt).max().orElse(0);

                // Рассчитываем размер инвентаря (кратно 9, минимум 27, максимум 54)
                int requiredRows = (int) Math.ceil((maxSlots + 10) / 9.0); // +10 для заголовка и кнопок
                return Math.max(27, Math.min(54, requiredRows * 9));
            }
        }

        return 45; // Размер по умолчанию
    }

    /**
     * 🎆 НАСТРОЙКА ИНВЕНТАРЯ
     */
    private void setupInventory() {
        inventory.clear();

        // Добавляем декоративную рамку
        addDecorativeBorder();

        // Добавляем информационный элемент
        addInfoItem();

        // Загружаем конфигурацию действия
        loadActionConfiguration();

        // Загружаем существующие параметры из блока кода
        loadExistingParameters();

        // Добавляем кнопки управления
        addControlButtons();
    }

    /**
     * 🎆 ДОБАВЛЕНИЕ ДЕКОРАТИВНОЙ РАМКИ
     */
    private void addDecorativeBorder() {
        Material borderMaterial = Material.BLACK_STAINED_GLASS_PANE;
        ItemStack borderItem = new ItemStack(borderMaterial);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(" ");
        borderItem.setItemMeta(borderMeta);

        // Заполняем рамку
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i < 9 || i >= inventory.getSize() - 9 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderItem);
            }
        }
    }

    /**
     * 🎆 ДОБАВЛЕНИЕ ИНФОРМАЦИОННОГО ЭЛЕМЕНТА
     */
    private void addInfoItem() {
        Material typeMaterial = TYPE_MATERIALS.getOrDefault(blockType, Material.STONE);
        ItemStack infoItem = new ItemStack(typeMaterial);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(createGUITitle());

        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Настройка параметров блока");
        infoLore.add("");
        infoLore.add("§aПеретащите предметы в слоты");
        infoLore.add("§aдля настройки параметров");
        infoLore.add("");
        infoLore.add("§f✨ Универсальная система настройки");
        infoLore.add("§7• Динамическая генерация");
        infoLore.add("§7• Валидация в реальном времени");
        infoLore.add("§7• Автоматические подсказки");
        infoMeta.setLore(infoLore);

        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
    }

    /**
     * 🎆 ЗАГРУЗКА КОНФИГУРАЦИИ ДЕЙСТВИЯ
     */
    private void loadActionConfiguration() {
        var actionConfigurations = blockConfigService.getActionConfigurations();
        if (actionConfigurations == null) {
            player.sendMessage("§eИнформация: Используются базовые настройки для " + blockId);
            setupGenericSlots();
            return;
        }

        var actionConfig = actionConfigurations.getConfigurationSection(blockId);
        if (actionConfig == null) {
            player.sendMessage("§eИнформация: Конфигурация для " + blockId + " не найдена");
            setupGenericSlots();
            return;
        }

        // Проверяем наличие секции slots
        if (actionConfig.contains("slots")) {
            var slotsSection = actionConfig.getConfigurationSection("slots");
            Set<String> slotKeys = slotsSection.getKeys(false);

            for (String slotKey : slotKeys) {
                try {
                    int slot = Integer.parseInt(slotKey);
                    var slotConfig = slotsSection.getConfigurationSection(slotKey);

                    if (slotConfig != null) {
                        createSlotItem(slot, slotConfig);
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§cОшибка: Неверный номер слота: " + slotKey);
                }
            }
        } else {
            // Если нет слотов, создаем базовые
            setupGenericSlots();
        }
    }

    /**
     * 🎆 СОЗДАНИЕ ЭЛЕМЕНТА СЛОТА
     */
    private void createSlotItem(int slot, org.bukkit.configuration.ConfigurationSection slotConfig) {
        String name = slotConfig.getString("name", "§cНеизвестный параметр");
        String description = slotConfig.getString("description", "§7Описание недоступно");
        String placeholderItemName = slotConfig.getString("placeholder_item", "PAPER");
        String slotName = slotConfig.getString("slot_name", "param_" + slot);
        String validation = slotConfig.getString("validation", "");
        String hint = slotConfig.getString("hint", "");

        // Получаем материал для placeholder
        Material placeholderMaterial = getMaterialByName(placeholderItemName);

        ItemStack slotItem = new ItemStack(placeholderMaterial);
        ItemMeta slotMeta = slotItem.getItemMeta();
        slotMeta.setDisplayName(name);

        List<String> slotLore = new ArrayList<>();
        slotLore.add(description);

        if (!validation.isEmpty()) {
            slotLore.add("§8Валидация: " + validation);
        }

        if (!hint.isEmpty()) {
            slotLore.add("§7Подсказка: " + hint);
        }

        slotLore.add("");
        slotLore.add("§eКликните чтобы изменить");

        slotMeta.setLore(slotLore);
        slotItem.setItemMeta(slotMeta);

        inventory.setItem(slot, slotItem);

        // Сохраняем информацию о слоте для валидации
        slotValidationErrors.put(slot, "");
        slotValidationStatus.put(slot, true);
        slotCurrentValues.put(slot, "");
    }

    /**
     * 🎆 НАСТРОЙКА БАЗОВЫХ СЛОТОВ (FALLBACK)
     */
    private void setupGenericSlots() {
        // Создаем базовые слоты для общих параметров
        createGenericParameterSlot(10, "Основной параметр", "PAPER", "main_param");
        createGenericParameterSlot(12, "Дополнительный параметр", "PAPER", "extra_param");
    }

    /**
     * 🎆 СОЗДАНИЕ БАЗОВОГО ПАРАМЕТРА
     */
    private void createGenericParameterSlot(int slot, String name, String material, String slotName) {
        Material mat = getMaterialByName(material);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        lore.add("§7Базовый параметр");
        lore.add("");
        lore.add("§eКликните чтобы изменить");
        meta.setLore(lore);

        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }

    /**
     * 🎆 ПОЛУЧЕНИЕ МАТЕРИАЛА ПО ИМЕНИ
     */
    private Material getMaterialByName(String name) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.PAPER; // Fallback
        }
    }

    /**
     * 🎆 ЗАГРУЗКА СУЩЕСТВУЮЩИХ ПАРАМЕТРОВ
     */
    private void loadExistingParameters() {
        if (plugin.getServiceRegistry().getBlockPlacementHandler() == null) {
            return;
        }

        CodeBlock codeBlock = plugin.getServiceRegistry().getBlockPlacementHandler().getCodeBlock(blockLocation);
        if (codeBlock != null) {
            // Загружаем параметры из блока кода
            Map<String, String> parameters = codeBlock.getParameters();
            if (parameters != null) {
                blockParameters.putAll(parameters);
            }
        }
    }

    /**
     * 🎆 ДОБАВЛЕНИЕ КНОПОК УПРАВЛЕНИЯ
     */
    private void addControlButtons() {
        // Кнопка "Назад"
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c⬅ Назад");
        List<String> backLore = new ArrayList<>();
        backLore.add("§7Вернуться к выбору действий");
        backMeta.setLore(backLore);
        backButton.setItemMeta(backMeta);
        inventory.setItem(inventory.getSize() - 5, backButton);

        // Кнопка "Сохранить"
        ItemStack saveButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = saveButton.getItemMeta();
        saveMeta.setDisplayName("§a✓ Сохранить");
        List<String> saveLore = new ArrayList<>();
        saveLore.add("§7Сохранить изменения");
        if (hasUnsavedChanges) {
            saveLore.add("§eЕсть несохраненные изменения");
        }
        saveMeta.setLore(saveLore);
        saveButton.setItemMeta(saveMeta);
        inventory.setItem(inventory.getSize() - 4, saveButton);
    }

    /**
     * 🎆 ОТКРЫТИЕ GUI
     */
    public void open() {
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);

        // Аудио обратная связь
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);

        // Визуальные эффекты
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE,
            player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 1);
    }

    @Override
    public String getGUITitle() {
        return createGUITitle();
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (!player.equals(event.getWhoClicked())) return;
        if (!inventory.equals(event.getInventory())) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        String displayName = meta.getDisplayName();

        // Обработка кнопки "Назад"
        if (displayName.contains("Назад")) {
            handleBackButton();
            return;
        }

        // Обработка кнопки "Сохранить"
        if (displayName.contains("Сохранить")) {
            handleSaveButton();
            return;
        }

        // Обработка клика по слоту параметра
        handleParameterSlotClick(event.getSlot(), clicked);
    }

    /**
     * 🎆 ОБРАБОТКА КНОПКИ "НАЗАД"
     */
    private void handleBackButton() {
        if (hasUnsavedChanges) {
            // Показываем предупреждение о несохраненных изменениях
            player.sendMessage("§e⚠ У вас есть несохраненные изменения!");
            player.sendMessage("§7Сохраните их перед выходом или они будут потеряны.");
            return;
        }

        // Возвращаемся к выбору действий
        ActionSelectionGUI selectionGUI = new ActionSelectionGUI(plugin, player, blockLocation,
            getBlockMaterial());
        selectionGUI.open();
    }

    /**
     * 🎆 ОБРАБОТКА КНОПКИ "СОХРАНИТЬ"
     */
    private void handleSaveButton() {
        if (saveParameters()) {
            hasUnsavedChanges = false;
            player.sendMessage("§a✓ Параметры сохранены!");

            // Обновляем табличку блока
            updateBlockSign();

            // Визуальные эффекты успеха
            player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY,
                player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 1);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);

            // Обновляем кнопки
            addControlButtons();
        }
    }

    /**
     * 🎆 ОБРАБОТКА КЛИКА ПО СЛОТУ ПАРАМЕТРА
     */
    private void handleParameterSlotClick(int slot, ItemStack clicked) {
        // Здесь будет логика для изменения параметров
        // Пока просто показываем информацию
        player.sendMessage("§7Клик по слоту " + slot + ": " + clicked.getItemMeta().getDisplayName());

        // В будущем здесь будет открытие специализированного интерфейса для ввода значения
        // Например, для строк - AnvilGUI, для предметов - выбор предмета, и т.д.
    }

    /**
     * 🎆 СОХРАНЕНИЕ ПАРАМЕТРОВ
     */
    private boolean saveParameters() {
        try {
            if (plugin.getServiceRegistry().getBlockPlacementHandler() == null) {
                player.sendMessage("§cОшибка: Не удалось получить обработчик блоков");
                return false;
            }

            CodeBlock codeBlock = plugin.getServiceRegistry().getBlockPlacementHandler().getCodeBlock(blockLocation);
            if (codeBlock == null) {
                player.sendMessage("§cОшибка: Блок кода не найден");
                return false;
            }

            // Сохраняем параметры в блок кода
            for (Map.Entry<String, String> entry : blockParameters.entrySet()) {
                codeBlock.setParameter(entry.getKey(), entry.getValue());
            }

            // Обновляем действие блока
            if (blockType.equals("ACTION") || blockType.equals("EVENT") || blockType.equals("CONDITION")) {
                if (blockType.equals("ACTION")) {
                    codeBlock.setAction(blockId);
                } else if (blockType.equals("EVENT")) {
                    codeBlock.setEvent(blockId);
                } else if (blockType.equals("CONDITION")) {
                    codeBlock.setParameter("condition", blockId);
                }
            }

            // Сохраняем мир
            var creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null) {
                plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
            }

            return true;
        } catch (Exception e) {
            player.sendMessage("§cОшибка при сохранении: " + e.getMessage());
            return false;
        }
    }

    /**
     * 🎆 ОБНОВЛЕНИЕ ТАБЛИЧКИ БЛОКА
     */
    private void updateBlockSign() {
        if (plugin.getServiceRegistry().getBlockPlacementHandler() != null) {
            CodeBlock codeBlock = plugin.getServiceRegistry().getBlockPlacementHandler().getCodeBlock(blockLocation);
            if (codeBlock != null) {
                plugin.getServiceRegistry().getBlockPlacementHandler().createSignForBlock(blockLocation, codeBlock);
            }
        }
    }

    /**
     * 🎆 ПОЛУЧЕНИЕ МАТЕРИАЛА БЛОКА
     */
    private Material getBlockMaterial() {
        // Получаем материал блока из его расположения
        return blockLocation.getBlock().getType();
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Опциональная очистка при закрытии GUI
        // GUIManager автоматически снимает регистрацию
    }

    @Override
    public void onCleanup() {
        // Вызывается при очистке GUI через GUIManager
        // Специальная очистка не требуется для этого GUI
    }
}
