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
import java.util.Map;

/**
 * GUI for selecting specific actions/events/conditions within a category
 * 
 * @author Андрій Будильников
 */
public class ActionSelectionGUI {
    private static final int INVENTORY_SIZE = 54;
    private static final String INVENTORY_TITLE = ChatColor.DARK_PURPLE + "Выбор действия";
    
    private final MegaCreative plugin;
    private final Player player;
    private final CodeBlock codeBlock;
    private final String category;
    private final Inventory inventory;
    
    public ActionSelectionGUI(MegaCreative plugin, Player player, CodeBlock codeBlock, String category) {
        this.plugin = plugin;
        this.player = player;
        this.codeBlock = codeBlock;
        this.category = category;
        this.inventory = Bukkit.createInventory(null, INVENTORY_SIZE, 
            INVENTORY_TITLE + " - " + category);
        setupGUI();
    }
    
    /**
     * Sets up the GUI with actions/events/conditions based on category
     */
    private void setupGUI() {
        inventory.clear();
        
        // Add items based on category
        switch (category) {
            case "Игрок":
                addPlayerItems();
                break;
            case "Мир":
                addWorldItems();
                break;
            case "Сервер":
                addServerItems();
                break;
            case "Блоки":
                addBlockItems();
                break;
            case "Существа":
                addEntityItems();
                break;
            case "Предметы":
                addItemItems();
                break;
            case "Чат":
                addChatItems();
                break;
            case "Звуки":
                addSoundItems();
                break;
            case "Телепортация":
                addTeleportItems();
                break;
            case "Время":
                addTimeItems();
                break;
            case "Переменные":
                addVariableItems();
                break;
            case "Циклы":
                addLoopItems();
                break;
            case "Условия":
                addConditionalItems();
                break;
            case "Функции":
                addFunctionItems();
                break;
            case "Пользовательские":
                addCustomFunctionItems();
                break;
            case "Системные":
                addSystemFunctionItems();
                break;
            case "Скорборд":
                addScoreboardItems();
                break;
            case "Команды":
                addTeamItems();
                break;
            case "Локации":
                addLocationItems();
                break;
            case "Экономика":
                addEconomyItems();
                break;
            case "Интеграция":
                addIntegrationItems();
                break;
            case "Права":
                addPermissionItems();
                break;
            case "Инвентарь":
                addInventoryItems();
                break;
            case "Статистика":
                addStatsItems();
                break;
            case "Онлайн":
                addOnlineItems();
                break;
            case "Погода":
                addWeatherItems();
                break;
            case "Регионы":
                addRegionItems();
                break;
            case "Тайминг":
                addTimingItems();
                break;
            case "Структуры данных":
                addDataStructureItems();
                break;
            case "Общие":
            default:
                addGeneralItems();
                break;
        }
        
        // Add back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Назад");
        backItem.setItemMeta(backMeta);
        inventory.setItem(45, backItem);
        
        // Add close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Закрыть");
        closeItem.setItemMeta(closeMeta);
        inventory.setItem(53, closeItem);
    }
    
    /**
     * Adds player-related items
     */
    private void addPlayerItems() {
        addItem(0, Material.PLAYER_HEAD, "onJoin", ChatColor.AQUA + "Игрок заходит", 
                "Событие: когда игрок заходит на сервер");
        addItem(1, Material.PLAYER_HEAD, "onLeave", ChatColor.AQUA + "Игрок выходит", 
                "Событие: когда игрок выходит с сервера");
        addItem(2, Material.WRITABLE_BOOK, "sendMessage", ChatColor.GREEN + "Отправить сообщение", 
                "Действие: отправить сообщение игроку");
        addItem(3, Material.GOLDEN_APPLE, "healPlayer", ChatColor.GREEN + "Исцелить игрока", 
                "Действие: исцелить игрока");
        addItem(4, Material.COMMAND_BLOCK, "setGameMode", ChatColor.GREEN + "Установить режим игры", 
                "Действие: установить режим игры игроку");
        addItem(5, Material.NAME_TAG, "getPlayerName", ChatColor.GREEN + "Получить имя игрока", 
                "Действие: получить имя игрока");
        addItem(6, Material.LEATHER_BOOTS, "onPlayerMove", ChatColor.AQUA + "Игрок двигается", 
                "Событие: когда игрок двигается");
        addItem(7, Material.SKELETON_SKULL, "onPlayerDeath", ChatColor.AQUA + "Игрок умер", 
                "Событие: когда игрок умирает");
        addItem(8, Material.STONE_BUTTON, "onPlayerInteract", ChatColor.AQUA + "Игрок взаимодействует", 
                "Событие: когда игрок взаимодействует");
        addItem(9, Material.EXPERIENCE_BOTTLE, "setExperience", ChatColor.GREEN + "Установить опыт", 
                "Действие: установить опыт игроку");
        addItem(10, Material.DIAMOND_CHESTPLATE, "setArmor", ChatColor.GREEN + "Установить броню", 
                "Действие: установить броню игроку");
    }
    
