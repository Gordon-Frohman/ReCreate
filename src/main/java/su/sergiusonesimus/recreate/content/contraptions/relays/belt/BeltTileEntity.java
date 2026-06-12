package su.sergiusonesimus.recreate.content.contraptions.relays.belt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.LazyOptional;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.content.contraptions.base.CasingBlock.CasingType;
import su.sergiusonesimus.recreate.content.contraptions.base.IRotate;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.transport.BeltInventory;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.transport.BeltMovementHandler;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.transport.ItemHandlerBeltSegment;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.transport.TransportedItemStack;
import su.sergiusonesimus.recreate.foundation.tileentity.TileEntityBehaviour;
import su.sergiusonesimus.recreate.foundation.utility.NBTHelper;
import su.sergiusonesimus.recreate.util.VecHelper;

public class BeltTileEntity extends KineticTileEntity {

    public BeltSlope slopeType = BeltSlope.HORIZONTAL;
    public BeltPart partType = BeltPart.START;

    public Map<Entity, TransportedEntityInfo> passengers;
    public Integer color;
    public int beltLength;
    public int index;
    public Direction lastInsert;
    public CasingType casing;

    protected int controllerX;
    protected int controllerY;
    protected int controllerZ;
    protected BeltInventory inventory;
    protected LazyOptional<IInventory> itemHandler;

    public NBTTagCompound trackerUpdateTag;

    public BeltTileEntity() {
        super();
        controllerX = controllerY = controllerZ = 0;
        itemHandler = LazyOptional.empty();
        casing = CasingType.NONE;
        color = null;
    }

    @Override
    public void addBehaviours(List<TileEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(
            new DirectBeltInputBehaviour(this).onlyInsertWhen(this::canInsertFrom)
                .setInsertionHandler(this::tryInsertingFromSide));
        behaviours.add(
            new TransportedItemStackHandlerBehaviour(this, this::applyToAllItems)
                .withStackPlacement(this::getWorldPositionOf));
    }

    @Override
    public void updateEntity() {
        // Init belt
        if (beltLength == 0) BeltBlock.initBelt(worldObj, xCoord, yCoord, zCoord);

        super.updateEntity();

        if (worldObj.getBlock(xCoord, yCoord, zCoord) != AllBlocks.belt) return;

        initializeItemHandler();

        // Move Items
        if (!isController()) return;

        getInventory().tick();

        if (getSpeed() == 0) return;

        // Move Entities
        if (passengers == null) passengers = new HashMap<>();

        List<Entity> toRemove = new ArrayList<Entity>();
        passengers.forEach((entity, info) -> {
            boolean canBeTransported = BeltMovementHandler.canBeTransported(entity);
            boolean leftTheBelt = info.getTicksSinceLastCollision() > (slopeType != BeltSlope.HORIZONTAL ? 3 : 1);
            if (!canBeTransported || leftTheBelt) {
                toRemove.add(entity);
                return;
            }

            info.tick();
            BeltMovementHandler.transportEntity(this, entity, info);
        });
        toRemove.forEach(passengers::remove);
    }

    @Override
    public float calculateStressApplied() {
        if (!isController()) return 0;
        return super.calculateStressApplied();
    }

    @Override
    public AxisAlignedBB makeRenderBoundingBox() {
        double expander = isController() ? beltLength + 1 : 0.5D;
        return super.makeRenderBoundingBox().expand(expander, expander, expander);
    }

    protected void initializeItemHandler() {
        if (worldObj.isRemote || itemHandler.isPresent()) return;
        if (!worldObj.blockExists(controllerX, controllerY, controllerZ)) return;
        TileEntity te = worldObj.getTileEntity(controllerX, controllerY, controllerZ);
        if (te == null || !(te instanceof BeltTileEntity)) return;
        BeltInventory inventory = ((BeltTileEntity) te).getInventory();
        if (inventory == null) return;
        IInventory handler = new ItemHandlerBeltSegment(inventory, index);
        itemHandler = LazyOptional.of(() -> handler);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        itemHandler.invalidate();
    }

