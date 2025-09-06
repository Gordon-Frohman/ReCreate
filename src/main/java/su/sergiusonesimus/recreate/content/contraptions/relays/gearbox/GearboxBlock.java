package su.sergiusonesimus.recreate.content.contraptions.relays.gearbox;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.base.RotatedPillarKineticBlock;
import su.sergiusonesimus.recreate.foundation.block.ITE;

public class GearboxBlock extends RotatedPillarKineticBlock implements ITE<GearboxTileEntity> {

    public static IIcon gearboxTop;
    public static IIcon gearboxSide;

    public GearboxBlock(Material materialIn) {
        super(materialIn);
        this.setHardness(1.5F);
        this.setResistance(10.0F);
        this.setStepSound(soundTypeWood);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        this.setBlockTextureName("planks_spruce");

    }

    @Override
    public Class<GearboxTileEntity> getTileEntityClass() {
        return GearboxTileEntity.class;
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
        return face.getAxis() != this.getAxis(world.getBlockMetadata(x, y, z));
    }

    @Override
    public int getRenderType() {
        return ReCreate.proxy.getGearboxBlockRenderID();
    }

    /**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        Axis axis = this.getAxis(meta);
        Direction dir = Direction.from3DDataValue(side);
        if (dir.getAxis() == axis) return gearboxTop;
        else return gearboxSide;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        gearboxTop = reg.registerIcon(ReCreate.ID + ":gearbox_top");
        gearboxSide = reg.registerIcon(ReCreate.ID + ":gearbox");
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
        return dir == side || dir.getOpposite() == side;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        Axis preferredAxis = Direction.getNearestLookingDirection(placer)
            .getAxis();
        int meta = worldIn.getBlockMetadata(x, y, z);
        if (preferredAxis != null && (placer == null || !placer.isSneaking())) {
            meta = this.getMetaFromAxis(preferredAxis);
        }
        worldIn.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }
}
