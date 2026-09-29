package su.sergiusonesimus.recreate.foundation.tileentity.behaviour.filtering;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.content.logistics.item.filter.FilterItem;
import su.sergiusonesimus.recreate.content.schematics.ItemRequirement;
import su.sergiusonesimus.recreate.foundation.networking.AllPackets;
import su.sergiusonesimus.recreate.foundation.tileentity.SmartTileEntity;
import su.sergiusonesimus.recreate.foundation.tileentity.TileEntityBehaviour;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.BehaviourType;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.ValueBoxTransform;
import su.sergiusonesimus.recreate.util.ItemHandlerHelper;
import su.sergiusonesimus.recreate.util.VecHelper;

public class FilteringBehaviour extends TileEntityBehaviour {

    public static BehaviourType<FilteringBehaviour> TYPE = new BehaviourType<>();

    ValueBoxTransform slotPositioning;
    boolean showCount;
    Vec3 textShift;

    private ItemStack filter;
    public int count;
    private Consumer<ItemStack> callback;
    private Supplier<Boolean> isActive;
    private Supplier<Boolean> showCountPredicate;

    int scrollableValue;
    int ticksUntilScrollPacket;
    boolean forceClientState;
    boolean recipeFilter;
    boolean fluidFilter;

    public FilteringBehaviour(SmartTileEntity te, ValueBoxTransform slot) {
        super(te);
        filter = null;
        slotPositioning = slot;
        showCount = false;
        callback = stack -> {};
        isActive = () -> true;
        textShift = VecHelper.ZERO;
        count = 0;
        ticksUntilScrollPacket = -1;
        showCountPredicate = () -> showCount;
        recipeFilter = false;
        fluidFilter = false;
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void write(NBTTagCompound nbt, boolean clientPacket) {
        nbt.setTag("Filter", getFilter().writeToNBT(new NBTTagCompound()));
        nbt.setInteger("FilterAmount", count);

        if (clientPacket && forceClientState) {
            nbt.setBoolean("ForceScrollable", true);
            forceClientState = false;
        }
        super.write(nbt, clientPacket);
    }

    @Override
    public void read(NBTTagCompound nbt, boolean clientPacket) {
        filter = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Filter"));
        count = nbt.getInteger("FilterAmount");
        if (nbt.hasKey("ForceScrollable")) {
            scrollableValue = count;
            ticksUntilScrollPacket = -1;
        }
        super.read(nbt, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();

        if (!getWorld().isRemote) return;
        if (ticksUntilScrollPacket == -1) return;
        if (ticksUntilScrollPacket > 0) {
            ticksUntilScrollPacket--;
            return;
        }

        AllPackets.channel.sendToServer(
            new FilteringCountUpdatePacket(this.getPosX(), this.getPosY(), this.getPosZ(), scrollableValue));
        ticksUntilScrollPacket = -1;
    }

    public FilteringBehaviour withCallback(Consumer<ItemStack> filterCallback) {
        callback = filterCallback;
        return this;
    }

    public FilteringBehaviour forRecipes() {
        recipeFilter = true;
        return this;
    }

    public FilteringBehaviour forFluids() {
        fluidFilter = true;
        return this;
    }

    public FilteringBehaviour onlyActiveWhen(Supplier<Boolean> condition) {
        isActive = condition;
        return this;
    }

    public FilteringBehaviour showCountWhen(Supplier<Boolean> condition) {
        showCountPredicate = condition;
        return this;
    }

    public FilteringBehaviour showCount() {
        showCount = true;
        return this;
    }

    public FilteringBehaviour moveText(Vec3 shift) {
        textShift = shift;
        return this;
    }

    @Override
    public void initialize() {
        super.initialize();
        scrollableValue = count;
    }

    public void setFilter(Direction face, ItemStack stack) {
        setFilter(stack);
    }

    public void setFilter(ItemStack stack) {
        boolean confirm = ItemHandlerHelper.canItemStacksStack(stack, filter);
        filter = stack.copy();
        callback.accept(filter);
        count = !confirm ? 0
            : (filter.getItem() instanceof FilterItem) ? 0 : Math.min(stack.stackSize, stack.getMaxStackSize());
        forceClientState = true;

        tileEntity.markDirty();
        tileEntity.sendData();
    }

    @Override
    public void destroy() {
        if (filter.getItem() instanceof FilterItem) {
            Vec3 pos = VecHelper.getCenterOf(getPosX(), getPosY(), getPosZ());
            World world = getWorld();
            world.spawnEntityInWorld(new EntityItem(world, pos.xCoord, pos.yCoord, pos.zCoord, filter.copy()));
        }

        super.destroy();
    }

    @Override
    public ItemRequirement getRequiredItems() {
        Item filterItem = filter.getItem();
        if (filterItem instanceof FilterItem)
            return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, filterItem);

        return ItemRequirement.NONE;
    }

    public ItemStack getFilter(Direction side) {
        return getFilter();
    }

    public ItemStack getFilter() {
        return filter.copy();
    }

    public boolean isCountVisible() {
        return showCountPredicate.get();
    }

    public boolean test(ItemStack stack) {
        return !isActive() || filter == null || FilterItem.test(tileEntity.getWorld(), stack, filter);
    }

    public boolean test(FluidStack stack) {
        return !isActive() || filter == null || FilterItem.test(tileEntity.getWorld(), stack, filter);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public boolean testHit(Vec3 hit) {
        Vec3 localHit = hit.subtract(Vec3.createVectorHelper(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord));
        return slotPositioning.testHit(tileEntity.getBlockType(), tileEntity.getBlockMetadata(), localHit);
    }

    public int getAmount() {
        return count;
    }

    public boolean anyAmount() {
        return count == 0;
    }

    public boolean isActive() {
        return isActive.get();
    }

}
