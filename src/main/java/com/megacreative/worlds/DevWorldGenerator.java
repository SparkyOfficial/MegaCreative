package com.megacreative.worlds;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Генератор мира разработки с платформами для кодирования
 * Создает структурированные линии для размещения блоков кода
 *
 * Development world generator with coding platforms
 * Creates structured lines for placing code blocks
 *
 * Entwicklungs-Weltgenerator mit Codierplattformen
 * Erstellt strukturierte Linien für die Platzierung von Codeblöcken
 */
public class DevWorldGenerator extends ChunkGenerator {

    /**
     * Высота платформы для лучшей видимости
     *
     * Platform height for better visibility
     *
     * Plattformhöhe für bessere Sichtbarkeit
     */
    private static final int PLATFORM_Y = 64; 
    
    /**
     * Размеры платформы
     *
     * Platform dimensions
     *
     * Plattformabmessungen
     */
    // Количество линий кода
    // Number of code lines
    // Anzahl der Codezeilen
    private static final int LINES_COUNT = 25; 
    // Блоков в каждой линии
    // Blocks per line
    // Blöcke pro Zeile
    private static final int BLOCKS_PER_LINE = 50; 
    // Высота линии
    // Line height
    // Zeilenhöhe
    private static final int LINE_HEIGHT = 1; 
    // Расстояние между линиями
    // Distance between lines
    // Abstand zwischen den Zeilen
    private static final int LINE_SPACING = 2; 
    // Общее расстояние между линиями
    // Total distance between lines
    // Gesamtabstand zwischen den Zeilen
    private static final int LINES_SPACING = LINE_HEIGHT + LINE_SPACING; 
    
    /**
     * Размеры для дополнительных этажей
     *
     * Dimensions for additional floors
     *
     * Abmessungen für zusätzliche Etagen
     */
    // Высота между этажами
    // Height between floors
    // Höhe zwischen den Etagen
    private static final int FLOOR_HEIGHT = 10; 
    // Максимальное количество этажей
    // Maximum number of floors
    // Maximale Anzahl von Etagen
    private static final int MAX_FLOORS = 10; 
    
