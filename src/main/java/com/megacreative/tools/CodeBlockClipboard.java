package com.megacreative.tools;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import lombok.Data;
import lombok.extern.java.Log;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Tool for copying and pasting code block logic and structures
 */
@Log
public class CodeBlockClipboard {
    
    // Player clipboard storage
    private final Map<UUID, ClipboardData> playerClipboards = new ConcurrentHashMap<>();
    
    // Shared clipboards (can be accessed by multiple players)
    private final Map<String, ClipboardData> sharedClipboards = new ConcurrentHashMap<>();
    
    /**
     * Copies a single code block to the player's clipboard
     */
    public void copyBlock(Player player, CodeBlock block) {
        ClipboardData data = new ClipboardData(ClipboardType.SINGLE_BLOCK);
        data.addBlock(block);
        
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Copied code block: " + block.getAction());
        
        log.fine("Player " + player.getName() + " copied block: " + block.getAction());
    }
    
    /**
     * Copies a chain of connected code blocks
     */
    public void copyChain(Player player, CodeBlock startBlock) {
        ClipboardData data = new ClipboardData(ClipboardType.BLOCK_CHAIN);
        
        // Follow the chain and collect all blocks
        CodeBlock current = startBlock;
        Set<UUID> visitedBlocks = new HashSet<>();
        
        while (current != null && !visitedBlocks.contains(current.getId())) {
            data.addBlock(current);
            visitedBlocks.add(current.getId());
            current = current.getNextBlock();
        }
        
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Copied code chain: " + data.getBlocks().size() + " blocks");
        
        log.fine("Player " + player.getName() + " copied chain of " + data.getBlocks().size() + " blocks");
    }
    
