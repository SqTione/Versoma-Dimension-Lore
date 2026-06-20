package versoma.dimension.lore.controller;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

public class ControllerCreakingEntity extends Monster {

    public static final EntityDataAccessor<Integer> PHASE =
            SynchedEntityData.defineId(ControllerCreakingEntity.class, EntityDataSerializers.INT);

    private static final int PULL_DURATION = 70;
    private static final int HOLD_DURATION = 130;
    private static final int RELEASE_DURATION = 20;

    private static final int SLOWNESS_AMPLIFIER = 4;
    private static final int SLOWNESS_DURATION_TICKS = 20;
    private static final double EFFECT_RADIUS = 24.0;
    private static final double TRIGGER_DISTANCE_SQR = 18.0 * 18.0;
    private static final double APPROACH_SPEED_MULTIPLIER = 1.2;

    private static final int SPAWN_WEIGHT = 10;
    private static final int MIN_GROUP_SIZE = 1;
    private static final int MAX_GROUP_SIZE = 1;

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

    private void setPhase(int phase) {
        this.entityData.set(PHASE, phase);
        this.phaseTimer = 0;
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

    public static void registerSpawns(EntityType<ControllerCreakingEntity> type) {
        SpawnPlacements.register(
                type,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules
        );

        ResourceKey<Biome> paleGardenKey = ResourceKey.create(
                Registries.BIOME,
                Identifier.withDefaultNamespace("pale_garden")
        );

        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(paleGardenKey),
                MobCategory.MONSTER,
                type,
                SPAWN_WEIGHT,
                MIN_GROUP_SIZE,
                MAX_GROUP_SIZE
        );
    }

    private void tickPhase() {
        int phase = getPhase();
        phaseTimer++;

        switch (phase) {
            case 1 -> {
                if (phaseTimer == 1) {
                    this.playSound(versoma.dimension.lore.registry.ModSoundsRegistry.CONTROLLER_GAZE, 3.0f, 1.0f);
                }
                if (phaseTimer >= PULL_DURATION) setPhase(2);
            }
            case 2 -> {
                if (phaseTimer >= HOLD_DURATION) {
                    this.setInvisible(true);
                    this.setInvulnerable(true);

                    if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        net.minecraft.world.phys.Vec3 pos = this.position();
                        serverLevel.sendParticles(
                                net.minecraft.core.particles.ParticleTypes.POOF,
                                pos.x, pos.y + 1.0, pos.z,
                                12, 0.3, 0.5, 0.3, 0.05
                        );
                    }

                    setPhase(3);
                }
            }
            case 3 -> {
                if (phaseTimer >= RELEASE_DURATION) {
                    this.discard();
                }
            }
        }
    }

    private void despawnWithParticles() {
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.world.phys.Vec3 pos = this.position();
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.POOF,
                    pos.x, pos.y + 1.0, pos.z,
                    12, 0.3, 0.5, 0.3, 0.05
            );
        }
        this.discard();
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