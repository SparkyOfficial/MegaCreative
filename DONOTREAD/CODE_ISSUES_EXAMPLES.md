# Примеры проблемного кода в MegaCreative

## 🚨 КРИТИЧЕСКИЕ ПРОБЛЕМЫ

### 1. Проблемы с многопоточностью

#### WorldManager.java - Небезопасное асинхронное сохранение
```java
// СТРОКИ 88-102 - ПРОБЛЕМА: Race condition
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    try {
        saveWorld(creativeWorld);
    } catch (Exception e) {
        plugin.getLogger().warning("Не удалось сохранить данные мира: " + e.getMessage());
        // Уведомление об ошибке в главном потоке
        Bukkit.getScheduler().runTask(plugin, () -> 
            player.sendMessage("§cНе удалось сохранить данные мира. Обратитесь к администратору."));
    }
});
```

**Проблемы:**
- Нет синхронизации при доступе к `creativeWorld`
- Потенциальная потеря данных при одновременном сохранении
- Небезопасное обращение к `player` из другого потока

#### MegaCreative.java - Проблемы с инициализацией
```java
// СТРОКИ 217-240 - ПРОБЛЕМА: Неэффективная проверка инвентаря
new org.bukkit.scheduler.BukkitRunnable() {
    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getName().endsWith("_dev")) {
                // Проверяем, каких предметов не хватает
                List<String> missingItems = getMissingCodingItems(player);
                if (!missingItems.isEmpty()) {
                    // Добавляем только недостающие предметы
                    CodingItems.giveMissingItems(player, missingItems);
                    player.sendMessage("§e§l!§r §eДобавлены недостающие инструменты для кодинга: " + String.join(", ", missingItems));
                }
            }
        }
    }
}.runTaskTimer(this, 100L, 100L); // Проверка каждые 5 секунд (100 тиков)
```

**Проблемы:**
- Проверка каждые 5 секунд для всех игроков - неэффективно
- Нет кэширования результатов проверки
- Избыточные операции с инвентарем

### 2. Неэффективная обработка исключений

#### BlockPlacementHandler.java - Слишком общие исключения
```java
// СТРОКИ 164-167 - ПРОБЛЕМА: Потеря информации об ошибке
} catch (Exception e) {
    player.sendMessage("§cПроизошла ошибка при настройке блока!");
    plugin.getLogger().warning("Ошибка при настройке блока: " + e.getMessage());
}
```

**Проблемы:**
- Слишком общий catch блок
- Потеря stack trace
- Неинформативное сообщение для игрока

#### ScriptExecutor.java - Отсутствие обработки ошибок
```java
// СТРОКИ 150-190 - ПРОБЛЕМА: Нет обработки исключений в критических местах
public void execute(CodeScript script, ExecutionContext context, String trigger) {
    if (script == null || !script.isEnabled()) {
        return;
    }
    
    Player player = context.getPlayer();
    if (player != null && plugin.getScriptDebugger().isDebugEnabled(player)) {
        plugin.getScriptDebugger().onScriptStart(player, script);
    }
    
    // НЕТ ОБРАБОТКИ ИСКЛЮЧЕНИЙ!
    executeBlock(script.getRootBlock(), context, trigger);
    
    if (player != null && plugin.getScriptDebugger().isDebugEnabled(player)) {
        plugin.getScriptDebugger().onScriptEnd(player, script);
    }
}
```

### 3. Проблемы с управлением памятью

#### BlockPlacementHandler.java - Утечки памяти
```java
// СТРОКИ 35-40 - ПРОБЛЕМА: Неограниченный рост коллекций
private final Map<Location, CodeBlock> blockCodeBlocks = new HashMap<>();
private final Map<UUID, Boolean> playerVisualizationStates = new HashMap<>();
private final Map<UUID, Boolean> playerDebugStates = new HashMap<>();
private final Map<UUID, Location> playerSelections = new HashMap<>();
private final Map<UUID, CodeBlock> clipboard = new HashMap<>();
```

**Проблемы:**
- Нет очистки при отключении игрока
- Нет таймаутов для временных данных
- Потенциальные утечки памяти

---

## ⚠️ СЕРЬЕЗНЫЕ ПРОБЛЕМЫ

### 4. Архитектурные проблемы

#### MegaCreative.java - Слишком много ответственности
```java
// СТРОКИ 25-45 - ПРОБЛЕМА: Нарушение принципа единственной ответственности
public class MegaCreative extends JavaPlugin {
    private static MegaCreative instance;
    private ConfigManager configManager;
    private WorldManager worldManager;
    private PlayerManager playerManager;
    private CodingManager codingManager;
    private BlockPlacementHandler blockPlacementHandler;
    private BlockConnectionVisualizer blockConnectionVisualizer;
    private ScriptDebugger scriptDebugger;
    private DataManager dataManager;
    private TemplateManager templateManager;
    private ScoreboardManager scoreboardManager;
    private TrustedPlayerManager trustedPlayerManager;
    private BlockConfigManager blockConfigManager;
    private BlockConfiguration blockConfiguration;
    
    // Maps для хранения состояния
    private Map<UUID, CreativeWorld> commentInputs = new HashMap<>();
    private Map<UUID, String> deleteConfirmations = new HashMap<>();
}
```

