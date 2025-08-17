package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import net.minecraft.client.model.ModelRenderer;

import su.sergiusonesimus.recreate.AllModelTextures;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.AbstractShaftModel;

public class EncasedShaftModel extends AbstractShaftModel {

    ModelRenderer[] sides = new ModelRenderer[4];
    ModelRenderer bearing;
    ModelRenderer bearing2;

    public EncasedShaftModel(AllModelTextures texture) {
        super(texture, 64, 64);

        bearing = new ModelRenderer(this, 0, 18).setTextureSize(textureWidth, textureHeight);
        bearing.addBox(-6.0F, -15.0F + 8F, -6.0F, 12, 1, 12, 0.0F);
        bearing.setRotationPoint(0, 0, 0);
        core.addChild(bearing);

        bearing2 = new ModelRenderer(this, 0, 18).setTextureSize(textureWidth, textureHeight);
        bearing2.addBox(-6.0F, -2.0F + 8F, -6.0F, 12, 1, 12);
        bearing2.setRotationPoint(0, 0, 0);
        core.addChild(bearing2);

        for (int i = 0; i < sides.length; i++) {
            sides[i] = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
            sides[i].addBox(-8, -16 + 8F, -8, 16, 16, 2, 0);
            sides[i].setRotationPoint(0, 0, 0);
            sides[i].rotateAngleY = (float) (i * Math.PI / 2F);
            core.addChild(sides[i]);
        }

    }

}
