package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;

public class DirectionalShaftHalvesTileEntity extends KineticTileEntity {

    public Direction getSourceFacing() {
        return Direction.getNearest(sourceX - this.xCoord, sourceY - this.yCoord, sourceZ - this.zCoord);
    }

}
