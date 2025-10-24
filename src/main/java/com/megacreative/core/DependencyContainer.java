package com.megacreative.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced Dependency Injection Container with automatic resolution and lifecycle management
 * Supports constructor injection, singleton management, and automatic dependency resolution
 *
 * Расширенный контейнер внедрения зависимостей с автоматическим разрешением и управлением жизненным циклом
 * Поддерживает внедрение через конструктор, управление синглтонами и автоматическое разрешение зависимостей
 *
 * Erweiterter Dependency-Injection-Container mit automatischer Auflösung und Lebenszyklusverwaltung
 * Unterstützt Konstruktorinjektion, Singleton-Verwaltung und automatische Abhängigkeitsauflösung
 */
public class DependencyContainer {
    
    private static final Logger log = Logger.getLogger(DependencyContainer.class.getName());
    static {
        log.setLevel(Level.INFO);
    }
    
    
    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();
    
    
    private final Map<Class<?>, Class<?>> implementations = new ConcurrentHashMap<>();
    
    
    private final Map<Class<?>, Supplier<?>> factories = new ConcurrentHashMap<>();
    
    
    private final Set<Class<?>> creating = Collections.synchronizedSet(new HashSet<>());
    
    
    private final List<Disposable> disposables = new ArrayList<>();
    
    /**
     * Registers a singleton service instance
     * @param type Service interface/class
     * @param implementation Service implementation instance
     * @param <T> Service type
     */
    public <T> void registerSingleton(Class<T> type, T implementation) {
        singletons.put(type, implementation);
        log.fine("Registered singleton: " + type.getSimpleName());
        
        
        if (implementation instanceof Disposable) {
            disposables.add((Disposable) implementation);
        }
    }
    
    /**
     * Registers an implementation class for a service type
     * @param type Service interface
     * @param implementationClass Implementation class
     * @param <T> Service type
     */
    public <T> void registerType(Class<T> type, Class<? extends T> implementationClass) {
        implementations.put(type, implementationClass);
        log.fine("Registered type mapping: " + type.getSimpleName() + " -> " + implementationClass.getSimpleName());
    }
    
    /**
     * Registers a factory function for creating instances of a type
     * @param type Service type
     * @param factory Factory function
     * @param <T> Service type
     */
    public <T> void registerFactory(Class<T> type, Supplier<T> factory) {
        factories.put(type, factory);
        log.fine("Registered factory for: " + type.getSimpleName());
    }
    
    /**
     * Gets or creates a service instance with automatic dependency injection
     * @param type Service type
     * @param <T> Service type
     * @return Service instance
     */
    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {
        
        if (singletons.containsKey(type)) {
            return (T) singletons.get(type);
        }
        
        
        if (factories.containsKey(type)) {
            Supplier<T> factory = (Supplier<T>) factories.get(type);
            T instance = factory.get();
            singletons.put(type, instance);
            
            
            if (instance instanceof Disposable) {
                disposables.add((Disposable) instance);
            }
            
            return instance;
        }
        
        
        if (creating.contains(type)) {
            throw new RuntimeException("Circular dependency detected for type: " + type.getName());
        }
        
        try {
            creating.add(type);
            
            
            Class<?> implementationClass = implementations.getOrDefault(type, type);
            
            
            T instance = createInstance((Class<T>) implementationClass);
            
            
            singletons.put(type, instance);
            
            
            if (instance instanceof Disposable) {
                disposables.add((Disposable) instance);
            }
            
            return instance;
        } finally {
            creating.remove(type);
        }
    }
    
    /**
     * Creates an instance with dependency injection
     */
    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> clazz) {
        
        Constructor<?> bestConstructor = findBestConstructor(clazz);
        
        if (bestConstructor == null) {
            
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("No suitable constructor found for: " + clazz.getName(), e);
            }
        }
        
        try {
            
            Parameter[] parameters = bestConstructor.getParameters();
            Object[] args = new Object[parameters.length];
            
            for (int i = 0; i < parameters.length; i++) {
                args[i] = resolve(parameters[i].getType());
            }
            
            return (T) bestConstructor.newInstance(args);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to create instance of " + clazz.getName(), e);
            throw new RuntimeException("Failed to create instance: " + clazz.getName(), e);
        }
    }
    
    /**
     * Finds the best constructor for dependency injection
     * Prefers constructors with the most resolvable parameters
     */
    private Constructor<?> findBestConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        
        
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
        
        return bestConstructor;
    }
    
    /**
     * Checks if a type can be resolved
     */
    private boolean canResolve(Class<?> type) {
        return singletons.containsKey(type) || 
               implementations.containsKey(type) || 
               factories.containsKey(type) ||
               !type.isInterface() ||
               hasSuitableConstructor(type);
    }
    
    /**
     * Checks if a type has a suitable constructor for dependency injection
     */
    private boolean hasSuitableConstructor(Class<?> type) {
        try {
            
            if (type.isInterface()) {
                
                return implementations.containsKey(type);
            } else {
                
                Constructor<?> constructor = findBestConstructor(type);
                return constructor != null;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Disposes all registered disposable services
     */
    public void dispose() {
        log.fine("Starting DependencyContainer disposal...");
        
        
        List<Exception> disposalExceptions = new ArrayList<>();
        
        for (int i = disposables.size() - 1; i >= 0; i--) {
            try {
                Disposable disposable = disposables.get(i);
                if (disposable != null) {
                    disposable.dispose();
                }
            } catch (Exception e) {
                disposalExceptions.add(e);
                log.log(Level.WARNING, "Error disposing service", e);
            }
        }
        
        
        try {
            disposables.clear();
            singletons.clear();
            implementations.clear();
            factories.clear();
            creating.clear();
        } catch (Exception e) {
            log.log(Level.WARNING, "Error clearing collections", e);
        }
        
        
        if (!disposalExceptions.isEmpty()) {
            log.warning("Encountered " + disposalExceptions.size() + " exceptions during disposal");
        }
        
        log.fine("DependencyContainer disposal completed");
    }
    
    
    
    /**
     * @deprecated Use registerSingleton instead
     */
    @Deprecated
    public <T> void register(Class<T> type, T implementation) {
        registerSingleton(type, implementation);
    }
    
    /**
     * @deprecated Use resolve instead
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
     */
    public boolean has(Class<?> type) {
        return singletons.containsKey(type) || 
               implementations.containsKey(type) || 
               factories.containsKey(type);
    }
    
    /**
     * Alias for has() method for better readability
     * @param type Service type
     * @return true if service is registered
     */
    public boolean isRegistered(Class<?> type) {
        return has(type);
    }
    
    /**
     * Removes a service from container
     * @param type Service type
     */
    public void remove(Class<?> type) {
        singletons.remove(type);
        implementations.remove(type);
        factories.remove(type);
    }
    
    /**
     * Clears all services
     */
    public void clear() {
        singletons.clear();
        implementations.clear();
        factories.clear();
        creating.clear();
        disposables.clear();
    }
    
    /**
     * Gets number of registered services
     * @return Number of services
     */
    public int size() {
        return singletons.size() + implementations.size() + factories.size();
    }
    
    /**
     * Functional interface for disposable services
     */
    public interface Disposable {
        void dispose();
    }
    
    /**
     * Functional interface for suppliers (similar to java.util.function.Supplier)
     */
    @FunctionalInterface
    public interface Supplier<T> {
        T get();
    }
}