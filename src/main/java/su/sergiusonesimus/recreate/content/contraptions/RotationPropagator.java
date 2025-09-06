package su.sergiusonesimus.recreate.content.contraptions;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.content.contraptions.base.IRotate;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.ICogWheel;
import su.sergiusonesimus.recreate.content.contraptions.relays.encased.DirectionalShaftHalvesTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.encased.SplitShaftTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.gearbox.GearboxTileEntity;
import su.sergiusonesimus.recreate.foundation.config.AllConfigs;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;

public class RotationPropagator {

    private static final int MAX_FLICKER_SCORE = 128;

    /**
     * Determines the change in rotation between two attached kinetic entities. For
     * instance, an axis connection returns 1 while a 1-to-1 gear connection
     * reverses the rotation and therefore returns -1.
     * 
     * @param from
     * @param to
     * @return
     */
    private static float getRotationSpeedModifier(KineticTileEntity from, KineticTileEntity to) {
        Block fromBlock = from.blockType;
        int fromMeta = from.blockMetadata;
        Block toBlock = to.blockType;
        int toMeta = to.blockMetadata;
        if (!(fromBlock instanceof IRotate && toBlock instanceof IRotate)) return 0;

        final IRotate definitionFrom = (IRotate) fromBlock;
        final IRotate definitionTo = (IRotate) toBlock;
        final Vec3 diff = Vec3.createVectorHelper(to.xCoord, to.yCoord, to.zCoord)
            .addVector(-from.xCoord, -from.yCoord, -from.zCoord);
        final Direction direction = Direction.getNearest(diff.xCoord, diff.yCoord, diff.zCoord);
        final World world = from.getWorldObj();

        boolean alignedAxes = true;
        for (Axis axis : Axis.values()) if (axis != direction.getAxis())
            if (axis.choose(diff.xCoord, diff.yCoord, diff.zCoord) != 0) alignedAxes = false;

        boolean connectedByAxis = alignedAxes
            && definitionFrom.hasShaftTowards(world, from.xCoord, from.yCoord, from.zCoord, direction)
            && definitionTo.hasShaftTowards(world, to.xCoord, to.yCoord, to.zCoord, direction.getOpposite());

        boolean connectedByGears = ICogWheel.isSmallCog(fromBlock) && ICogWheel.isSmallCog(toBlock);

        float custom = from.propagateRotationTo(to, diff, connectedByAxis, connectedByGears);
        if (custom != 0) return custom;

        // Axis <-> Axis
        if (connectedByAxis) {
            float axisModifier = getAxisModifier(to, direction.getOpposite());
            if (axisModifier != 0) axisModifier = 1 / axisModifier;
            return getAxisModifier(from, direction) * axisModifier;
        }

        // Attached Encased Belts
        // TODO
        // if (fromBlock instanceof EncasedBeltBlock && toBlock instanceof EncasedBeltBlock) {
        // boolean connected = EncasedBeltBlock.areBlocksConnected(stateFrom, stateTo, direction);
        // return connected ? EncasedBeltBlock.getRotationSpeedModifier(from, to) : 0;
        // }

        // Large Gear <-> Large Gear
        if (isLargeToLargeGear(from, to, diff)) {
            Axis sourceAxis = ((IRotate) fromBlock).getAxis(fromMeta);
            Axis targetAxis = ((IRotate) toBlock).getAxis(toMeta);
            int sourceAxisDiff = (int) sourceAxis.choose(diff.xCoord, diff.yCoord, diff.zCoord);
            int targetAxisDiff = (int) targetAxis.choose(diff.xCoord, diff.yCoord, diff.zCoord);

            return sourceAxisDiff > 0 ^ targetAxisDiff > 0 ? -1 : 1;
        }

        // Gear <-> Large Gear
        if (ICogWheel.isLargeCog(fromBlock) && ICogWheel.isSmallCog(toBlock))
            if (isLargeToSmallCog(from, to, definitionTo, diff)) return -2f;
        if (ICogWheel.isLargeCog(toBlock) && ICogWheel.isSmallCog(fromBlock))
            if (isLargeToSmallCog(to, from, definitionFrom, diff)) return -.5f;

        // Gear <-> Gear
        if (connectedByGears) {
            if (Math.abs(diff.xCoord) + Math.abs(diff.yCoord) + Math.abs(diff.zCoord) != 1) return 0;
            if (ICogWheel.isLargeCog(toBlock)) return 0;
            if (direction.getAxis() == definitionFrom.getAxis(fromMeta)) return 0;
            if (definitionFrom.getAxis(fromMeta) == definitionTo.getAxis(toMeta)) return -1;
        }

        return 0;
    }

