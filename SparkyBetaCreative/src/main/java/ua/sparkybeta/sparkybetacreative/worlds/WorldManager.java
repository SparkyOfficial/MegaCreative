package ua.sparkybeta.sparkybetacreative.worlds;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface WorldManager {

    void onEnable();

    void onDisable();

    CompletableFuture<Boolean> createWorld(Player player, WorldType type);

    CompletableFuture<Boolean> deleteWorld(SparkyWorld world);

    CompletableFuture<Void> loadPlayerWorlds(UUID playerUUID);

    List<SparkyWorld> getPlayerWorlds(UUID playerUUID);

    SparkyWorld getWorldByCustomId(String customId);
    
    SparkyWorld getWorld(Player player);

    List<SparkyWorld> getPublicWorlds();
    
    CompletableFuture<Boolean> teleportToWorld(Player player, SparkyWorld world);
    
    CompletableFuture<Boolean> teleportToDevWorld(Player player, SparkyWorld world);

} 