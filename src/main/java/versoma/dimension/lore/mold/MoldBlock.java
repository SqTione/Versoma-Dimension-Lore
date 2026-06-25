package versoma.dimension.lore.mold;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import versoma.dimension.lore.registry.ModEffectsRegistry;

public class MoldBlock extends MultifaceSpreadeableBlock {

    public static final MapCodec<MoldBlock> CODEC = simpleCodec(MoldBlock::new);
    private final MultifaceSpreader spreader = new MultifaceSpreader(new MultifaceSpreader.DefaultSpreaderConfig(this));

    public MoldBlock(BlockBehaviour.Properties properties) {
       super(properties);
   }

   @Override
   public MapCodec<MoldBlock> codec() {
        return CODEC;
   }

   @Override
   public MultifaceSpreader getSpreader() {
        return this.spreader;
   }

    @Override
    public void entityInside(final BlockState state, final Level level, final BlockPos pos, final Entity entity, final InsideBlockEffectApplier effectApplier, final boolean isPrecise) {
        if (level.isClientSide()) {
            super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
            return;
        }

        if (entity instanceof LivingEntity livingEntity) {
            if (level.getGameTime() % 20 == 0) {
                applyRotBasedOnDensity(level, pos, livingEntity);
            }
        }

        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
    }

    private void applyRotBasedOnDensity(Level level, BlockPos pos, LivingEntity entity) {
        int radius = 2;
        int moldCount = 0;

        for (BlockPos currentPos : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
            if (level.getBlockState(currentPos).is(this)) {
                moldCount++;
            }
        }

        int durationTicks;
        if (moldCount >= 15) {
            durationTicks = -1;
        } else if (moldCount >= 5) {
            durationTicks = 1200;
        } else {
            durationTicks = 200;
        }

        entity.addEffect(new MobEffectInstance(ModEffectsRegistry.ROT, durationTicks, 0, false, true, true));
    }
}