package com.megacreative.coding.data;

public enum DataType {
    TEXT("Текст"),
    NUMBER("Число"),
    VARIABLE("Переменная");
    
    private final String displayName;
    DataType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
} 