package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.events.CodeBlockPlacedEvent;
import com.megacreative.events.CodeBlockBrokenEvent;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Manages the structure of code blocks by creating horizontal (nextBlock)
 * and vertical (parent-child) relationships.
 */
public class CodeStructureManager implements Listener {
    
    private static final Logger LOGGER = Logger.getLogger(CodeStructureManager.class.getName());

    private final BlockPlacementHandler placementHandler;
    private final MegaCreative plugin;

    public CodeStructureManager(MegaCreative plugin, BlockPlacementHandler placementHandler) {
        this.plugin = plugin;
        this.placementHandler = placementHandler;
    }

    @EventHandler
    public void onCodeBlockPlaced(CodeBlockPlacedEvent event) {
        Location location = event.getLocation();
        CodeBlock newBlock = event.getCodeBlock();
        Player player = event.getPlayer();

        // Сразу после установки блока, обновляем все связи вокруг него.
        // Это надежнее, чем пытаться соединить только два блока.
        rebuildConnectionsAround(location, player);
    }

    @EventHandler
    public void onCodeBlockBroken(CodeBlockBrokenEvent event) {
        Location location = event.getLocation();
        Player player = event.getPlayer();
        CodeBlock brokenBlock = event.getCodeBlock();

        // При ломании блока, нужно разорвать связи
        // 1. Отсоединяем его от родителя
        findParentBlock(location).ifPresent(parent -> {
            parent.getChildren().remove(brokenBlock);
            player.sendMessage("§e[Debug] Detached from parent at " + formatLoc(parent.getX(), parent.getZ()));
        });
        
        // 2. Отсоединяем от предыдущего блока
        findPreviousBlockInLine(location).ifPresent(prev -> {
            if (prev.getNextBlock() == brokenBlock) {
                 prev.setNextBlock(null);
                 player.sendMessage("§e[Debug] Detached from previous block at " + formatLoc(prev.getX(), prev.getZ()));
            }
        });
        
        // Перестраиваем связи для соседних блоков, чтобы они соединились между собой
        rebuildConnectionsAround(location, player);
    }
    
    /**
     * Rebuilds connections for the block at the given location and its neighbors.
     * This is a robust way to ensure the structure is always correct.
     */
    public void rebuildConnectionsAround(Location location, Player player) {
        LOGGER.info("[CodeStructure] Rebuilding connections around " + formatLoc(location));
        
        // 1. Находим и связываем текущий блок
        CodeBlock currentBlock = placementHandler.getCodeBlock(location);
        if (currentBlock != null) {
            // Сбрасываем старые связи
            currentBlock.setNextBlock(null);
            
            // Связываем с предыдущим блоком
            findPreviousBlockInLine(location).ifPresent(prev -> {
                prev.setNextBlock(currentBlock);
                player.sendMessage("§a[Debug] Linked " + formatLoc(prev.getX(), prev.getZ()) + " -> " + formatLoc(currentBlock.getX(), currentBlock.getZ()));
                LOGGER.info("[CodeStructure] Linked " + prev.getAction() + " -> " + currentBlock.getAction());
            });

            // Находим и связываем с родительским блоком
            findParentBlock(location).ifPresent(parent -> {
                if (!parent.getChildren().contains(currentBlock)) {
                    parent.addChild(currentBlock);
                    player.sendMessage("§a[Debug] Child " + formatLoc(currentBlock.getX(), currentBlock.getZ()) + " added to parent " + formatLoc(parent.getX(), parent.getZ()));
                    LOGGER.info("[CodeStructure] Added " + currentBlock.getAction() + " as child to " + parent.getAction());
                }
            });
        }
        
        // 2. Проверяем следующий блок, чтобы он подхватил связь, если нужно
        Location nextLocation = getNextLocationInLine(location);
        CodeBlock nextBlock = placementHandler.getCodeBlock(nextLocation);
        if (nextBlock != null && currentBlock != null) {
            currentBlock.setNextBlock(nextBlock);
            player.sendMessage("§a[Debug] Linked " + formatLoc(currentBlock.getX(), currentBlock.getZ()) + " -> " + formatLoc(nextBlock.getX(), nextBlock.getZ()));
             LOGGER.info("[CodeStructure] Linked " + currentBlock.getAction() + " -> " + nextBlock.getAction());
        }
    }
    
    /**
     * Finds the parent block for a given location. A parent is a control block on a previous
     * line with less indentation.
     */
    private Optional<CodeBlock> findParentBlock(Location childLocation) {
        int childX = childLocation.getBlockX();
        int childZ = childLocation.getBlockZ();

        // Идем по линиям (Z) вверх от текущей
        for (int z = childZ - DevWorldGenerator.getLinesSpacing(); z >= 0; z -= DevWorldGenerator.getLinesSpacing()) {
            // Идем по блокам (X) слева направо до отступа дочернего блока
            for (int x = 0; x < childX; x++) {
                Location potentialParentLoc = new Location(childLocation.getWorld(), x, childLocation.getY(), z);
                CodeBlock potentialParent = placementHandler.getCodeBlock(potentialParentLoc);
                
                // Родитель должен существовать, быть блоком-условия/цикла и не быть скобкой
                if (potentialParent != null && isControlBlock(potentialParent)) {
                    LOGGER.info("[CodeStructure] Found potential parent: " + potentialParent.getAction() + " at " + formatLoc(x, z));
                    return Optional.of(potentialParent);
                }
            }
        }
        return Optional.empty();
    }
    
    /**
     * Finds the block immediately to the left on the same line.
     */
    private Optional<CodeBlock> findPreviousBlockInLine(Location currentLocation) {
        int x = currentLocation.getBlockX();
        if (x > 0) {
            Location prevLocation = new Location(currentLocation.getWorld(), x - 1, currentLocation.getY(), currentLocation.getZ());
            return Optional.ofNullable(placementHandler.getCodeBlock(prevLocation));
        }
        return Optional.empty();
    }
    
    private Location getNextLocationInLine(Location location) {
        return location.clone().add(1, 0, 0);
    }
    
    /**
     * A control block is one that can have children (e.g., IF, REPEAT).
     * This logic should eventually be data-driven from your block config.
     */
    private boolean isControlBlock(CodeBlock block) {
        if (block == null || block.getAction() == null) return false;
        
        // Скобки не могут быть родителями
        if (block.isBracket()) return false;
        
        // Примерный список типов блоков, которые могут иметь дочерние элементы
        switch (block.getMaterialName()) {
            case "OAK_PLANKS":  // CONDITION
            case "OBSIDIAN":    // IF_VAR
            case "REDSTONE_BLOCK": // IF_GAME
            case "BRICKS":      // IF_MOB
            case "EMERALD_BLOCK": // REPEAT
            case "END_STONE":   // ELSE
                return true;
            default:
                return false;
        }
    }
    
    private String formatLoc(Location loc) {
        if (loc == null) return "null";
        return "(" + loc.getBlockX() + ", " + loc.getBlockZ() + ")";
    }
     private String formatLoc(int x, int z) {
        return "(" + x + ", " + z + ")";
    }
}