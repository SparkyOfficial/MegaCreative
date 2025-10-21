package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Класс для создания и выдачи предметов-блоков кодирования игрокам.
 */
public class CodingItems {
    
    
    public static final String EVENT_BLOCK_NAME = "§b§lСобытие игрока";
    public static final String CONDITION_BLOCK_NAME = "§6§lУсловие игрока";
    public static final String ACTION_BLOCK_NAME = "§7§lДействие игрока";
    public static final String VARIABLE_BLOCK_NAME = "§f§lПрисвоить переменную";
    public static final String ELSE_BLOCK_NAME = "§e§lИначе";
    public static final String GAME_ACTION_BLOCK_NAME = "§8§lИгровое действие";
    public static final String IF_VAR_BLOCK_NAME = "§5§lЕсли переменная";
    public static final String IF_GAME_BLOCK_NAME = "§c§lЕсли игра";
    public static final String IF_MOB_BLOCK_NAME = "§d§lЕсли существо";
    public static final String GET_DATA_BLOCK_NAME = "§a§lПолучить данные";
    public static final String REPEAT_BLOCK_NAME = "§f🔄 Повторить N раз";
    public static final String CALL_FUNCTION_BLOCK_NAME = "§b📞 Вызвать функцию";
    public static final String SAVE_FUNCTION_BLOCK_NAME = "§d💾 Сохранить функцию";
    public static final String REPEAT_TRIGGER_BLOCK_NAME = "§e⏰ Повторяющийся триггер";
    public static final String BRACKET_BLOCK_NAME = "§6🔧 Скобка";
    public static final String ARROW_NOT_NAME = "§c⟨ Отрицание НЕ";
    public static final String GAME_VALUE_NAME = "§b🎮 Игровое значение";

    public static final String DATA_CREATOR_NAME = "§b§lСоздать данные";
    public static final String CODE_MOVER_NAME = "§6🔄 Инструмент Перемещения";

    
    private static final Set<String> CODING_ITEM_NAMES = new HashSet<>();

    static {
        CODING_ITEM_NAMES.add(EVENT_BLOCK_NAME);
        CODING_ITEM_NAMES.add(CONDITION_BLOCK_NAME);
        CODING_ITEM_NAMES.add(ACTION_BLOCK_NAME);
        CODING_ITEM_NAMES.add(VARIABLE_BLOCK_NAME);
        CODING_ITEM_NAMES.add(ELSE_BLOCK_NAME);
        CODING_ITEM_NAMES.add(GAME_ACTION_BLOCK_NAME);
        CODING_ITEM_NAMES.add(IF_VAR_BLOCK_NAME);
        CODING_ITEM_NAMES.add(IF_GAME_BLOCK_NAME);
        CODING_ITEM_NAMES.add(IF_MOB_BLOCK_NAME);
        CODING_ITEM_NAMES.add(GET_DATA_BLOCK_NAME);
        CODING_ITEM_NAMES.add(REPEAT_BLOCK_NAME);
        CODING_ITEM_NAMES.add(CALL_FUNCTION_BLOCK_NAME);
        CODING_ITEM_NAMES.add(SAVE_FUNCTION_BLOCK_NAME);
        CODING_ITEM_NAMES.add(REPEAT_TRIGGER_BLOCK_NAME);
        CODING_ITEM_NAMES.add(BRACKET_BLOCK_NAME);
        CODING_ITEM_NAMES.add(ARROW_NOT_NAME);
        CODING_ITEM_NAMES.add(GAME_VALUE_NAME);

        CODING_ITEM_NAMES.add(DATA_CREATOR_NAME);
        CODING_ITEM_NAMES.add(CODE_MOVER_NAME);
    }

    public static boolean isDisplayNameACodingItem(String displayName) {
        return CODING_ITEM_NAMES.contains(displayName);
    }
    

