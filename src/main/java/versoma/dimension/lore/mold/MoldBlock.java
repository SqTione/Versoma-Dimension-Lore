package versoma.dimension.lore.mold;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

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
    public void entityInside(
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final Entity entity,
            final InsideBlockEffectApplier effectApplier,
            final boolean isPrecise
    ) {
        if (level.isClientSide()) {
            super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
            return;
        }

        if (entity instanceof LivingEntity livingEntity) {
            // TODO: Add effect
        }

        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
    }
}