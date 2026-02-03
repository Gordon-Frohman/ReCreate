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
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.IFluidHandler;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.MetaworldsMod;
import su.sergiusonesimus.metaworlds.api.SubWorld;
import su.sergiusonesimus.metaworlds.util.BlockVolatilityMap;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.DisplacementHelper;
import su.sergiusonesimus.metaworlds.util.DisplacementHelper.CustomBlockDisplacement;
import su.sergiusonesimus.metaworlds.util.RotationHelper;
import su.sergiusonesimus.metaworlds.world.SubWorldServer;
import su.sergiusonesimus.metaworlds.zmixin.interfaces.minecraft.world.IMixinWorld;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.AllMovementBehaviours;
import su.sergiusonesimus.recreate.AllSounds;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.StabilizedContraption;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.WindmillBearingBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue.SuperGlueHandler;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonHeadBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import su.sergiusonesimus.recreate.foundation.config.AllConfigs;
import su.sergiusonesimus.recreate.foundation.fluid.CombinedTankWrapper;
import su.sergiusonesimus.recreate.foundation.networking.AllPackets;
import su.sergiusonesimus.recreate.foundation.utility.BlockFace;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
import su.sergiusonesimus.recreate.foundation.utility.NBTHelper;
import su.sergiusonesimus.recreate.foundation.utility.UniqueLinkedList;
import su.sergiusonesimus.recreate.util.VecHelper;
import su.sergiusonesimus.recreate.zmixin.interfaces.IMixinWorldReCreate;

public abstract class Contraption {

    public Optional<List<AxisAlignedBB>> simplifiedEntityColliders = Optional.empty();
    public ContraptionInvWrapper inventory;
    public CombinedTankWrapper fluidInventory;
    public Integer anchorX;
    public Integer anchorY;
    public Integer anchorZ;
    private int parentWorldID;
    public World parentWorld;
    public boolean stalled;
    public boolean hasUniversalCreativeCrate;

    protected List<ChunkCoordinates> blocks = new ArrayList<ChunkCoordinates>();
    protected Map<ChunkCoordinates, IInventory> storage = new HashMap<ChunkCoordinates, IInventory>();
    protected Map<ChunkCoordinates, IFluidHandler> fluidStorage = new HashMap<ChunkCoordinates, IFluidHandler>();
    protected List<MutablePair<ChunkCoordinates, MovementContext>> actors = new ArrayList<MutablePair<ChunkCoordinates, MovementContext>>();
    protected Set<Pair<ChunkCoordinates, Direction>> superglue = new HashSet<Pair<ChunkCoordinates, Direction>>();
    protected List<ChunkCoordinates> seats = new ArrayList<ChunkCoordinates>();
    protected Map<UUID, Integer> seatMapping = new HashMap<UUID, Integer>();
    private Map<Integer, BlockFace> stabilizedSubContraptionsIDs = new HashMap<Integer, BlockFace>();
    protected Map<Contraption, BlockFace> stabilizedSubContraptions = new HashMap<Contraption, BlockFace>();

    private List<SuperGlueEntity> glueToRemove = new ArrayList<SuperGlueEntity>();
    private Map<ChunkCoordinates, Entity> initialPassengers = new HashMap<ChunkCoordinates, Entity>();
    private List<BlockFace> pendingSubContraptions = new ArrayList<BlockFace>();

    private CompletableFuture<Void> simplifiedEntityColliderProvider;

    // Client
    public Map<ChunkCoordinates, TileEntity> presentTileEntities = new HashMap<ChunkCoordinates, TileEntity>();
    public List<TileEntity> maybeInstancedTileEntities = new ArrayList<TileEntity>();
    public List<TileEntity> specialRenderedTileEntities = new ArrayList<TileEntity>();

    protected World contraptionWorld;

    protected boolean initialized;
    public boolean ticking;
    public boolean beingRemoved = false;

    public Contraption() {}

