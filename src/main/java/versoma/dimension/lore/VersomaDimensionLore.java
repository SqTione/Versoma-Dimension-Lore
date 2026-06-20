package versoma.dimension.lore;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import versoma.dimension.lore.boundary.BoundaryEffectHandler;
import versoma.dimension.lore.maintenance.MaintenanceManager;
import versoma.dimension.lore.registry.ModEntityRegistry;
import versoma.dimension.lore.shadow.ShadowCreakingSpawner;
import versoma.dimension.lore.shadow.ShadowCreakingTracker;
import versoma.dimension.lore.sleep.SleepParalysisHandler;
import versoma.dimension.lore.sleep.SleepParalysisState;

public class VersomaDimensionLore implements ModInitializer {

	public static final String MOD_ID = "versoma-dimension-lore";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MaintenanceManager.registerCommands();
		ModEntityRegistry.register();

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			SleepParalysisHandler.tick(server);
			MaintenanceManager.tick(server);
		});

		EntitySleepEvents.ALLOW_SLEEPING.register((player, sleepingPos) -> {
			if (player instanceof ServerPlayer sp) {
				ServerLevel level = (ServerLevel) sp.level();
				return SleepParalysisHandler.checkCanSleep(sp, level);
			}
			return null;
		});

		EntitySleepEvents.ALLOW_RESETTING_TIME.register(player -> {
			if (player instanceof ServerPlayer sp) {
				ServerLevel level = (ServerLevel) sp.level();
				SleepParalysisState state = SleepParalysisState.get(level);
				return !state.hasActiveParalysis(sp.getUUID());
			}
			return true;
		});

		EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
			if (entity instanceof ServerPlayer sp) {
				ServerLevel level = (ServerLevel) sp.level();
				SleepParalysisState state = SleepParalysisState.get(level);
				if (state.hasActiveParalysis(sp.getUUID())) {
					SleepParalysisHandler.onWakeUp(sp, level, state);
				}
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			server.getPlayerList().getPlayers().forEach(BoundaryEffectHandler::tick);
			ShadowCreakingSpawner.tick(server);
			ShadowCreakingTracker.tick(server);
			SleepParalysisHandler.tick(server);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("debugsleep")
					.requires(source -> true)
					.executes(ctx -> {
						ServerPlayer player = ctx.getSource().getPlayerOrException();
						ServerLevel level = (ServerLevel) player.level();
						SleepParalysisState state = SleepParalysisState.get(level);
						state.clearSleepBlock(player.getUUID());
						state.clearSleepCreakings(player.getUUID());
						state.resetChance(player.getUUID());
						ctx.getSource().sendSuccess(() -> Component.literal("Sleep state reset"), false);
						return 1;
					}));
		});

		LOGGER.info("VersomaDimensionLore initialized.");
	}
}