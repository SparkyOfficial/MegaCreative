package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Простая и понятная система визуального программирования
 * Каждый тип блока = определенное действие
 */
public class SimpleVisualCodingSystem {
    
    private final MegaCreative plugin;
    
    // Простая карта: Материал блока -> Действие
    private static final Map<Material, String> BLOCK_ACTIONS = new HashMap<>();
    
    static {
        // СОБЫТИЯ (Триггеры) - Изумрудные блоки
        BLOCK_ACTIONS.put(Material.EMERALD_BLOCK, "player_join");
        BLOCK_ACTIONS.put(Material.DIAMOND_BLOCK, "player_leave");
        BLOCK_ACTIONS.put(Material.REDSTONE_BLOCK, "block_click");
        BLOCK_ACTIONS.put(Material.GOLD_BLOCK, "block_break");
        BLOCK_ACTIONS.put(Material.LAPIS_BLOCK, "player_move");
        
        // ДЕЙСТВИЯ - Разные блоки
        BLOCK_ACTIONS.put(Material.OAK_SIGN, "send_message");
        BLOCK_ACTIONS.put(Material.COMMAND_BLOCK, "run_command");
        BLOCK_ACTIONS.put(Material.CHEST, "give_item");
        BLOCK_ACTIONS.put(Material.NOTE_BLOCK, "play_sound");
        BLOCK_ACTIONS.put(Material.DISPENSER, "teleport");
        BLOCK_ACTIONS.put(Material.BREWING_STAND, "give_effect");
        
        // УСЛОВИЯ - Терракота
        BLOCK_ACTIONS.put(Material.ORANGE_TERRACOTTA, "has_item");
        BLOCK_ACTIONS.put(Material.PURPLE_TERRACOTTA, "player_health");
        BLOCK_ACTIONS.put(Material.WHITE_TERRACOTTA, "variable_equals");
        BLOCK_ACTIONS.put(Material.RED_TERRACOTTA, "is_op");
    }
    
    // Человекочитаемые названия действий
    private static final Map<String, String> ACTION_NAMES = new HashMap<>();
    
    static {
        // События
        ACTION_NAMES.put("player_join", "§a⚡ Вход игрока");
        ACTION_NAMES.put("player_leave", "§c⚡ Выход игрока");
        ACTION_NAMES.put("block_click", "§e⚡ Клик по блоку");
        ACTION_NAMES.put("block_break", "§6⚡ Разрушение блока");
        ACTION_NAMES.put("player_move", "§b⚡ Движение игрока");
        
        // Действия
        ACTION_NAMES.put("send_message", "§f📝 Отправить сообщение");
        ACTION_NAMES.put("run_command", "§d⚡ Выполнить команду");
        ACTION_NAMES.put("give_item", "§e📦 Выдать предмет");
        ACTION_NAMES.put("play_sound", "§a🎵 Воспроизвести звук");
        ACTION_NAMES.put("teleport", "§b🌀 Телепортировать");
        ACTION_NAMES.put("give_effect", "§d🧪 Дать эффект");
        
        // Условия
        ACTION_NAMES.put("has_item", "§6🔍 Есть предмет?");
        ACTION_NAMES.put("player_health", "§c❤ Здоровье игрока?");
        ACTION_NAMES.put("variable_equals", "§e📊 Переменная равна?");
        ACTION_NAMES.put("is_op", "§4👑 Является OP?");
    }
    
