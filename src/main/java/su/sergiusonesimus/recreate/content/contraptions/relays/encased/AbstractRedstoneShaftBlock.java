package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import su.sergiusonesimus.recreate.content.contraptions.RotationPropagator;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.util.Direction;

public class AbstractRedstoneShaftBlock extends AbstractEncasedShaftBlock {

    private boolean field_150171_a;

    public AbstractRedstoneShaftBlock(Material materialIn) {
        super(materialIn);

        setTickRandomly(true);
    }

    @Override
    public Direction.Axis getAxis(int meta) {
        switch (meta) {
            default:
            case 0, 3:
                return Direction.Axis.Y;
            case 1, 4:
                return Direction.Axis.X;
            case 2, 5:
                return Direction.Axis.Z;
        }
    }

    public void onBlockAdded(World worldIn, int x, int y, int z) {
        if (!worldIn.isRemote) {
            if (this.isPowered(worldIn, x, y, z) && !worldIn.isBlockIndirectlyGettingPowered(x, y, z)) {
                worldIn.scheduleBlockUpdate(x, y, z, this, 4);
            } else if (!this.isPowered(worldIn, x, y, z) && worldIn.isBlockIndirectlyGettingPowered(x, y, z)) {
                worldIn.setBlock(x, y, z, this, worldIn.getBlockMetadata(x, y, z) + 3, 2);
                detachKinetics(worldIn, x, y, z, true);
            }
        }
        super.onBlockAdded(worldIn, x, y, z);
    }

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor Block
     */
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(worldIn, x, y, z, neighbor);

        if (!worldIn.isRemote) {
            if (this.isPowered(worldIn, x, y, z) && !worldIn.isBlockIndirectlyGettingPowered(x, y, z)) {
                worldIn.scheduleBlockUpdate(x, y, z, this, 4);
            } else if (!this.isPowered(worldIn, x, y, z) && worldIn.isBlockIndirectlyGettingPowered(x, y, z)) {
                worldIn.setBlock(x, y, z, this, worldIn.getBlockMetadata(x, y, z) + 3, 2);
                detachKinetics(worldIn, x, y, z, true);
                // this.updateTileEntity(worldIn, x, y, z);
            }
        }
    }

    public void detachKinetics(World worldIn, int x, int y, int z, boolean reAttachNextTick) {
        TileEntity te = worldIn.getTileEntity(x, y, z);
        if (te == null || !(te instanceof KineticTileEntity)) return;
        RotationPropagator.handleRemoved(worldIn, x, y, z, (KineticTileEntity) te);

        // Re-attach next tick
        if (reAttachNextTick) worldIn.scheduleBlockUpdate(x, y, z, this, 4);
    }

    /**
     * Ticks the block if it's been scheduled
     */
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        super.updateTick(worldIn, x, y, z, random);

        TileEntity te = worldIn.getTileEntity(x, y, z);
        if (te == null || !(te instanceof KineticTileEntity kte)) return;
        RotationPropagator.handleAdded(worldIn, x, y, z, kte);

        if (!worldIn.isRemote && this.isPowered(worldIn, x, y, z)
            && !worldIn.isBlockIndirectlyGettingPowered(x, y, z)) {
            this.setPowered(false);
            worldIn.setBlock(x, y, z, this, worldIn.getBlockMetadata(x, y, z) - 3, 2);
        }
    }

    public void setPowered(boolean powered) {
        this.field_150171_a = powered;
    }

    public boolean isPowered(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta > 3) {
            return true;
        }
        return false;
    }

    public boolean isPowered(TileEntity te) {
        World world = te.getWorldObj();
        int x = te.xCoord;
        int y = te.yCoord;
        int z = te.zCoord;
        int meta = world.getBlockMetadata(x, y, z);
        if (meta > 3) {
            return true;
        }
        return false;
    }
}
