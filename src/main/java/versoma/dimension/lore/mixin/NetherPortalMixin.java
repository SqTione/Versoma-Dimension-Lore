package versoma.dimension.lore.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.creaking.CreakingAi;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import versoma.dimension.lore.boundary.WorldBoundary;

@Mixin(NetherPortalBlock.class)
public class NetherPortalMixin {
    @Redirect(
            method = "getPortalDestination",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;clampToBounds(DDD)Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos redirectClampToBounds(WorldBorder worldBorder, double x, double y, double z) {
        BlockPos vanilla = worldBorder.clampToBounds(x, y, z);

        double dist = WorldBoundary.distanceFromCenter(vanilla.getX(), vanilla.getZ());
        if (dist <= WorldBoundary.EFFECTS_START) return vanilla;

        double safeRadius = WorldBoundary.EFFECTS_START - 16.0;
        double scale = safeRadius / dist;

        return BlockPos.containing(vanilla.getX() * scale, vanilla.getY(), vanilla.getZ() * scale);
    }
}