    /**
     * Adds world-related items
     */
    private void addWorldItems() {
        addItem(0, Material.GRASS_BLOCK, "onBlockBreak", ChatColor.AQUA + "Блок сломан", 
                "Событие: когда блок ломается");
        addItem(1, Material.COBBLESTONE, "onBlockPlace", ChatColor.AQUA + "Блок поставлен", 
                "Событие: когда блок ставится");
        addItem(2, Material.CLOCK, "setTime", ChatColor.GREEN + "Установить время", 
                "Действие: установить время в мире");
        addItem(3, Material.WATER_BUCKET, "setWeather", ChatColor.GREEN + "Установить погоду", 
                "Действие: установить погоду в мире");
        addItem(4, Material.TNT, "explosion", ChatColor.GREEN + "Создать взрыв", 
                "Действие: создать взрыв в мире");
        addItem(5, Material.BEDROCK, "setBlock", ChatColor.GREEN + "Установить блок", 
                "Действие: установить блок");
        addItem(6, Material.REDSTONE_BLOCK, "onTick", ChatColor.AQUA + "Тик сервера", 
                "Событие: каждый тик сервера");
        addItem(7, Material.COMMAND_BLOCK, "onCommand", ChatColor.AQUA + "Команда выполнена", 
                "Событие: когда команда выполнена");
    }
    
    /**
     * Adds server-related items
     */
    private void addServerItems() {
        addItem(0, Material.COMMAND_BLOCK, "onCommand", ChatColor.AQUA + "Команда выполнена", 
                "Событие: когда команда выполнена");
        addItem(1, Material.REDSTONE_BLOCK, "onTick", ChatColor.AQUA + "Тик сервера", 
                "Событие: каждый тик сервера");
        addItem(2, Material.PLAYER_HEAD, "broadcast", ChatColor.GREEN + "Объявление", 
                "Действие: отправить объявление всем игрокам");
        addItem(3, Material.COMMAND_BLOCK, "onPlayerLevelUp", ChatColor.AQUA + "Игрок повысил уровень", 
                "Событие: когда игрок повышает уровень");
        addItem(4, Material.REDSTONE_TORCH, "onServerTPS", ChatColor.AQUA + "TPS сервера", 
                "Событие: когда TPS сервера изменяется");
    }
    
    /**
     * Adds block-related items
     */
    private void addBlockItems() {
        addItem(0, Material.COBBLESTONE, "onBlockBreak", ChatColor.AQUA + "Блок сломан", 
                "Событие: когда блок ломается");
        addItem(1, Material.STONE, "onBlockPlace", ChatColor.AQUA + "Блок поставлен", 
                "Событие: когда блок ставится");
        addItem(2, Material.BEDROCK, "setBlock", ChatColor.GREEN + "Установить блок", 
                "Действие: установить блок");
    }
    
    /**
     * Adds entity-related items
     */
    private void addEntityItems() {
        addItem(0, Material.ZOMBIE_HEAD, "onPlayerDeath", ChatColor.AQUA + "Игрок умер", 
                "Событие: когда игрок умирает");
        addItem(0, Material.ZOMBIE_SPAWN_EGG, "spawnEntity", ChatColor.GREEN + "Создать существо", 
                "Действие: создать существо");
        addItem(1, Material.SKELETON_SKULL, "spawnMob", ChatColor.GREEN + "Создать моба", 
                "Действие: создать моба");
        addItem(2, Material.ZOMBIE_HEAD, "mobNear", ChatColor.RED + "Моб рядом", 
                "Условие: проверить, рядом ли моб");
        addItem(3, Material.ZOMBIE_HEAD, "isNearEntity", ChatColor.RED + "Рядом с существом", 
                "Условие: проверить, рядом ли существо");
    }
    
    /**
     * Adds item-related items
     */
    private void addItemItems() {
        addItem(0, Material.CHEST, "giveItem", ChatColor.GREEN + "Выдать предмет", 
                "Действие: выдать предмет игроку");
        addItem(1, Material.BARRIER, "removeItems", ChatColor.GREEN + "Удалить предметы", 
                "Действие: удалить предметы у игрока");
        addItem(2, Material.DIAMOND_CHESTPLATE, "setArmor", ChatColor.GREEN + "Установить броню", 
                "Действие: установить броню игроку");
        addItem(3, Material.DIAMOND, "hasItem", ChatColor.RED + "Есть предмет", 
                "Условие: проверить, есть ли предмет у игрока");
        addItem(4, Material.CHEST, "giveItems", ChatColor.GREEN + "Выдать предметы", 
                "Действие: выдать предметы игроку");
        addItem(5, Material.DIAMOND, "isPlayerHolding", ChatColor.RED + "Держит предмет", 
                "Условие: проверить, держит ли игрок предмет");
        addItem(6, Material.CHEST, "checkPlayerInventory", ChatColor.RED + "Инвентарь игрока", 
                "Условие: проверить инвентарь игрока");
    }
    
    /**
     * Adds chat-related items
     */
    private void addChatItems() {
        addItem(0, Material.WRITABLE_BOOK, "onChat", ChatColor.AQUA + "Сообщение в чате", 
                "Событие: когда игрок пишет в чат");
        addItem(1, Material.WRITABLE_BOOK, "sendMessage", ChatColor.GREEN + "Отправить сообщение", 
                "Действие: отправить сообщение игроку");
        addItem(2, Material.PLAYER_HEAD, "broadcast", ChatColor.GREEN + "Объявление", 
                "Действие: отправить объявление всем игрокам");
        addItem(3, Material.NAME_TAG, "sendTitle", ChatColor.GREEN + "Отправить заголовок", 
                "Действие: отправить заголовок игроку");
        addItem(4, Material.PAPER, "sendActionBar", ChatColor.GREEN + "Отправить ActionBar", 
                "Действие: отправить сообщение в ActionBar");
        addItem(5, Material.NAME_TAG, "sendCustomTitle", ChatColor.GREEN + "Отправить заголовок", 
                "Действие: отправить пользовательский заголовок");
    }
    
