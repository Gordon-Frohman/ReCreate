package su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntityRenderer;
import su.sergiusonesimus.recreate.foundation.utility.Color;

public class ShaftTileEntityRenderer extends KineticTileEntityRenderer {

    private final ShaftModel model = new ShaftModel();

    @Override
    public void renderSafe(KineticTileEntity tileEntity, double x, double y, double z, float partialTicks) {
        Axis axis = ((ShaftBlock) tileEntity.getBlockType()).getAxis(tileEntity.getBlockMetadata());
        model.setAxis(axis);
        model.setRotation(getAngleForTe(tileEntity, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, axis));
        Color color = getColor(tileEntity);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GL11.glColor4f(color.getRedAsFloat(), color.getGreenAsFloat(), color.getBlueAsFloat(), color.getAlphaAsFloat());

        model.render(this);

        GL11.glPopMatrix();
    }

}