    private static float getConveyedSpeed(KineticTileEntity from, KineticTileEntity to) {
        // Rotation Speed Controller <-> Large Gear
        // TODO
        // if (isLargeCogToSpeedController(from, to, Vec3.createVectorHelper(to.xCoord, to.yCoord,
        // to.zCoord).addVector(-from.xCoord, -from.yCoord, -from.zCoord)))
        // return SpeedControllerTileEntity.getConveyedSpeed(from, to, true);
        // if (isLargeCogToSpeedController(to, from, Vec3.createVectorHelper(from.xCoord, from.yCoord,
        // from.zCoord).addVector(-to.xCoord, -to.yCoord, -to.zCoord)))
        // return SpeedControllerTileEntity.getConveyedSpeed(to, from, false);

        float rotationSpeedModifier = getRotationSpeedModifier(from, to);
        return from.getTheoreticalSpeed() * rotationSpeedModifier;
    }

    private static boolean isLargeToLargeGear(KineticTileEntity from, KineticTileEntity to, Vec3 diff) {
        if (!ICogWheel.isLargeCog(from.blockType) || !ICogWheel.isLargeCog(to.blockType)) return false;
        Axis fromAxis = ((IRotate) from.blockType).getAxis(from.getBlockMetadata());
        Axis toAxis = ((IRotate) to.blockType).getAxis(to.getBlockMetadata());
        if (fromAxis == toAxis) return false;
        for (Axis axis : Axis.values()) {
            int axisDiff = (int) axis.choose(diff.xCoord, diff.yCoord, diff.zCoord);
            if (axis == fromAxis || axis == toAxis) {
                if (axisDiff == 0) return false;

            } else if (axisDiff != 0) return false;
        }
        return true;
    }

    private static float getAxisModifier(KineticTileEntity te, Direction direction) {
        if (!(te.hasSource() || te.isSource()) || !(te instanceof DirectionalShaftHalvesTileEntity)) return 1;
        Direction source = ((DirectionalShaftHalvesTileEntity) te).getSourceFacing();

        if (te instanceof GearboxTileEntity)
            return direction.getAxis() == source.getAxis() ? direction == source ? 1 : -1
                : direction.getAxisDirection() == source.getAxisDirection() ? -1 : 1;

        if (te instanceof SplitShaftTileEntity) return ((SplitShaftTileEntity) te).getRotationSpeedModifier(direction);

        return 1;
    }

    private static boolean isLargeToSmallCog(KineticTileEntity from, KineticTileEntity to, IRotate defTo, Vec3 diff) {
        Axis axisFrom = ((IRotate) from.blockType).getAxis(from.getBlockMetadata());
        if (axisFrom != defTo.getAxis(to.getBlockMetadata())) return false;
        if (axisFrom.choose(diff.xCoord, diff.yCoord, diff.zCoord) != 0) return false;
        for (Axis axis : Axis.values()) {
            if (axis == axisFrom) continue;
            if (Math.abs(axis.choose(diff.xCoord, diff.yCoord, diff.zCoord)) != 1) return false;
        }
        return true;
    }

    private static boolean isLargeCogToSpeedController(KineticTileEntity from, KineticTileEntity to, Vec3 diff) {
        if (!ICogWheel.isLargeCog(from.blockType) /* || !AllBlocks.ROTATION_SPEED_CONTROLLER.has(to) */) return false;
        if (diff.xCoord != 0 || diff.yCoord != -1 || diff.zCoord != 0) return false;
        // Axis axis = from.getValue(CogWheelBlock.AXIS);
        // if (axis.isVertical())
        // return false;
        // if (to.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) == axis)
        // return false;
        return true;
    }

    /**
     * Insert the added position to the kinetic network.
     * 
     * @param worldIn
     * @param x
     * @param y
     * @param z
     */
    public static void handleAdded(World worldIn, int x, int y, int z, KineticTileEntity addedTE) {
        if (worldIn.isRemote) return;
        if (!worldIn.blockExists(x, y, z)) return;
        propagateNewSource(addedTE);
    }

