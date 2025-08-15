package com.megacreative.coding;

import com.megacreative.gui.AnvilInputGUI;
import com.megacreative.listeners.GuiListener;
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

public class CodingParameterGUI {
    private final Player player;
    private final String action;
    private final Location blockLocation;
    private final Consumer<Map<String, Object>> onComplete;
    private final Inventory inventory;
    private final Map<String, Object> parameters = new HashMap<>();
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 7;
    private boolean isCompleted = false; // Флаг для предотвращения двойного завершения

    public CodingParameterGUI(Player player, String action, Location blockLocation, Consumer<Map<String, Object>> onComplete) {
        this.player = player;
        this.action = action;
        this.blockLocation = blockLocation;
        this.onComplete = onComplete;
        this.inventory = Bukkit.createInventory(null, 9, "§bНастройка: " + action);
        
        // DEBUG: Логируем создание GUI
        com.megacreative.MegaCreative.getInstance().getLogger().info("=== DEBUG: СОЗДАНИЕ CodingParameterGUI ===");
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Игрок: " + player.getName());
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Действие: " + action);
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Локация: " + blockLocation);
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: onComplete: " + (onComplete != null));
        
        // Регистрируем GUI в централизованной системе
        GuiListener.registerOpenGui(player, this);
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
                meta.setDisplayName("§a" + field.getName());
                String currentValue = parameters.getOrDefault(field.getKey(), field.getDefaultValue()).toString();
                meta.setLore(Arrays.asList(
                    "§7" + field.getDescription(),
                    "§eТекущее значение: §f" + currentValue,
                    "§e▶ Нажмите для изменения"
                ));
                item.setItemMeta(meta);
            }
            inventory.setItem(i, item);
        }
        
        // Кнопка "Готово" - всегда показываем для действий с параметрами
        if (currentPage == 0) {
            ItemStack doneButton = new ItemStack(Material.EMERALD);
            ItemMeta doneMeta = doneButton.getItemMeta();
            if (fields.isEmpty()) {
                doneMeta.setDisplayName("§a§lГотово (автоматически)");
                doneMeta.setLore(Arrays.asList("§7Нажмите или подождите", "§7для автоматического завершения"));
            } else {
                doneMeta.setDisplayName("§a§lГотово");
                doneMeta.setLore(Arrays.asList("§7Нажмите для завершения", "§7настройки параметров"));
            }
            doneButton.setItemMeta(doneMeta);
            inventory.setItem(8, doneButton);
        }
        
        // Кнопки навигации
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName("§e§lПредыдущая страница");
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(7, prevButton);
        }
        
        if (endIndex < fields.size()) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName("§e§lСледующая страница");
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(8, nextButton);
        }
    }

    private List<ParameterField> getParameterFields(String action) {
        switch (action) {
            case "sendMessage":
                return Arrays.asList(
                    new ParameterField("message", "Сообщение", "Привет, %player%!", Material.PAPER)
                );
            case "teleport":
                return Arrays.asList(
                    new ParameterField("coords", "Координаты", "100 70 200", Material.COMPASS)
                );
            case "giveItem":
                return Arrays.asList(
                    new ParameterField("item", "Предмет", "DIAMOND", Material.CHEST),
                    new ParameterField("amount", "Количество", "1", Material.HOPPER)
                );
            case "playSound":
                return Arrays.asList(
                    new ParameterField("sound", "Звук", "ENTITY_PLAYER_LEVELUP", Material.NOTE_BLOCK),
                    new ParameterField("volume", "Громкость", "1.0", Material.SLIME_BLOCK),
                    new ParameterField("pitch", "Тон", "1.0", Material.NOTE_BLOCK)
                );
            case "effect":
                return Arrays.asList(
                    new ParameterField("effect", "Эффект", "SPEED", Material.POTION),
                    new ParameterField("duration", "Длительность", "200", Material.CLOCK),
                    new ParameterField("amplifier", "Усиление", "0", Material.REDSTONE)
                );
            case "command":
                return Arrays.asList(
                    new ParameterField("command", "Команда", "say Привет!", Material.COMMAND_BLOCK)
                );
            case "broadcast":
                return Arrays.asList(
                    new ParameterField("message", "Сообщение", "Всем привет!", Material.BELL)
                );
            case "healPlayer":
                return Arrays.asList(
                    new ParameterField("amount", "Количество здоровья", "10.0", Material.GOLDEN_APPLE)
                );
            case "setGameMode":
                return Arrays.asList(
                    new ParameterField("mode", "Режим игры", "CREATIVE", Material.DIAMOND_SWORD)
                );
            case "explosion":
                return Arrays.asList(
                    new ParameterField("power", "Мощность", "4.0", Material.TNT),
                    new ParameterField("breakBlocks", "Ломать блоки", "true", Material.BARRIER)
                );
            case "setBlock":
                return Arrays.asList(
                    new ParameterField("material", "Материал", "STONE", Material.STONE),
                    new ParameterField("coords", "Координаты", "100 70 200", Material.COMPASS)
                );
            case "setVar":
            case "addVar":
            case "subVar":
            case "mulVar":
            case "divVar":
                return Arrays.asList(
                    new ParameterField("var", "Переменная", "myVar", Material.IRON_INGOT),
                    new ParameterField("value", "Значение", "0", Material.GOLD_INGOT)
                );
            case "setTime":
                return Arrays.asList(
                    new ParameterField("time", "Время", "0", Material.CLOCK)
                );
            case "setWeather":
                return Arrays.asList(
                    new ParameterField("weather", "Погода", "clear", Material.WATER_BUCKET)
                );
            case "spawnMob":
                return Arrays.asList(
                    new ParameterField("mob", "Моб", "ZOMBIE", Material.ZOMBIE_HEAD),
                    new ParameterField("amount", "Количество", "1", Material.HOPPER)
                );
            case "isInWorld":
                return Arrays.asList(
                    new ParameterField("world", "Мир", "world", Material.GLOBE_BANNER_PATTERN)
                );
            case "hasItem":
                return Arrays.asList(
                    new ParameterField("item", "Предмет", "DIAMOND", Material.STICK)
                );
            case "isNearBlock":
                return Arrays.asList(
                    new ParameterField("block", "Блок", "STONE", Material.GLASS),
                    new ParameterField("radius", "Радиус", "5", Material.COMPASS)
                );
            case "timeOfDay":
            case "worldTime":
                return Arrays.asList(
                    new ParameterField("timeRange", "Время дня", "DAY", Material.SUNFLOWER)
                );
            case "ifVar":
            case "ifNotVar":
                return Arrays.asList(
                    new ParameterField("var", "Переменная", "myVar", Material.OBSIDIAN),
                    new ParameterField("value", "Значение", "0", Material.GOLD_INGOT)
                );
            case "compareVariable":
                return Arrays.asList(
                    new ParameterField("var1", "Переменная 1", "var1", Material.IRON_INGOT),
                    new ParameterField("operator", "Оператор", ">", Material.COMPARATOR),
                    new ParameterField("var2", "Переменная 2", "var2", Material.GOLD_INGOT)
                );
            case "playerHealth":
                return Arrays.asList(
                    new ParameterField("health", "Здоровье", "10.0", Material.GOLDEN_APPLE),
                    new ParameterField("operator", "Оператор", ">", Material.COMPARATOR)
                );
            case "ifGameMode":
                return Arrays.asList(
                    new ParameterField("mode", "Режим", "SURVIVAL", Material.DIAMOND_SWORD)
                );
            case "ifWorldType":
                return Arrays.asList(
                    new ParameterField("type", "Тип", "NORMAL", Material.GRASS_BLOCK)
                );
            case "ifMobType":
                return Arrays.asList(
                    new ParameterField("mob", "Моб", "ZOMBIE", Material.SPAWNER)
                );
            case "ifMobNear":
                return Arrays.asList(
                    new ParameterField("radius", "Радиус", "5", Material.ENDER_EYE)
                );
            case "getVar":
                return Arrays.asList(
                    new ParameterField("var", "Переменная", "myVar", Material.BOOK)
                );
            case "setGlobalVar":
                return Arrays.asList(
                    new ParameterField("var", "Глобальная переменная", "money", Material.EMERALD),
                    new ParameterField("value", "Значение", "100", Material.GOLD_INGOT)
                );
            case "getGlobalVar":
                return Arrays.asList(
                    new ParameterField("var", "Глобальная переменная", "money", Material.EMERALD),
                    new ParameterField("localVar", "Локальная переменная", "temp", Material.BOOK)
                );
            case "setServerVar":
                return Arrays.asList(
                    new ParameterField("var", "Серверная переменная", "event_active", Material.BEACON),
                    new ParameterField("value", "Значение", "true", Material.GOLD_INGOT)
                );
            case "getServerVar":
                return Arrays.asList(
                    new ParameterField("var", "Серверная переменная", "event_active", Material.BEACON),
                    new ParameterField("localVar", "Локальная переменная", "temp", Material.BOOK)
                );
            case "wait":
                return List.of(new ParameterField("ticks", "Задержка (в тиках)", "20", Material.CLOCK));
            
            case "randomNumber":
                return List.of(
                    new ParameterField("min", "Мин. число", "1", Material.IRON_NUGGET),
                    new ParameterField("max", "Макс. число", "100", Material.GOLD_NUGGET),
                    new ParameterField("var", "Сохранить в переменную", "random_num", Material.NAME_TAG)
                );
            
            case "playParticle":
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
        // DEBUG: Логируем открытие GUI
        com.megacreative.MegaCreative.getInstance().getLogger().info("=== DEBUG: ОТКРЫТИЕ CodingParameterGUI ===");
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Игрок: " + player.getName());
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Действие: " + action);
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Инвентарь создан: " + (inventory != null));
        
        player.openInventory(inventory);
        
        // DEBUG: Проверяем, что инвентарь открылся
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Инвентарь открыт: " + (player.getOpenInventory().getTopInventory().equals(inventory)));
        
        // ИСПРАВЛЕНИЕ: Автоматически завершаем GUI для действий без параметров
        List<ParameterField> fields = getParameterFields(action);
        if (fields.isEmpty()) {
            com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Действие без параметров - автоматическое завершение");
            // Запускаем с небольшой задержкой, чтобы GUI успел открыться
            Bukkit.getScheduler().runTaskLater(com.megacreative.MegaCreative.getInstance(), () -> {
                if (!isCompleted) {
                    completeGUI();
                }
            }, 5L); // 5 тиков = 0.25 секунды
        } else {
            // ИСПРАВЛЕНИЕ: Автоматическое завершение для действий с параметрами через 10 секунд
            com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Действие с параметрами - автоматическое завершение через 10 секунд");
            Bukkit.getScheduler().runTaskLater(com.megacreative.MegaCreative.getInstance(), () -> {
                if (!isCompleted) {
                    com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Автоматическое завершение GUI по таймауту");
                    completeGUI();
                }
            }, 200L); // 200 тиков = 10 секунд
        }
    }
    
    /**
     * Завершает GUI и вызывает колбэк
     */
    private void completeGUI() {
        if (isCompleted) return; // Предотвращаем двойное завершение
        isCompleted = true;
        
        // DEBUG: Логируем завершение настройки
        com.megacreative.MegaCreative.getInstance().getLogger().info("=== DEBUG: ЗАВЕРШЕНИЕ CodingParameterGUI ===");
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Игрок: " + player.getName());
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Действие: " + action);
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: Параметры: " + parameters);
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: onComplete: " + (onComplete != null));
        
        if (onComplete != null) {
            onComplete.accept(parameters);
        }
        
        player.closeInventory();
        // Удаляем регистрацию GUI
        GuiListener.unregisterOpenGui(player);
        
        // DEBUG: Подтверждаем завершение
        com.megacreative.MegaCreative.getInstance().getLogger().info("DEBUG: CodingParameterGUI завершен успешно");
    }
    
    /**
     * Обновляет инвентарь с новыми данными
     */
    public void refresh() {
        setupInventory(); // Просто перестраиваем иконки с обновленными параметрами
    }

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
            completeGUI();
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
                
                // Проверяем, нужно ли использовать специальный GUI для этого параметра
                if (shouldUseSelectionGUI(field)) {
                    openSelectionGUI(field);
                } else {
                    // Открываем наковальню для ввода
                    new AnvilInputGUI(com.megacreative.MegaCreative.getInstance(), player, "Ввод параметра: " + field.getName(), (newValue) -> {
                        try {
                            // Этот код выполнится, когда игрок подтвердит ввод в наковальне
                            parameters.put(field.getKey(), newValue);
                            player.sendMessage("§a✅ Параметр '" + field.getName() + "' обновлен!");

                            // --- ИСПРАВЛЕНИЕ ---
                            // Обновляем и открываем ТЕКУЩЕЕ GUI, а не создаем новое
                            Bukkit.getScheduler().runTask(com.megacreative.MegaCreative.getInstance(), () -> {
                                this.refresh(); // Обновляем иконки в инвентаре
                                this.open();    // Показываем его игроку снова
                            });
                        } catch (Exception e) {
                            player.sendMessage("§c❌ Ошибка при обновлении параметра: " + e.getMessage());
                            com.megacreative.MegaCreative.getInstance().getLogger().warning("Ошибка в CodingParameterGUI: " + e.getMessage());
                        }
                    }, () -> {
                        // Callback для отмены
                        player.sendMessage("§c❌ Ввод параметра отменен");
                    });
                }
            } catch (Exception e) {
                player.sendMessage("§c❌ Ошибка при выборе параметра: " + e.getMessage());
                com.megacreative.MegaCreative.getInstance().getLogger().warning("Ошибка в CodingParameterGUI при выборе параметра: " + e.getMessage());
            }
        }
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
    
    /**
     * Проверяет, нужно ли использовать специальный GUI для выбора параметра
     */
    private boolean shouldUseSelectionGUI(ParameterField field) {
        String key = field.getKey().toLowerCase();
        return key.contains("gamemode") || key.contains("sound") || 
               key.contains("weather") || key.contains("effect") ||
               key.contains("particle") || key.contains("potion");
    }
    
    /**
     * Открывает специальный GUI для выбора параметра
     */
    private void openSelectionGUI(ParameterField field) {
        String key = field.getKey().toLowerCase();
        com.megacreative.MegaCreative plugin = com.megacreative.MegaCreative.getInstance();
        
        if (key.contains("gamemode")) {
            com.megacreative.gui.ParameterSelectionGUI.openGameModeSelection(plugin, player, (newValue) -> {
                parameters.put(field.getKey(), newValue);
                player.sendMessage("§a✅ Параметр '" + field.getName() + "' обновлен!");
                refreshAndReopen();
            });
        } else if (key.contains("sound")) {
            com.megacreative.gui.ParameterSelectionGUI.openSoundSelection(plugin, player, (newValue) -> {
                parameters.put(field.getKey(), newValue);
                player.sendMessage("§a✅ Параметр '" + field.getName() + "' обновлен!");
                refreshAndReopen();
            });
        } else if (key.contains("weather")) {
            com.megacreative.gui.ParameterSelectionGUI.openWeatherSelection(plugin, player, (newValue) -> {
                parameters.put(field.getKey(), newValue);
                player.sendMessage("§a✅ Параметр '" + field.getName() + "' обновлен!");
                refreshAndReopen();
            });
        } else if (key.contains("effect") || key.contains("potion")) {
            com.megacreative.gui.ParameterSelectionGUI.openEffectSelection(plugin, player, (newValue) -> {
                parameters.put(field.getKey(), newValue);
                player.sendMessage("§a✅ Параметр '" + field.getName() + "' обновлен!");
                refreshAndReopen();
            });
        }
    }
    
    /**
     * Обновляет GUI и показывает его игроку снова
     */
    private void refreshAndReopen() {
        Bukkit.getScheduler().runTask(com.megacreative.MegaCreative.getInstance(), () -> {
            this.refresh();
            this.open();
        });
    }
} 