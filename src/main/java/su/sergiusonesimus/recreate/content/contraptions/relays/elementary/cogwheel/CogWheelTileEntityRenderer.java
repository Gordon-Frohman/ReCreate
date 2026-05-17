package su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.compat.tebreaker.TileEntityBreakerIntegration;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftModel;
import su.sergiusonesimus.recreate.foundation.utility.Color;

public class CogWheelTileEntityRenderer extends KineticTileEntityRenderer {

    private final ShaftModel shaft = new ShaftModel();
    private final CogWheelModel cogwheel = new CogWheelModel();
    private final LargeCogWheelModel largeCogwheel = new LargeCogWheelModel();

    @Override
    public void renderSafe(KineticTileEntity tileEntity, double x, double y, double z, float partialTicks) {
        CogWheelBlock block = (CogWheelBlock) tileEntity.getBlockType();
        Axis axis = block.getAxis(tileEntity.getBlockMetadata());

        // Base kinetic rotation angle fetched from the TileEntity
        float baseAngle = getAngleForTe(tileEntity, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, axis);

        // Separate variable for the gear teeth rotation to avoid offsetting the central shaft
        float gearAngle = baseAngle;

        if (block.isLarge && !shouldOffset(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, axis)) {
            /*
             * NOTE: If model/renderer uses Radians internally, use: (float) (gearAngle - Math.PI / 16F)
             * If it uses Degrees (Standard OpenGL/GL11), 11.25F is exactly half a tooth offset for a 16-tooth wheel
             */
            gearAngle -= 11.25F;
        }

        // Central shaft always uses the base angle to keep alignment with adjacent shafts
        shaft.setAxis(axis);
        shaft.setRotation(baseAngle);

        // Apply the correctly offset angle to the respective cogwheel model
        if (!block.isLarge) {
            cogwheel.setAxis(axis);
            cogwheel.setRotation(gearAngle);
        } else {
            largeCogwheel.setAxis(axis);
            largeCogwheel.setRotation(gearAngle);
        }

        Color color = getColor(tileEntity);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GL11.glColor4f(color.getRedAsFloat(), color.getGreenAsFloat(), color.getBlueAsFloat(), color.getAlphaAsFloat());

        boolean damageTexture = ReCreate.isTileEntityBreakerLoaded
            && TileEntityBreakerIntegration.shouldRenderDamageTexture(this);

        // Render the central shaft
        shaft.render(this);

        // Render the cogwheel body with its respective breaking texture if needed
        if (!block.isLarge) {
            if (damageTexture) {
                TileEntityBreakerIntegration.setBreakTexture(
                    this,
                    TileEntityBreakerIntegration.COGWHEEL,
                    TileEntityBreakerIntegration.getTileEntityDestroyProgress(tileEntity)
                );
            }
            cogwheel.render(this);
        } else {
            if (damageTexture) {
                TileEntityBreakerIntegration.setBreakTexture(
                    this,
                    TileEntityBreakerIntegration.LARGE_COGWHEEL,
                    TileEntityBreakerIntegration.getTileEntityDestroyProgress(tileEntity)
                );
            }
            largeCogwheel.render(this);
        }

        GL11.glPopMatrix();
    }
}
