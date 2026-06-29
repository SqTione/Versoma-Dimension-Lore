package versoma.dimension.lore.mold;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class MoldInfectionZone {

    public static final int INFECTION_TICK_INTERVAL = 10000;
    private static final float DEAD_NODE_CLEANUP_CHANCE = 0.1f;

    private final BoundingBox bounds;
    private final List<BlockPos> activeNodes;
    private int tickCounter;

    public static final Codec<MoldInfectionZone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BoundingBox.CODEC.fieldOf("bounds").forGetter(z -> z.bounds),
            BlockPos.CODEC.listOf().fieldOf("active_nodes").forGetter(z -> z.activeNodes),
            Codec.INT.fieldOf("tick_counter").forGetter(z -> z.tickCounter)
    ).apply(instance, MoldInfectionZone::new));

    public MoldInfectionZone(BoundingBox bounds, List<BlockPos> activeNodes, int tickCounter) {
        if (bounds == null) throw new IllegalArgumentException("Bounds cannot be null");
        if (activeNodes == null) throw new IllegalArgumentException("Active nodes list cannot be null");

        this.bounds = bounds;
        this.activeNodes = new ArrayList<>(activeNodes);
        this.tickCounter = tickCounter;
    }

    public MoldInfectionZone(BoundingBox bounds, BlockPos center) {
        this(bounds, List.of(center), 0);
        if (!bounds.isInside(center)) {
            throw new IllegalArgumentException("Center must be inside bounds");
        }
    }

    public String getZoneInfo() {
        return String.format("Центр: [%d, %d, %d], Активных узлов: %d",
                bounds.minX() + bounds.getXSpan() / 2,
                bounds.minY() + bounds.getYSpan() / 2,
                bounds.minZ() + bounds.getZSpan() / 2,
                activeNodes.size());
    }

    public void tick(ServerLevel level, MoldInfectionManager manager) {
        if (level == null) throw new IllegalArgumentException("Level cannot be null");
        if (manager == null) throw new IllegalArgumentException("Manager cannot be null");
        if (this.activeNodes.isEmpty()) return;

        this.tickCounter++;

        if (this.tickCounter >= INFECTION_TICK_INTERVAL) {
            this.tickCounter = 0;
            manager.setDirty();
            processGrowth(level, manager);
        }
    }

    private void processGrowth(ServerLevel level, MoldInfectionManager manager) {
        RandomSource random = level.getRandom();
        int nodeIndex = random.nextInt(this.activeNodes.size());
        BlockPos sourcePos = this.activeNodes.get(nodeIndex);

        BlockState sourceState = level.getBlockState(sourcePos);

        if (!(sourceState.getBlock() instanceof MoldBlock moldBlock)) {
            this.activeNodes.remove(nodeIndex);
            manager.setDirty();
            return;
        }

        var spreadResult = moldBlock.getSpreader().spreadFromRandomFaceTowardRandomDirection(
                sourceState, level, sourcePos, random
        );

        if (spreadResult.isPresent()) {
            BlockPos newPos = spreadResult.get().pos();

            if (this.bounds.isInside(newPos) && !this.activeNodes.contains(newPos)) {
                this.activeNodes.add(newPos);
                manager.setDirty();
            }
        } else {
            if (random.nextFloat() < DEAD_NODE_CLEANUP_CHANCE) {
                this.activeNodes.remove(nodeIndex);
                manager.setDirty();
            }
        }
    }

    private void scanAndRegisterNewNodes(ServerLevel level, BlockPos sourcePos, MoldInfectionManager manager) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = sourcePos.relative(direction);

            if (!this.bounds.isInside(neighborPos)) continue;
            if (this.activeNodes.contains(neighborPos)) continue;

            if (level.getBlockState(neighborPos).getBlock() instanceof MoldBlock) {
                this.activeNodes.add(neighborPos);
                manager.setDirty();
            }
        }
    }

    public boolean contains(BlockPos pos) {
        if (pos == null) return false;
        return this.bounds.isInside(pos);
    }
}