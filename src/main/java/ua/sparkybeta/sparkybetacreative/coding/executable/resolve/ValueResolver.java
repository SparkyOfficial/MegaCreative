package ua.sparkybeta.sparkybetacreative.coding.executable.resolve;

import ua.sparkybeta.sparkybetacreative.coding.executable.ExecutionContext;
import ua.sparkybeta.sparkybetacreative.coding.executable.resolved.ResolvedArgument;
import ua.sparkybeta.sparkybetacreative.coding.models.Argument;
import ua.sparkybeta.sparkybetacreative.coding.ValueType;


public class ValueResolver {

    /**
     * Resolves a static argument into a dynamic, usable value within a given execution context.
     * @param argument The static argument from the code block.
     * @param context The context in which the code is being executed (e.g., holding the event player).
     * @return A ResolvedArgument containing the actual value and its type.
     */
    public ResolvedArgument<?> resolve(Argument argument, ExecutionContext context) {
        if (argument.getType() == ValueType.DYNAMIC_PLAYER) {
            return new ResolvedArgument<>(context.getPlayer(), ValueType.PLAYER);
        }

        // For now, for all other types, we'll just return the static value.
        // More logic will be added here for other dynamic types.
        return new ResolvedArgument<>(argument.getValue(), argument.getType());
    }
} 