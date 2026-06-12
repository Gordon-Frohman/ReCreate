package su.sergiusonesimus.recreate.content.contraptions.components.waterwheel;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.base.DirectionalKineticBlock;
import su.sergiusonesimus.recreate.content.contraptions.base.GeneratingKineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticBlock;
import su.sergiusonesimus.recreate.foundation.block.ITE;
import su.sergiusonesimus.recreate.foundation.config.CKinetics;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
import su.sergiusonesimus.recreate.util.FluidHelper;
import su.sergiusonesimus.recreate.util.VecHelper;

public class WaterWheelBlock extends DirectionalKineticBlock implements ITE<WaterWheelTileEntity> {

    public WaterWheelBlock(Material materialIn) {
        super(materialIn);
        this.setHardness(2.0F);
        this.setResistance(5.0F);
        this.setStepSound(soundTypeWood);
        this.setBlockTextureName("planks_spruce");
    }

    @Override
    public boolean isToolEffective(String type, int metadata) {
        return type.equals("pickaxe") || type.equals("axe");
    }

    /**
     * Can this block stay at this position. Similar to canPlaceBlockAt except gets checked often with plants.
     */
    public boolean canBlockStay(World worldIn, int x, int y, int z) {
        for (Direction direction : Iterate.directions) {
            ChunkCoordinates normal = direction.getNormal();
            Block neighbourBlock = worldIn.getBlock(x + normal.posX, y + normal.posY, z + normal.posZ);
            if (!(neighbourBlock instanceof WaterWheelBlock waterWheelBlock)) continue;
            int neighbourMeta = worldIn.getBlockMetadata(x + normal.posX, y + normal.posY, z + normal.posZ);
            if (waterWheelBlock.getAxis(neighbourMeta) != this.getAxis(worldIn.getBlockMetadata(x, y, z))) return false;
        }

        return true;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
        // TODO
        // if (world instanceof WrappedWorld) return;
        updateAllSides(world, x, y, z);
    }

    public void onBlockAdded(World worldIn, int x, int y, int z) {
        super.onBlockAdded(worldIn, x, y, z);
        updateAllSides(worldIn, x, y, z);
    }

    public void updateAllSides(IBlockAccess world, int x, int y, int z) {
        for (Direction d : Iterate.directions) updateFlowAt(world, x, y, z, d);
        updateWheelSpeed(world, x, y, z);
    }

    private void updateFlowAt(IBlockAccess world, int x, int y, int z, Direction side) {
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

        // TODO
        // if (side.getAxis()
        // .isHorizontal()) {
        // BlockState adjacentBlock = world.getBlockState(pos.relative(side));
        // if (adjacentBlock.getBlock() == Blocks.BUBBLE_COLUMN)
        // vec = new Vec3(0, adjacentBlock.getValue(BubbleColumnBlock.DRAG_DOWN) ? -1 : 1, 0);
        // }

        vec = VecHelper.scale(
            vec,
            side.getAxisDirection()
                .getStep());
        vec = Vec3.createVectorHelper(Math.signum(vec.xCoord), Math.signum(vec.yCoord), Math.signum(vec.zCoord));
        Vec3 flow = vec;

        withTileEntityDo(world, x, y, z, te -> {
            double flowStrength = 0;

            if (block.getAxis(meta) == Direction.Axis.Z) {
                if (side.getAxis() == Direction.Axis.Y)
                    flowStrength = flow.xCoord > 0 ^ !clockwise ? -flow.xCoord * clockwiseMultiplier : -flow.xCoord;
                if (side.getAxis() == Direction.Axis.X)
                    flowStrength = flow.yCoord < 0 ^ !clockwise ? flow.yCoord * clockwiseMultiplier : flow.yCoord;
            }

            if (block.getAxis(meta) == Direction.Axis.X) {
                if (side.getAxis() == Direction.Axis.Y)
                    flowStrength = flow.zCoord < 0 ^ !clockwise ? flow.zCoord * clockwiseMultiplier : flow.zCoord;
                if (side.getAxis() == Direction.Axis.Z)
                    flowStrength = flow.yCoord > 0 ^ !clockwise ? -flow.yCoord * clockwiseMultiplier : -flow.yCoord;
            }

            if (block.getAxis(meta) == Direction.Axis.Y) {
                if (side.getAxis() == Direction.Axis.Z)
                    flowStrength = flow.xCoord < 0 ^ !clockwise ? flow.xCoord * clockwiseMultiplier : flow.xCoord;
                if (side.getAxis() == Direction.Axis.X)
                    flowStrength = flow.zCoord > 0 ^ !clockwise ? -flow.zCoord * clockwiseMultiplier : -flow.zCoord;
            }

            // TODO
            // if (te.getSpeed() == 0 && flowStrength != 0 && !world.isClientSide()) {
            // AllTriggers.triggerForNearbyPlayers(AllTriggers.WATER_WHEEL, world, pos, 5);
            // if (FluidHelper.isLava(fluid.getType()))
            // AllTriggers.triggerForNearbyPlayers(AllTriggers.LAVA_WHEEL, world, pos, 5);
            // if (fluid.getType()
            // .isSame(AllFluids.CHOCOLATE.get()))
            // AllTriggers.triggerForNearbyPlayers(AllTriggers.CHOCOLATE_WHEEL, world, pos, 5);
            // }

            Integer flowModifier = CKinetics.waterWheelFlowSpeed;
            te.setFlow(side, (float) ((flowStrength * flowModifier / (2f * 2f))));
        });

    }

