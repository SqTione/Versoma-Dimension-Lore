package versoma.dimension.lore.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class AnxietyEffect extends MobEffect {
    public final static float MOVING_THRESHOLD = 0.001f;

    public AnxietyEffect() {
        super(MobEffectCategory.HARMFUL, 0x3A404A);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && !player.level().isClientSide()) {
            double dx = player.getX() - player.xOld;
            double dy = player.getY() - player.yOld;
            double dz = player.getZ() - player.zOld;

            boolean isMoving = (dx * dx + dy * dy + dz * dz) > MOVING_THRESHOLD;

            int interval = Math.max(20, 200 - (amplifier * 20));

            if (isMoving && player.tickCount % interval == 0) {
                var foodData = player.getFoodData();
                int currentFood = foodData.getFoodLevel();

                if (currentFood > 0) {
                    foodData.setFoodLevel(currentFood - 1);
                }
            }
        }
        return super.applyEffectTick(level, entity, amplifier);
    }
}