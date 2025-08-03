package com.megacreative.coding;

import com.megacreative.MegaCreative;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Представляет один блок в скрипте.
 * Содержит тип, параметры и ссылки на другие блоки.
 */
@Data
@NoArgsConstructor
public class CodeBlock implements Cloneable {

    private UUID id;
    private Material material; // Тип блока (DIAMOND_BLOCK и т.д.)
    private String action;     // Выбранное действие (например, onJoin, sendMessage)
    private Map<String, Object> parameters;
    private List<CodeBlock> children; // Для вложенных блоков (например, внутри условия IF)
    private CodeBlock nextBlock; // Следующий блок в последовательности
    
    // --- ИЗМЕНЕНИЕ: Убираем 'transient' и меняем тип для правильного сохранения ---
    // Теперь мы храним не сам ItemStack, а его сериализованное представление.
    private Map<Integer, Map<String, Object>> configItems = new HashMap<>();
    
    // --- СИСТЕМА ГРУППИРОВКИ ПРЕДМЕТОВ ---
    // Группы предметов для сложных конфигураций
    private transient Map<String, List<Integer>> itemGroups = new HashMap<>();
    
    // --- ДОСТУП К ПЛАГИНУ ДЛЯ КОНФИГУРАЦИИ ---
    // Это поле не нужно сохранять, оно устанавливается при загрузке.
    private transient MegaCreative plugin;

    public CodeBlock(Material material, String action) {
        this.id = UUID.randomUUID();
        this.material = material;
        this.action = action;
        this.parameters = new HashMap<>();
        this.children = new ArrayList<>();
        this.configItems = new HashMap<>();
    }

    /**
     * Устанавливает параметр для блока.
     * @param key Ключ параметра (например, "material" или "message")
     * @param value Значение параметра
     */
    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Добавляет дочерний блок (для условий).
     * @param child Блок, который будет выполнен внутри этого блока
     */
    public void addChild(CodeBlock child) {
        children.add(child);
    }

    /**
     * Устанавливает следующий блок в цепочке.
     * @param next Следующий блок
     */
    public void setNext(CodeBlock next) {
        this.nextBlock = next;
    }
    
    /**
     * Устанавливает ссылку на плагин для доступа к конфигурации
     */
    public void setPlugin(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Получает ссылку на плагин
     */
    public MegaCreative getPlugin() {
        return plugin;
    }
    
    // --- НОВЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ВИРТУАЛЬНЫМИ ИНВЕНТАРЯМИ ---
    
    /**
     * Сохраняет ItemStack в виде, готовом для записи в JSON.
     * @param slot Слот
     * @param item Предмет
     */
    public void setConfigItem(int slot, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            configItems.remove(slot);
        } else {
            // Сериализуем ItemStack в Map<String, Object>, который Gson может сохранить
            configItems.put(slot, item.serialize());
        }
    }

    /**
     * Восстанавливает ItemStack из сохраненных данных.
     * @param slot Слот
     * @return Восстановленный предмет или null
     */
    public ItemStack getConfigItem(int slot) {
        Map<String, Object> serializedItem = configItems.get(slot);
        if (serializedItem == null) {
            return null;
        }
        // Десериализуем Map обратно в ItemStack
        return ItemStack.deserialize(serializedItem);
    }
    
