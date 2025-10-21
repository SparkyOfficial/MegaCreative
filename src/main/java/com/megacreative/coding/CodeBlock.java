package com.megacreative.coding;

import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


import java.util.*;

/**
 * Represents a single block in a script.
 * Contains type, parameters and references to other blocks.
 * Simplified to be a pure data object without plugin dependencies.
 * 
 * Представляет собой один блок в скрипте.
 * Содержит тип, параметры и ссылки на другие блоки.
 * Упрощен до чистого объекта данных без зависимостей от плагина.
 * 
 * @author Андрій Будильников
 */
public class CodeBlock implements Cloneable {
    
    
    /** Unique block identifier 
     *  Уникальный идентификатор блока */
    private UUID id;
    
    
    
    /** Selected action (e.g. onJoin, sendMessage) 
     *  Выбранное действие (например, onJoin, sendMessage) */
    private String action;
    
    /** Selected event (e.g. onJoin, onLeave) 
     *  Выбранное событие (например, onJoin, onLeave) */
    private String event;
    
    /** Block parameters 
     *  Параметры блока */
    private Map<String, DataValue> parameters;
    
    /** Nested blocks (e.g. inside IF condition) 
     *  Вложенные блоки (например, внутри условия IF) */
    private List<CodeBlock> children;
    
    /** Next block in sequence 
     *  Следующий блок в последовательности */
    private CodeBlock nextBlock;
    
    /** Configuration items 
     *  Элементы конфигурации */
    private Map<Integer, ItemStack> configItems;
    
    /** Item groups for complex configurations 
     *  Группы предметов для сложных конфигураций */
    private Map<String, List<Integer>> itemGroups;
    
    /** Bracket type for grouping blocks 
     *  Тип скобок для группировки блоков */
    private BracketType bracketType = null;
    
    /** World ID where the block is located 
     *  ID мира, где находится блок */
    private String worldId;
    
    /** X coordinate of the block 
     *  X координата блока */
    private int x;
    
    /** Y coordinate of the block 
     *  Y координата блока */
    private int y;
    
    /** Z coordinate of the block 
     *  Z координата блока */
    private int z;
    
    /** Material name instead of Material object 
     *  Название материала вместо объекта Material */
    private String materialName;
    
    /** Else block for conditional statements 
     *  Блок else для условных операторов */
    private CodeBlock elseBlock;
    
    
    
    /**
     * Enum for bracket types - used for grouping code blocks
     * 
     * Перечисление для типов скобок - используется для группировки блоков кода
     */
    public enum BracketType {
        OPEN("Opening Bracket", "{"),
        CLOSE("Closing Bracket", "}");
        
        private final String displayName;
        private final String symbol;
        
        BracketType(String displayName, String symbol) {
            this.displayName = displayName;
            this.symbol = symbol;
        }
        
        public String getDisplayName() { return displayName; }
        public String getSymbol() { return symbol; }
    }
    
    
    
    /**
     * Default constructor
     * 
     * Конструктор по умолчанию
     */
    public CodeBlock() {
        this.id = UUID.randomUUID();
        this.parameters = new HashMap<>();
        this.children = new ArrayList<>();
        this.configItems = new HashMap<>();
        this.itemGroups = new HashMap<>();
    }
    
    /**
     * Constructor with basic parameters
     * @param materialName Block material name
     * 
     * Конструктор с базовыми параметрами
     * @param materialName Название материала блока
     */
    public CodeBlock(String materialName, String action) {
        this();
        this.materialName = materialName;
        this.action = action;
    }
    
    /**
     * Constructor with action and event parameters
     * @param materialName Block material name
     * @param action Block action
     * @param event Block event
     * 
     * Конструктор с параметрами действия и события
     * @param materialName Название материала блока
     * @param action Действие блока
     * @param event Событие блока
     */
    public CodeBlock(String materialName, String action, String event) {
        this();
        this.materialName = materialName;
        this.action = action;
        this.event = event;
    }
    
    
    
