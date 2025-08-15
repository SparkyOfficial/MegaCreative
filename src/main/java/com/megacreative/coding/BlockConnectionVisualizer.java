package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockConnectionVisualizer {
    private final MegaCreative plugin;
    private final Map<UUID, Boolean> playerVisualizationStates = new HashMap<>();
    private final Map<CreativeWorld, Map<Location, CodeBlock>> worldBlocks = new HashMap<>();

    public BlockConnectionVisualizer(MegaCreative plugin) {
        this.plugin = plugin;
    }

    public void toggleVisualization(Player player) {
        UUID playerId = player.getUniqueId();
        boolean currentState = playerVisualizationStates.getOrDefault(playerId, false);
        playerVisualizationStates.put(playerId, !currentState);
        
        if (!currentState) {
            startVisualization(player);
            player.sendMessage("§a✓ Визуализация соединений включена");
        } else {
            stopVisualization(player);
            player.sendMessage("§c✗ Визуализация соединений отключена");
        }
    }

    public void startVisualization(Player player) {
        CreativeWorld world = plugin.getWorldManager().getWorld(player.getWorld().getName());
        if (world == null) return;

        // Загружаем блоки для мира
        loadWorldBlocks(world);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!playerVisualizationStates.getOrDefault(player.getUniqueId(), false)) {
                    this.cancel();
                    return;
                }

                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                showConnections(player, world);
            }
        }.runTaskTimer(plugin, 0L, 20L); // Обновляем каждую секунду
    }

    public void stopVisualization(Player player) {
        // Очищаем частицы (можно добавить эффект исчезновения)
        player.sendMessage("§7Визуализация остановлена");
    }

    private void loadWorldBlocks(CreativeWorld world) {
        Map<Location, CodeBlock> blocks = new HashMap<>();
        
        // Загружаем блоки из BlockPlacementHandler
        BlockPlacementHandler handler = plugin.getBlockPlacementHandler();
        Map<Location, CodeBlock> existingBlocks = handler.getBlockCodeBlocks();
        
        // Фильтруем блоки только для этого мира
        for (Map.Entry<Location, CodeBlock> entry : existingBlocks.entrySet()) {
            Location loc = entry.getKey();
            if (loc.getWorld().getName().equals(world.getWorldName())) {
                blocks.put(loc, entry.getValue());
            }
        }
        
        worldBlocks.put(world, blocks);
        plugin.getLogger().info("Загружено " + blocks.size() + " блоков для визуализации в мире " + world.getName());
    }

    private void showConnections(Player player, CreativeWorld world) {
        Map<Location, CodeBlock> blocks = worldBlocks.get(world);
        if (blocks == null) return;

        for (Map.Entry<Location, CodeBlock> entry : blocks.entrySet()) {
            Location blockLoc = entry.getKey();
            CodeBlock block = entry.getValue();
            
            if (block.getNextBlock() != null) {
                // Находим локацию следующего блока
                Location nextLoc = findBlockLocation(blocks, block.getNextBlock());
                if (nextLoc != null) {
                    drawConnection(player, blockLoc, nextLoc, getConnectionColor(block.getMaterial()));
                }
            }

            // Показываем дочерние блоки (для условий)
            for (CodeBlock child : block.getChildren()) {
                Location childLoc = findBlockLocation(blocks, child);
                if (childLoc != null) {
                    drawConnection(player, blockLoc, childLoc, Particle.VILLAGER_HAPPY);
                }
            }
        }
    }

    private Location findBlockLocation(Map<Location, CodeBlock> blocks, CodeBlock targetBlock) {
        for (Map.Entry<Location, CodeBlock> entry : blocks.entrySet()) {
            if (entry.getValue().equals(targetBlock)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void drawConnection(Player player, Location from, Location to, Particle particleType) {
        // Рисуем линию частиц между блоками
        double distance = from.distance(to);
        int steps = (int) (distance * 2); // Больше шагов для плавности
        
        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            Location particleLoc = interpolate(from, to, progress);
            player.spawnParticle(particleType, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    private Location interpolate(Location from, Location to, double progress) {
        double x = from.getX() + (to.getX() - from.getX()) * progress;
        double y = from.getY() + (to.getY() - from.getY()) * progress;
        double z = from.getZ() + (to.getZ() - from.getZ()) * progress;
        return new Location(from.getWorld(), x, y, z);
    }

    private Particle getConnectionColor(Material material) {
        switch (material) {
            case DIAMOND_BLOCK: return Particle.FIREWORKS_SPARK; // События - искры
            case OAK_PLANKS: return Particle.VILLAGER_HAPPY; // Условия - зеленые частицы
            case COBBLESTONE: return Particle.REDSTONE; // Действия - красные частицы
            case IRON_BLOCK: return Particle.ENCHANTMENT_TABLE; // Переменные - фиолетовые частицы
            case END_STONE: return Particle.SMOKE_LARGE; // Иначе - большие частицы дыма
            case NETHERITE_BLOCK: return Particle.LAVA; // Игровые действия - лава
            case OBSIDIAN: return Particle.SMOKE_NORMAL; // Если переменная - дым
            case REDSTONE_BLOCK: return Particle.FLAME; // Если игра - огонь
            case BRICKS: return Particle.SLIME; // Если существо - слизь
            case POLISHED_GRANITE: return Particle.END_ROD; // Получить данные - стержни
            default: return Particle.REDSTONE;
        }
    }

    public void addBlock(CreativeWorld world, Location location, CodeBlock block) {
        Map<Location, CodeBlock> blocks = worldBlocks.computeIfAbsent(world, k -> new HashMap<>());
        blocks.put(location, block);
    }

    public void removeBlock(CreativeWorld world, Location location) {
        Map<Location, CodeBlock> blocks = worldBlocks.get(world);
        if (blocks != null) {
            blocks.remove(location);
        }
    }

    public boolean isVisualizationEnabled(Player player) {
        return playerVisualizationStates.getOrDefault(player.getUniqueId(), false);
    }
} 