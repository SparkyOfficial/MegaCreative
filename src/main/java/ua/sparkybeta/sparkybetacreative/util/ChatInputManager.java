package ua.sparkybeta.sparkybetacreative.util;

import ua.sparkybeta.sparkybetacreative.worlds.SparkyWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

public class ChatInputManager {

    public record PendingInput(SparkyWorld world, InputType type) {}
    private final Map<UUID, PendingInput> pendingInputs = new HashMap<>();

    public void startInput(UUID playerUUID, SparkyWorld world, InputType type) {
        pendingInputs.put(playerUUID, new PendingInput(world, type));
    }

    public Optional<PendingInput> getPendingInput(UUID playerUUID) {
        return Optional.ofNullable(pendingInputs.get(playerUUID));
    }

    public void finishInput(UUID playerUUID) {
        pendingInputs.remove(playerUUID);
    }
} 