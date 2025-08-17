package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.IFluidHandler;

import org.apache.commons.lang3.tuple.Pair;

import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.util.BlockVolatilityMap;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.RotationHelper;
import su.sergiusonesimus.metaworlds.world.SubWorldServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.AllSounds;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.StabilizedContraption;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue.SuperGlueHandler;
import su.sergiusonesimus.recreate.foundation.config.AllConfigs;
import su.sergiusonesimus.recreate.foundation.fluid.CombinedTankWrapper;
import su.sergiusonesimus.recreate.foundation.utility.BlockFace;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
import su.sergiusonesimus.recreate.foundation.utility.NBTHelper;
import su.sergiusonesimus.recreate.foundation.utility.UniqueLinkedList;
import su.sergiusonesimus.recreate.zmixin.interfaces.IMixinWorldReCreate;

public abstract class Contraption {

    public Optional<List<AxisAlignedBB>> simplifiedEntityColliders;
    public ContraptionInvWrapper inventory;
    public CombinedTankWrapper fluidInventory;
    public int anchorX;
    public int anchorY;
    public int anchorZ;
    private int anchorWorldID;
    public World anchorWorld;
    public boolean stalled;
    public boolean hasUniversalCreativeCrate;

    protected List<ChunkCoordinates> blocks;
    protected Map<ChunkCoordinates, IInventory> storage;
    protected Map<ChunkCoordinates, IFluidHandler> fluidStorage;
    protected Set<Pair<ChunkCoordinates, Direction>> superglue;
    protected List<ChunkCoordinates> seats;
    protected Map<UUID, Integer> seatMapping;
    private Map<Integer, BlockFace> stabilizedSubContraptionsIDs;
    protected Map<Contraption, BlockFace> stabilizedSubContraptions;

    private List<SuperGlueEntity> glueToRemove;
    private Map<ChunkCoordinates, Entity> initialPassengers;
    private List<BlockFace> pendingSubContraptions;

    private CompletableFuture<Void> simplifiedEntityColliderProvider;

    // Client
    public Map<ChunkCoordinates, TileEntity> presentTileEntities;
    public List<TileEntity> maybeInstancedTileEntities;
    public List<TileEntity> specialRenderedTileEntities;

    protected World contraptionWorld;

    public Contraption() {
        blocks = new ArrayList<>();
        storage = new HashMap<>();
        seats = new ArrayList<>();
        superglue = new HashSet<>();
        seatMapping = new HashMap<>();
        fluidStorage = new HashMap<>();
        glueToRemove = new ArrayList<>();
        initialPassengers = new HashMap<>();
        presentTileEntities = new HashMap<>();
        maybeInstancedTileEntities = new ArrayList<>();
        specialRenderedTileEntities = new ArrayList<>();
        pendingSubContraptions = new ArrayList<>();
        stabilizedSubContraptions = new HashMap<>();
        stabilizedSubContraptionsIDs = new HashMap<>();
        simplifiedEntityColliders = Optional.empty();
    }

    public World getWorld() {
        if (anchorWorld == null) return null;
        if (contraptionWorld == null)
            contraptionWorld = ((IMixinWorldReCreate) anchorWorld).createContraptionWorld(this);
        return contraptionWorld;
    }

    public ContraptionWorld getContraptionWorld() {
        return (ContraptionWorld) getWorld();
    }

    public abstract String getSubWorldType();

    public abstract boolean assemble(World world, int x, int y, int z) throws AssemblyException;

    public void disassemble() {
        if (contraptionWorld == null || anchorWorld == null) return;
        World mainWorld = ((IMixinWorld) anchorWorld).getParentWorld();

        addBlocksToWorld(mainWorld);
        // TODO
        // addPassengersToWorld(level, transform, getPassengers());

        for (Contraption subContraption : stabilizedSubContraptions.keySet()) subContraption.disassemble();

        this.getContraptionWorld()
            .ejectPassengers();
        // TODO
        // moveCollidedEntitiesOnDisassembly(transform);
        AllSounds.CONTRAPTION_DISASSEMBLE.playOnServer(mainWorld, anchorX, anchorY, anchorZ);
    }

    public abstract boolean canBeStabilized(Direction facing, int localX, int localY, int localZ);

    protected abstract ContraptionType getType();

    protected boolean customBlockPlacement(IBlockAccess world, int x, int y, int z, Block block, int meta) {
        return false;
    }

    protected boolean customBlockRemoval(IBlockAccess world, int x, int y, int z, Block block, int meta) {
        return false;
    }

    protected boolean addToInitialFrontier(World world, int x, int y, int z, Direction forcedDirection,
        Queue<ChunkCoordinates> frontier) throws AssemblyException {
        return true;
    }

    public static Contraption fromNBT(NBTTagCompound nbt, boolean spawnData) {
        String type = nbt.getString("Type");
        Contraption contraption = ContraptionType.fromType(type);
        contraption.readNBT(nbt, spawnData);
        contraption.contraptionWorld = null;
        return contraption;
    }

    public boolean searchMovedStructure(World world, int x, int y, int z, @Nullable Direction forcedDirection)
        throws AssemblyException {
        initialPassengers.clear();
        Queue<ChunkCoordinates> frontier = new UniqueLinkedList<>();
        Set<ChunkCoordinates> visited = new HashSet<>();
        anchorX = x;
        anchorY = y;
        anchorZ = z;

        if (!BlockVolatilityMap.checkBlockVolatility(world.getBlock(x, y, z)))
            frontier.add(new ChunkCoordinates(x, y, z));
        if (!addToInitialFrontier(world, x, y, z, forcedDirection, frontier)) return false;
        for (int limit = 100000; limit > 0; limit--) {
            if (frontier.isEmpty()) return true;
            if (!moveBlock(world, forcedDirection, frontier, visited)) return false;
        }
        throw AssemblyException.structureTooLarge();
    }

    public void preInit() {
        // Create subcontraptions
        for (BlockFace blockFace : pendingSubContraptions) {
            Direction face = blockFace.getFace();
            StabilizedContraption subContraption = new StabilizedContraption(face);
            ChunkCoordinates pos = blockFace.getPos();
            try {
                if (!subContraption.assemble(contraptionWorld, pos.posX, pos.posY, pos.posZ)) continue;
            } catch (AssemblyException e) {
                continue;
            }
            subContraption.removeBlocksFromWorld(contraptionWorld);
            subContraption.preInit();
            stabilizedSubContraptions.put(subContraption, new BlockFace(toLocalPos(pos), face));
        }

        // TODO
        // // Gather itemhandlers of mounted storage
        // List<IInventory> list = storage.values()
        // .stream()
        // .map(MountedStorage::getItemHandler)
        // .collect(Collectors.toList());
        // inventory =
        // new ContraptionInvWrapper(Arrays.copyOf(list.toArray(), list.size(), IInventory[].class));
        //
        // List<IFluidHandler> fluidHandlers = fluidStorage.values()
        // .stream()
        // .map(MountedFluidStorage::getFluidHandler)
        // .collect(Collectors.toList());
        // fluidInventory = new CombinedTankWrapper(
        // Arrays.copyOf(fluidHandlers.toArray(), fluidHandlers.size(), IFluidHandler[].class));
    }