    public SimpleVisualCodingSystem(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Парсит блоки в мире и создает простые скрипты
     */
    public List<SimpleScript> parseWorld(CreativeWorld creativeWorld) {
        List<SimpleScript> scripts = new ArrayList<>();
        
        World world = plugin.getServer().getWorld(creativeWorld.getWorldName());
        if (world == null) {
            plugin.getLogger().warning("Мир не найден: " + creativeWorld.getWorldName());
            return scripts;
        }
        
        plugin.getLogger().info("§aПарсинг блоков в мире: " + creativeWorld.getName());
        
        // Сканируем область мира на наличие блоков кода
        int centerX = 0, centerZ = 0; // Центр мира
        int radius = 100; // Радиус сканирования
        
        for (int x = centerX - radius; x <= centerX + radius; x += 5) {
            for (int z = centerZ - radius; z <= centerZ + radius; z += 5) {
                for (int y = 60; y <= 120; y++) { // Сканируем по высоте
                    Block block = world.getBlockAt(x, y, z);
                    
                    // Проверяем, является ли блок триггером (событием)
                    String action = BLOCK_ACTIONS.get(block.getType());
                    if (action != null && isEventTrigger(action)) {
                        // Найден триггерный блок - создаем скрипт
                        SimpleScript script = parseScript(world, block, action);
                        if (script != null) {
                            scripts.add(script);
                            plugin.getLogger().info("§e  Найден скрипт: " + ACTION_NAMES.get(action) + 
                                    " в " + x + "," + y + "," + z);
                        }
                    }
                }
            }
        }
        
        plugin.getLogger().info("§aПарсинг завершен. Найдено скриптов: " + scripts.size());
        return scripts;
    }
    
    /**
     * Парсит один скрипт начиная с триггерного блока
     */
    private SimpleScript parseScript(World world, Block triggerBlock, String triggerAction) {
        SimpleScript script = new SimpleScript(triggerAction, triggerBlock.getLocation());
        
        // Ищем связанные блоки действий вокруг триггера
        List<SimpleAction> actions = findConnectedActions(world, triggerBlock);
        script.setActions(actions);
        
        return script.getActions().isEmpty() ? null : script;
    }
    
    /**
     * Находит все блоки действий, связанные с триггером
     */
    private List<SimpleAction> findConnectedActions(World world, Block triggerBlock) {
        List<SimpleAction> actions = new ArrayList<>();
        
        int triggerX = triggerBlock.getX();
        int triggerY = triggerBlock.getY();
        int triggerZ = triggerBlock.getZ();
        
        // Проверяем блоки вокруг триггера (в радиусе 3 блоков)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue; // Пропускаем сам триггер
                    
                    Block block = world.getBlockAt(triggerX + dx, triggerY + dy, triggerZ + dz);
                    String action = BLOCK_ACTIONS.get(block.getType());
                    
                    if (action != null && !isEventTrigger(action)) {
                        // Найден блок действия
                        SimpleAction simpleAction = new SimpleAction(action, block.getLocation());
                        
                        // Получаем параметры из таблички рядом с блоком
                        String params = getParametersFromNearbySign(world, block);
                        simpleAction.setParameters(params);
                        
                        actions.add(simpleAction);
                        
                        plugin.getLogger().info("§b    Действие: " + ACTION_NAMES.get(action) + 
                                " (" + params + ")");
                    }
                }
            }
        }
        
        return actions;
    }
    
    /**
     * Получает параметры из таблички рядом с блоком
     */
    private String getParametersFromNearbySign(World world, Block actionBlock) {
        int x = actionBlock.getX();
        int y = actionBlock.getY();
        int z = actionBlock.getZ();
        
        // Проверяем блоки вокруг на наличие таблички
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Block block = world.getBlockAt(x + dx, y + dy, z + dz);
                    
                    if (block.getState() instanceof Sign) {
                        Sign sign = (Sign) block.getState();
                        String[] lines = sign.getLines();
                        
                        // Объединяем все строки таблички
                        StringBuilder params = new StringBuilder();
                        for (String line : lines) {
                            if (line != null && !line.trim().isEmpty()) {
                                if (params.length() > 0) params.append(" ");
                                params.append(line.trim());
                            }
                        }
                        
                        return params.toString();
                    }
                }
            }
        }
        
        return ""; // Параметры не найдены
    }
    
    /**
     * Проверяет, является ли действие триггером события
     */
    private boolean isEventTrigger(String action) {
        return action.startsWith("player_") || action.startsWith("block_");
    }
    
    /**
     * Выполняет простой скрипт
     */
    public void executeScript(SimpleScript script, Player player, CreativeWorld world) {
        plugin.getLogger().info("§aВыполнение скрипта: " + ACTION_NAMES.get(script.getTrigger()));
        
        for (SimpleAction action : script.getActions()) {
            executeAction(action, player, world);
        }
    }
    
    /**
     * Выполняет одно действие
     */
    private void executeAction(SimpleAction action, Player player, CreativeWorld world) {
        String actionType = action.getAction();
        String params = action.getParameters();
        
        plugin.getLogger().info("§e  Выполнение: " + ACTION_NAMES.get(actionType) + " (" + params + ")");
        
        try {
            switch (actionType) {
                case "send_message":
                    if (!params.isEmpty()) {
                        player.sendMessage("§a" + params);
                    } else {
                        player.sendMessage("§aПривет от системы визуального программирования!");
                    }
                    break;
                    
                case "run_command":
                    if (!params.isEmpty()) {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), 
                                params.replace("%player%", player.getName()));
                    }
                    break;
                    
                case "give_item":
                    if (!params.isEmpty()) {
                        // Простая выдача предмета (можно расширить)
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                                "give " + player.getName() + " " + params);
                    }
                    break;
                    
                case "teleport":
                    if (!params.isEmpty()) {
                        String[] coords = params.split(" ");
                        if (coords.length >= 3) {
                            try {
                                double x = Double.parseDouble(coords[0]);
                                double y = Double.parseDouble(coords[1]);
                                double z = Double.parseDouble(coords[2]);
                                player.teleport(new org.bukkit.Location(player.getWorld(), x, y, z));
                                player.sendMessage("§aВы телепортированы!");
                            } catch (NumberFormatException e) {
                                player.sendMessage("§cНеверные координаты в скрипте!");
                            }
                        }
                    }
                    break;
                    
                case "play_sound":
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    break;
                    
                default:
                    player.sendMessage("§eВыполнено действие: " + ACTION_NAMES.get(actionType));
                    break;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка выполнения действия " + actionType + ": " + e.getMessage());
            player.sendMessage("§cОшибка выполнения скрипта!");
        }
    }
    
    /**
     * Получает информацию о блоке для отображения игроку
     */
    public String getBlockInfo(Material material) {
        String action = BLOCK_ACTIONS.get(material);
        if (action != null) {
            return ACTION_NAMES.get(action);
        }
        return "§7Неизвестный блок";
    }
    
    /**
     * Получает все доступные блоки кода
     */
    public Map<Material, String> getAvailableBlocks() {
        Map<Material, String> blocks = new HashMap<>();
        for (Map.Entry<Material, String> entry : BLOCK_ACTIONS.entrySet()) {
            blocks.put(entry.getKey(), ACTION_NAMES.get(entry.getValue()));
        }
        return blocks;
    }
}

/**
 * Простой скрипт
 */
class SimpleScript {
    private final String trigger;
    private final org.bukkit.Location location;
    private List<SimpleAction> actions = new ArrayList<>();
    
    public SimpleScript(String trigger, org.bukkit.Location location) {
        this.trigger = trigger;
        this.location = location;
    }
    
    public String getTrigger() { return trigger; }
    public org.bukkit.Location getLocation() { return location; }
    public List<SimpleAction> getActions() { return actions; }
    public void setActions(List<SimpleAction> actions) { this.actions = actions; }
}

/**
 * Простое действие
 */
class SimpleAction {
    private final String action;
    private final org.bukkit.Location location;
    private String parameters = "";
    
    public SimpleAction(String action, org.bukkit.Location location) {
        this.action = action;
        this.location = location;
    }
    
    public String getAction() { return action; }
    public org.bukkit.Location getLocation() { return location; }
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
}
