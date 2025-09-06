package su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.base.IRotate;
import su.sergiusonesimus.recreate.content.contraptions.base.RotatedPillarKineticBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.AbstractShaftBlock;
import su.sergiusonesimus.recreate.foundation.block.ITE;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
import su.sergiusonesimus.recreate.util.Raytracer;

@ParametersAreNonnullByDefault
public class CogWheelBlock extends AbstractShaftBlock implements ITE<CogWheelTileEntity>, ICogWheel {

    boolean isLarge;

    protected CogWheelBlock(Material materialIn, boolean large) {
        super(materialIn);
        this.setHardness(1.5F);
        this.setResistance(10.0F);
        this.setStepSound(soundTypeWood);
        this.setBlockTextureName("planks_oak");
        isLarge = large;
    }

    public static CogWheelBlock small(Material materialIn) {
        return new CogWheelBlock(materialIn, false);
    }

    public static CogWheelBlock large(Material materialIn) {
        return new CogWheelBlock(materialIn, true);
    }

    @Override
    public boolean isLargeCog() {
        return isLarge;
    }

    @Override
    public boolean isSmallCog() {
        return !isLarge;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        if (worldIn == null) return;
        int meta = worldIn.getBlockMetadata(x, y, z);
        double d = 1d / 16d;
        double minThickness = 6 * d;
        double maxThickness = 1D - 6 * d;
        double minDiameter = isSmallCog() ? 2 * d : 0D;
        double maxDiameter = isSmallCog() ? 1d - 2 * d : 1D;
        switch (meta) {
            default:
            case 0:
                minX = minZ = minDiameter;
                maxX = maxZ = maxDiameter;
                minY = minThickness;
                maxY = maxThickness;
                break;
            case 1:
                minY = minZ = minDiameter;
                maxY = maxZ = maxDiameter;
                minX = minThickness;
                maxX = maxThickness;
                break;
            case 2:
                minX = minY = minDiameter;
                maxX = maxY = maxDiameter;
                minZ = minThickness;
                maxZ = maxThickness;
                break;
        }
    }

