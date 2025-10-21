package com.megacreative.services;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.configs.WorldCode;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.*;
import com.megacreative.coding.ChestParser;

import java.util.*;
import java.util.logging.Logger;
import org.bukkit.block.BlockFace;

/**
 * Сервис компилятора кода, который сканирует структуры мира и преобразует их в объекты CodeScript
 * Реализует функцию "компиляции из мира", упомянутую в сравнении с эталонной системой
 *
 * CodeCompiler service that scans world structures and converts them to CodeScript objects
 * This implements the "compilation from world" feature mentioned in the reference system comparison
 *
 * CodeCompiler-Dienst, der Weltenstrukturen scannt und in CodeScript-Objekte umwandelt
 * Dies implementiert die Funktion "Kompilierung aus der Welt", die im Vergleich mit dem Referenzsystem erwähnt wird
 */
public class CodeCompiler implements org.bukkit.event.Listener {
    
    // These fields need to remain as class fields since they are used throughout multiple methods
    // Static analysis flags them as convertible to local variables, but this is a false positive
    private final MegaCreative plugin;
    private final Logger logger;
    private final BlockConfigService blockConfigService;
    private final BlockPlacementHandler blockPlacementHandler;
    
    /**
     * Инициализирует сервис компилятора кода
     * @param plugin Экземпляр основного плагина
     *
     * Initializes code compiler service
     * @param plugin Main plugin instance
     *
     * Initialisiert den Code-Compiler-Dienst
     * @param plugin Hauptplugin-Instanz
     */
    public CodeCompiler(MegaCreative plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        this.blockPlacementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
        try {
            org.bukkit.plugin.PluginManager pm = plugin.getServer().getPluginManager();
            pm.registerEvents(this, plugin);
        } catch (Exception e) {
            logger.warning("Failed to register events: " + e.getMessage());
        }
    }

    @org.bukkit.event.EventHandler
    public void onCodeBlockPlaced(com.megacreative.events.CodeBlockPlacedEvent event) {
        try {
            org.bukkit.Location location = event.getLocation();
            CodeBlock eventBlock = event.getCodeBlock();
            CodeScript script = compileScriptFromEventBlock(location, eventBlock);
            com.megacreative.interfaces.IWorldManager worldManager = plugin.getServiceRegistry().getWorldManager();
            com.megacreative.models.CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(location.getWorld());
            if (creativeWorld != null) {
                createAndRegisterActivator(eventBlock, creativeWorld, location);
                addScriptToWorld(eventBlock, script, creativeWorld, worldManager);
            }
        } catch (Exception e) {
            logger.warning("Error handling CodeBlockPlacedEvent: " + e.getMessage());
        }
    }

    @org.bukkit.event.EventHandler
    public void onCodeBlockBroken(com.megacreative.events.CodeBlockBrokenEvent event) {
        try {
            org.bukkit.Location location = event.getLocation();
            CodeBlock eventBlock = event.getCodeBlock();
            removeScript(eventBlock, location);
        } catch (Exception e) {
            logger.warning("Error handling CodeBlockBrokenEvent: " + e.getMessage());
        }
    }
    
    /**
     * Сканирует мир и компилирует все кодовые структуры в объекты CodeScript
     * Это основная точка входа для процесса "компиляции из мира"
     * 
     * @param world Мир для сканирования
     * @return Список скомпилированных объектов CodeScript
     *
     * Scans a world and compiles all code structures into CodeScript objects
     * This is the main entry point for the "compilation from world" process
     * 
     * @param world Die zu scannende Welt
     * @return Liste der kompilierten CodeScript-Objekte
     *
     * Scannt eine Welt und kompiliert alle Code-Strukturen in CodeScript-Objekte
     * Dies ist der Haupteinstiegspunkt für den Prozess der "Kompilierung aus der Welt"
     */
    public List<CodeScript> compileWorldScripts(World world) {
        logger.info("Starting compilation of world: " + world.getName());
        
        
        
        
        Map<Location, CodeBlock> scannedBlocks = scanWorldStructure(world);
        logger.info("World scan found " + scannedBlocks.size() + " code blocks");
        
        
        
        
        List<CodeScript> compiledScripts = new ArrayList<>();
        int scriptCount = 0;
        int errorCount = 0;
        
        
        
        
        Map<Location, CodeBlock> allCodeBlocks = blockPlacementHandler.getAllCodeBlocks();
        
        for (Map.Entry<Location, CodeBlock> entry : allCodeBlocks.entrySet()) {
            Location location = entry.getKey();
            CodeBlock codeBlock = entry.getValue();
            
            
            
            
            if (!location.getWorld().equals(world)) {
                continue;
            }
            
            
            
            
            if (isEventBlock(codeBlock)) {
                try {
                    CodeScript script = compileScriptFromEventBlock(location, codeBlock);
                    compiledScripts.add(script);
                    scriptCount++;
                    logger.fine("Compiled script: " + script.getName());
                } catch (Exception e) {
                    logger.log(java.util.logging.Level.SEVERE, "Failed to compile script from event block at " + formatLocation(location), e);
                    errorCount++;
                }
            }
        }
        
        logger.info("Compilation completed. Found " + scriptCount + " scripts with " + errorCount + " errors.");
        
        
        return compiledScripts;
    }
    
