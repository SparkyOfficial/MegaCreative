package com.megacreative.coding.errors;

import org.bukkit.Location;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VisualErrorHandler {
    // This field needs to be a class field to maintain state
    // Convert initialization tracking fields to local variables where possible
    // This field needs to remain as a class field since it's used in the constructor
    public VisualErrorHandler(Plugin plugin) {
        // Constructor implementation
    }
    
    public void showError(Location blockLocation, String errorMessage, ErrorSeverity severity, Player... viewers) {
        for (Player viewer : viewers) {
            viewer.sendMessage(severity.getIcon() + " " + errorMessage);
        }
    }
    
    public void showSyntaxError(Location blockLocation, String errorMessage, String problematicCode, Player... viewers) {
        showError(blockLocation, "Syntax Error: " + errorMessage, ErrorSeverity.ERROR, viewers);
    }
    
    public void showRuntimeError(Location blockLocation, String errorMessage, Exception exception, Player... viewers) {
        showError(blockLocation, "Runtime Error: " + errorMessage, ErrorSeverity.ERROR, viewers);
    }
    
    public void clearErrorAt(Location location) {
        
        
        Logger.getLogger(VisualErrorHandler.class.getName()).info("Clearing error at location: " + location);
    }
    
    public void cleanup() {
        
        
        Logger.getLogger(VisualErrorHandler.class.getName()).info("Cleaning up VisualErrorHandler resources");
    }
    
    public enum ErrorSeverity {
        ERROR("§c✖", "Error"),
        WARNING("§e⚠", "Warning"),
        INFO("§b⁇", "Info");
        
        private final String icon;
        private final String displayName;
        
        ErrorSeverity(String icon, String displayName) {
            this.icon = icon;
            this.displayName = displayName;
        }
        
        public String getIcon() { return icon; }
        public String getDisplayName() { return displayName; }
    }
    
    private static class ErrorDisplay {
        private final Location location;
        private final String message;
        private final ErrorSeverity severity;
        
        public ErrorDisplay(Location location, String message, ErrorSeverity severity) {
            this.location = location;
            this.message = message;
            this.severity = severity;
        }
    }
}