package su.sergiusonesimus.recreate.content.contraptions.components.motor;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.compat.tebreaker.TileEntityBreakerIntegration;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntityRenderer;
import su.sergiusonesimus.recreate.foundation.utility.Color;

public class CreativeMotorTileEntityRenderer extends KineticTileEntityRenderer {

    private final CreativeMotorModel model = new CreativeMotorModel();

    @Override
    public void renderSafe(KineticTileEntity tileEntity, double x, double y, double z, float partialTicks) {
        Direction direction = ((CreativeMotorBlock) tileEntity.getBlockType())
            .getDirection(tileEntity.getBlockMetadata());
        model.setFace(direction);
        model.setRotation(
            getAngleForTe(tileEntity, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, direction.getAxis()));
        Color color = getColor(tileEntity);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GL11.glColor4f(color.getRedAsFloat(), color.getGreenAsFloat(), color.getBlueAsFloat(), color.getAlphaAsFloat());

        boolean damageTexture = ReCreate.isTileEntityBreakerLoaded
            && TileEntityBreakerIntegration.shouldRenderDamageTexture(this);

        model.shaft.render(this);

        if (damageTexture) TileEntityBreakerIntegration.setBreakTexture(
            this,
            TileEntityBreakerIntegration.CREATIVE_MOTOR,
            TileEntityBreakerIntegration.getTileEntityDestroyProgress(tileEntity));

        model.render(this);

        GL11.glPopMatrix();
    }

}