    /**
     * Компилирует один скрипт, начиная с блока события
     * 
     * @param eventLocation Расположение блока события
     * @param eventBlock Блок события CodeBlock
     * @return Скомпилированный CodeScript или null, если компиляция не удалась
     *
     * Compiles a single script starting from an event block
     * 
     * @param eventLocation Die Position des Ereignisblocks
     * @param eventBlock Der Ereignis-CodeBlock
     * @return Der kompilierte CodeScript oder null, wenn die Kompilierung fehlgeschlagen ist
     *
     * Kompiliert ein einzelnes Skript, beginnend mit einem Ereignisblock
     */
    private CodeScript compileScriptFromEventBlock(Location eventLocation, CodeBlock eventBlock) {
        
        
        
        CodeScript script = new CodeScript(eventBlock);
        script.setName("Script from " + eventBlock.getAction() + " at " + formatLocation(eventLocation));
        script.setEnabled(true);
        script.setType(CodeScript.ScriptType.EVENT);
        
        logger.fine("Starting compilation of script from event block at " + formatLocation(eventLocation));
        
        
        
        buildScriptStructure(eventLocation, eventBlock, script);
        
        logger.fine("Completed compilation of script: " + script.getName());
        
        
        return script;
    }
    
    /**
     * Строит полную структуру скрипта, сканируя мир вокруг блока события
     * Это реализует логику "сканирования", которая читает физическую структуру в мире
     * 
     * @param startLocation Начальное расположение (блок события)
     * @param startBlock Начальный CodeBlock
     * @param script Строимый скрипт
     *
     * Builds the complete script structure by scanning the world around the event block
     * This implements the "scanning" logic that reads the physical structure in the world
     * 
     * @param startLocation Die Startposition (Ereignisblock)
     * @param startBlock Der Start-CodeBlock
     * @param script Das zu erstellende Skript
     *
     * Baut die vollständige Skriptstruktur durch Scannen der Welt um den Ereignisblock
     */
    private void buildScriptStructure(Location startLocation, CodeBlock startBlock, CodeScript script) {
        logger.fine("Building script structure starting from " + formatLocation(startLocation));
        
        
        
        scanPhysicalBlocks(startLocation, startBlock);
        
        
        
        
        
        
        logger.fine("Script structure building completed for script: " + script.getName());
        
        
        
        
    }

    private void createAndRegisterActivator(CodeBlock eventBlock, com.megacreative.models.CreativeWorld creativeWorld, org.bukkit.Location location) {
        try {
            com.megacreative.coding.CodeHandler codeHandler = creativeWorld.getCodeHandler();
            if (codeHandler == null) return;
            com.megacreative.coding.activators.Activator activator = null;
            String action = eventBlock.getAction();
            if ("onJoin".equals(action)) activator = new com.megacreative.coding.activators.PlayerJoinActivator(plugin, creativeWorld);
            else if ("onPlayerMove".equals(action)) activator = new com.megacreative.coding.activators.PlayerMoveActivator(plugin, creativeWorld);
            else if ("onBlockPlace".equals(action)) activator = new com.megacreative.coding.activators.BlockPlaceActivator(plugin, creativeWorld);
            else if ("onBlockBreak".equals(action)) activator = new com.megacreative.coding.activators.BlockBreakActivator(plugin, creativeWorld);
            else if ("onChat".equals(action)) activator = new com.megacreative.coding.activators.ChatActivator(plugin, creativeWorld);
            else if ("onPlayerQuit".equals(action)) activator = new com.megacreative.coding.activators.PlayerQuitActivator(plugin, creativeWorld);
            else if ("onPlayerDeath".equals(action)) activator = new com.megacreative.coding.activators.PlayerDeathActivator(plugin, creativeWorld);
            else if ("onPlayerRespawn".equals(action)) activator = new com.megacreative.coding.activators.PlayerRespawnActivator(plugin, creativeWorld);
            else if ("onPlayerTeleport".equals(action)) activator = new com.megacreative.coding.activators.PlayerTeleportActivator(plugin, creativeWorld);
            else if ("onEntityPickupItem".equals(action)) activator = new com.megacreative.coding.activators.EntityPickupItemActivator(plugin, creativeWorld);
            if (activator != null) {
                activator.addAction(eventBlock);
                if (activator instanceof com.megacreative.coding.activators.BukkitEventActivator) {
                    ((com.megacreative.coding.activators.BukkitEventActivator) activator).setLocation(location);
                }
                codeHandler.registerActivator(activator);
            }
        } catch (Exception e) {
            logger.warning("Error creating and registering activator: " + e.getMessage());
        }
    }

