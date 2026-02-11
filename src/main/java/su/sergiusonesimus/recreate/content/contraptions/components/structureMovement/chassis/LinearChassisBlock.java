package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.ReCreate;
import team.chisel.ctmlib.ICTMBlock;

public class LinearChassisBlock extends AbstractChassisBlock implements ICTMBlock<LinearChassisSubmapManager> {

    @SideOnly(Side.CLIENT)
    private LinearChassisSubmapManager manager;

    public LinearChassisBlock(Material material) {
        super(material);
    }

    private static Direction placeSide;

    @Override
    public int onBlockPlaced(World worldIn, int x, int y, int z, int side, float subX, float subY, float subZ,
        int meta) {
        placeSide = Direction.from3DDataValue(side);
        return super.onBlockPlaced(worldIn, x, y, z, side, subX, subY, subZ, meta);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        int type = itemIn.getItemDamage() == 1 ? 1 : 0;
        ChunkCoordinates normal = placeSide.getOpposite()
            .getNormal();
        Block neighbourBlock = worldIn.getBlock(x + normal.posX, y + normal.posY, z + normal.posZ);
        int neighbourMeta = worldIn.getBlockMetadata(x + normal.posX, y + normal.posY, z + normal.posZ);

        if (placer == null || !placer.isSneaking()) {
            if (isChassis(neighbourBlock)) {
                worldIn.setBlockMetadataWithNotify(x, y, z, neighbourMeta & 12 | type, 3);
                return;
            }
            worldIn.setBlockMetadataWithNotify(
                x,
                y,
                z,
                this.getMetaFromDirection(Direction.getNearestLookingDirection(placer)) + type,
                3);
            return;
        }
        worldIn.setBlockMetadataWithNotify(x, y, z, worldIn.getBlockMetadata(x, y, z) + type, 3);
    }

    @Override
    public Boolean getGlueableSide(IBlockAccess worldIn, int x, int y, int z, Direction face) {
        int meta = worldIn.getBlockMetadata(x, y, z);
        if (face.getAxis() != getAxis(meta)) return null;
        ChassisTileEntity te = (ChassisTileEntity) worldIn.getTileEntity(x, y, z);
        return te == null ? null : te.getGlueableSide(face);
    }

    @Override
    public void setGlueableSide(IBlockAccess worldIn, int x, int y, int z, Direction face, boolean value) {
        int meta = worldIn.getBlockMetadata(x, y, z);
        if (face.getAxis() != getAxis(meta)) return;
        ((ChassisTileEntity) worldIn.getTileEntity(x, y, z)).setGlueableSide(face, value);
    }

    @Override
    protected boolean glueAllowedOnSide(IBlockAccess world, int x, int y, int z, Direction side) {
        ChunkCoordinates normal = side.getNormal();
        int meta = world.getBlockMetadata(x, y, z);
        Block other = world.getBlock(x + normal.posX, y + normal.posY, z + normal.posZ);
        int otherMeta = world.getBlockMetadata(x + normal.posX, y + normal.posY, z + normal.posZ);
        return !sameKind(other, otherMeta, this, meta)
            || getAxis(meta) != ((LinearChassisBlock) other).getAxis(otherMeta);
    }

    public static boolean isChassis(Block block) {
        return block == AllBlocks.linear_chassis;
    }

    public static boolean sameKind(Block block1, int meta1, Block block2, int meta2) {
        return block1 == block2 && (meta1 % 4) == (meta2 % 4);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return manager.getIcon(world, x, y, z, side);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return manager.getIcon(side, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return ReCreate.proxy.getLinearChassisBlockRenderID();
    }

    @Override
    protected IIcon getSideIcon(int meta) {
        return manager.getIcon(2, meta);
    }

    @Override
    protected IIcon getTopIcon(int meta) {
        return manager.getIcon(0, meta);
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        manager = new LinearChassisSubmapManager();
        manager.registerIcons(ReCreate.ID, this, reg);
    }

    public int damageDropped(int meta) {
        return meta & 1;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
        list.add(new ItemStack(itemIn, 1, 0));
        list.add(new ItemStack(itemIn, 1, 1));
    }

    @Override
    public LinearChassisSubmapManager getManager(IBlockAccess world, int x, int y, int z, int meta) {
        return manager;
    }

    @Override
    public LinearChassisSubmapManager getManager(int meta) {
        return manager;
    }

}
