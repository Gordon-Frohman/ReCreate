package su.sergiusonesimus.recreate.foundation.networking;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.recreate.foundation.tileentity.SyncedTileEntity;

/**
 * A server to client version of {@link TileEntityConfigurationPacket}
 * 
 * @param <TE>
 */
public abstract class TileEntityDataPacket<TE extends SyncedTileEntity> implements IMessage {

    protected int posX, posY, posZ;

    public TileEntityDataPacket() {}

    public TileEntityDataPacket(int x, int y, int z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.posX = buffer.readInt();
        this.posY = buffer.readInt();
        this.posZ = buffer.readInt();
        readData(buffer);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(posX);
        buffer.writeInt(posY);
        buffer.writeInt(posZ);
        writeData(buffer);
    }

    public static class Handler<T extends TileEntityDataPacket<?>> implements IMessageHandler<T, IMessage> {

        @Override
        public IMessage onMessage(T message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            if (world != null) {
                TileEntity tile = world.getTileEntity(message.posX, message.posY, message.posZ);

                if (tile instanceof SyncedTileEntity ste) {
                    message.handlePacket(ste);
                }
            }

            return null;
        }
    }

    protected abstract void writeData(ByteBuf buffer);

    protected abstract void readData(ByteBuf buffer);

    protected abstract void handlePacket(SyncedTileEntity tile);
}
