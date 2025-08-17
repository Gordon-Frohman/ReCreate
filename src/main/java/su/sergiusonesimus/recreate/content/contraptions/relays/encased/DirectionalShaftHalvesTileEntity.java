package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.util.Direction;

public class DirectionalShaftHalvesTileEntity extends KineticTileEntity {

    public Direction getSourceFacing() {
        return Direction.getNearest(this.sourceX, this.sourceY, this.sourceZ);
    }

}