    public Contraption(World parentWorld) {
        this.parentWorld = parentWorld;
    }

    public World getWorld() {
        if (parentWorld == null) return null;
        if (contraptionWorld == null)
            contraptionWorld = ((IMixinWorldReCreate) parentWorld).createContraptionWorld(this);
        return contraptionWorld;
    }

    public World getWorld(double centerX, double centerY, double centerZ, double translationX, double translationY,
        double translationZ, double rotationPitch, double rotationYaw, double rotationRoll, double scaling) {
        if (parentWorld == null) return null;
        if (contraptionWorld == null)
            contraptionWorld = ((IMixinWorldReCreate) ((IMixinWorld) parentWorld).getParentWorld())
                .createContraptionWorld(
                    this,
                    centerX,
                    centerY,
                    centerZ,
                    translationX,
                    translationY,
                    translationZ,
                    rotationPitch,
                    rotationYaw,
                    rotationRoll,
                    scaling);
        else {
            ContraptionWorld contraption = (ContraptionWorld) contraptionWorld;
            contraption.setCenter(centerX, centerY, centerZ);
            contraption.setTranslation(translationX, translationY, translationZ);
            contraption.setRotationYaw(rotationYaw);
            contraption.setRotationPitch(rotationPitch);
            contraption.setRotationRoll(rotationRoll);
            contraption.setScaling(scaling);
        }
        return contraptionWorld;
    }

    public ContraptionWorld getContraptionWorld() {
        return (ContraptionWorld) getWorld();
    }

    public ContraptionWorld getContraptionWorld(double centerX, double centerY, double centerZ, double translationX,
        double translationY, double translationZ, double rotationPitch, double rotationYaw, double rotationRoll,
        double scaling) {
        return (ContraptionWorld) getWorld(
            centerX,
            centerY,
            centerZ,
            translationX,
            translationY,
            translationZ,
            rotationPitch,
            rotationYaw,
            rotationRoll,
            scaling);
    }

    public boolean hasContraptionWorld() {
        return contraptionWorld != null;
    }

    public abstract String getSubWorldType();

    public abstract boolean assemble(World world, int x, int y, int z) throws AssemblyException;

    public void disassemble() {
        if (contraptionWorld == null || parentWorld == null) return;
        World mainWorld = ((IMixinWorld) parentWorld).getParentWorld();

        alignContraption();

        // We are disassembling subcontraptions first, since some of them might want to modify blocks from current
        // contraption
        for (Contraption subContraption : stabilizedSubContraptions.keySet()) {
            subContraption.processSubContraptionPosition(getContraptionWorld());
            subContraption.disassemble();
        }

        addBlocksToWorld(mainWorld);
        // TODO
        // addPassengersToWorld(level, transform, getPassengers());

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
            ChunkCoordinates pos = blockFace.getPos();
            StabilizedContraption subContraption = new StabilizedContraption(
                this.getWorld(),
                (IControlContraption) this.getWorld()
                    .getTileEntity(pos.posX, pos.posY, pos.posZ),
                face);
            try {
                if (!subContraption.assemble(parentWorld, pos.posX, pos.posY, pos.posZ)) continue;
            } catch (AssemblyException e) {
                continue;
            }
            subContraption.removeBlocksFromWorld(parentWorld);
            subContraption.preInit();
            ChunkCoordinates offset = face.getNormal();
            subContraption.anchorX = pos.posX + offset.posX;
            subContraption.anchorY = pos.posY + offset.posY;
            subContraption.anchorZ = pos.posZ + offset.posZ;
            subContraption.init();
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
        beingRemoved = true;
        if (simplifiedEntityColliderProvider != null) {
            simplifiedEntityColliderProvider.cancel(false);
            simplifiedEntityColliderProvider = null;
        }
    }

