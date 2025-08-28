package com.megacreative.coding.debug;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableScope;
import lombok.extern.java.Log;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log
public class VisualDebugger {
    
    private final MegaCreative plugin;
    private final Map<UUID, DebugSession> activeSessions = new ConcurrentHashMap<>();
    
    public VisualDebugger(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    public void startDebugSession(Player player, String sessionName) {
        DebugSession session = new DebugSession(player, sessionName);
        activeSessions.put(player.getUniqueId(), session);
        player.sendMessage("§a✓ Visual debugger started: " + sessionName);
    }
    
    public void stopDebugSession(Player player) {
        DebugSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            player.sendMessage("§c✖ Visual debugger stopped");
        }
    }
    
    public void highlightBlockExecution(Player player, Location blockLocation, CodeBlock block) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        
        session.executionStep++;
        player.sendMessage("§e▶ Executing: " + block.getAction());
    }
    
    public void showVariableChange(Player player, Location blockLocation, String variableName, 
                                 DataValue oldValue, DataValue newValue, VariableScope scope) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        
        player.sendMessage("§b" + variableName + "§7: §c" + 
            (oldValue != null ? oldValue.asString() : "null") + " §7→ §a" + 
            (newValue != null ? newValue.asString() : "null"));
    }
    
    public void showError(Player player, Location blockLocation, String errorMessage) {
        DebugSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        
        session.errorCount++;
        player.sendMessage("§c✖ ERROR: " + errorMessage);
    }
    
    public void cleanup() {
        activeSessions.clear();
    }
    
    private static class DebugSession {
        final String sessionName;
        final Player player;
        int executionStep = 0;
        int errorCount = 0;
        
        DebugSession(Player player, String sessionName) {
            this.player = player;
            this.sessionName = sessionName;
        }
    }
}