package com.megacreative.coding.variables;

/**
 * Defines the scope/lifetime of variables in the coding system
 */
public enum VariableScope {
    
    /**
     * Local variables - exist only during script execution
     * Cleared after each script run
     */
    LOCAL("local", "§7Local", "Variable exists only during script execution"),
    
    /**
     * World variables - persistent for the world
     * Saved and loaded with world data
     */
    WORLD("world", "§aWorld", "Variable is saved with world data"),
    
    /**
     * Player variables - global for specific player
     * Follows player across worlds
     */
    PLAYER("player", "§bPlayer", "Variable follows player across worlds"),
    
    /**
     * Server variables - global for entire server
     * Shared across all worlds and players
     */
    SERVER("server", "§eServer", "Variable is shared across entire server");
    
    private final String identifier;
    private final String displayName;
    private final String description;
    
    VariableScope(String identifier, String displayName, String description) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Parses scope from string identifier
     */
    public static VariableScope fromString(String str) {
        if (str == null) return LOCAL;
        
        for (VariableScope scope : values()) {
            if (scope.identifier.equalsIgnoreCase(str)) {
                return scope;
            }
        }
        return LOCAL;
    }
    
    /**
     * Creates a full variable name with scope prefix
     */
    public String createVariableName(String baseName) {
        return identifier + ":" + baseName;
    }
    
    /**
     * Extracts base name from full variable name
     */
    public static String extractBaseName(String fullName) {
        int colonIndex = fullName.indexOf(':');
        if (colonIndex > 0) {
            return fullName.substring(colonIndex + 1);
        }
        return fullName;
    }
    
    /**
     * Extracts scope from full variable name
     */
    public static VariableScope extractScope(String fullName) {
        int colonIndex = fullName.indexOf(':');
        if (colonIndex > 0) {
            String scopeStr = fullName.substring(0, colonIndex);
            return fromString(scopeStr);
        }
        return LOCAL;
    }
}