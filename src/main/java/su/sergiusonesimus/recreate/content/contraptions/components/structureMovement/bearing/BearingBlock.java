package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.base.DirectionalKineticBlock;

public abstract class BearingBlock extends DirectionalKineticBlock {

    public static IIcon bearingTop;
    public static IIcon bearingSide;
    public static IIcon bearingBottom;

    public BearingBlock(Material materialIn) {
        super(materialIn);
        this.setHardness(2.0F);
        this.setResistance(5.0F);
        this.setStepSound(soundTypeWood);
    }

    @Override
    public boolean hasShaftTowards(IBlockAccess world, int x, int y, int z, Direction face) {
        return face == getDirection(world.getBlockMetadata(x, y, z)).getOpposite();
    }

    @Override
    public boolean showCapacityWithAnnotation() {
        return true;
    }

    @Override
    public boolean onWrenched(World world, int x, int y, int z, int face, EntityPlayer player) {
        boolean result = super.onWrenched(world, x, y, z, face, player);
        if (!world.isRemote && result) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof MechanicalBearingTileEntity) {
                ((MechanicalBearingTileEntity) te).disassemble();
            }
        }
        return result;
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
        return ReCreate.proxy.getBearingBlockRenderID();
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        switch (side) {
            case 0:
                return bearingBottom;
            case 1:
                return bearingTop;
            default:
                return bearingSide;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        BearingBlock.bearingTop = iconRegister.registerIcon(ReCreate.ID + ":bearing_top");
        BearingBlock.bearingSide = iconRegister.registerIcon(ReCreate.ID + ":mechanical_bearing_side");
        BearingBlock.bearingBottom = iconRegister.registerIcon(ReCreate.ID + ":gearbox");
    }
}
