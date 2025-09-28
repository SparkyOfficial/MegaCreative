package com.megacreative.coding.scanner;

import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.BlockType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
// import java.util.logging.Logger;  // Removed logger import
/**
 * Scanner for automatically discovering block actions and conditions using annotations.
 * This eliminates the need for manual registration in factory classes.
 */
public class BlockScanner {
    // private static final Logger log = Logger.getLogger(BlockScanner.class.getName());  // Removed logger declaration
    // Removed unused collections that were never queried:
    // private final Map<String, Class<? extends BlockAction>> actionClasses = new HashMap<>();
    // private final Map<String, Class<? extends BlockCondition>> conditionClasses = new HashMap<>();
    
    /**
     * Scans packages for annotated block classes
     * @param packageNames the package names to scan
     */
    public void scanPackages(String... packageNames) {
        for (String packageName : packageNames) {
            try {
                scanPackage(packageName);
            } catch (Exception e) {
                // Removed log statement
            }
        }
    }
    
    /**
     * Scans a single package for annotated classes
     * @param packageName the package name to scan
     */
    private void scanPackage(String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                scanFilePackage(resource.getFile(), packageName);
            } else if (resource.getProtocol().equals("jar")) {
                scanJarPackage(resource, packageName);
            }
        }
    }
    
    /**
     * Scans a file-based package
     * @param path the file path
     * @param packageName the package name
     */
    private void scanFilePackage(String path, String packageName) {
        File directory = new File(path);
        if (!directory.exists()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanFilePackage(file.getAbsolutePath(), packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                processClass(className);
            }
        }
    }
    
    /**
     * Scans a JAR package
     * @param resource the URL resource
     * @param packageName the package name
     */
    private void scanJarPackage(URL resource, String packageName) throws IOException {
        String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
        JarFile jar = new JarFile(jarPath);
        
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class") && entry.getName().startsWith(packageName.replace('.', '/'))) {
                String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                processClass(className);
            }
        }
        
        jar.close();
    }
    
    /**
     * Processes a class to check for annotations
     * @param className the class name
     */
    private void processClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            
            // Check if the class has the BlockMeta annotation
            if (clazz.isAnnotationPresent(BlockMeta.class)) {
                BlockMeta annotation = clazz.getAnnotation(BlockMeta.class);
                
                // Register based on type
                if (annotation.type() == BlockType.ACTION && BlockAction.class.isAssignableFrom(clazz)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends BlockAction> actionClass = (Class<? extends BlockAction>) clazz;
                    // Removed unused actionClasses map
                    // actionClasses.put(annotation.id(), actionClass);
                    // Removed log statement
                } else if (annotation.type() == BlockType.CONDITION && BlockCondition.class.isAssignableFrom(clazz)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends BlockCondition> conditionClass = (Class<? extends BlockCondition>) clazz;
                    // Removed unused conditionClasses map
                    // conditionClasses.put(annotation.id(), conditionClass);
                    // Removed log statement
                }
            }
        } catch (ClassNotFoundException e) {
            // Removed log statement
        } catch (Exception e) {
            // Removed log statement
        }
    }
    
}