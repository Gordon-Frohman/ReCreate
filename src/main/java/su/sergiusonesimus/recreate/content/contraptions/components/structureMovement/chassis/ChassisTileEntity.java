package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;

import cpw.mods.fml.common.FMLCommonHandler;
import su.sergiusonesimus.metaworlds.util.BlockVolatilityMap;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.BlockMovementChecks;
import su.sergiusonesimus.recreate.foundation.config.AllConfigs;
import su.sergiusonesimus.recreate.foundation.tileentity.SmartTileEntity;
import su.sergiusonesimus.recreate.foundation.tileentity.TileEntityBehaviour;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.CenteredSideValueBoxTransform;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.scrollvalue.BulkScrollValueBehaviour;
import su.sergiusonesimus.recreate.foundation.tileentity.behaviour.scrollvalue.ScrollValueBehaviour;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
import su.sergiusonesimus.recreate.foundation.utility.Lang;

public class ChassisTileEntity extends SmartTileEntity {

    ScrollValueBehaviour range;
    Map<Direction, Boolean> stickySides = new HashMap<Direction, Boolean>();

    public ChassisTileEntity() {
        super();
        for (Direction side : Iterate.directions) stickySides.put(side, false);
    }

    @SuppressWarnings("static-access")
    @Override
    public void addBehaviours(List<TileEntityBehaviour> behaviours) {
        int max = AllConfigs.SERVER.kinetics.maxChassisRange;
        range = new BulkScrollValueBehaviour(
            Lang.translate("generic.range"),
            this,
            new CenteredSideValueBoxTransform(),
            te -> ((ChassisTileEntity) te).collectChassisGroup());
        range.requiresWrench();
        range.between(1, max);
        range.withClientCallback(i -> {
            if (FMLCommonHandler.instance()
                .getSide()
                .isClient()) {
                ChassisRangeDisplay.display(this);
            }
        });
        range.value = max / 2;
        behaviours.add(range);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (getBlockType() instanceof RadialChassisBlock) range.setLabel(Lang.translate("generic.radius"));
    }

    public int getRange() {
        return range.getValue();
    }

    public List<ChunkCoordinates> getIncludedBlockPositions(Direction forcedMovement, boolean visualize) {
        if (!(getBlockType() instanceof AbstractChassisBlock)) return Collections.emptyList();
        return isRadial() ? getIncludedBlockPositionsRadial(forcedMovement, visualize)
            : getIncludedBlockPositionsLinear(forcedMovement, visualize);
    }

    protected boolean isRadial() {
        return worldObj.getBlock(xCoord, yCoord, zCoord) instanceof RadialChassisBlock;
    }

    public List<ChassisTileEntity> collectChassisGroup() {
        Queue<ChunkCoordinates> frontier = new LinkedList<>();
        List<ChassisTileEntity> collected = new ArrayList<>();
        Set<ChunkCoordinates> visited = new HashSet<>();
        frontier.add(new ChunkCoordinates(xCoord, yCoord, zCoord));
        while (!frontier.isEmpty()) {
            ChunkCoordinates current = frontier.poll();
            if (visited.contains(current)) continue;
            visited.add(current);
            TileEntity tileEntity = worldObj.getTileEntity(current.posX, current.posY, current.posZ);
            if (tileEntity instanceof ChassisTileEntity) {
                ChassisTileEntity chassis = (ChassisTileEntity) tileEntity;
                collected.add(chassis);
                visited.add(current);
                chassis.addAttachedChasses(frontier, visited);
            }
        }
        return collected;
    }

    public boolean addAttachedChasses(Queue<ChunkCoordinates> frontier, Set<ChunkCoordinates> visited) {
        int meta = getBlockMetadata();
        if (!(getBlockType() instanceof AbstractChassisBlock chassis)) return false;
        Axis axis = chassis.getAxis(meta);
        if (isRadial()) {

            // Collect chain of radial chassis
            for (int offset : new int[] { -1, 1 }) {
                Direction direction = Direction.get(AxisDirection.POSITIVE, axis);
                ChunkCoordinates normal = direction.getNormal();
                ChunkCoordinates currentPos = new ChunkCoordinates(
                    xCoord + normal.posX * offset,
                    yCoord + normal.posY * offset,
                    zCoord + normal.posZ * offset);
                if (!worldObj.blockExists(currentPos.posX, currentPos.posY, currentPos.posZ)) return false;

                Block neighbourBlock = worldObj.getBlock(currentPos.posX, currentPos.posY, currentPos.posZ);
                int neighbourMeta = worldObj.getBlockMetadata(currentPos.posX, currentPos.posY, currentPos.posZ);
                if (!(neighbourBlock instanceof RadialChassisBlock radialChassis)) continue;
                if (axis != radialChassis.getAxis(neighbourMeta)) continue;
                if (!visited.contains(currentPos)) frontier.add(currentPos);
            }

            return true;
        }

        // Collect group of connected linear chassis
        for (Direction offset : Iterate.directions) {
            ChunkCoordinates normal = offset.getNormal();
            ChunkCoordinates current = new ChunkCoordinates(
                xCoord + normal.posX,
                yCoord + normal.posY,
                zCoord + normal.posZ);
            if (visited.contains(current)) continue;
            if (!worldObj.blockExists(current.posX, current.posY, current.posZ)) return false;

            Block neighbourBlock = worldObj.getBlock(current.posX, current.posY, current.posZ);
            int neighbourMeta = worldObj.getBlockMetadata(current.posX, current.posY, current.posZ);
            if (!(neighbourBlock instanceof LinearChassisBlock linearChassis)) continue;
            if (!LinearChassisBlock.sameKind(getBlockType(), meta, neighbourBlock, neighbourMeta)) continue;
            if (linearChassis.getAxis(neighbourMeta) != axis) continue;

            frontier.add(current);
        }

        return true;
    }

