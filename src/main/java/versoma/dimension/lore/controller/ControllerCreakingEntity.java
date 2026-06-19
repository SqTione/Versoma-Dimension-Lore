package versoma.dimension.lore.controller;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ControllerCreakingEntity extends Monster {

    public static final EntityDataAccessor<Integer> PHASE =
            SynchedEntityData.defineId(ControllerCreakingEntity.class, EntityDataSerializers.INT);

    private static final int PULL_DURATION    = 60;
    private static final int HOLD_DURATION    = 20;
    private static final int RELEASE_DURATION = 10;

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
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    public int getPhase() {
        return entityData.get(PHASE);
    }

    private void setPhase(int phase) {
        entityData.set(PHASE, phase);
        phaseTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {

            this.tickPhase();

            if (this.tickCount % 10 == 0) {
                AABB aabb = this.getBoundingBox().inflate(24.0);
                for (Player player : this.level().getEntitiesOfClass(Player.class, aabb)) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 4, false, false, true));
                }
            }
        }
    }

    private void tickPhase() {
        int phase = getPhase();
        phaseTimer++;

        switch (phase) {
            case 0 -> setPhase(1);
            case 1 -> { if (phaseTimer >= PULL_DURATION)    setPhase(2); }
            case 2 -> { if (phaseTimer >= HOLD_DURATION)    setPhase(3); }
            case 3 -> { if (phaseTimer >= RELEASE_DURATION) discard();   }
        }
    }

    @Override
    protected void registerGoals() {}
}