package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Хранит всю информацию, необходимую для выполнения одного скрипта.
 * Передается между блоками во время исполнения.
 */
@Getter
@Builder
public class ExecutionContext {

    private final MegaCreative plugin; // Ссылка на основной плагин
    private final Player player; // Игрок, который вызвал событие (может быть null)
    private final CreativeWorld creativeWorld; // Мир, в котором выполняется скрипт
    private final Event event; // Само событие, которое вызвало скрипт
    private final Location blockLocation; // Локация выполняемого блока (может быть null)
    private final CodeBlock currentBlock; // Текущий выполняемый блок (может быть null)

    // Переменные скрипта (String -> Object)
    @Builder.Default
    private Map<String, Object> variables = new HashMap<>();
    
    // --- РАСШИРЕННЫЕ ТИПЫ ДАННЫХ ---
    // Списки (массивы) для хранения коллекций данных
    @Builder.Default
    private Map<String, List<Object>> lists = new HashMap<>();
    
    // Булевы переменные для логических операций
    @Builder.Default
    private Map<String, Boolean> booleans = new HashMap<>();
    
    // Числовые переменные с поддержкой int и double
    @Builder.Default
    private Map<String, Number> numbers = new HashMap<>();

    /**
     * Создает новый контекст с указанным текущим блоком.
     * @param currentBlock Текущий блок для выполнения
     * @param newLocation Новая локация блока
     * @return Новый контекст с обновленным блоком и локацией
     */
    public ExecutionContext withCurrentBlock(CodeBlock currentBlock, Location newLocation) {
        return ExecutionContext.builder()
                .plugin(this.plugin)
                .player(this.player)
                .creativeWorld(this.creativeWorld)
                .event(this.event)
                .blockLocation(newLocation) // Обновляем локацию
                .currentBlock(currentBlock)
                .variables(new HashMap<>(this.variables))
                .lists(new HashMap<>(this.lists))
                .booleans(new HashMap<>(this.booleans))
                .numbers(new HashMap<>(this.numbers))
                .build();
    }

    /**
     * Устанавливает переменную
     */
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }
    
    /**
     * Получает переменную
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }
    
    /**
     * Получает все переменные
     */
    public Map<String, Object> getVariables() {
        return new HashMap<>(variables);
    }
    
    // --- МЕТОДЫ ДЛЯ РАБОТЫ СО СПИСКАМИ ---
    
    /**
     * Создает или обновляет список
     */
    public void setList(String name, List<Object> list) {
        lists.put(name, new ArrayList<>(list));
    }
    
    /**
     * Получает список
     */
    public List<Object> getList(String name) {
        return lists.get(name);
    }
    
    /**
     * Добавляет элемент в список
     */
    public void addToList(String name, Object element) {
        lists.computeIfAbsent(name, k -> new ArrayList<>()).add(element);
    }
    
    /**
     * Удаляет элемент из списка
     */
    public void removeFromList(String name, Object element) {
        List<Object> list = lists.get(name);
        if (list != null) {
            list.remove(element);
        }
    }
    
    // --- МЕТОДЫ ДЛЯ РАБОТЫ С БУЛЕВЫМИ ПЕРЕМЕННЫМИ ---
    
    /**
     * Устанавливает булеву переменную
     */
    public void setBoolean(String name, boolean value) {
        booleans.put(name, value);
    }
    
    /**
     * Получает булеву переменную
     */
    public Boolean getBoolean(String name) {
        return booleans.get(name);
    }
    
    // --- МЕТОДЫ ДЛЯ РАБОТЫ С ЧИСЛАМИ ---
    
    /**
     * Устанавливает числовую переменную
     */
    public void setNumber(String name, Number value) {
        numbers.put(name, value);
    }
    
    /**
     * Получает числовую переменную
     */
    public Number getNumber(String name) {
        return numbers.get(name);
    }
    
    /**
     * Получает число как int
     */
    public int getInt(String name) {
        Number number = numbers.get(name);
        return number != null ? number.intValue() : 0;
    }
    
    /**
     * Получает число как double
     */
    public double getDouble(String name) {
        Number number = numbers.get(name);
        return number != null ? number.doubleValue() : 0.0;
    }
}
