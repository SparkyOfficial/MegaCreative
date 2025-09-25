package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import com.megacreative.managers.TrustedPlayerManager;
import com.megacreative.managers.PlayerModeManager;
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
 * Updated to use the new dynamic configuration system
 */
public class DevWorldProtectionListener implements Listener {

    private final MegaCreative plugin;
    private final TrustedPlayerManager trustedPlayerManager;
    private final BlockConfigService blockConfigService;
    
    // Жестко закодированные разрешенные материалы для инструментов разработчика
    private static final Set<Material> ALLOWED_TOOLS_AND_UTILITIES_HARDCODED = Set.of(
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
        Material.BLUE_STAINED_GLASS, // Платформа для размещения блоков кода (события/функции)
        Material.LIGHT_BLUE_STAINED_GLASS, // Платформа для соединений между линиями
        Material.GRAY_STAINED_GLASS,  // Платформа для размещения блоков кода
        Material.WHITE_STAINED_GLASS,      // Платформа для размещения блоков кода
        
        // Шалкеры
        Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX,
        Material.LIME_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
        Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
        Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
        Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX
    );
    
    // Этот сет будет содержать ВСЕ разрешенные материалы (инструменты + кодовые блоки)
    private final Set<Material> allPermittedPlaceAndBreakBlocks = new HashSet<>();
    
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
        Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX,
        