    @Override
    public void write(NBTTagCompound compound, boolean clientPacket) {
        compound.setInteger("ControllerX", controllerX);
        compound.setInteger("ControllerY", controllerY);
        compound.setInteger("ControllerZ", controllerZ);
        compound.setBoolean("IsController", isController());
        compound.setInteger("Length", beltLength);
        compound.setInteger("Index", index);
        NBTHelper.writeEnum(compound, "Casing", casing);
        NBTHelper.writeEnum(compound, "SlopeType", slopeType);
        NBTHelper.writeEnum(compound, "PartType", partType);

        if (color != null) compound.setInteger("Dye", color);

        if (isController()) compound.setTag("Inventory", getInventory().write());
        super.write(compound, clientPacket);
    }

    @Override
    protected void fromTag(NBTTagCompound compound, boolean clientPacket) {
        super.fromTag(compound, clientPacket);

        if (compound.getBoolean("IsController")) {
            controllerX = xCoord;
            controllerY = yCoord;
            controllerZ = zCoord;
        }

        color = compound.hasKey("Dye") ? compound.getInteger("Dye") : null;

        if (!wasMoved) {
            if (!isController()) {
                controllerX = compound.getInteger("ControllerX");
                controllerY = compound.getInteger("ControllerY");
                controllerZ = compound.getInteger("ControllerZ");
            }
            trackerUpdateTag = compound;
            index = compound.getInteger("Index");
            beltLength = compound.getInteger("Length");
        }

        if (isController()) getInventory().read(compound.getCompoundTag("Inventory"));

        CasingType casingBefore = casing;
        casing = NBTHelper.readEnum(compound, "Casing", CasingType.class);
        BeltSlope slopeTypeBefore = slopeType;
        slopeType = NBTHelper.readEnum(compound, "SlopeType", BeltSlope.class);
        BeltPart partTypeBefore = partType;
        partType = NBTHelper.readEnum(compound, "PartType", BeltPart.class);

        if (!clientPacket) return;

        if (casingBefore == casing && slopeTypeBefore == slopeType && partTypeBefore == partType) return;
        if (hasWorldObj()) worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, getBlockType(), getBlockMetadata());
    }

    @Override
    public void clearKineticInformation() {
        super.clearKineticInformation();
        beltLength = 0;
        index = 0;
        controllerX = 0;
        controllerY = 0;
        controllerZ = 0;
        trackerUpdateTag = new NBTTagCompound();
    }

    public void applyColor(Integer colorIn) {
        if (colorIn == null || colorIn < 0 || colorIn >= ItemDye.field_150921_b.length) {
            if (color == null) return;
        } else if (color != null && color == colorIn) return;

        ChunkCoordinates controller = getController();
        for (ChunkCoordinates blockPos : BeltBlock
            .getBeltChain(worldObj, controller.posX, controller.posY, controller.posZ)) {
            BeltTileEntity belt = BeltHelper.getSegmentTE(worldObj, blockPos);
            if (belt == null) continue;
            belt.color = colorIn;
            belt.markDirty();
            belt.sendData();
        }
    }

    public BeltTileEntity getControllerTE() {
        if (controllerX == 0 && controllerY == 0 && controllerZ == 0) return null;
        if (!worldObj.blockExists(controllerX, controllerY, controllerZ)) return null;
        TileEntity te = worldObj.getTileEntity(controllerX, controllerY, controllerZ);
        if (te == null || !(te instanceof BeltTileEntity)) return null;
        return (BeltTileEntity) te;
    }

    public void setController(ChunkCoordinates controller) {
        this.setController(controller.posX, controller.posY, controller.posZ);
    }

    public void setController(int controllerX, int controllerY, int controllerZ) {
        this.controllerX = controllerX;
        this.controllerY = controllerY;
        this.controllerZ = controllerZ;
    }

    public ChunkCoordinates getController() {
        return controllerX == 0 && controllerY == 0 && controllerZ == 0 ? new ChunkCoordinates(xCoord, yCoord, zCoord)
            : new ChunkCoordinates(controllerX, controllerY, controllerZ);
    }

    public boolean isController() {
        return controllerX == xCoord && controllerY == yCoord && controllerZ == zCoord;
    }

    public float getBeltMovementSpeed() {
        return getSpeed() / 480f;
    }

    public float getDirectionAwareBeltMovementSpeed() {
        int offset = getBeltFacing().getAxisDirection()
            .getStep();
        if (getBeltFacing().getAxis() == Axis.X) offset *= -1;
        return getBeltMovementSpeed() * offset;
    }

    public boolean hasPulley() {
        if (this.getBlockType() != AllBlocks.belt) return false;
        return partType != BeltPart.MIDDLE;
    }

    protected boolean isLastBelt() {
        if (getSpeed() == 0) return false;

        Direction direction = getBeltFacing();
        if (slopeType == BeltSlope.VERTICAL) return false;

        if (partType == BeltPart.MIDDLE) return false;

        boolean movingPositively = (getSpeed() > 0 == (direction.getAxisDirection()
            .getStep() == 1)) ^ direction.getAxis() == Axis.X;
        return partType == BeltPart.START ^ movingPositively;
    }

    public ChunkCoordinates getMovementDirection(boolean firstHalf) {
        return this.getMovementDirection(firstHalf, false);
    }

    public ChunkCoordinates getBeltChainDirection() {
        return this.getMovementDirection(true, true);
    }

    protected ChunkCoordinates getMovementDirection(boolean firstHalf, boolean ignoreHalves) {
        if (getSpeed() == 0) return new ChunkCoordinates(0, 0, 0);

        final BeltBlock block = (BeltBlock) getBlockType();
        final int meta = getBlockMetadata();
        final Direction beltFacing = block.getDirection(meta);
        final Axis axis = beltFacing.getAxis();

        Direction movementFacing = Direction
            .get(axis == Axis.X ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE, axis);
        boolean notHorizontal = slopeType != BeltSlope.HORIZONTAL;
        if (getSpeed() < 0) movementFacing = movementFacing.getOpposite();
        ChunkCoordinates movement = movementFacing.getNormal();

        boolean slopeBeforeHalf = (partType == BeltPart.END)
            == (beltFacing.getAxisDirection() == AxisDirection.POSITIVE);
        boolean onSlope = notHorizontal
            && (partType == BeltPart.MIDDLE || slopeBeforeHalf == firstHalf || ignoreHalves);
        boolean movingUp = onSlope
            && slopeType == (movementFacing == beltFacing ? BeltSlope.UPWARD : BeltSlope.DOWNWARD);

        if (!onSlope) return movement;

        return new ChunkCoordinates(movement.posX, movingUp ? 1 : -1, movement.posZ);
    }

    public Direction getMovementFacing() {
        Axis axis = getBeltFacing().getAxis();
        return Direction
            .get(getBeltMovementSpeed() < 0 ^ axis == Axis.X ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE, axis);
    }

    protected Direction getBeltFacing() {
        return ((BeltBlock) getBlockType()).getDirection(getBlockMetadata());
    }

    public BeltInventory getInventory() {
        if (!isController()) {
            BeltTileEntity controllerTE = getControllerTE();
            if (controllerTE != null) return controllerTE.getInventory();
            return null;
        }
        if (inventory == null) {
            inventory = new BeltInventory(this);
        }
        return inventory;
    }

    private void applyToAllItems(float maxDistanceFromCenter,
        Function<TransportedItemStack, TransportedResult> processFunction) {
        BeltTileEntity controller = getControllerTE();
        if (controller == null) return;
        BeltInventory inventory = controller.getInventory();
        if (inventory != null) inventory.applyToEachWithin(index + .5f, maxDistanceFromCenter, processFunction);
    }

    private Vec3 getWorldPositionOf(TransportedItemStack transported) {
        BeltTileEntity controllerTE = getControllerTE();
        if (controllerTE == null) return VecHelper.ZERO;
        return BeltHelper.getVectorForOffset(controllerTE, transported.beltPosition);
    }

    public void setCasingType(CasingType type) {
        if (casing == type) return;
        if (casing != CasingType.NONE) worldObj.playAuxSFX(
            2001,
            xCoord,
            yCoord,
            zCoord,
            (Block.getIdFromBlock(AllBlocks.casing) & 4095) | (casing == CasingType.BRASS ? 1 : 0) << 12);
        casing = type;
        markDirty();
        sendData();
    }

    private boolean canInsertFrom(Direction side) {
        if (getSpeed() == 0) return false;
        if (slopeType == BeltSlope.SIDEWAYS || slopeType == BeltSlope.VERTICAL) return false;
        return getMovementFacing() != side.getOpposite();
    }

    private ItemStack tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
        BeltTileEntity nextBeltController = getControllerTE();
        ItemStack inserted = transportedStack.stack;
        ItemStack empty = null;

        if (nextBeltController == null) return inserted;
        BeltInventory nextInventory = nextBeltController.getInventory();
        if (nextInventory == null) return inserted;

        // TODO
        // TileEntity teAbove = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
        // if (teAbove instanceof BrassTunnelTileEntity) {
        // BrassTunnelTileEntity tunnelTE = (BrassTunnelTileEntity) teAbove;
        // if (tunnelTE.hasDistributionBehaviour()) {
        // if (!tunnelTE.getStackToDistribute()
        // .isEmpty()) return inserted;
        // if (!tunnelTE.testFlapFilter(side.getOpposite(), inserted)) return inserted;
        // if (!simulate) {
        // BeltTunnelInteractionHandler.flapTunnel(nextInventory, index, side.getOpposite(), true);
        // tunnelTE.setStackToDistribute(inserted);
        // }
        // return empty;
        // }
        // }

        if (getSpeed() == 0) return inserted;
        if (getMovementFacing() == side.getOpposite()) return inserted;
        if (!nextInventory.canInsertAtFromSide(index, side)) return inserted;
        if (simulate) return empty;

        transportedStack = transportedStack.copy();
        transportedStack.beltPosition = index + .5f - Math.signum(getDirectionAwareBeltMovementSpeed()) / 16f;

        Direction movementFacing = getMovementFacing();
        if (!side.getAxis()
            .isVertical()) {
            if (movementFacing != side) {
                transportedStack.sideOffset = side.getAxisDirection()
                    .getStep() * .35f;
                if (side.getAxis() == Axis.X) transportedStack.sideOffset *= -1;
            } else transportedStack.beltPosition = getDirectionAwareBeltMovementSpeed() > 0 ? index : index + 1;
        }

        transportedStack.prevSideOffset = transportedStack.sideOffset;
        transportedStack.insertedAt = index;
        transportedStack.insertedFrom = side;
        transportedStack.prevBeltPosition = transportedStack.beltPosition;

        // TODO
        // BeltTunnelInteractionHandler.flapTunnel(nextInventory, index, side.getOpposite(), true);

        nextInventory.addItem(transportedStack);
        nextBeltController.markDirty();
        nextBeltController.sendData();
        return empty;
    }

    public ChunkCoordinates nextSegmentPosition(boolean forward) {
        if (!(getBlockType() instanceof BeltBlock block)) return null;
        int meta = getBlockMetadata();
        Direction direction = block.getDirection(meta);
        BeltSlope slope = slopeType;
        BeltPart part = partType;

        int offset = forward ? 1 : -1;

        if (part == BeltPart.END && forward || part == BeltPart.START && !forward) return null;
        ChunkCoordinates pos = new ChunkCoordinates(xCoord, yCoord, zCoord);
        if (slope == BeltSlope.VERTICAL) {
            pos.posY += direction.getAxisDirection() == AxisDirection.POSITIVE ? offset : -offset;
            return pos;
        }
        ChunkCoordinates normal = direction.getNormal();
        pos.posX += normal.posX * offset;
        pos.posY += normal.posY * offset;
        pos.posZ += normal.posZ * offset;
        if (slope != BeltSlope.HORIZONTAL && slope != BeltSlope.SIDEWAYS) {
            pos.posY += slope == BeltSlope.UPWARD ? offset : -offset;
            return pos;
        }
        return pos;
    }

    @Override
    protected boolean canPropagateDiagonally(IRotate block) {
        return slopeType == BeltSlope.UPWARD || slopeType == BeltSlope.DOWNWARD;
    }

    @Override
    public float propagateRotationTo(KineticTileEntity target, Vec3 diff, boolean connectedViaAxes,
        boolean connectedViaCogs) {
        if (target instanceof BeltTileEntity && !connectedViaAxes)
            return getController().equals(((BeltTileEntity) target).getController()) ? 1 : 0;
        return 0;
    }

    public void invalidateItemHandler() {
        itemHandler.invalidate();
    }

    // TODO
    // @Override
    // public boolean shouldRenderNormally() {
    // if (worldObj == null) return isController();
    // BlockState state = getBlockType();
    // return state != null && state.hasProperty(BeltBlock.PART) && state.getValue(BeltBlock.PART) == BeltPart.START;
    // }
    //
    // public GridAlignedBB getVolume() {
    // ChunkCoordinates endPos = BeltHelper.getPositionForOffset(this, beltLength - 1);
    // GridAlignedBB bb = GridAlignedBB.from(xCoord, yCoord, zCoord, endPos);
    // bb.fixMinMax();
    // return bb;
    // }
    //
    // @Override
    // public void onLightUpdate(LightProvider world, LightLayer type, ImmutableBox changed) {
    // if (this.remove) return;
    // if (this.worldObj == null) return;
    //
    // GridAlignedBB beltVolume = getVolume();
    //
    // if (beltVolume.intersects(changed)) {
    // if (light == null) {
    // initializeLight();
    // return;
    // }
    //
    // if (type == LightLayer.BLOCK) updateBlockLight();
    //
    // if (type == LightLayer.SKY) updateSkyLight();
    // }
    // }
    //
    // private void initializeLight() {
    // if (beltLength > 0) {
    // light = new byte[beltLength * 2];
    //
    // ChunkCoordinates vec = getBeltFacing().getNormal();
    // BeltSlope slope = getBlockType().getValue(BeltBlock.SLOPE);
    // int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
    //
    // MutableBlockPos pos = new MutableBlockPos(controller.getX(), controller.getY(), controller.getZ());
    // for (int i = 0; i < beltLength * 2; i += 2) {
    // light[i] = (byte) worldObj.getBrightness(LightLayer.BLOCK, pos);
    // light[i + 1] = (byte) worldObj.getBrightness(LightLayer.SKY, pos);
    // pos.move(vec.getX(), verticality, vec.getZ());
    // }
    // }
    // }
    //
    // private void updateBlockLight() {
    // ChunkCoordinates vec = getBeltFacing().getNormal();
    // BeltSlope slope = getBlockType().getValue(BeltBlock.SLOPE);
    // int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
    //
    // MutableBlockPos pos = new MutableBlockPos(controller.getX(), controller.getY(), controller.getZ());
    // for (int i = 0; i < beltLength * 2; i += 2) {
    // light[i] = (byte) worldObj.getBrightness(LightLayer.BLOCK, pos);
    //
    // pos.move(vec.getX(), verticality, vec.getZ());
    // }
    // }
    //
    // private void updateSkyLight() {
    // ChunkCoordinates vec = getBeltFacing().getNormal();
    // BeltSlope slope = getBlockType().getValue(BeltBlock.SLOPE);
    // int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
    //
    // MutableBlockPos pos = new MutableBlockPos(controller.getX(), controller.getY(), controller.getZ());
    // for (int i = 1; i < beltLength * 2; i += 2) {
    // light[i] = (byte) worldObj.getBrightness(LightLayer.SKY, pos);
    //
    // pos.move(vec.getX(), verticality, vec.getZ());
    // }
    // }
}