    public void init() {
        if (parentWorld.isRemote) return;

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

    public Vec3 toGlobalVector(Vec3 localVec) {
        return this.getContraptionWorld()
            .transformToGlobal(localVec);
    }

    public Vec3 toLocalVector(Vec3 globalVec) {
        return this.getContraptionWorld()
            .transformToLocal(globalVec);
    }

    public void tick() {
        if (parentWorld == null) {
            World mainWorld = MetaworldsMod.proxy.getMainWorld();
            if (mainWorld == null) return;
            parentWorld = ((IMixinWorld) mainWorld).getSubWorld(parentWorldID);
        }
        if (stabilizedSubContraptionsIDs.size() > stabilizedSubContraptions.size()) {
            for (Map.Entry<Integer, BlockFace> entry : stabilizedSubContraptionsIDs.entrySet()) {
                SubWorld subWorld = (SubWorld) ((IMixinWorld) DimensionManager.getWorld(0)).getSubWorld(entry.getKey());
                if (!(subWorld instanceof ContraptionWorld contraptionWorld)) continue;
                if (stabilizedSubContraptions.containsKey(contraptionWorld.getContraption())) continue;
                stabilizedSubContraptions.put(contraptionWorld.getContraption(), entry.getValue());
            }
        }
        if (parentWorld != null && !parentWorld.isRemote
            && parentWorld instanceof SubWorld subworld
            && subworld.getIsInMotion()) processSubContraptionPosition(subworld);
        // TODO Most likely not needed
        // fluidStorage.forEach((pos, mfs) -> mfs.tick(entity, pos, world.isRemote));
    }

    @SideOnly(Side.CLIENT)
    public void doTickPartial(double interpolationFactor) {
        if (parentWorld != null && parentWorld instanceof SubWorld subworld && subworld.getIsInMotion())
            processSubContraptionPosition(subworld);
    }

    protected void processSubContraptionPosition(SubWorld anchorWorld) {
        Vec3 anchor = Vec3.createVectorHelper(anchorX + 0.5d, anchorY + 0.5d, anchorZ + 0.5d);
        Vec3 globalAnchor = anchorWorld.transformToGlobal(anchor);
        Vec3 translation = anchor.subtract(globalAnchor);
        SubWorld contraptionSubworld = this.getContraptionWorld();
        contraptionSubworld.setTranslation(translation);
        contraptionSubworld.setRotationPitch(anchorWorld.getRotationPitch());
        contraptionSubworld.setRotationYaw(anchorWorld.getRotationYaw());
        contraptionSubworld.setRotationRoll(anchorWorld.getRotationRoll());
    }

    /** move the first block in frontier queue */
    @SuppressWarnings("static-access")
    protected boolean moveBlock(World world, @Nullable Direction forcedDirection, Queue<ChunkCoordinates> frontier,
        Set<ChunkCoordinates> visited) throws AssemblyException {
        ChunkCoordinates pos = frontier.poll();
        if (pos == null) return false;
        visited.add(pos);
        int posX = pos.posX;
        int posY = pos.posY;
        int posZ = pos.posZ;

        if (posY >= world.getHeight()) return true;
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
                ChunkCoordinates attached = new ChunkCoordinates(pos);
                if (te.adjacentChestZNeg != null) attached.posZ--;
                if (te.adjacentChestZPos != null) attached.posZ++;
                if (te.adjacentChestXNeg != null) attached.posX--;
                if (te.adjacentChestXPos != null) attached.posX++;
                if (!visited.contains(attached)) frontier.add(attached);
            }
        }

        // Bearings potentially create stabilized sub-contraptions
        if (block == AllBlocks.mechanical_bearing)
            moveBearing(posX, posY, posZ, frontier, visited, (MechanicalBearingBlock) block, meta);

        // WM Bearings attach their structure when moved
        if (block == AllBlocks.windmill_bearing)
            moveWindmillBearing(posX, posY, posZ, frontier, visited, (WindmillBearingBlock) block, meta);

