package com.megacreative.coding.actions;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;

/**
 * Represents a variable value with its scope and context information.
 */
public class VariableValue {
    public final DataValue value;
    public final VariableManager.VariableScope scope;
    public final String context;
    
    public VariableValue(DataValue value, VariableManager.VariableScope scope, String context) {
        this.value = value;
        this.scope = scope;
        this.context = context;
    }
}