# 🚀 ПЛАН МИГРАЦИИ: ОТ СТАРОЙ К НОВОЙ АРХИТЕКТУРЕ

## 📋 **ТЕКУЩАЯ СИТУАЦИЯ**

### **Проблема:**
- **3 исполнителя** одновременно создают путаницу:
  - `ScriptExecutor.java` (старый, рабочий)
  - `HybridScriptExecutor.java` (переходный мост) 
  - `EnhancedScriptExecutor.java` (новый, чистый)

### **Решение:**
Создать **единую централизованную систему** с четким планом миграции.

## 🎯 **СТРАТЕГИЯ МИГРАЦИИ**

### **Принцип работы:**
```
1. НОВАЯ СИСТЕМА (BlockFactory) - ПРИОРИТЕТ
   ↓ (если не найден)
2. СТАРАЯ СИСТЕМА (oldActionRegistry) - СОВМЕСТИМОСТЬ
   ↓ (если не найден)
3. ОШИБКА - Блок не найден
```

### **Процесс миграции одного блока:**
1. ✅ Создать новые аргументы (если нужны)
2. ✅ Создать новую версию блока в `blocks/actions/`
3. ✅ Зарегистрировать в `BlockFactory`
4. 🔄 **Удалить из `oldActionRegistry`**
5. ✅ Протестировать в игре

## 📊 **СТАТУС МИГРАЦИИ**

### **✅ УЖЕ МИГРИРОВАНЫ (8 блоков):**
- `sendMessage` → `SendMessageAction`
- `giveItem` → `GiveItemAction`
- `randomNumber` → `RandomNumberAction`
- `setVar` → `SetVarAction`
- `teleport` → `TeleportAction`
- `wait` → `WaitAction`
- `setBlock` → `SetBlockAction`
- `broadcast` → `BroadcastAction`

### **✅ УЖЕ МИГРИРОВАНЫ (4 условия):**
- `isOp` → `IsOpCondition`
- `hasItem` → `HasItemCondition`
- `ifVarEquals` → `IfVarEqualsCondition`
- `playerHealth` → `PlayerHealthCondition`

### **🔄 СЛЕДУЮЩИЕ КАНДИДАТЫ НА МИГРАЦИЮ:**

#### **Приоритет 1 (простые блоки):**
- `spawnMob` → `SpawnMobAction`
- `playSound` → `PlaySoundAction`
- `effect` → `EffectAction`
- `command` → `CommandAction`

#### **Приоритет 2 (математические):**
- `addVar` → `AddVarAction`
- `subVar` → `SubVarAction`
- `mulVar` → `MulVarAction`
- `divVar` → `DivVarAction`

#### **Приоритет 3 (сложные блоки):**
- `healPlayer` → `HealPlayerAction`
- `setGameMode` → `SetGameModeAction`
- `setTime` → `SetTimeAction`
- `setWeather` → `SetWeatherAction`

## 🛠 **ПОШАГОВЫЙ ПЛАН ДЕЙСТВИЙ**

### **Этап 1: Стабилизация (ТЕКУЩИЙ)**
- ✅ Гибридная система работает
- ✅ 12 блоков уже мигрированы
- 🔄 **НУЖНО: Протестировать в игре**
- 🔄 **НУЖНО: Удалить мигрированные блоки из oldActionRegistry**

### **Этап 2: Массовая миграция (СЛЕДУЮЩИЙ)**
- 🔄 Мигрировать блоки Приоритета 1 (4 блока)
- 🔄 Мигрировать блоки Приоритета 2 (4 блока)
- 🔄 Мигрировать блоки Приоритета 3 (4 блока)
- 🔄 Удалять блоки из старого реестра после миграции

### **Этап 3: Завершение миграции**
- 🔄 Мигрировать оставшиеся блоки
- 🔄 Удалить `oldActionRegistry` и `oldConditionRegistry`
- 🔄 Переименовать `HybridScriptExecutor` в `ScriptExecutor`
- 🔄 Удалить старые файлы

