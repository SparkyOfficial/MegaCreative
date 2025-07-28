package ua.sparkybeta.sparkybetacreative.worlds;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ua.sparkybeta.sparkybetacreative.coding.block.CodeBlock;
import ua.sparkybeta.sparkybetacreative.coding.models.Argument;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class StoredCodeBlock {
    private final UUID id;
    private transient Location location;
    private final String worldName;
    private final int x, y, z;
    private CodeBlock type;
    private final List<Argument> arguments = new ArrayList<>();
    private final List<UUID> nextBlocks = new ArrayList<>(); // IDs of the next blocks in the chain

    public StoredCodeBlock(Location location, CodeBlock type) {
        this.id = UUID.randomUUID();
        this.location = location;
        this.worldName = location.getWorld().getName();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.type = type;
    }
    
    public Location getLocation() {
        if (location == null) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                location = new Location(world, x, y, z);
            }
        }
        return location;
    }
} 