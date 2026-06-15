package versoma.dimension.lore.boundary;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

public final class BoundaryEffectHandler {

    private static final int EFFECT_DURATION_TICKS = 60;

    private BoundaryEffectHandler() {}

    public static void tick(ServerPlayer player) {
        if (!player.level().dimension().equals(Level.OVERWORLD)) return;

        double dist = WorldBoundary.distanceFromCenter(player.getX(), player.getZ());

        if (dist >= WorldBoundary.INSTANT_DEATH) {
            player.kill((net.minecraft.server.level.ServerLevel) player.level());
            return;
        }

        if (dist >= WorldBoundary.DAMAGE_START) {
            player.hurt(player.damageSources().outOfBorder(), 4.0f);
            applyEffect(player, new MobEffectInstance(MobEffects.NAUSEA, EFFECT_DURATION_TICKS, 1, false, false));
            applyEffect(player, new MobEffectInstance(MobEffects.DARKNESS, EFFECT_DURATION_TICKS, 1, false, false));
            return;
        }

        if (dist >= WorldBoundary.EFFECTS_START) {
            applyEffect(player, new MobEffectInstance(MobEffects.NAUSEA, EFFECT_DURATION_TICKS, 0, false, false));
            applyEffect(player, new MobEffectInstance(MobEffects.DARKNESS, EFFECT_DURATION_TICKS, 0, false, false));
        }
    }

    private static void applyEffect(ServerPlayer player, MobEffectInstance effect) {
        player.addEffect(effect);
    }
}