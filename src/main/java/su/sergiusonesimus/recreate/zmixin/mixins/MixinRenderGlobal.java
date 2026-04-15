package su.sergiusonesimus.recreate.zmixin.mixins;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Segment;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import su.sergiusonesimus.metaworlds.util.GeometryHelper3D;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinAxisAlignedBB;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.util.IMixinMovingObjectPosition;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
import su.sergiusonesimus.recreate.zmixin.interfaces.IMixinBlock;

@Mixin(value = RenderGlobal.class, priority = 1100)
public class MixinRenderGlobal {

    private static List<Segment> excludedSegments = new ArrayList<Segment>();

    @Inject(
        method = "drawSelectionBox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;drawOutlinedBoundingBox(Lnet/minecraft/util/AxisAlignedBB;I)V",
            opcode = Opcodes.INVOKESTATIC,
            shift = Shift.BEFORE))
    private void shareVariables(EntityPlayer entityPlayer, MovingObjectPosition rayTraceHit, int i,
        float partialTickTime, CallbackInfo ci, @Local(name = "block") Block block, @Local(name = "f1") float f1,
        @Local(name = "d0") double d0, @Local(name = "d1") double d1, @Local(name = "d2") double d2,
        @Share("rayTraceHit") LocalRef<MovingObjectPosition> sharedRayTraceHit,
        @Share("block") LocalRef<Block> sharedBlock, @Share("f1") LocalFloatRef sharedF1,
        @Share("d0") LocalDoubleRef sharedD0, @Share("d1") LocalDoubleRef sharedD1,
        @Share("d2") LocalDoubleRef sharedD2) {
        sharedRayTraceHit.set(rayTraceHit);
        sharedBlock.set(block);
        sharedF1.set(f1);
        sharedD0.set(d0);
        sharedD1.set(d1);
        sharedD2.set(d2);
    }

    @WrapOperation(
        method = "drawSelectionBox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderGlobal;drawOutlinedBoundingBox(Lnet/minecraft/util/AxisAlignedBB;I)V",
            opcode = Opcodes.INVOKESTATIC))
    private void wrapDrawOutlinedBoundingBox(AxisAlignedBB aabb, int localI, Operation<Void> original,
        @Share("rayTraceHit") LocalRef<MovingObjectPosition> sharedRayTraceHit,
        @Share("block") LocalRef<Block> sharedBlock, @Share("f1") LocalFloatRef sharedF1,
        @Share("d0") LocalDoubleRef sharedD0, @Share("d1") LocalDoubleRef sharedD1,
        @Share("d2") LocalDoubleRef sharedD2) {
        MovingObjectPosition rayTraceHit = sharedRayTraceHit.get();
        float f1 = sharedF1.get();
        double d0 = sharedD0.get();
        double d1 = sharedD1.get();
        double d2 = sharedD2.get();
        List<AxisAlignedBB> bbList = ((IMixinBlock) sharedBlock.get()).getSelectedBoundingBoxesList(
            ((IMixinMovingObjectPosition) rayTraceHit).getWorld(),
            rayTraceHit.blockX,
            rayTraceHit.blockY,
            rayTraceHit.blockZ);
        for (AxisAlignedBB partAABB : bbList) {
            boolean expandX = true;
            boolean expandY = true;
            boolean expandZ = true;
            for (AxisAlignedBB partAABB1 : bbList) {
                if (partAABB1 == partAABB) continue;
                if (partAABB1.minX == partAABB.maxX || partAABB1.maxX == partAABB.minX) expandX = false;
                if (partAABB1.minY == partAABB.maxY || partAABB1.maxY == partAABB.minY) expandY = false;
                if (partAABB1.minZ == partAABB.maxZ || partAABB1.maxZ == partAABB.minZ) expandZ = false;
            }
            for (AxisAlignedBB partAABB1 : bbList) {
                if (partAABB1 == partAABB) continue;
                findExcludedSegments(
                    partAABB.expand(expandX ? (double) f1 : 0, expandY ? (double) f1 : 0, expandZ ? (double) f1 : 0),
                    partAABB1.expand(expandX ? (double) f1 : 0, expandY ? (double) f1 : 0, expandZ ? (double) f1 : 0),
                    rayTraceHit,
                    d0,
                    d1,
                    d2);
            }
            original.call(
                ((IMixinAxisAlignedBB) partAABB
                    .expand(expandX ? (double) f1 : 0, expandY ? (double) f1 : 0, expandZ ? (double) f1 : 0))
                        .getTransformedToGlobalBoundingBox(((IMixinMovingObjectPosition) rayTraceHit).getWorld())
                        .offset(-d0, -d1, -d2),
                -1);
        }
        excludedSegments.clear();
    }

    private void findExcludedSegments(AxisAlignedBB aabb1, AxisAlignedBB aabb2, MovingObjectPosition rayTraceHit,
        double d0, double d1, double d2) {
        List<Segment> segments1 = new ArrayList<Segment>();
        List<Segment> segments2 = new ArrayList<Segment>();
        for (boolean first : Iterate.trueAndFalse) {
            AxisAlignedBB currentBB = first ? aabb1 : aabb2;
            List<Segment> currentSegments = first ? segments1 : segments2;
            Vector3D[] vertices = new Vector3D[8];
            vertices[0] = new Vector3D(currentBB.minX, currentBB.minY, currentBB.minZ);
            vertices[1] = new Vector3D(currentBB.maxX, currentBB.minY, currentBB.minZ);
            vertices[2] = new Vector3D(currentBB.maxX, currentBB.minY, currentBB.maxZ);
            vertices[3] = new Vector3D(currentBB.minX, currentBB.minY, currentBB.maxZ);
            vertices[4] = new Vector3D(currentBB.minX, currentBB.maxY, currentBB.minZ);
            vertices[5] = new Vector3D(currentBB.maxX, currentBB.maxY, currentBB.minZ);
            vertices[6] = new Vector3D(currentBB.maxX, currentBB.maxY, currentBB.maxZ);
            vertices[7] = new Vector3D(currentBB.minX, currentBB.maxY, currentBB.maxZ);
            for (int i = 0; i < 4; i++) {
                int nextI = (i + 1) % 4;
                currentSegments.add(new Segment(vertices[i], vertices[nextI], new Line(vertices[i], vertices[nextI])));
                currentSegments.add(
                    new Segment(vertices[i + 4], vertices[nextI + 4], new Line(vertices[i + 4], vertices[nextI + 4])));
                currentSegments.add(new Segment(vertices[i], vertices[i + 4], new Line(vertices[i], vertices[i + 4])));
            }
        }
        for (Segment segment1 : segments1) {
            for (Segment segment2 : segments2) {
                Line line = segment1.getLine();
                if (line.contains(segment2.getStart()) && line.contains(segment2.getEnd())) {
                    double start1 = line.getAbscissa(segment1.getStart());
                    double end1 = line.getAbscissa(segment1.getEnd());
                    if (start1 > end1) {
                        double temp = start1;
                        start1 = end1;
                        end1 = temp;
                    }
                    double start2 = line.getAbscissa(segment2.getStart());
                    double end2 = line.getAbscissa(segment2.getEnd());
                    if (start2 > end2) {
                        double temp = start2;
                        start2 = end2;
                        end2 = temp;
                    }
                    if (start1 < end2 && end1 > start2) {
                        Vector3D segmentStart;
                        Vector3D segmentEnd;
                        if (start1 > start2) {
                            segmentStart = segment1.getStart();
                            segmentEnd = end1 < end2 ? segment1.getEnd() : segment2.getEnd();
                        } else {
                            segmentStart = segment2.getStart();
                            segmentEnd = end2 < end1 ? segment2.getEnd() : segment1.getEnd();
                        }
                        if (!segmentStart.equals(segmentEnd)) {
                            Vec3 sStart = ((IMixinWorld) ((IMixinMovingObjectPosition) rayTraceHit).getWorld())
                                .transformToGlobal(segmentStart.getX(), segmentStart.getY(), segmentStart.getZ())
                                .addVector(-d0, -d1, -d2);
                            Vec3 sEnd = ((IMixinWorld) ((IMixinMovingObjectPosition) rayTraceHit).getWorld())
                                .transformToGlobal(segmentEnd.getX(), segmentEnd.getY(), segmentEnd.getZ())
                                .addVector(-d0, -d1, -d2);
                            segmentStart = GeometryHelper3D.transformVector(sStart);
                            segmentEnd = GeometryHelper3D.transformVector(sEnd);
                            line = new Line(segmentStart, segmentEnd);
                            excludedSegments.add(new Segment(segmentStart, segmentEnd, line));
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "drawOutlinedBoundingBox", at = @At(value = "HEAD"))
    private static void shareColor(AxisAlignedBB aabb, int color, CallbackInfo ci,
        @Share("color") LocalIntRef sharedColor) {
        sharedColor.set(color);
    }

    @WrapOperation(
        method = "drawOutlinedBoundingBox",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;startDrawing(I)V"))
    private static void shareDrawMode(Tessellator tessellator, int mode, Operation<Void> original,
        @Share("drawMode") LocalIntRef sharedDrawMode) {
        sharedDrawMode.set(mode);
        original.call(tessellator, mode);
    }

    @WrapOperation(
        method = "drawOutlinedBoundingBox",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;addVertex(DDD)V"))
    private static void checkForExcludedSegments(Tessellator tessellator, double x, double y, double z,
        Operation<Void> original, @Share("color") LocalIntRef sharedColor,
        @Share("drawMode") LocalIntRef sharedDrawMode, @Share("lastPos") LocalRef<Vec3> sharedLastPos) {
        Vec3 lastPos = sharedLastPos.get();
        if (lastPos != null) checkForExcludedSegments(
            tessellator,
            sharedDrawMode.get(),
            sharedColor.get(),
            lastPos.xCoord,
            lastPos.yCoord,
            lastPos.zCoord,
            x,
            y,
            z);
        original.call(tessellator, x, y, z);
        sharedLastPos.set(Vec3.createVectorHelper(x, y, z));
    }

    @Inject(
        method = "drawOutlinedBoundingBox",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;draw()I"))
    private static void clearLastVertex(AxisAlignedBB aabb, int color, CallbackInfo ci,
        @Share("lastPos") LocalRef<Vec3> sharedLastPos) {
        sharedLastPos.set(null);
    }

    private static void checkForExcludedSegments(Tessellator tessellator, int drawMode, int color, double startX,
        double startY, double startZ, double endX, double endY, double endZ) {
        Vector3D start = new Vector3D(startX, startY, startZ);
        Vector3D end = new Vector3D(endX, endY, endZ);
        Line line = new Line(start, end);
        for (Segment segment : excludedSegments) {
            Vector3D sStart = segment.getStart();
            Vector3D sEnd = segment.getEnd();
            if (line.contains(sStart) && line.contains(sEnd)) {
                double start1 = line.getAbscissa(start);
                double end1 = line.getAbscissa(end);
                if (line.getAbscissa(sStart) > line.getAbscissa(sEnd)) {
                    Vector3D temp = sStart;
                    sStart = sEnd;
                    sEnd = temp;
                }
                double start2 = line.getAbscissa(sStart);
                double end2 = line.getAbscissa(sEnd);
                if (start1 < end2 && end1 > start2) {
                    if (start1 >= start2) {
                        if (end1 <= end2) {
                            tessellator.addVertex(start.getX(), start.getY(), start.getZ());

                            tessellator.draw();
                            tessellator.startDrawing(drawMode);

                            if (color != -1) {
                                tessellator.setColorOpaque_I(color);
                            }

                            tessellator.addVertex(end.getX(), end.getY(), end.getZ());
                            return;
                        } else {
                            tessellator.addVertex(start.getX(), start.getY(), start.getZ());

                            tessellator.draw();
                            tessellator.startDrawing(drawMode);

                            if (color != -1) {
                                tessellator.setColorOpaque_I(color);
                            }

                            tessellator.addVertex(sEnd.getX(), sEnd.getY(), sEnd.getZ());
                            return;
                        }
                    } else {
                        if (end2 < end1) {
                            tessellator.addVertex(sStart.getX(), sStart.getY(), sStart.getZ());

                            tessellator.draw();
                            tessellator.startDrawing(drawMode);

                            if (color != -1) {
                                tessellator.setColorOpaque_I(color);
                            }

                            tessellator.addVertex(sEnd.getX(), sEnd.getY(), sEnd.getZ());
                            return;
                        } else {
                            tessellator.addVertex(sStart.getX(), sStart.getY(), sStart.getZ());

                            tessellator.draw();
                            tessellator.startDrawing(drawMode);

                            if (color != -1) {
                                tessellator.setColorOpaque_I(color);
                            }

                            tessellator.addVertex(end.getX(), end.getY(), end.getZ());
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Plays a pre-canned sound effect along with potentially auxiliary data-driven one-shot behaviour (particles, etc).
     */
    @Inject(method = "playAuxSFX", at = @At(value = "TAIL"))
    private void playAuxSFX(EntityPlayer player, int id, int x, int y, int z, int data, CallbackInfo ci) {
        switch (id) {
            // Rotation indicator
            case 3000:

                break;
        }
    }

}
