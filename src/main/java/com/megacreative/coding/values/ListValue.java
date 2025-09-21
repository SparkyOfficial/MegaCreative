package com.megacreative.coding.values;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class ListValue implements DataValue {
    private final List<DataValue> list;

    public ListValue(List<DataValue> list) {
        this.list = list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public static ListValue of(List<DataValue> list) {
        return new ListValue(list);
    }

    public static ListValue of(DataValue... values) {
        List<DataValue> list = new ArrayList<>();
        Collections.addAll(list, values);
        return new ListValue(list);
    }

    public List<DataValue> getList() {
        return new ArrayList<>(list);
    }

    public void add(DataValue value) {
        list.add(value);
    }

    public DataValue get(int index) {
        if (index >= 0 && index < list.size()) {
            return list.get(index);
        }
        return DataValue.of("");
    }

    public int size() {
        return list.size();
    }

    @Override
    public ValueType getType() {
        return ValueType.LIST;
    }

    @Override
    public Object getValue() {
        return list;
    }

    @Override
    public void setValue(Object value) throws IllegalArgumentException {
        // ListValue is immutable, so we don't allow setting values
        throw new IllegalArgumentException("ListValue is immutable");
    }

    @Override
    public String asString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(list.get(i).asString());
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public Number asNumber() {
        return list.size();
    }

    @Override
    public boolean asBoolean() {
        return !list.isEmpty();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean isValid() {
        for (DataValue value : list) {
            if (!value.isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "List with " + list.size() + " items";
    }

    @Override
    public DataValue clone() {
        try {
            ListValue cloned = (ListValue) super.clone();
            List<DataValue> clonedList = new ArrayList<>();
            for (DataValue value : list) {
                clonedList.add(value.clone());
            }
            // Use reflection to set the list field since it's final
            java.lang.reflect.Field listField = ListValue.class.getDeclaredField("list");
            listField.setAccessible(true);
            listField.set(cloned, clonedList);
            return cloned;
        } catch (CloneNotSupportedException | NoSuchFieldException | IllegalAccessException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Clone not supported", e);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "LIST");
        map.put("value", list);
        return map;
    }
}