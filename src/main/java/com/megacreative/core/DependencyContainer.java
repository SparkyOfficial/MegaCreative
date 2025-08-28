package com.megacreative.core;

import lombok.extern.java.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.logging.Level;

/**
 * Advanced Dependency Injection Container with automatic resolution
 */
@Log
public class DependencyContainer {
    
    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementations = new HashMap<>();
    private final Set<Class<?>> creating = new HashSet<>();
    
    /**
     * Registers a singleton service instance
     * @param type Service interface/class
     * @param implementation Service implementation instance
     * @param <T> Service type
     */
    public <T> void registerSingleton(Class<T> type, T implementation) {
        singletons.put(type, implementation);
        log.info("Registered singleton: " + type.getSimpleName());
    }
    
    /**
     * Registers an implementation class for a service type
     * @param type Service interface
     * @param implementationClass Implementation class
     * @param <T> Service type
     */
    public <T> void registerType(Class<T> type, Class<? extends T> implementationClass) {
        implementations.put(type, implementationClass);
        log.info("Registered type mapping: " + type.getSimpleName() + " -> " + implementationClass.getSimpleName());
    }
    
    /**
     * Gets or creates a service instance with automatic dependency injection
     * @param type Service type
     * @param <T> Service type
     * @return Service instance
     */
    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {
        // Check for existing singleton
        if (singletons.containsKey(type)) {
            return (T) singletons.get(type);
        }
        
        // Prevent circular dependencies
        if (creating.contains(type)) {
            throw new RuntimeException("Circular dependency detected for type: " + type.getName());
        }
        
        try {
            creating.add(type);
            
            // Get implementation class
            Class<?> implementationClass = implementations.getOrDefault(type, type);
            
            // Find constructor and resolve dependencies
            T instance = createInstance((Class<T>) implementationClass);
            
            // Register as singleton for future use
            singletons.put(type, instance);
            
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
        Constructor<?>[] constructors = clazz.getConstructors();
        
        // Find best constructor (prefer one with most parameters that we can resolve)
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
        }
        
        try {
            // Resolve constructor parameters
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
     * Checks if a type can be resolved
     */
    private boolean canResolve(Class<?> type) {
        return singletons.containsKey(type) || 
               implementations.containsKey(type) || 
               !type.isInterface();
    }
    
    // Legacy methods for backward compatibility
    
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
        return singletons.containsKey(type) || implementations.containsKey(type);
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
    }
    
    /**
     * Clears all services
     */
    public void clear() {
        singletons.clear();
        implementations.clear();
        creating.clear();
    }
    
    /**
     * Gets number of registered services
     * @return Number of services
     */
    public int size() {
        return singletons.size() + implementations.size();
    }
}
