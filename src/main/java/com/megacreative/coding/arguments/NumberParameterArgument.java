package com.megacreative.coding.arguments;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.NumberValue;
import com.megacreative.coding.values.Value;
import java.util.Optional;

/**
 * Аргумент для извлечения числового параметра из блока.
 */
public class NumberParameterArgument implements Argument<NumberValue> {
    private final String parameterName;

    public NumberParameterArgument(String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public Optional<NumberValue> parse(CodeBlock block) {
        Object parameter = block.getParameter(parameterName);
        if (parameter != null) {
            String parameterString = parameter.toString();
            if (!parameterString.trim().isEmpty()) {
                return Optional.of(new NumberValue(parameterString));
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