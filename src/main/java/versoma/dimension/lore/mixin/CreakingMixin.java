package versoma.dimension.lore.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import versoma.dimension.lore.registry.ModItemsRegistry;
import versoma.dimension.lore.shadow.ShadowCreakingManager;

@Mixin(Creaking.class)
public class CreakingMixin {
    private static final float TURN_SPEED = 5.0F;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Creaking self = (Creaking) (Object) this;

        if (!self.level().isClientSide() && ShadowCreakingManager.isShadow(self)) {
            Player player = self.level().getNearestPlayer(self, 128.0);

            if (player != null) {
                // Вычисляем целевой угол
                double dX = player.getX() - self.getX();
                double dZ = player.getZ() - self.getZ();
                float targetYaw = (float) (Math.atan2(dZ, dX) * (180.0 / Math.PI)) - 90.0F;

                // Плавно шагаем от текущего угла к целевому
                float currentYaw = self.getYHeadRot();
                float newYaw = Mth.approachDegrees(currentYaw, targetYaw, TURN_SPEED);

                // Применяем новый угол
                self.setYRot(newYaw);
                self.setYHeadRot(newYaw);
                self.setYBodyRot(newYaw);
            }
        }
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void preventDamage(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        Creaking self = (Creaking)(Object)this;
        if (ShadowCreakingManager.isShadow(self)) {
            cir.cancel();
        }
    }

    @Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
    private void onServerAiStep(CallbackInfo ci) {
        Creaking self = (Creaking) (Object) this;

        if (ShadowCreakingManager.isShadow(self)) {
            ci.cancel();
        }
    }
}