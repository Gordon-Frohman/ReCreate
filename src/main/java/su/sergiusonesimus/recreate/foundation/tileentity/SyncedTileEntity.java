package su.sergiusonesimus.recreate.foundation.tileentity;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

@ParametersAreNonnullByDefault
public abstract class SyncedTileEntity extends TileEntity {

    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBTClient(nbt);
        return nbt;
    }

    public void sendData() {
        if (worldObj != null && !worldObj.isRemote) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void causeBlockUpdate() {
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public Packet getDescriptionPacket() {
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, this.getBlockMetadata(), getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBTClient(pkt.func_148857_g());
    }

    /**
     * Special handling for client update packets.
     * Called readClientUpdate in latter versions
     */
    public void readFromNBTClient(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    /**
     * Special handling for client update packets
     * Called writeToClient in latter versions
     */
    public void writeToNBTClient(NBTTagCompound tag) {
        writeToNBT(tag);
    }

    public void notifyUpdate() {
        markDirty();
        sendData();
    }

    public TargetPoint packetTarget() {
        return new TargetPoint(this.worldObj.provider.dimensionId, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, 64.0);
    }

    public Chunk containedChunk() {
        return worldObj.getChunkFromBlockCoords(xCoord, zCoord);
    }
}
