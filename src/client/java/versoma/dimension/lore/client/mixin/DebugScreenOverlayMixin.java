package versoma.dimension.lore.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
    @Inject(method = "extractLines", at = @At("HEAD"))
    private void filterCoordinatesFromDebugScreen(GuiGraphicsExtractor graphics, List<String> lines, boolean alignLeft, CallbackInfo ci) {
        if (lines == null || lines.isEmpty() || !alignLeft) {
            return;
        }

        lines.removeIf(this::isPositionalData);
    }

    private boolean isPositionalData(String line) {
        if (line == null || line.isBlank()) {
            return false;
        }

        return line.startsWith("XYZ:")
                || line.startsWith("Block:")
                || line.startsWith("Chunk:")
                || line.startsWith("Facing:");
    }
}