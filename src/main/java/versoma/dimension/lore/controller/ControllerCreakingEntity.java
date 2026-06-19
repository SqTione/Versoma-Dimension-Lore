package versoma.dimension.lore.controller;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

public class ControllerCreakingEntity extends Monster {

    public static final EntityDataAccessor<Integer> PHASE =
            SynchedEntityData.defineId(ControllerCreakingEntity.class, EntityDataSerializers.INT);

    // --- Настройки фаз (в тиках, 20 тиков = 1 сек) ---
    private static final int PULL_DURATION    = 80;
    private static final int HOLD_DURATION    = 40;
    private static final int RELEASE_DURATION = 10;

    // --- Настройки контроля ---
    private static final int SLOWNESS_AMPLIFIER = 4;
    private static final int SLOWNESS_DURATION_TICKS = 20;
    private static final double EFFECT_RADIUS = 24.0;
    private static final double TRIGGER_DISTANCE_SQR = 18.0 * 18.0;
    private static final double APPROACH_SPEED_MULTIPLIER = 1.2;

    private int phaseTimer = 0;

    public ControllerCreakingEntity(EntityType<? extends ControllerCreakingEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PHASE, 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public int getPhase() {
        return entityData.get(PHASE);
    }

    public void setPhase(int phase) {
        entityData.set(PHASE, phase);
        phaseTimer = 0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ApproachAndControlGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && getPhase() > 0) {
            this.tickPhase();

            LivingEntity target = this.getTarget();
            if (target != null) {
                this.getLookControl().setLookAt(target, 30.0F, 30.0F);
                this.setYBodyRot(this.getYHeadRot());
            }

            if (this.tickCount % 10 == 0) {
                AABB aabb = this.getBoundingBox().inflate(EFFECT_RADIUS);
                for (Player player : this.level().getEntitiesOfClass(Player.class, aabb)) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_DURATION_TICKS, SLOWNESS_AMPLIFIER, false, false, true));
                }
            }
        }
    }

    private void tickPhase() {
        int phase = getPhase();
        phaseTimer++;

        switch (phase) {
            case 1 -> { if (phaseTimer >= PULL_DURATION)    setPhase(2); }
            case 2 -> { if (phaseTimer >= HOLD_DURATION)    setPhase(3); }
            case 3 -> { if (phaseTimer >= RELEASE_DURATION) discard();   }
        }
    }

    private static class ApproachAndControlGoal extends Goal {
        private final ControllerCreakingEntity mob;

        public ApproachAndControlGoal(ControllerCreakingEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.mob.getTarget() != null && this.mob.getPhase() == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.mob.getTarget() != null && this.mob.getPhase() == 0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target == null) return;

            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (this.mob.distanceToSqr(target) <= TRIGGER_DISTANCE_SQR && this.mob.getSensing().hasLineOfSight(target)) {
                this.mob.getNavigation().stop();
                this.mob.setPhase(1);
            } else {
                this.mob.getNavigation().moveTo(target, APPROACH_SPEED_MULTIPLIER);
            }
        }
    }
}