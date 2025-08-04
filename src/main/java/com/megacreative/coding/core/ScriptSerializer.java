package com.megacreative.coding.core;

import com.google.gson.*;
import com.megacreative.coding.blocks.Block;
import com.megacreative.coding.blocks.BlockRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Type;

/**
 * Класс для сериализации и десериализации скриптов в формат JSON.
 */
public class ScriptSerializer {
    private final JavaPlugin plugin;
    private final Gson gson;
    private final BlockRegistry blockRegistry;
    
    public ScriptSerializer(JavaPlugin plugin, BlockRegistry blockRegistry) {
        this.plugin = plugin;
        this.blockRegistry = blockRegistry;
        
        // Настраиваем Gson с кастомными адаптерами
        GsonBuilder builder = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Block.class, new BlockAdapter())
                .enableComplexMapKeySerialization();
                
        this.gson = builder.create();
    }
    
    /**
     * Сериализует блок в JSON-строку.
     */
    public String serialize(Block block) {
        return gson.toJson(block);
    }
    
    /**
     * Десериализует блок из JSON-строки.
     */
    public Block deserialize(String json) {
        return gson.fromJson(json, Block.class);
    }
    
    /**
     * Сохраняет скрипт в файл.
     */
    public void saveScript(Block script, File file) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(script, writer);
        }
    }
    
    /**
     * Загружает скрипт из файла.
     */
    public Block loadScript(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, Block.class);
        }
    }
    
    /**
     * Кастомный адаптер для сериализации/десериализации блоков.
     */
    private class BlockAdapter implements JsonSerializer<Block>, JsonDeserializer<Block> {
        private static final String CLASSNAME = "class";
        private static final String DATA = "data";
        
        @Override
        public JsonElement serialize(Block src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            String className = src.getClass().getName();
            jsonObject.addProperty(CLASSNAME, className);
            
            // Используем стандартную сериализацию для данных блока
            JsonElement data = context.serialize(src);
            jsonObject.add(DATA, data);
            
            return jsonObject;
        }
        
        @Override
        public Block deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
            String className = prim.getAsString();
            
            try {
                // Загружаем класс блока
                Class<?> clazz = Class.forName(className);
                
                // Создаем экземпляр блока через реестр
                Block block = blockRegistry.createBlock(clazz);
                if (block == null) {
                    throw new JsonParseException("Unknown block type: " + className);
                }
                
                // Десериализуем данные в блок
                JsonElement data = jsonObject.get(DATA);
                if (data != null) {
                    Block deserialized = gson.fromJson(data, clazz);
                    // Копируем свойства из десериализованного блока
                    copyBlockProperties(deserialized, block);
                }
                
                return block;
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Block class not found: " + className, e);
            }
        }
        
        /**
         * Копирует свойства из одного блока в другой.
         */
        private void copyBlockProperties(Block source, Block destination) {
            // Копируем основные свойства
            destination.setId(source.getId());
            destination.setName(source.getName());
            destination.setDescription(source.getDescription());
            
            // Копируем параметры
            source.getParameters().forEach((key, value) -> 
                destination.setParameter(key, value));
                
            // TODO: Копировать дочерние блоки и связи
        }
    }
}
