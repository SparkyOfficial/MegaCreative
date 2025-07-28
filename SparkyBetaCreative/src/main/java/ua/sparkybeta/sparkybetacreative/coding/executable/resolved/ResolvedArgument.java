package ua.sparkybeta.sparkybetacreative.coding.executable.resolved;

import lombok.Getter;
import ua.sparkybeta.sparkybetacreative.coding.ValueType;

@Getter
public class ResolvedArgument<T> {
    private final T value;
    private final ValueType type;

    public ResolvedArgument(T value, ValueType type) {
        this.value = value;
        this.type = type;
    }
} 