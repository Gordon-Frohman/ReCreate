package su.sergiusonesimus.recreate.content.contraptions.components.waterwheel;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftModel;
import su.sergiusonesimus.recreate.foundation.utility.Color;

public class WaterWheelTileEntityRenderer extends KineticTileEntityRenderer {

    private final ShaftModel shaft = new ShaftModel();
    private final WaterWheelModel wheel = new WaterWheelModel();

    public void renderSafe(KineticTileEntity tileEntity, double x, double y, double z, float partialTicks) {
        WaterWheelBlock block = (WaterWheelBlock) tileEntity.getBlockType();
        Direction.Axis axis = block.getAxis(tileEntity.getBlockMetadata());
        float angle = getAngleForTe(tileEntity, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, axis);
        shaft.setAxis(axis);
        shaft.setRotation(angle);
        wheel.setAxis(axis);
        wheel.setRotation(angle);

        Color color = getColor(tileEntity);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GL11.glColor4f(color.getRedAsFloat(), color.getGreenAsFloat(), color.getBlueAsFloat(), color.getAlphaAsFloat());

        wheel.render();

        GL11.glPopMatrix();
    }
}
