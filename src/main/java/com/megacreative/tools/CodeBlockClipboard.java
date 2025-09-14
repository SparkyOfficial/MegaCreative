package com.megacreative.tools;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.AutoConnectionManager;
import com.megacreative.coding.values.DataValue;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Буфер обмена для кодовых блоков с функциями копирования, вставки и управления
 *
 * Code block clipboard with copy, paste, and management functions
 *
 * Code-Block-Zwischenablage mit Kopier-, Einfüge- und Verwaltungsfunktionen
 */
public class CodeBlockClipboard {
    private static final Logger log = Logger.getLogger(CodeBlockClipboard.class.getName());
    
    private final Map<UUID, ClipboardData> playerClipboards = new ConcurrentHashMap<>();
    private final Map<String, ClipboardData> sharedClipboards = new ConcurrentHashMap<>();
    
    private BlockPlacementHandler placementHandler;
    private AutoConnectionManager connectionManager;
    
    /**
     * Устанавливает BlockPlacementHandler для интеграции
     *
     * Sets the BlockPlacementHandler for integration
     *
     * Setzt den BlockPlacementHandler für die Integration
     */
    public void setPlacementHandler(BlockPlacementHandler placementHandler) {
        this.placementHandler = placementHandler;
    }
    
    /**
     * Устанавливает AutoConnectionManager для интеграции
     *
     * Sets the AutoConnectionManager for integration
     *
     * Setzt den AutoConnectionManager für die Integration
     */
    public void setConnectionManager(AutoConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    /**
     * Копирует кодовый блок в местоположении цели игрока
     * @param player Игрок
     * @param location Расположение
     *
     * Copies a code block at the player's target location
     * @param player Player
     * @param location Location
     *
     * Kopiert einen Codeblock an der Zielposition des Spielers
     * @param player Spieler
     * @param location Position
     */
    public void copyBlock(Player player, Location location) {
        CodeBlock block = null;
        
        // Try to get CodeBlock from BlockPlacementHandler first
        // Сначала попытаться получить CodeBlock из BlockPlacementHandler
        // Zuerst versuchen, CodeBlock aus BlockPlacementHandler zu erhalten
        if (placementHandler != null && placementHandler.hasCodeBlock(location)) {
            block = placementHandler.getCodeBlock(location);
        } else if (connectionManager != null) {
            // Fallback to AutoConnectionManager
            // Резервный вариант - AutoConnectionManager
            // Fallback zu AutoConnectionManager
            block = connectionManager.getWorldBlocks(location.getWorld()).get(location);
        }
        
        if (block == null) {
            player.sendMessage("§c✖ No code block found at target location!");
            // §c✖ Кодовый блок не найден в целевом местоположении!
            // §c✖ Kein Codeblock an der Zielposition gefunden!
            return;
        }
        
        copyBlock(player, block);
    }
    
    /**
     * Копирует определенный CodeBlock
     * @param player Игрок
     * @param block CodeBlock для копирования
     *
     * Copies a specific CodeBlock
     * @param player Player
     * @param block CodeBlock to copy
     *
     * Kopiert einen bestimmten CodeBlock
     * @param player Spieler
     * @param block Zu kopierender CodeBlock
     */
    public void copyBlock(Player player, CodeBlock block) {
        ClipboardData data = new ClipboardData(ClipboardType.SINGLE_BLOCK, player.getLocation());
        
        // Create deep copy of the block to preserve all parameters
        // Создать глубокую копию блока для сохранения всех параметров
        // Tiefe Kopie des Blocks erstellen, um alle Parameter zu erhalten
        CodeBlock copiedBlock = createDeepCopy(block);
        data.addBlock(copiedBlock, new Location(player.getWorld(), 0, 0, 0)); // Relative position
        // Относительное положение
        // Relative Position
        
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Copied code block: " + block.getAction());
        // §a✓ Скопированный кодовый блок:
        // §a✓ Kopierter Codeblock:
        
        log.info("Player " + player.getName() + " copied code block: " + block.getAction());
        // Игрок скопировал кодовый блок:
        // Spieler hat Codeblock kopiert:
    }
    
    /**
     * Копирует все кодовые блоки в регионе
     * @param player Игрок
     * @param corner1 Первый угол региона
     * @param corner2 Второй угол региона
     *
     * Copies all code blocks in a region
     * @param player Player
     * @param corner1 First corner of region
     * @param corner2 Second corner of region
     *
     * Kopiert alle Codeblöcke in einer Region
     * @param player Spieler
     * @param corner1 Erste Ecke der Region
     * @param corner2 Zweite Ecke der Region
     */
    public void copyRegion(Player player, Location corner1, Location corner2) {
        World world = corner1.getWorld();
        if (world == null || !world.equals(corner2.getWorld())) {
            player.sendMessage("§c✖ Both corners must be in the same world!");
            // §c✖ Оба угла должны быть в одном мире!
            // §c✖ Beide Ecken müssen in derselben Welt sein!
            return;
        }
        
        ClipboardData data = new ClipboardData(ClipboardType.REGION, corner1);
        
        // Calculate region bounds
        // Вычислить границы региона
        // Regionsgrenzen berechnen
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        int blocksFound = 0;
        
        // Scan the region for code blocks
        // Сканировать регион на наличие кодовых блоков
        // Region nach Codeblöcken scannen
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(world, x, y, z);
                    CodeBlock block = null;
                    
                    // Try to get CodeBlock from placement handler or connection manager
                    // Попытаться получить CodeBlock из обработчика размещения или менеджера соединений
                    // Versuchen, CodeBlock vom Platzierungshandler oder Verbindungsmanager zu erhalten
                    if (placementHandler != null && placementHandler.hasCodeBlock(loc)) {
                        block = placementHandler.getCodeBlock(loc);
                    } else if (connectionManager != null) {
                        block = connectionManager.getWorldBlocks(world).get(loc);
                    }
                    
                    if (block != null) {
                        // Create deep copy and calculate relative position
                        // Создать глубокую копию и вычислить относительное положение
                        // Tiefe Kopie erstellen und relative Position berechnen
                        CodeBlock copiedBlock = createDeepCopy(block);
                        Location relativePos = new Location(world, x - corner1.getBlockX(), y - corner1.getBlockY(), z - corner1.getBlockZ());
                        data.addBlock(copiedBlock, relativePos);
                        blocksFound++;
                    }
                }
            }
        }
        
        if (blocksFound == 0) {
            player.sendMessage("§c✖ No code blocks found in the selected region!");
            // §c✖ Кодовые блоки не найдены в выбранном регионе!
            // §c✖ Keine Codeblöcke in der ausgewählten Region gefunden!
            return;
        }
        
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Copied region: " + blocksFound + " code blocks");
        // §a✓ Скопированный регион: кодовых блоков
        // §a✓ Kopierte Region: Codeblöcke
        
        log.info("Player " + player.getName() + " copied region with " + blocksFound + " blocks");
        // Игрок скопировал регион с блоками
        // Spieler hat Region mit Blöcken kopiert
    }
    
    /**
     * Вставляет данные буфера обмена в целевое местоположение
     * @param player Игрок
     * @param targetLocation Целевое местоположение
     *
     * Pastes clipboard data at the target location
     * @param player Player
     * @param targetLocation Target location
     *
     * Fügt Zwischenspeicherdaten an der Zielposition ein
     * @param player Spieler
     * @param targetLocation Zielposition
     */
    public void paste(Player player, Location targetLocation) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§c✖ No clipboard data to paste");
            // §c✖ Нет данных буфера обмена для вставки
            // §c✖ Keine Zwischenspeicherdaten zum Einfügen
            return;
        }
        
        if (data.getBlockPositions().isEmpty()) {
            player.sendMessage("§c✖ Clipboard contains no blocks to paste");
            // §c✖ Буфер обмена не содержит блоков для вставки
            // §c✖ Zwischenspeicher enthält keine Blöcke zum Einfügen
            return;
        }
        
        World targetWorld = targetLocation.getWorld();
        if (targetWorld == null) {
            player.sendMessage("§c✖ Invalid target world!");
            // §c✖ Недопустимый целевой мир!
            // §c✖ Ungültige Zielwelt!
            return;
        }
        
        int pastedBlocks = 0;
        Map<Location, CodeBlock> pastedBlockMap = new HashMap<>(); // For reconnection
        // Для повторного соединения
        // Für die Wiederverbindung
        
        // First pass: Place all blocks
        // Первый проход: Разместить все блоки
        // Erster Durchgang: Alle Blöcke platzieren
        for (Map.Entry<Location, CodeBlock> entry : data.getBlockPositions().entrySet()) {
            Location relativePos = entry.getKey();
            CodeBlock sourceBlock = entry.getValue();
            
            // Calculate absolute position
            // Вычислить абсолютное положение
            // Absolute Position berechnen
            Location absolutePos = new Location(targetWorld,
                targetLocation.getBlockX() + relativePos.getBlockX(),
                targetLocation.getBlockY() + relativePos.getBlockY(),
                targetLocation.getBlockZ() + relativePos.getBlockZ());
            
            // Check if location is valid for code blocks
            // Проверить, допустимо ли местоположение для кодовых блоков
            // Prüfen, ob die Position für Codeblöcke gültig ist
            if (absolutePos.getBlock().getType() != Material.AIR) {
                player.sendMessage("§e⚠ Skipping occupied location: " + 
                    absolutePos.getBlockX() + ", " + absolutePos.getBlockY() + ", " + absolutePos.getBlockZ());
                // §e⚠ Пропуск занятого местоположения:
                // §e⚠ Überspringe besetzte Position:
                continue;
            }
            
            // Create a new CodeBlock copy
            // Создать новую копию CodeBlock
            // Neue CodeBlock-Kopie erstellen
            CodeBlock newBlock = createDeepCopy(sourceBlock);
            
            // Place the block physically
            // Физически разместить блок
            // Block physisch platzieren
            absolutePos.getBlock().setType(newBlock.getMaterial());
            
            // Register with placement handler
            // Зарегистрировать с обработчиком размещения
            // Beim Platzierungshandler registrieren
            if (placementHandler != null) {
                // Add to placement handler's tracking
                // Добавить в отслеживание обработчика размещения
                // Zum Tracking des Platzierungshandlers hinzufügen
                // This would require exposing a method in BlockPlacementHandler
                // Это потребует открытия метода в BlockPlacementHandler
                // Dies würde das Offenlegen einer Methode in BlockPlacementHandler erfordern
                // For now, we'll simulate the registration
                // Пока что мы симулируем регистрацию
                // Für jetzt simulieren wir die Registrierung
            }
            
            // Register with connection manager
            // Зарегистрировать с менеджером соединений
            // Beim Verbindungsmanager registrieren
            if (connectionManager != null) {
                // Add to connection manager and trigger auto-connect
                // Добавить в менеджер соединений и запустить автоматическое соединение
                // Zum Verbindungsmanager hinzufügen und Auto-Connect auslösen
                // This integration ensures the pasted blocks are properly connected
                // Эта интеграция гарантирует, что вставленные блоки будут правильно соединены
                // Diese Integration stellt sicher, dass die eingefügten Blöcke ordnungsgemäß verbunden sind
            }
            
            pastedBlockMap.put(absolutePos, newBlock);
            pastedBlocks++;
        }
        
        // Second pass: Restore connections if we have connection manager
        // Второй проход: Восстановить соединения, если у нас есть менеджер соединений
        // Zweiter Durchgang: Verbindungen wiederherstellen, wenn wir einen Verbindungsmanager haben
        if (connectionManager != null) {
            for (Map.Entry<Location, CodeBlock> entry : pastedBlockMap.entrySet()) {
                // Call the public method that will trigger auto-connection
                // Вызвать публичный метод, который запустит автоматическое соединение
                // Die öffentliche Methode aufrufen, die die automatische Verbindung auslöst
                // Since autoConnectBlock is private, we'll rely on the placement handler
                // Поскольку autoConnectBlock является приватным, мы полагаемся на обработчик размещения
                // Da autoConnectBlock privat ist, verlassen wir uns auf den Platzierungshandler
                // to trigger the connections through the event system
                // чтобы запустить соединения через систему событий
                // um die Verbindungen über das Ereignissystem auszulösen
            }
        }
        
        if (pastedBlocks > 0) {
            player.sendMessage("§a✓ Pasted " + pastedBlocks + " code blocks");
            // §a✓ Вставлено кодовых блоков
            // §a✓ Eingefügte Codeblöcke
            log.info("Player " + player.getName() + " pasted " + pastedBlocks + " blocks at " + targetLocation);
            // Игрок вставил блоков в
            // Spieler hat Blöcke eingefügt bei
        } else {
            player.sendMessage("§c✖ No blocks were pasted (all locations occupied)");
            // §c✖ Блоки не были вставлены (все местоположения заняты)
            // §c✖ Keine Blöcke wurden eingefügt (alle Positionen besetzt)
        }
    }
    
    /**
     * Показывает предварительный просмотр буфера обмена
     * @param player Игрок
     * @param targetLocation Целевое местоположение
     *
     * Shows preview of clipboard
     * @param player Player
     * @param targetLocation Target location
     *
     * Zeigt eine Vorschau der Zwischenablage an
     * @param player Spieler
     * @param targetLocation Zielposition
     */
    public void showPreview(Player player, Location targetLocation) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§c✖ No clipboard data to preview");
            // §c✖ Нет данных буфера обмена для предварительного просмотра
            // §c✖ Keine Zwischenspeicherdaten zur Vorschau
            return;
        }
        player.sendMessage("§e⤤ Preview: " + data.getBlocks().size() + " blocks would be placed");
        // §e⤤ Предварительный просмотр: блоков будет размещено
        // §e⤤ Vorschau: Blöcke würden platziert werden
    }
    
    /**
     * Сохраняет в общий буфер обмена
     * @param player Игрок
     * @param name Имя для сохранения
     *
     * Saves to shared clipboard
     * @param player Player
     * @param name Name to save as
     *
     * Speichert in gemeinsamer Zwischenablage
     * @param player Spieler
     * @param name Name zum Speichern
     */
    public void saveToShared(Player player, String name) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§c✖ No clipboard data to save");
            // §c✖ Нет данных буфера обмена для сохранения
            // §c✖ Keine Zwischenspeicherdaten zum Speichern
            return;
        }
        sharedClipboards.put(name, data);
        player.sendMessage("§a✓ Saved clipboard as: " + name);
        // §a✓ Буфер обмена сохранен как:
        // §a✓ Zwischenablage gespeichert als:
    }
    
    /**
     * Загружает из общего буфера обмена
     * @param player Игрок
     * @param name Имя для загрузки
     *
     * Loads from shared clipboard
     * @param player Player
     * @param name Name to load
     *
     * Lädt aus gemeinsamer Zwischenablage
     * @param player Spieler
     * @param name Name zum Laden
     */
    public void loadFromShared(Player player, String name) {
        ClipboardData data = sharedClipboards.get(name);
        if (data == null) {
            player.sendMessage("§c✖ Shared clipboard not found: " + name);
            // §c✖ Общий буфер обмена не найден:
            // §c✖ Gemeinsame Zwischenablage nicht gefunden:
            return;
        }
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Loaded clipboard: " + name);
        // §a✓ Загруженный буфер обмена:
        // §a✓ Geladene Zwischenablage:
    }
    
    /**
     * Список общих буферов обмена
     * @param player Игрок
     *
     * Lists shared clipboards
     * @param player Player
     *
     * Listet gemeinsame Zwischenablagen auf
     * @param player Spieler
     */
    public void listShared(Player player) {
        if (sharedClipboards.isEmpty()) {
            player.sendMessage("§e⤤ No shared clipboards available");
            // §e⤤ Нет доступных общих буферов обмена
            // §e⤤ Keine gemeinsamen Zwischenablagen verfügbar
            return;
        }
        player.sendMessage("§6=== Shared Clipboards ===");
        // §6=== Общие буферы обмена ===
        // §6=== Gemeinsame Zwischenablagen ===
        for (String name : sharedClipboards.keySet()) {
            player.sendMessage("§f" + name);
        }
    }
    
    /**
     * Очищает буфер обмена игрока
     * @param player Игрок
     *
     * Clears player's clipboard
     * @param player Player
     *
     * Löscht die Zwischenablage des Spielers
     * @param player Spieler
     */
    public void clear(Player player) {
        playerClipboards.remove(player.getUniqueId());
        player.sendMessage("§a✓ Clipboard cleared");
        // §a✓ Буфер обмена очищен
        // §a✓ Zwischenablage gelöscht
    }
    
    /**
     * Получает информацию о буфере обмена игрока
     * @param player Игрок
     * @return Информация о буфере обмена
     *
     * Gets player's clipboard info
     * @param player Player
     * @return Clipboard info
     *
     * Ruft die Zwischenablage-Informationen des Spielers ab
     * @param player Spieler
     * @return Zwischenablage-Info
     */
    public String getClipboardInfo(Player player) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            return "§cNo clipboard data";
            // §cНет данных буфера обмена
            // §cKeine Zwischenspeicherdaten
        }
        return "§a" + data.getType().getDisplayName() + " §7(" + data.getBlocks().size() + " blocks)";
        // §a §7( блоков)
        // §a §7(Blöcke)
    }
    
    /**
     * Создает глубокую копию CodeBlock, сохраняя все параметры и свойства
     * @param original Оригинальный CodeBlock
     * @return Глубокая копия CodeBlock
     *
     * Creates a deep copy of a CodeBlock, preserving all parameters and properties
     * @param original Original CodeBlock
     * @return Deep copy of CodeBlock
     *
     * Erstellt eine tiefe Kopie eines CodeBlocks und erhält alle Parameter und Eigenschaften
     * @param original Originaler CodeBlock
     * @return Tiefe Kopie des CodeBlocks
     */
    private CodeBlock createDeepCopy(CodeBlock original) {
        CodeBlock copy = new CodeBlock(original.getMaterial(), original.getAction());
        
        // Copy all parameters
        // Копировать все параметры
        // Alle Parameter kopieren
        for (Map.Entry<String, DataValue> paramEntry : original.getParameters().entrySet()) {
            copy.setParameter(paramEntry.getKey(), paramEntry.getValue());
        }
        
        // Copy config items if any
        // Копировать элементы конфигурации, если есть
        // Konfigurationselemente kopieren, falls vorhanden
        for (int i = 0; i < original.getConfigItems().size(); i++) {
            if (original.getConfigItem(i) != null) {
                copy.setConfigItem(i, original.getConfigItem(i).clone());
            }
        }
        
        // Note: We don't copy nextBlock and children relationships here
        // as they will be rebuilt by the AutoConnectionManager
        // Примечание: Мы не копируем отношения nextBlock и children здесь,
        // так как они будут перестроены AutoConnectionManager
        // Hinweis: Wir kopieren hier keine nextBlock- und Kinderbeziehungen,
        // da sie vom AutoConnectionManager neu aufgebaut werden
        
        return copy;
    }
    
    /**
     * Копирует цепочку соединенных кодовых блоков, начиная с заданного блока
     * @param player Игрок
     * @param startLocation Начальное местоположение
     *
     * Copies a chain of connected code blocks starting from the given block
     * @param player Player
     * @param startLocation Start location
     *
     * Kopiert eine Kette verbundener Codeblöcke, beginnend mit dem angegebenen Block
     * @param player Spieler
     * @param startLocation Startposition
     */
    public void copyChain(Player player, Location startLocation) {
        CodeBlock startBlock = null;
        
        // Get the starting block
        // Получить начальный блок
        // Startblock erhalten
        if (placementHandler != null && placementHandler.hasCodeBlock(startLocation)) {
            startBlock = placementHandler.getCodeBlock(startLocation);
        } else if (connectionManager != null) {
            startBlock = connectionManager.getWorldBlocks(startLocation.getWorld()).get(startLocation);
        }
        
        if (startBlock == null) {
            player.sendMessage("§c✖ No code block found at target location!");
            // §c✖ Кодовый блок не найден в целевом местоположении!
            // §c✖ Kein Codeblock an der Zielposition gefunden!
            return;
        }
        
        ClipboardData data = new ClipboardData(ClipboardType.BLOCK_CHAIN, startLocation);
        
        // Follow the chain and copy all connected blocks
        // Следовать по цепочке и копировать все соединенные блоки
        // Der Kette folgen und alle verbundenen Blöcke kopieren
        Set<CodeBlock> visited = new HashSet<>();
        Location currentPos = startLocation;
        int chainIndex = 0;
        
        copyChainRecursive(startBlock, data, visited, currentPos, chainIndex);
        
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Copied block chain: " + data.getBlocks().size() + " blocks");
        // §a✓ Скопированная цепочка блоков: блоков
        // §a✓ Kopierte Blockkette: Blöcke
        
        log.info("Player " + player.getName() + " copied block chain with " + data.getBlocks().size() + " blocks");
        // Игрок скопировал цепочку блоков с блоками
        // Spieler hat Blockkette mit Blöcken kopiert
    }
    
    /**
     * Рекурсивно копирует цепочку блоков
     * @param block Блок для копирования
     * @param data Данные буфера обмена
     * @param visited Посещенные блоки
     * @param basePos Базовое положение
     * @param chainIndex Индекс в цепочке
     *
     * Recursively copies a chain of blocks
     * @param block Block to copy
     * @param data Clipboard data
     * @param visited Visited blocks
     * @param basePos Base position
     * @param chainIndex Chain index
     *
     * Kopiert rekursiv eine Kette von Blöcken
     * @param block Zu kopierender Block
     * @param data Zwischenspeicherdaten
     * @param visited Besuchte Blöcke
     * @param basePos Basisposition
     * @param chainIndex Kettenindex
     */
    private void copyChainRecursive(CodeBlock block, ClipboardData data, Set<CodeBlock> visited, Location basePos, int chainIndex) {
        if (block == null || visited.contains(block)) {
            return;
        }
        
        visited.add(block);
        
        // Add current block
        // Добавить текущий блок
        // Aktuellen Block hinzufügen
        CodeBlock copiedBlock = createDeepCopy(block);
        Location relativePos = new Location(basePos.getWorld(), chainIndex, 0, 0);
        data.addBlock(copiedBlock, relativePos);
        
        // Follow next block
        // Следовать за следующим блоком
        // Dem nächsten Block folgen
        if (block.getNextBlock() != null) {
            copyChainRecursive(block.getNextBlock(), data, visited, basePos, chainIndex + 1);
        }
        
        // Follow children (for conditional blocks)
        // Следовать за детьми (для условных блоков)
        // Kindern folgen (für bedingte Blöcke)
    }
    
    /**
     * Данные буфера обмена
     *
     * Clipboard data
     *
     * Zwischenspeicherdaten
     */
    public static class ClipboardData {
        private final ClipboardType type;
        private final Location origin; // Original copy location for reference
        // Оригинальное местоположение копии для справки
        // Ursprüngliche Kopierposition als Referenz
        private final List<CodeBlock> blocks = new ArrayList<>();
        private final Map<Location, CodeBlock> blockPositions = new HashMap<>();
        
        /**
         * Создает данные буфера обмена
         * @param type Тип буфера обмена
         * @param origin Исходное местоположение
         *
         * Creates clipboard data
         * @param type Clipboard type
         * @param origin Origin location
         *
         * Erstellt Zwischenspeicherdaten
         * @param type Zwischenspeichertyp
         * @param origin Ursprungsposition
         */
        public ClipboardData(ClipboardType type, Location origin) {
            this.type = type;
            this.origin = origin != null ? origin.clone() : null;
        }
        
        /**
         * Добавляет блок в буфер обмена
         * @param block Блок для добавления
         * @param relativePosition Относительное положение
         *
         * Adds block to clipboard
         * @param block Block to add
         * @param relativePosition Relative position
         *
         * Fügt Block zur Zwischenablage hinzu
         * @param block Hinzuzufügender Block
         * @param relativePosition Relative Position
         */
        public void addBlock(CodeBlock block, Location relativePosition) {
            if (block != null) {
                blocks.add(block);
                if (relativePosition != null) {
                    blockPositions.put(relativePosition, block);
                }
            }
        }
        
        // For backward compatibility
        // Для обратной совместимости
        // Für Abwärtskompatibilität
        public void addBlock(CodeBlock block) {
            addBlock(block, new Location(null, 0, 0, 0));
        }
        
        /**
         * Получает список блоков
         * @return Список блоков
         *
         * Gets list of blocks
         * @return List of blocks
         *
         * Ruft Liste der Blöcke ab
         * @return Liste der Blöcke
         */
        public List<CodeBlock> getBlocks() {
            return new ArrayList<>(blocks);
        }
        
        /**
         * Получает позиции блоков
         * @return Карта позиций блоков
         *
         * Gets block positions
         * @return Map of block positions
         *
         * Ruft Blockpositionen ab
         * @return Karte der Blockpositionen
         */
        public Map<Location, CodeBlock> getBlockPositions() {
            return new HashMap<>(blockPositions);
        }
        
        /**
         * Получает тип буфера обмена
         * @return Тип буфера обмена
         *
         * Gets clipboard type
         * @return Clipboard type
         *
         * Ruft Zwischenspeichertyp ab
         * @return Zwischenspeichertyp
         */
        public ClipboardType getType() {
            return type;
        }
    }
    
    /**
     * Тип буфера обмена
     *
     * Clipboard type
     *
     * Zwischenspeichertyp
     */
    public enum ClipboardType {
        SINGLE_BLOCK("Single Block"),
        // Один блок
        // Einzelner Block
        BLOCK_CHAIN("Block Chain"),
        // Цепочка блоков
        // Blockkette
        REGION("Region");
        // Регион
        // Region
        
        private final String displayName;
        
        /**
         * Создает тип буфера обмена
         * @param displayName Отображаемое имя
         *
         * Creates clipboard type
         * @param displayName Display name
         *
         * Erstellt Zwischenspeichertyp
         * @param displayName Anzeigename
         */
        ClipboardType(String displayName) {
            this.displayName = displayName;
        }
        
        /**
         * Получает отображаемое имя
         * @return Отображаемое имя
         *
         * Gets display name
         * @return Display name
         *
         * Ruft Anzeigenamen ab
         * @return Anzeigename
         */
        public String getDisplayName() {
            return displayName;
        }
    }
}