package com.megacreative.coding;

import com.megacreative.MegaCreative;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * Обработчик размещения и взаимодействия с блоками кодирования в мире разработки.
 */
public class BlockPlacementHandler implements Listener {

    private final MegaCreative plugin;
    
    // Хранилище размещенных блоков кода: Location -> BlockType
    private final Map<Location, BlockType> placedCodeBlocks = new HashMap<>();
    
    // Хранилище соединений между блоками: Location -> Location (следующий блок)
    private final Map<Location, Location> blockConnections = new HashMap<>();

    public BlockPlacementHandler(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Обрабатывает размещение блоков кодирования
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        ItemStack item = event.getItemInHand();
        
        // Проверяем, что игрок в мире разработки
        if (!isInDevWorld(player)) {
            return;
        }
        
        // Проверяем, что это блок кодирования
        BlockType blockType = getBlockTypeFromItem(item);
        if (blockType == null) {
            return;
        }
        
        // Сохраняем информацию о размещенном блоке
        placedCodeBlocks.put(block.getLocation(), blockType);
        
        player.sendMessage("§a✓ Блок '" + getBlockDisplayName(blockType) + "' размещен!");
        player.sendMessage("§7Кликните ПКМ по блоку для настройки или соединения.");
    }

    /**
     * Обрабатывает клики по размещенным блокам кодирования
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        
        if (clickedBlock == null || !isInDevWorld(player)) {
            return;
        }
        
        // Проверяем, что это размещенный блок кодирования
        BlockType blockType = placedCodeBlocks.get(clickedBlock.getLocation());
        if (blockType == null) {
            return;
        }
        
        event.setCancelled(true); // Отменяем стандартное взаимодействие
        
        // Открываем меню настройки блока
        openBlockConfigMenu(player, clickedBlock.getLocation(), blockType);
    }

    /**
     * Определяет тип блока кодирования по предмету
     */
    private BlockType getBlockTypeFromItem(ItemStack item) {
        return CodingItems.getCodingBlockType(item);
    }

    /**
     * Проверяет, находится ли игрок в мире разработки
     */
    private boolean isInDevWorld(Player player) {
        String worldName = player.getWorld().getName();
        return worldName.startsWith("megacreative_") && worldName.endsWith("_dev");
    }

    /**
     * Открывает меню настройки блока
     */
    private void openBlockConfigMenu(Player player, Location blockLocation, BlockType blockType) {
        player.sendMessage("§e=== Настройка блока ===");
        player.sendMessage("§7Тип: §f" + getBlockDisplayName(blockType));
        player.sendMessage("§7Позиция: §f" + locationToString(blockLocation));
        player.sendMessage("");
        
        switch (blockType) {
            case ACTION_SEND_MESSAGE:
                player.sendMessage("§a▶ Введите в чат сообщение для отправки:");
                player.sendMessage("§7Пример: §fПривет, %player%!");
                break;
                
            case ACTION_TELEPORT_PLAYER:
                player.sendMessage("§a▶ Введите координаты для телепортации:");
                player.sendMessage("§7Пример: §f100 70 200");
                break;
                
            case CONDITION_HAS_ITEM:
                player.sendMessage("§a▶ Введите название предмета для проверки:");
                player.sendMessage("§7Пример: §fDIAMOND_SWORD");
                break;
                
            case VARIABLE_SET:
                player.sendMessage("§a▶ Введите имя и значение переменной:");
                player.sendMessage("§7Пример: §fplayerScore 100");
                break;
                
            default:
                player.sendMessage("§a▶ Этот блок не требует дополнительной настройки.");
                player.sendMessage("§7Соедините его с другими блоками для создания скрипта.");
                break;
        }
        
        player.sendMessage("");
        player.sendMessage("§e[Будущая функция] Соединение блоков и создание скриптов");
    }

    /**
     * Возвращает отображаемое имя блока
     */
    private String getBlockDisplayName(BlockType blockType) {
        switch (blockType) {
            case EVENT_PLAYER_JOIN: return "Игрок зашел";
            case EVENT_PLAYER_QUIT: return "Игрок вышел";
            case EVENT_PLAYER_INTERACT: return "Игрок кликнул";
            case ACTION_SEND_MESSAGE: return "Отправить сообщение";
            case ACTION_TELEPORT_PLAYER: return "Телепортировать";
            case CONDITION_HAS_ITEM: return "Есть предмет";
            case VARIABLE_SET: return "Присвоить переменную";
            default: return blockType.name();
        }
    }

    /**
     * Преобразует Location в строку
     */
    private String locationToString(Location location) {
        return String.format("(%d, %d, %d)", 
            location.getBlockX(), 
            location.getBlockY(), 
            location.getBlockZ());
    }

    /**
     * Возвращает карту размещенных блоков (для отладки)
     */
    public Map<Location, BlockType> getPlacedCodeBlocks() {
        return placedCodeBlocks;
    }

    /**
     * Возвращает карту соединений блоков (для отладки)
     */
    public Map<Location, Location> getBlockConnections() {
        return blockConnections;
    }
}
