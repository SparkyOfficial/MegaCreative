package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.Location;

import java.util.Map;

/**
 * 🎆 Пользовательские события в стиле эталонной системы для расширенной функциональности
 *
 * 🎆 Custom Reference System-style events for enhanced functionality
 *
 * 🎆 Benutzerdefinierte Ereignisse im Referenzsystem-Stil für erweiterte Funktionalität
 */
public class ReferenceSystemCustomEvents {

    /**
     * Событие входа игрока в регион
     *
     * Player enter region event
     *
     * Spieler betritt Region Ereignis
     */
    public static class PlayerEnterRegionEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String regionName;
        private final Location location;

        /**
         * Создает новое событие входа игрока в регион
         * @param player Игрок
         * @param regionName Имя региона
         * @param location Расположение
         *
         * Creates a new player enter region event
         * @param player Player
         * @param regionName Region name
         * @param location Location
         *
         * Erstellt ein neues Ereignis, wenn ein Spieler eine Region betritt
         * @param player Spieler
         * @param regionName Regionsname
         * @param location Standort
         */
        public PlayerEnterRegionEvent(Player player, String regionName, Location location) {
            this.player = player;
            this.regionName = regionName;
            this.location = location;
        }

        /**
         * Получает игрока
         * @return Игрок
         *
         * Gets the player
         * @return Player
         *
         * Ruft den Spieler ab
         * @return Spieler
         */
        public Player getPlayer() { return player; }
        
        /**
         * Получает имя региона
         * @return Имя региона
         *
         * Gets the region name
         * @return Region name
         *
         * Ruft den Regionsnamen ab
         * @return Regionsname
         */
        public String getRegionName() { return regionName; }
        
