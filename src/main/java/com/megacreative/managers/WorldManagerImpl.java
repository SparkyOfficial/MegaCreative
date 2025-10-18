package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeHandler;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.interfaces.ICodingManager;
import com.megacreative.models.*;
import com.megacreative.utils.ConfigManager;
import com.megacreative.utils.JsonSerializer;
import com.megacreative.worlds.DevWorldGenerator;
import com.megacreative.coding.activators.ActivatorManager;
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
        
        
        if (configManager != null) {
            this.maxWorldsPerPlayer = configManager.getMaxWorldsPerPlayer();
            this.worldBorderSize = configManager.getWorldBorderSize();
        } else {
            this.maxWorldsPerPlayer = 5; 
            this.worldBorderSize = 300; 
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
        this.maxWorldsPerPlayer = 5; 
        this.worldBorderSize = 300; 
    }
    
    /**
     * Constructor for ServiceRegistry (uses ConfigManager and Plugin)
     *
     * Конструктор для ServiceRegistry (использует ConfigManager и Plugin)
     *
     * Konstruktor für ServiceRegistry (verwendet ConfigManager und Plugin)
     */
    public WorldManagerImpl(ConfigManager configManager, MegaCreative plugin) {
        this.plugin = plugin; 
        this.codingManager = null; 
        this.configManager = configManager;
        this.worlds = new HashMap<>();
        this.playerWorlds = new HashMap<>();
        
        
        if (configManager != null) {
            this.maxWorldsPerPlayer = configManager.getMaxWorldsPerPlayer();
            this.worldBorderSize = configManager.getWorldBorderSize();
        } else {
            this.maxWorldsPerPlayer = 5; 
            this.worldBorderSize = 300; 
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
        this.plugin = null; 
        this.codingManager = null; 
        this.configManager = configManager;
        this.worlds = new HashMap<>();
        this.playerWorlds = new HashMap<>();
        
        
        if (configManager != null) {
            this.maxWorldsPerPlayer = configManager.getMaxWorldsPerPlayer();
            this.worldBorderSize = configManager.getWorldBorderSize();
        } else {
            this.maxWorldsPerPlayer = 5; 
            this.worldBorderSize = 300; 
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
        
        Plugin plugin = getPlugin();
        if (plugin == null) {
            return;
        }
        
        try {
            
            
            
            
            loadWorlds();
            
            
            
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
                if (!worldsDir.mkdirs()) {
                    plugin.getLogger().severe("Failed to create worlds directory: " + worldsDir.getAbsolutePath());
                    return;
                }
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
            
            String jsonContent = new String(java.nio.file.Files.readAllBytes(worldFile.toPath()));
            
            
            CreativeWorld world = com.megacreative.utils.JsonSerializer.deserializeWorld(jsonContent, (MegaCreative) plugin);
            
            if (world != null) {
                
                MegaCreative megaPlugin = (MegaCreative) plugin;
                ScriptEngine scriptEngine = megaPlugin.getServiceRegistry().getScriptEngine();
                world.setScriptEngine(scriptEngine);

                
                worlds.put(world.getId(), world);
                
                
                playerWorlds.computeIfAbsent(world.getOwnerId(), k -> new ArrayList<>()).add(world.getId());
                
                
                if (codingManager != null) {
                    codingManager.loadScriptsForWorld(world);
                }
                
                
                ActivatorManager activatorManager = megaPlugin.getServiceRegistry().getActivatorManager();
                if (activatorManager != null && world.getCodeHandler() != null) {
                    activatorManager.registerCodeHandler(world.getId(), world.getCodeHandler());
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
        
        
        if (name.length() < 3 || name.length() > 20) {
            return false;
        }
        
        
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
            
            String json = com.megacreative.utils.JsonSerializer.serializeWorld(world);
            
            
            File worldsDir = new File(plugin.getDataFolder(), "worlds");
            if (!worldsDir.exists()) {
                if (!worldsDir.mkdirs()) {
                    plugin.getLogger().warning("Failed to create worlds directory: " + worldsDir.getAbsolutePath());
                }
            }
            
            File worldFile = new File(worldsDir, world.getId() + ".json");
            java.nio.file.Files.write(worldFile.toPath(), json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            
            if (codingManager != null) {
                
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
        
        if (!isValidWorldName(name)) {
            player.sendMessage("§cНекорректное имя мира!");
            player.sendMessage("§7Имя должно содержать 3-20 символов (буквы, цифры, подчеркивания)");
            return;
        }
        
        
        if (worldExists(name)) {
            player.sendMessage("§cМир с таким именем уже существует!");
            return;
        }
        
        
        if (getPlayerWorldCount(player) >= maxWorldsPerPlayer) {
            player.sendMessage("§cВы достигли лимита в " + maxWorldsPerPlayer + " миров.");
            return;
        }

        player.sendMessage("§eПодготовка к созданию мира '" + name + "'...");

        
        String worldId = generateUniqueId();
        CreativeWorld creativeWorld = new CreativeWorld(worldId, name, player.getUniqueId(), player.getName(), worldType);
        
        
        creativeWorld.setDualMode(dualMode);
        if (pairedWorldId != null) {
            creativeWorld.setPairedWorldId(pairedWorldId);
        }

        
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                
                player.closeInventory();
                player.sendMessage("§eСоздание мира... Пожалуйста, подождите.");
                World newWorld = createMinecraftWorld(creativeWorld);

                if (newWorld != null) {
                    
                    setupWorld(newWorld, creativeWorld);

                    
                    if (plugin instanceof MegaCreative) {
                        MegaCreative megaPlugin = (MegaCreative) plugin;
                        ScriptEngine scriptEngine = megaPlugin.getServiceRegistry().getScriptEngine();
                        creativeWorld.setScriptEngine(scriptEngine);
                    }

                    
                    worlds.put(worldId, creativeWorld);
                    playerWorlds.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(worldId);

                    
                    if (codingManager != null) {
                        codingManager.loadScriptsForWorld(creativeWorld);
                    }
                    
                    
                    MegaCreative megaPlugin = (MegaCreative) plugin;
                    CodeHandler codeHandler = new CodeHandler(megaPlugin, creativeWorld);
                    creativeWorld.setCodeHandler(codeHandler);
                    
                    
                    ActivatorManager activatorManager = megaPlugin.getServiceRegistry().getActivatorManager();
                    if (activatorManager != null) {
                        activatorManager.registerCodeHandler(worldId, codeHandler);
                    }
                    
                    
                    player.teleport(newWorld.getSpawnLocation());
                    player.sendMessage("§aМир '" + name + "' успешно создан!");

                    
                    saveWorldAsync(creativeWorld, player);
                    
                } else {
                    throw new RuntimeException("Не удалось создать мир (Bukkit.createWorld вернул null)");
                }
            } catch (Exception e) {
                getPlugin().getLogger().severe("Критическая ошибка при создании мира: " + e.getMessage());
                getPlugin().getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
                player.sendMessage("§cПроизошла ошибка при создании мира. Пожалуйста, обратитесь к администратору.");

                
                if (creativeWorld.getWorldName() != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> { 
                        World partiallyCreatedWorld = Bukkit.getWorld(creativeWorld.getWorldName());
                        if (partiallyCreatedWorld != null) {
                            
                            

                            
                            for (Player p : partiallyCreatedWorld.getPlayers()) {
                                p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                                p.sendMessage("§cМир, в котором вы находились, не смог создаться и был удален.");
                            }
                            
                            
                            if (!Bukkit.unloadWorld(partiallyCreatedWorld, false)) {
                                getPlugin().getLogger().warning("Не удалось выгрузить частично созданный мир для удаления.");
                                return; 
                            }
                            
                            
                            File worldFolder = partiallyCreatedWorld.getWorldFolder();
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                try {
                                    deleteFolder(worldFolder);
                                    
                                    
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
        
        
        switch (creativeWorld.getWorldType()) {
            case FLAT:
                creator.type(org.bukkit.WorldType.FLAT);
                
                creator.generatorSettings("{\"layers\":[{\"block\":\"bedrock\",\"height\":1},{\"block\":\"stone\",\"height\":2},{\"block\":\"grass_block\",\"height\":1}],\"biome\":\"plains\"}");
                break;
            case VOID:
                creator.type(org.bukkit.WorldType.FLAT);
                creator.generateStructures(false);
                
                creator.generatorSettings("{\"layers\":[{\"block\":\"bedrock\",\"height\":1},{\"block\":\"stone\",\"height\":2},{\"block\":\"grass_block\",\"height\":1}],\"biome\":\"plains\"}");
                break;
            case OCEAN:
                creator.type(org.bukkit.WorldType.NORMAL);
                break;
            default:
                creator.type(org.bukkit.WorldType.NORMAL);
                break;
        }
        
        
        
        return Bukkit.createWorld(creator);
    }
    
    private void setupWorld(World world, CreativeWorld creativeWorld) {
        
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(worldBorderSize);
        border.setWarningDistance(10);
        
        
        WorldFlags flags = creativeWorld.getFlags();
        world.setGameRule(GameRule.DO_MOB_SPAWNING, flags.isMobSpawning());
        world.setGameRule(GameRule.DO_FIRE_TICK, flags.isFireSpread());
        world.setGameRule(GameRule.MOB_GRIEFING, flags.isMobGriefing());
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, flags.isWeatherCycle());
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, flags.isDayNightCycle());
        world.setPVP(flags.isPvp());
        
        
        world.setSpawnLocation(0, world.getHighestBlockYAt(0, 0) + 1, 0);
    }
    
    @Override
    public void deleteWorld(String worldId, Player requester) {
        CreativeWorld world = worlds.get(worldId);
        if (world == null || !world.isOwner(requester)) {
            return;
        }
        
        
        World bukkitWorld = Bukkit.getWorld(world.getWorldName());
        if (bukkitWorld != null) {
            if (!bukkitWorld.getPlayers().isEmpty()) { 
                bukkitWorld.getPlayers().forEach(p -> p.teleport(getPlugin().getServer().getWorlds().get(0).getSpawnLocation()));
            }
            boolean unloadedMain = Bukkit.unloadWorld(bukkitWorld, false); 
            if (!unloadedMain) {
                getPlugin().getLogger().warning("Failed to unload main world: " + world.getWorldName() + ". Files might be locked.");
                requester.sendMessage("§cНе удалось полностью выгрузить основной мир. Возможно, требуется перезагрузка сервера.");
                return; 
            }
        }

        World devWorld = Bukkit.getWorld(world.getDevWorldName());
        if (devWorld != null) {
            if (!devWorld.getPlayers().isEmpty()) { 
                devWorld.getPlayers().forEach(p -> p.teleport(getPlugin().getServer().getWorlds().get(0).getSpawnLocation()));
            }
            boolean unloadedDev = Bukkit.unloadWorld(devWorld, false);
             if (!unloadedDev) {
                getPlugin().getLogger().warning("Failed to unload dev world: " + world.getDevWorldName() + ". Files might be locked.");
                requester.sendMessage("§cНе удалось полностью выгрузить мир разработки. Возможно, требуется перезагрузка сервера.");
                return; 
            }
        }

        
        worlds.remove(worldId);
        if (playerWorlds.containsKey(world.getOwnerId())) {
            List<String> playerWorldList = playerWorlds.get(world.getOwnerId());
            playerWorldList.remove(worldId);
            
            if (playerWorldList.isEmpty()) {
                playerWorlds.remove(world.getOwnerId());
            }
        }
        if (codingManager != null) {
            codingManager.unloadScriptsForWorld(world);
        }
        
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
        
        
        com.megacreative.coding.activators.ActivatorManager activatorManager = megaPlugin.getServiceRegistry().getActivatorManager();
        if (activatorManager != null) {
            activatorManager.unregisterCodeHandler(worldId);
        }

        
        
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> deleteWorldFilesInternal(world, requester));

        requester.sendMessage("§aМир '" + world.getName() + "' успешно помечен к удалению файлов!");
        requester.sendMessage("§7Файлы мира будут удалены в фоновом режиме.");
    }
    
    private void deleteWorldFilesInternal(CreativeWorld world, Player requester) {
        File worldFolder = new File(Bukkit.getWorldContainer(), world.getWorldName());
        File devWorldFolder = new File(Bukkit.getWorldContainer(), world.getDevWorldName());
        File dataFile = new File(getPlugin().getDataFolder(), "worlds/" + world.getId() + ".json");
        
        try {
            
            boolean successMain = deleteFolderRecursive(worldFolder);
            if (!successMain) {
                
                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                    boolean retrySuccess = deleteFolderRecursive(worldFolder);
                    if (!retrySuccess) {
                        getPlugin().getLogger().warning("Failed to delete main world folder after retry: " + world.getWorldName());
                    }
                    
                    attemptDeleteDevWorld(world, devWorldFolder, dataFile, requester);
                }, 20L); 
                return;
            }
            
            
            attemptDeleteDevWorld(world, devWorldFolder, dataFile, requester);
        } catch (Exception e) {
            getPlugin().getLogger().severe("Error deleting world files for world ID " + world.getId() + ": " + e.getMessage());
            requester.sendMessage("§c❌ Непредвиденная ошибка при удалении файлов мира: " + e.getMessage());
        }
    }
    
    private void attemptDeleteDevWorld(CreativeWorld world, File devWorldFolder, File dataFile, Player requester) {
        try {
            
            boolean successDev = deleteFolderRecursive(devWorldFolder);
            if (!successDev) {
                
                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                    boolean retrySuccess = deleteFolderRecursive(devWorldFolder);
                    if (!retrySuccess) {
                        getPlugin().getLogger().warning("Failed to delete dev world folder after retry: " + world.getDevWorldName());
                    }
                    
                    attemptDeleteDataFile(world, dataFile, requester);
                }, 20L); 
                return;
            }
            
            
            attemptDeleteDataFile(world, dataFile, requester);
        } catch (Exception e) {
            getPlugin().getLogger().severe("Error deleting dev world files for world ID " + world.getId() + ": " + e.getMessage());
            requester.sendMessage("§c❌ Ошибка при удалении файлов dev мира: " + e.getMessage());
        }
    }
    
    private void attemptDeleteDataFile(CreativeWorld world, File dataFile, Player requester) {
        try {
            
            // Value successDataFile is always 'true'
            // Removed redundant assignment since we're checking the result of dataFile.delete()
            boolean successDataFile = true;
            if (dataFile.exists()) {
                successDataFile = dataFile.delete();
                if (!successDataFile) {
                    
                    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                        boolean retrySuccess = dataFile.delete();
                        if (!retrySuccess) {
                            getPlugin().getLogger().warning("Failed to delete data file after retry: " + dataFile.getName());
                        }
                        
                        finishWorldDeletion(world, requester, true, true, retrySuccess);
                    }, 20L); 
                    return;
                }
            }
            
            
            finishWorldDeletion(world, requester, true, true, successDataFile);
        } catch (Exception e) {
            getPlugin().getLogger().severe("Error deleting data file for world ID " + world.getId() + ": " + e.getMessage());
            requester.sendMessage("§c❌ Ошибка при удалении файла данных: " + e.getMessage());
        }
    }
    
    private void finishWorldDeletion(CreativeWorld world, Player requester, boolean successMain, boolean successDev, boolean successDataFile) {
        if (successMain && successDev && successDataFile) {
            
            
            requester.sendMessage("§a✓ Файлы мира '" + world.getName() + "' полностью удалены.");
        } else {
            getPlugin().getLogger().warning("Failed to fully delete world files for world ID " + world.getId() + 
                                        ". Main: " + successMain + ", Dev: " + successDev + ", Data: " + successDataFile);
            requester.sendMessage("§c⚠ Ошибка удаления всех файлов мира. Возможно, они были заблокированы. Проверьте логи сервера.");
            
            
            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                File worldFolder = new File(Bukkit.getWorldContainer(), world.getWorldName());
                File devWorldFolder = new File(Bukkit.getWorldContainer(), world.getDevWorldName());
                File dataFile = new File(getPlugin().getDataFolder(), "worlds/" + world.getId() + ".json");
                
                if (worldFolder.exists()) {
                    deleteFolderRecursive(worldFolder);
                }
                if (devWorldFolder.exists()) {
                    deleteFolderRecursive(devWorldFolder);
                }
                if (dataFile.exists() && !dataFile.delete()) {
                    getPlugin().getLogger().warning("Failed to delete data file: " + dataFile.getAbsolutePath());
                }
            }, 20L); 
        }
    }

    
    private boolean deleteFolderRecursive(File folder) {
        if (!folder.exists()) {
            return true;
        }
        
        
        
        
        
        
        try {
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        deleteFolderRecursive(file); 
                    }
                }
            }
            
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
                        if (!file.delete()) {
                            getPlugin().getLogger().warning("Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
            if (!folder.delete()) {
                getPlugin().getLogger().warning("Failed to delete folder: " + folder.getAbsolutePath());
            }
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
        
        
        world.setMode(com.megacreative.models.WorldMode.DEV);
        
        
        if (plugin instanceof MegaCreative) {
            PlayerModeManager modeManager = ((MegaCreative) plugin).getServiceRegistry().getPlayerModeManager();
            modeManager.setMode(player, PlayerModeManager.PlayerMode.DEV);
        }
        
        String devWorldName = world.isDevWorld() ? world.getWorldName() : world.getDevWorldName();
        World bukkitWorld = Bukkit.getWorld(devWorldName);
        
        if (bukkitWorld == null) {
            
            createDevWorldIfNotExists(world);
            bukkitWorld = Bukkit.getWorld(devWorldName);
        }
        
        if (bukkitWorld != null) {
            player.teleport(bukkitWorld.getSpawnLocation());
            player.setGameMode(org.bukkit.GameMode.CREATIVE);
            
            
            com.megacreative.coding.CodingItems.giveCodingItems(player, (MegaCreative) plugin);
            
            player.sendMessage("§a🎮 Переключение в режим разработки!");
            player.sendMessage("§7✅ Код включен, скрипты будут выполняться");
            player.sendMessage("§7Креатив для кодирования");
            
            
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
        
        
        world.setMode(com.megacreative.models.WorldMode.PLAY);
        
        
        if (plugin instanceof MegaCreative) {
            PlayerModeManager modeManager = ((MegaCreative) plugin).getServiceRegistry().getPlayerModeManager();
            modeManager.setMode(player, PlayerModeManager.PlayerMode.PLAY);
        }
        
        
        String playWorldName;
        if (world.getDualMode() == CreativeWorld.WorldDualMode.STANDALONE) {
            
            playWorldName = world.getWorldName();
        } else if (world.getDualMode() == CreativeWorld.WorldDualMode.DEV) {
            
            if (world.getPairedWorldId() != null) {
                CreativeWorld pairedWorld = getWorld(world.getPairedWorldId());
                if (pairedWorld != null) {
                    playWorldName = pairedWorld.getWorldName();
                } else {
                    
                    playWorldName = world.getPlayWorldName();
                }
            } else {
                
                playWorldName = world.getPlayWorldName();
            }
        } else if (world.getDualMode() == CreativeWorld.WorldDualMode.PLAY) {
            
            playWorldName = world.getWorldName();
        } else {
            
            playWorldName = world.getPlayWorldName();
        }
        
        World bukkitWorld = Bukkit.getWorld(playWorldName);
        
        if (bukkitWorld == null) {
            player.sendMessage("§cPlay world does not exist! Creating it...");
            
            WorldCreator creator = new WorldCreator(playWorldName);
            creator.environment(world.getWorldType().getEnvironment());
            
            
            String devWorldName = world.getDevWorldName();
            World devWorld = Bukkit.getWorld(devWorldName);
            if (devWorld != null) {
                creator.copy(devWorld);
            } else {
                
                switch (world.getWorldType()) {
                    case FLAT:
                        creator.type(WorldType.FLAT);
                        creator.generatorSettings("{\"layers\":[{\"block\":\"bedrock\",\"height\":1},{\"block\":\"stone\",\"height\":2},{\"block\":\"grass_block\",\"height\":1}],\"biome\":\"plains\"}");
                        break;
                    case VOID:
                        creator.type(WorldType.FLAT);
                        creator.generatorSettings("{\"layers\":[{\"block\":\"air\",\"height\":1}],\"biome\":\"plains\"}");
                        break;
                    case OCEAN:
                        creator.type(WorldType.NORMAL);
                        break;
                    case NETHER:
                        creator.environment(World.Environment.NETHER);
                        break;
                    case END:
                        creator.environment(World.Environment.THE_END);
                        break;
                    default:
                        creator.type(WorldType.NORMAL);
                        break;
                }
            }
            bukkitWorld = Bukkit.createWorld(creator);
            
            if (bukkitWorld == null) {
                player.sendMessage("§cFailed to create play world!");
                return;
            }
        }
        
        
        player.teleport(bukkitWorld.getSpawnLocation());
        player.setGameMode(org.bukkit.GameMode.ADVENTURE); 
        player.getInventory().clear(); 
        
        player.sendMessage("§a🎮 Switched to play mode!");
        
        
        if (plugin instanceof MegaCreative) {
            ((MegaCreative) plugin).getServiceRegistry().getPlayerManager().trackPlayerWorldEntry(player, worldId, "PLAY");
        }
        
        
        saveWorld(world);
    }
    
    
    @Override
    public void switchToBuildWorld(Player player, String worldId) {
        CreativeWorld world = getWorld(worldId);
        if (world == null) {
            player.sendMessage("§cWorld not found!");
            return;
        }
        
        
        if (!world.canEdit(player)) {
            player.sendMessage("§cYou don't have permission to edit this world!");
            return;
        }
        
        
        world.setMode(com.megacreative.models.WorldMode.BUILD);
        
        String devWorldName = world.isDevWorld() ? world.getWorldName() : world.getDevWorldName();
        World bukkitWorld = Bukkit.getWorld(devWorldName);
        
        if (bukkitWorld == null) {
            
            createDevWorldIfNotExists(world);
            bukkitWorld = Bukkit.getWorld(devWorldName);
        }
        
        if (bukkitWorld != null) {
            player.teleport(bukkitWorld.getSpawnLocation());
            player.setGameMode(org.bukkit.GameMode.CREATIVE);
            
            
            com.megacreative.coding.CodingItems.giveCodingItems(player, (MegaCreative) plugin);
            
            player.sendMessage("§a🎮 Switched to build mode!");
            player.sendMessage("§7✅ Building enabled, scripts will execute");
            player.sendMessage("§7Creative mode for building");
            
            
            if (plugin instanceof MegaCreative) {
                ((MegaCreative) plugin).getServiceRegistry().getPlayerManager().trackPlayerWorldEntry(player, worldId, "BUILD");
            }
            
            saveWorld(world);
        }
    }
    
    /**
     * Creates a development world if it doesn't exist
     * @param world The creative world to create a dev world for
     */
    private void createDevWorldIfNotExists(CreativeWorld world) {
        String devWorldName = world.getDevWorldName();
        World bukkitWorld = Bukkit.getWorld(devWorldName);
        
        if (bukkitWorld == null) {
            WorldCreator creator = new WorldCreator(devWorldName);
            creator.environment(world.getWorldType().getEnvironment());
            
            // Use the DevWorldGenerator instead of flat world settings
            creator.generator(new com.megacreative.worlds.DevWorldGenerator());
            creator.generateStructures(false);
            
            bukkitWorld = Bukkit.createWorld(creator);
            
            if (bukkitWorld != null) {
                // Setup the world with proper spawn location
                bukkitWorld.setSpawnLocation(0, 65, 0);
                
                // Apply world settings
                setupWorld(bukkitWorld, world);
            }
        }
    }
    
    /**
     * Gets all public worlds
     * @return List of all public creative worlds
     */
    @Override
    public List<CreativeWorld> getAllPublicWorlds() {
        List<CreativeWorld> publicWorlds = new ArrayList<>();
        for (CreativeWorld world : worlds.values()) {
            if (world.isPublic()) {
                publicWorlds.add(world);
            }
        }
        return publicWorlds;
    }
    
    @Override
    public CreativeWorld getWorldByName(String name) {
        for (CreativeWorld world : worlds.values()) {
            if (world.getName().equalsIgnoreCase(name)) {
                return world;
            }
        }
        return null;
    }
    
    @Override
    public CreativeWorld findCreativeWorldByBukkit(World bukkitWorld) {
        if (bukkitWorld == null) return null;
        
        String worldName = bukkitWorld.getName();
        for (CreativeWorld world : worlds.values()) {
            if (world.getWorldName().equals(worldName) || world.getDevWorldName().equals(worldName)) {
                return world;
            }
        }
        return null;
    }
    
    @Override
    public List<CreativeWorld> getPlayerWorlds(Player player) {
        if (player == null) return new ArrayList<>();
        
        List<String> worldIds = playerWorlds.get(player.getUniqueId());
        if (worldIds == null) return new ArrayList<>();
        
        List<CreativeWorld> playerWorldsList = new ArrayList<>();
        for (String worldId : worldIds) {
            CreativeWorld world = worlds.get(worldId);
            if (world != null) {
                playerWorldsList.add(world);
            }
        }
        return playerWorldsList;
    }
    
    @Override
    public void shutdown() {
        
        saveAllWorlds();
        
        
        worlds.clear();
        playerWorlds.clear();
    }
}
