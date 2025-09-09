package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.interfaces.ITrustedPlayerManager;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import java.util.logging.Logger;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Обрабатывает размещение и взаимодействие с блоками кодирования
 */
public class BlockPlacementHandler implements Listener {
    private static final Logger log = Logger.getLogger(BlockPlacementHandler.class.getName());
    
    private final MegaCreative plugin;
    private final ITrustedPlayerManager trustedPlayerManager;
    private final BlockConfigService blockConfigService;
    private final Map<Location, CodeBlock> blockCodeBlocks = new HashMap<>();
    private final Map<UUID, Boolean> playerVisualizationStates = new HashMap<>();
    private final Map<UUID, Boolean> playerDebugStates = new HashMap<>();
    private final Map<UUID, Location> playerSelections = new HashMap<>();
    private final Map<UUID, CodeBlock> clipboard = new HashMap<>(); // Буфер обмена для копирования

    public BlockPlacementHandler(MegaCreative plugin) {
        this.plugin = plugin;
        ServiceRegistry registry = plugin.getServiceRegistry();
        this.trustedPlayerManager = registry.getTrustedPlayerManager();
        this.blockConfigService = registry.getBlockConfigService();
    }
    
    /**
     * Очищает данные игрока при отключении
     * @param playerId UUID игрока
     */
    public void cleanUpPlayerData(UUID playerId) {
        playerVisualizationStates.remove(playerId);
        playerDebugStates.remove(playerId);
        playerSelections.remove(playerId);
        clipboard.remove(playerId);
    }

    /**
     * Обрабатывает размещение блоков кодирования
     */
    @EventHandler(priority = EventPriority.HIGH) // Run before AutoConnectionManager (MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        ItemStack itemInHand = event.getItemInHand();
        
        // Получаем конфигурацию по предмету в руке, а не по материалу
        String displayName = itemInHand.hasItemMeta() ? org.bukkit.ChatColor.stripColor(itemInHand.getItemMeta().getDisplayName()) : "";
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByDisplayName(displayName);

        // Если это не наш блок кода, игнорируем
        if (config == null) {
            return;
        }

        // Создаем CodeBlock с правильным ID действия из конфига
        CodeBlock newCodeBlock = new CodeBlock(block.getType(), config.getId());
        blockCodeBlocks.put(block.getLocation(), newCodeBlock);
        
        // Устанавливаем одну табличку с правильным отображаемым именем
        setSignOnBlock(block.getLocation(), config.getDisplayName());
        
