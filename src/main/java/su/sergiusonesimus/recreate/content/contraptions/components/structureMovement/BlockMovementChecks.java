package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockTorch;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.util.BlockVolatilityMap;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.MechanicalBearingTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.SailBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.WindmillBearingBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.WindmillBearingTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis.AbstractChassisBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonHeadBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import su.sergiusonesimus.recreate.foundation.config.ContraptionMovementSetting;

public class BlockMovementChecks {

    private static final List<MovementNecessaryCheck> MOVEMENT_NECESSARY_CHECKS = new ArrayList<>();
    private static final List<MovementAllowedCheck> MOVEMENT_ALLOWED_CHECKS = new ArrayList<>();
    private static final List<BrittleCheck> BRITTLE_CHECKS = new ArrayList<>();
    private static final List<AttachedCheck> ATTACHED_CHECKS = new ArrayList<>();
    private static final List<NotSupportiveCheck> NOT_SUPPORTIVE_CHECKS = new ArrayList<>();

    // Registration
    // Add new checks to the front instead of the end

    public static void registerMovementNecessaryCheck(MovementNecessaryCheck check) {
        MOVEMENT_NECESSARY_CHECKS.add(0, check);
    }

    public static void registerMovementAllowedCheck(MovementAllowedCheck check) {
        MOVEMENT_ALLOWED_CHECKS.add(0, check);
    }

    public static void registerBrittleCheck(BrittleCheck check) {
        BRITTLE_CHECKS.add(0, check);
    }

    public static void registerAttachedCheck(AttachedCheck check) {
        ATTACHED_CHECKS.add(0, check);
    }

    public static void registerNotSupportiveCheck(NotSupportiveCheck check) {
        NOT_SUPPORTIVE_CHECKS.add(0, check);
    }

    public static void registerAllChecks(AllChecks checks) {
        registerMovementNecessaryCheck(checks);
        registerMovementAllowedCheck(checks);
        registerBrittleCheck(checks);
        registerAttachedCheck(checks);
        registerNotSupportiveCheck(checks);
    }

    // Actual check methods

    public static boolean isMovementNecessary(Block block, int meta, World world, ChunkCoordinates blockPos) {
        return isMovementNecessary(block, meta, world, blockPos.posX, blockPos.posY, blockPos.posZ);
    }

    public static boolean isMovementNecessary(Block block, int meta, World world, int x, int y, int z) {
        for (MovementNecessaryCheck check : MOVEMENT_NECESSARY_CHECKS) {
            CheckResult result = check.isMovementNecessary(block, meta, world, x, y, z);
            if (result != CheckResult.PASS) {
                return result.toBoolean();
            }
        }
        return isMovementNecessaryFallback(block, meta, world, x, y, z);
    }

    public static boolean isMovementAllowed(Block block, int meta, World world, ChunkCoordinates blockPos) {
        return isMovementAllowed(block, meta, world, blockPos.posX, blockPos.posY, blockPos.posZ);
    }

    public static boolean isMovementAllowed(Block block, int meta, World world, int x, int y, int z) {
        for (MovementAllowedCheck check : MOVEMENT_ALLOWED_CHECKS) {
            CheckResult result = check.isMovementAllowed(block, meta, world, x, y, z);
            if (result != CheckResult.PASS) {
                return result.toBoolean();
            }
        }
        return isMovementAllowedFallback(block, meta, world, x, y, z);
    }

    /**
     * Attached blocks will move if blocks they are attached to are moved
     */
    public static boolean isBlockAttachedTowards(Block block, int meta, World world, ChunkCoordinates blockPos,
        Direction direction) {
        return isBlockAttachedTowards(block, meta, world, blockPos.posX, blockPos.posY, blockPos.posZ, direction);
    }

    /**
     * Attached blocks will move if blocks they are attached to are moved
     */
    public static boolean isBlockAttachedTowards(Block block, int meta, World world, int x, int y, int z,
        Direction direction) {
        for (AttachedCheck check : ATTACHED_CHECKS) {
            CheckResult result = check.isBlockAttachedTowards(block, meta, world, x, y, z, direction);
            if (result != CheckResult.PASS) {
                return result.toBoolean();
            }
        }
        return isBlockAttachedTowardsFallback(block, meta, world, x, y, z, direction);
    }

    /**
     * Non-Supportive blocks will not continue a chain of blocks picked up by e.g. a
     * piston
     */
    public static boolean isNotSupportive(Block block, int meta, Direction facing) {
        for (NotSupportiveCheck check : NOT_SUPPORTIVE_CHECKS) {
            CheckResult result = check.isNotSupportive(block, meta, facing);
            if (result != CheckResult.PASS) {
                return result.toBoolean();
            }
        }
        return isNotSupportiveFallback(block, meta, facing);
    }

    // Fallback checks