    /**
     * Выдаёт игроку только недостающие предметы для кодинга
     */
    public static void giveMissingItems(Player player, List<String> missingItems) {
        for (String itemName : missingItems) {
            switch (itemName) {
                case "Блок события":
                    player.getInventory().addItem(createNamedItem(Material.DIAMOND_BLOCK, EVENT_BLOCK_NAME));
                    break;
                case "Блок действия":
                    player.getInventory().addItem(createNamedItem(Material.COBBLESTONE, ACTION_BLOCK_NAME));
                    break;
                case "Блок условия":
                    player.getInventory().addItem(createNamedItem(Material.OAK_PLANKS, CONDITION_BLOCK_NAME));
                    break;
                case "Блок переменной":
                    player.getInventory().addItem(createNamedItem(Material.IRON_BLOCK, VARIABLE_BLOCK_NAME));
                    break;
                case "Блок повтора":
                    player.getInventory().addItem(createNamedItem(Material.EMERALD_BLOCK, REPEAT_BLOCK_NAME));
                    break;
                case "Блок иначе":
                    player.getInventory().addItem(createNamedItem(Material.END_STONE, ELSE_BLOCK_NAME));
                    break;
                case "Игровое действие":
                    player.getInventory().addItem(createNamedItem(Material.NETHERITE_BLOCK, GAME_ACTION_BLOCK_NAME));
                    break;
                case "Если переменная":
                    player.getInventory().addItem(createNamedItem(Material.OBSIDIAN, IF_VAR_BLOCK_NAME));
                    break;
                case "Если игра":
                    player.getInventory().addItem(createNamedItem(Material.REDSTONE_BLOCK, IF_GAME_BLOCK_NAME));
                    break;
                case "Если существо":
                    player.getInventory().addItem(createNamedItem(Material.BRICKS, IF_MOB_BLOCK_NAME));
                    break;
                case "Получить данные":
                    player.getInventory().addItem(createNamedItem(Material.POLISHED_GRANITE, GET_DATA_BLOCK_NAME));
                    break;
                case "Вызвать функцию":
                    player.getInventory().addItem(createNamedItem(Material.LAPIS_BLOCK, CALL_FUNCTION_BLOCK_NAME));
                    break;
                case "Сохранить функцию":
                    player.getInventory().addItem(createNamedItem(Material.BOOKSHELF, SAVE_FUNCTION_BLOCK_NAME));
                    break;
                case "Повторяющийся триггер":
                    player.getInventory().addItem(createNamedItem(Material.REDSTONE_BLOCK, REPEAT_TRIGGER_BLOCK_NAME));
                    break;
                case "Скобка":
                    player.getInventory().addItem(createNamedItem(Material.PISTON, BRACKET_BLOCK_NAME));
                    break;
                case "Отрицание НЕ":
                    player.getInventory().addItem(getArrowNot());
                    break;
                case "Игровое значение":
                    player.getInventory().addItem(getGameValue());
                    break;
                case "Создать данные":
                    player.getInventory().addItem(getDataCreator());
                    break;
                case "Перемещатель кода":
                    player.getInventory().addItem(getCodeMover());
                    break;
                default:
                    
                    if (itemName.contains("блок") || itemName.contains("Блок")) {
                        player.getInventory().addItem(createNamedItem(Material.STONE, "§7" + itemName));
                    }
                    break;
            }
        }
    }

    /**
     * Выдаёт игроку 9 новых универсальных блоков для кодинга
     */
    public static void giveCodingItems(Player player) {
        // Extracted method for creating simple block items
        giveSimpleBlockItems(player);
        
        player.getInventory().addItem(getArrowNot());
        
        player.getInventory().addItem(getDataCreator());
        
        player.getInventory().addItem(getCodeMover());
    }
    
    /**
     * Helper method to give simple block items to player
     */
    private static void giveSimpleBlockItems(Player player) {
        player.getInventory().addItem(createNamedItem(Material.DIAMOND_BLOCK, "§b§lСобытие игрока"));
        player.getInventory().addItem(createNamedItem(Material.OAK_PLANKS, "§6§lУсловие игрока"));
        player.getInventory().addItem(createNamedItem(Material.COBBLESTONE, "§7§lДействие игрока"));
        player.getInventory().addItem(createNamedItem(Material.IRON_BLOCK, "§f§lПрисвоить переменную"));
        player.getInventory().addItem(createNamedItem(Material.END_STONE, "§e§lИначе"));
        player.getInventory().addItem(createNamedItem(Material.NETHERITE_BLOCK, "§8§lИгровое действие"));
        player.getInventory().addItem(createNamedItem(Material.OBSIDIAN, "§5§lЕсли переменная"));
        player.getInventory().addItem(createNamedItem(Material.REDSTONE_BLOCK, "§c§lЕсли игра"));
        player.getInventory().addItem(createNamedItem(Material.BRICKS, "§d§lЕсли существо"));
        player.getInventory().addItem(createNamedItem(Material.POLISHED_GRANITE, "§a§lПолучить данные"));
        player.getInventory().addItem(createNamedItem(Material.EMERALD_BLOCK, "§f🔄 Повторить N раз"));
        player.getInventory().addItem(createNamedItem(Material.LAPIS_BLOCK, "§b📞 Вызвать функцию"));
        player.getInventory().addItem(createNamedItem(Material.BOOKSHELF, "§d💾 Сохранить функцию"));
        player.getInventory().addItem(createNamedItem(Material.REDSTONE_BLOCK, "§e⏰ Повторяющийся триггер"));
        player.getInventory().addItem(createNamedItem(Material.PISTON, BRACKET_BLOCK_NAME));
    }
    
