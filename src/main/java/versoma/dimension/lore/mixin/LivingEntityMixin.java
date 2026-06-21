package versoma.dimension.lore.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.monster.creaking.Creaking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import versoma.dimension.lore.registry.ModItemsRegistry;
import versoma.dimension.lore.shadow.ShadowCreakingEntityMarker;

// Подмешиваем наш интерфейс в ванильный класс
@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ShadowCreakingEntityMarker {

    @Unique
    private boolean versoma$isShadow = false;

    @Override
    public boolean versoma$isShadowCreaking() {
        return this.versoma$isShadow;
    }

    @Override
    public void versoma$setShadowCreaking(boolean isShadow) {
        this.versoma$isShadow = isShadow;
    }

    @Inject(method = "dropCustomDeathLoot", at = @At("TAIL"))
    private void onDropCustomLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit, CallbackInfo ci) {
        if ((Object) this instanceof Creaking) {

            if (this.versoma$isShadow) {
                LivingEntity self = (LivingEntity) (Object) this;

                ItemStack stack = new ItemStack(ModItemsRegistry.PALE_BRANCH);
                ItemEntity itemEntity = new ItemEntity(level, self.getX(), self.getY(), self.getZ(), stack);
                level.addFreshEntity(itemEntity);
            }
        }
    }
}