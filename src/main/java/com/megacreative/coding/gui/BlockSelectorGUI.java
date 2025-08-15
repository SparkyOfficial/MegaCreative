package com.megacreative.coding.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GUI для вибору типу дії для блоку програмування
 */
public class BlockSelectorGUI implements Listener {

    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final CodeBlock codeBlock;
    private final Inventory inventory;

    // Статична карта відкритих інвентарів для обробки кліків
    private static final Map<UUID, BlockSelectorGUI> openInventories = new HashMap<>();

    /**
     * Конструктор
     * @param plugin Посилання на основний плагін
     * @param player Гравець, для якого відкривається GUI
     * @param blockLocation Розташування блоку програмування
     * @param codeBlock Блок коду
     */
    public BlockSelectorGUI(MegaCreative plugin, Player player, Location blockLocation, CodeBlock codeBlock) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.codeBlock = codeBlock;

        // Створюємо інвентар для вибору дії
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
     * Створює інвентар для вибору дії
     * @return Створений інвентар
     */
    private Inventory createInventory() {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Вибір дії для блоку");

        // Отримуємо можливі дії для матеріалу блоку
        Material blockMaterial = codeBlock.getMaterial();
        List<String> actions = plugin.getBlockConfiguration().getActionsForMaterial(blockMaterial);

        // Якщо дій немає, показуємо повідомлення
        if (actions.isEmpty()) {
            ItemStack noActions = new ItemBuilder(Material.BARRIER)
                .setName("§cНемає доступних дій")
                .setLore("§7Для цього блоку не налаштовано дій.")
                .build();
            inv.setItem(22, noActions);
            return inv;
        }

        // Заповнюємо інвентар доступними діями
        int slot = 0;
        for (String action : actions) {
            Material iconMaterial = getIconForAction(action);
            String displayName = getDisplayNameForAction(action);
            List<String> lore = getLoreForAction(action);

            ItemStack icon = new ItemBuilder(iconMaterial)
                .setName("§a" + displayName)
                .setLore(lore)
                .build();

            inv.setItem(slot, icon);
            slot++;

            if (slot >= 54) break; // Обмеження на розмір інвентаря
        }

        return inv;
    }

    /**
     * Отримує іконку для дії
     * @param action Назва дії
     * @return Матеріал для іконки
     */
    private Material getIconForAction(String action) {
        // Ви можете додати більше іконок для різних дій
        switch (action.toLowerCase()) {
            case "onjoin":
                return Material.ENDER_PEARL;
            case "onleave":
                return Material.ENDER_EYE;
            case "onchat":
                return Material.PAPER;
            case "onblockbreak":
                return Material.DIAMOND_PICKAXE;
            case "onblockplace":
                return Material.BRICKS;
            case "onplayermove":
                return Material.LEATHER_BOOTS;
            case "onplayerdeath":
                return Material.SKELETON_SKULL;
            case "oncommand":
                return Material.COMMAND_BLOCK;
            case "ontick":
                return Material.CLOCK;

            // Дії гравця
            case "sendmessage":
                return Material.WRITABLE_BOOK;
            case "teleport":
                return Material.ENDER_PEARL;
            case "giveitem":
                return Material.CHEST;
            case "playsound":
                return Material.NOTE_BLOCK;
            case "effect":
                return Material.POTION;
            case "command":
                return Material.COMMAND_BLOCK;
            case "broadcast":
                return Material.BELL;
            case "giveitems":
                return Material.CHEST;
            case "spawnentity":
                return Material.ZOMBIE_SPAWN_EGG;
            case "removeitems":
                return Material.BARRIER;
            case "setarmor":
                return Material.DIAMOND_CHESTPLATE;
            case "setvar":
                return Material.NAME_TAG;
            case "spawnmob":
                return Material.ZOMBIE_SPAWN_EGG;
            case "healplayer":
                return Material.GOLDEN_APPLE;
            case "setgamemode":
                return Material.BEDROCK;
            case "settime":
                return Material.CLOCK;
            case "setweather":
                return Material.WATER_BUCKET;
            case "explosion":
                return Material.TNT;
            case "setblock":
                return Material.STONE;
            case "getvar":
                return Material.NAME_TAG;
            case "getplayername":
                return Material.PLAYER_HEAD;
            case "setglobalvar":
                return Material.ENDER_CHEST;
            case "getglobalvar":
                return Material.ENDER_CHEST;
            case "setservervar":
                return Material.BEACON;
            case "getservervar":
                return Material.BEACON;
            case "wait":
                return Material.CLOCK;
            case "randomnumber":
                return Material.REDSTONE_TORCH;
            case "playparticle":
                return Material.FIREWORK_ROCKET;

            // Умови
            case "isop":
                return Material.DIAMOND;
            case "comparevariable":
                return Material.COMPASS;
            case "worldtime":
                return Material.CLOCK;
            case "isnearblock":
                return Material.STONE;
            case "mobnear":
                return Material.ZOMBIE_HEAD;
            case "playergamemode":
                return Material.BEDROCK;
            case "playerhealth":
                return Material.GOLDEN_APPLE;
            case "hasitem":
                return Material.CHEST;
            case "haspermission":
                return Material.PAPER;
            case "isinworld":
                return Material.GRASS_BLOCK;
            case "ifvarequals":
                return Material.COMPARATOR;
            case "ifvargreater":
                return Material.REDSTONE_TORCH;
            case "ifvarless":
                return Material.REDSTONE;
            case "isblocktype":
                return Material.STONE;
            case "isplayerholding":
                return Material.DIAMOND_SWORD;
            case "isnearentity":
                return Material.ZOMBIE_HEAD;
            case "hasarmor":
                return Material.DIAMOND_CHESTPLATE;
            case "isnight":
                return Material.BLACK_BED;
            case "isriding":
                return Material.SADDLE;

            // Цикли та функції
            case "repeat":
                return Material.REPEATER;
            case "repeattrigger":
                return Material.COMPARATOR;
            case "callfunction":
                return Material.BOOKSHELF;
            case "savefunction":
                return Material.BOOK;
            case "else":
                return Material.LEVER;

            // За замовчуванням
            default:
                return Material.STONE_BUTTON;
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

            // Умови
            case "isop":
                return "Є оператором";
            case "comparevariable":
                return "Порівняти змінні";
            case "worldtime":
                return "Час у світі";
            case "isnearblock":
                return "Поблизу блоку";
            case "mobnear":
                return "Моб поблизу";
            case "playergamemode":
                return "Режим гри гравця";
            case "playerhealth":
                return "Здоров'я гравця";
            case "hasitem":
                return "Має предмет";
            case "haspermission":
                return "Має дозвіл";
            case "isinworld":
                return "Знаходиться у світі";
            case "ifvarequals":
                return "Якщо змінна дорівнює";
            case "ifvargreater":
                return "Якщо змінна більше";
            case "ifvarless":
                return "Якщо змінна менше";
            case "isblocktype":
                return "Блок певного типу";
            case "isplayerholding":
                return "Гравець тримає";
            case "isnearentity":
                return "Істота поблизу";
            case "hasarmor":
                return "Має броню";
            case "isnight":
                return "Зараз ніч";
            case "isriding":
                return "Верхи на істоті";

            // Цикли та функції
            case "repeat":
                return "Повторити N разів";
            case "repeattrigger":
                return "Періодичний тригер";
            case "callfunction":
                return "Викликати функцію";
            case "savefunction":
                return "Зберегти функцію";
            case "else":
                return "Інакше";

            // За замовчуванням
            default:
                return action;
        }
    }