    public void onRemoved() {
        if (simplifiedEntityColliderProvider != null) {
            simplifiedEntityColliderProvider.cancel(false);
            simplifiedEntityColliderProvider = null;
        }
    }

    public void init() {
        if (anchorWorld.isRemote) return;

        // TODO
        // for (ChunkCoordinates seatPos : getSeats()) {
        // Entity passenger = initialPassengers.get(seatPos);
        // if (passenger == null)
        // continue;
        // int seatIndex = getSeats().indexOf(seatPos);
        // if (seatIndex == -1)
        // continue;
        // contraptionEntity.addSittingPassenger(passenger, seatIndex);
        // }
    }

    public void tick() {
        if (anchorWorld == null) {
            anchorWorld = ((IMixinWorld) (this.contraptionWorld.isRemote ? Minecraft.getMinecraft().theWorld
                : DimensionManager.getWorld(0))).getSubWorld(anchorWorldID);
            // if (this instanceof BearingContraption) {
            // ChunkCoordinates normal = ((BearingContraption) this).getFacing()
            // .getOpposite()
            // .getNormal();
            // ReCreate.LOGGER.info("Looking for tile entity on coordinates: " + (anchorX + normal.posX) + ", " +
            // (anchorY + normal.posY) + ", " + (anchorZ + normal.posZ));
            // TileEntity anchorTE = anchorWorld
            // .getTileEntity(anchorX + normal.posX, anchorY + normal.posY, anchorZ + normal.posZ);
            // ReCreate.LOGGER.info(anchorTE == null? "Found none" : "Tile entity found");
            // if (anchorTE instanceof MechanicalBearingTileEntity)
            // ((MechanicalBearingTileEntity) anchorTE).attach(this);
            // }
        }
        if (stabilizedSubContraptionsIDs.size() > stabilizedSubContraptions.size()) {
            for (Map.Entry<Integer, BlockFace> entry : stabilizedSubContraptionsIDs.entrySet()) {
                SubWorld subWorld = (SubWorld) ((IMixinWorld) DimensionManager.getWorld(0)).getSubWorld(entry.getKey());
                if (!(subWorld instanceof ContraptionWorld)) continue;
                ContraptionWorld contraptionWorld = (ContraptionWorld) subWorld;
                if (stabilizedSubContraptions.containsKey(contraptionWorld.getContraption())) continue;
                stabilizedSubContraptions.put(contraptionWorld.getContraption(), entry.getValue());
            }
        }
        if (anchorWorld instanceof SubWorld && !anchorWorld.isRemote) {
            SubWorld subworld = (SubWorld) anchorWorld;
            if (subworld.getMotionX() != 0 || subworld.getMotionY() != 0
                || subworld.getMotionZ() != 0
                || subworld.getRotationPitchSpeed() != 0
                || subworld.getRotationYawSpeed() != 0
                || subworld.getRotationRollSpeed() != 0) {
                Vec3 anchor = Vec3.createVectorHelper(anchorX + 0.5d, anchorY + 0.5d, anchorZ + 0.5d);
                Vec3 globalAnchor = subworld.transformToGlobal(anchor);
                Vec3 translation = anchor.subtract(globalAnchor);
                SubWorld contraptionSubworld = (SubWorld) contraptionWorld;
                contraptionSubworld.setTranslation(translation);
                contraptionSubworld.setRotationPitch(subworld.getRotationPitch());
                contraptionSubworld.setRotationYaw(subworld.getRotationYaw());
                contraptionSubworld.setRotationRoll(subworld.getRotationRoll());
            }
        }
        // TODO Most likely not needed
        // fluidStorage.forEach((pos, mfs) -> mfs.tick(entity, pos, world.isRemote));
    }

