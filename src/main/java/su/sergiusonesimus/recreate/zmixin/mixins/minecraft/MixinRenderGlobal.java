package su.sergiusonesimus.recreate.zmixin.mixins.minecraft;

import java.lang.reflect.Field;
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
                if (!expandX && !expandY && !expandZ) break;
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

    private static final ThreadLocal<Vector3D[]> VERTICES1_POOL = ThreadLocal.withInitial(() -> {
        Vector3D[] arr = new Vector3D[8];
        for (int i = 0; i < 8; i++) {
            arr[i] = new Vector3D(0, 0, 0);
        }
        return arr;
    });

    private static final ThreadLocal<Vector3D[]> VERTICES2_POOL = ThreadLocal.withInitial(() -> {
        Vector3D[] arr = new Vector3D[8];
        for (int i = 0; i < 8; i++) {
            arr[i] = new Vector3D(0, 0, 0);
        }
        return arr;
    });

    private static final ThreadLocal<Segment[]> SEGMENTS1_POOL = ThreadLocal.withInitial(() -> new Segment[12]);
    private static final ThreadLocal<Segment[]> SEGMENTS2_POOL = ThreadLocal.withInitial(() -> new Segment[12]);

    private Vector3D updateVector(Vector3D existing, double x, double y, double z) {
        try {
            Field xField = Vector3D.class.getDeclaredField("x");
            xField.setAccessible(true);
            xField.set(existing, x);
            Field yField = Vector3D.class.getDeclaredField("y");
            yField.setAccessible(true);
            yField.set(existing, y);
            Field zField = Vector3D.class.getDeclaredField("z");
            zField.setAccessible(true);
            zField.set(existing, z);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return existing;
    }

    private Segment updateOrCreateSegment(Segment existing, Vector3D start, Vector3D end) {
        if (existing == null) return new Segment(start, end, new Line(start, end));
        if (existing.getStart() != start) {
            try {
                Field startField = Segment.class.getDeclaredField("start");
                startField.setAccessible(true);
                startField.set(existing, start);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (existing.getEnd() != end) {
            try {
                Field endField = Segment.class.getDeclaredField("end");
                endField.setAccessible(true);
                endField.set(existing, end);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        existing.getLine()
            .reset(start, end);
        return existing;
    }

    private void findExcludedSegments(AxisAlignedBB aabb1, AxisAlignedBB aabb2, MovingObjectPosition rayTraceHit,
        double d0, double d1, double d2) {
        Segment[] segments1 = SEGMENTS1_POOL.get();
        Segment[] segments2 = SEGMENTS2_POOL.get();

        Vector3D[] vertices1 = VERTICES1_POOL.get();
        Vector3D[] vertices2 = VERTICES2_POOL.get();

        for (boolean first : Iterate.trueAndFalse) {
            AxisAlignedBB currentBB = first ? aabb1 : aabb2;
            Segment[] currentSegments = first ? segments1 : segments2;
            Vector3D[] currentVertices = first ? vertices1 : vertices2;

            currentVertices[0] = updateVector(currentVertices[0], currentBB.minX, currentBB.minY, currentBB.minZ);
            currentVertices[1] = updateVector(currentVertices[1], currentBB.maxX, currentBB.minY, currentBB.minZ);
            currentVertices[2] = updateVector(currentVertices[2], currentBB.maxX, currentBB.minY, currentBB.maxZ);
            currentVertices[3] = updateVector(currentVertices[3], currentBB.minX, currentBB.minY, currentBB.maxZ);
            currentVertices[4] = updateVector(currentVertices[4], currentBB.minX, currentBB.maxY, currentBB.minZ);
            currentVertices[5] = updateVector(currentVertices[5], currentBB.maxX, currentBB.maxY, currentBB.minZ);
            currentVertices[6] = updateVector(currentVertices[6], currentBB.maxX, currentBB.maxY, currentBB.maxZ);
            currentVertices[7] = updateVector(currentVertices[7], currentBB.minX, currentBB.maxY, currentBB.maxZ);

            for (int i = 0; i < 4; i++) {
                int nextI = (i + 1) % 4;
                int j = i * 3;
                currentSegments[j] = updateOrCreateSegment(
                    currentSegments[j],
                    currentVertices[i],
                    currentVertices[nextI]);
                j++;
                currentSegments[j] = updateOrCreateSegment(
                    currentSegments[j],
                    currentVertices[i + 4],
                    currentVertices[nextI + 4]);
                j++;
                currentSegments[j] = updateOrCreateSegment(
                    currentSegments[j],
                    currentVertices[i],
                    currentVertices[i + 4]);
            }
        }

        IMixinWorld mixinWorld = (IMixinWorld) ((IMixinMovingObjectPosition) rayTraceHit).getWorld();

        int size1 = segments1.length;
        int size2 = segments2.length;

        for (int i = 0; i < size1; i++) {
            Segment segment1 = segments1[i];
            Line line = segment1.getLine();

            for (int j = 0; j < size2; j++) {
                Segment segment2 = segments2[j];

                if (line.contains(segment2.getStart()) && line.contains(segment2.getEnd())) {

                    Vector3D startVec1 = segment1.getStart();
                    double start1 = line.getAbscissa(startVec1);
                    Vector3D endVec1 = segment1.getEnd();
                    double end1 = line.getAbscissa(endVec1);

                    if (start1 > end1) {
                        double temp = start1;
                        start1 = end1;
                        end1 = temp;
                        Vector3D tempVec = startVec1;
                        startVec1 = endVec1;
                        endVec1 = tempVec;
                    }

                    Vector3D startVec2 = segment2.getStart();
                    double start2 = line.getAbscissa(startVec2);
                    Vector3D endVec2 = segment2.getEnd();
                    double end2 = line.getAbscissa(endVec2);

                    if (start2 > end2) {
                        double temp = start2;
                        start2 = end2;
                        end2 = temp;
                        Vector3D tempVec = startVec2;
                        startVec2 = endVec2;
                        endVec2 = tempVec;
                    }

                    if (start1 < end2 && end1 > start2) {
                        Vector3D segmentStart;
                        Vector3D segmentEnd;

                        if (start1 > start2) {
                            segmentStart = startVec1;
                            segmentEnd = (end1 < end2) ? endVec1 : endVec2;
                        } else {
                            segmentStart = startVec2;
                            segmentEnd = (end2 < end1) ? endVec2 : endVec1;
                        }

                        if (!segmentStart.equals(segmentEnd)) {
                            Vec3 sStart = mixinWorld
                                .transformToGlobal(segmentStart.getX(), segmentStart.getY(), segmentStart.getZ())
                                .addVector(-d0, -d1, -d2);

                            Vec3 sEnd = mixinWorld
                                .transformToGlobal(segmentEnd.getX(), segmentEnd.getY(), segmentEnd.getZ())
                                .addVector(-d0, -d1, -d2);

                            Vector3D finalStart = GeometryHelper3D.transformVector(sStart);
                            Vector3D finalEnd = GeometryHelper3D.transformVector(sEnd);

                            Line finalLine = new Line(finalStart, finalEnd);
                            excludedSegments.add(new Segment(finalStart, finalEnd, finalLine));
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
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

}