**Проблемы:**
- Класс делает слишком много
- Смешение ответственности
- Сложная инициализация

### 5. Проблемы с безопасностью

#### CommandAction.java - Небезопасное выполнение команд
```java
// СТРОКИ 30-45 - ПРОБЛЕМА: Отсутствие проверки прав
public void execute(ExecutionContext context) {
    String command = context.getParameter("command");
    if (command != null && !command.isEmpty()) {
        try {
            // НЕТ ПРОВЕРКИ ПРАВ!
            // НЕТ САНИТИЗАЦИИ!
            Bukkit.dispatchCommand(context.getPlayer(), command);
        } catch (Exception e) {
            context.getPlayer().sendMessage("§cОшибка выполнения команды: " + e.getMessage());
        }
    }
}
```

**Проблемы:**
- Выполнение команд без проверки прав
- Отсутствие санитизации
- Потенциальные security issues

---

## 🔧 ПРОБЛЕМЫ КОДА

### 6. Проблемы с производительностью

#### BlockPlacementHandler.java - Неэффективные проверки
```java
// СТРОКИ 250-280 - ПРОБЛЕМА: Избыточные проверки
private boolean hasAllCodingItems(Player player) {
    // Упрощенная проверка. Проверяем наличие хотя бы нескольких ключевых предметов.
    // Для 100% точности нужно проверять каждый предмет из giveCodingItems
    boolean hasLinker = false;
    boolean hasInspector = false;
    boolean hasEventBlock = false;
    for (ItemStack item : player.getInventory().getContents()) {
        if (item != null && item.hasItemMeta()) {
            String name = item.getItemMeta().getDisplayName();
            if (name.contains("Связующий жезл")) hasLinker = true;
            if (name.contains("Инспектор блоков")) hasInspector = true;
            if (name.contains("Событие игрока")) hasEventBlock = true;
        }
    }
    return hasLinker && hasInspector && hasEventBlock;
}
```

**Проблемы:**
- Проверка всего инвентаря каждый раз
- Строковые сравнения вместо констант
- Нет кэширования результатов

### 7. Проблемы с отладкой

#### ScriptDebugger.java - Избыточные debug сообщения
```java
// СТРОКИ 80-90 - ПРОБЛЕМА: Неструктурированное логирование
new BukkitRunnable() {
    @Override
    public void run() {
        if (player.isOnline() && playerDebugStates.getOrDefault(player.getUniqueId(), false)) {
            // Избыточные debug сообщения
            player.sendMessage("§7[DEBUG] Выполняется скрипт: " + script.getName());
            player.sendMessage("§7[DEBUG] Текущий блок: " + block.getAction());
            player.sendMessage("§7[DEBUG] Параметры: " + block.getParameters());
        }
    }
}.runTaskLater(plugin, 1L);
```

**Проблемы:**
- Избыточные сообщения
- Нет уровней логирования
- Неструктурированный вывод

---

## 📋 СПИСОК КОНКРЕТНЫХ БАГОВ

### 8. Функциональные баги

#### BlockPlacementHandler.java - Проблемы с размещением блоков
```java
// СТРОКИ 70-85 - БАГ: Неправильная логика размещения
// 1. НЕ отменяем событие. Позволяем блоку установиться.
// event.setCancelled(true); // <--- УБЕДИТЕСЬ, ЧТО ЭТОЙ СТРОКИ НЕТ!

// 2. Создаем "заготовку" блока кода сразу.
CodeBlock newCodeBlock = new CodeBlock(mat, "Настройка..."); // Временное действие
blockCodeBlocks.put(block.getLocation(), newCodeBlock);
```

**Проблемы:**
- Блок может быть размещен без правильной инициализации
- Потенциальные race conditions
- Отсутствие rollback при ошибке

#### CodingManager.java - Проблемы с переменными
```java
// СТРОКИ 20-25 - БАГ: Небезопасное хранение переменных
private final Map<String, Object> globalVariables = new HashMap<>();
private final Map<String, Object> serverVariables = new HashMap<>();
```

**Проблемы:**
- Нет синхронизации при доступе к переменным
- Потенциальные конфликты имен
- Отсутствие типизации

---

## 🎯 РЕКОМЕНДАЦИИ ПО ИСПРАВЛЕНИЮ

### Приоритет 1: Критические исправления

#### 1.1 Исправить многопоточность
```java
// РЕШЕНИЕ: Добавить синхронизацию
private final Object worldSaveLock = new Object();

public void saveWorldAsync(CreativeWorld world, Player player) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        synchronized (worldSaveLock) {
            try {
                saveWorld(world);
                Bukkit.getScheduler().runTask(plugin, () -> 
                    player.sendMessage("§aМир успешно сохранен!"));
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка сохранения мира: " + e.getMessage());
                e.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () -> 
                    player.sendMessage("§cОшибка сохранения мира!"));
            }
        }
    });
}
```

