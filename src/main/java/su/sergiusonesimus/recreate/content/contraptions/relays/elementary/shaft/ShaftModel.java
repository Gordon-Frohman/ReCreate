package su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft;

import net.minecraft.client.model.ModelRenderer;

import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.AllModelTextures;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.AbstractShaftModel;

public class ShaftModel extends AbstractShaftModel {

    protected ModelRenderer shaft;

    public ShaftModel() {
        super(AllModelTextures.SHAFT, 32, 32);
        shaft = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
        shaft.addBox(-2F, -8F, -2F, 4, 16, 4, 0F);
        shaft.setRotationPoint(0F, 0F, 0F);
        core.addChild(shaft);
    }

    public ShaftModel(AxisDirection direction) {
        super(AllModelTextures.SHAFT, 32, 32);
        shaft = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
        shaft.addBox(-2F, direction == AxisDirection.POSITIVE ? -8F : 0, -2F, 4, 8, 4, 0F);
        shaft.setRotationPoint(0F, 0F, 0F);
        core.addChild(shaft);
    }

}
