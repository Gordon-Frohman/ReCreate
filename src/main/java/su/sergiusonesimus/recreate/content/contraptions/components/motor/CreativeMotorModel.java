package su.sergiusonesimus.recreate.content.contraptions.components.motor;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.AllModelTextures;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftModel;

public class CreativeMotorModel extends ModelBase {

    Direction face;

    public ShaftModel shaft;
    public ModelRenderer sideCover1;
    public ModelRenderer sideCover2;
    public ModelRenderer bottomCover;
    public ModelRenderer rod1;
    public ModelRenderer rod2;
    public ModelRenderer rod3;
    public ModelRenderer rod4;
    public ModelRenderer motor;
    public ModelRenderer stand;
    public ModelRenderer coiling;
    public ModelRenderer wipers;
    public ModelRenderer wiper1;
    public ModelRenderer wiper2;
    public ModelRenderer wiper3;
    public ModelRenderer wiper4;

    public CreativeMotorModel() {
        final int textureWidth = 64;
        final int textureHeight = 48;

        shaft = new ShaftModel(AxisDirection.POSITIVE);

        stand = new ModelRenderer(this, 22, 31).setTextureSize(textureWidth, textureHeight);
        stand.addBox(-5F, 5F, -7F, 7, 3, 14, 0F);
        stand.setRotationPoint(0F, 0F, 0F);

        motor = new ModelRenderer(this, 0, 26).setTextureSize(textureWidth, textureHeight);
        motor.addBox(-4F, -3F, -4F, 8, 9, 8, 0F);
        motor.setRotationPoint(0F, 0F, 0F);

        sideCover1 = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
        sideCover1.addBox(-5F, -6F, -5F, 10, 2, 10, 0F);
        sideCover1.setRotationPoint(0F, 0F, 0F);
        motor.addChild(sideCover1);

        sideCover2 = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
        sideCover2.addBox(-5F, -7.9F, -5F, 10, 2, 10, 0F);
        sideCover2.setRotationPoint(0F, 0F, 0F);
        sideCover2.rotateAngleX = (float) Math.PI;
        motor.addChild(sideCover2);

        bottomCover = new ModelRenderer(this, 0, 12).setTextureSize(textureWidth, textureHeight);
        bottomCover.addBox(-5F, 3.9F, -5F, 10, 4, 10, 0F);
        bottomCover.setRotationPoint(0F, 0F, 0F);
        motor.addChild(bottomCover);

        int rodOffsetX = 40;
        int rodOffsetY = 10;
        Vector3f rodOrigin = new Vector3f(-6F, -7F, -6F);
        int rodWidth = 2;
        int rodHeight = 15;
        int rodDepth = 2;
        Vector3f rodRP = new Vector3f(0F, 0F, 0F);

        rod1 = new ModelRenderer(this, rodOffsetX, rodOffsetY).setTextureSize(textureWidth, textureHeight);
        rod1.addBox(rodOrigin.x, rodOrigin.y, rodOrigin.z, rodWidth, rodHeight, rodDepth, 0F);
        rod1.setRotationPoint(rodRP.x, rodRP.y, rodRP.z);
        motor.addChild(rod1);

        rod2 = new ModelRenderer(this, rodOffsetX, rodOffsetY).setTextureSize(textureWidth, textureHeight);
        rod2.addBox(rodOrigin.x, rodOrigin.y, rodOrigin.z, rodWidth, rodHeight, rodDepth, 0F);
        rod2.setRotationPoint(rodRP.x, rodRP.y, rodRP.z);
        rod2.rotateAngleY = (float) Math.PI / 2;
        motor.addChild(rod2);

        rod3 = new ModelRenderer(this, rodOffsetX, rodOffsetY).setTextureSize(textureWidth, textureHeight);
        rod3.addBox(rodOrigin.x, rodOrigin.y, rodOrigin.z, rodWidth, rodHeight, rodDepth, 0F);
        rod3.setRotationPoint(rodRP.x, rodRP.y, rodRP.z);
        rod3.rotateAngleY = (float) Math.PI;
        motor.addChild(rod3);

        rod4 = new ModelRenderer(this, rodOffsetX, rodOffsetY).setTextureSize(textureWidth, textureHeight);
        rod4.addBox(rodOrigin.x, rodOrigin.y, rodOrigin.z, rodWidth, rodHeight, rodDepth, 0F);
        rod4.setRotationPoint(rodRP.x, rodRP.y, rodRP.z);
        rod4.rotateAngleY = (float) -Math.PI / 2;
        motor.addChild(rod4);

        coiling = new ModelRenderer(this, 40, 0).setTextureSize(textureWidth, textureHeight);
        coiling.addBox(-3F, -4F, -3F, 6, 4, 6, 0F);
        coiling.setRotationPoint(0F, 0F, 0F);
        motor.addChild(coiling);

        int wiperOffsetX = 48;
        int wiperOffsetY = 10;
        Vector3f wiperOrigin = new Vector3f(-4F, 0F, -4.5F);
        int wiperWidth = 8;
        int wiperHeight = 3;
        int wiperDepth = 0;
        Vector3f wiperRP = new Vector3f(0F, 0F, 0F);

        wipers = new ModelRenderer(this, 0, 0);

        wiper1 = new ModelRenderer(this, wiperOffsetX, wiperOffsetY).setTextureSize(textureWidth, textureHeight);
        wiper1.addBox(wiperOrigin.x, wiperOrigin.y, wiperOrigin.z, wiperWidth, wiperHeight, wiperDepth, 0F);
        wiper1.setRotationPoint(wiperRP.x, wiperRP.y, wiperRP.z);
        wipers.addChild(wiper1);

        wiper2 = new ModelRenderer(this, wiperOffsetX, wiperOffsetY).setTextureSize(textureWidth, textureHeight);
        wiper2.addBox(wiperOrigin.x, wiperOrigin.y, wiperOrigin.z, wiperWidth, wiperHeight, wiperDepth, 0F);
        wiper2.setRotationPoint(wiperRP.x, wiperRP.y, wiperRP.z);
        wiper2.rotateAngleY = (float) Math.PI / 2;
        wipers.addChild(wiper2);

        wiper3 = new ModelRenderer(this, wiperOffsetX, wiperOffsetY).setTextureSize(textureWidth, textureHeight);
        wiper3.addBox(wiperOrigin.x, wiperOrigin.y, wiperOrigin.z, wiperWidth, wiperHeight, wiperDepth, 0F);
        wiper3.setRotationPoint(wiperRP.x, wiperRP.y, wiperRP.z);
        wiper3.rotateAngleY = (float) Math.PI;
        wipers.addChild(wiper3);

        wiper4 = new ModelRenderer(this, wiperOffsetX, wiperOffsetY).setTextureSize(textureWidth, textureHeight);
        wiper4.addBox(wiperOrigin.x, wiperOrigin.y, wiperOrigin.z, wiperWidth, wiperHeight, wiperDepth, 0F);
        wiper4.setRotationPoint(wiperRP.x, wiperRP.y, wiperRP.z);
        wiper4.rotateAngleY = (float) -Math.PI / 2;
        wipers.addChild(wiper4);

        motor.addChild(wipers);

        face = Direction.UP;
    }

