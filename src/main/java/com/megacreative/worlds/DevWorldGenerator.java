package com.megacreative.worlds;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Генератор мира разработки с платформами для кодирования
 * Создает структурированные линии для размещения блоков кода
 */
public class DevWorldGenerator extends ChunkGenerator {

    private static final int PLATFORM_Y = 64; // Высота платформы для лучшей видимости
    
    // Размеры платформы
    private static final int LINES_COUNT = 25; // Количество линий кода
    private static final int BLOCKS_PER_LINE = 50; // Блоков в каждой линии
    private static final int LINE_HEIGHT = 1; // Высота линии
    private static final int LINE_SPACING = 2; // Расстояние между линиями
    private static final int LINES_SPACING = LINE_HEIGHT + LINE_SPACING; // Общее расстояние между линиями
    
    // Размеры для дополнительных этажей
    private static final int FLOOR_HEIGHT = 10; // Высота между этажами
    private static final int MAX_FLOORS = 10; // Максимальное количество этажей
    
    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        // Генерируем платформу кодирования для всех чанков в разумных пределах
        generateCodingPlatform(chunkData, chunkX, chunkZ, 0); // Базовый этаж
    }
    
    /**
     * Генерирует платформу кодирования на указанном этаже
     * @param chunkData Данные чанка
     * @param chunkX X координата чанка
     * @param chunkZ Z координата чанка 
     * @param floor Номер этажа (0 = базовый)
     */
    public void generateCodingPlatform(ChunkData chunkData, int chunkX, int chunkZ, int floor) {
        int floorY = PLATFORM_Y + (floor * FLOOR_HEIGHT);
        
        // Генерируем платформу кодирования
        for (int x = 0; x < 16; x++) {
            int worldX = chunkX * 16 + x;
            
            for (int z = 0; z < 16; z++) {
                int worldZ = chunkZ * 16 + z;
                
                // Базовая платформа - создаем большую основу из белого стекла
                if (worldX >= -10 && worldX < BLOCKS_PER_LINE + 10 && 
                    worldZ >= -10 && worldZ < LINES_COUNT * LINES_SPACING + 10) {
                    chunkData.setBlock(x, floorY, z, Material.WHITE_STAINED_GLASS);
                }
                
                // Генерируем линии кода
                if (worldX >= 0 && worldX < BLOCKS_PER_LINE) {
                    for (int line = 0; line < LINES_COUNT; line++) {
                        int lineZ = line * LINES_SPACING;
                        
                        // Если текущий Z находится в пределах линии
                        if (worldZ == lineZ) {
                            generateCodeLine(chunkData, x, floorY, z, worldX);
                        }
                        
                        // Генерируем соединительные блоки между линиями
                        if (worldZ > lineZ && worldZ < lineZ + LINES_SPACING && worldX == 0) {
                            // Вертикальные соединители между линиями (светло-синее стекло)
                            chunkData.setBlock(x, floorY, z, Material.LIGHT_BLUE_STAINED_GLASS);
                        }
                    }
                }
                
                // Генерируем барьеры по краям для безопасности
                generateBoundaries(chunkData, x, floorY, z, worldX, worldZ);
            }
        }
        
        // Генерируем воздушные блоки над платформой для комфорта
        generateAirSpace(chunkData, chunkX, chunkZ, floorY);
    }
    
    /**
     * Генерирует одну линию кода
     */
    private void generateCodeLine(ChunkData chunkData, int x, int y, int z, int worldX) {
        // Первый блок - синий (для событий/функций/циклов)
        if (worldX == 0) {
            chunkData.setBlock(x, y, z, Material.BLUE_STAINED_GLASS);
            // Добавляем маркер для начала линии
            chunkData.setBlock(x, y + 1, z, Material.BEACON);
        } 
        // Второй блок - белое стекло (разделитель)
        else if (worldX == 1) {
            chunkData.setBlock(x, y, z, Material.WHITE_STAINED_GLASS);
        }
        // Остальные блоки - чередующийся серый/белый паттерн
        else if (worldX < BLOCKS_PER_LINE) {
            // Чередующийся паттерн: серый - белый - серый - белый
            if ((worldX - 2) % 2 == 0) {
                chunkData.setBlock(x, y, z, Material.GRAY_STAINED_GLASS);
            } else {
                chunkData.setBlock(x, y, z, Material.WHITE_STAINED_GLASS);
            }
        }
    }
    
    /**
     * Генерирует границы платформы
     */
    private void generateBoundaries(ChunkData chunkData, int x, int y, int z, int worldX, int worldZ) {
        // Генерируем барьеры по краям
        boolean isEdge = (worldX == -10 || worldX == BLOCKS_PER_LINE + 9 || 
                         worldZ == -10 || worldZ == LINES_COUNT * LINES_SPACING + 9);
        
        if (isEdge) {
            chunkData.setBlock(x, y + 1, z, Material.BARRIER);
            chunkData.setBlock(x, y + 2, z, Material.BARRIER);
        }
        
        // Добавляем информационные таблички в углах
        if ((worldX == -5 && worldZ == -5) || 
            (worldX == BLOCKS_PER_LINE + 5 && worldZ == LINES_COUNT * LINES_SPACING + 5)) {
            chunkData.setBlock(x, y + 1, z, Material.OAK_SIGN);
        }
    }
    
    /**
     * Генерирует воздушное пространство над платформой
     */
    private void generateAirSpace(ChunkData chunkData, int chunkX, int chunkZ, int floorY) {
        for (int x = 0; x < 16; x++) {
            int worldX = chunkX * 16 + x;
            
            for (int z = 0; z < 16; z++) {
                int worldZ = chunkZ * 16 + z;
                
                // Очищаем воздушное пространство над рабочей областью
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
     */
    public static void addFloorForPlayer(org.bukkit.World world, org.bukkit.entity.Player player, int floorNumber) {
        if (floorNumber > MAX_FLOORS) {
            player.sendMessage("§cОшибка: Максимальное количество этажей: " + MAX_FLOORS);
            return;
        }
        
        if (floorNumber < 1) {
            player.sendMessage("§cОшибка: Номер этажа должен быть больше 0");
            return;
        }
        
        player.sendMessage("§eСоздание этажа " + floorNumber + "...");
        
        // Генерируем новый этаж
        addFloorToWorld(world, floorNumber);
        
        // Телепортируем игрока на новый этаж
        int floorY = PLATFORM_Y + (floorNumber * FLOOR_HEIGHT);
        org.bukkit.Location teleportLocation = new org.bukkit.Location(world, 0, floorY + 2, 0);
        player.teleport(teleportLocation);
        
        player.sendMessage("§a✓ Этаж " + floorNumber + " создан! Вы телепортированы на новый этаж.");
    }
    
    /**
     * Статический метод для добавления нового этажа в существующий мир
     */
    public static void addFloorToWorld(org.bukkit.World world, int floorNumber) {
        if (floorNumber > MAX_FLOORS) {
            return; // Ограничиваем количество этажей
        }
        
        DevWorldGenerator generator = new DevWorldGenerator();
        int floorY = PLATFORM_Y + (floorNumber * FLOOR_HEIGHT);
        
        // Генерируем новый этаж в загруженных чанках
        for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
            // Создаем временный ChunkData для генерации
            // В данном случае мы будем устанавливать блоки напрямую
            generateFloorDirectly(world, chunk.getX(), chunk.getZ(), floorNumber);
        }
    }
    
    /**
     * Генерирует этаж напрямую в мире
     */
    private static void generateFloorDirectly(org.bukkit.World world, int chunkX, int chunkZ, int floor) {
        int floorY = PLATFORM_Y + (floor * FLOOR_HEIGHT);
        
        for (int x = 0; x < 16; x++) {
            int worldX = chunkX * 16 + x;
            
            for (int z = 0; z < 16; z++) {
                int worldZ = chunkZ * 16 + z;
                
                // Базовая платформа
                if (worldX >= -10 && worldX < BLOCKS_PER_LINE + 10 && 
                    worldZ >= -10 && worldZ < LINES_COUNT * LINES_SPACING + 10) {
                    
                    org.bukkit.Location loc = new org.bukkit.Location(world, worldX, floorY, worldZ);
                    world.getBlockAt(loc).setType(Material.WHITE_STAINED_GLASS);
                }
                
                // Генерируем линии кода
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
     */
    public static int getCodeLineFromZ(int z) {
        if (z < 0) return -1;
        return z / LINES_SPACING;
    }
    
    /**
     * Получает Z координату для указанной линии кода
     */
    public static int getZForCodeLine(int line) {
        return line * LINES_SPACING;
    }
    
    /**
     * Проверяет, является ли позиция валидной для размещения блока кода
     */
    public static boolean isValidCodePosition(int x, int z) {
        if (x < 0 || x >= BLOCKS_PER_LINE) return false;
        
        int line = getCodeLineFromZ(z);
        return line >= 0 && line < LINES_COUNT && z == getZForCodeLine(line);
    }
    
    /**
     * Получает следующую позицию в линии кода
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
    
    // Геттеры для констант
    public static int getLinesCount() { return LINES_COUNT; }
    public static int getBlocksPerLine() { return BLOCKS_PER_LINE; }
    public static int getLinesSpacing() { return LINES_SPACING; }
    public static int getPlatformY() { return PLATFORM_Y; }
    public static int getFloorHeight() { return FLOOR_HEIGHT; }
    public static int getMaxFloors() { return MAX_FLOORS; }
}