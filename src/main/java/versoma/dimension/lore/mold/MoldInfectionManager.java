package versoma.dimension.lore.mold;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import versoma.dimension.lore.VersomaDimensionLore;
import versoma.dimension.lore.registry.ModBlocksRegistry;

import java.util.ArrayList;
import java.util.List;

public class MoldInfectionManager extends SavedData {
    private final List<MoldInfectionZone> activeZones;

    public static final Codec<MoldInfectionManager> CODEC = MoldInfectionZone.CODEC.listOf().xmap(
            MoldInfectionManager::new,
            manager -> manager.activeZones
    );

    public static final SavedDataType<MoldInfectionManager> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(VersomaDimensionLore.MOD_ID, "infection_manager"),
            MoldInfectionManager::new,
            CODEC,
            null
    );

    public MoldInfectionManager() {
        this.activeZones = new ArrayList<>();
    }

    public MoldInfectionManager(List<MoldInfectionZone> zones) {
        this.activeZones = new ArrayList<>(zones);
    }

    public static MoldInfectionManager get(ServerLevel level) {
        if (level == null) throw new IllegalArgumentException("Level cannot be null");
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public List<MoldInfectionZone> getZones() {
        return this.activeZones;
    }

    public void addZone(MoldInfectionZone zone) {
        if (zone == null) throw new IllegalArgumentException("Zone cannot be null");
        this.activeZones.add(zone);
        this.setDirty();
    }

    public void removeZonesAt(BlockPos pos) {
        boolean removed = activeZones.removeIf(zone -> zone.contains(pos));
        if (removed) {
            this.setDirty();
        }
    }

    public void tick(ServerLevel level) {
        if (activeZones.isEmpty()) return;

        for (MoldInfectionZone zone : activeZones) {
            zone.tick(level, this);
        }
    }

    public int initializeNewZone(ServerLevel level, BoundingBox bounds) {
        if (level == null) throw new IllegalArgumentException("Level cannot be null");
        if (bounds == null) throw new IllegalArgumentException("Bounds cannot be null");

        List<BlockPos> initialNodes = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
            if (level.getBlockState(pos).getBlock() instanceof MoldBlock) {
                BlockPos spawnPos = findSolidSurface(level, bounds);

                BlockState startingState = ModBlocksRegistry.MOLD.defaultBlockState();
                if (startingState.hasProperty(BlockStateProperties.DOWN)) {
                    startingState = startingState.setValue(BlockStateProperties.DOWN, true);
                }

                level.setBlockAndUpdate(spawnPos, startingState);
                initialNodes.add(spawnPos);
            }
        }

        if (initialNodes.isEmpty()) {
            BlockPos spawnPos = findSolidSurface(level, bounds);

            level.setBlockAndUpdate(spawnPos, ModBlocksRegistry.MOLD.defaultBlockState());
            initialNodes.add(spawnPos);
        }

        this.addZone(new MoldInfectionZone(bounds, initialNodes, 0));
        return initialNodes.size();
    }

    private BlockPos findSolidSurface(ServerLevel level, BoundingBox bounds) {
        int centerX = bounds.minX() + bounds.getXSpan() / 2;
        int centerZ = bounds.minZ() + bounds.getZSpan() / 2;

        BlockPos.MutableBlockPos searchPos = new BlockPos.MutableBlockPos(centerX, bounds.maxY(), centerZ);

        while (searchPos.getY() > bounds.minY()) {
            if (!level.getBlockState(searchPos).isAir() && level.getBlockState(searchPos.above()).isAir()) {
                return searchPos.above().immutable();
            }
            searchPos.move(net.minecraft.core.Direction.DOWN);
        }

        return new BlockPos(centerX, bounds.minY() + bounds.getYSpan() / 2, centerZ);
    }
}