    /** move the first block in frontier queue */
    protected boolean moveBlock(World world, @Nullable Direction forcedDirection, Queue<ChunkCoordinates> frontier,
        Set<ChunkCoordinates> visited) throws AssemblyException {
        ChunkCoordinates pos = frontier.poll();
        if (pos == null) return false;
        visited.add(pos);
        int posX = pos.posX;
        int posY = pos.posY;
        int posZ = pos.posZ;

        if (posX >= world.getHeight()) return true;
        if (!world.blockExists(posX, posY, posZ)) throw AssemblyException.unloadedChunk(pos);
        if (isAnchoringBlockAt(posX, posY, posZ)) return true;
        Block block = world.getBlock(posX, posY, posZ);
        int meta = world.getBlockMetadata(posX, posY, posZ);
        if (!BlockMovementChecks.isMovementNecessary(block, meta, world, posX, posY, posZ)) return true;
        if (!movementAllowed(block, meta, world, posX, posY, posZ))
            throw AssemblyException.unmovableBlock(posX, posY, posZ, block, meta);
        // TODO
        // if (block instanceof AbstractChassisBlock
        // && !moveChassis(world, posX, posY, posZ, forcedDirection, frontier, visited))
        // return false;
        //
        // if (AllBlocks.BELT.has(state))
        // moveBelt(posX, posY, posZ, frontier, visited, state);
        //
        // if (AllBlocks.GANTRY_CARRIAGE.has(state))
        // moveGantryPinion(world, posX, posY, posZ, frontier, visited, state);
        //
        // if (AllBlocks.GANTRY_SHAFT.has(state))
        // moveGantryShaft(world, posX, posY, posZ, frontier, visited, state);
        //
        // if (AllBlocks.STICKER.has(state) && state.getValue(StickerBlock.EXTENDED)) {
        // Direction offset = state.getValue(StickerBlock.FACING);
        // BlockPos attached = posX, posY, posZ.relative(offset);
        // if (!visited.contains(attached)
        // && !BlockMovementChecks.isNotSupportive(world.getBlockState(attached), offset.getOpposite()))
        // frontier.add(attached);
        // }

        // Double Chest halves stick together
        if (block instanceof BlockChest) {
            TileEntityChest te = (TileEntityChest) world.getTileEntity(posX, posY, posZ);
            if (te.func_145980_j() == 1) {
                ChunkCoordinates attached = pos;
                if (te.adjacentChestZNeg != null) attached.posZ--;
                if (te.adjacentChestZPos != null) attached.posZ++;
                if (te.adjacentChestXNeg != null) attached.posX--;
                if (te.adjacentChestXPos != null) attached.posX++;
                if (!visited.contains(attached)) frontier.add(attached);
            }
        }

        // Bearings potentially create stabilized sub-contraptions
        if (block == AllBlocks.mechanical_bearing) moveBearing(posX, posY, posZ, frontier, visited, block, meta);

        // TODO
        // // WM Bearings attach their structure when moved
        // if (AllBlocks.WINDMILL_BEARING.has(state)) moveWindmillBearing(posX, posY, posZ, frontier, visited, state);
        //
        // // Seats transfer their passenger to the contraption
        // if (block instanceof SeatBlock) moveSeat(world, posX, posY, posZ);
        //
        // // Pulleys drag their rope and their attached structure
        // if (block instanceof PulleyBlock) movePulley(world, posX, posY, posZ, frontier, visited);
        //
        // // Pistons drag their attaches poles and extension
        // if (block instanceof MechanicalPistonBlock)
        // if (!moveMechanicalPiston(world, posX, posY, posZ, frontier, visited, state))
        // return false;
        // if (isExtensionPole(state))
        // movePistonPole(world, posX, posY, posZ, frontier, visited, state);
        // if (isPistonHead(state))
        // movePistonHead(world, posX, posY, posZ, frontier, visited, state);
        //
        // // Cart assemblers attach themselves
        // ChunkCoordinates posDown = pos.below();
        // BlockState stateBelow = world.getBlockState(posDown);
        // if (!visited.contains(posDown) && AllBlocks.CART_ASSEMBLER.has(stateBelow))
        // frontier.add(posDown);

        Map<Direction, SuperGlueEntity> superglue = SuperGlueHandler.gatherGlue(world, posX, posY, posZ);

        // Slime blocks and super glue drag adjacent blocks if possible
        for (Direction offset : Iterate.directions) {
            ChunkCoordinates normal = offset.getNormal();
            int offsetX = posX + normal.posX;
            int offsetY = posY + normal.posY;
            int offsetZ = posZ + normal.posZ;
            Block block1 = world.getBlock(offsetX, offsetY, offsetZ);
            int meta1 = world.getBlockMetadata(offsetX, offsetY, offsetZ);
            if (isAnchoringBlockAt(offsetX, offsetY, offsetZ)) continue;
            if (!movementAllowed(block1, meta1, world, offsetX, offsetY, offsetZ)) {
                if (offset == forcedDirection) throw AssemblyException.unmovableBlock(posX, posY, posZ, block, meta);
                continue;
            }

            ChunkCoordinates offsetPos = new ChunkCoordinates(offsetX, offsetY, offsetZ);
            boolean wasVisited = visited.contains(offsetPos);
            boolean faceHasGlue = superglue.containsKey(offset);
            boolean blockAttachedTowardsFace = BlockMovementChecks
                .isBlockAttachedTowards(block1, meta1, world, offsetX, offsetY, offsetZ, offset.getOpposite());
            // TODO Only applicable for slime blocks
            // boolean brittle = BlockMovementChecks.isBrittle(block1, meta1);
            // boolean canStick = !brittle && state.canStickTo(block1) && block1.canStickTo(state);
            // if (canStick) {
            // if (state.getPistonPushReaction() == PushReaction.PUSH_ONLY
            // || block1.getPistonPushReaction() == PushReaction.PUSH_ONLY) {
            // canStick = false;
            // }
            // if (BlockMovementChecks.isNotSupportive(state, offset)) {
            // canStick = false;
            // }
            // if (BlockMovementChecks.isNotSupportive(block1, offset.getOpposite())) {
            // canStick = false;
            // }
            // }

            if (!wasVisited && (/* TODO canStick || */blockAttachedTowardsFace || faceHasGlue
                || (offset == forcedDirection && !BlockMovementChecks.isNotSupportive(block, meta, forcedDirection))))
                frontier.add(offsetPos);
            if (faceHasGlue) addGlue(superglue.get(offset));
        }

        addBlock(world, posX, posY, posZ);
        if (blocks.size() <= AllConfigs.SERVER.kinetics.maxBlocksMoved) return true;
        else throw AssemblyException.structureTooLarge();
    }

    // TODO
    // protected void movePistonHead(World world, int x, int y, int z, Queue<ChunkCoordinates> frontier,
    // Set<ChunkCoordinates> visited,
    // Block block, int meta) {
    // Direction direction = state.getValue(MechanicalPistonHeadBlock.FACING);
    // BlockPos offset = pos.relative(direction.getOpposite());
    // if (!visited.contains(offset)) {
    // BlockState blockState = world.getBlockState(offset);
    // if (isExtensionPole(blockState) && blockState.getValue(PistonExtensionPoleBlock.FACING)
    // .getAxis() == direction.getAxis())
    // frontier.add(offset);
    // if (blockState.getBlock() instanceof MechanicalPistonBlock) {
    // Direction pistonFacing = blockState.getValue(MechanicalPistonBlock.FACING);
    // if (pistonFacing == direction
    // && blockState.getValue(MechanicalPistonBlock.STATE) == PistonState.EXTENDED)
    // frontier.add(offset);
    // }
    // }
    // if (state.getValue(MechanicalPistonHeadBlock.TYPE) == PistonType.STICKY) {
    // BlockPos attached = pos.relative(direction);
    // if (!visited.contains(attached))
    // frontier.add(attached);
    // }
    // }
    //
    // protected void movePistonPole(World world, int x, int y, int z, Queue<ChunkCoordinates> frontier,
    // Set<ChunkCoordinates> visited,
    // Block block, int meta) {
    // for (Direction d : Iterate.directionsInAxis(state.getValue(PistonExtensionPoleBlock.FACING)
    // .getAxis())) {
    // BlockPos offset = pos.relative(d);
    // if (!visited.contains(offset)) {
    // BlockState blockState = world.getBlockState(offset);
    // if (isExtensionPole(blockState) && blockState.getValue(PistonExtensionPoleBlock.FACING)
    // .getAxis() == d.getAxis())
    // frontier.add(offset);
    // if (isPistonHead(blockState) && blockState.getValue(MechanicalPistonHeadBlock.FACING)
    // .getAxis() == d.getAxis())
    // frontier.add(offset);
    // if (blockState.getBlock() instanceof MechanicalPistonBlock) {
    // Direction pistonFacing = blockState.getValue(MechanicalPistonBlock.FACING);
    // if (pistonFacing == d || pistonFacing == d.getOpposite()
    // && blockState.getValue(MechanicalPistonBlock.STATE) == PistonState.EXTENDED)
    // frontier.add(offset);
    // }
    // }
    // }
    // }
    //
    // protected void moveGantryPinion(World world, int x, int y, int z, Queue<ChunkCoordinates> frontier,
    // Set<ChunkCoordinates> visited,
    // Block block, int meta) {
    // BlockPos offset = pos.relative(state.getValue(GantryCarriageBlock.FACING));
    // if (!visited.contains(offset))
    // frontier.add(offset);
    // Axis rotationAxis = ((IRotate) block).getRotationAxis(state);
    // for (Direction d : Iterate.directionsInAxis(rotationAxis)) {
    // offset = pos.relative(d);
    // BlockState offsetState = world.getBlockState(offset);
    // if (AllBlocks.GANTRY_SHAFT.has(offsetState) && offsetState.getValue(GantryShaftBlock.FACING)
    // .getAxis() == d.getAxis())
    // if (!visited.contains(offset))
    // frontier.add(offset);
    // }
    // }
    //
    // protected void moveGantryShaft(World world, int x, int y, int z, Queue<ChunkCoordinates> frontier,
    // Set<ChunkCoordinates> visited,
    // Block block, int meta) {
    // for (Direction d : Iterate.directions) {
    // BlockPos offset = pos.relative(d);
    // if (!visited.contains(offset)) {
    // BlockState offsetState = world.getBlockState(offset);
    // Direction facing = state.getValue(GantryShaftBlock.FACING);
    // if (d.getAxis() == facing.getAxis() && AllBlocks.GANTRY_SHAFT.has(offsetState)
    // && offsetState.getValue(GantryShaftBlock.FACING) == facing)
    // frontier.add(offset);
    // else if (AllBlocks.GANTRY_CARRIAGE.has(offsetState)
    // && offsetState.getValue(GantryCarriageBlock.FACING) == d)
    // frontier.add(offset);
    // }
    // }
    // }
    //
    // private void moveWindmillBearing(int x, int y, int z, Queue<ChunkCoordinates> frontier,
    // Set<ChunkCoordinates> visited, Block block, int meta) {
    // Direction facing = state.getValue(WindmillBearingBlock.FACING);
    // BlockPos offset = pos.relative(facing);
    // if (!visited.contains(offset))
    // frontier.add(offset);
    // }

