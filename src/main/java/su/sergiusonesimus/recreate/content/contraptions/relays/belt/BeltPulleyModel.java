package su.sergiusonesimus.recreate.content.contraptions.relays.belt;

import net.minecraft.client.model.ModelRenderer;

import su.sergiusonesimus.recreate.AllModelTextures;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.AbstractShaftModel;

public class BeltPulleyModel extends AbstractShaftModel {

    public final ModelRenderer[] pulleyParts = new ModelRenderer[4];

    public BeltPulleyModel() {
        super(AllModelTextures.BELT_PULLEY, 16, 16);
        for (int i = 0; i < pulleyParts.length; i++) {
            pulleyParts[i] = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
            pulleyParts[i].addBox(-3F, -6F, -4F, 6, 12, 2, 0F);
            pulleyParts[i].setRotationPoint(0F, 0F, 0F);
            pulleyParts[i].rotateAngleY = i * (float) Math.PI / 2;
            core.addChild(pulleyParts[i]);
        }
    }

}
