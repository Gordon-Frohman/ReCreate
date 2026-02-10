package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.ReCreate;

public class RadialChassisBlock extends AbstractChassisBlock {

    public static IIcon chassisEnd;
    public static IIcon chassisSide;
    public static IIcon chassisSideSticky;

    public RadialChassisBlock(Material material) {
        super(material);
    }

    @Override
    public Boolean getGlueableSide(IBlockAccess worldIn, int x, int y, int z, Direction face) {
        int meta = worldIn.getBlockMetadata(x, y, z);
        if (face.getAxis() == getAxis(meta)) return null;
        return ((ChassisTileEntity) worldIn.getTileEntity(x, y, z)).getGlueableSide(face);
    }

    @Override
    public void setGlueableSide(IBlockAccess worldIn, int x, int y, int z, Direction face, boolean value) {
        int meta = worldIn.getBlockMetadata(x, y, z);
        if (face.getAxis() == getAxis(meta)) return;
        ((ChassisTileEntity) worldIn.getTileEntity(x, y, z)).setGlueableSide(face, value);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess worldIn, int x, int y, int z, int side) {
        Boolean stickySide = this.getGlueableSide(worldIn, x, y, z, Direction.from3DDataValue(side));
        if (stickySide != null && stickySide) return chassisSideSticky;
        return super.getIcon(worldIn, x, y, z, side);
    }

    @Override
    protected IIcon getSideIcon(int meta) {
        return chassisSide;
    }

    @Override
    protected IIcon getTopIcon(int meta) {
        return chassisEnd;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        chassisEnd = reg.registerIcon(ReCreate.ID + ":radial_chassis_end");
        chassisSide = reg.registerIcon(ReCreate.ID + ":radial_chassis_side");
        chassisSideSticky = reg.registerIcon(ReCreate.ID + ":radial_chassis_side_sticky");
    }

}
