package com.megacreative.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Advanced Dependency Injection Container with automatic resolution
 *
 * Расширенный контейнер внедрения зависимостей с автоматическим разрешением
 *
 * Erweiterter Dependency-Injection-Container mit automatischer Auflösung
 */
public class DependencyContainer {
    
    private static final Logger log = Logger.getLogger(DependencyContainer.class.getName());
    static {
        log.setLevel(Level.INFO);
    }
    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementations = new HashMap<>();
    private final Set<Class<?>> creating = new HashSet<>();
    
    /**
     * Registers a singleton service instance
     * @param type Service interface/class
     * @param implementation Service implementation instance
     * @param <T> Service type
     *
     * Регистрирует экземпляр синглтон-сервиса
     * @param type Интерфейс/класс сервиса
     * @param implementation Экземпляр реализации сервиса
     * @param <T> Тип сервиса
     *
     * Registriert eine Singleton-Service-Instanz
     * @param type Service-Schnittstelle/Klasse
     * @param implementation Service-Implementierungsinstanz
     * @param <T> Service-Typ
     */
    public <T> void registerSingleton(Class<T> type, T implementation) {
        singletons.put(type, implementation);
        log.info("Registered singleton: " + type.getSimpleName());
        // Зарегистрированный синглтон:
        // Registrierter Singleton:
    }
    
    /**
     * Registers an implementation class for a service type
     * @param type Service interface
     * @param implementationClass Implementation class
     * @param <T> Service type
     *
     * Регистрирует класс реализации для типа сервиса
     * @param type Интерфейс сервиса
     * @param implementationClass Класс реализации
     * @param <T> Тип сервиса
     *
     * Registriert eine Implementierungsklasse für einen Service-Typ
     * @param type Service-Schnittstelle
     * @param implementationClass Implementierungsklasse
     * @param <T> Service-Typ
     */
    public <T> void registerType(Class<T> type, Class<? extends T> implementationClass) {
        implementations.put(type, implementationClass);
        log.info("Registered type mapping: " + type.getSimpleName() + " -> " + implementationClass.getSimpleName());
        // Зарегистрированное сопоставление типов:
        // Registrierte Typzuordnung:
    }
    
    /**
     * Gets or creates a service instance with automatic dependency injection
     * @param type Service type
     * @param <T> Service type
     * @return Service instance
     *
     * Получает или создает экземпляр сервиса с автоматическим внедрением зависимостей
     * @param type Тип сервиса
     * @param <T> Тип сервиса
     * @return Экземпляр сервиса
     *
     * Ruft eine Service-Instanz mit automatischer Dependency Injection ab oder erstellt sie
     * @param type Service-Typ
     * @param <T> Service-Typ
     * @return Service-Instanz
     */
    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {
        // Check for existing singleton
        // Проверить существующий синглтон
        // Auf vorhandenen Singleton prüfen
        if (singletons.containsKey(type)) {
            return (T) singletons.get(type);
        }
        
        // Prevent circular dependencies
        // Предотвратить циклические зависимости
        // Zirkuläre Abhängigkeiten verhindern
        if (creating.contains(type)) {
            throw new RuntimeException("Circular dependency detected for type: " + type.getName());
            // Обнаружена циклическая зависимость для типа:
            // Zirkuläre Abhängigkeit für Typ erkannt:
        }
        
        try {
            creating.add(type);
            
            // Get implementation class
            // Получить класс реализации
            // Implementierungsklasse abrufen
            Class<?> implementationClass = implementations.getOrDefault(type, type);
            
            // Find constructor and resolve dependencies
            // Найти конструктор и разрешить зависимости
            // Konstruktor finden und Abhängigkeiten auflösen
            T instance = createInstance((Class<T>) implementationClass);
            
            // Register as singleton for future use
            // Зарегистрировать как синглтон для будущего использования
            // Als Singleton für zukünftige Verwendung registrieren
            singletons.put(type, instance);
            
            return instance;
        } finally {
            creating.remove(type);
        }
    }
    
    /**
     * Creates an instance with dependency injection
     *
     * Создает экземпляр с внедрением зависимостей
     *
     * Erstellt eine Instanz mit Dependency Injection
     */
    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        
        // Find best constructor (prefer one with most parameters that we can resolve)
        // Найти лучший конструктор (предпочитать тот, у которого больше параметров, которые мы можем разрешить)
        // Besten Konstruktor finden (bevorzugen Sie einen mit den meisten Parametern, die wir auflösen können)
        Constructor<?> bestConstructor = null;
        int maxResolvableParams = -1;
        