    public void updateWheelSpeed(IBlockAccess world, int x, int y, int z) {
        withTileEntityDo(world, x, y, z, GeneratingKineticTileEntity::updateGeneratedRotation);
    }

    /**
     * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z, side, hitX, hitY, hitZ, block metadata
     */
    public int onBlockPlaced(World worldIn, int x, int y, int z, int side, float subX, float subY, float subZ,
        int meta) {
        int localMeta = this.getMetaFromDirection(Direction.from3DDataValue(side));
        worldIn.setBlockMetadataWithNotify(x, y, z, localMeta, 2 | 4);
        return localMeta;
    }

    /**
     * Called when the block is placed in the world.
     */
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        int meta = worldIn.getBlockMetadata(x, y, z);
        Direction facing = this.getDirection(meta);
        if (placer != null) {
            ChunkCoordinates neighbourDir = facing.getOpposite()
                .getNormal();
            Vec3 lookVec = placer.getLookVec();
            Direction horizontalFacing = Direction.getNearest(lookVec.xCoord, 0, lookVec.zCoord);

            Block placedOn = worldIn.getBlock(x + neighbourDir.posX, y + neighbourDir.posY, z + neighbourDir.posZ);
            if (placedOn instanceof WaterWheelBlock) {
                worldIn.setBlockMetadataWithNotify(
                    worldIn.getBlockMetadata(x + neighbourDir.posX, y + neighbourDir.posY, z + neighbourDir.posZ),
                    x,
                    y,
                    z,
                    2);
                return;
            }

            boolean sneaking = placer != null && placer.isSneaking();
            double tolerance = 0.985;

            // Checking if water wheel facing up can stay here
            worldIn.setBlockMetadataWithNotify(x, y, z, this.getMetaFromDirection(Direction.UP), 2 | 4);
            if (!this.canBlockStay(worldIn, x, y, z)) facing = horizontalFacing;
            else if (Vec3.createVectorHelper(0, -1, 0)
                .dotProduct(lookVec.normalize()) > tolerance) facing = Direction.DOWN;
            else if (Vec3.createVectorHelper(0, 1, 0)
                .dotProduct(lookVec.normalize()) > tolerance) facing = Direction.UP;
            else facing = horizontalFacing;

            if (sneaking) facing = facing.getOpposite();
        }

        worldIn.setBlockMetadataWithNotify(x, y, z, this.getMetaFromDirection(facing), 2);
    }

    @Override
    public boolean hasShaftTowards(IBlockAccess world, int x, int y, int z, Direction face) {
        return face.getAxis() == this.getAxis(world.getBlockMetadata(x, y, z));
    }

    @Override
    public float getParticleTargetRadius() {
        return 1.125f;
    }

    @Override
    public float getParticleInitialRadius() {
        return 1f;
    }

    @Override
    public boolean hideStressImpact() {
        return true;
    }

    @Override
    public Class<WaterWheelTileEntity> getTileEntityClass() {
        return WaterWheelTileEntity.class;
    }

    @Override
    public int getRenderType() {
        return ReCreate.proxy.getWaterWheelBlockRenderID();
    }
}
