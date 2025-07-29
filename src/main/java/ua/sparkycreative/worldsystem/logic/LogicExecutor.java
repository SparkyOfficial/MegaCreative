package ua.sparkycreative.worldsystem.logic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.List;

public class LogicExecutor {
    private final LogicBlockManager manager;

    public LogicExecutor(LogicBlockManager manager) {
        this.manager = manager;
    }

    // Запуск логики с EVENT-блока
    public void triggerEvent(World world, LogicBlock eventBlock, Player player) {
        executeChain(world, eventBlock, player);
    }

    private void executeChain(World world, LogicBlock block, Player player) {
        for (Location out : block.getOutputs()) {
            LogicBlock next = manager.getBlocks(world).stream()
                .filter(b -> b.getLocation().equals(out))
                .findFirst().orElse(null);
            if (next == null) continue;
            switch (next.getType()) {
                case IF -> {
                    if (checkCondition(next, player)) {
                        executeChain(world, next, player);
                    }
                }
                case ACTION -> {
                    doAction(next, player);
                    executeChain(world, next, player);
                }
                case EVENT -> {
                    // Игнорировать, чтобы не было циклов
                }
                default -> {}
            }
        }
    }

    private boolean checkCondition(LogicBlock block, Player player) {
        String cond = (String) block.getParams().getOrDefault("condition", "игрок на земле");
        return switch (cond) {
            case "игрок на земле" -> player.isOnGround();
            case "игрок в воздухе" -> !player.isOnGround();
            case "игрок в воде" -> player.getLocation().getBlock().isLiquid();
            default -> true;
        };
    }

    private void doAction(LogicBlock block, Player player) {
        String act = (String) block.getParams().getOrDefault("action", "выдать алмаз");
        switch (act) {
            case "выдать алмаз" -> player.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND));
            case "поджечь" -> player.setFireTicks(60);
            case "подпрыгнуть" -> player.setVelocity(player.getVelocity().setY(1));
        }
    }
} 