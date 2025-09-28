package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeHandler;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.interfaces.ICodingManager;
import com.megacreative.models.*;
import com.megacreative.utils.ConfigManager;
import com.megacreative.utils.JsonSerializer;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementation of the world manager
 *
 * Реализация менеджера миров
 *
 * Implementierung des Welt-Managers
 */
public class WorldManagerImpl implements IWorldManager {
    
    private Plugin plugin;
    private ICodingManager codingManager;
    private final ConfigManager configManager;
    private final Map<String, CreativeWorld> worlds;
    private final Map<UUID, List<String>> playerWorlds;
    private final int maxWorldsPerPlayer;
    private final int worldBorderSize;
    
    // Синхронизация для операций с мирами
    // Synchronization for world operations
    // Synchronisation für Weltoperationen
    private final Object worldSaveLock = new Object();
    private final Object worldCreationLock = new Object();
    
    /**
     * Constructor with specific dependencies (no God Object)
     *
     * Конструктор с конкретными зависимостями (без God Object)
     *
     * Konstruktor mit spezifischen Abhängigigkeiten (kein God Object)
     */
    public WorldManagerImpl(Plugin plugin, ICodingManager codingManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.codingManager = codingManager;
        this.configManager = configManager;
        this.worlds = new HashMap<>();
        this.playerWorlds = new HashMap<>();
        
        // Load settings from config with null safety
        if (configManager != null) {
            this.maxWorldsPerPlayer = configManager.getMaxWorldsPerPlayer();
            this.worldBorderSize = configManager.getWorldBorderSize();
        } else {
            this.maxWorldsPerPlayer = 5; // Default value
            this.worldBorderSize = 300; // Default value
        }
    }
    
    /**
     * Legacy constructor for backward compatibility
     * @deprecated Use constructor with specific dependencies
     *
     * Устаревший конструктор для обратной совместимости
     * @deprecated Используйте конструктор с конкретными зависимостями
     *
     * Legacy-Konstruktor für Abwärtskompatibilität
     * @deprecated Verwenden Sie den Konstruktor mit spezifischen Abhängigkeiten
     */
    @Deprecated
    public WorldManagerImpl(com.megacreative.MegaCreative plugin) {
        this.plugin = plugin;
        this.codingManager = plugin.getServiceRegistry().getCodingManager();
        this.configManager = plugin.getServiceRegistry().getConfigManager();
        this.worlds = new HashMap<>();
        this.playerWorlds = new HashMap<>();
        this.maxWorldsPerPlayer = 5; // Default value
        this.worldBorderSize = 300; // Default value
    }
    
    /**
     * Constructor for ServiceRegistry (uses ConfigManager and Plugin)
     *
     * Конструктор для ServiceRegistry (использует ConfigManager и Plugin)
     *
     * Konstruktor für ServiceRegistry (verwendet ConfigManager und Plugin)
     */
    public WorldManagerImpl(ConfigManager configManager, MegaCreative plugin) {
        this.plugin = plugin; // Use the injected plugin
        this.codingManager = null; // Will be injected by ServiceRegistry
        this.configManager = configManager;
        this.worlds = new HashMap<>();
        this.playerWorlds = new HashMap<>();
        
        // Load settings from config with null safety
        if (configManager != null) {
            this.maxWorldsPerPlayer = configManager.getMaxWorldsPerPlayer();
            this.worldBorderSize = configManager.getWorldBorderSize();
        } else {
            this.maxWorldsPerPlayer = 5; // Default value
            this.worldBorderSize = 300; // Default value
        }
        
        // Do not load worlds immediately - wait for delayed initialization
        if (plugin != null) {
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("WorldManagerImpl created, worlds will be loaded later");
        }
    }
    
    /**
     * Constructor for ServiceRegistry (uses ConfigManager)
     *
     * Конструктор для ServiceRegistry (использует ConfigManager)
     *
     * Konstruktor für ServiceRegistry (verwendet ConfigManager)
     */
    public WorldManagerImpl(ConfigManager configManager) {
        this.plugin = null; // Will be injected later
        this.codingManager = null; // Will be injected by ServiceRegistry
        this.configManager = configManager;
        this.worlds = new HashMap<>();
        this.playerWorlds = new HashMap<>();
        
        // Load settings from config with null safety
        if (configManager != null) {
            this.maxWorldsPerPlayer = configManager.getMaxWorldsPerPlayer();
            this.worldBorderSize = configManager.getWorldBorderSize();
        } else {
            this.maxWorldsPerPlayer = 5; // Default value
            this.worldBorderSize = 300; // Default value
        }
        
        // Do not load worlds immediately - wait for delayed initialization
        Plugin plugin = getPlugin();
        if (plugin != null) {
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("WorldManagerImpl created, worlds will be loaded later");
        }
    }
    
