package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import su.sergiusonesimus.metaworlds.util.Direction;

public class ClutchTileEntity extends SplitShaftTileEntity {

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource()) {
            if (face != getSourceFacing() && ((ClutchBlock) this.blockType).isPowered) return 0;
        }
        return 1;
    }
}
