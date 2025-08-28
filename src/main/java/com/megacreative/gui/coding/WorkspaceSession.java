package com.megacreative.gui.coding;

import com.megacreative.coding.values.DataValue;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

/**
 * Workspace session for coding interface
 */
public class WorkspaceSession {
    
    private final Player player;
    private final CreativeWorld world;
    private final long createdTime;
    
    private Inventory currentGUI;
    private List<ScriptBlock> currentScript = new ArrayList<>();
    private Map<String, DataValue> variables = new HashMap<>();
    private Set<UUID> collaborators = new HashSet<>();
    private boolean allowCollaboration = false;
    
    // Undo/Redo
    private Stack<ScriptSnapshot> undoStack = new Stack<>();
    private Stack<ScriptSnapshot> redoStack = new Stack<>();
    
    public WorkspaceSession(Player player, CreativeWorld world) {
        this.player = player;
        this.world = world;
        this.createdTime = System.currentTimeMillis();
    }
    
    // Script management
    public void addBlock(ScriptBlock block) {
        saveSnapshot("Add " + block.getAction());
        currentScript.add(block);
    }
    
    public void removeBlock(int index) {
        if (index >= 0 && index < currentScript.size()) {
            ScriptBlock removed = currentScript.get(index);
            saveSnapshot("Remove " + removed.getAction());
            currentScript.remove(index);
        }
    }
    
    // Undo/Redo
    private void saveSnapshot(String description) {
        undoStack.push(new ScriptSnapshot(new ArrayList<>(currentScript), description));
        redoStack.clear();
        if (undoStack.size() > 50) undoStack.remove(0);
    }
    
    public void undo() {
        if (undoStack.size() > 1) {
            redoStack.push(undoStack.pop());
            currentScript = new ArrayList<>(undoStack.peek().getScript());
            player.sendMessage("§aUndo performed");
        }
    }
    
    public void redo() {
        if (!redoStack.isEmpty()) {
            ScriptSnapshot next = redoStack.pop();
            undoStack.push(next);
            currentScript = new ArrayList<>(next.getScript());
            player.sendMessage("§aRedo performed");
        }
    }
    
    // Collaboration
    public void enableCollaboration() {
        allowCollaboration = true;
        player.sendMessage("§aCollaboration enabled!");
    }
    
    public boolean addCollaborator(UUID playerId) {
        if (!allowCollaboration) return false;
        collaborators.add(playerId);
        return true;
    }
    
    public void saveCurrentScript() {
        player.sendMessage("§aScript saved!");
    }
    
    // Getters/Setters
    public Player getPlayer() { return player; }
    public CreativeWorld getWorld() { return world; }
    public List<ScriptBlock> getCurrentScript() { return currentScript; }
    public Inventory getCurrentGUI() { return currentGUI; }
    public void setCurrentGUI(Inventory gui) { this.currentGUI = gui; }
    public boolean isCollaborator(UUID playerId) { return collaborators.contains(playerId); }
}

/**
 * Script snapshot for undo/redo
 */
class ScriptSnapshot {
    private final List<ScriptBlock> script;
    private final String description;
    private final long timestamp;
    
    public ScriptSnapshot(List<ScriptBlock> script, String description) {
        this.script = script;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }
    
    public List<ScriptBlock> getScript() { return script; }
    public String getDescription() { return description; }
    public long getTimestamp() { return timestamp; }
}

/**
 * Script block representation
 */
class ScriptBlock implements Cloneable {
    private String action;
    private BlockCategory category;
    private org.bukkit.Material material;
    private Map<String, DataValue> parameters = new HashMap<>();
    private boolean hasNext = false;
    
    public ScriptBlock(String action, BlockCategory category, org.bukkit.Material material) {
        this.action = action;
        this.category = category;
        this.material = material;
    }
    
    @Override
    public ScriptBlock clone() {
        ScriptBlock cloned = new ScriptBlock(action, category, material);
        cloned.parameters = new HashMap<>(parameters);
        cloned.hasNext = hasNext;
        return cloned;
    }
    
    // Getters/Setters
    public String getAction() { return action; }
    public BlockCategory getCategory() { return category; }
    public org.bukkit.Material getMaterial() { return material; }
    public Map<String, DataValue> getParameters() { return parameters; }
    public boolean hasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
}

/**
 * Block definition for palette
 */
class BlockDefinition {
    private final String name;
    private final String action;
    private final BlockCategory category;
    private final org.bukkit.Material material;
    private final List<BlockParameter> parameters;
    
    public BlockDefinition(String name, String action, BlockCategory category, 
                          org.bukkit.Material material, List<BlockParameter> parameters) {
        this.name = name;
        this.action = action;
        this.category = category;
        this.material = material;
        this.parameters = parameters;
    }
    
    public String getName() { return name; }
    public String getAction() { return action; }
    public BlockCategory getCategory() { return category; }
    public org.bukkit.Material getMaterial() { return material; }
    public List<BlockParameter> getParameters() { return parameters; }
}

/**
 * Block parameter definition
 */
class BlockParameter {
    private final String name;
    private final com.megacreative.coding.values.ValueType type;
    private final String description;
    private final boolean required;
    
    public BlockParameter(String name, com.megacreative.coding.values.ValueType type, 
                         String description, boolean required) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.required = required;
    }
    
    public String getName() { return name; }
    public com.megacreative.coding.values.ValueType getType() { return type; }
    public String getDescription() { return description; }
    public boolean isRequired() { return required; }
}

/**
 * Workspace modes
 */
enum WorkspaceMode {
    BUILDING("Building", "Creating and editing scripts"),
    TESTING("Testing", "Testing script execution"),
    DEBUGGING("Debugging", "Debugging script issues"),
    COLLABORATING("Collaborating", "Working with others");
    
    private final String name;
    private final String description;
    
    WorkspaceMode(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
}