package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.client.multiplayer.SubWorldClient;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.foundation.utility.AngleHelper;
import su.sergiusonesimus.recreate.foundation.utility.NBTHelper;
import su.sergiusonesimus.recreate.util.VecHelper;

/**
 * Ex: Pistons, bearings <br>
 * Controlled contraptions can rotate around one axis and translate.
 * <br>
 * They are bound to an {@link IControlContraption}
 */
public abstract class ControlledContraption extends Contraption {

    protected Integer controllerX;
    protected Integer controllerY;
    protected Integer controllerZ;
    protected Axis rotationAxis;

    public ControlledContraption() {
        super();
    }

    public ControlledContraption(World parentWorld, IControlContraption controller, Axis rotationAxis) {
        super(parentWorld);
        this.controllerX = controller.getPositionX();
        this.controllerY = controller.getPositionY();
        this.controllerZ = controller.getPositionZ();
        this.rotationAxis = rotationAxis;
    }

    @Override
    public void readNBT(NBTTagCompound compound, boolean spawnPacket) {
        super.readNBT(compound, spawnPacket);
        NBTTagCompound controller = compound.getCompoundTag("Controller");
        this.controllerX = controller.getInteger("X");
        this.controllerY = controller.getInteger("Y");
        this.controllerZ = controller.getInteger("Z");
        if (compound.hasKey("Axis")) rotationAxis = NBTHelper.readEnum(compound, "Axis", Axis.class);
    }

    @Override
    public NBTTagCompound writeNBT(boolean spawnPacket) {
        NBTTagCompound compound = super.writeNBT(spawnPacket);
        NBTTagCompound controller = new NBTTagCompound();
        controller.setInteger("X", controllerX);
        controller.setInteger("Y", controllerY);
        controller.setInteger("Z", controllerZ);
        compound.setTag("Controller", controller);
        if (rotationAxis != null) NBTHelper.writeEnum(compound, "Axis", rotationAxis);
        return compound;
    }

    @Override
    public Vec3 applyRotation(Vec3 localPos, float partialTicks) {
        localPos = VecHelper.rotate(localPos, getAngle(partialTicks), rotationAxis);
        return localPos;
    }

    @Override
    public Vec3 reverseRotation(Vec3 localPos, float partialTicks) {
        localPos = VecHelper.rotate(localPos, -getAngle(partialTicks), rotationAxis);
        return localPos;
    }

    public void setAngle(float angle) {
        if (rotationAxis == null) return;
        ContraptionWorld contraption = this.getContraptionWorld();
        double currentAngle = 0;
        switch (rotationAxis) {
            case X:
                currentAngle = contraption.getRotationRoll();
                break;
            case Y:
                currentAngle = contraption.getRotationYaw();
                break;
            case Z:
                currentAngle = contraption.getRotationPitch();
                break;
        }
        // A small fix to marry Create's 360 degree system with MetaWorlds' unlimited system
        if (Math.abs(currentAngle) > 360 || (currentAngle * angle <= 0))
            angle += Math.floor(currentAngle / 360 + (angle < 0 ? 1 : 0)) * 360;
        switch (rotationAxis) {
            case X:
                contraption.setRotationRoll(angle);
                break;
            case Y:
                contraption.setRotationYaw(angle);
                break;
            case Z:
                contraption.setRotationPitch(angle);
                break;
        }
    }

    public float getAngle(float partialTicks) {
        return partialTicks == 1.0F ? getAngle() : AngleHelper.angleLerp(partialTicks, getPrevAngle(), getAngle());
    }

    public float getAngle() {
        if (this.rotationAxis == null) return 0;
        ContraptionWorld contraptionWorld = this.getContraptionWorld();
        switch (this.rotationAxis) {
            case X:
                return (float) (contraptionWorld.getRotationRoll() % 360);
            case Y:
                return (float) (contraptionWorld.getRotationYaw() % 360);
            case Z:
                return (float) (contraptionWorld.getRotationPitch() % 360);
        }
        return 0;
    }

    protected float getPrevAngle() {
        if (!(this.getContraptionWorld() instanceof SubWorldClient subworld)) return this.getAngle();
        switch (this.rotationAxis) {
            case X:
                return (float) (subworld.lastTickRotationRoll % 360);
            case Y:
                return (float) (subworld.lastTickRotationYaw % 360);
            case Z:
                return (float) (subworld.lastTickRotationPitch % 360);
        }
        return 0;
    }

    public void setRotationAxis(Axis rotationAxis) {
        this.rotationAxis = rotationAxis;
    }

    public Axis getRotationAxis() {
        return rotationAxis;
    }

    public void tick() {
        super.tick();
        tickActors();

        if (controllerX == null || controllerY == null
            || controllerZ == null
            || parentWorld == null
            || !parentWorld.blockExists(controllerX, controllerY, controllerZ)) return;
        IControlContraption controller = getController();
        if (controller == null) {
            this.disassemble();
            return;
        }
        if (!controller.isAttachedTo(this)) {
            controller.attach(this);
            // if (parentWorld.isRemote) setPos(getX(), getY(), getZ());
        }
    }

    protected IControlContraption getController() {
        if (parentWorld == null || controllerX == null || controllerY == null || controllerZ == null) return null;
        if (!parentWorld.blockExists(controllerX, controllerY, controllerZ)) return null;
        TileEntity te = parentWorld.getTileEntity(controllerX, controllerY, controllerZ);
        if (!(te instanceof IControlContraption controller)) return null;
        return controller;
    }

    @Override
    protected void onContraptionStalled() {
        IControlContraption controller = getController();
        if (controller != null) controller.onStall();
        super.onContraptionStalled();
    }

    @Override
    protected float getStalledAngle() {
        return getAngle();
    }

    @Override
    protected void handleStallInformation(float x, float y, float z, float angle) {
        setPosition(x, y, z);
        setAngle(angle);
    }

}
