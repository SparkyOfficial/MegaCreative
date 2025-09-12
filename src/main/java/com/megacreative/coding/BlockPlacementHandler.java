package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.interfaces.ITrustedPlayerManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.gui.coding.ActionParameterGUI;
import com.megacreative.gui.coding.ActionSelectionGUI;
import com.megacreative.coding.values.DataValue; // 🔧 FIX: Add correct import
import com.megacreative.coding.values.types.AnyValue; // 🔧 FIX: Add correct import
import com.megacreative.coding.values.types.TextValue; // 🔧 FIX: Add correct import
import com.megacreative.coding.values.types.NumberValue; // 🔧 FIX: Add correct import
import com.megacreative.coding.values.types.BooleanValue; // 🔧 FIX: Add correct import
import com.megacreative.coding.values.types.ListValue; // 🔧 FIX: Add correct import
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
import java.util.List; // 🔧 FIX: Add missing import

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
        
        // Проверяем, что это специальный предмет кодирования
        if (!itemInHand.hasItemMeta() || !itemInHand.getItemMeta().hasDisplayName()) {
            // Если это не специальный предмет, возможно, это обычный блок - его нужно запретить
            if (!blockConfigService.isCodeBlock(block.getType())) {
                // Особая обработка для поршней (скобок) - они могут не иметь конфига
                if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
                    CodeBlock newCodeBlock = new CodeBlock(block.getType(), "BRACKET"); // Уникальный ID для скобок
                    newCodeBlock.setBracketType(CodeBlock.BracketType.OPEN); // По умолчанию открывающая
                    setPistonDirection(block, CodeBlock.BracketType.OPEN); // Задать направление
                    updateBracketSign(block.getLocation(), CodeBlock.BracketType.OPEN); // Повесить табличку
                    blockCodeBlocks.put(block.getLocation(), newCodeBlock);
                    
                    player.sendMessage("§a✓ Скобка размещена: " + CodeBlock.BracketType.OPEN.getDisplayName());
                    player.sendMessage("§7Кликните правой кнопкой для смены типа");
                    return; // Завершаем обработку
                }
                return; // Это обычный блок, не кодовый
            }
        }
        
        // Получаем конфиг блока из предмета в руке
        String displayName = org.bukkit.ChatColor.stripColor(itemInHand.getItemMeta().getDisplayName());
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByDisplayName(displayName);
        
        if (config == null) {
            // Это не кодовый блок, запрещаем установку
            // event.setCancelled(true); // УБИРАЕМ ОТМЕНУ СОБЫТИЯ
            player.sendMessage("§cВы можете размещать только специальные блоки для кодирования!");
            return;
        }
        
        // ===============================================
        //           НОВАЯ ЛОГИКА КОНСТРУКТОРОВ
        // ===============================================
        
        // 1. Проверяем, является ли блок "конструктором"
        if (config.isConstructor()) {
            // 2. НЕ отменяем стандартное размещение блока, позволяем ему установиться
            // event.setCancelled(true); // УБИРАЕМ ОТМЕНУ СОБЫТИЯ
            
            // 3. Размещаем блок программно (без вызова события) - НЕ НУЖНО, ПОЗВОЛЯЕМ СОБЫТИЮ ЗАВЕРШИТЬСЯ
            
            // 4. Вызываем метод для постройки структуры на следующий тик
            // Запланируем создание структуры на следующий тик, чтобы событие BlockPlaceEvent полностью завершилось
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                buildStructureFor(event, config);
                
                // 5. Визуальная и аудио обратная связь
                player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.1);
                player.playSound(block.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
                
                player.sendMessage("§a✓ Структура " + config.getDisplayName() + " создана!");
                player.sendMessage("§7Кликните по табличке для настройки параметров");
            });
            
            return; // Завершаем обработку, чтобы не создавать блок дважды
        }
        
        // Для обычных блоков продолжаем стандартную логику
        // Создаем CodeBlock с ID из конфига
        String actionId = config.getId();
        
        // 🔧 FIX: Use default action if available for immediate functionality
        if (config.getDefaultAction() != null) {
            actionId = config.getDefaultAction();
        }
        
        CodeBlock newCodeBlock = new CodeBlock(block.getType(), actionId);
        
        // Special handling for bracket blocks (pistons)
        if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
            newCodeBlock.setBracketType(CodeBlock.BracketType.OPEN); // Default to opening bracket
            setPistonDirection(block, CodeBlock.BracketType.OPEN);
        }
        
        blockCodeBlocks.put(block.getLocation(), newCodeBlock);
        
        // Устанавливаем табличку с названием из конфига (для конструкторов табличка уже создана в buildStructureFor)
        if (!config.isConstructor()) {
            setSignOnBlock(block.getLocation(), config.getDisplayName());
        }
        
        // Визуальная и аудио обратная связь
        player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.1);
        player.playSound(block.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
        
        if (config.isConstructor()) {
            player.sendMessage("§a✓ Структура " + config.getDisplayName() + " создана!");
            player.sendMessage("§7Кликните по основному блоку для выбора действия");
        } else {
            player.sendMessage("§a✓ Блок кода размещен: " + config.getDisplayName());
            player.sendMessage("§7Кликните правой кнопкой для выбора действия");
        }
    }
    
    /**
     * Создает структуру для блоков-конструкторов (аналог WOOD из FrameLand)
     */
    private void buildStructureFor(BlockPlaceEvent event, BlockConfigService.BlockConfig config) {
        Block placedBlock = event.getBlock();
        Location loc = placedBlock.getLocation();
        World world = loc.getWorld();
        Player player = event.getPlayer();
        
        BlockConfigService.StructureConfig structure = config.getStructure();
        if (structure == null) {
            plugin.getLogger().warning("Constructor block " + config.getId() + " has no structure configuration!");
            return;
        }
        
        // 🎆 ENHANCED: More intuitive structure creation like FrameLand
        if (config.getType().equals("CONDITION") || config.getType().equals("CONTROL")) {
            int bracketDistance = structure.getBracketDistance();
            
            // Calculate optimal positioning based on surrounding blocks
            BlockFace buildDirection = findOptimalBuildDirection(loc, bracketDistance);
            
            // Create opening bracket (piston pointing inward)
            Location openBracketLoc = loc.clone().add(buildDirection.getModX(), 0, buildDirection.getModZ());
            
            // Create closing bracket (piston pointing outward)
            // 🔧 FIX: Correct bracket positioning - count from main block, not from open bracket
            Location closeBracketLoc = loc.clone().add(
                buildDirection.getModX() * (bracketDistance + 1), 
                0, 
                buildDirection.getModZ() * (bracketDistance + 1)
            );
            
            // 1. Create bracket pistons with proper orientation
            createBracketPiston(openBracketLoc, CodeBlock.BracketType.OPEN, player, buildDirection);
            createBracketPiston(closeBracketLoc, CodeBlock.BracketType.CLOSE, player, buildDirection.getOppositeFace());
            
            // 2. Create smart sign on main block that opens configuration GUI
            if (structure.hasSign()) {
                setSmartSignOnBlock(loc, config.getDisplayName(), config.getId());
            }
            
            // 3. Create container above main block for parameters (optional)
            if (config.getType().equals("CONDITION") && !config.getId().equals("else")) {
                spawnContainerAboveBlock(loc, config.getId());
            }
            
            // 4. Add visual effects for "magical" feeling
            addConstructionEffects(loc, player);
        }
        
        // Additional structure types can be added here
        // For example, EVENT blocks could spawn helper blocks
        else if (config.getType().equals("EVENT")) {
            // Event blocks get special treatment
            if (structure.hasSign()) {
                setSmartSignOnBlock(loc, config.getDisplayName() + " Event", config.getId());
            }
            
            // 🔧 FIX: Add "ore" block for event blocks to make them visible
            // Add diamond ore block to make the event block "magical"
            Location oreLoc = loc.clone().add(-1, 0, 0); // Place ore to the west of the event block
            if (oreLoc.getBlock().getType().isAir()) {
                oreLoc.getBlock().setType(Material.DIAMOND_ORE);
            }
            
            addConstructionEffects(loc, player);
        }
    }
    
    /**
     * 🎆 ENHANCED: Creates bracket piston with proper orientation
     */
    private void createBracketPiston(Location location, CodeBlock.BracketType bracketType, Player player, BlockFace facing) {
        Block pistonBlock = location.getWorld().getBlockAt(location);
        
        // Проверяем, что место свободно
        if (!pistonBlock.getType().isAir()) {
            return;
        }
        
        // Ставим поршень
        pistonBlock.setType(Material.PISTON);
        
        // Используем современный BlockData для установки направления
        org.bukkit.block.data.type.Piston pistonData = (org.bukkit.block.data.type.Piston) pistonBlock.getBlockData();
        
        // 🎆 ENHANCED: Smart piston orientation for FrameLand-style brackets
        if (bracketType == CodeBlock.BracketType.OPEN) {
            pistonData.setFacing(facing); // Points inward toward the structure
        } else {
            pistonData.setFacing(facing.getOppositeFace()); // Points outward from the structure
        }
        
        pistonBlock.setBlockData(pistonData);
        
        // Создаем CodeBlock для скобки
        CodeBlock bracketCodeBlock = new CodeBlock(Material.PISTON, "BRACKET");
        bracketCodeBlock.setBracketType(bracketType);
        blockCodeBlocks.put(location, bracketCodeBlock);
        
        // Добавляем табличку к скобке
        updateBracketSign(location, bracketType);
        
        // Add visual effects for FrameLand-style magic
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, location.add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, 1);
        player.playSound(location, org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
        
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

        }
    }

    /**
     * Обрабатывает разрушение блоков кодирования
     */
    @EventHandler(priority = EventPriority.HIGH) // Run before AutoConnectionManager
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Location loc = event.getBlock().getLocation();
        Player player = event.getPlayer();
        
        // Удаляем блок из нашей карты
        if (blockCodeBlocks.containsKey(loc)) {
            CodeBlock removedBlock = blockCodeBlocks.remove(loc);
            
            // Проверяем, является ли это блоком-конструктором
            String blockId = removedBlock != null ? removedBlock.getAction() : null;
            if (blockId != null) {
                BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(blockId);
                if (config != null && config.isConstructor()) {
                    // Это блок-конструктор, удаляем всю структуру
                    removeConstructorStructure(loc, config, player);
                }
                // ===============================================
                //           ДОРАБОТКА РАЗРУШЕНИЯ СТРУКТУР
                // ===============================================
                // Если это событие, удаляем "руду"
                else if (config != null && "EVENT".equals(config.getType())) {
                    Block oreBlock = loc.clone().add(-1, 0, 0).getBlock();
                    if (oreBlock.getType() == Material.DIAMOND_ORE) { // или другой соответствующей руды
                        oreBlock.setType(Material.AIR);
                    }
                }
            }
            
            // Удаляем табличку, если она есть
            removeSignFromBlock(loc);
            
            // Удаляем контейнер над блоком, если он есть
            removeContainerAboveBlock(loc);
            
            // AutoConnectionManager will handle disconnection automatically at MONITOR priority
            player.sendMessage("§cБлок кода удален!");
            
            plugin.getLogger().info("CodeBlock removed from " + loc + " with action: " + (removedBlock != null ? removedBlock.getAction() : "unknown"));
        }
    }
    
    /**
     * Удаляет всю структуру конструктора (скобки, блоки внутри)
     */
    private void removeConstructorStructure(Location mainBlockLoc, BlockConfigService.BlockConfig config, Player player) {
        BlockConfigService.StructureConfig structure = config.getStructure();
        if (structure == null) return;
        
        if (config.getType().equals("CONDITION") || config.getType().equals("CONTROL")) {
            int bracketDistance = structure.getBracketDistance();
            
            // Calculate optimal positioning based on surrounding blocks (same as creation)
            BlockFace buildDirection = findOptimalBuildDirection(mainBlockLoc, bracketDistance);
            
            // Места скобок
            Location openBracketLoc = mainBlockLoc.clone().add(buildDirection.getModX(), 0, buildDirection.getModZ());
            // 🔧 FIX: Correct bracket positioning - count from main block, not from open bracket
            Location closeBracketLoc = mainBlockLoc.clone().add(
                buildDirection.getModX() * (bracketDistance + 1), 
                0, 
                buildDirection.getModZ() * (bracketDistance + 1)
            );
            
            // Удаляем скобки-поршни
            removeBracketPiston(openBracketLoc, player);
            removeBracketPiston(closeBracketLoc, player);
            
            // Удаляем все блоки между скобками (предотвращаем мусор)
            for (int i = 1; i < bracketDistance; i++) {
                Location innerBlockLoc = mainBlockLoc.clone().add(
                    buildDirection.getModX() * i, 
                    0, 
                    buildDirection.getModZ() * i
                );
                if (blockCodeBlocks.containsKey(innerBlockLoc)) {
                    blockCodeBlocks.remove(innerBlockLoc);
                    removeSignFromBlock(innerBlockLoc);
                    removeContainerAboveBlock(innerBlockLoc);
                }
            }
            
            player.sendMessage("§eСтруктура " + config.getDisplayName() + " полностью удалена!");
        }
    }
    
    /**
     * Удаляет поршень-скобку
     */
    private void removeBracketPiston(Location location, Player player) {
        if (blockCodeBlocks.containsKey(location)) {
            blockCodeBlocks.remove(location);
            removeSignFromBlock(location);
            
            Block pistonBlock = location.getBlock();
            if (pistonBlock.getType() == Material.PISTON || pistonBlock.getType() == Material.STICKY_PISTON) {
                pistonBlock.setType(Material.AIR);
            }
            
            // plugin.getLogger().fine("Removed bracket piston at " + location); // УБИРАЕМ СПАМ
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
            // plugin.getLogger().fine("Removed container above code block at " + blockLocation); // УБИРАЕМ СПАМ
        }
    }

    /**
     * Обрабатывает взаимодействие с блоками
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Fix double firing by only processing main hand
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }
        
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
        
        // 🎆 ENHANCED: Check if player clicked on a smart sign
        if (clickedBlock.getType().name().contains("SIGN")) {
            if (handleSmartSignClick(clickedBlock, player)) {
                event.setCancelled(true);
                return;
            }
        }
        
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
            
            // Handle block interaction with proper GUI opening
            handleBlockInteraction(player, location);
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
     * Завершение интерактивности (возвращение "магии" FrameLand)
     * Обрабатывает взаимодействие с блоком кода для открытия соответствующего GUI
     */
    private void handleBlockInteraction(Player player, Location blockLocation) {
        CodeBlock codeBlock = blockCodeBlocks.get(blockLocation);
        if (codeBlock == null) return;
        
        // Если это поршень (скобка), переключаем тип
        if (codeBlock.isBracket()) {
            toggleBracketType(codeBlock, blockLocation.getBlock(), player);
            return;
        }

        // Если у блока еще нет действия, открываем GUI выбора действия
        if (codeBlock.getAction() == null || codeBlock.getAction().equals("NOT_SET") || blockConfigService.getBlockConfig(codeBlock.getAction()) == null) {
            // Вызываем GUI для выбора действия, которое ты уже создал
            openActionSelectionGUI(player, blockLocation, codeBlock.getMaterial());
        } else {
            // Иначе открываем GUI настройки параметров
            BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(codeBlock.getAction());
            if (config != null) {
                // Вызываем GUI для настройки параметров, которое ты уже создал
                openParameterConfigGUI(player, blockLocation, codeBlock, config);
            } else {
                player.sendMessage("§cОшибка: Не удалось найти конфигурацию для действия '" + codeBlock.getAction() + "'");
            }
        }
    }
    
    /**
     * Открывает GUI для выбора действия
     */
    private void openActionSelectionGUI(Player player, Location blockLocation, Material blockMaterial) {
        ActionSelectionGUI gui = new ActionSelectionGUI(plugin, player, blockLocation, blockMaterial);
        gui.open();
        
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(blockMaterial);
        String blockName = config != null ? config.getDisplayName() : blockMaterial.name();
        player.sendMessage("§eВыберите действие для блока: §f" + blockName);
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
                // plugin.getLogger().fine("Removed sign at " + signBlock.getLocation() + " near block at " + location); // УБИРАЕМ СПАМ
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
        
        // Set direction based on bracket type for FrameLand-style orientation
        if (bracketType == CodeBlock.BracketType.OPEN) {
            pistonData.setFacing(org.bukkit.block.BlockFace.EAST); // Pointing right for opening bracket {
        } else {
            pistonData.setFacing(org.bukkit.block.BlockFace.WEST); // Pointing left for closing bracket }
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
        
        // ВАЖНО: Сохраняем мир после изменения типа скобки
        var creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getWorldManager().saveWorld(creativeWorld);
        }
        
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
                
                org.bukkit.block.Sign signState = (Sign) signBlock.getState();
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
    
    /**
     * 🎆 ENHANCED: Finds optimal build direction based on available space
     */
    private BlockFace findOptimalBuildDirection(Location center, int bracketDistance) {
        BlockFace[] directions = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
        
        for (BlockFace direction : directions) {
            boolean hasSpace = true;
            
            // Check if we have enough space in this direction
            for (int i = 1; i <= bracketDistance + 1; i++) {
                Location checkLoc = center.clone().add(
                    direction.getModX() * i, 
                    0, 
                    direction.getModZ() * i
                );
                
                if (!checkLoc.getBlock().getType().isAir()) {
                    hasSpace = false;
                    break;
                }
            }
            
            if (hasSpace) {
                return direction;
            }
        }
        
        // Default to east if no space found
        return BlockFace.EAST;
    }
    
    /**
     * 🎆 ENHANCED: Adds magical construction effects
     */
    private void addConstructionEffects(Location location, Player player) {
        // Visual effects
        player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, 
            location.clone().add(0.5, 1.0, 0.5), 10, 0.3, 0.3, 0.3, 0.1);
        player.spawnParticle(org.bukkit.Particle.END_ROD, 
            location.clone().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.05);
        
        // Audio feedback
        player.playSound(location, org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.2f);
        player.playSound(location, org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        
        // Delayed sparkle effect
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.spawnParticle(org.bukkit.Particle.FIREWORKS_SPARK, 
                location.clone().add(0.5, 1.5, 0.5), 8, 0.4, 0.4, 0.4, 0.1);
        }, 10L);
    }
    
    /**
     * 🎆 ENHANCED: Handles smart sign clicks to open configuration GUIs
     * This restores the FrameLand "magic" of clicking signs to configure blocks
     */
    private boolean handleSmartSignClick(Block signBlock, Player player) {
        if (!(signBlock.getState() instanceof Sign)) {
            return false;
        }
        
        Sign sign = (Sign) signBlock.getState();
        
        // Check if this is a smart sign (has our special markers)
        String[] lines = sign.getLines();
        if (lines.length < 3 || !lines[2].contains("Клик для настройки")) {
            return false;
        }
        
        // Find the associated code block (sign should be adjacent to it)
        BlockFace[] adjacentFaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        
        for (BlockFace face : adjacentFaces) {
            Block adjacentBlock = signBlock.getRelative(face);
            Location blockLoc = adjacentBlock.getLocation();
            
            if (blockCodeBlocks.containsKey(blockLoc)) {
                CodeBlock codeBlock = blockCodeBlocks.get(blockLoc);
                
                // Open appropriate GUI based on block state
                if (codeBlock.getAction() == null || "NOT_SET".equals(codeBlock.getAction())) {
                    // No action set - open action selection GUI
                    openActionSelectionGUI(player, blockLoc, adjacentBlock.getType());
                } else {
                    // Action already set - open parameter configuration GUI
                    BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(codeBlock.getAction());
                    if (config != null) {
                        openParameterConfigGUI(player, blockLoc, codeBlock, config);
                    } else {
                        player.sendMessage("§cОшибка: Не удалось найти конфигурацию для действия " + codeBlock.getAction());
                    }
                }
                
                // Add magical click effects
                player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, 
                    signBlock.getLocation().add(0.5, 0.5, 0.5), 3, 0.1, 0.1, 0.1, 0.1);
                player.playSound(signBlock.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                
                return true; // Successfully handled
            }
        }
        
        return false; // Not a smart sign or no associated block found
    }
    
    /**
     * 🎆 ENHANCED: Creates smart sign that opens configuration GUI on right-click
     * This restores the FrameLand "magic" of clicking signs to configure blocks
     */
    private void setSmartSignOnBlock(Location location, String displayName, String blockId) {
        removeSignFromBlock(location); // Remove old signs first

        Block block = location.getBlock();
        // Define priority sides for installation
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            if (signBlock.getType().isAir()) {
                signBlock.setType(Material.OAK_WALL_SIGN, false); // false - don't trigger physics
                
                WallSign wallSignData = (WallSign) signBlock.getBlockData();
                wallSignData.setFacing(face); // Sign faces the block
                signBlock.setBlockData(wallSignData);
                
                Sign signState = (Sign) signBlock.getState();
                signState.setLine(0, "§6★★★★★★★★★★★★");
                // Trim text if too long
                String line1 = displayName.length() > 15 ? displayName.substring(0, 15) : displayName;
                signState.setLine(1, "§e" + line1);
                signState.setLine(2, "§a➜ Клик для настройки");
                signState.setLine(3, "§6★★★★★★★★★★★★");
                signState.update(true);
                
                return; // IMPORTANT: Exit after placing FIRST sign
            }
        }
    }
    
    /**
     * 🎆 ENHANCED: Recreates a CodeBlock from an existing physical block and sign
     * This is used during world loading to "hydrate" code blocks
     */
    public void recreateCodeBlockFromExisting(Block block, Sign sign) {
        Location location = block.getLocation();
        
        // Check if we already have this block registered
        if (blockCodeBlocks.containsKey(location)) {
            return; // Already exists
        }
        
        Material material = block.getType();
        String action = determineActionFromBlockAndSign(block, sign);
        
        // Create the CodeBlock
        CodeBlock codeBlock = new CodeBlock(material, action);
        
        // Special handling for bracket blocks
        if (material == Material.PISTON || material == Material.STICKY_PISTON) {
            // Determine bracket type from sign text
            String[] lines = sign.getLines();
            if (lines.length > 1) {
                String line2 = lines[1];
                if (line2.contains("{")) {
                    codeBlock.setBracketType(CodeBlock.BracketType.OPEN);
                } else if (line2.contains("}")) {
                    codeBlock.setBracketType(CodeBlock.BracketType.CLOSE);
                }
            }
        }
        
        // 🔧 FIX: Restore parameters from container above the block
        restoreParametersFromContainer(block, codeBlock);
        
        // Add to our tracking
        blockCodeBlocks.put(location, codeBlock);
        
        plugin.getLogger().fine("Recreated CodeBlock at " + location + " with action: " + action);
    }
    
    /**
     * 🔧 FIX: Restores parameters from container (chest) above the code block
     * This is used during world loading to "hydrate" code block parameters
     */
    private void restoreParametersFromContainer(Block block, CodeBlock codeBlock) {
        try {
            // Look for container above the block
            Location containerLocation = block.getLocation().clone().add(0, 1, 0);
            Block containerBlock = containerLocation.getBlock();
            
            // Check if it's a container block
            if (containerBlock.getState() instanceof org.bukkit.block.Container container) {
                org.bukkit.inventory.Inventory inventory = container.getInventory();
                
                // Convert ItemStacks to DataValue parameters
                convertItemStacksToParameters(inventory, codeBlock);
                
                plugin.getLogger().fine("Restored parameters for CodeBlock at " + block.getLocation() + " from container");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to restore parameters from container for block at " + block.getLocation() + ": " + e.getMessage());
        }
    }
    
    /**
     * Converts ItemStacks from GUI inventory to DataValue parameters in CodeBlock
     * This is a copy of the method from BlockConfigManager with necessary modifications
     */
    private void convertItemStacksToParameters(org.bukkit.inventory.Inventory inventory, CodeBlock codeBlock) {
        Map<String, com.megacreative.coding.values.DataValue> newParameters = new HashMap<>();
        int processedItems = 0;
        
        // Process each slot in the inventory
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            org.bukkit.inventory.ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            
            // Skip placeholder items
            if (isPlaceholderItem(item)) continue;
            
            // Try to determine parameter name for this slot
            String paramName = getParameterNameForSlot(codeBlock.getAction(), slot);
            if (paramName == null) {
                // Fallback: use generic slot-based parameter name
                paramName = "slot_" + slot;
            }
            
            // Convert ItemStack to DataValue
            com.megacreative.coding.values.DataValue paramValue = convertItemStackToDataValue(item);
            if (paramValue != null) {
                newParameters.put(paramName, paramValue);
                processedItems++;
            }
        }
        
        // Update CodeBlock parameters
        for (Map.Entry<String, com.megacreative.coding.values.DataValue> entry : newParameters.entrySet()) {
            codeBlock.setParameter(entry.getKey(), entry.getValue());
        }
        
        if (processedItems > 0) {
            plugin.getLogger().info("Converted " + processedItems + " ItemStacks to DataValue parameters for block " + codeBlock.getAction());
        }
    }
    
    /**
     * Converts an ItemStack to a DataValue
     * This is a copy of the method from BlockConfigManager with necessary modifications
     */
    private com.megacreative.coding.values.DataValue convertItemStackToDataValue(org.bukkit.inventory.ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return new com.megacreative.coding.values.types.AnyValue(null);
        }
        
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        String displayName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "";
        
        // Clean display name from color codes for processing
        String cleanName = displayName.replaceAll("§[0-9a-fk-or]", "");
        
        // 1. Try to extract value from existing parameter items (our converted items)
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                if (line.startsWith("§8Parameter: ")) {
                    // This is a parameter item we created - extract the value
                    return extractValueFromParameterItem(item, lore);
                }
            }
        }
        
        // 2. Try to detect type from material
        switch (item.getType()) {
            case PAPER:
                // Extract text from display name or use item name
                if (!cleanName.isEmpty()) {
                    return new com.megacreative.coding.values.types.TextValue(cleanName);
                } else {
                    return new com.megacreative.coding.values.types.TextValue("Текст");
                }
            
            case GOLD_NUGGET:
            case GOLD_INGOT:
                // Try to parse number from name or use amount
                if (!cleanName.isEmpty()) {
                    try {
                        String numberStr = cleanName.replaceAll("[^0-9.-]", "");
                        if (!numberStr.isEmpty()) {
                            return new com.megacreative.coding.values.types.NumberValue(Double.parseDouble(numberStr));
                        }
                    } catch (NumberFormatException ignored) {}
                }
                return new com.megacreative.coding.values.types.NumberValue(item.getAmount());
            
            case LIME_DYE:
                return new com.megacreative.coding.values.types.BooleanValue(true);
            case RED_DYE:
                return new com.megacreative.coding.values.types.BooleanValue(false);
            
            case CHEST:
            case BARREL:
                // Consider these as lists or containers
                return new com.megacreative.coding.values.types.ListValue(new java.util.ArrayList<>()); // 🔧 FIX: Pass empty list to constructor
            
            default:
                // For other items, create text value from name or material
                if (!cleanName.isEmpty()) {
                    return new com.megacreative.coding.values.types.TextValue(cleanName);
                } else {
                    // Use material name as text value
                    return new com.megacreative.coding.values.types.TextValue(item.getType().name().toLowerCase().replace("_", " "));
                }
        }
    }
    
    /**
     * Extracts value from a parameter item we created
     * This is a copy of the method from BlockConfigManager with necessary modifications
     */
    private com.megacreative.coding.values.DataValue extractValueFromParameterItem(org.bukkit.inventory.ItemStack item, List<String> lore) {
        // Look for "Value: " line in lore
        for (String line : lore) {
            String cleanLine = line.replaceAll("§[0-9a-fk-or]", "");
            if (cleanLine.startsWith("Value: ")) {
                String valueStr = cleanLine.substring(7); // Remove "Value: "
                
                // Check type from the previous line
                int index = lore.indexOf(line);
                if (index > 0) {
                    String typeLine = lore.get(index - 1).replaceAll("§[0-9a-fk-or]", "");
                    
                    if (typeLine.contains("Number")) {
                        try {
                            return new com.megacreative.coding.values.types.NumberValue(Double.parseDouble(valueStr));
                        } catch (NumberFormatException e) {
                            return new com.megacreative.coding.values.types.TextValue(valueStr);
                        }
                    } else if (typeLine.contains("Boolean")) {
                        return new com.megacreative.coding.values.types.BooleanValue("True".equalsIgnoreCase(valueStr));
                    } else if (typeLine.contains("List")) {
                        return new com.megacreative.coding.values.types.ListValue(new java.util.ArrayList<>()); // 🔧 FIX: Pass empty list to constructor
                    }
                }
                
                // Default to text
                return new com.megacreative.coding.values.types.TextValue(valueStr);
            }
        }
        
        // Fallback
        return new com.megacreative.coding.values.types.TextValue(item.getType().name().toLowerCase());
    }
    
    /**
     * Gets parameter name for a specific slot based on action type
     * This is a copy of the method from BlockConfigManager with necessary modifications
     */
    private String getParameterNameForSlot(String action, int slot) {
        // Action-specific parameter mapping based on coding_blocks.yml
        switch (action) {
            case "sendMessage":
                return slot == 0 ? "message" : "param_" + slot;
            case "teleport":
                return slot == 0 ? "coords" : "param_" + slot;
            case "giveItem":
                return switch (slot) {
                    case 0 -> "item";
                    case 1 -> "amount";
                    default -> "param_" + slot;
                };
            case "playSound":
                return switch (slot) {
                    case 0 -> "sound";
                    case 1 -> "volume";
                    case 2 -> "pitch";
                    default -> "param_" + slot;
                };
            case "effect":
                return switch (slot) {
                    case 0 -> "effect";
                    case 1 -> "duration";
                    case 2 -> "amplifier";
                    default -> "param_" + slot;
                };
            case "setVar":
            case "addVar":
            case "subVar":
            case "mulVar":
            case "divVar":
                return switch (slot) {
                    case 0 -> "var";
                    case 1 -> "value";
                    default -> "param_" + slot;
                };
            case "spawnMob":
                return switch (slot) {
                    case 0 -> "mob";
                    case 1 -> "amount";
                    default -> "param_" + slot;
                };
            case "wait":
                return slot == 0 ? "ticks" : "param_" + slot;
            case "randomNumber":
                return switch (slot) {
                    case 0 -> "min";
                    case 1 -> "max";
                    case 2 -> "var";
                    default -> "param_" + slot;
                };
            case "setTime":
                return slot == 0 ? "time" : "param_" + slot;
            case "setWeather":
                return slot == 0 ? "weather" : "param_" + slot;
            case "command":
                return slot == 0 ? "command" : "param_" + slot;
            case "broadcast":
                return slot == 0 ? "message" : "param_" + slot;
            case "healPlayer":
                return slot == 0 ? "amount" : "param_" + slot;
            case "explosion":
                return switch (slot) {
                    case 0 -> "power";
                    case 1 -> "breakBlocks";
                    default -> "param_" + slot;
                };
            case "setBlock":
                return switch (slot) {
                    case 0 -> "material";
                    case 1 -> "coords";
                    default -> "param_" + slot;
                };
            // Variable conditions (unified handling)
            case "compareVariable":
                return switch (slot) {
                    case 0 -> "var1";
                    case 1 -> "operator";
                    case 2 -> "var2";
                    default -> "param_" + slot;
                };
            case "ifVarEquals":
            case "ifVarGreater":
            case "ifVarLess":
                return switch (slot) {
                    case 0 -> "variable"; // Legacy parameter name for backward compatibility
                    case 1 -> "value";
                    default -> "param_" + slot;
                };
            case "hasItem":
                return slot == 0 ? "item" : "param_" + slot;
            case "isNearBlock":
                return switch (slot) {
                    case 0 -> "block";
                    case 1 -> "radius";
                    default -> "param_" + slot;
                };
            case "mobNear":
                return switch (slot) {
                    case 0 -> "mob";
                    case 1 -> "radius";
                    default -> "param_" + slot;
                };
            
            // Generic fallback
            default:
                return switch (slot) {
                    case 0 -> "message";
                    case 1 -> "amount";
                    case 2 -> "target";
                    case 3 -> "item";
                    case 4 -> "location";
                    default -> "param_" + slot;
                };
        }
    }
    
    /**
     * Checks if an ItemStack is a placeholder item
     * This is a copy of the method from BlockConfigManager with necessary modifications
     */
    private boolean isPlaceholderItem(org.bukkit.inventory.ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                if (line.contains("placeholder") || line.contains("Placeholder")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 🎆 ENHANCED: Determines the action for a block based on its material and sign
     */
    private String determineActionFromBlockAndSign(Block block, Sign sign) {
        Material material = block.getType();
        
        // Get the block configuration
        BlockConfigService.BlockConfig config = blockConfigService.getFirstBlockConfig(material);
        if (config != null) {
            // Check if there's a default action
            if (config.getDefaultAction() != null) {
                return config.getDefaultAction();
            }
            // Fallback to block ID
            return config.getId();
        }
        
        // Fallback for unknown blocks
        return "UNKNOWN";
    }
    
    /**
     * Gets the first block configuration for a material
     * @param material The material to look up
     * @return The block configuration or null if not found
     */
    public BlockConfigService.BlockConfig getBlockConfigForMaterial(Material material) {
        return blockConfigService.getFirstBlockConfig(material);
    }
}