        player.sendMessage("§a✓ Блок кода размещен: " + config.getDisplayName());
        player.sendMessage("§7Кликните правой кнопкой для настройки");
    }

    /**
     * Обрабатывает разрушение блоков кодирования
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        
        // Удаляем блок из нашей карты
        if (blockCodeBlocks.containsKey(loc)) {
            blockCodeBlocks.remove(loc);
            
            // Удаляем табличку, если она есть
            removeSignFromBlock(loc);
        }
    }

    /**
     * Обрабатывает взаимодействие с блоками
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        
        // Проверяем железный слиток для создания данных
        if (itemInHand.getType() == Material.IRON_INGOT && itemInHand.hasItemMeta() &&
            itemInHand.getItemMeta().getDisplayName().contains(CodingItems.DATA_CREATOR_NAME)) {
            event.setCancelled(true);
            // Убираем ссылку на несуществующий DataGUI
            return;
        }
        
        // Остальная логика только для кликов по уже существующим блокам
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        
        Location location = clickedBlock.getLocation();
        
        // Проверяем, есть ли уже блок кода на этой локации
        if (blockCodeBlocks.containsKey(location)) {
            // Предотвращаем открытие GUI, если в руке инструмент
            if (isTool(itemInHand)) {
                return;
            }
            
            event.setCancelled(true); // Важно, чтобы не открылся, например, верстак
            
            // Открываем GUI конфигурации блока
            plugin.getServiceRegistry().getBlockConfigManager().openConfigGUI(player, location);
        }
    }

    /**
     * Проверяет, находится ли игрок в мире разработки
     */
    public boolean isInDevWorld(Player player) {  // Changed from private to public
        String worldName = player.getWorld().getName();
        // Проверяем разные варианты названий миров разработки
        return worldName.contains("dev") || worldName.contains("Dev") || 
               worldName.contains("разработка") || worldName.contains("Разработка") ||
               worldName.contains("creative") || worldName.contains("Creative");
    }

    /**
     * Проверяет, является ли предмет инструментом
     */
    private boolean isTool(ItemStack item) {
        if (item == null) return false;
        
        Material type = item.getType();
        return type == Material.WOODEN_AXE || type == Material.STONE_AXE || 
               type == Material.IRON_AXE || type == Material.DIAMOND_AXE || 
               type == Material.NETHERITE_AXE || type == Material.WOODEN_PICKAXE || 
               type == Material.STONE_PICKAXE || type == Material.IRON_PICKAXE || 
               type == Material.DIAMOND_PICKAXE || type == Material.NETHERITE_PICKAXE ||
               type == Material.WOODEN_SHOVEL || type == Material.STONE_SHOVEL || 
               type == Material.IRON_SHOVEL || type == Material.DIAMOND_SHOVEL || 
               type == Material.NETHERITE_SHOVEL || type == Material.WOODEN_HOE || 
               type == Material.STONE_HOE || type == Material.IRON_HOE || 
               type == Material.DIAMOND_HOE || type == Material.NETHERITE_HOE;
    }

    /**
     * Исправленная логика установки таблички.
     */
    private void setSignOnBlock(Location location, String text) {
        removeSignFromBlock(location); // Сначала удаляем старые таблички

        Block block = location.getBlock();
        // Определяем приоритетные стороны для установки
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            if (signBlock.getType().isAir()) {
                signBlock.setType(Material.OAK_WALL_SIGN, false); // false - не вызывать физику
                
                WallSign wallSignData = (WallSign) signBlock.getBlockData();
                wallSignData.setFacing(face); // Табличка смотрит НА блок
                signBlock.setBlockData(wallSignData);
                
                Sign signState = (Sign) signBlock.getState();
                signState.setLine(0, "§8============");
                // Обрезаем текст, если он слишком длинный
                String line2 = text.length() > 15 ? text.substring(0, 15) : text;
                signState.setLine(1, line2);
                signState.setLine(2, "§7Кликните ПКМ");
                signState.setLine(3, "§8============");
                signState.update(true);
                
                return; // ВАЖНО: Выходим из метода после установки ПЕРВОЙ таблички
            }
        }
    }
    
    /**
     * Удаляет все таблички вокруг блока
     */
    private void removeSignFromBlock(Location location) {
        Block block = location.getBlock();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            if (signBlock.getBlockData() instanceof WallSign) {
                signBlock.setType(Material.AIR);
            }
        }
    }

    /**
     * Получает CodeBlock по локации
     */
    public CodeBlock getCodeBlock(Location location) {
        return blockCodeBlocks.get(location);
    }

    /**
     * Проверяет, есть ли CodeBlock по локации
     */
    public boolean hasCodeBlock(Location location) {
        return blockCodeBlocks.containsKey(location);
    }

    /**
     * Получает все CodeBlock'и
     */
    public Map<Location, CodeBlock> getAllCodeBlocks() {
        return new HashMap<>(blockCodeBlocks);
    }

    /**
     * Получает все CodeBlock'и (для совместимости)
     */
    public Map<Location, CodeBlock> getBlockCodeBlocks() {
        return new HashMap<>(blockCodeBlocks);
    }
    
    /**
     * Очищает все CodeBlock'и в мире
     */
    public void clearAllCodeBlocksInWorld(World world) {
        blockCodeBlocks.entrySet().removeIf(entry -> entry.getKey().getWorld().equals(world));
        plugin.getLogger().info("Cleared all code blocks from world: " + world.getName() + " in BlockPlacementHandler.");
    }
}