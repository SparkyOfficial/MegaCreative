package com.megacreative.coding;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.megacreative.MegaCreative;

import java.util.Arrays;
import java.util.List;

/**
 * Класс для создания и выдачи предметов-блоков кодирования игрокам.
 */
public class CodingItems {

    /**
     * Выдает игроку полный набор блоков для кодирования
     */
    public static void giveCodingItems(Player player) {
        player.getInventory().clear();
        
        // События игрока
        player.getInventory().addItem(createEventBlock(BlockType.EVENT_PLAYER_JOIN));
        player.getInventory().addItem(createEventBlock(BlockType.EVENT_PLAYER_QUIT));
        player.getInventory().addItem(createEventBlock(BlockType.EVENT_PLAYER_INTERACT));
        player.getInventory().addItem(createEventBlock(BlockType.EVENT_PLAYER_MOVE));
        player.getInventory().addItem(createEventBlock(BlockType.EVENT_PLAYER_CHAT));
        
        // Условия
        player.getInventory().addItem(createConditionBlock(BlockType.CONDITION_HAS_ITEM));
        player.getInventory().addItem(createConditionBlock(BlockType.CONDITION_PLAYER_HEALTH));
        player.getInventory().addItem(createConditionBlock(BlockType.CONDITION_PLAYER_GAMEMODE));
        player.getInventory().addItem(createConditionBlock(BlockType.CONDITION_VARIABLE_EQUALS));
        
        // Действия игрока
        player.getInventory().addItem(createActionBlock(BlockType.ACTION_SEND_MESSAGE));
        player.getInventory().addItem(createActionBlock(BlockType.ACTION_TELEPORT_PLAYER));
        player.getInventory().addItem(createActionBlock(BlockType.ACTION_GIVE_ITEM));
        player.getInventory().addItem(createActionBlock(BlockType.ACTION_SET_HEALTH));
        player.getInventory().addItem(createActionBlock(BlockType.ACTION_PLAY_SOUND));
        
        // Переменные
        player.getInventory().addItem(createVariableBlock(BlockType.VARIABLE_SET));
        player.getInventory().addItem(createVariableBlock(BlockType.VARIABLE_GET));
        player.getInventory().addItem(createVariableBlock(BlockType.VARIABLE_ADD));
        
        // Условные блоки
        player.getInventory().addItem(createControlBlock(BlockType.IF_CONDITION));
        player.getInventory().addItem(createControlBlock(BlockType.ELSE_CONDITION));
        
        // Игровые действия
        player.getInventory().addItem(createGameActionBlock(BlockType.GAME_ACTION_SPAWN_MOB));
        player.getInventory().addItem(createGameActionBlock(BlockType.GAME_ACTION_EXPLOSION));
        player.getInventory().addItem(createGameActionBlock(BlockType.GAME_ACTION_WEATHER));
        player.getInventory().addItem(createGameActionBlock(BlockType.GAME_ACTION_BROADCAST));
        
        player.sendMessage("§a✓ Вы получили полный набор блоков для кодирования!");
        player.sendMessage("§7Размещайте блоки в мире и соединяйте их для создания скриптов.");
    }