    private void moveBearing(int x, int y, int z, Queue<ChunkCoordinates> frontier, Set<ChunkCoordinates> visited,
        Block block, int meta) {
        Direction facing = ((MechanicalBearingBlock) block).getDirection(meta);
        if (!canBeStabilized(facing, x - anchorX, y - anchorY, z - anchorZ)) {
            ChunkCoordinates normal = facing.getNormal();
            ChunkCoordinates offset = new ChunkCoordinates(x + normal.posX, y + normal.posY, z + normal.posZ);
            if (!visited.contains(offset)) frontier.add(offset);
            return;
        }
        pendingSubContraptions.add(new BlockFace(x, y, z, facing));
    }
    // TODO
    // private void moveBelt(int x, int y, int z, Queue<ChunkCoordinates> frontier, Set<ChunkCoordinates> visited,
    // Block block, int meta) {
    // BlockPos nextPos = BeltBlock.nextSegmentPosition(state, pos, true);
    // BlockPos prevPos = BeltBlock.nextSegmentPosition(state, pos, false);
    // if (nextPos != null && !visited.contains(nextPos))
    // frontier.add(nextPos);
    // if (prevPos != null && !visited.contains(prevPos))
    // frontier.add(prevPos);
    // }
    //
    // private void moveSeat(World world, int x, int y, int z) {
    // BlockPos local = toLocalPos(pos);
    // getSeats().add(local);
    // List<SeatEntity> seatsEntities = world.getEntitiesOfClass(SeatEntity.class, new AxisAlignedBB(pos));
    // if (!seatsEntities.isEmpty()) {
    // SeatEntity seat = seatsEntities.get(0);
    // List<Entity> passengers = seat.getPassengers();
    // if (!passengers.isEmpty())
    // initialPassengeputTag(local, passengers.get(0));
    // }
    // }
    //
    // private void movePulley(World world, int x, int y, int z, Queue<ChunkCoordinates> frontier,
    // Set<ChunkCoordinates> visited) {
    // int limit = AllConfigs.SERVER.kinetics.maxRopeLength.get();
    // BlockPos ropePos = pos;
    // while (limit-- >= 0) {
    // ropePos = ropePos.below();
    // if (!world.isLoaded(ropePos))
    // break;
    // BlockState ropeState = world.getBlockState(ropePos);
    // Block block = ropeState.getBlock();
    // if (!(block instanceof RopeBlock) && !(block instanceof MagnetBlock)) {
    // if (!visited.contains(ropePos))
    // frontier.add(ropePos);
    // break;
    // }
    // addBlock(ropePos, capture(world, ropePos));
    // }
    // }
    //
    // private boolean moveMechanicalPiston(World world, int x, int y, int z, Queue<ChunkCoordinates> frontier,
    // Set<ChunkCoordinates> visited,
    // Block block, int meta) throws AssemblyException {
    // Direction direction = state.getValue(MechanicalPistonBlock.FACING);
    // PistonState pistonState = state.getValue(MechanicalPistonBlock.STATE);
    // if (pistonState == PistonState.MOVING)
    // return false;
    //
    // BlockPos offset = pos.relative(direction.getOpposite());
    // if (!visited.contains(offset)) {
    // BlockState poleState = world.getBlockState(offset);
    // if (AllBlocks.PISTON_EXTENSION_POLE.has(poleState) && poleState.getValue(PistonExtensionPoleBlock.FACING)
    // .getAxis() == direction.getAxis())
    // frontier.add(offset);
    // }
    //
    // if (pistonState == PistonState.EXTENDED || MechanicalPistonBlock.isStickyPiston(state)) {
    // offset = pos.relative(direction);
    // if (!visited.contains(offset))
    // frontier.add(offset);
    // }
    //
    // return true;
    // }
    //
    // private boolean moveChassis(World world, int x, int y, int z, Direction movementDirection,
    // Queue<ChunkCoordinates> frontier,
    // Set<ChunkCoordinates> visited) {
    // TileEntity te = world.getTileEntity(pos);
    // if (!(te instanceof ChassisTileEntity))
    // return false;
    // ChassisTileEntity chassis = (ChassisTileEntity) te;
    // chassis.addAttachedChasses(frontier, visited);
    // List<ChunkCoordinates> includedBlockPositions = chassis.getIncludedBlockPositions(movementDirection, false);
    // if (includedBlockPositions == null)
    // return false;
    // for (ChunkCoordinates pos : includedBlockPositions)
    // if (!visited.contains(pos))
    // frontier.add(pos);
    // return true;
    // }

    protected void addBlock(World world, int x, int y, int z) {
        ChunkCoordinates localPos = new ChunkCoordinates(x, y, z);
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);

        if (!blocks.add(localPos)) return;

