package com.megacreative.services;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.services.BlockConfigService;
import com.megacreative.configs.WorldCode;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.*;

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
public class CodeCompiler {
    
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
        // Начало компиляции мира:
        // Beginn der Kompilierung der Welt:
        
        // First, scan the world structure to ensure all blocks are registered
        // Сначала сканируем структуру мира, чтобы убедиться, что все блоки зарегистрированы
        // Zuerst wird die Weltstruktur gescannt, um sicherzustellen, dass alle Blöcke registriert sind
        Map<Location, CodeBlock> scannedBlocks = scanWorldStructure(world);
        logger.info("World scan found " + scannedBlocks.size() + " code blocks");
        // Сканирование мира нашло кодовых блоков
        // Weltscan fand Codeblöcke
        // Scan der Welt fand Code-Blöcke
        
        List<CodeScript> compiledScripts = new ArrayList<>();
        int scriptCount = 0;
        int errorCount = 0;
        
        // Find all event blocks in the world (diamond blocks that represent events)
        // Найти все блоки событий в мире (алмазные блоки, представляющие события)
        // Finde alle Ereignisblöcke in der Welt (Diamantblöcke, die Ereignisse darstellen)
        Map<Location, CodeBlock> allCodeBlocks = blockPlacementHandler.getAllCodeBlocks();
        
        for (Map.Entry<Location, CodeBlock> entry : allCodeBlocks.entrySet()) {
            Location location = entry.getKey();
            CodeBlock codeBlock = entry.getValue();
            
            // Only process blocks in the specified world
            // Обрабатывать только блоки в указанном мире
            // Verarbeite nur Blöcke in der angegebenen Welt
            if (!location.getWorld().equals(world)) {
                continue;
            }
            
            // Check if this is an event block (starting point for a script)
            // Проверить, является ли это блоком события (начальная точка для скрипта)
            // Prüfe, ob dies ein Ereignisblock ist (Startpunkt für ein Skript)
            if (isEventBlock(codeBlock)) {
                try {
                    CodeScript script = compileScriptFromEventBlock(location, codeBlock);
                    if (script != null) {
                        compiledScripts.add(script);
                        scriptCount++;
                        logger.fine("Compiled script: " + script.getName());
                        // Скомпилированный скрипт:
                        // Kompiliertes Skript:
                    } else {
                        logger.warning("Failed to compile script from event block at " + formatLocation(location));
                        // Не удалось скомпилировать скрипт из блока события в
                        // Fehler beim Kompilieren des Skripts aus dem Ereignisblock bei
                        errorCount++;
                    }
                } catch (Exception e) {
                    logger.severe("Failed to compile script from event block at " + formatLocation(location) + ": " + e.getMessage());
                    // Не удалось скомпилировать скрипт из блока события в
                    // Fehler beim Kompilieren des Skripts aus dem Ereignisblock bei
                    e.printStackTrace();
                    errorCount++;
                }
            }
        }
        
        logger.info("Compilation completed. Found " + scriptCount + " scripts with " + errorCount + " errors.");
        // Компиляция завершена. Найдено скриптов с ошибками
        // Kompilierung abgeschlossen. Gefundene Skripte mit Fehlern
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
        // Create the script with the event block as root
        // Создать скрипт с блоком события в качестве корня
        // Erstelle das Skript mit dem Ereignisblock als Wurzel
        CodeScript script = new CodeScript(eventBlock);
        script.setName("Script from " + eventBlock.getAction() + " at " + formatLocation(eventLocation));
        script.setEnabled(true);
        script.setType(CodeScript.ScriptType.EVENT);
        
        logger.fine("Starting compilation of script from event block at " + formatLocation(eventLocation));
        // Начало компиляции скрипта из блока события в
        // Beginn der Kompilierung des Skripts aus dem Ereignisblock bei
        
        // Build the complete structure by scanning the world
        // Построить полную структуру, сканируя мир
        // Baue die vollständige Struktur durch Scannen der Welt
        buildScriptStructure(eventLocation, eventBlock, script);
        
