package versoma.dimension.lore.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class RotEffect extends MobEffect {
    public RotEffect() {
        super(MobEffectCategory.HARMFUL, 0x4A5D23);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}