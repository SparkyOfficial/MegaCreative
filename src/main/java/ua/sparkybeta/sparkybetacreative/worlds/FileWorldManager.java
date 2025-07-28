package ua.sparkybeta.sparkybetacreative.worlds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import ua.sparkybeta.sparkybetacreative.SparkyBetaCreative;
import ua.sparkybeta.sparkybetacreative.util.MessageUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FileWorldManager implements WorldManager {

    private final Map<UUID, SparkyWorld> worldsByInternalName = new ConcurrentHashMap<>();
    private final File worldsFolder = new File(SparkyBetaCreative.getInstance().getDataFolder(), "worlds");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_WORLDS_PER_PLAYER = 3;

    @Override
    public void onEnable() {
        if (!worldsFolder.exists()) {
            worldsFolder.mkdirs();
        }
        // Asynchronously load all worlds metadata from files
        loadAllWorlds();
    }

    private void loadAllWorlds() {
        File[] playerFolders = worldsFolder.listFiles(File::isDirectory);
        if (playerFolders == null) return;

        for (File playerFolder : playerFolders) {
            File[] worldFolders = playerFolder.listFiles(File::isDirectory);
            if (worldFolders == null) continue;

            for (File worldFolder : worldFolders) {
                File worldFile = new File(worldFolder, "world.json");
                if (worldFile.exists()) {
                    try (FileReader reader = new FileReader(worldFile)) {
                        SparkyWorld world = gson.fromJson(reader, SparkyWorld.class);
                        worldsByInternalName.put(UUID.fromString(world.getInternalName()), world);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        SparkyBetaCreative.getInstance().getLogger().info("Loaded " + worldsByInternalName.size() + " worlds.");
    }

    @Override
    public void onDisable() {
        // Save all worlds metadata to files
        worldsByInternalName.values().forEach(this::saveWorldToFile);
    }

    private void saveWorldToFile(SparkyWorld world) {
        File worldFolder = new File(worldsFolder, world.getOwner().toString() + File.separator + world.getInternalName());
        if (!worldFolder.exists()) {
            worldFolder.mkdirs();
        }
        File worldFile = new File(worldFolder, "world.json");
        try (FileWriter writer = new FileWriter(worldFile)) {
            gson.toJson(world, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void deleteWorldFolders(SparkyWorld world) {
        // Удаляем папки миров по новым именам
        File worldFolder = new File(SparkyBetaCreative.getInstance().getDataFolder().getParentFile().getParentFile(), getBukkitWorldName(world));
        File devWorldFolder = new File(SparkyBetaCreative.getInstance().getDataFolder().getParentFile().getParentFile(), getDevBukkitWorldName(world));
        
        try {
            if(worldFolder.exists()) {
                Files.walk(worldFolder.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(java.nio.file.Path::toFile)
                        .forEach(java.io.File::delete);
            }
            if(devWorldFolder.exists()) {
                Files.walk(devWorldFolder.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(java.nio.file.Path::toFile)
                        .forEach(java.io.File::delete);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CompletableFuture<Void> deleteWorldFoldersAsync(SparkyWorld world) {
        return CompletableFuture.runAsync(() -> {
            deleteWorldFolders(world);
        }, runnable -> Bukkit.getScheduler().runTaskAsynchronously(SparkyBetaCreative.getInstance(), runnable));
    }

    @Override
    public CompletableFuture<Boolean> createWorld(Player player, ua.sparkybeta.sparkybetacreative.worlds.WorldType type) {
        final SparkyBetaCreative plugin = SparkyBetaCreative.getInstance();
        
        if (getPlayerWorlds(player.getUniqueId()).size() >= MAX_WORLDS_PER_PLAYER) {
            MessageUtils.sendError(player, "You have reached the maximum number of worlds (" + MAX_WORLDS_PER_PLAYER + ").");
            return CompletableFuture.completedFuture(false);
        }

        final SparkyWorld sparkyWorld = new SparkyWorld(player.getUniqueId(), UUID.randomUUID().toString(), type, String.format("%06d", new Random().nextInt(999999)));

        CompletableFuture<World> mainWorldCreationFuture = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                WorldCreator creator = new WorldCreator(getBukkitWorldName(sparkyWorld));
                applyWorldSettings(creator, type);
                World world = creator.createWorld();
                if (world != null) {
                    org.bukkit.WorldBorder border = world.getWorldBorder();
                    border.setCenter(0, 0);
                    border.setSize(300);
                    mainWorldCreationFuture.complete(world);
                } else {
                    mainWorldCreationFuture.completeExceptionally(new RuntimeException("Main world creation returned null."));
                }
            } catch (Exception e) {
                mainWorldCreationFuture.completeExceptionally(e);
            }
        });

        return mainWorldCreationFuture.thenCompose(mainWorld -> {
            CompletableFuture<World> devWorldCreationFuture = new CompletableFuture<>();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    WorldCreator devCreator = new WorldCreator(getDevBukkitWorldName(sparkyWorld));
                    devCreator.generator(new VoidWorldGenerator());
                    World devWorld = devCreator.createWorld();
                    if (devWorld != null) {
                        org.bukkit.WorldBorder devBorder = devWorld.getWorldBorder();
                        devBorder.setCenter(0, 0);
                        devBorder.setSize(300);
                        devWorldCreationFuture.complete(devWorld);
                    } else {
                        devWorldCreationFuture.completeExceptionally(new RuntimeException("Dev world creation returned null."));
                    }
                } catch (Exception e) {
                    devWorldCreationFuture.completeExceptionally(e);
                }
            });

            return devWorldCreationFuture.handle((devWorld, ex) -> {
                if (ex != null) {
                    plugin.getLogger().warning("Dev world creation failed. Cleaning up main world.");
                    plugin.getServer().getScheduler().runTask(plugin, () -> Bukkit.unloadWorld(mainWorld, false));
                    deleteWorldFoldersAsync(sparkyWorld);
                    throw new CompletionException(ex);
                }
                return mainWorld;
            });
        }).thenComposeAsync(mainWorld -> {
            saveWorldToFile(sparkyWorld);
            worldsByInternalName.put(UUID.fromString(sparkyWorld.getInternalName()), sparkyWorld);
            
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.teleport(mainWorld.getSpawnLocation());
            });
            return CompletableFuture.completedFuture(true);
        }).exceptionally(ex -> {
            plugin.getLogger().severe("An exception occurred during world creation for " + player.getName() + ": " + ex.getMessage());
            if (ex.getCause() != null) {
                ex.getCause().printStackTrace();
            }
            return false;
        });
    }
    
    private void applyWorldSettings(WorldCreator creator, ua.sparkybeta.sparkybetacreative.worlds.WorldType type) {
        switch (type) {
            case FLAT:
                creator.type(WorldType.FLAT);
                creator.generatorSettings("{\"layers\":[{\"block\":\"bedrock\",\"height\":1},{\"block\":\"dirt\",\"height\":2},{\"block\":\"grass_block\",\"height\":1}],\"biome\":\"plains\"}");
                break;
            case VOID:
                creator.type(WorldType.FLAT);
                creator.generator(new VoidWorldGenerator()); // Custom generator needed
                break;
            case NETHER:
                creator.environment(World.Environment.NETHER);
                break;
            case THE_END:
                creator.environment(World.Environment.THE_END);
                break;
            case DEFAULT:
            default:
                creator.type(WorldType.NORMAL);
                break;
        }
    }

    private String getBukkitWorldName(SparkyWorld world) {
        return "sparkyworlds_" + world.getOwner().toString() + "_" + world.getInternalName();
    }

    private String getDevBukkitWorldName(SparkyWorld world) {
        return "sparkyworlds_" + world.getOwner().toString() + "_" + world.getInternalName() + "_dev";
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(SparkyWorld world) {
        return CompletableFuture.supplyAsync(() -> {
            World bukkitWorld = Bukkit.getWorld(getBukkitWorldName(world));
            if (bukkitWorld != null) {
                bukkitWorld.getPlayers().forEach(p -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())); // Teleport players away
                Bukkit.unloadWorld(bukkitWorld, false);
            }
            
            World devBukkitWorld = Bukkit.getWorld(getDevBukkitWorldName(world));
            if (devBukkitWorld != null) {
                devBukkitWorld.getPlayers().forEach(p -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())); // Teleport players away
                Bukkit.unloadWorld(devBukkitWorld, false);
            }
            
            worldsByInternalName.remove(UUID.fromString(world.getInternalName()));
            deleteWorldFolders(world);
            
            return true;
        });
    }

    @Override
    public CompletableFuture<Void> loadPlayerWorlds(UUID playerUUID) {
        // Worlds are pre-loaded at startup, but this could force-reload from files if needed
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public List<SparkyWorld> getPlayerWorlds(UUID playerUUID) {
        return worldsByInternalName.values().stream()
                .filter(world -> world.getOwner().equals(playerUUID))
                .collect(Collectors.toList());
    }

    @Override
    public SparkyWorld getWorldByCustomId(String customId) {
        return worldsByInternalName.values().stream()
                .filter(world -> world.getCustomId().equalsIgnoreCase(customId))
                .findFirst().orElse(null);
    }

    @Override
    public SparkyWorld getWorld(Player player) {
        String worldName = player.getWorld().getName();
        if (!worldName.startsWith("sparkyworlds_")) {
            return null;
        }
        try {
            // Новый формат: sparkyworlds_uuid_internalName[_dev]
            String[] parts = worldName.split("_");
            String uuid = parts[1];
            String internalName = parts[2];
            return worldsByInternalName.get(UUID.fromString(internalName));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<SparkyWorld> getPublicWorlds() {
        return worldsByInternalName.values().stream()
                .filter(world -> world.getSettings().isPublic())
                .collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<Boolean> teleportToWorld(Player player, SparkyWorld world) {
        return CompletableFuture.supplyAsync(() -> {
            World bukkitWorld = Bukkit.getWorld(getBukkitWorldName(world));
            if (bukkitWorld == null) {
                // World is not loaded, load it.
                WorldCreator creator = new WorldCreator(getBukkitWorldName(world));
                applyWorldSettings(creator, world.getType());
                bukkitWorld = Bukkit.createWorld(creator);
                if (bukkitWorld == null) {
                    return false; // Failed to load or create world
                }
            }

            final World finalWorld = bukkitWorld;
            // Teleportation must be done on the main thread
            Bukkit.getScheduler().runTask(SparkyBetaCreative.getInstance(), () -> {
                player.teleport(finalWorld.getSpawnLocation());
            });

            return true;
        }, (runnable) -> Bukkit.getScheduler().runTaskAsynchronously(SparkyBetaCreative.getInstance(), runnable));
    }

    @Override
    public CompletableFuture<Boolean> teleportToDevWorld(Player player, SparkyWorld world) {
        return CompletableFuture.supplyAsync(() -> {
            World bukkitWorld = Bukkit.getWorld(getDevBukkitWorldName(world));
            if (bukkitWorld == null) {
                // World is not loaded, load it.
                WorldCreator creator = new WorldCreator(getDevBukkitWorldName(world));
                creator.generator(new VoidWorldGenerator());
                bukkitWorld = Bukkit.createWorld(creator);
                if (bukkitWorld == null) {
                    return false; // Failed to load or create world
                }
            }

            final World finalWorld = bukkitWorld;
            // Teleportation must be done on the main thread
            Bukkit.getScheduler().runTask(SparkyBetaCreative.getInstance(), () -> {
                player.teleport(finalWorld.getSpawnLocation());
            });

            return true;
        }, (runnable) -> Bukkit.getScheduler().runTaskAsynchronously(SparkyBetaCreative.getInstance(), runnable));
    }
} 