package versoma.dimension.lore.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import versoma.dimension.lore.client.controller.ControllerCreakingRenderer;
import versoma.dimension.lore.registry.ModEntityRegistry;

public class VersomaDimensionLoreClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntityRegistry.CONTROLLER_CREAKING, ControllerCreakingRenderer::new);
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}