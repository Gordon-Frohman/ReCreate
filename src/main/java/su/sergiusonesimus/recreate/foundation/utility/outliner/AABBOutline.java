package su.sergiusonesimus.recreate.foundation.utility.outliner;

import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction.Axis;

public class AABBOutline extends Outline {

    protected AxisAlignedBB bb;

    public AABBOutline(AxisAlignedBB bb) {
        this.setBounds(bb);
    }

    @Override
    public void render(float pt) {
        renderBB(bb);
    }

    public void renderBB(AxisAlignedBB bb) {
        Vec3 projectedView = Minecraft.getMinecraft().renderViewEntity.getPosition(1);
        boolean noCull = bb.isVecInside(projectedView);
        double inflator = noCull ? -1 / 128d : 1 / 128d;
        bb = bb.expand(inflator, inflator, inflator);
        noCull |= params.disableCull;

        Vec3 xyz = Vec3.createVectorHelper(bb.minX, bb.minY, bb.minZ);
        Vec3 Xyz = Vec3.createVectorHelper(bb.maxX, bb.minY, bb.minZ);
        Vec3 xYz = Vec3.createVectorHelper(bb.minX, bb.maxY, bb.minZ);
        Vec3 XYz = Vec3.createVectorHelper(bb.maxX, bb.maxY, bb.minZ);
        Vec3 xyZ = Vec3.createVectorHelper(bb.minX, bb.minY, bb.maxZ);
        Vec3 XyZ = Vec3.createVectorHelper(bb.maxX, bb.minY, bb.maxZ);
        Vec3 xYZ = Vec3.createVectorHelper(bb.minX, bb.maxY, bb.maxZ);
        Vec3 XYZ = Vec3.createVectorHelper(bb.maxX, bb.maxY, bb.maxZ);

        Vec3 start = xyz;
        renderAACuboidLine(start, Xyz);
        renderAACuboidLine(start, xYz);
        renderAACuboidLine(start, xyZ);

        start = XyZ;
        renderAACuboidLine(start, xyZ);
        renderAACuboidLine(start, XYZ);
        renderAACuboidLine(start, Xyz);

        start = XYz;
        renderAACuboidLine(start, xYz);
        renderAACuboidLine(start, Xyz);
        renderAACuboidLine(start, XYZ);

        start = xYZ;
        renderAACuboidLine(start, XYZ);
        renderAACuboidLine(start, xyZ);
        renderAACuboidLine(start, xYz);

        renderFace(ForgeDirection.NORTH, xYz, XYz, Xyz, xyz, noCull);
        renderFace(ForgeDirection.SOUTH, XYZ, xYZ, xyZ, XyZ, noCull);
        renderFace(ForgeDirection.EAST, XYz, XYZ, XyZ, Xyz, noCull);
        renderFace(ForgeDirection.WEST, xYZ, xYz, xyz, xyZ, noCull);
        renderFace(ForgeDirection.UP, xYZ, XYZ, XYz, xYz, noCull);
        renderFace(ForgeDirection.DOWN, xyz, Xyz, XyZ, xyZ, noCull);

    }

    protected void renderFace(ForgeDirection direction, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, boolean noCull) {
        if (!params.faceTexture.isPresent()) return;

        float alphaBefore = params.alpha;

        params.alpha = (direction == params.getHighlightedFace()
            .toForgeDirection() && params.hightlightedFaceTexture.isPresent()) ? 1.0f : 0.5f;

        params.faceTexture.get()
            .bind();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (noCull) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }

        Axis axis = Axis.values()[direction.ordinal() % 3];
        Vec3 uDiff = Vec3.createVectorHelper(p2.xCoord - p1.xCoord, p2.yCoord - p1.yCoord, p2.zCoord - p1.zCoord);
        Vec3 vDiff = Vec3.createVectorHelper(p4.xCoord - p1.xCoord, p4.yCoord - p1.yCoord, p4.zCoord - p1.zCoord);

        float maxU = (float) Math.abs(axis == Axis.X ? uDiff.zCoord : uDiff.xCoord);
        float maxV = (float) Math.abs(axis == Axis.Y ? vDiff.zCoord : vDiff.yCoord);

        putQuadUV(p1, p2, p3, p4, 0, 0, maxU, maxV, ForgeDirection.UP);

        if (noCull) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        }
        params.alpha = alphaBefore;
    }

    public void setBounds(AxisAlignedBB bb) {
        this.bb = bb;
    }

}