    /**
     * Adds sound-related items
     */
    private void addSoundItems() {
        addItem(0, Material.NOTE_BLOCK, "playSound", ChatColor.GREEN + "Проиграть звук", 
                "Действие: проиграть звук");
        addItem(1, Material.FIREWORK_ROCKET, "playParticle", ChatColor.GREEN + "Проиграть частицы", 
                "Действие: проиграть частицы");
        addItem(2, Material.NOTE_BLOCK, "playCustomSound", ChatColor.GREEN + "Проиграть звук", 
                "Действие: проиграть пользовательский звук");
        addItem(3, Material.FIREWORK_ROCKET, "spawnParticleEffect", ChatColor.GREEN + "Проиграть частицы", 
                "Действие: создать эффект частиц");
    }
    
    /**
     * Adds teleportation-related items
     */
    private void addTeleportItems() {
        addItem(0, Material.ENDER_PEARL, "teleport", ChatColor.GREEN + "Телепортировать", 
                "Действие: телепортировать игрока");
        addItem(1, Material.COMPASS, "teleportToLocation", ChatColor.GREEN + "Телепорт к локации", 
                "Действие: телепортировать к сохраненной локации");
        addItem(2, Material.COMPASS, "saveLocation", ChatColor.GREEN + "Сохранить локацию", 
                "Действие: сохранить текущую локацию");
        addItem(3, Material.COMPASS, "getLocation", ChatColor.GREEN + "Получить локацию", 
                "Действие: получить сохраненную локацию");
    }
    
    /**
     * Adds time-related items
     */
    private void addTimeItems() {
        addItem(0, Material.CLOCK, "worldTime", ChatColor.RED + "Время мира", 
                "Условие: проверить время мира");
        addItem(1, Material.CLOCK, "setTime", ChatColor.GREEN + "Установить время", 
                "Действие: установить время в мире");
        addItem(2, Material.CLOCK, "isNight", ChatColor.RED + "Ночь", 
                "Условие: проверить, ночь ли сейчас");
    }
    
    /**
     * Adds variable-related items
     */
    private void addVariableItems() {
        addItem(0, Material.REDSTONE, "setVar", ChatColor.GREEN + "Установить переменную", 
                "Действие: установить значение переменной");
        addItem(1, Material.REDSTONE_TORCH, "getVar", ChatColor.GREEN + "Получить переменную", 
                "Действие: получить значение переменной");
        addItem(2, Material.COMPARATOR, "compareVariable", ChatColor.RED + "Сравнить переменные", 
                "Условие: сравнить две переменные");
        addItem(3, Material.REPEATER, "ifVarEquals", ChatColor.RED + "Если переменная равна", 
                "Условие: если переменная равна значению");
        addItem(4, Material.REPEATER, "ifVarGreater", ChatColor.RED + "Если переменная больше", 
                "Условие: если переменная больше значения");
        addItem(5, Material.REPEATER, "ifVarLess", ChatColor.RED + "Если переменная меньше", 
                "Условие: если переменная меньше значения");
        addItem(6, Material.REDSTONE, "addVar", ChatColor.GREEN + "Добавить к переменной", 
                "Действие: добавить значение к переменной");
        addItem(7, Material.REDSTONE, "subVar", ChatColor.GREEN + "Вычесть из переменной", 
                "Действие: вычесть значение из переменной");
        addItem(8, Material.REDSTONE, "mulVar", ChatColor.GREEN + "Умножить переменную", 
                "Действие: умножить переменную на значение");
        addItem(9, Material.REDSTONE, "divVar", ChatColor.GREEN + "Разделить переменную", 
                "Действие: разделить переменную на значение");
        addItem(10, Material.REDSTONE_BLOCK, "setGlobalVar", ChatColor.GREEN + "Установить глобальную переменную", 
                "Действие: установить значение глобальной переменной");
        addItem(11, Material.REDSTONE_TORCH, "getGlobalVar", ChatColor.GREEN + "Получить глобальную переменную", 
                "Действие: получить значение глобальной переменной");
        addItem(12, Material.COMMAND_BLOCK, "setServerVar", ChatColor.GREEN + "Установить серверную переменную", 
                "Действие: установить значение серверной переменной");
        addItem(13, Material.COMMAND_BLOCK, "getServerVar", ChatColor.GREEN + "Получить серверную переменную", 
                "Действие: получить значение серверной переменной");
        addItem(14, Material.BOOK, "createList", ChatColor.GREEN + "Создать список", 
                "Действие: создать список");
        addItem(15, Material.WRITABLE_BOOK, "listOperation", ChatColor.GREEN + "Операция со списком", 
                "Действие: выполнить операцию со списком");
        addItem(16, Material.MAP, "createMap", ChatColor.GREEN + "Создать карту", 
                "Действие: создать карту");
        addItem(17, Material.FILLED_MAP, "mapOperation", ChatColor.GREEN + "Операция с картой", 
                "Действие: выполнить операцию с картой");
        addItem(18, Material.SPYGLASS, "variableInspector", ChatColor.GREEN + "Инспектор переменных", 
                "Действие: отобразить значения переменных");
    }
    
