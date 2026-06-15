package versoma.dimension.lore.shadow;

import net.minecraft.world.entity.monster.creaking.Creaking;
import versoma.dimension.lore.sleep.SleepParalysisHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShadowCreakingManager {
    public static final String SHADOW_TAG = "shadow_creaking";

    private final Map<UUID, UUID> assignments = new HashMap<>();
    private static ShadowCreakingManager instance;

    public static ShadowCreakingManager get() {
        if (instance == null) {
            instance = new ShadowCreakingManager();
        }
        return instance;
    }

    public static boolean isShadow(Creaking entity) {
        if (entity == null) return false;
        var tags = entity.entityTags();
        return tags.contains(SHADOW_TAG) || tags.contains(SleepParalysisHandler.SLEEP_CREAKING_TAG);
    }

    public boolean hasAssignment(UUID playerUUID) {
        if (playerUUID == null) return false;
        return assignments.containsKey(playerUUID);
    }

    public void assign(UUID playerUUID, UUID creakingUUID) {
        if (playerUUID == null || creakingUUID == null) return;
        assignments.put(playerUUID, creakingUUID);
    }

    public void unassign(UUID playerUUID) {
        if (playerUUID == null) return;
        assignments.remove(playerUUID);
    }

    public UUID getAssigned(UUID playerUUID) {
        if (playerUUID == null) return null;
        return assignments.get(playerUUID);
    }

    public void onShadowRemoved(UUID creakingUUID) {
        if (creakingUUID == null) return;
        assignments.values().removeIf(id -> id.equals(creakingUUID));
    }
}