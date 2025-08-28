package com.megacreative.coding;

import com.megacreative.MegaCreative;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Обрабатывает размещение и взаимодействие с блоками кодирования
 */
public class BlockPlacementHandler implements Listener {
    
    private final MegaCreative plugin;
    private final Map<Location, CodeBlock> blockCodeBlocks = new HashMap<>();
    private final Map<UUID, Boolean> playerVisualizationStates = new HashMap<>();
    private final Map<UUID, Boolean> playerDebugStates = new HashMap<>();
    private final Map<UUID, Location> playerSelections = new HashMap<>();
    private final Map<UUID, CodeBlock> clipboard = new HashMap<>(); // Буфер обмена для копирования

    public BlockPlacementHandler(MegaCreative plugin) {
        this.plugin = plugin;
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
        Material mat = block.getType();
        
        // Проверяем, является ли предмет "блоком кода" по названию, а не просто по материалу.
        if (!isCodingBlock(event.getItemInHand())) {
            return;
        }
        if (!isInDevWorld(player)) {
            return;
        }
        
        // Проверяем права доверенного игрока
        if (!plugin.getTrustedPlayerManager().canCodeInDevWorld(player)) {
            event.setCancelled(true);
            return;
        }
        
        // Создаем "заготовку" блока кода сразу.
        CodeBlock newCodeBlock = new CodeBlock(mat, "Настройка..."); // Временное действие
        blockCodeBlocks.put(block.getLocation(), newCodeBlock);
        
        // Устанавливаем табличку на блок
        setSignOnBlock(block.getLocation(), "Настройка...");
        
        // Простая настройка блока без GUI
        handleSimpleBlockConfiguration(player, mat, block.getLocation());
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
     * Простая настройка блока без GUI
     */
    private void handleSimpleBlockConfiguration(Player player, Material material, Location location) {
        // Создаем простой CodeBlock с базовыми параметрами
        CodeBlock codeBlock = new CodeBlock(material, "Блок кода");
        blockCodeBlocks.put(location, codeBlock);
        
        // Обновляем табличку
        setSignOnBlock(location, "Блок кода готов");
    }

    /**
     * Проверяет, находится ли игрок в мире разработки
     */
    private boolean isInDevWorld(Player player) {
        String worldName = player.getWorld().getName();
        // Проверяем разные варианты названий миров разработки
        return worldName.contains("dev") || worldName.contains("Dev") || 
               worldName.contains("разработка") || worldName.contains("Разработка") ||
               worldName.contains("creative") || worldName.contains("Creative");
    }

    /**
     * Проверяет, является ли предмет блоком кода
     */
    private boolean isCodingBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        
        // Проверяем все возможные названия блоков кода
        return displayName.contains(CodingItems.EVENT_BLOCK_NAME) ||
               displayName.contains(CodingItems.ACTION_BLOCK_NAME) ||
               displayName.contains(CodingItems.CONDITION_BLOCK_NAME) ||
               displayName.contains(CodingItems.VARIABLE_BLOCK_NAME) ||
               displayName.contains(CodingItems.REPEAT_TRIGGER_BLOCK_NAME) ||
               displayName.contains(CodingItems.SAVE_FUNCTION_BLOCK_NAME) ||
               displayName.contains(CodingItems.IF_MOB_BLOCK_NAME) ||
               displayName.contains(CodingItems.GET_DATA_BLOCK_NAME) ||
               displayName.contains(CodingItems.COPIER_TOOL_NAME) ||
               displayName.contains(CodingItems.DATA_CREATOR_NAME);
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
     * Устанавливает табличку на блок
     */
    private void setSignOnBlock(Location location, String text) {
        // Удаляем существующую табличку
        removeSignFromBlock(location);
        
        // Создаем новую табличку
        Block block = location.getBlock();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Location signLocation = location.clone().add(face.getDirection());
            Block signBlock = signLocation.getBlock();
            
            if (signBlock.getType().isAir()) {
                signBlock.setType(Material.OAK_WALL_SIGN);
                
                if (signBlock.getState() instanceof WallSign) {
                    org.bukkit.block.data.type.WallSign wallSignData = (org.bukkit.block.data.type.WallSign) signBlock.getBlockData();
                    wallSignData.setFacing(face.getOppositeFace());
                    signBlock.setBlockData(wallSignData);
                    
                    Sign sign = (Sign) signBlock.getState();
                    sign.setLine(0, text);
                    sign.update();
                    break;
                }
            }
        }
    }

    /**
     * Удаляет табличку с блока
     */
    private void removeSignFromBlock(Location location) {
        Block block = location.getBlock();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Location signLocation = location.clone().add(face.getDirection());
            Block signBlock = signLocation.getBlock();
            
            if (signBlock.getType().name().contains("SIGN")) {
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
}

