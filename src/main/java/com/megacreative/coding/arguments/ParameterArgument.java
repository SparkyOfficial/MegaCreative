package com.megacreative.coding.arguments;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.values.TextValue;
import com.megacreative.coding.values.Value;
import java.util.Optional;

/**
 * Аргумент для извлечения параметра из блока.
 */
public class ParameterArgument implements Argument<TextValue> {
    private final String parameterName;

    public ParameterArgument(String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public Optional<TextValue> parse(CodeBlock block) {
        Object parameter = block.getParameter(parameterName);
        if (parameter != null) {
            String parameterString = parameter.toString();
            if (!parameterString.trim().isEmpty()) {
                return Optional.of(new TextValue(parameterString));
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