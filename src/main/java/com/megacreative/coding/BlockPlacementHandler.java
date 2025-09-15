package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.interfaces.ITrustedPlayerManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.gui.coding.ActionParameterGUI;
import com.megacreative.gui.coding.ActionSelectionGUI;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.AnyValue;
import com.megacreative.coding.values.types.TextValue;
import com.megacreative.coding.values.types.NumberValue;
import com.megacreative.coding.values.types.BooleanValue;
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodingItems;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

/**
 * Обрабатывает размещение и взаимодействие с блоками кодирования
 * Реализует стиль: универсальные блоки с настройкой через GUI
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
     * Sets the direction of a piston block
     */
    private void setPistonDirection(Block pistonBlock, CodeBlock.BracketType bracketType) {
        if (pistonBlock.getType() == Material.PISTON || pistonBlock.getType() == Material.STICKY_PISTON) {
            org.bukkit.block.data.type.Piston pistonData = (org.bukkit.block.data.type.Piston) pistonBlock.getBlockData();
            
            // For opening bracket, piston should point inward (toward the structure)
            // For closing bracket, piston should point outward (away from the structure)
            if (bracketType == CodeBlock.BracketType.OPEN) {
                // Point inward - this would depend on the structure, for now we'll use NORTH as default
                pistonData.setFacing(BlockFace.NORTH);
            } else {
                // Point outward - this would depend on the structure, for now we'll use SOUTH as default
                pistonData.setFacing(BlockFace.SOUTH);
            }
            
            pistonBlock.setBlockData(pistonData);
        }
    }
    
    /**
     * Добавляет эффекты создания структуры
     */
    private void addConstructionEffects(Location location, Player player) {
        // Add visual effect for structure creation
        Location effectLoc = location.add(0.5, 1.0, 0.5);
        player.spawnParticle(org.bukkit.Particle.FLAME, effectLoc, 10, 0.2, 0.2, 0.2, 0.1);
        player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, effectLoc, 10, 0.2, 0.2, 0.2, 0.1);
        player.playSound(location, org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
    }

    /**
     * Обрабатывает размещение блоков кодирования
     * Реализует стиль: универсальные блоки с настройкой через GUI
     */
    @EventHandler(priority = EventPriority.HIGH) // Run before AutoConnectionManager (MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return; // Don't process if already cancelled by DevWorldProtectionListener
        
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        ItemStack itemInHand = event.getItemInHand();
        
        // Only process in dev worlds
        if (!isInDevWorld(player)) return;
        
        // Проверяем стекло под блоком (как в FrameLand)
        Block glassUnder = player.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());
        if (glassUnder.getType() != Material.BLUE_STAINED_GLASS && glassUnder.getType() != Material.GRAY_STAINED_GLASS) {
            player.sendMessage("§cВы можете размещать блоки кода только на синее (события) или серое (действия) стекло!");
            player.playSound(block.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            event.setCancelled(true);
            return;
        }
        
        // Проверяем, что это универсальный блок для кодирования
        if (!blockConfigService.isCodeBlock(block.getType())) {
            // Особая обработка для поршней (скобок) - они могут не иметь конфига
            if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
                CodeBlock newCodeBlock = new CodeBlock(block.getType(), "BRACKET"); // Уникальный ID для скобок
                newCodeBlock.setBracketType(CodeBlock.BracketType.OPEN); // По умолчанию открывающая
                setPistonDirection(block, CodeBlock.BracketType.OPEN); // Задать направление
                updateBracketSign(block.getLocation(), CodeBlock.BracketType.OPEN); // Повесить табличку
                blockCodeBlocks.put(block.getLocation(), newCodeBlock);
                
                // Enhanced feedback for bracket placement
                player.sendMessage("§a✓ Скобка размещена: " + CodeBlock.BracketType.OPEN.getDisplayName());
                player.sendMessage("§7Кликните правой кнопкой для смены типа");
                
                // Add visual effects
                Location effectLoc = block.getLocation().add(0.5, 0.5, 0.5);
                player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, effectLoc, 10, 0.3, 0.3, 0.3, 1.0);
                player.playSound(block.getLocation(), org.bukkit.Sound.BLOCK_PISTON_EXTEND, 1.0f, 1.5f);
                
                return; // Завершаем обработку
            }
            return; // Это обычный блок, не кодовый
        }
        
        // Получаем конфиг блока из материала блока
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(block.getType());
        
        if (config == null) {
            // Это не кодовый блок, запрещаем установку
            player.sendMessage("§cВы можете размещать только специальные блоки для кодирования!");
            player.playSound(block.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            return;
        }
        
        // ===============================================
        //           НОВАЯ ЛОГИКА КОНСТРУКТОРОВ
        // ===============================================
        
        // 1. Проверяем, является ли блок "конструктором"
        if (config.isConstructor()) {
            // 2. НЕ отменяем стандартное размещение блока, позволяем ему установиться
            
            // 3. Вызываем метод для постройки структуры на следующий тик
            // Запланируем создание структуры на следующий тик, чтобы событие BlockPlaceEvent полностью завершилось
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                buildStructureFor(event, config);
                
                // 4. Визуальная и аудио обратная связь
                player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.1);
                player.playSound(block.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
                
                player.sendMessage("§a✓ Структура " + config.getDisplayName() + " создана!");
                player.sendMessage("§7Кликните по табличке для настройки параметров");
            });
            
            return; // Завершаем обработку, чтобы не создавать блок дважды
        }
        
        // Для обычных блоков создаем "пустой" блок, который будет настроен через GUI
        // Создаем CodeBlock с ID из конфига, но без действия (пустой блок)
        String actionId = "NOT_SET"; // Пустой блок без действия
        
        CodeBlock newCodeBlock = new CodeBlock(block.getType(), actionId);
        
        // 🔧 FIX: Устанавливаем действие по умолчанию из конфигурации, если есть
        if (config.getDefaultAction() != null) {
            newCodeBlock.setAction(config.getDefaultAction());
            actionId = config.getDefaultAction();
        }
        
        // Special handling for bracket blocks (pistons)
        if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
            newCodeBlock.setBracketType(CodeBlock.BracketType.OPEN); // Default to opening bracket
            setPistonDirection(block, CodeBlock.BracketType.OPEN);
        }
        
        // 🔧 FIX: Check if this block is being placed between existing brackets and reposition them
        handleBracketRepositioning(block.getLocation(), player);
        
        blockCodeBlocks.put(block.getLocation(), newCodeBlock);
        
        // Устанавливаем табличку с названием из конфига (для конструкторов табличка уже создана в buildStructureFor)
        if (!config.isConstructor()) {
            // 🔧 FIX: Show the actual action instead of "Пустой"
            String displayText = config.getDisplayName();
            if (!actionId.equals("NOT_SET")) {
                displayText += " (" + actionId + ")";
            } else {
                displayText += " (Пустой)";
            }
            setSignOnBlock(block.getLocation(), displayText);
        }
        
        // Визуальная и аудио обратная связь
        player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.1);
        player.playSound(block.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
        
        if (config.isConstructor()) {
            player.sendMessage("§a✓ Структура " + config.getDisplayName() + " создана!");
            player.sendMessage("§7Кликните по табличке для настройки параметров");
        } else {
            player.sendMessage("§a✓ Блок кода размещен: " + config.getDisplayName());
            if (!actionId.equals("NOT_SET")) {
                player.sendMessage("§7Действие: " + actionId);
            }
            player.sendMessage("§7Кликните правой кнопкой для выбора действия");
        }
    }
    
    /**
     * Создает структуру для блоков-конструкторов
     * Реализует стиль: визуальное построение кода с обратной связью
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
        
        // ENHANCED: More intuitive structure creation
        if (config.getType().equals("CONDITION") || config.getType().equals("CONTROL")) {
            int bracketDistance = structure.getBracketDistance();
            
            // Calculate optimal positioning based on surrounding blocks
            BlockFace buildDirection = findOptimalBuildDirection(loc, bracketDistance);
            
            // Create opening bracket (piston pointing inward)
            Location openBracketLoc = loc.clone().add(buildDirection.getModX(), 0, buildDirection.getModZ());
            
            // Create closing bracket (piston pointing outward)
            // 🔧 FIX: Correct bracket positioning - count from main block, not from open bracket
            Location closeBracketLoc = loc.clone().add(
                buildDirection.getModX() * bracketDistance, 
                0, 
                buildDirection.getModZ() * bracketDistance
            );
            
            // 1. Create bracket pistons with proper orientation
            createBracketPiston(openBracketLoc, CodeBlock.BracketType.OPEN, player, buildDirection);
            createBracketPiston(closeBracketLoc, CodeBlock.BracketType.CLOSE, player, buildDirection);
            
            // 2. Create smart sign on main block that opens configuration GUI
            if (structure.hasSign()) {
                setSmartSignOnBlock(loc, config.getDisplayName(), config.getId());
            }
            
            // 3. Create container above main block for parameters (optional)
            if (config.getType().equals("CONDITION") && !config.getId().equals("else")) {
                spawnContainerAboveBlock(loc, config.getId());
            }
            
            // 4. Add enhanced visual effects for "magical" feeling
            addConstructionEffects(loc, player);
            
            // 5. Add directional beam effect to show structure formation
            showStructureBeam(loc, buildDirection, bracketDistance, player);
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
            // Place ore at (1, 0, 0) relative to event block (to the east)
            Location oreLoc = loc.clone().add(1, 0, 0); // Place ore to the east of the event block
            if (oreLoc.getBlock().getType().isAir()) {
                oreLoc.getBlock().setType(Material.DIAMOND_ORE);
                
                // Add visual effect for ore placement
                player.spawnParticle(org.bukkit.Particle.FLAME, oreLoc.add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.1);
                player.playSound(oreLoc, org.bukkit.Sound.BLOCK_STONE_PLACE, 0.8f, 1.2f);
            }
            
            addConstructionEffects(loc, player);
        }
        
        // ACTION blocks also get structure building
        else if (config.getType().equals("ACTION")) {
            // Action blocks get a simple structure with sign
            if (structure.hasSign()) {
                setSmartSignOnBlock(loc, config.getDisplayName() + " Action", config.getId());
            }
            
            // Add visual effects
            addConstructionEffects(loc, player);
        }
        
        // VARIABLE blocks
        else if (config.getType().equals("VARIABLE")) {
            // Variable blocks get a simple structure with sign
            if (structure.hasSign()) {
                setSmartSignOnBlock(loc, config.getDisplayName() + " Variable", config.getId());
            }
            
            // Add visual effects
            addConstructionEffects(loc, player);
        }
        
        // Notify player of successful structure creation
        player.sendMessage("§a✓ Структура " + config.getDisplayName() + " полностью создана!");
        player.sendMessage("§7Кликните по табличке для настройки параметров");
    }
    
    /**
     * 🎆 ENHANCED: Creates bracket piston with proper orientation
     * Реализует стиль: визуальное построение кода с обратной связью
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
        
        // ENHANCED: Smart piston orientation for brackets
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
        
        // Add enhanced visual effects for reference system-style magic
        Location effectLoc = location.add(0.5, 0.5, 0.5);
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, effectLoc, 15, 0.4, 0.4, 0.4, 1.5);
        player.spawnParticle(org.bukkit.Particle.CRIT_MAGIC, effectLoc, 10, 0.3, 0.3, 0.3, 0.5);
        player.playSound(location, org.bukkit.Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.2f, 1.8f);
        
        // Add extra visual feedback for bracket creation
        player.spawnParticle(org.bukkit.Particle.FIREWORKS_SPARK, effectLoc, 8, 0.3, 0.3, 0.3, 0.2);
        player.spawnParticle(org.bukkit.Particle.SPELL_WITCH, effectLoc, 5, 0.2, 0.2, 0.2, 0.1);
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
            
            // Add visual effect for container placement
            containerLocation.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, 
                containerLocation.add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.1);
            containerLocation.getWorld().playSound(containerLocation, 
                org.bukkit.Sound.BLOCK_WOOD_PLACE, 0.7f, 1.3f);
            
            // Pre-populate container with placeholder items based on action configuration
            populateContainerWithPlaceholders(containerBlock, actionId);
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
                        // Add visual effect for ore removal
                        Location effectLoc = oreBlock.getLocation().add(0.5, 0.5, 0.5);
                        player.spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, effectLoc, 6, 0.3, 0.3, 0.3, 0.1);
                        player.playSound(oreBlock.getLocation(), org.bukkit.Sound.BLOCK_STONE_BREAK, 0.8f, 0.8f);
                        
                        oreBlock.setType(Material.AIR);
                    }
                }
            }
            
            // Удаляем табличку, если она есть
            removeSignFromBlock(loc);
            
            // Удаляем контейнер над блоком, если он есть
            removeContainerAboveBlock(loc);
            
            // Enhanced feedback for block removal
            player.sendMessage("§cБлок кода удален!");
            player.playSound(loc, org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.8f, 0.8f);
            
            // Add visual effect for block removal
            Location effectLoc = loc.add(0.5, 0.5, 0.5);
            player.spawnParticle(org.bukkit.Particle.CLOUD, effectLoc, 8, 0.3, 0.3, 0.3, 0.1);
            
            // AutoConnectionManager will handle disconnection automatically at MONITOR priority
            plugin.getLogger().info("CodeBlock removed from " + loc + " with action: " + (removedBlock != null ? removedBlock.getAction() : "unknown"));
        }
        
        // Особая обработка для поршней (скобок)
        else if (event.getBlock().getType() == Material.PISTON || event.getBlock().getType() == Material.STICKY_PISTON) {
            // Это поршень-скобка, удаляем его из нашей карты
            blockCodeBlocks.remove(loc);
            
            // Удаляем табличку, если она есть
            removeSignFromBlock(loc);
            
            // Enhanced feedback for bracket removal
            player.sendMessage("§cСкобка удалена!");
            player.playSound(loc, org.bukkit.Sound.BLOCK_PISTON_CONTRACT, 0.8f, 1.2f);
            
            // Add visual effect for bracket removal
            Location effectLoc = loc.add(0.5, 0.5, 0.5);
            player.spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, effectLoc, 8, 0.3, 0.3, 0.3, 0.1);
            player.spawnParticle(org.bukkit.Particle.FLAME, effectLoc, 3, 0.2, 0.2, 0.2, 0.05);
            
            // 🔧 FIX: Actually remove the physical block
            event.getBlock().setType(Material.AIR);
            
            plugin.getLogger().info("Bracket piston removed from " + loc);
        }
        
        // 🔧 FIX: Handle block movement with connected brackets
        // Check if this is a code block that might have connected brackets
        handleConnectedStructureRemoval(loc, player);
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
                // Add visual effect for bracket removal
                Location effectLoc = location.add(0.5, 0.5, 0.5);
                player.spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, effectLoc, 8, 0.3, 0.3, 0.3, 0.1);
                player.spawnParticle(org.bukkit.Particle.FLAME, effectLoc, 3, 0.2, 0.2, 0.2, 0.05);
                player.playSound(location, org.bukkit.Sound.BLOCK_PISTON_CONTRACT, 0.8f, 1.2f);
                
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
            
            // Add visual effect for container removal
            Location effectLoc = containerLocation.add(0.5, 0.5, 0.5);
            containerLocation.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, effectLoc, 6, 0.3, 0.3, 0.3, 0.1);
            containerLocation.getWorld().playSound(containerLocation, org.bukkit.Sound.BLOCK_WOOD_BREAK, 0.7f, 0.8f);
            
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
     * Removes a sign from above the specified block location
     * Handles both wall signs and standing signs safely
     */
    private void removeSignFromBlock(Location location) {
        if (location == null) {
            plugin.getLogger().warning("Attempted to remove sign from null location");
            return;
        }
        
        Block block = location.getBlock();
        // Check adjacent blocks for wall signs
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            Material blockType = signBlock.getType();
            
            // Check for wall signs
            if (blockType == Material.OAK_WALL_SIGN) {
                BlockData blockData = signBlock.getBlockData();
                if (blockData instanceof WallSign) {
                    WallSign wallSign = (WallSign) blockData;
                    // Check if the sign is facing toward the block
                    if (wallSign.getFacing() == face.getOppositeFace()) {
                        // Add removal effect
                        Location effectLoc = signBlock.getLocation().add(0.5, 0.5, 0.5);
                        signBlock.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, effectLoc, 5, 0.2, 0.2, 0.2, 0.05);
                        
                        // Remove the sign
                        signBlock.setType(Material.AIR, false);
                        plugin.getLogger().fine("Removed wall sign at " + signBlock.getLocation());
                        return; // Remove only one sign
                    }
                }
            }
        }
        
        // Also check one block above for standing signs
        Location signLoc = location.clone().add(0, 1, 0);
        if (signLoc.getWorld().isChunkLoaded(signLoc.getBlockX() >> 4, signLoc.getBlockZ() >> 4)) {
            Block signBlock = signLoc.getBlock();
            Material blockType = signBlock.getType();
            
            // Check for standing signs
            if (blockType == Material.OAK_SIGN) {
                // Add removal effect
                Location effectLoc = signLoc.clone().add(0.5, 0.5, 0.5);
                signBlock.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, effectLoc, 5, 0.2, 0.2, 0.2, 0.05);
                
                // Remove the sign
                signBlock.setType(Material.AIR, false);
                plugin.getLogger().fine("Removed standing sign at " + signLoc);
            }
        }
    }

    /**
     * Places a sign above the specified block with the given text and action ID
     * Uses OAK_SIGN for horizontal surfaces instead of WALL_SIGN to avoid facing issues
     */
    private void setSmartSignOnBlock(Location location, String text, String actionId) {
        if (location == null || text == null || actionId == null) {
            plugin.getLogger().warning("Invalid parameters for setSmartSignOnBlock");
            return;
        }
        
        World world = location.getWorld();
        if (world == null) {
            plugin.getLogger().warning("World is null in setSmartSignOnBlock");
            return;
        }
        
        // Calculate sign position (one block above)
        Location signLoc = location.clone().add(0, 1, 0);
        Block signBlock = signLoc.getBlock();
        
        // Check if the block is already occupied
        if (!signBlock.getType().isAir()) {
            plugin.getLogger().warning("Cannot place sign - block at " + signLoc + " is not air");
            return;
        }
        
        try {
            // Set the sign type
            signBlock.setType(Material.OAK_SIGN);
            
            // Update the sign data
            Sign sign = (Sign) signBlock.getState();
            
            // Set sign lines
            String[] lines = formatSignText(text, actionId);
            for (int i = 0; i < 4 && i < lines.length; i++) {
                sign.setLine(i, lines[i]);
            }
            
            // Update the sign
            sign.update(true);
            
            plugin.getLogger().fine("Placed sign at " + signLoc + " with text: " + text);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error placing sign at " + signLoc, e);
            // Clean up if something went wrong
            signBlock.setType(Material.AIR);
        }
    }

    /**
     * Formats text to fit on a sign with proper line breaks
     */
    private String[] formatSignText(String text, String actionId) {
        String[] lines = new String[4];
        lines[0] = "§a" + (text.length() > 15 ? text.substring(0, 15) : text);
        lines[1] = "§7ID: §f" + (actionId.length() > 13 ? actionId.substring(0, 13) : actionId);
        lines[2] = "§8[§eMegaCreative§8]";
        lines[3] = "";
        return lines;
    }

    /**
     * Показывает направленный луч формирования структуры
     */
    private void showStructureBeam(Location location, BlockFace direction, int distance, Player player) {
        // Calculate positions of all blocks in the structure
        World world = location.getWorld();
        if (world == null) return;
        
        for (int i = 0; i <= distance; i++) {
            Location blockLoc = location.clone().add(
                direction.getModX() * i, 
                0, 
                direction.getModZ() * i
            );
            
            // Add visual effect for each block
            Location effectLoc = blockLoc.add(0.5, 0.5, 0.5);
            player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, effectLoc, 1, 0.2, 0.2, 0.2, 0.1);
            player.playSound(blockLoc, org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
        }
    }
    
    /**
     * Наполняет контейнер заполнителями в зависимости от действия
     */
    private void populateContainerWithPlaceholders(Block containerBlock, String actionId) {
        // Получаем конфиг действия из сервиса действий
        org.bukkit.configuration.ConfigurationSection actionConfigurations = blockConfigService.getActionConfigurations();
        if (actionConfigurations == null) return;
        
        org.bukkit.configuration.ConfigurationSection actionConfig = actionConfigurations.getConfigurationSection(actionId);
        if (actionConfig == null) return;
        
        // Get action-specific slot configuration from action configurations
        org.bukkit.configuration.ConfigurationSection slotsConfig = actionConfig.getConfigurationSection("slots");
        if (slotsConfig == null) return;
        
        // Создаем инвентарь контейнера
        org.bukkit.inventory.Inventory containerInventory = ((org.bukkit.block.Container) containerBlock).getInventory();
        
        // Process slots configuration
        for (String slotKey : slotsConfig.getKeys(false)) {
            try {
                int slotIndex = Integer.parseInt(slotKey);
                if (slotIndex < 0 || slotIndex >= containerInventory.getSize()) continue;
                
                org.bukkit.configuration.ConfigurationSection slotConfig = slotsConfig.getConfigurationSection(slotKey);
                if (slotConfig == null) continue;
                
                String placeholderItemName = slotConfig.getString("placeholder_item", "PAPER");
                String slotName = slotConfig.getString("name", "Параметр");
                String description = slotConfig.getString("description", "Описание параметра");
                
                // Create placeholder item
                Material material = Material.matchMaterial(placeholderItemName);
                if (material == null) {
                    material = Material.PAPER;
                }
                
                org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material);
                org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(slotName);
                    
                    // Add description
                    meta.setLore(java.util.Arrays.asList(
                        "§7" + description,
                        "§8Поместите сюда нужный предмет"
                    ));
                    
                    item.setItemMeta(meta);
                    containerInventory.setItem(slotIndex, item);
                }
            } catch (NumberFormatException ignored) {
                // Skip invalid slot indices
            }
        }
    }
    
    /**
     * Создает заполнитель значения указанного типа
     */
    private DataValue createPlaceholderValue(String type) {
        if (type.equals("ANY")) {
            return new AnyValue("Параметр");
        }
        if (type.equals("TEXT")) {
            return new TextValue("Текст");
        }
        if (type.equals("NUMBER")) {
            return new NumberValue(0);
        }
        if (type.equals("BOOLEAN")) {
            return new BooleanValue(false);
        }
        if (type.equals("LIST")) {
            return new ListValue();
        }
        return null;
    }
    
    /**
     * Вычисляет оптимальное направление строительства
     */
    private BlockFace findOptimalBuildDirection(Location location, int bracketDistance) {
        World world = location.getWorld();
        if (world == null) return BlockFace.EAST;
        
        // Check available space in each direction
        BlockFace[] directions = {BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH};
        
        for (BlockFace face : directions) {
            boolean hasSpace = true;
            
            // Check if we have enough space for the structure
            for (int i = 1; i <= bracketDistance; i++) {
                Location checkLoc = location.clone().add(
                    face.getModX() * i, 
                    0, 
                    face.getModZ() * i
                );
                
                if (!checkLoc.getBlock().getType().isAir()) {
                    hasSpace = false;
                    break;
                }
            }
            
            if (hasSpace) {
                return face;
            }
        }
        
        // Fallback to first available direction
        return BlockFace.EAST;
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
            BlockFace buildDirection = findActualBuildDirection(mainBlockLoc, bracketDistance);
            
            // Места скобок - исправлено для правильного позиционирования
            Location openBracketLoc = mainBlockLoc.clone().add(buildDirection.getModX(), 0, buildDirection.getModZ());
            Location closeBracketLoc = mainBlockLoc.clone().add(
                buildDirection.getModX() * bracketDistance, 
                0, 
                buildDirection.getModZ() * bracketDistance
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
            
            // Также удаляем основной блок
            blockCodeBlocks.remove(mainBlockLoc);
            removeSignFromBlock(mainBlockLoc);
            removeContainerAboveBlock(mainBlockLoc);
            
            // Add visual effect for complete structure removal
            player.sendMessage("§eСтруктура " + config.getDisplayName() + " полностью удалена!");
            player.playSound(mainBlockLoc, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
            player.spawnParticle(org.bukkit.Particle.EXPLOSION_NORMAL, mainBlockLoc.add(0.5, 0.5, 0.5), 10, 0.5, 0.5, 0.5, 0.2);
            
            // Удаляем физический блок
            mainBlockLoc.getBlock().setType(Material.AIR);
        }
        // 🔧 FIX: Add handling for EVENT blocks
        else if (config.getType().equals("EVENT")) {
            // For event blocks, just remove the block and any associated "ore"
            blockCodeBlocks.remove(mainBlockLoc);
            removeSignFromBlock(mainBlockLoc);
            removeContainerAboveBlock(mainBlockLoc);
            
            // Remove the "ore" block to the east
            Block oreBlock = mainBlockLoc.clone().add(1, 0, 0).getBlock();
            if (oreBlock.getType() == Material.DIAMOND_ORE) {
                oreBlock.setType(Material.AIR);
            }
            
            // Add visual effect for complete structure removal
            player.sendMessage("§eСтруктура " + config.getDisplayName() + " полностью удалена!");
            player.playSound(mainBlockLoc, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
            player.spawnParticle(org.bukkit.Particle.EXPLOSION_NORMAL, mainBlockLoc.add(0.5, 0.5, 0.5), 10, 0.5, 0.5, 0.5, 0.2);
        }
        // 🔧 FIX: Add handling for ACTION blocks
        else if (config.getType().equals("ACTION")) {
            // For action blocks, just remove the block
            blockCodeBlocks.remove(mainBlockLoc);
            removeSignFromBlock(mainBlockLoc);
            removeContainerAboveBlock(mainBlockLoc);
            
            // Add visual effect for complete structure removal
            player.sendMessage("§eСтруктура " + config.getDisplayName() + " полностью удалена!");
            player.playSound(mainBlockLoc, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
            player.spawnParticle(org.bukkit.Particle.EXPLOSION_NORMAL, mainBlockLoc.add(0.5, 0.5, 0.5), 10, 0.5, 0.5, 0.5, 0.2);
            
            // Удаляем физический блок
            mainBlockLoc.getBlock().setType(Material.AIR);
        }
        
        // 🔧 FIX: Handle block movement with connected brackets
        // Check if this is a code block that might have connected brackets
        handleConnectedStructureRemoval(mainBlockLoc, player);
    }
    
    /**
     * 🔧 FIX: Handle removal of connected structure components when a block is broken
     * This ensures brackets and other connected elements move with the main block
     */
    private void handleConnectedStructureRemoval(Location blockLocation, Player player) {
        // Check for connected brackets in all directions
        BlockFace[] directions = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        
        for (BlockFace face : directions) {
            Location adjacentLoc = blockLocation.clone().add(face.getModX(), 0, face.getModZ());
            Block adjacentBlock = adjacentLoc.getBlock();
            
            // Check if adjacent block is a bracket piston
            if ((adjacentBlock.getType() == Material.PISTON || adjacentBlock.getType() == Material.STICKY_PISTON) 
                && blockCodeBlocks.containsKey(adjacentLoc)) {
                CodeBlock codeBlock = blockCodeBlocks.get(adjacentLoc);
                if (codeBlock != null && codeBlock.isBracket()) {
                    // This is a connected bracket, remove it
                    removeBracketPiston(adjacentLoc, player);
                }
            }
        }
    }
    
    /**
     * 🔧 FIX: Handle movement of connected structure components when a block is moved
     * This ensures brackets and other connected elements move with the main block
     */
    private void handleConnectedStructureMovement(Location oldLocation, Location newLocation, Player player) {
        // Check for connected brackets in all directions around the old location
        BlockFace[] directions = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        
        for (BlockFace face : directions) {
            Location oldAdjacentLoc = oldLocation.clone().add(face.getModX(), 0, face.getModZ());
            Location newAdjacentLoc = newLocation.clone().add(face.getModX(), 0, face.getModZ());
            
            // Check if old adjacent location has a bracket piston
            if (blockCodeBlocks.containsKey(oldAdjacentLoc)) {
                CodeBlock codeBlock = blockCodeBlocks.get(oldAdjacentLoc);
                if (codeBlock != null && codeBlock.isBracket()) {
                    // Move the bracket to the new location
                    Block oldBlock = oldAdjacentLoc.getBlock();
                    Block newBlock = newAdjacentLoc.getBlock();
                    
                    // Copy the block type and data
                    newBlock.setType(oldBlock.getType());
                    newBlock.setBlockData(oldBlock.getBlockData());
                    
                    // Update our tracking
                    blockCodeBlocks.remove(oldAdjacentLoc);
                    blockCodeBlocks.put(newAdjacentLoc, codeBlock);
                    
                    // Update the sign
                    removeSignFromBlock(oldAdjacentLoc);
                    updateBracketSign(newAdjacentLoc, codeBlock.getBracketType());
                    
                    // Remove the old block
                    oldBlock.setType(Material.AIR);
                    
                    // Add visual effects
                    player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, 
                        newAdjacentLoc.add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 1.0);
                }
            }
        }
    }
    
    /**
     * Определяет фактическое направление строительства структуры
     */
    private BlockFace findActualBuildDirection(Location location, int bracketDistance) {
        // Check for existing brackets to determine the actual build direction
        for (BlockFace face : new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH}) {
            Location potentialBracketLoc = location.clone().add(face.getModX(), 0, face.getModZ());
            Block potentialBracket = potentialBracketLoc.getBlock();
            
            // Check if this location has a bracket piston
            if ((potentialBracket.getType() == Material.PISTON || potentialBracket.getType() == Material.STICKY_PISTON) 
                && blockCodeBlocks.containsKey(potentialBracketLoc)) {
                CodeBlock codeBlock = blockCodeBlocks.get(potentialBracketLoc);
                if (codeBlock.isBracket()) {
                    return face;
                }
            }
        }
        
        // Fallback to default direction if no brackets found
        return BlockFace.EAST;
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
        
        // 🔧 FIX: Add debug logging to see what's happening
        plugin.getLogger().info("Player " + player.getName() + " interacted with block. Action: " + event.getAction() + ", In dev world: " + isInDevWorld(player));
        
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
        
        // 🔧 FIX: Add debug logging
        plugin.getLogger().info("Player " + player.getName() + " right-clicked block at " + location + ", type: " + clickedBlock.getType());
        
        // 🎆 ENHANCED: Check if player clicked on a smart sign
        if (clickedBlock.getType().name().contains("SIGN")) {
            if (handleSmartSignClick(clickedBlock, player)) {
                event.setCancelled(true);
                return;
            }
        }
        
        // 🔧 FIX: Ensure we handle all sign types properly
        if (clickedBlock.getState() instanceof Sign) {
            if (handleSmartSignClick(clickedBlock, player)) {
                event.setCancelled(true);
                return;
            }
        }
        
        // Проверяем, есть ли уже блок кода на этой локации
        if (blockCodeBlocks.containsKey(location)) {
            // 🔧 FIX: Add debug logging
            plugin.getLogger().info("Found code block at " + location + " for player " + player.getName());
            
            // Предотвращаем открытие GUI, если в руке инструмент
            if (isTool(itemInHand)) {
                plugin.getLogger().info("Player " + player.getName() + " has tool in hand, not opening GUI");
                return;
            }
            
            event.setCancelled(true); // Важно, чтобы не открылся, например, верстак
            
            // Открываем GUI конфигурации блока - используем новый улучшенный интерфейс
            CodeBlock codeBlock = blockCodeBlocks.get(location);
            
            // Special handling for bracket blocks - toggle bracket type instead of opening GUI
            if (codeBlock.isBracket()) {
                plugin.getLogger().info("Player " + player.getName() + " clicked bracket block, toggling type");
                toggleBracketType(codeBlock, event.getClickedBlock(), player);
                return;
            }
            
            // Handle block interaction with proper GUI opening
            plugin.getLogger().info("Player " + player.getName() + " opening block interaction GUI");
            handleBlockInteraction(player, location);
            return;
        }
        
        // Проверяем, кликнул ли игрок по контейнеру над блоком кода
        Location blockBelow = location.clone().add(0, -1, 0);
        CodeBlock codeBlock = blockCodeBlocks.get(blockBelow);
        if (codeBlock != null) {
            plugin.getLogger().info("Player " + player.getName() + " clicked container above code block at " + blockBelow);
            event.setCancelled(true);
            
            // Проверяем тип блока - открываем специализированный GUI для параметров
            BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(codeBlock.getAction());
            if (config != null) {
                plugin.getLogger().info("Opening parameter config GUI for container interaction for player " + player.getName());
                // Открываем уникальный drag-and-drop GUI для конкретного действия
                openParameterConfigGUI(player, blockBelow, codeBlock, config);
            } else {
                player.sendMessage("§cОшибка: Не удалось найти конфигурацию для действия " + codeBlock.getAction());
            }
        }
    }

    /**
     * Завершение интерактивности (возвращение "магии")
     * Обрабатывает взаимодействие с блоком кода для открытия соответствующего GUI
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private void handleBlockInteraction(Player player, Location blockLocation) {
        plugin.getLogger().info("Handling block interaction for player " + player.getName() + " at " + blockLocation);
        CodeBlock codeBlock = blockCodeBlocks.get(blockLocation);
        if (codeBlock == null) {
            plugin.getLogger().info("No code block found at " + blockLocation + " for player " + player.getName());
            return;
        }
        
        // Если это поршень (скобка), переключаем тип
        if (codeBlock.isBracket()) {
            toggleBracketType(codeBlock, blockLocation.getBlock(), player);
            return;
        }

        // Для универсальных блоков всегда открываем GUI выбора действия
        // Это реализует стиль: один блок - множество функций
        if (codeBlock.getAction() == null || codeBlock.getAction().equals("NOT_SET") || codeBlock.getAction().equals("UNKNOWN")) {
            plugin.getLogger().info("Opening action selection GUI for player " + player.getName() + " at " + blockLocation);
            // Вызываем GUI для выбора действия
            openActionSelectionGUI(player, blockLocation, codeBlock.getMaterial());
        } else {
            plugin.getLogger().info("Opening parameter config GUI for player " + player.getName() + " at " + blockLocation + " with action " + codeBlock.getAction());
            // Иначе открываем GUI настройки параметров
            BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(codeBlock.getAction());
            if (config != null) {
                // Вызываем GUI для настройки параметров
                openParameterConfigGUI(player, blockLocation, codeBlock, config);
            } else {
                // Если конфигурация не найдена, открываем GUI выбора действия
                player.sendMessage("§cОшибка: Не удалось найти конфигурацию для действия '" + codeBlock.getAction() + "'");
                player.sendMessage("§eОткрываю GUI для выбора нового действия...");
                openActionSelectionGUI(player, blockLocation, codeBlock.getMaterial());
            }
        }
    }
    
    /**
     * Открывает GUI для выбора действия
     * Implements reference system-style: universal blocks with GUI configuration
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
     * Implements reference system-style: universal blocks with GUI configuration
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
               worldName.contains("creative") || worldName.contains("Creative") ||
               worldName.contains("-code") || worldName.endsWith("-code"); // 🔧 FIX: Add -code suffix detection for dev worlds
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
     * Implements reference system-style: visual code construction with feedback
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
                signState.setLine(0, "§6★★★★★★★★★★★★");
                // Обрезаем текст, если он слишком длинный
                String line2 = text.length() > 15 ? text.substring(0, 15) : text;
                signState.setLine(1, "§e" + line2);
                signState.setLine(2, "§a➜ Кликните ПКМ");
                signState.setLine(3, "§6★★★★★★★★★★★★");
                signState.update(true);
                
                // Add visual effects
                Location effectLoc = signBlock.getLocation().add(0.5, 0.5, 0.5);
                block.getWorld().spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, effectLoc, 5, 0.3, 0.3, 0.3, 1);
                
                return; // ВАЖНО: Выходим из метода после установки ПЕРВОЙ таблички
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
     * 🎆 ENHANCED: Updates the sign for a bracket block
     * Implements reference system-style: visual code construction with feedback
     */
    private void updateBracketSign(Location location, CodeBlock.BracketType bracketType) {
        removeSignFromBlock(location); // Remove existing sign first

        Block block = location.getBlock();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            if (signBlock.getType().isAir()) {
                signBlock.setType(Material.OAK_WALL_SIGN, false);
                
                WallSign wallSignData = (WallSign) signBlock.getBlockData();
                wallSignData.setFacing(face);
                signBlock.setBlockData(wallSignData);
                
                Sign signState = (Sign) signBlock.getState();
                signState.setLine(0, "§8============");
                signState.setLine(1, "§6" + bracketType.getSymbol());
                signState.setLine(2, "§7" + bracketType.getDisplayName());
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
     * 🎆 ENHANCED: Handles smart sign clicks to open configuration GUIs
     * This restores the reference system "magic" of clicking signs to configure blocks
     */
    private boolean handleSmartSignClick(Block signBlock, Player player) {
        // 🔧 FIX: Add debug logging
        plugin.getLogger().info("Player " + player.getName() + " clicked sign at " + signBlock.getLocation());
        
        if (!(signBlock.getState() instanceof Sign)) {
            plugin.getLogger().info("Block at " + signBlock.getLocation() + " is not a sign");
            return false;
        }
        
        Sign sign = (Sign) signBlock.getState();
        
        // Check if this is a smart sign (has our special markers)
        String[] lines = sign.getLines();
        // 🔧 FIX: Enhanced smart sign detection with more patterns
        boolean isSmartSign = false;
        plugin.getLogger().info("Checking sign lines for player " + player.getName() + ":");
        for (int i = 0; i < lines.length; i++) {
            plugin.getLogger().info("Line " + i + ": " + lines[i]);
        }
        
        for (String line : lines) {
            String cleanLine = ChatColor.stripColor(line).toLowerCase().trim();
            plugin.getLogger().info("Checking clean line: " + cleanLine);
            if (cleanLine.contains("клик для настройки") || cleanLine.contains("кликните пкм") || 
                cleanLine.contains("click to configure") || cleanLine.contains("right-click") ||
                cleanLine.contains("★") || cleanLine.contains("megacreative") || cleanLine.contains("кликните") ||
                cleanLine.contains("настройка") || cleanLine.contains("configure") || cleanLine.contains("setup") ||
                cleanLine.contains("magic") || cleanLine.contains("code") || cleanLine.contains("блок") ||
                cleanLine.contains("[megacreative]")) { // 🔧 FIX: Add [megacreative] pattern from logs
                isSmartSign = true;
                plugin.getLogger().info("Found smart sign pattern in line: " + cleanLine);
                break;
            }
        }
        
        if (!isSmartSign) {
            plugin.getLogger().info("Sign at " + signBlock.getLocation() + " is not a smart sign for player " + player.getName());
            return false;
        }
        plugin.getLogger().info("Sign at " + signBlock.getLocation() + " is a smart sign for player " + player.getName());
        
        // Find the associated code block (sign should be adjacent to it)
        BlockFace[] adjacentFaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        
        for (BlockFace face : adjacentFaces) {
            Block adjacentBlock = signBlock.getRelative(face);
            Location blockLoc = adjacentBlock.getLocation();
            
            plugin.getLogger().info("Checking adjacent block at " + blockLoc + " for player " + player.getName() + ", face: " + face);
            
            if (blockCodeBlocks.containsKey(blockLoc)) {
                plugin.getLogger().info("Found code block at " + blockLoc + " for player " + player.getName());
                CodeBlock codeBlock = blockCodeBlocks.get(blockLoc);
                
                // Open appropriate GUI based on block state
                if (codeBlock.getAction() == null || "NOT_SET".equals(codeBlock.getAction())) {
                    plugin.getLogger().info("Opening action selection GUI for player " + player.getName());
                    // No action set - open action selection GUI
                    openActionSelectionGUI(player, blockLoc, adjacentBlock.getType());
                } else {
                    // Action already set - open parameter configuration GUI
                    BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(codeBlock.getAction());
                    if (config != null) {
                        plugin.getLogger().info("Opening parameter config GUI for player " + player.getName());
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
     * Handles piston extension events to move connected code blocks
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        handlePistonMovement(event.getBlocks(), event.getDirection(), event.getBlock().getWorld());
    }
    
    /**
     * Handles piston retraction events to move connected code blocks
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        handlePistonMovement(event.getBlocks(), event.getDirection(), event.getBlock().getWorld());
    }
    
    /**
     * Handles piston movement of code blocks
     */
    private void handlePistonMovement(List<Block> blocks, BlockFace direction, World world) {
        // Check if any of the moved blocks are code blocks
        for (Block block : blocks) {
            Location blockLoc = block.getLocation();
            if (blockCodeBlocks.containsKey(blockLoc)) {
                // This is a code block, we need to move it
                Location newLoc = blockLoc.clone().add(direction.getModX(), direction.getModY(), direction.getModZ());
                
                // Move the code block data
                CodeBlock codeBlock = blockCodeBlocks.remove(blockLoc);
                if (codeBlock != null) {
                    blockCodeBlocks.put(newLoc, codeBlock);
                    
                    // Update any associated signs
                    removeSignFromBlock(blockLoc);
                    // Recreate sign at new location if needed
                    if (codeBlock.isBracket()) {
                        updateBracketSign(newLoc, codeBlock.getBracketType());
                    } else if (codeBlock.getAction() != null && !codeBlock.getAction().equals("NOT_SET")) {
                        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(codeBlock.getAction());
                        if (config != null) {
                            setSignOnBlock(newLoc, config.getDisplayName());
                        } else {
                            // Fallback to material name
                            setSignOnBlock(newLoc, codeBlock.getMaterial().name());
                        }
                    } else {
                        // For blocks without actions, use material name
                        setSignOnBlock(newLoc, codeBlock.getMaterial().name() + " (Пустой)");
                    }
                    
                    // Add visual effect for movement
                    world.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, newLoc.add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 1.0);
                }
            }
        }
        
        // 🔧 FIX: Handle connected bracket movement
        // Check for brackets that might be connected to moved blocks
        for (Block block : blocks) {
            Location blockLoc = block.getLocation();
            // Check adjacent locations for brackets
            for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}) {
                Location adjacentLoc = blockLoc.clone().add(face.getModX(), face.getModY(), face.getModZ());
                if (blockCodeBlocks.containsKey(adjacentLoc)) {
                    CodeBlock adjacentBlock = blockCodeBlocks.get(adjacentLoc);
                    if (adjacentBlock != null && adjacentBlock.isBracket()) {
                        // Move the bracket as well
                        Location newLoc = adjacentLoc.clone().add(direction.getModX(), direction.getModY(), direction.getModZ());
                        
                        // Move the bracket data
                        CodeBlock bracketBlock = blockCodeBlocks.remove(adjacentLoc);
                        if (bracketBlock != null) {
                            blockCodeBlocks.put(newLoc, bracketBlock);
                            
                            // Update signs
                            removeSignFromBlock(adjacentLoc);
                            updateBracketSign(newLoc, bracketBlock.getBracketType());
                            
                            // Add visual effect
                            world.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, newLoc.add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 1.0);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 🔧 FIX: Handle repositioning of brackets when a new block is placed between them
     * This ensures that when a block is placed between existing brackets, 
     * the brackets move to maintain the proper distance
     */
    private void handleBracketRepositioning(Location newBlockLocation, Player player) {
        // Check adjacent locations for existing brackets in all directions
        BlockFace[] directions = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
        
        for (BlockFace direction : directions) {
            // Look for opening bracket in this direction
            Location openBracketLoc = findBracketInDirection(newBlockLocation, direction, CodeBlock.BracketType.OPEN);
            if (openBracketLoc != null) {
                // Look for corresponding closing bracket
                Location closeBracketLoc = findBracketInDirection(openBracketLoc, direction, CodeBlock.BracketType.CLOSE);
                if (closeBracketLoc != null) {
                    // Calculate the new positions for brackets (3 blocks apart from the new block)
                    Location newOpenBracketLoc = newBlockLocation.clone().add(direction.getModX() * 1, 0, direction.getModZ() * 1);
                    Location newCloseBracketLoc = newBlockLocation.clone().add(direction.getModX() * 3, 0, direction.getModZ() * 3);
                    
                    // Move the brackets to their new positions
                    moveBracket(openBracketLoc, newOpenBracketLoc, CodeBlock.BracketType.OPEN, player);
                    moveBracket(closeBracketLoc, newCloseBracketLoc, CodeBlock.BracketType.CLOSE, player);
                    
                    // Update piston directions
                    updateBracketPistonDirection(newOpenBracketLoc.getBlock(), CodeBlock.BracketType.OPEN, direction);
                    updateBracketPistonDirection(newCloseBracketLoc.getBlock(), CodeBlock.BracketType.CLOSE, direction);
                    
                    player.sendMessage("§a✓ Brackets repositioned to maintain proper distance");
                    return; // Only handle one pair of brackets
                }
            }
        }
    }
    
    /**
     * 🔧 FIX: Find a bracket in a specific direction
     */
    private Location findBracketInDirection(Location startLocation, BlockFace direction, CodeBlock.BracketType bracketType) {
        // Search up to 10 blocks in the given direction
        for (int i = 1; i <= 10; i++) {
            Location checkLocation = startLocation.clone().add(
                direction.getModX() * i, 
                0, 
                direction.getModZ() * i
            );
            
            if (blockCodeBlocks.containsKey(checkLocation)) {
                CodeBlock codeBlock = blockCodeBlocks.get(checkLocation);
                if (codeBlock != null && codeBlock.isBracket() && codeBlock.getBracketType() == bracketType) {
                    return checkLocation;
                }
            }
        }
        return null;
    }
    
    /**
     * 🔧 FIX: Move a bracket from one location to another
     */
    private void moveBracket(Location oldLocation, Location newLocation, CodeBlock.BracketType bracketType, Player player) {
        // Get the code block
        CodeBlock codeBlock = blockCodeBlocks.remove(oldLocation);
        if (codeBlock == null) return;
        
        // Remove old block and sign
        removeBracketPiston(oldLocation, player);
        
        // Create new bracket at the new location
        createBracketPiston(newLocation, bracketType, player, BlockFace.EAST); // Direction will be updated later
        
        // Update our tracking
        blockCodeBlocks.put(newLocation, codeBlock);
    }
    
    /**
     * 🔧 FIX: Update the direction of a bracket piston
     */
    private void updateBracketPistonDirection(Block pistonBlock, CodeBlock.BracketType bracketType, BlockFace direction) {
        if (pistonBlock.getType() == Material.PISTON || pistonBlock.getType() == Material.STICKY_PISTON) {
            org.bukkit.block.data.type.Piston pistonData = (org.bukkit.block.data.type.Piston) pistonBlock.getBlockData();
            
            if (bracketType == CodeBlock.BracketType.OPEN) {
                pistonData.setFacing(direction); // Points inward toward the structure
            } else {
                pistonData.setFacing(direction.getOppositeFace()); // Points outward from the structure
            }
            
            pistonBlock.setBlockData(pistonData);
        }
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