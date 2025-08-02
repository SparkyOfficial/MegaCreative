package com.megacreative.coding;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    public static final String LINKER_TOOL_NAME = "§e§lСвязующий жезл";
    public static final String INSPECTOR_TOOL_NAME = "§b🔍 Инспектор блоков";
    public static final String COPIER_TOOL_NAME = "§6📋 Копировщик блоков";
    public static final String DATA_CREATOR_NAME = "§b§lСоздать данные";

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
        CODING_ITEM_NAMES.add(LINKER_TOOL_NAME);
        CODING_ITEM_NAMES.add(INSPECTOR_TOOL_NAME);
        CODING_ITEM_NAMES.add(COPIER_TOOL_NAME);
        CODING_ITEM_NAMES.add(DATA_CREATOR_NAME);
    }

    public static boolean isDisplayNameACodingItem(String displayName) {
        return CODING_ITEM_NAMES.contains(displayName);
    }
    // --- КОНЕЦ БЛОКА ---

    /**
     * Создает связующий жезл для соединения блоков
     */
    public static ItemStack getLinkingTool() {
        ItemStack tool = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = tool.getItemMeta();
        meta.setDisplayName("§e§lСвязующий жезл");
        meta.setLore(Arrays.asList(
            "§7Используйте для соединения блоков кода:",
            "§aЛКМ§7 - выбрать начальный блок",
            "§aПКМ§7 - выбрать конечный блок для связи"
        ));
        tool.setItemMeta(meta);
        return tool;
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
        
        // Железный слиток для создания данных
        player.getInventory().addItem(getDataCreator());
        
        // Связующий жезл
        player.getInventory().setItem(8, getLinkingTool());
        
        // Инструменты разработчика
        ItemStack inspector = new ItemStack(Material.DEBUG_STICK);
        ItemMeta inspectorMeta = inspector.getItemMeta();
        inspectorMeta.setDisplayName("§b🔍 Инспектор блоков");
        inspectorMeta.setLore(Arrays.asList(
            "§7ПКМ по блоку кода для просмотра",
            "§7информации о действии и параметрах"
        ));
        inspector.setItemMeta(inspectorMeta);
        player.getInventory().addItem(inspector);
        
        ItemStack copier = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta copierMeta = copier.getItemMeta();
        copierMeta.setDisplayName("§6📋 Копировщик блоков");
        copierMeta.setLore(Arrays.asList(
            "§7ЛКМ по блоку - скопировать",
            "§7ПКМ по блоку - вставить"
        ));
        copier.setItemMeta(copierMeta);
        player.getInventory().addItem(copier);

        player.sendMessage("§a✓ Вы получили универсальные блоки для кодинга!");
        player.sendMessage("§7Используйте §eСвязующий жезл§7 для их соединения.");
        player.sendMessage("§7Используйте §eЖелезный слиток§7 для создания данных.");
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

    private static ItemStack createSimpleBlock(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            item.setItemMeta(meta);
        }
        return item;
    }
}
