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

    @Inject(method = "tick", at = @At("TAIL"))
    private void pullGaze(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer)(Object)this;

        // Строим AABB вокруг игрока с радиусом 64 блока
        AABB searchBox = self.getBoundingBox().inflate(64.0);

        // Получаем только нужный класс сущностей в этой зоне
        List<ControllerCreakingEntity> controllers = self.level().getEntitiesOfClass(ControllerCreakingEntity.class, searchBox);

        ControllerCreakingEntity closestController = null;
        double maxDistSqr = 64.0 * 64.0;

        for (ControllerCreakingEntity c : controllers) {
            int phase = c.getPhase();
            if (phase != 1 && phase != 2) continue;

            double dist = self.distanceToSqr(c);
            if (dist < maxDistSqr) {
                maxDistSqr = dist;
                closestController = c;
            }
        }

        if (closestController == null) return;

        // Вычисляем угол к контролёру
        Vec3 toController = closestController.getEyePosition().subtract(self.getEyePosition());
        double horizontalDist = Math.sqrt(toController.x * toController.x + toController.z * toController.z);

        float targetYaw = (float)(Math.toDegrees(Math.atan2(toController.z, toController.x))) - 90f;
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(toController.y, horizontalDist)));

        float speedFactor = closestController.getPhase() == 1 ? 0.15f : 0.4f;

        self.setYRot(lerpAngle(self.getYRot(), targetYaw, speedFactor));
        self.setXRot(lerpAngle(self.getXRot(), targetPitch, speedFactor));

        // Блокируем повороты мыши
        ControllerGazeState.isControlled = true;
    }

    private float lerpAngle(float current, float target, float factor) {
        float delta = target - current;

        while (delta > 180f)  delta -= 360f;
        while (delta < -180f) delta += 360f;

        // Если угол почти совпал — прилипаем намертво, чтобы не было микро-тремора float'ов
        if (Math.abs(delta) < 0.5f) {
            return target;
        }

        return current + delta * factor;
    }
}