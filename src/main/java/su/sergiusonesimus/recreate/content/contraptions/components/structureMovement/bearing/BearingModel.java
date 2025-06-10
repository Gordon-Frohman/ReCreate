package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

import su.sergiusonesimus.recreate.AllModelTextures;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftModel;
import su.sergiusonesimus.recreate.util.Direction;
import su.sergiusonesimus.recreate.util.Direction.Axis;
import su.sergiusonesimus.recreate.util.Direction.AxisDirection;

public class BearingModel extends ModelBase {

    Direction face;

    ShaftModel shaft;
    ModelRenderer bearing;
    ModelRenderer[] sides = new ModelRenderer[4];
    ModelRenderer blockIndicator;
    ModelRenderer top;
    ModelRenderer topIndicator;

    public BearingModel() {
        final int textureWidth = 64;
        final int textureHeight = 64;

        shaft = new ShaftModel(AxisDirection.NEGATIVE);

        bearing = new ModelRenderer(this, 0, 20).setTextureSize(textureWidth, textureHeight);
        bearing.addBox(-6, -4, -6, 12, 11, 12);
        bearing.setRotationPoint(0, 0, 0);

        for (int i = 0; i < sides.length; i++) {
            sides[i] = new ModelRenderer(this, 0, 43).setTextureSize(textureWidth, textureHeight);
            sides[i].addBox(-6, -4, -8, 14, 12, 2);
            sides[i].setRotationPoint(0, 0, 0);
            sides[i].rotateAngleY = (float) (i * Math.PI / 2F);
            bearing.addChild(sides[i]);
        }

        blockIndicator = new ModelRenderer(this, 0, 3).setTextureSize(textureWidth, textureHeight);
        blockIndicator.addBox(-2, -1, 0, 4, 2, 1);
        blockIndicator.setRotationPoint(0, -3, 8);
        bearing.addChild(blockIndicator);

        top = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
        top.addBox(-8, -12, -8, 16, 4, 16);
        top.setRotationPoint(0, 4, 0);

        topIndicator = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
        topIndicator.addBox(-2, -1, 0, 4, 2, 1);
        topIndicator.setRotationPoint(0, -9, 8);
        top.addChild(topIndicator);

        bearing.addChild(top);
    }

    public BearingModel setFace(Direction direction) {
        shaft = new ShaftModel(
            direction.getAxisDirection()
                .opposite());
        shaft.setAxis(direction.getAxis());
        face = direction;
        double rotationX = 0f;
        double rotationZ = 0f;
        if (direction.getAxis() == Axis.Y) {
            if (direction == Direction.DOWN) rotationX = Math.PI;
        } else {
            switch (direction) {
                default:
                case EAST:
                    rotationZ = -Math.PI / 2;
                    break;
                case WEST:
                    rotationZ = Math.PI / 2;
                    break;
                case SOUTH:
                    rotationX = -Math.PI / 2;
                    break;
                case NORTH:
                    rotationX = Math.PI / 2;
                    break;
            }
        }
        bearing.rotateAngleX = (float) rotationX;
        bearing.rotateAngleZ = (float) rotationZ;
        return this;
    }

    public BearingModel setRotations(float shaftAngle, float topAngle) {
        if (face.getAxis() != Axis.Z) {
            shaftAngle *= -1;
        }
        if (face.getAxis() != Axis.X) {
            topAngle *= -1;
        }
        shaft.setRotation(shaftAngle);
        top.rotateAngleY = topAngle;
        return this;
    }

    public void render(AllModelTextures bearingTexture) {
        shaft.render();
        bearingTexture.bind();
        bearing.render(0.0625F);
    }

}
