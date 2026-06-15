package versoma.dimension.lore.boundary;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;

public final class WorldBoundary {

    public static final double EFFECTS_START = 3750.0;
    public static final double DAMAGE_START  = 3900.0;
    public static final double INSTANT_DEATH = 4250.0;

    private WorldBoundary() {}

    public static double distanceFromCenter(double x, double z) {
        double dx = x;
        double dz = z;

        return Math.sqrt(dx * dx + dz * dz);
    }
}