    /**
     * Выдает игроку полный набор блоков кодирования, сгенерированный из конфигурации.
     */
    public static void giveCodingItems(Player player, MegaCreative plugin) {
        player.getInventory().clear(); 
        
        BlockConfigService configService = plugin.getServiceRegistry().getBlockConfigService();
        if (configService == null) {
            player.sendMessage("§cОшибка: Сервис конфигурации блоков не загружен!");
            return;
        }

        
        for (BlockConfigService.BlockConfig config : configService.getAllBlockConfigs()) {
            ItemStack item = new ItemStack(config.getMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                
                meta.setDisplayName(config.getDisplayName()); 
                
                List<String> lore = new ArrayList<>();
                lore.add("§7" + config.getDescription());
                lore.add("§8Тип: " + config.getType());
                lore.add("§8ID: " + config.getId()); 
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            player.getInventory().addItem(item);
        }

        player.updateInventory();
    }
    
    public static ItemStack getDataCreator() {
        return createItemWithLore(Material.IRON_INGOT, "§b§lСоздать данные", Arrays.asList(
            "§7Используйте для создания предметов-данных:",
            "§aПКМ§7 - открыть меню создания данных",
            "§7Данные можно вставлять в параметры блоков"
        ));
    }
    
    public static ItemStack getCodeMover() {
        return createItemWithLore(Material.COMPARATOR, CODE_MOVER_NAME, Arrays.asList(
            "§7Инструмент для перемещения блоков кода:",
            "§eShift+ПКМ§7 - скопировать цепочку",
            "§aПКМ§7 - вставить цепочку",
            "§cЛКМ§7 - очистить буфер",
            "§8Для быстрого перемещения отдельных веток кода"
        ));
    }
    
    public static ItemStack getBracketBlock() {
        return createItemWithLore(Material.PISTON, BRACKET_BLOCK_NAME, Arrays.asList(
            "§7Блок для группировки логических секций:",
            "§aПО умолчанию: Открывающая скобка {",
            "§eПКМ§7 - переключить тип скобки",
            "§8Используется для структурирования кода"
        ));
    }
    
    public static ItemStack getArrowNot() {
        return createItemWithLore(Material.ARROW, ARROW_NOT_NAME, Arrays.asList(
            "§7Инструмент для отрицания условий:",
            "§eПКМ§ по блоку условия - инвертировать результат",
            "§7Преобразует 'истина' в 'ложь' и наоборот",
            "§8Полезно для создания 'Если НЕ' условий"
        ));
    }
    
    public static ItemStack getGameValue() {
        return createItemWithLore(Material.GOLDEN_APPLE, GAME_VALUE_NAME, Arrays.asList(
            "§7Используйте для получения игровых значений:",
            "§aПКМ§7 - открыть меню выбора значения",
            "§7Можно использовать в параметрах блоков",
            "§8Примеры: здоровье, голод, позиция и т.д."
        ));
    }
    
    /**
     * Creates an item with the specified material, display name, and lore
     * @param material The material for the item
     * @param displayName The display name for the item
     * @param lore The lore for the item
     * @return The created ItemStack
     */
    private static ItemStack createItemWithLore(Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createSimpleBlock(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Creates an item with the specified material and display name
     * @param material The material for the item
     * @param displayName The display name for the item
     * @return The created ItemStack
     */
    private static ItemStack createNamedItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }
}
