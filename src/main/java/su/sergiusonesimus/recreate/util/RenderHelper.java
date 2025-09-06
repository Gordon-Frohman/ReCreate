package su.sergiusonesimus.recreate.util;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

public class RenderHelper {

    public static void renderInvBox(RenderBlocks renderblocks, Block block, int meta) {
        Tessellator tessellator = Tessellator.instance;

        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        renderblocks.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, meta));
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderblocks.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, meta));
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderblocks.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, meta));
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderblocks.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, meta));
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderblocks.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, meta));
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderblocks.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, meta));
        tessellator.draw();

        GL11.glTranslatef(0.5F, 0.5F, 0.5F);

        block.setBlockBoundsForItemRender();
    }

}
