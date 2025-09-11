package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.CodingItems;
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
 * Handles code block moving functionality similar to FrameLand
 * Provides copy/paste functionality for code chains using a redstone comparator tool
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
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Check if this is our code mover tool and we're in a dev world
        if (!isInDevWorld(player) || !isMoverTool(item)) {
            return;
        }
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            Location clickedLoc = event.getClickedBlock().getLocation();
            BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
            
            if (player.isSneaking()) {
                // COPY - Shift + Right Click
                copyCodeChain(player, clickedLoc, placementHandler);
            } else {
                // PASTE - Right Click
                pasteCodeChain(player, clickedLoc, placementHandler);
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            // LEFT CLICK - Clear clipboard
            clearClipboard(player);
        }
    }
    
    /**
     * Copies a code chain starting from the clicked block
     */
    private void copyCodeChain(Player player, Location startLoc, BlockPlacementHandler placementHandler) {
        // Find the root CodeBlock to start copying from
        CodeBlock startBlock = placementHandler.getCodeBlock(startLoc);
        if (startBlock == null) {
            player.sendMessage("§cЭто не блок кода!");
            return;
        }
        
        // Collect the entire chain of blocks (including children) into a list
        List<CodeBlock> chain = new ArrayList<>();
        List<Location> locations = new ArrayList<>();
        collectChain(startBlock, startLoc, chain, locations, placementHandler);
        
        // Save this chain to the player's clipboard
        clipboard.put(player.getUniqueId(), new ArrayList<>(chain));
        clipboardLocations.put(player.getUniqueId(), new ArrayList<>(locations));
        
        player.sendMessage("§aЦепочка из " + chain.size() + " блоков скопирована!");
        player.sendMessage("§7Используйте ПКМ для вставки, ЛКМ для очистки буфера");
        
        plugin.getLogger().info("Player " + player.getName() + " copied " + chain.size() + " code blocks");
    }
    
    /**
     * Pastes the copied code chain at the specified location
     */
    private void pasteCodeChain(Player player, Location pasteLoc, BlockPlacementHandler placementHandler) {
        // Get the chain from clipboard
        List<CodeBlock> chainToPaste = clipboard.get(player.getUniqueId());
        List<Location> originalLocations = clipboardLocations.get(player.getUniqueId());
        
        if (chainToPaste == null || chainToPaste.isEmpty()) {
            player.sendMessage("§cВаш буфер обмена пуст! Используйте Shift+ПКМ для копирования.");
            return;
        }
        
        // Check if the paste location is valid
        if (!DevWorldGenerator.isValidCodePosition(pasteLoc.getBlockX(), pasteLoc.getBlockZ())) {
            player.sendMessage("§cНельзя вставить код в эту позицию! Используйте линии кодирования.");
            return;
        }
        
        // Calculate offset from original first block to paste location
        Location originalFirst = originalLocations.get(0);\n        int xOffset = pasteLoc.getBlockX() - originalFirst.getBlockX();\n        int zOffset = pasteLoc.getBlockZ() - originalFirst.getBlockZ();\n        \n        // Remove old blocks from their locations\n        for (Location oldLoc : originalLocations) {\n            Block block = oldLoc.getBlock();\n            if (block.getType() != Material.AIR) {\n                block.setType(Material.AIR);\n                // Remove signs and containers\n                removeSignFromBlock(oldLoc);\n                removeContainerAboveBlock(oldLoc);\n            }\n            placementHandler.getBlockCodeBlocks().remove(oldLoc);\n        }\n        \n        // Create copies of blocks to avoid modifying originals in clipboard\n        List<CodeBlock> newChain = new ArrayList<>();\n        List<Location> newLocations = new ArrayList<>();\n        \n        for (int i = 0; i < chainToPaste.size(); i++) {\n            CodeBlock oldBlock = chainToPaste.get(i);\n            Location oldLoc = originalLocations.get(i);\n            \n            // Calculate new location with offset\n            Location newLoc = new Location(\n                pasteLoc.getWorld(),\n                oldLoc.getBlockX() + xOffset,\n                pasteLoc.getBlockY(), // Use paste location Y\n                oldLoc.getBlockZ() + zOffset\n            );\n            \n            // Validate new location\n            if (!DevWorldGenerator.isValidCodePosition(newLoc.getBlockX(), newLoc.getBlockZ())) {\n                player.sendMessage("§cНе удается вставить блок в позицию: \" + newLoc.getBlockX() + \", \" + newLoc.getBlockZ());\n                continue;\n            }\n            \n            // Create a deep copy of the block\n            CodeBlock newBlock = oldBlock.clone();\n            newChain.add(newBlock);\n            newLocations.add(newLoc);\n            \n            // Place the block physically\n            Block physicalBlock = newLoc.getBlock();\n            physicalBlock.setType(newBlock.getMaterial());\n            \n            // Handle special block types\n            if (newBlock.isBracket()) {\n                setPistonDirection(physicalBlock, newBlock.getBracketType());\n                updateBracketSign(newLoc, newBlock.getBracketType());\n            } else {\n                // Create normal sign\n                setSignOnBlock(newLoc, getBlockDisplayName(newBlock));\n            }\n            \n            // Add to placement handler\n            placementHandler.getBlockCodeBlocks().put(newLoc, newBlock);\n        }\n        \n        // Rebuild connections for the entire world\n        plugin.getServiceRegistry().getAutoConnectionManager().rebuildWorldConnections(player.getWorld());\n        \n        player.sendMessage("§aЦепочка из " + newChain.size() + " блоков вставлена!");\n        plugin.getLogger().info("Player " + player.getName() + " pasted " + newChain.size() + " code blocks");\n    }\n    \n    /**\n     * Clears the player's clipboard\n     */\n    private void clearClipboard(Player player) {\n        clipboard.remove(player.getUniqueId());\n        clipboardLocations.remove(player.getUniqueId());\n        player.sendMessage("§7Буфер обмена очищен.");\n    }\n    \n    /**\n     * Recursively collects a chain of connected blocks\n     */\n    private void collectChain(CodeBlock currentBlock, Location currentLoc, \n                             List<CodeBlock> chain, List<Location> locations, \n                             BlockPlacementHandler placementHandler) {\n        if (currentBlock == null || chain.contains(currentBlock)) {\n            return; // Avoid infinite loops\n        }\n        \n        // Add current block to chain\n        chain.add(currentBlock);\n        locations.add(currentLoc);\n        \n        // Follow next block in sequence\n        CodeBlock nextBlock = currentBlock.getNextBlock();\n        if (nextBlock != null) {\n            Location nextLoc = findLocationOfBlock(nextBlock, placementHandler);\n            if (nextLoc != null) {\n                collectChain(nextBlock, nextLoc, chain, locations, placementHandler);\n            }\n        }\n        \n        // Follow child blocks (for conditionals, loops)\n        for (CodeBlock childBlock : currentBlock.getChildren()) {\n            Location childLoc = findLocationOfBlock(childBlock, placementHandler);\n            if (childLoc != null) {\n                collectChain(childBlock, childLoc, chain, locations, placementHandler);\n            }\n        }\n    }\n    \n    /**\n     * Finds the location of a CodeBlock in the world\n     */\n    private Location findLocationOfBlock(CodeBlock targetBlock, BlockPlacementHandler placementHandler) {\n        for (Map.Entry<Location, CodeBlock> entry : placementHandler.getBlockCodeBlocks().entrySet()) {\n            if (entry.getValue().equals(targetBlock)) {\n                return entry.getKey();\n            }\n        }\n        return null;\n    }\n    \n    /**\n     * Checks if the item is our code mover tool\n     */\n    private boolean isMoverTool(ItemStack item) {\n        if (item == null || item.getType() != Material.REDSTONE_COMPARATOR) {\n            return false;\n        }\n        \n        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {\n            return false;\n        }\n        \n        return item.getItemMeta().getDisplayName().contains("Перемещатель кода");\n    }\n    \n    /**\n     * Checks if player is in a dev world\n     */\n    private boolean isInDevWorld(Player player) {\n        return player.getWorld().getName().endsWith("_dev");\n    }\n    \n    // Helper methods for block placement (similar to BlockPlacementHandler)\n    \n    private void setPistonDirection(Block pistonBlock, CodeBlock.BracketType bracketType) {\n        if (pistonBlock.getBlockData() instanceof org.bukkit.block.data.type.Piston pistonData) {\n            if (bracketType == CodeBlock.BracketType.OPEN) {\n                pistonData.setFacing(org.bukkit.block.BlockFace.EAST);\n            } else {\n                pistonData.setFacing(org.bukkit.block.BlockFace.WEST);\n            }\n            pistonBlock.setBlockData(pistonData);\n        }\n    }\n    \n    private void updateBracketSign(Location location, CodeBlock.BracketType bracketType) {\n        // Implementation similar to BlockPlacementHandler.updateBracketSign()\n        removeSignFromBlock(location);\n        \n        Block block = location.getBlock();\n        org.bukkit.block.BlockFace[] faces = {org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.EAST, \n                                             org.bukkit.block.BlockFace.SOUTH, org.bukkit.block.BlockFace.WEST};\n        \n        for (org.bukkit.block.BlockFace face : faces) {\n            Block signBlock = block.getRelative(face);\n            if (signBlock.getType().isAir()) {\n                signBlock.setType(Material.OAK_WALL_SIGN, false);\n                \n                org.bukkit.block.data.type.WallSign wallSignData = (org.bukkit.block.data.type.WallSign) signBlock.getBlockData();\n                wallSignData.setFacing(face);\n                signBlock.setBlockData(wallSignData);\n                \n                org.bukkit.block.Sign signState = (org.bukkit.block.Sign) signBlock.getState();\n                signState.setLine(0, "§8============");\n                signState.setLine(1, "§6" + bracketType.getSymbol() + " Скобка");\n                signState.setLine(2, "§7ПКМ для смены");\n                signState.setLine(3, "§8============");\n                signState.update(true);\n                return;\n            }\n        }\n    }\n    \n    private void setSignOnBlock(Location location, String text) {\n        // Implementation similar to BlockPlacementHandler.setSignOnBlock()\n        removeSignFromBlock(location);\n        \n        Block block = location.getBlock();\n        org.bukkit.block.BlockFace[] faces = {org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.EAST, \n                                             org.bukkit.block.BlockFace.SOUTH, org.bukkit.block.BlockFace.WEST};\n        \n        for (org.bukkit.block.BlockFace face : faces) {\n            Block signBlock = block.getRelative(face);\n            if (signBlock.getType().isAir()) {\n                signBlock.setType(Material.OAK_WALL_SIGN, false);\n                \n                org.bukkit.block.data.type.WallSign wallSignData = (org.bukkit.block.data.type.WallSign) signBlock.getBlockData();\n                wallSignData.setFacing(face);\n                signBlock.setBlockData(wallSignData);\n                \n                org.bukkit.block.Sign signState = (org.bukkit.block.Sign) signBlock.getState();\n                signState.setLine(0, "§8============");\n                String line2 = text.length() > 15 ? text.substring(0, 15) : text;\n                signState.setLine(1, line2);\n                signState.setLine(2, "§7Кликните ПКМ");\n                signState.setLine(3, "§8============");\n                signState.update(true);\n                return;\n            }\n        }\n    }\n    \n    private void removeSignFromBlock(Location location) {\n        Block block = location.getBlock();\n        org.bukkit.block.BlockFace[] faces = {org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.SOUTH, \n                                             org.bukkit.block.BlockFace.EAST, org.bukkit.block.BlockFace.WEST};\n        \n        for (org.bukkit.block.BlockFace face : faces) {\n            Block signBlock = block.getRelative(face);\n            if (signBlock.getBlockData() instanceof org.bukkit.block.data.type.WallSign) {\n                signBlock.setType(Material.AIR);\n            }\n        }\n    }\n    \n    private void removeContainerAboveBlock(Location blockLocation) {\n        Location containerLocation = blockLocation.clone().add(0, 1, 0);\n        Block containerBlock = containerLocation.getBlock();\n        \n        if (containerBlock.getType() == Material.CHEST) {\n            containerBlock.setType(Material.AIR);\n        }\n    }\n    \n    private String getBlockDisplayName(CodeBlock codeBlock) {\n        var config = plugin.getServiceRegistry().getBlockConfigService().getBlockConfig(codeBlock.getAction());\n        return config != null ? config.getDisplayName() : codeBlock.getAction();\n    }\n}