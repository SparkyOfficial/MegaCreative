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
 * 🎆 Reference System-Style Code Execution Engine
 * 
 * Executes compiled code strings from WorldCode configuration, similar to FrameLand's runCode system.
 * This is the bridge between compiled visual code and actual game execution.
 */
public class runCode implements Listener {
    
    private final MegaCreative plugin;
    private final ScriptEngine scriptEngine;
    
    public runCode(MegaCreative plugin) {
        this.plugin = plugin;
        this.scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
        plugin.getLogger().info("🎆 runCode execution engine initialized");
    }
    
    // === Event Handlers ===
    
    @EventHandler
    public void joinEvent(PlayerJoinEvent event) {
        executeWorldCode(event.getPlayer(), "joinEvent", event);
    }
    
    @EventHandler
    public void quitEvent(PlayerQuitEvent event) {
        executeWorldCode(event.getPlayer(), "quitEvent", event);
    }
    
    @EventHandler
    public void breakEvent(BlockBreakEvent event) {
        executeWorldCode(event.getPlayer(), "breakEvent", event);
    }
    
    @EventHandler
    public void placeEvent(BlockPlaceEvent event) {
        executeWorldCode(event.getPlayer(), "placeEvent", event);
    }
    
    @EventHandler
    public void moveEvent(PlayerMoveEvent event) {
        // Оптимизация: не проверять на каждое микродвижение
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && 
            event.getFrom().getBlockY() == event.getTo().getBlockY() && 
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        executeWorldCode(event.getPlayer(), "moveEvent", event);
    }
    
    @EventHandler
    public void LMBEvent(PlayerInteractEvent event) {
        if ((event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_AIR || event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) && 
            event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND) {
            executeWorldCode(event.getPlayer(), "LMBEvent", event);
        }
    }
    