    /**
     * Search for sourceless networks attached to the given entity and update them.
     * 
     * @param currentTE
     */
    private static void propagateNewSource(KineticTileEntity currentTE) {
        int posX = currentTE.xCoord;
        int posY = currentTE.yCoord;
        int posZ = currentTE.zCoord;
        World world = currentTE.getWorldObj();

        for (KineticTileEntity neighbourTE : getConnectedNeighbours(currentTE)) {
            float speedOfCurrent = currentTE.getTheoreticalSpeed();
            float speedOfNeighbour = neighbourTE.getTheoreticalSpeed();
            float newSpeed = getConveyedSpeed(currentTE, neighbourTE);
            float oppositeSpeed = getConveyedSpeed(neighbourTE, currentTE);

            if (newSpeed == 0 && oppositeSpeed == 0) continue;

            boolean incompatible = Math.signum(newSpeed) != Math.signum(speedOfNeighbour)
                && (newSpeed != 0 && speedOfNeighbour != 0);

            boolean tooFast = Math.abs(newSpeed) > AllConfigs.SERVER.kinetics.maxRotationSpeed;
            boolean speedChangedTooOften = currentTE.getFlickerScore() > MAX_FLICKER_SCORE;
            if (tooFast || speedChangedTooOften) {
                currentTE.blockType.dropBlockAsItem(world, posX, posY, posZ, currentTE.getBlockMetadata(), 0);
                world.setBlockToAir(posX, posY, posZ);
                return;
            }

            // Opposite directions
            if (incompatible) {
                currentTE.blockType.dropBlockAsItem(world, posX, posY, posZ, currentTE.getBlockMetadata(), 0);
                world.setBlockToAir(posX, posY, posZ);
                return;

                // Same direction: overpower the slower speed
            } else {

                // Neighbour faster, overpower the incoming tree
                if (Math.abs(oppositeSpeed) > Math.abs(speedOfCurrent)) {
                    float prevSpeed = currentTE.getSpeed();
                    currentTE.setSource(neighbourTE.xCoord, neighbourTE.yCoord, neighbourTE.zCoord);
                    currentTE.setSpeed(getConveyedSpeed(neighbourTE, currentTE));
                    currentTE.onSpeedChanged(prevSpeed);
                    currentTE.sendData();

                    propagateNewSource(currentTE);
                    return;
                }

                // Current faster, overpower the neighbours' tree
                if (Math.abs(newSpeed) >= Math.abs(speedOfNeighbour)) {

                    // Do not overpower you own network -> cycle
                    if (!currentTE.hasNetwork() || currentTE.network.equals(neighbourTE.network)) {
                        float epsilon = Math.abs(speedOfNeighbour) / 256f / 256f;
                        if (Math.abs(newSpeed) > Math.abs(speedOfNeighbour) + epsilon) {
                            currentTE.blockType
                                .dropBlockAsItem(world, posX, posY, posZ, currentTE.getBlockMetadata(), 0);
                            world.setBlockToAir(posX, posY, posZ);
                        }
                        continue;
                    }

                    if (currentTE.hasSource() && currentTE.sourceX == neighbourTE.xCoord
                        && currentTE.sourceY == neighbourTE.yCoord
                        && currentTE.sourceZ == neighbourTE.zCoord) currentTE.removeSource();

                    float prevSpeed = neighbourTE.getSpeed();
                    neighbourTE.setSource(currentTE.xCoord, currentTE.yCoord, currentTE.zCoord);
                    neighbourTE.setSpeed(getConveyedSpeed(currentTE, neighbourTE));
                    neighbourTE.onSpeedChanged(prevSpeed);
                    neighbourTE.sendData();
                    propagateNewSource(neighbourTE);
                    continue;
                }
            }

            if (neighbourTE.getTheoreticalSpeed() == newSpeed) continue;

            float prevSpeed = neighbourTE.getSpeed();
            neighbourTE.setSpeed(newSpeed);
            neighbourTE.setSource(currentTE.xCoord, currentTE.yCoord, currentTE.zCoord);
            neighbourTE.onSpeedChanged(prevSpeed);
            neighbourTE.sendData();
            propagateNewSource(neighbourTE);

        }
    }

    /**
     * Remove the given entity from the network.
     * 
     * @param worldIn
     * @param x
     * @param y
     * @param z
     * @param removedTE
     */
    public static void handleRemoved(World worldIn, int x, int y, int z, KineticTileEntity removedTE) {
        if (worldIn.isRemote) return;
        if (removedTE == null) return;
        if (removedTE.getTheoreticalSpeed() == 0) return;

        for (ChunkCoordinates neighbourPos : getPotentialNeighbourLocations(removedTE)) {
            Block block = worldIn.getBlock(neighbourPos.posX, neighbourPos.posY, neighbourPos.posZ);
            if (!(block instanceof IRotate)) continue;
            TileEntity tileEntity = worldIn.getTileEntity(neighbourPos.posX, neighbourPos.posY, neighbourPos.posZ);
            if (!(tileEntity instanceof KineticTileEntity)) continue;

            final KineticTileEntity neighbourTE = (KineticTileEntity) tileEntity;
            if (!neighbourTE.hasSource()
                || !(neighbourTE.sourceX == x && neighbourTE.sourceY == y && neighbourTE.sourceZ == z)) continue;

            propagateMissingSource(neighbourTE);
        }

    }

