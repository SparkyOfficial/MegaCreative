package com.megacreative.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Утилитный класс для сериализации объектов в JSON с помощью Gson.
 * Заменяет ручную сериализацию на более надежную и автоматическую.
 */
public class JsonSerializer {
    
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    
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
     * Сериализует CodeBlock в JSON
     */
    public static String serializeBlock(CodeBlock block) {
        return toJson(block);
    }
    
    /**
     * Десериализует CodeBlock из JSON
     */
    public static CodeBlock deserializeBlock(String json) {
        return fromJson(json, CodeBlock.class);
    }

    /**
     * TypeAdapterFactory для поддержки java.util.Optional в Gson 2.11+ без рефлексии приватных полей JDK.
     */
    private static class OptionalTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!Optional.class.isAssignableFrom(type.getRawType())) {
                return null;
            }

            Type optionalType = type.getType();
            Type valueType;
            if (optionalType instanceof ParameterizedType) {
                valueType = ((ParameterizedType) optionalType).getActualTypeArguments()[0];
            } else {
                // Без параметра типа трактуем как Optional<Object>
                valueType = Object.class;
            }

            final TypeAdapter<?> valueAdapter = gson.getAdapter(TypeToken.get(valueType));

            TypeAdapter<Optional<?>> result = new TypeAdapter<Optional<?>>() {
                @Override
                public void write(JsonWriter out, Optional<?> value) throws IOException {
                    if (value == null || value.isEmpty()) {
                        out.nullValue();
                        return;
                    }
                    ((TypeAdapter) valueAdapter).write(out, value.get());
                }

                @Override
                public Optional<?> read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return Optional.empty();
                    }
                    Object inner = ((TypeAdapter) valueAdapter).read(in);
                    return Optional.ofNullable(inner);
                }
            }.nullSafe();

            return (TypeAdapter<T>) result;
        }
    }
} 