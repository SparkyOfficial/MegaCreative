package com.megacreative.coding;

import com.megacreative.MegaCreative;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @deprecated This class is deprecated and will be removed in a future version.
 * Please migrate to the new {@link com.megacreative.coding.debug.VisualDebugger} class.
 * See the migration guide for more information.
 */
@Deprecated(forRemoval = true, since = "1.0.0")
public class ScriptDebugger {
    private final MegaCreative plugin;
    private final Map<UUID, Boolean> playerDebugStates = new HashMap<>();
    private final Map<UUID, Map<Location, Long>> blockExecutionTimes = new HashMap<>();

    public ScriptDebugger(MegaCreative plugin) {
        this.plugin = plugin;
    }

    public void toggleDebug(Player player) {
        UUID playerId = player.getUniqueId();
        boolean currentState = playerDebugStates.getOrDefault(playerId, false);
        playerDebugStates.put(playerId, !currentState);
    }

    public void onBlockExecute(Player player, CodeBlock block, Location blockLocation) {
        if (!playerDebugStates.getOrDefault(player.getUniqueId(), false)) {
            return;
        }

        // Показываем эффект выполнения блока
        showExecutionEffect(player, blockLocation, block.getMaterial());
        
        // Записываем время выполнения
        recordExecutionTime(player, blockLocation);
    }

    public void onBlockExecuteWithParameters(Player player, CodeBlock block, Location blockLocation, Map<String, Object> parameters) {
        if (!playerDebugStates.getOrDefault(player.getUniqueId(), false)) {
            return;
        }

        // Показываем эффект выполнения блока
        showExecutionEffect(player, blockLocation, block.getMaterial());
        
        // Записываем время выполнения
        recordExecutionTime(player, blockLocation);
    }

    private void showExecutionEffect(Player player, Location location, Material material) {
        // Создаем эффект частиц вокруг блока
        Particle effectParticle = getExecutionParticle(material);
        
        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (step >= 10) {
                    this.cancel();
                    return;
                }
                
                // Создаем кольцо частиц вокруг блока
                for (int i = 0; i < 8; i++) {
                    double angle = (i * Math.PI * 2) / 8;
                    double x = location.getX() + 0.5 + Math.cos(angle) * 0.8;
                    double z = location.getZ() + 0.5 + Math.sin(angle) * 0.8;
                    Location particleLoc = new Location(location.getWorld(), x, location.getY() + 0.5, z);
                    player.spawnParticle(effectParticle, particleLoc, 1, 0, 0, 0, 0);
                }
                
                step++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private Particle getExecutionParticle(Material material) {
        switch (material) {
            case DIAMOND_BLOCK: return Particle.FIREWORKS_SPARK;
            case OAK_PLANKS: return Particle.VILLAGER_HAPPY;
            case COBBLESTONE: return Particle.REDSTONE;
            case IRON_BLOCK: return Particle.ENCHANTMENT_TABLE;
            case END_STONE: return Particle.SMOKE_LARGE;
            case NETHERITE_BLOCK: return Particle.LAVA;
            case OBSIDIAN: return Particle.SMOKE_NORMAL;
            case REDSTONE_BLOCK: return Particle.FLAME;
            case BRICKS: return Particle.SLIME;
            case POLISHED_GRANITE: return Particle.END_ROD;
            default: return Particle.REDSTONE;
        }
    }

    private String getMaterialDisplayName(Material material) {
        switch (material) {
            case DIAMOND_BLOCK: return "Событие";
            case OAK_PLANKS: return "Условие";
            case COBBLESTONE: return "Действие";
            case IRON_BLOCK: return "Переменная";
            case END_STONE: return "Иначе";
            case NETHERITE_BLOCK: return "Игровое действие";
            case OBSIDIAN: return "Если переменная";
            case REDSTONE_BLOCK: return "Если игра";
            case BRICKS: return "Если существо";
            case POLISHED_GRANITE: return "Получить данные";
            default: return material.name();
        }
    }

    private void recordExecutionTime(Player player, Location blockLocation) {
        Map<Location, Long> times = blockExecutionTimes.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        times.put(blockLocation, System.currentTimeMillis());
    }

    public void onScriptStart(Player player, CodeScript script) {
        // Метод оставлен для совместимости
    }

    public void onScriptEnd(Player player, CodeScript script) {
        // Метод оставлен для совместимости
    }

    public void onConditionResult(Player player, CodeBlock conditionBlock, boolean result) {
        // Метод оставлен для совместимости
    }

    public boolean isDebugEnabled(Player player) {
        return playerDebugStates.getOrDefault(player.getUniqueId(), false);
    }

    public void showDebugStats(Player player) {
        // Метод оставлен для совместимости
    }
}