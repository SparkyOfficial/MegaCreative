package com.megacreative.tools;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.BlockPlacementHandler;
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
                    
                    // Try to get CodeBlock from placement handler
                    // Попытаться получить CodeBlock из обработчика размещения
                    // Versuchen, CodeBlock vom Platzierungshandler zu erhalten
                    if (placementHandler != null && placementHandler.hasCodeBlock(loc)) {
                        block = placementHandler.getCodeBlock(loc);
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
        player.sendMessage("§a✓ Copied " + blocksFound + " code blocks from region");
        // §a✓ Скопировано кодовых блоков из региона:
        // §a✓ Kopierte Codeblöcke aus der Region:
        
        log.info("Player " + player.getName() + " copied " + blocksFound + " code blocks from region");
        // Игрок скопировал кодовых блоков из региона:
        // Spieler hat Codeblöcke aus der Region kopiert:
    }
    
    /**
     * Вставляет скопированные блоки в местоположении игрока
     * @param player Игрок
     *
     * Pastes copied blocks at the player's location
     * @param player Player
     *
     * Fügt kopierte Blöcke an der Position des Spielers ein
     * @param player Spieler
     */
    public void pasteBlocks(Player player) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§c✖ No blocks in clipboard! Copy some blocks first.");
            // §c✖ Нет блоков в буфере обмена! Сначала скопируйте несколько блоков.
            // §c✖ Keine Blöcke in der Zwischenablage! Kopiere zuerst einige Blöcke.
            return;
        }
        
        Location pasteLocation = player.getLocation();
        World world = pasteLocation.getWorld();
        
        if (world == null) {
            player.sendMessage("§c✖ Invalid world!");
            // §c✖ Недействительный мир!
            // §c✖ Ungültige Welt!
            return;
        }
        
        int blocksPasted = 0;
        
        // Paste each block in the clipboard
        // Вставить каждый блок из буфера обмена
        // Jeden Block aus der Zwischenablage einfügen
        for (Map.Entry<Location, CodeBlock> entry : data.getBlocks().entrySet()) {
            Location relativePos = entry.getKey();
            CodeBlock originalBlock = entry.getValue();
            
            // Calculate absolute position
            // Вычислить абсолютное положение
            // Absolute Position berechnen
            Location absolutePos = new Location(
                world,
                pasteLocation.getBlockX() + relativePos.getBlockX(),
                pasteLocation.getBlockY() + relativePos.getBlockY(),
                pasteLocation.getBlockZ() + relativePos.getBlockZ()
            );
            
            // Create new block and copy all properties
            // Создать новый блок и скопировать все свойства
            // Neuen Block erstellen und alle Eigenschaften kopieren
            CodeBlock newBlock = createDeepCopy(originalBlock);
            
            // Place the block using BlockPlacementHandler
            // Разместить блок с помощью BlockPlacementHandler
            // Block mit BlockPlacementHandler platzieren
            if (placementHandler != null) {
                placementHandler.addCodeBlock(absolutePos, newBlock);
                blocksPasted++;
            }
        }
        
        player.sendMessage("§a✓ Pasted " + blocksPasted + " code blocks");
        // §a✓ Вставлено кодовых блоков:
        // §a✓ Eingefügte Codeblöcke:
        
        log.info("Player " + player.getName() + " pasted " + blocksPasted + " code blocks");
        // Игрок вставил кодовых блоков:
        // Spieler hat Codeblöcke eingefügt:
    }
    
    /**
     * Сохраняет буфер обмена как общий
     * @param player Игрок
     * @param name Имя для общего буфера обмена
     *
     * Saves clipboard as shared
     * @param player Player
     * @param name Name for shared clipboard
     *
     * Speichert die Zwischenablage als geteilt
     * @param player Spieler
     * @param name Name für die geteilte Zwischenablage
     */
    public void saveShared(Player player, String name) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§c✖ No blocks in clipboard! Copy some blocks first.");
            // §c✖ Нет блоков в буфере обмена! Сначала скопируйте несколько блоков.
            // §c✖ Keine Blöcke in der Zwischenablage! Kopiere zuerst einige Blöcke.
            return;
        }
        
        sharedClipboards.put(name, data);
        player.sendMessage("§a✓ Saved clipboard as shared: " + name);
        // §a✓ Сохранено в буфере обмена как общее:
        // §a✓ Zwischenablage als geteilt gespeichert:
        
        log.info("Player " + player.getName() + " saved shared clipboard: " + name);
        // Игрок сохранил общий буфер обмена:
        // Spieler hat geteilte Zwischenablage gespeichert:
    }
    
    /**
     * Загружает общий буфер обмена
     * @param player Игрок
     * @param name Имя общего буфера обмена
     *
     * Loads a shared clipboard
     * @param player Player
     * @param name Name of shared clipboard
     *
     * Lädt eine geteilte Zwischenablage
     * @param player Spieler
     * @param name Name der geteilten Zwischenablage
     */
    public void loadShared(Player player, String name) {
        ClipboardData data = sharedClipboards.get(name);
        if (data == null) {
            player.sendMessage("§c✖ Shared clipboard not found: " + name);
            // §c✖ Общий буфер обмена не найден:
            // §c✖ Geteilte Zwischenablage nicht gefunden:
            return;
        }
        
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Loaded shared clipboard: " + name);
        // §a✓ Загружен общий буфер обмена:
        // §a✓ Geteilte Zwischenablage geladen:
        
        log.info("Player " + player.getName() + " loaded shared clipboard: " + name);
        // Игрок загрузил общий буфер обмена:
        // Spieler hat geteilte Zwischenablage geladen:
    }
    
    /**
     * Очищает буфер обмена игрока
     * @param player Игрок
     *
     * Clears player's clipboard
     * @param player Player
     *
     * Leert die Zwischenablage des Spielers
     * @param player Spieler
     */
    public void clearClipboard(Player player) {
        ClipboardData removed = playerClipboards.remove(player.getUniqueId());
        if (removed != null) {
            player.sendMessage("§a✓ Clipboard cleared");
            // §a✓ Буфер обмена очищен
            // §a✓ Zwischenablage geleert
        } else {
            player.sendMessage("§c✖ No clipboard to clear");
            // §c✖ Нет буфера обмена для очистки
            // §c✖ Keine Zwischenablage zum Leeren
        }
    }
    
    /**
     * Получает список общих буферов обмена
     * @return Список имен общих буферов обмена
     *
     * Gets list of shared clipboards
     * @return List of shared clipboard names
     *
     * Gibt Liste der geteilten Zwischenablagen zurück
     * @return Liste der Namen geteilter Zwischenablagen
     */
    public Set<String> getSharedClipboardNames() {
        return new HashSet<>(sharedClipboards.keySet());
    }
    
    /**
     * Создает глубокую копию CodeBlock
     * @param original Оригинальный CodeBlock
     * @return Глубокая копия CodeBlock
     *
     * Creates a deep copy of CodeBlock
     * @param original Original CodeBlock
     * @return Deep copy of CodeBlock
     *
     * Erstellt eine tiefe Kopie von CodeBlock
     * @param original Originaler CodeBlock
     * @return Tiefe Kopie von CodeBlock
     */
    private CodeBlock createDeepCopy(CodeBlock original) {
        // Create new block with same properties
        // Создать новый блок с теми же свойствами
        // Neuen Block mit denselben Eigenschaften erstellen
        CodeBlock copy = new CodeBlock(original.getMaterial().name(), original.getAction());
        
        // Copy all parameters
        // Скопировать все параметры
        // Alle Parameter kopieren
        Map<String, DataValue> parameters = original.getParameters();
        for (Map.Entry<String, DataValue> entry : parameters.entrySet()) {
            copy.setParameter(entry.getKey(), entry.getValue());
        }
        
        // Copy config items
        // Скопировать элементы конфигурации
        // Konfigurationselemente kopieren
        Map<Integer, org.bukkit.inventory.ItemStack> configItems = original.getConfigItems();
        for (Map.Entry<Integer, org.bukkit.inventory.ItemStack> entry : configItems.entrySet()) {
            copy.setConfigItem(entry.getKey(), entry.getValue().clone());
        }
        
        // Copy bracket properties if any
        // Скопировать свойства скобок, если есть
        // Klammereigenschaften kopieren, falls vorhanden
        if (original.isBracket()) {
            copy.setBracketType(original.getBracketType());
        }
        
        return copy;
    }
    
    /**
     * Типы буфера обмена
     *
     * Clipboard types
     *
     * Zwischenablagentypen
     */
    public enum ClipboardType {
        SINGLE_BLOCK, // Один блок // Ein Block // Single block
        REGION,       // Регион // Region // Region
        TEMPLATE      // Шаблон // Vorlage // Template
    }
    
    /**
     * Данные буфера обмена
     *
     * Clipboard data
     *
     * Zwischenablagendaten
     */
    public static class ClipboardData {
        private final ClipboardType type;
        private final Location referencePoint;
        private final Map<Location, CodeBlock> blocks;
        
        public ClipboardData(ClipboardType type, Location referencePoint) {
            this.type = type;
            this.referencePoint = referencePoint != null ? referencePoint.clone() : null;
            this.blocks = new HashMap<>();
        }
        
        public void addBlock(CodeBlock block, Location relativePosition) {
            blocks.put(relativePosition != null ? relativePosition.clone() : new Location(null, 0, 0, 0), block);
        }
        
        public ClipboardType getType() { return type; }
        public Location getReferencePoint() { return referencePoint != null ? referencePoint.clone() : null; }
        public Map<Location, CodeBlock> getBlocks() { return new HashMap<>(blocks); }
    }
}