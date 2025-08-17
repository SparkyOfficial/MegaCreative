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
        // Устанавливаем уникальный ID для шаблона, если его нет
        if (template.getId() == null) {
            template.setId(UUID.randomUUID());
        }

        // Удаляем старую версию шаблона с таким же именем, чтобы избежать дубликатов
        templates.removeIf(t -> t.getName().equals(template.getName()));

        // Добавляем новую или обновленную версию
        templates.add(template);

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
    @SuppressWarnings("unchecked")
    private void loadTemplates() {
        if (!templatesFile.exists()) {
            return;
        }
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(templatesFile);
            if (config.contains("templates")) {
                List<CodeScript> loadedTemplates = (List<CodeScript>) config.getList("templates");
                if (loadedTemplates != null) {
                    templates.clear();
                    templates.addAll(loadedTemplates);
                    plugin.getLogger().info("Загружено " + templates.size() + " шаблонов");
                } else {
                    plugin.getLogger().warning("Не удалось загрузить шаблоны из файла templates.yml (список пуст).");
                }
            }
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
            config.set("templates", templates);
            config.save(templatesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка сохранения шаблонов: " + e.getMessage());
        }
    }
} 