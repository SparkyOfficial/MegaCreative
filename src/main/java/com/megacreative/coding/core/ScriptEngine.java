package com.megacreative.coding.core;

import com.megacreative.coding.blocks.Block;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Движок для выполнения скриптов из блоков.
 * Управляет выполнением, приостановкой и остановкой скриптов.
 */
public class ScriptEngine {
    private final JavaPlugin plugin;
    private final Map<String, ScriptInstance> runningScripts = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> scheduledTasks = new ConcurrentHashMap<>();
    
    public ScriptEngine(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Запускает выполнение скрипта.
     * 
     * @param scriptId Уникальный идентификатор скрипта
     * @param rootBlock Корневой блок скрипта
     * @param context Контекст выполнения
     * @return true, если скрипт успешно запущен
     */
    public boolean executeScript(String scriptId, Block rootBlock, BlockContext context) {
        if (runningScripts.containsKey(scriptId)) {
            return false; // Скрипт уже выполняется
        }
        
        ScriptInstance instance = new ScriptInstance(scriptId, rootBlock, context);
        runningScripts.put(scriptId, instance);
        
        // Запускаем выполнение в асинхронном режиме
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, instance);
        scheduledTasks.put(instance.getInstanceId(), task);
        
        return true;
    }
    
    /**
     * Останавливает выполнение скрипта.
     * 
     * @param scriptId Идентификатор скрипта
     * @return true, если скрипт был остановлен
     */
    public boolean stopScript(String scriptId) {
        ScriptInstance instance = runningScripts.remove(scriptId);
        if (instance != null) {
            instance.stop();
            BukkitTask task = scheduledTasks.remove(instance.getInstanceId());
            if (task != null) {
                task.cancel();
            }
            return true;
        }
        return false;
    }
    
    /**
     * Приостанавливает выполнение скрипта.
     */
    public boolean pauseScript(String scriptId) {
        ScriptInstance instance = runningScripts.get(scriptId);
        if (instance != null) {
            instance.pause();
            return true;
        }
        return false;
    }
    
    /**
     * Возобновляет выполнение приостановленного скрипта.
     */
    public boolean resumeScript(String scriptId) {
        ScriptInstance instance = runningScripts.get(scriptId);
        if (instance != null && instance.isPaused()) {
            instance.resume();
            return true;
        }
        return false;
    }
    
    /**
     * Проверяет, выполняется ли скрипт.
     */
    public boolean isScriptRunning(String scriptId) {
        ScriptInstance instance = runningScripts.get(scriptId);
        return instance != null && instance.isRunning();
    }
    
    /**
     * Останавливает все выполняющиеся скрипты.
     */
    public void stopAllScripts() {
        for (String scriptId : new ArrayList<>(runningScripts.keySet())) {
            stopScript(scriptId);
        }
    }
    
    /**
     * Внутренний класс, представляющий экземпляр выполняющегося скрипта.
     */
    private static class ScriptInstance implements Runnable {
        private final String scriptId;
        private final UUID instanceId;
        private final Block rootBlock;
        private final BlockContext context;
        private volatile boolean running = false;
        private volatile boolean paused = false;
        private final Object pauseLock = new Object();
        
        public ScriptInstance(String scriptId, Block rootBlock, BlockContext context) {
            this.scriptId = scriptId;
            this.instanceId = UUID.randomUUID();
            this.rootBlock = rootBlock;
            this.context = context;
        }
        
        @Override
        public void run() {
            running = true;
            try {
                executeBlock(rootBlock);
            } catch (Exception e) {
                // Логируем ошибку, но не прерываем выполнение
                context.getPlugin().getLogger().severe("Ошибка при выполнении скрипта " + scriptId + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                running = false;
            }
        }
        
        private void executeBlock(Block block) {
            if (!running || block == null) {
                return;
            }
            
            checkPaused();
            
            try {
                // Выполняем текущий блок
                boolean success = block.execute(context);
                
                if (success) {
                    // Рекурсивно выполняем дочерние блоки
                    for (Block child : block.getChildren()) {
                        executeBlock(child);
                        checkPaused();
                    }
                }
                
                // Переходим к следующему блоку в цепочке
                if (block.getNext() != null) {
                    executeBlock(block.getNext());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
        
        private void checkPaused() throws InterruptedException {
            while (paused) {
                synchronized (pauseLock) {
                    if (paused) {
                        pauseLock.wait();
                    }
                }
            }
        }
        
        public void stop() {
            running = false;
            resume(); // Разблокируем поток, если он был приостановлен
        }
        
        public void pause() {
            paused = true;
        }
        
        public void resume() {
            if (paused) {
                synchronized (pauseLock) {
                    paused = false;
                    pauseLock.notifyAll();
                }
            }
        }
        
        public boolean isRunning() {
            return running && !paused;
        }
        
        public boolean isPaused() {
            return paused;
        }
        
        public UUID getInstanceId() {
            return instanceId;
        }
    }
}
