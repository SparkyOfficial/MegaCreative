package ua.sparkycreative.worldsystem;

import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public class WorldManager {
    private static final int MAX_WORLDS = 3;
    private static final int WORLD_SIZE = 300;
    private static final String DEV_SUFFIX = "_dev";
    private final JavaPlugin plugin;
    private final Map<UUID, List<String>> playerWorlds = new HashMap<>();

    public WorldManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean canCreateWorld(UUID playerId) {
        return playerWorlds.getOrDefault(playerId, new ArrayList<>()).size() < MAX_WORLDS;
    }

    public String createWorld(UUID playerId, String baseName) {
        if (!canCreateWorld(playerId)) return null;
        String worldName = baseName + "_" + playerId.toString().substring(0, 8);
        WorldCreator creator = new WorldCreator(worldName)
                .generator(new Flat300Generator())
                .type(WorldType.FLAT)
                .environment(World.Environment.NORMAL);
        World world = creator.createWorld();
        if (world == null) return null;
        playerWorlds.computeIfAbsent(playerId, k -> new ArrayList<>()).add(worldName);
        // Создать dev-мир
        String devWorldName = worldName + DEV_SUFFIX;
        WorldCreator devCreator = new WorldCreator(devWorldName)
                .generator(new Flat300Generator())
                .type(WorldType.FLAT)
                .environment(World.Environment.NORMAL);
        devCreator.createWorld();
        return worldName;
    }

    public boolean deleteWorld(UUID playerId, String worldName) {
        List<String> worlds = playerWorlds.get(playerId);
        if (worlds == null || !worlds.contains(worldName)) return false;
        Bukkit.unloadWorld(worldName, false);
        Bukkit.unloadWorld(worldName + DEV_SUFFIX, false);
        // Физическое удаление мира — TODO
        worlds.remove(worldName);
        return true;
    }

    public List<String> getWorlds(UUID playerId) {
        return playerWorlds.getOrDefault(playerId, Collections.emptyList());
    }

    public static class Flat300Generator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
            ChunkData data = createChunkData(world);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    data.setBlock(x, 0, z, Material.BEDROCK);
                    data.setBlock(x, 1, z, Material.DIRT);
                    data.setBlock(x, 2, z, Material.GRASS_BLOCK);
                }
            }
            return data;
        }
    }
} 