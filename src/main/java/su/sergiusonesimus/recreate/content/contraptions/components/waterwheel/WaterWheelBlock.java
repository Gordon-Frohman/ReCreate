package su.sergiusonesimus.recreate.content.contraptions.components.waterwheel;

import net.minecraft.block.material.Material;
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
import su.sergiusonesimus.recreate.foundation.utility.FluidHelper;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
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
    public Class<WaterWheelTileEntity> getTileEntityClass() {
        return WaterWheelTileEntity.class;
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
    public boolean hasShaftTowards(IBlockAccess world, int x, int y, int z, Direction face) {
        return face.getAxis() == this.getAxis(world.getBlockMetadata(x, y, z));
    }

    public void updateAllSides(World world, int x, int y, int z) {
        for (Direction d : Iterate.directions) updateFlowAt(world, x, y, z, d);
        updateWheelSpeed(world, x, y, z);
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
            te.setFlow(side, (float) ((flowStrength * flowModifier / (2f * 2f))));
        });

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

    public void updateWheelSpeed(World world, int x, int y, int z) {
        withTileEntityDo(world, x, y, z, GeneratingKineticTileEntity::updateGeneratedRotation);
    }

    @Override
    public int getRenderType() {
        return ReCreate.proxy.getWaterWheelBlockRenderID();
    }
}
