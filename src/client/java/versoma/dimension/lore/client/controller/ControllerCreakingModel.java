package versoma.dimension.lore.client.controller;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.creaking.CreakingModel;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;

public class ControllerCreakingModel extends CreakingModel {

    private final ModelPart headPart;

    public ControllerCreakingModel(ModelPart roots) {
        super(roots);
        this.headPart = roots.getChild("root").getChild("upper_body").getChild("head");
    }

    @Override
    public void setupAnim(CreakingRenderState state) {
        super.setupAnim(state);
        this.headPart.visible = false;
    }
}