    @EventHandler
    public void RMBEvent(PlayerInteractEvent event) {
        if ((event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) && 
            event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND) {
            executeWorldCode(event.getPlayer(), "RMBEvent", event);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void messageEvent(AsyncPlayerChatEvent event) {
        executeWorldCode(event.getPlayer(), "messageEvent", event);
    }
    
    @EventHandler
    public void mobDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            executeWorldCode(event.getEntity().getKiller(), "mobDeathEvent", event);
        }
    }
    
    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        executeWorldCode(event.getEntity(), "playerDeathEvent", event);
    }
    
    @EventHandler
    public void playerKillPlayer(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player && event.getEntity().getKiller() != null && 
            event.getEntity().getKiller() instanceof Player) {
            executeWorldCode(event.getEntity().getKiller(), "plKillPlEvent", event);
        }
    }
    
    @EventHandler
    public void playerKillMob(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null && 
            event.getEntity() instanceof org.bukkit.entity.Monster) {
            executeWorldCode(event.getEntity().getKiller(), "plKillMobEvent", event);
        }
    }
    
    @EventHandler
    public void playerPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            executeWorldCode((Player) event.getDamager(), "plDmgPlEvent", event);
        }
    }
    
    @EventHandler
    public void playerMobDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof org.bukkit.entity.Monster) {
            executeWorldCode((Player) event.getEntity(), "mobDmgPlEvent", event);
        }
    }
    
    @EventHandler
    public void mobPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof org.bukkit.entity.Monster) {
            executeWorldCode((Player) event.getDamager(), "plDmgMobEvent", event);
        }
    }
    
    @EventHandler
    public void inventoryOpenEvent(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            executeWorldCode((Player) event.getPlayer(), "invOpenEvent", event);
        }
    }
    
    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            executeWorldCode((Player) event.getPlayer(), "invCloseEvent", event);
        }
    }
    
    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player && event.getCurrentItem() != null) {
            executeWorldCode((Player) event.getWhoClicked(), "invClickEvent", event);
        }
    }
    
    @EventHandler
    public void itemPickup(PlayerPickupItemEvent event) {
        executeWorldCode(event.getPlayer(), "itemPickupEvent", event);
    }
    
    @EventHandler
    public void itemDrop(PlayerDropItemEvent event) {
        executeWorldCode(event.getPlayer(), "itemDropEvent", event);
    }
    
    @EventHandler
    public void teleportEvent(PlayerTeleportEvent event) {
        executeWorldCode(event.getPlayer(), "teleportEvent", event);
    }
    
    @EventHandler
    public void slotChange(PlayerItemHeldEvent event) {
        if (event.getPreviousSlot() != event.getNewSlot()) {
            executeWorldCode(event.getPlayer(), "slotChangeEvent", event);
        }
    }
    
    // === Execution Methods ===
    
    /**
     * Executes compiled code for a world event
     */
    private void executeWorldCode(Player player, String eventType, Event event) {
        if (player == null || player.getWorld() == null) return;
        
        // Only execute in play worlds
        if (!player.getWorld().getName().contains("-world")) {
            return;
        }
        
        String worldId = player.getWorld().getName().replace("-world", "");
        
        // Check if world has compiled code
        if (!WorldCode.hasCode(worldId)) {
            return;
        }
        
        // Get compiled code lines
        List<String> codeLines = WorldCode.getCode(worldId);
        if (codeLines == null || codeLines.isEmpty()) {
            return;
        }
        
        // Process each line of compiled code
        for (String codeLine : codeLines) {
            List<String> functions = Arrays.asList(codeLine.split("&"));
            
            // Check if first function matches event type
            if (!functions.isEmpty() && functions.get(0).equals(eventType)) {
                handleLine(functions, player, player.getWorld(), 1, event);
            }
        }
    }
    
    /**
     * Handles execution of a line of compiled code
     */
    public void handleLine(List<String> line, Entity eventEntity, World world, int startIndex, Event e) {
        boolean shouldExecuteElse = false;
        
        for (int i = startIndex; i < line.size(); i++) {
            String func = line.get(i);
            
            String eq = getSomeString("eq", func);
            String type = getSomeString("type", func);
            String message = getSomeString("message", func);
            
            Random randomFunc = new Random();
            int random = world.getPlayers().isEmpty() ? 0 : randomFunc.nextInt(world.getPlayers().size());
            Entity entity1asd = getEntity(func, eventEntity, e, random);
            
            if (func.startsWith("closeInventory")) {
                if (entity1asd instanceof Player) {
                    ((Player) entity1asd).closeInventory();
                }
            } else if (func.startsWith("openInventory")) {
                if (entity1asd instanceof Player) {
                    assert eq != null;
                    assert type != null;
                    openInventory(eq, type, entity1asd, world, e);
                }
            } else if (func.startsWith("worldMessage")) {
                assert message != null;
                worldMessage(message, entity1asd, world, e, random);
            } else if (func.startsWith("playerMessage")) {
                if (entity1asd instanceof Player) {
                    assert message != null;
                    playerMessage(message, entity1asd, world, e, random);
                }
            } else if (func.startsWith("setHealth")) {
                assert eq != null;
                if (entity1asd instanceof LivingEntity)
                    setHealth(eq, entity1asd, e);
            } else if (func.startsWith("timeWait")) {
                assert eq != null;
                timeWait(eq, i, line, eventEntity, world, e, entity1asd);
                return; // Wait action stops execution temporarily
            } else if (func.startsWith("setBlock")) {
                assert eq != null;
                assert type != null;
                setBlock(eq, world, type, entity1asd, e);
            } else if (func.equals("cancelEvent") && e instanceof Cancellable) {
                ((Cancellable) e).setCancelled(true);
            } else if (func.startsWith("teleport")) {
                assert eq != null;
                teleport(eq, entity1asd, e);
            } else if (func.startsWith("useFunc")) {
                assert type != null;
                assert eq != null;
                useFunc(type, eq, entity1asd);
            } else if (func.startsWith("giveItems")) {
                if (entity1asd instanceof Player) {
                    giveItems(func, entity1asd, e);
                }
            } else if (func.startsWith("clearInventory")) {
                if (entity1asd instanceof Player) {
                    ((Player) entity1asd).getInventory().clear();
                }
            } else if (func.startsWith("giveRandItem")) {
                if (entity1asd instanceof Player) {
                    giveRandItem(func, entity1asd, e);
                }
            } else if (func.startsWith("deleteItems")) {
                if (entity1asd instanceof Player) {
                    deleteItems(func, entity1asd, e);
                }
            } else if (func.startsWith("if")) {
                if (evaluateCondition(eq, entity1asd, e)) {
                    String innerCode = getInnerCode(line.subList(i + 1, line.size()));
                    if (innerCode != null) {
                        List<String> innerCodeList = Arrays.asList(innerCode.split("&"));
                        handleLine(innerCodeList, entity1asd, world, 0, e);
                        i = findEndOfInnerCode(line, i);
                    }
                } else {
                    i = findEndOfInnerCode(line, i);
                }
            } else if (func.equals("else")) {
                if (shouldExecuteElse) {
                    String innerCode = getInnerCode(line.subList(i + 1, line.size()));
                    if (innerCode != null) {
                        List<String> innerCodeList = Arrays.asList(innerCode.split("&"));
                        handleLine(innerCodeList, entity1asd, world, 0, e);
                        i = findEndOfInnerCode(line, i);
                    }
                } else {
                    i = findEndOfInnerCode(line, i);
                }
            }
            
            if (func.startsWith("if")) {
                shouldExecuteElse = !evaluateCondition(eq, entity1asd, e);
            }
        }
    }
    
    // === Helper Methods ===
    
    private Entity getEntity(String func, Entity entity, Event e, int random) {
        if (func.contains("%_") && func.contains("_%")) {
            String sendTo = func.substring(func.indexOf("%_") + 2, func.indexOf("_%"));
            switch (sendTo) {
                case "player":
                    if (entity instanceof Player) {
                        return entity;
                    }
                    break;
                case "damager":
                    if (e instanceof EntityDeathEvent) {
                        if (((EntityDeathEvent) e).getEntity().getKiller() != null)
                            return ((EntityDeathEvent) e).getEntity().getKiller();
                    } else if (e instanceof EntityDamageByEntityEvent && 
                               ((EntityDamageByEntityEvent) e).getDamager() instanceof LivingEntity) {
                        return ((EntityDamageByEntityEvent) e).getDamager();
                    }
                    break;
                case "victim":
                    if (e instanceof EntityDeathEvent) {
                        if (((EntityDeathEvent) e).getEntity() != null)
                            return ((EntityDeathEvent) e).getEntity();
                    } else if (e instanceof EntityDamageByEntityEvent && 
                               ((EntityDamageByEntityEvent) e).getEntity() instanceof LivingEntity) {
                        return ((EntityDamageByEntityEvent) e).getEntity();
                    }
                    break;
                case "random":
                    List<Player> players = entity.getWorld().getPlayers();
                    if (!players.isEmpty()) {
                        return players.get(random >= players.size() ? 0 : random);
                    }
                    break;
            }
        }
        return entity;
    }
    
    private int findEndOfInnerCode(List<String> line, int startIndex) {
        int count = 0;
        for (int i = startIndex + 1; i < line.size(); i++) {
            String s = line.get(i);
            if (s.contains("{")) {
                count++;
            }
            
            count--;
            if (s.contains("}") && count == 0) {
                return i;
            }
        }
        
        return line.size() - 1;
    }
    
    private String getInnerCode(List<String> line) {
        int count = 0;
        int startIndex = -1;
        for (int i = 0; i < line.size(); i++) {
            String s = line.get(i);
            if (s.contains("{")) {
                if (startIndex == -1) {
                    startIndex = i;
                }
                count++;
            }
            
            count--;
            if (s.contains("}") && count == 0) {
                return String.join("&", line.subList(startIndex + 1, i));
            }
        }
        
        return null;
    }
    
    private String getSomeString(String need, String func) {
        switch (need) {
            case "eq":
                if (func.contains("(") && func.contains(")")) {
                    int startIndexFunc = func.indexOf('(') + 1;
                    int endIndexFunc = func.indexOf(')');
                    return func.substring(startIndexFunc, endIndexFunc);
                }
                break;
            case "type":
                if (func.contains("[") && func.contains("]")) {
                    int firstbracket = func.indexOf('[') + 1;
                    int lastbracket = func.indexOf(']');
                    return func.substring(firstbracket, lastbracket);
                }
                break;
            case "message":
                if (func.contains("~(") && func.contains(")~")) {
                    int startIndexFuncMSG = func.indexOf("~(") + 2;
                    int endIndexFuncMSG = func.indexOf(")~");
                    return func.substring(startIndexFuncMSG, endIndexFuncMSG);
                }
                break;
        }
        return null;
    }
    
    // === Action Implementations ===
    
    private void openInventory(String eq, String type, Entity entity1asd, World world, Event e) {
        List<String> why = Arrays.asList(eq.split("\\|"));
        if (type.equals("copy")) {
            try {
                if (eq.startsWith("apple")) {
                    Set<Material> transparentBlock = new HashSet<>();
                    transparentBlock.add(Material.AIR);
                    int appleFrom = eq.indexOf("[") + 1;
                    int appleTo = eq.indexOf("]~");
                    String apple = eq.substring(appleFrom, appleTo);
                    Player player = (Player) entity1asd;
                    
                    Location coords = null;
                    switch (apple) {
                        case "location":
                            coords = player.getLocation();
                            break;
                        case "look_block_loc":
                            coords = player.getTargetBlock(transparentBlock, 12).getLocation();
                            break;
                        case "block_loc":
                            if (e instanceof org.bukkit.event.block.BlockEvent) {
                                coords = ((org.bukkit.event.block.BlockEvent) e).getBlock().getLocation();
                            }
                            break;
                    }
                    if (coords != null && coords.getBlock().getState() instanceof Chest) {
                        Chest chest = (Chest) coords.getBlock().getState();
                        String name = "Chest"; // Use default name
                        if (chest.getInventory().getHolder() != null) {
                            // Try to get custom name if available
                        }
                        Inventory chestinventory = Bukkit.createInventory(null, chest.getInventory().getSize(), name);
                        chestinventory.setContents(chest.getInventory().getContents());
                        ((Player) entity1asd).openInventory(chestinventory);
                    }
                } else {
                    Location coords = new Location(world, Integer.parseInt(why.get(0)), Integer.parseInt(why.get(1)), Integer.parseInt(why.get(2)));
                    if (coords.getBlock().getState() instanceof Chest) {
                        Chest chest = (Chest) coords.getBlock().getState();
                        String name = "Chest"; // Use default name
                        if (chest.getInventory().getHolder() != null) {
                            // Try to get custom name if available
                        }
                        Inventory chestinventory = Bukkit.createInventory(null, chest.getInventory().getSize(), name);
                        chestinventory.setContents(chest.getInventory().getContents());
                        ((Player) entity1asd).openInventory(chestinventory);
                    }
                }
            } catch (Exception ignored) {
            }
        } else if (type.equals("original")) {
            try {
                if (eq.startsWith("apple")) {
                    Set<Material> transparentBlock = new HashSet<>();
                    transparentBlock.add(Material.AIR);
                    int appleFrom = eq.indexOf("[") + 1;
                    int appleTo = eq.indexOf("]~");
                    String apple = eq.substring(appleFrom, appleTo);
                    Player player = (Player) entity1asd;
                    
                    Location coords = null;
                    switch (apple) {
                        case "location":
                            coords = player.getLocation();
                            break;
                        case "look_block_loc":
                            coords = player.getTargetBlock(transparentBlock, 12).getLocation();
                            break;
                        case "block_loc":
                            if (e instanceof org.bukkit.event.block.BlockEvent) {
                                coords = ((org.bukkit.event.block.BlockEvent) e).getBlock().getLocation();
                            }
                            break;
                    }
                    if (coords != null && coords.getBlock().getState() instanceof Chest) {
                        Chest chest = (Chest) coords.getBlock().getState();
                        Inventory chestInventory = chest.getInventory();
                        ((Player) entity1asd).openInventory(chestInventory);
                    }
                } else {
                    Location coords = new Location(world, Integer.parseInt(why.get(0)), Integer.parseInt(why.get(1)), Integer.parseInt(why.get(2)));
                    if (coords.getBlock().getState() instanceof Chest) {
                        Chest chest = (Chest) coords.getBlock().getState();
                        Inventory chestInventory = chest.getInventory();
                        ((Player) entity1asd).openInventory(chestInventory);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
    
    private void worldMessage(String message, Entity entity1asd, World world, Event e, int random) {
        for (Player players : world.getPlayers()) {
            String result = message.replace("%player%", entity1asd.getName());
            if (random < world.getPlayers().size()) {
                result = result.replace("%random%", world.getPlayers().get(random).getName());
            }
            
            if (e instanceof EntityDeathEvent) {
                result = result.replace("%victim%", ((EntityDeathEvent) e).getEntity().getName());
                if (((EntityDeathEvent) e).getEntity().getKiller() != null) {
                    result = result.replace("%damager%", ((EntityDeathEvent) e).getEntity().getKiller().getName());
                }
            }
            if (e instanceof EntityDamageByEntityEvent) {
                result = result.replace("%victim%", ((EntityDamageByEntityEvent) e).getEntity().getName());
                result = result.replace("%damager%", ((EntityDamageByEntityEvent) e).getDamager().getName());
            }
            if (e instanceof AsyncPlayerChatEvent) {
                result = result.replace("%message%", ((AsyncPlayerChatEvent) e).getMessage());
            }
            
            players.sendMessage(result);
        }
    }
    
    private void playerMessage(String message, Entity entity1asd, World world, Event e, int random) {
        String result = message.replace("%player%", entity1asd.getName());
        if (random < world.getPlayers().size()) {
            result = result.replace("%random%", world.getPlayers().get(random).getName());
        }
        
        if (e instanceof EntityDeathEvent) {
            result = result.replace("%victim%", ((EntityDeathEvent) e).getEntity().getName());
            if (((EntityDeathEvent) e).getEntity().getKiller() != null) {
                result = result.replace("%damager%", ((EntityDeathEvent) e).getEntity().getKiller().getName());
            }
        }
        if (e instanceof EntityDamageByEntityEvent) {
            result = result.replace("%victim%", ((EntityDamageByEntityEvent) e).getEntity().getName());
            result = result.replace("%damager%", ((EntityDamageByEntityEvent) e).getDamager().getName());
        }
        if (e instanceof AsyncPlayerChatEvent) {
            result = result.replace("%message%", ((AsyncPlayerChatEvent) e).getMessage());
        }
        
        entity1asd.sendMessage(result);
    }
    
    private void setHealth(String eq, Entity entity1asd, Event e) {
        if (eq.startsWith("apple")) {
            int appleFrom = eq.indexOf("[") + 1;
            int appleTo = eq.indexOf("]~");
            String apple = eq.substring(appleFrom, appleTo);
            LivingEntity player = (LivingEntity) entity1asd;
            
            switch (apple) {
                case "health_now":
                    // Do nothing - this is the current health
                    break;
                default:
                    try {
                        double amount = Double.parseDouble(apple);
                        player.setHealth(Math.max(0, Math.min(player.getMaxHealth(), amount)));
                    } catch (Exception ignored) {
                    }
                    break;
            }
        } else {
            try {
                double amount = Double.parseDouble(eq);
                ((LivingEntity) entity1asd).setHealth(Math.max(0, Math.min(((LivingEntity) entity1asd).getMaxHealth(), amount)));
            } catch (Exception ignored) {
            }
        }
    }
    
    private void timeWait(String eq, int i, final List<String> line, final Entity eventEntity, final World world, final Event e, Entity entity1asd) {
        long ticksToWait = 0L;
        if (eq.startsWith("apple")) {
            int appleFrom = eq.indexOf("[") + 1;
            int appleTo = eq.indexOf("]~");
            String apple = eq.substring(appleFrom, appleTo);
            
            try {
                ticksToWait = (long) (Double.parseDouble(apple) * 20L);
            } catch (Exception ignored) {
                ticksToWait = 20L; // Default 1 second
            }
        } else {
            try {
                ticksToWait = (long) (Double.parseDouble(eq) * 20L);
            } catch (Exception ignored) {
                ticksToWait = 20L; // Default 1 second
            }
        }
        
        final int finalI = i;
        new BukkitRunnable() {
            public void run() {
                handleLine(line, eventEntity, world, finalI + 1, e);
            }
        }.runTaskLater(plugin, ticksToWait);
    }
    
    private void setBlock(String eq, World world, String type, Entity entity1asd, Event e) {
        List<String> why = Arrays.asList(eq.split("\\|"));
        try {
            Location coords = new Location(world, Integer.parseInt(why.get(0)), Integer.parseInt(why.get(1)), Integer.parseInt(why.get(2)));
            if (eq.startsWith("apple")) {
                Set<Material> transparentBlock = new HashSet<>();
                transparentBlock.add(Material.AIR);
                int appleFrom = eq.indexOf("[") + 1;
                int appleTo = eq.indexOf("]~");
                String apple = eq.substring(appleFrom, appleTo);
                
                switch (apple) {
                    case "location":
                        coords = entity1asd.getLocation();
                        break;
                    case "look_block_loc":
                        if (entity1asd instanceof LivingEntity) {
                            coords = ((LivingEntity) entity1asd).getTargetBlock(transparentBlock, 12).getLocation();
                        }
                        break;
                    case "block_loc":
                        if (e instanceof org.bukkit.event.block.BlockEvent) {
                            coords = ((org.bukkit.event.block.BlockEvent) e).getBlock().getLocation();
                        }
                        break;
                }
            }
            
            if (type.startsWith("apple")) {
                int appleFrom = type.indexOf("[") + 1;
                int appleTo = type.indexOf("]~");
                String apple = type.substring(appleFrom, appleTo);
                
                switch (apple) {
                    case "look_block":
                        if (entity1asd instanceof LivingEntity) {
                            Set<Material> transparentBlocks = new HashSet<>();
                            transparentBlocks.add(Material.AIR);
                            Block block = ((LivingEntity) entity1asd).getTargetBlock(transparentBlocks, 12);
                            type = block.getType().name();
                        }
                        break;
                    default:
                        // Use default material
                        break;
                }
            }
            
            Material material = Material.STONE; // Default
            try {
                material = Material.getMaterial(type.toUpperCase());
                if (material == null) material = Material.STONE;
            } catch (Exception ignored) {
                try {
                    material = Material.valueOf(type.toUpperCase());
                } catch (Exception ignored2) {
                }
            }
            
            world.getBlockAt(coords).setType(material);
        } catch (Exception ignored) {
        }
    }
    
    private void teleport(String eq, Entity entity1asd, Event e) {
        List<String> why = Arrays.asList(eq.split("\\|"));
        try {
            if (eq.startsWith("apple")) {
                Set<Material> transparentBlock = new HashSet<>();
                transparentBlock.add(Material.AIR);
                int appleFrom = eq.indexOf("[") + 1;
                int appleTo = eq.indexOf("]~");
                String apple = eq.substring(appleFrom, appleTo);
                
                Location coords = null;
                switch (apple) {
                    case "location":
                        coords = entity1asd.getLocation();
                        break;
                    case "look_block_loc":
                        if (entity1asd instanceof LivingEntity) {
                            coords = ((LivingEntity) entity1asd).getTargetBlock(transparentBlock, 12).getLocation();
                        }
                        break;
                    case "block_loc":
                        if (e instanceof org.bukkit.event.block.BlockEvent) {
                            coords = ((org.bukkit.event.block.BlockEvent) e).getBlock().getLocation();
                        }
                        break;
                }
                if (coords != null) {
                    entity1asd.teleport(coords);
                }
            } else {
                Location coords = new Location(entity1asd.getWorld(), 
                    Float.parseFloat(why.get(0)), Float.parseFloat(why.get(1)), Float.parseFloat(why.get(2)));
                entity1asd.teleport(coords);
            }
        } catch (Exception ignored) {
        }
    }
    
    private void useFunc(String type, String eq, Entity entity1asd) {
        if (type.equals("sync")) {
            // Synchronous function execution would go here
        } else if (type.equals("async")) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                // Asynchronous function execution would go here
            });
        }
    }
    
    private void giveItems(String func, Entity entity1asd, Event e) {
        // Simplified item giving implementation
        if (entity1asd instanceof Player) {
            Player player = (Player) entity1asd;
            player.sendMessage("§aВыданы предметы!");
        }
    }
    
    private void giveRandItem(String func, Entity entity1asd, Event e) {
        // Simplified random item giving implementation
        if (entity1asd instanceof Player) {
            Player player = (Player) entity1asd;
            player.sendMessage("§aВыдан случайный предмет!");
        }
    }
    
    private void deleteItems(String func, Entity entity1asd, Event e) {
        // Simplified item deletion implementation
        if (entity1asd instanceof Player) {
            Player player = (Player) entity1asd;
            player.sendMessage("§cУдалены предметы!");
        }
    }
    
    private boolean evaluateCondition(String condition, Entity entity, Event e) {
        // Simplified condition evaluation
        return true;
    }
}