package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.content.contraptions.base.IRotate;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.AbstractShaftModel;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftModel;
import su.sergiusonesimus.recreate.util.RenderHelper;

public class SplitShaftRenderBlock implements ISimpleBlockRenderingHandler {

    final int renderID;
    ShaftModel shaft1 = new ShaftModel(Direction.AxisDirection.POSITIVE);
    ShaftModel shaft2 = new ShaftModel(Direction.AxisDirection.NEGATIVE);

    public AbstractShaftModel getUnlitModel() {
        return null;
    }

    public AbstractShaftModel getLitModel() {
        return null;
    }

    public SplitShaftRenderBlock(int blockComplexRenderID) {
        this.renderID = blockComplexRenderID;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        double pixel = 1D / 16D;
        Direction.Axis axis = Direction.Axis.X;
        int correctMeta = ((AbstractEncasedShaftBlock) block).getMetaFromAxis(axis);

        renderer.setRenderBounds(0D, 0D, 0D, 1D, pixel, 1D);
        RenderHelper.renderInvBox(renderer, block, correctMeta);
        renderer.setRenderBounds(0D, 1 - pixel, 0D, 1D, 1D, 1D);
        RenderHelper.renderInvBox(renderer, block, correctMeta);
        renderer.setRenderBounds(pixel, pixel, pixel, 1 - pixel, 1 - pixel, 1 - pixel);
        RenderHelper.renderInvBox(renderer, block, correctMeta);
        renderer.setRenderBounds(0D, pixel, 0D, 1D, 1 - pixel, pixel);
        RenderHelper.renderInvBox(renderer, block, correctMeta);
        renderer.setRenderBounds(0D, pixel, 1 - pixel, 1D, 1 - pixel, 1D);
        RenderHelper.renderInvBox(renderer, block, correctMeta);

        shaft1.setAxis(axis);
        shaft2.setAxis(axis);
        shaft1.render();
        shaft2.render();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {

        double pixel = 1D / 16D;

        Axis axis = ((IRotate) block).getAxis(world.getBlockMetadata(x, y, z));
        switch (axis) {
            case X:
            case Z:
                if (axis == Axis.X) {
                    renderer.setRenderBounds(0D, pixel, 0D, 1D, 1 - pixel, pixel);
                    renderer.renderStandardBlock(block, x, y, z);
                    renderer.setRenderBounds(0D, pixel, 1 - pixel, 1D, 1 - pixel, 1D);
                    renderer.renderStandardBlock(block, x, y, z);
                } else {
                    renderer.uvRotateTop = 1;
                    renderer.uvRotateBottom = 1;
                    renderer.setRenderBounds(0D, pixel, 0D, pixel, 1 - pixel, 1D);
                    renderer.renderStandardBlock(block, x, y, z);
                    renderer.setRenderBounds(1 - pixel, pixel, 0D, 1D, 1 - pixel, 1D);
                    renderer.renderStandardBlock(block, x, y, z);
                }
                renderer.setRenderBounds(0D, 0D, 0D, 1D, pixel, 1D);
                renderer.renderStandardBlock(block, x, y, z);
                renderer.setRenderBounds(0D, 1 - pixel, 0D, 1D, 1D, 1D);
                renderer.renderStandardBlock(block, x, y, z);
                renderer.setRenderBounds(pixel, pixel, pixel, 1 - pixel, 1 - pixel, 1 - pixel);
                renderer.renderStandardBlock(block, x, y, z);
                break;
            case Y:
                renderer.uvRotateEast = 1;
                renderer.uvRotateWest = 1;
                renderer.uvRotateNorth = 1;
                renderer.uvRotateSouth = 1;
                renderer.setRenderBounds(0D, 0D, 0D, pixel, 1D, 1D);
                renderer.renderStandardBlock(block, x, y, z);
                renderer.setRenderBounds(1 - pixel, 0D, 0D, 1D, 1D, 1D);
                renderer.renderStandardBlock(block, x, y, z);
                renderer.setRenderBounds(pixel, pixel, pixel, 1 - pixel, 1 - pixel, 1 - pixel);
                renderer.renderStandardBlock(block, x, y, z);
                renderer.setRenderBounds(pixel, 0D, 0D, 1 - pixel, 1D, pixel);
                renderer.renderStandardBlock(block, x, y, z);
                renderer.setRenderBounds(pixel, 0D, 1 - pixel, 1 - pixel, 1D, 1D);
                renderer.renderStandardBlock(block, x, y, z);
                break;
        }

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