        for (Constructor<?> constructor : constructors) {
            Parameter[] parameters = constructor.getParameters();
            int resolvableCount = 0;
            
            for (Parameter param : parameters) {
                if (canResolve(param.getType())) {
                    resolvableCount++;
                }
            }
            
            if (resolvableCount == parameters.length && resolvableCount > maxResolvableParams) {
                bestConstructor = constructor;
                maxResolvableParams = resolvableCount;
            }
        }
        
        if (bestConstructor == null) {
            throw new RuntimeException("No suitable constructor found for: " + clazz.getName());
            // Не найден подходящий конструктор для:
            // Kein geeigneter Konstruktor gefunden für:
        }
        
        try {
            // Resolve constructor parameters
            // Разрешить параметры конструктора
            // Konstruktorparameter auflösen
            Parameter[] parameters = bestConstructor.getParameters();
            Object[] args = new Object[parameters.length];
            
            for (int i = 0; i < parameters.length; i++) {
                args[i] = resolve(parameters[i].getType());
            }
            
            return (T) bestConstructor.newInstance(args);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to create instance of " + clazz.getName(), e);
            // Не удалось создать экземпляр
            // Fehler beim Erstellen der Instanz von
            throw new RuntimeException("Failed to create instance: " + clazz.getName(), e);
            // Не удалось создать экземпляр:
            // Fehler beim Erstellen der Instanz:
        }
    }
    
    /**
     * Checks if a type can be resolved
     *
     * Проверяет, можно ли разрешить тип
     *
     * Prüft, ob ein Typ aufgelöst werden kann
     */
    private boolean canResolve(Class<?> type) {
        return singletons.containsKey(type) || 
               implementations.containsKey(type) || 
               !type.isInterface();
    }
    
    // Legacy methods for backward compatibility
    // Устаревшие методы для обратной совместимости
    // Veraltete Methoden für Abwärtskompatibilität
    
    /**
     * @deprecated Use registerSingleton instead
     *
     * @deprecated Вместо этого используйте registerSingleton
     *
     * @deprecated Verwenden Sie stattdessen registerSingleton
     */
    @Deprecated
    public <T> void register(Class<T> type, T implementation) {
        registerSingleton(type, implementation);
    }
    
    /**
     * @deprecated Use resolve instead
     *
     * @deprecated Вместо этого используйте resolve
     *
     * @deprecated Verwenden Sie stattdessen resolve
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        return resolve(type);
    }
    
    /**
     * Checks if a service is registered
     * @param type Service type
     * @return true if service is registered
     *
     * Проверяет, зарегистрирован ли сервис
     * @param type Тип сервиса
     * @return true, если сервис зарегистрирован
     *
     * Prüft, ob ein Service registriert ist
     * @param type Service-Typ
     * @return true, wenn der Service registriert ist
     */
    public boolean has(Class<?> type) {
        return singletons.containsKey(type) || implementations.containsKey(type);
    }
    
    /**
     * Alias for has() method for better readability
     * @param type Service type
     * @return true if service is registered
     *
     * Псевдоним для метода has() для лучшей читаемости
     * @param type Тип сервиса
     * @return true, если сервис зарегистрирован
     *
     * Alias für die has()-Methode zur besseren Lesbarkeit
     * @param type Service-Typ
     * @return true, wenn der Service registriert ist
     */
    public boolean isRegistered(Class<?> type) {
        return has(type);
    }
    
    /**
     * Removes a service from container
     * @param type Service type
     *
     * Удаляет сервис из контейнера
     * @param type Тип сервиса
     *
     * Entfernt einen Service aus dem Container
     * @param type Service-Typ
     */
    public void remove(Class<?> type) {
        singletons.remove(type);
        implementations.remove(type);
    }
    
    /**
     * Clears all services
     *
     * Очищает все сервисы
     *
     * Löscht alle Services
     */
    public void clear() {
        singletons.clear();
        implementations.clear();
        creating.clear();
    }
    
    /**
     * Gets number of registered services
     * @return Number of services
     *
     * Получает количество зарегистрированных сервисов
     * @return Количество сервисов
     *
     * Ruft die Anzahl der registrierten Services ab
     * @return Anzahl der Services
     */
    public int size() {
        return singletons.size() + implementations.size();
    }
}