    /**
     * Adds loop-related items
     */
    private void addLoopItems() {
        addItem(0, Material.REPEATER, "repeat", ChatColor.GREEN + "Повторить", 
                "Контроль: повторить действия N раз");
        addItem(1, Material.CLOCK, "timedExecution", ChatColor.GREEN + "Таймер", 
                "Контроль: выполнить через определенное время");
        addItem(2, Material.REPEATER, "repeatTrigger", ChatColor.GREEN + "Повторить триггер", 
                "Контроль: повторить триггер");
    }
    
    /**
     * Adds conditional-related items
     */
    private void addConditionalItems() {
        addItem(0, Material.COMPARATOR, "ifVarEquals", ChatColor.RED + "Если переменная равна", 
                "Условие: если переменная равна значению");
        addItem(1, Material.COMPARATOR, "ifVarGreater", ChatColor.RED + "Если переменная больше", 
                "Условие: если переменная больше значения");
        addItem(2, Material.COMPARATOR, "ifVarLess", ChatColor.RED + "Если переменная меньше", 
                "Условие: если переменная меньше значения");
        addItem(3, Material.COMPARATOR, "compareVariable", ChatColor.RED + "Сравнить переменные", 
                "Условие: сравнить две переменные");
        addItem(4, Material.OBSIDIAN, "else", ChatColor.GREEN + "Иначе", 
                "Контроль: альтернативное выполнение");
        addItem(5, Material.COMPARATOR, "conditionalBranch", ChatColor.GREEN + "Условная ветвь", 
                "Контроль: условная ветвь выполнения");
    }
    
    /**
     * Adds function-related items
     */
    private void addFunctionItems() {
        addItem(0, Material.BOOK, "callFunction", ChatColor.GREEN + "Вызвать функцию", 
                "Действие: вызвать сохраненную функцию");
        addItem(1, Material.ENCHANTED_BOOK, "saveFunction", ChatColor.GREEN + "Сохранить функцию", 
                "Действие: сохранить текущую последовательность как функцию");
        addItem(2, Material.ENDER_CHEST, "customFunction", ChatColor.GREEN + "Пользовательская функция", 
                "Действие: создать пользовательскую функцию");
    }
    
    /**
     * Adds custom function items
     */
    private void addCustomFunctionItems() {
        addItem(0, Material.BOOK, "callFunction", ChatColor.GREEN + "Вызвать функцию", 
                "Действие: вызвать сохраненную функцию");
        addItem(1, Material.ENCHANTED_BOOK, "saveFunction", ChatColor.GREEN + "Сохранить функцию", 
                "Действие: сохранить текущую последовательность как функцию");
        addItem(2, Material.ENDER_CHEST, "customFunction", ChatColor.GREEN + "Пользовательская функция", 
                "Действие: создать пользовательскую функцию");
    }
    
    /**
     * Adds system function items
     */
    private void addSystemFunctionItems() {
        // Placeholder for system functions
        addItem(0, Material.COMMAND_BLOCK, "executeAsyncCommand", ChatColor.GREEN + "Выполнить команду", 
                "Действие: выполнить команду асинхронно");
    }
    
    /**
     * Adds scoreboard-related items
     */
    private void addScoreboardItems() {
        addItem(0, Material.OAK_SIGN, "createScoreboard", ChatColor.GREEN + "Создать скорборд", 
                "Действие: создать скорборд");
        addItem(1, Material.OAK_SIGN, "setScore", ChatColor.GREEN + "Установить счет", 
                "Действие: установить счет в скорборде");
        addItem(2, Material.OAK_SIGN, "incrementScore", ChatColor.GREEN + "Увеличить счет", 
                "Действие: увеличить счет в скорборде");
    }
    
    /**
     * Adds team-related items
     */
    private void addTeamItems() {
        addItem(0, Material.WHITE_BANNER, "createTeam", ChatColor.GREEN + "Создать команду", 
                "Действие: создать команду");
        addItem(1, Material.WHITE_BANNER, "addPlayerToTeam", ChatColor.GREEN + "Добавить игрока в команду", 
                "Действие: добавить игрока в команду");
    }
    
    /**
     * Adds location-related items
     */
    private void addLocationItems() {
        addItem(0, Material.COMPASS, "saveLocation", ChatColor.GREEN + "Сохранить локацию", 
                "Действие: сохранить текущую локацию");
        addItem(1, Material.COMPASS, "getLocation", ChatColor.GREEN + "Получить локацию", 
                "Действие: получить сохраненную локацию");
        addItem(2, Material.ENDER_PEARL, "teleportToLocation", ChatColor.GREEN + "Телепорт к локации", 
                "Действие: телепортировать к сохраненной локации");
    }
    
    /**
     * Adds economy-related items
     */
    private void addEconomyItems() {
        addItem(0, Material.GOLD_INGOT, "economyTransaction", ChatColor.GREEN + "Экономическая транзакция", 
                "Действие: выполнить экономическую транзакцию");
    }
    
    /**
     * Adds integration-related items
     */
    private void addIntegrationItems() {
        addItem(0, Material.PAPER, "discordWebhook", ChatColor.GREEN + "Discord Webhook", 
                "Действие: отправить сообщение через Discord webhook");
    }
    
