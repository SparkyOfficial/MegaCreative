package com.megacreative.coding.data;

public enum DataType {
    TEXT("Текст"),
    NUMBER("Число"),
    VARIABLE("Переменная"),
    POTION_EFFECT("Эффект Зелья");
    
    private final String displayName;
    DataType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
} 