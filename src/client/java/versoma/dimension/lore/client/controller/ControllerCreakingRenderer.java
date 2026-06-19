package versoma.dimension.lore.client.controller;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.creaking.CreakingModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;
import net.minecraft.resources.Identifier;
import versoma.dimension.lore.controller.ControllerCreakingEntity;

public class ControllerCreakingRenderer extends MobRenderer<ControllerCreakingEntity, CreakingRenderState, CreakingModel> {

    private static final Identifier TEXTURE = Identifier.withDefaultNamespace(
            "textures/entity/creaking/creaking.png"
    );

    public ControllerCreakingRenderer(EntityRendererProvider.Context context) {
        super(context, new CreakingModel(context.bakeLayer(ModelLayers.CREAKING)), 0.5f);
    }
    @Override
    public CreakingRenderState createRenderState() {
        return new CreakingRenderState();
    }

    @Override
    public Identifier getTextureLocation(CreakingRenderState state) {
        return TEXTURE;
    }
}