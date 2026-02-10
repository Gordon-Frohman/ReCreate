package su.sergiusonesimus.recreate.foundation.utility.outliner;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.vecmath.Matrix3f;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.AllSpecialTextures;
import su.sergiusonesimus.recreate.foundation.render.RenderTypes;
import su.sergiusonesimus.recreate.foundation.utility.Color;
import su.sergiusonesimus.recreate.util.VecHelper;

public abstract class Outline {

    protected OutlineParams params;
    protected Matrix3f transformNormals; // TODO: not used?

    public Outline() {
        params = new OutlineParams();
    }

    public abstract void render(float pt);

    public void tick() {}

    public OutlineParams getParams() {
        return params;
    }

    public void renderCuboidLine(Vec3 start, Vec3 end) {
        Vec3 diff = Vec3
            .createVectorHelper(end.xCoord - start.xCoord, end.yCoord - start.yCoord, end.zCoord - start.zCoord);

        float hAngle = (float) Math.toDegrees(Math.atan2(diff.xCoord, diff.zCoord));
        float hDistance = (float) Vec3.createVectorHelper(diff.xCoord, 0, diff.zCoord)
            .lengthVector();
        float vAngle = (float) Math.toDegrees(Math.atan2(hDistance, diff.yCoord)) - 90;

        GL11.glPushMatrix();

        GL11.glTranslated(start.xCoord, start.yCoord, start.zCoord);
        GL11.glRotatef(hAngle, 0, 1, 0);
        GL11.glRotatef(vAngle, 1, 0, 0);

        renderAACuboidLine(Vec3.createVectorHelper(0, 0, 0), Vec3.createVectorHelper(0, 0, diff.lengthVector()));

        GL11.glPopMatrix();
    }

    public void renderAACuboidLine(Vec3 start, Vec3 end) {
        float lineWidth = params.getLineWidth();
        if (lineWidth == 0) return;

        RenderTypes.setupOutlineSolid();

        Vec3 diff = start.subtract(end);
        if (diff.xCoord + diff.yCoord + diff.zCoord < 0) {
            Vec3 temp = start;
            start = end;
            end = temp;
            diff = Vec3.createVectorHelper(-diff.xCoord, -diff.yCoord, -diff.zCoord);
        }

        float modifier = lineWidth / 2;
        Vec3 extension = diff.normalize();
        extension = Vec3
            .createVectorHelper(extension.xCoord * modifier, extension.yCoord * modifier, extension.zCoord * modifier);
        Vec3 plane = VecHelper.axisAlingedPlaneOf(diff);
        Direction face = Direction.getNearest(diff.xCoord, diff.yCoord, diff.zCoord);
        Axis axis = face.getAxis();

        start = extension.subtract(start);
        end = end.addVector(extension.xCoord, extension.yCoord, extension.zCoord);
        plane = Vec3.createVectorHelper(plane.xCoord * modifier, plane.yCoord * modifier, plane.zCoord * modifier);

        Vec3 a1 = plane.addVector(start.xCoord, start.yCoord, start.zCoord);
        Vec3 b1 = plane.addVector(end.xCoord, end.yCoord, end.zCoord);
        plane = VecHelper.rotate(plane, -90, axis);
        Vec3 a2 = plane.addVector(start.xCoord, start.yCoord, start.zCoord);
        Vec3 b2 = plane.addVector(end.xCoord, end.yCoord, end.zCoord);
        plane = VecHelper.rotate(plane, -90, axis);
        Vec3 a3 = plane.addVector(start.xCoord, start.yCoord, start.zCoord);
        Vec3 b3 = plane.addVector(end.xCoord, end.yCoord, end.zCoord);
        plane = VecHelper.rotate(plane, -90, axis);
        Vec3 a4 = plane.addVector(start.xCoord, start.yCoord, start.zCoord);
        Vec3 b4 = plane.addVector(end.xCoord, end.yCoord, end.zCoord);

        if (params.disableNormals) {
            face = Direction.UP;
            putQuad(b4, b3, b2, b1, face);
            putQuad(a1, a2, a3, a4, face);
            putQuad(a1, b1, b2, a2, face);
            putQuad(a2, b2, b3, a3, face);
            putQuad(a3, b3, b4, a4, face);
            putQuad(a4, b4, b1, a1, face);
            return;
        }

        putQuad(b4, b3, b2, b1, face);
        putQuad(a1, a2, a3, a4, face.getOpposite());
        Vec3 vec = a4.subtract(a1);
        face = Direction.getNearest(vec.xCoord, vec.yCoord, vec.zCoord);
        putQuad(a1, b1, b2, a2, face);
        vec = VecHelper.rotate(vec, -90, axis);
        face = Direction.getNearest(vec.xCoord, vec.yCoord, vec.zCoord);
        putQuad(a2, b2, b3, a3, face);
        vec = VecHelper.rotate(vec, -90, axis);
        face = Direction.getNearest(vec.xCoord, vec.yCoord, vec.zCoord);
        putQuad(a3, b3, b4, a4, face);
        vec = VecHelper.rotate(vec, -90, axis);
        face = Direction.getNearest(vec.xCoord, vec.yCoord, vec.zCoord);
        putQuad(a4, b4, b1, a1, face);

        RenderTypes.cleanUp();
    }