        /**
         * Получает расположение
         * @return Расположение
         *
         * Gets the location
         * @return Location
         *
         * Ruft den Standort ab
         * @return Standort
         */
        public Location getLocation() { return location; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    /**
     * Событие выхода игрока из региона
     *
     * Player leave region event
     *
     * Spieler verlässt Region Ereignis
     */
    public static class PlayerLeaveRegionEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String regionName;
        private final Location location;

        /**
         * Создает новое событие выхода игрока из региона
         * @param player Игрок
         * @param regionName Имя региона
         * @param location Расположение
         *
         * Creates a new player leave region event
         * @param player Player
         * @param regionName Region name
         * @param location Location
         *
         * Erstellt ein neues Ereignis, wenn ein Spieler eine Region verlässt
         * @param player Spieler
         * @param regionName Regionsname
         * @param location Standort
         */
        public PlayerLeaveRegionEvent(Player player, String regionName, Location location) {
            this.player = player;
            this.regionName = regionName;
            this.location = location;
        }

        /**
         * Получает игрока
         * @return Игрок
         *
         * Gets the player
         * @return Player
         *
         * Ruft den Spieler ab
         * @return Spieler
         */
        public Player getPlayer() { return player; }
        
        /**
         * Получает имя региона
         * @return Имя региона
         *
         * Gets the region name
         * @return Region name
         *
         * Ruft den Regionsnamen ab
         * @return Regionsname
         */
        public String getRegionName() { return regionName; }
        
        /**
         * Получает расположение
         * @return Расположение
         *
         * Gets the location
         * @return Location
         *
         * Ruft den Standort ab
         * @return Standort
         */
        public Location getLocation() { return location; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    /**
     * Событие изменения переменной игрока
     *
     * Player variable change event
     *
     * Spieler-Variablenänderungsereignis
     */
    public static class PlayerVariableChangeEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String variableName;
        private final Object oldValue;
        private final Object newValue;

        /**
         * Создает новое событие изменения переменной игрока
         * @param player Игрок
         * @param variableName Имя переменной
         * @param oldValue Старое значение
         * @param newValue Новое значение
         *
         * Creates a new player variable change event
         * @param player Player
         * @param variableName Variable name
         * @param oldValue Old value
         * @param newValue New value
         *
         * Erstellt ein neues Ereignis für die Änderung einer Spieler-Variable
         * @param player Spieler
         * @param variableName Variablenname
         * @param oldValue Alter Wert
         * @param newValue Neuer Wert
         */
        public PlayerVariableChangeEvent(Player player, String variableName, Object oldValue, Object newValue) {
            this.player = player;
            this.variableName = variableName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        /**
         * Получает игрока
         * @return Игрок
         *
         * Gets the player
         * @return Player
         *
         * Ruft den Spieler ab
         * @return Spieler
         */
        public Player getPlayer() { return player; }
        
        /**
         * Получает имя переменной
         * @return Имя переменной
         *
         * Gets the variable name
         * @return Variable name
         *
         * Ruft den Variablennamen ab
         * @return Variablenname
         */
        public String getVariableName() { return variableName; }
        
        /**
         * Получает старое значение
         * @return Старое значение
         *
         * Gets the old value
         * @return Old value
         *
         * Ruft den alten Wert ab
         * @return Alter Wert
         */
        public Object getOldValue() { return oldValue; }
        
        /**
         * Получает новое значение
         * @return Новое значение
         *
         * Gets the new value
         * @return New value
         *
         * Ruft den neuen Wert ab
         * @return Neuer Wert
         */
        public Object getNewValue() { return newValue; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    /**
     * Событие истечения таймера
     *
     * Timer expire event
     *
     * Timer-Ablaufereignis
     */
    public static class TimerExpireEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String timerName;
        private final long duration;
        private final Object timerData;

        /**
         * Создает новое событие истечения таймера
         * @param player Игрок
         * @param timerName Имя таймера
         * @param duration Продолжительность
         * @param timerData Данные таймера
         *
         * Creates a new timer expire event
         * @param player Player
         * @param timerName Timer name
         * @param duration Duration
         * @param timerData Timer data
         *
         * Erstellt ein neues Timer-Ablaufereignis
         * @param player Spieler
         * @param timerName Timer-Name
         * @param duration Dauer
         * @param timerData Timer-Daten
         */
        public TimerExpireEvent(Player player, String timerName, long duration, Object timerData) {
            this.player = player;
            this.timerName = timerName;
            this.duration = duration;
            this.timerData = timerData;
        }

        /**
         * Получает игрока
         * @return Игрок
         *
         * Gets the player
         * @return Player
         *
         * Ruft den Spieler ab
         * @return Spieler
         */
        public Player getPlayer() { return player; }
        
        /**
         * Получает имя таймера
         * @return Имя таймера
         *
         * Gets the timer name
         * @return Timer name
         *
         * Ruft den Timer-Namen ab
         * @return Timer-Name
         */
        public String getTimerName() { return timerName; }
        
        /**
         * Получает продолжительность
         * @return Продолжительность
         *
         * Gets the duration
         * @return Duration
         *
         * Ruft die Dauer ab
         * @return Dauer
         */
        public long getDuration() { return duration; }
        
        /**
         * Получает данные таймера
         * @return Данные таймера
         *
         * Gets the timer data
         * @return Timer data
         *
         * Ruft die Timer-Daten ab
         * @return Timer-Daten
         */
        public Object getTimerData() { return timerData; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    /**
     * Событие пользовательского действия игрока
     *
     * Player custom action event
     *
     * Spieler benutzerdefinierte Aktion Ereignis
     */
    public static class PlayerCustomActionEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String actionName;
        private final Map<String, Object> actionData;

        /**
         * Создает новое событие пользовательского действия игрока
         * @param player Игрок
         * @param actionName Имя действия
         * @param actionData Данные действия
         *
         * Creates a new player custom action event
         * @param player Player
         * @param actionName Action name
         * @param actionData Action data
         *
         * Erstellt ein neues Ereignis für eine benutzerdefinierte Spieleraktion
         * @param player Spieler
         * @param actionName Aktionsname
         * @param actionData Aktionsdaten
         */
        public PlayerCustomActionEvent(Player player, String actionName, Map<String, Object> actionData) {
            this.player = player;
            this.actionName = actionName;
            this.actionData = actionData;
        }

        /**
         * Получает игрока
         * @return Игрок
         *
         * Gets the player
         * @return Player
         *
         * Ruft den Spieler ab
         * @return Spieler
         */
        public Player getPlayer() { return player; }
        
        /**
         * Получает имя действия
         * @return Имя действия
         *
         * Gets the action name
         * @return Action name
         *
         * Ruft den Aktionsnamen ab
         * @return Aktionsname
         */
        public String getActionName() { return actionName; }
        
        /**
         * Получает данные действия
         * @return Данные действия
         *
         * Gets the action data
         * @return Action data
         *
         * Ruft die Aktionsdaten ab
         * @return Aktionsdaten
         */
        public Map<String, Object> getActionData() { return actionData; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    /**
     * Событие изменения счета игрока
     *
     * Player score change event
     *
     * Spieler-Punkteänderungsereignis
     */
    public static class PlayerScoreChangeEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String scoreType;
        private final int oldScore;
        private final int newScore;
        private final String reason;

        /**
         * Создает новое событие изменения счета игрока
         * @param player Игрок
         * @param scoreType Тип счета
         * @param oldScore Старый счет
         * @param newScore Новый счет
         * @param reason Причина
         *
         * Creates a new player score change event
         * @param player Player
         * @param scoreType Score type
         * @param oldScore Old score
         * @param newScore New score
         * @param reason Reason
         *
         * Erstellt ein neues Ereignis für die Änderung der Spielerpunkte
         * @param player Spieler
         * @param scoreType Punkte-Typ
         * @param oldScore Alte Punkte
         * @param newScore Neue Punkte
         * @param reason Grund
         */
        public PlayerScoreChangeEvent(Player player, String scoreType, int oldScore, int newScore, String reason) {
            this.player = player;
            this.scoreType = scoreType;
            this.oldScore = oldScore;
            this.newScore = newScore;
            this.reason = reason;
        }

        /**
         * Получает игрока
         * @return Игрок
         *
         * Gets the player
         * @return Player
         *
         * Ruft den Spieler ab
         * @return Spieler
         */
        public Player getPlayer() { return player; }
        
        /**
         * Получает тип счета
         * @return Тип счета
         *
         * Gets the score type
         * @return Score type
         *
         * Ruft den Punkte-Typ ab
         * @return Punkte-Typ
         */
        public String getScoreType() { return scoreType; }
        
        /**
         * Получает старый счет
         * @return Старый счет
         *
         * Gets the old score
         * @return Old score
         *
         * Ruft die alten Punkte ab
         * @return Alte Punkte
         */
        public int getOldScore() { return oldScore; }
        
        /**
         * Получает новый счет
         * @return Новый счет
         *
         * Gets the new score
         * @return New score
         *
         * Ruft die neuen Punkte ab
         * @return Neue Punkte
         */
        public int getNewScore() { return newScore; }
        
        /**
         * Получает причину
         * @return Причина
         *
         * Gets the reason
         * @return Reason
         *
         * Ruft den Grund ab
         * @return Grund
         */
        public String getReason() { return reason; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    /**
     * Событие вызова функции
     *
     * Function call event
     *
     * Funktionsaufrufereignis
     */
    public static class FunctionCallEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String functionName;
        private final Object[] parameters;

        /**
         * Создает новое событие вызова функции
         * @param player Игрок
         * @param functionName Имя функции
         * @param parameters Параметры
         *
         * Creates a new function call event
         * @param player Player
         * @param functionName Function name
         * @param parameters Parameters
         *
         * Erstellt ein neues Funktionsaufrufereignis
         * @param player Spieler
         * @param functionName Funktionsname
         * @param parameters Parameter
         */
        public FunctionCallEvent(Player player, String functionName, Object[] parameters) {
            this.player = player;
            this.functionName = functionName;
            this.parameters = parameters;
        }

        /**
         * Получает игрока
         * @return Игрок
         *
         * Gets the player
         * @return Player
         *
         * Ruft den Spieler ab
         * @return Spieler
         */
        public Player getPlayer() { return player; }
        
        /**
         * Получает имя функции
         * @return Имя функции
         *
         * Gets the function name
         * @return Function name
         *
         * Ruft den Funktionsnamen ab
         * @return Funktionsname
         */
        public String getFunctionName() { return functionName; }
        
        /**
         * Получает параметры
         * @return Параметры
         *
         * Gets the parameters
         * @return Parameters
         *
         * Ruft die Parameter ab
         * @return Parameter
         */
        public Object[] getParameters() { return parameters; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }

    /**
     * Событие изменения режима мира
     *
     * World mode change event
     *
     * Weltmodusänderungsereignis
     */
    public static class WorldModeChangeEvent extends Event {
        private static final HandlerList handlers = new HandlerList();
        private final Player player;
        private final String worldId;
        private final String oldMode;
        private final String newMode;

        /**
         * Создает новое событие изменения режима мира
         * @param player Игрок
         * @param worldId ID мира
         * @param oldMode Старый режим
         * @param newMode Новый режим
         *
         * Creates a new world mode change event
         * @param player Player
         * @param worldId World ID
         * @param oldMode Old mode
         * @param newMode New mode
         *
         * Erstellt ein neues Ereignis für die Änderung des Weltmodus
         * @param player Spieler
         * @param worldId Welt-ID
         * @param oldMode Alter Modus
         * @param newMode Neuer Modus
         */
        public WorldModeChangeEvent(Player player, String worldId, String oldMode, String newMode) {
            this.player = player;
            this.worldId = worldId;
            this.oldMode = oldMode;
            this.newMode = newMode;
        }

        /**
         * Получает игрока
         * @return Игрок
         *
         * Gets the player
         * @return Player
         *
         * Ruft den Spieler ab
         * @return Spieler
         */
        public Player getPlayer() { return player; }
        
        /**
         * Получает ID мира
         * @return ID мира
         *
         * Gets the world ID
         * @return World ID
         *
         * Ruft die Welt-ID ab
         * @return Welt-ID
         */
        public String getWorldId() { return worldId; }
        
        /**
         * Получает старый режим
         * @return Старый режим
         *
         * Gets the old mode
         * @return Old mode
         *
         * Ruft den alten Modus ab
         * @return Alter Modus
         */
        public String getOldMode() { return oldMode; }
        
        /**
         * Получает новый режим
         * @return Новый режим
         *
         * Gets the new mode
         * @return New mode
         *
         * Ruft den neuen Modus ab
         * @return Neuer Modus
         */
        public String getNewMode() { return newMode; }

        @Override
        public HandlerList getHandlers() { return handlers; }
        public static HandlerList getHandlerList() { return handlers; }
    }
}