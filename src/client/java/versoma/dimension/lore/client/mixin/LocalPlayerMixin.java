package versoma.dimension.lore.client.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import versoma.dimension.lore.client.controller.ControllerGazeState;
import versoma.dimension.lore.controller.ControllerCreakingEntity;

import java.util.List;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    private final double MAX_DISTANCE = 64.0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void calculateGazeTarget(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer)(Object)this;
        AABB searchBox = self.getBoundingBox().inflate(64.0);

        List<ControllerCreakingEntity> controllers = self.level().getEntitiesOfClass(ControllerCreakingEntity.class, searchBox);
        ControllerCreakingEntity closest = null;
        double maxDistSqr = this.MAX_DISTANCE * this.MAX_DISTANCE;

        for (ControllerCreakingEntity c : controllers) {
            int phase = c.getPhase();
            if (phase != 1 && phase != 2) continue;
            double dist = self.distanceToSqr(c);
            if (dist < maxDistSqr) {
                maxDistSqr = dist;
                closest = c;
            }
        }

        if (closest == null) {
            ControllerGazeState.reset();
            return;
        }

        Vec3 toController = closest.getEyePosition().subtract(self.getEyePosition());
        double horizontalDist = Math.sqrt(toController.x * toController.x + toController.z * toController.z);

        ControllerGazeState.targetYaw = (float)(Math.toDegrees(Math.atan2(toController.z, toController.x))) - 90f;
        ControllerGazeState.targetPitch = (float)(-Math.toDegrees(Math.atan2(toController.y, horizontalDist)));
        ControllerGazeState.speedFactor = closest.getPhase() == 1 ? 5.0f : 15.0f;
        ControllerGazeState.isControlled = true;
    }
}