package su.sergiusonesimus.recreate.foundation.tileentity.behaviour.scrollvalue;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import su.sergiusonesimus.recreate.foundation.networking.AllPackets;
import su.sergiusonesimus.recreate.foundation.tileentity.SmartTileEntity;
import su.sergiusonesimus.recreate.foundation.tileentity.TileEntityBehaviour;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.BehaviourType;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.ValueBoxTransform;

public class ScrollValueBehaviour extends TileEntityBehaviour {

    public static BehaviourType<ScrollValueBehaviour> TYPE = new BehaviourType<>();

    ValueBoxTransform slotPositioning;
    Vec3 textShift;

    int min = 0;
    int max = 1;
    public int value;
    public int scrollableValue;
    int ticksUntilScrollPacket;
    boolean forceClientState;
    IChatComponent label;
    Consumer<Integer> callback;
    Consumer<Integer> clientCallback;
    Function<Integer, String> formatter;
    Function<Integer, IChatComponent> unit;
    Function<StepContext, Integer> step;
    private Supplier<Boolean> isActive;
    boolean needsWrench;

    public ScrollValueBehaviour(IChatComponent label, SmartTileEntity te, ValueBoxTransform slot) {
        super(te);
        this.setLabel(label);
        slotPositioning = slot;
        callback = i -> {};
        clientCallback = i -> {};
        textShift = Vec3.createVectorHelper(0, 0, 0);
        formatter = i -> Integer.toString(i);
        step = (c) -> 1;
        value = 0;
        isActive = () -> true;
        ticksUntilScrollPacket = -1;
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void write(NBTTagCompound nbt, boolean clientPacket) {
        nbt.setInteger("ScrollValue", value);
        if (clientPacket && forceClientState) {
            nbt.setBoolean("ForceScrollable", true);
            forceClientState = false;
        }
        super.write(nbt, clientPacket);
    }

    @Override
    public void read(NBTTagCompound nbt, boolean clientPacket) {
        value = nbt.getInteger("ScrollValue");
        if (nbt.hasKey("ForceScrollable")) {
            ticksUntilScrollPacket = -1;
            scrollableValue = value;
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

        AllPackets.channel.sendToServer(new ScrollValueUpdatePacket(getPosX(), getPosY(), getPosZ(), scrollableValue));
        ticksUntilScrollPacket = -1;
    }

    public ScrollValueBehaviour withClientCallback(Consumer<Integer> valueCallback) {
        clientCallback = valueCallback;
        return this;
    }

    public ScrollValueBehaviour withCallback(Consumer<Integer> valueCallback) {
        callback = valueCallback;
        return this;
    }

    public ScrollValueBehaviour between(int min, int max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public ScrollValueBehaviour moveText(Vec3 shift) {
        textShift = shift;
        return this;
    }

    public ScrollValueBehaviour requiresWrench() {
        this.needsWrench = true;
        return this;
    }

    public ScrollValueBehaviour withFormatter(Function<Integer, String> formatter) {
        this.formatter = formatter;
        return this;
    }

    public ScrollValueBehaviour withUnit(Function<Integer, IChatComponent> unit) {
        this.unit = unit;
        return this;
    }

    public ScrollValueBehaviour onlyActiveWhen(Supplier<Boolean> condition) {
        isActive = condition;
        return this;
    }

    public ScrollValueBehaviour withStepFunction(Function<StepContext, Integer> step) {
        this.step = step;
        return this;
    }

    @Override
    public void initialize() {
        super.initialize();
        setValue(value);
        scrollableValue = value;
    }

    public void setValue(int value) {
        value = MathHelper.clamp_int(value, min, max);
        if (value == this.value) return;
        this.value = value;
        forceClientState = true;
        callback.accept(value);
        tileEntity.notifyUpdate();
        tileEntity.sendData();
        scrollableValue = value;
    }

    public int getValue() {
        return value;
    }

    public String formatValue() {
        return formatter.apply(scrollableValue);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public boolean isActive() {
        return isActive.get();
    }

    public boolean testHit(Vec3 hit) {
        Vec3 localHit = Vec3.createVectorHelper(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord)
            .subtract(hit);
        return slotPositioning.testHit(tileEntity.getBlockType(), tileEntity.getBlockMetadata(), localHit);
    }

    public void setLabel(IChatComponent label) {
        this.label = label;
    }

    public static class StepContext {

        public int currentValue;
        public boolean forward;
        public boolean shift;
        public boolean control;
    }

}
