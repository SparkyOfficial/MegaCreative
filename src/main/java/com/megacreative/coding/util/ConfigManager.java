package com.megacreative.coding.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * Менеджер конфигурации плагина.
 */
public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    private final String fileName;
    
    /**
     * Создает новый менеджер конфигурации.
     * 
     * @param plugin Экземпляр плагина
     * @param fileName Имя файла конфигурации (например, "config.yml")
     */
    public ConfigManager(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.configFile = new File(plugin.getDataFolder(), fileName);
        
        // Создаем папку плагина, если она не существует
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        loadConfig();
    }
    
    /**
     * Загружает конфигурацию из файла.
     */
    public void loadConfig() {
        // Если файл не существует, копируем его из ресурсов
        if (!configFile.exists()) {
            try (InputStream in = plugin.getResource(fileName)) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                    plugin.getLogger().info("Создан новый файл конфигурации: " + fileName);
                } else {
                    // Если файл не найден в ресурсах, создаем пустой
                    configFile.createNewFile();
                    plugin.getLogger().info("Создан пустой файл конфигурации: " + fileName);
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Ошибка при создании конфигурации: " + e.getMessage());
                return;
            }
        }
        
        // Загружаем конфигурацию
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Загружена конфигурация: " + fileName);
    }
    
    /**
     * Сохраняет конфигурацию в файл.
     * 
     * @return true, если сохранение прошло успешно, иначе false
     */
    public boolean saveConfig() {
        if (config == null || configFile == null) {
            return false;
        }
        
        try {
            config.save(configFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка при сохранении конфигурации: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Перезагружает конфигурацию из файла.
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Конфигурация перезагружена: " + fileName);
    }
    
    /**
     * Получает значение из конфигурации.
     * 
     * @param path Путь к значению
     * @param def Значение по умолчанию, если путь не существует
     * @return Значение из конфигурации или значение по умолчанию
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String path, T def) {
        if (config == null) {
            return def;
        }
        
        Object value = config.get(path, def);
        
        // Обрабатываем случай, когда значение не найдено
        if (value == null) {
            // Если значение по умолчанию не null, сохраняем его в конфиг
            if (def != null) {
                set(path, def);
            }
            return def;
        }
        
        // Пытаемся привести к нужному типу
        try {
            return (T) value;
        } catch (ClassCastException e) {
            plugin.getLogger().warning(String.format(
                "Некорректный тип значения для '%s'. Ожидался %s, получен %s. Возвращено значение по умолчанию.",
                path, def != null ? def.getClass().getSimpleName() : "Object",
                value.getClass().getSimpleName()
            ));
            return def;
        }
    }
    
    /**
     * Устанавливает значение в конфигурации.
     * 
     * @param path Путь к значению
     * @param value Новое значение
     */
    public void set(String path, Object value) {
        if (config != null) {
            config.set(path, value);
        }
    }
    
    /**
     * Получает строку из конфигурации.
     */
    public String getString(String path, String def) {
        return get(path, def);
    }
    
    /**
     * Получает целое число из конфигурации.
     */
    public int getInt(String path, int def) {
        return get(path, def);
    }
    
    /**
     * Получает число с плавающей точкой из конфигурации.
     */
    public double getDouble(String path, double def) {
        return get(path, def);
    }
    
    /**
     * Получает логическое значение из конфигурации.
     */
    public boolean getBoolean(String path, boolean def) {
        return get(path, def);
    }
    
    /**
     * Получает список строк из конфигурации.
     */
    public List<String> getStringList(String path, List<String> def) {
        List<String> list = get(path, def);
        return list != null ? list : def;
    }
    
    /**
     * Проверяет существование пути в конфигурации.
     */
    public boolean contains(String path) {
        return config != null && config.contains(path);
    }
    
    /**
     * Удаляет путь из конфигурации.
     */
    public void remove(String path) {
        if (config != null) {
            config.set(path, null);
        }
    }
    
    /**
     * Возвращает объект FileConfiguration для прямого доступа к конфигурации.
     */
    public FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * Возвращает файл конфигурации.
     */
    public File getConfigFile() {
        return configFile;
    }
}