    private static boolean isMovementNecessaryFallback(Block block, int meta, World world, int x, int y, int z) {
        if (BlockVolatilityMap.checkBlockVolatility(block)) return true;
        if (block instanceof BlockFenceGate) return true;
        if (block.getMaterial()
            .isReplaceable()) return false;
        AxisAlignedBB bb = block.getCollisionBoundingBoxFromPool(world, x, y, z);
        if (bb == null || bb.minX == bb.maxX || bb.minY == bb.maxY || bb.minZ == bb.maxZ) return false;
        return true;
    }

    private static boolean isMovementAllowedFallback(Block block, int meta, World world, int x, int y, int z) {
        if (block instanceof AbstractChassisBlock) return true;
        if (block.getBlockHardness(world, x, y, z) < 0) return false;
        if (block.getMobilityFlag() == 2) return false;
        if (ContraptionMovementSetting.get(block) == ContraptionMovementSetting.UNMOVABLE) return false;

        // Move controllers only when they aren't moving
        if (block instanceof MechanicalPistonBlock piston
            && piston.getPistonState(world, x, y, z) != PistonState.MOVING) return true;
        if (block instanceof MechanicalBearingBlock) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof MechanicalBearingTileEntity) return !((MechanicalBearingTileEntity) te).isRunning();
        }
        if (block instanceof WindmillBearingBlock
            && world.getTileEntity(x, y, z) instanceof WindmillBearingTileEntity windmill) {
            return !windmill.isRunning();
        }
        // TODO
        // if (block instanceof ClockworkBearingBlock) {
        // TileEntity te = world.getTileEntity(x, y, z);
        // if (te instanceof ClockworkBearingTileEntity) return !((ClockworkBearingTileEntity) te).isRunning();
        // }
        // if (block instanceof PulleyBlock) {
        // TileEntity te = world.getTileEntity(x, y, z);
        // if (te instanceof PulleyTileEntity) return !((PulleyTileEntity) te).running;
        // }
        //
        // if (AllBlocks.BELT.has(state))
        // return true;
        // if (block instanceof GrindstoneBlock)
        // return true;
        return block.getMobilityFlag() != 2;
    }

    private static boolean isBlockAttachedTowardsFallback(Block block, int meta, World world, int x, int y, int z,
        Direction direction) {
        if (block instanceof BlockLadder) return Direction.from2DDataValue(meta) == direction.getOpposite();
        if (block instanceof BlockTorch && meta > 0)
            return Direction.from2DDataValue(meta - 1) == direction.getOpposite();
        if (block instanceof BlockSign) {
            if (((BlockSign) block).field_149967_b) return direction == Direction.DOWN;
            else return Direction.from3DDataValue(meta) == direction.getOpposite();
        }
        if (block instanceof BlockBasePressurePlate) return direction == Direction.DOWN;
        if (block instanceof BlockDoor) {
            if ((meta & 8) == 0 && direction == Direction.UP) return true;
            return direction == Direction.DOWN;
        }
        if (block instanceof BlockBed) {
            int dir = BlockDirectional.getDirection(meta);
            Direction facing;
            switch (dir) {
                default:
                case 0:
                    facing = Direction.NORTH;
                    break;
                case 1:
                    facing = Direction.WEST;
                    break;
                case 2:
                    facing = Direction.SOUTH;
                    break;
                case 3:
                    facing = Direction.EAST;
                    break;
            }
            return direction == facing;
        }
        // TODO
        // if (block instanceof RedstoneLinkBlock)
        // return direction.getOpposite() == state.getValue(RedstoneLinkBlock.FACING);
        if (block instanceof BlockFlowerPot) return direction == Direction.DOWN;
        if (block instanceof BlockRedstoneDiode) return direction == Direction.DOWN;
        if (block instanceof BlockRedstoneWire) return direction == Direction.DOWN;
        if (block instanceof BlockCarpet) return direction == Direction.DOWN;
        if (block instanceof BlockRedstoneTorch && meta > 0)
            return Direction.from2DDataValue(meta - 1) == direction.getOpposite();
        if (block instanceof BlockTorch) return direction == Direction.DOWN;
        // TODO
        // if (block instanceof FaceAttachedHorizontalDirectionalBlock) {
        // AttachFace attachFace = state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE);
        // if (attachFace == AttachFace.CEILING)
        // return direction == Direction.UP;
        // if (attachFace == AttachFace.FLOOR)
        // return direction == Direction.DOWN;
        // if (attachFace == AttachFace.WALL)
        // return direction.getOpposite() == state.getValue(FaceAttachedHorizontalDirectionalBlock.FACING);
        // }
        // if (state.hasProperty(BlockStateProperties.HANGING))
        // return direction == (state.getValue(BlockStateProperties.HANGING) ? Direction.UP : Direction.DOWN);
        if (block instanceof BlockRailBase) return direction == Direction.DOWN;
        // TODO
        // if (block instanceof AttachedActorBlock)
        // return direction == state.getValue(HarvesterBlock.FACING)
        // .getOpposite();
        // if (block instanceof HandCrankBlock)
        // return direction == state.getValue(HandCrankBlock.FACING)
        // .getOpposite();
        // if (block instanceof NozzleBlock)
        // return direction == state.getValue(NozzleBlock.FACING)
        // .getOpposite();
        // if (block instanceof EngineBlock)
        // return direction == state.getValue(EngineBlock.FACING)
        // .getOpposite();
        // if (block instanceof BellBlock) {
        // BellAttachType attachment = state.getValue(BlockStateProperties.BELL_ATTACHMENT);
        // if (attachment == BellAttachType.FLOOR)
        // return direction == Direction.DOWN;
        // if (attachment == BellAttachType.CEILING)
        // return direction == Direction.UP;
        // return direction == state.getValue(HorizontalDirectionalBlock.FACING);
        // }
        if (block instanceof SailBlock sail) return direction.getAxis() != sail.getAxis(meta);
        // TODO
        // if (block instanceof FluidTankBlock)
        // return FluidTankConnectivityHandler.isConnected(world, x, y, z, pos.relative(direction));
        // if (block instanceof ItemVaultBlock)
        // return ItemVaultConnectivityHandler.isConnected(world, x, y, z, pos.relative(direction));
        // if (AllBlocks.STICKER.has(state) && state.getValue(StickerBlock.EXTENDED)) {
        // return direction == state.getValue(StickerBlock.FACING)
        // && !isNotSupportive(world.getBlockState(pos.relative(direction)), direction.getOpposite());
        // }
        return false;
    }

    private static boolean isNotSupportiveFallback(Block block, int meta, Direction facing) {
        // TODO
        // if (AllBlocks.MECHANICAL_DRILL.has(state))
        // return state.getValue(BlockStateProperties.FACING) == facing;
        // if (AllBlocks.MECHANICAL_BEARING.has(state))
        // return state.getValue(BlockStateProperties.FACING) == facing;
        // if (AllBlocks.CART_ASSEMBLER.has(state))
        // return Direction.DOWN == facing;
        // if (AllBlocks.MECHANICAL_SAW.has(state))
        // return state.getValue(BlockStateProperties.FACING) == facing;
        // if (AllBlocks.PORTABLE_STORAGE_INTERFACE.has(state))
        // return state.getValue(PortableStorageInterfaceBlock.FACING) == facing;
        // if (block instanceof AttachedActorBlock)
        // return state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;
        // if (AllBlocks.ROPE_PULLEY.has(state))
        // return facing == Direction.DOWN;
        if (block instanceof BlockCarpet) return facing == Direction.UP;
        if (block instanceof SailBlock sail) return facing.getAxis() == sail.getAxis(meta);
        if (block instanceof PistonExtensionPoleBlock pole) return facing.getAxis() != pole.getAxis(meta);
        if (block instanceof MechanicalPistonHeadBlock head) return facing.getAxis() != head.getAxis(meta);
        // TODO
        // if (AllBlocks.STICKER.has(state) && !state.getValue(StickerBlock.EXTENDED))
        // return facing == state.getValue(StickerBlock.FACING);
        return BlockVolatilityMap.checkBlockVolatility(block);
    }

    // Check classes

    public static interface MovementNecessaryCheck {

        public CheckResult isMovementNecessary(Block block, int meta, World world, int x, int y, int z);
    }

    public static interface MovementAllowedCheck {

        public CheckResult isMovementAllowed(Block block, int meta, World world, int x, int y, int z);
    }

    public static interface BrittleCheck {

        /**
         * Brittle blocks will be collected first, as they may break when other blocks
         * are removed before them
         */
        public CheckResult isBrittle(Block block, int meta);
    }

    public static interface AttachedCheck {

        /**
         * Attached blocks will move if blocks they are attached to are moved
         */
        public CheckResult isBlockAttachedTowards(Block block, int meta, World world, int x, int y, int z,
            Direction direction);
    }

    public static interface NotSupportiveCheck {

        /**
         * Non-Supportive blocks will not continue a chain of blocks picked up by e.g. a
         * piston
         */
        public CheckResult isNotSupportive(Block block, int meta, Direction direction);
    }

    public static interface AllChecks
        extends MovementNecessaryCheck, MovementAllowedCheck, BrittleCheck, AttachedCheck, NotSupportiveCheck {
    }

    public static enum CheckResult {

        SUCCESS,
        FAIL,
        PASS;

        public Boolean toBoolean() {
            return this == PASS ? null : (this == SUCCESS ? true : false);
        }

        public static CheckResult of(boolean b) {
            return b ? SUCCESS : FAIL;
        }

        public static CheckResult of(Boolean b) {
            return b == null ? PASS : (b ? SUCCESS : FAIL);
        }
    }

}
