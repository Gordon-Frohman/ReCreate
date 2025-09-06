package su.sergiusonesimus.recreate.content.contraptions.components.waterwheel;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.content.contraptions.base.GeneratingKineticTileEntity;
import su.sergiusonesimus.recreate.foundation.config.CKinetics;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;

public class WaterWheelTileEntity extends GeneratingKineticTileEntity {

    private Map<Direction, Float> flows;

    public WaterWheelTileEntity() {
        flows = new HashMap<>();
        for (Direction d : Iterate.directions) setFlow(d, 0);
        setLazyTickRate(20);
    }

    @Override
    protected void fromTag(NBTTagCompound compound, boolean clientPacket) {
        super.fromTag(compound, clientPacket);
        if (compound.hasKey("Flows")) {
            for (Direction d : Iterate.directions) setFlow(
                d,
                compound.getCompoundTag("Flows")
                    .getFloat(d.getSerializedName()));
        }
    }

    @Override
    public void write(NBTTagCompound compound, boolean clientPacket) {
        NBTTagCompound flows = new NBTTagCompound();
        for (Direction d : Iterate.directions) flows.setFloat(d.getSerializedName(), this.flows.get(d));
        compound.setTag("Flows", flows);

        super.write(compound, clientPacket);
    }

    public void setFlow(Direction direction, float speed) {
        flows.put(direction, speed);
        this.updateSpeed = true;
        // setChanged();
    }

    @Override
    public float getGeneratedSpeed() {
        float speed = 0;
        for (Float f : flows.values()) speed += f;
        if (speed != 0) speed += CKinetics.waterWheelBaseSpeed * Math.signum(speed);
        return speed;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        WaterWheelBlock ww = (WaterWheelBlock) this.blockType;
        if (ww != null) {
            ww.updateAllSides(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
        }
        this.reActivateSource = true;
    }

}
