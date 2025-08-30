package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Представляет один блок в скрипте.
 * Содержит тип, параметры и ссылки на другие блоки.
 */
public class CodeBlock implements Cloneable {
    // ===== ПОЛЯ КЛАССА =====
    
    /** Уникальный идентификатор блока */
    private UUID id;
    
    /** Тип блока (DIAMOND_BLOCK и т.д.) */
    private Material material;
    
    /** Выбранное действие (например, onJoin, sendMessage) */
    private String action;
    
    /** Параметры блока */
    private Map<String, DataValue> parameters;
    
    /** Вложенные блоки (например, внутри условия IF) */
    private List<CodeBlock> children;
    
    /** Следующий блок в последовательности */
    private CodeBlock nextBlock;
    
    /** Предметы-конфигурации */
    private Map<Integer, ItemStack> configItems;
    
    /** Группы предметов для сложных конфигураций */
    private Map<String, List<Integer>> itemGroups;
    
    /** Ссылка на основной плагин */
    private transient MegaCreative plugin;
    
    // ===== КОНСТРУКТОРЫ =====
    
    /**
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
     * Конструктор с основными параметрами
     * @param material Материал блока
     * @param action Действие блока
     */
    public CodeBlock(Material material, String action) {
        this();
        this.material = material;
        this.action = action;
    }
    
    // ===== ГЕТТЕРЫ И СЕТТЕРЫ =====
    
    public UUID getId() { 
        return id; 
    }
    
    public void setId(UUID id) { 
        this.id = id; 
    }
    
    public Material getMaterial() { 
        return material; 
    }
    
    public void setMaterial(Material material) { 
        this.material = material; 
    }
    
    public String getAction() { 
        return action; 
    }
    
