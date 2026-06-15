package versoma.dimension.lore.sleep;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;

import java.util.*;

public class SleepParalysisHandler {

    public static final String SLEEP_CREAKING_TAG = "sleep_creaking";
    private static final int CREAKING_COUNT = 4;
    private static final double SPAWN_RADIUS = 2.5;

    private static final Random random = new Random();

    private record HeartbeatTask(UUID playerUuid, long targetTick, float volume) {}
    private static final List<HeartbeatTask> heartbeatTasks = new ArrayList<>();
    private static final Set<UUID> checkedThisSleep = new HashSet<>();

    public static void tick(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        SleepParalysisState state = SleepParalysisState.get(overworld);
        long currentTick = overworld.getGameTime();

        processHeartbeatTasks(server, currentTick, overworld.getRandom());

        for (ServerPlayer player : overworld.players()) {
            if (player == null) continue;

            UUID uuid = player.getUUID();

            if (!player.isSleeping()) {
                checkedThisSleep.remove(uuid);
                continue;
            }

            if (!state.hasActiveParalysis(uuid)) {
                if (!checkedThisSleep.contains(uuid)) {
                    checkedThisSleep.add(uuid);
                    tryTriggerParalysis(player, overworld, state, currentTick);
                }
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false));

                if (currentTick % 40 == 0) {
                    playSound(player, overworld.getRandom(), 1.0f);
                }
            }
        }
    }

    private static void processHeartbeatTasks(MinecraftServer server, long currentTick, RandomSource randomSource) {
        heartbeatTasks.removeIf(task -> {
            if (currentTick >= task.targetTick()) {
                ServerPlayer player = server.getPlayerList().getPlayer(task.playerUuid());
                if (player != null && !player.isRemoved()) {
                    playSound(player, randomSource, task.volume());
                }
                return true;
            }
            return false;
        });
    }

    private static void playSound(ServerPlayer player, net.minecraft.util.RandomSource random, float volume) {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WARDEN_HEARTBEAT),
                net.minecraft.sounds.SoundSource.AMBIENT,
                player.getX(), player.getY(), player.getZ(),
                volume, 1.0f,
                random.nextLong()
        ));
    }

    private static void tryTriggerParalysis(ServerPlayer player, ServerLevel level,
                                            SleepParalysisState state, long currentTick) {
        int days = state.getDayCounter(player.getUUID());
        float chance = 0.05f + days * 0.08f + (random.nextFloat() - 0.5f) * 0.06f;

        if (random.nextFloat() > chance) {
            state.incrementDayCounter(player.getUUID());
            return;
        }

        triggerParalysis(player, level, state, currentTick);
    }

    private static void triggerParalysis(ServerPlayer player, ServerLevel level,
                                         SleepParalysisState state, long currentTick) {
        UUID playerUuid = player.getUUID();
        Vec3 bedPos = player.position();

        Optional<BlockPos> sleepingPos = player.getSleepingPos();
        double facingAngle;

        if (sleepingPos.isPresent()) {
            BlockState bedState = level.getBlockState(sleepingPos.get());
            Direction bedFacing = bedState.getOptionalValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)
                    .orElse(Direction.NORTH);
            facingAngle = Math.toRadians(bedFacing.toYRot());
        } else {
            facingAngle = Math.toRadians(player.getYRot());
        }

        List<UUID> spawnedCreakings = new ArrayList<>();
        for (int i = 0; i < CREAKING_COUNT; i++) {
            double angleOffset = Math.toRadians(-70 + (140.0 / (CREAKING_COUNT - 1)) * i);
            double angle = facingAngle + angleOffset;

            double x = bedPos.x + Math.sin(angle) * SPAWN_RADIUS;
            double z = bedPos.z + Math.cos(angle) * SPAWN_RADIUS;

            Creaking creaking = EntityType.CREAKING.create(level, EntitySpawnReason.EVENT);
            if (creaking == null) continue;

            creaking.setPos(new Vec3(x, bedPos.y, z));
            creaking.addTag(SLEEP_CREAKING_TAG);
            level.addFreshEntity(creaking);
            spawnedCreakings.add(creaking.getUUID());
            facePlayer(creaking, player);
        }

        state.setSleepCreakings(playerUuid, spawnedCreakings);
        state.resetDayCounter(playerUuid);

        int blockDays = 1 + random.nextInt(3);
        state.blockSleep(playerUuid, currentTick, blockDays);

        player.sendSystemMessage(Component.literal("Я слышу их дыхание..."));
    }

    public static void onWakeUp(ServerPlayer player, ServerLevel level, SleepParalysisState state) {
        UUID playerUuid = player.getUUID();

        for (UUID creakingUuid : state.getSleepCreakings(playerUuid)) {
            var entity = level.getEntity(creakingUuid);
            if (entity instanceof Creaking creaking) {
                Vec3 pos = creaking.position();
                level.sendParticles(ParticleTypes.POOF,
                        pos.x, pos.y + 1.0, pos.z,
                        10, 0.3, 0.5, 0.3, 0.05);
                creaking.discard();
            }
        }

        state.clearSleepCreakings(playerUuid);

        playHeartbeatFading(player, level);

        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 150, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 0, false, false));
    }

    private static void facePlayer(Creaking creaking, ServerPlayer player) {
        double dx = player.getX() - creaking.getX();
        double dz = player.getZ() - creaking.getZ();
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx))) - 90f;
        creaking.setYRot(yaw);
        creaking.setYHeadRot(yaw);
        creaking.setYBodyRot(yaw);
    }

    public static net.minecraft.world.entity.player.Player.BedSleepingProblem checkCanSleep(ServerPlayer player, ServerLevel level) {
        SleepParalysisState state = SleepParalysisState.get(level);
        long currentTick = level.getGameTime();

        if (state.isSleepBlocked(player.getUUID(), currentTick)) {
            player.sendSystemMessage(Component.literal("Я не могу спать. Они снова придут."));
            return net.minecraft.world.entity.player.Player.BedSleepingProblem.OTHER_PROBLEM;
        }
        return null;
    }

    private static void playHeartbeatFading(ServerPlayer player, ServerLevel level) {
        int[] delayTicks = {0, 30, 65, 105, 150};
        float[] volumes = {1.0f, 0.8f, 0.6f, 0.35f, 0.15f};
        long currentTick = level.getGameTime();

        for (int i = 0; i < delayTicks.length; i++) {
            heartbeatTasks.add(new HeartbeatTask(player.getUUID(), currentTick + delayTicks[i], volumes[i]));
        }
    }
}