    /**
     * Copies a region of code blocks (rectangular selection)
     */
    public void copyRegion(Player player, Location corner1, Location corner2) {
        ClipboardData data = new ClipboardData(ClipboardType.REGION);
        
        // Calculate bounds
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        data.setBounds(minX, minY, minZ, maxX, maxY, maxZ);
        
        // Collect all code blocks in the region
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(corner1.getWorld(), x, y, z);
                    Material material = loc.getBlock().getType();
                    
                    // Check if this is a code block (this would integrate with BlockConfigService)
                    if (isCodeBlock(material)) {\n                        // Create code block representation\n                        CodeBlock block = createCodeBlockFromLocation(loc);\n                        if (block != null) {\n                            data.addBlock(block, x - minX, y - minY, z - minZ);\n                        }\n                    }\n                }\n            }\n        }\n        \n        playerClipboards.put(player.getUniqueId(), data);\n        player.sendMessage(\"\u00a7a\u2713 Copied region: \" + data.getBlocks().size() + \" blocks (\" + \n                          (maxX - minX + 1) + \"x\" + (maxY - minY + 1) + \"x\" + (maxZ - minZ + 1) + \")\");\n        \n        log.fine(\"Player \" + player.getName() + \" copied region of \" + data.getBlocks().size() + \" blocks\");\n    }\n    \n    /**\n     * Pastes the clipboard content at the specified location\n     */\n    public void paste(Player player, Location targetLocation) {\n        ClipboardData data = playerClipboards.get(player.getUniqueId());\n        if (data == null) {\n            player.sendMessage(\"\u00a7c\u2716 No clipboard data to paste\");\n            return;\n        }\n        \n        try {\n            switch (data.getType()) {\n                case SINGLE_BLOCK -> pasteSingleBlock(player, targetLocation, data);\n                case BLOCK_CHAIN -> pasteChain(player, targetLocation, data);\n                case REGION -> pasteRegion(player, targetLocation, data);\n            }\n            \n            player.sendMessage(\"\u00a7a\u2713 Pasted \" + data.getBlocks().size() + \" blocks\");\n            \n        } catch (Exception e) {\n            player.sendMessage(\"\u00a7c\u2716 Failed to paste: \" + e.getMessage());\n            log.log(Level.WARNING, \"Failed to paste for player \" + player.getName(), e);\n        }\n    }\n    \n    /**\n     * Shows a preview of what will be pasted\n     */\n    public void showPreview(Player player, Location targetLocation) {\n        ClipboardData data = playerClipboards.get(player.getUniqueId());\n        if (data == null) {\n            player.sendMessage(\"\u00a7c\u2716 No clipboard data to preview\");\n            return;\n        }\n        \n        // Show particles at locations where blocks would be placed\n        for (Map.Entry<RelativePosition, CodeBlock> entry : data.getBlocksWithPositions().entrySet()) {\n            RelativePosition relPos = entry.getKey();\n            Location previewLoc = targetLocation.clone().add(relPos.x, relPos.y, relPos.z);\n            \n            // Show particles\n            player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, previewLoc.add(0.5, 0.5, 0.5), 5);\n        }\n        \n        player.sendMessage(\"\u00a7e\u27a4 Preview: \" + data.getBlocks().size() + \" blocks would be placed\");\n    }\n    \n    /**\n     * Saves clipboard to a shared clipboard with a name\n     */\n    public void saveToShared(Player player, String name) {\n        ClipboardData data = playerClipboards.get(player.getUniqueId());\n        if (data == null) {\n            player.sendMessage(\"\u00a7c\u2716 No clipboard data to save\");\n            return;\n        }\n        \n        ClipboardData copy = data.copy();\n        copy.setAuthor(player.getName());\n        copy.setCreatedTime(System.currentTimeMillis());\n        copy.setName(name);\n        \n        sharedClipboards.put(name, copy);\n        player.sendMessage(\"\u00a7a\u2713 Saved clipboard as: \" + name);\n        \n        log.info(\"Player \" + player.getName() + \" saved clipboard as: \" + name);\n    }\n    \n    /**\n     * Loads from a shared clipboard\n     */\n    public void loadFromShared(Player player, String name) {\n        ClipboardData data = sharedClipboards.get(name);\n        if (data == null) {\n            player.sendMessage(\"\u00a7c\u2716 Shared clipboard not found: \" + name);\n            return;\n        }\n        \n        playerClipboards.put(player.getUniqueId(), data.copy());\n        player.sendMessage(\"\u00a7a\u2713 Loaded clipboard: \" + name + \" (\" + data.getBlocks().size() + \" blocks)\");\n        \n        log.fine(\"Player \" + player.getName() + \" loaded shared clipboard: \" + name);\n    }\n    \n    /**\n     * Lists available shared clipboards\n     */\n    public void listShared(Player player) {\n        if (sharedClipboards.isEmpty()) {\n            player.sendMessage(\"\u00a7e\u27a4 No shared clipboards available\");\n            return;\n        }\n        \n        player.sendMessage(\"\u00a76=== Shared Clipboards ===\");\n        for (Map.Entry<String, ClipboardData> entry : sharedClipboards.entrySet()) {\n            ClipboardData data = entry.getValue();\n            player.sendMessage(String.format(\"\u00a7f%s \u00a77(%d blocks, by %s)\", \n                entry.getKey(), data.getBlocks().size(), data.getAuthor()));\n        }\n    }\n    \n    /**\n     * Clears the player's clipboard\n     */\n    public void clear(Player player) {\n        playerClipboards.remove(player.getUniqueId());\n        player.sendMessage(\"\u00a7a\u2713 Clipboard cleared\");\n    }\n    \n    /**\n     * Gets clipboard info for a player\n     */\n    public String getClipboardInfo(Player player) {\n        ClipboardData data = playerClipboards.get(player.getUniqueId());\n        if (data == null) {\n            return \"\u00a7cNo clipboard data\";\n        }\n        \n        return String.format(\"\u00a7a%s \u00a77(%d blocks)\", \n            data.getType().getDisplayName(), data.getBlocks().size());\n    }\n    \n    // Helper methods\n    \n    private void pasteSingleBlock(Player player, Location location, ClipboardData data) {\n        if (data.getBlocks().isEmpty()) return;\n        \n        CodeBlock block = data.getBlocks().get(0);\n        placeCodeBlock(location, block);\n    }\n    \n    private void pasteChain(Player player, Location startLocation, ClipboardData data) {\n        Location currentLoc = startLocation.clone();\n        \n        for (CodeBlock block : data.getBlocks()) {\n            placeCodeBlock(currentLoc, block);\n            currentLoc.add(1, 0, 0); // Move to next position in line\n        }\n    }\n    \n    private void pasteRegion(Player player, Location targetLocation, ClipboardData data) {\n        for (Map.Entry<RelativePosition, CodeBlock> entry : data.getBlocksWithPositions().entrySet()) {\n            RelativePosition relPos = entry.getKey();\n            CodeBlock block = entry.getValue();\n            \n            Location placeLoc = targetLocation.clone().add(relPos.x, relPos.y, relPos.z);\n            placeCodeBlock(placeLoc, block);\n        }\n    }\n    \n    private void placeCodeBlock(Location location, CodeBlock block) {\n        // Place the physical block\n        location.getBlock().setType(block.getMaterial());\n        \n        // This would integrate with the AutoConnectionManager to register the block\n        // and set up its connections and properties\n    }\n    \n    private boolean isCodeBlock(Material material) {\n        // This would integrate with BlockConfigService\n        return false; // Placeholder\n    }\n    \n    private CodeBlock createCodeBlockFromLocation(Location location) {\n        // This would create a CodeBlock instance from the block at the location\n        // integrating with existing systems\n        return null; // Placeholder\n    }\n    \n    // Data classes\n    \n    @Data\n    public static class ClipboardData {\n        private final ClipboardType type;\n        private final List<CodeBlock> blocks = new ArrayList<>();\n        private final Map<RelativePosition, CodeBlock> blocksWithPositions = new HashMap<>();\n        private String name;\n        private String author;\n        private long createdTime;\n        \n        // Region bounds (for REGION type)\n        private int minX, minY, minZ, maxX, maxY, maxZ;\n        \n        public ClipboardData(ClipboardType type) {\n            this.type = type;\n            this.createdTime = System.currentTimeMillis();\n        }\n        \n        public void addBlock(CodeBlock block) {\n            blocks.add(block);\n            blocksWithPositions.put(new RelativePosition(0, 0, 0), block);\n        }\n        \n        public void addBlock(CodeBlock block, int relX, int relY, int relZ) {\n            blocks.add(block);\n            blocksWithPositions.put(new RelativePosition(relX, relY, relZ), block);\n        }\n        \n        public void setBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {\n            this.minX = minX;\n            this.minY = minY;\n            this.minZ = minZ;\n            this.maxX = maxX;\n            this.maxY = maxY;\n            this.maxZ = maxZ;\n        }\n        \n        public ClipboardData copy() {\n            ClipboardData copy = new ClipboardData(this.type);\n            copy.setName(this.name);\n            copy.setAuthor(this.author);\n            copy.setBounds(minX, minY, minZ, maxX, maxY, maxZ);\n            \n            // Deep copy blocks\n            for (Map.Entry<RelativePosition, CodeBlock> entry : this.blocksWithPositions.entrySet()) {\n                // This would create a proper copy of the CodeBlock\n                copy.blocksWithPositions.put(entry.getKey(), entry.getValue());\n                copy.blocks.add(entry.getValue());\n            }\n            \n            return copy;\n        }\n    }\n    \n    public enum ClipboardType {\n        SINGLE_BLOCK(\"Single Block\"),\n        BLOCK_CHAIN(\"Block Chain\"),\n        REGION(\"Region\");\n        \n        private final String displayName;\n        \n        ClipboardType(String displayName) {\n            this.displayName = displayName;\n        }\n        \n        public String getDisplayName() {\n            return displayName;\n        }\n    }\n    \n    @Data\n    public static class RelativePosition {\n        final int x, y, z;\n        \n        public RelativePosition(int x, int y, int z) {\n            this.x = x;\n            this.y = y;\n            this.z = z;\n        }\n    }\n}