    /**
     * Clear the entire subnetwork depending on the given entity and find a new
     * source
     * 
     * @param updateTE
     */
    private static void propagateMissingSource(KineticTileEntity updateTE) {
        final World world = updateTE.getWorldObj();

        List<KineticTileEntity> potentialNewSources = new LinkedList<>();
        List<ChunkCoordinates> frontier = new LinkedList<>();
        frontier.add(new ChunkCoordinates(updateTE.xCoord, updateTE.yCoord, updateTE.zCoord));
        ChunkCoordinates missingSource = updateTE.hasSource()
            ? new ChunkCoordinates(updateTE.sourceX, updateTE.sourceZ, updateTE.sourceZ)
            : null;

        while (!frontier.isEmpty()) {
            final ChunkCoordinates pos = frontier.remove(0);
            TileEntity tileEntity = world.getTileEntity(pos.posX, pos.posY, pos.posZ);
            if (!(tileEntity instanceof KineticTileEntity)) continue;
            final KineticTileEntity currentTE = (KineticTileEntity) tileEntity;

            currentTE.removeSource();
            currentTE.sendData();

            for (KineticTileEntity neighbourTE : getConnectedNeighbours(currentTE)) {
                if (neighbourTE.xCoord == missingSource.posX && neighbourTE.yCoord == missingSource.posY
                    && neighbourTE.zCoord == missingSource.posZ) continue;
                if (!neighbourTE.hasSource()) continue;

                if (neighbourTE.sourceX != pos.posX || neighbourTE.sourceY != pos.posY
                    || neighbourTE.sourceZ != pos.posZ) {
                    potentialNewSources.add(neighbourTE);
                    continue;
                }

                if (neighbourTE.isSource()) potentialNewSources.add(neighbourTE);

                frontier.add(new ChunkCoordinates(neighbourTE.xCoord, neighbourTE.yCoord, neighbourTE.zCoord));
            }
        }

        for (KineticTileEntity newSource : potentialNewSources) {
            if (newSource.hasSource() || newSource.isSource()) {
                propagateNewSource(newSource);
                return;
            }
        }
    }

    private static KineticTileEntity findConnectedNeighbour(KineticTileEntity currentTE, int neighbourX, int neighbourY,
        int neighbourZ) {
        Block neighbourBlock = currentTE.getWorldObj()
            .getBlock(neighbourX, neighbourY, neighbourZ);
        if (!(neighbourBlock instanceof IRotate)) return null;
        TileEntity neighbourTE = currentTE.getWorldObj()
            .getTileEntity(neighbourX, neighbourY, neighbourZ);
        if (neighbourTE == null || !(neighbourTE instanceof KineticTileEntity neighbourKTE)) return null;
        if (!(neighbourKTE.getBlockType() instanceof IRotate)) return null;
        if (!isConnected(currentTE, neighbourKTE) && !isConnected(neighbourKTE, currentTE)) return null;
        return neighbourKTE;
    }

    public static boolean isConnected(KineticTileEntity from, KineticTileEntity to) {
        return isLargeCogToSpeedController(
            from,
            to,
            Vec3.createVectorHelper(to.xCoord, to.yCoord, to.zCoord)
                .addVector(-from.xCoord, -from.yCoord, -from.zCoord))
            || getRotationSpeedModifier(from, to) != 0
            || from.isCustomConnection(to);
    }

    private static List<KineticTileEntity> getConnectedNeighbours(KineticTileEntity te) {
        List<KineticTileEntity> neighbours = new LinkedList<>();
        for (ChunkCoordinates neighbourPos : getPotentialNeighbourLocations(te)) {
            final KineticTileEntity neighbourTE = findConnectedNeighbour(
                te,
                neighbourPos.posX,
                neighbourPos.posY,
                neighbourPos.posZ);
            if (neighbourTE == null) continue;

            neighbours.add(neighbourTE);
        }
        return neighbours;
    }

    private static List<ChunkCoordinates> getPotentialNeighbourLocations(KineticTileEntity te) {
        List<ChunkCoordinates> neighbours = new LinkedList<>();

        if (!te.getWorldObj()
            .blockExists(te.xCoord, te.yCoord, te.zCoord)) return neighbours;

        for (Direction facing : Iterate.directions) {
            ChunkCoordinates normal = facing.getNormal();
            neighbours
                .add(new ChunkCoordinates(te.xCoord + normal.posX, te.yCoord + normal.posY, te.zCoord + normal.posZ));
        }

        Block blockState = te.blockType;
        if (!(blockState instanceof IRotate)) return neighbours;
        IRotate block = (IRotate) blockState;
        return te.addPropagationLocations(block, te.getBlockMetadata(), neighbours);
    }

}