    public void putQuad(Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, Direction normal) {
        putQuadUV(v1, v2, v3, v4, 0, 0, 1, 1, normal.toForgeDirection());
    }

    public void putQuadUV(Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, float minU, float minV, float maxU, float maxV,
        ForgeDirection normal) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        Color rgb = params.rgb;
        tessellator.setColorRGBA(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), (int) (rgb.getAlpha() * params.alpha));
        tessellator.setBrightness(params.lightMap);
        if (normal != null) {
            tessellator.setNormal(normal.offsetX, normal.offsetY, normal.offsetZ);
        }
        putVertex(tessellator, v1, minU, minV);
        putVertex(tessellator, v2, maxU, minV);
        putVertex(tessellator, v3, maxU, maxV);
        putVertex(tessellator, v4, minU, maxV);
        tessellator.draw();
    }

    protected void putVertex(Tessellator tessellator, Vec3 pos, float u, float v) {
        tessellator.addVertexWithUV((float) pos.xCoord, (float) pos.yCoord, (float) pos.zCoord, u, v);
    }

    public static class OutlineParams {

        protected Optional<AllSpecialTextures> faceTexture;
        protected Optional<AllSpecialTextures> hightlightedFaceTexture;
        protected Direction highlightedFace;
        protected boolean fadeLineWidth;
        protected boolean disableCull;
        protected boolean disableNormals;
        protected float alpha;
        protected int lightMap;
        protected Color rgb;
        private float lineWidth;

        public OutlineParams() {
            faceTexture = hightlightedFaceTexture = Optional.empty();
            alpha = 1;
            lineWidth = 1 / 32f;
            fadeLineWidth = true;
            rgb = Color.WHITE;
            lightMap = 15728880;
        }

        // builder

        public OutlineParams colored(int color) {
            rgb = new Color(color, false);
            return this;
        }

        public OutlineParams colored(Color c) {
            rgb = c.copy();
            return this;
        }

        public OutlineParams lightMap(int light) {
            lightMap = light;
            return this;
        }

        public OutlineParams lineWidth(float width) {
            this.lineWidth = width;
            return this;
        }

        public OutlineParams withFaceTexture(AllSpecialTextures texture) {
            this.faceTexture = Optional.ofNullable(texture);
            return this;
        }

        public OutlineParams clearTextures() {
            return this.withFaceTextures(null, null);
        }

        public OutlineParams withFaceTextures(AllSpecialTextures texture, AllSpecialTextures highlightTexture) {
            this.faceTexture = Optional.ofNullable(texture);
            this.hightlightedFaceTexture = Optional.ofNullable(highlightTexture);
            return this;
        }

        public OutlineParams highlightFace(@Nullable Direction face) {
            highlightedFace = face;
            return this;
        }

        public OutlineParams disableNormals() {
            disableNormals = true;
            return this;
        }

        public OutlineParams disableCull() {
            disableCull = true;
            return this;
        }

        // getter

        public float getLineWidth() {
            return fadeLineWidth ? alpha * lineWidth : lineWidth;
        }

        public Direction getHighlightedFace() {
            return highlightedFace;
        }

    }

}
