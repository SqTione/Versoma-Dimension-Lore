package versoma.dimension.lore.client.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import versoma.dimension.lore.client.controller.ControllerGazeState;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    private long lastTime = System.nanoTime();

    @Inject(method = "render", at = @At("HEAD"))
    private void applyGazePerFrame(net.minecraft.client.DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastTime) / 1_000_000_000.0f;

        lastTime = currentTime;

        float zoomSpeed = 1.0f - (float)Math.exp(-4.0f * deltaTime);
        ControllerGazeState.currentFovMod += (ControllerGazeState.targetFovMod - ControllerGazeState.currentFovMod) * zoomSpeed;

        if (!ControllerGazeState.isControlled) return;

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null || player.isRemoved()) {
            ControllerGazeState.reset();
            return;
        }

        float factor = 1.0f - (float)Math.exp(-ControllerGazeState.speedFactor * deltaTime);

        player.setYRot(lerpAngle(player.getYRot(), ControllerGazeState.targetYaw, factor));
        player.setXRot(lerpAngle(player.getXRot(), ControllerGazeState.targetPitch, factor));
    }

    private float lerpAngle(float current, float target, float factor) {
        float delta = target - current;
        while (delta > 180f)  delta -= 360f;
        while (delta < -180f) delta += 360f;

        if (Math.abs(delta) < 0.1f) return target;

        return current + delta * factor;
    }
}