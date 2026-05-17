package su.sergiusonesimus.recreate.zmixin.mixins.angelica;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.angelica.render.SelectionBoxRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import su.sergiusonesimus.metaworlds.util.OrientedBB;

@Mixin(value = RenderGlobal.class, priority = 2100)
public class MixinRenderGlobal {

    @Shadow(remap = false)
    private static void checkForExcludedSegments(Tessellator tessellator, int drawMode, int color, double startX,
        double startY, double startZ, double endX, double endY, double endZ) {}

    @Inject(method = "drawOrientedBoundingBox", remap = false, at = @At(value = "HEAD"))
    private static void shareColor(OrientedBB obb, int color, CallbackInfo ci,
        @Share("color") LocalIntRef sharedColor) {
        sharedColor.set(color);
    }

    @WrapOperation(
        method = "drawOrientedBoundingBox",
        remap = false,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;startDrawing(I)V"))
    private static void shareDrawMode(Tessellator tessellator, int mode, Operation<Void> original,
        @Share("drawMode") LocalIntRef sharedDrawMode) {
        sharedDrawMode.set(mode);
        original.call(tessellator, mode);
    }

    @WrapOperation(
        method = "drawOrientedBoundingBox",
        remap = false,
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
        method = "drawOrientedBoundingBox",
        remap = false,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;draw()I"))
    private static void clearLastVertex(OrientedBB obb, int color, CallbackInfo ci,
        @Share("lastPos") LocalRef<Vec3> sharedLastPos) {
        sharedLastPos.set(null);
    }

    /**
     * 
     * @author Sergius Onesimus
     * @reason Angelica's optimization broke ReCreate's selection render. Using modified vanilla renderer for it
     */
    @WrapOperation(
        method = "drawOutlinedBoundingBox",
        remap = true,
        at = @At(
            value = "INVOKE",
            target = "Lcom/gtnewhorizons/angelica/render/SelectionBoxRenderer;draw(Lnet/minecraft/util/AxisAlignedBB;I)V",
            remap = false))
    private static void drawOrientedBoundingBox(AxisAlignedBB aabb, int color,
        Operation<SelectionBoxRenderer> original) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(3);

        if (color != -1) {
            tessellator.setColorOpaque_I(color);
        }

        //@formatter:off
        tessellator.addVertex(aabb.minX, aabb.minY, aabb.minZ);
        checkForExcludedSegments(
            tessellator,
            3,
            color,
            aabb.minX, aabb.minY, aabb.minZ,
            aabb.maxX, aabb.minY, aabb.minZ);
        tessellator.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
        checkForExcludedSegments(
            tessellator,
            3,
            color,
            aabb.maxX, aabb.minY, aabb.minZ,
            aabb.maxX, aabb.minY, aabb.maxZ);
        tessellator.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
        checkForExcludedSegments(
            tessellator,
            3,
            color,
            aabb.maxX, aabb.minY, aabb.maxZ,
            aabb.minX, aabb.minY, aabb.maxZ);
        tessellator.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
        checkForExcludedSegments(
            tessellator,
            3,
            color,
            aabb.minX, aabb.minY, aabb.maxZ,
            aabb.minX, aabb.minY, aabb.minZ);
        tessellator.addVertex(aabb.minX, aabb.minY, aabb.minZ);
        tessellator.draw();
        tessellator.startDrawing(3);

        if (color != -1)
        {
            tessellator.setColorOpaque_I(color);
        }

        tessellator.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
        checkForExcludedSegments(
            tessellator,
            3,
            color,
            aabb.minX, aabb.maxY, aabb.minZ,
            aabb.maxX, aabb.maxY, aabb.minZ);
        tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
        checkForExcludedSegments(
            tessellator,
            3,
            color,
            aabb.maxX, aabb.maxY, aabb.minZ,
            aabb.maxX, aabb.maxY, aabb.maxZ);
        tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
        checkForExcludedSegments(
            tessellator,
            3,
            color,
            aabb.maxX, aabb.maxY, aabb.maxZ,
            aabb.minX, aabb.maxY, aabb.maxZ);
        tessellator.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
        checkForExcludedSegments(
            tessellator,
            3,
            color,
            aabb.minX, aabb.maxY, aabb.maxZ,
            aabb.minX, aabb.maxY, aabb.minZ);
        tessellator.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
        tessellator.draw();
        tessellator.startDrawing(1);

        if (color != -1)
        {
            tessellator.setColorOpaque_I(color);
        }

        tessellator.addVertex(aabb.minX, aabb.minY, aabb.minZ);
        checkForExcludedSegments(
            tessellator,
            1,
            color,
            aabb.minX, aabb.minY, aabb.minZ,
            aabb.minX, aabb.maxY, aabb.minZ);
        tessellator.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
        checkForExcludedSegments(
            tessellator,
            1,
            color,
            aabb.minX, aabb.maxY, aabb.minZ,
            aabb.maxX, aabb.minY, aabb.minZ);
        tessellator.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
        checkForExcludedSegments(
            tessellator,
            1,
            color,
            aabb.maxX, aabb.minY, aabb.minZ,
            aabb.maxX, aabb.maxY, aabb.minZ);
        tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
        checkForExcludedSegments(
            tessellator,
            1,
            color,
            aabb.maxX, aabb.maxY, aabb.minZ,
            aabb.maxX, aabb.minY, aabb.maxZ);
        tessellator.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
        checkForExcludedSegments(
            tessellator,
            1,
            color,
            aabb.maxX, aabb.minY, aabb.maxZ,
            aabb.maxX, aabb.maxY, aabb.maxZ);
        tessellator.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
        checkForExcludedSegments(
            tessellator,
            1,
            color,
            aabb.maxX, aabb.maxY, aabb.maxZ,
            aabb.minX, aabb.minY, aabb.maxZ);
        tessellator.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
        checkForExcludedSegments(
            tessellator,
            1,
            color,
            aabb.minX, aabb.minY, aabb.maxZ,
            aabb.minX, aabb.maxY, aabb.maxZ);
        tessellator.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
        tessellator.draw();
        //@formatter:on
    }

}
