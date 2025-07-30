package com.megacreative.coding;

import com.megacreative.models.CreativeWorld;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Хранит всю информацию, необходимую для выполнения одного скрипта.
 * Передается между блоками во время исполнения.
 */
@Getter
@Builder
public class ExecutionContext {

    private final Player player; // Игрок, который вызвал событие (может быть null)
    private final CreativeWorld creativeWorld; // Мир, в котором выполняется скрипт
    private final Event event; // Само событие, которое вызвало скрипт

    @Builder.Default
    private Map<String, Object> variables = new HashMap<>(); // Переменные, доступные в скрипте

    /**
     * Устанавливает значение переменной в контексте.
     * @param name Имя переменной
     * @param value Значение
     */
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    /**
     * Получает значение переменной.
     * @param name Имя переменной
     * @return Значение или null, если переменная не найдена
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }
}