        TileEntity te = world.getTileEntity(x, y, z);
        // TODO
        // if (te != null && MountedStorage.canUseAsStorage(te))
        // storage.put(localPos, new MountedStorage(te));
        // if (te != null && MountedFluidStorage.canUseAsStorage(te))
        // fluidStorage.put(localPos, new MountedFluidStorage(te));
        // if (AllMovementBehaviours.contains(captured.state.getBlock()))
        // actors.add(MutablePair.of(StructureBlockInfo, null));
        // if (AllInteractionBehaviours.contains(captured.state.getBlock()))
        // interactors.put(localPos, AllInteractionBehaviours.of(captured.state.getBlock()));
        // if (te instanceof CreativeCrateTileEntity
        // && ((CreativeCrateTileEntity) te).getBehaviour(FilteringBehaviour.TYPE)
        // .getFilter()
        // .isEmpty())
        // hasUniversalCreativeCrate = true;
    }

    // TODO
    // @Nullable
    // protected NBTTagCompound getTileEntityNBT(World world, int x, int y, int z) {
    // TileEntity tileentity = world.getTileEntity(x, y, z);
    // if (tileentity == null)
    // return null;
    // NBTTagCompound nbt = tileentity.save(new NBTTagCompound());
    // nbt.remove("x");
    // nbt.remove("y");
    // nbt.remove("z");
    //
    // if ((tileentity instanceof FluidTankTileEntity || tileentity instanceof ItemVaultTileEntity)
    // && nbt.contains("Controller"))
    // nbt.setTag("Controller",
    // NBTHelper.writeChunkCoordinates(toLocalPos(NBTHelper.readChunkCoordinates(nbt.getCompoundTag("Controller")))));
    //
    // return nbt;
    // }

    protected void addGlue(SuperGlueEntity entity) {
        ChunkCoordinates pos = new ChunkCoordinates(
            entity.getHangingPositionX(),
            entity.getHangingPositionY(),
            entity.getHangingPositionZ());
        Direction direction = entity.getFacingDirection();
        this.superglue.add(Pair.of(pos, direction));
        glueToRemove.add(entity);
    }

    protected ChunkCoordinates toLocalPos(ChunkCoordinates globalPos) {
        return toLocalPos(globalPos.posX, globalPos.posY, globalPos.posZ);
    }

    protected ChunkCoordinates toLocalPos(int x, int y, int z) {
        Vec3 localPos = this.getContraptionWorld()
            .transformToLocal(x + 0.5, y + 0.5, z + 0.5);
        return new ChunkCoordinates(
            (int) Math.floor(localPos.xCoord),
            (int) Math.floor(localPos.yCoord),
            (int) Math.floor(localPos.zCoord));
    }

    protected ChunkCoordinates toGlobalPos(ChunkCoordinates localPos) {
        return toGlobalPos(localPos.posX, localPos.posY, localPos.posZ);
    }

    protected ChunkCoordinates toGlobalPos(int x, int y, int z) {
        Vec3 globalPos = this.getContraptionWorld()
            .transformToGlobal(x + 0.5, y + 0.5, z + 0.5);
        return new ChunkCoordinates(
            (int) Math.floor(globalPos.xCoord),
            (int) Math.floor(globalPos.yCoord),
            (int) Math.floor(globalPos.zCoord));
    }

    protected boolean movementAllowed(Block block, int meta, World world, int x, int y, int z) {
        return BlockMovementChecks.isMovementAllowed(block, meta, world, x, y, z);
    }

    protected boolean isAnchoringBlockAt(int x, int y, int z) {
        return anchorX == x && anchorY == y && anchorZ == z;
    }

    public void readNBT(NBTTagCompound nbt, boolean spawnData) {
        blocks.clear();
        presentTileEntities.clear();
        specialRenderedTileEntities.clear();

        NBTTagCompound blocks = nbt.getCompoundTag("Blocks");
        readBlocksCompound(blocks);

        superglue.clear();
        NBTHelper.iterateCompoundList(
            nbt.getTagList("Superglue", 10),
            c -> superglue
                .add(Pair.of(NBTHelper.readChunkCoordinates(c), Direction.from3DDataValue(c.getByte("Direction")))));

        seats.clear();
        NBTHelper.iterateCompoundList(nbt.getTagList("Seats", 10), c -> seats.add(NBTHelper.readChunkCoordinates(c)));

        // TODO
        // seatMapping.clear();
        // NBTHelper.iterateCompoundList(nbt.getTagList("Passengers", 10),
        // c -> seatMapping.put(NbtUtils.loadUUID(NBTHelper.getINBT(c, "Id")), c.getInteger("Seat")));

        stabilizedSubContraptions.clear();
        stabilizedSubContraptionsIDs.clear();
        NBTHelper.iterateCompoundList(
            nbt.getTagList("SubContraptions", 10),
            c -> stabilizedSubContraptionsIDs.put(c.getInteger("Id"), BlockFace.fromNBT(c.getCompoundTag("Location"))));

        // TODO
        // storage.clear();
        // NBTHelper.iterateCompoundList(nbt.getTagList("Storage", 10), c -> storage
        // .put(NBTHelper.readChunkCoordinates(c.getCompoundTag("Pos")),
        // MountedStorage.deserialize(c.getCompoundTag("Data"))));

        // TODO
        // fluidStorage.clear();
        // NBTHelper.iterateCompoundList(nbt.getTagList("FluidStorage", 10), c -> fluidStorage
        // .put(NBTHelper.readChunkCoordinates(c.getCompoundTag("Pos")),
        // MountedFluidStorage.deserialize(c.getCompoundTag("Data"))));

        // TODO
        // if (spawnData)
        // fluidStorage.forEach((pos, mfs) -> {
        // TileEntity tileEntity = presentTileEntities.get(pos);
        // if (!(tileEntity instanceof FluidTankTileEntity))
        // return;
        // FluidTankTileEntity tank = (FluidTankTileEntity) tileEntity;
        // IFluidTank tankInventory = tank.getTankInventory();
        // if (tankInventory instanceof FluidTank)
        // ((FluidTank) tankInventory).setFluid(mfs.tank.getFluid());
        // tank.getFluidLevel()
        // .start(tank.getFillState());
        // mfs.assignTileEntity(tank);
        // });

        // TODO
        // IInventory[] handlers = new IInventory[storage.size()];
        // int index = 0;
        // for (MountedStorage mountedStorage : storage.values())
        // handlers[index++] = mountedStorage.getItemHandler();
        //
        // IFluidHandler[] fluidHandlers = new IFluidHandler[fluidStorage.size()];
        // index = 0;
        // for (MountedFluidStorage mountedStorage : fluidStorage.values())
        // fluidHandlers[index++] = mountedStorage.getFluidHandler();
        //
        // inventory = new ContraptionInvWrapper(handlers);
        // fluidInventory = new CombinedTankWrapper(fluidHandlers);

        stalled = nbt.getBoolean("Stalled");
        hasUniversalCreativeCrate = nbt.getBoolean("BottomlessSupply");
        int[] anchor = nbt.getIntArray("Anchor");
        anchorX = anchor[0];
        anchorY = anchor[1];
        anchorZ = anchor[2];
        if (nbt.hasKey("AnchorWorld")) anchorWorldID = nbt.getInteger("AnchorWorld");
    }

    public NBTTagCompound writeNBT(boolean spawnPacket) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("Type", getType().id);

        NBTTagCompound blocksNBT = writeBlocksCompound();

        // TODO
        // NBTTagList actorsNBT = new NBTTagList();
        // for (MutablePair<StructureBlockInfo, MovementContext> actor : getActors()) {
        // NBTTagCompound compound = new NBTTagCompound();
        // compound.setTag("Pos", NBTHelper.writeChunkCoordinates(actor.left.pos));
        // AllMovementBehaviours.of(actor.left.state)
        // .writeExtraData(actor.right);
        // actor.right.writeToNBT(compound);
        // actorsNBT.appendTag(compound);
        // }

        NBTTagList superglueNBT = new NBTTagList();
        if (!spawnPacket) {
            for (Pair<ChunkCoordinates, Direction> glueEntry : superglue) {
                NBTTagCompound c = NBTHelper.writeChunkCoordinates(glueEntry.getKey());
                c.setByte(
                    "Direction",
                    (byte) glueEntry.getValue()
                        .get3DDataValue());
                superglueNBT.appendTag(c);
            }

            // TODO
            // NBTTagList storageNBT = new NBTTagList();
            // for (ChunkCoordinates pos : storage.keySet()) {
            // NBTTagCompound c = new NBTTagCompound();
            // MountedStorage mountedStorage = storage.get(pos);
            // if (!mountedStorage.isValid())
            // continue;
            // c.setTag("Pos", NBTHelper.writeChunkCoordinates(pos));
            // c.setTag("Data", mountedStorage.serialize());
            // storageNBT.appendTag(c);
            // }
        }

        // TODO
        // NBTTagList fluidStorageNBT = new NBTTagList();
        // for (ChunkCoordinates pos : fluidStorage.keySet()) {
        // NBTTagCompound c = new NBTTagCompound();
        // MountedFluidStorage mountedStorage = fluidStorage.get(pos);
        // if (!mountedStorage.isValid())
        // continue;
        // c.setTag("Pos", NBTHelper.writeChunkCoordinates(pos));
        // c.setTag("Data", mountedStorage.serialize());
        // fluidStorageNBT.appendTag(c);
        // }

        // TODO
        // NBTTagList interactorNBT = new NBTTagList();
        // for (ChunkCoordinates pos : interactors.keySet()) {
        // NBTTagCompound c = new NBTTagCompound();
        // c.setTag("Pos", NBTHelper.writeChunkCoordinates(pos));
        // interactorNBT.appendTag(c);
        // }

        // TODO
        // nbt.setTag("Seats", NBTHelper.writeCompoundList(getSeats(), NBTHelper::writeChunkCoordinates));
        // nbt.setTag("Passengers", NBTHelper.writeCompoundList(getSeatMapping().entrySet(), e -> {
        // NBTTagCompound tag = new NBTTagCompound();
        // tag.setTag("Id", NbtUtils.createUUID(e.getKey()));
        // tag.setInteger("Seat", e.getValue());
        // return tag;
        // }));

        nbt.setTag("SubContraptions", NBTHelper.writeCompoundList(stabilizedSubContraptions.entrySet(), e -> {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("Id", ((SubWorld) e.getKey().contraptionWorld).getSubWorldID());
            tag.setTag(
                "Location",
                e.getValue()
                    .serializeNBT());
            return tag;
        }));

        nbt.setTag("Blocks", blocksNBT);
        // TODO
        // nbt.setTag("Actors", actorsNBT);
        // nbt.setTag("Interactors", interactorNBT);
        nbt.setTag("Superglue", superglueNBT);
        // TODO
        // nbt.setTag("Storage", storageNBT);
        // nbt.setTag("FluidStorage", fluidStorageNBT);
        nbt.setIntArray("Anchor", new int[] { anchorX, anchorY, anchorZ });
        if (anchorWorld != null) nbt.setInteger("AnchorWorld", ((IMixinWorld) anchorWorld).getSubWorldID());
        nbt.setBoolean("Stalled", stalled);
        nbt.setBoolean("BottomlessSupply", hasUniversalCreativeCrate);

        return nbt;
    }

    private NBTTagCompound writeBlocksCompound() {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList blockList = new NBTTagList();

        for (ChunkCoordinates pos : this.blocks) {
            NBTTagCompound c = NBTHelper.writeChunkCoordinates(pos);
            blockList.appendTag(c);
        }
        compound.setTag("BlockList", blockList);
        return compound;
    }

    private void readBlocksCompound(NBTTagCompound compound) {
        NBTTagList blockList = compound.getTagList("BlockList", 10);

        for (int i = 0; i < blockList.tagCount(); i++) {
            NBTTagCompound c = blockList.getCompoundTagAt(i);
            blocks.add(NBTHelper.readChunkCoordinates(c));
        }
    }

    public void removeBlocksFromWorld(World world) {
        removeBlocksFromWorld(world, 0, 0, 0);
    }

    @SuppressWarnings("unchecked")
    public void removeBlocksFromWorld(World parentWorld, int offsetX, int offsetY, int offsetZ) {
        offsetX += anchorX;
        offsetY += anchorY;
        offsetZ += anchorZ;
        glueToRemove.forEach(SuperGlueEntity::setDead);
        ContraptionWorld contraption = this.getContraptionWorld();

        Block block;
        int meta;
        NBTTagCompound nbttag;
        TileEntity oldTE;
        TileEntity newTE;
        Entity oldEntity;
        Entity newEntity;

        ArrayList<ChunkCoordinates> blocksToTake = new ArrayList<ChunkCoordinates>();
        ArrayList<ChunkCoordinates> blocksToTakeSolidBrittle = new ArrayList<ChunkCoordinates>();
        ArrayList<ChunkCoordinates> blocksToTakeBrittle = new ArrayList<ChunkCoordinates>();

        for (ChunkCoordinates curCoord : blocks) {
            block = parentWorld.getBlock(curCoord.posX, curCoord.posY, curCoord.posZ);
            if (BlockVolatilityMap.checkBlockVolatility(block)) {
                if (BlockVolatilityMap.isBlockSolid(block, parentWorld, curCoord.posX, curCoord.posY, curCoord.posZ)) {
                    blocksToTakeSolidBrittle.add(curCoord);
                } else {
                    blocksToTakeBrittle.add(curCoord);
                }
            } else {
                blocksToTake.add(curCoord);
            }
        }

        List<List<ChunkCoordinates>> listsToParse = new ArrayList<List<ChunkCoordinates>>();
        listsToParse.add(blocksToTake);
        listsToParse.add(blocksToTakeSolidBrittle);
        listsToParse.add(blocksToTakeBrittle);
        for (List<ChunkCoordinates> currentList : listsToParse) {
            for (ChunkCoordinates curCoord : currentList) {
                block = parentWorld.getBlock(curCoord.posX, curCoord.posY, curCoord.posZ);
                meta = parentWorld.getBlockMetadata(curCoord.posX, curCoord.posY, curCoord.posZ);
                contraptionWorld.setBlock(curCoord.posX, curCoord.posY, curCoord.posZ, block, meta, 0);
                contraptionWorld.setBlockMetadataWithNotify(curCoord.posX, curCoord.posY, curCoord.posZ, meta, 0);
                if (block.hasTileEntity(meta)) {
                    oldTE = parentWorld.getTileEntity(curCoord.posX, curCoord.posY, curCoord.posZ);
                    nbttag = new NBTTagCompound();
                    oldTE.writeToNBT(nbttag);
                    oldTE.invalidate();
                    newTE = TileEntity.createAndLoadEntity(nbttag);
                    contraptionWorld.setTileEntity(curCoord.posX, curCoord.posY, curCoord.posZ, newTE);
                }
                List<Entity> entities = parentWorld.getEntitiesWithinAABBExcludingEntity(
                    null,
                    AxisAlignedBB
                        .getBoundingBox(
                            curCoord.posX,
                            curCoord.posY,
                            curCoord.posZ,
                            curCoord.posX + 1,
                            curCoord.posY + 1,
                            curCoord.posZ + 1)
                        .expand(0.25, 0.25, 0.25));
                Iterator<Entity> j$ = entities.iterator();
                while (j$.hasNext()) {
                    oldEntity = j$.next();
                    if (oldEntity instanceof EntityMinecart || (oldEntity instanceof EntityHanging
                        && (((EntityHanging) oldEntity).field_146063_b == curCoord.posX
                            && ((EntityHanging) oldEntity).field_146064_c == curCoord.posY
                            && ((EntityHanging) oldEntity).field_146062_d == curCoord.posZ))) {
                        nbttag = new NBTTagCompound();
                        newEntity = EntityList
                            .createEntityByName(EntityList.getEntityString(oldEntity), contraptionWorld);
                        newEntity.copyDataFrom(oldEntity, true);
                        contraptionWorld.spawnEntityInWorld(newEntity);
                        oldEntity.setDead();
                    }
                }
            }
        }

        listsToParse.set(0, blocksToTakeBrittle);
        listsToParse.set(2, blocksToTake);
        for (List<ChunkCoordinates> currentList : listsToParse) {
            for (ChunkCoordinates curCoord : currentList) {
                block = contraptionWorld.getBlock(curCoord.posX, curCoord.posY, curCoord.posZ);
                meta = contraptionWorld.getBlockMetadata(curCoord.posX, curCoord.posY, curCoord.posZ);
                parentWorld.setBlockToAir(curCoord.posX, curCoord.posY, curCoord.posZ);
            }
        }

        contraption.setTranslation(
            ((IMixinWorld) parentWorld).getTranslationX(),
            ((IMixinWorld) parentWorld).getTranslationY(),
            ((IMixinWorld) parentWorld).getTranslationZ());
        contraption.setRotationYaw(((IMixinWorld) parentWorld).getRotationYaw());
        contraption.setRotationPitch(((IMixinWorld) parentWorld).getRotationPitch());
        contraption.setRotationRoll(((IMixinWorld) parentWorld).getRotationRoll());
        contraption.setScaling(((IMixinWorld) parentWorld).getScaling());
        contraption.setCenter((double) offsetX + 0.5D, (double) offsetY + 0.5D, (double) offsetZ + 0.5D);
    }

    @SuppressWarnings("unchecked")
    public void addBlocksToWorld(World world) {
        ContraptionWorld subWorldPar = (ContraptionWorld) contraptionWorld;
        double oldCenterX = subWorldPar.getCenterX();
        double oldCenterY = subWorldPar.getCenterY();
        double oldCenterZ = subWorldPar.getCenterZ();
        subWorldPar.setRotationYaw((double) Math.round(subWorldPar.getRotationYaw() / 90.0D) * 90.0D);
        subWorldPar.setRotationPitch(0.0D);
        subWorldPar.setRotationRoll(0.0D);
        subWorldPar.setTranslation(
            (double) Math.round(subWorldPar.getTranslationX()),
            (double) Math.round(subWorldPar.getTranslationY()),
            (double) Math.round(subWorldPar.getTranslationZ()));
        subWorldPar.setScaling(1.0D);
        subWorldPar.setCenter(0.0D, 0.0D, 0.0D);
        subWorldPar.setMotion(0.0D, 0.0D, 0.0D);
        subWorldPar.setRotationYawSpeed(0.0D);
        subWorldPar.setRotationPitchSpeed(0.0D);
        subWorldPar.setRotationRollSpeed(0.0D);
        subWorldPar.setScaleChangeRate(0.0D);

        for (Contraption subContraption : stabilizedSubContraptions.keySet()) subContraption.addBlocksToWorld(world);

        Block block;
        int oldMeta;
        int newMeta;
        NBTTagCompound nbttag;
        TileEntity oldTE;
        TileEntity newTE;
        Entity oldEntity;
        Entity newEntity;

        ArrayList<ChunkCoordinates> blocksToTake = new ArrayList<ChunkCoordinates>();
        ArrayList<ChunkCoordinates> blocksToTakeSolidBrittle = new ArrayList<ChunkCoordinates>();
        ArrayList<ChunkCoordinates> blocksToTakeBrittle = new ArrayList<ChunkCoordinates>();

        for (ChunkCoordinates curCoord : blocks) {
            block = contraptionWorld.getBlock(curCoord.posX, curCoord.posY, curCoord.posZ);
            if (BlockVolatilityMap.checkBlockVolatility(block)) {
                if (BlockVolatilityMap
                    .isBlockSolid(block, contraptionWorld, curCoord.posX, curCoord.posY, curCoord.posZ)) {
                    blocksToTakeSolidBrittle.add(curCoord);
                } else {
                    blocksToTakeBrittle.add(curCoord);
                }
            } else {
                blocksToTake.add(curCoord);
            }
        }

        List<List<ChunkCoordinates>> listsToParse = new ArrayList<List<ChunkCoordinates>>();
        listsToParse.add(blocksToTake);
        listsToParse.add(blocksToTakeSolidBrittle);
        listsToParse.add(blocksToTakeBrittle);
        for (List<ChunkCoordinates> currentList : listsToParse) {
            for (ChunkCoordinates curCoord : currentList) {
                block = contraptionWorld.getBlock(curCoord.posX, curCoord.posY, curCoord.posZ);
                oldMeta = contraptionWorld.getBlockMetadata(curCoord.posX, curCoord.posY, curCoord.posZ);
                newMeta = RotationHelper.getRotatedMeta(contraptionWorld, curCoord.posX, curCoord.posY, curCoord.posZ);
                ChunkCoordinates globalPos = this.getContraptionWorld()
                    .transformBlockToGlobal(curCoord.posX, curCoord.posY, curCoord.posZ);
                world.setBlock(globalPos.posX, globalPos.posY, globalPos.posZ, block, newMeta, 3);
                world.setBlockMetadataWithNotify(globalPos.posX, globalPos.posY, globalPos.posZ, newMeta, 3);
                if (block.hasTileEntity(oldMeta)) {
                    RotationHelper.rotateTileEntity(contraptionWorld, curCoord.posX, curCoord.posY, curCoord.posZ);
                    oldTE = contraptionWorld.getTileEntity(curCoord.posX, curCoord.posY, curCoord.posZ);
                    nbttag = new NBTTagCompound();
                    oldTE.writeToNBT(nbttag);
                    oldTE.invalidate();
                    newTE = TileEntity.createAndLoadEntity(nbttag);
                    if (newTE.blockMetadata != -1) newTE.blockMetadata = newMeta;
                    newTE.xCoord = globalPos.posX;
                    newTE.yCoord = globalPos.posY;
                    newTE.zCoord = globalPos.posZ;
                    newTE.setWorldObj(world);
                    world.setTileEntity(globalPos.posX, globalPos.posY, globalPos.posZ, newTE);
                }
            }
        }

        Iterator<Entity> iter = contraptionWorld.loadedEntityList.iterator();
        while (iter.hasNext()) {
            oldEntity = iter.next();
            if (oldEntity instanceof EntityPlayer) continue;
            nbttag = new NBTTagCompound();
            newEntity = EntityList.createEntityByName(EntityList.getEntityString(oldEntity), world);
            newEntity.copyDataFrom(oldEntity, true);
            Vec3 globalCoords = this.getContraptionWorld()
                .transformToGlobal(newEntity);
            newEntity.setLocationAndAngles(
                globalCoords.xCoord,
                globalCoords.yCoord,
                globalCoords.zCoord,
                newEntity.rotationYaw,
                newEntity.rotationPitch);
            RotationHelper.rotateEntity(world, newEntity);
            world.spawnEntityInWorld(newEntity);
            oldEntity.setDead();
        }

        listsToParse.set(0, blocksToTakeBrittle);
        listsToParse.set(2, blocksToTake);
        for (List<ChunkCoordinates> currentList : listsToParse) {
            for (ChunkCoordinates curCoord : currentList) {
                block = contraptionWorld.getBlock(curCoord.posX, curCoord.posY, curCoord.posZ);
                oldMeta = contraptionWorld.getBlockMetadata(curCoord.posX, curCoord.posY, curCoord.posZ);
                contraptionWorld.setBlockToAir(curCoord.posX, curCoord.posY, curCoord.posZ);
            }
        }

        subWorldPar.setCenter(oldCenterX, oldCenterY, oldCenterZ);
        if (subWorldPar instanceof SubWorldServer && ((SubWorldServer) subWorldPar).isEmpty()) {
            SubWorldServer subWorldServer = ((SubWorldServer) subWorldPar);
            ((IMixinWorld) subWorldServer.getParentWorld()).getSubWorldsMap()
                .remove(subWorldServer.getSubWorldID());
            subWorldServer.removeSubWorld();
            subWorldServer.flush();
            subWorldServer.deleteSubWorldData();
        }

        for (Pair<ChunkCoordinates, Direction> pair : superglue) {
            ChunkCoordinates pos = this.toGlobalPos(pair.getKey());
            Direction facing = RotationHelper.getRotatedDirection(contraptionWorld, pair.getValue());
            // TODO Apply transformations later
            // ChunkCoordinates targetPos = transform.apply(pair.getKey());
            // Direction targetFacing = transform.transformFacing(pair.getValue());

            SuperGlueEntity entity = new SuperGlueEntity(world, pos.posX, pos.posY, pos.posZ, facing);
            if (entity.onValidSurface()) {
                if (!world.isRemote) world.spawnEntityInWorld(entity);
            }
        }
    }

    // TODO
    // public void addPassengersToWorld(World world, StructureTransform transform, List<Entity> seatedEntities) {
    // for (Entity seatedEntity : seatedEntities) {
    // if (getSeatMapping().isEmpty())
    // continue;
    // Integer seatIndex = getSeatMapping().get(seatedEntity.getUniqueID());
    // BlockPos seatPos = getSeats().get(seatIndex);
    // seatPos = transform.apply(seatPos);
    // if (!(world.getBlockState(seatPos)
    // .getBlock() instanceof SeatBlock))
    // continue;
    // if (SeatBlock.isSeatOccupied(world, seatPos))
    // continue;
    // SeatBlock.sitDown(world, seatPos, seatedEntity);
    // }
    // }

    public void startMoving(World world) {
        // TODO
        // for (MutablePair<StructureBlockInfo, MovementContext> pair : actors) {
        // MovementContext context = new MovementContext(world, pair.left, this);
        // AllMovementBehaviours.of(pair.left.state)
        // .startMoving(context);
        // pair.setRight(context);
        // }
    }

    public void stop(World world) {
        // TODO
        // foreachActor(world, (behaviour, ctx) -> {
        // behaviour.stopMoving(ctx);
        // ctx.position = null;
        // ctx.motion = Vec3.ZERO;
        // ctx.relativeMotion = Vec3.ZERO;
        // ctx.rotation = v -> v;
        // });
    }

    // TODO
    // public void foreachActor(World world, BiConsumer<MovementBehaviour, MovementContext> callBack) {
    // for (MutablePair<StructureBlockInfo, MovementContext> pair : actors)
    // callBack.accept(AllMovementBehaviours.of(pair.getLeft().state), pair.getRight());
    // }

    // TODO
    // protected boolean shouldUpdateAfterMovement(StructureBlockInfo info) {
    // if (PoiType.forState(info.state)
    // .isPresent())
    // return false;
    // return true;
    // }

    public void addExtraInventories(Entity entity) {}

    public Map<UUID, Integer> getSeatMapping() {
        return seatMapping;
    }

    public ChunkCoordinates getSeatOf(UUID entityId) {
        if (!getSeatMapping().containsKey(entityId)) return null;
        int seatIndex = getSeatMapping().get(entityId);
        if (seatIndex >= getSeats().size()) return null;
        return getSeats().get(seatIndex);
    }

    public void setSeatMapping(Map<UUID, Integer> seatMapping) {
        this.seatMapping = seatMapping;
    }

    public List<ChunkCoordinates> getSeats() {
        return seats;
    }

    public List<ChunkCoordinates> getBlocks() {
        return blocks;
    }

    // TODO
    // public void updateContainedFluid(int localX, int localY, int localZ, FluidStack containedFluid) {
    // MountedFluidStorage mountedFluidStorage = fluidStorage.get(localPos);
    // if (mountedFluidStorage != null)
    // mountedFluidStorage.updateFluid(containedFluid);
    // }
}
