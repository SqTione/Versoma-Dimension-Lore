package versoma.dimension.lore.shadow;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class ShadowCreakingSpawner {

    private static final double SPAWN_MIN_DIST = 48.0;
    private static final double SPAWN_MAX_DIST = 60.0;
    private static final float SPAWN_CHANCE = 0.02f;
    private static final int CHECK_INTERVAL = 100;

    private static final Random random = new Random();
    private static int tickCounter = 0;

    public static void tick(net.minecraft.server.MinecraftServer server) {
        if (server == null) return;
        if (++tickCounter % CHECK_INTERVAL != 0) return;

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);

        if (overworld == null || !overworld.isDarkOutside()) return;

        for (ServerPlayer player : overworld.players()) {
            if (player == null) continue;
            if (ShadowCreakingManager.get().hasAssignment(player.getUUID())) continue;
            if (random.nextFloat() > SPAWN_CHANCE) continue;

            trySpawnFor(player, overworld);
        }
    }

    private static void trySpawnFor(ServerPlayer player, ServerLevel level) {
        double angle = random.nextDouble() * 2 * Math.PI;
        double dist = SPAWN_MIN_DIST + random.nextDouble() * (SPAWN_MAX_DIST - SPAWN_MIN_DIST);

        double x = player.getX() + Math.cos(angle) * dist;
        double z = player.getZ() + Math.sin(angle) * dist;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);

        if (y <= level.getMinY()) return;

        Creaking creaking = EntityType.CREAKING.create(level, EntitySpawnReason.COMMAND);
        if (creaking == null) return;

        creaking.setPos(new Vec3(x, y, z));

        level.addFreshEntity(creaking);
        ShadowCreakingManager.get().assign(player.getUUID(), creaking.getUUID());
    }
}