        // ДОБАВЛЯЕМ ВСЕ ВИДЫ ТАБЛИЧЕК ДЛЯ "УМНЫХ ТАБЛИЧЕК"
        Material.OAK_SIGN, Material.OAK_WALL_SIGN,
        Material.SPRUCE_SIGN, Material.SPRUCE_WALL_SIGN,
        Material.BIRCH_SIGN, Material.BIRCH_WALL_SIGN,
        Material.JUNGLE_SIGN, Material.JUNGLE_WALL_SIGN,
        Material.ACACIA_SIGN, Material.ACACIA_WALL_SIGN,
        Material.DARK_OAK_SIGN, Material.DARK_OAK_WALL_SIGN,
        Material.MANGROVE_SIGN, Material.MANGROVE_WALL_SIGN,
        Material.CHERRY_SIGN, Material.CHERRY_WALL_SIGN,
        Material.BAMBOO_SIGN, Material.BAMBOO_WALL_SIGN,
        Material.CRIMSON_SIGN, Material.CRIMSON_WALL_SIGN,
        Material.WARPED_SIGN, Material.WARPED_WALL_SIGN
    );

    // Конструктор должен принимать зависимости
    public DevWorldProtectionListener(MegaCreative plugin, TrustedPlayerManager trustedPlayerManager, BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.trustedPlayerManager = trustedPlayerManager;
        this.blockConfigService = blockConfigService;
        // Здесь не инициализируем allowedDevWorldBlocks. Инициализируем после загрузки всех сервисов.
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
        
        Material placedMaterial = event.getBlockPlaced().getType();
        
        // 1. Сначала проверьте, можно ли поставить это вообще, используя динамически созданный список
        if (!isMaterialAllowedInDevWorldForAction(placedMaterial)) {
            event.setCancelled(true);
            player.sendMessage("§cВы не можете размещать этот блок в мире разработки!");
            return;
        }

        // 2. Проверяем права доступа к кодированию
        if (isMaterialAConfiguredCodeBlock(placedMaterial)) {
            CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null && !creativeWorld.canCode(player)) {
                event.setCancelled(true);
                player.sendMessage("§cУ вас нет прав на размещение блоков кода в этом мире!");
                return;
            }
            
            // 3. Проверяем правильность позиции блока кода (Reference system-like validation)
            if (!isValidCodeBlockPlacement(event.getBlockPlaced(), player)) {
                event.setCancelled(true);
                return; // Сообщение об ошибке уже отправлено в методе
            }
        }
        
        // Если блок является одним из `ALLOWED_TOOLS_AND_UTILITIES_HARDCODED`
        // (сундук, наковальня и т.д.), он будет разрешен уже первой проверкой.
        // `BlockPlacementHandler` затем создаст `CodeBlock` для настоящих блоков кода,
        // а для остальных - просто разместит их как есть.
    }
    
    // === ЗАЩИТА ОТ РАЗРУШЕНИЯ БЛОКОВ ===
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isInDevWorld(player)) return;

        Material brokenBlockType = event.getBlock().getType();
        
        // 1. Не даем ломать саму структуру платформы (стены из барьера, стекло линий)
        if (brokenBlockType == Material.BARRIER || brokenBlockType == Material.BEACON || 
            (brokenBlockType.name().contains("GLASS") && brokenBlockType.name().contains("STAINED"))) {
            event.setCancelled(true);
            player.sendMessage("§cНельзя ломать элементы платформы разработки!");
            return;
        }

        // 2. Проверяем, можно ли ломать этот блок вообще
        // Разрешаем ломать И настоящие блоки кодинга, И утилиты/контейнеры.
        if (isMaterialAConfiguredCodeBlock(brokenBlockType) || ALLOWED_TOOLS_AND_UTILITIES_HARDCODED.contains(brokenBlockType)) {
             CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null && !creativeWorld.canCode(player)) {
                event.setCancelled(true);
                player.sendMessage("§cУ вас нет прав на удаление блоков в этом мире!");
            }
        } else {
            // Если это не CodeBlock, не инструмент и не часть платформы, значит, это что-то нежелательное.
            event.setCancelled(true);
            player.sendMessage("§cВы не можете ломать этот блок в мире разработки!");
        }
    }
    
    // === ЗАЩИТА ВЗАИМОДЕЙСТВИЙ ===
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isInDevWorld(player)) return;

        // Check if player is in DEV mode
        if (plugin != null && plugin.getServiceRegistry() != null) {
            PlayerModeManager modeManager = plugin.getServiceRegistry().getPlayerModeManager();
            if (modeManager.isInPlayMode(player)) {
                // If player is in PLAY mode, don't allow interactions with coding blocks
                // This interaction may be part of their game, not development
                return;
            }
        }

        // Не отменяем событие, если это AIR
        if (event.getClickedBlock() == null) return; 

        Material clickedBlockType = event.getClickedBlock().getType();

        // 1. Если блок НЕ является разрешенным для взаимодействия
        //    (т.е. не является контейнером, наковальней, книжной полкой и т.д., И не является блоком кодинга)
        if (!ALLOWED_INTERACT.contains(clickedBlockType) && !isMaterialAConfiguredCodeBlock(clickedBlockType)) {
             // И при этом он не является блоком-основой платформы
            if (!((clickedBlockType.name().contains("GLASS") && clickedBlockType.name().contains("STAINED")) ||
                 clickedBlockType == Material.BEACON || clickedBlockType == Material.BARRIER)) {
                event.setCancelled(true);
                //player.sendMessage("§cВзаимодействие с этим блоком в мире разработки запрещено!");
                return;
            }
        }
        // Если взаимодействие разрешено, то BlockPlacementHandler или BlockContainerManager могут дальше обработать.
        // НЕ ставим тут setCancelled(true), чтобы дать возможность другим слушателям (нашим) сработать
        // Но `BlockPlacementHandler` и `BlockContainerManager` (которые будут слушателями на NORMAL)
        // должны уметь обрабатывать и отменять событие, если они хотят управлять им.
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
    public void initializeDynamicAllowedBlocks() {
        allPermittedPlaceAndBreakBlocks.clear();
        allPermittedPlaceAndBreakBlocks.addAll(ALLOWED_TOOLS_AND_UTILITIES_HARDCODED); 
        if (blockConfigService != null) {
            allPermittedPlaceAndBreakBlocks.addAll(blockConfigService.getCodeBlockMaterials());
            plugin.getLogger().info("DevWorldProtectionListener: Dynamically added " + blockConfigService.getCodeBlockMaterials().size() + " code blocks to permitted list.");
        } else {
            plugin.getLogger().severe("DevWorldProtectionListener: BlockConfigService is null during dynamic initialization. This indicates a ServiceRegistry initialization order issue.");
        }
    }
    
    /**
     * Проверяет, является ли материал блоком кода
     * Uses the new BlockConfigService to determine this dynamically
     */
    public boolean isMaterialAConfiguredCodeBlock(Material material) {
        return blockConfigService != null && blockConfigService.isCodeBlock(material);
    }
    
    /**
     * Переименуем для ясности: этот метод определяет, МОЖНО ли _поместить_ или _сломать_ блок этого типа.
     */
    public boolean isMaterialAllowedInDevWorldForAction(Material material) {
        // Мы хотим, чтобы ЛЮБОЙ код-блок можно было ставить, а также любые utility-блоки
        return isMaterialAConfiguredCodeBlock(material) || ALLOWED_TOOLS_AND_UTILITIES_HARDCODED.contains(material);
    }
    
    /**
     * Gets the list of allowed blocks
     * Returns a copy to prevent external modification
     */
    public Set<Material> getAllowedBlocks() {
        return new HashSet<>(allPermittedPlaceAndBreakBlocks);
    }
    
    /**
     * Validates that a code block is placed on the correct glass color platform
     * This implements reference system-like placement rules
     */
    private boolean isValidCodeBlockPlacement(org.bukkit.block.Block placedBlock, Player player) {
        org.bukkit.Location location = placedBlock.getLocation();
        
        // Check if we're in a valid code line position
        if (!DevWorldGenerator.isValidCodePosition(location.getBlockX(), location.getBlockZ())) {
            player.sendMessage("§cБлоки кода можно размещать только на линиях кодирования!");
            return false;
        }
        
        // Get the glass block underneath
        org.bukkit.block.Block underBlock = location.clone().add(0, -1, 0).getBlock();
        Material underMaterial = underBlock.getType();
        
        // Get block configuration from the item in hand
        org.bukkit.inventory.ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (!itemInHand.hasItemMeta() || !itemInHand.getItemMeta().hasDisplayName()) {
            player.sendMessage("§cИспользуйте специальные предметы кодирования!");
            return false;
        }
        
        String displayName = org.bukkit.ChatColor.stripColor(itemInHand.getItemMeta().getDisplayName());
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByDisplayName(displayName);
        
        if (config == null) {
            player.sendMessage("§cНеизвестный блок кодирования!");
            return false;
        }
        
        // Validate placement based on block type and glass color (Reference system rules)
        return validatePlacementByTypeAndGlass(config, underMaterial, location, player);
    }
    
    /**
     * Validates placement based on block type and glass color underneath
     * Implements reference system-like placement validation rules
     */
    private boolean validatePlacementByTypeAndGlass(BlockConfigService.BlockConfig config, Material glassMaterial, org.bukkit.Location location, Player player) {
        String blockType = config.getType();
        int positionX = location.getBlockX();
        
        switch (blockType) {
            case "EVENT":
            case "FUNCTION":
                // Events and functions can only be placed on blue glass at position 0
                if (positionX != 0 || glassMaterial != Material.BLUE_STAINED_GLASS) {
                    player.sendMessage("§cСобытия и функции можно размещать только на синем стекле в начале линии!");
                    return false;
                }
                break;
                
            case "CONTROL":
                // Control blocks (if, loops) can be placed on blue glass (start of line) or after other blocks
                if (positionX == 0 && glassMaterial != Material.BLUE_STAINED_GLASS) {
                    player.sendMessage("§cБлоки управления в начале линии можно размещать только на синем стекле!");
                    return false;
                } else if (positionX > 0 && glassMaterial != Material.GRAY_STAINED_GLASS && glassMaterial != Material.WHITE_STAINED_GLASS) {
                    player.sendMessage("§cБлоки управления можно размещать только на серых или белых линиях!");
                    return false;
                }
                break;
                
            case "ACTION":
            case "CONDITION":
            case "VARIABLE":
                // Actions, conditions, and variables cannot be placed at position 0
                if (positionX == 0) {
                    player.sendMessage("§cДействия, условия и переменные нельзя размещать в начале линии!");
                    return false;
                }
                // Must be placed on gray or white glass
                if (glassMaterial != Material.GRAY_STAINED_GLASS && glassMaterial != Material.WHITE_STAINED_GLASS) {
                    player.sendMessage("§cЭтот блок можно размещать только на серых или белых линиях!");
                    return false;
                }
                break;
                
            default:
                // For unknown types, allow placement on gray or white glass only
                if (glassMaterial != Material.GRAY_STAINED_GLASS && glassMaterial != Material.WHITE_STAINED_GLASS) {
                    player.sendMessage("§cЭтот блок можно размещать только на серых или белых линиях!");
                    return false;
                }
                break;
        }
        
        return true;
    }
    
    /**
     * Reloads the block configuration
     * Should be called when the block configuration changes
     */
    public void reloadBlockConfig() {
        if (blockConfigService != null) {
            blockConfigService.reload();
            initializeDynamicAllowedBlocks();
            plugin.getLogger().info("DevWorldProtectionListener: Block configuration reloaded and permissions updated.");
        }
    }
}