    /**
     * Отримує опис для дії
     * @param action Назва дії
     * @return Список рядків опису
     */
    private List<String> getLoreForAction(String action) {
        // Ви можете додати докладні описи для кожної дії
        switch (action.toLowerCase()) {
            // Події
            case "onjoin":
                return List.of(
                    "§7Запускається, коли гравець приєднується до світу.",
                    "§7Використовуйте для привітання або",
                    "§7початкових налаштувань гравця.",
                    "",
                    "§eНатисніть, щоб вибрати"
                );
            case "onleave":
                return List.of(
                    "§7Запускається, коли гравець виходить зі світу.",
                    "§7Використовуйте для збереження даних",
                    "§7або сповіщення інших гравців.",
                    "",
                    "§eНатисніть, щоб вибрати"
                );
            case "sendmessage":
                return List.of(
                    "§7Відправляє повідомлення гравцю.",
                    "§7Можна використовувати кольорові коди:",
                    "§7&a - зелений, &c - червоний, і т.д.",
                    "§7Плейсхолдери: %player%, %world%",
                    "",
                    "§eНатисніть, щоб вибрати"
                );
            case "teleport":
                return List.of(
                    "§7Телепортує гравця у вказане місце.",
                    "§7Можна вказати координати або",
                    "§7вибрати інший світ.",
                    "",
                    "§eНатисніть, щоб вибрати"
                );

            // За замовчуванням повертаємо базовий опис
            default:
                return List.of(
                    "§7Дія: " + action,
                    "§7Натисніть, щоб вибрати цю дію",
                    "§7для блоку програмування.",
                    "",
                    "§eНатисніть, щоб вибрати"
                );
        }
    }

    /**
     * Відкриває GUI для гравця
     */
    public void open() {
        player.openInventory(inventory);
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
        BlockSelectorGUI gui = openInventories.get(clickedPlayer.getUniqueId());
        if (gui == null || !event.getInventory().equals(gui.inventory)) {
            return;
        }

        event.setCancelled(true); // Відміняємо клік

        // Перевіряємо, чи клікнуто по предмету
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        ItemStack clickedItem = event.getInventory().getItem(slot);
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        // Якщо це бар'єр (немає доступних дій), то виходимо
        if (clickedItem.getType() == Material.BARRIER) {
            clickedPlayer.closeInventory();
            return;
        }

        // Отримуємо дію з списку за індексом слота
        List<String> actions = plugin.getBlockConfiguration().getActionsForMaterial(gui.codeBlock.getMaterial());
        if (slot >= 0 && slot < actions.size()) {
            String selectedAction = actions.get(slot);

            // Оновлюємо блок коду та зберігаємо через менеджер світу
            gui.codeBlock.setAction(selectedAction);
            com.megacreative.models.CreativeWorld world = plugin.getWorldManager().findCreativeWorldByBukkit(gui.blockLocation.getWorld());
            if (world != null) {
                String key = String.format("%s,%.0f,%.0f,%.0f",
                        gui.blockLocation.getWorld().getName(),
                        gui.blockLocation.getX(),
                        gui.blockLocation.getY(),
                        gui.blockLocation.getZ());
                world.getDevWorldBlocks().put(key, gui.codeBlock);
                plugin.getWorldManager().saveWorld(world);
            }

            // Закриваємо інвентар
            clickedPlayer.closeInventory();

            // Відкриваємо інвентар налаштування, якщо дія потребує налаштування
            if (plugin.getBlockConfiguration().hasActionConfig(selectedAction)) {
                // Відкладаємо відкриття на 1 тік, щоб уникнути проблем з інвентарем
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    new BlockConfigurationGUI(plugin, clickedPlayer, gui.blockLocation, gui.codeBlock).open();
                }, 1L);
            } else {
                clickedPlayer.sendMessage("§a✓ Дію '" + selectedAction + "' вибрано для блоку!");
            }
        }
    }
}
