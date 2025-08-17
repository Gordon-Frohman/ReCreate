package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import su.sergiusonesimus.recreate.util.Direction;

public class ClutchTileEntity extends SplitShaftTileEntity {

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource()) {
            if (face != getSourceFacing() && ((AbstractRedstoneShaftBlock) this.blockType).isPowered(this)) return 0;
        }
        return 1;
    }
}
