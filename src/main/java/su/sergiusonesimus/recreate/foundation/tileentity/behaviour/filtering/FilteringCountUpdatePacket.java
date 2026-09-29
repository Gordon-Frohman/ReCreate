package su.sergiusonesimus.recreate.foundation.tileentity.behaviour.filtering;

import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.recreate.foundation.networking.TileEntityConfigurationPacket;
import su.sergiusonesimus.recreate.foundation.tileentity.SmartTileEntity;
import su.sergiusonesimus.recreate.foundation.tileentity.SyncedTileEntity;

public class FilteringCountUpdatePacket extends TileEntityConfigurationPacket<SmartTileEntity> {

    int amount;

    public FilteringCountUpdatePacket() {}

    public FilteringCountUpdatePacket(int x, int y, int z, int amount) {
        super(x, y, z);
        this.amount = amount;
    }

    @Override
    protected void writeSettings(ByteBuf buffer) {
        buffer.writeInt(amount);
    }

    @Override
    protected void readSettings(ByteBuf buffer) {
        amount = buffer.readInt();
    }

    @Override
    protected void applySettings(SyncedTileEntity te) {
        FilteringBehaviour behaviour = ((SmartTileEntity) te).getBehaviour(FilteringBehaviour.TYPE);
        if (behaviour == null) return;
        behaviour.forceClientState = true;
        behaviour.count = amount;
        te.markDirty();
        te.sendData();
    }

}
