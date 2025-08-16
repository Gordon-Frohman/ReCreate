package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.AbstractShaftBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.AbstractShaftModel;
import su.sergiusonesimus.recreate.util.Direction;

public class SplitShaftRenderBlock implements ISimpleBlockRenderingHandler {

    final int renderID;
    private final SplitShaftModel model = new SplitShaftModel();
    AbstractShaftModel lit = getLitModel();
    AbstractShaftModel unlit = getUnlitModel();

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
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

        Direction.Axis axis = Direction.Axis.X;

        model.setAxis(axis);
        if (block instanceof AbstractRedstoneShaftBlock redstonete) {
            this.unlit.setAxis(axis);
        }

        model.render();

        if (block instanceof AbstractRedstoneShaftBlock redstonete) {
            this.unlit.render();
        }

        block.setBlockBoundsForItemRender();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        if (world == null || world.getTileEntity(x, y, z) != null) return false;
        MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
            || !(world.getBlock(mop.blockX, mop.blockY, mop.blockZ) instanceof AbstractShaftBlock)) return false;
        Direction.Axis axis = ((AbstractShaftBlock) block)
            .getAxis(world.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ));

        model.setAxis(axis);
        if (block instanceof AbstractRedstoneShaftBlock redstonete) {
            if (redstonete.isPowered((World) world, x, y, z)) {
                this.lit.setAxis(axis);
            } else {
                this.unlit.setAxis(axis);
            }
        }

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);

        model.render();
        if (block instanceof AbstractRedstoneShaftBlock redstonete) {
            if (redstonete.isPowered((World) world, x, y, z)) {
                this.lit.render();
            } else {
                this.unlit.render();
            }
        }

        GL11.glPopMatrix();
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
