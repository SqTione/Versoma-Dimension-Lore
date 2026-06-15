package versoma.dimension.lore.sleep;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

public class SleepParalysisState extends SavedData {

    private static final Codec<Map<UUID, Float>> UUID_FLOAT_MAP_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.FLOAT)
                    .xmap(
                            map -> {
                                Map<UUID, Float> result = new HashMap<>();
                                map.forEach((k, v) -> result.put(UUID.fromString(k), v));
                                return result;
                            },
                            map -> {
                                Map<String, Float> result = new HashMap<>();
                                map.forEach((k, v) -> result.put(k.toString(), v));
                                return result;
                            }
                    );

    private static final Codec<Map<UUID, Long>> UUID_LONG_MAP_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.LONG)
                    .xmap(
                            map -> {
                                Map<UUID, Long> result = new HashMap<>();
                                map.forEach((k, v) -> result.put(UUID.fromString(k), v));
                                return result;
                            },
                            map -> {
                                Map<String, Long> result = new HashMap<>();
                                map.forEach((k, v) -> result.put(k.toString(), v));
                                return result;
                            }
                    );

    private static final Codec<Map<UUID, List<UUID>>> UUID_LIST_MAP_CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()
                            .xmap(
                                    list -> list.stream().map(UUID::fromString).toList(),
                                    list -> list.stream().map(UUID::toString).toList()
                            ))
                    .xmap(
                            map -> {
                                Map<UUID, List<UUID>> result = new HashMap<>();
                                map.forEach((k, v) -> result.put(UUID.fromString(k), v));
                                return result;
                            },
                            map -> {
                                Map<String, List<UUID>> result = new HashMap<>();
                                map.forEach((k, v) -> result.put(k.toString(), v));
                                return result;
                            }
                    );


    public static final Codec<SleepParalysisState> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUID_FLOAT_MAP_CODEC.fieldOf("paralysis_chance").forGetter(s -> s.paralysisChance),
                    UUID_LONG_MAP_CODEC.fieldOf("blocked_until").forGetter(s -> s.blockedUntilTick),
                    UUID_LIST_MAP_CODEC.fieldOf("sleep_creakings").forGetter(s -> s.sleepCreakings)
            ).apply(instance, SleepParalysisState::new)
    );

    public static final SavedDataType<SleepParalysisState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("versoma-dimension-lore", "sleep_paralysis"),
            SleepParalysisState::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, Float> paralysisChance;
    private final Map<UUID, Long> blockedUntilTick;
    private final Map<UUID, List<UUID>> sleepCreakings;

    public SleepParalysisState() {
        this.paralysisChance = new HashMap<>();
        this.blockedUntilTick = new HashMap<>();
        this.sleepCreakings = new HashMap<>();
    }

    private SleepParalysisState(Map<UUID, Float> paralysisChance,
                                Map<UUID, Long> blockedUntilTick,
                                Map<UUID, List<UUID>> sleepCreakings) {
        this.paralysisChance = new HashMap<>(paralysisChance);
        this.blockedUntilTick = new HashMap<>(blockedUntilTick);
        this.sleepCreakings = new HashMap<>(sleepCreakings);
    }

    public static SleepParalysisState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    // --- Chance logic ---

    public float getChance(UUID player, float defaultChance) {
        return paralysisChance.getOrDefault(player, defaultChance);
    }

    public void setChance(UUID player, float chance) {
        paralysisChance.put(player, chance);
        setDirty();
    }

    public void resetChance(UUID player) {
        paralysisChance.remove(player);
        setDirty();
    }

    // --- Sleep block ---

    public boolean isSleepBlocked(UUID player, long currentTick) {
        Long until = blockedUntilTick.get(player);
        return until != null && currentTick < until;
    }

    public void blockSleep(UUID player, long currentTick, int gameDays) {
        blockedUntilTick.put(player, currentTick + gameDays * 24000L);
        setDirty();
    }

    public void clearSleepBlock(UUID player) {
        blockedUntilTick.remove(player);
        setDirty();
    }

    // --- Sleep creakings ---

    public void setSleepCreakings(UUID player, List<UUID> creakings) {
        sleepCreakings.put(player, new ArrayList<>(creakings));
        setDirty();
    }

    public List<UUID> getSleepCreakings(UUID player) {
        return sleepCreakings.getOrDefault(player, List.of());
    }

    public void clearSleepCreakings(UUID player) {
        sleepCreakings.remove(player);
        setDirty();
    }

    public boolean hasActiveParalysis(UUID player) {
        return sleepCreakings.containsKey(player);
    }
}