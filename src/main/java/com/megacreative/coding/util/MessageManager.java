package com.megacreative.coding.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер сообщений для загрузки и форматирования локализованных строк.
 */
public class MessageManager {
    private final JavaPlugin plugin;
    private YamlConfiguration messages;
    private final File messagesFile;
    
    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        loadMessages();
    }
    
    /**
     * Загружает сообщения из файла.
     */
    public void loadMessages() {
        // Создаем папку плагина, если она не существует
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Копируем файл сообщений, если он отсутствует
        if (!messagesFile.exists()) {
            try (InputStream in = plugin.getResource("messages.yml")) {
                if (in != null) {
                    Files.copy(in, messagesFile.toPath());
                } else {
                    plugin.getLogger().warning("Не удалось загрузить messages.yml из ресурсов!");
                    return;
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Ошибка при создании messages.yml: " + e.getMessage());
                return;
            }
        }
        
        // Загружаем сообщения из файла
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        plugin.getLogger().info("Загружены сообщения из messages.yml");
    }
    
    /**
     * Перезагружает сообщения из файла.
     */
    public void reloadMessages() {
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    /**
     * Получает сообщение по пути и форматирует его с указанными аргументами.
     * 
     * @param path Путь к сообщению в конфигурации
     * @param args Аргументы для форматирования (в формате "%key%", "value")
     * @return Отформатированное сообщение с цветовыми кодами
     */
    public String getMessage(String path, String... args) {
        String message = messages.getString(path, "&cСообщение не найдено: " + path);
        
        // Применяем цветовые коды
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        // Заменяем плейсхолдеры
        if (args != null && args.length >= 2) {
            for (int i = 0; i < args.length; i += 2) {
                if (i + 1 < args.length) {
                    message = message.replace("%" + args[i] + "%", args[i + 1]);
                }
            }
        }
        
        return message;
    }
    
    /**
     * Получает список сообщений по пути и форматирует их с указанными аргументами.
     */
    public List<String> getMessages(String path, String... args) {
        List<String> messageList = new ArrayList<>();
        
        // Проверяем, существует ли путь как список
        if (messages.isList(path)) {
            for (String line : messages.getStringList(path)) {
                // Применяем цветовые коды
                line = ChatColor.translateAlternateColorCodes('&', line);
                
                // Заменяем плейсхолдеры
                if (args != null && args.length >= 2) {
                    for (int i = 0; i < args.length; i += 2) {
                        if (i + 1 < args.length) {
                            line = line.replace("%" + args[i] + "%", args[i + 1]);
                        }
                    }
                }
                
                messageList.add(line);
            }
        } else {
            // Если это не список, возвращаем одиночное сообщение как список из одного элемента
            messageList.add(getMessage(path, args));
        }
        
        return messageList;
    }
    
    /**
     * Сохраняет сообщение в конфигурацию и файл.
     */
    public void setMessage(String path, Object value) {
        messages.set(path, value);
        try {
            messages.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка при сохранении сообщений: " + e.getMessage());
        }
    }
    
    /**
     * Добавляет сообщение в список в конфигурации.
     */
    public void addMessage(String path, String value) {
        List<String> list = new ArrayList<>();
        if (messages.isList(path)) {
            list = messages.getStringList(path);
        }
        list.add(value);
        setMessage(path, list);
    }
    
    /**
     * Проверяет существование пути в конфигурации сообщений.
     */
    public boolean hasMessage(String path) {
        return messages.contains(path);
    }
}
