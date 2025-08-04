package com.megacreative.coding.blocks;

import com.megacreative.coding.core.BlockType;
import com.megacreative.coding.blocks.actions.*;
import com.megacreative.coding.blocks.conditions.*;
import com.megacreative.coding.blocks.events.*;
import com.megacreative.coding.blocks.logic.*;
import com.megacreative.coding.blocks.loops.*;
import com.megacreative.coding.blocks.variables.*;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Supplier;

/**
 * Фабрика для создания и управления блоками.
 * Централизованная система регистрации всех доступных блоков.
 */
public class BlockFactory {
    
    private final Map<String, Supplier<Block>> blockRegistry = new HashMap<>();
    private static BlockFactory instance;
    private final JavaPlugin plugin;
    
    public BlockFactory(JavaPlugin plugin) {
        this.plugin = plugin;
        registerDefaultBlocks();
    }
    
    /**
     * Получает экземпляр фабрики (синглтон).
     */
    public static synchronized BlockFactory getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new BlockFactory(plugin);
        }
        return instance;
    }
        
    /**
     * Регистрирует все стандартные блоки.
     */
    private void registerDefaultBlocks() {
        // Регистрируем действия
        registerActionBlocks();
        // Регистрируем условия
        registerConditionBlocks();
        // Регистрируем события
        registerEventBlocks();
        // Регистрируем логические блоки
        registerLogicBlocks();
        // Регистрируем циклы
        registerLoopBlocks();
        // Регистрируем переменные
        registerVariableBlocks();
        
        plugin.getLogger().info("Зарегистрировано " + blockRegistry.size() + " блоков");
    }
    
    /**
     * Регистрирует блоки действий.
     */
    private void registerActionBlocks() {
        // Основные действия
        registerBlock("action_send_message", () -> new SendMessageAction(
            "action_send_message",
            "Отправить сообщение",
            "Отправляет сообщение игроку",
            BlockType.ACTION,
            Material.PAPER
        ));
        
        registerBlock("action_give_item", () -> new GiveItemAction(
            "action_give_item",
            "Выдать предмет",
            "Выдает предмет игроку",
            BlockType.ACTION,
            Material.CHEST
        ));
        
        registerBlock("action_teleport", () -> new TeleportAction(
            "action_teleport",
            "Телепортировать",
            "Телепортирует игрока в указанное место",
            BlockType.ACTION,
            Material.ENDER_PEARL
        ));
        
        registerBlock("action_play_sound", () -> new PlaySoundAction(
            "action_play_sound",
            "Воспроизвести звук",
            "Воспроизводит звук для игрока",
            BlockType.ACTION,
            Material.NOTE_BLOCK
        ));
        
        registerBlock("action_spawn_particle", () -> new SpawnParticleAction(
            "action_spawn_particle",
            "Создать частицы",
            "Создает частицы в указанном месте",
            BlockType.ACTION,
            Material.FIRE_CHARGE
        ));
        
        registerBlock("action_execute_command", () -> new ExecuteCommandAction(
            "action_execute_command",
            "Выполнить команду",
            "Выполняет команду от имени игрока",
            BlockType.ACTION,
            Material.COMMAND_BLOCK
        ));
        
        registerBlock("action_delay", () -> new DelayAction(
            "action_delay",
            "Задержка",
            "Приостанавливает выполнение на указанное время",
            BlockType.ACTION,
            Material.CLOCK
        ));
        
        registerBlock("action_heal", () -> new HealAction(
            "action_heal",
            "Вылечить",
            "Восстанавливает здоровье игрока",
            BlockType.ACTION,
            Material.GOLDEN_APPLE
        ));
        
        registerBlock("action_damage", () -> new DamageAction(
            "action_damage",
            "Нанести урон",
            "Наносит урон игроку",
            BlockType.ACTION,
            Material.IRON_SWORD
        ));
        
        registerBlock("action_effect", () -> new EffectAction(
            "action_effect",
            "Эффект",
            "Применяет эффект к игроку",
            BlockType.ACTION,
            Material.POTION
        ));
        
        registerBlock("action_title", () -> new TitleAction(
            "action_title",
            "Показать заголовок",
            "Показывает заголовок игроку",
            BlockType.ACTION,
            Material.PAPER
        ));
        
        registerBlock("action_actionbar", () -> new ActionBarAction(
            "action_actionbar",
            "Показать в панели действий",
            "Показывает текст в панели действий игрока",
            BlockType.ACTION,
            Material.NAME_TAG
        ));
        
        registerBlock("action_broadcast", () -> new BroadcastAction(
            "action_broadcast",
            "Объявление",
            "Отправляет сообщение всем игрокам",
            BlockType.ACTION,
            Material.BELL
        ));
        
        // Блоки для работы с игровыми механиками
        registerBlock("action_set_game_rule", () -> new SetGameRuleAction(
            "action_set_game_rule",
            "Изменить правило игры",
            "Изменяет игровое правило",
            BlockType.ACTION,
            Material.WRITTEN_BOOK
        ));
        
        registerBlock("action_set_weather", () -> new SetWeatherAction(
            "action_set_weather",
            "Изменить погоду",
            "Изменяет погоду в мире",
            BlockType.ACTION,
            Material.WATER_BUCKET
        ));
        
        registerBlock("action_set_time", () -> new SetTimeAction(
            "action_set_time",
            "Установить время",
            "Устанавливает время суток",
            BlockType.ACTION,
            Material.CLOCK
        ));
        
        // Блоки для работы с инвентарем
        registerBlock("action_clear_inventory", () -> new ClearInventoryAction(
            "action_clear_inventory",
            "Очистить инвентарь",
            "Очищает инвентарь игрока",
            BlockType.ACTION,
            Material.BARRIER
        ));
        
        registerBlock("action_give_xp", () -> new GiveXPAction(
            "action_give_xp",
            "Выдать опыт",
            "Выдает опыт игроку",
            BlockType.ACTION,
            Material.EXPERIENCE_BOTTLE
        ));
    }
    
    /**
     * Регистрирует блоки условий.
     */
    private void registerConditionBlocks() {
        // Проверки прав и разрешений
        registerBlock("condition_has_permission", () -> new HasPermissionBlock(
            "condition_has_permission",
            "Имеет право",
            "Проверяет наличие права у игрока",
            BlockType.CONDITION,
            Material.WRITTEN_BOOK
        ));
        
        // Проверки инвентаря
        registerBlock("condition_has_item", () -> new HasItemBlock(
            "condition_has_item",
            "Имеет предмет",
            "Проверяет наличие предмета у игрока",
            BlockType.CONDITION,
            Material.ITEM_FRAME
        ));
        
        registerBlock("condition_inventory_full", () -> new InventoryFullCondition(
            "condition_inventory_full",
            "Инвентарь полон",
            "Проверяет, полон ли инвентарь игрока",
            BlockType.CONDITION,
            Material.CHEST
        ));
        
        // Проверки игрового состояния
        registerBlock("condition_is_sneaking", () -> new IsSneakingCondition(
            "condition_is_sneaking",
            "Игрок крадется",
            "Проверяет, крадется ли игрок",
            BlockType.CONDITION,
            Material.LEATHER_BOOTS
        ));
        
        registerBlock("condition_is_flying", () -> new IsFlyingCondition(
            "condition_is_flying",
            "Игрок летает",
            "Проверяет, летает ли игрок",
            BlockType.CONDITION,
            Material.ELYTRA
        ));
        
        // Проверки местоположения
        registerBlock("condition_in_region", () -> new InRegionCondition(
            "condition_in_region",
            "В регионе",
            "Проверяет, находится ли игрок в регионе",
            BlockType.CONDITION,
            Material.STONE_BRICKS
        ));
        
        registerBlock("condition_in_world", () -> new InWorldCondition(
            "condition_in_world",
            "В мире",
            "Проверяет, находится ли игрок в указанном мире",
            BlockType.CONDITION,
            Material.GRASS_BLOCK
        ));
        
        // Проверки статистики
        registerBlock("condition_has_xp_level", () -> new HasXPLevelCondition(
            "condition_has_xp_level",
            "Имеет уровень опыта",
            "Проверяет уровень опыта игрока",
            BlockType.CONDITION,
            Material.EXPERIENCE_BOTTLE
        ));
        
        // Комбинированные условия
        registerBlock("condition_and", () -> new AndCondition(
            "condition_and",
            "И",
            "Возвращает true, если оба условия верны",
            BlockType.CONDITION,
            Material.IRON_INGOT
        ));
        
        registerBlock("condition_or", () -> new OrCondition(
            "condition_or",
            "Или",
            "Возвращает true, если хотя бы одно условие верно",
            BlockType.CONDITION,
            Material.GOLD_INGOT
        ));
        
        registerBlock("condition_not", () -> new NotCondition(
            "condition_not",
            "Не",
            "Инвертирует условие",
            BlockType.CONDITION,
            Material.REDSTONE_TORCH
        ));
        
        // Проверки времени и погоды
        registerBlock("condition_is_day", () -> new IsDayCondition(
            "condition_is_day",
            "День",
            "Проверяет, день ли сейчас",
            BlockType.CONDITION,
            Material.CLOCK
        ));
        
        registerBlock("condition_is_raining", () -> new IsRainingCondition(
            "condition_is_raining",
            "Идет дождь",
            "Проверяет, идет ли дождь",
            BlockType.CONDITION,
            Material.WATER_BUCKET
        ));
        
        // Проверки состояния игрока
        registerBlock("condition_has_effect", () -> new HasEffectCondition(
            "condition_has_effect",
            "Имеет эффект",
            "Проверяет наличие эффекта у игрока",
            BlockType.CONDITION,
            Material.POTION
        ));
        
        registerBlock("condition_health_below", () -> new HealthBelowCondition(
            "condition_health_below",
            "Здоровье меньше",
            "Проверяет, что здоровье игрока ниже указанного значения",
            BlockType.CONDITION,
            Material.APPLE
        ));
        
        registerBlock("condition_food_below", () -> new FoodBelowCondition(
            "condition_food_below",
            "Голод меньше",
            "Проверяет, что уровень голода игрока ниже указанного значения",
            BlockType.CONDITION,
            Material.BREAD
        ));
    }
    
    /**
     * Регистрирует блоки событий.
     */
    private void registerEventBlocks() {
        // События игрока
        registerBlock("event_player_join", () -> new OnJoinEvent(
            "event_player_join",
            "При входе игрока",
            "Срабатывает при входе игрока на сервер",
            BlockType.EVENT,
            Material.PLAYER_HEAD
        ));
        
        registerBlock("event_player_quit", () -> new OnQuitEvent(
            "event_player_quit",
            "При выходе игрока",
            "Срабатывает при выходе игрока с сервера",
            BlockType.EVENT,
            Material.BARRIER
        ));
        
        registerBlock("event_player_death", () -> new OnPlayerDeathEvent(
            "event_player_death",
            "При смерти игрока",
            "Срабатывает при смерти игрока",
            BlockType.EVENT,
            Material.SKELETON_SKULL
        ));
        
        registerBlock("event_player_respawn", () -> new OnRespawnEvent(
            "event_player_respawn",
            "При возрождении игрока",
            "Срабатывает при возрождении игрока",
            BlockType.EVENT,
            Material.TOTEM_OF_UNDYING
        ));
        
        // События блоков
        registerBlock("event_block_break", () -> new OnBlockBreakEvent(
            "event_block_break",
            "При разрушении блока",
            "Срабатывает при разрушении блока игроком",
            BlockType.EVENT,
            Material.DIAMOND_PICKAXE
        ));
        
        registerBlock("event_block_place", () -> new OnBlockPlaceEvent(
            "event_block_place",
            "При установке блока",
            "Срабатывает при установке блока игроком",
            BlockType.EVENT,
            Material.GRASS_BLOCK
        ));
        
        registerBlock("event_block_interact", () -> new OnBlockInteractEvent(
            "event_block_interact",
            "При взаимодействии с блоком",
            "Срабатывает при взаимодействии с блоком",
            BlockType.EVENT,
            Material.OAK_BUTTON
        ));
        
        // События предметов
        registerBlock("event_item_interact", () -> new OnItemInteractEvent(
            "event_item_interact",
            "При использовании предмета",
            "Срабатывает при использовании предмета",
            BlockType.EVENT,
            Material.WOODEN_SWORD
        ));
        
        registerBlock("event_item_break", () -> new OnItemBreakEvent(
            "event_item_break",
            "При поломке предмета",
            "Срабатывает при поломке предмета в руках",
            BlockType.EVENT,
            Material.IRON_SWORD
        ));
        
        // События сущностей
        registerBlock("event_entity_damage", () -> new OnEntityDamageEvent(
            "event_entity_damage",
            "При получении урона",
            "Срабатывает при получении урона игроком",
            BlockType.EVENT,
            Material.IRON_SWORD
        ));
        
        registerBlock("event_entity_kill", () -> new OnEntityKillEvent(
            "event_entity_kill",
            "При убийстве моба",
            "Срабатывает при убийстве моба игроком",
            BlockType.EVENT,
            Material.SKELETON_SKULL
        ));
        
        // События чата
        registerBlock("event_player_chat", () -> new OnPlayerChatEvent(
            "event_player_chat",
            "При отправке сообщения",
            "Срабатывает при отправке сообщения в чат",
            BlockType.EVENT,
            Material.OAK_SIGN
        ));
        
        registerBlock("event_player_command", () -> new OnPlayerCommandEvent(
            "event_player_command",
            "При выполнении команды",
            "Срабатывает при выполнении команды игроком",
            BlockType.EVENT,
            Material.COMMAND_BLOCK
        ));
        
        // События инвентаря
        registerBlock("event_inventory_click", () -> new OnInventoryClickEvent(
            "event_inventory_click",
            "При клике в инвентаре",
            "Срабатывает при клике в инвентаре",
            BlockType.EVENT,
            Material.CHEST
        ));
        
        registerBlock("event_item_drop", () -> new OnItemDropEvent(
            "event_item_drop",
            "При выбросе предмета",
            "Срабатывает при выбросе предмета игроком",
            BlockType.EVENT,
            Material.DROPPER
        ));
        
        // События мира
        registerBlock("event_time_change", () -> new OnTimeChangeEvent(
            "event_time_change",
            "При смене времени суток",
            "Срабатывает при смене времени суток",
            BlockType.EVENT,
            Material.CLOCK
        ));
        
        registerBlock("event_weather_change", () -> new OnWeatherChangeEvent(
            "event_weather_change",
            "При смене погоды",
            "Срабатывает при смене погоды",
            BlockType.EVENT,
            Material.WATER_BUCKET
        ));
        
        // Кастомные события
        registerBlock("event_custom_trigger", () -> new OnCustomTriggerEvent(
            "event_custom_trigger",
            "По кастомному триггеру",
            "Срабатывает при вызове кастомного триггера",
            BlockType.EVENT,
            Material.REDSTONE_TORCH
        ));
        
        registerBlock("event_timer", () -> new OnTimerEvent(
            "event_timer",
            "По таймеру",
            "Срабатывает через указанные интервалы времени",
            BlockType.EVENT,
            Material.CLOCK
        ));
    }
    
    /**
     * Регистрирует логические блоки.
     */
    private void registerLogicBlocks() {
        // Условные операторы
        registerBlock("logic_if", () -> new IfBlock(
            "logic_if",
            "Если",
            "Выполняет код, если условие истинно",
            BlockType.LOGIC,
            Material.COMPARATOR
        ));
        
        registerBlock("logic_else", () -> new ElseBlock(
            "logic_else",
            "Иначе",
            "Выполняет код, если условие ложно",
            BlockType.LOGIC,
            Material.REPEATER
        ));
        
        registerBlock("logic_else_if", () -> new ElseIfBlock(
            "logic_else_if",
            "Иначе если",
            "Проверяет дополнительное условие, если предыдущие не сработали",
            BlockType.LOGIC,
            Material.COMPARATOR
        ));
        
        // Логические операторы
        registerBlock("logic_and", () -> new AndBlock(
            "logic_and",
            "И",
            "Возвращает true, если оба условия истинны",
            BlockType.LOGIC,
            Material.IRON_INGOT
        ));
        
        registerBlock("logic_or", () -> new OrBlock(
            "logic_or",
            "Или",
            "Возвращает true, если хотя бы одно условие истинно",
            BlockType.LOGIC,
            Material.GOLD_INGOT
        ));
        
        registerBlock("logic_not", () -> new NotBlock(
            "logic_not",
            "Не",
            "Инвертирует значение условия",
            BlockType.LOGIC,
            Material.REDSTONE_TORCH
        ));
        
        // Операторы сравнения
        registerBlock("logic_equals", () -> new EqualsBlock(
            "logic_equals",
            "Равно",
            "Проверяет, равны ли значения",
            BlockType.LOGIC,
            Material.STONE_BUTTON
        ));
        
        registerBlock("logic_not_equals", () -> new NotEqualsBlock(
            "logic_not_equals",
            "Не равно",
            "Проверяет, что значения не равны",
            BlockType.LOGIC,
            Material.LEVER
        ));
        
        registerBlock("logic_greater_than", () -> new GreaterThanBlock(
            "logic_greater_than",
            "Больше чем",
            "Проверяет, что первое значение больше второго",
            BlockType.LOGIC,
            Material.IRON_BLOCK
        ));
        
        registerBlock("logic_less_than", () -> new LessThanBlock(
            "logic_less_than",
            "Меньше чем",
            "Проверяет, что первое значение меньше второго",
            BlockType.LOGIC,
            Material.GOLD_BLOCK
        ));
        
        registerBlock("logic_greater_than_equals", () -> new GreaterThanOrEqualsBlock(
            "logic_greater_than_equals",
            "Больше или равно",
            "Проверяет, что первое значение больше или равно второму",
            BlockType.LOGIC,
            Material.IRON_INGOT
        ));
        
        registerBlock("logic_less_than_equals", () -> new LessThanOrEqualsBlock(
            "logic_less_than_equals",
            "Меньше или равно",
            "Проверяет, что первое значение меньше или равно второму",
            BlockType.LOGIC,
            Material.GOLD_INGOT
        ));
        
        // Логические константы
        registerBlock("logic_true", () -> new TrueBlock(
            "logic_true",
            "Истина",
            "Всегда возвращает true",
            BlockType.LOGIC,
            Material.LIME_CONCRETE
        ));
        
        registerBlock("logic_false", () -> new FalseBlock(
            "logic_false",
            "Ложь",
            "Всегда возвращает false",
            BlockType.LOGIC,
            Material.RED_CONCRETE
        ));
        
        // Проверка на null
        registerBlock("logic_is_null", () -> new IsNullBlock(
            "logic_is_null",
            "Пустое значение",
            "Проверяет, является ли значение null",
            BlockType.LOGIC,
            Material.BARRIER
        ));
        
        registerBlock("logic_is_not_null", () -> new IsNotNullBlock(
            "logic_is_not_null",
            "Не пустое значение",
            "Проверяет, что значение не является null",
            BlockType.LOGIC,
            Material.STONE_BUTTON
        ));
    }
    
    /**
     * Регистрирует блоки циклов.
     */
    private void registerLoopBlocks() {
        // Основные циклы
        registerBlock("loop_repeat", () -> new RepeatBlock(
            "loop_repeat",
            "Повторить N раз",
            "Повторяет действия указанное количество раз",
            BlockType.LOOP,
            Material.REPEATER
        ));
        
        registerBlock("loop_while", () -> new WhileBlock(
            "loop_while",
            "Пока условие истинно",
            "Повторяет действия, пока условие истинно",
            BlockType.LOOP,
            Material.COMPARATOR
        ));
        
        // Управление циклами
        registerBlock("loop_break", () -> new BreakLoopBlock(
            "loop_break",
            "Прервать цикл",
            "Немедленно выходит из текущего цикла",
            BlockType.LOOP,
            Material.BARRIER
        ));
        
        registerBlock("loop_continue", () -> new ContinueLoopBlock(
            "loop_continue",
            "Продолжить со следующей итерации",
            "Переходит к следующей итерации цикла",
            BlockType.LOOP,
            Material.LEVER
        ));
        
        // Специальные циклы
        registerBlock("loop_for_each", () -> new ForEachBlock(
            "loop_for_each",
            "Для каждого элемента",
            "Выполняет действия для каждого элемента коллекции",
            BlockType.LOOP,
            Material.CHEST
        ));
        
        registerBlock("loop_timer", () -> new TimerLoopBlock(
            "loop_timer",
            "Таймер с интервалом",
            "Выполняет действия с указанным интервалом",
            BlockType.LOOP,
            Material.CLOCK
        ));
    }
    
    /**
     * Регистрирует блоки переменных.
     */
    private void registerVariableBlocks() {
        // Основные операции с переменными
        registerBlock("variable_set", () -> new SetVariableBlock(
            "variable_set",
            "Установить переменную",
            "Устанавливает значение переменной",
            BlockType.VARIABLE,
            Material.NAME_TAG
        ));
        
        registerBlock("variable_get", () -> new GetVariableBlock(
            "variable_get",
            "Получить переменную",
            "Получает значение переменной",
            BlockType.VARIABLE,
            Material.NAME_TAG
        ));
        
        // Математические операции
        registerBlock("variable_math_operation", () -> new MathOperationBlock(
            "variable_math_operation",
            "Математическая операция",
            "Выполняет математическую операцию над числами",
            BlockType.VARIABLE,
            Material.CALCITE
        ));
        
        registerBlock("variable_random_number", () -> new RandomNumberBlock(
            "variable_random_number",
            "Случайное число",
            "Генерирует случайное число в заданном диапазоне",
            BlockType.VARIABLE,
            Material.DICE
        ));
        
        // Операции со строками
        registerBlock("variable_string_concat", () -> new StringConcatBlock(
            "variable_string_concat",
            "Объединить строки",
            "Объединяет несколько строк в одну",
            BlockType.VARIABLE,
            Material.PAPER
        ));
        
        registerBlock("variable_string_length", () -> new StringLengthBlock(
            "variable_string_length",
            "Длина строки",
            "Возвращает длину строки",
            BlockType.VARIABLE,
            Material.STRING
        ));
        
        // Операции со списками
        registerBlock("variable_list_create", () -> new CreateListBlock(
            "variable_list_create",
            "Создать список",
            "Создает новый список из элементов",
            BlockType.VARIABLE,
            Material.CHEST_MINECART
        ));
        
        registerBlock("variable_list_add", () -> new ListAddItemBlock(
            "variable_list_add",
            "Добавить в список",
            "Добавляет элемент в список",
            BlockType.VARIABLE,
            Material.HOPPER_MINECART
        ));
        
        registerBlock("variable_list_get", () -> new ListGetItemBlock(
            "variable_list_get",
            "Получить из списка",
            "Получает элемент из списка по индексу",
            BlockType.VARIABLE,
            Material.MINECART
        ));
        
        // Глобальные переменные
        registerBlock("variable_global_set", () -> new SetGlobalVariableBlock(
            "variable_global_set",
            "Установить глобальную переменную",
            "Устанавливает значение глобальной переменной",
            BlockType.VARIABLE,
            Material.ENDER_EYE
        ));
        
        registerBlock("variable_global_get", () -> new GetGlobalVariableBlock(
            "variable_global_get",
            "Получить глобальную переменную",
            "Получает значение глобальной переменной",
            BlockType.VARIABLE,
            Material.ENDER_PEARL
        ));
        
        // Преобразование типов
        registerBlock("variable_convert_to_string", () -> new ConvertToStringBlock(
            "variable_convert_to_string",
            "В строку",
            "Преобразует значение в строку",
            BlockType.VARIABLE,
            Material.PAPER
        ));
        
        registerBlock("variable_convert_to_number", () -> new ConvertToNumberBlock(
            "variable_convert_to_number",
            "В число",
            "Преобразует значение в число",
            BlockType.VARIABLE,
            Material.IRON_INGOT
        ));
        
        // Специальные переменные
        registerBlock("variable_player_name", () -> new PlayerNameVariableBlock(
            "variable_player_name",
            "Имя игрока",
            "Возвращает имя текущего игрока",
            BlockType.VARIABLE,
            Material.PLAYER_HEAD
        ));
        
        registerBlock("variable_world_time", () -> new WorldTimeVariableBlock(
            "variable_world_time",
            "Время мира",
            "Возвращает текущее время в мире",
            BlockType.VARIABLE,
            Material.CLOCK
        ));
    }
    
    /**
     * Регистрирует блок в фабрике.
     * 
     * @param id Уникальный идентификатор блока
     * @param blockSupplier Поставщик экземпляров блока
     */
    public void registerBlock(String id, Supplier<Block> blockSupplier) {
        if (blockRegistry.containsKey(id)) {
            plugin.getLogger().warning("Блок с ID '" + id + "' уже зарегистрирован, перезаписываем");
        }
        blockRegistry.put(id, blockSupplier);
    }
    
    /**
     * Создает новый экземпляр блока по его ID.
     * 
     * @param blockId ID блока
     * @return Новый экземпляр блока
     * @throws IllegalArgumentException если блок с указанным ID не найден
     */
    public Block createBlock(String blockId) {
        Supplier<Block> supplier = blockRegistry.get(blockId);
        if (supplier == null) {
            throw new IllegalArgumentException("Неизвестный ID блока: " + blockId);
        }
        return supplier.get();
    }
    
    /**
     * Проверяет, зарегистрирован ли блок с указанным ID.
     * 
     * @param blockId ID блока
     * @return true, если блок зарегистрирован
     */
    public boolean isBlockRegistered(String blockId) {
        return blockRegistry.containsKey(blockId);
    }
    
    /**
     * Возвращает список всех зарегистрированных ID блоков.
     * 
     * @return Набор ID блоков
     */
    public Set<String> getRegisteredBlockIds() {
        return Collections.unmodifiableSet(blockRegistry.keySet());
    }
} 