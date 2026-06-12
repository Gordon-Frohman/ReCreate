package su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.AbstractShaftBlock;
import su.sergiusonesimus.recreate.foundation.block.ITE;
import su.sergiusonesimus.recreate.foundation.utility.placement.IPlacementHelper;
import su.sergiusonesimus.recreate.foundation.utility.placement.PlacementHelpers;
import su.sergiusonesimus.recreate.foundation.utility.placement.util.PoleHelper;

public class ShaftBlock extends AbstractShaftBlock implements ITE<ShaftTileEntity> {

    public static IIcon shaftTop;
    public static IIcon shaftSide;

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public ShaftBlock(Material materialIn) {
        super(materialIn);
        this.setHardness(1.5F);
        this.setResistance(10.0F);
        this.setStepSound(soundTypePiston);
    }

    public static boolean isShaft(Block block) {
        return block == AllBlocks.shaft;
    }

    @Override
    public float getParticleTargetRadius() {
        return .35f;
    }

    @Override
    public float getParticleInitialRadius() {
        return .125f;
    }

    @Override
    public void setBlockBoundsForItemRender() {
        float f = 6f / 16f;
        this.setBlockBounds(f, 0.0F, f, 1 - f, 1.0F, 1 - f);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        if (player.isSneaking() || player.isPlayerSleeping() || player.isRiding()) return false;

        ItemStack heldItem = player.getHeldItem();
        int meta = worldIn.getBlockMetadata(x, y, z);
        // TODO
        // for (EncasedShaftBlock encasedShaft : new EncasedShaftBlock[] { AllBlocks.ANDESITE_ENCASED_SHAFT.get(),
        // AllBlocks.BRASS_ENCASED_SHAFT.get() }) {
        //
        // if (!encasedShaft.getCasing()
        // .isIn(heldItem))
        // continue;
        //
        // if (worldIn.isRemote)
        // return true;
        //
        // //TODO
        // //AllTriggers.triggerFor(AllTriggers.CASING_SHAFT, player);
        // KineticTileEntity.switchToBlockState(worldIn, x, y, z, this, meta);
        // return true;
        // }

        IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
        if (helper.matchesItem(heldItem)) {
            MovingObjectPosition ray = Minecraft.getMinecraft().objectMouseOver;
            return helper.getOffset(player, worldIn, this, meta, x, y, z, ray)
                .placeInWorld(worldIn, (ItemBlock) heldItem.getItem(), player, ray);
        }

        return false;
    }

    @Override
    public boolean isToolEffective(String type, int metadata) {
        return type.equals("pickaxe") || type.equals("axe");
    }

    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        if (worldIn == null) return;
        int meta = worldIn.getBlockMetadata(x, y, z);
        minX = 0;
        minY = 0;
        minZ = 0;
        maxX = 1;
        maxY = 1;
        maxZ = 1;
        double d = 6d / 16d;
        switch (meta) {
            default:
            case 0:
                minX = minZ = d;
                maxX = maxZ = 1 - d;
                break;
            case 1:
                minY = minZ = d;
                maxY = maxZ = 1 - d;
                break;
            case 2:
                minX = minY = d;
                maxX = maxY = 1 - d;
                break;
        }
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        int meta = worldIn.getBlockMetadata(x, y, z);
        double minX = 0;
        double minY = 0;
        double minZ = 0;
        double maxX = 1;
        double maxY = 1;
        double maxZ = 1;
        double d = 6d / 16d;
        switch (meta) {
            default:
            case 0:
                minX = minZ = d;
                maxX = maxZ = 1 - d;
                break;
            case 1:
                minY = minZ = d;
                maxY = maxZ = 1 - d;
                break;
            case 2:
                minX = minY = d;
                maxX = maxY = 1 - d;
                break;
        }
        return AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ);
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        int meta = worldIn.getBlockMetadata(x, y, z);
        double minX = 0;
        double minY = 0;
        double minZ = 0;
        double maxX = 1;
        double maxY = 1;
        double maxZ = 1;
        double d = 5d / 16d;
        switch (meta) {
            default:
            case 0:
                minX = minZ = d;
                maxX = maxZ = 1 - d;
                break;
            case 1:
                minY = minZ = d;
                maxY = maxZ = 1 - d;
                break;
            case 2:
                minX = minY = d;
                maxX = maxY = 1 - d;
                break;
        }
        return AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ);
    }

    @Override
    public int getRenderType() {
        return ReCreate.proxy.getShaftBlockRenderID();
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return (side == 1 || side == 0) ? shaftTop : shaftSide;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        ShaftBlock.shaftTop = iconRegister.registerIcon(ReCreate.ID + ":axis_top");
        ShaftBlock.shaftSide = iconRegister.registerIcon(ReCreate.ID + ":axis");
    }

    @Override
    public Class<ShaftTileEntity> getTileEntityClass() {
        return ShaftTileEntity.class;
    }

    private static class PlacementHelper extends PoleHelper<Axis> {
        // used for extending a shaft in its axis, like the piston poles. works with shafts and cogs

        private PlacementHelper() {
            super(
                (block, meta) -> block instanceof AbstractShaftBlock,
                (block, meta) -> ((AbstractShaftBlock) block).getAxis(meta));
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return i -> i != null && i.getItem() instanceof ItemBlock
                && ((ItemBlock) i.getItem()).field_150939_a instanceof AbstractShaftBlock;
        }

        @Override
        public BiPredicate<Block, Integer> getBlockPredicate() {
            return (block, meta) -> block instanceof ShaftBlock;
        }
    }
}
