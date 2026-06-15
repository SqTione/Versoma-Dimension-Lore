package versoma.dimension.lore.shadow;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ShadowCreakingTracker {

    private static final double PROXIMITY_DIST_SQ = 12.0 * 12.0;
    private static final double LOS_COS_THRESHOLD = Math.cos(Math.toRadians(30.0));

    public static void tick(net.minecraft.server.MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        for (ServerPlayer player : overworld.players()) {
            UUID assigned = ShadowCreakingManager.get().getAssigned(player.getUUID());
            if (assigned == null) continue;

            Entity entity = overworld.getEntity(assigned);
            if (!(entity instanceof Creaking creaking)) {
                ShadowCreakingManager.get().unassign(player.getUUID());
                continue;
            }

            if (shouldDespawn(player, creaking)) {
                despawnWithParticles(creaking, overworld);
                ShadowCreakingManager.get().onShadowRemoved(assigned);
            }
        }
    }

    private static boolean shouldDespawn(ServerPlayer player, Creaking creaking) {
        // Proximity
        if (player.distanceToSqr(creaking) < PROXIMITY_DIST_SQ) return true;

        // Line of sight
        Vec3 toCreaking = creaking.getEyePosition()
                .subtract(player.getEyePosition())
                .normalize();
        Vec3 lookVec = player.getLookAngle();
        double dot = lookVec.dot(toCreaking);

        if (dot < LOS_COS_THRESHOLD) return false;

        // Raycast — нет ли стены между игроком и скрипуном
        var hit = overworld_raycast(player, creaking);
        return hit;
    }

    private static boolean overworld_raycast(ServerPlayer player, Creaking creaking) {
        // Используем прямую проверку через уровень
        var level = (ServerLevel) player.level();
        var from = player.getEyePosition();
        var to = creaking.getEyePosition();

        var result = level.clip(new net.minecraft.world.level.ClipContext(
                from, to,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player
        ));

        return result.getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    private static void despawnWithParticles(Creaking creaking, ServerLevel level) {
        Vec3 pos = creaking.position();
        level.sendParticles(
                ParticleTypes.POOF,
                pos.x, pos.y + 1.0, pos.z,
                12, 0.3, 0.5, 0.3, 0.05
        );
        creaking.discard();
    }
}