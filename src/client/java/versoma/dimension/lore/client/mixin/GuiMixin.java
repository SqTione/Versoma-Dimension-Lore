package versoma.dimension.lore.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import versoma.dimension.lore.VersomaDimensionLore;
import versoma.dimension.lore.registry.ModEffectsRegistry;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Unique
    private static final String HEART_PREFIX = "hud/heart/";

    @ModifyArg(
            method = "extractHeart",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"
            ),
            index = 1
    )
    private Identifier versoma$modifyHeartSprite(Identifier originalSprite) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.hasEffect(ModEffectsRegistry.ROT)) {
            return originalSprite;
        }

        String path = originalSprite.getPath();

        if (path.startsWith(HEART_PREFIX) && !path.contains("container")) {
            if (path.contains("full")) {
                return Identifier.fromNamespaceAndPath(VersomaDimensionLore.MOD_ID, "hud/heart/rot_full");
            } else if (path.contains("half")) {
                return Identifier.fromNamespaceAndPath(VersomaDimensionLore.MOD_ID, "hud/heart/rot_half");
            }
        }

        return originalSprite;
    }
}