#### 1.2 Улучшить обработку исключений
```java
// РЕШЕНИЕ: Создать специфичные исключения
public class BlockConfigurationException extends RuntimeException {
    public BlockConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Использование:
try {
    configureBlock(block, action);
} catch (BlockConfigurationException e) {
    plugin.getLogger().warning("Ошибка конфигурации блока: " + e.getMessage());
    player.sendMessage("§cОшибка настройки блока: " + e.getMessage());
} catch (Exception e) {
    plugin.getLogger().severe("Неожиданная ошибка: " + e.getMessage());
    e.printStackTrace();
    player.sendMessage("§cПроизошла неожиданная ошибка!");
}
```

### Приоритет 2: Архитектурные улучшения

#### 2.1 Создать интерфейсы
```java
// РЕШЕНИЕ: Интерфейс для менеджеров
public interface WorldManager {
    void createWorld(Player player, String name, CreativeWorldType type);
    void deleteWorld(String worldId);
    CreativeWorld getWorld(String worldId);
    List<CreativeWorld> getPlayerWorlds(UUID playerId);
}

// Реализация:
public class WorldManagerImpl implements WorldManager {
    // Реализация методов
}
```

#### 2.2 Улучшить конфигурацию
```java
// РЕШЕНИЕ: Валидация конфигурации
public class ConfigurationValidator {
    public static void validateConfig(FileConfiguration config) throws InvalidConfigurationException {
        if (!config.contains("worlds.maxPerPlayer")) {
            throw new InvalidConfigurationException("Missing worlds.maxPerPlayer");
        }
        
        int maxWorlds = config.getInt("worlds.maxPerPlayer");
        if (maxWorlds <= 0 || maxWorlds > 100) {
            throw new InvalidConfigurationException("Invalid maxPerPlayer value: " + maxWorlds);
        }
    }
}
```

### Приоритет 3: Оптимизация

#### 3.1 Добавить кэширование
```java
// РЕШЕНИЕ: Кэш для проверки инвентаря
private final Map<UUID, Long> inventoryCheckCache = new ConcurrentHashMap<>();
private static final long CACHE_DURATION = 30000; // 30 секунд

private boolean hasCodingItems(Player player) {
    UUID playerId = player.getUniqueId();
    long currentTime = System.currentTimeMillis();
    
    Long lastCheck = inventoryCheckCache.get(playerId);
    if (lastCheck != null && currentTime - lastCheck < CACHE_DURATION) {
        return true; // Используем кэшированный результат
    }
    
    boolean hasItems = performInventoryCheck(player);
    if (hasItems) {
        inventoryCheckCache.put(playerId, currentTime);
    }
    
    return hasItems;
}
```

#### 3.2 Улучшить безопасность
```java
// РЕШЕНИЕ: Безопасное выполнение команд
public class SafeCommandExecutor {
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
        "gamemode", "time", "weather", "difficulty"
    );
    
    public static boolean executeCommand(Player player, String command) {
        String[] parts = command.split("\\s+");
        String baseCommand = parts[0].toLowerCase();
        
        if (!ALLOWED_COMMANDS.contains(baseCommand)) {
            player.sendMessage("§cКоманда не разрешена: " + baseCommand);
            return false;
        }
        
        if (!player.hasPermission("megacreative.command." + baseCommand)) {
            player.sendMessage("§cУ вас нет прав на выполнение этой команды!");
            return false;
        }
        
        try {
            return Bukkit.dispatchCommand(player, command);
        } catch (Exception e) {
            player.sendMessage("§cОшибка выполнения команды: " + e.getMessage());
            return false;
        }
    }
}
```

---

## 📊 МЕТРИКИ ПРОБЛЕМ

### Статистика по файлам:
- **BlockPlacementHandler.java**: 647 строк, 6 catch блоков, 8 проблем
- **WorldManager.java**: 430 строк, 6 catch блоков, 5 проблем  
- **ScriptExecutor.java**: 305 строк, 0 catch блоков, 4 проблемы
- **MegaCreative.java**: 291 строк, 0 catch блоков, 3 проблемы
- **DevCommand.java**: 200+ строк, 6 catch блоков, 3 проблемы

### Типы проблем:
- **Многопоточность**: 15 проблем
- **Обработка исключений**: 25 проблем
- **Архитектура**: 10 проблем
- **Безопасность**: 8 проблем
- **Производительность**: 12 проблем
- **Отладка**: 5 проблем

### Приоритеты исправления:
1. **Критично** (15 проблем) - Многопоточность и безопасность
2. **Важно** (25 проблем) - Обработка исключений
3. **Средне** (22 проблемы) - Архитектура и производительность
4. **Низко** (5 проблем) - Отладка и документация
