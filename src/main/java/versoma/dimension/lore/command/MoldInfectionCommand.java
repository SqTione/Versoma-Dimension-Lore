package versoma.dimension.lore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import versoma.dimension.lore.mold.MoldInfectionManager;
import versoma.dimension.lore.mold.MoldInfectionZone;

import java.util.List;

public class MoldInfectionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("moldzone")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                .then(Commands.literal("list")
                        .executes(MoldInfectionCommand::executeList)
                )
                .then(Commands.literal("create")
                        .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                                .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                        .executes(MoldInfectionCommand::executeCreate)))
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(MoldInfectionCommand::executeRemove))
                )
        );
    }

    private static int executeList(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        List<MoldInfectionZone> zones = MoldInfectionManager.get(level).getZones();

        if (zones.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Активных зон заражения нет."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("=== Активные зоны заражения (" + zones.size() + ") ==="), false);

        for (int i = 0; i < zones.size(); i++) {
            int index = i + 1;
            MoldInfectionZone zone = zones.get(i);
            source.sendSuccess(() -> Component.literal(index + ". " + zone.getZoneInfo()), false);
        }

        return 1;
    }

    private static int executeCreate(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        BlockPos pos1 = BlockPosArgument.getLoadedBlockPos(context, "pos1");
        BlockPos pos2 = BlockPosArgument.getLoadedBlockPos(context, "pos2");

        BoundingBox bounds = BoundingBox.fromCorners(pos1, pos2);

        try {
            int nodesCount = MoldInfectionManager.get(level).initializeNewZone(level, bounds);
            source.sendSuccess(() -> Component.literal(
                    "Зона заражения инициализирована. Активных точек: " + nodesCount
            ), true);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Ошибка валидации: " + e.getMessage()));
            return 0;
        }

        return 1;
    }

    private static int executeRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");

        MoldInfectionManager.get(level).removeZonesAt(pos);
        source.sendSuccess(() -> Component.literal("Зоны по координатам " + pos.toShortString() + " удалены (если существовали)."), true);

        return 1;
    }
}