package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class WorldManager {
    
    private final MegaCreative plugin;
    private final Map<String, CreativeWorld> worlds;
    private final Map<UUID, List<String>> playerWorlds;
    private final int maxWorldsPerPlayer = 5;
    private final int worldBorderSize = 300;
    
    public WorldManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.worlds = new HashMap<>();
        this.playerWorlds = new HashMap<>();
        loadWorlds();
    }
    
    public void createWorld(Player player, String name, CreativeWorldType worldType) {
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
                    plugin.getCodingManager().loadScriptsForWorld(creativeWorld);
                    
                    // Телепортация - синхронно
                    player.teleport(newWorld.getSpawnLocation());
                    player.sendMessage("§aМир '" + name + "' успешно создан!");

                    // Асинхронное сохранение файла
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            saveWorld(creativeWorld);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Не удалось сохранить данные мира: " + e.getMessage());
                            // Уведомление об ошибке в главном потоке
                            Bukkit.getScheduler().runTask(plugin, () -> 
                                player.sendMessage("§cНе удалось сохранить данные мира. Обратитесь к администратору."));
                        }
                    });
                    
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
                creator.generateStructures(false);
                creator.type(org.bukkit.WorldType.FLAT);
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
        
        // Кик всех игроков из мира
        World bukkitWorld = Bukkit.getWorld(world.getWorldName());
        plugin.getCodingManager().unloadScriptsForWorld(world);

        if (bukkitWorld != null) {
            bukkitWorld.getPlayers().forEach(player -> 
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())
            );
            Bukkit.unloadWorld(bukkitWorld, false);
        }
        
        // Удаление dev мира
        World devWorld = Bukkit.getWorld(world.getDevWorldName());
        if (devWorld != null) {
            devWorld.getPlayers().forEach(player -> 
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())
            );
            Bukkit.unloadWorld(devWorld, false);
        }
        
        // Удаление из памяти
        worlds.remove(worldId);
        playerWorlds.get(world.getOwnerId()).remove(worldId);
        
        // Удаление файлов
        deleteWorldFiles(world);
    }
    
    private void deleteWorldFiles(CreativeWorld world) {
        File worldFolder = new File(Bukkit.getWorldContainer(), world.getWorldName());
        File devWorldFolder = new File(Bukkit.getWorldContainer(), world.getDevWorldName());
        File dataFile = new File(plugin.getDataFolder(), "worlds/" + world.getId() + ".yml");
        
        deleteFolder(worldFolder);
        deleteFolder(devWorldFolder);
        dataFile.delete();
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
    
    public void saveWorld(CreativeWorld world) {
        File dataFolder = new File(plugin.getDataFolder(), "worlds");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File worldFile = new File(dataFolder, world.getId() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        // Сохранение данных мира
        config.set("id", world.getId());
        config.set("name", world.getName());
        config.set("description", world.getDescription());
        config.set("owner.id", world.getOwnerId().toString());
        config.set("owner.name", world.getOwnerName());
        config.set("type", world.getWorldType().name());
        config.set("mode", world.getMode().name());
        config.set("private", world.isPrivate());
        config.set("created", world.getCreatedTime());
        config.set("lastActivity", world.getLastActivity());
        
        // Сохранение флагов
        config.set("flags.mobSpawning", world.getFlags().isMobSpawning());
        config.set("flags.pvp", world.getFlags().isPvp());
        config.set("flags.explosions", world.getFlags().isExplosions());
        
        // Сохранение доверенных игроков
        config.set("trusted.builders", world.getTrustedBuilders().stream().map(UUID::toString).toList());
        config.set("trusted.coders", world.getTrustedCoders().stream().map(UUID::toString).toList());
        
        // Сохранение статистики
        config.set("stats.likes", world.getLikes());
        config.set("stats.dislikes", world.getDislikes());

        // Сохранение скриптов
        List<Map<String, Object>> serializedScripts = new ArrayList<>();
        for (CodeScript script : world.getScripts()) {
            Map<String, Object> scriptMap = new HashMap<>();
            scriptMap.put("name", script.getName());
            scriptMap.put("enabled", script.isEnabled());
            scriptMap.put("rootBlock", serializeBlock(script.getRootBlock()));
            serializedScripts.add(scriptMap);
        }
        config.set("scripts", serializedScripts);
        
        try {
            config.save(worldFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сохранения мира " + world.getId() + ": " + e.getMessage());
        }
    }

    private Map<String, Object> serializeBlock(CodeBlock block) {
        if (block == null) return null;
        Map<String, Object> blockMap = new HashMap<>();
        blockMap.put("type", block.getType().name());
        blockMap.put("parameters", block.getParameters());
        blockMap.put("nextBlock", serializeBlock(block.getNextBlock()));
        
        // Сериализация дочерних блоков (список)
        List<Map<String, Object>> childrenList = new ArrayList<>();
        for (CodeBlock child : block.getChildren()) {
            Map<String, Object> childMap = serializeBlock(child);
            if (childMap != null) {
                childrenList.add(childMap);
            }
        }
        blockMap.put("children", childrenList);
        
        return blockMap;
    }

    private CodeBlock deserializeBlock(Map<String, Object> blockMap) {
        if (blockMap == null || blockMap.isEmpty()) return null;

        BlockType type = BlockType.valueOf((String) blockMap.get("type"));
        CodeBlock block = new CodeBlock(type);
        
        // Восстановление параметров
        Map<String, Object> parameters = (Map<String, Object>) blockMap.get("parameters");
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                block.setParameter(entry.getKey(), entry.getValue());
            }
        }

        // Восстановление следующего блока
        if (blockMap.containsKey("nextBlock")) {
            block.setNext(deserializeBlock((Map<String, Object>) blockMap.get("nextBlock")));
        }
        
        // Восстановление дочерних блоков
        if (blockMap.containsKey("children")) {
            List<Map<String, Object>> childrenList = (List<Map<String, Object>>) blockMap.get("children");
            for (Map<String, Object> childMap : childrenList) {
                CodeBlock child = deserializeBlock(childMap);
                if (child != null) {
                    block.addChild(child);
                }
            }
        }

        return block;
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
        
        String id = config.getString("id");
        String name = config.getString("name");
        UUID ownerId = UUID.fromString(config.getString("owner.id"));
        String ownerName = config.getString("owner.name");
        CreativeWorldType worldType = CreativeWorldType.valueOf(config.getString("type"));
        
        CreativeWorld world = new CreativeWorld(id, name, ownerId, ownerName, worldType);
        
        // Загрузка данных
        world.setDescription(config.getString("description", ""));
        world.setMode(WorldMode.valueOf(config.getString("mode", "BUILD")));
        world.setPrivate(config.getBoolean("private", false));
        world.setCreatedTime(config.getLong("created"));
        world.setLastActivity(config.getLong("lastActivity"));
        
        // Загрузка флагов
        WorldFlags flags = world.getFlags();
        flags.setMobSpawning(config.getBoolean("flags.mobSpawning", true));
        flags.setPvp(config.getBoolean("flags.pvp", false));
        flags.setExplosions(config.getBoolean("flags.explosions", false));
        
        // Загрузка доверенных игроков
        config.getStringList("trusted.builders").forEach(uuidStr -> 
            world.getTrustedBuilders().add(UUID.fromString(uuidStr))
        );
        config.getStringList("trusted.coders").forEach(uuidStr -> 
            world.getTrustedCoders().add(UUID.fromString(uuidStr))
        );
        
        // Загрузка статистики
        world.setLikes(config.getInt("stats.likes", 0));
        world.setDislikes(config.getInt("stats.dislikes", 0));

        // Загрузка скриптов
        List<Map<?, ?>> scriptsList = config.getMapList("scripts");
        for (Map<?, ?> scriptMapUntyped : scriptsList) {
            Map<String, Object> scriptMap = (Map<String, Object>) scriptMapUntyped;
            String scriptName = (String) scriptMap.getOrDefault("name", "Безымянный скрипт");
            boolean enabled = (boolean) scriptMap.getOrDefault("enabled", true);
            CodeBlock rootBlock = deserializeBlock((Map<String, Object>) scriptMap.get("rootBlock"));
            if (rootBlock != null) {
                world.getScripts().add(new CodeScript(scriptName, enabled, rootBlock));
            }
        }

        worlds.put(id, world);
        playerWorlds.computeIfAbsent(ownerId, k -> new ArrayList<>()).add(id);

        // Автоматическая загрузка мира и скриптов, если он уже существует
        if (Bukkit.getWorld(world.getWorldName()) != null) {
            plugin.getCodingManager().loadScriptsForWorld(world);
        }
    }
}
