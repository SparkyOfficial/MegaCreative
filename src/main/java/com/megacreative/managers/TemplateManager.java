package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.CodeBlock;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TemplateManager {
    
    private final MegaCreative plugin;
    private final File templatesFile;
    private final List<CodeScript> templates = new ArrayList<>();
    
    public TemplateManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.templatesFile = new File(plugin.getDataFolder(), "templates.yml");
        loadTemplates();
    }
    
    /**
     * Сохраняет шаблон
     */
    public void saveTemplate(CodeScript template) {
        // Устанавливаем уникальный ID для шаблона
        if (template.getId() == null) {
            template.setId(UUID.randomUUID());
        }
        
        // Добавляем в список, если его там нет
        if (!templates.contains(template)) {
            templates.add(template);
        }
        
        saveTemplates();
    }
    
    /**
     * Удаляет шаблон
     */
    public void deleteTemplate(String name) {
        templates.removeIf(template -> template.getName().equals(name));
        saveTemplates();
    }
    
    /**
     * Получает все шаблоны
     */
    public List<CodeScript> getTemplates() {
        return new ArrayList<>(templates);
    }
    
    /**
     * Находит шаблон по имени
     */
    public CodeScript getTemplate(String name) {
        return templates.stream()
                .filter(template -> template.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Загружает шаблоны из файла
     */
    private void loadTemplates() {
        if (!templatesFile.exists()) {
            return;
        }
        
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(templatesFile);
            
            if (config.contains("templates")) {
                for (String key : config.getConfigurationSection("templates").getKeys(false)) {
                    String path = "templates." + key;
                    
                    String name = config.getString(path + ".name");
                    String author = config.getString(path + ".author");
                    String idStr = config.getString(path + ".id");
                    String description = config.getString(path + ".description", "");
                    boolean enabled = config.getBoolean(path + ".enabled", true);
                    
                    if (name != null && author != null && idStr != null) {
                        // Загружаем корневой блок
                        CodeBlock rootBlock = null;
                        if (config.contains(path + ".rootBlock")) {
                            rootBlock = deserializeBlock((Map<String, Object>) config.get(path + ".rootBlock"));
                        }
                        
                        CodeScript template = new CodeScript(name, enabled, rootBlock);
                        template.setAuthor(author);
                        template.setId(UUID.fromString(idStr));
                        template.setTemplate(true);
                        template.setDescription(description);
                        
                        templates.add(template);
                    }
                }
            }
            
            plugin.getLogger().info("Загружено " + templates.size() + " шаблонов");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка загрузки шаблонов: " + e.getMessage());
        }
    }
    
    /**
     * Сохраняет шаблоны в файл
     */
    private void saveTemplates() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            
            for (int i = 0; i < templates.size(); i++) {
                CodeScript template = templates.get(i);
                String path = "templates." + i;
                
                config.set(path + ".name", template.getName());
                config.set(path + ".author", template.getAuthor());
                config.set(path + ".id", template.getId().toString());
                config.set(path + ".description", template.getDescription());
                config.set(path + ".enabled", template.isEnabled());
                
                // Сохраняем корневой блок и всю структуру
                if (template.getRootBlock() != null) {
                    config.set(path + ".rootBlock", serializeBlock(template.getRootBlock()));
                }
            }
            
            config.save(templatesFile);
            
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сохранения шаблонов: " + e.getMessage());
        }
    }
    
    /**
     * Сериализует блок кода в Map для сохранения
     */
    private Map<String, Object> serializeBlock(CodeBlock block) {
        if (block == null) return null;
        Map<String, Object> blockMap = new HashMap<>();
        blockMap.put("material", block.getMaterial().name());
        blockMap.put("action", block.getAction());
        blockMap.put("parameters", block.getParameters());
        blockMap.put("nextBlock", serializeBlock(block.getNextBlock()));
        
        // Сериализация дочерних блоков (список)
        List<Map<String, Object>> childrenList = new ArrayList<>();
        for (CodeBlock child : block.getChildren()) {
            Map<String, Object> childMap = serializeBlock(child);
            if (childMap != null) {
                childrenList.add(childMap);
            }
        }
        blockMap.put("children", childrenList);
        
        return blockMap;
    }

    /**
     * Десериализует блок кода из Map
     */
    private CodeBlock deserializeBlock(Map<String, Object> blockMap) {
        if (blockMap == null || blockMap.isEmpty()) return null;

        Material material = Material.valueOf((String) blockMap.get("material"));
        String action = (String) blockMap.get("action");
        CodeBlock block = new CodeBlock(material, action);
        
        // Восстановление параметров
        Map<String, Object> parameters = (Map<String, Object>) blockMap.get("parameters");
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                block.setParameter(entry.getKey(), entry.getValue());
            }
        }

        // Восстановление следующего блока
        if (blockMap.containsKey("nextBlock")) {
            block.setNext(deserializeBlock((Map<String, Object>) blockMap.get("nextBlock")));
        }
        
        // Восстановление дочерних блоков
        if (blockMap.containsKey("children")) {
            List<Map<String, Object>> childrenList = (List<Map<String, Object>>) blockMap.get("children");
            for (Map<String, Object> childMap : childrenList) {
                CodeBlock child = deserializeBlock(childMap);
                if (child != null) {
                    block.addChild(child);
                }
            }
        }

        return block;
    }
} 