        logger.fine("Completed compilation of script: " + script.getName());
        // Завершена компиляция скрипта:
        // Abgeschlossene Kompilierung des Skripts:
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
     * Dies implementiert die "Scanning"-Logik, die die physische Struktur in der Welt liest
     */
    private void buildScriptStructure(Location startLocation, CodeBlock startBlock, CodeScript script) {
        logger.fine("Building script structure starting from " + formatLocation(startLocation));
        // Построение структуры скрипта, начиная с
        // Aufbau der Skriptstruktur beginnend bei
        
        // Scan physical blocks in the world to build the complete structure
        // Сканировать физические блоки в мире для построения полной структуры
        // Scanne physische Blöcke in der Welt, um die vollständige Struktur zu erstellen
        scanPhysicalBlocks(startLocation, startBlock);
        
        // Use ScriptCompiler to recompile world scripts
        // Использовать ScriptCompiler для перекомпиляции скриптов мира
        // Verwende ScriptCompiler, um Weltenskripte neu zu kompilieren
        com.megacreative.coding.ScriptCompiler scriptCompiler = plugin.getServiceRegistry().getScriptCompiler();
        if (scriptCompiler != null) {
            logger.fine("Recompiling world scripts with ScriptCompiler");
            // Перекомпиляция скриптов мира с ScriptCompiler
            // Neukompilierung von Weltenskripten mit ScriptCompiler
            scriptCompiler.recompileWorldScripts(startLocation.getWorld());
        }
        
        logger.fine("Script structure building completed for script: " + script.getName());
        // Построение структуры скрипта завершено для скрипта:
        // Aufbau der Skriptstruktur abgeschlossen für Skript:
        // Script structure building completed for script:
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
        
        // Enhanced scanning with better area coverage
        // Улучшенное сканирование с лучшим покрытием области
        // Verbessertes Scannen mit besserer Flächenabdeckung
        int scanRadius = 25; // Increased scan radius
        // Увеличенный радиус сканирования
        // Erhöhter Scanradius
        int startX = Math.max(0, startLocation.getBlockX() - scanRadius);
        int endX = Math.min(255, startLocation.getBlockX() + scanRadius);
        int startZ = Math.max(0, startLocation.getBlockZ() - scanRadius);
        int endZ = Math.min(255, startLocation.getBlockZ() + scanRadius);
        int y = startLocation.getBlockY();
        
        int blocksProcessed = 0;
        
        logger.fine("Scanning physical blocks in area: (" + startX + "," + startZ + ") to (" + endX + "," + endZ + ")");
        // Сканирование физических блоков в области: до
        // Scannen physischer Blöcke im Bereich: bis
        // Scanning physical blocks in area: to
        
        // Look for code blocks in the area
        // Искать кодовые блоки в области
        // Suche nach Codeblöcken im Bereich
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                Location checkLocation = new Location(world, x, y, z);
                Block block = checkLocation.getBlock();
                
                // 🔧 FIX: Add null check for block
                // 🔧 ИСПРАВЛЕНИЕ: Добавить проверку на null для блока
                // 🔧 FIX: Null-Prüfung für Block hinzufügen
                if (block == null) {
                    continue;
                }
                
                // Check if this is a code block material
                // Проверить, является ли это материалом кодового блока
                // Prüfen, ob dies ein Codeblock-Material ist
                if (blockConfigService.isCodeBlock(block.getType())) {
                    // Try to get existing CodeBlock or create new one
                    // Попытаться получить существующий CodeBlock или создать новый
                    // Versuche, einen vorhandenen CodeBlock zu erhalten oder einen neuen zu erstellen
                    CodeBlock codeBlock = blockPlacementHandler.getCodeBlock(checkLocation);
                    boolean wasCreated = false;
                    
                    if (codeBlock == null) {
                        // Create new CodeBlock from physical block
                        // Создать новый CodeBlock из физического блока
                        // Erstelle neuen CodeBlock aus physischem Block
                        codeBlock = createCodeBlockFromPhysicalBlock(block);
                        // 🔧 FIX: codeBlock is never null here, so condition is always false
                        // 🔧 ИСПРАВЛЕНИЕ: codeBlock никогда не равен null здесь, поэтому условие всегда ложно
                        // 🔧 FIX: codeBlock ist hier nie null, daher ist die Bedingung immer falsch
                        // Removed unnecessary condition check as codeBlock is never null here
                        blockPlacementHandler.getAllCodeBlocks().put(checkLocation, codeBlock);
                        wasCreated = true;
                    }
                    
                    // Read action from sign if not already set
                    // Прочитать действие из таблички, если оно еще не установлено
                    // Lese Aktion aus Schild, wenn noch nicht gesetzt
                    // 🔧 FIX: codeBlock is never null here, so condition is always false
                    // 🔧 ИСПРАВЛЕНИЕ: codeBlock никогда не равен null здесь, поэтому условие всегда ложно
                    // 🔧 FIX: codeBlock ist hier nie null, daher ist die Bedingung immer falsch
                    if ((codeBlock.getAction() == null || "NOT_SET".equals(codeBlock.getAction()))) {
                        String action = readActionFromSign(checkLocation);
                        if (action != null) {
                            codeBlock.setAction(action);
                            if (wasCreated) {
                                logger.fine("Created code block with action '" + action + "' at " + formatLocation(checkLocation));
                                // Создан кодовый блок с действием в
                                // Erstellter Codeblock mit Aktion bei
                            }
                        }
                    }
                    
                    // Read parameters from container if available
                    // Прочитать параметры из контейнера, если доступно
                    // Parameter aus Container lesen, falls verfügbar
                    // 🔧 FIX: codeBlock is never null here, so condition is always false
                    // 🔧 ИСПРАВЛЕНИЕ: codeBlock никогда не равен null здесь, поэтому условие всегда ложно
                    // 🔧 FIX: codeBlock ist hier nie null, daher ist die Bedingung immer falsch
                    readParametersFromContainer(checkLocation, codeBlock);
                    
                    // 🔧 FIX: codeBlock is never null here, so condition is always false
                    // 🔧 ИСПРАВЛЕНИЕ: codeBlock никогда не равен null здесь, поэтому условие всегда ложно
                    // 🔧 FIX: codeBlock ist hier nie null, daher ist die Bedingung immer falsch
                    blocksProcessed++;
                }
                // Also check for bracket pistons
                // Также проверить на поршни скобок
                // Auch auf Klammerkolben prüfen
                else if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
                    CodeBlock codeBlock = blockPlacementHandler.getCodeBlock(checkLocation);
                    boolean wasCreated = false;
                    
                    if (codeBlock == null) {
                        // Create new CodeBlock for bracket
                        // Создать новый CodeBlock для скобки
                        // Erstelle neuen CodeBlock für Klammer
                        codeBlock = createBracketBlockFromPhysicalBlock(block);
                        // 🔧 FIX: codeBlock is never null here, so condition is always false
                        // 🔧 ИСПРАВЛЕНИЕ: codeBlock никогда не равен null здесь, поэтому условие всегда ложно
                        // 🔧 FIX: codeBlock ist hier nie null, daher ist die Bedingung immer falsch
                        // Removed unnecessary condition check as codeBlock is never null here
                        blockPlacementHandler.getAllCodeBlocks().put(checkLocation, codeBlock);
                        wasCreated = true;
                    }
                    
                    // 🔧 FIX: codeBlock is never null here, so condition is always false
                    // 🔧 ИСПРАВЛЕНИЕ: codeBlock никогда не равен null здесь, поэтому условие всегда ложно
                    // 🔧 FIX: codeBlock ist hier nie null, daher ist die Bedingung immer falsch
                    if (wasCreated) {
                        logger.fine("Created bracket block at " + formatLocation(checkLocation));
                        // Создан блок скобки в
                        // Erstellter Klammerblock bei
                        blocksProcessed++;
                    }
                }
            }
        }
        
        logger.fine("Physical block scan completed. Processed " + blocksProcessed + " blocks.");
        // Сканирование физических блоков завершено. Обработано блоков
        // Physischer Blockscan abgeschlossen. Verarbeitete Blöcke
        // Physical block scan completed. Processed blocks
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
        // 🔧 FIX: Add null check for block
        // 🔧 ИСПРАВЛЕНИЕ: Добавить проверку на null для блока
        // 🔧 FIX: Null-Prüfung für Block hinzufügen
        if (block == null) {
            return null;
        }
        
        Material material = block.getType();
        String action = "NOT_SET"; // Default to not set
        // По умолчанию не установлено
        // Standardmäßig nicht gesetzt
        
        // Try to determine action from block configuration
        // Попытаться определить действие из конфигурации блока
        // Versuche, die Aktion aus der Blockkonfiguration zu bestimmen
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(material);
        if (config != null) {
            if (config.getDefaultAction() != null) {
                action = config.getDefaultAction();
            } else {
                action = config.getId();
            }
        }
        
        // Create the CodeBlock
        // Создать CodeBlock
        // Erstelle den CodeBlock
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
        // 🔧 FIX: Add null check for block
        // 🔧 ИСПРАВЛЕНИЕ: Добавить проверку на null для блока
        // 🔧 FIX: Null-Prüfung für Block hinzufügen
        if (block == null) {
            return null;
        }
        
        Material material = block.getType();
        
        // Create the CodeBlock for bracket
        // Создать CodeBlock для скобки
        // Erstelle den CodeBlock für die Klammer
        CodeBlock codeBlock = new CodeBlock(material.name(), "BRACKET");
        
        // Try to determine bracket type from block data and sign
        // Попытаться определить тип скобки из данных блока и таблички
        // Versuche, den Klammentyp aus Blockdaten und Schild zu bestimmen
        Location location = block.getLocation();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Block adjacentBlock = block.getRelative(face);
            // 🔧 FIX: Add null check for adjacentBlock
            // 🔧 ИСПРАВЛЕНИЕ: Добавить проверку на null для соседнего блока
            // 🔧 FIX: Null-Prüfung für angrenzenden Block hinzufügen
            if (adjacentBlock == null) {
                continue;
            }
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
        
        // If we couldn't determine from sign, try from piston orientation
        // Если не удалось определить по табличке, попробовать по ориентации поршня
        // Wenn wir es nicht vom Schild bestimmen konnten, versuchen wir es von der Kolbenausrichtung
        if (codeBlock.getBracketType() == null) {
            if (block.getBlockData() instanceof org.bukkit.block.data.type.Piston pistonData) {
                BlockFace facing = pistonData.getFacing();
                // Simple heuristic: if facing east, it's likely an opening bracket
                // if facing west, it's likely a closing bracket
                // Простая эвристика: если направлен на восток, это, вероятно, открывающая скобка
                // если направлен на запад, это, вероятно, закрывающая скобка
                // Einfache Heuristik: wenn nach Osten ausgerichtet, ist es wahrscheinlich eine öffnende Klammer
                // wenn nach Westen ausgerichtet, ist es wahrscheinlich eine schließende Klammer
                if (facing == BlockFace.EAST) {
                    codeBlock.setBracketType(CodeBlock.BracketType.OPEN);
                } else if (facing == BlockFace.WEST) {
                    codeBlock.setBracketType(CodeBlock.BracketType.CLOSE);
                } else {
                    codeBlock.setBracketType(CodeBlock.BracketType.OPEN); // Default
                    // По умолчанию
                    // Standardmäßig
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
        // 🔧 FIX: codeBlock is never null here, so condition is always false
        // 🔧 ИСПРАВЛЕНИЕ: codeBlock никогда не равен null здесь, поэтому условие всегда ложно
        // 🔧 FIX: codeBlock ist hier nie null, daher ist die Bedingung immer falsch
        // Removed unnecessary null check as codeBlock is never null here
        return codeBlock.getMaterial() == Material.DIAMOND_BLOCK;
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
        
        // Enhanced world scanning with proper structure detection
        // Улучшенное сканирование мира с правильным обнаружением структуры
        // Verbessertes Welten-Scanning mit richtiger Struktur-Erkennung
        logger.info("Starting enhanced world scan for code structures in world: " + world.getName());
        // Начало улучшенного сканирования мира для кодовых структур в мире:
        // Beginn des verbesserten Weltenscans für Code-Strukturen in der Welt:
        
        // Iterate through the development area with optimized scanning
        // Итерироваться по области разработки с оптимизированным сканированием
        // Iteriere durch den Entwicklungsbereich mit optimiertem Scanning
        int minX = 0, maxX = 255;
        int minZ = 0, maxZ = 255;
        int y = world.getHighestBlockYAt(0, 0); // Assume consistent height in dev world
        // Предполагать постоянную высоту в мире разработки
        // Angenommene konstante Höhe in der Entwicklungs-Welt
    
        int blocksScanned = 0;
        int blocksProcessed = 0;
        
        // Scan in chunks for better performance
        // Сканировать по чанкам для лучшей производительности
        // In Chunks scannen für bessere Leistung
        for (int chunkX = minX; chunkX <= maxX; chunkX += 16) {
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ += 16) {
                // Scan each position in the chunk
                // Сканировать каждую позицию в чанке
                // Jede Position im Chunk scannen
                for (int x = chunkX; x < Math.min(chunkX + 16, maxX + 1); x++) {
                    for (int z = chunkZ; z < Math.min(chunkZ + 16, maxZ + 1); z++) {
                        Location checkLocation = new Location(world, x, y, z);
                        Block block = checkLocation.getBlock();
                        
                        // 🔧 FIX: Add null check for block
                        // 🔧 ИСПРАВЛЕНИЕ: Добавить проверку на null для блока
                        // 🔧 FIX: Null-Prüfung für Block hinzufügen
                        if (block == null) {
                            continue;
                        }
                        
                        blocksScanned++;
                        
                        // Identify code blocks by their material
                        // Идентифицировать кодовые блоки по их материалу
                        // Code-Blöcke nach ihrem Material identifizieren
                        if (blockConfigService.isCodeBlock(block.getType()) || 
                            block.getType() == Material.PISTON || 
                            block.getType() == Material.STICKY_PISTON) {
                            
                            // Create or get existing CodeBlock
                            // Создать или получить существующий CodeBlock
                            // Erstelle oder erhalte vorhandenen CodeBlock
                            CodeBlock codeBlock = blockPlacementHandler.getCodeBlock(checkLocation);
                            boolean isNewBlock = (codeBlock == null);
                            
                            if (codeBlock == null) {
                                if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
                                    codeBlock = createBracketBlockFromPhysicalBlock(block);
                                } else {
                                    codeBlock = createCodeBlockFromPhysicalBlock(block);
                                }
                                
                                // 🔧 FIX: codeBlock is never null here, so condition is always false
                                // 🔧 ИСПРАВЛЕНИЕ: codeBlock никогда не равен null здесь, поэтому условие всегда ложно
                                // 🔧 FIX: codeBlock ist hier nie null, daher ist die Bedingung immer falsch
                                blockPlacementHandler.getAllCodeBlocks().put(checkLocation, codeBlock);
                            }
                            
                            // 🔧 FIX: codeBlock is never null here, so condition is always false
                            // 🔧 ИСПРАВЛЕНИЕ: codeBlock никогда не равен null здесь, поэтому условие всегда ложно
                            // 🔧 FIX: codeBlock ist hier nie null, daher ist die Bedingung immer falsch
                            // Read action from sign
                            // Прочитать действие из таблички
                            // Aktion aus Schild lesen
                            if (codeBlock.getAction() == null || "NOT_SET".equals(codeBlock.getAction())) {
                                String action = readActionFromSign(checkLocation);
                                if (action != null) {
                                    codeBlock.setAction(action);
                                }
                            }
                            
                            // Read parameters from container
                            // Прочитать параметры из контейнера
                            // Parameter aus Container lesen
                            readParametersFromContainer(checkLocation, codeBlock);
                            
                            scannedBlocks.put(checkLocation, codeBlock);
                            blocksProcessed++;
                            
                            // Log new block discovery
                            // Записать обнаружение нового блока
                            // Neuentdeckung von Block protokollieren
                            if (isNewBlock) {
                                logger.fine("Discovered new code block at " + formatLocation(checkLocation) + 
                                    " with action: " + codeBlock.getAction());
                                // Обнаружен новый кодовый блок в с действием:
                                // Neuer Codeblock bei mit Aktion entdeckt:
                            }
                        }
                    }
                }
            }
        }
        
        logger.info("World scan completed. Scanned " + blocksScanned + " blocks, processed " + blocksProcessed + " code blocks.");
        // Сканирование мира завершено. Просканировано блоков, обработано кодовых блоков
        // Weltenscan abgeschlossen. Gescannte Blöcke, verarbeitete Codeblöcke
        // World scan completed. Scanned blocks, processed code blocks
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
        // Look for signs adjacent to the block
        // Искать таблички, прилегающие к блоку
        // Suche nach Schildern, die an den Block angrenzen
        Block block = blockLocation.getBlock();
        
        // 🔧 FIX: Add null check for block
        // 🔧 ИСПРАВЛЕНИЕ: Добавить проверку на null для блока
        // 🔧 FIX: Null-Prüfung für Block hinzufügen
        if (block == null) {
            return null;
        }
        
        // Check all adjacent faces for signs
        // Проверить все прилегающие грани на наличие табличек
        // Alle angrenzenden Flächen auf Schilder prüfen
        org.bukkit.block.BlockFace[] faces = {
            org.bukkit.block.BlockFace.NORTH,
            org.bukkit.block.BlockFace.SOUTH,
            org.bukkit.block.BlockFace.EAST,
            org.bukkit.block.BlockFace.WEST
        };
        
        for (org.bukkit.block.BlockFace face : faces) {
            Block adjacentBlock = block.getRelative(face);
            // 🔧 FIX: Add null check for adjacentBlock
            // 🔧 ИСПРАВЛЕНИЕ: Добавить проверку на null для соседнего блока
            // 🔧 FIX: Null-Prüfung für angrenzenden Block hinzufügen
            if (adjacentBlock == null) {
                continue;
            }
            if (adjacentBlock.getState() instanceof Sign) {
                Sign sign = (Sign) adjacentBlock.getState();
                String[] lines = sign.getLines();
                
                // Look for action information in the sign with enhanced parsing
                // Искать информацию о действии в табличке с улучшенным парсингом
                // Suche nach Aktionsinformationen im Schild mit verbesserter Analyse
                for (String line : lines) {
                    String cleanLine = ChatColor.stripColor(line).trim();
                    if (!cleanLine.isEmpty() && !cleanLine.equals("============") && 
                        !cleanLine.contains("Клик") && !cleanLine.contains("Скобка") &&
                        !cleanLine.contains("★★★★★★★★★★★★") && !cleanLine.contains("➜")) {
                        
                        // Try to match with known actions from configuration
                        // Попытаться сопоставить с известными действиями из конфигурации
                        // Versuche, mit bekannten Aktionen aus der Konfiguration abzugleichen
                        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(block.getType());
                        if (config != null) {
                            // First try exact match
                            // Сначала попробовать точное совпадение
                            // Zuerst exakte Übereinstimmung versuchen
                            List<String> availableActions = blockConfigService.getActionsForMaterial(block.getType());
                            for (String action : availableActions) {
                                if (action.equalsIgnoreCase(cleanLine)) {
                                    logger.fine("Found exact action match: " + action + " for block at " + formatLocation(blockLocation));
                                    // Найдено точное совпадение действия: для блока в
                                    // Exakte Aktionsübereinstimmung gefunden: für Block bei
                                    return action;
                                }
                            }
                            
                            // Then try partial match
                            // Затем попробовать частичное совпадение
                            // Dann partielle Übereinstimmung versuchen
                            for (String action : availableActions) {
                                if (action.toLowerCase().contains(cleanLine.toLowerCase()) ||
                                    cleanLine.toLowerCase().contains(action.toLowerCase())) {
                                    logger.fine("Found partial action match: " + action + " for block at " + formatLocation(blockLocation));
                                    // Найдено частичное совпадение действия: для блока в
                                    // Partielle Aktionsübereinstimmung gefunden: für Block bei
                                    return action;
                                }
                            }
                        }
                        
                        // If no match found in configuration, try to determine from context
                        // Если совпадений в конфигурации не найдено, попытаться определить из контекста
                        // Wenn keine Übereinstimmung in der Konfiguration gefunden wurde, versuche es aus dem Kontext zu bestimmen
                        String determinedAction = determineActionFromContext(cleanLine, block.getType());
                        if (determinedAction != null) {
                            logger.fine("Determined action from context: " + determinedAction + " for block at " + formatLocation(blockLocation));
                            // Определено действие из контекста: для блока в
                            // Aktion aus Kontext bestimmt: für Block bei
                            return determinedAction;
                        }
                        
                        // If still no match, return the line as is
                        // Если все еще нет совпадения, вернуть строку как есть
                        // Wenn immer noch keine Übereinstimmung, gib die Zeile so zurück
                        logger.fine("Using raw sign text as action: " + cleanLine + " for block at " + formatLocation(blockLocation));
                        // Использование необработанного текста таблички как действия: для блока в
                        // Verwende rohen Schildtext als Aktion: für Block bei
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
        // Common action patterns
        // Общие шаблоны действий
        // Gemeinsame Aktionsmuster
        String lowerText = signText.toLowerCase();
        
        // Event blocks (diamond)
        // Блоки событий (алмаз)
        // Ereignisblöcke (Diamant)
        if (blockType == Material.DIAMOND_BLOCK) {
            if (lowerText.contains("join") || lowerText.contains("вход")) return "onJoin";
            if (lowerText.contains("leave") || lowerText.contains("выход")) return "onLeave";
            if (lowerText.contains("chat") || lowerText.contains("чат")) return "onChat";
            if (lowerText.contains("break") || lowerText.contains("сломать")) return "onBlockBreak";
            if (lowerText.contains("place") || lowerText.contains("поставить")) return "onBlockPlace";
        }
        
        // Action blocks (cobblestone)
        // Блоки действий (булыжник)
        // Aktionsblöcke (Bruchstein)
        if (blockType == Material.COBBLESTONE) {
            if (lowerText.contains("message") || lowerText.contains("сообщение")) return "sendMessage";
            if (lowerText.contains("teleport") || lowerText.contains("телепорт")) return "teleport";
            if (lowerText.contains("give") || lowerText.contains("выдать")) return "giveItem";
            if (lowerText.contains("sound") || lowerText.contains("звук")) return "playSound";
        }
        
        // Condition blocks (planks)
        // Блоки условий (доски)
        // Bedingungsblöcke (Bretter)
        if (blockType == Material.OAK_PLANKS) {
            if (lowerText.contains("item") || lowerText.contains("предмет")) return "hasItem";
            if (lowerText.contains("op") || lowerText.contains("оператор")) return "isOp";
            if (lowerText.contains("near") || lowerText.contains("рядом")) return "isNearBlock";
        }
        
        return null; // Could not determine action from context
        // Не удалось определить действие из контекста
        // Konnte Aktion aus Kontext nicht bestimmen
    }
    
    private void readParametersFromContainer(Location blockLocation, CodeBlock codeBlock) {
        // Look for container (chest) above the block
        // Искать контейнер (сундук) над блоком
        // Suche nach Container (Truhe) über dem Block
        Location containerLocation = blockLocation.clone().add(0, 1, 0);
        Block containerBlock = containerLocation.getBlock();
        
        // 🔧 FIX: Add null check for containerBlock
        // 🔧 ИСПРАВЛЕНИЕ: Добавить проверку на null для блока контейнера
        // 🔧 FIX: Null-Prüfung für Containerblock hinzufügen
        if (containerBlock == null) {
            return;
        }
        
        // 🔧 FIX: containerBlock is never null here, so condition is always false
        // 🔧 ИСПРАВЛЕНИЕ: containerBlock никогда не равен null здесь, поэтому условие всегда ложно
        // 🔧 FIX: containerBlock ist hier nie null, daher ist die Bedingung immer falsch
        // Removed unnecessary null check as containerBlock is never null here
        if (containerBlock.getState() instanceof Container) {
            Container container = (Container) containerBlock.getState();
            Inventory inventory = container.getInventory();
            
            // Convert ItemStacks to DataValue parameters
            // Преобразовать ItemStacks в параметры DataValue
            // ItemStacks in DataValue-Parameter konvertieren
            convertItemStacksToParameters(inventory, codeBlock);
            
            logger.fine("Found container with parameters for block at " + blockLocation);
            // Найден контейнер с параметрами для блока в
            // Container mit Parametern für Block bei gefunden
            // Found container with parameters for block at
            
            // Add visual feedback for parameter reading
            // Добавить визуальную обратную связь для чтения параметров
            // Visuelles Feedback für Parameterlesen hinzufügen
            // 🔧 FIX: Add null check for world
            // 🔧 ИСПРАВЛЕНИЕ: Добавить проверку на null для мира
            // 🔧 FIX: Null-Prüfung für Welt hinzufügen
            if (containerLocation.getWorld() != null) {
                containerLocation.getWorld().spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, 
                    containerLocation.add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 1.0);
            }
        }
    }

    /**
     * Преобразует ItemStacks из инвентаря контейнера в параметры DataValue в CodeBlock
     *
     * Converts ItemStacks from container inventory to DataValue parameters in CodeBlock
     *
     * Konvertiert ItemStacks aus Container-Inventar in DataValue-Parameter im CodeBlock
     */
    private void convertItemStacksToParameters(Inventory inventory, CodeBlock codeBlock) {
        Map<String, DataValue> newParameters = new HashMap<>();
        int processedItems = 0;
        
        // Process each slot in the inventory
        // Обработать каждый слот в инвентаре
        // Jeden Slot im Inventar verarbeiten
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            
            // Skip placeholder items
            // Пропустить элементы-заполнители
            // Platzhalterelemente überspringen
            if (isPlaceholderItem(item)) continue;
            
            // Try to determine parameter name for this slot
            // Попытаться определить имя параметра для этого слота
            // Versuche, den Parameternamen für diesen Slot zu bestimmen
            String paramName = getParameterNameForSlot(codeBlock.getAction(), slot);
            // 🔧 FIX: paramName is never null here, so condition is always false
            // 🔧 ИСПРАВЛЕНИЕ: paramName никогда не равен null здесь, поэтому условие всегда ложно
            // 🔧 FIX: paramName ist hier nie null, daher ist die Bedingung immer falsch
            // Removed unnecessary null check as paramName is never null here
            // Fallback: use generic slot-based parameter name
            // Резервный вариант: использовать общее имя параметра на основе слота
            // Fallback: Generischen Slot-basierten Parameternamen verwenden
            // paramName = "slot_" + slot;
            
            // Convert ItemStack to DataValue
            // Преобразовать ItemStack в DataValue
            // ItemStack in DataValue konvertieren
            DataValue paramValue = convertItemStackToDataValue(item);
            // 🔧 FIX: paramValue is never null here, so condition is always true
            // 🔧 ИСПРАВЛЕНИЕ: paramValue никогда не равен null здесь, поэтому условие всегда истинно
            // 🔧 FIX: paramValue ist hier nie null, daher ist die Bedingung immer wahr
            // Removed unnecessary null check as paramValue is never null here
            newParameters.put(paramName, paramValue);
            processedItems++;
        }
        
        // Update CodeBlock parameters
        // Обновить параметры CodeBlock
        // CodeBlock-Parameter aktualisieren
        for (Map.Entry<String, DataValue> entry : newParameters.entrySet()) {
            codeBlock.setParameter(entry.getKey(), entry.getValue());
        }
        
        if (processedItems > 0) {
            logger.fine("Converted " + processedItems + " ItemStacks to DataValue parameters for block " + codeBlock.getAction());
            // Преобразовано ItemStacks в параметры DataValue для блока
            // Konvertierte ItemStacks in DataValue-Parameter für Block
            // Converted ItemStacks to DataValue parameters for block
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
        
        // Clean display name from color codes for processing
        // Очистить отображаемое имя от цветовых кодов для обработки
        // Anzeigenamen von Farbcodes für die Verarbeitung bereinigen
        String cleanName = ChatColor.stripColor(displayName).trim();
        
        // 1. Try to extract value from existing parameter items (our converted items)
        // 1. Попытаться извлечь значение из существующих элементов параметров (наши преобразованные элементы)
        // 1. Versuche, Wert aus vorhandenen Parameterelementen (unsere konvertierten Elemente) zu extrahieren
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            // 🔧 FIX: Add null check for lore
            // 🔧 ИСПРАВЛЕНИЕ: Добавить проверку на null für описания
            // 🔧 FIX: Null-Prüfung für Beschreibung hinzufügen
            if (lore != null) {
                for (String line : lore) {
                    if (line.startsWith("§8Parameter: ")) {
                        // This is a parameter item we created - extract the value
                        // Это элемент параметра, который мы создали - извлечь значение
                        // Dies ist ein von uns erstelltes Parameterelement - den Wert extrahieren
                        return extractValueFromParameterItem(item, lore);
                    }
                }
            }
        }
        
        // 2. Try to detect type from material
        // 2. Попытаться определить тип по материалу
        // 2. Versuche, Typ aus Material zu erkennen
        switch (item.getType()) {
            case PAPER:
                // Extract text from display name or use item name
                // Извлечь текст из отображаемого имени или использовать имя элемента
                // Text aus Anzeigenamen extrahieren oder Elementnamen verwenden
                if (!cleanName.isEmpty()) {
                    return new TextValue(cleanName);
                } else {
                    return new TextValue("Текст");
                    // Text
                    // Text
                }
            
            case GOLD_NUGGET:
            case GOLD_INGOT:
                // Try to parse number from name or use amount
                // Попытаться разобрать число из имени или использовать количество
                // Versuche, Zahl aus Namen zu parsen oder Menge verwenden
                if (!cleanName.isEmpty()) {
                    try {
                        String numberStr = cleanName.replaceAll("[^0-9.-]", "");
                        if (!numberStr.isEmpty()) {
                            return new NumberValue(Double.parseDouble(numberStr));
                        }
                    } catch (NumberFormatException ignored) {}
                }
                return new NumberValue(item.getAmount());
            
            case LIME_DYE:
                return new BooleanValue(true);
            case RED_DYE:
                return new BooleanValue(false);
            
            case CHEST:
            case BARREL:
                // Consider these as lists or containers
                // Считать их списками или контейнерами
                // Diese als Listen oder Container betrachten
                return new ListValue(new ArrayList<>());
            
            default:
                // For other items, create text value from name or material
                // Для других элементов создать текстовое значение из имени или материала
                // Für andere Elemente Textwert aus Name oder Material erstellen
                if (!cleanName.isEmpty()) {
                    return new TextValue(cleanName);
                } else {
                    // Use material name as text value
                    // Использовать имя материала как текстовое значение
                    // Materialname als Textwert verwenden
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
        // Look for "Value: " line in lore
        // Искать строку "Value: " в описании
        // Suche nach "Value: "-Zeile in der Beschreibung
        // 🔧 FIX: Add null check for lore
        // 🔧 ИСПРАВЛЕНИЕ: Добавить проверку на null für описания
        // 🔧 FIX: Null-Prüfung für Beschreibung hinzufügen
        if (lore == null) {
            return new TextValue(item.getType().name().toLowerCase());
        }
        
        for (String line : lore) {
            String cleanLine = ChatColor.stripColor(line);
            if (cleanLine.startsWith("Value: ")) {
                String valueStr = cleanLine.substring(7); // Remove "Value: "
                // Удалить "Value: "
                // "Value: " entfernen
                
                // Check type from the previous line
                // Проверить тип по предыдущей строке
                // Typ aus der vorherigen Zeile prüfen
                int index = lore.indexOf(line);
                // 🔧 FIX: Add bounds check for index
                // 🔧 ИСПРАВЛЕНИЕ: Добавить проверку границ для индекса
                // 🔧 FIX: Grenzprüfung für Index hinzufügen
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
                
                // Default to text
                // По умолчанию текст
                // Standardmäßig Text
                return new TextValue(valueStr);
            }
        }
        
        // Fallback
        // Резервный вариант
        // Fallback
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
        // Action-specific parameter mapping based on coding_blocks.yml
        // Сопоставление параметров в зависимости от действия на основе coding_blocks.yml
        // Aktionspezifische Parameterzuordnung basierend auf coding_blocks.yml
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
            // Условия переменных (единая обработка)
            // Variablenbedingungen (vereinheitlichte Behandlung)
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
                    // Устаревшее имя параметра для обратной совместимости
                    // Legacy-Parametername für Abwärtskompatibilität
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
            // Общий резервный вариант
            // Generischer Fallback
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
     * Проверяет, является ли ItemStack элементом-заполнителем
     *
     * Checks if an ItemStack is a placeholder item
     *
     * Prüft, ob ein ItemStack ein Platzhalterelement ist
     */
    private boolean isPlaceholderItem(ItemStack item) {
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
        
        // Scan the world for code structures
        // Сканировать мир на наличие кодовых структур
        // Scan die Welt nach Code-Strukturen
        Map<Location, CodeBlock> scannedBlocks = scanWorldStructure(world);
        
        // Group blocks by Y level (line) for structured compilation
        // Группировать блоки по уровню Y (линии) для структурированной компиляции
        // Blöcke nach Y-Ebene (Zeile) für strukturierte Kompilierung gruppieren
        Map<Integer, List<CodeBlock>> blocksByLine = new HashMap<>();
        
        for (Map.Entry<Location, CodeBlock> entry : scannedBlocks.entrySet()) {
            Location location = entry.getKey();
            CodeBlock block = entry.getValue();
            
            int yLevel = location.getBlockY();
            blocksByLine.computeIfAbsent(yLevel, k -> new ArrayList<>()).add(block);
        }
        
        // Process each line
        // Обработать каждую линию
        // Jede Zeile verarbeiten
        for (Map.Entry<Integer, List<CodeBlock>> lineEntry : blocksByLine.entrySet()) {
            int yLevel = lineEntry.getKey();
            List<CodeBlock> lineBlocks = lineEntry.getValue();
            
            // Sort blocks by X coordinate (left to right)
            // Сортировать блоки по координате X (слева направо)
            // Blöcke nach X-Koordinate sortieren (von links nach rechts)
            lineBlocks.sort((a, b) -> {
                Location locA = null;
                Location locB = null;
                
                // Find locations for these blocks
                // Найти расположения для этих блоков
                // Positionen für diese Blöcke finden
                for (Map.Entry<Location, CodeBlock> blockEntry : scannedBlocks.entrySet()) {
                    if (blockEntry.getValue() == a) locA = blockEntry.getKey();
                    if (blockEntry.getValue() == b) locB = blockEntry.getKey();
                }
                
                if (locA != null && locB != null) {
                    return Integer.compare(locA.getBlockX(), locB.getBlockX());
                }
                return 0;
            });
            
            // Convert line to code string
            // Преобразовать линию в строку кода
            // Zeile in Code-Zeichenfolge konvertieren
            List<String> lineCode = new ArrayList<>();
            for (CodeBlock block : lineBlocks) {
                String function = getFunctionFromBlock(block);
                if (function != null && !function.isEmpty()) {
                    lineCode.add(function);
                }
            }
            
            if (!lineCode.isEmpty()) {
                // Join functions with "&" separator like reference system
                // Объединить функции с разделителем "&" как в эталонной системе
                // Funktionen mit "&"-Trennzeichen wie im Referenzsystem verbinden
                String lineResult = String.join("&", lineCode);
                compiledCode.add(lineResult);
            }
        }
        
        logger.info("Compiled " + compiledCode.size() + " lines of code from world: " + world.getName());
        // Скомпилировано строк кода из мира:
        // Kompilierte Codezeilen aus Welt:
        // Compiled lines of code from world:
        
        // 🎆 ENHANCED: Save compiled code to WorldCode like FrameLand
        // 🎆 УЛУЧШЕНО: Сохранить скомпилированный код в WorldCode как FrameLand
        // 🎆 VERBESSERT: Kompilierten Code in WorldCode wie FrameLand speichern
        String worldId = world.getName().replace("-code", "");
        if (!compiledCode.isEmpty()) {
            WorldCode.setCode(worldId, compiledCode);
            logger.info("Saved " + compiledCode.size() + " lines of compiled code for world: " + worldId);
            // Сохранено строк скомпилированного кода для мира:
            // Gespeicherte Codezeilen für Welt:
            // Saved lines of compiled code for world:
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
    private String getFunctionFromBlock(CodeBlock block) {
        if (block == null) return null;
        
        String action = block.getAction();
        // 🔧 FIX: Handle empty or unset actions properly
        // 🔧 ИСПРАВЛЕНИЕ: Правильно обрабатывать пустые или неустановленные действия
        // 🔧 FIX: Leere oder nicht gesetzte Aktionen richtig behandeln
        if (action == null || action.equals("NOT_SET") || action.isEmpty()) {
            // For bracket blocks, we still want to include them even if action is not set
            // Для блоков скобок мы все равно хотим включить их, даже если действие не установлено
            // Für Klammerblöcke möchten wir sie trotzdem einbeziehen, auch wenn die Aktion nicht gesetzt ist
            if (block.getMaterial() == Material.PISTON || block.getMaterial() == Material.STICKY_PISTON) {
                if (block.getBracketType() == CodeBlock.BracketType.OPEN) {
                    return "{";
                } else if (block.getBracketType() == CodeBlock.BracketType.CLOSE) {
                    return "}";
                }
            }
            return null;
        }
        
        // Handle special cases like brackets
        // Обрабатывать специальные случаи, такие как скобки
        // Spezialfälle wie Klammern behandeln
        if (block.getMaterial() == Material.PISTON || block.getMaterial() == Material.STICKY_PISTON) {
            if (block.getBracketType() == CodeBlock.BracketType.OPEN) {
                return "{";
            } else if (block.getBracketType() == CodeBlock.BracketType.CLOSE) {
                return "}";
            }
            return null;
        }
        
        // Handle event blocks (diamond)
        // Обрабатывать блоки событий (алмаз)
        // Ereignisblöcke behandeln (Diamant)
        if (block.getMaterial() == Material.DIAMOND_BLOCK) {
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
        
        // Handle action blocks (cobblestone)
        // Обрабатывать блоки действий (булыжник)
        // Aktionsblöcke behandeln (Bruchstein)
        if (block.getMaterial() == Material.COBBLESTONE) {
            return action;
        }
        
        // Handle condition blocks (planks)
        // Обрабатывать блоки условий (доски)
        // Bedingungsblöcke behandeln (Bretter)
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
     * Speichert kompилиerten Code in einer Konfigurationsdatei wie im WorldCode-System des Referenzsystems
     */
    public void saveCompiledCode(String worldId, List<String> codeLines) {
        // Save to WorldCode configuration like reference system's WorldCode system
        // Сохранить в конфигурацию WorldCode как в системе WorldCode эталонной системы
        // In WorldCode-Konfiguration wie im WorldCode-System des Referenzsystems speichern
        logger.info("Saving compiled code for world: " + worldId);
        // Сохранение скомпилированного кода для мира:
        // Speichern von kompiliertem Code für Welt:
        // Saving compiled code for world:
        logger.info("Code lines: " + codeLines.size());
        // Строки кода:
        // Codezeilen:
        // Code lines:
        
        // Import and use WorldCode system
        // Импортировать и использовать систему WorldCode
        // WorldCode-System importieren und verwenden
        com.megacreative.configs.WorldCode.setCode(worldId, codeLines);
        
        logger.info("Successfully saved compiled code to WorldCode configuration");
        // Успешно сохранен скомпилированный код в конфигурацию WorldCode
        // Erfolgreich kompilierter Code in WorldCode-Konfiguration gespeichert
        // Successfully saved compiled code to WorldCode configuration
    }
}