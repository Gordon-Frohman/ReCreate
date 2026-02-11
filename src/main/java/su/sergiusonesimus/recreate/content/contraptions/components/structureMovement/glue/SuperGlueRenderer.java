package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.AllItems;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.foundation.utility.AngleHelper;
import su.sergiusonesimus.recreate.util.VecHelper;

public class SuperGlueRenderer extends Render {

    private ResourceLocation regular = ReCreate.asResource("textures/entity/super_glue/slime.png");

    private float[] insideQuad;
    private float[] outsideQuad;

    public SuperGlueRenderer() {
        super();
        initQuads();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return regular;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        SuperGlueEntity glueEntity = (SuperGlueEntity) entity;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        boolean visible = glueEntity.isVisible();
        ItemStack heldItem = player.getHeldItem();
        boolean holdingGlue = heldItem != null && heldItem.getItem() == AllItems.super_glue;

        if (!visible && !holdingGlue) return;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        double offset = 0.523D;
        ChunkCoordinates normal = glueEntity.facingDirection.getOpposite()
            .getNormal();
        GL11.glTranslated(offset * normal.posX, offset * normal.posY, offset * normal.posZ);

        bindEntityTexture(glueEntity);
        int light = getBrightnessForRender(glueEntity);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light % 65536, light / 65536);

        Direction face = glueEntity.getFacingDirection();

        GL11.glRotatef(AngleHelper.horizontalAngle(face), 0, 1, 0);
        GL11.glRotatef(AngleHelper.verticalAngle(face), 1, 0, 0);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        renderQuad(tessellator, insideQuad, light, -1);
        renderQuad(tessellator, outsideQuad, light, 1);

        tessellator.draw();

        GL11.glPopMatrix();
    }

    private void initQuads() {
        ChunkCoordinates normal = Direction.SOUTH.getNormal();
        Vec3 diff = Vec3.createVectorHelper(normal.posX, normal.posY, normal.posZ);

        float multiplier = 1 / 32f - 1 / 128f;
        Vec3 extension = diff.normalize();
        extension = Vec3.createVectorHelper(
            extension.xCoord * multiplier,
            extension.yCoord * multiplier,
            extension.zCoord * multiplier);

        Vec3 plane = VecHelper.axisAlingedPlaneOf(diff);
        Direction dir = Direction.getNearest((int) diff.xCoord, (int) diff.yCoord, (int) diff.zCoord);

        Vec3 start = Vec3.createVectorHelper(-extension.xCoord, -extension.yCoord, -extension.zCoord);
        Vec3 end = Vec3.createVectorHelper(extension.xCoord, extension.yCoord, extension.zCoord);

        multiplier = 1 / 2f;
        plane = Vec3
            .createVectorHelper(plane.xCoord * multiplier, plane.yCoord * multiplier, plane.zCoord * multiplier);
        Vec3 a1 = plane.addVector(start.xCoord, start.yCoord, start.zCoord);
        Vec3 b1 = plane.addVector(end.xCoord, end.yCoord, end.zCoord);
        plane = VecHelper.rotate(plane, -90, dir.getAxis());
        Vec3 a2 = plane.addVector(start.xCoord, start.yCoord, start.zCoord);
        Vec3 b2 = plane.addVector(end.xCoord, end.yCoord, end.zCoord);
        plane = VecHelper.rotate(plane, -90, dir.getAxis());
        Vec3 a3 = plane.addVector(start.xCoord, start.yCoord, start.zCoord);
        Vec3 b3 = plane.addVector(end.xCoord, end.yCoord, end.zCoord);
        plane = VecHelper.rotate(plane, -90, dir.getAxis());
        Vec3 a4 = plane.addVector(start.xCoord, start.yCoord, start.zCoord);
        Vec3 b4 = plane.addVector(end.xCoord, end.yCoord, end.zCoord);

        insideQuad = new float[] { (float) a1.xCoord, (float) a1.yCoord, (float) a1.zCoord, 1, 0, (float) a2.xCoord,
            (float) a2.yCoord, (float) a2.zCoord, 1, 1, (float) a3.xCoord, (float) a3.yCoord, (float) a3.zCoord, 0, 1,
            (float) a4.xCoord, (float) a4.yCoord, (float) a4.zCoord, 0, 0 };

        outsideQuad = new float[] { (float) b4.xCoord, (float) b4.yCoord, (float) b4.zCoord, 0, 0, (float) b3.xCoord,
            (float) b3.yCoord, (float) b3.zCoord, 0, 1, (float) b2.xCoord, (float) b2.yCoord, (float) b2.zCoord, 1, 1,
            (float) b1.xCoord, (float) b1.yCoord, (float) b1.zCoord, 1, 0 };
    }

    private int getBrightnessForRender(SuperGlueEntity entity) {
        int blockposX = entity.hangingPositionX;
        int blockposY = entity.hangingPositionY;
        int blockposZ = entity.hangingPositionZ;

        ChunkCoordinates dir = entity.getFacingDirection()
            .getNormal();
        int blockpos2X = blockposX + dir.posX;
        int blockpos2Y = blockposY + dir.posY;
        int blockpos2Z = blockposZ + dir.posZ;

        int light = entity.worldObj.getLightBrightnessForSkyBlocks(blockposX, blockposY, blockposZ, 0);
        int light2 = entity.worldObj.getLightBrightnessForSkyBlocks(blockpos2X, blockpos2Y, blockpos2Z, 0);
        return Math.max(light, light2);
    }

    private void renderQuad(Tessellator tessellator, float[] data, int light, float normalZ) {
        for (int i = 0; i < 4; i++) {
            tessellator
                .addVertexWithUV(data[5 * i], data[5 * i + 1], data[5 * i + 2], data[5 * i + 3], data[5 * i + 4]);
            tessellator.setColorRGBA(255, 255, 255, 255);
            tessellator.setBrightness(light);
            tessellator.setNormal(0.0f, 0.0f, normalZ);
        }
    }
}
