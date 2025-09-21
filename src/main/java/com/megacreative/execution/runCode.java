package com.megacreative.execution;

import com.megacreative.MegaCreative;
import com.megacreative.configs.WorldCode;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 🎆 Движок выполнения кода в стиле эталонной системы
 * 
 * Выполняет скомпилированные строки кода из конфигурации WorldCode, аналогично системе runCode FrameLand.
 * Это мост между скомпилированным визуальным кодом и фактическим выполнением игры.
 *
 * 🎆 Reference System-Style Code Execution Engine
 * 
 * Executes compiled code strings from WorldCode configuration, similar to FrameLand's runCode system.
 * This is the bridge between compiled visual code and actual game execution.
 *
 * 🎆 Code-Ausführungs-Engine im Referenzsystem-Stil
 * 
 * Führt kompilierte Code-Zeichenfolgen aus der WorldCode-Konfiguration aus, ähnlich wie das runCode-System von FrameLand.
 * Dies ist die Brücke zwischen kompiliertem visuellem Code und der tatsächlichen Spielausführung.
 */
public class runCode implements Listener {
    
    private final MegaCreative plugin;
    private final ScriptEngine scriptEngine;
    
    /**
     * Инициализирует движок выполнения кода
     * @param plugin Экземпляр основного плагина
     *
     * Initializes code execution engine
     * @param plugin Main plugin instance
     *
     * Initialisiert die Code-Ausführungs-Engine
     * @param plugin Hauptplugin-Instanz
     */
    public runCode(MegaCreative plugin) {
        this.plugin = plugin;
        this.scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
        plugin.getLogger().info("🎆 runCode execution engine initialized");
        // 🎆 Движок выполнения кода runCode инициализирован
        // 🎆 runCode-Ausführungs-Engine initialisiert
    }
    
    // === Обработчики событий ===
    // === Event Handlers ===
    // === Ereignis-Handler ===
    
    /**
     * Обрабатывает событие входа игрока
     * @param event Событие входа игрока
     *
     * Handles player join event
     * @param event Player join event
     *
     * Behandelt das Spieler-Beitrittsereignis
     * @param event Spieler-Beitrittsereignis
     */
    @EventHandler
    public void joinEvent(PlayerJoinEvent event) {
        executeWorldCode(event.getPlayer(), "joinEvent", event);
    }
    
    /**
     * Обрабатывает событие выхода игрока
     * @param event Событие выхода игрока
     *
     * Handles player quit event
     * @param event Player quit event
     *
     * Behandelt das Spieler-Austrittsereignis
     * @param event Spieler-Austrittsereignis
     */
    @EventHandler
    public void quitEvent(PlayerQuitEvent event) {
        executeWorldCode(event.getPlayer(), "quitEvent", event);
    }
    
    /**
     * Обрабатывает событие разрушения блока
     * @param event Событие разрушения блока
     *
     * Handles block break event
     * @param event Block break event
     *
     * Behandelt das Block-Zerstörungsereignis
     * @param event Block-Zerstörungsereignis
     */
    @EventHandler
    public void breakEvent(BlockBreakEvent event) {
        executeWorldCode(event.getPlayer(), "breakEvent", event);
    }
    
    /**
     * Обрабатывает событие размещения блока
     * @param event Событие размещения блока
     *
     * Handles block place event
     * @param event Block place event
     *
     * Behandelt das Block-Platzierungsereignis
     * @param event Block-Platzierungsereignis
     */
    @EventHandler
    public void placeEvent(BlockPlaceEvent event) {
        executeWorldCode(event.getPlayer(), "placeEvent", event);
    }
    
    /**
     * Обрабатывает событие перемещения игрока
     * @param event Событие перемещения игрока
     *
     * Handles player move event
     * @param event Player move event
     *
     * Behandelt das Spieler-Bewegungsereignis
     * @param event Spieler-Bewegungsereignis
     */
    @EventHandler
    public void moveEvent(PlayerMoveEvent event) {
        // Оптимизация: не проверять на каждое микродвижение
        // Optimization: don't check on every micro-movement
        // Optimierung: nicht bei jeder Mikrobewegung prüfen
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && 
            event.getFrom().getBlockY() == event.getTo().getBlockY() && 
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        executeWorldCode(event.getPlayer(), "moveEvent", event);
    }
    
    /**
     * Обрабатывает событие левого клика мыши
     * @param event Событие взаимодействия игрока
     *
     * Handles left mouse button event
     * @param event Player interact event
     *
     * Behandelt das Ereignis der linken Maustaste
     * @param event Spieler-Interaktionsereignis
     */
    @EventHandler
    public void LMBEvent(PlayerInteractEvent event) {
        if ((event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_AIR || event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) && 
            event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND) {
            executeWorldCode(event.getPlayer(), "LMBEvent", event);
        }
    }
    
