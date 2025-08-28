package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.*;

/**
 * Менеджер автоматических соединений блоков кода
 * Автоматически связывает блоки в соответствии с линиями кода в dev-генераторе
 */
public class AutoConnectionManager implements Listener {
    
    private final MegaCreative plugin;
    private final BlockConfigService blockConfigService;
    private final Map<Location, CodeBlock> locationToBlock = new HashMap<>();
    private final Map<UUID, List<CodeBlock>> playerScriptBlocks = new HashMap<>();
    
    public AutoConnectionManager(MegaCreative plugin, BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.blockConfigService = blockConfigService;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();
        
        // Проверяем, что это dev-мир
        if (!isDevWorld(block.getWorld())) return;
        
        // Проверяем, что это блок кода
        if (!blockConfigService.isCodeBlock(block.getType())) return;
        
        // Проверяем, что блок размещен на валидной позиции
        if (!DevWorldGenerator.isValidCodePosition(location.getBlockX(), location.getBlockZ())) {
            event.setCancelled(true);
            player.sendMessage("§cБлоки кода можно размещать только на специальных позициях!");
            return;
        }
        
        // Создаем новый CodeBlock
        CodeBlock codeBlock = createCodeBlockFromMaterial(block.getType(), location);
        if (codeBlock == null) return;
        
        // Сохраняем связь между позицией и блоком
        locationToBlock.put(location, codeBlock);
        
        // Добавляем блок к скрипту игрока
        addBlockToPlayerScript(player, codeBlock);
        
        // Автоматически соединяем с соседними блоками
        autoConnectBlock(codeBlock, location);
        
        player.sendMessage("§aБлок кода размещен и автоматически подключен!");
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Location location = event.getBlock().getLocation();
        CodeBlock codeBlock = locationToBlock.get(location);
        
        if (codeBlock != null) {
            // Удаляем связи с соседними блоками
            disconnectBlock(codeBlock, location);
            
            // Удаляем из хранилища
            locationToBlock.remove(location);
            
            // Удаляем из скрипта игрока
            removeBlockFromPlayerScript(event.getPlayer(), codeBlock);
            
            event.getPlayer().sendMessage("§cБлок кода удален и отключен от цепочки!");
        }
    }
    
    /**
     * Автоматически соединяет блок с соседними блоками в той же линии
     */
    private void autoConnectBlock(CodeBlock codeBlock, Location location) {
        int line = DevWorldGenerator.getCodeLineFromZ(location.getBlockZ());
        if (line == -1) return;
        
        // Ищем предыдущий блок в той же линии
        Location prevLocation = getPreviousLocationInLine(location);
        if (prevLocation != null) {
            CodeBlock prevBlock = locationToBlock.get(prevLocation);
            if (prevBlock != null) {
                prevBlock.setNextBlock(codeBlock);
                plugin.getLogger().info("Автосоединение: блок на " + prevLocation + " -> " + location);
            }
        }
        
        // Ищем следующий блок в той же линии
        Location nextLocation = getNextLocationInLine(location);
        if (nextLocation != null) {
            CodeBlock nextBlock = locationToBlock.get(nextLocation);
            if (nextBlock != null) {
                codeBlock.setNextBlock(nextBlock);
                plugin.getLogger().info("Автосоединение: блок на " + location + " -> " + nextLocation);
            }
        }
        
        // Если это начало линии (X=0), проверяем дочерние блоки
        if (location.getBlockX() == 0) {
            connectChildBlocks(codeBlock, location);
        }
    }
    
    /**
     * Соединяет дочерние блоки для условий и циклов
     */
    private void connectChildBlocks(CodeBlock parentBlock, Location parentLocation) {
        int line = DevWorldGenerator.getCodeLineFromZ(parentLocation.getBlockZ());
        
        // Ищем блоки в следующих линиях, которые могут быть дочерними
        for (int nextLine = line + 1; nextLine < line + 10 && nextLine < DevWorldGenerator.getLinesCount(); nextLine++) {
            int childZ = DevWorldGenerator.getZForCodeLine(nextLine);
            Location childLocation = new Location(parentLocation.getWorld(), 2, parentLocation.getBlockY(), childZ);
            
            CodeBlock childBlock = locationToBlock.get(childLocation);
            if (childBlock != null) {
                parentBlock.addChild(childBlock);
                plugin.getLogger().info("Автосоединение дочернего блока: " + parentLocation + " -> " + childLocation);
            } else {
                break; // Прерываем поиск если нет блока
            }
        }
    }
    
    /**
     * Отключает блок от соседних блоков
     */
    private void disconnectBlock(CodeBlock codeBlock, Location location) {
        // Находим предыдущий блок и обновляем его ссылку
        Location prevLocation = getPreviousLocationInLine(location);
        if (prevLocation != null) {
            CodeBlock prevBlock = locationToBlock.get(prevLocation);
            if (prevBlock != null && prevBlock.getNextBlock() == codeBlock) {
                // Ищем следующий блок для переподключения
                CodeBlock nextBlock = codeBlock.getNextBlock();
                prevBlock.setNextBlock(nextBlock);
            }
        }
        
        // Удаляем из дочерних блоков родительского блока
        for (CodeBlock block : locationToBlock.values()) {
            block.getChildren().remove(codeBlock);
        }
    }
    