    /**
     * Получает все предметы конфигурации
     * @return Карта слотов и предметов
     */
    public Map<Integer, ItemStack> getConfigItems() {
        Map<Integer, ItemStack> items = new HashMap<>();
        for (Map.Entry<Integer, Map<String, Object>> entry : configItems.entrySet()) {
            ItemStack item = ItemStack.deserialize(entry.getValue());
            if (item != null) {
                items.put(entry.getKey(), item);
            }
        }
        return items;
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
    
    // --- МЕТОДЫ ДЛЯ РАБОТЫ С ГРУППАМИ ПРЕДМЕТОВ ---
    
    /**
     * Создает группу предметов
     * @param groupName Имя группы
     * @param slots Слоты, входящие в группу
     */
    public void createItemGroup(String groupName, List<Integer> slots) {
        itemGroups.put(groupName, new ArrayList<>(slots));
    }
    
    /**
     * Получает предметы из группы
     * @param groupName Имя группы
     * @return Список предметов в группе
     */
    public List<ItemStack> getItemsFromGroup(String groupName) {
        List<Integer> groupSlots = itemGroups.get(groupName);
        if (groupSlots == null) {
            return new ArrayList<>();
        }
        
        List<ItemStack> items = new ArrayList<>();
        for (Integer slot : groupSlots) {
            ItemStack item = getConfigItem(slot);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }
    
    /**
     * Получает все группы предметов
     * @return Карта групп
     */
    public Map<String, List<Integer>> getItemGroups() {
        return new HashMap<>(itemGroups);
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
    
    // --- НОВЫЕ МЕТОДЫ ДЛЯ ИМЕНОВАННЫХ СЛОТОВ ---
    
    /**
     * Получает предмет из именованного слота
     * @param slotName Имя слота (например, "entity_slot", "radius_slot")
     * @return Предмет из слота или null, если слот не найден
     */
    public ItemStack getItemFromSlot(String slotName) {
         if (plugin == null) {
             this.plugin = MegaCreative.getInstance();
        }
        // Получаем номер слота по имени из конфигурации
        Integer slotNumber = plugin.getBlockConfiguration().getSlotNumber(this.getAction(), slotName);
        if (slotNumber != null) {
            return getConfigItem(slotNumber);
        }
        return null;
    }
    
    /**
     * Получает предметы из именованной группы
     * @param groupName Имя группы (например, "items_to_give")
     * @return Список предметов из группы
     */
    public List<ItemStack> getItemsFromNamedGroup(String groupName) {
         if (plugin == null) {
             this.plugin = MegaCreative.getInstance();
        }
        List<Integer> groupSlots = plugin.getBlockConfiguration().getSlotsForGroup(this.getAction(), groupName);
        List<ItemStack> items = new ArrayList<>();
        for (Integer slotNumber : groupSlots) {
            ItemStack item = getConfigItem(slotNumber);
            if (item != null && !item.getType().isAir()) {
                items.add(item);
            }
        }
        return items;
    }
    
    /**
     * Проверяет, есть ли конфигурация слотов для данного действия
     */
    public boolean hasSlotConfiguration() {
        if (plugin == null) {
            this.plugin = MegaCreative.getInstance();
        }
        return plugin.getBlockConfiguration().getActionSlotConfig(this.getAction()) != null;
    }
    
    /**
     * Проверяет, есть ли конфигурация групп для данного действия
     */
    public boolean hasGroupConfiguration() {
        if (plugin == null) {
            this.plugin = MegaCreative.getInstance();
        }
        return plugin.getBlockConfiguration().getActionGroupConfig(this.getAction()) != null;
    }
    
    /**
     * Получает все именованные слоты для данного действия
     */
    public Map<String, Integer> getNamedSlots() {
        if (plugin == null) {
            this.plugin = MegaCreative.getInstance();
        }
        Map<String, Integer> namedSlots = new HashMap<>();
        var slotConfig = plugin.getBlockConfiguration().getActionSlotConfig(this.getAction());
        if (slotConfig != null) {
            for (Map.Entry<Integer, BlockConfiguration.SlotConfig> entry : slotConfig.getSlots().entrySet()) {
                namedSlots.put(entry.getValue().getSlotName(), entry.getKey());
            }
        }
        return namedSlots;
    }
    
    /**
     * Получает все именованные группы для данного действия
     */
    public Map<String, List<Integer>> getNamedGroups() {
        if (plugin == null) {
            this.plugin = MegaCreative.getInstance();
        }
        Map<String, List<Integer>> namedGroups = new HashMap<>();
        var groupConfig = plugin.getBlockConfiguration().getActionGroupConfig(this.getAction());
        if (groupConfig != null) {
            for (Map.Entry<String, BlockConfiguration.GroupConfig> entry : groupConfig.getGroups().entrySet()) {
                namedGroups.put(entry.getKey(), entry.getValue().getSlots());
            }
        }
        return namedGroups;
    }

    @Override
    public CodeBlock clone() throws CloneNotSupportedException {
        CodeBlock cloned = (CodeBlock) super.clone();
        cloned.id = UUID.randomUUID(); // У нового блока новый ID
        cloned.parameters = new HashMap<>(this.parameters);
        cloned.configItems = new HashMap<>(this.configItems);
        // Важно: не копируем nextBlock и children, чтобы не создавать непредсказуемых связей
        cloned.nextBlock = null;
        cloned.children = new ArrayList<>();
        return cloned;
    }
}
