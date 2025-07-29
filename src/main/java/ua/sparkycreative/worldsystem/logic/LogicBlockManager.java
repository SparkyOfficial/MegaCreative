package ua.sparkycreative.worldsystem.logic;

import org.bukkit.Location;
import org.bukkit.World;
import java.util.*;

public class LogicBlockManager {
    // Карта: имя dev-мира -> список логических блоков
    private final Map<String, List<LogicBlock>> logicBlocks = new HashMap<>();

    public void addLogicBlock(World world, LogicBlock block) {
        if (!world.getName().endsWith("_dev")) return;
        logicBlocks.computeIfAbsent(world.getName(), k -> new ArrayList<>()).add(block);
    }

    public void removeLogicBlock(World world, Location loc) {
        List<LogicBlock> list = logicBlocks.get(world.getName());
        if (list == null) return;
        list.removeIf(b -> b.getLocation().equals(loc));
    }

    public List<LogicBlock> getBlocks(World world) {
        return logicBlocks.getOrDefault(world.getName(), Collections.emptyList());
    }

    public List<LogicBlock> getBlocksOfType(World world, LogicBlockType type) {
        List<LogicBlock> result = new ArrayList<>();
        for (LogicBlock b : getBlocks(world)) {
            if (b.getType() == type) result.add(b);
        }
        return result;
    }
} 