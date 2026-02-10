package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.compat.tebreaker.TileEntityBreakerIntegration;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntityRenderer;
import su.sergiusonesimus.recreate.foundation.utility.Color;

public class BearingTileEntityRenderer extends KineticTileEntityRenderer {

    BearingModel model = new BearingModel();

    @Override
    public void renderSafe(KineticTileEntity tileEntity, double x, double y, double z, float partialTicks) {
        BearingBlock block = (BearingBlock) tileEntity.getBlockType();
        Direction direction = block.getDirection(tileEntity.getBlockMetadata());
        model.setFace(direction);
        MechanicalBearingTileEntity mbte = (MechanicalBearingTileEntity) tileEntity;
        float topAngle = mbte.movedContraption == null ? mbte.getInterpolatedAngle(partialTicks - 1f)
            : (float) mbte.movedContraption.getAngle();
        switch (direction) {
            default:
                break;
            case DOWN:
            case EAST:
            case NORTH:
                topAngle = -topAngle;
                break;
        }
        model.setRotations(
            getAngleForTe(mbte, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, direction.getAxis()),
            topAngle / 180f * (float) Math.PI);
        Color color = getColor(mbte);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GL11.glColor4f(color.getRedAsFloat(), color.getGreenAsFloat(), color.getBlueAsFloat(), color.getAlphaAsFloat());

        boolean damageTexture = ReCreate.isTileEntityBreakerLoaded
            && TileEntityBreakerIntegration.shouldRenderDamageTexture(this);
        model.shaft.render(this);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (damageTexture) TileEntityBreakerIntegration.setBreakTexture(
            this,
            TileEntityBreakerIntegration.BEARING,
            TileEntityBreakerIntegration.getTileEntityDestroyProgress(tileEntity));
        model.render(block.getTexture(), this);

        GL11.glPopMatrix();
    }

}