    /**
     * Adds permission-related items
     */
    private void addPermissionItems() {
        addItem(0, Material.COMMAND_BLOCK, "isOp", ChatColor.RED + "Админ", 
                "Условие: проверить, является ли игрок админом");
        addItem(1, Material.COMMAND_BLOCK, "hasPermission", ChatColor.RED + "Есть разрешение", 
                "Условие: проверить, есть ли разрешение у игрока");
    }
    
    /**
     * Adds inventory-related items
     */
    private void addInventoryItems() {
        addItem(0, Material.CHEST, "checkPlayerInventory", ChatColor.RED + "Инвентарь игрока", 
                "Условие: проверить инвентарь игрока");
        addItem(1, Material.CHEST, "hasItem", ChatColor.RED + "Есть предмет", 
                "Условие: проверить, есть ли предмет у игрока");
        addItem(2, Material.DIAMOND, "isPlayerHolding", ChatColor.RED + "Держит предмет", 
                "Условие: проверить, держит ли игрок предмет");
    }
    
    /**
     * Adds stats-related items
     */
    private void addStatsItems() {
        addItem(0, Material.PLAYER_HEAD, "checkPlayerStats", ChatColor.RED + "Статистика игрока", 
                "Условие: проверить статистику игрока");
        addItem(1, Material.APPLE, "playerHealth", ChatColor.RED + "Здоровье игрока", 
                "Условие: проверить здоровье игрока");
    }
    
    /**
     * Adds online-related items
     */
    private void addOnlineItems() {
        addItem(0, Material.COMMAND_BLOCK, "checkServerOnline", ChatColor.RED + "Сервер онлайн", 
                "Условие: проверить онлайн сервера");
    }
    
    /**
     * Adds weather-related items
     */
    private void addWeatherItems() {
        addItem(0, Material.WATER_BUCKET, "checkWorldWeather", ChatColor.RED + "Погода в мире", 
                "Условие: проверить погоду в мире");
        addItem(1, Material.WATER_BUCKET, "setWeather", ChatColor.GREEN + "Установить погоду", 
                "Действие: установить погоду в мире");
    }
    
    /**
     * Adds region-related items
     */
    private void addRegionItems() {
        addItem(0, Material.BARRIER, "worldGuardRegionCheck", ChatColor.RED + "Регион WorldGuard", 
                "Условие: проверить регион WorldGuard");
    }
    
    /**
     * Adds timing-related items
     */
    private void addTimingItems() {
        addItem(0, Material.CLOCK, "wait", ChatColor.GREEN + "Подождать", 
                "Действие: подождать определенное время");
        addItem(1, Material.CLOCK, "timedExecution", ChatColor.GREEN + "Таймер", 
                "Контроль: выполнить через определенное время");
    }
    
    /**
     * Adds data structure items
     */
    private void addDataStructureItems() {
        addItem(0, Material.BOOK, "createList", ChatColor.GREEN + "Создать список", 
                "Действие: создать список");
        addItem(1, Material.WRITABLE_BOOK, "listOperation", ChatColor.GREEN + "Операция со списком", 
                "Действие: выполнить операцию со списком");
        addItem(2, Material.MAP, "createMap", ChatColor.GREEN + "Создать карту", 
                "Действие: создать карту");
        addItem(3, Material.FILLED_MAP, "mapOperation", ChatColor.GREEN + "Операция с картой", 
                "Действие: выполнить операцию с картой");
    }
    
    /**
     * Adds general items
     */
    private void addGeneralItems() {
        BlockConfigService configService = plugin.getServiceRegistry().getBlockConfigService();
        if (configService != null) {
            BlockConfigService.BlockConfig blockConfig = configService.getBlockConfigByMaterial(
                Material.getMaterial(codeBlock.getMaterialName()));
            
            if (blockConfig != null && blockConfig.getActions() != null) {
                List<String> actions = blockConfig.getActions();
                int slot = 0;
                
                for (String action : actions) {
                    if (slot >= INVENTORY_SIZE - 9) break; // Leave space for navigation buttons
                    
                    // Get action display name from config
                    String displayName = getActionDisplayName(action);
                    String description = getActionDescription(action);
                    
                    addItem(slot, getActionMaterial(action), action, displayName, description);
                    slot++;
                }
            }
        }
    }
    
