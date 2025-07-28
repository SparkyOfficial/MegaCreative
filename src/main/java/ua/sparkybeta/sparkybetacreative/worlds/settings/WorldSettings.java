package ua.sparkybeta.sparkybetacreative.worlds.settings;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class WorldSettings {

    private boolean isPublic = true;

    // Flags
    private boolean pvpAllowed = false;
    private boolean explosionsAllowed = false;
    private boolean mobSpawningAllowed = true;

    // Trusted players
    private Set<UUID> buildTrusted = new HashSet<>();
    private Set<UUID> codeTrusted = new HashSet<>();

} 