    /**
     * Генерирует поверхность мира
     * @param worldInfo Информация о мире
     * @param random Генератор случайных чисел
     * @param chunkX X координата чанка
     * @param chunkZ Z координата чанка
     * @param chunkData Данные чанка
     *
     * Generates world surface
     * @param worldInfo World information
     * @param random Random number generator
     * @param chunkX Chunk X coordinate
     * @param chunkZ Chunk Z coordinate
     * @param chunkData Chunk data
     *
     * Generiert die Welt-Oberfläche
     * @param worldInfo Weltinformationen
     * @param random Zufallszahlengenerator
     * @param chunkX Chunk-X-Koordinate
     * @param chunkZ Chunk-Z-Koordinate
     * @param chunkData Chunk-Daten
     */
    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        // Генерируем платформу кодирования для всех чанков в разумных пределах
        // Generate coding platform for all chunks within reasonable limits
        // Codierplattform für alle Chunks innerhalb angemessener Grenzen generieren
        generateCodingPlatform(chunkData, chunkX, chunkZ, 0); // Базовый этаж
        // Base floor
        // Grundboden
    }
    
    /**
     * Генерирует платформу кодирования на указанном этаже
     * @param chunkData Данные чанка
     * @param chunkX X координата чанка
     * @param chunkZ Z координата чанка 
     * @param floor Номер этажа (0 = базовый)
     *
     * Generates coding platform on specified floor
     * @param chunkData Chunk data
     * @param chunkX Chunk X coordinate
     * @param chunkZ Chunk Z coordinate
     * @param floor Floor number (0 = base)
     *
     * Generiert Codierplattform auf bestimmter Etage
     * @param chunkData Chunk-Daten
     * @param chunkX Chunk-X-Koordinate
     * @param chunkZ Chunk-Z-Koordinate
     * @param floor Etagennummer (0 = Basis)
     */
    public void generateCodingPlatform(ChunkData chunkData, int chunkX, int chunkZ, int floor) {
        int floorY = PLATFORM_Y + (floor * FLOOR_HEIGHT);
        
        // Генерируем платформу кодирования
        // Generate coding platform
        // Codierplattform generieren
        for (int x = 0; x < 16; x++) {
            int worldX = chunkX * 16 + x;
            
            for (int z = 0; z < 16; z++) {
                int worldZ = chunkZ * 16 + z;
                
                // Базовая платформа - создаем большую основу из белого стекла
                // Base platform - create large foundation from white glass
                // Basisplattform - große Grundlage aus weißem Glas erstellen
                if (worldX >= -10 && worldX < BLOCKS_PER_LINE + 10 && 
                    worldZ >= -10 && worldZ < LINES_COUNT * LINES_SPACING + 10) {
                    chunkData.setBlock(x, floorY, z, Material.WHITE_STAINED_GLASS);
                }
                
                // Генерируем линии кода
                // Generate code lines
                // Codezeilen generieren
                if (worldX >= 0 && worldX < BLOCKS_PER_LINE) {
                    for (int line = 0; line < LINES_COUNT; line++) {
                        int lineZ = line * LINES_SPACING;
                        
                        // Если текущий Z находится в пределах линии
                        // If current Z is within line bounds
                        // Wenn aktuelles Z innerhalb der Zeilengrenzen liegt
                        if (worldZ == lineZ) {
                            generateCodeLine(chunkData, x, floorY, z, worldX);
                        }
                        
                        // Генерируем соединительные блоки между линиями
                        // Generate connecting blocks between lines
                        // Verbindungsblöcke zwischen den Zeilen generieren
                        if (worldZ > lineZ && worldZ < lineZ + LINES_SPACING && worldX == 0) {
                            // Вертикальные соединители между линиями (светло-синее стекло)
                            // Vertical connectors between lines (light blue glass)
                            // Vertikale Verbinder zwischen den Zeilen (hellblaues Glas)
                            chunkData.setBlock(x, floorY, z, Material.LIGHT_BLUE_STAINED_GLASS);
                        }
                    }
                }
                
                // Генерируем барьеры по краям для безопасности
                // Generate barriers at edges for safety
                // Barrieren an den Rändern zur Sicherheit generieren
                generateBoundaries(chunkData, x, floorY, z, worldX, worldZ);
            }
        }
        
        // Генерируем воздушные блоки над платформой для комфорта
        // Generate air blocks above platform for comfort
        // Luftblöcke über der Plattform zur Bequemlichkeit generieren
        generateAirSpace(chunkData, chunkX, chunkZ, floorY);
    }
    
    /**
     * Генерирует одну линию кода
     *
     * Generates one code line
     *
     * Generiert eine Codezeile
     */
    private void generateCodeLine(ChunkData chunkData, int x, int y, int z, int worldX) {
        // Первый блок - синий (для событий/функций/циклов)
        // First block - blue (for events/functions/loops)
        // Erster Block - blau (für Ereignisse/Funktionen/Schleifen)
        if (worldX == 0) {
            chunkData.setBlock(x, y, z, Material.BLUE_STAINED_GLASS);
            // Добавляем маркер для начала линии
            // Add marker for line start
            // Markierung für Zeilenbeginn hinzufügen
            chunkData.setBlock(x, y + 1, z, Material.BEACON);
        } 
        // Второй блок - белое стекло (разделитель)
        // Second block - white glass (separator)
        // Zweiter Block - weißes Glas (Trennzeichen)
        else if (worldX == 1) {
            chunkData.setBlock(x, y, z, Material.WHITE_STAINED_GLASS);
        }
        // Остальные блоки - чередующийся серый/белый паттерн
        // Remaining blocks - alternating gray/white pattern
        // Verbleibende Blöcke - abwechselndes grau/weiß-Muster
        else if (worldX < BLOCKS_PER_LINE) {
            // Чередующийся паттерн: серый - белый - серый - белый
            // Alternating pattern: gray - white - gray - white
            // Abwechselndes Muster: grau - weiß - grau - weiß
            if ((worldX - 2) % 2 == 0) {
                chunkData.setBlock(x, y, z, Material.GRAY_STAINED_GLASS);
            } else {
                chunkData.setBlock(x, y, z, Material.WHITE_STAINED_GLASS);
            }
        }
    }
    
    /**
     * Генерирует границы платформы
     *
     * Generates platform boundaries
     *
     * Generiert Plattformgrenzen
     */
    private void generateBoundaries(ChunkData chunkData, int x, int y, int z, int worldX, int worldZ) {
        // Генерируем барьеры по краям
        // Generate barriers at edges
        // Barrieren an den Rändern generieren
        boolean isEdge = (worldX == -10 || worldX == BLOCKS_PER_LINE + 9 || 
                         worldZ == -10 || worldZ == LINES_COUNT * LINES_SPACING + 9);
        
        if (isEdge) {
            chunkData.setBlock(x, y + 1, z, Material.BARRIER);
            chunkData.setBlock(x, y + 2, z, Material.BARRIER);
        }
        
        // Добавляем информационные таблички в углах
        // Add information signs in corners
        // Informationsschilder in den Ecken hinzufügen
        if ((worldX == -5 && worldZ == -5) || 
            (worldX == BLOCKS_PER_LINE + 5 && worldZ == LINES_COUNT * LINES_SPACING + 5)) {
            chunkData.setBlock(x, y + 1, z, Material.OAK_SIGN);
        }
    }
    
    /**
     * Генерирует воздушное пространство над платформой
     *
     * Generates air space above platform
     *
     * Generiert Luftraum über der Plattform
     */
    private void generateAirSpace(ChunkData chunkData, int chunkX, int chunkZ, int floorY) {
        for (int x = 0; x < 16; x++) {
            int worldX = chunkX * 16 + x;
            
            for (int z = 0; z < 16; z++) {
                int worldZ = chunkZ * 16 + z;
                
                // Очищаем воздушное пространство над рабочей областью
                // Clear air space above work area
                // Luftraum über Arbeitsbereich freiräumen
                if (worldX >= -10 && worldX < BLOCKS_PER_LINE + 10 && 
                    worldZ >= -10 && worldZ < LINES_COUNT * LINES_SPACING + 10) {
                    
                    for (int y = floorY + 1; y <= floorY + 8; y++) {
                        if (y < chunkData.getMaxHeight()) {
                            chunkData.setBlock(x, y, z, Material.AIR);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Добавляет новый этаж в указанном мире для игрока
     * Вызывается из команды /addfloor
     *
     * Adds new floor in specified world for player
     * Called from /addfloor command
     *
     * Fügt neue Etage in der angegebenen Welt für Spieler hinzu
     * Wird vom /addfloor-Befehl aufgerufen
     */
    public static void addFloorForPlayer(org.bukkit.World world, org.bukkit.entity.Player player, int floorNumber) {
        if (floorNumber > MAX_FLOORS) {
            player.sendMessage("§cОшибка: Максимальное количество этажей: " + MAX_FLOORS);
            // Error: Maximum number of floors:
            // Fehler: Maximale Anzahl von Etagen:
            return;
        }
        
        if (floorNumber < 1) {
            player.sendMessage("§cОшибка: Номер этажа должен быть больше 0");
            // Error: Floor number must be greater than 0
            // Fehler: Etagennummer muss größer als 0 sein
            return;
        }
        
        player.sendMessage("§eСоздание этажа " + floorNumber + "...");
        // Creating floor...
        // Etage wird erstellt...
        
        // Генерируем новый этаж
        // Generate new floor
        // Neue Etage generieren
        addFloorToWorld(world, floorNumber);
        
        // Телепортируем игрока на новый этаж
        // Teleport player to new floor
        // Spieler auf neue Etage teleportieren
        int floorY = PLATFORM_Y + (floorNumber * FLOOR_HEIGHT);
        org.bukkit.Location teleportLocation = new org.bukkit.Location(world, 0, floorY + 2, 0);
        player.teleport(teleportLocation);
        
        player.sendMessage("§a✓ Этаж " + floorNumber + " создан! Вы телепортированы на новый этаж.");
        // Floor created! You have been teleported to the new floor.
        // Etage erstellt! Sie wurden auf die neue Etage teleportiert.
    }
    
    /**
     * Статический метод для добавления нового этажа в существующий мир
     *
     * Static method for adding new floor to existing world
     *
     * Statische Methode zum Hinzufügen einer neuen Etage zur bestehenden Welt
     */
    public static void addFloorToWorld(org.bukkit.World world, int floorNumber) {
        if (floorNumber > MAX_FLOORS) {
            return; // Ограничиваем количество этажей
            // Limit number of floors
            // Anzahl der Etagen begrenzen
        }
        
        int floorY = PLATFORM_Y + (floorNumber * FLOOR_HEIGHT);
        
        // Генерируем новый этаж в загруженных чанках
        // Generate new floor in loaded chunks
        // Neue Etage in geladenen Chunks generieren
        for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
            // Создаем временный ChunkData для генерации
            // Create temporary ChunkData for generation
            // Temporäre ChunkData für die Generierung erstellen
            // В данном случае мы будем устанавливать блоки напрямую
            // In this case we will set blocks directly
            // In diesem Fall werden wir Blöcke direkt setzen
            generateFloorDirectly(world, chunk.getX(), chunk.getZ(), floorNumber);
        }
    }
    
    /**
     * Генерирует этаж напрямую в мире
     *
     * Generates floor directly in world
     *
     * Generiert Etage direkt in der Welt
     */
    private static void generateFloorDirectly(org.bukkit.World world, int chunkX, int chunkZ, int floor) {
        int floorY = PLATFORM_Y + (floor * FLOOR_HEIGHT);
        
        for (int x = 0; x < 16; x++) {
            int worldX = chunkX * 16 + x;
            
            for (int z = 0; z < 16; z++) {
                int worldZ = chunkZ * 16 + z;
                
                // Базовая платформа
                // Base platform
                // Basisplattform
                if (worldX >= -10 && worldX < BLOCKS_PER_LINE + 10 && 
                    worldZ >= -10 && worldZ < LINES_COUNT * LINES_SPACING + 10) {
                    
                    org.bukkit.Location loc = new org.bukkit.Location(world, worldX, floorY, worldZ);
                    world.getBlockAt(loc).setType(Material.WHITE_STAINED_GLASS);
                }
                
                // Генерируем линии кода
                // Generate code lines
                // Codezeilen generieren
                if (worldX >= 0 && worldX < BLOCKS_PER_LINE) {
                    for (int line = 0; line < LINES_COUNT; line++) {
                        int lineZ = line * LINES_SPACING;
                        
                        if (worldZ == lineZ) {
                            org.bukkit.Location loc = new org.bukkit.Location(world, worldX, floorY, worldZ);
                            
                            if (worldX == 0) {
                                world.getBlockAt(loc).setType(Material.BLUE_STAINED_GLASS);
                                world.getBlockAt(loc.add(0, 1, 0)).setType(Material.BEACON);
                            } else if (worldX == 1) {
                                world.getBlockAt(loc).setType(Material.WHITE_STAINED_GLASS);
                            } else if ((worldX - 2) % 2 == 0) {
                                world.getBlockAt(loc).setType(Material.GRAY_STAINED_GLASS);
                            } else {
                                world.getBlockAt(loc).setType(Material.WHITE_STAINED_GLASS);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Получает номер линии кода по Z координате
     *
     * Gets code line number by Z coordinate
     *
     * Ruft Codezeilennummer nach Z-Koordinate ab
     */
    public static int getCodeLineFromZ(int z) {
        if (z < 0) return -1;
        return z / LINES_SPACING;
    }
    
    /**
     * Получает Z координату для указанной линии кода
     *
     * Gets Z coordinate for specified code line
     *
     * Ruft Z-Koordinate für angegebene Codezeile ab
     */
    public static int getZForCodeLine(int line) {
        return line * LINES_SPACING;
    }
    
    /**
     * Проверяет, является ли позиция валидной для размещения блока кода
     *
     * Checks if position is valid for placing code block
     *
     * Prüft, ob die Position für die Platzierung eines Codeblocks gültig ist
     */
    public static boolean isValidCodePosition(int x, int z) {
        if (x < 0 || x >= BLOCKS_PER_LINE) return false;
        
        int line = getCodeLineFromZ(z);
        return line >= 0 && line < LINES_COUNT && z == getZForCodeLine(line);
    }
    
    /**
     * Получает следующую позицию в линии кода
     *
     * Gets next position in code line
     *
     * Ruft nächste Position in Codezeile ab
     */
    public static org.bukkit.Location getNextPositionInLine(org.bukkit.Location current) {
        int line = getCodeLineFromZ(current.getBlockZ());
        if (line == -1) return null;
        
        int nextX = current.getBlockX() + 1;
        if (nextX >= BLOCKS_PER_LINE) return null;
        
        return new org.bukkit.Location(
            current.getWorld(), 
            nextX, 
            current.getBlockY(), 
            getZForCodeLine(line)
        );
    }
    
    /**
     * Геттеры для констант
     *
     * Getters for constants
     *
     * Getter für Konstanten
     */
    public static int getLinesCount() { return LINES_COUNT; }
    public static int getBlocksPerLine() { return BLOCKS_PER_LINE; }
    public static int getLinesSpacing() { return LINES_SPACING; }
    public static int getPlatformY() { return PLATFORM_Y; }
    public static int getFloorHeight() { return FLOOR_HEIGHT; }
    public static int getMaxFloors() { return MAX_FLOORS; }
}