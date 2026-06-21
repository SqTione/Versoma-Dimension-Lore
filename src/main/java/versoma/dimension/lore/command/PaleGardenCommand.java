package versoma.dimension.lore.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import versoma.dimension.lore.registry.ModGameRulesRegistry;
import versoma.dimension.lore.registry.ModSoundsRegistry;

public class PaleGardenCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("palegarden")
                .requires(source -> source.getEntity() != null)
                .then(Commands.literal("call").executes(context -> {
                    CommandSourceStack source = context.getSource();
                    var server = source.getServer();

                    server.getGameRules().set(ModGameRulesRegistry.PALE_GARDEN_CALL_ACTIVE, true, server);

                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {

                        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 400, 0, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 400, 0, false, false));

                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSoundsRegistry.PALE_GARDEN_CALL, SoundSource.MASTER, 1.0f, 1.0f);
                    }

                    source.sendSuccess(() -> Component.literal("Зов Бледного Сада активирован. Контроллеры вышли на охоту."), false);
                    return 1;
                })));
    }
}