    /**
     * Создает предмет для блока события (зеленые блоки)
     */
    private static ItemStack createEventBlock(BlockType blockType) {
        ItemStack item = new ItemStack(blockType.getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Устанавливаем NBT-метку для идентификации типа блока
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(BLOCK_TYPE_KEY, PersistentDataType.STRING, blockType.name());
            
            meta.setDisplayName("§a§l" + getBlockDisplayName(blockType));
            meta.setLore(Arrays.asList(
                "§7Тип: §aСобытие",
                "§7" + getBlockDescription(blockType),
                "",
                "§e▶ Размести в мире для создания скрипта"
            ));
            
            // Добавляем свечение для красоты
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Создает предмет для блока условия (синие блоки)
     */
    private static ItemStack createConditionBlock(BlockType blockType) {
        ItemStack item = new ItemStack(blockType.getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Устанавливаем NBT-метку для идентификации типа блока
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(BLOCK_TYPE_KEY, PersistentDataType.STRING, blockType.name());
            
            meta.setDisplayName("§9§l" + getBlockDisplayName(blockType));
            meta.setLore(Arrays.asList(
                "§7Тип: §9Условие",
                "§7" + getBlockDescription(blockType),
                "",
                "§e▶ Размести в мире для проверки условий"
            ));
            
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    // Ключ для хранения типа блока в NBT
    private static final NamespacedKey BLOCK_TYPE_KEY = new NamespacedKey(MegaCreative.getInstance(), "block_type");
    
    /**
     * Создает предмет для блока действия (красные блоки)
     */
    private static ItemStack createActionBlock(BlockType blockType) {
        ItemStack item = new ItemStack(blockType.getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Устанавливаем NBT-метку для идентификации типа блока
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(BLOCK_TYPE_KEY, PersistentDataType.STRING, blockType.name());
            
            meta.setDisplayName("§c§l" + getBlockDisplayName(blockType));
            meta.setLore(Arrays.asList(
                "§7Тип: §cДействие",
                "§7" + getBlockDescription(blockType),
                "",
                "§e▶ Размести в мире для выполнения действий"
            ));
            
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Создает предмет для блока переменной (желтые блоки)
     */
    private static ItemStack createVariableBlock(BlockType blockType) {
        ItemStack item = new ItemStack(blockType.getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Устанавливаем NBT-метку для идентификации типа блока
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(BLOCK_TYPE_KEY, PersistentDataType.STRING, blockType.name());
            
            meta.setDisplayName("§e§l" + getBlockDisplayName(blockType));
            meta.setLore(Arrays.asList(
                "§7Тип: §eПеременная",
                "§7" + getBlockDescription(blockType),
                "",
                "§e▶ Размести в мире для работы с данными"
            ));
            
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Создает предмет для условного блока (фиолетовые блоки)
     */
    private static ItemStack createControlBlock(BlockType blockType) {
        ItemStack item = new ItemStack(blockType.getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Устанавливаем NBT-метку для идентификации типа блока
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(BLOCK_TYPE_KEY, PersistentDataType.STRING, blockType.name());
            
            meta.setDisplayName("§d§l" + getBlockDisplayName(blockType));
            meta.setLore(Arrays.asList(
                "§7Тип: §dУправление",
                "§7" + getBlockDescription(blockType),
                "",
                "§e▶ Размести в мире для ветвления логики"
            ));
            
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Создает предмет для игрового действия (оранжевые блоки)
     */
    private static ItemStack createGameActionBlock(BlockType blockType) {
        ItemStack item = new ItemStack(blockType.getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Устанавливаем NBT-метку для идентификации типа блока
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(BLOCK_TYPE_KEY, PersistentDataType.STRING, blockType.name());
            
            meta.setDisplayName("§6§l" + getBlockDisplayName(blockType));
            meta.setLore(Arrays.asList(
                "§7Тип: §6Игровое действие",
                "§7" + getBlockDescription(blockType),
                "",
                "§e▶ Размести в мире для воздействия на игру"
            ));
            
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Возвращает отображаемое имя блока
     */
    private static String getBlockDisplayName(BlockType blockType) {
        switch (blockType) {
            // События
            case EVENT_PLAYER_JOIN: return "Игрок зашел";
            case EVENT_PLAYER_QUIT: return "Игрок вышел";
            case EVENT_PLAYER_INTERACT: return "Игрок кликнул";
            case EVENT_PLAYER_MOVE: return "Игрок двигается";
            case EVENT_PLAYER_CHAT: return "Игрок написал";
            
            // Условия
            case CONDITION_HAS_ITEM: return "Есть предмет";
            case CONDITION_PLAYER_HEALTH: return "Здоровье игрока";
            case CONDITION_PLAYER_GAMEMODE: return "Режим игрока";
            case CONDITION_VARIABLE_EQUALS: return "Переменная равна";
            
            // Действия
            case ACTION_SEND_MESSAGE: return "Отправить сообщение";
            case ACTION_TELEPORT_PLAYER: return "Телепортировать";
            case ACTION_GIVE_ITEM: return "Выдать предмет";
            case ACTION_SET_HEALTH: return "Установить здоровье";
            case ACTION_PLAY_SOUND: return "Проиграть звук";
            
            // Переменные
            case VARIABLE_SET: return "Присвоить переменную";
            case VARIABLE_GET: return "Получить переменную";
            case VARIABLE_ADD: return "Прибавить к переменной";
            
            // Управление
            case IF_CONDITION: return "Если";
            case ELSE_CONDITION: return "Иначе";
            
            // Игровые действия
            case GAME_ACTION_SPAWN_MOB: return "Заспаунить моба";
            case GAME_ACTION_EXPLOSION: return "Создать взрыв";
            case GAME_ACTION_WEATHER: return "Изменить погоду";
            case GAME_ACTION_BROADCAST: return "Объявление всем";
            
            default: return blockType.name();
        }
    }

    /**
     * Возвращает описание блока
     */
    private static String getBlockDescription(BlockType blockType) {
        switch (blockType) {
            // События
            case EVENT_PLAYER_JOIN: return "Срабатывает когда игрок заходит в мир";
            case EVENT_PLAYER_QUIT: return "Срабатывает когда игрок выходит из мира";
            case EVENT_PLAYER_INTERACT: return "Срабатывает при клике игрока";
            case EVENT_PLAYER_MOVE: return "Срабатывает при движении игрока";
            case EVENT_PLAYER_CHAT: return "Срабатывает при отправке сообщения";
            
            // Условия
            case CONDITION_HAS_ITEM: return "Проверяет наличие предмета у игрока";
            case CONDITION_PLAYER_HEALTH: return "Проверяет здоровье игрока";
            case CONDITION_PLAYER_GAMEMODE: return "Проверяет игровой режим";
            case CONDITION_VARIABLE_EQUALS: return "Сравнивает значение переменной";
            
            // Действия
            case ACTION_SEND_MESSAGE: return "Отправляет сообщение игроку";
            case ACTION_TELEPORT_PLAYER: return "Телепортирует игрока";
            case ACTION_GIVE_ITEM: return "Выдает предмет игроку";
            case ACTION_SET_HEALTH: return "Устанавливает здоровье игрока";
            case ACTION_PLAY_SOUND: return "Проигрывает звук игроку";
            
            // Переменные
            case VARIABLE_SET: return "Устанавливает значение переменной";
            case VARIABLE_GET: return "Получает значение переменной";
            case VARIABLE_ADD: return "Прибавляет к переменной число";
            
            // Управление
            case IF_CONDITION: return "Выполняет действия при выполнении условия";
            case ELSE_CONDITION: return "Выполняет действия если условие не выполнено";
            
            // Игровые действия
            case GAME_ACTION_SPAWN_MOB: return "Создает моба в указанном месте";
            case GAME_ACTION_EXPLOSION: return "Создает взрыв в указанном месте";
            case GAME_ACTION_WEATHER: return "Изменяет погоду в мире";
            case GAME_ACTION_BROADCAST: return "Отправляет сообщение всем игрокам";
            
            default: return "Блок кодирования";
        }
    }
    
    /**
     * Проверяет, является ли предмет блоком кодирования
     * @param item Предмет для проверки
     * @return true, если предмет является блоком кодирования
     */
    public static boolean isCodingBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        
        // Проверяем наличие NBT-метки
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(BLOCK_TYPE_KEY, PersistentDataType.STRING);
    }
    
    /**
     * Получает тип блока кодирования из предмета
     * @param item Предмет для проверки
     * @return BlockType или null, если предмет не является блоком кодирования
     */
    public static BlockType getCodingBlockType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String typeName = container.get(BLOCK_TYPE_KEY, PersistentDataType.STRING);
        
        if (typeName == null) return null;
        
        try {
            return BlockType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
