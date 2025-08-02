package com.megacreative.coding.arguments;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.BooleanValue;
import com.megacreative.coding.values.Value;
import java.util.Optional;

/**
 * Аргумент для извлечения булевого значения из параметра блока.
 */
public class BooleanArgument implements Argument<BooleanValue> {
    private final String parameterName;

    public BooleanArgument(String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public Optional<BooleanValue> parse(CodeBlock block) {
        Object parameter = block.getParameter(parameterName);
        if (parameter != null) {
            String parameterString = parameter.toString();
            if (!parameterString.trim().isEmpty()) {
                return Optional.of(new BooleanValue(parameterString));
            }
        }
        return Optional.empty();
    }

    /**
     * Возвращает имя параметра, из которого извлекается значение.
     * @return Имя параметра
     */
    public String getParameterName() {
        return parameterName;
    }
} 