        // TODO
        // // Seats transfer their passenger to the contraption
        // if (block instanceof SeatBlock) moveSeat(world, posX, posY, posZ);
        //
        // // Pulleys drag their rope and their attached structure
        // if (block instanceof PulleyBlock) movePulley(world, posX, posY, posZ, frontier, visited);

        // Pistons drag their attaches poles and extension
        if (block instanceof MechanicalPistonBlock piston
            && !moveMechanicalPiston(world, posX, posY, posZ, frontier, visited, piston, meta)) return false;
        if (MechanicalPistonBlock.isExtensionPole(block))
            movePistonPole(world, posX, posY, posZ, frontier, visited, (PistonExtensionPoleBlock) block, meta);
        if (MechanicalPistonBlock.isPistonHead(block))
            movePistonHead(world, posX, posY, posZ, frontier, visited, (MechanicalPistonHeadBlock) block, meta);

        // TODO
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

    protected void movePistonHead(World world, int x, int y, int z, Queue<ChunkCoordinates> frontier,
        Set<ChunkCoordinates> visited, MechanicalPistonHeadBlock block, int meta) {
        Direction direction = block.getDirection(meta);
        ChunkCoordinates offset = direction.getOpposite()
            .getNormal();
        offset.posX += x;
        offset.posY += y;
        offset.posZ += z;
        if (!visited.contains(offset)) {
            Block neighbourBlock = world.getBlock(offset.posX, offset.posY, offset.posZ);
            int neighbourMeta = world.getBlockMetadata(offset.posX, offset.posY, offset.posZ);
            if (neighbourBlock instanceof PistonExtensionPoleBlock pole && pole.getDirection(neighbourMeta)
                .getAxis() == direction.getAxis()) frontier.add(offset);
            if (neighbourBlock instanceof MechanicalPistonBlock piston) {
                Direction pistonFacing = piston.getDirection(neighbourMeta);
                if (pistonFacing == direction
                    && piston.getPistonState(world, offset.posX, offset.posY, offset.posZ) == PistonState.EXTENDED)
                    frontier.add(offset);
            }
        }
        if (block.isSticky(meta)) {
            ChunkCoordinates attached = direction.getNormal();
            attached.posX += x;
            attached.posY += y;
            attached.posZ += z;
            if (!visited.contains(attached)) frontier.add(attached);
        }
    }

    protected void movePistonPole(World world, int x, int y, int z, Queue<ChunkCoordinates> frontier,
        Set<ChunkCoordinates> visited, PistonExtensionPoleBlock block, int meta) {
        for (Direction d : Iterate.directionsInAxis(block.getAxis(meta))) {
            ChunkCoordinates offset = d.getNormal();
            offset.posX += x;
            offset.posY += y;
            offset.posZ += z;
            if (!visited.contains(offset)) {
                Block neighbourBlock = world.getBlock(offset.posX, offset.posY, offset.posZ);
                int neighbourMeta = world.getBlockMetadata(offset.posX, offset.posY, offset.posZ);
                if (neighbourBlock instanceof PistonExtensionPoleBlock pole
                    && pole.getAxis(neighbourMeta) == d.getAxis()) frontier.add(offset);
                if (neighbourBlock instanceof MechanicalPistonHeadBlock head
                    && head.getAxis(neighbourMeta) == d.getAxis()) frontier.add(offset);
                if (neighbourBlock instanceof MechanicalPistonBlock piston) {
                    Direction pistonFacing = piston.getDirection(neighbourMeta);
                    if (pistonFacing == d || pistonFacing == d.getOpposite()
                        && piston.getPistonState(world, offset.posX, offset.posY, offset.posZ) == PistonState.EXTENDED)
                        frontier.add(offset);
                }
            }
        }
    }

    // TODO
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

    // TODO
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

    private void moveWindmillBearing(int x, int y, int z, Queue<ChunkCoordinates> frontier,
        Set<ChunkCoordinates> visited, WindmillBearingBlock block, int meta) {
        Direction facing = block.getDirection(meta);
        ChunkCoordinates offset = facing.getNormal();
        offset.posX += x;
        offset.posY += y;
        offset.posZ += z;
        if (!visited.contains(offset)) frontier.add(offset);
    }

