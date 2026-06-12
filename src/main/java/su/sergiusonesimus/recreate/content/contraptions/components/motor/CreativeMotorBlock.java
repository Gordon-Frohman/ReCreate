package su.sergiusonesimus.recreate.content.contraptions.components.motor;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.base.DirectionalKineticBlock;
import su.sergiusonesimus.recreate.foundation.block.ITE;

public class CreativeMotorBlock extends DirectionalKineticBlock implements ITE<CreativeMotorTileEntity> {

    public static IIcon creativeCasing;

    public CreativeMotorBlock(Material materialIn) {
        super(materialIn);
        this.setHardness(1.5F);
        this.setResistance(10.0F);
        this.setStepSound(soundTypePiston);
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
        double d = 1d / 16d;
        switch (meta) {
            default:
            case 0:
                minX = minZ = 3 * d;
                minY = 2 * d;
                maxX = maxZ = 1 - 3 * d;
                maxY = 1.0D;
                break;
            case 1:
                minX = minZ = 3 * d;
                minY = 0.0D;
                maxX = maxZ = 1 - 3 * d;
                maxY = 1.0D - 2 * d;
                break;
            case 2:
                minX = minY = 3 * d;
                minZ = 2 * d;
                maxX = maxY = 1 - 3 * d;
                maxZ = 1.0D;
                break;
            case 3:
                minX = minY = 3 * d;
                minZ = 0.0D;
                maxX = maxY = 1 - 3 * d;
                maxZ = 1.0D - 2 * d;
                break;
            case 4:
                minZ = minY = 3 * d;
                minX = 2 * d;
                maxZ = maxY = 1 - 3 * d;
                maxX = 1.0D;
                break;
            case 5:
                minZ = minY = 3 * d;
                minX = 0.0D;
                maxZ = maxY = 1 - 3 * d;
                maxX = 1.0D - 2 * d;
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
        double d = 1d / 16d;
        switch (meta) {
            default:
            case 0:
                minX = minZ = 3 * d;
                minY = 2 * d;
                maxX = maxZ = 1 - 3 * d;
                maxY = 1.0D;
                break;
            case 1:
                minX = minZ = 3 * d;
                minY = 0.0D;
                maxX = maxZ = 1 - 3 * d;
                maxY = 1.0D - 2 * d;
                break;
            case 2:
                minX = minY = 3 * d;
                minZ = 2 * d;
                maxX = maxY = 1 - 3 * d;
                maxZ = 1.0D;
                break;
            case 3:
                minX = minY = 3 * d;
                minZ = 0.0D;
                maxX = maxY = 1 - 3 * d;
                maxZ = 1.0D - 2 * d;
                break;
            case 4:
                minZ = minY = 3 * d;
                minX = 2 * d;
                maxZ = maxY = 1 - 3 * d;
                maxX = 1.0D;
                break;
            case 5:
                minZ = minY = 3 * d;
                minX = 0.0D;
                maxZ = maxY = 1 - 3 * d;
                maxX = 1.0D - 2 * d;
                break;
        }
        return AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ);
    }

    @Override
    public Direction getPreferredFacing(World world, int x, int y, int z) {
        Direction result = super.getPreferredFacing(world, x, y, z);
        return result == null ? null : result.getOpposite();
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
        return ReCreate.proxy.getCreativeMotorBlockRenderID();
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return creativeCasing;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        CreativeMotorBlock.creativeCasing = iconRegister.registerIcon(ReCreate.ID + ":creative_casing");
    }

    @Override
    public Class<CreativeMotorTileEntity> getTileEntityClass() {
        return CreativeMotorTileEntity.class;
    }

    // IRotate:

    @Override
    public boolean hasShaftTowards(IBlockAccess world, int x, int y, int z, Direction face) {
        return face == this.getDirection(world.getBlockMetadata(x, y, z));
    }

    @Override
    public boolean hideStressImpact() {
        return true;
    }

}
