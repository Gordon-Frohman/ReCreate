package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import net.minecraft.client.model.ModelRenderer;

import su.sergiusonesimus.recreate.AllModelTextures;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.AbstractShaftModel;
import su.sergiusonesimus.recreate.util.Direction;

public class SplitShaftModel extends AbstractShaftModel {

    ModelRenderer shaft;
    ModelRenderer shaft2;

    public SplitShaftModel() {
        super(AllModelTextures.SHAFT, 32, 32);

        shaft = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
        shaft.addBox(-2F, -8F, -2F, 4, 8, 4, 0F);
        shaft.setRotationPoint(0F, 0F, 0F);
        core.addChild(shaft);

        shaft2 = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
        shaft2.addBox(-2F, 0F, -2F, 4, 8, 4, 0F);
        shaft2.setRotationPoint(0F, 0F, 0F);

        core.addChild(shaft2);
    }

    public AbstractShaftModel setAxis(Direction.Axis axis) {
        this.axis = axis;
        switch (this.axis) {
            case X:
                shaft.rotateAngleX = 0;
                shaft.rotateAngleY = 0;
                shaft.rotateAngleZ = (float) (-Math.PI / 2);
                shaft2.rotateAngleX = 0;
                shaft2.rotateAngleY = 0;
                shaft2.rotateAngleZ = (float) (-Math.PI / 2);
                break;
            default:
            case Y:
                shaft.rotateAngleX = 0;
                shaft.rotateAngleY = 0;
                shaft.rotateAngleZ = 0;
                shaft2.rotateAngleX = 0;
                shaft2.rotateAngleY = 0;
                shaft2.rotateAngleZ = 0;
                break;
            case Z:
                shaft.rotateAngleX = (float) (-Math.PI / 2);
                shaft.rotateAngleY = 0;
                shaft.rotateAngleZ = 0;
                shaft2.rotateAngleX = (float) (-Math.PI / 2);
                shaft2.rotateAngleY = 0;
                shaft2.rotateAngleZ = 0;
                break;
        }
        return this;
    }

    public SplitShaftModel setRotations(float shaftAngle, float shaftTopAngle) {
        switch (axis) {
            default:
            case X:
            case Y:
                shaft.rotateAngleY = shaftAngle;
                shaft2.rotateAngleY = shaftTopAngle;
                break;
            case Z:
                shaft.rotateAngleZ = shaftAngle;
                shaft2.rotateAngleZ = shaftTopAngle;
                break;
        }
        return this;
    }

}