    private void moveBearing(int x, int y, int z, Queue<ChunkCoordinates> frontier, Set<ChunkCoordinates> visited,
        MechanicalBearingBlock block, int meta) {
        Direction facing = block.getDirection(meta);
        if (!canBeStabilized(facing, x, y, z)) {
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

    // TODO
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

    // TODO
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

    private boolean moveMechanicalPiston(World world, int x, int y, int z, Queue<ChunkCoordinates> frontier,
        Set<ChunkCoordinates> visited, MechanicalPistonBlock block, int meta) throws AssemblyException {
        Direction direction = block.getDirection(meta);
        PistonState pistonState = block.getPistonState(world, x, y, z);
        if (pistonState == PistonState.MOVING) return false;

        ChunkCoordinates offset = direction.getOpposite()
            .getNormal();
        offset.posX += x;
        offset.posY += y;
        offset.posZ += z;
        if (!visited.contains(offset)) {
            Block neighbourBlock = world.getBlock(offset.posX, offset.posY, offset.posZ);
            int neighbourMeta = world.getBlockMetadata(offset.posX, offset.posY, offset.posZ);
            if (neighbourBlock instanceof PistonExtensionPoleBlock pole && pole.getDirection(neighbourMeta)
                .getAxis() == direction.getAxis()) frontier.add(offset);
        }

        if (pistonState == PistonState.EXTENDED || MechanicalPistonBlock.isStickyPiston(block)) {
            offset = direction.getNormal();
            offset.posX += x;
            offset.posY += y;
            offset.posZ += z;
            if (!visited.contains(offset)) frontier.add(offset);
        }

        return true;
    }

    // TODO
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

        if (AllMovementBehaviours.contains(block)) actors.add(MutablePair.of(new ChunkCoordinates(x, y, z), null));

        // TODO
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

    public ChunkCoordinates getCenterBlock() {
        if (this.contraptionWorld == null) return null;
        ContraptionWorld contraption = this.getContraptionWorld();
        return new ChunkCoordinates(
            MathHelper.floor_double(contraption.getCenterX()),
            MathHelper.floor_double(contraption.getCenterY()),
            MathHelper.floor_double(contraption.getCenterZ()));
    }

    public Vec3 getPosition() {
        if (this.contraptionWorld == null) return null;
        return this.getContraptionWorld()
            .transformToGlobal(anchorX + 0.5D, anchorY + 0.5D, anchorZ + 0.5D);
    }

    public void setPosition(float x, float y, float z) {
        setPosition(Vec3.createVectorHelper(x, y, z));
    }

    public void setPosition(Vec3 newPos) {
        if (this.contraptionWorld == null) return;
        ContraptionWorld contraption = this.getContraptionWorld();
        contraption.setTranslation(
            Vec3.createVectorHelper(contraption.getCenterX(), contraption.getCenterY(), contraption.getCenterZ())
                .subtract(newPos));
    }

    public Vec3 getMotion() {
        if (this.contraptionWorld == null) return null;
        ContraptionWorld contraption = this.getContraptionWorld();
        return Vec3.createVectorHelper(contraption.getMotionX(), contraption.getMotionY(), contraption.getMotionZ());
    }

    public void setMotion(Vec3 newMotion) {
        setMotion(newMotion.xCoord, newMotion.yCoord, newMotion.zCoord);
    }

    public void setMotion(double dX, double dY, double dZ) {
        if (this.contraptionWorld == null) return;
        ContraptionWorld contraption = this.getContraptionWorld();
        contraption.setMotion(dX, dY, dZ);
    }

    public void move(Vec3 newMotion) {
        move(newMotion.xCoord, newMotion.yCoord, newMotion.zCoord);
    }

    public void move(double dX, double dY, double dZ) {
        if (this.contraptionWorld == null) return;
        ContraptionWorld contraption = this.getContraptionWorld();
        contraption.setTranslation(
            contraption.getTranslationX() + dX,
            contraption.getTranslationY() + dY,
            contraption.getTranslationZ() + dZ);
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

        actors.clear();
        NBTHelper.iterateCompoundList(nbt.getTagList("Actors", 10), c -> {
            ChunkCoordinates pos = NBTHelper.readChunkCoordinates(c.getCompoundTag("Pos"));
            MovementContext context = MovementContext.readNBT(parentWorld, pos.posX, pos.posY, pos.posZ, c, this);
            getActors().add(MutablePair.of(pos, context));
        });

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
        if (nbt.hasKey("AnchorWorld")) parentWorldID = nbt.getInteger("AnchorWorld");
    }

    public NBTTagCompound writeNBT(boolean spawnPacket) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("Type", getType().id);

        NBTTagCompound blocksNBT = writeBlocksCompound();

        NBTTagList actorsNBT = new NBTTagList();
        for (MutablePair<ChunkCoordinates, MovementContext> actor : actors) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("Pos", NBTHelper.writeChunkCoordinates(actor.left));
            AllMovementBehaviours.of(actor.right.block)
                .writeExtraData(actor.right);
            actor.right.writeToNBT(compound);
            actorsNBT.appendTag(compound);
        }

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
        nbt.setTag("Actors", actorsNBT);
        // TODO
        // nbt.setTag("Interactors", interactorNBT);
        nbt.setTag("Superglue", superglueNBT);
        // TODO
        // nbt.setTag("Storage", storageNBT);
        // nbt.setTag("FluidStorage", fluidStorageNBT);
        nbt.setIntArray("Anchor", new int[] { anchorX, anchorY, anchorZ });
        if (parentWorld != null) nbt.setInteger("AnchorWorld", ((IMixinWorld) parentWorld).getSubWorldID());
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

    public void removeBlocksFromWorld(World parentWorld, int offsetX, int offsetY, int offsetZ) {
        offsetX += anchorX;
        offsetY += anchorY;
        offsetZ += anchorZ;
        glueToRemove.forEach(SuperGlueEntity::setDead);
        getContraptionWorld(
            (double) offsetX + 0.5D,
            (double) offsetY + 0.5D,
            (double) offsetZ + 0.5D,
            ((IMixinWorld) parentWorld).getTranslationX(),
            ((IMixinWorld) parentWorld).getTranslationY(),
            ((IMixinWorld) parentWorld).getTranslationZ(),
            ((IMixinWorld) parentWorld).getRotationPitch(),
            ((IMixinWorld) parentWorld).getRotationYaw(),
            ((IMixinWorld) parentWorld).getRotationRoll(),
            ((IMixinWorld) parentWorld).getScaling());

        Block oldBlock;
        Block block;

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
                DisplacementHelper.displaceBlock(
                    curCoord.posX,
                    curCoord.posY,
                    curCoord.posZ,
                    parentWorld,
                    contraptionWorld,
                    new CustomBlockDisplacement() {

                        @Override
                        public boolean tryDisplaceBlock(World sourceWorld, World targetWorld, int x, int y, int z,
                            Block block, int metadata) {
                            return customBlockRemoval(sourceWorld, x, y, z, block, metadata);
                        }

                    });
            }
        }

