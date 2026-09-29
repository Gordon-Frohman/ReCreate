package su.sergiusonesimus.recreate.foundation.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import su.sergiusonesimus.recreate.foundation.tileentity.SyncedTileEntity;

public abstract class TileEntityConfigurationPacket<TE extends SyncedTileEntity> implements IMessage {

    protected int posX, posY, posZ;

    public TileEntityConfigurationPacket() {}

    public TileEntityConfigurationPacket(int x, int y, int z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        if (buffer.isReadable()) {
            this.posX = buffer.readInt();
            this.posY = buffer.readInt();
            this.posZ = buffer.readInt();
            readSettings(buffer);
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(posX);
        buffer.writeInt(posY);
        buffer.writeInt(posZ);
        writeSettings(buffer);
    }

    public static class Handler<T extends TileEntityConfigurationPacket<?>> implements IMessageHandler<T, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(T message, MessageContext ctx) {
            World world = Minecraft.getMinecraft().theWorld;

            if (!world.blockExists(message.posX, message.posY, message.posZ)) {
                return null;
            }

            TileEntity tileEntity = world.getTileEntity(message.posX, message.posY, message.posZ);
            if (tileEntity instanceof SyncedTileEntity ste) {
                message.applySettings(ste);
                ste.sendData();
                ste.markDirty();
            }

            return null;
        }
    }

    protected abstract void writeSettings(ByteBuf buffer);

    protected abstract void readSettings(ByteBuf buffer);

    protected abstract void applySettings(SyncedTileEntity te);
}
