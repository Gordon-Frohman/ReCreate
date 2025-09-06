package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import su.sergiusonesimus.metaworlds.util.Direction;

public class GearshiftTileEntity extends SplitShaftTileEntity {

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource()) {
            if (face != getSourceFacing() && ((GearshiftBlock) this.getBlockType()).isPowered) return -1;
        }
        return 1;
    }

}
