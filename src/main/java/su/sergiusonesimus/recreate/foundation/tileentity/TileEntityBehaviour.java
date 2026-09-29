package su.sergiusonesimus.recreate.foundation.tileentity;

import java.util.ConcurrentModificationException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import su.sergiusonesimus.recreate.content.schematics.ItemRequirement;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.BehaviourType;

public abstract class TileEntityBehaviour {

    public SmartTileEntity tileEntity;
    private int lazyTickRate;
    private int lazyTickCounter;

    public TileEntityBehaviour(SmartTileEntity te) {
        tileEntity = te;
        setLazyTickRate(10);
    }

    public abstract BehaviourType<?> getType();

    public void initialize() {

    }

    public void tick() {
        if (lazyTickCounter-- <= 0) {
            lazyTickCounter = lazyTickRate;
            lazyTick();
        }

    }

    public void read(NBTTagCompound nbt, boolean clientPacket) {

    }

    public void write(NBTTagCompound nbt, boolean clientPacket) {

    }

    public boolean isSafeNBT() {
        return false;
    }

    public ItemRequirement getRequiredItems() {
        return ItemRequirement.NONE;
    }

    public void onBlockChanged(int meta) {

    }

    public void onNeighborChanged(int neighbourX, int neighbourY, int neighbourZ) {

    }

    public void remove() {

    }

    public void destroy() {

    }

    public void setLazyTickRate(int slowTickRate) {
        this.lazyTickRate = slowTickRate;
        this.lazyTickCounter = slowTickRate;
    }

    public void lazyTick() {

    }

    public int getPosX() {
        return tileEntity.xCoord;
    }

    public int getPosY() {
        return tileEntity.yCoord;
    }

    public int getPosZ() {
        return tileEntity.zCoord;
    }

    public World getWorld() {
        return tileEntity.getWorldObj();
    }

    public static <T extends TileEntityBehaviour> T get(IBlockAccess reader, int x, int y, int z,
        BehaviourType<T> type) {
        TileEntity te;
        try {
            te = reader.getTileEntity(x, y, z);
        } catch (ConcurrentModificationException e) {
            te = null;
        }
        return get(te, type);
    }

    public static <T extends TileEntityBehaviour> void destroy(IBlockAccess reader, int x, int y, int z,
        BehaviourType<T> type) {
        T behaviour = get(reader.getTileEntity(x, y, z), type);
        if (behaviour != null) behaviour.destroy();
    }

    public static <T extends TileEntityBehaviour> T get(TileEntity te, BehaviourType<T> type) {
        if (te == null) return null;
        if (!(te instanceof SmartTileEntity)) return null;
        SmartTileEntity ste = (SmartTileEntity) te;
        return ste.getBehaviour(type);
    }
}
