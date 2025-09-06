package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import java.util.Random;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.ReCreate;

@ParametersAreNonnullByDefault
public class ClutchBlock extends GearshiftBlock {

    public ClutchBlock(Material materialIn, boolean powered) {
        super(materialIn, powered);
    }

    protected GearshiftBlock getBlockPowered() {
        return AllBlocks.powered_clutch;
    }

    protected GearshiftBlock getBlockUnpowered() {
        return AllBlocks.unpowered_clutch;
    }

    @Override
    public Class<? extends SplitShaftTileEntity> getTileEntityClass() {
        return ClutchTileEntity.class;
    }

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor Block
     */
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        if (worldIn.isRemote) return;

        boolean isNowPowered = worldIn.isBlockIndirectlyGettingPowered(x, y, z);
        if (this.isPowered != isNowPowered) {
            worldIn.setBlock(
                x,
                y,
                z,
                isNowPowered ? this.getBlockPowered() : this.getBlockUnpowered(),
                worldIn.getBlockMetadata(x, y, z),
                2);
            detachKinetics(worldIn, x, y, z, this.isPowered);
        }
    }

    @Override
    public int getRenderType() {
        return ReCreate.proxy.getSplitShaftBlockRenderID();
    }

    public Item getItemDropped(int meta, Random random, int fortune) {
        return Item.getItemFromBlock(AllBlocks.unpowered_clutch);
    }

    /**
     * Gets an item for the block being called on. Args: world, x, y, z
     */
    @SideOnly(Side.CLIENT)
    public Item getItem(World worldIn, int x, int y, int z) {
        return Item.getItemFromBlock(AllBlocks.unpowered_clutch);
    }

    /**
     * Returns an item stack containing a single instance of the current block type. 'i' is the block's subtype/damage
     * and is ignored for blocks which do not support subtypes. Blocks which cannot be harvested should return null.
     */
    protected ItemStack createStackedBlock(int meta) {
        return new ItemStack(AllBlocks.unpowered_clutch);
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = reg.registerIcon(ReCreate.ID + ":clutch_" + (this.isPowered ? "on" : "off"));
    }
}
