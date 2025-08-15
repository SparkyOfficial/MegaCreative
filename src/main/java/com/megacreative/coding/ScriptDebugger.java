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

public class ScriptDebugger {
    private final MegaCreative plugin;
    private final Map<UUID, Boolean> playerDebugStates = new HashMap<>();
    private final Map<UUID, Map<Location, Long>> blockExecutionTimes = new HashMap<>();
    private final Map<UUID, Boolean> playerStepStates = new HashMap<>();
    private final Map<UUID, CodeBlock> playerNextStepBlocks = new HashMap<>();

    public ScriptDebugger(MegaCreative plugin) {
        this.plugin = plugin;
    }

    public void toggleDebug(Player player) {
        UUID playerId = player.getUniqueId();
        boolean currentState = playerDebugStates.getOrDefault(playerId, false);
        playerDebugStates.put(playerId, !currentState);
        
        if (!currentState) {
            player.sendMessage("§a✓ Отладка скриптов включена");
            player.sendMessage("§7Теперь вы будете видеть выполнение блоков!");
        } else {
            player.sendMessage("§c✗ Отладка скриптов отключена");
        }
    }

    public void onBlockExecute(Player player, CodeBlock block, Location blockLocation) {
        if (!playerDebugStates.getOrDefault(player.getUniqueId(), false)) {
            return;
        }

        // Показываем эффект выполнения блока
        showExecutionEffect(player, blockLocation, block.getMaterial());
        
        // Отправляем сообщение о выполнении
        String actionName = block.getAction();
        String materialName = getMaterialDisplayName(block.getMaterial());
        player.sendMessage("§e▶ Выполняется: §f" + materialName + " §8(" + actionName + ")");
        
        // Записываем время выполнения
        recordExecutionTime(player, blockLocation);
    }

    public void onBlockExecuteWithParameters(Player player, CodeBlock block, Location blockLocation, Map<String, Object> parameters) {
        if (!playerDebugStates.getOrDefault(player.getUniqueId(), false)) {
            return;
        }

        // Показываем эффект выполнения блока
        showExecutionEffect(player, blockLocation, block.getMaterial());
        
        // Отправляем сообщение с параметрами
        String actionName = block.getAction();
        String materialName = getMaterialDisplayName(block.getMaterial());
        player.sendMessage("§e▶ Выполняется: §f" + materialName + " §8(" + actionName + ")");
        
        if (parameters != null && !parameters.isEmpty()) {
            StringBuilder paramText = new StringBuilder("§7Параметры: ");
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                paramText.append("§f").append(entry.getKey()).append("=").append(entry.getValue()).append("§7, ");
            }
            player.sendMessage(paramText.substring(0, paramText.length() - 4));
        }
        
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
        if (!playerDebugStates.getOrDefault(player.getUniqueId(), false)) {
            return;
        }
        
