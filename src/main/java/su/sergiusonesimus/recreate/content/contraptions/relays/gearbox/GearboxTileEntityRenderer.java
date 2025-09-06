package su.sergiusonesimus.recreate.content.contraptions.relays.gearbox;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.compat.tebreaker.TileEntityBreakerIntegration;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftModel;
import su.sergiusonesimus.recreate.foundation.utility.Color;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
import su.sergiusonesimus.recreate.util.AnimationTickHolder;

public class GearboxTileEntityRenderer extends KineticTileEntityRenderer {

    ShaftModel shaftPos = new ShaftModel(Direction.AxisDirection.POSITIVE);
    ShaftModel shaftNeg = new ShaftModel(Direction.AxisDirection.NEGATIVE);

    @Override
    public void renderSafe(KineticTileEntity tileEntity, double x, double y, double z, float partialTicks) {
        Color color = getColor(tileEntity);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GL11.glColor4f(color.getRedAsFloat(), color.getGreenAsFloat(), color.getBlueAsFloat(), color.getAlphaAsFloat());

        final Axis boxAxis = ((GearboxBlock) tileEntity.getBlockType()).getAxis(tileEntity.getBlockMetadata());
        float time = AnimationTickHolder.getRenderTime(tileEntity.getWorld());

        boolean damageTexture = ReCreate.isTileEntityBreakerLoaded
            && TileEntityBreakerIntegration.shouldRenderDamageTexture(this);

        for (Direction direction : Iterate.directions) {
            final Axis axis = direction.getAxis();
            if (boxAxis == axis) continue;

            ShaftModel shaft = direction.getAxisDirection() == AxisDirection.POSITIVE ? shaftPos : shaftNeg;
            shaft.setAxis(direction.getAxis());
            float offset = getRotationOffsetForPosition(
                tileEntity,
                tileEntity.xCoord,
                tileEntity.yCoord,
                tileEntity.zCoord,
                axis);
            float angle = (time * tileEntity.getSpeed() * 3f / 10) % 360;

            if (tileEntity.getSpeed() != 0 && tileEntity.hasSource()) {
                double sourceX = tileEntity.sourceX - tileEntity.xCoord;
                double sourceY = tileEntity.sourceY - tileEntity.yCoord;
                double sourceZ = tileEntity.sourceZ - tileEntity.zCoord;
                Direction sourceFacing = Direction.getNearest(sourceX, sourceY, sourceZ);
                if (sourceFacing.getAxis() == direction.getAxis()) angle *= sourceFacing == direction ? 1 : -1;
                else if (sourceFacing.getAxisDirection() == direction.getAxisDirection()) angle *= -1;
            }

            angle += offset;
            angle = angle / 180f * (float) Math.PI;

            shaft.setRotation(angle);
            shaft.render(this);
            if (damageTexture) TileEntityBreakerIntegration.setBreakTexture(
                this,
                TileEntityBreakerIntegration.SHAFT,
                TileEntityBreakerIntegration.getTileEntityDestroyProgress(tileEntity));
        }
        if (damageTexture) TileEntityBreakerIntegration.setBreakTexture(this, null);

        GL11.glPopMatrix();
    }
}
