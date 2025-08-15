package com.megacreative.coding.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockConfiguration;
import com.megacreative.coding.CodeBlock;
import com.megacreative.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUI для налаштування параметрів блоку програмування
 */
public class BlockConfigurationGUI implements Listener {

    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final CodeBlock codeBlock;
    private final Inventory inventory;

    // Слоти для спеціальних дій у інвентарі
    private static final int SAVE_SLOT = 49;
    private static final int CANCEL_SLOT = 48;
    private static final int INFO_SLOT = 45;

    // Статична карта відкритих інвентарів для обробки кліків
    private static final Map<UUID, BlockConfigurationGUI> openInventories = new HashMap<>();

    /**
     * Конструктор
     * @param plugin Посилання на основний плагін
     * @param player Гравець, для якого відкривається GUI
     * @param blockLocation Розташування блоку програмування
     * @param codeBlock Блок коду
     */
    public BlockConfigurationGUI(MegaCreative plugin, Player player, Location blockLocation, CodeBlock codeBlock) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.codeBlock = codeBlock;

        // Створюємо інвентар для налаштування
        this.inventory = createInventory();

        // Реєструємо GUI для обробки кліків
        openInventories.put(player.getUniqueId(), this);

        // Реєструємо слухача подій, якщо ще не зареєстрований
        if (!isListenerRegistered()) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    /**
     * Перевіряє, чи зареєстрований слухач подій
     */
    private boolean isListenerRegistered() {
        try {
            // Це хак для перевірки, чи зареєстрований слухач
            return Bukkit.getPluginManager().isPluginEnabled(plugin.getName()) &&
                   Bukkit.getPluginManager().getPlugin(plugin.getName()).equals(plugin);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Створює інвентар для налаштування блоку
     * @return Створений інвентар
     */
    private Inventory createInventory() {
        String actionName = getDisplayNameForAction(codeBlock.getAction());
        Inventory inv = Bukkit.createInventory(null, 54, "§8Налаштування: " + actionName);

        // Заповнюємо інвентар елементами налаштування
        fillConfigurationItems(inv);

        // Додаємо кнопки керування в нижньому ряду
        addControlButtons(inv);

        return inv;
    }

    /**
     * Заповнює інвентар елементами налаштування
     * @param inv Інвентар для заповнення
     */
    private void fillConfigurationItems(Inventory inv) {
        BlockConfiguration.ActionConfig actionConfig = plugin.getBlockConfiguration().getActionConfig(codeBlock.getAction());

        if (actionConfig == null) {
            // Якщо немає конфігурації, показуємо базове повідомлення
            ItemStack noConfig = new ItemBuilder(Material.PAPER)
                .setName("§eІнформація")
                .setLore(
                    "§7Ця дія не потребує додаткових налаштувань.",
                    "§7Просто з'єднайте її з іншими блоками."
                )
                .build();
            inv.setItem(22, noConfig);
            return;
        }

        // Перевіряємо наявність слотів
        if (actionConfig.hasSlots()) {
            // Обробляємо іменовані слоти
            for (Map.Entry<Integer, BlockConfiguration.SlotConfig> entry : actionConfig.getSlots().entrySet()) {
                int slot = entry.getKey();
                BlockConfiguration.SlotConfig slotConfig = entry.getValue();

                // Обмежуємо слоти до розміру інвентаря
                if (slot >= 45) continue;

                // Створюємо базовий предмет для слоту
                Material placeholderMaterial = Material.getMaterial(slotConfig.getPlaceholderItem());
                if (placeholderMaterial == null) placeholderMaterial = Material.STONE_BUTTON;

                ItemStack placeholderItem = new ItemBuilder(placeholderMaterial)
                    .setName(slotConfig.getName())
                    .setLore(slotConfig.getDescription().split("\\n"))
                    .build();

                // Якщо є предмет у цьому слоті, показуємо його замість плейсхолдера
                ItemStack existingItem = codeBlock.getConfigItem(slot);
                if (existingItem != null) {
                    inv.setItem(slot, existingItem);
                } else {
                    inv.setItem(slot, placeholderItem);
                }
            }
        }

        // Перевіряємо наявність груп предметів
        if (actionConfig.hasGroups()) {
            for (Map.Entry<String, BlockConfiguration.GroupConfig> entry : actionConfig.getGroups().entrySet()) {
                String groupName = entry.getKey();
                BlockConfiguration.GroupConfig groupConfig = entry.getValue();

                // Для кожного слоту в групі
                for (int i = 0; i < groupConfig.getSlots().size(); i++) {
                    int slot = groupConfig.getSlots().get(i);

                    // Обмежуємо слоти до розміру інвентаря
                    if (slot >= 45) continue;

                    // Створюємо базовий предмет для слоту групи
                    Material placeholderMaterial = Material.getMaterial(groupConfig.getPlaceholderItem());
                    if (placeholderMaterial == null) placeholderMaterial = Material.CHEST;

                    ItemStack placeholderItem = new ItemBuilder(placeholderMaterial)
                        .setName(groupConfig.getName() + " #" + (i + 1))
                        .setLore(groupConfig.getDescription().split("\\n"))
                        .addLore("", "§7Слот групи: §f" + groupName)
                        .build();

                    // Якщо є предмет у цьому слоті, показуємо його замість плейсхолдера
                    ItemStack existingItem = codeBlock.getConfigItem(slot);
                    if (existingItem != null) {
                        inv.setItem(slot, existingItem);
                    } else {
                        inv.setItem(slot, placeholderItem);
                    }
                }
            }
        }
    }

    /**
     * Додає кнопки керування до інвентаря
     * @param inv Інвентар для заповнення
     */
    private void addControlButtons(Inventory inv) {
        // Кнопка інформації
        ItemStack infoButton = new ItemBuilder(Material.BOOK)
            .setName("§bІнформація про дію")
            .setLore(
                "§7Дія: §f" + codeBlock.getAction(),
                "§7Тип блоку: §f" + codeBlock.getMaterial().name(),
                "",
                "§7Ця панель дозволяє налаштувати",
                "§7параметри для блоку програмування.",
                "§7Розмістіть предмети у відповідні слоти,",
                "§7щоб налаштувати поведінку блоку."
            )
            .build();
        inv.setItem(INFO_SLOT, infoButton);

        // Кнопка скасування
        ItemStack cancelButton = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
            .setName("§cСкасувати")
            .setLore(
                "§7Натисніть, щоб закрити меню",
                "§7без збереження змін."
            )
            .build();
        inv.setItem(CANCEL_SLOT, cancelButton);

        // Кнопка збереження
        ItemStack saveButton = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
            .setName("§aЗберегти")
            .setLore(
                "§7Натисніть, щоб зберегти всі",
                "§7налаштування для цього блоку."
            )
            .build();
        inv.setItem(SAVE_SLOT, saveButton);

        // Заповнюємо проміжки склом
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").build();
        for (int i = 45; i < 54; i++) {
            if (i != INFO_SLOT && i != CANCEL_SLOT && i != SAVE_SLOT && inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }

    /**
     * Отримує відображуване ім'я для дії
     * @param action Назва дії
     * @return Відображуване ім'я
     */
    private String getDisplayNameForAction(String action) {
        // Ви можете налаштувати красиві назви для дій
        switch (action.toLowerCase()) {
            // Події
            case "onjoin":
                return "При приєднанні гравця";
            case "onleave":
                return "При виході гравця";
            case "onchat":
                return "При повідомленні в чаті";
            case "onblockbreak":
                return "При знищенні блоку";
            case "onblockplace":
                return "При розміщенні блоку";
            case "onplayermove":
                return "При русі гравця";
            case "onplayerdeath":
                return "При смерті гравця";
            case "oncommand":
                return "При виконанні команди";
            case "ontick":
                return "Періодично";

            // Дії гравця
            case "sendmessage":
                return "Відправити повідомлення";
            case "teleport":
                return "Телепортувати гравця";
            case "giveitem":
                return "Видати предмет";
            case "playsound":
                return "Відтворити звук";
            case "effect":
                return "Накласти ефект";
            case "command":
                return "Виконати команду";
            case "broadcast":
                return "Оголосити всім";
            case "giveitems":
                return "Видати предмети";
            case "spawnentity":
                return "Створити істоту";
            case "removeitems":
                return "Видалити предмети";
            case "setarmor":
                return "Одягнути броню";
            case "setvar":
                return "Встановити змінну";
            case "spawnmob":
                return "Створити моба";
            case "healplayer":
                return "Вилікувати гравця";
            case "setgamemode":
                return "Змінити режим гри";
            case "settime":
                return "Встановити час";
            case "setweather":
                return "Змінити погоду";
            case "explosion":
                return "Створити вибух";
            case "setblock":
                return "Встановити блок";
            case "getvar":
                return "Отримати змінну";
            case "getplayername":
                return "Отримати ім'я гравця";
            case "setglobalvar":
                return "Встановити глобальну змінну";
            case "getglobalvar":
                return "Отримати глобальну змінну";
            case "setservervar":
                return "Встановити серверну змінну";
            case "getservervar":
                return "Отримати серверну змінну";
            case "wait":
                return "Зачекати";
            case "randomnumber":
                return "Випадкове число";
            case "playparticle":
                return "Показати частинки";

            // За замовчуванням
            default:
                return action;
        }
    }

    /**
     * Відкриває GUI для гравця
     */
    public void open() {
        player.openInventory(inventory);
    }

    /**
     * Зберігає конфігурацію блоку з інвентаря
     */
    private void saveConfiguration() {
        // Очищаємо поточні предмети конфігурації
        codeBlock.clearConfigItems();

        // Отримуємо конфігурацію дії
        BlockConfiguration.ActionConfig actionConfig = plugin.getBlockConfiguration().getActionConfig(codeBlock.getAction());

        if (actionConfig != null) {
            // Якщо є слоти, зберігаємо предмети з них
            if (actionConfig.hasSlots()) {
                for (int slot : actionConfig.getSlots().keySet()) {
                    if (slot >= 45) continue; // Пропускаємо слоти нижнього ряду

                    ItemStack item = inventory.getItem(slot);
                    if (item != null && !isPlaceholderItem(item, actionConfig.getSlots().get(slot))) {
                        codeBlock.setConfigItem(slot, item);
                    }
                }
            }

            // Якщо є групи, зберігаємо предмети з них
            if (actionConfig.hasGroups()) {
                for (BlockConfiguration.GroupConfig groupConfig : actionConfig.getGroups().values()) {
                    for (int slot : groupConfig.getSlots()) {
                        if (slot >= 45) continue; // Пропускаємо слоти нижнього ряду

                        ItemStack item = inventory.getItem(slot);
                        if (item != null && !isGroupPlaceholderItem(item, groupConfig)) {
                            codeBlock.setConfigItem(slot, item);
                        }
                    }
                }
            }
        } else {
            // Якщо немає конфігурації, зберігаємо всі предмети з верхньої частини інвентаря
            for (int slot = 0; slot < 45; slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item != null && item.getType() != Material.PAPER) {
                    codeBlock.setConfigItem(slot, item);
                }
            }
        }

        // Оновлюємо блок у менеджері
        // Сохранение блока выполняется через менеджер миров, а не через CodingManager
        com.megacreative.models.CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(blockLocation.getWorld());
        if (world != null) {
            String key = String.format("%s,%.0f,%.0f,%.0f",
                    blockLocation.getWorld().getName(),
                    blockLocation.getX(),
                    blockLocation.getY(),
                    blockLocation.getZ());
            world.getDevWorldBlocks().put(key, codeBlock);
            plugin.getWorldManager().saveWorld(world);
        }
    }

    /**
     * Перевіряє, чи є предмет плейсхолдером для слоту
     * @param item Предмет для перевірки
     * @param slotConfig Конфігурація слоту
     * @return true, якщо це плейсхолдер
     */
    private boolean isPlaceholderItem(ItemStack item, BlockConfiguration.SlotConfig slotConfig) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        String displayName = item.getItemMeta().getDisplayName();
        return displayName.equals(slotConfig.getName());
    }

    /**
     * Перевіряє, чи є предмет плейсхолдером для групи
     * @param item Предмет для перевірки
     * @param groupConfig Конфігурація групи
     * @return true, якщо це плейсхолдер
     */
    private boolean isGroupPlaceholderItem(ItemStack item, BlockConfiguration.GroupConfig groupConfig) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        String displayName = item.getItemMeta().getDisplayName();
        return displayName.startsWith(groupConfig.getName());
    }

    /**
     * Обробляє клік у інвентарі
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player clickedPlayer)) {
            return;
        }

        // Перевіряємо, чи це наш інвентар
        BlockConfigurationGUI gui = openInventories.get(clickedPlayer.getUniqueId());
        if (gui == null || !event.getInventory().equals(gui.inventory)) {
            return;
        }

        int slot = event.getRawSlot();

        // Якщо клік у нижньому ряду, обробляємо спеціальні кнопки
        if (slot >= 45 && slot < 54) {
            event.setCancelled(true); // Відміняємо клік

            // Кнопка скасування
            if (slot == CANCEL_SLOT) {
                clickedPlayer.closeInventory();
                return;
            }

            // Кнопка збереження
            if (slot == SAVE_SLOT) {
                gui.saveConfiguration();
                clickedPlayer.closeInventory();
                clickedPlayer.sendMessage("§a✓ Налаштування блоку збережено!");
                return;
            }

            return;
        }

        // Для верхньої частини інвентаря дозволяємо перетягувати предмети
        // але забороняємо взаємодію з плейсхолдерами
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null && (isActionPlaceholder(currentItem) || isBlackGlassPane(currentItem))) {
            event.setCancelled(true);
        }
    }

    /**
     * Перевіряє, чи є предмет плейсхолдером для дії
     * @param item Предмет для перевірки
     * @return true, якщо це плейсхолдер
     */
    private boolean isActionPlaceholder(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        // Перевіряємо, чи це предмет з особливим форматуванням кольорів
        String displayName = item.getItemMeta().getDisplayName();
        return displayName.startsWith("§a") || displayName.startsWith("§b") || 
               displayName.startsWith("§e") || displayName.startsWith("§c");
    }

    /**
     * Перевіряє, чи є предмет чорним склом-розділювачем
     * @param item Предмет для перевірки
     * @return true, якщо це чорне скло
     */
    private boolean isBlackGlassPane(ItemStack item) {
        return item != null && item.getType() == Material.BLACK_STAINED_GLASS_PANE;
    }

    /**
     * Обробляє закриття інвентаря
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        // Видаляємо GUI з карти відкритих інвентарів
        BlockConfigurationGUI gui = openInventories.remove(player.getUniqueId());
        if (gui != null && event.getInventory().equals(gui.inventory)) {
            // Запитуємо гравця, чи хоче він зберегти зміни
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Перевіряємо, що гравець не відкрив новий інвентар
                if (player.isOnline() && player.getOpenInventory().getTopInventory().getSize() == 0) {
                    player.sendMessage("§eНалаштування блоку не збережено. Клікніть правою кнопкою миші по блоку, щоб налаштувати його знову.");
                }
            }, 1L);
        }
    }
}
