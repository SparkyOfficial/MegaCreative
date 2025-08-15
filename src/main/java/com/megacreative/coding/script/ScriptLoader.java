package com.megacreative.coding.script;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingManager;
import com.megacreative.coding.CodeScript;
import org.bukkit.Bukkit;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Клас для завантаження скриптів з файлів
 */
public class ScriptLoader {

    private final MegaCreative plugin;
    private final CodingManager codingManager;

    /**
     * Конструктор
     * @param plugin Посилання на основний плагін
     * @param codingManager Менеджер програмування
     */
    public ScriptLoader(MegaCreative plugin, CodingManager codingManager) {
        this.plugin = plugin;
        this.codingManager = codingManager;
    }

    /**
     * Конструктор з одним параметром для сумісності
     * @param plugin Посилання на основний плагін
     */
    public ScriptLoader(MegaCreative plugin) {
        this.plugin = plugin;
        this.codingManager = null; // Буде встановлено пізніше
    }

    /**
     * Завантажує скрипти для вказаного світу
     * @param worldId ID світу
     * @return Список завантажених скриптів
     */
    public List<CodeScript> loadScripts(String worldId) {
        List<CodeScript> scripts = new ArrayList<>();
        File scriptsDir = new File(plugin.getDataFolder(), "scripts/" + worldId);

        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs();
            return scripts; // Повертаємо порожній список, якщо папка не існує
        }

        File[] scriptFiles = scriptsDir.listFiles((dir, name) -> name.endsWith(".json"));

        if (scriptFiles == null) return scripts;

        for (File file : scriptFiles) {
            try {
                String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                CodeScript script = com.megacreative.utils.JsonSerializer.fromJson(json, CodeScript.class);
                if (script != null) {
                    scripts.add(script);
                    Bukkit.getLogger().info("Завантажено скрипт: " + file.getName());
                }
            } catch (IOException e) {
                Bukkit.getLogger().warning("Помилка при завантаженні скрипту " + file.getName() + ": " + e.getMessage());
            }
        }

        return scripts;
    }
}
