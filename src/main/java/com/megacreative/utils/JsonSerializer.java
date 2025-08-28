package com.megacreative.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;

/**
 * Утилитный класс для сериализации объектов в JSON с помощью Gson.
 * Заменяет ручную сериализацию на более надежную и автоматическую.
 */
public class JsonSerializer {
    
    // Standard Gson for objects without ItemStack serialization needs
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    
    // Enhanced Gson with ItemStack serialization support for CodeBlocks
    private static final Gson gsonWithItemStacks = ConfigItemsTypeAdapters.createGsonWithAdapters();
    
    /**
     * Сериализует объект в JSON строку
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
    
    /**
     * Десериализует JSON строку в объект указанного типа
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
    
    /**
     * Сериализует CreativeWorld в JSON
     */
    public static String serializeWorld(CreativeWorld world) {
        return toJson(world);
    }
    
    /**
     * Десериализует CreativeWorld из JSON
     */
    public static CreativeWorld deserializeWorld(String json) {
        return fromJson(json, CreativeWorld.class);
    }
    
    /**
     * Сериализует CodeScript в JSON
     */
    public static String serializeScript(CodeScript script) {
        return toJson(script);
    }
    
    /**
     * Десериализует CodeScript из JSON
     */
    public static CodeScript deserializeScript(String json) {
        return fromJson(json, CodeScript.class);
    }
    
    /**
     * Сериализует CodeBlock в JSON с поддержкой ItemStack
     */
    public static String serializeBlock(CodeBlock block) {
        return gsonWithItemStacks.toJson(block);
    }
    
    /**
     * Десериализует CodeBlock из JSON с поддержкой ItemStack
     */
    public static CodeBlock deserializeBlock(String json) {
        return gsonWithItemStacks.fromJson(json, CodeBlock.class);
    }
} 