    /**
     * Обрабатывает событие правого клика мыши
     * @param event Событие взаимодействия игрока
     *
     * Handles right mouse button event
     * @param event Player interact event
     *
     * Behandelt das Ereignis der rechten Maustaste
     * @param event Spieler-Interaktionsereignis
     */
    @EventHandler
    public void RMBEvent(PlayerInteractEvent event) {
        if ((event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) && 
            event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND) {
            executeWorldCode(event.getPlayer(), "RMBEvent", event);
        }
    }
    
    /**
     * Обрабатывает событие сообщения в чате
     * @param event Событие чата игрока
     *
     * Handles player chat message event
     * @param event Async player chat event
     *
     * Behandelt das Ereignis einer Chat-Nachricht des Spielers
     * @param event Asynchrones Spieler-Chat-Ereignis
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void messageEvent(AsyncPlayerChatEvent event) {
        executeWorldCode(event.getPlayer(), "messageEvent", event);
    }
    
    /**
     * Обрабатывает событие смерти моба
     * @param event Событие смерти сущности
     *
     * Handles mob death event
     * @param event Entity death event
     *
     * Behandelt das Ereignis des Todes eines Mobs
     * @param event Entitäts-Todesereignis
     */
    @EventHandler
    public void mobDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            executeWorldCode(event.getEntity().getKiller(), "mobDeathEvent", event);
        }
    }
    
    /**
     * Обрабатывает событие смерти игрока
     * @param event Событие смерти игрока
     *
     * Handles player death event
     * @param event Player death event
     *
     * Behandelt das Ereignis des Todes eines Spielers
     * @param event Spieler-Todesereignis
     */
    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        executeWorldCode(event.getEntity(), "playerDeathEvent", event);
    }
    
    /**
     * Обрабатывает событие убийства игрока игроком
     * @param event Событие смерти сущности
     *
     * Handles player killing player event
     * @param event Entity death event
     *
     * Behandelt das Ereignis, wenn ein Spieler einen Spieler tötet
     * @param event Entitäts-Todesereignis
     */
    @EventHandler
    public void playerKillPlayer(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player && event.getEntity().getKiller() != null && 
            event.getEntity().getKiller() instanceof Player) {
            executeWorldCode(event.getEntity().getKiller(), "plKillPlEvent", event);
        }
    }
    
    /**
     * Обрабатывает событие убийства моба игроком
     * @param event Событие смерти сущности
     *
     * Handles player killing mob event
     * @param event Entity death event
     *
     * Behandelt das Ereignis, wenn ein Spieler einen Mob tötet
     * @param event Entitäts-Todesereignis
     */
    @EventHandler
    public void playerKillMob(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null && 
            event.getEntity() instanceof org.bukkit.entity.Monster) {
            executeWorldCode(event.getEntity().getKiller(), "plKillMobEvent", event);
        }
    }
    
    /**
     * Обрабатывает событие нанесения урона игроком игроку
     * @param event Событие нанесения урона сущностью
     *
     * Handles player damaging player event
     * @param event Entity damage by entity event
     *
     * Behandelt das Ereignis, wenn ein Spieler einem Spieler Schaden zufügt
     * @param event Entitätsschaden durch Entitätsereignis
     */
    @EventHandler
    public void playerPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            executeWorldCode((Player) event.getDamager(), "plDmgPlEvent", event);
        }
    }
    
    /**
     * Обрабатывает событие нанесения урона мобом игроку
     * @param event Событие нанесения урона сущностью
     *
     * Handles mob damaging player event
     * @param event Entity damage by entity event
     *
     * Behandelt das Ereignis, wenn ein Mob einem Spieler Schaden zufügt
     * @param event Entitätsschaden durch Entitätsereignis
     */
    @EventHandler
    public void playerMobDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof org.bukkit.entity.Monster) {
            executeWorldCode((Player) event.getEntity(), "mobDmgPlEvent", event);
        }
    }
    
    /**
     * Обрабатывает событие нанесения урона игроком мобу
     * @param event Событие нанесения урона сущностью
     *
     * Handles player damaging mob event
     * @param event Entity damage by entity event
     *
     * Behandelt das Ereignis, wenn ein Spieler einem Mob Schaden zufügt
     * @param event Entitätsschaden durch Entitätsereignis
     */
    @EventHandler
    public void mobPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof org.bukkit.entity.Monster) {
            executeWorldCode((Player) event.getDamager(), "plDmgMobEvent", event);
        }
    }
    
    /**
     * Обрабатывает событие открытия инвентаря
     * @param event Событие открытия инвентаря
     *
     * Handles inventory open event
     * @param event Inventory open event
     *
     * Behandelt das Ereignis des Öffnens eines Inventars
     * @param event Inventar-Öffnungsereignis
     */
    @EventHandler
    public void inventoryOpenEvent(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            executeWorldCode((Player) event.getPlayer(), "invOpenEvent", event);
        }
    }
    
    /**
     * Обрабатывает событие закрытия инвентаря
     * @param event Событие закрытия инвентаря
     *
     * Handles inventory close event
     * @param event Inventory close event
     *
     * Behandelt das Ereignis des Schließens eines Inventars
     * @param event Inventar-Schließungsereignis
     */
    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            executeWorldCode((Player) event.getPlayer(), "invCloseEvent", event);
        }
    }
    
    /**
     * Обрабатывает событие клика в инвентаре
     * @param event Событие клика в инвентаре
     *
     * Handles inventory click event
     * @param event Inventory click event
     *
     * Behandelt das Ereignis eines Klicks im Inventar
     * @param event Inventar-Klick-Ereignis
     */
    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player && event.getCurrentItem() != null) {
            executeWorldCode((Player) event.getWhoClicked(), "invClickEvent", event);
        }
    }
    
    /**
     * Обрабатывает событие подбора предмета
     * @param event Событие подбора предмета игроком
     *
     * Handles item pickup event
     * @param event Player pickup item event
     *
     * Behandelt das Ereignis des Aufhebens eines Gegenstands
     * @param event Spieler hebt Gegenstand auf Ereignis
     */
    @EventHandler
    public void itemPickup(PlayerPickupItemEvent event) {
        executeWorldCode(event.getPlayer(), "itemPickupEvent", event);
    }
    
    /**
     * Обрабатывает событие выбрасывания предмета
     * @param event Событие выбрасывания предмета игроком
     *
     * Handles item drop event
     * @param event Player drop item event
     *
     * Behandelt das Ereignis des Wegwerfens eines Gegenstands
     * @param event Spieler wirft Gegenstand weg Ereignis
     */
    @EventHandler
    public void itemDrop(PlayerDropItemEvent event) {
        executeWorldCode(event.getPlayer(), "itemDropEvent", event);
    }
    
    /**
     * Обрабатывает событие телепортации
     * @param event Событие телепортации игрока
     *
     * Handles teleport event
     * @param event Player teleport event
     *
     * Behandelt das Teleport-Ereignis
     * @param event Spieler-Teleport-Ereignis
     */
    @EventHandler
    public void teleportEvent(PlayerTeleportEvent event) {
        executeWorldCode(event.getPlayer(), "teleportEvent", event);
    }
    
    /**
     * Обрабатывает событие изменения слота
     * @param event Событие изменения слота предмета игрока
     *
     * Handles slot change event
     * @param event Player item held event
     *
     * Behandelt das Ereignis der Slot-Änderung
     * @param event Spieler hält Gegenstand Ereignis
     */
    @EventHandler
    public void slotChange(PlayerItemHeldEvent event) {
        if (event.getPreviousSlot() != event.getNewSlot()) {
            executeWorldCode(event.getPlayer(), "slotChangeEvent", event);
        }
    }
    
    // === Методы выполнения ===
    // === Execution Methods ===
    // === Ausführungsmethoden ===
    
    /**
     * Выполняет скомпилированный код для события мира
     * @param player Игрок
     * @param eventType Тип события
     * @param event Событие
     *
     * Executes compiled code for a world event
     * @param player Player
     * @param eventType Event type
     * @param event Event
     *
     * Führt kompilierten Code für ein Weltenereignis aus
     * @param player Spieler
     * @param eventType Ereignistyp
     * @param event Ereignis
     */
    private void executeWorldCode(Player player, String eventType, Event event) {
        if (player == null || player.getWorld() == null) return;
        
        // Only execute in play worlds
        // Выполнять только в игровых мирах
        // Nur in Spielwelten ausführen
        if (!player.getWorld().getName().contains("-world")) {
            return;
        }
        
        String worldId = player.getWorld().getName().replace("-world", "");
        
        // Check if world has compiled code
        // Проверить, есть ли у мира скомпилированный код
        // Prüfen, ob die Welt kompilierten Code hat
        if (!WorldCode.hasCode(worldId)) {
            return;
        }
        
        // Get compiled code lines
        // Получить строки скомпилированного кода
        // Kompilierte Codezeilen abrufen
        List<String> codeLines = WorldCode.getCode(worldId);
        if (codeLines == null || codeLines.isEmpty()) {
            return;
        }
        
        // Process each line of compiled code
        // Обработать каждую строку скомпилированного кода
        // Jede Zeile des kompilierten Codes verarbeiten
        for (String codeLine : codeLines) {
            List<String> functions = Arrays.asList(codeLine.split("&"));
            
            // Check if first function matches event type
            // Проверить, соответствует ли первая функция типу события
            // Prüfen, ob die erste Funktion dem Ereignistyp entspricht
            if (!functions.isEmpty() && functions.get(0).equals(eventType)) {
                // 🎆 FIXED: Ensure thread safety for async events
                // 🎆 ИСПРАВЛЕНО: Обеспечить безопасность потоков для асинхронных событий
                // 🎆 FIX: Thread-Sicherheit für asynchrone Ereignisse gewährleisten
                if (event.isAsynchronous()) {
                    final List<String> finalFunctions = functions;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            CodeBlock firstBlock = parseStringToCodeChain(finalFunctions, player);
                            if (firstBlock != null) {
                                scriptEngine.executeBlockChain(firstBlock, player, eventType);
                            }
                        }
                    }.runTask(plugin);
                } else {
                    CodeBlock firstBlock = parseStringToCodeChain(functions, player);
                    if (firstBlock != null) {
                        scriptEngine.executeBlockChain(firstBlock, player, eventType);
                    }
                }
            }
        }
    }

    /**
     * 🎆 НОВОЕ: Преобразует старый строковый формат в современную, надежную цепочку объектов CodeBlock.
     * Это мост между устаревшим строковым кодом и мощным ScriptEngine.
     *
     * @param functions Список строк функций из старого скомпилированного кода.
     * @param player Контекст игрока.
     * @return Первый CodeBlock в новой созданной цепочке, готовый для выполнения.
     *
     * 🎆 NEW: Parses the old string format into a modern, reliable chain of CodeBlock objects.
     * This is the bridge between the legacy string code and the powerful ScriptEngine.
     *
     * @param functions The list of function strings from the old compiled code.
     * @param player The player context.
     * @return The first CodeBlock in the newly created chain, ready for execution.
     *
     * 🎆 NEU: Parst das alte Zeichenfolgenformat in eine moderne, zuverlässige Kette von CodeBlock-Objekten.
     * Dies ist die Brücke zwischen dem veralteten Zeichenfolgencode und der leistungsstarken ScriptEngine.
     *
     * @param functions Die Liste der Funktionszeichenfolgen aus dem alten kompilierten Code.
     * @param player Der Spielerkontext.
     * @return Der erste CodeBlock in der neu erstellten Kette, bereit zur Ausführung.
     */
    private CodeBlock parseStringToCodeChain(List<String> functions, Player player) {
        if (functions.size() <= 1) {
            return null;
        }

        CodeBlock head = null;
        CodeBlock current = null;

        // Start from 1 to skip the event trigger
        // Начать с 1, чтобы пропустить триггер события
        // Beginne bei 1, um den Ereignisauslöser zu überspringen
        for (int i = 1; i < functions.size(); i++) {
            String funcStr = functions.get(i);
            String actionId = getActionIdFromString(funcStr);
            
            if (actionId == null || actionId.isEmpty()) {
                plugin.getLogger().warning("Could not parse action from string: " + funcStr);
                // Не удалось разобрать действие из строки:
                // Konnte Aktion aus Zeichenfolge nicht parsen:
                continue;
            }

            // Create a new CodeBlock for this action
            // Создать новый CodeBlock для этого действия
            // Erstelle einen neuen CodeBlock für diese Aktion
            CodeBlock newBlock = new CodeBlock(Material.COMMAND_BLOCK, actionId); // Material is a placeholder
            // Материал является заполнителем
            // Material ist ein Platzhalter
            
            // Here, you would parse parameters from funcStr and add them to the newBlock
            // For example: newBlock.setParameter("message", new DataValue(parsedMessage));
            // Здесь вы бы разобрали параметры из funcStr и добавили их в newBlock
            // Например: newBlock.setParameter("message", new DataValue(parsedMessage));
            // Hier würden Sie Parameter aus funcStr parsen und sie dem newBlock hinzufügen
            // Zum Beispiel: newBlock.setParameter("message", new DataValue(parsedMessage));

            // Collapse if statement with common parts
            if (head == null) {
                head = newBlock;
            } else {
                current.setNextBlock(newBlock);
            }
            current = newBlock;
        }

        return head;
    }

    /**
     * Извлекает ID действия (например, "playerMessage") из строки функции.
     * @param func Строка функции
     * @return ID действия или null, если не найдено
     *
     * Extracts the action ID (e.g., "playerMessage") from a function string.
     * @param func Function string
     * @return Action ID or null if not found
     *
     * Extrahiert die Aktions-ID (z.B. "playerMessage") aus einer Funktionszeichenfolge.
     * @param func Funktionszeichenfolge
     * @return Aktions-ID oder null, wenn nicht gefunden
     */
    private String getActionIdFromString(String func) {
        int parenthesisIndex = func.indexOf('(');
        if (parenthesisIndex != -1) {
            return func.substring(0, parenthesisIndex);
        }
        return func; // No parameters, the whole string is the ID
        // Нет параметров, вся строка является ID
        // Keine Parameter, die gesamte Zeichenfolge ist die ID
    }
}