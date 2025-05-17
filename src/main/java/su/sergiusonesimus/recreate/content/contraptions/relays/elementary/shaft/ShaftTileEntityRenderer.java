package su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft;

import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.tileentity.TileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntityRenderer;
import su.sergiusonesimus.recreate.foundation.utility.Color;
import su.sergiusonesimus.recreate.util.Direction.Axis;

public class ShaftTileEntityRenderer extends KineticTileEntityRenderer {
	
    private final ShaftModel model = new ShaftModel();

	@SuppressWarnings("unchecked")
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z,
			float partialTicks) {
		Axis axis = ((ShaftBlock)tileEntity.getBlockType()).getAxis(tileEntity.getBlockMetadata());
		model.setAxis(axis);
		model.setRotation(getAngleForTe((KineticTileEntity) tileEntity, tileEntity.xCoord, tileEntity.yCoord,
			tileEntity.zCoord, axis));
		Color color = getColor((KineticTileEntity) tileEntity);
		
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
        GL11.glColor4f(color.getRedAsFloat(), color.getGreenAsFloat(), color.getBlueAsFloat(), color.getAlphaAsFloat());
        
        model.render();
        
        for(Object entryObject : Minecraft.getMinecraft().renderGlobal.damagedBlocks.entrySet()) {
        	DestroyBlockProgress dbp = ((Entry<Integer, DestroyBlockProgress>) entryObject).getValue();
        	if(dbp.getPartialBlockX() != tileEntity.xCoord || dbp.getPartialBlockY() != tileEntity.yCoord
        			|| dbp.getPartialBlockZ() != tileEntity.zCoord) continue;
        	model.renderDamageTexture(dbp.getPartialBlockDamage());
        }
        
        GL11.glPopMatrix();
	}

}
