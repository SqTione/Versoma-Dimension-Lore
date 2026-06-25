package versoma.dimension.lore.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.monster.creaking.Creaking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import versoma.dimension.lore.VersomaDimensionLore;
import versoma.dimension.lore.registry.ModEffectsRegistry;
import versoma.dimension.lore.registry.ModItemsRegistry;
import versoma.dimension.lore.shadow.ShadowCreakingEntityMarker;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ShadowCreakingEntityMarker {

    @Unique
    private boolean versoma$isShadow = false;

    @Unique
    private static final Identifier ROT_PENALTY_ID = Identifier.fromNamespaceAndPath(VersomaDimensionLore.MOD_ID, "rot_penalty");
    @Unique
    private static final double MIN_HEALTH_THRESHOLD = 2.0;
    @Unique
    private static final double HEALTH_PENALTY_STEP = 2.0;
    @Unique
    private static final int RECOVERY_TICK_RATE = 300;

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

    @Inject(method="tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
       LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide()) return;

        boolean hasRot = entity.hasEffect(ModEffectsRegistry.ROT);

        if (hasRot && entity.isEyeInFluid(FluidTags.WATER)) {
            entity.removeEffect(ModEffectsRegistry.ROT);
            hasRot = false;
        }

        if(!hasRot && entity.tickCount % RECOVERY_TICK_RATE == 0) {
            AttributeInstance maxHealthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
            if(maxHealthAttr == null) return;

            AttributeModifier existingModifier = maxHealthAttr.getModifier(ROT_PENALTY_ID);
            if (existingModifier != null && existingModifier.amount() < 0) {
                double newPenalty = existingModifier.amount() + HEALTH_PENALTY_STEP;
                maxHealthAttr.removeModifier(ROT_PENALTY_ID);

                if (newPenalty < 0) {
                    maxHealthAttr.addPermanentModifier(new AttributeModifier(ROT_PENALTY_ID, newPenalty, AttributeModifier.Operation.ADD_VALUE));
                }
            }
        }
    }

    @Inject(method = "setHealth", at = @At("HEAD"))
    private void onSetHealth(float newHealth, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.level().isClientSide() || entity.isDeadOrDying()) return;

        float oldHealth = entity.getHealth();
        if (newHealth >= oldHealth) return;

        if (entity.hasEffect(ModEffectsRegistry.ROT)) {
            float damageTaken = oldHealth - newHealth;

            AttributeInstance maxHealthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr == null) return;

            double currentMaxHealth = maxHealthAttr.getValue();
            if (currentMaxHealth <= MIN_HEALTH_THRESHOLD) return;

            double targetMaxHealth = Math.max(currentMaxHealth - damageTaken, MIN_HEALTH_THRESHOLD);
            double requiredPenalty = targetMaxHealth - maxHealthAttr.getBaseValue();

            maxHealthAttr.removeModifier(ROT_PENALTY_ID);
            maxHealthAttr.addPermanentModifier(new AttributeModifier(ROT_PENALTY_ID, requiredPenalty, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void versoma$onHurtServerInfection(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity victim = (LivingEntity) (Object) this;

        if (amount <= 0.0F) return;

        Entity attacker = source.getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;
        if (source.getDirectEntity() != attacker) return;

        MobEffectInstance attackerEffect = livingAttacker.getEffect(ModEffectsRegistry.ROT);
        if (attackerEffect != null) {
            int attackerDuration = attackerEffect.getDuration();
            int transferDuration;

            if (attackerDuration < 0) {
                transferDuration = 12000;
            } else {
                transferDuration = Math.max(1200, Math.min(12000, attackerDuration));
            }

            victim.addEffect(new MobEffectInstance(ModEffectsRegistry.ROT, transferDuration, 0, false, true, true));
        }
    }
}