    /**
     * Gets display name for an action
     */
    private String getActionDisplayName(String action) {
        switch (action) {
            case "onJoin": return ChatColor.AQUA + "Игрок заходит";
            case "onLeave": return ChatColor.AQUA + "Игрок выходит";
            case "onChat": return ChatColor.AQUA + "Сообщение в чате";
            case "onBlockBreak": return ChatColor.AQUA + "Блок сломан";
            case "onBlockPlace": return ChatColor.AQUA + "Блок поставлен";
            case "onPlayerMove": return ChatColor.AQUA + "Игрок двигается";
            case "onPlayerDeath": return ChatColor.AQUA + "Игрок умер";
            case "onCommand": return ChatColor.AQUA + "Команда выполнена";
            case "onTick": return ChatColor.AQUA + "Тик сервера";
            case "onPlayerInteract": return ChatColor.AQUA + "Игрок взаимодействует";
            case "sendMessage": return ChatColor.GREEN + "Отправить сообщение";
            case "teleport": return ChatColor.GREEN + "Телепортировать";
            case "giveItem": return ChatColor.GREEN + "Выдать предмет";
            case "playSound": return ChatColor.GREEN + "Проиграть звук";
            case "effect": return ChatColor.GREEN + "Эффект";
            case "command": return ChatColor.GREEN + "Выполнить команду";
            case "broadcast": return ChatColor.GREEN + "Объявление";
            case "giveItems": return ChatColor.GREEN + "Выдать предметы";
            case "spawnEntity": return ChatColor.GREEN + "Создать существо";
            case "removeItems": return ChatColor.GREEN + "Удалить предметы";
            case "setArmor": return ChatColor.GREEN + "Установить броню";
            case "spawnMob": return ChatColor.GREEN + "Создать моба";
            case "healPlayer": return ChatColor.GREEN + "Исцелить игрока";
            case "setGameMode": return ChatColor.GREEN + "Установить режим игры";
            case "setTime": return ChatColor.GREEN + "Установить время";
            case "setWeather": return ChatColor.GREEN + "Установить погоду";
            case "explosion": return ChatColor.GREEN + "Создать взрыв";
            case "setBlock": return ChatColor.GREEN + "Установить блок";
            case "getPlayerName": return ChatColor.GREEN + "Получить имя игрока";
            case "wait": return ChatColor.GREEN + "Подождать";
            case "randomNumber": return ChatColor.GREEN + "Случайное число";
            case "playParticle": return ChatColor.GREEN + "Проиграть частицы";
            case "sendTitle": return ChatColor.GREEN + "Отправить заголовок";
            case "sendActionBar": return ChatColor.GREEN + "Отправить ActionBar";
            case "executeAsyncCommand": return ChatColor.GREEN + "Выполнить команду";
            case "teleportToLocation": return ChatColor.GREEN + "Телепорт к локации";
            case "setExperience": return ChatColor.GREEN + "Установить опыт";
            case "sendCustomTitle": return ChatColor.GREEN + "Отправить заголовок";
            case "playCustomSound": return ChatColor.GREEN + "Проиграть звук";
            case "spawnParticleEffect": return ChatColor.GREEN + "Проиграть частицы";
            case "setVar": return ChatColor.YELLOW + "Установить переменную";
            case "getVar": return ChatColor.YELLOW + "Получить переменную";
            case "addVar": return ChatColor.YELLOW + "Добавить к переменной";
            case "subVar": return ChatColor.YELLOW + "Вычесть из переменной";
            case "mulVar": return ChatColor.YELLOW + "Умножить переменную";
            case "divVar": return ChatColor.YELLOW + "Разделить переменную";
            case "setGlobalVar": return ChatColor.YELLOW + "Установить глобальную переменную";
            case "getGlobalVar": return ChatColor.YELLOW + "Получить глобальную переменную";
            case "setServerVar": return ChatColor.YELLOW + "Установить серверную переменную";
            case "getServerVar": return ChatColor.YELLOW + "Получить серверную переменную";
            case "createList": return ChatColor.YELLOW + "Создать список";
            case "listOperation": return ChatColor.YELLOW + "Операция со списком";
            case "createMap": return ChatColor.YELLOW + "Создать карту";
            case "mapOperation": return ChatColor.YELLOW + "Операция с картой";
            case "isOp": return ChatColor.RED + "Админ";
            case "compareVariable": return ChatColor.RED + "Сравнить переменные";
            case "worldTime": return ChatColor.RED + "Время мира";
            case "isNearBlock": return ChatColor.RED + "Рядом с блоком";
            case "mobNear": return ChatColor.RED + "Моб рядом";
            case "playerGameMode": return ChatColor.RED + "Режим игры";
            case "playerHealth": return ChatColor.RED + "Здоровье игрока";
            case "hasItem": return ChatColor.RED + "Есть предмет";
            case "hasPermission": return ChatColor.RED + "Есть разрешение";
            case "isInWorld": return ChatColor.RED + "В мире";
            case "isBlockType": return ChatColor.RED + "Тип блока";
            case "isPlayerHolding": return ChatColor.RED + "Держит предмет";
            case "isNearEntity": return ChatColor.RED + "Рядом с существом";
            case "hasArmor": return ChatColor.RED + "Есть броня";
            case "isNight": return ChatColor.RED + "Ночь";
            case "isRiding": return ChatColor.RED + "Оседлал";
            case "checkPlayerInventory": return ChatColor.RED + "Инвентарь игрока";
            case "checkPlayerStats": return ChatColor.RED + "Статистика игрока";
            case "checkServerOnline": return ChatColor.RED + "Сервер онлайн";
            case "checkWorldWeather": return ChatColor.RED + "Погода в мире";
            case "worldGuardRegionCheck": return ChatColor.RED + "Регион WorldGuard";
            case "ifVarEquals": return ChatColor.LIGHT_PURPLE + "Если равно";
            case "ifVarGreater": return ChatColor.LIGHT_PURPLE + "Если больше";
            case "ifVarLess": return ChatColor.LIGHT_PURPLE + "Если меньше";
            case "repeat": return ChatColor.GOLD + "Повторить";
            case "repeatTrigger": return ChatColor.GOLD + "Повторить триггер";
            case "timedExecution": return ChatColor.GOLD + "Таймер";
            case "callFunction": return ChatColor.DARK_PURPLE + "Вызвать функцию";
            case "saveFunction": return ChatColor.DARK_PURPLE + "Сохранить функцию";
            case "customFunction": return ChatColor.DARK_PURPLE + "Пользовательская функция";
            case "else": return ChatColor.GOLD + "Иначе";
            case "conditionalBranch": return ChatColor.GOLD + "Условная ветвь";
            default: return ChatColor.WHITE + action;
        }
    }
    
