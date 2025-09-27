package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.CodeBlock;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Enhanced CodeMoverListener for moving and reorganizing code blocks
 * Supports copying entire chains with structure preservation
 */
public class CodeMoverListener implements Listener {
    
    private final MegaCreative plugin;
    private final Map<UUID, List<CodeBlock>> clipboard = new HashMap<>();
    private final Map<UUID, List<Location>> clipboardLocations = new HashMap<>();
    
    public CodeMoverListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Fix double firing by only processing main hand
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Check if player is using the code mover tool
        if (!isMoverTool(item)) {
            return;
        }
        
        // Only work in dev worlds
        if (!isInDevWorld(player)) {
            player.sendMessage("§cИнструмент перемещения работает только в мирах разработки!");
            return;
        }
        
        event.setCancelled(true);
        
        // Handle different actions
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
            // Shift + Right-click: Copy chain
            copyChain(player, event.getClickedBlock());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Right-click: Paste chain
            pasteChain(player, event.getClickedBlock());
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // Left-click: Clear clipboard
            clearClipboard(player);
        }
    }
    
    /**
     * Copies an entire chain starting from the clicked block
     */
    private void copyChain(Player player, Block clickedBlock) {
        if (clickedBlock == null) {
            player.sendMessage("§cОшибка: Не удалось определить блок!");
            return;
        }
        
        BlockPlacementHandler placementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
        if (placementHandler == null) {
            player.sendMessage("§cОшибка: Система размещения блоков недоступна!");
            return;
        }
        
        Location location = clickedBlock.getLocation();
        CodeBlock codeBlock = placementHandler.getCodeBlock(location);
        
        if (codeBlock == null) {
            player.sendMessage("§cЭто не блок кода!");
            return;
        }
        
        // Collect the entire chain starting from this block
        List<CodeBlock> chain = new ArrayList<>();
        List<Location> locations = new ArrayList<>();
        
        collectChain(codeBlock, location, chain, locations, placementHandler);
        
        if (chain.isEmpty()) {
            player.sendMessage("§cНе удалось собрать цепочку блоков!");
            return;
        }
        
        // Store in clipboard
        clipboard.put(player.getUniqueId(), chain);
        clipboardLocations.put(player.getUniqueId(), locations);
        
        player.sendMessage("§a✓ Скопирована цепочка из " + chain.size() + " блоков!");
        plugin.getLogger().info("Player " + player.getName() + " copied " + chain.size() + " code blocks");
    }
    
    /**
     * Pastes the chain from clipboard to the target location
     */
    private void pasteChain(Player player, Block clickedBlock) {
        if (clickedBlock == null) {
            player.sendMessage("§cОшибка: Не удалось определить позицию для вставки!");
            return;
        }
        
        UUID playerId = player.getUniqueId();
        List<CodeBlock> chainToPaste = clipboard.get(playerId);
        List<Location> originalLocations = clipboardLocations.get(playerId);
        
        if (chainToPaste == null || chainToPaste.isEmpty()) {
            player.sendMessage("§cБуфер обмена пуст! Сначала скопируйте блоки.");
            return;
        }
        
        BlockPlacementHandler placementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
        if (placementHandler == null) {
            player.sendMessage("§cОшибка: Система размещения блоков недоступна!");
            return;
        }
        
        Location pasteLoc = clickedBlock.getLocation();
        
        // Calculate offset from original first block to paste location
        Location originalFirst = originalLocations.get(0);
        int xOffset = pasteLoc.getBlockX() - originalFirst.getBlockX();
        int zOffset = pasteLoc.getBlockZ() - originalFirst.getBlockZ();
        
        // Remove old blocks from their locations
        for (Location oldLoc : originalLocations) {
            Block block = oldLoc.getBlock();
            if (block.getType() != Material.AIR) {
                block.setType(Material.AIR);
                // Remove signs and containers
                removeSignFromBlock(oldLoc);
                removeContainerAboveBlock(oldLoc);
            }
            placementHandler.getBlockCodeBlocks().remove(oldLoc);
        }
        
        // Create copies of blocks to avoid modifying originals in clipboard
        List<CodeBlock> newChain = new ArrayList<>();
        List<Location> newLocations = new ArrayList<>();
        
        for (int i = 0; i < chainToPaste.size(); i++) {
            CodeBlock oldBlock = chainToPaste.get(i);
            Location oldLoc = originalLocations.get(i);
            
            // Calculate new location with offset
            Location newLoc = new Location(
                pasteLoc.getWorld(),
                oldLoc.getBlockX() + xOffset,
                pasteLoc.getBlockY(), // Use paste location Y
                oldLoc.getBlockZ() + zOffset
            );
            
            // Validate new location
            if (!isValidCodePosition(newLoc.getBlockX(), newLoc.getBlockZ())) {
                player.sendMessage("§cНе удается вставить блок в позицию: " + newLoc.getBlockX() + ", " + newLoc.getBlockZ());
                continue;
            }
            
            // Create a deep copy of the block
            CodeBlock newBlock = oldBlock.clone();
            newChain.add(newBlock);
            newLocations.add(newLoc);
            
            // Place the block physically
            Block physicalBlock = newLoc.getBlock();
            physicalBlock.setType(newBlock.getMaterial());
            
            // Handle special block types
            if (newBlock.isBracket()) {
                setPistonDirection(physicalBlock, newBlock.getBracketType());
                updateBracketSign(newLoc, newBlock.getBracketType());
            } else {
                // Create normal sign
                setSignOnBlock(newLoc, getBlockDisplayName(newBlock));
            }
            
            // Add to placement handler
            placementHandler.getBlockCodeBlocks().put(newLoc, newBlock);
        }
        
        // In the new architecture, connections are handled automatically by BlockLinker and BlockHierarchyManager
        // No need to manually rebuild connections
        
        player.sendMessage("§aЦепочка из " + newChain.size() + " блоков вставлена!");
        plugin.getLogger().info("Player " + player.getName() + " pasted " + newChain.size() + " code blocks");
    }
    
    /**
     * Clears the player's clipboard
     */
    private void clearClipboard(Player player) {
        clipboard.remove(player.getUniqueId());
        clipboardLocations.remove(player.getUniqueId());
        player.sendMessage("§7Буфер обмена очищен.");
    }
    
    /**
     * Recursively collects a chain of connected blocks
     */
    private void collectChain(CodeBlock currentBlock, Location currentLoc, 
                             List<CodeBlock> chain, List<Location> locations, 
                             BlockPlacementHandler placementHandler) {
        if (currentBlock == null || chain.contains(currentBlock)) {
            return; // Avoid infinite loops
        }
        
        // Add current block to chain
        chain.add(currentBlock);
        locations.add(currentLoc);
        
        // Follow next block in sequence
        CodeBlock nextBlock = currentBlock.getNextBlock();
        if (nextBlock != null) {
            Location nextLoc = findLocationOfBlock(nextBlock, placementHandler);
            if (nextLoc != null) {
                collectChain(nextBlock, nextLoc, chain, locations, placementHandler);
            }
        }
        
        // Follow child blocks (for conditionals, loops)
        for (CodeBlock childBlock : currentBlock.getChildren()) {
            Location childLoc = findLocationOfBlock(childBlock, placementHandler);
            if (childLoc != null) {
                collectChain(childBlock, childLoc, chain, locations, placementHandler);
            }
        }
    }
    
    /**
     * Finds the location of a CodeBlock in the world
     */
    private Location findLocationOfBlock(CodeBlock targetBlock, BlockPlacementHandler placementHandler) {
        for (Map.Entry<Location, CodeBlock> entry : placementHandler.getBlockCodeBlocks().entrySet()) {
            if (entry.getValue().equals(targetBlock)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Checks if the item is our code mover tool
     */
    private boolean isMoverTool(ItemStack item) {
        if (item == null || item.getType() != Material.COMPARATOR) {
            return false;
        }
        
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        
        return item.getItemMeta().getDisplayName().contains("Инструмент Перемещения");
    }
    
    /**
     * Checks if player is in a dev world
     */
    private boolean isInDevWorld(Player player) {
        return player.getWorld().getName().endsWith("_dev");
    }
    
    // Helper methods for block placement (similar to BlockPlacementHandler)
    
    private void setPistonDirection(Block pistonBlock, CodeBlock.BracketType bracketType) {
        if (pistonBlock.getBlockData() instanceof org.bukkit.block.data.type.Piston pistonData) {
            if (bracketType == CodeBlock.BracketType.OPEN) {
                pistonData.setFacing(org.bukkit.block.BlockFace.EAST);
            } else {
                pistonData.setFacing(org.bukkit.block.BlockFace.WEST);
            }
            pistonBlock.setBlockData(pistonData);
        }
    }
    
    private void updateBracketSign(Location location, CodeBlock.BracketType bracketType) {
        // Implementation similar to BlockPlacementHandler.updateBracketSign()
        removeSignFromBlock(location);
        
        Block block = location.getBlock();
        org.bukkit.block.BlockFace[] faces = {org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.EAST, 
                                             org.bukkit.block.BlockFace.SOUTH, org.bukkit.block.BlockFace.WEST};
        
        for (org.bukkit.block.BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            if (signBlock.getType().isAir()) {
                signBlock.setType(Material.OAK_WALL_SIGN, false);
                
                org.bukkit.block.data.type.WallSign wallSignData = (org.bukkit.block.data.type.WallSign) signBlock.getBlockData();
                wallSignData.setFacing(face);
                signBlock.setBlockData(wallSignData);
                
                org.bukkit.block.Sign signState = (org.bukkit.block.Sign) signBlock.getState();
                signState.setLine(0, "§8============");
                signState.setLine(1, "§6" + bracketType.getSymbol() + " Скобка");
                signState.setLine(2, "§7ПКМ для смены");
                signState.setLine(3, "§8============");
                signState.update(true);
                return;
            }
        }
    }
    
    private void setSignOnBlock(Location location, String text) {
        // Implementation similar to BlockPlacementHandler.setSignOnBlock()
        removeSignFromBlock(location);
        
        Block block = location.getBlock();
        org.bukkit.block.BlockFace[] faces = {org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.EAST, 
                                             org.bukkit.block.BlockFace.SOUTH, org.bukkit.block.BlockFace.WEST};
        
        for (org.bukkit.block.BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            if (signBlock.getType().isAir()) {
                signBlock.setType(Material.OAK_WALL_SIGN, false);
                
                org.bukkit.block.data.type.WallSign wallSignData = (org.bukkit.block.data.type.WallSign) signBlock.getBlockData();
                wallSignData.setFacing(face);
                signBlock.setBlockData(wallSignData);
                
                org.bukkit.block.Sign signState = (org.bukkit.block.Sign) signBlock.getState();
                signState.setLine(0, "§8============");
                String line2 = text.length() > 15 ? text.substring(0, 15) : text;
                signState.setLine(1, line2);
                signState.setLine(2, "§7Кликните ПКМ");
                signState.setLine(3, "§8============");
                signState.update(true);
                return;
            }
        }
    }
    
    private void removeSignFromBlock(Location location) {
        Block block = location.getBlock();
        org.bukkit.block.BlockFace[] faces = {org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.SOUTH, 
                                             org.bukkit.block.BlockFace.EAST, org.bukkit.block.BlockFace.WEST};
        
        for (org.bukkit.block.BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            if (signBlock.getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
                signBlock.setType(Material.AIR);
            }
        }
    }
    
    private void removeContainerAboveBlock(Location blockLocation) {
        Location containerLocation = blockLocation.clone().add(0, 1, 0);
        Block containerBlock = containerLocation.getBlock();
        
        if (containerBlock.getType() == Material.CHEST) {
            containerBlock.setType(Material.AIR);
        }
    }
    
    private String getBlockDisplayName(CodeBlock codeBlock) {
        var config = plugin.getServiceRegistry().getBlockConfigService().getBlockConfig(codeBlock.getAction());
        return config != null ? config.getDisplayName() : codeBlock.getAction();
    }
    
    /**
     * Validates if a position is valid for code placement
     */
    private boolean isValidCodePosition(int x, int z) {
        // Basic validation - can be enhanced based on DevWorldGenerator logic
        return x >= 0 && x < 50 && z >= 0; // Adjust limits as needed
    }
}