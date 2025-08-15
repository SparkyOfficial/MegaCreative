package com.megacreative.coding.script;

import com.megacreative.MegaCreative;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import com.megacreative.coding.CodeScript;
import com.megacreative.utils.JsonSerializer;

import java.util.List;

/**
 * Зберігає скрипти у файли
 */
public class ScriptSaver {

    private final MegaCreative plugin;

    /**
     * Конструктор
     * @param plugin Посилання на основний плагін
     */
    public ScriptSaver(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Зберігає скрипти асинхронно
     * @param worldId ID світу
     * @param scripts Список скриптів
     */
    public void saveScriptsAsync(String worldId, List<CodeScript> scripts) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            saveScripts(worldId, scripts);
        });
    }

    /**
     * Зберігає скрипти
     * @param worldId ID світу
     * @param scripts Список скриптів
     */
    public void saveScripts(String worldId, List<CodeScript> scripts) {
        File scriptsDir = new File(plugin.getDataFolder(), "scripts/" + worldId);
        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs();
        }

        // Видаляємо старі файли скриптів
        File[] oldFiles = scriptsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (oldFiles != null) {
            for (File oldFile : oldFiles) {
                oldFile.delete();
            }
        }

        // Зберігаємо кожен скрипт
        for (CodeScript script : scripts) {
            File scriptFile = new File(scriptsDir, script.getId() + ".json");
            try (FileWriter writer = new FileWriter(scriptFile)) {
                String json = JsonSerializer.serializeScript(script);
                writer.write(json);
            } catch (Exception e) {
                plugin.getLogger().warning("Помилка збереження скрипта " + script.getId() + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Збережено " + scripts.size() + " скриптів для світу " + worldId);
    }
}
