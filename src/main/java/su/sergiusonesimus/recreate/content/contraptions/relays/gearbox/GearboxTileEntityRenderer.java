package su.sergiusonesimus.recreate.content.contraptions.relays.gearbox;

import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.recreate.AllModelTextures;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.AbstractShaftModel;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftModel;
import su.sergiusonesimus.recreate.content.contraptions.relays.encased.*;
import su.sergiusonesimus.recreate.foundation.utility.Color;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
import su.sergiusonesimus.recreate.util.AnimationTickHolder;
import su.sergiusonesimus.recreate.util.Direction;

public class GearboxTileEntityRenderer extends KineticTileEntityRenderer {

    ShaftModel shaft1 = new ShaftModel(Direction.AxisDirection.POSITIVE);
    ShaftModel shaft2 = new ShaftModel(Direction.AxisDirection.NEGATIVE);
    ShaftModel shaft3 = new ShaftModel(Direction.AxisDirection.POSITIVE);
    ShaftModel shaft4 = new ShaftModel(Direction.AxisDirection.NEGATIVE);
    AbstractShaftModel normal = getModel();

    public AbstractShaftModel getModel() {
        return new GearboxModel(AllModelTextures.GEARBOX);
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
        Direction.Axis axis = ((AbstractEncasedShaftBlock) tileEntity.getBlockType())
            .getAxis(tileEntity.getBlockMetadata());

        if (tileEntity.blockType instanceof GearboxBlock gearboxte) {
            this.normal.setAxis(axis);
            this.shaft3.setAxis(gearboxte.getSecondAxis(gearboxte.getMetaFromAxis(axis)));
            this.shaft4.setAxis(gearboxte.getSecondAxis(gearboxte.getMetaFromAxis(axis)));
        }

        this.shaft1.setAxis(axis);
        this.shaft2.setAxis(axis);

        float angle2 = getAngleForTe(
            (KineticTileEntity) tileEntity,
            tileEntity.xCoord,
            tileEntity.yCoord,
            tileEntity.zCoord,
            axis);

        float angle3 = angle2;
        if (tileEntity.blockType instanceof GearboxBlock gearboxte) {
            angle3 = getAngleForTe(
                (KineticTileEntity) tileEntity,
                tileEntity.xCoord,
                tileEntity.yCoord,
                tileEntity.zCoord,
                gearboxte.getSecondAxis(gearboxte.getMetaFromAxis(axis)));
        }

        KineticTileEntity te = (KineticTileEntity) tileEntity;

        float time = AnimationTickHolder.getRenderTime();
        for (Direction direction : Iterate.directions) {
            float offset = getRotationOffsetForPosition(te, te.xCoord, te.yCoord, te.zCoord, axis);
            float offset2 = offset;
            if (tileEntity.blockType instanceof GearboxBlock gearboxte) {
                offset2 = getRotationOffsetForPosition(
                    te,
                    te.xCoord,
                    te.yCoord,
                    te.zCoord,
                    gearboxte.getSecondAxis(gearboxte.getMetaFromAxis(axis)));
            }
            float angle = ((time * te.getSpeed() * 3f / 10) % 360);

            if (te.getSpeed() != 0 && te.hasSource()) {
                Direction sourceFacing = Direction.getNearest(te.sourceX, te.sourceY, te.sourceZ);
                if (te.hasSource()) {
                    if (direction != sourceFacing) {
                        angle *= -1;
                    }
                }
                angle *= 1;
            }

            float angle0 = angle;
            angle += offset;
            angle0 += offset2;
            angle = angle / 180f * (float) Math.PI;
            angle0 = angle0 / 180f * (float) Math.PI;

            shaft1.setRotation(angle);
            shaft2.setRotation(angle2);
            shaft3.setRotation(angle0);
            shaft4.setRotation(angle3);
        }

        Color color = getColor((KineticTileEntity) tileEntity);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GL11.glColor4f(color.getRedAsFloat(), color.getGreenAsFloat(), color.getBlueAsFloat(), color.getAlphaAsFloat());

        this.normal.render();
        shaft1.render();
        shaft2.render();
        shaft3.render();
        shaft4.render();

        GL11.glPopMatrix();
    }
}
