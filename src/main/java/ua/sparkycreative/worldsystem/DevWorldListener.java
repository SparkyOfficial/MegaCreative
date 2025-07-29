package ua.sparkycreative.worldsystem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.Location;
import ua.sparkycreative.worldsystem.logic.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Set;
import ua.sparkycreative.worldsystem.logic.LogicBlockConfigGUI;
import ua.sparkycreative.worldsystem.logic.LogicExecutor;

public class DevWorldListener implements Listener {
    // Здесь будут разрешённые "логические" блоки
    private static final Set<Material> LOGIC_BLOCKS = Set.of(
            Material.REPEATER, Material.COMPARATOR, Material.REDSTONE_TORCH, Material.REDSTONE_BLOCK
            // TODO: добавить свои блоки
    );

    private static final LogicBlockManager LOGIC_MANAGER = new LogicBlockManager();
    private static final LogicExecutor EXECUTOR = new LogicExecutor(LOGIC_MANAGER);

    public DevWorldListener() {
        Bukkit.getPluginManager().registerEvents(this, WorldSystemPlugin.getInstance());
    }

    private boolean isDevWorld(World world) {
        return world.getName().endsWith("_dev");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!isDevWorld(e.getBlock().getWorld())) return;
        if (!LOGIC_BLOCKS.contains(e.getBlock().getType())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cВ dev-мире можно ставить только логические блоки!");
        } else {
            // Добавить LogicBlock
            LogicBlockType type = materialToType(e.getBlock().getType());
            if (type != null) {
                LogicBlock block = new LogicBlock(type, e.getBlock().getLocation());
                LOGIC_MANAGER.addLogicBlock(e.getBlock().getWorld(), block);
                e.getPlayer().sendMessage("§aЛогический блок добавлен: " + type);
            }
        }
    }

    private LogicBlockType materialToType(Material mat) {
        return switch (mat) {
            case REPEATER -> LogicBlockType.IF;
            case COMPARATOR -> LogicBlockType.ACTION;
            case REDSTONE_TORCH -> LogicBlockType.EVENT;
            case REDSTONE_BLOCK -> LogicBlockType.SET;
            default -> null;
        };
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        if (!isDevWorld(e.getPlayer().getWorld())) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;
        if (!LOGIC_BLOCKS.contains(e.getClickedBlock().getType())) return;
        // Проверка режима связывания
        LogicBlock linking = LogicBlockConfigGUI.getLinking(e.getPlayer());
        if (linking != null) {
            e.setCancelled(true);
            LogicBlock target = LOGIC_MANAGER.getBlocks(e.getPlayer().getWorld()).stream()
                .filter(b -> b.getLocation().equals(e.getClickedBlock().getLocation()))
                .findFirst().orElse(null);
            if (target != null && !target.getLocation().equals(linking.getLocation())) {
                if (linking.getOutputs().contains(target.getLocation())) {
                    linking.getOutputs().remove(target.getLocation());
                    e.getPlayer().sendMessage("§cСвязь удалена!");
                } else {
                    linking.getOutputs().add(target.getLocation());
                    e.getPlayer().sendMessage("§aСвязь создана!");
                }
            } else {
                e.getPlayer().sendMessage("§cНельзя связать блок сам с собой!");
            }
            LogicBlockConfigGUI.clearLinking(e.getPlayer());
            return;
        }
        // Открыть GUI настройки блока
        LogicBlockType type = materialToType(e.getClickedBlock().getType());
        if (type != null) {
            LogicBlock block = LOGIC_MANAGER.getBlocks(e.getPlayer().getWorld()).stream()
                .filter(b -> b.getLocation().equals(e.getClickedBlock().getLocation()))
                .findFirst().orElse(null);
            if (block != null) {
                if (block.getType() == ua.sparkycreative.worldsystem.logic.LogicBlockType.EVENT) {
                    // Если EVENT и условие "по клику" — запуск логики
                    String eventType = (String) block.getParams().getOrDefault("event", "по клику");
                    if ("по клику".equals(eventType)) {
                        EXECUTOR.triggerEvent(e.getPlayer().getWorld(), block, e.getPlayer());
                        e.getPlayer().sendMessage("§aЛогика EVENT-блока запущена!");
                        return;
                    }
                }
                new ua.sparkycreative.worldsystem.logic.LogicBlockConfigGUI(block).open(e.getPlayer());
            } else {
                e.getPlayer().sendMessage("§cБлок не найден в системе логики!");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!isDevWorld(e.getBlock().getWorld())) return;
        if (!LOGIC_BLOCKS.contains(e.getBlock().getType())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cВ dev-мире можно ломать только логические блоки!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!isDevWorld(e.getPlayer().getWorld())) return;
        for (ua.sparkycreative.worldsystem.logic.LogicBlock block : LOGIC_MANAGER.getBlocksOfType(e.getPlayer().getWorld(), ua.sparkycreative.worldsystem.logic.LogicBlockType.EVENT)) {
            String eventType = (String) block.getParams().getOrDefault("event", "по клику");
            if ("при входе".equals(eventType)) {
                EXECUTOR.triggerEvent(e.getPlayer().getWorld(), block, e.getPlayer());
                e.getPlayer().sendMessage("§aЛогика EVENT-блока 'при входе' запущена!");
            }
        }
    }

    public static LogicBlockManager getLogicManager() {
        return LOGIC_MANAGER;
    }
    public static LogicExecutor getExecutor() {
        return EXECUTOR;
    }
} 