    public CreativeMotorModel setFace(Direction direction) {
        shaft = new ShaftModel(direction.getAxisDirection());
        shaft.setAxis(direction.getAxis());
        face = direction;
        double rotationX = 0f;
        double rotationY = 0f;
        double rotationZ = 0f;
        if (direction.getAxis() == Axis.Y) {
            sideCover2.isHidden = stand.isHidden = true;
            bottomCover.isHidden = false;
            wipers.offsetY = 0f;
            if (direction == Direction.DOWN) rotationX = Math.PI;
        } else {
            sideCover2.isHidden = stand.isHidden = false;
            bottomCover.isHidden = true;
            wipers.offsetY = 2f / 16f;
            switch (direction) {
                default:
                case EAST:
                    rotationZ = -Math.PI / 2;
                    rotationY = Math.PI;
                    break;
                case WEST:
                    rotationZ = Math.PI / 2;
                    break;
                case SOUTH:
                    rotationX = -Math.PI / 2;
                    rotationY = -Math.PI / 2;
                    break;
                case NORTH:
                    rotationX = Math.PI / 2;
                    rotationY = Math.PI / 2;
                    break;
            }
        }
        motor.rotateAngleX = (float) rotationX;
        stand.rotateAngleY = (float) rotationY;
        motor.rotateAngleZ = (float) rotationZ;
        return this;
    }

    public CreativeMotorModel setRotation(float angle) {
        shaft.setRotation(angle);
        return this;
    }

    public void renderCore() {
        GL11.glPushMatrix();

        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

        motor.render(0.0625F);
        stand.render(0.0625F);

        GL11.glPopMatrix();
    }

    public void render() {
        AllModelTextures.CREATIVE_MOTOR.bind();
        renderCore();
    }

    public void render(TileEntitySpecialRenderer renderer) {
        renderer.bindTexture(AllModelTextures.CREATIVE_MOTOR.getLocation());
        renderCore();
    }

}
