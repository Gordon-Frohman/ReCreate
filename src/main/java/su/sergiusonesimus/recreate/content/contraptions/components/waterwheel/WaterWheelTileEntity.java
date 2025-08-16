package su.sergiusonesimus.recreate.content.contraptions.components.waterwheel;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import su.sergiusonesimus.recreate.content.contraptions.base.GeneratingKineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticBlock;
import su.sergiusonesimus.recreate.foundation.config.CKinetics;
import su.sergiusonesimus.recreate.foundation.utility.FluidHelper;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
import su.sergiusonesimus.recreate.util.Direction;
import su.sergiusonesimus.recreate.util.Direction.Axis;
import su.sergiusonesimus.recreate.util.VecHelper;

public class WaterWheelTileEntity extends GeneratingKineticTileEntity {

    private Map<Direction, Float> flows;

    public WaterWheelTileEntity() {
        flows = new HashMap<>();
        for (Direction d : Iterate.directions) setFlow(d, 0);
        setLazyTickRate(20);
    }

    @Override
    public void initialize() {
        updateAllSides(this.getWorld(), this.xCoord, this.yCoord, this.zCoord);

        super.initialize();
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
        updateAllSides(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
        this.reActivateSource = true;
    }

    private void updateFlowAt(World world, int x, int y, int z, Direction side) {
        KineticBlock block = (KineticBlock) world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        if (side.getAxis() == block.getAxis(world.getBlockMetadata(x, y, z))) return;

        Direction wf = block.getDirection(world.getBlockMetadata(x, y, z));
        boolean clockwise = wf.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        int clockwiseMultiplier = 2;

        int wx = x - side.getNormal().posX;
        int wy = y - side.getNormal().posY;
        int wz = z - side.getNormal().posZ;

        Vec3 vec = FluidHelper.getFlowVector(world, wx, wy, wz);

        /*
         * if (side.getAxis()
         * .isHorizontal()) {
         * BlockState adjacentBlock = world.getBlockState(pos.relative(side));
         * if (adjacentBlock.getBlock() == Blocks.BUBBLE_COLUMN)
         * vec = new Vec3(0, adjacentBlock.getValue(BubbleColumnBlock.DRAG_DOWN) ? -1 : 1, 0);
         * }
         */

        vec = VecHelper.scale(
            vec,
            side.getAxisDirection()
                .getStep());
        vec = Vec3.createVectorHelper(Math.signum(vec.xCoord), Math.signum(vec.yCoord), Math.signum(vec.zCoord));
        Vec3 flow = vec;

        double flowStrength = 0;

        if (block.getAxis(meta) == Axis.Z) {
            if (side.getAxis() == Axis.Y)
                flowStrength = flow.xCoord > 0 ^ !clockwise ? -flow.xCoord * clockwiseMultiplier : -flow.xCoord;
            if (side.getAxis() == Axis.X)
                flowStrength = flow.yCoord < 0 ^ !clockwise ? flow.yCoord * clockwiseMultiplier : flow.yCoord;
        }

        if (block.getAxis(meta) == Axis.X) {
            if (side.getAxis() == Axis.Y)
                flowStrength = flow.zCoord < 0 ^ !clockwise ? flow.zCoord * clockwiseMultiplier : flow.zCoord;
            if (side.getAxis() == Axis.Z)
                flowStrength = flow.yCoord > 0 ^ !clockwise ? -flow.yCoord * clockwiseMultiplier : -flow.yCoord;
        }

        if (block.getAxis(meta) == Axis.Y) {
            if (side.getAxis() == Axis.Z)
                flowStrength = flow.xCoord < 0 ^ !clockwise ? flow.xCoord * clockwiseMultiplier : flow.xCoord;
            if (side.getAxis() == Axis.X)
                flowStrength = flow.zCoord > 0 ^ !clockwise ? -flow.zCoord * clockwiseMultiplier : -flow.zCoord;
        }

        /*
         * if (te.getSpeed() == 0 && flowStrength != 0 && !world.isClientSide()) {
         * AllTriggers.triggerForNearbyPlayers(AllTriggers.WATER_WHEEL, world, pos, 5);
         * if (FluidHelper.isLava(fluid.getType()))
         * AllTriggers.triggerForNearbyPlayers(AllTriggers.LAVA_WHEEL, world, pos, 5);
         * if (fluid.getType()
         * .isSame(AllFluids.CHOCOLATE.get()))
         * AllTriggers.triggerForNearbyPlayers(AllTriggers.CHOCOLATE_WHEEL, world, pos, 5);
         * }
         */
        Integer flowModifier = CKinetics.waterWheelFlowSpeed;
        this.setFlow(side, (float) ((flowStrength * flowModifier / (2f * 2f))));
    }

    public void updateAllSides(World world, int x, int y, int z) {
        for (Direction d : Iterate.directions) updateFlowAt(world, x, y, z, d);
        updateWheelSpeed();
    }

    public void updateWheelSpeed() {
        updateGeneratedRotation();
    }
}
