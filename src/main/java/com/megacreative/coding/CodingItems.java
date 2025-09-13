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
    
    // Константы названий предметов для защиты от потери
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

    public static final String COPIER_TOOL_NAME = "§6📋 Копировщик блоков";
    public static final String DATA_CREATOR_NAME = "§b§lСоздать данные";
    public static final String CODE_MOVER_NAME = "§6🔄 Перемещатель кода";

    // --- ДОБАВЛЯЕМ АВТОМАТИЧЕСКУЮ ПРОВЕРКУ ---
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

        CODING_ITEM_NAMES.add(COPIER_TOOL_NAME);
        CODING_ITEM_NAMES.add(DATA_CREATOR_NAME);
        CODING_ITEM_NAMES.add(CODE_MOVER_NAME);
    }

    public static boolean isDisplayNameACodingItem(String displayName) {
        return CODING_ITEM_NAMES.contains(displayName);
    }
    // --- КОНЕЦ БЛОКА ---

    /**
     * Выдаёт игроку только недостающие предметы для кодинга
     */
    public static void giveMissingItems(Player player, List<String> missingItems) {
        for (String itemName : missingItems) {
            switch (itemName) {

                case "Блок события":
                    player.getInventory().addItem(createSimpleBlock(Material.DIAMOND_BLOCK, EVENT_BLOCK_NAME));
                    break;
                case "Блок действия":
                    player.getInventory().addItem(createSimpleBlock(Material.COBBLESTONE, ACTION_BLOCK_NAME));
                    break;
                case "Блок условия":
                    player.getInventory().addItem(createSimpleBlock(Material.OAK_PLANKS, CONDITION_BLOCK_NAME));
                    break;
                case "Блок переменной":
                    player.getInventory().addItem(createSimpleBlock(Material.IRON_BLOCK, VARIABLE_BLOCK_NAME));
                    break;
                case "Блок повтора":
                    player.getInventory().addItem(createSimpleBlock(Material.EMERALD_BLOCK, REPEAT_BLOCK_NAME));
                    break;
                default:
                    // Для неизвестных предметов выдаем базовый набор
                    if (itemName.contains("блок") || itemName.contains("Блок")) {
                        player.getInventory().addItem(createSimpleBlock(Material.STONE, "§7" + itemName));
                    }
                    break;
            }
        }
    }

    /**
     * Выдаёт игроку 9 новых универсальных блоков для кодинга
     */
    public static void giveCodingItems(Player player) {
        // Универсальные блоки для кодинга
        player.getInventory().addItem(createSimpleBlock(Material.DIAMOND_BLOCK, "§b§lСобытие игрока"));
        player.getInventory().addItem(createSimpleBlock(Material.OAK_PLANKS, "§6§lУсловие игрока"));
        player.getInventory().addItem(createSimpleBlock(Material.COBBLESTONE, "§7§lДействие игрока"));
        player.getInventory().addItem(createSimpleBlock(Material.IRON_BLOCK, "§f§lПрисвоить переменную"));
        player.getInventory().addItem(createSimpleBlock(Material.END_STONE, "§e§lИначе"));
        player.getInventory().addItem(createSimpleBlock(Material.NETHERITE_BLOCK, "§8§lИгровое действие"));
        player.getInventory().addItem(createSimpleBlock(Material.OBSIDIAN, "§5§lЕсли переменная"));
        player.getInventory().addItem(createSimpleBlock(Material.REDSTONE_BLOCK, "§c§lЕсли игра"));
        player.getInventory().addItem(createSimpleBlock(Material.BRICKS, "§d§lЕсли существо"));
        player.getInventory().addItem(createSimpleBlock(Material.POLISHED_GRANITE, "§a§lПолучить данные"));
        player.getInventory().addItem(createSimpleBlock(Material.EMERALD_BLOCK, "§f🔄 Повторить N раз"));
        player.getInventory().addItem(createSimpleBlock(Material.LAPIS_BLOCK, "§b📞 Вызвать функцию"));
        player.getInventory().addItem(createSimpleBlock(Material.BOOKSHELF, "§d💾 Сохранить функцию"));
        player.getInventory().addItem(createSimpleBlock(Material.REDSTONE_BLOCK, "§e⏰ Повторяющийся триггер"));
        player.getInventory().addItem(createSimpleBlock(Material.PISTON, BRACKET_BLOCK_NAME));
        player.getInventory().addItem(getArrowNot());
        player.getInventory().addItem(getGameValue());
    
        // Железный слиток для создания данных
        player.getInventory().addItem(getDataCreator());
        
        // Перемещатель кода
        player.getInventory().addItem(getCodeMover());
        
        ItemStack copier = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta copierMeta = copier.getItemMeta();
        copierMeta.setDisplayName("§6📋 Копировщик блоков");
        copierMeta.setLore(Arrays.asList(
            "§7ЛКМ по блоку - скопировать",
            "§7ПКМ по блоку - вставить"
        ));
        copier.setItemMeta(copierMeta);
        player.getInventory().addItem(copier);


    }
    
    /**
     * Выдает игроку полный набор блоков кодирования, сгенерированный из конфигурации.
     */
    public static void giveCodingItems(Player player, MegaCreative plugin) {
        player.getInventory().clear(); // Очищаем инвентарь для чистоты
        
        BlockConfigService configService = plugin.getServiceRegistry().getBlockConfigService();
        if (configService == null) {
            player.sendMessage("§cОшибка: Сервис конфигурации блоков не загружен!");
            return;
        }

        // Проходим по ВСЕМ блокам, определенным в coding_blocks.yml
        for (BlockConfigService.BlockConfig config : configService.getAllBlockConfigs()) {
            ItemStack item = new ItemStack(config.getMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // ВАЖНО: Отображаемое имя предмета ДОЛЖНО быть уникальным displayName из конфига
                meta.setDisplayName(config.getDisplayName()); 
                
                List<String> lore = new ArrayList<>();
                lore.add("§7" + config.getDescription());
                lore.add("§8Тип: " + config.getType());
                lore.add("§8ID: " + config.getId()); // ID для внутренней логики
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            player.getInventory().addItem(item);
        }

        player.updateInventory();
    }
    
    public static ItemStack getDataCreator() {
        ItemStack item = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lСоздать данные");
        meta.setLore(Arrays.asList(
            "§7Используйте для создания предметов-данных:",
            "§aПКМ§7 - открыть меню создания данных",
            "§7Данные можно вставлять в параметры блоков"
        ));
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack getCodeMover() {
        ItemStack item = new ItemStack(Material.COMPARATOR);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(CODE_MOVER_NAME);
        meta.setLore(Arrays.asList(
            "§7Инструмент для перемещения блоков кода:",
            "§eShift+ПКМ§7 - скопировать цепочку",
            "§aПКМ§7 - вставить цепочку",
            "§cЛКМ§7 - очистить буфер",
            "§8Копирует всю связанную цепочку"
        ));
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack getBracketBlock() {
        ItemStack item = new ItemStack(Material.PISTON);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(BRACKET_BLOCK_NAME);
        meta.setLore(Arrays.asList(
            "§7Блок для группировки логических секций:",
            "§aПО умолчанию: Открывающая скобка {",
            "§eПКМ§7 - переключить тип скобки",
            "§8Используется для структурирования кода"
        ));
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack getArrowNot() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ARROW_NOT_NAME);
        meta.setLore(Arrays.asList(
            "§7Инструмент для отрицания условий:",
            "§eПКМ§ по блоку условия - инвертировать результат",
            "§7Преобразует 'истина' в 'ложь' и наоборот",
            "§8Полезно для создания 'Если НЕ' условий"
        ));
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
    
    public static ItemStack getGameValue() {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(GAME_VALUE_NAME);
        meta.setLore(Arrays.asList(
            "§7Используйте для получения игровых значений:",
            "§aПКМ§7 - открыть меню выбора значения",
            "§7Можно использовать в параметрах блоков",
            "§8Примеры: здоровье, голод, позиция и т.д."
        ));
        item.setItemMeta(meta);
        return item;
    }

}