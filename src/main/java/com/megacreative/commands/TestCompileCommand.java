package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.testing.CompilationTest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для тестирования процесса компиляции
 * Позволяет проверить корректность работы компилятора кода
 * Диагностика и тестирование компиляционных процессов
 *
 * Command to test the compilation process
 * Allows checking the correctness of the code compiler
 * Diagnostics and testing of compilation processes
 *
 * Befehl zum Testen des Kompilierungsprozesses
 * Ermöglicht die Überprüfung der Korrektheit des Code-Compilers
 * Diagnose und Testen von Kompilierungsprozessen
 */
public class TestCompileCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    /**
     * Инициализирует команду тестирования компиляции
     * @param plugin основной экземпляр плагина
     *
     * Initializes the compilation test command
     * @param plugin main plugin instance
     *
     * Initialisiert den Kompilierungstest-Befehl
     * @param plugin Haupt-Plugin-Instanz
     */
    public TestCompileCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает выполнение команды тестирования компиляции
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles compilation test command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Kompilierungstest-Befehls
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        // Run compilation test
        player.sendMessage("§eRunning compilation test...");
        plugin.getLogger().info("Player " + player.getName() + " initiated compilation test");
        
        try {
            CompilationTest test = new CompilationTest(plugin);
            boolean result = test.testCompilationProcess(player.getName());
            
            if (result) {
                player.sendMessage("§a✓ Compilation test completed successfully!");
                player.sendMessage("§aThe CodeCompiler is working correctly!");
            } else {
                player.sendMessage("§c✗ Compilation test failed!");
                player.sendMessage("§cCheck the console for error details.");
            }
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Compilation test failed with exception: " + e.getMessage());
            plugin.getLogger().severe("Compilation test failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
}