    /**
     * Sets the plugin instance for dependency injection
     *
     * Устанавливает экземпляр плагина для внедрения зависимостей
     *
     * Setzt die Plugin-Instanz für die Abhängigkeitsinjektion
     */
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
        if (plugin != null) {
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("Plugin set in WorldManagerImpl");
        }
    }
    
    /**
     * Gets the plugin instance
     *
     * Получает экземпляр плагина
     *
     * Gibt die Plugin-Instanz zurück
     */
    private Plugin getPlugin() {
        return plugin;
    }
    
    /**
     * Sets the coding manager for dependency injection
     *
     * Устанавливает менеджер кодирования для внедрения зависимостей
     *
     * Setzt den Coding-Manager für die Abhängigkeitsinjektion
     */
    public void setCodingManager(ICodingManager codingManager) {
        this.codingManager = codingManager;
    }
    
    /**
     * Инициализация менеджера миров - загружает все миры из файлов
     * Должен вызываться ПОСЛЕ создания всех остальных менеджеров
     *
     * World manager initialization - loads all worlds from files
     * Should be called AFTER creating all other managers
     *
     * Welt-Manager-Initialisierung - lädt alle Welten aus Dateien
     * Sollte NACH der Erstellung aller anderen Manager aufgerufen werden
     */
    @Override
    public void initialize() {
        // Check if plugin is available
        Plugin plugin = getPlugin();
        if (plugin == null) {
            return;
        }
        
        try {
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("Initializing WorldManagerImpl...");
            
            // Load all worlds from storage
            loadWorlds();
            
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("WorldManagerImpl initialized successfully with " + worlds.size() + " worlds");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize WorldManagerImpl: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
    }
    
    /**
     * Loads all worlds from storage
     */
    public void loadWorlds() {
        Plugin plugin = getPlugin();
        if (plugin == null) {
            return;
        }
        
        try {
            File worldsDir = new File(plugin.getDataFolder(), "worlds");
            if (!worldsDir.exists()) {
                worldsDir.mkdirs();
                return;
            }
            
            File[] worldFiles = worldsDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (worldFiles == null) return;
            
            int loadedCount = 0;
            for (File worldFile : worldFiles) {
                try {
                    loadWorldFromFile(worldFile);
                    loadedCount++;
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load world from " + worldFile.getName() + ": " + e.getMessage());
                }
            }
            
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("Loaded " + loadedCount + " worlds from storage");
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().severe("Error in loadWorlds: " + e.getMessage());
            }
        }
    }
    
    /**
     * Loads a single world from a JSON file
     * @param worldFile The JSON file containing world data
     */
    private void loadWorldFromFile(File worldFile) {
        Plugin plugin = getPlugin();
        if (plugin == null || !(plugin instanceof MegaCreative)) {
            return;
        }
        
        try {
            // Read the JSON file
            String jsonContent = new String(java.nio.file.Files.readAllBytes(worldFile.toPath()));
            
            // Deserialize the world
            CreativeWorld world = com.megacreative.utils.JsonSerializer.deserializeWorld(jsonContent, (MegaCreative) plugin);
            
            if (world != null) {
                // Register the world in memory
                worlds.put(world.getId(), world);
                
                // Register the world with the player
                playerWorlds.computeIfAbsent(world.getOwnerId(), k -> new ArrayList<>()).add(world.getId());
                
                // Load scripts for the world
                if (codingManager != null) {
                    codingManager.loadScriptsForWorld(world);
                }
                
                plugin.getLogger().info("Successfully loaded world: " + world.getName() + " (ID: " + world.getId() + ")");
            } else {
                plugin.getLogger().warning("Failed to deserialize world from file: " + worldFile.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading world from file " + worldFile.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generates a unique ID for worlds
     * @return A unique ID string
     */
    private String generateUniqueId() {
        // Generate a 6-digit numeric ID instead of long UUID
        return String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 999999));
    }
    
    /**
     * Checks if a world with the given name already exists
     * @param name The world name to check
     * @return true if a world with that name exists, false otherwise
     */
    private boolean worldExists(String name) {
        for (CreativeWorld world : worlds.values()) {
            if (world.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the number of worlds owned by a player
     * @param player The player to check
     * @return The number of worlds owned by the player
     */
    @Override
    public int getPlayerWorldCount(Player player) {
        List<String> playerWorldIds = playerWorlds.get(player.getUniqueId());
        return playerWorldIds != null ? playerWorldIds.size() : 0;
    }
    
    /**
     * Validates a world name
     * @param name The world name to validate
     * @return true if the name is valid, false otherwise
     */
    private boolean isValidWorldName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Check length (3-20 characters)
        if (name.length() < 3 || name.length() > 20) {
            return false;
        }
        
        // Check for valid characters (letters, numbers, underscores)
        return name.matches("[a-zA-Z0-9_]+");
    }
    
    /**
     * Saves a world to storage
     * @param world The world to save
     */
    @Override
    public void saveWorld(CreativeWorld world) {
        Plugin plugin = getPlugin();
        if (plugin == null || !(plugin instanceof MegaCreative)) {
            return;
        }
        
        try {
            // Serialize the world to JSON
            String json = com.megacreative.utils.JsonSerializer.serializeWorld(world);
            
            // Write to file
            File worldsDir = new File(plugin.getDataFolder(), "worlds");
            if (!worldsDir.exists()) {
                worldsDir.mkdirs();
            }
            
            File worldFile = new File(worldsDir, world.getId() + ".json");
            java.nio.file.Files.write(worldFile.toPath(), json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            // Save scripts for the world
            if (codingManager != null) {
                // Scripts are saved by the coding manager
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error saving world " + world.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Saves a world asynchronously
     * @param world The world to save
     * @param player The player who triggered the save
     */
    @Override
    public void saveWorldAsync(CreativeWorld world, Player player) {
        Plugin plugin = getPlugin();
        if (plugin == null) {
            return;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            saveWorld(world);
        });
    }
    
    /**
     * Saves all worlds
     */
    @Override
    public void saveAllWorlds() {
        // Save all worlds in the system
        for (CreativeWorld world : worlds.values()) {
            saveWorld(world);
        }
    }
    
    /**
     * Creates a world for a player
     * @param player the player
     * @param name the world name
     * @param worldType the world type
     *
     * Создает мир для игрока
     * @param player игрок
     * @param name название мира
     * @param worldType тип мира
     *
     * Erstellt eine Welt für einen Spieler
     * @param player der Spieler
     * @param name der Weltname
     * @param worldType der Welttyp
     */
    @Override
    public void createWorld(Player player, String name, CreativeWorldType worldType) {
        createWorld(player, name, worldType, CreativeWorld.WorldDualMode.STANDALONE, null);
    }
    
    // 🎆 ENHANCED: Reference system-style dual world creation with pairing support
    // 🎆 УСОВЕРШЕНСТВОВАННАЯ: Создание парных миров в стиле reference system с поддержкой сопряжения
    // 🎆 VERBESSERTE: Referenzsystem-Stil duale Welt-Erstellung mit Paarungsunterstützung
    /**
     * Creates a dual world for a player
     * @param player the player
     * @param name the world name
     * @param worldType the world type
     *
     * Создает парный мир для игрока
     * @param player игрок
     * @param name название мира
     * @param worldType тип мира
     *
     * Erstellt eine duale Welt für einen Spieler
     * @param player der Spieler
     * @param name der Weltname
     * @param worldType der Welttyp
     */
    @Override
    public void createDualWorld(Player player, String name, CreativeWorldType worldType) {
        // Create dev world first
        String devWorldId = generateUniqueId();
        String playWorldId = generateUniqueId();
        
        createWorld(player, name, worldType, CreativeWorld.WorldDualMode.DEV, playWorldId);
        createWorld(player, name + " (Play)", worldType, CreativeWorld.WorldDualMode.PLAY, devWorldId);
    }
    
    /**
     * Creates a world for a player with specific parameters
     * @param player the player
     * @param name the world name
     * @param worldType the world type
     * @param dualMode the dual mode
     * @param pairedWorldId the paired world ID
     *
     * Создает мир для игрока с конкретными параметрами
     * @param player игрок
     * @param name название мира
     * @param worldType тип мира
     * @param dualMode двойной режим
     * @param pairedWorldId ID сопряженного мира
     *
     * Erstellt eine Welt für einen Spieler mit spezifischen Parametern
     * @param player der Spieler
     * @param name der Weltname
     * @param worldType der Welttyp
     * @param dualMode der duale Modus
     * @param pairedWorldId die ID der gekoppelten Welt
     */
    public void createWorld(Player player, String name, CreativeWorldType worldType, 
                           CreativeWorld.WorldDualMode dualMode, String pairedWorldId) {
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

        player.sendMessage("§eПодготовка к созданию мира '" + name + "'...");

        // Генерация ID и создание объекта мира синхронно
        String worldId = generateUniqueId();
        CreativeWorld creativeWorld = new CreativeWorld(worldId, name, player.getUniqueId(), player.getName(), worldType);
        
        // 🎆 ENHANCED: Set dual world properties
        creativeWorld.setDualMode(dualMode);
        if (pairedWorldId != null) {
            creativeWorld.setPairedWorldId(pairedWorldId);
        }

        // Вся работа с миром выполняется синхронно
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                // Закрываем инвентарь в основном потоке
                player.closeInventory();
                player.sendMessage("§eСоздание мира... Пожалуйста, подождите.");
                World newWorld = createMinecraftWorld(creativeWorld);

                if (newWorld != null) {
                    // Настройка мира (границы, геймрулы) - должно должно быть синхронно
                    setupWorld(newWorld, creativeWorld);

                    // Регистрация мира в памяти
                    worlds.put(worldId, creativeWorld);
                    playerWorlds.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(worldId);

                    // Загрузка скриптов для мира (тоже синхронно, т.к. связано с миром)
                    if (codingManager != null) {
                        codingManager.loadScriptsForWorld(creativeWorld);
                    }
                    
                    // Initialize CodeHandler for the world
                    if (plugin instanceof MegaCreative) {
                        MegaCreative megaPlugin = (MegaCreative) plugin;
                        CodeHandler codeHandler = new CodeHandler(megaPlugin, creativeWorld);
                        creativeWorld.setCodeHandler(codeHandler);
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
                getPlugin().getLogger().severe("Критическая ошибка при создании мира: " + e.getMessage());
                getPlugin().getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
                player.sendMessage("§cПроизошла ошибка при создании мира. Пожалуйста, обратитесь к администратору.");

                // Пытаемся очистить мир, если он был частично создан
                if (creativeWorld != null && creativeWorld.getWorldName() != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> { // Очистку делаем синхронно
                        World partiallyCreatedWorld = Bukkit.getWorld(creativeWorld.getWorldName());
                        if (partiallyCreatedWorld != null) {
                            // Reduced logging - only log when debugging
                            // getPlugin().getLogger().info("Попытка очистки частично созданного мира: " + partiallyCreatedWorld.getName());

                            // Кикаем всех игроков (хотя их там быть не должно)
                            for (Player p : partiallyCreatedWorld.getPlayers()) {
                                p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                                p.sendMessage("§cМир, в котором вы находились, не смог создаться и был удален.");
                            }
                            
                            // Выгружаем мир
                            if (!Bukkit.unloadWorld(partiallyCreatedWorld, false)) {
                                getPlugin().getLogger().warning("Не удалось выгрузить частично созданный мир для удаления.");
                                return; // Дальше нет смысла, так как файлы заблокированы
                            }
                            
                            // Удаляем файлы мира асинхронно, чтобы не тормозить сервер
                            File worldFolder = partiallyCreatedWorld.getWorldFolder();
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                try {
                                    deleteFolder(worldFolder);
                                    // Reduced logging - only log when debugging
                                    // getPlugin().getLogger().info("Файлы поврежденного мира удалены: " + worldFolder.getName());
                                } catch (Exception deleteEx) {
                                    getPlugin().getLogger().severe("Не удалось удалить файлы поврежденного мира: " + deleteEx.getMessage());
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
                // 🔧 FIX: Add proper flat world generator settings to prevent "No key layers" error
                creator.generatorSettings("{\"layers\":[{\"block\":\"bedrock\",\"height\":1},{\"block\":\"stone\",\"height\":2},{\"block\":\"grass_block\",\"height\":1}],\"biome\":\"plains\"}");
                break;
            case VOID:
                creator.type(org.bukkit.WorldType.FLAT);
                creator.generateStructures(false);
                // Настройка генератора для создания только спавн платформы (современный JSON формат)
                creator.generatorSettings("{\"layers\":[{\"block\":\"bedrock\",\"height\":1},{\"block\":\"stone\",\"height\":2},{\"block\":\"grass_block\",\"height\":1}],\"biome\":\"plains\"}");
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
    
    @Override
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
                bukkitWorld.getPlayers().forEach(p -> p.teleport(getPlugin().getServer().getWorlds().get(0).getSpawnLocation()));
            }
            unloadedMain = Bukkit.unloadWorld(bukkitWorld, false); // Сохранять изменения в момент удаления - не всегда хорошая идея, т.к. может сохранить ошибки
            if (!unloadedMain) {
                getPlugin().getLogger().warning("Failed to unload main world: " + world.getWorldName() + ". Files might be locked.");
                requester.sendMessage("§cНе удалось полностью выгрузить основной мир. Возможно, требуется перезагрузка сервера.");
                return; // Не можем удалить файлы, если мир не выгружен
            }
        }

        boolean unloadedDev = true;
        World devWorld = Bukkit.getWorld(world.getDevWorldName());
        if (devWorld != null) {
            if (!devWorld.getPlayers().isEmpty()) { // Кикаем игроков
                devWorld.getPlayers().forEach(p -> p.teleport(getPlugin().getServer().getWorlds().get(0).getSpawnLocation()));
            }
            unloadedDev = Bukkit.unloadWorld(devWorld, false);
             if (!unloadedDev) {
                getPlugin().getLogger().warning("Failed to unload dev world: " + world.getDevWorldName() + ". Files might be locked.");
                requester.sendMessage("§cНе удалось полностью выгрузить мир разработки. Возможно, требуется перезагрузка сервера.");
                return; // Не можем удалить файлы, если мир не выгружен
            }
        }

        // 🔧 FIX: Condition !unloadedMain || !unloadedDev is always false
        // 🔧 ИСПРАВЛЕНИЕ: Условие !unloadedMain || !unloadedDev всегда ложно
        // 🔧 FIX: Bedingung !unloadedMain || !unloadedDev ist immer falsch
        // Removed unnecessary condition check as it's always false

        // Удаление из памяти
        worlds.remove(worldId);
        if (playerWorlds.containsKey(world.getOwnerId())) {
            List<String> playerWorldList = playerWorlds.get(world.getOwnerId());
            playerWorldList.remove(worldId);
            // If the player has no more worlds, remove the entry entirely
            if (playerWorldList.isEmpty()) {
                playerWorlds.remove(world.getOwnerId());
            }
        }
        if (codingManager != null) {
            codingManager.unloadScriptsForWorld(world);
        }
        // Также очистите все связанные блоки кодинга, если они хранятся вне самого мира.
        if (plugin instanceof MegaCreative) {
            MegaCreative megaPlugin = (MegaCreative) plugin;
            com.megacreative.coding.BlockPlacementHandler blockPlacementHandler = megaPlugin.getServiceRegistry().getBlockPlacementHandler();
            World worldToRemove = Bukkit.getWorld(world.getWorldName());
            World devWorldToRemove = Bukkit.getWorld(world.getDevWorldName());
            
            if (blockPlacementHandler != null && worldToRemove != null) {
                blockPlacementHandler.clearAllCodeBlocksInWorld(worldToRemove);
            }
            
            if (blockPlacementHandler != null && devWorldToRemove != null) {
                blockPlacementHandler.clearAllCodeBlocksInWorld(devWorldToRemove);
            }
        }

        // Удаление файлов мира - асинхронно
        // Переместим deleteWorldFiles(world); сюда:
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> deleteWorldFilesInternal(world, requester));

        requester.sendMessage("§aМир '" + world.getName() + "' успешно помечен к удалению файлов!");
        requester.sendMessage("§7Файлы мира будут удалены в фоновом режиме.");
    }
    
    private void deleteWorldFilesInternal(CreativeWorld world, Player requester) {
        File worldFolder = new File(Bukkit.getWorldContainer(), world.getWorldName());
        File devWorldFolder = new File(Bukkit.getWorldContainer(), world.getDevWorldName());
        File dataFile = new File(getPlugin().getDataFolder(), "worlds/" + world.getId() + ".json");
        
        try {
            // Try to delete main world folder
            boolean successMain = deleteFolderRecursive(worldFolder);
            if (!successMain) {
                // Try again after a short delay
                try {
                    Thread.sleep(100);
                    successMain = deleteFolderRecursive(worldFolder);
                } catch (InterruptedException ignored) {}
            }
            
            // Try to delete dev world folder
            boolean successDev = deleteFolderRecursive(devWorldFolder);
            if (!successDev) {
                // Try again after a short delay
                try {
                    Thread.sleep(100);
                    successDev = deleteFolderRecursive(devWorldFolder);
                } catch (InterruptedException ignored) {}
            }
            
            // Try to delete data file
            boolean successDataFile = true;
            if (dataFile.exists()) {
                successDataFile = dataFile.delete();
                if (!successDataFile) {
                    // Try again after a short delay
                    try {
                        Thread.sleep(100);
                        successDataFile = dataFile.delete();
                    } catch (InterruptedException ignored) {}
                }
            }
            
            if (successMain && successDev && successDataFile) {
                // Reduced logging - only log when debugging
                // getPlugin().getLogger().info("Successfully deleted world files for world ID " + world.getId());
                requester.sendMessage("§a✓ Файлы мира '" + world.getName() + "' полностью удалены.");
            } else {
                getPlugin().getLogger().warning("Failed to fully delete world files for world ID " + world.getId() + 
                                            ". Main: " + successMain + ", Dev: " + successDev + ", Data: " + successDataFile);
                requester.sendMessage("§c⚠ Ошибка удаления всех файлов мира. Возможно, они были заблокированы. Проверьте логи сервера.");
                
                // Try one more time with more aggressive approach
                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                    if (worldFolder.exists()) deleteFolderRecursive(worldFolder);
                    if (devWorldFolder.exists()) deleteFolderRecursive(devWorldFolder);
                    if (dataFile.exists()) dataFile.delete();
                }, 20L); // Run after 1 second
            }
        } catch (Exception e) {
            getPlugin().getLogger().severe("Error deleting world files for world ID " + world.getId() + ": " + e.getMessage());
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
            getPlugin().getLogger().severe("Failed to delete " + folder.getName() + ": " + e.getMessage());
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
    
    @Override
    public CreativeWorld getWorld(String id) {
        return worlds.get(id);
    }
    
    /**
     * Получает все творческие миры
     * @return Список всех творческих миров
     */
    @Override
    public List<CreativeWorld> getCreativeWorlds() {
        return new ArrayList<>(worlds.values());
    }
    
    /**
     * 🎆 ENHANCED: Reference system-style world pairing and switching methods
     */
    @Override
    public CreativeWorld getPairedWorld(CreativeWorld world) {
        if (world.getPairedWorldId() != null) {
            return getWorld(world.getPairedWorldId());
        }
        return null;
    }
    
    // 🎆 ENHANCED: Add missing switchToDevWorld method for proper dev mode switching
    @Override
    public void switchToDevWorld(Player player, String worldId) {
        CreativeWorld world = getWorld(worldId);
        if (world == null) {
            player.sendMessage("§cМир не найден!");
            return;
        }
        
        if (!world.canCode(player)) {
            player.sendMessage("§cУ вас нет прав на кодирование в этом мире!");
            return;
        }
        
        // Set mode to DEV
        world.setMode(com.megacreative.models.WorldMode.DEV);
        
        // Set player mode to DEV
        if (plugin instanceof MegaCreative) {
            PlayerModeManager modeManager = ((MegaCreative) plugin).getServiceRegistry().getPlayerModeManager();
            modeManager.setMode(player, PlayerModeManager.PlayerMode.DEV);
        }
        
        String devWorldName = world.isDevWorld() ? world.getWorldName() : world.getDevWorldName();
        World bukkitWorld = Bukkit.getWorld(devWorldName);
        
        if (bukkitWorld == null) {
            // Create dev world if it doesn't exist
            createDevWorldIfNotExists(world);
            bukkitWorld = Bukkit.getWorld(devWorldName);
        }
        
        if (bukkitWorld != null) {
            player.teleport(bukkitWorld.getSpawnLocation());
            player.setGameMode(org.bukkit.GameMode.CREATIVE);
            
            // Выдаем блоки кодирования ДИНАМИЧЕСКИ
            com.megacreative.coding.CodingItems.giveCodingItems(player, (MegaCreative) plugin);
            
            player.sendMessage("§a🎮 Переключение в режим разработки!");
            player.sendMessage("§7✅ Код включен, скрипты будут выполняться");
            player.sendMessage("§7Креатив для кодирования");
            
            // 🎆 ENHANCED: Track world mode switch
            if (plugin instanceof MegaCreative) {
                ((MegaCreative) plugin).getServiceRegistry().getPlayerManager().trackPlayerWorldEntry(player, worldId, "DEV");
            }
            
            saveWorld(world);
        }
    }
    
    @Override
    public void switchToPlayWorld(Player player, String worldId) {
        CreativeWorld world = getWorld(worldId);
        if (world == null) {
            player.sendMessage("§cWorld not found!");
            return;
        }
        
        // Set mode to PLAY
        world.setMode(com.megacreative.models.WorldMode.PLAY);
        
        // Set player mode to PLAY
        if (plugin instanceof MegaCreative) {
            PlayerModeManager modeManager = ((MegaCreative) plugin).getServiceRegistry().getPlayerModeManager();
            modeManager.setMode(player, PlayerModeManager.PlayerMode.PLAY);
        }
        
        // Get the correct play world name based on dual mode
        String playWorldName;
        if (world.getDualMode() == CreativeWorld.WorldDualMode.STANDALONE) {
            // For standalone worlds, use the main world name
            playWorldName = world.getWorldName();
        } else if (world.getDualMode() == CreativeWorld.WorldDualMode.DEV) {
            // If this is a dev world, get the paired play world
            if (world.getPairedWorldId() != null) {
                CreativeWorld pairedWorld = getWorld(world.getPairedWorldId());
                if (pairedWorld != null) {
                    playWorldName = pairedWorld.getWorldName();
                } else {
                    // Fallback to generated play world name
                    playWorldName = world.getPlayWorldName();
                }
            } else {
                // Fallback to generated play world name
                playWorldName = world.getPlayWorldName();
            }
        } else if (world.getDualMode() == CreativeWorld.WorldDualMode.PLAY) {
            // This is already a play world
            playWorldName = world.getWorldName();
        } else {
            // Fallback to generated play world name
            playWorldName = world.getPlayWorldName();
        }
        
        World bukkitWorld = Bukkit.getWorld(playWorldName);
        
        if (bukkitWorld == null) {
            player.sendMessage("§cPlay world does not exist! Creating it...");
            // Try to create it
            WorldCreator creator = new WorldCreator(playWorldName);
            creator.environment(world.getWorldType().getEnvironment());
            
            // Copy from dev world if it exists
            String devWorldName = world.getDevWorldName();
            World devWorld = Bukkit.getWorld(devWorldName);
            if (devWorld != null) {
                creator.copy(devWorld);
            } else {
                // Set appropriate world type
                switch (world.getWorldType()) {
                    case FLAT -> {
                        creator.type(WorldType.FLAT);
                        creator.generatorSettings("{\"layers\":[{\"block\":\"bedrock\",\"height\":1},{\"block\":\"stone\",\"height\":2},{\"block\":\"grass_block\",\"height\":1}],\"biome\":\"plains\"}");
                    }
                    case VOID -> {
                        creator.type(WorldType.FLAT);
                        creator.generatorSettings("{\"layers\":[{\"block\":\"air\",\"height\":1}],\"biome\":\"plains\"}");
                    }
                    case OCEAN -> creator.type(WorldType.NORMAL);
                    case NETHER -> creator.environment(World.Environment.NETHER);
                    case END -> creator.environment(World.Environment.THE_END);
                    default -> creator.type(WorldType.NORMAL);
                }
            }
            bukkitWorld = Bukkit.createWorld(creator);
            
            if (bukkitWorld == null) {
                player.sendMessage("§cFailed to create play world!");
                return;
            }
        }
        
        // Teleport player to play world
        player.teleport(bukkitWorld.getSpawnLocation());
        player.setGameMode(org.bukkit.GameMode.ADVENTURE); // Play mode should be adventure
        player.getInventory().clear(); // Clear inventory for play mode
        
        player.sendMessage("§a🎮 Switched to play mode!");
        
        // 🎆 ENHANCED: Track world mode switch
        if (plugin instanceof MegaCreative) {
            ((MegaCreative) plugin).getServiceRegistry().getPlayerManager().trackPlayerWorldEntry(player, worldId, "PLAY");
        }
        
        // Save world state
        saveWorld(world);
    }
    
    // 🎆 ENHANCED: Add missing switchToBuildWorld method for proper build mode switching
    @Override
    public void switchToBuildWorld(Player player, String worldId) {
        CreativeWorld world = getWorld(worldId);
        if (world == null) {
            player.sendMessage("§cWorld not found!");
            return;
        }
        
        // Check permissions
        if (!world.canEdit(player)) {
            player.sendMessage("§cYou don't have permission to edit this world!");
            return;
        }
        
        // Set mode to BUILD
        world.setMode(com.megacreative.models.WorldMode.BUILD);
        
        String devWorldName = world.isDevWorld() ? world.getWorldName() : world.getDevWorldName();
        World bukkitWorld = Bukkit.getWorld(devWorldName);
        
        if (bukkitWorld == null) {
            // Create dev world if it doesn't exist
            createDevWorldIfNotExists(world);
            bukkitWorld = Bukkit.getWorld(devWorldName);
        }
        
        if (bukkitWorld != null) {
            player.teleport(bukkitWorld.getSpawnLocation());
            player.setGameMode(org.bukkit.GameMode.CREATIVE);
            player.sendMessage("§aWorld mode changed to §f§lBUILD§a!");
            player.sendMessage("§7❌ Code disabled, scripts will not execute");
            player.sendMessage("§7Creative mode for builders");
            
            // 🎆 ENHANCED: Track world mode switch
            if (plugin instanceof MegaCreative) {
                ((MegaCreative) plugin).getServiceRegistry().getPlayerManager().trackPlayerWorldEntry(player, worldId, "BUILD");
            }
            
            saveWorld(world);
        } else {
            player.sendMessage("§cFailed to switch to build world!");
        }
    }
    
    private void createDevWorldIfNotExists(CreativeWorld world) {
        String devWorldName = world.getDevWorldName();
        if (Bukkit.getWorld(devWorldName) == null) {
            plugin.getLogger().info("Development world " + devWorldName + " does not exist. Generating a new one...");

            // ИСПОЛЬЗУЕМ ПРАВИЛЬНЫЙ ГЕНЕРАТОР!
            WorldCreator creator = new WorldCreator(devWorldName);
            creator.environment(World.Environment.NORMAL); // Обязательно для кастомных генераторов
            creator.generator(new DevWorldGenerator()); // Указываем наш генератор!
            
            // Удаляем generatorSettings, так как они конфликтуют с кастомным генератором
            // creator.generatorSettings("..."); // ЭТА СТРОКА БОЛЬШЕ НЕ НУЖНА

            World devWorld = creator.createWorld();
            if (devWorld != null) {
                setupDevWorld(devWorld, world);
                
                // ВАЖНО: Обновляем статус основного мира, чтобы он знал о своей паре
                world.setDualMode(CreativeWorld.WorldDualMode.PLAY); // Исходный мир теперь считается игровым
                world.setPairedWorldId(world.getId()); // ID остается тот же, но теперь он знает о паре
                saveWorld(world);
                
                plugin.getLogger().info("Successfully generated dev world: " + devWorld.getName());
            } else {
                plugin.getLogger().severe("CRITICAL: Failed to create development world " + devWorldName);
            }
        }
    }
    
    private void setupDevWorld(World devWorld, CreativeWorld creativeWorld) {
        // Enhanced setup for development world
        setupWorld(devWorld, creativeWorld);
        
        // Additional dev world specific settings
        devWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false); // No mobs in dev mode
        devWorld.setGameRule(GameRule.KEEP_INVENTORY, true); // Keep items on death
        devWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true); // Instant respawn
    }
    
    @Override
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
    
    @Override
    public CreativeWorld findCreativeWorldByBukkit(World bukkitWorld) {
        if (bukkitWorld == null) return null;
        
        String worldName = bukkitWorld.getName();
        // Reduced logging - only log when debugging
        // getPlugin().getLogger().info("Looking for CreativeWorld for Bukkit world: " + worldName);
        // getPlugin().getLogger().info("Available worlds in memory: " + worlds.size());
        
        // Handle old-style megacreative_ naming
        if (worldName.startsWith("megacreative_")) {
            // 🔧 FIX: More precise ID extraction for complex naming
            // Extract everything between "megacreative_" and the first suffix
            int startIndex = "megacreative_".length();
            int endIndex = worldName.length();
            
            // Find the first suffix
            int codeIndex = worldName.indexOf("-code");
            int worldIndex = worldName.indexOf("-world");
            int devIndex = worldName.indexOf("_dev");
            
            if (codeIndex != -1 && codeIndex < endIndex) endIndex = codeIndex;
            if (worldIndex != -1 && worldIndex < endIndex) endIndex = worldIndex;
            if (devIndex != -1 && devIndex < endIndex) endIndex = devIndex;
            
            if (startIndex < endIndex) {
                String id = worldName.substring(startIndex, endIndex);
                return worlds.get(id);
            }
        }
        
        // Handle new reference system-style naming
        if (worldName.endsWith("-world") || worldName.endsWith("-code")) {
            // Extract the base name
            String baseName = worldName.substring(0, worldName.length() - 6);
            for (CreativeWorld world : worlds.values()) {
                if (world.getDevWorldName().equals(worldName) || world.getPlayWorldName().equals(worldName)) {
                    return world;
                }
            }
        }
        
        // If nothing found, return null
        return null;
    }
    
    @Override
    public List<CreativeWorld> getPlayerWorlds(Player player) {
        List<CreativeWorld> playerWorldsList = new ArrayList<>();
        List<String> worldIds = playerWorlds.get(player.getUniqueId());
        
        if (worldIds != null) {
            for (String worldId : worldIds) {
                CreativeWorld world = worlds.get(worldId);
                if (world != null) {
                    playerWorldsList.add(world);
                }
            }
        }
        
        return playerWorldsList;
    }
    
    @Override
    public List<CreativeWorld> getAllPublicWorlds() {
        List<CreativeWorld> publicWorlds = new ArrayList<>();
        for (CreativeWorld world : worlds.values()) {
            // A world is public if it's not private
            if (!world.isPrivate()) {
                publicWorlds.add(world);
            }
        }
        return publicWorlds;
    }
    
    @Override
    public void shutdown() {
        // Save all worlds before shutdown
        saveAllWorlds();
        
        // Clear collections
        worlds.clear();
        playerWorlds.clear();
    }
}