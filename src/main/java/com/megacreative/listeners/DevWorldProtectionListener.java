package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import com.megacreative.managers.TrustedPlayerManager;
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
    private final TrustedPlayerManager trustedPlayerManager;
    private final BlockConfigService blockConfigService;
    
    // Разрешенные материалы для размещения в dev-мире (будет динамически обновлен)
    private final Set<Material> allowedDevWorldBlocks = new HashSet<>();
    
    // Жестко закодированные разрешенные материалы для инструментов разработчика
    private static final Set<Material> ALLOWED_BLOCKS_INITIAL_HARDCODED = Set.of(
        Material.ENDER_CHEST,    // Эндер сундук
        Material.ANVIL,          // Наковальня
        Material.CHIPPED_ANVIL,  // Поврежденная наковальня
        Material.DAMAGED_ANVIL,  // Сильно поврежденная наковальня
        Material.CRAFTING_TABLE, // Верстак
        Material.CHEST,          // Сундук
        Material.BARREL,         // Бочка
        Material.LECTERN,        // Кафедра
        Material.REDSTONE_WIRE,  // Редстоун провод
        Material.REPEATER,       // Редстоун повторитель
        Material.COMPARATOR,     // Редстоун компаратор
        Material.TORCH,          // Факел
        Material.REDSTONE_TORCH, // Редстоун факел
        
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
        Material.DAMAGED_ANVIL, Material.CRAFTING_TABLE, Material.CHEST,
        Material.BARREL, Material.LECTERN, Material.SHULKER_BOX,
        Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX,
        Material.LIME_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
        Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
        Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
        Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX
    );

    // Конструктор должен принимать зависимости
    public DevWorldProtectionListener(MegaCreative plugin, TrustedPlayerManager trustedPlayerManager, BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.trustedPlayerManager = trustedPlayerManager;
        this.blockConfigService = blockConfigService;
        initializeAllowedBlocks(); // Вызвать метод инициализации
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
        
        Material blockType = event.getBlockPlaced().getType(); // Важно: getBlockPlaced(), а не getBlock()
        
        // Debug message - можно удалить позже
        // player.sendMessage("§a[DEBUG] DevWorldProtectionListener: Попытка размещения блока: " + blockType.name());
        
        // Проверяем, разрешен ли этот материал
        if (!isMaterialPermittedInDevWorld(blockType)) {
            event.setCancelled(true);
            player.sendMessage("§cВ мире разработки можно размещать только разрешенные блоки и инструменты!");
            plugin.getLogger().fine("Placement denied for " + blockType.name() + " in dev world.");
            return;
        }
        
        // Для блоков кода проверяем права на кодирование
        if (isMaterialAConfiguredCodeBlock(blockType)) {
            CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null && !creativeWorld.canCode(player)) {
                event.setCancelled(true);
                player.sendMessage("§cУ вас нет прав на размещение блоков кода в этом мире!");
                plugin.getLogger().warning("Player " + player.getName() + " attempted to place code block " + blockType.name() + " without permissions.");
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
        
        // Запрещаем ломать блоки платформы (стекло, маяки, барьеры)
        if ((blockType.name().contains("GLASS") && blockType.name().contains("STAINED")) || 
            blockType == Material.BEACON || blockType == Material.BARRIER) {
            event.setCancelled(true);
            player.sendMessage("§cНельзя ломать элементы платформы разработки!");
            return;
        }
        
        // Если это наш разрешенный CodeBlock или инструмент разработчика
        if (isMaterialAConfiguredCodeBlock(blockType) || isMaterialPermittedInDevWorld(blockType)) {
            // Дополнительная проверка прав для кодинг-блоков/инструментов
            CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null && !creativeWorld.canCode(player)) {
                event.setCancelled(true);
                player.sendMessage("§cУ вас нет прав на удаление блоков в этом мире!");
                plugin.getLogger().warning("Player " + player.getName() + " attempted to break block " + blockType.name() + " without permissions.");
                return;
            }
        } else {
            // Все остальные блоки, которые не разрешены и не являются частью платформы, ЗАПРЕЩЕНО ломать
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
            if (!isMaterialPermittedInDevWorld(blockType) && !isMaterialAConfiguredCodeBlock(blockType)) {
                // Проверяем, не является ли это блоком платформы
                if ((blockType.name().contains("GLASS") && blockType.name().contains("STAINED")) || 
                    blockType == Material.BEACON || blockType == Material.BARRIER) {
                    event.setCancelled(true);
                    return;
                }
                
                // Для всех остальных блоков, которые не разрешены, отменяем взаимодействие
                if (!ALLOWED_INTERACT.contains(blockType)) {
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
     * Инициализирует список разрешенных блоков
     * Должен вызываться после полной инициализации BlockConfigService
     */
    public void initializeAllowedBlocks() {
        allowedDevWorldBlocks.clear();
        allowedDevWorldBlocks.addAll(ALLOWED_BLOCKS_INITIAL_HARDCODED); // Добавляем основные
        
        // Получаем материалы блоков кода из BlockConfigService
        if (blockConfigService != null) {
            allowedDevWorldBlocks.addAll(blockConfigService.getCodeBlockMaterials());
            plugin.getLogger().info("DevWorldProtectionListener: Добавлено " + blockConfigService.getCodeBlockMaterials().size() + " блоков кода к разрешенным.");
        } else {
            plugin.getLogger().warning("DevWorldProtectionListener: BlockConfigService не доступен при инициализации ALLOWED_BLOCKS.");
        }
    }
    
    /**
     * Проверяет, является ли материал блоком кода
     */
    public boolean isMaterialAConfiguredCodeBlock(Material material) {
        return blockConfigService != null && blockConfigService.isCodeBlock(material);
    }
    
    /**
     * Проверяет, разрешен ли материал для использования в dev-мире
     */
    public boolean isMaterialPermittedInDevWorld(Material material) {
        return allowedDevWorldBlocks.contains(material);
    }
    
    /**
     * Получает список разрешенных блоков
     */
    public Set<Material> getAllowedBlocks() {
        return new HashSet<>(allowedDevWorldBlocks);
    }
}