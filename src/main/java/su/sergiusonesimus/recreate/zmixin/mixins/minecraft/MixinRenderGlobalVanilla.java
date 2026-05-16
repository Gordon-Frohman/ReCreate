package su.sergiusonesimus.recreate.zmixin.mixins.minecraft;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

@Mixin(value = RenderGlobal.class, priority = 1200)
public class MixinRenderGlobalVanilla {

    @Shadow(remap = false)
    private static void checkForExcludedSegments(Tessellator tessellator, int drawMode, int color, double startX,
        double startY, double startZ, double endX, double endY, double endZ) {}

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

}
