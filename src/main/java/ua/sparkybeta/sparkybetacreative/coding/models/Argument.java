package ua.sparkybeta.sparkybetacreative.coding.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.sparkybeta.sparkybetacreative.coding.ValueType;

@Getter
@RequiredArgsConstructor
public class Argument {
    private final ValueType type;
    private final Object value;
} 