    public Boolean getGlueableSide(Direction side) {
        return stickySides.get(side);
    }

    public void setGlueableSide(Direction side, boolean isSticky) {
        stickySides.put(side, isSticky);
    }

    private List<ChunkCoordinates> getIncludedBlockPositionsLinear(Direction forcedMovement, boolean visualize) {
        List<ChunkCoordinates> positions = new ArrayList<>();
        AbstractChassisBlock block = (AbstractChassisBlock) getBlockType();
        Axis axis = block.getAxis(getBlockMetadata());
        Direction facing = Direction.get(AxisDirection.POSITIVE, axis);
        int chassisRange = visualize ? range.scrollableValue : getRange();

        for (int offset : new int[] { 1, -1 }) {
            if (offset == -1) facing = facing.getOpposite();
            boolean sticky = getGlueableSide(facing);
            for (int i = 1; i <= chassisRange; i++) {
                ChunkCoordinates normal = facing.getNormal();
                ChunkCoordinates current = new ChunkCoordinates(
                    xCoord + normal.posX * i,
                    yCoord + normal.posY * i,
                    zCoord + normal.posZ * i);
                Block currentBlock = worldObj.getBlock(current.posX, current.posY, current.posZ);
                int currentMeta = worldObj.getBlockMetadata(current.posX, current.posY, current.posZ);

                if (forcedMovement != facing && !sticky) break;

                // Ignore replaceable Blocks and Air-like
                if (!BlockMovementChecks.isMovementNecessary(currentBlock, currentMeta, worldObj, current)) break;
                if (BlockVolatilityMap.checkBlockVolatility(currentBlock)) break;

                positions.add(current);

                if (BlockMovementChecks.isNotSupportive(currentBlock, currentMeta, facing)) break;
            }
        }

        return positions;
    }

    private List<ChunkCoordinates> getIncludedBlockPositionsRadial(Direction forcedMovement, boolean visualize) {
        List<ChunkCoordinates> positions = new ArrayList<>();
        AbstractChassisBlock block = (AbstractChassisBlock) worldObj.getBlock(xCoord, yCoord, zCoord);
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        Axis axis = block.getAxis(meta);
        int chassisRange = visualize ? range.scrollableValue : getRange();

        for (Direction facing : Iterate.directions) {
            if (facing.getAxis() == axis) continue;
            if (!getGlueableSide(facing)) continue;

            ChunkCoordinates normal = facing.getNormal();
            ChunkCoordinates startPos = new ChunkCoordinates(
                xCoord + normal.posX,
                yCoord + normal.posY,
                zCoord + normal.posZ);
            List<ChunkCoordinates> localFrontier = new LinkedList<>();
            Set<ChunkCoordinates> localVisited = new HashSet<>();
            localFrontier.add(startPos);

            while (!localFrontier.isEmpty()) {
                ChunkCoordinates searchPos = localFrontier.remove(0);
                Block searchedBlock = worldObj.getBlock(searchPos.posX, searchPos.posY, searchPos.posZ);
                int searchedMeta = worldObj.getBlockMetadata(searchPos.posX, searchPos.posY, searchPos.posZ);

                if (localVisited.contains(searchPos)) continue;
                if (Math.sqrt(searchPos.getDistanceSquared(xCoord, yCoord, zCoord)) >= chassisRange + .5f) continue;
                if (!BlockMovementChecks.isMovementNecessary(searchedBlock, searchedMeta, worldObj, searchPos))
                    continue;
                if (BlockVolatilityMap.checkBlockVolatility(searchedBlock)) continue;

                localVisited.add(searchPos);
                if (searchPos.posX != xCoord || searchPos.posY != yCoord || searchPos.posZ != zCoord)
                    positions.add(searchPos);

                for (Direction offset : Iterate.directions) {
                    if (offset.getAxis() == axis) continue;
                    if (searchPos.posX == xCoord && searchPos.posY == yCoord
                        && searchPos.posZ == zCoord
                        && offset != facing) continue;
                    if (BlockMovementChecks.isNotSupportive(searchedBlock, searchedMeta, offset)) continue;

                    normal = offset.getNormal();
                    localFrontier.add(
                        new ChunkCoordinates(
                            searchPos.posX + normal.posX,
                            searchPos.posY + normal.posY,
                            searchPos.posZ + normal.posZ));
                }
            }
        }

        return positions;
    }

    protected void fromTag(NBTTagCompound compound, boolean clientPacket) {
        super.fromTag(compound, clientPacket);
        NBTTagCompound stickySides = compound.getCompoundTag("stickySides");
        if (stickySides != null) {
            boolean rerenderBlock = false;
            for (Direction side : Iterate.directions) {
                boolean isSideSticky = stickySides.getBoolean(side.getSerializedName());
                if (clientPacket && !rerenderBlock
                    && this.stickySides.get(side) != null
                    && this.stickySides.get(side)
                        .booleanValue() != isSideSticky)
                    rerenderBlock = true;
                this.stickySides.put(side, isSideSticky);
            }
            if (rerenderBlock) getWorld().markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    protected void write(NBTTagCompound compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        NBTTagCompound stickySides = new NBTTagCompound();
        for (Direction side : Iterate.directions) {
            stickySides.setBoolean(side.getSerializedName(), this.stickySides.get(side));
        }
        compound.setTag("stickySides", stickySides);
    }

}