    public UUID getId() { 
        return id; 
    }
    
    public void setId(UUID id) { 
        this.id = id; 
    }
    
    public String getAction() { 
        return action; 
    }
    
    public void setAction(String action) { 
        this.action = action; 
    }
    
    public String getEvent() { 
        return event; 
    }
    
    public void setEvent(String event) { 
        this.event = event; 
    }
    
    public Map<String, DataValue> getParameters() { 
        return parameters; 
    }
    
    public void setParameters(Map<String, DataValue> parameters) { 
        this.parameters = parameters; 
    }
    
    public List<CodeBlock> getChildren() { 
        return children; 
    }
    
    public void setChildren(List<CodeBlock> children) { 
        this.children = children; 
    }
    
    public CodeBlock getNextBlock() { 
        return nextBlock; 
    }
    
    public void setNextBlock(CodeBlock nextBlock) { 
        this.nextBlock = nextBlock; 
    }
    
    public Map<Integer, ItemStack> getConfigItems() { 
        return configItems; 
    }
    
    public void setConfigItems(Map<Integer, ItemStack> configItems) { 
        this.configItems = configItems; 
    }
    
    public Map<String, List<Integer>> getItemGroups() { 
        return itemGroups; 
    }
    
    public void setItemGroups(Map<String, List<Integer>> itemGroups) { 
        this.itemGroups = itemGroups; 
    }
    
    public BracketType getBracketType() {
        return bracketType;
    }
    
    public void setBracketType(BracketType bracketType) {
        this.bracketType = bracketType;
    }
    
    public boolean isBracket() {
        return bracketType != null;
    }
    
    /**
     * Gets the location of this block (removed Bukkit dependency)
     * @return Block location data as separate components
     */
    public String getLocationWorldId() {
        return worldId;
    }
    
    /**
     * Gets X coordinate
     * @return X coordinate
     */
    public int getLocationX() {
        return x;
    }
    
    /**
     * Gets Y coordinate
     * @return Y coordinate
     */
    public int getLocationY() {
        return y;
    }
    
    /**
     * Gets Z coordinate
     * @return Z coordinate
     */
    public int getLocationZ() {
        return z;
    }
    
    /**
     * Sets the location of this block (removed Bukkit dependency)
     * @param worldId World ID
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public void setLocation(String worldId, int x, int y, int z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public String getWorldId() {
        return worldId;
    }
    
    public void setWorldId(String worldId) {
        this.worldId = worldId;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getZ() {
        return z;
    }
    
    public void setZ(int z) {
        this.z = z;
    }
    
    public String getMaterialName() {
        return materialName;
    }
    
    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }
    
    public CodeBlock getElseBlock() {
        return elseBlock;
    }
    
    public void setElseBlock(CodeBlock elseBlock) {
        this.elseBlock = elseBlock;
    }
    
    
    
    /**
     * Sets a parameter for the block.
     * @param key Parameter key (e.g. "material" or "message")
     * @param value Parameter value (DataValue)
     */
    public void setParameter(String key, DataValue value) {
        parameters.put(key, value);
    }
    
    /**
     * Sets a parameter for the block with automatic conversion.
     * @param key Parameter key
     * @param value Value for automatic conversion to DataValue
     */
    public void setParameter(String key, Object value) {
        parameters.put(key, DataValue.fromObject(value));
    }
    
    /**
     * Gets a parameter by key
     * @param key Parameter key
     * @return Parameter value or null if not found
     */
    public DataValue getParameter(String key) {
        return parameters.get(key);
    }
    
