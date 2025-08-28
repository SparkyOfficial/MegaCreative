package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.CodeBlock;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.interfaces.ICodingManager;
import com.megacreative.models.*;
import com.megacreative.utils.ConfigManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class WorldManagerImpl implements IWorldManager {
    
    private final Plugin plugin;
    private ICodingManager codingManager;
    private final ConfigManager configManager;
    private final Map<String, CreativeWorld> worlds;
    private final Map<UUID, List<String>> playerWorlds;
    private final int maxWorldsPerPlayer;
    private final int worldBorderSize;
    
    // Синхронизация для операций с мирами
    private final Object worldSaveLock = new Object();
    private final Object worldCreationLock = new Object();
    
    /**
     * Constructor with specific dependencies (no God Object)
     */
    public WorldManagerImpl(Plugin plugin, ICodingManager codingManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.codingManager = codingManager;
        this.configManager = configManager;
        this.worlds = new HashMap<>();
        this.playerWorlds = new HashMap<>();
        
        // Load settings from config
        this.maxWorldsPerPlayer = configManager.getMaxWorldsPerPlayer();
        this.worldBorderSize = configManager.getWorldBorderSize();
    }
    
    /**
     * Legacy constructor for backward compatibility
     * @deprecated Use constructor with specific dependencies
     */
    @Deprecated
    public WorldManagerImpl(com.megacreative.MegaCreative plugin) {
        this.plugin = plugin;
        this.codingManager = plugin.getCodingManager();
        this.configManager = plugin.getConfigManager();
        this.worlds = new HashMap<>();
        this.playerWorlds = new HashMap<>();
        this.maxWorldsPerPlayer = 5; // Default value
        this.worldBorderSize = 300; // Default value
    }
    
    /**
     * Constructor for ServiceRegistry (uses ConfigManager)
     */
    public WorldManagerImpl(ConfigManager configManager) {
        this.plugin = com.megacreative.MegaCreative.getInstance(); // Get the singleton instance
        this.codingManager = null; // Will be injected by ServiceRegistry
        this.configManager = configManager;
        this.worlds = new HashMap<>();
        this.playerWorlds = new HashMap<>();
        this.maxWorldsPerPlayer = configManager.getMaxWorldsPerPlayer();
        this.worldBorderSize = configManager.getWorldBorderSize();
    }
    
    /**
     * Sets the coding manager for dependency injection
     */
    public void setCodingManager(ICodingManager codingManager) {
        this.codingManager = codingManager;
    }
    
    /**
     * Инициализация менеджера миров - загружает все миры из файлов
     * Должен вызываться ПОСЛЕ создания всех остальных менеджеров
     */
    public void initialize() {
        loadWorlds();
    }
    
    public void createWorld(Player player, String name, CreativeWorldType worldType) {
        // Валидация имени мира
        if (!isValidWorldName(name)) {
            player.sendMessage("§cНекорректное имя мира!");
            player.sendMessage("§7Имя должно содержать 3-20 символов (буквы, цифры, подчеркивания)");
            return;
        }
        
        // Проверка на дублирование имени
        if (worldExists(name)) {
            player.sendMessage("§cМир с таким именем уже существует!");
            return;
        }
        
        // Проверка лимита миров
        if (getPlayerWorldCount(player) >= maxWorldsPerPlayer) {
            player.sendMessage("§cВы достигли лимита в " + maxWorldsPerPlayer + " миров.");
            return;
        }

        player.closeInventory();
        player.sendMessage("§eПодготовка к созданию мира '" + name + "'...");

        // Генерация ID и создание объекта мира синхронно
        String worldId = generateUniqueId();
        CreativeWorld creativeWorld = new CreativeWorld(worldId, name, player.getUniqueId(), player.getName(), worldType);

        // Вся работа с миром выполняется синхронно
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                player.sendMessage("§eСоздание мира... Пожалуйста, подождите.");
                World newWorld = createMinecraftWorld(creativeWorld);

                if (newWorld != null) {
                    // Настройка мира (границы, геймрулы) - должно быть синхронно
                    setupWorld(newWorld, creativeWorld);

                    // Регистрация мира в памяти
                    worlds.put(worldId, creativeWorld);
                    playerWorlds.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(worldId);

                    // Загрузка скриптов для мира (тоже синхронно, т.к. связано с миром)
                    if (codingManager != null) {
                        codingManager.loadScriptsForWorld(creativeWorld);
                    }
                    
                    // Телепортация - синхронно
                    player.teleport(newWorld.getSpawnLocation());
                    player.sendMessage("§aМир '" + name + "' успешно создан!");

                    // Асинхронное сохранение файла с синхронизацией
                    saveWorldAsync(creativeWorld, player);
                    
                } else {
                    throw new RuntimeException("Не удалось создать мир (Bukkit.createWorld вернул null)");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Критическая ошибка при создании мира: " + e.getMessage());
                e.printStackTrace();
                player.sendMessage("§cПроизошла ошибка при создании мира. Пожалуйста, обратитесь к администратору.");

                // Пытаемся очистить мир, если он был частично создан
                if (creativeWorld != null && creativeWorld.getWorldName() != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> { // Очистку делаем синхронно
                        World partiallyCreatedWorld = Bukkit.getWorld(creativeWorld.getWorldName());
                        if (partiallyCreatedWorld != null) {
                            plugin.getLogger().info("Попытка очистки частично созданного мира: " + partiallyCreatedWorld.getName());

                            // Кикаем всех игроков (хотя их там быть не должно)
                            for (Player p : partiallyCreatedWorld.getPlayers()) {
                                p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                                p.sendMessage("§cМир, в котором вы находились, не смог создаться и был удален.");
                            }
                            
                            // Выгружаем мир
                            if (!Bukkit.unloadWorld(partiallyCreatedWorld, false)) {
                                plugin.getLogger().warning("Не удалось выгрузить частично созданный мир для удаления.");
                                return; // Дальше нет смысла, так как файлы заблокированы
                            }
                            
                            // Удаляем файлы мира асинхронно, чтобы не тормозить сервер
                            File worldFolder = partiallyCreatedWorld.getWorldFolder();
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                try {
                                    deleteFolder(worldFolder);
                                    plugin.getLogger().info("Файлы поврежденного мира удалены: " + worldFolder.getName());
                                } catch (Exception deleteEx) {
                                    plugin.getLogger().severe("Не удалось удалить файлы поврежденного мира: " + deleteEx.getMessage());
                                }
                            });
                        }
                    });
                }
            }
        });
    }
    
    private World createMinecraftWorld(CreativeWorld creativeWorld) {
        WorldCreator creator = new WorldCreator(creativeWorld.getWorldName());
        creator.environment(creativeWorld.getWorldType().getEnvironment());
        
        // Настройка генератора в зависимости от типа
        switch (creativeWorld.getWorldType()) {
            case FLAT:
                creator.type(org.bukkit.WorldType.FLAT);
                break;
            case VOID:
                // Создаем плоский мир без структур для пустоты
                creator.type(org.bukkit.WorldType.FLAT);
                creator.generateStructures(false);
                // Настройка генератора для создания только спавн платформы
                creator.generatorSettings("minecraft:flat;minecraft:bedrock,2*minecraft:stone,minecraft:grass_block;minecraft:plains");
                break;
            case OCEAN:
                creator.type(org.bukkit.WorldType.NORMAL);
                break;
            default:
                creator.type(org.bukkit.WorldType.NORMAL);
                break;
        }
        
        // Этот метод вызывается асинхронно. Он только создает объект мира.
        // Вся настройка (setupWorld) будет произведена в основном потоке.
        return Bukkit.createWorld(creator);
    }
    
    private void setupWorld(World world, CreativeWorld creativeWorld) {
        // Настройка границ мира
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(worldBorderSize);
        border.setWarningDistance(10);
        
        // Настройка правил мира
        WorldFlags flags = creativeWorld.getFlags();
        world.setGameRule(GameRule.DO_MOB_SPAWNING, flags.isMobSpawning());
        world.setGameRule(GameRule.DO_FIRE_TICK, flags.isFireSpread());
        world.setGameRule(GameRule.MOB_GRIEFING, flags.isMobGriefing());
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, flags.isWeatherCycle());
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, flags.isDayNightCycle());
        world.setPVP(flags.isPvp());
        
        // Установка спавна
        world.setSpawnLocation(0, world.getHighestBlockYAt(0, 0) + 1, 0);
    }
    
    public void deleteWorld(String worldId, Player requester) {
        CreativeWorld world = worlds.get(worldId);
        if (world == null || !world.isOwner(requester)) {
            return;
        }
        
        // Сначала убеждаемся, что мир полностью выгружен
        boolean unloadedMain = true;
        World bukkitWorld = Bukkit.getWorld(world.getWorldName());
        if (bukkitWorld != null) {
            if (!bukkitWorld.getPlayers().isEmpty()) { // Сначала кикаем игроков, если они там есть.
                bukkitWorld.getPlayers().forEach(p -> p.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation()));
            }
            unloadedMain = Bukkit.unloadWorld(bukkitWorld, false); // Сохранять изменения в момент удаления - не всегда хорошая идея, т.к. может сохранить ошибки
            if (!unloadedMain) {
                plugin.getLogger().warning("Failed to unload main world: " + world.getWorldName() + ". Files might be locked.");
                requester.sendMessage("§cНе удалось полностью выгрузить основной мир. Возможно, требуется перезагрузка сервера.");
                return; // Не можем удалить файлы, если мир не выгружен
            }
        }

        boolean unloadedDev = true;
        World devWorld = Bukkit.getWorld(world.getDevWorldName());
        if (devWorld != null) {
            if (!devWorld.getPlayers().isEmpty()) { // Кикаем игроков
                devWorld.getPlayers().forEach(p -> p.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation()));
            }
            unloadedDev = Bukkit.unloadWorld(devWorld, false);
             if (!unloadedDev) {
                plugin.getLogger().warning("Failed to unload dev world: " + world.getDevWorldName() + ". Files might be locked.");
                requester.sendMessage("§cНе удалось полностью выгрузить мир разработки. Возможно, требуется перезагрузка сервера.");
                return; // Не можем удалить файлы, если мир не выгружен
            }
        }

        if (!unloadedMain || !unloadedDev) {
            requester.sendMessage("§cНекоторые миры не удалось выгрузить полностью. Пожалуйста, повторите команду или свяжитесь с администратором.");
            plugin.getLogger().severe("Cannot proceed with deleting world files as world unload failed.");
            return;
        }

        // Удаление из памяти
        worlds.remove(worldId);
        if (playerWorlds.containsKey(world.getOwnerId())) {
            playerWorlds.get(world.getOwnerId()).remove(worldId);
        }
        if (codingManager != null) {
            codingManager.unloadScriptsForWorld(world);
        }
        // Также очистите все связанные блоки кодинга, если они хранятся вне самого мира.
        if (plugin instanceof MegaCreative) {
            MegaCreative megaPlugin = (MegaCreative) plugin;
            megaPlugin.getServiceRegistry().getBlockPlacementHandler().clearAllCodeBlocksInWorld(Bukkit.getWorld(world.getWorldName()));
            megaPlugin.getServiceRegistry().getBlockPlacementHandler().clearAllCodeBlocksInWorld(Bukkit.getWorld(world.getDevWorldName()));
        }

        // Удаление файлов мира - асинхронно
        // Переместим deleteWorldFiles(world); сюда:
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> deleteWorldFilesInternal(world, requester));

        requester.sendMessage("§aМир '" + world.getName() + "' успешно помечен к удалению файлов!");
        requester.sendMessage("§7Файлы мира будут удалены в фоновом режиме.");
    }
    
    private void deleteWorldFilesInternal(CreativeWorld world, Player requester) {
        File worldFolder = new File(Bukkit.getWorldContainer(), world.getWorldName());
        File devWorldFolder = new File(Bukkit.getWorldContainer(), world.getDevWorldName());
        File dataFile = new File(plugin.getDataFolder(), "worlds/" + world.getId() + ".yml");
        
        try {
            boolean successMain = deleteFolderRecursive(worldFolder);
            boolean successDev = deleteFolderRecursive(devWorldFolder);
            boolean successDataFile = dataFile.delete();
            
            if (successMain && successDev && successDataFile) {
                plugin.getLogger().info("Successfully deleted world files for world ID " + world.getId());
                requester.sendMessage("§a✓ Файлы мира '" + world.getName() + "' полностью удалены.");
            } else {
                plugin.getLogger().warning("Failed to fully delete world files for world ID " + world.getId() + 
                                            ". Main: " + successMain + ", Dev: " + successDev + ", Data: " + successDataFile);
                requester.sendMessage("§c⚠ Ошибка удаления всех файлов мира. Возможно, они были заблокированы. Проверьте логи сервера.");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error deleting world files for world ID " + world.getId() + ": " + e.getMessage());
            requester.sendMessage("§c❌ Непредвиденная ошибка при удалении файлов мира: " + e.getMessage());
        }
    }

    // Агрессивная рекурсивная функция удаления папки
    private boolean deleteFolderRecursive(File folder) {
        if (!folder.exists()) {
            return true;
        }
        
        // ВАЖНО: нужно очищать только те файлы, которые реально принадлежат миру.
        // Здесь используется очень агрессивное удаление. 
        // ВНИМАНИЕ: НЕ ИСПОЛЬЗУЙТЕ ЕГО НА ПАПКАХ ВАЖНЕЕ МИРОВОЙ ПАПКИ, иначе можете удалить важные данные.
        // Также удостоверьтесь, что это папка точно для мира.
        
        try {
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        deleteFolderRecursive(file); // Рекурсивный вызов для подпапок и файлов
                    }
                }
            }
            // Удаляем саму папку или файл после того, как ее содержимое удалено
            return folder.delete();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to delete " + folder.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    private void deleteFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }
            folder.delete();
        }
    }
    
    public CreativeWorld getWorld(String id) {
        return worlds.get(id);
    }
    
    /**
     * Находит мир по его отображаемому имени
     * @param name Отображаемое имя мира
     * @return Найденный мир или null, если мир не найден
     */
    public CreativeWorld getWorldByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        
        // Сначала ищем точное совпадение
        for (CreativeWorld world : worlds.values()) {
            if (name.equalsIgnoreCase(world.getName())) {
                return world;
            }
        }
        
        // Если точного совпадения нет, ищем частичное (без учета регистра)
        for (CreativeWorld world : worlds.values()) {
            if (world.getName().toLowerCase().contains(name.toLowerCase())) {
                return world;
            }
        }
        
        return null;
    }
    
    /**
     * Находит мир по Bukkit-миру
     * @param bukkitWorld Bukkit-мир
     * @return Найденный CreativeWorld или null, если не найден
     */
    public CreativeWorld findCreativeWorldByBukkit(World bukkitWorld) {
        if (bukkitWorld == null) return null;
        
        String worldName = bukkitWorld.getName();
        if (worldName.startsWith("megacreative_")) {
            String id = worldName.replace("megacreative_", "").replace("_dev", "");
            return getWorld(id);
        }
        
        return null;
    }
    
    public List<CreativeWorld> getPlayerWorlds(Player player) {
        List<String> worldIds = playerWorlds.get(player.getUniqueId());
        if (worldIds == null) return new ArrayList<>();
        
        return worldIds.stream()
            .map(worlds::get)
            .filter(Objects::nonNull)
            .toList();
    }
    
    public List<CreativeWorld> getAllPublicWorlds() {
        return worlds.values().stream()
            .filter(world -> !world.isPrivate())
            .sorted((a, b) -> Integer.compare(b.getOnlineCount(), a.getOnlineCount()))
            .toList();
    }
    
    public int getPlayerWorldCount(Player player) {
        List<String> worldIds = playerWorlds.get(player.getUniqueId());
        return worldIds != null ? worldIds.size() : 0;
    }
    
    private String generateUniqueId() {
        String id;
        do {
            id = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        } while (worlds.containsKey(id));
        return id;
    }
    
    /**
     * Асинхронное сохранение мира с синхронизацией
     */
    public void saveWorldAsync(CreativeWorld world, Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (worldSaveLock) {
                try {
                    saveWorld(world);
                    // Уведомление об успехе в главном потоке
                    Bukkit.getScheduler().runTask(plugin, () -> 
                        player.sendMessage("§aМир '" + world.getName() + "' успешно сохранен!"));
                } catch (Exception e) {
                    plugin.getLogger().severe("Критическая ошибка при сохранении мира " + world.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                    // Уведомление об ошибке в главном потоке
                    Bukkit.getScheduler().runTask(plugin, () -> 
                        player.sendMessage("§cПроизошла ошибка при сохранении мира."));
                }
            }
        });
    }
    
    public void saveWorld(CreativeWorld world) {
        File dataFolder = new File(plugin.getDataFolder(), "worlds");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File worldFile = new File(dataFolder, world.getId() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        // Используем Gson для сериализации всего мира в JSON
        String worldJson = com.megacreative.utils.JsonSerializer.serializeWorld(world);
        config.set("worldData", worldJson);
        
        try {
            config.save(worldFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сохранения мира " + world.getId() + ": " + e.getMessage());
        }
    }
    
    public void saveAllWorlds() {
        worlds.values().forEach(this::saveWorld);
    }
    
    private void loadWorlds() {
        File dataFolder = new File(plugin.getDataFolder(), "worlds");
        if (!dataFolder.exists()) {
            return;
        }
        
        File[] worldFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (worldFiles == null) return;
        
        for (File worldFile : worldFiles) {
            try {
                loadWorld(worldFile);
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка загрузки мира " + worldFile.getName() + ": " + e.getMessage());
            }
        }
    }
    
    private void loadWorld(File worldFile) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(worldFile);
        
        // Используем Gson для десериализации мира из JSON
        String worldJson = config.getString("worldData");
        if (worldJson == null) {
            plugin.getLogger().warning("Файл мира " + worldFile.getName() + " не содержит данных worldData");
            return;
        }
        
        try {
            CreativeWorld world = com.megacreative.utils.JsonSerializer.deserializeWorld(worldJson);
            if (world != null) {
                worlds.put(world.getId(), world);
                playerWorlds.computeIfAbsent(world.getOwnerId(), k -> new ArrayList<>()).add(world.getId());

                // Автоматическая загрузка мира и скриптов, если он уже существует
                World bukkitWorld = Bukkit.getWorld(world.getWorldName());
                if (bukkitWorld == null) {
                    WorldCreator creator = new WorldCreator(world.getWorldName());
                    switch (world.getWorldType()) {
                        case FLAT -> creator.type(WorldType.FLAT);
                        case VOID -> creator.generator("VoidWorld"); // если есть генератор пустоты
                        case OCEAN -> creator.type(WorldType.NORMAL); // можно добавить генератор океана
                        case NETHER -> creator.environment(World.Environment.NETHER);
                        case END -> creator.environment(World.Environment.THE_END);
                        default -> creator.type(WorldType.NORMAL);
                    }
                    bukkitWorld = creator.createWorld();
                }
                if (bukkitWorld != null) {
                    ((MegaCreative) plugin).getCodingManager().loadScriptsForWorld(world);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка загрузки мира " + worldFile.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Проверяет корректность имени мира
     */
    private boolean isValidWorldName(String name) {
        return name != null && 
               name.length() >= 3 && 
               name.length() <= 20 && 
               name.matches("^[a-zA-Z0-9_\\sА-Яа-яЁё]+$");
    }
    
    /**
     * Проверяет, существует ли мир с таким именем
     */
    private boolean worldExists(String name) {
        return worlds.values().stream()
                .anyMatch(world -> world.getName().equalsIgnoreCase(name));
    }
}