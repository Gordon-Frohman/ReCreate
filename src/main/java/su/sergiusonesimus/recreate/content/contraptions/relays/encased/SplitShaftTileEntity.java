package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import su.sergiusonesimus.recreate.util.Direction;

public abstract class SplitShaftTileEntity extends DirectionalShaftHalvesTileEntity {

    public abstract float getRotationSpeedModifier(Direction face);
}
