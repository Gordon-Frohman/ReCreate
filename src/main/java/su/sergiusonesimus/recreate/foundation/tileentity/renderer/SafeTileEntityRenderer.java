package su.sergiusonesimus.recreate.foundation.tileentity.renderer;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;

public abstract class SafeTileEntityRenderer<T extends TileEntity> extends TileEntitySpecialRenderer {

    @SuppressWarnings("unchecked")
    @Override
    public final void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (isInvalid((T) te)) return;
        renderSafe((T) te, x, y, z, partialTicks);
    }

    protected abstract void renderSafe(T te, double x, double y, double z, float partialTicks);

    public boolean isInvalid(T te) {
        return !te.hasWorldObj() || te.getBlockType() == Blocks.air;
    }
}
