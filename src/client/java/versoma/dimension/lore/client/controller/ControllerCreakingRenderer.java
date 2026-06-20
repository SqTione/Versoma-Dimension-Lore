package versoma.dimension.lore.client.controller;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;
import net.minecraft.resources.Identifier;
import versoma.dimension.lore.controller.ControllerCreakingEntity;

public class ControllerCreakingRenderer extends MobRenderer<ControllerCreakingEntity, CreakingRenderState, ControllerCreakingModel> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("versoma-dimension-lore", "textures/entity/controller_creaking.png");

    public ControllerCreakingRenderer(EntityRendererProvider.Context context) {
        super(context, new ControllerCreakingModel(context.bakeLayer(ModelLayers.CREAKING)), 0.6f);
    }

    @Override
    public CreakingRenderState createRenderState() {
        return new CreakingRenderState();
    }

    @Override
    public void extractRenderState(ControllerCreakingEntity entity, CreakingRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.canMove = entity.getPhase() == 0;
    }

    @Override
    public Identifier getTextureLocation(CreakingRenderState state) {
        return TEXTURE;
    }
}