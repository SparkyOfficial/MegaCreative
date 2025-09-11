package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.interfaces.ITrustedPlayerManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.gui.coding.ActionParameterGUI;
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
        if (event.isCancelled()) return; // Don't process if already cancelled by DevWorldProtectionListener
        
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        ItemStack itemInHand = event.getItemInHand();
        
        // Only process in dev worlds
        if (!isInDevWorld(player)) return;
        
        // Получаем конфигурацию по предмету в руке, а не по материалу
        String displayName = itemInHand.hasItemMeta() ? org.bukkit.ChatColor.stripColor(itemInHand.getItemMeta().getDisplayName()) : "";
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByDisplayName(displayName);

        // Особая обработка для поршней (скобок) - они могут не иметь конфига
        if (config == null) {
            // Если это поршень без конфига, значит это скобка
            if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
                CodeBlock newCodeBlock = new CodeBlock(block.getType(), "BRACKET"); // Уникальный ID для скобок
                newCodeBlock.setBracketType(CodeBlock.BracketType.OPEN); // По умолчанию открывающая
                setPistonDirection(block, CodeBlock.BracketType.OPEN); // Задать направление
                updateBracketSign(block.getLocation(), CodeBlock.BracketType.OPEN); // Повесить табличку
                blockCodeBlocks.put(block.getLocation(), newCodeBlock);
                
                player.sendMessage("§a✓ Скобка размещена: " + CodeBlock.BracketType.OPEN.getDisplayName());
                player.sendMessage("§7Кликните правой кнопкой для смены типа");
                plugin.getLogger().fine("Bracket block created at " + block.getLocation() + " with type: OPEN");
                return; // Завершаем обработку
            }
            return; // Это обычный блок, не кодовый
        }

        // Создаем CodeBlock с правильным ID действия из конфига
        CodeBlock newCodeBlock = new CodeBlock(block.getType(), config.getId());
        
        // Special handling for bracket blocks (pistons)
        if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
            newCodeBlock.setBracketType(CodeBlock.BracketType.OPEN); // Default to opening bracket
            setPistonDirection(block, CodeBlock.BracketType.OPEN);
        }
        
        blockCodeBlocks.put(block.getLocation(), newCodeBlock);
        
        // Устанавливаем одну табличку с правильным отображаемым именем
        setSignOnBlock(block.getLocation(), config.getDisplayName());
        
        // Создаем контейнер над блоком для параметров (только для ACTION, CONDITION и других типов, кроме EVENT, FUNCTION, CONTROL)
        if (!"EVENT".equals(config.getType()) && !"FUNCTION".equals(config.getType()) && !"CONTROL".equals(config.getType())) {
            spawnContainerAboveBlock(block.getLocation(), config.getId());
        }
        
        // Notify AutoConnectionManager to handle connections (it will run at MONITOR priority)
        // AutoConnectionManager will handle this automatically due to event priority ordering
        
        player.sendMessage("§a✓ Блок кода размещен: " + config.getDisplayName());
        player.sendMessage("§7Кликните правой кнопкой для настройки");
        
        plugin.getLogger().fine("CodeBlock created at " + block.getLocation() + " with action: " + config.getId());
    }

    /**
     * Создает контейнер (сундук) над блоком кода для параметров
     */
    private void spawnContainerAboveBlock(Location blockLocation, String actionId) {
        Location containerLocation = blockLocation.clone().add(0, 1, 0);
        Block containerBlock = containerLocation.getBlock();
        
        // Проверяем, что место свободно
        if (containerBlock.getType().isAir() || containerBlock.isLiquid()) {
            // Создаем сундук
            containerBlock.setType(Material.CHEST);
            
            // Здесь можно добавить дополнительную настройку сундука, если нужно
            plugin.getLogger().info("Spawned container above code block at " + blockLocation);
        }
    }

    /**
     * Обрабатывает разрушение блоков кодирования
     */
    @EventHandler(priority = EventPriority.HIGH) // Run before AutoConnectionManager
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Location loc = event.getBlock().getLocation();
        
        // Удаляем блок из нашей карты
        if (blockCodeBlocks.containsKey(loc)) {
            CodeBlock removedBlock = blockCodeBlocks.remove(loc);
            
            // Удаляем табличку, если она есть
            removeSignFromBlock(loc);
            
            // Удаляем контейнер над блоком, если он есть
            removeContainerAboveBlock(loc);
            
            // AutoConnectionManager will handle disconnection automatically at MONITOR priority
            event.getPlayer().sendMessage("§cБлок кода удален!");
            
            plugin.getLogger().info("CodeBlock removed from " + loc + " with action: " + (removedBlock != null ? removedBlock.getAction() : "unknown"));
        }
    }

    /**
     * Удаляет контейнер над блоком кода (улучшенная версия)
     */
    private void removeContainerAboveBlock(Location blockLocation) {
        Location containerLocation = blockLocation.clone().add(0, 1, 0);
        Block containerBlock = containerLocation.getBlock();
        
        // Проверяем все типы контейнеров
        if (containerBlock.getType() == Material.CHEST || 
            containerBlock.getType() == Material.TRAPPED_CHEST ||
            containerBlock.getType() == Material.BARREL ||
            containerBlock.getType() == Material.SHULKER_BOX ||
            containerBlock.getType().name().contains("SHULKER_BOX")) {
            
            // Очищаем содержимое перед удалением
            if (containerBlock.getState() instanceof org.bukkit.inventory.InventoryHolder) {
                org.bukkit.inventory.InventoryHolder holder = (org.bukkit.inventory.InventoryHolder) containerBlock.getState();
                holder.getInventory().clear();
            }
            
            containerBlock.setType(Material.AIR);
            plugin.getLogger().fine("Removed container above code block at " + blockLocation);
        }
    }

    /**
     * Обрабатывает взаимодействие с блоками
     */
    @EventHandler(priority = EventPriority.NORMAL)
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
        
        // Проверяем стрелу НЕ для инверсии условий
        if (itemInHand.getType() == Material.ARROW && itemInHand.hasItemMeta() &&
            itemInHand.getItemMeta().getDisplayName().contains(CodingItems.ARROW_NOT_NAME)) {
            handleArrowNotInteraction(player, event.getClickedBlock());
            event.setCancelled(true);
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
            
            // Открываем GUI конфигурации блока - используем новый улучшенный интерфейс
            CodeBlock codeBlock = blockCodeBlocks.get(location);
            
            // Special handling for bracket blocks - toggle bracket type instead of opening GUI
            if (codeBlock.isBracket()) {
                toggleBracketType(codeBlock, event.getClickedBlock(), player);
                return;
            }
            
            BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(codeBlock.getAction());
            
            if (config != null) {
                openParameterConfigGUI(player, location, codeBlock, config);
            } else {
                player.sendMessage("§cОшибка: Не удалось найти конфигурацию для действия " + codeBlock.getAction());
            }
            return;
        }
        
        // Проверяем, кликнул ли игрок по контейнеру над блоком кода
        Location blockBelow = location.clone().add(0, -1, 0);
        CodeBlock codeBlock = blockCodeBlocks.get(blockBelow);
        if (codeBlock != null) {
            event.setCancelled(true);
            
            // Проверяем тип блока - открываем специализированный GUI для параметров
            BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(codeBlock.getAction());
            if (config != null) {
                // Открываем уникальный drag-and-drop GUI для конкретного действия
                openParameterConfigGUI(player, blockBelow, codeBlock, config);
            } else {
                player.sendMessage("§cОшибка: Не удалось найти конфигурацию для действия " + codeBlock.getAction());
            }
        }
    }

    /**
     * Открывает уникальный drag-and-drop GUI для настройки параметров блока
     */
    private void openParameterConfigGUI(Player player, Location blockLocation, CodeBlock codeBlock, BlockConfigService.BlockConfig config) {
        // Создаем и открываем уникальный GUI для конкретного действия
        ActionParameterGUI gui = new ActionParameterGUI(
            plugin, player, blockLocation, codeBlock.getAction());
        gui.open();
        
        player.sendMessage("§eОткрытие настройки параметров для действия: §f" + config.getDisplayName());
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
     * Удаляет все таблички вокруг блока (улучшенная версия)
     */
    private void removeSignFromBlock(Location location) {
        Block block = location.getBlock();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, 
                            BlockFace.UP, BlockFace.DOWN}; // Проверяем все стороны
        
        for (BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            // Проверяем все типы табличек
            if (signBlock.getBlockData() instanceof WallSign || 
                signBlock.getType() == Material.OAK_SIGN ||
                signBlock.getType() == Material.OAK_WALL_SIGN ||
                signBlock.getType().name().contains("SIGN")) {
                
                signBlock.setType(Material.AIR);
                plugin.getLogger().fine("Removed sign at " + signBlock.getLocation() + " near block at " + location);
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
    
    /**
     * Синхронизирует CodeBlocks с AutoConnectionManager
     * Должно вызываться после полной инициализации
     */
    public void synchronizeWithAutoConnection() {
        com.megacreative.coding.AutoConnectionManager autoConnection = plugin.getServiceRegistry().getAutoConnectionManager();
        if (autoConnection != null) {
            autoConnection.synchronizeWithPlacementHandler(this);
            plugin.getLogger().info("BlockPlacementHandler synchronized with AutoConnectionManager");
        }
    }
    
    /**
     * Sets the direction of a piston based on bracket type
     */
    private void setPistonDirection(Block pistonBlock, CodeBlock.BracketType bracketType) {
        org.bukkit.block.data.type.Piston pistonData = (org.bukkit.block.data.type.Piston) pistonBlock.getBlockData();
        
        // Set direction based on bracket type
        if (bracketType == CodeBlock.BracketType.OPEN) {
            pistonData.setFacing(org.bukkit.block.BlockFace.EAST); // Pointing right >
        } else {
            pistonData.setFacing(org.bukkit.block.BlockFace.WEST); // Pointing left <
        }
        
        pistonBlock.setBlockData(pistonData);
    }
    
    /**
     * Toggles the bracket type and updates the visual representation
     */
    private void toggleBracketType(CodeBlock codeBlock, Block pistonBlock, Player player) {
        CodeBlock.BracketType currentType = codeBlock.getBracketType();
        CodeBlock.BracketType newType = (currentType == CodeBlock.BracketType.OPEN) ? 
            CodeBlock.BracketType.CLOSE : CodeBlock.BracketType.OPEN;
        
        codeBlock.setBracketType(newType);
        setPistonDirection(pistonBlock, newType);
        
        player.sendMessage("§aСкобка изменена на: §f" + newType.getSymbol() + " " + newType.getDisplayName());
        
        // Update the sign to reflect the new bracket type
        updateBracketSign(pistonBlock.getLocation(), newType);
        
        plugin.getLogger().info("Bracket type toggled to: " + newType + " at " + pistonBlock.getLocation());
    }
    
    /**
     * Updates the sign for a bracket block
     */
    private void updateBracketSign(Location location, CodeBlock.BracketType bracketType) {
        // Remove old sign and create new one with bracket info
        removeSignFromBlock(location);
        
        Block block = location.getBlock();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            if (signBlock.getType().isAir()) {
                signBlock.setType(Material.OAK_WALL_SIGN, false);
                
                org.bukkit.block.data.type.WallSign wallSignData = (org.bukkit.block.data.type.WallSign) signBlock.getBlockData();
                wallSignData.setFacing(face);
                signBlock.setBlockData(wallSignData);
                
                org.bukkit.block.Sign signState = (org.bukkit.block.Sign) signBlock.getState();
                signState.setLine(0, "§8============");
                signState.setLine(1, "§6" + bracketType.getSymbol() + " Скобка");
                signState.setLine(2, "§7ПКМ для смены");
                signState.setLine(3, "§8============");
                signState.update(true);
                
                return;
            }
        }
    }
    
    /**
     * Handles Arrow NOT interaction for negating conditions
     */
    private void handleArrowNotInteraction(Player player, Block clickedBlock) {
        if (clickedBlock == null) {
            player.sendMessage("§cОшибка: Не удалось определить блок!");
            return;
        }
        
        Location location = clickedBlock.getLocation();
        CodeBlock codeBlock = blockCodeBlocks.get(location);
        
        if (codeBlock == null) {
            player.sendMessage("§cОшибка: Это не блок кода!");
            return;
        }
        
        // Проверяем, является ли это блоком условия
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(codeBlock.getAction());
        if (config == null || !"CONDITION".equals(config.getType())) {
            player.sendMessage("§cОшибка: Стрелку НЕ можно применять только к блокам условий!");
            return;
        }
        
        // Переключаем параметр negated
        boolean currentNegated = false;
        if (codeBlock.getParameter("negated") != null) {
            currentNegated = Boolean.parseBoolean(codeBlock.getParameter("negated").toString());
        }
        
        boolean newNegated = !currentNegated;
        codeBlock.setParameter("negated", newNegated);
        
        // Обновляем табличку, чтобы показать состояние отрицания
        updateConditionSign(location, config.getDisplayName(), newNegated);
        
        if (newNegated) {
            player.sendMessage("§a✓ Отрицание добавлено к условию: §fНЕ " + config.getDisplayName());
        } else {
            player.sendMessage("§c✗ Отрицание убрано с условия: §f" + config.getDisplayName());
        }
        
        plugin.getLogger().info("Arrow NOT applied to condition block at " + location + ", negated: " + newNegated);
    }
    
    /**
     * Updates the sign for a condition block to show negation status
     */
    private void updateConditionSign(Location location, String displayName, boolean negated) {
        // Удаляем старую табличку и создаем новую
        removeSignFromBlock(location);
        
        Block block = location.getBlock();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            if (signBlock.getType().isAir()) {
                signBlock.setType(Material.OAK_WALL_SIGN, false);
                
                org.bukkit.block.data.type.WallSign wallSignData = (org.bukkit.block.data.type.WallSign) signBlock.getBlockData();
                wallSignData.setFacing(face);
                signBlock.setBlockData(wallSignData);
                
                org.bukkit.block.Sign signState = (org.bukkit.block.Sign) signBlock.getState();
                signState.setLine(0, "§8============");
                
                if (negated) {
                    signState.setLine(1, "§cНЕ " + displayName.substring(0, Math.min(displayName.length(), 12)));
                    signState.setLine(2, "§7(отрицание)");
                } else {
                    String line2 = displayName.length() > 15 ? displayName.substring(0, 15) : displayName;
                    signState.setLine(1, line2);
                    signState.setLine(2, "§7Кликните ПКМ");
                }
                signState.setLine(3, "§8============");
                signState.update(true);
                
                return;
            }
        }
    }
}