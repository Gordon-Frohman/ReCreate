package su.sergiusonesimus.recreate.content.contraptions.wrench;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.AllSounds;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.base.DirectionalKineticBlock;
import su.sergiusonesimus.recreate.content.contraptions.base.GeneratingKineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.HorizontalAxisKineticBlock;
import su.sergiusonesimus.recreate.content.contraptions.base.IAxisAlongFirstCoordinate;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.RotatedPillarKineticBlock;

public interface IWrenchable {

    public default boolean onWrenched(World world, int x, int y, int z, int face, EntityPlayer player) {
        Block block = (Block) this;
        int rotatedMeta = getRotatedBlockMeta(world, x, y, z, face);
        if (!block.canBlockStay(world, x, y, z)) return false;

        KineticTileEntity.switchToBlockState(world, x, y, z, block, rotatedMeta);

        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof GeneratingKineticTileEntity) {
            ((GeneratingKineticTileEntity) te).reActivateSource = true;
        }

        if (world.getBlockMetadata(x, y, z) != rotatedMeta) playRotateSound(world, x, y, z);

        return true;
    }

    public default boolean onSneakWrenched(World world, int x, int y, int z, int face, EntityPlayer player) {
        if (player != null && !player.capabilities.isCreativeMode) world.getBlock(x, y, z)
            .getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0)
            .forEach(itemStack -> { player.inventory.addItemStackToInventory(itemStack); });
        world.setBlockToAir(x, y, z);
        playRemoveSound(world, x, y, z);

        return true;
    }

    public default void playRemoveSound(World world, int x, int y, int z) {
        AllSounds.WRENCH_REMOVE.playOnServer(world, x, y, z, 1, ReCreate.RANDOM.nextFloat() * .5f + .5f);
    }

    public default void playRotateSound(World world, int x, int y, int z) {
        AllSounds.WRENCH_ROTATE.playOnServer(world, x, y, z, 1, ReCreate.RANDOM.nextFloat() + .5f);
    }

    public default Axis getAxis(int meta) {
        return getDirection(meta).getAxis();
    }

    public Direction getDirection(int meta);

    public int getMetaFromDirection(Direction direction);

    public default int getRotatedBlockMeta(World world, int x, int y, int z, int face) {
        Block block = world.getBlock(x, y, z);
        int originalMeta = world.getBlockMetadata(x, y, z);
        int newMeta = originalMeta;

        if (Direction.from3DDataValue(face)
            .getAxis() == Axis.Y && block instanceof HorizontalAxisKineticBlock) {
            return this.getMetaFromDirection(
                this.getDirection(originalMeta)
                    .rotateAround(Axis.Y));
        }

        if (block instanceof RotatedPillarKineticBlock) return this.getMetaFromDirection(
            this.getDirection(originalMeta)
                .rotateAround(
                    Direction.from3DDataValue(face)
                        .getAxis()));

        if (!(block instanceof DirectionalKineticBlock)) return originalMeta;

        Direction originalFacing = this.getDirection(originalMeta);
        Direction rotationDirection = Direction.from3DDataValue(face);
        Axis rotationAxis = rotationDirection.getAxis();

        if (originalFacing.getAxis() == rotationAxis) {
            if (block instanceof IAxisAlongFirstCoordinate aafc) return aafc.cycleMetadata(originalMeta);
            else return originalMeta;
        } else {
            int rotCount = rotationDirection.getAxisDirection() == AxisDirection.POSITIVE ? 1 : 3;
            Direction newFacing = this.getDirection(newMeta);
            for (int i = 0; i < rotCount; i++) newFacing = newFacing.rotateAround(rotationAxis);
            newMeta = this.getMetaFromDirection(newFacing);
            if (block instanceof IAxisAlongFirstCoordinate aafc) {
                boolean axisAlongFirst = aafc.isAxisAlongFirstCoordinate(originalMeta);
                Axis originalAxis = ((IWrenchable) block).getAxis(originalMeta);
                if (originalAxis == rotationAxis) {
                    switch (newFacing.getAxis()) {
                        case X:
                            axisAlongFirst = originalAxis == Axis.Y;
                            break;
                        case Y:
                        case Z:
                            axisAlongFirst = originalAxis == Axis.X;
                            break;
                    }
                } else {
                    switch (newFacing.getAxis()) {
                        case X:
                            axisAlongFirst = originalFacing.getAxis() == Axis.Y;
                            break;
                        case Y:
                        case Z:
                            axisAlongFirst = originalFacing.getAxis() == Axis.X;
                            break;
                    }
                }
                newMeta = aafc.getMetadata(newFacing, axisAlongFirst);
            }
        }
        return newMeta;
    }
}
