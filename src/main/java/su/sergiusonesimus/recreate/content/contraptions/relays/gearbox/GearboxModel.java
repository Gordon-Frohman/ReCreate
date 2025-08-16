package su.sergiusonesimus.recreate.content.contraptions.relays.gearbox;

import net.minecraft.client.model.ModelRenderer;

import su.sergiusonesimus.recreate.AllModelTextures;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.AbstractShaftModel;
import su.sergiusonesimus.recreate.util.Direction;

public class GearboxModel extends AbstractShaftModel {

    ModelRenderer bearing;
    ModelRenderer bearing2;
    ModelRenderer bearing3;
    ModelRenderer bearing4;

    ModelRenderer side;
    ModelRenderer side2;

    public GearboxModel(AllModelTextures texture) {
        super(texture, 64, 64);

        bearing = new ModelRenderer(this, -2, 16).setTextureSize(textureWidth, textureHeight);
        bearing.addBox(-6.0F, -15.0F + 8F, -7.0F, 12, 1, 14);
        bearing.setRotationPoint(0, 0, 0);
        core.addChild(bearing);

        bearing2 = new ModelRenderer(this, -2, 16).setTextureSize(textureWidth, textureHeight);
        bearing2.addBox(-6.0F, -2.0F + 8F, -7.0F, 12, 1, 14);
        bearing2.setRotationPoint(0, 0, 0);
        core.addChild(bearing2);

        bearing3 = new ModelRenderer(this, 11, 30).setTextureSize(textureWidth, textureHeight);
        bearing3.addBox(-6.0F, -14.0F + 8F, -7.0F, 12, 12, 1);
        bearing3.setRotationPoint(0, 0, 0);
        core.addChild(bearing3);

        bearing4 = new ModelRenderer(this, 11, 30).setTextureSize(textureWidth, textureHeight);
        bearing4.addBox(-6.0F, -14.0F + 8F, 6.0F, 12, 12, 1);
        bearing4.setRotationPoint(0, 0, 0);
        core.addChild(bearing4);

        side = new ModelRenderer(this, 21, 32).setTextureSize(textureWidth, textureHeight);
        side.addBox(6.0F, -16.0F + 8F, -8.0F, 2, 16, 16);
        side.setRotationPoint(7 + 7, 0, 0);
        side.rotateAngleY = (float) Math.PI;
        core.addChild(side);

        side2 = new ModelRenderer(this, 21, 32).setTextureSize(textureWidth, textureHeight);
        side2.addBox(-8.0F, -16.0F + 8F, -8.0F, 2, 16, 16);
        side2.setRotationPoint(0, 0, 0);
        core.addChild(side2);
    }

    @Override
    public AbstractShaftModel setAxis(Direction.Axis axis) {
        this.axis = axis;
        switch (this.axis) {
            case Y:
                core.rotateAngleX = (float) (-Math.PI / 2);
                core.rotateAngleY = 0;
                core.rotateAngleZ = 0;
                break;
            case X:
                core.rotateAngleX = 0;
                core.rotateAngleY = (float) (-Math.PI / 2);
                core.rotateAngleZ = (float) (-Math.PI / 2);
                break;
            default:
            case Z:
                core.rotateAngleX = (float) (-Math.PI / 2);
                core.rotateAngleY = 0;
                core.rotateAngleZ = (float) (-Math.PI / 2);
                break;
        }
        return this;
    }
}
