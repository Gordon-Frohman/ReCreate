package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import su.sergiusonesimus.recreate.util.Direction;

public class GearshiftTileEntity extends SplitShaftTileEntity {

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource()) {
            if (face != getSourceFacing() && ((AbstractRedstoneShaftBlock) this.blockType).isPowered(this)) {
                return -1;
            }
        }
        return 1;
    }

}