    private void addScriptToWorld(CodeBlock eventBlock, CodeScript script, com.megacreative.models.CreativeWorld creativeWorld, com.megacreative.interfaces.IWorldManager worldManager) {
        try {
            java.util.List<CodeScript> scripts = creativeWorld.getScripts();
            if (scripts == null) {
                scripts = new java.util.ArrayList<>();
                creativeWorld.setScripts(scripts);
            }
            scripts.removeIf(existing -> existing.getRootBlock() != null && existing.getRootBlock().getId().equals(eventBlock.getId()));
            scripts.add(script);
            worldManager.saveWorld(creativeWorld);
            logger.fine("Added compiled script to world: " + creativeWorld.getName());
        } catch (Exception e) {
            logger.warning("Error adding script to world: " + e.getMessage());
        }
    }

    private void removeScript(CodeBlock eventBlock, org.bukkit.Location location) {
        try {
            com.megacreative.interfaces.IWorldManager worldManager = plugin.getServiceRegistry().getWorldManager();
            com.megacreative.models.CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(location.getWorld());
            if (creativeWorld == null) return;
            java.util.List<CodeScript> scripts = creativeWorld.getScripts();
            if (scripts == null) return;
            boolean removed = scripts.removeIf(script -> script.getRootBlock() != null && script.getRootBlock().getId().equals(eventBlock.getId()));
            if (removed) {
                worldManager.saveWorld(creativeWorld);
                logger.fine("Removed script for block at: " + formatLocation(location));
            }
        } catch (Exception e) {
            logger.warning("Error removing script: " + e.getMessage());
        }
    }
    