    @Override
    public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB mask, List list,
        Entity collider) {
        int meta = worldIn.getBlockMetadata(x, y, z);
        double shaftMinX = 0;
        double shaftMinY = 0;
        double shaftMinZ = 0;
        double shaftMaxX = 1;
        double shaftMaxY = 1;
        double shaftMaxZ = 1;
        double cogMinX = 0;
        double cogMinY = 0;
        double cogMinZ = 0;
        double cogMaxX = 1;
        double cogMaxY = 1;
        double cogMaxZ = 1;
        double d = 1d / 16d;
        double minThickness = 6 * d;
        double maxThickness = 1D - 6 * d;
        double minDiameter = isSmallCog() ? 2 * d : 0D;
        double maxDiameter = isSmallCog() ? 1d - 2 * d : 1D;
        switch (meta) {
            default:
            case 0:
                shaftMinX = shaftMinZ = 6 * d;
                shaftMaxX = shaftMaxZ = 1 - 6 * d;
                cogMinX = cogMinZ = minDiameter;
                cogMaxX = cogMaxZ = maxDiameter;
                cogMinY = minThickness;
                cogMaxY = maxThickness;
                break;
            case 1:
                shaftMinY = shaftMinZ = 6 * d;
                shaftMaxY = shaftMaxZ = 1 - 6 * d;
                cogMinY = cogMinZ = minDiameter;
                cogMaxY = cogMaxZ = maxDiameter;
                cogMinX = minThickness;
                cogMaxX = maxThickness;
                break;
            case 2:
                shaftMinX = shaftMinY = 6 * d;
                shaftMaxX = shaftMaxY = 1 - 6 * d;
                cogMinX = cogMinY = minDiameter;
                cogMaxX = cogMaxY = maxDiameter;
                cogMinZ = minThickness;
                cogMaxZ = maxThickness;
                break;
        }
        AxisAlignedBB shaft = AxisAlignedBB
            .getBoundingBox(x + shaftMinX, y + shaftMinY, z + shaftMinZ, x + shaftMaxX, y + shaftMaxY, z + shaftMaxZ);
        AxisAlignedBB cog = AxisAlignedBB
            .getBoundingBox(x + cogMinX, y + cogMinY, z + cogMinZ, x + cogMaxX, y + cogMaxY, z + cogMaxZ);

        if (shaft != null && mask.intersectsWith(shaft)) list.add(shaft);
        if (cog != null && mask.intersectsWith(cog)) list.add(cog);
    }

    public List<AxisAlignedBB> getSelectedBoundingBoxesList(World worldIn, int x, int y, int z) {
        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();

        int meta = worldIn.getBlockMetadata(x, y, z);
        double shaft1MinX = 0;
        double shaft1MinY = 0;
        double shaft1MinZ = 0;
        double shaft1MaxX = 1;
        double shaft1MaxY = 1;
        double shaft1MaxZ = 1;
        double shaft2MinX = 0;
        double shaft2MinY = 0;
        double shaft2MinZ = 0;
        double shaft2MaxX = 1;
        double shaft2MaxY = 1;
        double shaft2MaxZ = 1;
        double cogMinX = 0;
        double cogMinY = 0;
        double cogMinZ = 0;
        double cogMaxX = 1;
        double cogMaxY = 1;
        double cogMaxZ = 1;
        double d = 1d / 16d;
        double minThickness = isSmallCog() ? 6 * d : 5.5d * d;
        double maxThickness = isSmallCog() ? 1D - 6 * d : 1D - 5.5d * d;
        double minDiameter = isSmallCog() ? 2 * d : 0D;
        double maxDiameter = isSmallCog() ? 1d - 2 * d : 1D;
        switch (meta) {
            default:
            case 0:
                shaft1MinX = shaft1MinZ = shaft2MinX = shaft2MinZ = 5 * d;
                shaft1MaxX = shaft1MaxZ = shaft2MaxX = shaft2MaxZ = 1 - 5 * d;
                cogMinX = cogMinZ = minDiameter;
                cogMaxX = cogMaxZ = maxDiameter;
                cogMinY = shaft1MaxY = minThickness;
                cogMaxY = shaft2MinY = maxThickness;
                break;
            case 1:
                shaft1MinY = shaft1MinZ = shaft2MinY = shaft2MinZ = 5 * d;
                shaft1MaxY = shaft1MaxZ = shaft2MaxY = shaft2MaxZ = 1 - 5 * d;
                cogMinY = cogMinZ = minDiameter;
                cogMaxY = cogMaxZ = maxDiameter;
                cogMinX = shaft1MaxX = minThickness;
                cogMaxX = shaft2MinX = maxThickness;
                break;
            case 2:
                shaft1MinX = shaft1MinY = shaft2MinX = shaft2MinY = 5 * d;
                shaft1MaxX = shaft1MaxY = shaft2MaxX = shaft2MaxY = 1 - 5 * d;
                cogMinX = cogMinY = minDiameter;
                cogMaxX = cogMaxY = maxDiameter;
                cogMinZ = shaft1MaxZ = minThickness;
                cogMaxZ = shaft2MinZ = maxThickness;
                break;
        }
        AxisAlignedBB cog = AxisAlignedBB
            .getBoundingBox(x + cogMinX, y + cogMinY, z + cogMinZ, x + cogMaxX, y + cogMaxY, z + cogMaxZ);
        AxisAlignedBB shaft1 = AxisAlignedBB.getBoundingBox(
            x + shaft1MinX,
            y + shaft1MinY,
            z + shaft1MinZ,
            x + shaft1MaxX,
            y + shaft1MaxY,
            z + shaft1MaxZ);
        AxisAlignedBB shaft2 = AxisAlignedBB.getBoundingBox(
            x + shaft2MinX,
            y + shaft2MinY,
            z + shaft2MinZ,
            x + shaft2MaxX,
            y + shaft2MaxY,
            z + shaft2MaxZ);

        list.add(cog);
        list.add(shaft1);
        list.add(shaft2);
        return list;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, int x, int y, int z, Vec3 startVec, Vec3 endVec) {
        this.setBlockBoundsBasedOnState(worldIn, x, y, z);
        startVec = startVec.addVector((double) (-x), (double) (-y), (double) (-z));
        endVec = endVec.addVector((double) (-x), (double) (-y), (double) (-z));
        List<AxisAlignedBB> collisionList = new ArrayList<AxisAlignedBB>();
        this.addCollisionBoxesToList(
            worldIn,
            0,
            0,
            0,
            AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1),
            collisionList,
            null);
        for (AxisAlignedBB aabb : collisionList) {
            Vec3 minXVec = startVec.getIntermediateWithXValue(endVec, aabb.minX);
            Vec3 maxXVec = startVec.getIntermediateWithXValue(endVec, aabb.maxX);
            Vec3 minYVec = startVec.getIntermediateWithYValue(endVec, aabb.minY);
            Vec3 maxYVec = startVec.getIntermediateWithYValue(endVec, aabb.maxY);
            Vec3 minZVec = startVec.getIntermediateWithZValue(endVec, aabb.minZ);
            Vec3 maxZVec = startVec.getIntermediateWithZValue(endVec, aabb.maxZ);

            if (minXVec != null && !(minXVec.yCoord >= aabb.minY && minXVec.yCoord <= aabb.maxY
                && minXVec.zCoord >= aabb.minZ
                && minXVec.zCoord <= aabb.maxZ)) {
                minXVec = null;
            }

            if (maxXVec != null && !(maxXVec.yCoord >= aabb.minY && maxXVec.yCoord <= aabb.maxY
                && maxXVec.zCoord >= aabb.minZ
                && maxXVec.zCoord <= aabb.maxZ)) {
                maxXVec = null;
            }

            if (minYVec != null && !(minYVec.xCoord >= aabb.minX && minYVec.xCoord <= aabb.maxX
                && minYVec.zCoord >= aabb.minZ
                && minYVec.zCoord <= aabb.maxZ)) {
                minYVec = null;
            }

            if (maxYVec != null && !(maxYVec.xCoord >= aabb.minX && maxYVec.xCoord <= aabb.maxX
                && maxYVec.zCoord >= aabb.minZ
                && maxYVec.zCoord <= aabb.maxZ)) {
                maxYVec = null;
            }

            if (minZVec != null && !(minZVec.xCoord >= aabb.minX && minZVec.xCoord <= aabb.maxX
                && minZVec.yCoord >= aabb.minY
                && minZVec.yCoord <= aabb.maxY)) {
                minZVec = null;
            }

            if (maxZVec != null && !(maxZVec.xCoord >= aabb.minX && maxZVec.xCoord <= aabb.maxX
                && maxZVec.yCoord >= aabb.minY
                && maxZVec.yCoord <= aabb.maxY)) {
                maxZVec = null;
            }

            Vec3 resultVec = null;

            if (minXVec != null
                && (resultVec == null || startVec.squareDistanceTo(minXVec) < startVec.squareDistanceTo(resultVec))) {
                resultVec = minXVec;
            }

            if (maxXVec != null
                && (resultVec == null || startVec.squareDistanceTo(maxXVec) < startVec.squareDistanceTo(resultVec))) {
                resultVec = maxXVec;
            }

            if (minYVec != null
                && (resultVec == null || startVec.squareDistanceTo(minYVec) < startVec.squareDistanceTo(resultVec))) {
                resultVec = minYVec;
            }

            if (maxYVec != null
                && (resultVec == null || startVec.squareDistanceTo(maxYVec) < startVec.squareDistanceTo(resultVec))) {
                resultVec = maxYVec;
            }

            if (minZVec != null
                && (resultVec == null || startVec.squareDistanceTo(minZVec) < startVec.squareDistanceTo(resultVec))) {
                resultVec = minZVec;
            }

            if (maxZVec != null
                && (resultVec == null || startVec.squareDistanceTo(maxZVec) < startVec.squareDistanceTo(resultVec))) {
                resultVec = maxZVec;
            }

            if (resultVec != null) {
                byte b0 = -1;

                if (resultVec == minXVec) {
                    b0 = 4;
                }

                if (resultVec == maxXVec) {
                    b0 = 5;
                }

                if (resultVec == minYVec) {
                    b0 = 0;
                }

                if (resultVec == maxYVec) {
                    b0 = 1;
                }

                if (resultVec == minZVec) {
                    b0 = 2;
                }

                if (resultVec == maxZVec) {
                    b0 = 3;
                }

                return new MovingObjectPosition(x, y, z, b0, resultVec.addVector((double) x, (double) y, (double) z));
            }
        }
        return null;
    }

    @Override
    public int getRenderType() {
        return ReCreate.proxy.getCogWheelBlockRenderID();
    }

    @Override
    public boolean canBlockStay(World worldIn, int x, int y, int z) {
        CogWheelBlock block = (CogWheelBlock) worldIn.getBlock(x, y, z);
        int meta = worldIn.getBlockMetadata(x, y, z);
        return isValidCogwheelPosition(ICogWheel.isLargeCog(block), worldIn, x, y, z, block.getAxis(meta));
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        if (player.isSneaking() || player.isPlayerSleeping() || player.isRiding()) return false;

        // TODO
        // ItemStack heldItem = player.getHeldItem();
        // EncasedCogwheelBlock[] encasedBlocks = isLarge
        // ? new EncasedCogwheelBlock[] { AllBlocks.ANDESITE_ENCASED_LARGE_COGWHEEL.get(),
        // AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL.get() }
        // : new EncasedCogwheelBlock[] { AllBlocks.ANDESITE_ENCASED_COGWHEEL.get(),
        // AllBlocks.BRASS_ENCASED_COGWHEEL.get() };
        //
        // for (EncasedCogwheelBlock encasedCog : encasedBlocks) {
        // if (!encasedCog.getCasing()
        // .isIn(heldItem))
        // continue;
        //
        // if (world.isRemote)
        // return true;
        //
        // BlockState encasedState = encasedCog.defaultBlockState()
        // .setValue(AXIS, state.getValue(AXIS));
        //
        // for (Direction d : Iterate.directionsInAxis(state.getValue(AXIS))) {
        // BlockState adjacentState = world.getBlockState(pos.relative(d));
        // if (!(adjacentState.getBlock() instanceof IRotate))
        // continue;
        // IRotate def = (IRotate) adjacentState.getBlock();
        // if (!def.hasShaftTowards(world, pos.relative(d), adjacentState, d.getOpposite()))
        // continue;
        // encasedState =
        // encasedState.cycle(d.getAxisDirection() == AxisDirection.POSITIVE ? EncasedCogwheelBlock.TOP_SHAFT
        // : EncasedCogwheelBlock.BOTTOM_SHAFT);
        // }
        //
        // KineticTileEntity.switchToBlockState(world, pos, encasedState);
        // return true;
        // }

        return false;
    }

    public static boolean isValidCogwheelPosition(boolean large, IBlockAccess worldIn, int x, int y, int z,
        Axis cogAxis) {
        for (Direction facing : Iterate.directions) {
            if (facing.getAxis() == cogAxis) continue;

            ChunkCoordinates facingNormal = facing.getNormal();
            Block block = worldIn.getBlock(x + facingNormal.posX, y + facingNormal.posY, z + facingNormal.posZ);
            int meta = worldIn.getBlockMetadata(x + facingNormal.posX, y + facingNormal.posY, z + facingNormal.posZ);
            // if(large && !block.isReplaceable(worldIn, x + facingNormal.posX, y + facingNormal.posY, z +
            // facingNormal.posZ))
            // return false;
            if (block instanceof RotatedPillarKineticBlock
                && facing.getAxis() == ((RotatedPillarKineticBlock) block).getAxis(meta)) continue;

            if (ICogWheel.isLargeCog(block) || large && ICogWheel.isSmallCog(block)) return false;
        }
        return true;
    }

    protected Axis getAxisForPlacement(World worldIn, int x, int y, int z, EntityPlayer placer) {
        // TODO
        // Block blockBelow = worldIn.getBlock(x, y - 1, z);
        // int metaBelow = worldIn.getBlockMetadata(x, y - 1, z);
        //
        // if (AllBlocks.ROTATION_SPEED_CONTROLLER.has(blockBelow) && isLargeCog())
        // return blockBelow.getValue(SpeedControllerBlock.HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X;

        MovingObjectPosition mop = Raytracer.raytracePlayerView(placer);
        if (mop == null) return null;
        Direction sideNormal = Direction.from3DDataValue(mop.sideHit);
        ChunkCoordinates normalAgainst = sideNormal.getOpposite()
            .getNormal();

        Block blockAgainst = worldIn.getBlock(x + normalAgainst.posX, y + normalAgainst.posY, z + normalAgainst.posZ);
        int metaAgainst = worldIn
            .getBlockMetadata(x + normalAgainst.posX, y + normalAgainst.posY, z + normalAgainst.posZ);
        if (ICogWheel.isSmallCog(blockAgainst)) return ((IRotate) blockAgainst).getAxis(metaAgainst);

        Axis preferredAxis = getPreferredAxis(worldIn, x, y, z);
        return preferredAxis != null ? preferredAxis : sideNormal.getAxis();
    }

    @Override
    public float getParticleTargetRadius() {
        return isLargeCog() ? 1.125f : .65f;
    }

    @Override
    public float getParticleInitialRadius() {
        return isLargeCog() ? 1f : .75f;
    }

    @Override
    public boolean isDedicatedCogWheel() {
        return true;
    }

    @Override
    public Class<CogWheelTileEntity> getTileEntityClass() {
        return CogWheelTileEntity.class;
    }
}