## 📝 **ПРИМЕР МИГРАЦИИ БЛОКА**

### **Шаг 1: Анализ старого блока**
```java
// Старый SendMessageAction
public void execute(ExecutionContext context) {
    Player player = context.getPlayer();
    String message = context.getCurrentBlock().getParameter("message");
    player.sendMessage(message);
}
```

### **Шаг 2: Создание аргументов**
```java
// ParameterArgument.java (уже создан)
public class ParameterArgument implements Argument<TextValue> {
    private final String parameterName;
    
    @Override
    public Optional<TextValue> parse(CodeBlock block) {
        Object parameter = block.getParameter(parameterName);
        if (parameter != null) {
            return Optional.of(new TextValue(parameter.toString()));
        }
        return Optional.empty();
    }
}
```

### **Шаг 3: Создание нового блока**
```java
// blocks/actions/SendMessageAction.java
public class SendMessageAction implements BlockAction {
    private final Argument<TextValue> messageArgument = new ParameterArgument("message");
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        TextValue messageValue = messageArgument.parse(context.getCurrentBlock())
            .orElse(null);
            
        if (messageValue == null) {
            player.sendMessage("§cОшибка: не найден параметр 'message'");
            return;
        }
        
        String finalMessage = messageValue.get(context);
        player.sendMessage(finalMessage);
    }
}
```

### **Шаг 4: Регистрация в BlockFactory**
```java
// BlockFactory.java
private static void registerActions() {
    actionRegistry.put("sendMessage", new SendMessageAction());
    // ... другие блоки
}
```

### **Шаг 5: Удаление из старого реестра**
```java
// HybridScriptExecutor.java
private void registerOldActions() {
    // oldActionRegistry.put("sendMessage", new SendMessageAction()); // УДАЛЕНО
    // ... остальные блоки
}
```

## 🎮 **ТЕСТИРОВАНИЕ МИГРАЦИИ**

### **Команды для проверки:**
```bash
/testhybrid info    # Показать статистику миграции
/testhybrid test    # Тест гибридной системы
/testhybrid list    # Список доступных блоков
```

### **Что проверять:**
1. **Старые скрипты** - должны работать без изменений
2. **Новые блоки** - должны показывать `[НОВАЯ СИСТЕМА]`
3. **Старые блоки** - должны показывать `[СТАРАЯ СИСТЕМА]`
4. **Обработка ошибок** - должна работать корректно

## 🚀 **ПРЕИМУЩЕСТВА ПОСЛЕ МИГРАЦИИ**

### **✅ Архитектурные улучшения:**
- Единая система аргументов и значений
- Лучшая обработка ошибок
- Поддержка плейсхолдеров и выражений
- Модульная структура

### **✅ Производительность:**
- Оптимизированное выполнение
- Меньше дублирования кода
- Лучшая отладка

### **✅ Расширяемость:**
- Легкое добавление новых блоков
- Сложные типы значений
- Система событий
- Блоки итераций

## 📅 **ГРАФИК МИГРАЦИИ**

### **Неделя 1: Стабилизация**
- ✅ Тестирование в игре
- 🔄 Удаление мигрированных блоков из старого реестра
- 🔄 Исправление ошибок

### **Неделя 2: Массовая миграция**
- 🔄 Миграция блоков Приоритета 1 (4 блока)
- 🔄 Миграция блоков Приоритета 2 (4 блока)
- 🔄 Тестирование каждого блока

### **Неделя 3: Завершение**
- 🔄 Миграция блоков Приоритета 3 (4 блока)
- 🔄 Удаление старого реестра
- 🔄 Переименование в `ScriptExecutor`

### **Неделя 4: Оптимизация**
- 🔄 Удаление старых файлов
- 🔄 Оптимизация производительности
- 🔄 Документация

## 🎯 **ЦЕЛЬ**

**Полностью перейти на новую архитектуру, сохранив 100% обратной совместимости и добавив новые возможности!**

---

**Готовы начать миграцию? Следующий шаг - тестирование в игре и удаление мигрированных блоков из старого реестра!** 🚀 