    /**
     * Gets a parameter with default value
     * @param key Parameter key
     * @param defaultValue Default value
     * @return Parameter value or default value
     */
    public DataValue getParameter(String key, DataValue defaultValue) {
        DataValue value = parameters.get(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Gets a parameter value as a specific type
     * @param key Parameter key
     * @param type Expected type class
     * @return Parameter value converted to type or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameterValue(String key, Class<T> type) {
        DataValue value = parameters.get(key);
        if (value == null) return null;
        
        if (type == String.class) {
            return (T) value.asString();
        } else if (type == Boolean.class) {
            return (T) Boolean.valueOf(value.asBoolean());
        } else if (type == Integer.class) {
            return (T) Integer.valueOf(value.asNumber().intValue());
        } else if (type == Long.class) {
            return (T) Long.valueOf(value.asNumber().longValue());
        } else if (type == Double.class) {
            return (T) Double.valueOf(value.asNumber().doubleValue());
        } else if (type == Float.class) {
            return (T) Float.valueOf(value.asNumber().floatValue());
        }
        
        return null;
    }
    
    /**
     * Gets a parameter value as a specific type with default
     * @param key Parameter key
     * @param type Expected type class
     * @param defaultValue Default value
     * @return Parameter value converted to type or default value
     */
    public <T> T getParameterValue(String key, Class<T> type, T defaultValue) {
        T value = getParameterValue(key, type);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Gets a parameter value as Object
     * @param key Parameter key
     * @return Parameter value as Object or null if not found
     */
    public Object getParameterValue(String key) {
        DataValue value = parameters.get(key);
        if (value == null) return null;
        
        return value.getRawValue();
    }
    
    /**
     * Checks if a parameter exists
     * @param key Parameter key
     * @return true if parameter exists
     */
    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }
    
    /**
     * Adds a child block (for conditions).
     * @param child Block that will be executed inside this block
     */
    public void addChild(CodeBlock child) {
        if (child != null) {
            children.add(child);
        }
    }
    
    /**
     * Sets the next block in the chain.
     * @param next Next block
     */
    public void setNext(CodeBlock next) {
        this.nextBlock = next;
    }
    
    
    
    /**
     * Sets a configuration item in the specified slot
     * @param slot Inventory slot (0-8)
     * @param item Item to save
     */
    public void setConfigItem(int slot, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            configItems.remove(slot);
        } else {
            
            configItems.put(slot, item.clone());
        }
    }
    
    /**
     * Gets a configuration item from the specified slot
     * @param slot Inventory slot (0-8)
     * @return Item or null if slot is empty
     */
    public ItemStack getConfigItem(int slot) {
        return configItems.get(slot);
    }
    
    /**
     * Checks if there are items in the configuration
     * @return true if there is at least one item
     */
    public boolean hasConfigItems() {
        return !configItems.isEmpty();
    }
    
    /**
     * Clears all configuration items
     */
    public void clearConfigItems() {
        configItems.clear();
    }
    
    
    
    /**
     * Creates an item group
     * @param groupName Group name
     * @param slots Slots included in the group
     */
    public void createItemGroup(String groupName, List<Integer> slots) {
        if (groupName != null && slots != null) {
            itemGroups.put(groupName, new ArrayList<>(slots));
        }
    }
    
    /**
     * Gets items from a group
     * @param groupName Group name
     * @return List of items in the group
     */
    public List<ItemStack> getItemsFromGroup(String groupName) {
        List<Integer> groupSlots = itemGroups.get(groupName);
        if (groupSlots == null || groupSlots.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ItemStack> items = new ArrayList<>();
        for (Integer slot : groupSlots) {
            ItemStack item = configItems.get(slot);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }
    
    /**
     * Checks if a group with the specified name exists
     * @param groupName Group name
     * @return true if the group exists
     */
    public boolean hasItemGroup(String groupName) {
        return itemGroups.containsKey(groupName);
    }
    
    /**
     * Removes an item group
     * @param groupName Group name to remove
     */
    public void removeItemGroup(String groupName) {
        itemGroups.remove(groupName);
    }
    
    /**
     * Clears all item groups
     */
    public void clearItemGroups() {
        itemGroups.clear();
    }
    
    /**
     * Gets an item from a named slot
     * @param slotName Slot name (e.g. "entity_slot", "radius_slot")
     * @param slotNumberResolver Function to resolve slot numbers from names
     * @return Item from slot or null if slot not found
     */
    public ItemStack getItemFromSlot(String slotName, java.util.function.Function<String, Integer> slotNumberResolver) {
        if (slotName == null || slotNumberResolver == null) {
            return null;
        }
        Integer slotNumber = slotNumberResolver.apply(slotName);
        return slotNumber != null ? getConfigItem(slotNumber) : null;
    }
    
    /**
     * Gets items from a named group
     * @param groupName Group name (e.g. "items_to_give")
     * @param groupSlotsResolver Function to resolve group slots
     * @return List of items from the group
     */
    public List<ItemStack> getItemsFromNamedGroup(String groupName, java.util.function.Function<String, List<Integer>> groupSlotsResolver) {
        if (groupName == null || groupSlotsResolver == null) {
            return new ArrayList<>();
        }
        
        List<Integer> groupSlots = groupSlotsResolver.apply(groupName);
        if (groupSlots == null || groupSlots.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ItemStack> items = new ArrayList<>();
        for (Integer slot : groupSlots) {
            ItemStack item = configItems.get(slot);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }
    
    /**
     * Gets all named slots for this action
     * @param namedSlotsResolver Function to resolve named slots
     * @return Map of slot names and their numbers
     */
    public Map<String, Integer> getNamedSlots(java.util.function.Function<Void, Map<String, Integer>> namedSlotsResolver) {
        if (namedSlotsResolver == null) {
            return new HashMap<>();
        }
        return namedSlotsResolver.apply(null);
    }
    
    /**
     * Gets all named groups for this action
     * @param namedGroupsResolver Function to resolve named groups
     * @return Map of group names and slot lists
     */
    public Map<String, List<Integer>> getNamedGroups(java.util.function.Function<Void, Map<String, List<Integer>>> namedGroupsResolver) {
        if (namedGroupsResolver == null) {
            return new HashMap<>();
        }
        return namedGroupsResolver.apply(null);
    }
    
    /**
     * 🎆 ENHANCED: Gets a parameter value from a named slot with placeholder resolution
     * @param slotName The name of the slot to get the parameter from
     * @param slotResolver Function to resolve slot names to numbers
     * @param context Execution context for placeholder resolution
     * @return The resolved parameter value or null if not found
     */
    public String getNamedSlotParameter(String slotName, java.util.function.Function<String, Integer> slotResolver, com.megacreative.coding.ExecutionContext context) {
        if (slotName == null || slotResolver == null || context == null) {
            return null;
        }
        
        Integer slotNumber = slotResolver.apply(slotName);
        if (slotNumber == null) {
            return null;
        }
        
        ItemStack item = getConfigItem(slotNumber);
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return null;
        }
        
        String rawValue = meta.getDisplayName();
        
        
        return com.megacreative.coding.placeholders.ReferenceSystemPlaceholderResolver.resolvePlaceholders(rawValue, context);
    }
    
    /**
     * 🎆 ENHANCED: Gets a list of parameter values from a named group with placeholder resolution
     * @param groupName The name of the group to get parameters from
     * @param groupResolver Function to resolve group names to slot numbers
     * @param context Execution context for placeholder resolution
     * @return List of resolved parameter values
     */
    public List<String> getNamedGroupParameters(String groupName, java.util.function.Function<String, int[]> groupResolver, com.megacreative.coding.ExecutionContext context) {
        List<String> parameters = new ArrayList<>();
        
        if (groupName == null || groupResolver == null || context == null) {
            return parameters;
        }
        
        int[] slotNumbers = groupResolver.apply(groupName);
        // Check both null and empty array cases
        if (slotNumbers == null || slotNumbers.length == 0) {
            return parameters;
        }
        
        for (int slotNumber : slotNumbers) {
            ItemStack item = getConfigItem(slotNumber);
            if (item != null && item.hasItemMeta()) {
                org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String rawValue = meta.getDisplayName();
                    String resolvedValue = com.megacreative.coding.placeholders.ReferenceSystemPlaceholderResolver.resolvePlaceholders(rawValue, context);
                    parameters.add(resolvedValue);
                }
            }
        }
        
        return parameters;
    }
    
    /**
     * 🎆 ENHANCED: Gets all named slot parameters with placeholder resolution
     * @param slotResolver Function to resolve slot names to numbers
     * @param context Execution context for placeholder resolution
     * @return Map of slot names to resolved parameter values
     */
    public Map<String, String> getAllNamedSlotParameters(java.util.function.Function<String, Integer> slotResolver, com.megacreative.coding.ExecutionContext context) {
        Map<String, String> parameters = new HashMap<>();
        
        if (slotResolver == null || context == null) {
            return parameters;
        }
        
        
        for (Map.Entry<Integer, ItemStack> entry : configItems.entrySet()) {
            Integer slotNumber = entry.getKey();
            ItemStack item = entry.getValue();
            
            if (item != null && item.hasItemMeta()) {
                org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String rawValue = meta.getDisplayName();
                    String resolvedValue = com.megacreative.coding.placeholders.ReferenceSystemPlaceholderResolver.resolvePlaceholders(rawValue, context);
                    
                    parameters.put("slot_" + slotNumber, resolvedValue);
                }
            }
        }
        
        return parameters;
    }
    
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeBlock codeBlock = (CodeBlock) o;
        return Objects.equals(id, codeBlock.id) &&
               Objects.equals(materialName, codeBlock.materialName) &&
               Objects.equals(action, codeBlock.action) &&
               Objects.equals(parameters, codeBlock.parameters) &&
               Objects.equals(children, codeBlock.children) &&
               Objects.equals(nextBlock, codeBlock.nextBlock) &&
               Objects.equals(configItems, codeBlock.configItems) &&
               Objects.equals(itemGroups, codeBlock.itemGroups) &&
               Objects.equals(worldId, codeBlock.worldId) &&
               x == codeBlock.x &&
               y == codeBlock.y &&
               z == codeBlock.z;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, materialName, action, parameters, children, nextBlock, configItems, itemGroups, worldId, x, y, z);
    }
    
