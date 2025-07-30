package com.megacreative.coding;

import org.bukkit.Material;

/**
 * Перечисление всех типов блоков, доступных в системе кодирования.
 * Разделены на категории для удобства.
 */
public enum BlockType {
    // События игрока (зеленые блоки)
    EVENT_PLAYER_JOIN(Material.LIME_CONCRETE),      // Игрок входит в мир
    EVENT_PLAYER_QUIT(Material.LIME_CONCRETE),      // Игрок выходит из мира
    EVENT_PLAYER_INTERACT(Material.LIME_CONCRETE),  // Игрок кликает ПКМ
    EVENT_PLAYER_MOVE(Material.LIME_CONCRETE),      // Игрок перемещается
    EVENT_PLAYER_CHAT(Material.LIME_CONCRETE),      // Игрок отправляет сообщение
    EVENT_PLAYER_DEATH(Material.LIME_CONCRETE),     // Игрок умирает
    EVENT_PLAYER_RESPAWN(Material.LIME_CONCRETE),   // Игрок респаунится

    // Условия игрока (синие)
    CONDITION_HAS_ITEM(Material.BLUE_CONCRETE),
    CONDITION_PLAYER_HEALTH(Material.BLUE_CONCRETE),
    CONDITION_PLAYER_GAMEMODE(Material.BLUE_CONCRETE),
    CONDITION_PLAYER_LOCATION(Material.BLUE_CONCRETE),
    CONDITION_VARIABLE_EQUALS(Material.BLUE_CONCRETE),
    CONDITION_IS_IN_REGION(Material.BLUE_CONCRETE),
    CONDITION_HAS_PERMISSION(Material.BLUE_CONCRETE),

    // Действия игрока (красные)
    ACTION_SEND_MESSAGE(Material.RED_CONCRETE),
    ACTION_TELEPORT_PLAYER(Material.RED_CONCRETE),
    ACTION_GIVE_ITEM(Material.RED_CONCRETE),
    ACTION_SET_HEALTH(Material.RED_CONCRETE),
    ACTION_SET_GAMEMODE(Material.RED_CONCRETE),
    ACTION_PLAY_SOUND(Material.RED_CONCRETE),

    // Переменные (желтые)
    VARIABLE_SET(Material.YELLOW_CONCRETE),
    VARIABLE_GET(Material.YELLOW_CONCRETE),
    VARIABLE_ADD(Material.YELLOW_CONCRETE),
    VARIABLE_SUBTRACT(Material.YELLOW_CONCRETE),

    // Условные блоки (фиолетовые)
    IF_CONDITION(Material.PURPLE_CONCRETE),
    ELSE_CONDITION(Material.PURPLE_CONCRETE),

    // Игровые действия (оранжевые)
    GAME_ACTION_SPAWN_MOB(Material.ORANGE_CONCRETE),
    GAME_ACTION_EXPLOSION(Material.ORANGE_CONCRETE),
    GAME_ACTION_WEATHER(Material.ORANGE_CONCRETE),
    GAME_ACTION_TIME(Material.ORANGE_CONCRETE),
    GAME_ACTION_BROADCAST(Material.ORANGE_CONCRETE);

    private final Material material;

    BlockType(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }
}