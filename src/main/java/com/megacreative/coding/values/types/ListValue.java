package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data value representing a list/array of values
 * Supports operations like add, remove, get, size, etc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ListValue extends DataValue {
    
    private final List<DataValue> values;
    
    public ListValue() {
        super(DataValue.Type.LIST);
        this.values = new ArrayList<>();
    }
    
    public ListValue(List<DataValue> values) {
        super(DataValue.Type.LIST);
        this.values = new ArrayList<>(values);
    }
    
    /**
     * Adds a value to the end of the list
     */
    public void add(DataValue value) {
        values.add(value);
    }
    
    /**
     * Adds a value at a specific index
     */
    public void add(int index, DataValue value) {
        if (index >= 0 && index <= values.size()) {
            values.add(index, value);
        }
    }
    
    /**
     * Removes a value at a specific index
     */
    public DataValue remove(int index) {
        if (index >= 0 && index < values.size()) {
            return values.remove(index);
        }
        return null;
    }
    
    /**
     * Removes the first occurrence of a value
     */
    public boolean remove(DataValue value) {\n        return values.remove(value);\n    }\n    \n    /**\n     * Gets a value at a specific index\n     */\n    public DataValue get(int index) {\n        if (index >= 0 && index < values.size()) {\n            return values.get(index);\n        }\n        return null;\n    }\n    \n    /**\n     * Sets a value at a specific index\n     */\n    public void set(int index, DataValue value) {\n        if (index >= 0 && index < values.size()) {\n            values.set(index, value);\n        }\n    }\n    \n    /**\n     * Gets the size of the list\n     */\n    public int size() {\n        return values.size();\n    }\n    \n    /**\n     * Checks if the list is empty\n     */\n    public boolean isEmpty() {\n        return values.isEmpty();\n    }\n    \n    /**\n     * Checks if the list contains a value\n     */\n    public boolean contains(DataValue value) {\n        return values.contains(value);\n    }\n    \n    /**\n     * Clears all values from the list\n     */\n    public void clear() {\n        values.clear();\n    }\n    \n    /**\n     * Gets the index of the first occurrence of a value\n     */\n    public int indexOf(DataValue value) {\n        return values.indexOf(value);\n    }\n    \n    /**\n     * Gets a copy of the internal list\n     */\n    public List<DataValue> getValues() {\n        return new ArrayList<>(values);\n    }\n    \n    @Override\n    public String asString() {\n        StringBuilder sb = new StringBuilder();\n        sb.append(\"[\");\n        for (int i = 0; i < values.size(); i++) {\n            if (i > 0) sb.append(\", \");\n            sb.append(values.get(i).asString());\n        }\n        sb.append(\"]\");\n        return sb.toString();\n    }\n    \n    @Override\n    public double asNumber() {\n        return size();\n    }\n    \n    @Override\n    public boolean asBoolean() {\n        return !isEmpty();\n    }\n    \n    @Override\n    public DataValue copy() {\n        List<DataValue> copiedValues = new ArrayList<>();\n        for (DataValue value : values) {\n            copiedValues.add(value.copy());\n        }\n        return new ListValue(copiedValues);\n    }\n    \n    @Override\n    public boolean equals(Object obj) {\n        if (this == obj) return true;\n        if (obj == null || getClass() != obj.getClass()) return false;\n        ListValue listValue = (ListValue) obj;\n        return Objects.equals(values, listValue.values);\n    }\n    \n    @Override\n    public int hashCode() {\n        return Objects.hash(values);\n    }\n    \n    @Override\n    public String toString() {\n        return \"ListValue{size=\" + size() + \", values=\" + asString() + \"}\";\n    }\n}