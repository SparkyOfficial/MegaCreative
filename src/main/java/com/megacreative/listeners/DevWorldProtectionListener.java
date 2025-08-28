package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import com.megacreative.models.CreativeWorld;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Защита dev-мира от нежелательных действий
 * Разрешает только размещение блоков кода и специальных инструментов
 */
public class DevWorldProtectionListener implements Listener {

    private final MegaCreative plugin;
    
    // Разрешенные материалы для размещения в dev-мире
    private static final Set<Material> ALLOWED_BLOCKS = Set.of(
        // Блоки кода (будут динамически обновлены из конфигурации)
        
        // Инструменты разработчика
        Material.ENDER_CHEST,    // Эндер сундук
        Material.ANVIL,          // Наковальня
        Material.CHIPPED_ANVIL,  // Поврежденная наковальня
        Material.DAMAGED_ANVIL,  // Сильно поврежденная наковальня
        Material.CRAFTING_TABLE, // Верстак
        
        // Шалкеры
        Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX,
        Material.LIME_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
        Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
        Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
        Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX
    );
    
    // Разрешенные материалы для взаимодействия
    private static final Set<Material> ALLOWED_INTERACT = Set.of(
        Material.ENDER_CHEST, Material.ANVIL, Material.CHIPPED_ANVIL, 
        Material.DAMAGED_ANVIL, Material.CRAFTING_TABLE
    );

    public DevWorldProtectionListener(MegaCreative plugin) {
        this.plugin = plugin;
    }

    public boolean isInDevWorld(Player player) {
        return player.getWorld().getName().endsWith("_dev");
    }

    private boolean isCodingItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        String displayName = item.getItemMeta().getDisplayName();
        return displayName.contains(CodingItems.EVENT_BLOCK_NAME) ||
               displayName.contains(CodingItems.CONDITION_BLOCK_NAME) ||
               displayName.contains(CodingItems.ACTION_BLOCK_NAME) ||
               displayName.contains(CodingItems.VARIABLE_BLOCK_NAME) ||
               displayName.contains(CodingItems.ELSE_BLOCK_NAME) ||
               displayName.contains(CodingItems.GAME_ACTION_BLOCK_NAME) ||
               displayName.contains(CodingItems.IF_VAR_BLOCK_NAME) ||
               displayName.contains(CodingItems.IF_GAME_BLOCK_NAME) ||
               displayName.contains(CodingItems.IF_MOB_BLOCK_NAME) ||
               displayName.contains(CodingItems.GET_DATA_BLOCK_NAME) ||

               displayName.contains(CodingItems.COPIER_TOOL_NAME) ||
               displayName.contains(CodingItems.DATA_CREATOR_NAME);
    }

    // === ЗАЩИТА ОТ РАЗМЕЩЕНИЯ НЕРАЗРЕШЕННЫХ БЛОКОВ ===
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!isInDevWorld(player)) return;
        
        Material blockType = event.getBlock().getType();
        
        // Debug message - можно удалить позже
        player.sendMessage("§a[DEBUG] DevWorldProtectionListener: Попытка размещения блока: " + blockType.name());
        
        // Проверяем, разрешен ли этот материал
        if (!isBlockAllowed(blockType)) {
            event.setCancelled(true);
            player.sendMessage("§cВ мире разработки можно размещать только блоки кода и инструменты разработчика!");
            return;
        }
        
        // Для блоков кода проверяем права на кодирование
        if (isCodeBlock(blockType)) {
            CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null && !creativeWorld.canCode(player)) {
                event.setCancelled(true);
                player.sendMessage("§cУ вас нет прав на размещение блоков кода в этом мире!");
                return;
            }
        }
    }
    
    // === ЗАЩИТА ОТ РАЗРУШЕНИЯ БЛОКОВ ===
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isInDevWorld(player)) return;
        
        Material blockType = event.getBlock().getType();
        
        // Запрещаем ломать блоки платформы (стекло)
        if (blockType.name().contains("GLASS") || blockType == Material.BEACON || blockType == Material.BARRIER) {
            event.setCancelled(true);
            player.sendMessage("§cНельзя ломать элементы платформы разработки!");
            return;
        }
        
        // Для блоков кода проверяем права
        if (isCodeBlock(blockType) || isBlockAllowed(blockType)) {
            CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null && !creativeWorld.canCode(player)) {
                event.setCancelled(true);
                player.sendMessage("§cУ вас нет прав на удаление блоков в этом мире!");
                return;
            }
        } else {
            // Запрещаем ломать любые другие блоки
            event.setCancelled(true);
            player.sendMessage("§cВ мире разработки можно ломать только блоки кода и инструменты разработчика!");
        }
    }
    
    // === ЗАЩИТА ВЗАИМОДЕЙСТВИЙ ===
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isInDevWorld(player)) return;
        
        if (event.getClickedBlock() != null) {
            Material blockType = event.getClickedBlock().getType();
            
            // Разрешаем взаимодействие только с определенными блоками
            if (!ALLOWED_INTERACT.contains(blockType) && !isCodeBlock(blockType)) {
                // Проверяем, не является ли это блоком платформы
                if (blockType.name().contains("GLASS") || blockType == Material.BEACON) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
    
    // === ЗАЩИТА ПРЕДМЕТОВ КОДИНГА ===
    
    // Запрещаем выкидывать предметы кодинга
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (isInDevWorld(player) && isCodingItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            player.sendMessage("§cНельзя выбрасывать инструменты разработчика!");
        }
    }

    // Запрещаем перемещать предметы кодинга в инвентаре (например, в сундук)
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!isInDevWorld(player)) return;

        ItemStack clickedItem = event.getCurrentItem();

        if (isCodingItem(clickedItem)) {
            // Разрешаем клики в своем инвентаре (hotbar/main), но отменяем любые другие
            if (event.getClickedInventory() != player.getInventory()) {
                event.setCancelled(true);
                player.sendMessage("§cНельзя перемещать инструменты разработчика в другие инвентари!");
            }
        }
    }
    
    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===
    
    /**
     * Проверяет, является ли материал блоком кода
     */
    public boolean isCodeBlock(Material material) {
        // Используем BlockConfigService для проверки
        return plugin.getServiceRegistry().getBlockConfigService().isCodeBlock(material);
    }
    
    /**
     * Получает список разрешенных блоков
     */
    public static Set<Material> getAllowedBlocks() {
        return ALLOWED_BLOCKS;
    }
    
    /**
     * Проверяет, разрешен ли блок для размещения в dev-мире
     */
    public static boolean isBlockAllowed(Material material) {
        // Проверяем, является ли материал кодовым блоком
        MegaCreative plugin = MegaCreative.getInstance();
        if (plugin != null && plugin.getServiceRegistry() != null) {
            if (plugin.getServiceRegistry().getBlockConfigService().isCodeBlock(material)) {
                return true;
            }
        }
        
        // Проверяем в статическом списке разрешенных блоков
        return ALLOWED_BLOCKS.contains(material);
    }
}