        listsToParse.set(0, blocksToTakeBrittle);
        listsToParse.set(2, blocksToTake);
        for (List<ChunkCoordinates> currentList : listsToParse) {
            for (ChunkCoordinates curCoord : currentList) {
                oldBlock = parentWorld.getBlock(curCoord.posX, curCoord.posY, curCoord.posZ);
                block = contraptionWorld.getBlock(curCoord.posX, curCoord.posY, curCoord.posZ);
                if (oldBlock == block) parentWorld.setBlockToAir(curCoord.posX, curCoord.posY, curCoord.posZ);
            }
        }
    }

    private double oldCenterX;
    private double oldCenterY;
    private double oldCenterZ;

    public void alignContraption() {
        ((ContraptionWorld) contraptionWorld).alignSubWorld();
    }

    @SuppressWarnings("unchecked")
    public void addBlocksToWorld(World world) {
        ContraptionWorld subworld = (ContraptionWorld) contraptionWorld;
        Block block;
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
                DisplacementHelper.displaceBlock(
                    curCoord.posX,
                    curCoord.posY,
                    curCoord.posZ,
                    contraptionWorld,
                    world,
                    new CustomBlockDisplacement() {

                        @Override
                        public boolean tryDisplaceBlock(World sourceWorld, World targetWorld, int x, int y, int z,
                            Block block, int metadata) {
                            ChunkCoordinates globalPos = getContraptionWorld().transformBlockToGlobal(x, y, z);
                            return customBlockPlacement(
                                targetWorld,
                                globalPos.posX,
                                globalPos.posY,
                                globalPos.posZ,
                                block,
                                metadata);
                        }

                    });
            }
        }

        Iterator<Entity> iter = contraptionWorld.loadedEntityList.iterator();
        while (iter.hasNext()) {
            oldEntity = iter.next();
            if (oldEntity instanceof EntityPlayer) continue;
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
                contraptionWorld.setBlockToAir(curCoord.posX, curCoord.posY, curCoord.posZ);
            }
        }

        subworld.setCenter(oldCenterX, oldCenterY, oldCenterZ);
        if (subworld instanceof SubWorldServer subWorldServer) {
            ((IMixinWorld) subWorldServer.getParentWorld()).getSubWorldsMap()
                .remove(subWorldServer.getSubWorldID());
            subWorldServer.removeSubWorld();
            subWorldServer.flush();
            subWorldServer.deleteSubWorldData();
        }

        for (Pair<ChunkCoordinates, Direction> pair : superglue) {
            ChunkCoordinates pos = this.toGlobalPos(pair.getKey());
            Direction facing = RotationHelper.getRotatedDirection(contraptionWorld, pair.getValue());
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
        for (MutablePair<ChunkCoordinates, MovementContext> pair : actors) {
            MovementContext context = new MovementContext(world, pair.left.posX, pair.left.posY, pair.left.posZ, this);
            AllMovementBehaviours.of(pair.right.block)
                .startMoving(context);
            pair.setRight(context);
        }
    }

    public void stop(World world) {
        foreachActor(world, (behaviour, ctx) -> {
            behaviour.stopMoving(ctx);
            ctx.position = null;
            ctx.motion = VecHelper.ZERO;
            ctx.relativeMotion = VecHelper.ZERO;
            ctx.rotation = v -> v;
        });
    }

    public void foreachActor(World world, BiConsumer<MovementBehaviour, MovementContext> callBack) {
        for (MutablePair<ChunkCoordinates, MovementContext> pair : actors)
            callBack.accept(AllMovementBehaviours.of(pair.getRight().block), pair.getRight());
    }

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

    public List<MutablePair<ChunkCoordinates, MovementContext>> getActors() {
        return actors;
    }

    // TODO
    // public void updateContainedFluid(int localX, int localY, int localZ, FluidStack containedFluid) {
    // MountedFluidStorage mountedFluidStorage = fluidStorage.get(localPos);
    // if (mountedFluidStorage != null)
    // mountedFluidStorage.updateFluid(containedFluid);
    // }

    public abstract Vec3 applyRotation(Vec3 localPos, float partialTicks);

    public abstract Vec3 reverseRotation(Vec3 localPos, float partialTicks);

    public void tickActors() {
        if (parentWorld == null) return;
        boolean stalledPreviously = stalled;

        if (!parentWorld.isRemote) stalled = false;

        ticking = true;
        for (MutablePair<ChunkCoordinates, MovementContext> pair : actors) {
            MovementContext context = pair.right;
            ChunkCoordinates blockPos = pair.left;
            MovementBehaviour actor = AllMovementBehaviours.of(context.block);

            Vec3 oldMotion = context.motion;
            Vec3 activeAreaOffset = actor.getActiveAreaOffset(context);
            Vec3 actorPosition = toGlobalVector(
                VecHelper.getCenterOf(blockPos)
                    .addVector(activeAreaOffset.xCoord, activeAreaOffset.yCoord, activeAreaOffset.zCoord));
            int gridX = MathHelper.floor_double(actorPosition.xCoord);
            int gridY = MathHelper.floor_double(actorPosition.yCoord);
            int gridZ = MathHelper.floor_double(actorPosition.zCoord);
            boolean newPosVisited = !context.stall
                && shouldActorTrigger(context, blockPos, actor, actorPosition, gridX, gridY, gridZ);

            context.rotation = v -> applyRotation(v, 1);
            context.position = actorPosition;
            if (!actor.isActive(context)) continue;
            if (newPosVisited && !context.stall) {
                actor.visitNewPosition(context, gridX, gridY, gridZ);
                context.firstMovement = false;
            }
            if (!oldMotion.equals(context.motion)) {
                actor.onSpeedChanged(context, oldMotion, context.motion);
            }
            actor.tick(context);
            stalled |= context.stall;
        }
        ticking = false;

        for (Contraption subContraption : stabilizedSubContraptions.keySet()) {
            if (!(subContraption instanceof StabilizedContraption stabilizedContraption)) continue;
            if (stabilizedContraption.stalled) {
                stalled = true;
                break;
            }
        }

        if (!parentWorld.isRemote) {
            if (!stalledPreviously && stalled) onContraptionStalled();
            getContraptionWorld().setStalled(stalled);
            return;
        }

        stalled = getContraptionWorld().isStalled();
    }

    protected void onContraptionStalled() {
        if (this.getWorld().isRemote) return;
        ContraptionWorld contraption = this.getContraptionWorld();
        AllPackets.CHANNEL.sendToAll(
            new ContraptionStallPacket(
                contraption.getSubWorldID(),
                contraption.getTranslationX(),
                contraption.getTranslationY(),
                contraption.getTranslationZ(),
                getStalledAngle()));
    }

    protected boolean shouldActorTrigger(MovementContext context, ChunkCoordinates blockPos, MovementBehaviour actor,
        Vec3 actorPosition, int gridX, int gridY, int gridZ) {
        Vec3 previousPosition = context.position;
        if (previousPosition == null) return false;

        context.motion = actorPosition.subtract(previousPosition);
        Vec3 relativeMotion = context.motion;
        relativeMotion = reverseRotation(relativeMotion, 1);
        context.relativeMotion = relativeMotion;
        return MathHelper.floor_double(previousPosition.xCoord) != gridX
            || MathHelper.floor_double(previousPosition.yCoord) != gridY
            || MathHelper.floor_double(previousPosition.zCoord) != gridZ
            || context.relativeMotion.lengthVector() > 0 && context.firstMovement;
    }

    @SideOnly(Side.CLIENT)
    static void handleStallPacket(ContraptionStallPacket packet) {
        World subworld = ((IMixinWorld) MetaworldsMod.proxy.getMainWorld()).getSubWorld(packet.subworldID);
        if (!(subworld instanceof ContraptionWorld contraptionWorld)) return;
        contraptionWorld.getContraption()
            .handleStallInformation(packet.x, packet.y, packet.z, packet.angle);
    }

    protected abstract float getStalledAngle();

    protected abstract void handleStallInformation(float x, float y, float z, float angle);

    public boolean supportsTerrainCollision() {
        return this instanceof TranslatingContraption;
    }

    public AxisAlignedBB getBoundingBox() {
        if (this.contraptionWorld == null) return null;
        return this.getContraptionWorld()
            .getMaximumStretchedWorldBB(false, false);
    }
}
