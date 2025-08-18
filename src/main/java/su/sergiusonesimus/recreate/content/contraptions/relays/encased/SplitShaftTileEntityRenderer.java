package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.AbstractShaftModel;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftModel;
import su.sergiusonesimus.recreate.foundation.utility.Color;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
import su.sergiusonesimus.recreate.util.AnimationTickHolder;

public class SplitShaftTileEntityRenderer extends KineticTileEntityRenderer {

    ShaftModel shaft1 = new ShaftModel(Direction.AxisDirection.POSITIVE);
    ShaftModel shaft2 = new ShaftModel(Direction.AxisDirection.NEGATIVE);
    AbstractShaftModel lit = getLitModel();
    AbstractShaftModel unlit = getUnlitModel();

    public AbstractShaftModel getUnlitModel() {
        return null;
    }

    public AbstractShaftModel getLitModel() {
        return null;
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
        Direction.Axis axis = ((AbstractEncasedShaftBlock) tileEntity.getBlockType())
            .getAxis(tileEntity.getBlockMetadata());

        if (tileEntity.blockType instanceof AbstractRedstoneShaftBlock redstonete) {
            if (redstonete.isPowered(tileEntity)) {
                this.lit.setAxis(axis);
            } else {
                this.unlit.setAxis(axis);
            }
        }

        shaft1.setAxis(axis);
        shaft2.setAxis(axis);

        float angle2 = getAngleForTe(
            (KineticTileEntity) tileEntity,
            tileEntity.xCoord,
            tileEntity.yCoord,
            tileEntity.zCoord,
            axis);

        KineticTileEntity te = (KineticTileEntity) tileEntity;

        float time = AnimationTickHolder.getRenderTime();
        for (Direction direction : Iterate.directions) {
            float offset = getRotationOffsetForPosition(te, te.xCoord, te.yCoord, te.zCoord, axis);
            float angle = ((time * te.getSpeed() * 3f / 10) % 360);
            float modifier = 1;

            if (te instanceof SplitShaftTileEntity) {
                modifier = ((SplitShaftTileEntity) te).getRotationSpeedModifier(direction);
            }

            angle *= modifier;
            angle += offset;
            angle = angle / 180f * (float) Math.PI;

            shaft1.setRotation(angle);
            shaft2.setRotation(angle2);
        }

        Color color = getColor((KineticTileEntity) tileEntity);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GL11.glColor4f(color.getRedAsFloat(), color.getGreenAsFloat(), color.getBlueAsFloat(), color.getAlphaAsFloat());

        if (tileEntity.blockType instanceof AbstractRedstoneShaftBlock redstonete) {
            if (redstonete.isPowered(tileEntity)) {
                this.lit.render();
            } else {
                this.unlit.render();
            }
        }

        shaft1.render();
        shaft2.render();

        GL11.glPopMatrix();
    }

}