    /**
     * Gets description for an action
     */
    private String getActionDescription(String action) {
        switch (action) {
            case "onJoin": return "Событие: когда игрок заходит на сервер";
            case "onLeave": return "Событие: когда игрок выходит с сервера";
            case "onChat": return "Событие: когда игрок пишет в чат";
            case "onBlockBreak": return "Событие: когда блок ломается";
            case "onBlockPlace": return "Событие: когда блок ставится";
            case "onPlayerMove": return "Событие: когда игрок двигается";
            case "onPlayerDeath": return "Событие: когда игрок умирает";
            case "onCommand": return "Событие: когда команда выполнена";
            case "onTick": return "Событие: каждый тик сервера";
            case "onPlayerInteract": return "Событие: когда игрок взаимодействует";
            case "sendMessage": return "Действие: отправить сообщение игроку";
            case "teleport": return "Действие: телепортировать игрока";
            case "giveItem": return "Действие: выдать предмет игроку";
            case "playSound": return "Действие: проиграть звук";
            case "effect": return "Действие: применить эффект";
            case "command": return "Действие: выполнить команду";
            case "broadcast": return "Действие: отправить объявление всем игрокам";
            case "giveItems": return "Действие: выдать предметы игроку";
            case "spawnEntity": return "Действие: создать существо";
            case "removeItems": return "Действие: удалить предметы у игрока";
            case "setArmor": return "Действие: установить броню игроку";
            case "spawnMob": return "Действие: создать моба";
            case "healPlayer": return "Действие: исцелить игрока";
            case "setGameMode": return "Действие: установить режим игры игроку";
            case "setTime": return "Действие: установить время в мире";
            case "setWeather": return "Действие: установить погоду в мире";
            case "explosion": return "Действие: создать взрыв в мире";
            case "setBlock": return "Действие: установить блок";
            case "getPlayerName": return "Действие: получить имя игрока";
            case "wait": return "Действие: подождать определенное время";
            case "randomNumber": return "Действие: сгенерировать случайное число";
            case "playParticle": return "Действие: проиграть частицы";
            case "sendTitle": return "Действие: отправить заголовок игроку";
            case "sendActionBar": return "Действие: отправить сообщение в ActionBar";
            case "executeAsyncCommand": return "Действие: выполнить команду асинхронно";
            case "teleportToLocation": return "Действие: телепортировать к сохраненной локации";
            case "setExperience": return "Действие: установить опыт игроку";
            case "sendCustomTitle": return "Действие: отправить пользовательский заголовок";
            case "playCustomSound": return "Действие: проиграть пользовательский звук";
            case "spawnParticleEffect": return "Действие: создать эффект частиц";
            case "setVar": return "Действие: установить значение переменной";
            case "getVar": return "Действие: получить значение переменной";
            case "addVar": return "Действие: добавить значение к переменной";
            case "subVar": return "Действие: вычесть значение из переменной";
            case "mulVar": return "Действие: умножить переменную на значение";
            case "divVar": return "Действие: разделить переменную на значение";
            case "setGlobalVar": return "Действие: установить значение глобальной переменной";
            case "getGlobalVar": return "Действие: получить значение глобальной переменной";
            case "setServerVar": return "Действие: установить значение серверной переменной";
            case "getServerVar": return "Действие: получить значение серверной переменной";
            case "createList": return "Действие: создать список";
            case "listOperation": return "Действие: выполнить операцию со списком";
            case "createMap": return "Действие: создать карту";
            case "mapOperation": return "Действие: выполнить операцию с картой";
            case "isOp": return "Условие: проверить, является ли игрок админом";
            case "compareVariable": return "Условие: сравнить две переменные";
            case "worldTime": return "Условие: проверить время мира";
            case "isNearBlock": return "Условие: проверить, рядом ли блок";
            case "mobNear": return "Условие: проверить, рядом ли моб";
            case "playerGameMode": return "Условие: проверить режим игры игрока";
            case "playerHealth": return "Условие: проверить здоровье игрока";
            case "hasItem": return "Условие: проверить, есть ли предмет у игрока";
            case "hasPermission": return "Условие: проверить, есть ли разрешение у игрока";
            case "isInWorld": return "Условие: проверить, находится ли игрок в мире";
            case "isBlockType": return "Условие: проверить тип блока";
            case "isPlayerHolding": return "Условие: проверить, держит ли игрок предмет";
            case "isNearEntity": return "Условие: проверить, рядом ли существо";
            case "hasArmor": return "Условие: проверить, есть ли броня у игрока";
            case "isNight": return "Условие: проверить, ночь ли сейчас";
            case "isRiding": return "Условие: проверить, оседлал ли игрок существо";
            case "checkPlayerInventory": return "Условие: проверить инвентарь игрока";
            case "checkPlayerStats": return "Условие: проверить статистику игрока";
            case "checkServerOnline": return "Условие: проверить онлайн сервера";
            case "checkWorldWeather": return "Условие: проверить погоду в мире";
            case "worldGuardRegionCheck": return "Условие: проверить регион WorldGuard";
            case "ifVarEquals": return "Условие: если переменная равна значению";
            case "ifVarGreater": return "Условие: если переменная больше значения";
            case "ifVarLess": return "Условие: если переменная меньше значения";
            case "repeat": return "Контроль: повторить действия N раз";
            case "repeatTrigger": return "Контроль: повторить триггер";
            case "timedExecution": return "Контроль: выполнить через определенное время";
            case "callFunction": return "Действие: вызвать сохраненную функцию";
            case "saveFunction": return "Действие: сохранить текущую последовательность как функцию";
            case "customFunction": return "Действие: создать пользовательскую функцию";
            case "else": return "Контроль: альтернативное выполнение";
            case "conditionalBranch": return "Контроль: условная ветвь выполнения";
            default: return "Описание отсутствует";
        }
    }
    
