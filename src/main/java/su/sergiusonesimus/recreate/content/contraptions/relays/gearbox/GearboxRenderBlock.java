package su.sergiusonesimus.recreate.content.contraptions.relays.gearbox;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.content.contraptions.base.IRotate;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftModel;
import su.sergiusonesimus.recreate.util.RenderHelper;

public class GearboxRenderBlock implements ISimpleBlockRenderingHandler {

    final int renderID;
    ShaftModel[] shafts = { new ShaftModel(Direction.AxisDirection.POSITIVE),
        new ShaftModel(Direction.AxisDirection.NEGATIVE) };

    public GearboxRenderBlock(int blockComplexRenderID) {
        this.renderID = blockComplexRenderID;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        double pixel = 1D / 16D;

        int correctMeta = ((GearboxBlock) block).getMetaFromAxis(Axis.Y);

        renderer.setRenderBounds(pixel, pixel, pixel, 1 - pixel, 1 - pixel, 1 - pixel);
        RenderHelper.renderInvBox(renderer, block, correctMeta);

        renderer.setRenderBounds(0, 0, 0, 1D, 2 * pixel, 1D);
        RenderHelper.renderInvBox(renderer, block, correctMeta);
        renderer.setRenderBounds(0, 1 - 2 * pixel, 0, 1D, 1D, 1D);
        RenderHelper.renderInvBox(renderer, block, correctMeta);

        Axis[] axes = { Axis.X, Axis.Z };

        for (Axis axis : axes) {
            for (ShaftModel shaft : shafts) {
                shaft.setAxis(axis);
                shaft.render();
            }
        }
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {

        double pixel = 1D / 16D;

        Axis axis = ((IRotate) block).getAxis(world.getBlockMetadata(x, y, z));

        renderer.setRenderBounds(pixel, pixel, pixel, 1 - pixel, 1 - pixel, 1 - pixel);
        renderer.renderStandardBlock(block, x, y, z);

        double borderX = 1;
        double borderY = 1;
        double borderZ = 1;

        switch (axis) {
            case X:
                borderX = 2 * pixel;
                break;
            case Y:
                borderY = 2 * pixel;
                break;
            case Z:
                borderZ = 2 * pixel;
                break;
        }

        renderer.setRenderBounds(0, 0, 0, borderX, borderY, borderZ);
        renderer.renderStandardBlock(block, x, y, z);
        renderer.setRenderBounds(1 - borderX, 1 - borderY, 1 - borderZ, 1D, 1D, 1D);
        renderer.renderStandardBlock(block, x, y, z);

        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return renderID;
    }
}
