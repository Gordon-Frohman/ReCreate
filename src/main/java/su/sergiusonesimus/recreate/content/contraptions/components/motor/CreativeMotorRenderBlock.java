package su.sergiusonesimus.recreate.content.contraptions.components.motor;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import su.sergiusonesimus.metaworlds.util.Direction;

public class CreativeMotorRenderBlock implements ISimpleBlockRenderingHandler {

    final int renderID;
    private final CreativeMotorModel model = new CreativeMotorModel();

    public CreativeMotorRenderBlock(int blockComplexRenderID) {
        this.renderID = blockComplexRenderID;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        model.setFace(Direction.EAST);

        model.shaft.render();

        model.render();

        block.setBlockBoundsForItemRender();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        return false;
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
