package com.megacreative.coding;

import com.megacreative.coding.data.DataType;
import org.bukkit.potion.PotionEffectType;

public class ParameterResolver {
    public static String resolve(ExecutionContext context, Object parameterValue) {
        if (context == null) return parameterValue != null ? parameterValue.toString() : "";
        
        // Если значение не строка или не начинается с "data:", возвращаем как есть.
        if (!(parameterValue instanceof String strValue) || !strValue.startsWith("data:")) {
            return parameterValue != null ? parameterValue.toString() : "";
        }

        String[] parts = strValue.split(":", 3);
        if (parts.length < 3) {
            // Логируем ошибку формата
            if (context.getPlayer() != null) {
                context.getPlayer().sendMessage("§cОшибка формата данных: " + strValue);
            }
            return strValue; // Неверный формат
        }

        try {
            DataType type = DataType.valueOf(parts[1]);
            String valueKey = parts[2];

            switch (type) {
                case VARIABLE:
                    // Ищем значение переменной в контексте
                    Object resolvedVar = context.getVariable(valueKey);
                    if (resolvedVar == null) {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§cПеременная '" + valueKey + "' не найдена");
                        }
                        return "";
                    }
                    return resolvedVar.toString();
                    
                case TEXT:
                case NUMBER:
                    // Для TEXT и NUMBER просто возвращаем их значение
                    return valueKey;
                    
                case POTION_EFFECT:
                    // Для POTION_EFFECT проверяем, что это валидный эффект
                    PotionEffectType effectType = PotionEffectType.getByName(valueKey.toUpperCase());
                    if (effectType == null) {
                        if (context.getPlayer() != null) {
                            context.getPlayer().sendMessage("§cНеизвестный эффект зелья: " + valueKey);
                        }
                        return "";
                    }
                    return effectType.getName();
                    
                default:
                    if (context.getPlayer() != null) {
                        context.getPlayer().sendMessage("§cНеизвестный тип данных: " + type);
                    }
                    return strValue;
            }
        } catch (IllegalArgumentException e) {
            if (context.getPlayer() != null) {
                context.getPlayer().sendMessage("§cОшибка обработки данных: " + strValue);
            }
            return strValue; // Неизвестный тип данных, возвращаем как есть
        }
    }
    
    /**
     * Проверяет, является ли значение указателем на данные
     */
    public static boolean isDataPointer(Object value) {
        return value instanceof String && ((String) value).startsWith("data:");
    }
    
    /**
     * Извлекает тип данных из указателя
     */
    public static DataType getDataType(Object value) {
        if (!isDataPointer(value)) return null;
        
        String strValue = (String) value;
        String[] parts = strValue.split(":", 3);
        if (parts.length < 3) return null;
        
        try {
            return DataType.valueOf(parts[1]);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
} 