    /**
     * Сканирует физические блоки в мире для построения структуры скрипта
     * Реализует стиль эталонной системы: компиляция из мира с полным сканированием структуры
     *
     * Scans physical blocks in the world to build the script structure
     * Implements reference system-style: compilation from world with full structure scanning
     *
     * Scannt physische Blöcke in der Welt, um die Skriptstruktur zu erstellen
     * Implementiert Referenzsystem-Stil: Kompilierung aus der Welt mit vollständigem Struktur-Scanning
     */
    private void scanPhysicalBlocks(Location startLocation, CodeBlock startBlock) {
        World world = startLocation.getWorld();
        
        
        
        
        int scanRadius = 25; 
        
        
        int startX = Math.max(0, startLocation.getBlockX() - scanRadius);
        int endX = Math.min(255, startLocation.getBlockX() + scanRadius);
        int startZ = Math.max(0, startLocation.getBlockZ() - scanRadius);
        int endZ = Math.min(255, startLocation.getBlockZ() + scanRadius);
        int y = startLocation.getBlockY();
        
        int blocksProcessed = 0;
        
        logger.fine("Scanning physical blocks in area: (" + startX + "," + startZ + ") to (" + endX + "," + endZ + ")");
        
        
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                Location checkLocation = new Location(world, x, y, z);
                Block block = checkLocation.getBlock();
                // Removed redundant null check - static analysis flagged it as always non-null when this method is called
                // According to Bukkit API, getBlock() should never return null
                // This check is kept for safety but should never be true
                
                
                
                
                if (blockConfigService.isCodeBlock(block.getType())) {
                    
                    
                    
                    
                    CodeBlock codeBlock = blockPlacementHandler.getCodeBlock(checkLocation);
                    boolean wasCreated = false;
                    
                    if (codeBlock == null) {
                        
                        
                        
                        codeBlock = createCodeBlockFromPhysicalBlock(block);
                        
                        
                        
                        
                        blockPlacementHandler.getAllCodeBlocks().put(checkLocation, codeBlock);
                        wasCreated = true;
                    }
                    
                    
                    
                    
                    
                    if ((codeBlock.getAction() == null || "NOT_SET".equals(codeBlock.getAction()))) {
                        String action = readActionFromSign(checkLocation);
                        if (action != null) {
                            codeBlock.setAction(action);
                            if (wasCreated) {
                                logger.fine("Created code block with action '" + action + "' at " + formatLocation(checkLocation));
                                
                                
                            }
                        }
                    }
                    
                    
                    
                    
                    
                    readParametersFromContainer(checkLocation, codeBlock);
                    
                    blocksProcessed++;
                }
                
                
                
                else if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
                    CodeBlock codeBlock = blockPlacementHandler.getCodeBlock(checkLocation);
                    boolean wasCreated = false;
                    
                    if (codeBlock == null) {
                        
                        
                        
                        codeBlock = createBracketBlockFromPhysicalBlock(block);
                        
                        
                        
                        
                        blockPlacementHandler.getAllCodeBlocks().put(checkLocation, codeBlock);
                        wasCreated = true;
                    }
                    
                    
                    
                    
                    if (wasCreated) {
                        logger.fine("Created bracket block at " + formatLocation(checkLocation));
                        
                        
                        blocksProcessed++;
                    }
                }
            }
        }
        
        logger.fine("Physical block scan completed. Processed " + blocksProcessed + " blocks.");
        
        
    }
    
    /**
     * Создает CodeBlock из физического блока в мире
     * Реализует стиль эталонной системы: компиляция из мира с полным сканированием структуры
     *
     * Creates a CodeBlock from a physical block in the world
     * Implements reference system-style: compilation from world with full structure scanning
     *
     * Erstellt einen CodeBlock aus einem physischen Block in der Welt
     * Implementiert Referenzsystem-Stil: Kompilierung aus der Welt mit vollständigem Struktur-Scanning
     */
    private CodeBlock createCodeBlockFromPhysicalBlock(Block block) {
        
        
        // According to Bukkit API, getBlock() should never return null
        // The condition "block == null" is always false, so this check is redundant
        // This check has been removed as it's no longer needed
        // Removed redundant null check: if (block == null) return null;
        
        Material material = block.getType();
        String action = "NOT_SET"; 
        
        
        
        
        
        
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(material);
        if (config != null) {
            if (config.getDefaultAction() != null) {
                action = config.getDefaultAction();
            } else {
                action = config.getId();
            }
        }
        
        
        
        
        CodeBlock codeBlock = new CodeBlock(material.name(), action);
        
        return codeBlock;
    }
    
    /**
     * Создает CodeBlock скобки из физического блока поршня
     *
     * Creates a bracket CodeBlock from a physical piston block
     *
     * Erstellt einen Klammer-CodeBlock aus einem physischen Kolbenblock
     */
    private CodeBlock createBracketBlockFromPhysicalBlock(Block block) {
        
        
        // According to Bukkit API, getBlock() should never return null
        // The condition "block == null" is always false, so this check is redundant
        // This check has been removed as it's no longer needed
        // Removed redundant null check: if (block == null) return null;
        
        Material material = block.getType();
        
        
        
        
        CodeBlock codeBlock = new CodeBlock(material.name(), "BRACKET");
        
        
        
        
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Block adjacentBlock = block.getRelative(face);
            
            if (adjacentBlock.getState() instanceof Sign) {
                Sign sign = (Sign) adjacentBlock.getState();
                String[] lines = sign.getLines();
                
                if (lines.length > 1) {
                    String line2 = ChatColor.stripColor(lines[1]).trim();
                    if (line2.contains("{")) {
                        codeBlock.setBracketType(CodeBlock.BracketType.OPEN);
                        break;
                    } else if (line2.contains("}")) {
                        codeBlock.setBracketType(CodeBlock.BracketType.CLOSE);
                        break;
                    }
                }
            }
        }
        
        
        
        
        if (codeBlock.getBracketType() == null) {
            if (block.getBlockData() instanceof org.bukkit.block.data.type.Piston pistonData) {
                BlockFace facing = pistonData.getFacing();
                
                
                
                
                
                if (facing == BlockFace.EAST) {
                    codeBlock.setBracketType(CodeBlock.BracketType.OPEN);
                } else if (facing == BlockFace.WEST) {
                    codeBlock.setBracketType(CodeBlock.BracketType.CLOSE);
                } else {
                    codeBlock.setBracketType(CodeBlock.BracketType.OPEN); 
                    
                    
                }
            }
        }
        
        return codeBlock;
    }
    
    /**
     * Проверяет, представляет ли CodeBlock событие (начальную точку для скрипта)
     * 
     * @param codeBlock Проверяемый CodeBlock
     * @return true, если это блок события
     *
     * Checks if a CodeBlock represents an event (starting point for a script)
     * 
     * @param codeBlock Der zu prüfende CodeBlock
     * @return true, wenn dies ein Ereignisblock ist
     *
     * Prüft, ob ein CodeBlock ein Ereignis darstellt (Startpunkt für ein Skript)
     */
    private boolean isEventBlock(CodeBlock codeBlock) {
        
        
        
        
        return Material.getMaterial(codeBlock.getMaterialName()) == Material.DIAMOND_BLOCK;
    }

    /**
     * Сканирует мир для поиска всех кодовых блоков и их связей
     * Это ядро функции "компиляции из мира"
     * 
     * @param world Мир для сканирования
     * @return Карта расположений CodeBlocks с установленными связями
     *
     * Scans the world to find all code blocks and their relationships
     * This is the core of the "compilation from world" feature
     * 
     * @param world Die zu scannende Welt
     * @return Karte der Positionen zu CodeBlocks mit etablierten Beziehungen
     *
     * Scannt die Welt, um alle Code-Blöcke und ihre Beziehungen zu finden
     * Dies ist der Kern der Funktion "Kompilierung aus der Welt"
     */
    public Map<Location, CodeBlock> scanWorldStructure(World world) {
        Map<Location, CodeBlock> scannedBlocks = new HashMap<>();
        
        
        
        
        logger.info("Starting enhanced world scan for code structures in world: " + world.getName());
        
        
        
        int minX = 0, maxX = 255;
        int minZ = 0, maxZ = 255;
        int y = world.getHighestBlockYAt(0, 0); 
        
        
    
        int blocksScanned = 0;
        int blocksProcessed = 0;
        
        
        
        
        for (int chunkX = minX; chunkX <= maxX; chunkX += 16) {
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ += 16) {
                
                
                
                for (int x = chunkX; x < Math.min(chunkX + 16, maxX + 1); x++) {
                    for (int z = chunkZ; z < Math.min(chunkZ + 16, maxZ + 1); z++) {
                        Location checkLocation = new Location(world, x, y, z);
                        Block block = checkLocation.getBlock();
                        
                        
                        
                        
                        // According to Bukkit API, getBlock() should never return null
                        // The condition "block == null" is always false, so this check is redundant
                        // This check has been removed as it's no longer needed
                        // Removed redundant null check: if (block == null) return null;
                        
                        blocksScanned++;
                        
                        
                        
                        
                        if (blockConfigService.isCodeBlock(block.getType()) || 
                            block.getType() == Material.PISTON || 
                            block.getType() == Material.STICKY_PISTON) {
                            
                            
                            
                            
                            CodeBlock codeBlock = blockPlacementHandler.getCodeBlock(checkLocation);
                            boolean isNewBlock = (codeBlock == null);
                            
                            if (codeBlock == null) {
                                if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
                                    codeBlock = createBracketBlockFromPhysicalBlock(block);
                                } else {
                                    codeBlock = createCodeBlockFromPhysicalBlock(block);
                                }
                                
                                
                                
                                
                                blockPlacementHandler.getAllCodeBlocks().put(checkLocation, codeBlock);
                            }
                            
                            
                            
                            
                            
                            
                            
                            if (codeBlock.getAction() == null || "NOT_SET".equals(codeBlock.getAction())) {
                                String action = readActionFromSign(checkLocation);
                                if (action != null) {
                                    codeBlock.setAction(action);
                                }
                            }
                            
                            
                            
                            
                            readParametersFromContainer(checkLocation, codeBlock);
                            
                            scannedBlocks.put(checkLocation, codeBlock);
                            blocksProcessed++;
                            
                            
                            
                            
                            if (isNewBlock) {
                                logger.fine("Discovered new code block at " + formatLocation(checkLocation) + 
                                    " with action: " + codeBlock.getAction());
                                
                                
                            }
                        }
                    }
                }
            }
        }
        
        logger.info("World scan completed. Scanned " + blocksScanned + " blocks, processed " + blocksProcessed + " code blocks.");
        
        
        
        return scannedBlocks;
    }

    /**
     * Читает тип действия из таблички блока с улучшенным парсингом
     * 
     * @param blockLocation Расположение блока
     * @return Тип действия или null, если не найден
     *
     * Reads action type from a block's sign with enhanced parsing
     * 
     * @param blockLocation Die Position des Blocks
     * @return Der Aktionstyp oder null, wenn nicht gefunden
     *
     * Liest Aktionstyp aus einem Block-Schild mit verbesserter Analyse
     */
    private String readActionFromSign(Location blockLocation) {
        
        
        
        Block block = blockLocation.getBlock();
        
        
        org.bukkit.block.BlockFace[] faces = {
            org.bukkit.block.BlockFace.NORTH,
            org.bukkit.block.BlockFace.SOUTH,
            org.bukkit.block.BlockFace.EAST,
            org.bukkit.block.BlockFace.WEST
        };
        
        for (org.bukkit.block.BlockFace face : faces) {
            Block adjacentBlock = block.getRelative(face);
            
            // Condition adjacentBlock == null is always false
            // Removed redundant null check
            if (adjacentBlock.getState() instanceof Sign) {
                Sign sign = (Sign) adjacentBlock.getState();
                String[] lines = sign.getLines();
                
                
                
                for (String line : lines) {
                    String cleanLine = ChatColor.stripColor(line).trim();
                    if (!cleanLine.isEmpty() && !cleanLine.equals("============") && 
                        !cleanLine.contains("Клик") && !cleanLine.contains("Скобка") &&
                        !cleanLine.contains("★★★★★★★★★★★★") && !cleanLine.contains("➜")) {
                        
                        
                        
                        
                        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(block.getType());
                        if (config != null) {
                            
                            
                            
                            List<String> availableActions = blockConfigService.getActionsForMaterial(block.getType());
                            for (String action : availableActions) {
                                if (action.equalsIgnoreCase(cleanLine)) {
                                    logger.fine("Found exact action match: " + action + " for block at " + formatLocation(blockLocation));
                                    
                                    
                                    return action;
                                }
                            }
                            
                            
                            
                            
                            for (String action : availableActions) {
                                if (action.toLowerCase().contains(cleanLine.toLowerCase()) ||
                                    cleanLine.toLowerCase().contains(action.toLowerCase())) {
                                    logger.fine("Found partial action match: " + action + " for block at " + formatLocation(blockLocation));
                                    
                                    
                                    return action;
                                }
                            }
                        }
                        
                        
                        
                        
                        String determinedAction = determineActionFromContext(cleanLine, block.getType());
                        if (determinedAction != null) {
                            logger.fine("Determined action from context: " + determinedAction + " for block at " + formatLocation(blockLocation));
                            
                            
                            return determinedAction;
                        }
                        
                        
                        
                        
                        logger.fine("Using raw sign text as action: " + cleanLine + " for block at " + formatLocation(blockLocation));
                        
                        
                        return cleanLine;
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Определяет действие из контекстных подсказок и типа блока
     * 
     * @param signText Текст из таблички
     * @param blockType Тип блока
     * @return Определенное действие или null, если не определено
     *
     * Determines action from context clues and block type
     * 
     * @param signText Der Text vom Schild
     * @param blockType Der Blocktyp
     * @return Die bestimmte Aktion oder null, wenn unbestimmt
     *
     * Bestimmt Aktion aus Kontexthinweisen und Blocktyp
     */
    private String determineActionFromContext(String signText, Material blockType) {
        
        
        
        String lowerText = signText.toLowerCase();
        
        
        
        
        if (blockType == Material.DIAMOND_BLOCK) {
            if (lowerText.contains("join") || lowerText.contains("вход")) return "onJoin";
            if (lowerText.contains("leave") || lowerText.contains("выход")) return "onLeave";
            if (lowerText.contains("chat") || lowerText.contains("чат")) return "onChat";
            if (lowerText.contains("break") || lowerText.contains("сломать")) return "onBlockBreak";
            if (lowerText.contains("place") || lowerText.contains("поставить")) return "onBlockPlace";
        }
        
        
        
        
        if (blockType == Material.COBBLESTONE) {
            if (lowerText.contains("message") || lowerText.contains("сообщение")) return "sendMessage";
            if (lowerText.contains("teleport") || lowerText.contains("телепорт")) return "teleport";
            if (lowerText.contains("give") || lowerText.contains("выдать")) return "giveItem";
            if (lowerText.contains("sound") || lowerText.contains("звук")) return "playSound";
        }
        
        
        
        
        if (blockType == Material.OAK_PLANKS) {
            if (lowerText.contains("item") || lowerText.contains("предмет")) return "hasItem";
            if (lowerText.contains("op") || lowerText.contains("оператор")) return "isOp";
            if (lowerText.contains("near") || lowerText.contains("рядом")) return "isNearBlock";
        }
        
        return null; 
        
        
    }
    
    private void readParametersFromContainer(Location blockLocation, CodeBlock codeBlock) {
        
        ChestParser chestParser = ChestParser.forAdjacentChest(blockLocation);
        
        if (chestParser != null) {
            
            convertChestItemsToParameters(chestParser, codeBlock);
            
            logger.fine("Found adjacent chest with parameters for block at " + blockLocation);
            
            
            
            
            
            if (chestParser.getChestLocation().getWorld() != null) {
                chestParser.getChestLocation().getWorld().spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, 
                    chestParser.getChestLocation().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 1.0);
            }
        }
    }

    /**
     * Преобразует предметы из сундука в параметры DataValue в CodeBlock
     *
     * Converts chest items to DataValue parameters in CodeBlock
     *
     * Konvertiert Truhengegenstände in DataValue-Parameter im CodeBlock
     */
    private void convertChestItemsToParameters(ChestParser chestParser, CodeBlock codeBlock) {
        Map<String, DataValue> newParameters = new HashMap<>();
        int processedItems = 0;
        
        
        
        
        for (int slot = 0; slot < chestParser.getChestInventory().getSize(); slot++) {
            
            String textParam = chestParser.getText(slot);
            if (textParam != null) {
                String paramName = getParameterNameForSlot(codeBlock.getAction(), slot);
                newParameters.put(paramName, new TextValue(textParam));
                processedItems++;
                continue;
            }
            
            double numberParam = chestParser.getNumber(slot);
            if (numberParam != 0) { 
                String paramName = getParameterNameForSlot(codeBlock.getAction(), slot);
                newParameters.put(paramName, new NumberValue(numberParam));
                processedItems++;
                continue;
            }
            
            Location locationParam = chestParser.getLocation(slot);
            if (locationParam != null) {
                String paramName = getParameterNameForSlot(codeBlock.getAction(), slot);
                newParameters.put(paramName, new LocationValue(locationParam));
                processedItems++;
                continue;
            }
            
            ItemStack itemParam = chestParser.getItem(slot);
            if (itemParam != null && itemParam.getType() != Material.AIR) {
                String paramName = getParameterNameForSlot(codeBlock.getAction(), slot);
                newParameters.put(paramName, convertItemStackToDataValue(itemParam));
                processedItems++;
            }
        }
        
        
        
        
        for (Map.Entry<String, DataValue> entry : newParameters.entrySet()) {
            codeBlock.setParameter(entry.getKey(), entry.getValue());
        }
        
        if (processedItems > 0) {
            logger.fine("Converted " + processedItems + " chest items to DataValue parameters for block " + codeBlock.getAction());
        }
    }

    /**
     * Преобразует ItemStack в DataValue
     *
     * Converts an ItemStack to a DataValue
     *
     * Konvertiert einen ItemStack in einen DataValue
     */
    private DataValue convertItemStackToDataValue(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return new AnyValue(null);
        }
        
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        String displayName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "";
        
        
        
        
        String cleanName = ChatColor.stripColor(displayName).trim();
        
        
        
        
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            
            
            
            if (lore != null) {
                for (String line : lore) {
                    if (line.startsWith("§8Parameter: ")) {
                        
                        
                        
                        return extractValueFromParameterItem(item, lore);
                    }
                }
            }
        }
        
        
        
        
        switch (item.getType()) {
            case PAPER:
                
                
                
                if (!cleanName.isEmpty()) {
                    return new TextValue(cleanName);
                } else {
                    return new TextValue("Текст");
                    
                    
                }
            
            case GOLD_NUGGET:
            case GOLD_INGOT:
                
                
                
                if (!cleanName.isEmpty()) {
                    try {
                        String numberStr = cleanName.replaceAll("[^0-9.-]", "");
                        if (!numberStr.isEmpty()) {
                            return new NumberValue(Double.parseDouble(numberStr));
                        }
                    } catch (NumberFormatException e) {
                        // If we can't parse the number, use the item amount as fallback
                        return new NumberValue(item.getAmount());
                    }
                }
                return new NumberValue(item.getAmount());
            
            case LIME_DYE:
                return new BooleanValue(true);
            case RED_DYE:
                return new BooleanValue(false);
            
            case CHEST:
            case BARREL:
                
                
                
                return new ListValue(new ArrayList<>());
            
            default:
                
                
                
                if (!cleanName.isEmpty()) {
                    return new TextValue(cleanName);
                } else {
                    
                    
                    
                    return new TextValue(item.getType().name().toLowerCase().replace("_", " "));
                }
        }
    }

    /**
     * Извлекает значение из элемента параметра, который мы создали
     *
     * Extracts value from a parameter item we created
     *
     * Extrahiert Wert aus einem von uns erstellten Parameterelement
     */
    private DataValue extractValueFromParameterItem(ItemStack item, List<String> lore) {
        
        
        
        
        
        
        if (lore == null) {
            return new TextValue(item.getType().name().toLowerCase());
        }
        
        for (String line : lore) {
            String cleanLine = ChatColor.stripColor(line);
            if (cleanLine.startsWith("Value: ")) {
                String valueStr = cleanLine.substring(7); 
                
                
                
                
                
                int index = lore.indexOf(line);
                
                
                
                if (index > 0 && index < lore.size()) {
                    String typeLine = ChatColor.stripColor(lore.get(index - 1));
                    
                    if (typeLine.contains("Number")) {
                        try {
                            return new NumberValue(Double.parseDouble(valueStr));
                        } catch (NumberFormatException e) {
                            return new TextValue(valueStr);
                        }
                    } else if (typeLine.contains("Boolean")) {
                        return new BooleanValue("True".equalsIgnoreCase(valueStr));
                    } else if (typeLine.contains("List")) {
                        return new ListValue(new ArrayList<>());
                    }
                }
                
                
                
                return new TextValue(valueStr);
            }
        }
        
        
        
        
        return new TextValue(item.getType().name().toLowerCase());
    }

    /**
     * Получает имя параметра для определенного слота на основе типа действия
     *
     * Gets parameter name for a specific slot based on action type
     *
     * Ruft Parameternamen für einen bestimmten Slot basierend auf dem Aktionstyp ab
     */
    private String getParameterNameForSlot(String action, int slot) {
        
        
        
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
                    case 0 -> "variable"; 
                    
                    
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
     * Форматирует расположение для целей логирования/отображения
     * 
     * @param location Расположение для форматирования
     * @return Отформатированное строковое представление
     *
     * Formats a location for logging/display purposes
     * 
     * @param location Die zu formatierende Position
     * @return Formatierter String-Repräsentation
     *
     * Formatiert eine Position für Protokollierungs-/Anzeigezwecke
     */
    private String formatLocation(Location location) {
        if (location == null) return "null";
        return String.format("(%d, %d, %d)", location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Получает сервис конфигурации блоков
     * 
     * @return BlockConfigService
     *
     * Gets the block configuration service
     * 
     * @return Der BlockConfigService
     *
     * Ruft den Blockkonfigurationsdienst ab
     */
    public BlockConfigService getBlockConfigService() {
        return blockConfigService;
    }
    
    /**
     * Компилирует все скрипты в мире и генерирует строки исполняемого кода
     * Этот метод имитирует подход эталонной системы преобразования структур мира в строки кода
     * 
     * @param world Мир для компиляции
     * @return Список скомпилированных строк кода
     *
     * Compiles all scripts in a world and generates executable code strings
     * This method mimics reference system's approach of converting world structures to code strings
     * 
     * @param world Die zu kompilierende Welt
     * @return Liste der kompilierten Code-Zeichenfolgen
     *
     * Kompiliert alle Skripte in einer Welt und generiert ausführbare Code-Zeichenfolgen
     * Diese Methode ahmt den Ansatz des Referenzsystems nach, Weltenstrukturen in Code-Zeichenfolgen umzuwandeln
     */
    public List<String> compileWorldToCodeStrings(World world) {
        List<String> compiledCode = new ArrayList<>();
        
        
        
        
        Map<Location, CodeBlock> scannedBlocks = scanWorldStructure(world);
        
        
        
        
        Map<Integer, List<CodeBlock>> blocksByLine = new HashMap<>();
        
        for (Map.Entry<Location, CodeBlock> entry : scannedBlocks.entrySet()) {
            Location location = entry.getKey();
            CodeBlock block = entry.getValue();
            
            int yLevel = location.getBlockY();
            blocksByLine.computeIfAbsent(yLevel, k -> new ArrayList<>()).add(block);
        }
        
        
        
        
        for (Map.Entry<Integer, List<CodeBlock>> lineEntry : blocksByLine.entrySet()) {
            int yLevel = lineEntry.getKey();
            List<CodeBlock> lineBlocks = lineEntry.getValue();
            
            
            
            
            sortLineBlocks(lineBlocks, scannedBlocks);
            
            
            
            
            List<String> lineCode = new ArrayList<>();
            for (CodeBlock block : lineBlocks) {
                String function = getFunctionFromBlock(block);
                if (function != null && !function.isEmpty()) {
                    lineCode.add(function);
                }
            }
            
            if (!lineCode.isEmpty()) {
                
                
                
                String lineResult = String.join("&", lineCode);
                compiledCode.add(lineResult);
            }
        }
        
        logger.info("Compiled " + compiledCode.size() + " lines of code from world: " + world.getName());
        
        
        
        String worldId = world.getName().replace("-code", "");
        if (!compiledCode.isEmpty()) {
            WorldCode.setCode(worldId, compiledCode);
            logger.info("Saved " + compiledCode.size() + " lines of compiled code for world: " + worldId);
            
            
            
        }
        
        return compiledCode;
    }

    /**
     * Преобразует CodeBlock в его функциональное представление
     * Это имитирует метод GetFunc_new.get() эталонной системы
     * 
     * @param block Преобразуемый CodeBlock
     * @return Строковое представление функции
     *
     * Converts a CodeBlock to its function representation
     * This mimics reference system's GetFunc_new.get() method
     * 
     * @param block Der zu konvertierende CodeBlock
     * @return Funktions-Zeichenfolgenrepräsentation
     *
     * Konvertiert einen CodeBlock in seine Funktionsdarstellung
     * Dies ahmt die Methode GetFunc_new.get() des Referenzsystems nach
     */
    /**
     * Sorts line blocks by their X coordinate
     */
    private void sortLineBlocks(List<CodeBlock> lineBlocks, Map<Location, CodeBlock> scannedBlocks) {
        lineBlocks.sort((a, b) -> {
            Location locA = null;
            Location locB = null;
            
            
            
            
            for (Map.Entry<Location, CodeBlock> blockEntry : scannedBlocks.entrySet()) {
                if (blockEntry.getValue() == a) locA = blockEntry.getKey();
                if (blockEntry.getValue() == b) locB = blockEntry.getKey();
            }
            
            if (locA != null && locB != null) {
                return Integer.compare(locA.getBlockX(), locB.getBlockX());
            }
            return 0;
        });
    }
    
    private String getFunctionFromBlock(CodeBlock block) {
        // According to static analysis, block is never null when this method is called
        // The condition "block == null" is always false, so this check is redundant
        // Removed redundant null check: if (block == null) return null;
        
        String action = block.getAction();
        
        
        
        if (action == null || action.equals("NOT_SET") || action.isEmpty()) {
            
            
            
            if (Material.getMaterial(block.getMaterialName()) == Material.PISTON || Material.getMaterial(block.getMaterialName()) == Material.STICKY_PISTON) {
                if (block.getBracketType() == CodeBlock.BracketType.OPEN) {
                    return "{";
                } else if (block.getBracketType() == CodeBlock.BracketType.CLOSE) {
                    return "}";
                }
            }
            return null;
        }
        
        
        
        
        if (Material.getMaterial(block.getMaterialName()) == Material.PISTON || Material.getMaterial(block.getMaterialName()) == Material.STICKY_PISTON) {
            if (block.getBracketType() == CodeBlock.BracketType.OPEN) {
                return "{";
            } else if (block.getBracketType() == CodeBlock.BracketType.CLOSE) {
                return "}";
            }
            return null;
        }
        
        
        
        
        if (Material.getMaterial(block.getMaterialName()) == Material.DIAMOND_BLOCK) {
            switch (action) {
                case "onJoin": return "joinEvent";
                case "onLeave": return "quitEvent";
                case "onChat": return "messageEvent";
                case "onBlockBreak": return "breakEvent";
                case "onBlockPlace": return "placeEvent";
                case "onPlayerMove": return "moveEvent";
                case "onPlayerDeath": return "playerDeathEvent";
                default: return action;
            }
        }
        
        
        
        
        if (Material.getMaterial(block.getMaterialName()) == Material.COBBLESTONE) {
            return action;
        }
        
        
        
        
        return action;
    }

    /**
     * Сохраняет скомпилированный код в файл конфигурации как в системе WorldCode эталонной системы
     * 
     * @param worldId ID мира
     * @param codeLines Строки скомпилированного кода
     *
     * Saves compiled code to a configuration file like reference system's WorldCode system
     * 
     * @param worldId Die Welt-ID
     * @param codeLines Die kompilierten Codezeilen
     *
     * Speichert kompilierten Code in einer Konfigurationsdatei wie im WorldCode-System des Referenzsystems
     */
    public void saveCompiledCode(String worldId, List<String> codeLines) {
        
        
        
        logger.info("Saving compiled code for world: " + worldId);
        
        
        
        logger.info("Code lines: " + codeLines.size());
        
        
        
        com.megacreative.configs.WorldCode.setCode(worldId, codeLines);
        
        logger.info("Successfully saved compiled code to WorldCode configuration");
        
        
        
    }
}