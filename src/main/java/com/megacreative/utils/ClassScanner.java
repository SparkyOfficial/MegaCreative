package com.megacreative.utils;

import org.bukkit.plugin.java.JavaPlugin;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ClassScanner {
    public static List<Class<?>> findClasses(JavaPlugin plugin, String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        URL pluginURL = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();

        try (JarInputStream jarStream = new JarInputStream(pluginURL.openStream())) {
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                    continue;
                }
                String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                if (className.startsWith(packageName)) {
                    try {
                        classes.add(Class.forName(className, false, plugin.getClass().getClassLoader()));
                    } catch (ClassNotFoundException | NoClassDefFoundError e) { 
                        plugin.getLogger().warning("Не удалось загрузить класс (возможно, отсутствует зависимость): " + className);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при сканировании классов в плагине:");
            e.printStackTrace();
        }
        return classes;
    }
}