    @Override
    public CodeBlock clone() {
        try {
            CodeBlock cloned = (CodeBlock) super.clone();
            cloned.id = UUID.randomUUID(); 
            cloned.parameters = new HashMap<>(this.parameters);
            
            
            cloned.configItems = new HashMap<>();
            for (Map.Entry<Integer, ItemStack> entry : this.configItems.entrySet()) {
                if (entry.getValue() != null) {
                    cloned.configItems.put(entry.getKey(), entry.getValue().clone());
                }
            }
            
            
            
            cloned.nextBlock = null;
            cloned.children = new ArrayList<>();
            
            
            cloned.itemGroups = new HashMap<>();
            for (Map.Entry<String, List<Integer>> entry : this.itemGroups.entrySet()) {
                if (entry.getValue() != null) {
                    cloned.itemGroups.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }
            }
            
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("CodeBlock cloning failed", e);
        }
    }
    
    @Override
    public String toString() {
        return "CodeBlock{" +
               "id=" + id +
               ", materialName='" + materialName + '\'' +
               ", action='" + action + '\'' +
               ", parameters=" + parameters.size() +
               ", children=" + children.size() +
               ", configItems=" + configItems.size() +
               ", itemGroups=" + itemGroups.size() +
               ", worldId='" + worldId + '\'' +
               ", x=" + x +
               ", y=" + y +
               ", z=" + z +
               '}';
    }
    
    /**
     * Gets the condition of this block
     * @return The execution condition or an empty string if not defined
     */
    public String getCondition() {
        DataValue conditionValue = getParameter("condition");
        return conditionValue != null ? conditionValue.asString() : "";
    }
}