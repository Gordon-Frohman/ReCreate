package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import net.minecraft.block.Block;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.compat.tebreaker.TileEntityBreakerIntegration;
import su.sergiusonesimus.recreate.content.contraptions.base.IRotate;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftModel;
import su.sergiusonesimus.recreate.foundation.utility.Color;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
import su.sergiusonesimus.recreate.util.AnimationTickHolder;

public class SplitShaftTileEntityRenderer extends KineticTileEntityRenderer {

    ShaftModel[] shafts = { new ShaftModel(Direction.AxisDirection.POSITIVE),
        new ShaftModel(Direction.AxisDirection.NEGATIVE) };

    @Override
    public void renderSafe(KineticTileEntity tileEntity, double x, double y, double z, float partialTicks) {
        Color color = getColor(tileEntity);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GL11.glColor4f(color.getRedAsFloat(), color.getGreenAsFloat(), color.getBlueAsFloat(), color.getAlphaAsFloat());

        Block block = tileEntity.getBlockType();
        final Axis boxAxis = ((IRotate) block).getAxis(tileEntity.getBlockMetadata());
        float time = AnimationTickHolder.getRenderTime(tileEntity.getWorldObj());

        for (ShaftModel shaft : shafts) shaft.setAxis(boxAxis);

        for (Direction direction : Iterate.directions) {
            Axis axis = direction.getAxis();
            if (boxAxis != axis) continue;

            float offset = getRotationOffsetForPosition(
                tileEntity,
                tileEntity.xCoord,
                tileEntity.yCoord,
                tileEntity.zCoord,
                axis);
            float angle = time * tileEntity.getSpeed() * 3f / 10;
            float modifier = 1;

            if (tileEntity instanceof SplitShaftTileEntity)
                modifier = ((SplitShaftTileEntity) tileEntity).getRotationSpeedModifier(direction);

            angle *= modifier;
            angle += offset;
            angle = angle % 360 / 180f * (float) Math.PI;

            for (ShaftModel shaft : shafts) {
                if (shaft.direction == direction.getAxisDirection()) {
                    shaft.setRotation(angle);
                    break;
                }
            }
        }

        for (int i = 0; i < shafts.length; i++) {
            ShaftModel shaft = shafts[i];
            boolean damageTexture = ReCreate.isTileEntityBreakerLoaded
                && TileEntityBreakerIntegration.shouldRenderDamageTexture(this)
                && i < (shafts.length - 1);
            shaft.render(this);
            if (damageTexture) TileEntityBreakerIntegration.setBreakTexture(
                this,
                TileEntityBreakerIntegration.SHAFT,
                TileEntityBreakerIntegration.getTileEntityDestroyProgress(tileEntity));
        }

        GL11.glPopMatrix();
    }

}
