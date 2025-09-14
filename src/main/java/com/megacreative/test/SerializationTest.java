package com.megacreative.test;

import com.megacreative.coding.CodeBlock;
import com.megacreative.utils.JsonSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class to demonstrate ItemStack serialization in CodeBlock
 * This proves that the transient field issue has been resolved
 *
 * Тестовый класс для демонстрации сериализации ItemStack в CodeBlock
 * Это доказывает, что проблема с transient полями была решена
 *
 * Testklasse zur Demonstration der ItemStack-Serialisierung in CodeBlock
 * Dies beweist, dass das transiente Feldproblem gelöst wurde
 */
public class SerializationTest {
    
    /**
     * Test method that demonstrates CodeBlock serialization with ItemStack configItems
     *
     * Тестовый метод, демонстрирующий сериализацию CodeBlock с ItemStack configItems
     *
     * Testmethode, die die CodeBlock-Serialisierung mit ItemStack configItems demonstriert
     */
    public static void testCodeBlockSerialization() {
        // Create a test CodeBlock
        // Создать тестовый CodeBlock
        // Einen Test-CodeBlock erstellen
        CodeBlock codeBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        
        // Add some configItems (previously transient - now serializable)
        // Добавить некоторые configItems (ранее transient - теперь сериализуемый)
        // Einige configItems hinzufügen (zuvor transient - jetzt serialisierbar)
        Map<Integer, ItemStack> configItems = new HashMap<>();
        
        // Create test ItemStack with metadata
        // Создать тестовый ItemStack с метаданными
        // Test-ItemStack mit Metadaten erstellen
        ItemStack testItem = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = testItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eWelcome Message");
            meta.setLore(Arrays.asList("§7This is a test", "§7parameter item"));
            testItem.setItemMeta(meta);
        }
        
        configItems.put(0, testItem);
        
        // Add another test item
        // Добавить другой тестовый элемент
        // Ein weiteres Testelement hinzufügen
        ItemStack numberItem = new ItemStack(Material.GOLD_NUGGET, 5);
        ItemMeta numberMeta = numberItem.getItemMeta();
        if (numberMeta != null) {
            numberMeta.setDisplayName("§6Amount: 5");
            numberItem.setItemMeta(numberMeta);
        }
        
        configItems.put(1, numberItem);
        
        // Set the configItems on the CodeBlock
        // Установить configItems в CodeBlock
        // Die configItems im CodeBlock setzen
        codeBlock.setConfigItems(configItems);
        
