package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.SimpleKineticTileEntity;
import su.sergiusonesimus.recreate.util.Direction;

public class DirectionalShaftHalvesTileEntity extends SimpleKineticTileEntity {

    public Direction getSourceFacing() {
        if (this.hasSource()) {
            return Direction.getNearest(this.sourceX, this.sourceY, this.sourceZ);
        }
        return null;
    }

}
