package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.RotationPropagator;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.gearbox.GearboxBlock;
import su.sergiusonesimus.recreate.foundation.block.ITE;

@ParametersAreNonnullByDefault
public class GearshiftBlock extends AbstractEncasedShaftBlock implements ITE<SplitShaftTileEntity> {

    public final boolean isPowered;

    public GearshiftBlock(Material materialIn, boolean powered) {
        super(materialIn);
        this.setHardness(1.5F);
        this.setResistance(10.0F);
        this.setStepSound(soundTypeWood);
        this.setBlockTextureName("planks_spruce");
        this.isPowered = powered;
        setTickRandomly(true);
    }

    protected GearshiftBlock getBlockPowered() {
        return AllBlocks.powered_gearshift;
    }

    protected GearshiftBlock getBlockUnpowered() {
        return AllBlocks.unpowered_gearshift;
    }

    public void onBlockAdded(World worldIn, int x, int y, int z) {
        if (worldIn.isBlockIndirectlyGettingPowered(x, y, z)) {
            worldIn.scheduleBlockUpdate(x, y, z, this, 1);
        }
        super.onBlockAdded(worldIn, x, y, z);
    }

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor Block
     */
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        if (worldIn.isRemote) return;

        boolean isNowPowered = worldIn.isBlockIndirectlyGettingPowered(x, y, z);
        if (this.isPowered != isNowPowered) {
            detachKinetics(worldIn, x, y, z, true);
            worldIn.setBlock(
                x,
                y,
                z,
                isNowPowered ? this.getBlockPowered() : this.getBlockUnpowered(),
                worldIn.getBlockMetadata(x, y, z),
                2);
        }
    }

    public void detachKinetics(World worldIn, int x, int y, int z, boolean reAttachNextTick) {
        TileEntity te = worldIn.getTileEntity(x, y, z);
        if (te == null || !(te instanceof KineticTileEntity kte)) return;
        RotationPropagator.handleRemoved(worldIn, x, y, z, kte);

        // Re-attach next tick
        if (reAttachNextTick) worldIn.scheduleBlockUpdate(x, y, z, this, 4);
    }

    /**
     * Ticks the block if it's been scheduled
     */
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        boolean isNowPowered = worldIn.isBlockIndirectlyGettingPowered(x, y, z);
        if (this.isPowered != isNowPowered) {
            worldIn.setBlock(
                x,
                y,
                z,
                isNowPowered ? this.getBlockPowered() : this.getBlockUnpowered(),
                worldIn.getBlockMetadata(x, y, z),
                2);
        }

        TileEntity te = worldIn.getTileEntity(x, y, z);
        if (te == null || !(te instanceof KineticTileEntity kte)) return;
        RotationPropagator.handleAdded(worldIn, x, y, z, kte);
    }

    public boolean isAssociatedBlock(Block other) {
        return other == this.getBlockPowered() || other == this.getBlockUnpowered();
    }

    @Override
    public Class<? extends SplitShaftTileEntity> getTileEntityClass() {
        return GearshiftTileEntity.class;
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
    public int getRenderType() {
        return ReCreate.proxy.getSplitShaftBlockRenderID();
    }

    public Item getItemDropped(int meta, Random random, int fortune) {
        return Item.getItemFromBlock(AllBlocks.unpowered_gearshift);
    }

    /**
     * Gets an item for the block being called on. Args: world, x, y, z
     */
    @SideOnly(Side.CLIENT)
    public Item getItem(World worldIn, int x, int y, int z) {
        return Item.getItemFromBlock(AllBlocks.unpowered_gearshift);
    }

    /**
     * Returns an item stack containing a single instance of the current block type. 'i' is the block's subtype/damage
     * and is ignored for blocks which do not support subtypes. Blocks which cannot be harvested should return null.
     */
    protected ItemStack createStackedBlock(int meta) {
        return new ItemStack(AllBlocks.unpowered_gearshift);
    }

    /**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        Axis axis = this.getAxis(meta);
        Direction dir = Direction.from3DDataValue(side);
        if (dir.getAxis() == axis) return GearboxBlock.gearboxSide;
        else return this.blockIcon;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = reg.registerIcon(ReCreate.ID + ":gearshift_" + (this.isPowered ? "on" : "off"));
    }

    /**
     * Checks if the block is a solid face on the given side, used by placement logic.
     *
     * @param world The current world
     * @param x     X Position
     * @param y     Y position
     * @param z     Z position
     * @param side  The side to check
     * @return True if the block is solid on the specified side.
     */
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        ForgeDirection dir = this.getDirection(world.getBlockMetadata(x, y, z))
            .toForgeDirection();
        return dir != side && dir.getOpposite() != side;
    }
}
