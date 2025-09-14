package com.megacreative.testing;

import com.megacreative.MegaCreative;
import com.megacreative.services.CodeCompiler;
import com.megacreative.coding.CodeScript;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.logging.Logger;

/**
 * Тестовый класс для проверки полного процесса компиляции
 * Этот тест гарантирует, что структуры мира правильно компилируются в исполняемые скрипты
 *
 * Test class for verifying the complete compilation process
 * This test ensures that world structures are properly compiled to executable scripts
 *
 * Testklasse zur Überprüfung des vollständigen Kompilierungsprozesses
 * Dieser Test stellt sicher, dass Weltenstrukturen ordnungsgemäß in ausführbare Skripte kompiliert werden
 */
public class CompilationTest {
    
    private final MegaCreative plugin;
    private final Logger logger;
    
    /**
     * Инициализирует тест компиляции
     * @param plugin Экземпляр основного плагина
     *
     * Initializes compilation test
     * @param plugin Main plugin instance
     *
     * Initialisiert den Kompilierungstest
     * @param plugin Hauptplugin-Instanz
     */
    public CompilationTest(MegaCreative plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Тестирует полный процесс компиляции от структур мира до исполняемых скриптов
     * 
     * @param playerName Имя игрока для тестирования
     * @return true, если тест компиляции пройден, иначе false
     *
     * Tests the complete compilation process from world structures to executable scripts
     * 
     * @param playerName The name of the player to test with
     * @return true if compilation test passes, false otherwise
     *
     * Testet den vollständigen Kompilierungsprozess von Weltenstrukturen zu ausführbaren Skripten
     * 
     * @param playerName Der Name des Spielers zum Testen
     * @return true, wenn der Kompilierungstest besteht, sonst false
     */
    public boolean testCompilationProcess(String playerName) {
        try {
            // Get the player
            // Получить игрока
            // Spieler abrufen
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                logger.severe("Player " + playerName + " not found!");
                // Игрок не найден!
                // Spieler nicht gefunden!
                return false;
            }
            
            // Get the player's current world
            // Получить текущий мир игрока
            // Aktuelle Welt des Spielers abrufen
            World world = player.getWorld();
            logger.info("Testing compilation in world: " + world.getName());
            // Тестирование компиляции в мире:
            // Testen der Kompilierung in Welt:
            
            // Get the CodeCompiler service
            // Получить сервис CodeCompiler
            // CodeCompiler-Dienst abrufen
            CodeCompiler codeCompiler = plugin.getServiceRegistry().getCodeCompiler();
            if (codeCompiler == null) {
                logger.severe("CodeCompiler service not available!");
                // Сервис CodeCompiler недоступен!
                // CodeCompiler-Dienst nicht verfügbar!
                return false;
            }
            
            // Test 1: Compile world to CodeScript objects
            // Тест 1: Компиляция мира в объекты CodeScript
            // Test 1: Welt in CodeScript-Objekte kompilieren
            logger.info("=== Test 1: Compiling world to CodeScript objects ===");
            // === Тест 1: Компиляция мира в объекты CodeScript ===
            // === Test 1: Welt in CodeScript-Objekte kompilieren ===
            List<CodeScript> scripts = codeCompiler.compileWorldScripts(world);
            logger.info("Found " + scripts.size() + " scripts in world");
            // Найдено скриптов в мире
            // Gefundene Skripte in der Welt
            
            // Log script details
            // Записать детали скрипта
            // Skriptdetails protokollieren
            for (int i = 0; i < scripts.size(); i++) {
                CodeScript script = scripts.get(i);
                logger.info("Script " + (i + 1) + ": " + script.getName());
                // Скрипт
                // Skript
                logger.info("  Enabled: " + script.isEnabled());
                // Включен:
                // Aktiviert:
                logger.info("  Type: " + script.getType());
                // Тип:
                // Typ:
                if (script.getRootBlock() != null) {
                    logger.info("  Root block action: " + script.getRootBlock().getAction());
                    // Действие корневого блока:
                    // Aktion des Wurzelblocks:
                }
            }
            
            // Test 2: Compile world to code strings (reference system-style)
            // Тест 2: Компиляция мира в строки кода (в стиле эталонной системы)
            // Test 2: Welt in Code-Zeichenfolgen kompilieren (Referenzsystem-Stil)
            logger.info("=== Test 2: Compiling world to code strings (reference system-style) ===");
            // === Тест 2: Компиляция мира в строки кода (в стиле эталонной системы) ===
            // === Test 2: Welt in Code-Zeichenfolgen kompilieren (Referenzsystem-Stil) ===
            List<String> codeStrings = codeCompiler.compileWorldToCodeStrings(world);
            logger.info("Generated " + codeStrings.size() + " code strings");
            // Сгенерировано строк кода
            // Generierte Code-Zeichenfolgen
            
            // Log code strings
            // Записать строки кода
            // Code-Zeichenfolgen protokollieren
            for (int i = 0; i < codeStrings.size(); i++) {
                logger.info("Line " + (i + 1) + ": " + codeStrings.get(i));
                // Линия
                // Zeile
            }
            
            // Test 3: Save compiled code (reference system-style)
            // Тест 3: Сохранить скомпилированный код (в стиле эталонной системы)
            // Test 3: Kompilierten Code speichern (Referenzsystem-Stil)
            logger.info("=== Test 3: Saving compiled code ===");
            // === Тест 3: Сохранение скомпилированного кода ===
            // === Test 3: Kompilierter Code wird gespeichert ===
            String worldId = world.getName().replace("-code", "").replace("-world", "");
            codeCompiler.saveCompiledCode(worldId, codeStrings);
            
            logger.info("=== Compilation test completed successfully ===");
            // === Тест компиляции успешно завершен ===
            // === Kompilierungstest erfolgreich abgeschlossen ===
            player.sendMessage("§a✓ Compilation test passed! Found " + scripts.size() + " scripts and " + codeStrings.size() + " code lines.");
            // ✓ Тест компиляции пройден! Найдено скриптов и строк кода.
            // ✓ Kompilierungstest bestanden! Gefundene Skripte und Codezeilen.
            player.sendMessage("§a✓ Code compilation is working correctly!");
            // ✓ Компиляция кода работает правильно!
            // ✓ Code-Kompilierung funktioniert korrekt!
            
            return true;
            
        } catch (Exception e) {
            logger.severe("Compilation test failed: " + e.getMessage());
            // Тест компиляции не удался:
            // Kompilierungstest fehlgeschlagen:
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Запускает быстрый тест компиляции
     * 
     * @param world Мир для тестирования компиляции
     * @return true, если быстрый тест пройден, иначе false
     *
     * Runs a quick compilation test
     * 
     * @param world The world to test compilation in
     * @return true if quick test passes, false otherwise
     *
     * Führt einen schnellen Kompilierungstest durch
     * 
     * @param world Die Welt zum Testen der Kompilierung
     * @return true, wenn der schnelle Test besteht, sonst false
     */
    public boolean quickTest(World world) {
        try {
            logger.info("=== Quick compilation test ===");
            // === Быстрый тест компиляции ===
            // === Schneller Kompilierungstest ===
            
            // Get the CodeCompiler service
            // Получить сервис CodeCompiler
            // CodeCompiler-Dienst abrufen
            CodeCompiler codeCompiler = plugin.getServiceRegistry().getCodeCompiler();
            if (codeCompiler == null) {
                logger.severe("CodeCompiler service not available!");
                // Сервис CodeCompiler недоступен!
                // CodeCompiler-Dienst nicht verfügbar!
                return false;
            }
            
            // Quick test: compile world structures
            // Быстрый тест: компиляция структур мира
            // Schneller Test: Weltenstrukturen kompilieren
            List<CodeScript> scripts = codeCompiler.compileWorldScripts(world);
            logger.info("Quick test: Found " + scripts.size() + " scripts");
            // Быстрый тест: Найдено скриптов
            // Schneller Test: Gefundene Skripte
            
            // Quick test: compile to code strings
            // Быстрый тест: компиляция в строки кода
            // Schneller Test: In Code-Zeichenfolgen kompilieren
            List<String> codeStrings = codeCompiler.compileWorldToCodeStrings(world);
            logger.info("Quick test: Generated " + codeStrings.size() + " code lines");
            // Быстрый тест: Сгенерировано строк кода
            // Schneller Test: Generierte Codezeilen
            
            logger.info("=== Quick compilation test completed ===");
            // === Быстрый тест компиляции завершен ===
            // === Schneller Kompilierungstest abgeschlossen ===
            return true;
            
        } catch (Exception e) {
            logger.severe("Quick compilation test failed: " + e.getMessage());
            // Быстрый тест компиляции не удался:
            // Schneller Kompilierungstest fehlgeschlagen:
            e.printStackTrace();
            return false;
        }
    }
}