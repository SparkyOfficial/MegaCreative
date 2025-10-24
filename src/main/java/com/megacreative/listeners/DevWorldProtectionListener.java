package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import com.megacreative.interfaces.ITrustedPlayerManager;
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
    private final ITrustedPlayerManager trustedPlayerManager;
    private final BlockConfigService blockConfigService;
    
    
    private static final Set<Material> ALLOWED_TOOLS_AND_UTILITIES_HARDCODED = Set.of(
        Material.ENDER_CHEST,    
        Material.ANVIL,          
        Material.CHIPPED_ANVIL,  
        Material.DAMAGED_ANVIL,  
        Material.CRAFTING_TABLE, 
        Material.CHEST,          
        Material.BARREL,         
        Material.LECTERN,        
        Material.REDSTONE_WIRE,  
        Material.REPEATER,       
        Material.COMPARATOR,     
        Material.TORCH,          
        Material.REDSTONE_TORCH, 
        Material.BLUE_STAINED_GLASS, 
        Material.LIGHT_BLUE_STAINED_GLASS, 
        Material.GRAY_STAINED_GLASS,  
        Material.WHITE_STAINED_GLASS,      
        
        
        Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
        Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX,
        Material.LIME_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
        Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
        Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.GREEN_SHULKER_BOX,
        Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX
    );
    
    
    private final Set<Material> allPermittedPlaceAndBreakBlocks = new HashSet<>();
    
    
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

    
    public DevWorldProtectionListener(MegaCreative plugin, ITrustedPlayerManager trustedPlayerManager, BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.trustedPlayerManager = trustedPlayerManager;
        this.blockConfigService = blockConfigService;
        
    }

    public boolean isInDevWorld(Player player) {
        String worldName = player.getWorld().getName();
        
        return worldName.contains("dev") || worldName.contains("Dev") || 
               worldName.contains("разработка") || worldName.contains("Разработка") ||
               worldName.contains("creative") || worldName.contains("Creative") ||
               worldName.contains("-code") || worldName.endsWith("-code") || 
               worldName.contains("_code") || worldName.endsWith("_dev") ||
               worldName.contains("megacreative_") || worldName.contains("DEV") ||
               
               (worldName.startsWith("megacreative_") && worldName.endsWith("-code"));
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
               displayName.contains(CodingItems.REPEAT_BLOCK_NAME) ||
               displayName.contains(CodingItems.CALL_FUNCTION_BLOCK_NAME) ||
               displayName.contains(CodingItems.SAVE_FUNCTION_BLOCK_NAME) ||
               displayName.contains(CodingItems.REPEAT_TRIGGER_BLOCK_NAME) ||
               displayName.contains(CodingItems.BRACKET_BLOCK_NAME) ||
               displayName.contains(CodingItems.ARROW_NOT_NAME) ||
               displayName.contains(CodingItems.GAME_VALUE_NAME) ||
               displayName.contains(CodingItems.DATA_CREATOR_NAME) ||
               displayName.contains(CodingItems.CODE_MOVER_NAME);
    }

    /**
     * Инициализирует список разрешенных блоков
     * Должен вызываться после полной инициализации BlockConfigService
     */
    public void initializeDynamicAllowedBlocks() {
        allPermittedPlaceAndBreakBlocks.clear();
        allPermittedPlaceAndBreakBlocks.addAll(ALLOWED_TOOLS_AND_UTILITIES_HARDCODED); 
        
        
        if (blockConfigService != null) {
            
            
            if (blockConfigService.getCodeBlockMaterials().isEmpty()) {
                plugin.getLogger().warning("DevWorldProtectionListener: BlockConfigService has empty code block materials, attempting to reload configuration");
                blockConfigService.reload();
            }
            
            allPermittedPlaceAndBreakBlocks.addAll(blockConfigService.getCodeBlockMaterials());
            plugin.getLogger().fine("DevWorldProtectionListener: Dynamically added " + blockConfigService.getCodeBlockMaterials().size() + " code blocks to permitted list.");
        } else {
            plugin.getLogger().severe("DevWorldProtectionListener: BlockConfigService is null during dynamic initialization. This indicates a ServiceRegistry initialization order issue.");
        }
    }
    
    /**
     * Reloads the block configuration
     * Should be called when the block configuration changes
     */
    public void reloadBlockConfig() {
        if (blockConfigService != null) {
            blockConfigService.reload();
            initializeDynamicAllowedBlocks();
            plugin.getLogger().fine("DevWorldProtectionListener: Block configuration reloaded and permissions updated.");
        }
    }
    
    /**
     * Lazy initialization of allowed blocks
     * This ensures that the allowed blocks are properly initialized even if the initial initialization failed
     */
    private void ensureAllowedBlocksAreInitialized() {
        
        
        if (allPermittedPlaceAndBreakBlocks.size() <= ALLOWED_TOOLS_AND_UTILITIES_HARDCODED.size()) {
            plugin.getLogger().fine("DevWorldProtectionListener: Reinitializing allowed blocks");
            initializeDynamicAllowedBlocks();
        }
    }
    
    
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!isInDevWorld(player)) return;
        
        
        ensureAllowedBlocksAreInitialized();
        
        Material placedMaterial = event.getBlockPlaced().getType();
        
        
        if (!isMaterialAllowedInDevWorldForAction(placedMaterial)) {
            event.setCancelled(true);
            player.sendMessage("§cВы не можете размещать этот блок в мире разработки!");
            return;
        }

        
        if (isMaterialAConfiguredCodeBlock(placedMaterial)) {
            CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null && !creativeWorld.canCode(player)) {
                event.setCancelled(true);
                player.sendMessage("§cУ вас нет прав на размещение блоков кода в этом мире!");
                return;
            }
            
            
            if (!isValidCodeBlockPlacement(event.getBlockPlaced(), player)) {
                event.setCancelled(true);
                return; 
            }
        }
        
        
        
        
        
    }
    
    
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isInDevWorld(player)) return;

        Material brokenBlockType = event.getBlock().getType();
        
        
        if (brokenBlockType == Material.BARRIER || brokenBlockType == Material.BEACON || 
            (brokenBlockType.name().contains("GLASS") && brokenBlockType.name().contains("STAINED"))) {
            event.setCancelled(true);
            player.sendMessage("§cНельзя ломать элементы платформы разработки!");
            return;
        }

        
        
        if (isMaterialAConfiguredCodeBlock(brokenBlockType) || ALLOWED_TOOLS_AND_UTILITIES_HARDCODED.contains(brokenBlockType)) {
             CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld != null && !creativeWorld.canCode(player)) {
                event.setCancelled(true);
                player.sendMessage("§cУ вас нет прав на удаление блоков в этом мире!");
            }
        } else {
            
            event.setCancelled(true);
            player.sendMessage("§cВы не можете ломать этот блок в мире разработки!");
        }
    }
    
    
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isInDevWorld(player)) return;

        
        if (plugin != null && plugin.getServiceRegistry() != null) {
            PlayerModeManager modeManager = plugin.getServiceRegistry().getPlayerModeManager();
            if (modeManager.isInPlayMode(player)) {
                
                
                return;
            }
        }

        
        if (event.getClickedBlock() == null) return; 

        Material clickedBlockType = event.getClickedBlock().getType();

        
        
        if (!ALLOWED_INTERACT.contains(clickedBlockType) && !isMaterialAConfiguredCodeBlock(clickedBlockType)) {
             
            if (!((clickedBlockType.name().contains("GLASS") && clickedBlockType.name().contains("STAINED")) ||
                 clickedBlockType == Material.BEACON || clickedBlockType == Material.BARRIER)) {
                event.setCancelled(true);
                
                return;
            }
        }
        
        
        
        
    }
    
    
    
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (isInDevWorld(player) && isCodingItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            player.sendMessage("§cНельзя выбрасывать инструменты разработчика!");
        }
    }

    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!isInDevWorld(player)) return;

        ItemStack clickedItem = event.getCurrentItem();

        if (isCodingItem(clickedItem)) {
            
            if (event.getClickedInventory() != player.getInventory()) {
                event.setCancelled(true);
                player.sendMessage("§cНельзя перемещать инструменты разработчика в другие инвентари!");
            }
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
        
        
        if (!DevWorldGenerator.isValidCodePosition(location.getBlockX(), location.getBlockZ())) {
            player.sendMessage("§cБлоки кода можно размещать только на линиях кодирования!");
            return false;
        }
        
        
        org.bukkit.block.Block underBlock = location.clone().add(0, -1, 0).getBlock();
        Material underMaterial = underBlock.getType();
        
        
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
                
                if (positionX != 0 || glassMaterial != Material.BLUE_STAINED_GLASS) {
                    player.sendMessage("§cСобытия и функции можно размещать только на синем стекле в начале линии!");
                    return false;
                }
                break;
                
            case "CONTROL":
                
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
                
                if (positionX == 0) {
                    player.sendMessage("§cДействия, условия и переменные нельзя размещать в начале линии!");
                    return false;
                }
                
                if (glassMaterial != Material.GRAY_STAINED_GLASS && glassMaterial != Material.WHITE_STAINED_GLASS) {
                    player.sendMessage("§cЭтот блок можно размещать только на серых или белых линиях!");
                    return false;
                }
                break;
                
            default:
                
                if (glassMaterial != Material.GRAY_STAINED_GLASS && glassMaterial != Material.WHITE_STAINED_GLASS) {
                    player.sendMessage("§cЭтот блок можно размещать только на серых или белых линиях!");
                    return false;
                }
                break;
        }
        
        return true;
    }
}