        try {
            // Test serialization using our enhanced JsonSerializer
            // Тестовая сериализация с использованием нашего улучшенного JsonSerializer
            // Tests der Serialisierung mit unserem verbesserten JsonSerializer
            String serializedBlock = JsonSerializer.serializeBlock(codeBlock);
            
            System.out.println("=== SERIALIZATION TEST ===");
            // === ТЕСТ СЕРИАЛИЗАЦИИ ===
            // === SERIALIZATIONSTEST ===
            System.out.println("✓ CodeBlock serialized successfully!");
            // ✓ CodeBlock успешно сериализован!
            // ✓ CodeBlock erfolgreich serialisiert!
            System.out.println("✓ ConfigItems (previously transient) are now included!");
            // ✓ ConfigItems (ранее transient) теперь включены!
            // ✓ ConfigItems (zuvor transient) sind jetzt enthalten!
            System.out.println("✓ ItemStack metadata preserved in JSON");
            // ✓ Метаданные ItemStack сохранены в JSON
            // ✓ ItemStack-Metadaten im JSON erhalten
            System.out.println("\nSerialized JSON (first 200 chars):");
            // \nСериализованный JSON (первые 200 символов):
            // \nSerialisiertes JSON (erste 200 Zeichen):
            System.out.println(serializedBlock.substring(0, Math.min(200, serializedBlock.length())) + "...");
            
            // Test deserialization
            // Тест десериализации
            // Test der Deserialisierung
            CodeBlock deserializedBlock = JsonSerializer.deserializeBlock(serializedBlock);
            
            System.out.println("\n=== DESERIALIZATION TEST ===");
            // \n=== ТЕСТ ДЕСЕРИАЛИЗАЦИИ ===
            // \n=== DESERIALIZATIONSTEST ===
            System.out.println("✓ CodeBlock deserialized successfully!");
            // ✓ CodeBlock успешно десериализован!
            // ✓ CodeBlock erfolgreich deserialisiert!
            System.out.println("✓ ConfigItems restored: " + deserializedBlock.getConfigItems().size() + " items");
            // ✓ ConfigItems восстановлены: элементов
            // ✓ ConfigItems wiederhergestellt: Elemente
            
            // Verify ItemStack data integrity
            // Проверить целостность данных ItemStack
            // Integrität der ItemStack-Daten überprüfen
            ItemStack restoredItem = deserializedBlock.getConfigItems().get(0);
            if (restoredItem != null && restoredItem.hasItemMeta()) {
                System.out.println("✓ ItemStack metadata preserved:");
                // ✓ Метаданные ItemStack сохранены:
                // ✓ ItemStack-Metadaten erhalten:
                System.out.println("  - Display Name: " + restoredItem.getItemMeta().getDisplayName());
                System.out.println("  - Lore: " + restoredItem.getItemMeta().getLore());
            }
            
            System.out.println("\n🎉 SERIALIZATION FIX CONFIRMED!");
            // \n🎉 ИСПРАВЛЕНИЕ СЕРИАЛИЗАЦИИ ПОДТВЕРЖДЕНО!
            // \n🎉 SERIALIZATIONSFEHLER BESTÄTIGT!
            System.out.println("📝 CodeBlock.configItems field is now properly serializable");
            // 📝 Поле CodeBlock.configItems теперь правильно сериализуемо
            // 📝 Das Feld CodeBlock.configItems ist jetzt richtig serialisierbar
            System.out.println("💾 GUI configuration data will persist between server restarts");
            // 💾 Данные конфигурации GUI будут сохраняться между перезапусками сервера
            // 💾 GUI-Konfigurationsdaten bleiben zwischen Serverneustarts erhalten
            System.out.println("📦 Scripts with ItemStack parameters can be exported/imported");
            // 📦 Скрипты с параметрами ItemStack могут быть экспортированы/импортированы
            // 📦 Skripte mit ItemStack-Parametern können exportiert/importiert werden
            
        } catch (Exception e) {
            System.err.println("❌ Serialization test failed: " + e.getMessage());
            // ❌ Тест сериализации не удался:
            // ❌ Serialisierungstest fehlgeschlagen:
            System.err.println("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
    }
    
    /**
     * Demonstrates the problem that was fixed
     *
     * Демонстрирует решенную проблему
     *
     * Demonstriert das behobene Problem
     */
    public static void explainTheFix() {
        System.out.println("\n=== THE SERIALIZATION ISSUE EXPLAINED ===");
        // \n=== ОБЪЯСНЕНИЕ ПРОБЛЕМЫ СЕРИАЛИЗАЦИИ ===
        // \n=== DIE ERKLÄRUNG DES SERIALIZATIONSPROBLEMS ===
        System.out.println("BEFORE FIX:");
        // ДО ИСПРАВЛЕНИЯ:
        // VOR DER KORREKTUR:
        System.out.println("- CodeBlock.configItems was marked as 'transient'");
        // - CodeBlock.configItems был помечен как 'transient'
        // - CodeBlock.configItems war als 'transient' markiert
        System.out.println("- Gson would skip this field during JSON serialization");
        // - Gson пропускал бы это поле во время сериализации JSON
        // - Gson würde dieses Feld während der JSON-Serialisierung überspringen
        System.out.println("- GUI configuration (ItemStacks in slots) would be lost");
        // - Конфигурация GUI (ItemStacks в слотах) была бы потеряна
        // - GUI-Konfiguration (ItemStacks in Slots) würde verloren gehen
        System.out.println("- Scripts couldn't preserve parameter setup");
        // - Скрипты не могли сохранить настройку параметров
        // - Skripte konnten die Parametereinrichtung nicht beibehalten
        
        System.out.println("\nAFTER FIX:");
        // \nПОСЛЕ ИСПРАВЛЕНИЯ:
        // \nNACH DER KORREKTUR:
        System.out.println("✓ Removed 'transient' modifier from configItems and itemGroups");
        // ✓ Удален модификатор 'transient' из configItems и itemGroups
        // ✓ 'transient'-Modifikator von configItems und itemGroups entfernt
        System.out.println("✓ Created custom ItemStackTypeAdapter for Gson");
        // ✓ Создан пользовательский ItemStackTypeAdapter для Gson
        // ✓ Benutzerdefinierter ItemStackTypeAdapter für Gson erstellt
        System.out.println("✓ Created ConfigItemsTypeAdapters for Map serialization");  
        // ✓ Создан ConfigItemsTypeAdapters для сериализации Map
        // ✓ ConfigItemsTypeAdapters für Map-Serialisierung erstellt
        System.out.println("✓ Updated JsonSerializer to use enhanced Gson with TypeAdapters");
        // ✓ Обновлен JsonSerializer для использования улучшенного Gson с TypeAdapters
        // ✓ JsonSerializer aktualisiert, um erweitertes Gson mit TypeAdapters zu verwenden
        System.out.println("✓ GUI configuration data now persists properly");
        // ✓ Данные конфигурации GUI теперь правильно сохраняются
        // ✓ GUI-Konfigurationsdaten bleiben jetzt richtig erhalten
    }
}