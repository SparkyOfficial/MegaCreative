package com.megacreative.coding;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

/**
 * Класс для создания и выдачи предметов-блоков кодирования игрокам.
 */
public class CodingItems {

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
        player.getInventory().clear();

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

        // Добавляем наш новый инструмент в инвентарь
        player.getInventory().setItem(8, getLinkingTool()); // В последний слот хотбара

        player.sendMessage("§a✓ Вы получили универсальные блоки для кодинга!");
        player.sendMessage("§7Используйте §eСвязующий жезл§7 для их соединения.");
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
