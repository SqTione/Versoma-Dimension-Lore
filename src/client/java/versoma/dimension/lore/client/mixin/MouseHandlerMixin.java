package versoma.dimension.lore.client.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import versoma.dimension.lore.client.controller.ControllerGazeState;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void blockMouseLook(CallbackInfo ci) {
        if (ControllerGazeState.isControlled) {
            ControllerGazeState.isControlled = false; // сбрасываем каждый тик
            ci.cancel();
        }
    }
}