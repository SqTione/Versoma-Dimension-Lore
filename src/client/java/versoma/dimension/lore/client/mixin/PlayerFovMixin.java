package versoma.dimension.lore.client.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import versoma.dimension.lore.client.controller.ControllerGazeState;

@Mixin(AbstractClientPlayer.class)
public class PlayerFovMixin {

    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    private void applyGazeZoom(CallbackInfoReturnable<Float> cir) {
        if (ControllerGazeState.currentFovMod < 0.99f) {
            float original = cir.getReturnValue();
            cir.setReturnValue(original * ControllerGazeState.currentFovMod);
        }
    }
}