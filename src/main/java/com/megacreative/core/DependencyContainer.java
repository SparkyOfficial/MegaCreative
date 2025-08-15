package com.megacreative.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Простой контейнер для Dependency Injection
 */
public class DependencyContainer {
    
    private final Map<Class<?>, Object> services = new HashMap<>();
    
    /**
     * Регистрирует сервис в контейнере
     * @param type Тип сервиса
     * @param implementation Реализация сервиса
     * @param <T> Тип сервиса
     */
    public <T> void register(Class<T> type, T implementation) {
        services.put(type, implementation);
    }
    
    /**
     * Получает сервис из контейнера
     * @param type Тип сервиса
     * @param <T> Тип сервиса
     * @return Реализация сервиса или null, если не найден
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        return (T) services.get(type);
    }
    
    /**
     * Проверяет, зарегистрирован ли сервис
     * @param type Тип сервиса
     * @return true если сервис зарегистрирован
     */
    public boolean has(Class<?> type) {
        return services.containsKey(type);
    }
    
    /**
     * Удаляет сервис из контейнера
     * @param type Тип сервиса
     */
    public void remove(Class<?> type) {
        services.remove(type);
    }
    
    /**
     * Очищает все сервисы
     */
    public void clear() {
        services.clear();
    }
    
    /**
     * Получает количество зарегистрированных сервисов
     * @return Количество сервисов
     */
    public int size() {
        return services.size();
    }
}
