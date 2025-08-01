package com.megacreative.coding;

import com.megacreative.models.CreativeWorld;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Location;
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
    private final Location blockLocation; // Локация выполняемого блока (может быть null)
    private final CodeBlock currentBlock; // Текущий выполняемый блок (может быть null)

    @Builder.Default
    private Map<String, Object> variables = new HashMap<>(); // Переменные, доступные в скрипте

    /**
     * Создает новый контекст с указанным текущим блоком.
     * @param currentBlock Текущий блок для выполнения
     * @param newLocation Новая локация блока
     * @return Новый контекст с обновленным блоком и локацией
     */
    public ExecutionContext withCurrentBlock(CodeBlock currentBlock, Location newLocation) {
        return ExecutionContext.builder()
                .player(this.player)
                .creativeWorld(this.creativeWorld)
                .event(this.event)
                .blockLocation(newLocation) // Обновляем локацию
                .currentBlock(currentBlock)
                .variables(new HashMap<>(this.variables))
                .build();
    }

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
