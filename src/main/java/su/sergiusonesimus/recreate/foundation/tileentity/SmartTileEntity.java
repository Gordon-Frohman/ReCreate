package su.sergiusonesimus.recreate.foundation.tileentity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import su.sergiusonesimus.recreate.api.event.TileEntityBehaviourEvent;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.BehaviourType;
import su.sergiusonesimus.recreate.util.IInteractionChecker;
import su.sergiusonesimus.recreate.util.IPartialSafeNBT;

public abstract class SmartTileEntity extends SyncedTileEntity implements IPartialSafeNBT, IInteractionChecker {

    private final Map<BehaviourType<?>, TileEntityBehaviour> behaviours;
    // Internally maintained to be identical to behaviorMap.values() in order to
    // improve iteration performance.
    private final List<TileEntityBehaviour> behaviourList;
    private boolean initialized;
    private boolean firstNbtRead;
    private int lazyTickRate;
    private int lazyTickCounter;

    // Used for simulating this TE in a client-only setting
    private boolean virtualMode;

    public SmartTileEntity() {
        behaviours = new HashMap<>();
        initialized = false;
        firstNbtRead = true;
        setLazyTickRate(10);

        ArrayList<TileEntityBehaviour> list = new ArrayList<>();
        addBehaviours(list);
        list.forEach(b -> behaviours.put(b.getType(), b));

        behaviourList = new ArrayList<>(list.size());
        updateBehaviorList();
    }

    public abstract void addBehaviours(List<TileEntityBehaviour> behaviours);

    /**
     * Gets called just before reading tile data for behaviours. Register anything
     * here that depends on your custom te data.
     */
    public void addBehavioursDeferred(List<TileEntityBehaviour> behaviours) {}

    @Override
    public void updateEntity() {
        if (!initialized && hasWorldObj()) {
            initialize();
            initialized = true;
        }

        if (lazyTickCounter-- <= 0) {
            lazyTickCounter = lazyTickRate;
            lazyTick();
        }

        behaviourList.forEach(TileEntityBehaviour::tick);
    }

    public void initialize() {
        if (firstNbtRead) {
            firstNbtRead = false;
            MinecraftForge.EVENT_BUS.post(new TileEntityBehaviourEvent<>(this, behaviours));
            updateBehaviorList();
        }

        behaviourList.forEach(TileEntityBehaviour::initialize);
        lazyTick();
    }

    @Override
    public final void writeToNBT(NBTTagCompound compound) {
        write(compound, false);
    }

    @Override
    public final void writeToNBTClient(NBTTagCompound compound) {
        write(compound, true);
    }

    @Override
    public final void readFromNBT(NBTTagCompound tag) {
        fromTag(tag, false);
    }

    @Override
    public final void readFromNBTClient(NBTTagCompound tag) {
        fromTag(tag, true);
    }

    /**
     * Hook only these in future subclasses of STE
     */
    protected void fromTag(NBTTagCompound compound, boolean clientPacket) {
        if (firstNbtRead) {
            firstNbtRead = false;
            ArrayList<TileEntityBehaviour> list = new ArrayList<>();
            addBehavioursDeferred(list);
            list.forEach(b -> behaviours.put(b.getType(), b));
            MinecraftForge.EVENT_BUS.post(new TileEntityBehaviourEvent<>(this, behaviours));
            updateBehaviorList();
        }
        super.readFromNBT(compound);
        behaviourList.forEach(tb -> tb.read(compound, clientPacket));
    }

    /**
     * Hook only these in future subclasses of STE
     */
    protected void write(NBTTagCompound compound, boolean clientPacket) {
        super.writeToNBT(compound);
        behaviourList.forEach(tb -> tb.write(compound, clientPacket));
    }

    @Override
    public void writeSafe(NBTTagCompound compound, boolean clientPacket) {
        super.writeToNBT(compound);
        behaviourList.forEach(tb -> { if (tb.isSafeNBT()) tb.write(compound, clientPacket); });
    }

    // public ItemRequirement getRequiredItems() {
    // return behaviourList.stream()
    // .reduce(ItemRequirement.NONE, (a, b) -> a.with(b.getRequiredItems()), (a, b) -> a.with(b));
    // }

    /*
     * TODO: Remove this hack once this issue is resolved: https://github.com/MinecraftForge/MinecraftForge/issues/8302
     * Once the PR linked in the issue is accepted, we should use the new method for determining whether setRemoved was
     * called due to a chunk unload or not, and remove this volatile workaround
     */
    private boolean unloaded;

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        unloaded = true;
    }

    protected void setRemovedNotDueToChunkUnload() {

    }

    @Override
    public void invalidate() {
        forEachBehaviour(TileEntityBehaviour::remove);
        super.invalidate();

        if (!unloaded) {
            setRemovedNotDueToChunkUnload();
        }
    }

    public void setLazyTickRate(int slowTickRate) {
        this.lazyTickRate = slowTickRate;
        this.lazyTickCounter = slowTickRate;
    }

    public void lazyTick() {

    }

    protected void forEachBehaviour(Consumer<TileEntityBehaviour> action) {
        behaviourList.forEach(action);
    }

    protected void attachBehaviourLate(TileEntityBehaviour behaviour) {
        behaviours.put(behaviour.getType(), behaviour);
        behaviour.initialize();

        updateBehaviorList();
    }

    protected void removeBehaviour(BehaviourType<?> type) {
        TileEntityBehaviour remove = behaviours.remove(type);
        if (remove != null) {
            remove.remove();
            updateBehaviorList();
        }
    }

    // We don't trust the input to the API will be sane, so we
    // update all the contents whenever something changes. It's
    // simpler than trying to manipulate the list one element at
    // a time.
    private void updateBehaviorList() {
        behaviourList.clear();
        behaviourList.addAll(behaviours.values());
    }

    @SuppressWarnings("unchecked")
    public <T extends TileEntityBehaviour> T getBehaviour(BehaviourType<T> type) {
        return (T) behaviours.get(type);
    }

    // protected boolean isItemHandlerCap(Capability<?> cap) {
    // return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    // }
    //
    // protected boolean isFluidHandlerCap(Capability<?> cap) {
    // return cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    // }

    public void markVirtual() {
        virtualMode = true;
    }

    public boolean isVirtual() {
        return virtualMode;
    }

    @Override
    public boolean canPlayerUse(EntityPlayer player) {
        if (worldObj == null || worldObj.getTileEntity(xCoord, yCoord, zCoord) != this) return false;
        return player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }

    public void sendToContainer(PacketBuffer buffer) {
        buffer.writeInt(xCoord);
        buffer.writeInt(yCoord);
        buffer.writeInt(zCoord);
        try {
            buffer.writeNBTTagCompoundToBuffer(this.getUpdateTag());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshBlockState() {
        this.blockMetadata = getWorldObj().getBlockMetadata(xCoord, yCoord, zCoord);
    }

    public World getWorld() {
        return getWorldObj();
    }
}
