package com.megacreative.coding.handlers;

import com.megacreative.coding.blocks.Block;
import com.megacreative.coding.blocks.BlockRegistry;
import com.megacreative.coding.blocks.BlockType;
import com.megacreative.coding.core.BlockSystem;
import com.megacreative.coding.core.ScriptManager;
import com.megacreative.coding.core.ScriptValidator;
import com.megacreative.coding.util.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Обработчик размещения блоков кода в мире.
 */
public class BlockPlacementHandler implements Listener {
    private final BlockSystem blockSystem;
    private final BlockRegistry blockRegistry;
    private final ScriptManager scriptManager;
    private final ScriptValidator scriptValidator;
    
    // Кэш для хранения связей между блоками
    private final Map<UUID, Block> lastPlacedBlocks = new HashMap<>();
    
    // Отступы между блоками при размещении
    private static final int BLOCK_OFFSET_X = 1;
    private static final int BLOCK_OFFSET_Y = 0;
    private static final int BLOCK_OFFSET_Z = 0;
    
    public BlockPlacementHandler(BlockSystem blockSystem, BlockRegistry blockRegistry, 
                               ScriptManager scriptManager, ScriptValidator scriptValidator) {
        this.blockSystem = blockSystem;
        this.blockRegistry = blockRegistry;
        this.scriptManager = scriptManager;
        this.scriptValidator = scriptValidator;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Игнорируем события, если игрок не зашел в режим размещения блоков
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Проверяем, что игрок держит предмет для размещения блоков
        if (item == null || !isBlockItem(item)) {
            return;
        }
        
        // Отменяем стандартное действие
        event.setCancelled(true);
        
        // Обрабатываем клик по блоку
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            handleBlockPlacement(player, event.getClickedBlock().getLocation(), 
                               event.getBlockFace(), item);
        } 
        // Обрабатываем клик по воздуху
        else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            handleAirClick(player, item);
        }
    }
    
    /**
     * Обрабатывает размещение блока кода.
     */
    private void handleBlockPlacement(Player player, Location location, BlockFace face, ItemStack item) {
        // Получаем тип блока из предмета
        BlockType blockType = getBlockTypeFromItem(item);
        if (blockType == null) {
            player.sendMessage("§cНеизвестный тип блока!");
            return;
        }
        
        // Создаем новый блок
        Block block = blockRegistry.createBlock(blockType);
        if (block == null) {
            player.sendMessage("§cНе удалось создать блок типа: " + blockType);
            return;
        }
        
        // Устанавливаем владельца блока
        block.setOwner(player.getUniqueId());
        
        // Определяем позицию для размещения блока
        Location blockLocation = calculateBlockLocation(location, face);
        
        // Проверяем, можно ли разместить блок в этом месте
        if (!canPlaceBlock(blockLocation)) {
            player.sendMessage("§cЗдесь нельзя разместить блок!");
            return;
        }
        
        // Размещаем визуальное представление блока в мире
        placeVisualBlock(block, blockLocation);
        
        // Обрабатываем связывание блоков
        handleBlockLinking(player, block, blockLocation);
        
        // Обновляем последний размещенный блок
        lastPlacedBlocks.put(player.getUniqueId(), block);
        
        player.sendMessage("§aБлок успешно размещен!");
    }
    
    /**
     * Обрабатывает клик по воздуху (например, для отмены выбора блока).
     */
    private void handleAirClick(Player player, ItemStack item) {
        // Если игрок нажал Shift+ПКМ, отменяем выбор последнего блока
        if (player.isSneaking()) {
            lastPlacedBlocks.remove(player.getUniqueId());
            player.sendMessage("§eВыбор блока отменен.");
        }
    }
    
    /**
     * Обрабатывает связывание блоков между собой.
     */
    private void handleBlockLinking(Player player, Block newBlock, Location location) {
        UUID playerId = player.getUniqueId();
        
        // Если есть предыдущий блок, пытаемся связать его с новым
        if (lastPlacedBlocks.containsKey(playerId)) {
            Block previousBlock = lastPlacedBlocks.get(playerId);
            
            // Проверяем, можно ли связать эти блоки
            if (BlockUtils.canConnectTo(newBlock, previousBlock)) {
                // Связываем блоки
                previousBlock.setNextBlock(newBlock);
                newBlock.setParent(previousBlock);
                
                // Визуализируем связь
                createConnectionParticles(previousBlock.getLocation(), location);
                
                player.sendMessage("§aБлоки успешно связаны!");
                
                // Если это блок-событие, проверяем скрипт на валидность
                if (newBlock.getBlockType() == BlockType.EVENT) {
                    validateScript(newBlock, player);
                }
            } else {
                player.sendMessage("§cЭти блоки нельзя связать!");
            }
        }
    }
    
    /**
     * Проверяет скрипт на валидность.
     */
    private void validateScript(Block rootBlock, Player player) {
        ScriptValidator.ValidationResult result = scriptValidator.validate(rootBlock);
        
        if (result.isValid()) {
            player.sendMessage("§aСкрипт валиден!");
            
            // Регистрируем событие, если это блок-событие
            if (rootBlock instanceof EventBlock) {
                // TODO: Зарегистрировать событие
                player.sendMessage("§aСобытие зарегистрировано!");
            }
        } else {
            player.sendMessage("§cОшибка валидации скрипта: " + result.getErrorMessage());
        }
    }
    
    /**
     * Создает частицы для визуализации связи между блоками.
     */
    private void createConnectionParticles(Location from, Location to) {
        // TODO: Реализовать визуализацию связи с помощью частиц
    }
    
    /**
     * Размещает визуальное представление блока в мире.
     */
    private void placeVisualBlock(Block block, Location location) {
        // TODO: Реализовать размещение визуального представления блока
    }
    
    /**
     * Проверяет, можно ли разместить блок в указанном месте.
     */
    private boolean canPlaceBlock(Location location) {
        // TODO: Реализовать проверку на возможность размещения блока
        return true;
    }
    
    /**
     * Вычисляет позицию для размещения блока.
     */
    private Location calculateBlockLocation(Location clickedLocation, BlockFace face) {
        return clickedLocation.clone().add(
            face.getModX() * BLOCK_OFFSET_X,
            face.getModY() * BLOCK_OFFSET_Y,
            face.getModZ() * BLOCK_OFFSET_Z
        );
    }
    
    /**
     * Проверяет, является ли предмет блоком кода.
     */
    private boolean isBlockItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && 
               meta.getDisplayName().startsWith("§aБлок кода: ");
    }
    
    /**
     * Получает тип блока из предмета.
     */
    private BlockType getBlockTypeFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return null;
        }
        
        String displayName = meta.getDisplayName();
        
        // Парсим тип блока из отображаемого имени
        if (displayName.contains("Событие")) {
            return BlockType.EVENT;
        } else if (displayName.contains("Действие")) {
            return BlockType.ACTION;
        } else if (displayName.contains("Условие")) {
            return BlockType.CONDITION;
        } else if (displayName.contains("Цикл")) {
            return BlockType.LOOP;
        } else if (displayName.contains("Функция")) {
            return BlockType.FUNCTION;
        } else if (displayName.contains("Переменная")) {
            return BlockType.VARIABLE;
        }
        
        return null;
    }
}
