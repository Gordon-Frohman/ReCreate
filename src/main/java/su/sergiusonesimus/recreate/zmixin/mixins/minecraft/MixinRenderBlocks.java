package su.sergiusonesimus.recreate.zmixin.mixins.minecraft;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import su.sergiusonesimus.recreate.zmixin.interfaces.IMixinRenderBlocks;

@Mixin(RenderBlocks.class)
public class MixinRenderBlocks implements IMixinRenderBlocks {

    private Float overrideAlpha = null;

    @Override
    public void setOverrideAlpha(int alpha) {
        overrideAlpha = (float) alpha / 255.0F;
    }

    @Override
    public void setOverrideAlpha(float alpha) {
        overrideAlpha = alpha;
    }

    @Override
    public void clearOverrideAlpha() {
        overrideAlpha = null;
    }

    @WrapOperation(
        method = { "renderFaceYPos", "renderFaceYNeg", "renderFaceXPos", "renderFaceXNeg", "renderFaceZPos",
            "renderFaceZNeg" },
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;setColorOpaque_F(FFF)V"))
    public void setColorWithAlpha(Tessellator tessellator, float red, float green, float blue,
        Operation<Void> original) {
        if (overrideAlpha == null) original.call(tessellator, red, green, blue);
        else tessellator.setColorRGBA_F(red, green, blue, overrideAlpha);
    }

    private Double textureMinX = null;
    private Double textureMinY = null;
    private Double textureMinZ = null;
    private Double textureMaxX = null;
    private Double textureMaxY = null;
    private Double textureMaxZ = null;

    public void overrideTextureBlockBounds(double minX, double minY, double minZ, double maxX, double maxY,
        double maxZ) {
        textureMinX = minX / 16.0D;
        textureMinY = minY / 16.0D;
        textureMinZ = minZ / 16.0D;
        textureMaxX = maxX / 16.0D;
        textureMaxY = maxY / 16.0D;
        textureMaxZ = maxZ / 16.0D;
    }

    public void clearTextureBlockBounds() {
        textureMinX = null;
        textureMinY = null;
        textureMinZ = null;
        textureMaxX = null;
        textureMaxY = null;
        textureMaxZ = null;
    }

    @WrapOperation(
        method = { "renderFaceYPos", "renderFaceYNeg", "renderFaceZPos", "renderFaceZNeg" },
        at = { @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D", ordinal = 0),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D", ordinal = 1),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D", ordinal = 2),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D", ordinal = 3),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D", ordinal = 4) })
    public double getMinX1(RenderBlocks renderBlocks, Operation<Double> original) {
        return getMinX(renderBlocks, original);
    }

    @WrapOperation(
        method = "renderFaceZNeg",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinX:D", ordinal = 5))
    public double getMinX2(RenderBlocks renderBlocks, Operation<Double> original) {
        return getMinX(renderBlocks, original);
    }

    private double getMinX(RenderBlocks renderBlocks, Operation<Double> original) {
        if (textureMinX == null) return original.call(renderBlocks);
        else return textureMinX;
    }

    @WrapOperation(
        method = { "renderFaceYPos", "renderFaceYNeg", "renderFaceZPos", "renderFaceZNeg" },
        at = { @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxX:D", ordinal = 0),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxX:D", ordinal = 1),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxX:D", ordinal = 2),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxX:D", ordinal = 3),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxX:D", ordinal = 4) })
    public double getMaxX1(RenderBlocks renderBlocks, Operation<Double> original) {
        return getMaxX(renderBlocks, original);
    }

    @WrapOperation(
        method = "renderFaceZNeg",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxX:D", ordinal = 5))
    public double getMaxX2(RenderBlocks renderBlocks, Operation<Double> original) {
        return getMaxX(renderBlocks, original);
    }

    private double getMaxX(RenderBlocks renderBlocks, Operation<Double> original) {
        if (textureMaxX == null) return original.call(renderBlocks);
        else return textureMaxX;
    }

    @WrapOperation(
        method = { "renderFaceXPos", "renderFaceXNeg", "renderFaceZPos", "renderFaceZNeg" },
        at = { @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinY:D", ordinal = 0),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinY:D", ordinal = 1),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinY:D", ordinal = 2),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinY:D", ordinal = 3),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinY:D", ordinal = 4) })
    public double getMinY(RenderBlocks renderBlocks, Operation<Double> original) {
        if (textureMinY == null) return original.call(renderBlocks);
        else return textureMinY;
    }

    @WrapOperation(
        method = { "renderFaceXPos", "renderFaceXNeg", "renderFaceZPos", "renderFaceZNeg" },
        at = { @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxY:D", ordinal = 0),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxY:D", ordinal = 1),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxY:D", ordinal = 2),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxY:D", ordinal = 3),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxY:D", ordinal = 4) })
    public double getMaxY(RenderBlocks renderBlocks, Operation<Double> original) {
        if (textureMaxY == null) return original.call(renderBlocks);
        else return textureMaxY;
    }

    @WrapOperation(
        method = { "renderFaceXPos", "renderFaceXNeg", "renderFaceYPos", "renderFaceYNeg" },
        at = { @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinZ:D", ordinal = 0),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinZ:D", ordinal = 1),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinZ:D", ordinal = 2),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinZ:D", ordinal = 3),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinZ:D", ordinal = 4) })
    public double getMinZ1(RenderBlocks renderBlocks, Operation<Double> original) {
        return getMinZ(renderBlocks, original);
    }

    @WrapOperation(
        method = "renderFaceXPos",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMinZ:D", ordinal = 5))
    public double getMinZ2(RenderBlocks renderBlocks, Operation<Double> original) {
        return getMinZ(renderBlocks, original);
    }

    private double getMinZ(RenderBlocks renderBlocks, Operation<Double> original) {
        if (textureMinZ == null) return original.call(renderBlocks);
        else return textureMinZ;
    }

    @WrapOperation(
        method = { "renderFaceXPos", "renderFaceXNeg", "renderFaceYPos", "renderFaceYNeg" },
        at = { @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxZ:D", ordinal = 0),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxZ:D", ordinal = 1),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxZ:D", ordinal = 2),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxZ:D", ordinal = 3),
            @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxZ:D", ordinal = 4) })
    public double getMaxZ1(RenderBlocks renderBlocks, Operation<Double> original) {
        return getMaxZ(renderBlocks, original);
    }

    @WrapOperation(
        method = "renderFaceXPos",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderBlocks;renderMaxZ:D", ordinal = 5))
    public double getMaxZ2(RenderBlocks renderBlocks, Operation<Double> original) {
        return getMaxZ(renderBlocks, original);
    }

    private double getMaxZ(RenderBlocks renderBlocks, Operation<Double> original) {
        if (textureMaxZ == null) return original.call(renderBlocks);
        else return textureMaxZ;
    }
}