        player.sendMessage("§a=== Начало выполнения скрипта ===");
        player.sendMessage("§7Название: §f" + script.getName());
        player.sendMessage("§7Активен: §fДа");
    }

    public void onScriptEnd(Player player, CodeScript script) {
        if (!playerDebugStates.getOrDefault(player.getUniqueId(), false)) {
            return;
        }
        
        player.sendMessage("§c=== Конец выполнения скрипта ===");
    }

    public void onConditionResult(Player player, CodeBlock conditionBlock, boolean result) {
        if (!playerDebugStates.getOrDefault(player.getUniqueId(), false)) {
            return;
        }
        
        String resultText = result ? "§aИстина" : "§cЛожь";
        player.sendMessage("§7Условие " + conditionBlock.getAction() + ": " + resultText);
    }

    public boolean isDebugEnabled(Player player) {
        return playerDebugStates.getOrDefault(player.getUniqueId(), false);
    }

    public void showDebugStats(Player player) {
        Map<Location, Long> times = blockExecutionTimes.get(player.getUniqueId());
        if (times == null || times.isEmpty()) {
            player.sendMessage("§7Нет данных о выполнении блоков");
            return;
        }
        
        player.sendMessage("§e=== Статистика выполнения ===");
        player.sendMessage("§7Выполнено блоков: §f" + times.size());
        
        long totalTime = 0;
        for (Long time : times.values()) {
            totalTime += time;
        }
        
        if (times.size() > 0) {
            long avgTime = totalTime / times.size();
            player.sendMessage("§7Среднее время выполнения: §f" + avgTime + "мс");
        }
    }
    
    /**
     * Показывает переменные в контексте выполнения.
     */
    public void onVariableAccess(Player player, String variableName, Object value, String operation) {
        if (!playerDebugStates.getOrDefault(player.getUniqueId(), false)) {
            return;
        }
        
        String operationText = "";
        switch (operation) {
            case "get":
                operationText = "§e📖 Чтение";
                break;
            case "set":
                operationText = "§a✏️ Запись";
                break;
            case "delete":
                operationText = "§c🗑️ Удаление";
                break;
        }
        
        player.sendMessage(operationText + " переменной: §f" + variableName + " §8= §e" + value);
    }
    
    /**
     * Показывает путь выполнения между блоками.
     */
    public void onBlockTransition(Player player, CodeBlock fromBlock, CodeBlock toBlock) {
        if (!playerDebugStates.getOrDefault(player.getUniqueId(), false)) {
            return;
        }
        
        String fromAction = fromBlock.getAction();
        String toAction = toBlock.getAction();
        
        player.sendMessage("§7  ↳ Переход: §f" + fromAction + " §7→ §f" + toAction);
        
        // Показываем визуальный путь частицами
        showTransitionPath(player, fromBlock, toBlock);
    }
    
    /**
     * Показывает визуальный путь между блоками.
     */
    private void showTransitionPath(Player player, CodeBlock fromBlock, CodeBlock toBlock) {
        // Получаем локации блоков
        Location fromLocation = plugin.getCodingManager().getScriptExecutor().findBlockLocation(fromBlock);
        Location toLocation = plugin.getCodingManager().getScriptExecutor().findBlockLocation(toBlock);
        
        if (fromLocation == null || toLocation == null) {
            return; // Не можем показать путь без локаций
        }
        
        // Создаем эффект частиц между блоками
        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (step >= 20) {
                    this.cancel();
                    return;
                }
                
                // Интерполируем позицию между блоками
                double progress = (double) step / 20.0;
                double x = fromLocation.getX() + (toLocation.getX() - fromLocation.getX()) * progress;
                double y = fromLocation.getY() + (toLocation.getY() - fromLocation.getY()) * progress + 1.0;
                double z = fromLocation.getZ() + (toLocation.getZ() - fromLocation.getZ()) * progress;
                
                Location particleLoc = new Location(fromLocation.getWorld(), x, y, z);
                
                // Создаем частицы разных цветов для разных типов переходов
                Particle particleType = Particle.END_ROD;
                if (toBlock.getMaterial() == Material.OAK_PLANKS) {
                    particleType = Particle.VILLAGER_HAPPY; // Условия
                } else if (toBlock.getMaterial() == Material.REDSTONE_BLOCK) {
                    particleType = Particle.FLAME; // Действия
                } else if (toBlock.getMaterial() == Material.BOOKSHELF) {
                    particleType = Particle.ENCHANTMENT_TABLE; // Функции
                }
                
                player.spawnParticle(particleType, particleLoc, 3, 0.1, 0.1, 0.1, 0);
                
                step++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    
    /**
     * Включает/выключает пошаговое выполнение для игрока.
     */
    public void toggleStepExecution(Player player) {
        UUID playerId = player.getUniqueId();
        boolean currentState = playerStepStates.getOrDefault(playerId, false);
        playerStepStates.put(playerId, !currentState);
        
        if (!currentState) {
            player.sendMessage("§a✓ Пошаговое выполнение включено");
            player.sendMessage("§7Используйте §f/debug step §7для выполнения следующего блока");
        } else {
            player.sendMessage("§c✗ Пошаговое выполнение отключено");
        }
    }
    
    /**
     * Выполняет следующий блок в пошаговом режиме.
     */
    public void stepExecution(Player player) {
        if (!playerDebugStates.getOrDefault(player.getUniqueId(), false)) {
            player.sendMessage("§cОтладка должна быть включена для пошагового выполнения!");
            return;
        }
        
        if (!playerStepStates.getOrDefault(player.getUniqueId(), false)) {
            player.sendMessage("§cПошаговое выполнение должно быть включено!");
            return;
        }
        
        CodeBlock nextBlock = playerNextStepBlocks.get(player.getUniqueId());
        if (nextBlock == null) {
            player.sendMessage("§e⚠ Нет блоков для выполнения");
            return;
        }
        
        player.sendMessage("§a⏭️ Выполняется следующий блок...");
        
        // Выполняем следующий блок
        var executor = plugin.getCodingManager().getScriptExecutor();
        Location blockLocation = executor.findBlockLocation(nextBlock);
        var context = new ExecutionContext.ExecutionContextBuilder()
            .plugin(plugin)
            .player(player)
            .currentBlock(nextBlock)
            .blockLocation(blockLocation)
            .build();
        
        executor.processBlock(nextBlock, context);
    }
    
    /**
     * Устанавливает следующий блок для пошагового выполнения.
     */
    public void setNextStepBlock(Player player, CodeBlock nextBlock) {
        if (playerStepStates.getOrDefault(player.getUniqueId(), false)) {
            playerNextStepBlocks.put(player.getUniqueId(), nextBlock);
            if (nextBlock != null) {
                player.sendMessage("§e⏸️ Следующий блок готов к выполнению: §f" + nextBlock.getAction());
                player.sendMessage("§7Используйте §f/debug step §7для выполнения");
            }
        }
    }
    
    /**
     * Проверяет, включено ли пошаговое выполнение для игрока.
     */
    public boolean isStepExecutionEnabled(Player player) {
        return playerStepStates.getOrDefault(player.getUniqueId(), false);
    }
    
    /**
     * Улучшенная версия onConditionResult с детальной информацией.
     */
    public void onConditionResultDetailed(Player player, CodeBlock conditionBlock, boolean result) {
        if (!playerDebugStates.getOrDefault(player.getUniqueId(), false)) {
            return;
        }

        String conditionName = conditionBlock.getAction();
        String materialName = getMaterialDisplayName(conditionBlock.getMaterial());
        String resultText = result ? "§aИСТИННО" : "§cЛОЖНО";
        
        player.sendMessage("§b🔍 Условие: §f" + materialName + " §8(" + conditionName + ") = " + resultText);
        
        // Показываем путь выполнения
        if (result) {
            player.sendMessage("§a  ↳ Выполняется IF ветка");
        } else {
            player.sendMessage("§c  ↳ Выполняется ELSE ветка (если есть)");
        }
    }
    
    /**
     * Очищает данные отладки для игрока.
     */
    public void clearDebugData(Player player) {
        UUID playerId = player.getUniqueId();
        playerDebugStates.remove(playerId);
        blockExecutionTimes.remove(playerId);
        playerStepStates.remove(playerId);
        playerNextStepBlocks.remove(playerId);
    }
} 