    /**
     * Gets material for an action
     */
    private Material getActionMaterial(String action) {
        switch (action) {
            case "onJoin":
            case "onLeave":
            case "getPlayerName":
                return Material.PLAYER_HEAD;
            case "onChat":
            case "sendMessage":
            case "broadcast":
            case "sendTitle":
            case "sendActionBar":
                return Material.WRITABLE_BOOK;
            case "onBlockBreak":
            case "onBlockPlace":
            case "setBlock":
                return Material.COBBLESTONE;
            case "onPlayerMove":
                return Material.LEATHER_BOOTS;
            case "onPlayerDeath":
                return Material.SKELETON_SKULL;
            case "onCommand":
            case "command":
            case "executeAsyncCommand":
                return Material.COMMAND_BLOCK;
            case "onTick":
                return Material.REDSTONE_BLOCK;
            case "onPlayerInteract":
                return Material.STONE_BUTTON;
            case "teleport":
            case "teleportToLocation":
                return Material.ENDER_PEARL;
            case "giveItem":
            case "giveItems":
            case "removeItems":
                return Material.CHEST;
            case "playSound":
            case "playCustomSound":
                return Material.NOTE_BLOCK;
            case "effect":
                return Material.POTION;
            case "spawnEntity":
            case "spawnMob":
                return Material.ZOMBIE_SPAWN_EGG;
            case "setArmor":
                return Material.DIAMOND_CHESTPLATE;
            case "healPlayer":
                return Material.GOLDEN_APPLE;
            case "setGameMode":
                return Material.COMMAND_BLOCK;
            case "setTime":
                return Material.CLOCK;
            case "setWeather":
                return Material.WATER_BUCKET;
            case "explosion":
                return Material.TNT;
            case "wait":
                return Material.CLOCK;
            case "randomNumber":
                return Material.SUNFLOWER;
            case "playParticle":
            case "spawnParticleEffect":
                return Material.FIREWORK_ROCKET;
            case "setExperience":
                return Material.EXPERIENCE_BOTTLE;
            case "setVar":
            case "getVar":
            case "addVar":
            case "subVar":
            case "mulVar":
            case "divVar":
            case "setGlobalVar":
            case "getGlobalVar":
            case "setServerVar":
            case "getServerVar":
                return Material.REDSTONE;
            case "createList":
            case "listOperation":
                return Material.BOOK;
            case "createMap":
            case "mapOperation":
                return Material.MAP;
            case "isOp":
            case "hasPermission":
                return Material.COMMAND_BLOCK;
            case "compareVariable":
            case "ifVarEquals":
            case "ifVarGreater":
            case "ifVarLess":
                return Material.COMPARATOR;
            case "worldTime":
            case "isNight":
                return Material.CLOCK;
            case "isNearBlock":
            case "isBlockType":
                return Material.STONE;
            case "mobNear":
            case "isNearEntity":
                return Material.ZOMBIE_HEAD;
            case "playerGameMode":
                return Material.GRASS_BLOCK;
            case "playerHealth":
                return Material.APPLE;
            case "hasItem":
            case "isPlayerHolding":
                return Material.DIAMOND;
            case "isInWorld":
                return Material.GRASS_BLOCK;
            case "hasArmor":
                return Material.DIAMOND_CHESTPLATE;
            case "isRiding":
                return Material.SADDLE;
            case "checkPlayerInventory":
                return Material.CHEST;
            case "checkPlayerStats":
                return Material.PLAYER_HEAD;
            case "checkServerOnline":
                return Material.COMMAND_BLOCK;
            case "checkWorldWeather":
                return Material.WATER_BUCKET;
            case "worldGuardRegionCheck":
                return Material.BARRIER;
            case "repeat":
            case "repeatTrigger":
                return Material.REPEATER;
            case "timedExecution":
                return Material.CLOCK;
            case "callFunction":
            case "saveFunction":
            case "customFunction":
                return Material.BOOK;
            case "else":
                return Material.OBSIDIAN;
            case "conditionalBranch":
                return Material.COMPARATOR;
            default:
                return Material.PAPER;
        }
    }
    
    /**
     * Adds an item to the GUI
     */
    private void addItem(int slot, Material material, String actionId, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + description);
        lore.add("");
        lore.add(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + actionId);
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
    
    /**
     * Gets the category
     */
    public String getCategory() {
        return category;
    }
}