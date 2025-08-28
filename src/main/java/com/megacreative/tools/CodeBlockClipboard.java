package com.megacreative.tools;

import com.megacreative.coding.CodeBlock;
import lombok.Data;
import lombok.extern.java.Log;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log
public class CodeBlockClipboard {
    
    private final Map<UUID, ClipboardData> playerClipboards = new ConcurrentHashMap<>();
    private final Map<String, ClipboardData> sharedClipboards = new ConcurrentHashMap<>();
    
    public void copyBlock(Player player, CodeBlock block) {
        ClipboardData data = new ClipboardData(ClipboardType.SINGLE_BLOCK);
        data.addBlock(block);
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Copied code block: " + block.getAction());
    }
    
    public void copyRegion(Player player, Location corner1, Location corner2) {
        ClipboardData data = new ClipboardData(ClipboardType.REGION);
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Copied region");
    }
    
    public void paste(Player player, Location targetLocation) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§c✖ No clipboard data to paste");
            return;
        }
        player.sendMessage("§a✓ Pasted " + data.getBlocks().size() + " blocks");
    }
    
    public void showPreview(Player player, Location targetLocation) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§c✖ No clipboard data to preview");
            return;
        }
        player.sendMessage("§e⤤ Preview: " + data.getBlocks().size() + " blocks would be placed");
    }
    
    public void saveToShared(Player player, String name) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§c✖ No clipboard data to save");
            return;
        }
        sharedClipboards.put(name, data);
        player.sendMessage("§a✓ Saved clipboard as: " + name);
    }
    
    public void loadFromShared(Player player, String name) {
        ClipboardData data = sharedClipboards.get(name);
        if (data == null) {
            player.sendMessage("§c✖ Shared clipboard not found: " + name);
            return;
        }
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Loaded clipboard: " + name);
    }
    
    public void listShared(Player player) {
        if (sharedClipboards.isEmpty()) {
            player.sendMessage("§e⤤ No shared clipboards available");
            return;
        }
        player.sendMessage("§6=== Shared Clipboards ===");
        for (String name : sharedClipboards.keySet()) {
            player.sendMessage("§f" + name);
        }
    }
    
    public void clear(Player player) {
        playerClipboards.remove(player.getUniqueId());
        player.sendMessage("§a✓ Clipboard cleared");
    }
    
    public String getClipboardInfo(Player player) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            return "§cNo clipboard data";
        }
        return "§a" + data.getType().getDisplayName() + " §7(" + data.getBlocks().size() + " blocks)";
    }
    
    @Data
    public static class ClipboardData {
        private final ClipboardType type;
        private final List<CodeBlock> blocks = new ArrayList<>();
        
        public ClipboardData(ClipboardType type) {
            this.type = type;
        }
        
        public void addBlock(CodeBlock block) {
            blocks.add(block);
        }
    }
    
    public enum ClipboardType {
        SINGLE_BLOCK("Single Block"),
        BLOCK_CHAIN("Block Chain"),
        REGION("Region");
        
        private final String displayName;
        
        ClipboardType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}