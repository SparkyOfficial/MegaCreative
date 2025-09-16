# 🎆 Как добавлять новые блоки, события, условия и действия в MegaCreative

## 📋 Содержание
1. [Архитектура системы](#архитектура-системы)
2. [Добавление новых блоков](#добавление-новых-блоков)
3. [Добавление новых событий](#добавление-новых-событий)
4. [Добавление новых условий](#добавление-новых-условий)
5. [Добавление новых действий](#добавление-новых-действй)
6. [Создание GUI для блоков](#создание-gui-для-блоков)
7. [Тестирование](#тестирование)

## 🏗️ Архитектура системы

### Основные компоненты:
```
src/
├── main/
│   ├── java/com/megacreative/
│   │   ├── coding/                     # Основная логика кодирования
│   │   │   ├── BlockPlacementHandler.java  # Обработчик размещения блоков
│   │   │   ├── CodeBlock.java          # Модель блока кода
│   │   │   └── ...                     # Другие компоненты
│   │   ├── services/
│   │   │   └── BlockConfigService.java # Сервис конфигурации блоков
│   │   ├── gui/
│   │   │   └── coding/                 # GUI для кодирования
│   │   │       ├── variable/           # GUI для переменных (Железный блок)
│   │   │       ├── variable_condition/ # GUI для условий переменных (Обсидиан)
│   │   │       ├── game_action/        # GUI для игровых действий (Незерит)
│   │   │       ├── game_condition/     # GUI для игровых условий (Редстоун)
│   │   │       ├── player_event/       # GUI для событий игроков (Алмаз)
│   │   │       ├── game_event/         # GUI для игровых событий (Изумруд)
│   │   │       ├── entity_event/       # GUI для событий сущностей (Кирпичи)
│   │   │       ├── entity_condition/   # GUI для условий сущностей
│   │   │       ├── player_condition/   # GUI для условий игроков
│   │   │       ├── entity_action/      # GUI для действий над сущностями
│   │   │       ├── EventSelectionGUI.java    # GUI для событий
│   │   │       ├── ActionSelectionGUI.java   # GUI для действий
│   │   │       └── ConditionSelectionGUI.java # GUI для условий
│   │   └── ...                         # Другие пакеты
│   └── resources/
│       └── coding_blocks.yml           # Конфигурация блоков
└── docs/
    └── ADDING_NEW_BLOCKS.md            # Этот документ
```

### Принцип работы:
1. **Конфигурация** - Все блоки настраиваются в `coding_blocks.yml`
2. **Распознавание** - `BlockConfigService` загружает конфигурацию
3. **Размещение** - `BlockPlacementHandler` обрабатывает размещение блоков
4. **Интерфейс** - Соответствующий GUI открывается при взаимодействии
5. **Выполнение** - Действия/условия выполняются через фабрики

## 🧱 Добавление новых блоков

### 1. Добавление в конфигурацию (`coding_blocks.yml`)

```yaml
blocks:
  # Пример нового блока
  EMERALD_BLOCK:
    name: "Новый блок"
    type: "ACTION"        # ACTION, CONDITION, EVENT, CONTROL, FUNCTION
    description: "Описание нового блока"
    default_action: "sendMessage"  # Действие по умолчанию (опционально)
    is_constructor: true   # Является ли блок конструктором (опционально)
    structure:            # Конфигурация структуры (для конструкторов)
      brackets: PISTON    # Тип скобок
      sign: true          # Наличие таблички
      bracket_distance: 3 # Расстояние между скобками
    actions:              # Список доступных действий/условий
      - "sendMessage"
      - "teleport"
      - "giveItem"
```

### 2. Типы блоков:
- **ACTION** - Блоки действий (COBBLESTONE, IRON_BLOCK, NETHERITE_BLOCK)
- **CONDITION** - Блоки условий (OAK_PLANKS, OBSIDIAN, REDSTONE_BLOCK)
- **EVENT** - Блоки событий (DIAMOND_BLOCK)
- **CONTROL** - Блоки управления (PISTON - скобки)
- **FUNCTION** - Блоки функций (LAPIS_BLOCK, BOOKSHELF)

### 3. Специальные блоки:
- **IRON_BLOCK** - Переменные (специализированный GUI)
- **OBSIDIAN** - Условия переменных (специализированный GUI)
- **NETHERITE_BLOCK** - Игровые действия (специализированный GUI)
- **REDSTONE_BLOCK** - Игровые условия (специализированный GUI)
- **DIAMOND_BLOCK** - События игроков (специализированный GUI)
- **EMERALD_BLOCK** - Игровые события (специализированный GUI)
- **BRICKS** - События сущностей (специализированный GUI)
- **COBBLESTONE** - Действия над сущностями (специализированный GUI)

## ⚡ Добавление новых событий

### 1. Добавление в конфигурацию:

```yaml
blocks:
  DIAMOND_BLOCK:  # Или создать новый блок типа EVENT
    name: "Событие"
    type: "EVENT"
    description: "Блоки событий, которые запускают скрипт"
    default_action: "onJoin"
    is_constructor: true
    structure:
      brackets: PISTON
      sign: true
      bracket_distance: 3
    actions:
      - "onJoin"          # Новое событие
      - "onLeave"
      - "onChat"
      # ... другие события
```

### 2. Реализация события:
События реализуются в пакете `com.megacreative.coding.events` и должны реализовывать интерфейс `CustomEvent`.

Пример нового события:
```java
package com.megacreative.coding.events;

public class OnPlayerLevelUpEvent implements CustomEvent {
    @Override
    public String getName() {
        return "onPlayerLevelUp";
    }
    
    @Override
    public String getDescription() {
        return "Срабатывает когда игрок повышает уровень";
    }
    
    @Override
    public String getCategory() {
        return "player";
    }
    
    // Реализация логики события
}
```

## 🎯 Добавление новых условий

### 1. Добавление в конфигурацию:

```yaml
blocks:
  OAK_PLANKS:  # Или создать новый блок типа CONDITION
    name: "Условие"
    type: "CONDITION"
    description: "Блоки условий для проверки различных состояний"
    default_action: "hasItem"
    is_constructor: true
    structure:
      brackets: PISTON
      sign: true
      bracket_distance: 3
    actions:
      - "hasItem"
      - "isOp"
      - "ifPlayerLevel"   # Новое условие
      # ... другие условия
```

### 2. Реализация условия:
Условия реализуются в пакете `com.megacreative.coding.conditions` и должны реализовывать интерфейс `BlockCondition`.

Пример нового условия:
```java
package com.megacreative.coding.conditions;

public class IfPlayerLevelCondition implements BlockCondition {
    @Override
    public String getId() {
        return "ifPlayerLevel";
    }
    
    @Override
    public boolean evaluate(Player player, Map<String, Object> parameters) {
        int requiredLevel = (int) parameters.get("level");
        return player.getLevel() >= requiredLevel;
    }
    
    @Override
    public String getDescription() {
        return "Проверяет уровень игрока";
    }
}
```

## 🔧 Добавление новых действий

### 1. Добавление в конфигурацию:

```yaml
blocks:
  COBBLESTONE:  # Или создать новый блок типа ACTION
    name: "Действие"
    type: "ACTION"
    description: "Блоки действий, которые выполняются в скрипте"
    default_action: "sendMessage"
    is_constructor: true
    structure:
      brackets: PISTON
      sign: true
      bracket_distance: 3
    actions:
      - "sendMessage"
      - "teleport"
      - "setPlayerLevel"  # Новое действие
      # ... другие действия
```

### 2. Реализация действия:
Действия реализуются в пакете `com.megacreative.coding.actions` и должны реализовывать интерфейс `BlockAction`.

Пример нового действия:
```java
package com.megacreative.coding.actions;

public class SetPlayerLevelAction implements BlockAction {
    @Override
    public String getId() {
        return "setPlayerLevel";
    }
    
    @Override
    public void execute(Player player, Map<String, Object> parameters) {
        int level = (int) parameters.get("level");
        player.setLevel(level);
        player.sendMessage("§aВаш уровень установлен на " + level);
    }
    
    @Override
    public String getDescription() {
        return "Устанавливает уровень игрока";
    }
}
```

### 3. Конфигурация параметров действия:

```yaml
action_configurations:
  setPlayerLevel:
    slots:
      0:
        name: "§aУровень игрока"
        description: "§7Положите бумагу с уровнем игрока"
        placeholder_item: "PAPER"
        slot_name: "level"
```

## 🖼️ Создание GUI для блоков

### 1. Структура GUI пакетов:
```
gui/coding/
├── variable/           # Для IRON_BLOCK (переменные)
├── variable_condition/ # Для OBSIDIAN (условия переменных)
├── game_action/        # Для NETHERITE_BLOCK (игровые действия)
├── game_condition/     # Для REDSTONE_BLOCK (игровые условия)
├── player_event/       # Для DIAMOND_BLOCK (события игроков)
├── game_event/         # Для EMERALD_BLOCK (игровые события)
├── entity_event/       # Для BRICKS (события сущностей)
├── entity_condition/   # Для условий сущностей
├── player_condition/   # Для условий игроков
├── entity_action/      # Для COBBLESTONE (действия над сущностями)
├── EventSelectionGUI.java    # Для EVENT блоков
├── ActionSelectionGUI.java   # Для ACTION блоков
└── ConditionSelectionGUI.java # Для CONDITION блоков
```

### 2. Создание нового GUI класса:

Пример для специализированного GUI:
```java
package com.megacreative.gui.coding.variable;

public class VariableBlockGUI implements GUIManager.ManagedGUIInterface {
    // Реализация GUI для переменных
    // См. существующие реализации для примера
}
```

### 3. Регистрация GUI в BlockPlacementHandler:

```java
// В методе handleBlockInteraction
if (codeBlock.getMaterial() == Material.IRON_BLOCK) {
    // Открытие специализированного GUI
    com.megacreative.gui.coding.variable.VariableBlockGUI gui = 
        new com.megacreative.gui.coding.variable.VariableBlockGUI(plugin, player, blockLocation, codeBlock.getMaterial());
    gui.open();
}
```

### 4. Создание папки для нового типа блока:

1. Создать новую папку в `gui/coding/` для нового типа блока
2. Создать GUI класс в этой папке
3. Обновить `BlockPlacementHandler` для использования нового GUI
4. Добавить конфигурацию в `coding_blocks.yml`

## 🧪 Тестирование

### 1. Компиляция:
```bash
mvn clean compile
```

### 2. Сборка:
```bash
mvn clean package
```

### 3. Тестирование GUI:
- Разместить блок в мире разработки
- Кликнуть по блоку правой кнопкой мыши
- Проверить открытие правильного GUI
- Выбрать действие/условие/событие
- Проверить сохранение выбора

### 4. Тестирование функциональности:
- Создать простой скрипт с новым блоком
- Запустить скрипт
- Проверить корректность выполнения

### 5. Проверка обратной совместимости:
- Убедиться, что существующие блоки работают корректно
- Проверить, что новые блоки не ломают старую функциональность

## 🎯 Лучшие практики

### 1. Именование:
- Используйте понятные, описательные имена
- Следуйте существующим соглашениям об именовании
- Используйте camelCase для ID действий/условий/событий

### 2. Документация:
- Добавляйте описание для каждого нового элемента
- Комментируйте сложную логику
- Обновляйте этот документ при добавлении новых возможностей

### 3. Производительность:
- Избегайте тяжелых операций в методах execute/evaluate
- Используйте кэширование при необходимости
- Освобождайте ресурсы в методах onCleanup

### 4. Безопасность:
- Проверяйте права доступа игроков
- Валидируйте входные параметры
- Обрабатывайте исключения корректно

## 🆘 Устранение неполадок

### Частые проблемы:

1. **Блок не распознается как кодовый**:
   - Проверьте, что материал добавлен в `coding_blocks.yml`
   - Убедитесь, что конфигурация загружается корректно
   - Проверьте логи на наличие ошибок

2. **GUI не открывается**:
   - Проверьте, что GUI класс корректно реализует `ManagedGUIInterface`
   - Убедитесь, что путь к классу указан правильно в `BlockPlacementHandler`
   - Проверьте, что нет ошибок компиляции

3. **Действие/условие не выполняется**:
   - Проверьте, что класс реализует правильный интерфейс
   - Убедитесь, что ID совпадает с конфигурацией
   - Проверьте логи на наличие ошибок выполнения

### Полезные команды:

```bash
# Очистка и пересборка проекта
mvn clean compile

# Сборка с созданием JAR файла
mvn clean package

# Запуск тестов (если включены)
mvn test
```

## 📞 Поддержка

Если у вас возникли вопросы или проблемы при добавлении новых элементов:
1. Проверьте логи сервера на наличие ошибок
2. Убедитесь, что все зависимости корректно подключены
3. Обратитесь к существующим примерам в коде
4. Создайте issue в системе отслеживания ошибок

---

🎉 **Поздравляем!** Теперь вы знаете, как добавлять новые блоки, события, условия и действия в MegaCreative!

Помните: главное в разработке - это следовать архитектуре, тестировать изменения и документировать новую функциональность. Удачи в создании удивительных возможностей для игроков!