    public void setAction(String action) { 
        this.action = action; 
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
    
    public MegaCreative getPlugin() { 
        return plugin; 
    }
    
    public void setPlugin(MegaCreative plugin) { 
        this.plugin = plugin; 
    }
    
    // ===== ОСНОВНЫЕ МЕТОДЫ =====
    
    /**
     * Устанавливает параметр для блока.
     * @param key Ключ параметра (например, "material" или "message")
     * @param value Значение параметра (DataValue)
     */
    public void setParameter(String key, DataValue value) {
        parameters.put(key, value);
    }
    
    /**
     * Устанавливает параметр для блока с автоматическим преобразованием.
     * @param key Ключ параметра
     * @param value Значение для автоматического преобразования в DataValue
     */
    public void setParameter(String key, Object value) {
        parameters.put(key, DataValue.fromObject(value));
    }
    
    /**
     * Получает параметр по ключу
     * @param key Ключ параметра
     * @return Значение параметра или null, если не найдено
     */
    public DataValue getParameter(String key) {
        return parameters.get(key);
    }
    
    /**
     * Получает параметр с значением по умолчанию
     * @param key Ключ параметра
     * @param defaultValue Значение по умолчанию
     * @return Значение параметра или значение по умолчанию
     */
    public DataValue getParameter(String key, DataValue defaultValue) {
        DataValue value = parameters.get(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Добавляет дочерний блок (для условий).
     * @param child Блок, который будет выполнен внутри этого блока
     */
    public void addChild(CodeBlock child) {
        if (child != null) {
            children.add(child);
        }
    }
    
    /**
     * Устанавливает следующий блок в цепочке.
     * @param next Следующий блок
     */
    public void setNext(CodeBlock next) {
        this.nextBlock = next;
    }
    
    // ===== МЕТОДЫ ДЛЯ РАБОТЫ С ПРЕДМЕТАМИ =====
    
    /**
     * Устанавливает предмет конфигурации в указанный слот
     * @param slot Слот в инвентаре (0-8)
     * @param item Предмет для сохранения
     */
    public void setConfigItem(int slot, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            configItems.remove(slot);
        } else {
            // Сохраняем копию, чтобы избежать проблем с изменением оригинала
            configItems.put(slot, item.clone());
        }
    }
    
    /**
     * Получает предмет конфигурации из указанного слота
     * @param slot Слот в инвентаре (0-8)
     * @return Предмет или null, если слот пустой
     */
    public ItemStack getConfigItem(int slot) {
        return configItems.get(slot);
    }
    
    /**
     * Проверяет, есть ли предметы в конфигурации
     * @return true, если есть хотя бы один предмет
     */
    public boolean hasConfigItems() {
        return !configItems.isEmpty();
    }
    
    /**
     * Очищает все предметы конфигурации
     */
    public void clearConfigItems() {
        configItems.clear();
    }
    
    // ===== МЕТОДЫ ДЛЯ РАБОТЫ С ГРУППАМИ ПРЕДМЕТОВ =====
    
    /**
     * Создает группу предметов
     * @param groupName Имя группы
     * @param slots Слоты, входящие в группу
     */
    public void createItemGroup(String groupName, List<Integer> slots) {
        if (groupName != null && slots != null) {
            itemGroups.put(groupName, new ArrayList<>(slots));
        }
    }
    
    /**
     * Получает предметы из группы
     * @param groupName Имя группы
     * @return Список предметов в группе
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
     * Проверяет, есть ли группа с указанным именем
     * @param groupName Имя группы
     * @return true, если группа существует
     */
    public boolean hasItemGroup(String groupName) {
        return itemGroups.containsKey(groupName);
    }
    
    /**
     * Удаляет группу предметов
     * @param groupName Имя группы для удаления
     */
    public void removeItemGroup(String groupName) {
        itemGroups.remove(groupName);
    }
    
    /**
     * Очищает все группы предметов
     */
    public void clearItemGroups() {
        itemGroups.clear();
    }
    
    // ===== МЕТОДЫ ДЛЯ РАБОТЫ С ИМЕНОВАННЫМИ СЛОТАМИ =====
    
    /**
     * Получает предмет из именованного слота
     * @param slotName Имя слота (например, "entity_slot", "radius_slot")
     * @return Предмет из слота или null, если слот не найден
     */
    public ItemStack getItemFromSlot(String slotName) {
        if (plugin == null || slotName == null) {
            return null;
        }
        Integer slotNumber = plugin.getBlockConfiguration().getSlotNumber(this.getAction(), slotName);
        return slotNumber != null ? getConfigItem(slotNumber) : null;
    }
    
    /**
     * Получает предметы из именованной группы
     * @param groupName Имя группы (например, "items_to_give")
     * @return Список предметов из группы
     */
    public List<ItemStack> getItemsFromNamedGroup(String groupName) {
        if (plugin == null || groupName == null) {
            return new ArrayList<>();
        }
        
        List<Integer> groupSlots = plugin.getBlockConfiguration().getSlotsForGroup(this.getAction(), groupName);
        if (groupSlots.isEmpty()) {
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
     * Проверяет, есть ли конфигурация слотов для данного действия
     * @return true, если конфигурация слотов существует
     */
    public boolean hasSlotConfiguration() {
        return plugin != null && plugin.getBlockConfiguration().getActionSlotConfig(this.getAction()) != null;
    }
    
    /**
     * Проверяет, есть ли конфигурация групп для данного действия
     * @return true, если конфигурация групп существует
     */
    public boolean hasGroupConfiguration() {
        return plugin != null && plugin.getBlockConfiguration().getActionGroupConfig(this.getAction()) != null;
    }
    
    /**
     * Получает все именованные слоты для данного действия
     * @return Карта имен слотов и их номеров
     */
    public Map<String, Integer> getNamedSlots() {
        Map<String, Integer> namedSlots = new HashMap<>();
        if (plugin != null) {
            var slotConfig = plugin.getBlockConfiguration().getActionSlotConfig(this.getAction());
            if (slotConfig != null) {
                for (Map.Entry<Integer, BlockConfiguration.SlotConfig> entry : slotConfig.getSlots().entrySet()) {
                    namedSlots.put(entry.getValue().getSlotName(), entry.getKey());
                }
            }
        }
        return namedSlots;
    }
    
    /**
     * Получает все именованные группы для данного действия
     * @return Карта имен групп и списков слотов
     */
    public Map<String, List<Integer>> getNamedGroups() {
        Map<String, List<Integer>> namedGroups = new HashMap<>();
        if (plugin != null) {
            var groupConfig = plugin.getBlockConfiguration().getActionGroupConfig(this.getAction());
            if (groupConfig != null) {
                for (Map.Entry<String, BlockConfiguration.GroupConfig> entry : groupConfig.getGroups().entrySet()) {
                    namedGroups.put(entry.getKey(), entry.getValue().getSlots());
                }
            }
        }
        return namedGroups;
    }
    
    // ===== ПЕРЕОПРЕДЕЛЕННЫЕ МЕТОДЫ =====
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeBlock codeBlock = (CodeBlock) o;
        return Objects.equals(id, codeBlock.id) &&
               material == codeBlock.material &&
               Objects.equals(action, codeBlock.action) &&
               Objects.equals(parameters, codeBlock.parameters) &&
               Objects.equals(children, codeBlock.children) &&
               Objects.equals(nextBlock, codeBlock.nextBlock) &&
               Objects.equals(configItems, codeBlock.configItems) &&
               Objects.equals(itemGroups, codeBlock.itemGroups);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, material, action, parameters, children, nextBlock, configItems, itemGroups);
    }
    
    @Override
    public CodeBlock clone() {
        try {
            CodeBlock cloned = (CodeBlock) super.clone();
            cloned.id = UUID.randomUUID(); // У нового блока новый ID
            cloned.parameters = new HashMap<>(this.parameters);
            
            // Глубокая копия configItems
            cloned.configItems = new HashMap<>();
            for (Map.Entry<Integer, ItemStack> entry : this.configItems.entrySet()) {
                if (entry.getValue() != null) {
                    cloned.configItems.put(entry.getKey(), entry.getValue().clone());
                }
            }
            
            // Важно: не копируем nextBlock и children, чтобы не создавать непредсказуемых связей
            cloned.nextBlock = null;
            cloned.children = new ArrayList<>();
            
            // Глубокая копия itemGroups
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
               ", material=" + material +
               ", action='" + action + '\'' +
               ", parameters=" + parameters.size() +
               ", children=" + children.size() +
               ", configItems=" + configItems.size() +
               ", itemGroups=" + itemGroups.size() +
               '}';
    }
    
    // ===== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =====
    
    /**
     * Gets the location of this block
     * @return The location of the block or null if not defined
     */
    public org.bukkit.Location getLocation() {
        // In a real implementation, this should return the actual location of the block
        return null;
    }

    /**
     * Gets the condition of this block
     * @return The execution condition or an empty string if not defined
     */
    public String getCondition() {
        return "";
    }
}
