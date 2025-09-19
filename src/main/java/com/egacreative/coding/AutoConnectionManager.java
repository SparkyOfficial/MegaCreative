
    /**
     * Creates a script from an event block and adds it to the world
     * Enhanced with improved script creation and error handling.
     */
    public void createAndAddScript(CodeBlock eventBlock, Player player, Location location) {
        try {
            // Use the new compilation method to create a complete script
            CodeScript script = compileScriptFromEventBlock(eventBlock, location);
            if (script == null) {
                player.sendMessage("§cОшибка при создании скрипта!");
                return;
            }
            
            handleScriptCreation(script, eventBlock, player, location);
        } catch (Exception e) {
            logSevere("Failed to create script for event block: " + e.getMessage());
            logSevere("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
    }
    
    /**
     * Handles the script creation process
     */
    private void handleScriptCreation(CodeScript script, CodeBlock eventBlock, Player player, Location location) {
        // Find the creative world using service registry
        com.megacreative.interfaces.IWorldManager worldManager = getWorldManager();
        if (worldManager == null) {
            plugin.getLogger().warning("World manager not available");
            return;
        }
        
        com.megacreative.models.CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(location.getWorld());
        if (creativeWorld != null) {
            addScriptToWorld(script, eventBlock, creativeWorld, player);
        } else {
            logWarning("Could not find creative world for location: " + location);
        }
    }
    
    /**
     * Adds the script to the creative world
     */
    private void addScriptToWorld(CodeScript script, CodeBlock eventBlock, CreativeWorld creativeWorld, Player player) {
        // Add the script to the world
        List<CodeScript> scripts = creativeWorld.getScripts();
        if (scripts == null) {
            scripts = new ArrayList<>();
            creativeWorld.setScripts(scripts);
        }
        
        // Remove any existing script with the same root block action to avoid duplicates
        scripts.removeIf(existingScript -> 
            existingScript.getRootBlock() != null && 
            eventBlock.getAction().equals(existingScript.getRootBlock().getAction()));
        
        scripts.add(script);
        
        // Save the creative world to persist the script
        com.megacreative.interfaces.IWorldManager worldManager = getWorldManager();
        if (worldManager != null) {
            worldManager.saveWorld(creativeWorld);
        }
        
        player.sendMessage("§a✓ Скрипт скомпилирован и создан для события: §f" + eventBlock.getAction());
        logFine("Compiled and added script for event block: " + eventBlock.getAction() + " in world: " + creativeWorld.getName());
    }
    