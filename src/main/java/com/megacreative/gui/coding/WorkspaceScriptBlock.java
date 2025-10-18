package com.megacreative.gui.coding;

import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;

/**
 * Представление блока скрипта
 *
 * Script block representation
 *
 * Skriptblock-Darstellung
 */
public class WorkspaceScriptBlock {
    public String action;
    public BlockCategory category;
    public org.bukkit.Material material;
    public Map<String, DataValue> parameters = new HashMap<>();
    public boolean hasNext = false;
    
    /**
     * Инициализирует блок скрипта
     * @param action Действие блока
     * @param category Категория блока
     * @param material Материал блока
     *
     * Initializes script block
     * @param action Block action
     * @param category Block category
     * @param material Block material
     *
     * Initialisiert den Skriptblock
     * @param action Block-Aktion
     * @param category Block-Kategorie
     * @param material Block-Material
     */
    public WorkspaceScriptBlock(String action, BlockCategory category, org.bukkit.Material material) {
        this.action = action;
        this.category = category;
        this.material = material;
    }
    
    /**
     * Copy constructor
     * @param other The WorkspaceScriptBlock to copy
     */
    public WorkspaceScriptBlock(WorkspaceScriptBlock other) {
        this.action = other.action;
        this.category = other.category;
        this.material = other.material;
        this.parameters = new HashMap<>(other.parameters);
        this.hasNext = other.hasNext;
    }
    
    /**
     * Creates a copy of this WorkspaceScriptBlock
     * @return A new WorkspaceScriptBlock with the same properties
     */
    public WorkspaceScriptBlock copy() {
        return new WorkspaceScriptBlock(this);
    }
    
    /**
     * Gets the action of this script block
     * @return The action
     */
    public String getAction() { return action; }
    
    /**
     * Gets the category of this script block
     * @return The category
     */
    public BlockCategory getCategory() { return category; }
    
    /**
     * Gets the material of this script block
     * @return The material
     */
    public org.bukkit.Material getMaterial() { return material; }
}