    /**
     * Получает предыдущую позицию в той же линии
     */
    private Location getPreviousLocationInLine(Location location) {
        int prevX = location.getBlockX() - 1;
        if (prevX < 0) return null;
        
        return new Location(location.getWorld(), prevX, location.getBlockY(), location.getBlockZ());
    }
    
    /**
     * Получает следующую позицию в той же линии
     */
    private Location getNextLocationInLine(Location location) {
        int nextX = location.getBlockX() + 1;
        if (nextX >= DevWorldGenerator.getBlocksPerLine()) return null;
        
        return new Location(location.getWorld(), nextX, location.getBlockY(), location.getBlockZ());
    }
    
    /**
     * Создает CodeBlock на основе материала блока
     */
    private CodeBlock createCodeBlockFromMaterial(Material material, Location location) {
        String action = determineActionFromMaterial(material);
        if (action == null) return null;
        
        CodeBlock codeBlock = new CodeBlock(material, action);
        codeBlock.setPlugin(plugin);
        
        return codeBlock;
    }
    
    /**
     * Определяет действие блока на основе его материала
     * Теперь использует конфигурацию из coding_blocks.yml
     */
    private String determineActionFromMaterial(Material material) {
        return blockConfigService.getDefaultAction(material);
    }
    
    /**
     * Проверяет, является ли материал блоком кода
     * Теперь использует конфигурацию из coding_blocks.yml
     */
    private boolean isCodeBlock(Material material) {
        return blockConfigService.isCodeBlock(material);
    }
    
    /**
     * Проверяет, является ли мир dev-миром
     */
    private boolean isDevWorld(World world) {
        return world.getName().endsWith("_dev");
    }
    
    /**
     * Добавляет блок к скрипту игрока
     */
    private void addBlockToPlayerScript(Player player, CodeBlock codeBlock) {
        UUID playerId = player.getUniqueId();
        playerScriptBlocks.computeIfAbsent(playerId, k -> new ArrayList<>()).add(codeBlock);
    }
    
    /**
     * Удаляет блок из скрипта игрока
     */
    private void removeBlockFromPlayerScript(Player player, CodeBlock codeBlock) {
        UUID playerId = player.getUniqueId();
        List<CodeBlock> blocks = playerScriptBlocks.get(playerId);
        if (blocks != null) {
            blocks.remove(codeBlock);
        }
    }
    
    /**
     * Получает все блоки скрипта игрока
     */
    public List<CodeBlock> getPlayerScriptBlocks(Player player) {
        return playerScriptBlocks.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }
    
    /**
     * Создает CodeScript из блоков игрока на определенной линии
     */
    public CodeScript createScriptFromLine(Player player, int line) {
        List<CodeBlock> allBlocks = getPlayerScriptBlocks(player);
        List<CodeBlock> lineBlocks = new ArrayList<>();
        
        // Находим все блоки на указанной линии
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            Location loc = entry.getKey();
            if (DevWorldGenerator.getCodeLineFromZ(loc.getBlockZ()) == line) {
                lineBlocks.add(entry.getValue());
            }
        }
        
        // Сортируем блоки по X координате
        lineBlocks.sort((a, b) -> {
            Location locA = getLocationForBlock(a);
            Location locB = getLocationForBlock(b);
            return Integer.compare(locA.getBlockX(), locB.getBlockX());
        });
        
        if (lineBlocks.isEmpty()) return null;
        
        // Создаем скрипт с корректным конструктором
        CodeScript script = new CodeScript("script_line_" + line, true, lineBlocks.get(0));
        
        return script;
    }
    
    /**
     * Получает позицию для блока
     */
    private Location getLocationForBlock(CodeBlock block) {
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            if (entry.getValue() == block) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Очищает все связи для мира
     */
    public void clearWorldConnections(World world) {
        locationToBlock.entrySet().removeIf(entry -> entry.getKey().getWorld().equals(world));
    }
    
    /**
     * Получает все блоки в мире
     */
    public Map<Location, CodeBlock> getWorldBlocks(World world) {
        Map<Location, CodeBlock> worldBlocks = new HashMap<>();
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            if (entry.getKey().getWorld().equals(world)) {
                worldBlocks.put(entry.getKey(), entry.getValue());
            }
        }
        return worldBlocks;
    }
    
    /**
     * Получает все доступные действия для материала
     */
    public List<String> getAvailableActions(Material material) {
        return blockConfigService.getAvailableActions(material);
    }
    
    /**
     * Получает конфигурацию блока
     */
    public BlockConfigService.BlockConfig getBlockConfig(Material material) {
        return blockConfigService.getBlockConfig(material);
    }
    
    /**
     * Получает все доступные материалы блоков кода
     */
    public Set<Material> getCodeBlockMaterials() {
        return blockConfigService.getCodeBlockMaterials();
    }
    
    /**
     * Перезагружает конфигурацию блоков
     */
    public void reloadBlockConfig() {
        blockConfigService.reload();
        plugin.getLogger().info("Конфигурация блоков перезагружена");
    }
}